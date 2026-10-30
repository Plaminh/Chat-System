-- Java Chat System - fictional development data for Database V1
-- Development only. All people/addresses are fictional and all emails use example.test.
-- BCrypt hashes are fixtures only; no plaintext password is published or reusable.
-- Apply with psql --single-transaction -v ON_ERROR_STOP=1.

INSERT INTO users (
    user_id, username, password_hash, email, full_name, address,
    date_of_birth, gender, role, account_status, locked_at,
    password_changed_at, created_at, updated_at
) VALUES
    (1, 'admin_dev', '$2a$10$Ke96E.TGgMFMnqmMF093B.5GzwaVDziSsy6ndIy5YuoeJZA2.pVyO',
     'admin.dev@example.test', 'Development Admin', 'Fictional District 1',
     DATE '1988-01-15', 'other', 'admin', 'active', NULL,
     TIMESTAMPTZ '2026-01-01 01:00:00+00', TIMESTAMPTZ '2026-01-01 01:00:00+00', TIMESTAMPTZ '2026-01-01 01:00:00+00'),
    (2, 'alice_sample', '$2a$10$Ke96E.TGgMFMnqmMF093B.5GzwaVDziSsy6ndIy5YuoeJZA2.pVyO',
     'alice.sample@example.test', 'Alice Sample', 'Fictional District 2',
     DATE '1995-02-20', 'female', 'user', 'active', NULL,
     TIMESTAMPTZ '2026-01-05 02:00:00+00', TIMESTAMPTZ '2026-01-05 02:00:00+00', TIMESTAMPTZ '2026-01-05 02:00:00+00'),
    (3, 'bob_sample', '$2a$10$Ke96E.TGgMFMnqmMF093B.5GzwaVDziSsy6ndIy5YuoeJZA2.pVyO',
     'bob.sample@example.test', 'Bob Sample', 'Fictional District 3',
     DATE '1993-03-21', 'male', 'user', 'locked', TIMESTAMPTZ '2026-03-10 04:00:00+00',
     TIMESTAMPTZ '2026-01-08 03:00:00+00', TIMESTAMPTZ '2026-01-08 03:00:00+00', TIMESTAMPTZ '2026-03-10 04:00:00+00'),
    (4, 'carol_sample', '$2a$10$Ke96E.TGgMFMnqmMF093B.5GzwaVDziSsy6ndIy5YuoeJZA2.pVyO',
     'carol.sample@example.test', 'Carol Sample', 'Fictional District 4',
     DATE '1997-04-22', 'female', 'user', 'active', NULL,
     TIMESTAMPTZ '2026-02-01 04:00:00+00', TIMESTAMPTZ '2026-02-01 04:00:00+00', TIMESTAMPTZ '2026-02-01 04:00:00+00'),
    (5, 'dave_sample', '$2a$10$Ke96E.TGgMFMnqmMF093B.5GzwaVDziSsy6ndIy5YuoeJZA2.pVyO',
     'dave.sample@example.test', 'Dave Sample', 'Fictional District 5',
     DATE '1991-05-23', NULL, 'user', 'active', NULL,
     TIMESTAMPTZ '2026-02-14 05:00:00+00', TIMESTAMPTZ '2026-02-14 05:00:00+00', TIMESTAMPTZ '2026-02-14 05:00:00+00'),
    (6, 'erin_sample', '$2a$10$Ke96E.TGgMFMnqmMF093B.5GzwaVDziSsy6ndIy5YuoeJZA2.pVyO',
     'erin.sample@example.test', 'Erin Sample', 'Fictional District 6',
     DATE '1998-06-24', 'other', 'user', 'active', NULL,
     TIMESTAMPTZ '2026-03-01 06:00:00+00', TIMESTAMPTZ '2026-03-01 06:00:00+00', TIMESTAMPTZ '2026-03-01 06:00:00+00');

INSERT INTO login_history (login_history_id, user_id, logged_in_at, logged_out_at) VALUES
    (101, 1, TIMESTAMPTZ '2026-03-15 01:00:00+00', TIMESTAMPTZ '2026-03-15 02:00:00+00'),
    (102, 2, TIMESTAMPTZ '2026-03-15 02:10:00+00', TIMESTAMPTZ '2026-03-15 03:30:00+00'),
    (103, 2, TIMESTAMPTZ '2026-03-16 02:00:00+00', NULL),
    (104, 3, TIMESTAMPTZ '2026-03-09 03:00:00+00', TIMESTAMPTZ '2026-03-09 03:45:00+00'),
    (105, 4, TIMESTAMPTZ '2026-03-16 04:00:00+00', TIMESTAMPTZ '2026-03-16 05:00:00+00');

INSERT INTO friend_requests (
    friend_request_id, sender_user_id, receiver_user_id, status, created_at, responded_at
) VALUES
    (201, 2, 3, 'accepted', TIMESTAMPTZ '2026-01-10 00:00:00+00', TIMESTAMPTZ '2026-01-11 00:00:00+00'),
    (202, 4, 5, 'pending', TIMESTAMPTZ '2026-03-10 00:00:00+00', NULL),
    (203, 6, 2, 'declined', TIMESTAMPTZ '2026-03-05 00:00:00+00', TIMESTAMPTZ '2026-03-06 00:00:00+00'),
    (204, 5, 3, 'accepted', TIMESTAMPTZ '2026-02-18 00:00:00+00', TIMESTAMPTZ '2026-02-19 00:00:00+00');

INSERT INTO friendships (
    friendship_id, user_low_id, user_high_id, established_by_request_id, created_at
) VALUES
    (301, 2, 3, 201, TIMESTAMPTZ '2026-01-11 00:00:00+00'),
    (302, 2, 4, NULL, TIMESTAMPTZ '2026-02-10 00:00:00+00'),
    (303, 3, 5, 204, TIMESTAMPTZ '2026-02-19 00:00:00+00');

INSERT INTO user_blocks (user_block_id, blocker_user_id, blocked_user_id, created_at) VALUES
    (401, 5, 6, TIMESTAMPTZ '2026-03-12 00:00:00+00');

INSERT INTO conversations (
    conversation_id, conversation_type, name, owner_user_id, encryption_enabled, created_at, updated_at
) VALUES
    (501, 'direct', NULL, NULL, FALSE, TIMESTAMPTZ '2026-02-01 00:00:00+00', TIMESTAMPTZ '2026-03-16 02:30:00+00'),
    (502, 'direct', NULL, NULL, FALSE, TIMESTAMPTZ '2026-02-10 00:00:00+00', TIMESTAMPTZ '2026-03-16 04:30:00+00'),
    (503, 'group', 'Sample Study Group', 2, FALSE, TIMESTAMPTZ '2026-02-20 00:00:00+00', TIMESTAMPTZ '2026-03-16 05:00:00+00'),
    (504, 'group', 'Admin Review Group', 1, FALSE, TIMESTAMPTZ '2026-03-01 00:00:00+00', TIMESTAMPTZ '2026-03-15 02:00:00+00');

INSERT INTO direct_conversations (conversation_id, user_low_id, user_high_id) VALUES
    (501, 2, 3),
    (502, 2, 4);

INSERT INTO conversation_members (
    conversation_member_id, conversation_id, user_id, member_role, joined_at, left_at, history_cleared_at
) VALUES
    (601, 501, 2, 'member', TIMESTAMPTZ '2026-02-01 00:00:00+00', NULL, NULL),
    (602, 501, 3, 'member', TIMESTAMPTZ '2026-02-01 00:00:00+00', NULL, TIMESTAMPTZ '2026-03-01 00:00:00+00'),
    (603, 502, 2, 'member', TIMESTAMPTZ '2026-02-10 00:00:00+00', NULL, NULL),
    (604, 502, 4, 'member', TIMESTAMPTZ '2026-02-10 00:00:00+00', NULL, NULL),
    (605, 503, 2, 'admin', TIMESTAMPTZ '2026-02-20 00:00:00+00', NULL, NULL),
    (606, 503, 3, 'member', TIMESTAMPTZ '2026-02-20 00:00:00+00', NULL, NULL),
    (607, 503, 5, 'member', TIMESTAMPTZ '2026-02-21 00:00:00+00', NULL, NULL),
    (608, 504, 1, 'admin', TIMESTAMPTZ '2026-03-01 00:00:00+00', NULL, NULL),
    (609, 504, 4, 'member', TIMESTAMPTZ '2026-03-01 00:00:00+00', NULL, NULL),
    (610, 504, 6, 'member', TIMESTAMPTZ '2026-03-02 00:00:00+00', TIMESTAMPTZ '2026-03-14 00:00:00+00', NULL);

INSERT INTO messages (
    message_id, conversation_id, sender_user_id, body, created_at, unsent_at, unsent_by
) VALUES
    (701, 501, 2, 'Hello from the fictional direct-chat seed.', TIMESTAMPTZ '2026-03-15 02:15:00+00', NULL, NULL),
    (702, 501, 3, 'This sample reply is used for history search.', TIMESTAMPTZ '2026-03-15 02:16:00+00', NULL, NULL),
    (703, 502, 4, 'A second direct conversation for UI filtering.', TIMESTAMPTZ '2026-03-16 04:30:00+00', NULL, NULL),
    (704, 503, 2, 'Welcome to the fictional study group.', TIMESTAMPTZ '2026-03-15 05:00:00+00', NULL, NULL),
    (705, 503, 5, 'This fictional message is referenced by a report.', TIMESTAMPTZ '2026-03-15 05:05:00+00', NULL, NULL),
    (706, 503, 2, '[Message unsent]', TIMESTAMPTZ '2026-03-15 05:10:00+00', TIMESTAMPTZ '2026-03-15 05:11:00+00', 2),
    (707, 504, 1, 'Administrative sample group message.', TIMESTAMPTZ '2026-03-15 01:30:00+00', NULL, NULL);

INSERT INTO message_deletions (message_deletion_id, message_id, user_id, deleted_at) VALUES
    (801, 702, 2, TIMESTAMPTZ '2026-03-16 00:00:00+00'),
    (802, 705, 3, TIMESTAMPTZ '2026-03-16 00:05:00+00');

INSERT INTO spam_reports (
    spam_report_id, reporter_user_id, reported_user_id, message_id, reason,
    review_status, reviewed_by, resolution_note, created_at, resolved_at
) VALUES
    (901, 3, 5, 705, 'Repeated fictional unsolicited messages in the sample group.',
     'resolved', 1, 'Reviewed as development-only sample data.',
     TIMESTAMPTZ '2026-03-15 06:00:00+00', TIMESTAMPTZ '2026-03-16 01:00:00+00'),
    (902, 4, 6, NULL, 'Fictional pending report without message evidence.',
     'pending', NULL, NULL, TIMESTAMPTZ '2026-03-16 06:00:00+00', NULL);

INSERT INTO user_activity_logs (
    user_activity_log_id, user_id, activity_type, conversation_id, occurred_at
) VALUES
    (1001, 1, 'app_open', NULL, TIMESTAMPTZ '2026-03-15 01:00:00+00'),
    (1002, 2, 'app_open', NULL, TIMESTAMPTZ '2026-03-15 02:00:00+00'),
    (1003, 2, 'direct_chat', 501, TIMESTAMPTZ '2026-03-15 02:15:00+00'),
    (1004, 3, 'direct_chat', 501, TIMESTAMPTZ '2026-03-15 02:16:00+00'),
    (1005, 4, 'app_open', NULL, TIMESTAMPTZ '2026-03-16 04:00:00+00'),
    (1006, 4, 'direct_chat', 502, TIMESTAMPTZ '2026-03-16 04:30:00+00'),
    (1007, 2, 'group_chat', 503, TIMESTAMPTZ '2026-03-15 05:00:00+00'),
    (1008, 5, 'group_chat', 503, TIMESTAMPTZ '2026-03-15 05:05:00+00'),
    (1009, 1, 'group_chat', 504, TIMESTAMPTZ '2026-03-15 01:30:00+00'),
    (1010, 6, 'app_open', NULL, TIMESTAMPTZ '2026-03-14 06:00:00+00');

-- Explicit fixture IDs do not advance identity sequences automatically.
SELECT setval(pg_get_serial_sequence('users', 'user_id'), (SELECT max(user_id) FROM users));
SELECT setval(pg_get_serial_sequence('login_history', 'login_history_id'), (SELECT max(login_history_id) FROM login_history));
SELECT setval(pg_get_serial_sequence('friend_requests', 'friend_request_id'), (SELECT max(friend_request_id) FROM friend_requests));
SELECT setval(pg_get_serial_sequence('friendships', 'friendship_id'), (SELECT max(friendship_id) FROM friendships));
SELECT setval(pg_get_serial_sequence('user_blocks', 'user_block_id'), (SELECT max(user_block_id) FROM user_blocks));
SELECT setval(pg_get_serial_sequence('conversations', 'conversation_id'), (SELECT max(conversation_id) FROM conversations));
SELECT setval(pg_get_serial_sequence('conversation_members', 'conversation_member_id'), (SELECT max(conversation_member_id) FROM conversation_members));
SELECT setval(pg_get_serial_sequence('messages', 'message_id'), (SELECT max(message_id) FROM messages));
SELECT setval(pg_get_serial_sequence('message_deletions', 'message_deletion_id'), (SELECT max(message_deletion_id) FROM message_deletions));
SELECT setval(pg_get_serial_sequence('spam_reports', 'spam_report_id'), (SELECT max(spam_report_id) FROM spam_reports));
SELECT setval(pg_get_serial_sequence('user_activity_logs', 'user_activity_log_id'), (SELECT max(user_activity_log_id) FROM user_activity_logs));
