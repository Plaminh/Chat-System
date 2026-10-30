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
| User search | Case-insensitive username/full-name lookup. Exclude either direction of `user_blocks`: accounts blocked by the searching user and, critically, candidates whose `blocker_user_id` is their own ID and whose `blocked_user_id` is the searcher's ID. |
| Unfriend/block | Delete canonical `friendships` row or insert directed `user_blocks`; Version 2 reconciles incompatible states transactionally. |
| Personal conversation | `conversations(type=direct)` + `direct_conversations` + two `conversation_members`. UI `username/displayName` is derived from the other participant. |
| Group conversation | `conversations(type=group)` + `conversation_members`; group owner/admin guarantees and membership reactivation are Version 2 transaction rules. |
| Ordered/searchable message history | `messages` ordered by `(created_at, message_id)`; exclude rows hidden by a member's clear boundary or `message_deletions`. The GIN full-text index supports token/word search, not arbitrary substring matching; Version 2 uses an escaped case-insensitive `LIKE` query when the UI requires substrings. |
| Delete for me | Insert unique `(message_id, user_id)` into `message_deletions`. |
| Clear history | Set the current member's `conversation_members.history_cleared_at`; shared messages remain. |
| Unsend | In one Version 2 backend transaction, atomically set `messages.body = '[Message unsent]'`, `unsent_at`, and optional `unsent_by`. The original content is not retained in `messages.body`; `message_deletions` remains independent. |
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
7. Bidirectional block exclusion for user discovery.
8. Group membership reactivation using the single existing membership row.

## Blocking direction and search visibility

`user_blocks` is directed: `blocker_user_id` is the account that initiated the block and `blocked_user_id` is the account being blocked. If User A blocks User B, the row is `(blocker_user_id = A, blocked_user_id = B)`. User B must not discover User A. The existing specification also treats a block as incompatible with discovery in the other direction, so User A must not discover User B while the row exists.

The Version 2 backend must apply this rule on every user-search path; the client must not receive blocked candidates and filter them locally. Representative query shape, where `:searcher_id` is the current user and `:pattern` is an escaped case-insensitive search pattern:

```sql
SELECT u.user_id, u.username, u.full_name
FROM users AS u
WHERE u.user_id <> :searcher_id
  AND (lower(u.username) LIKE :pattern ESCAPE '\'
       OR lower(u.full_name) LIKE :pattern ESCAPE '\')
  AND NOT EXISTS (
      SELECT 1
      FROM user_blocks AS b
      WHERE (b.blocker_user_id = :searcher_id AND b.blocked_user_id = u.user_id)
         OR (b.blocker_user_id = u.user_id AND b.blocked_user_id = :searcher_id)
  );
```

## Message search limitation

`idx_messages_search` indexes `to_tsvector('simple', body)`. It supports lexeme/token search such as whole words, but it does not satisfy arbitrary substring behavior such as matching `lic` inside `Alice`. For a UI substring requirement, Version 2 should use `lower(body) LIKE '%' || <escaped lower term> || '%'` together with membership, clear-history, per-user deletion, and `unsent_at IS NULL` visibility filters. This may scan more rows; no `pg_trgm` extension is introduced in V1. Recalled bodies contain only the canonical placeholder, so original content cannot be found through normal message queries.

## Group rejoining

`UNIQUE(conversation_id, user_id)` intentionally permits only one membership row for a user in a conversation. Rejoining therefore reactivates that row in one Version 2 transaction instead of inserting another: set `joined_at` to the approved rejoin time, set `left_at = NULL`, reset `history_cleared_at` according to the rejoin visibility policy, and assign the authorized current role. This model does not preserve multiple join/leave intervals; the previous interval is overwritten. If complete membership episode history becomes a requirement, it needs a separately approved post-V1 design rather than a thirteenth V1 table.

These mappings define persistence compatibility; they do not authorize UI source changes in Database V1.
