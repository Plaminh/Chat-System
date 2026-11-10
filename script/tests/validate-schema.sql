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
          'user_blocks', 'user_public_keys', 'conversations',
          'direct_conversations', 'conversation_members',
          'conversation_key_versions', 'conversation_key_envelopes',
          'messages', 'message_deletions',
          'spam_reports', 'user_activity_logs'
      );

    IF actual_tables <> 15 THEN
        RAISE EXCEPTION 'Expected 15 required tables, found %', actual_tables;
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

INSERT INTO user_public_keys (
    user_public_key_id, user_id, key_version, public_key, created_at
) VALUES
    (1051, 1001, 1, decode(repeat('41', 32), 'hex'), TIMESTAMPTZ '2026-02-03 00:00:00+00'),
    (1052, 1002, 1, decode(repeat('42', 32), 'hex'), TIMESTAMPTZ '2026-02-03 00:01:00+00'),
    (1053, 1003, 1, decode(repeat('43', 32), 'hex'), TIMESTAMPTZ '2026-02-03 00:02:00+00');

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
    (2002, 'group', 'Validation Group', 1001, TRUE, TIMESTAMPTZ '2026-02-04 00:00:00+00', TIMESTAMPTZ '2026-02-04 00:00:00+00');

INSERT INTO direct_conversations (conversation_id, user_low_id, user_high_id)
VALUES (2001, 1002, 1003);

INSERT INTO conversation_members (
    conversation_member_id, conversation_id, user_id, member_role, joined_at, left_at, history_cleared_at
) VALUES
    (2101, 2001, 1002, 'member', TIMESTAMPTZ '2026-02-03 00:00:00+00', NULL, NULL),
    (2102, 2001, 1003, 'member', TIMESTAMPTZ '2026-02-03 00:00:00+00', NULL, TIMESTAMPTZ '2026-02-05 00:00:00+00'),
    (2103, 2002, 1001, 'admin', TIMESTAMPTZ '2026-02-04 00:00:00+00', NULL, NULL),
    (2104, 2002, 1002, 'member', TIMESTAMPTZ '2026-02-04 00:00:00+00', NULL, NULL);

INSERT INTO conversation_key_versions (
    conversation_key_version_id, conversation_id, key_version, encryption_algorithm, created_at
) VALUES (
    2201, 2002, 1, 'AES-256-GCM', TIMESTAMPTZ '2026-02-04 00:00:00+00'
);

INSERT INTO conversation_key_envelopes (
    conversation_key_envelope_id, conversation_key_version_id, recipient_public_key_id,
    envelope_algorithm, encapsulated_key, encrypted_group_key, created_at
) VALUES
    (2251, 2201, 1051, 'HPKE-BASE-X25519-HKDF-SHA256-AES-256-GCM',
     decode(repeat('51', 32), 'hex'), decode(repeat('61', 48), 'hex'),
     TIMESTAMPTZ '2026-02-04 00:01:00+00'),
    (2252, 2201, 1052, 'HPKE-BASE-X25519-HKDF-SHA256-AES-256-GCM',
     decode(repeat('52', 32), 'hex'), decode(repeat('62', 48), 'hex'),
     TIMESTAMPTZ '2026-02-04 00:02:00+00');

INSERT INTO messages (
    message_id, conversation_id, sender_user_id, body, content_encrypted, ciphertext,
    encryption_key_version, encryption_nonce, authentication_tag,
    created_at, unsent_at, unsent_by
)
VALUES
    (3001, 2001, 1002, 'Validation direct message', FALSE, NULL, NULL, NULL, NULL,
     TIMESTAMPTZ '2026-02-05 01:00:00+00', NULL, NULL),
    (3002, 2002, 1001, '[Message unsent]', TRUE, NULL, NULL, NULL, NULL,
     TIMESTAMPTZ '2026-02-05 02:00:00+00', TIMESTAMPTZ '2026-02-05 03:00:00+00', 1001),
    (3003, 2002, 1001, NULL, TRUE, decode('aabbccddeeff', 'hex'), 1,
     decode('00112233445566778899aabb', 'hex'),
     decode('00112233445566778899aabbccddeeff', 'hex'),
     TIMESTAMPTZ '2026-02-05 02:30:00+00', NULL, NULL),
    (3004, 2001, 1002, '[Message unsent]', FALSE, NULL, NULL, NULL, NULL,
     TIMESTAMPTZ '2026-02-05 03:00:00+00', TIMESTAMPTZ '2026-02-05 03:01:00+00', 1002);

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
    user_activity_log_id, user_id, activity_type, conversation_id,
    conversation_id_snapshot, counterpart_user_id_snapshot, occurred_at
) VALUES
    (5001, 1002, 'app_open', NULL, NULL, NULL, TIMESTAMPTZ '2026-02-05 00:00:00+00'),
    (5002, 1002, 'direct_chat', 2001, 2001, 1003, TIMESTAMPTZ '2026-02-05 01:00:00+00'),
    (5003, 1001, 'group_chat', 2002, 2002, NULL, TIMESTAMPTZ '2026-02-05 02:00:00+00');

DO $$
BEGIN
    IF (SELECT conversation_type FROM conversations WHERE conversation_id = 2001) <> 'direct'
       OR (SELECT count(*) FROM conversation_members WHERE conversation_id = 2001 AND left_at IS NULL) <> 2
       OR EXISTS (
           SELECT 1
           FROM conversation_members cm
           WHERE cm.conversation_id = 2001
             AND cm.user_id NOT IN (1002, 1003)
       ) THEN
        RAISE EXCEPTION 'Valid direct-conversation fixture does not match its pair and memberships';
    END IF;
    IF NOT EXISTS (
        SELECT 1
        FROM conversations c
        JOIN conversation_members cm
          ON cm.conversation_id = c.conversation_id
         AND cm.user_id = c.owner_user_id
        JOIN users u ON u.user_id = c.owner_user_id
        WHERE c.conversation_id = 2002
          AND cm.left_at IS NULL
          AND cm.member_role = 'admin'
          AND u.account_status = 'active'
    ) THEN
        RAISE EXCEPTION 'Valid group owner fixture is not an active administrator member';
    END IF;
    RAISE NOTICE 'PASS: valid direct membership and active group-owner/admin states accepted';
END
$$;

DO $$
DECLARE
    rejected_constraint TEXT;
BEGIN
    BEGIN
        INSERT INTO messages (
            conversation_id, sender_user_id, body, content_encrypted,
            created_at, unsent_at, unsent_by
        ) VALUES (
            2001, 1002, NULL, FALSE,
            TIMESTAMPTZ '2026-02-05 04:00:00+00',
            TIMESTAMPTZ '2026-02-05 04:01:00+00', 1002
        );
        RAISE EXCEPTION 'NULL recalled plaintext body was not rejected';
    EXCEPTION WHEN check_violation THEN
        GET STACKED DIAGNOSTICS rejected_constraint = CONSTRAINT_NAME;
        IF rejected_constraint <> 'chk_messages_content_state' THEN
            RAISE EXCEPTION 'NULL recalled plaintext body was rejected by unexpected constraint %', rejected_constraint;
        END IF;
        RAISE NOTICE 'PASS: NULL recalled plaintext body rejected by chk_messages_content_state';
    END;

    BEGIN
        INSERT INTO messages (
            conversation_id, sender_user_id, body, content_encrypted,
            created_at, unsent_at, unsent_by
        ) VALUES (
            2002, 1001, NULL, TRUE,
            TIMESTAMPTZ '2026-02-05 04:00:00+00',
            TIMESTAMPTZ '2026-02-05 04:01:00+00', 1001
        );
        RAISE EXCEPTION 'NULL recalled encrypted-group body was not rejected';
    EXCEPTION WHEN check_violation THEN
        GET STACKED DIAGNOSTICS rejected_constraint = CONSTRAINT_NAME;
        IF rejected_constraint <> 'chk_messages_content_state' THEN
            RAISE EXCEPTION 'NULL recalled encrypted-group body was rejected by unexpected constraint %', rejected_constraint;
        END IF;
        RAISE NOTICE 'PASS: NULL recalled encrypted-group body rejected by chk_messages_content_state';
    END;

    BEGIN
        INSERT INTO messages (
            conversation_id, sender_user_id, body, content_encrypted,
            created_at, unsent_at, unsent_by
        ) VALUES (
            2001, 1002, 'Not the canonical placeholder', FALSE,
            TIMESTAMPTZ '2026-02-05 04:00:00+00',
            TIMESTAMPTZ '2026-02-05 04:01:00+00', 1002
        );
        RAISE EXCEPTION 'Non-placeholder recalled body was not rejected';
    EXCEPTION WHEN check_violation THEN
        GET STACKED DIAGNOSTICS rejected_constraint = CONSTRAINT_NAME;
        IF rejected_constraint <> 'chk_messages_content_state' THEN
            RAISE EXCEPTION 'Non-placeholder recalled body was rejected by unexpected constraint %', rejected_constraint;
        END IF;
        RAISE NOTICE 'PASS: non-placeholder recalled body rejected by chk_messages_content_state';
    END;

    BEGIN
        UPDATE messages
        SET body = NULL,
            unsent_at = TIMESTAMPTZ '2026-02-05 04:01:00+00',
            unsent_by = 1002
        WHERE message_id = 3001;
        RAISE EXCEPTION 'UPDATE to a NULL recalled plaintext body was not rejected';
    EXCEPTION WHEN check_violation THEN
        GET STACKED DIAGNOSTICS rejected_constraint = CONSTRAINT_NAME;
        IF rejected_constraint <> 'chk_messages_content_state' THEN
            RAISE EXCEPTION 'NULL recalled UPDATE was rejected by unexpected constraint %', rejected_constraint;
        END IF;
        RAISE NOTICE 'PASS: UPDATE to NULL recalled body rejected by chk_messages_content_state';
    END;

    BEGIN
        UPDATE messages
        SET body = 'Not the canonical placeholder',
            ciphertext = NULL,
            encryption_key_version = NULL,
            encryption_nonce = NULL,
            authentication_tag = NULL,
            unsent_at = TIMESTAMPTZ '2026-02-05 04:01:00+00',
            unsent_by = 1001
        WHERE message_id = 3003;
        RAISE EXCEPTION 'UPDATE to a non-placeholder recalled encrypted body was not rejected';
    EXCEPTION WHEN check_violation THEN
        GET STACKED DIAGNOSTICS rejected_constraint = CONSTRAINT_NAME;
        IF rejected_constraint <> 'chk_messages_content_state' THEN
            RAISE EXCEPTION 'Non-placeholder recalled UPDATE was rejected by unexpected constraint %', rejected_constraint;
        END IF;
        RAISE NOTICE 'PASS: UPDATE to non-placeholder recalled body rejected by chk_messages_content_state';
    END;

    IF NOT EXISTS (
        SELECT 1 FROM messages
        WHERE message_id = 3004
          AND body = '[Message unsent]'
          AND unsent_at IS NOT NULL
    ) OR NOT EXISTS (
        SELECT 1 FROM messages
        WHERE message_id = 3002
          AND body = '[Message unsent]'
          AND unsent_at IS NOT NULL
    ) OR NOT EXISTS (
        SELECT 1 FROM messages
        WHERE message_id = 3001
          AND body = 'Validation direct message'
          AND unsent_at IS NULL
    ) OR NOT EXISTS (
        SELECT 1 FROM messages
        WHERE message_id = 3003
          AND body IS NULL
          AND ciphertext IS NOT NULL
          AND unsent_at IS NULL
    ) THEN
        RAISE EXCEPTION 'Valid ordinary or recalled message states were not preserved';
    END IF;
    RAISE NOTICE 'PASS: plaintext and recalled placeholder states remain valid with nonce safeguards';

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
            username, password_hash, email, full_name, address, date_of_birth
        ) VALUES (
            'different_user', '$2a$10$012345678901234567890u12345678901234567890123456789012',
            'ALICE.V1@EXAMPLE.TEST', 'Duplicate Email User', 'Sample Ward', DATE '1990-01-01'
        );
        RAISE EXCEPTION 'Case-insensitive email uniqueness was not enforced';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASS: case-insensitive email duplicate rejected';
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
        INSERT INTO user_public_keys (user_id, key_version, public_key)
        VALUES (1002, 2, decode(repeat('44', 32), 'hex'));
        RAISE EXCEPTION 'A second active public key for one user was not rejected';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASS: per-user model permits only one active public key';
    END;

    BEGIN
        UPDATE user_public_keys
        SET retired_at = TIMESTAMPTZ '2026-02-04 00:00:00+00'
        WHERE user_public_key_id = 1053;
        INSERT INTO user_public_keys (user_id, key_version, public_key)
        VALUES (1003, 2, decode('0011', 'hex'));
        RAISE EXCEPTION 'Malformed X25519 public-key storage was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: malformed recipient public key rejected';
    END;

    BEGIN
        INSERT INTO conversation_key_envelopes (
            conversation_key_version_id, recipient_public_key_id,
            encapsulated_key, encrypted_group_key
        ) VALUES (
            2201, 1052, decode(repeat('71', 32), 'hex'), decode(repeat('72', 48), 'hex')
        );
        RAISE EXCEPTION 'Duplicate group-key envelope was not rejected';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASS: duplicate envelope for an epoch and recipient key rejected';
    END;

    BEGIN
        INSERT INTO conversation_key_envelopes (
            conversation_key_version_id, recipient_public_key_id,
            encapsulated_key, encrypted_group_key
        ) VALUES (
            2201, 1053, decode('0011', 'hex'), decode(repeat('73', 48), 'hex')
        );
        RAISE EXCEPTION 'Malformed HPKE envelope was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: malformed HPKE envelope rejected';
    END;

    BEGIN
        INSERT INTO conversation_key_envelopes (
            conversation_key_version_id, recipient_public_key_id,
            encapsulated_key, encrypted_group_key
        ) VALUES (
            999999, 1053, decode(repeat('74', 32), 'hex'), decode(repeat('75', 48), 'hex')
        );
        RAISE EXCEPTION 'Envelope for an unknown group-key epoch was not rejected';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'PASS: envelope requires an existing group-key epoch';
    END;

    BEGIN
        INSERT INTO conversation_key_envelopes (
            conversation_key_version_id, recipient_public_key_id,
            encapsulated_key, encrypted_group_key
        ) VALUES (
            2201, 999999, decode(repeat('76', 32), 'hex'), decode(repeat('77', 48), 'hex')
        );
        RAISE EXCEPTION 'Envelope for an unknown recipient key was not rejected';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'PASS: envelope requires an existing recipient public key';
    END;

    UPDATE user_public_keys
    SET retired_at = TIMESTAMPTZ '2026-02-06 00:00:00+00'
    WHERE user_public_key_id = 1051;

    INSERT INTO user_public_keys (
        user_public_key_id, user_id, key_version, public_key, created_at
    ) VALUES (
        1054, 1001, 2, decode(repeat('45', 32), 'hex'),
        TIMESTAMPTZ '2026-02-06 00:00:01+00'
    );

    IF NOT EXISTS (
        SELECT 1
        FROM conversation_key_envelopes
        WHERE conversation_key_version_id = 2201
          AND recipient_public_key_id = 1051
    ) THEN
        RAISE EXCEPTION 'Retiring a public key removed its historical envelope';
    END IF;
    RAISE NOTICE 'PASS: public-key replacement retains historical envelopes';

    -- This epoch models independently generated group-key material after replacement.
    INSERT INTO conversation_key_versions (
        conversation_key_version_id, conversation_id, key_version, created_at
    ) VALUES (
        2202, 2002, 2, TIMESTAMPTZ '2026-02-06 00:00:02+00'
    );

    INSERT INTO conversation_key_envelopes (
        conversation_key_envelope_id, conversation_key_version_id, recipient_public_key_id,
        encapsulated_key, encrypted_group_key, created_at
    ) VALUES
        (2253, 2202, 1054, decode(repeat('78', 32), 'hex'), decode(repeat('79', 48), 'hex'),
         TIMESTAMPTZ '2026-02-06 00:00:03+00'),
        (2254, 2202, 1052, decode(repeat('7a', 32), 'hex'), decode(repeat('7b', 48), 'hex'),
         TIMESTAMPTZ '2026-02-06 00:00:04+00');

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
        INSERT INTO conversations (conversation_type, encryption_enabled)
        VALUES ('direct', TRUE);
        RAISE EXCEPTION 'Encrypted direct conversation was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: encryption is limited to group conversations';
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
        INSERT INTO messages (
            conversation_id, sender_user_id, content_encrypted, ciphertext, encryption_key_version,
            encryption_nonce, authentication_tag
        ) VALUES (
            2002, 1001, TRUE, decode('aabb', 'hex'), 1,
            decode('0011', 'hex'), decode('00112233445566778899aabbccddeeff', 'hex')
        );
        RAISE EXCEPTION 'Encrypted message with an invalid nonce was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: malformed encrypted message state rejected';
    END;

    BEGIN
        INSERT INTO messages (
            conversation_id, sender_user_id, content_encrypted, ciphertext, encryption_key_version,
            encryption_nonce, authentication_tag
        ) VALUES (
            2002, 1001, TRUE, decode('aabb', 'hex'), 99,
            decode('00112233445566778899aabb', 'hex'),
            decode('00112233445566778899aabbccddeeff', 'hex')
        );
        RAISE EXCEPTION 'Encrypted message with an unknown key version was not rejected';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'PASS: unknown conversation key version rejected';
    END;

    BEGIN
        INSERT INTO messages (
            conversation_id, sender_user_id, content_encrypted, ciphertext, encryption_key_version,
            encryption_nonce, authentication_tag
        ) VALUES (
            2002, 1001, TRUE, decode('c001', 'hex'), 1,
            decode('00112233445566778899aabb', 'hex'),
            decode('10112233445566778899aabbccddeeff', 'hex')
        );
        RAISE EXCEPTION 'Duplicate nonce in one conversation/key epoch was not rejected';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASS: duplicate nonce in one conversation/key epoch rejected';
    END;

    INSERT INTO messages (
        conversation_id, sender_user_id, content_encrypted, ciphertext, encryption_key_version,
        encryption_nonce, authentication_tag
    ) VALUES (
        2002, 1001, TRUE, decode('c002', 'hex'), 1,
        decode('102132435465768798a9bacb', 'hex'),
        decode('20112233445566778899aabbccddeeff', 'hex')
    );
    RAISE NOTICE 'PASS: distinct nonces in one conversation/key epoch accepted';

    INSERT INTO messages (
        conversation_id, sender_user_id, content_encrypted, ciphertext, encryption_key_version,
        encryption_nonce, authentication_tag
    ) VALUES (
        2002, 1001, TRUE, decode('c003', 'hex'), 2,
        decode('00112233445566778899aabb', 'hex'),
        decode('30112233445566778899aabbccddeeff', 'hex')
    );
    RAISE NOTICE 'PASS: same nonce value under an independently generated key epoch accepted';

    BEGIN
        INSERT INTO messages (conversation_id, sender_user_id, body)
        VALUES (2002, 1001, 'Plaintext must not enter an encrypted group');
        RAISE EXCEPTION 'Plaintext message in an encrypted group was not rejected';
    EXCEPTION WHEN foreign_key_violation THEN
        RAISE NOTICE 'PASS: encrypted group rejected plaintext message state';
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
        INSERT INTO user_activity_logs (user_id, activity_type, conversation_id)
        VALUES (1002, 'direct_chat', 2001);
        RAISE EXCEPTION 'Chat activity without a historical conversation snapshot was not rejected';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASS: chat activity requires a conversation snapshot';
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
          AND body IS DISTINCT FROM '[Message unsent]'
    ) THEN
        RAISE EXCEPTION 'A recalled message retained non-placeholder content';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM messages m
        JOIN conversations c USING (conversation_id)
        WHERE c.encryption_enabled
          AND m.unsent_at IS NULL
          AND m.body IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'An encrypted-group message retained plaintext body content';
    END IF;
    RAISE NOTICE 'PASS: recalled bodies are redacted and encrypted-group fixtures contain no plaintext';
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
       OR EXISTS (SELECT 1 FROM message_deletions WHERE message_id = 3001) THEN
        RAISE EXCEPTION 'Conversation child rows did not cascade';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM user_activity_logs
        WHERE user_activity_log_id = 5002
          AND activity_type = 'direct_chat'
          AND conversation_id IS NULL
          AND conversation_id_snapshot = 2001
          AND counterpart_user_id_snapshot = 1003
    ) THEN
        RAISE EXCEPTION 'Conversation deletion did not retain and de-identify activity history';
    END IF;
    IF (SELECT message_id FROM spam_reports WHERE spam_report_id = 4001) IS NOT NULL THEN
        RAISE EXCEPTION 'Report message evidence was not set null after message cascade';
    END IF;
    RAISE NOTICE 'PASS: conversation child cleanup and activity/report retention verified';
END
$$;

DELETE FROM conversations WHERE conversation_id = 2002;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM conversation_members WHERE conversation_id = 2002)
       OR EXISTS (SELECT 1 FROM messages WHERE conversation_id = 2002)
       OR EXISTS (SELECT 1 FROM conversation_key_versions WHERE conversation_id = 2002)
       OR EXISTS (
           SELECT 1
           FROM conversation_key_envelopes
           WHERE conversation_key_version_id = 2201
       ) THEN
        RAISE EXCEPTION 'Encrypted-group key or message rows did not cascade';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM user_activity_logs
        WHERE user_activity_log_id = 5003
          AND activity_type = 'group_chat'
          AND conversation_id IS NULL
          AND conversation_id_snapshot = 2002
    ) THEN
        RAISE EXCEPTION 'Encrypted-group deletion did not retain activity history';
    END IF;
    RAISE NOTICE 'PASS: encrypted-group deletion removed epochs/envelopes and retained activity history';
END
$$;

ROLLBACK;

\echo 'Database V1 runtime schema validation passed.'
