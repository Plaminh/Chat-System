# Database V1 Schema Contract

This contract defines the observable guarantees that `script/schema.sql`, validation fixtures, and future Version 2 server repositories must share.

## Object contract

The V1 schema defines these fifteen required base tables and no trigger-managed companion tables:

`users`, `user_public_keys`, `login_history`, `friend_requests`, `friendships`, `user_blocks`, `conversations`, `direct_conversations`, `conversation_members`, `conversation_key_versions`, `conversation_key_envelopes`, `messages`, `message_deletions`, `spam_reports`, `user_activity_logs`.

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
| Account status | `active`, `locked`, `deleted` |
| Friend request | `pending`, `accepted`, `declined`, `canceled` |
| Conversation type | `direct`, `group` |
| Member role | `admin`, `member` |
| Report review | `pending`, `reviewing`, `resolved`, `dismissed` |
| Activity type | `app_open`, `direct_chat`, `group_chat` |

## Relationship contract

- Friendship and direct-conversation pairs are stored in strictly increasing numeric ID order and are unique.
- Friend requests, blocks, reports, and message deletions reject self/duplicate cases where enforceable within the row or unique key.
- Foreign keys use explicit delete behavior. Ordinary accounts are soft-deleted, so physical user cascades are not part of the account workflow. Moderation/message authorship uses `SET NULL` where retaining historical evidence is valuable; direct-pair and group-owner references restrict accidental hard deletion.
- Conversation deletion cascades its specialization, membership, group-key epochs/envelopes, messages, and message deletions. Activity rows remain with `conversation_id = NULL`; immutable conversation/counterpart snapshots preserve distinct historical group and people statistics.

## Lifecycle contract

- A locked user has `locked_at`; an active user has neither lifecycle timestamp; a deleted user has `deleted_at` and no `locked_at`.
- A pending request has no response time; every final request has one.
- Logout, response, leave, clear, unsend, resolution, and update timestamps cannot precede their relevant creation/join timestamps.
- A direct conversation has no name/owner; a group has both a non-blank name and owner.
- `unsent_at` represents global recall and requires a non-null `messages.body = '[Message unsent]'`; SQL `NULL` is invalid for every recalled message. The original body is not retained there. `message_deletions` represents delete-for-me. `conversation_members.history_cleared_at` represents clearing earlier history for one participant.
- A Version 2 recall operation changes `body`, `unsent_at`, and `unsent_by` atomically. It does not create or remove per-user deletion rows.
- Report review status never serves as account status and causes no implicit sanction.
- Direct conversations cannot enable group encryption. Group mode is selected at creation and is immutable by V2 server policy; changing mode requires another group and does not convert history. A composite foreign key requires `messages.content_encrypted` to equal the parent flag, but does not prevent every flag update, so SQL does not claim complete mode immutability. A normal message stores either non-blank plaintext or complete AES-256-GCM ciphertext metadata, never both.
- The recipient model is per-user, not per-device. Each user has at most one active versioned X25519 public key; private keys never enter PostgreSQL or server-accessible recovery storage.
- Client-generated group-key epochs are versioned per conversation. Each authorized recipient public key receives at most one RFC 9180 HPKE base-mode envelope per epoch using KEM `0x0020`, KDF `0x0001`, AEAD `0x0002`, and UTF-8 `info = JavaChatSystem-V1-GroupKey`. Canonical AAD is `JavaChatSystem-V1|conversation=<conversation_id>|epoch=<key_version>|recipient-user=<user_id>|recipient-key=<key_version>` with unsigned unpadded base-10 values derived through the envelope FKs. The server stores/forwards opaque envelopes and ciphertext but has no independent decryption key.
- Membership changes rotate the key for future messages. New/rejoining members receive no earlier epoch by default; retired public keys and old envelopes remain for clients that still possess the matching private key.
- User Clients generate a fresh 12-byte AES-GCM nonce with Java `SecureRandom` for every new encryption operation and never reuse one with the same group key. The partial unique nonce index rejects duplicates among retained rows in one conversation/epoch, but unsending clears that metadata and removes it from index coverage. Random generation is probabilistic; V2 defines volume/rotation limits. Retry idempotency also remains a V2 protocol/service rule, not a database guarantee.
- Replacing a public key requires authenticity verification, retirement plus a higher version, and a complete new epoch for every encrypted group the user currently belongs to before sending resumes. An authorized member's client generates/envelopes the key; the server never does, and no plaintext fallback is allowed. Old history requires the matching old private key; ciphertext and envelopes are not rewritten.
- Password change/reset does not recover encryption keys. Losing local private keys loses access to affected encrypted history. Protected client-local persistence, public-key authenticity, envelope completeness, membership/replacement coordination, send pausing, and atomic rotation remain V2 responsibilities.

## Query-support contract

Indexes MUST support:

1. Case-insensitive account lookup, account status, registration dates, and names.
2. User login chronology.
3. Incoming requests by status/date; unique pending unordered pairs.
4. Both sides of friendship and blocking relationships.
5. Conversation type/name/owner/date and user membership lists.
6. Conversation message chronology and sender history, plus retained encrypted-message nonce duplication within one conversation/key epoch. Literal substring search uses escaped case-insensitive `LIKE` for plaintext; encrypted-history search runs on an authorized User Client after decryption. No plaintext search derivative is stored by the server.
7. Per-user message deletion lookup.
8. Report status/date, reporter, reported account, reviewer, and message evidence.
9. User/type/conversation activity by time range for event and distinct-user statistics.

## Validation contract

`script/tests/validate-schema.ps1` MUST statically confirm the six-module entry point, required table-name synchronization, nonce-index synchronization, transaction boundaries, and unsafe construct absence. It MUST NOT act as a second catalog specification. `script/tests/validate-schema.sql` MUST run inside an explicitly isolated database and roll back its fixture data after proving representative valid and invalid cases, including nonce duplicates within an epoch, distinct nonces, same-value nonces across independent epochs, plaintext, and unsent states.

Runtime tests MUST NOT connect to or modify a production or unknown database. Absence of PostgreSQL tooling or an explicitly supplied isolated database is reported as incomplete runtime verification, not as a pass.

Restriction tests accept only PostgreSQL foreign-key/restrict SQLSTATE classes (`23503` or `23001`); unrelated exceptions must continue to fail validation.

## Compatibility contract

- The schema is PostgreSQL-native and consumable through Spring JDBC without JPA/Hibernate.
- Database access remains server-only. No client module receives a database dependency or credential.
- Schema changes after V1 require a reviewed contract and deployment decision. Initialization skips a non-empty `public` schema without claiming compatibility, and clients must not infer physical column names as network protocol fields.
