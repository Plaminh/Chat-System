-- Database V1 migration: retain accounts and history after an administrator deletion.
-- Apply with psql -X -v ON_ERROR_STOP=1. This script owns its transaction and is rerunnable.
\set ON_ERROR_STOP on

BEGIN;

DO $$
DECLARE
    users_oid OID;
    public_table_count INTEGER;
    required_table_count INTEGER;
    has_deleted_at BOOLEAN;
    status_constraint TEXT;
    state_constraint TEXT;
BEGIN
    SELECT count(*)
    INTO public_table_count
    FROM pg_catalog.pg_tables
    WHERE schemaname = 'public';

    SELECT count(*)
    INTO required_table_count
    FROM pg_catalog.pg_tables
    WHERE schemaname = 'public'
      AND tablename IN (
          'users', 'login_history', 'friend_requests', 'friendships',
          'user_blocks', 'conversations', 'direct_conversations',
          'conversation_members', 'messages', 'message_deletions',
          'spam_reports', 'user_activity_logs'
      );

    IF public_table_count <> 12 OR required_table_count <> 12 THEN
        RAISE EXCEPTION
            'Unexpected Database V1 structure: expected exactly the 12 approved public tables, found % public and % required',
            public_table_count, required_table_count;
    END IF;

    users_oid := to_regclass('public.users');
    IF users_oid IS NULL THEN
        RAISE EXCEPTION 'Unexpected Database V1 structure: public.users is missing';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'account_status'
          AND data_type = 'character varying'
          AND character_maximum_length = 10
          AND is_nullable = 'NO'
    ) THEN
        RAISE EXCEPTION 'Unexpected users.account_status definition; expected VARCHAR(10) NOT NULL';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'locked_at'
          AND data_type = 'timestamp with time zone'
          AND is_nullable = 'YES'
    ) THEN
        RAISE EXCEPTION 'Unexpected users.locked_at definition; expected nullable TIMESTAMPTZ';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'updated_at'
          AND data_type = 'timestamp with time zone'
          AND is_nullable = 'NO'
    ) THEN
        RAISE EXCEPTION 'Unexpected users.updated_at definition; expected TIMESTAMPTZ NOT NULL';
    END IF;

    SELECT pg_get_constraintdef(oid)
    INTO status_constraint
    FROM pg_catalog.pg_constraint
    WHERE conrelid = users_oid
      AND conname = 'chk_users_account_status'
      AND contype = 'c';

    SELECT pg_get_constraintdef(oid)
    INTO state_constraint
    FROM pg_catalog.pg_constraint
    WHERE conrelid = users_oid
      AND conname = 'chk_users_lock_state'
      AND contype = 'c';

    IF status_constraint IS NULL OR state_constraint IS NULL THEN
        RAISE EXCEPTION
            'Unexpected users constraints: chk_users_account_status and chk_users_lock_state must both exist';
    END IF;

    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'deleted_at'
    )
    INTO has_deleted_at;

    IF NOT has_deleted_at THEN
        IF lower(status_constraint) NOT LIKE '%active%'
           OR lower(status_constraint) NOT LIKE '%locked%'
           OR lower(status_constraint) LIKE '%deleted%'
           OR lower(state_constraint) NOT LIKE '%account_status%'
           OR lower(state_constraint) NOT LIKE '%locked_at%'
           OR lower(state_constraint) LIKE '%deleted_at%' THEN
            RAISE EXCEPTION
                'Unexpected legacy account constraints; migration only accepts the original active/locked Database V1 rules';
        END IF;

        LOCK TABLE public.users IN ACCESS EXCLUSIVE MODE;

        ALTER TABLE public.users
            ADD COLUMN deleted_at TIMESTAMPTZ;
        ALTER TABLE public.users
            DROP CONSTRAINT chk_users_account_status,
            DROP CONSTRAINT chk_users_lock_state;
        ALTER TABLE public.users
            ADD CONSTRAINT chk_users_account_status
                CHECK (account_status IN ('active', 'locked', 'deleted')),
            ADD CONSTRAINT chk_users_lock_state
                CHECK (
                    (account_status = 'active' AND locked_at IS NULL AND deleted_at IS NULL)
                    OR (
                        account_status = 'locked'
                        AND locked_at IS NOT NULL
                        AND deleted_at IS NULL
                    )
                    OR (
                        account_status = 'deleted'
                        AND locked_at IS NULL
                        AND deleted_at IS NOT NULL
                    )
                );
    ELSE
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'users'
              AND column_name = 'deleted_at'
              AND data_type = 'timestamp with time zone'
              AND is_nullable = 'YES'
        ) THEN
            RAISE EXCEPTION 'Unexpected users.deleted_at definition; expected nullable TIMESTAMPTZ';
        END IF;

        IF lower(status_constraint) NOT LIKE '%active%'
           OR lower(status_constraint) NOT LIKE '%locked%'
           OR lower(status_constraint) NOT LIKE '%deleted%'
           OR lower(state_constraint) NOT LIKE '%account_status%'
           OR lower(state_constraint) NOT LIKE '%locked_at%'
           OR lower(state_constraint) NOT LIKE '%deleted_at%' THEN
            RAISE EXCEPTION
                'Unexpected migrated account constraints; refusing to accept a partially modified users table';
        END IF;
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public.users
        WHERE NOT (
            (account_status = 'active' AND locked_at IS NULL AND deleted_at IS NULL)
            OR (account_status = 'locked' AND locked_at IS NOT NULL AND deleted_at IS NULL)
            OR (account_status = 'deleted' AND locked_at IS NULL AND deleted_at IS NOT NULL)
        )
    ) THEN
        RAISE EXCEPTION 'Account lifecycle validation failed after migration';
    END IF;
END
$$;

COMMIT;

\echo 'Database V1 admin soft-delete migration is applied and validated.'
