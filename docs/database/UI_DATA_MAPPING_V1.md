# Admin and User UI Data Mapping — Database V1

## Source review

The current database branch does not contain the completed Admin/User UI implementations. Their models and specifications were inspected read-only from `origin/feature/admin-ui-v1` and `origin/feature/user-ui-v1`; no branch was switched or modified.

## Identifier and value normalization

| UI convention | Database representation |
|---|---|
| `USR-1001` | Presentation prefix plus numeric `users.user_id`; the prefix is not stored as a key. |
| `GRP-001` / `group-1` | Presentation/session value mapped to numeric `conversations.conversation_id`. |
| `REP-8041` | Presentation prefix plus numeric `spam_reports.spam_report_id`. |
| `Male`, `Female`, `Other`, `Not specified` | `male`, `female`, `other`, `NULL`. |
| `Active`, `Locked` | `users.account_status = active | locked`; `locked_at` carries the lock time. |
| Friend request `PENDING`, `ACCEPTED`, `DECLINED` | Lowercase states; database additionally supports `canceled`. |
| Group role `ADMIN`, `MEMBER` / boolean `groupAdmin` | `conversation_members.member_role = admin | member`. |
| UI report status `Pending`, `Account Locked`, `Dismissed` | `pending` or `dismissed` belongs to `spam_reports.review_status`; `Account Locked` maps separately to `users.account_status = locked`. |

## Admin UI mapping

| Admin view/model | Source columns or derivation |
|---|---|
| `AdminUser.id` | Format `users.user_id` for display, e.g. `USR-` plus a chosen padded number. |
| Username, name, email, address, birth date, gender | Directly from `users`; gender is converted to the UI label casing. |
| Created date and Active/Locked | `users.created_at`, `users.account_status`. |
| Password reset | Version 2 server updates `users.password_hash`, `password_changed_at`, and `updated_at`; the client never accesses the hash. |
| `LoginRecord` chronology | `login_history` joined to `users`, ordered by `logged_in_at DESC`; full name is not duplicated in history. |
| Direct friend count | Count `friendships` rows where the user is either `user_low_id` or `user_high_id`. |
| Friends-of-friends count | Derive from two-hop friendship joins with duplicates and the subject user excluded; never stored on `users`. |
| `FriendConnection.online` | Future presence/runtime policy; not a durable Database V1 boolean. `user_activity_logs` may support a last-activity hint but not live presence. |
| Group list and created date | `conversations` filtered to `group`; use `name`, `created_at`, and membership count. |
| Group member/admin roster | `conversation_members` joined to `users`; active rows have `left_at IS NULL`. |
| Spam reports | `spam_reports` joined three times to `users` for reporter, reported account, and reviewer; optional `messages.body` is evidence. |
| Lock from spam panel | A Version 2 transaction updates `users.account_status/locked_at`; it separately updates report workflow if the reviewer chooses. |
| New registration list/chart | Filter/group `users.created_at` by requested date range/month. |
| Monthly active users chart | `COUNT(DISTINCT user_id)` over `user_activity_logs` by month, normally using `app_open` events. |
| App open count | Count `user_activity_logs` rows of type `app_open` in the selected period. |
| Chatted users count | Count distinct other participants reached through direct-chat activity/conversation membership in the period; Version 2 query defines exact event grouping. |
| Chatted groups count | Count distinct group `conversation_id` values from `group_chat` activity in the period. |

## User UI mapping

| User view/model/action | Source columns or transaction effect |
|---|---|
| `UserProfile` | `users.username`, `full_name`, `email`, `address`, `date_of_birth`, `gender`. Avatar resources remain presentation-only. |
| Registration/sign-in/password change/recovery | `users` and `login_history` are the persistence targets, but all workflow, BCrypt handling, recovery tokens/email, and authorization are Version 2. |
| Friend list | Resolve the other endpoint from canonical `friendships`; join to `users`. Online/offline remains runtime state. |
| Incoming requests | `friend_requests.receiver_user_id` with `status = pending`; accept/decline records `responded_at`. |
| User search | Case-insensitive username/full-name lookup excluding accounts in `user_blocks` where the searching user is blocker. |
| Unfriend/block | Delete canonical `friendships` row or insert directed `user_blocks`; Version 2 reconciles incompatible states transactionally. |
| Personal conversation | `conversations(type=direct)` + `direct_conversations` + two `conversation_members`. UI `username/displayName` is derived from the other participant. |
| Group conversation | `conversations(type=group)` + `conversation_members`; group owner/admin guarantees are Version 2 transaction rules. |
| Ordered/searchable message history | `messages` ordered by `(created_at, message_id)`; exclude rows hidden by a member's clear boundary or `message_deletions`. Simple-text search is for plaintext V1 content only. |
| Delete for me | Insert unique `(message_id, user_id)` into `message_deletions`. |
| Clear history | Set the current member's `conversation_members.history_cleared_at`; shared messages remain. |
| Unsend | Set `messages.unsent_at` and optional `unsent_by`; all viewers render the recall placeholder while the stored body remains subject to backend access policy. |
| Spam report | Insert `spam_reports` with reporter, reported account, reason, and optional evidence message. Prior-interaction eligibility is checked in Version 2. |
| Writing assistance | No Database V1 entity; draft/provider behavior is Version 2 and must not store provider secrets in clients. |
| Encryption indicator | `conversations.encryption_enabled` is metadata only. No V1 keys, device records, ciphertext guarantees, or E2EE security claim exist. |

## Cross-table queries to implement in Version 2

1. Canonical friendship lookup and two-hop friend statistics.
2. Direct-conversation create-or-get with pair and exactly two memberships in one transaction.
3. Message visibility combining membership interval, `history_cleared_at`, unsend state, and per-user deletion.
4. Report eligibility based on prior direct/group interaction.
5. Date-bounded distinct-user and distinct-conversation analytics without persisting calculated counters.
6. Account deletion workflow that first resolves restricted group ownership and direct-conversation pairs.

These mappings define persistence compatibility; they do not authorize UI source changes in Database V1.
