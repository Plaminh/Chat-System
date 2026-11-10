# Admin and User UI Data Mapping — Database V1

## Source review

The current Admin/User Swing implementations, mock models, specifications, and server database configuration were inspected in this worktree. Database V1 does not change those interfaces or add live persistence access.

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
| Friends-of-friends count | Build the subject's canonical direct-neighbor set, traverse one more friendship edge, then `COUNT(DISTINCT candidate_id)` after excluding the subject and existing direct neighbors; never stored on `users`. |
| `FriendConnection.online` | Future presence/runtime policy; not a durable Database V1 boolean. `user_activity_logs` may support a last-activity hint but not live presence. |
| Group list and created date | `conversations` filtered to `group`; use `name`, `created_at`, and membership count. |
| Group member/admin roster | `conversation_members` joined to `users`; active rows have `left_at IS NULL`. |
| Spam reports | `spam_reports` joined three times to `users` for reporter, reported account, and reviewer. A referenced plaintext message may be readable; an encrypted message supplies metadata only because administrators have no decryption key. |
| Lock from spam panel | A Version 2 transaction updates `users.account_status/locked_at`; it separately updates report workflow if the reviewer chooses. |
| New registration list/chart | Filter/group `users.created_at` by requested date range/month. |
| Monthly active users chart | `COUNT(DISTINCT user_id)` over `user_activity_logs` by month, normally using `app_open` events. |
| App open count | Count `user_activity_logs` rows of type `app_open` in the selected period. |
| Chatted users count | Filter the subject's `direct_chat` events by half-open time range and `COUNT(DISTINCT counterpart_user_id_snapshot)`; the snapshot survives conversation deletion and differs from the actor. |
| Chatted groups count | Count distinct `conversation_id_snapshot` values from `group_chat` activity in the period. |

Account deletion is soft, so registration, login, friendship, and activity facts remain attributable to the stable `user_id`. Conversation deletion nulls only the live activity FK; immutable conversation/counterpart snapshots continue supporting historical app-open/direct/group totals, distinct groups, and distinct people chatted with.

## User UI mapping

| User view/model/action | Source columns or transaction effect |
|---|---|
| `UserProfile` | `users.username`, `full_name`, `email`, `address`, `date_of_birth`, `gender`. Avatar resources remain presentation-only. |
| Registration/sign-in/password change/recovery | `users` and `login_history` are the persistence targets, but all workflow, BCrypt handling, recovery tokens/email, and authorization are Version 2. Password changes do not rotate encryption keys; password reset cannot recover a lost client private key. |
| Friend list | Resolve the other endpoint from canonical `friendships`; join to `users`. Online/offline remains runtime state. |
| Incoming requests | `friend_requests.receiver_user_id` with `status = pending`; accept/decline records `responded_at`. |
| User search | Case-insensitive username/full-name lookup. Exclude either direction of `user_blocks`: accounts blocked by the searching user and, critically, candidates whose `blocker_user_id` is their own ID and whose `blocked_user_id` is the searcher's ID. |
| Unfriend/block | Delete canonical `friendships` row or insert directed `user_blocks`; Version 2 reconciles incompatible states transactionally. |
| Personal conversation | `conversations(type=direct)` + `direct_conversations` + two `conversation_members`. UI `username/displayName` is derived from the other participant. |
| Group conversation | `conversations(type=group)` + `conversation_members`. Creation selects plaintext or encrypted mode once; the group cannot switch later, so another group is required for another mode and history is never converted. Group owner/admin guarantees, mode immutability, and membership reactivation are Version 2 transaction rules. |
| Ordered/searchable message history | `messages` ordered by `(created_at, message_id)`; enforce membership interval/access, `history_cleared_at`, `message_deletions`, and unsend visibility. Plaintext rows use escaped case-insensitive `LIKE`; encrypted rows are decrypted and searched only on an authorized User Client. |
| Delete for me | Insert unique `(message_id, user_id)` into `message_deletions`. |
| Clear history | Set the current member's `conversation_members.history_cleared_at`; shared messages remain. |
| Unsend | In one Version 2 backend transaction, atomically set non-null `messages.body = '[Message unsent]'`, clear ciphertext/nonce/tag/key version, and set `unsent_at` plus optional `unsent_by`. This removes the server copy but cannot erase plaintext already received by another client; `message_deletions` remains independent. |
| Spam report | Insert `spam_reports` with reporter, reported account, reason, and optional message reference. For encrypted content the reference is metadata only; V1 stores no hidden plaintext evidence. Prior-interaction eligibility is checked in Version 2. |
| Writing assistance | No Database V1 entity. The server never automatically discloses encrypted-group plaintext to an LLM. A future client may send user-selected text only after an explicit action and documented provider disclosure/retention policy. |
| Encryption indicator | Display `conversations.encryption_enabled` as read-only status after creation, never as an edit toggle. For encrypted groups, `user_public_keys` stores per-user public recipient keys, `conversation_key_versions` identifies group-key epochs, `conversation_key_envelopes` stores one HPKE envelope per recipient key, and `messages` stores AES-GCM ciphertext metadata. Private keys and plaintext group keys stay on clients. |
| Public-key replacement | After V2 verifies and registers the higher key version, affected encrypted groups disable sending until an authorized member's User Client publishes a complete new epoch for the current recipient set. UI must not offer plaintext fallback. Old history remains available only where the matching old private key was preserved locally. |

## Cross-table queries to implement in Version 2

1. Canonical friendship lookup and two-hop friend statistics.
2. Direct-conversation create-or-get with pair and exactly two memberships in one transaction.
3. Message visibility combining membership interval, `history_cleared_at`, unsend state, and per-user deletion.
4. Report eligibility based on prior direct/group interaction.
5. Date-bounded distinct-user and distinct-conversation analytics without persisting calculated counters.
6. Account deletion workflow that first resolves restricted group ownership and direct-conversation pairs.
7. Bidirectional block exclusion for user discovery.
8. Group membership reactivation using the single existing membership row.
9. Authenticated public-key replacement, coordinated membership/recipient snapshots, client-generated group-key rotation, complete per-recipient envelope distribution, send pausing until completion, and client-side message encryption/decryption.

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

The assignment requires literal substring search, so the former `to_tsvector` index was removed: it only supports token search. For plaintext rows, the server must escape the user's literal term in this order—backslash to `\\`, `%` to `\%`, and `_` to `\_`—then lowercase it, surround it with `%`, and bind it as `:pattern`:

```sql
SELECT m.message_id, m.conversation_id, m.sender_user_id, m.body, m.created_at
FROM messages AS m
JOIN conversation_members AS cm
  ON cm.conversation_id = m.conversation_id
 AND cm.user_id = :viewer_id
WHERE m.body IS NOT NULL
  AND m.unsent_at IS NULL
  AND m.created_at >= cm.joined_at
  AND (cm.left_at IS NULL OR m.created_at <= cm.left_at)
  AND (cm.history_cleared_at IS NULL OR m.created_at > cm.history_cleared_at)
  AND NOT EXISTS (
      SELECT 1 FROM message_deletions AS md
      WHERE md.message_id = m.message_id AND md.user_id = :viewer_id
  )
  AND lower(m.body) LIKE :pattern ESCAPE '\';
```

No extension is required at the expected project scale. An optional future `pg_trgm` index would need an explicit deployment dependency and is not part of V1. For encrypted groups, PostgreSQL has no plaintext to search and no plaintext search index. The server returns authorized ciphertext/envelope data, and the User Client decrypts and performs literal substring matching locally. Recalled content is absent from server storage in both modes.

## Group rejoining

`UNIQUE(conversation_id, user_id)` intentionally permits only one membership row for a user in a conversation. Rejoining therefore reactivates that row in one Version 2 transaction instead of inserting another: set `joined_at` to the approved rejoin time, set `left_at = NULL`, clear `history_cleared_at`, and assign the authorized current role. The new `joined_at` is the visibility boundary, so the member does not regain earlier history. This model overwrites the prior interval; complete membership episode history needs a separately approved later design.

## Encrypted-group lifecycle

- V1 uses one active public encryption key per user, not per device. Independent multi-device history access is therefore not claimed.
- Group creation chooses the immutable mode. An encrypted group persists epoch 1 and envelopes for every active member before accepting encrypted messages; changing mode requires creating another group.
- Adding, removing, or rejoining a member rotates the key for future messages. New/rejoining members do not receive old envelopes by default; removed members cannot receive later epochs.
- Replacing a public key requires V2 authenticity verification, retires the old row, and pauses each affected encrypted group until a complete rotated epoch targets every current authorized member. Existing envelopes are not rewritten and remain readable only if the client retains the matching old private key.
- Logout/restart must preserve private keys in protected client-local storage. Password change is unrelated to these keys, and password reset or key loss does not restore encrypted history.
- The server's key directory is not authenticity proof. V2 must authenticate key registration, resist substitution, coordinate membership with replacement, check completeness, and persist rotation/envelopes atomically without generating or decrypting group keys.

The current UI exposes no conflicting mode toggle. `CreateGroupDialog.java` lines 35-36 and 131-167 currently collect only a name and participants; V2 must add the one-time mode choice there. `GroupChatPanel.java` lines 28-29 contain only the non-functional encryption placeholder; V2 must replace it with read-only mode/rotation status, not an editable toggle. No UI source is changed by Database V1.

These mappings define persistence compatibility; they do not authorize UI source changes in Database V1.
