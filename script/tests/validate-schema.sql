\set ON_ERROR_STOP on

BEGIN;

DO $$
DECLARE
    actual_tables INTEGER;
BEGIN
    SELECT count(*)
    INTO actual_tables
    FROM pg_catalog.pg_tables
    WHERE schemaname = current_schema()
      AND tablename IN (
          'users', 'login_history', 'friend_requests', 'friendships',
          'user_blocks', 'conversations', 'direct_conversations',
          'conversation_members', 'messages', 'message_deletions',
          'spam_reports', 'user_activity_logs'
      );

    IF actual_tables <> 12 THEN
        RAISE EXCEPTION 'Expected 12 required tables, found %', actual_tables;
    END IF;
END
$$;

INSERT INTO users (
    user_id, username, password_hash, email, full_name, address,
    date_of_birth, gender, role, account_status, locked_at,
    password_changed_at, created_at, updated_at
) VALUES
    (1001, 'admin_v1', '$2a$10$012345678901234567890u12345678901234567890123456789012',
     'admin.v1@example.test', 'Database Admin', 'Fictional District',
     DATE '1990-01-01', 'other', 'admin', 'active', NULL,
     TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00',
     TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    (1002, 'alice_v1', '$2a$10$012345678901234567890u12345678901234567890123456789012',
     'alice.v1@example.test', 'Alice Example', 'Sample Ward',
     DATE '1995-02-02', 'female', 'user', 'active', NULL,
     TIMESTAMPTZ '2026-01-02 00:00:00+00', TIMESTAMPTZ '2026-01-02 00:00:00+00',
     TIMESTAMPTZ '2026-01-02 00:00:00+00'),
    (1003, 'bob_v1', '$2a$10$012345678901234567890u12345678901234567890123456789012',
     'bob.v1@example.test', 'Bob Example', 'Sample Ward',
     DATE '1994-03-03', 'male', 'user', 'locked', TIMESTAMPTZ '2026-02-01 00:00:00+00',
     TIMESTAMPTZ '2026-01-03 00:00:00+00', TIMESTAMPTZ '2026-01-03 00:00:00+00',
     TIMESTAMPTZ '2026-02-01 00:00:00+00');

INSERT INTO login_history (login_history_id, user_id, logged_in_at, logged_out_at)
VALUES (1101, 1002, TIMESTAMPTZ '2026-02-01 08:00:00+00', TIMESTAMPTZ '2026-02-01 09:00:00+00');

INSERT INTO friend_requests (
    friend_request_id, sender_user_id, receiver_user_id, status, created_at, responded_at
) VALUES
    (1201, 1002, 1003, 'accepted', TIMESTAMPTZ '2026-01-10 00:00:00+00', TIMESTAMPTZ '2026-01-11 00:00:00+00'),
    (1202, 1001, 1002, 'pending', TIMESTAMPTZ '2026-02-01 00:00:00+00', NULL);

INSERT INTO friendships (friendship_id, user_low_id, user_high_id, established_by_request_id, created_at)
VALUES (1301, 1002, 1003, 1201, TIMESTAMPTZ '2026-01-11 00:00:00+00');

INSERT INTO user_blocks (user_block_id, blocker_user_id, blocked_user_id, created_at)
VALUES (1401, 1003, 1001, TIMESTAMPTZ '2026-02-02 00:00:00+00');

INSERT INTO conversations (
    conversation_id, conversation_type, name, owner_user_id, encryption_enabled, created_at, updated_at
) VALUES
    (2001, 'direct', NULL, NULL, FALSE, TIMESTAMPTZ '2026-02-03 00:00:00+00', TIMESTAMPTZ '2026-02-03 00:00:00+00'),
    (2002, 'group', 'Validation Group', 1001, FALSE, TIMESTAMPTZ '2026-02-04 00:00:00+00', TIMESTAMPTZ '2026-02-04 00:00:00+00');

INSERT INTO direct_conversations (conversation_id, user_low_id, user_high_id)
VALUES (2001, 1002, 1003);

INSERT INTO conversation_members (
    conversation_member_id, conversation_id, user_id, member_role, joined_at, left_at, history_cleared_at
) VALUES
    (2101, 2001, 1002, 'member', TIMESTAMPTZ '2026-02-03 00:00:00+00', NULL, NULL),
    (2102, 2001, 1003, 'member', TIMESTAMPTZ '2026-02-03 00:00:00+00', NULL, TIMESTAMPTZ '2026-02-05 00:00:00+00'),
    (2103, 2002, 1001, 'admin', TIMESTAMPTZ '2026-02-04 00:00:00+00', NULL, NULL),
    (2104, 2002, 1002, 'member', TIMESTAMPTZ '2026-02-04 00:00:00+00', NULL, NULL);

INSERT INTO messages (message_id, conversation_id, sender_user_id, body, created_at, unsent_at, unsent_by)
VALUES
    (3001, 2001, 1002, 'Validation direct message', TIMESTAMPTZ '2026-02-05 01:00:00+00', NULL, NULL),
    (3002, 2002, 1001, '[Message unsent]', TIMESTAMPTZ '2026-02-05 02:00:00+00', TIMESTAMPTZ '2026-02-05 03:00:00+00', 1001);

INSERT INTO message_deletions (message_deletion_id, message_id, user_id, deleted_at)
VALUES (3101, 3001, 1003, TIMESTAMPTZ '2026-02-06 00:00:00+00');

INSERT INTO spam_reports (
    spam_report_id, reporter_user_id, reported_user_id, message_id, reason,
    review_status, reviewed_by, resolution_note, created_at, resolved_at
) VALUES (
    4001, 1003, 1002, 3001, 'Fictional validation report',
    'resolved', 1001, 'Reviewed in isolated test transaction',
    TIMESTAMPTZ '2026-02-06 00:00:00+00', TIMESTAMPTZ '2026-02-07 00:00:00+00'
);

INSERT INTO user_activity_logs (
    user_activity_log_id, user_id, activity_type, conversation_id, occurred_at
) VALUES
    (5001, 1002, 'app_open', NULL, TIMESTAMPTZ '2026-02-05 00:00:00+00'),
    (5002, 1002, 'direct_chat', 2001, TIMESTAMPTZ '2026-02-05 01:00:00+00'),
    (5003, 1001, 'group_chat', 2002, TIMESTAMPTZ '2026-02-05 02:00:00+00');

DO $$
BEGIN
    BEGIN
        INSERT INTO users (
            username, password_hash, email, full_name, address, date_of_birth
        ) VALUES (
            'ALICE_V1', '$2a$10$012345678901234567890u12345678901234567890123456789012',
            'different@example.test', 'Duplicate User', 'Sample Ward', DATE '1990-01-01'
        );
        RAISE EXCEPTION 'Case-insensitive username uniqueness was not enforced';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASS: case-insensitive username duplicate rejected';
    END;

    BEGIN
        INSERT INTO users (
            username, password_hash, email, full_name, address, date_of_birth,
            account_status
        ) VALUES (
            'deleted_without_time',
            '$2a$10$012345678901234567890u12345678901234567890123456789012',
            'deleted.without.time@example.test', 'Invalid Deleted User',
            'Sample Ward', DATE '1990-01-01', 'deleted'
        );
        RAISE EXCEPTION 'Deleted account without deleted_at was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: deleted account requires deleted_at';
    END;

    BEGIN
        UPDATE users
        SET deleted_at = CURRENT_TIMESTAMP
        WHERE user_id = 1002;
        RAISE EXCEPTION 'Active account with deleted_at was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: active account rejects lifecycle timestamps';
    END;

    BEGIN
        UPDATE users
        SET deleted_at = CURRENT_TIMESTAMP
        WHERE user_id = 1003;
        RAISE EXCEPTION 'Locked account with deleted_at was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: locked account rejects deleted_at';
    END;

    BEGIN
        INSERT INTO friend_requests (sender_user_id, receiver_user_id)
        VALUES (1002, 1002);
        RAISE EXCEPTION 'Self friend request was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: self friend request rejected';
    END;

    BEGIN
        INSERT INTO friend_requests (sender_user_id, receiver_user_id)
        VALUES (1002, 1001);
        RAISE EXCEPTION 'Reverse pending friend request was not rejected';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASS: reverse pending friend request rejected';
    END;

    BEGIN
        INSERT INTO friendships (user_low_id, user_high_id)
        VALUES (1003, 1002);
        RAISE EXCEPTION 'Non-canonical friendship was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: non-canonical friendship rejected';
    END;

    BEGIN
        INSERT INTO user_blocks (blocker_user_id, blocked_user_id)
        VALUES (1002, 1002);
        RAISE EXCEPTION 'Self block was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: self block rejected';
    END;

    BEGIN
        INSERT INTO conversations (conversation_type, name, owner_user_id)
        VALUES ('direct', 'Invalid direct name', NULL);
        RAISE EXCEPTION 'Invalid direct conversation shape was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: invalid direct conversation shape rejected';
    END;

    BEGIN
        INSERT INTO conversation_members (
            conversation_id, user_id, joined_at, left_at
        ) VALUES (
            2002, 1003, TIMESTAMPTZ '2026-02-10 00:00:00+00', TIMESTAMPTZ '2026-02-09 00:00:00+00'
        );
        RAISE EXCEPTION 'Membership ending before join was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: invalid membership interval rejected';
    END;

    BEGIN
        INSERT INTO messages (conversation_id, sender_user_id, body)
        VALUES (2001, 1002, '   ');
        RAISE EXCEPTION 'Blank message body was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: blank message rejected';
    END;

    BEGIN
        INSERT INTO messages (
            conversation_id, sender_user_id, body, unsent_by
        ) VALUES (2001, 1002, '[Message unsent]', 1002);
        RAISE EXCEPTION 'Recall actor without recall time was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: invalid recall state rejected';
    END;

    BEGIN
        INSERT INTO messages (
            conversation_id, sender_user_id, body, created_at, unsent_at, unsent_by
        ) VALUES (
            2001, 1002, 'Original recalled content must not remain',
            TIMESTAMPTZ '2026-02-05 04:00:00+00', TIMESTAMPTZ '2026-02-05 04:01:00+00', 1002
        );
        RAISE EXCEPTION 'Recalled message retaining original content was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: recalled message content must use canonical placeholder';
    END;

    BEGIN
        INSERT INTO message_deletions (message_id, user_id)
        VALUES (3001, 1003);
        RAISE EXCEPTION 'Duplicate message deletion was not rejected';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASS: duplicate message deletion rejected';
    END;

    BEGIN
        INSERT INTO spam_reports (
            reporter_user_id, reported_user_id, reason, review_status
        ) VALUES (1002, 1003, 'Invalid resolved report', 'resolved');
        RAISE EXCEPTION 'Resolved report without resolution time was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: invalid report resolution state rejected';
    END;

    BEGIN
        INSERT INTO user_activity_logs (user_id, activity_type, conversation_id)
        VALUES (1002, 'app_open', 2001);
        RAISE EXCEPTION 'App-open event with conversation was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: invalid activity context rejected';
    END;

    BEGIN
        INSERT INTO login_history (user_id)
        VALUES (999999);
        RAISE EXCEPTION 'Unknown login-history user was not rejected';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'PASS: invalid foreign key rejected';
    END;

    BEGIN
        DELETE FROM users WHERE user_id = 1001;
        RAISE EXCEPTION 'Group owner deletion was not restricted';
    EXCEPTION WHEN foreign_key_violation OR restrict_violation THEN
        RAISE NOTICE 'PASS: group owner deletion restricted';
    END;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM messages
        WHERE unsent_at IS NOT NULL
          AND body <> '[Message unsent]'
    ) THEN
        RAISE EXCEPTION 'A recalled message retained non-placeholder content';
    END IF;
    RAISE NOTICE 'PASS: recalled message bodies are redacted';
END
$$;

INSERT INTO users (
    user_id, username, password_hash, email, full_name, address, date_of_birth,
    account_status, locked_at, password_changed_at, created_at, updated_at
) VALUES
    (1010, 'active_delete_test',
     '$2a$10$012345678901234567890u12345678901234567890123456789012',
     'active.delete@example.test', 'Active Delete Test', 'Sample Ward', DATE '1992-04-04',
     'active', NULL, TIMESTAMPTZ '2026-02-01 00:00:00+00',
     TIMESTAMPTZ '2026-02-01 00:00:00+00', TIMESTAMPTZ '2026-02-01 00:00:00+00'),
    (1011, 'locked_delete_test',
     '$2a$10$012345678901234567890u12345678901234567890123456789012',
     'locked.delete@example.test', 'Locked Delete Test', 'Sample Ward', DATE '1992-05-05',
     'locked', TIMESTAMPTZ '2026-02-02 00:00:00+00',
     TIMESTAMPTZ '2026-02-01 00:00:00+00', TIMESTAMPTZ '2026-02-01 00:00:00+00',
     TIMESTAMPTZ '2026-02-02 00:00:00+00'),
    (1012, 'replacement_admin',
     '$2a$10$012345678901234567890u12345678901234567890123456789012',
     'replacement.admin@example.test', 'Replacement Admin', 'Sample Ward', DATE '1992-06-06',
     'active', NULL, TIMESTAMPTZ '2026-02-01 00:00:00+00',
     TIMESTAMPTZ '2026-02-01 00:00:00+00', TIMESTAMPTZ '2026-02-01 00:00:00+00'),
    (1013, 'cancel_delete_test',
     '$2a$10$012345678901234567890u12345678901234567890123456789012',
     'cancel.delete@example.test', 'Cancel Delete Test', 'Sample Ward', DATE '1992-07-07',
     'active', NULL, TIMESTAMPTZ '2026-02-01 00:00:00+00',
     TIMESTAMPTZ '2026-02-01 00:00:00+00', TIMESTAMPTZ '2026-02-01 00:00:00+00');

INSERT INTO login_history (login_history_id, user_id, logged_in_at)
VALUES (1110, 1010, TIMESTAMPTZ '2026-02-03 00:00:00+00');

INSERT INTO conversations (
    conversation_id, conversation_type, name, owner_user_id, created_at, updated_at
) VALUES
    (2010, 'direct', NULL, NULL, TIMESTAMPTZ '2026-02-03 00:00:00+00',
     TIMESTAMPTZ '2026-02-03 00:00:00+00'),
    (2011, 'group', 'Transfer Validation Group', 1010,
     TIMESTAMPTZ '2026-02-04 00:00:00+00', TIMESTAMPTZ '2026-02-04 00:00:00+00'),
    (2012, 'group', 'Cancellation Validation Group', 1013,
     TIMESTAMPTZ '2026-02-04 00:00:00+00', TIMESTAMPTZ '2026-02-04 00:00:00+00'),
    (2013, 'group', 'Last Member Validation Group', 1013,
     TIMESTAMPTZ '2026-02-04 00:00:00+00', TIMESTAMPTZ '2026-02-04 00:00:00+00');

INSERT INTO direct_conversations (conversation_id, user_low_id, user_high_id)
VALUES (2010, 1010, 1012);

INSERT INTO conversation_members (
    conversation_member_id, conversation_id, user_id, member_role, joined_at
) VALUES
    (2110, 2010, 1010, 'member', TIMESTAMPTZ '2026-02-03 00:00:00+00'),
    (2111, 2010, 1012, 'member', TIMESTAMPTZ '2026-02-03 00:00:00+00'),
    (2112, 2011, 1010, 'admin', TIMESTAMPTZ '2026-02-04 00:00:00+00'),
    (2113, 2011, 1012, 'admin', TIMESTAMPTZ '2026-02-04 00:00:00+00'),
    (2114, 2012, 1013, 'admin', TIMESTAMPTZ '2026-02-04 00:00:00+00'),
    (2115, 2012, 1012, 'member', TIMESTAMPTZ '2026-02-04 00:00:00+00'),
    (2116, 2013, 1013, 'admin', TIMESTAMPTZ '2026-02-04 00:00:00+00');

INSERT INTO messages (message_id, conversation_id, sender_user_id, body, created_at)
VALUES
    (3010, 2010, 1010, 'History remains visible', TIMESTAMPTZ '2026-02-05 01:00:00+00'),
    (3011, 2010, 1010, 'Hidden only for replacement user', TIMESTAMPTZ '2026-02-05 02:00:00+00'),
    (3012, 2011, 1010, 'Group history keeps deleted sender', TIMESTAMPTZ '2026-02-05 03:00:00+00');

INSERT INTO message_deletions (message_deletion_id, message_id, user_id, deleted_at)
VALUES (3110, 3011, 1012, TIMESTAMPTZ '2026-02-06 00:00:00+00');

-- A failed owner-selection/last-group decision is represented by a subtransaction. Earlier
-- tentative changes roll back together, which is the required shape for the future server service.
DO $$
BEGIN
    BEGIN
        UPDATE conversations
        SET owner_user_id = 1012, updated_at = CURRENT_TIMESTAMP
        WHERE conversation_id = 2012;
        UPDATE conversation_members
        SET left_at = CURRENT_TIMESTAMP
        WHERE conversation_id = 2012 AND user_id = 1013;

        RAISE EXCEPTION USING
            ERRCODE = '23514',
            MESSAGE = 'Administrator canceled deletion of the final-member group';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: canceled group decisions rolled back as one unit';
    END;

    IF (SELECT owner_user_id FROM conversations WHERE conversation_id = 2012) <> 1013
       OR (SELECT left_at FROM conversation_members
           WHERE conversation_id = 2012 AND user_id = 1013) IS NOT NULL
       OR (SELECT account_status FROM users WHERE user_id = 1013) <> 'active'
       OR NOT EXISTS (SELECT 1 FROM conversations WHERE conversation_id = 2013) THEN
        RAISE EXCEPTION 'Canceled deletion left partial group/account changes';
    END IF;
END
$$;

-- Successful owner transfer, membership leave, and account tombstone are one transaction here.
UPDATE conversations
SET owner_user_id = 1012, updated_at = CURRENT_TIMESTAMP
WHERE conversation_id = 2011 AND owner_user_id = 1010;

UPDATE conversation_members
SET member_role = 'admin'
WHERE conversation_id = 2011 AND user_id = 1012 AND left_at IS NULL;

UPDATE conversation_members
SET left_at = CURRENT_TIMESTAMP
WHERE user_id = 1010 AND left_at IS NULL AND conversation_id IN (
    SELECT conversation_id FROM conversations WHERE conversation_type = 'group'
);

UPDATE users
SET account_status = 'deleted',
    locked_at = NULL,
    deleted_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = 1010 AND account_status IN ('active', 'locked');

UPDATE users
SET account_status = 'deleted',
    locked_at = NULL,
    deleted_at = CURRENT_TIMESTAMP,
    updated_at = CURRENT_TIMESTAMP
WHERE user_id = 1011 AND account_status IN ('active', 'locked');

DO $$
DECLARE
    visible_message_count INTEGER;
    hidden_message_count INTEGER;
    direct_sender_label TEXT;
    group_sender_label TEXT;
BEGIN
    IF (SELECT account_status FROM users WHERE user_id = 1010) <> 'deleted'
       OR (SELECT deleted_at FROM users WHERE user_id = 1010) IS NULL
       OR (SELECT locked_at FROM users WHERE user_id = 1010) IS NOT NULL THEN
        RAISE EXCEPTION 'Active account was not soft-deleted correctly';
    END IF;
    IF (SELECT account_status FROM users WHERE user_id = 1011) <> 'deleted'
       OR (SELECT deleted_at FROM users WHERE user_id = 1011) IS NULL
       OR (SELECT locked_at FROM users WHERE user_id = 1011) IS NOT NULL THEN
        RAISE EXCEPTION 'Locked account was not soft-deleted correctly';
    END IF;
    IF (SELECT owner_user_id FROM conversations WHERE conversation_id = 2011) <> 1012
       OR (SELECT member_role FROM conversation_members
           WHERE conversation_id = 2011 AND user_id = 1012) <> 'admin'
       OR (SELECT left_at FROM conversation_members
           WHERE conversation_id = 2011 AND user_id = 1010) IS NULL THEN
        RAISE EXCEPTION 'Group owner transfer or departing membership was incomplete';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM login_history WHERE login_history_id = 1110 AND user_id = 1010)
       OR NOT EXISTS (SELECT 1 FROM messages WHERE message_id = 3010 AND sender_user_id = 1010) THEN
        RAISE EXCEPTION 'Soft deletion removed account history or stable references';
    END IF;

    SELECT count(*)
    INTO visible_message_count
    FROM messages m
    WHERE m.conversation_id = 2010
      AND NOT EXISTS (
          SELECT 1
          FROM message_deletions md
          WHERE md.message_id = m.message_id AND md.user_id = 1012
      );
    SELECT count(*)
    INTO hidden_message_count
    FROM messages m
    WHERE m.conversation_id = 2010
      AND EXISTS (
          SELECT 1
          FROM message_deletions md
          WHERE md.message_id = m.message_id AND md.user_id = 1012
      );

    SELECT CASE WHEN u.account_status = 'deleted' THEN 'Tài khoản đã xóa' ELSE u.full_name END
    INTO direct_sender_label
    FROM messages m
    JOIN users u ON u.user_id = m.sender_user_id
    WHERE m.message_id = 3010;
    SELECT CASE WHEN u.account_status = 'deleted' THEN 'Tài khoản đã xóa' ELSE u.full_name END
    INTO group_sender_label
    FROM messages m
    JOIN users u ON u.user_id = m.sender_user_id
    WHERE m.message_id = 3012;

    IF visible_message_count <> 1 OR hidden_message_count <> 1 THEN
        RAISE EXCEPTION 'Per-user deletion visibility changed after sender deletion';
    END IF;
    IF direct_sender_label <> 'Tài khoản đã xóa'
       OR group_sender_label <> 'Tài khoản đã xóa' THEN
        RAISE EXCEPTION 'Deleted sender label was not applied to retained history';
    END IF;
    IF EXISTS (
        SELECT 1 FROM users
        WHERE user_id IN (1010, 1011) AND account_status <> 'deleted'
    ) THEN
        RAISE EXCEPTION 'Deleted users remained eligible for login/search/recovery predicates';
    END IF;

    RAISE NOTICE 'PASS: active/locked soft delete, group transfer, history retention, and per-user hiding verified';
END
$$;

DELETE FROM conversations WHERE conversation_id = 2001;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM direct_conversations WHERE conversation_id = 2001)
       OR EXISTS (SELECT 1 FROM conversation_members WHERE conversation_id = 2001)
       OR EXISTS (SELECT 1 FROM messages WHERE conversation_id = 2001)
       OR EXISTS (SELECT 1 FROM message_deletions WHERE message_id = 3001)
       OR EXISTS (SELECT 1 FROM user_activity_logs WHERE conversation_id = 2001) THEN
        RAISE EXCEPTION 'Conversation child rows did not cascade';
    END IF;
    IF (SELECT message_id FROM spam_reports WHERE spam_report_id = 4001) IS NOT NULL THEN
        RAISE EXCEPTION 'Report message evidence was not set null after message cascade';
    END IF;
    RAISE NOTICE 'PASS: conversation delete cascade/set-null actions verified';
END
$$;

ROLLBACK;

\echo 'Database V1 runtime schema validation passed.'
