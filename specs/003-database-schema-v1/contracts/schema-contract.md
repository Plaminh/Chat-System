# Database V1 Schema Contract

This contract defines the observable guarantees that `script/schema.sql`, validation fixtures, and future Version 2 server repositories must share.

## Object contract

The public V1 schema contains exactly these twelve base tables and no trigger-managed companion tables:

`users`, `login_history`, `friend_requests`, `friendships`, `user_blocks`, `conversations`, `direct_conversations`, `conversation_members`, `messages`, `message_deletions`, `spam_reports`, `user_activity_logs`.

## Identity contract

- Every independent record uses a numeric `BIGINT` key. `direct_conversations.conversation_id` is both its primary key and its parent reference.
- UI display IDs are formatted at the presentation/application boundary and are never accepted as database foreign keys.
- Usernames and email addresses are case-insensitively unique; display casing remains stored.
- Passwords are represented only as non-blank hashes. Seed hashes are development fixtures, never production credentials.

## Allowed-state contract

| Concept | Values |
|---|---|
| Gender | `male`, `female`, `other`, `NULL` |
| User role | `admin`, `user` |
| Account status | `active`, `locked` |
| Friend request | `pending`, `accepted`, `declined`, `canceled` |
| Conversation type | `direct`, `group` |
| Member role | `admin`, `member` |
| Report review | `pending`, `reviewing`, `resolved`, `dismissed` |
| Activity type | `app_open`, `direct_chat`, `group_chat` |

## Relationship contract

- Friendship and direct-conversation pairs are stored in strictly increasing numeric ID order and are unique.
- Friend requests, blocks, reports, and message deletions reject self/duplicate cases where enforceable within the row or unique key.
- Foreign keys use explicit delete behavior. Child-only history cascades when its parent cannot remain meaningful; moderation/message authorship uses `SET NULL` where retaining the historical record is valuable; direct-pair and group-owner references restrict account deletion until a Version 2 transaction resolves them.
- Conversation deletion cascades its specialization, membership, messages, message deletions, and activity records.

## Lifecycle contract

- A locked user has `locked_at`; an active user does not.
- A pending request has no response time; every final request has one.
- Logout, response, leave, clear, unsend, resolution, and update timestamps cannot precede their relevant creation/join timestamps.
- A direct conversation has no name/owner; a group has both a non-blank name and owner.
- `unsent_at` represents global recall. `message_deletions` represents delete-for-me. `conversation_members.history_cleared_at` represents clearing earlier history for one participant.
- Report review status never serves as account status and causes no implicit sanction.

## Query-support contract

Indexes MUST support:

1. Case-insensitive account lookup, account status, registration dates, and names.
2. User login chronology.
3. Incoming requests by status/date; unique pending unordered pairs.
4. Both sides of friendship and blocking relationships.
5. Conversation type/name/owner/date and user membership lists.
6. Conversation message chronology, sender history, and simple-text content search.
7. Per-user message deletion lookup.
8. Report status/date, reporter, reported account, reviewer, and message evidence.
9. User/type/conversation activity by time range for event and distinct-user statistics.

## Validation contract

`script/tests/validate-schema.ps1` MUST statically confirm table names/count, required constraints/indexes, unsafe construct absence, and artifact synchronization. `script/tests/validate-schema.sql` MUST run inside an explicitly isolated database and roll back its fixture data after proving representative valid and invalid cases.

Runtime tests MUST NOT connect to or modify a production or unknown database. Absence of PostgreSQL tooling or an explicitly supplied isolated database is reported as incomplete runtime verification, not as a pass.

## Compatibility contract

- The schema is PostgreSQL-native and consumable through Spring JDBC without JPA/Hibernate.
- Database access remains server-only. No client module receives a database dependency or credential.
- Schema changes after V1 require a reviewed contract change and migration strategy; clients must not infer physical column names as network protocol fields.
