# Wire Data Model: Common V2 Foundation

This is the JSON wire model, not the Server domain model or PostgreSQL schema.

## Conventions

- Domain IDs are positive JSON integers backed by `long`; correlation IDs are non-blank strings.
- Instants are ISO-8601 offset date-times; calendar dates are `YYYY-MM-DD`.
- Enums serialize as uppercase names. Optional values may be null; collections never are.
- Binary values use Base64 JSON and defensive copies.

## Envelopes

| Model | Fields | Invariant |
|---|---|---|
| RequestEnvelope | requestId, action, data | non-blank ID; action required; action determines payload |
| ResponseEnvelope | requestId, success, data, errorCode, message | success excludes errors; failure requires errorCode |
| EventEnvelope | eventId, type, data | non-blank ID; type and object data required |

## Shared query models

| Model | Fields | Rules |
|---|---|---|
| PageRequest | page, size | page >= 0; size 1–100 |
| PageResult<T> | items, page, size, totalItems, totalPages | items non-null; totals non-negative |
| DateRange | from, to | both required; from <= to |
| SortSpec | field, direction | field non-blank; Server allow-lists per action |
| NumericFilter | operator, value | EQ, GT, or LT |

## Authentication and accounts

- `RegisterRequest`: username, password, email, fullName, address, dateOfBirth, gender.
- `LoginRequest`: username, password.
- `PasswordChangeRequest`: currentPassword, newPassword.
- `PasswordRecoveryRequest`: email.
- `PasswordResetRequest`: resetCode, newPassword; the code is never echoed.
- `AuthenticationResult`: safe user summary and authenticatedAt; connection/session implementation is deferred.
- `UserSummary`: userId, username, fullName, accountStatus, presence.
- `UserAccountDetail`: summary plus email, address, birth date, gender, role, createdAt, updatedAt.
- `UpdateProfileRequest`: fullName, email, address, birth date, gender.

## Social

- `FriendInfo`: user summary and friendshipCreatedAt.
- `FriendRequestInfo`: requestId, sender, receiver, status, createdAt, respondedAt.
- `FriendRequestTarget`: targetUserId.
- `FriendRequestDecisionRequest`: requestId and ACCEPT/DECLINE decision.
- `UserTarget`: targetUserId for remove/block/unblock.
- `PresenceUpdate`: userId, presence, changedAt.

## Conversations and messages

- `ConversationSummary`: conversationId, type, name, encryptionEnabled, latestMessage, updatedAt.
- `MessageInfo`: messageId, conversationId, sender, body or encrypted content, createdAt, recall metadata.
- `SendMessageRequest`: conversationId and exactly one of body/encryptedContent.
- `MessageHistoryRequest`: conversationId and page.
- `MessageTarget`: messageId for delete-for-me/recall.
- `ConversationTarget`: conversationId for history clearing.
- `MessageSearchRequest`: query, optional conversationId/date range, page.
- `MessageSearchHit`: identity/context, sender, createdAt, matched text; Server results cover plaintext only.

Normal messages have exactly one content form. Recalled messages have neither original body nor ciphertext and carry recall metadata.

## Groups

- `CreateGroupRequest`: name, initial member IDs, and immutable encryptionEnabled selection. Encrypted groups use the separate key-rotation action before encrypted sending begins.
- `GroupInfo`: group/conversation ID, name, owner, encryption flag, members, timestamps.
- `GroupMemberInfo`: user summary, ADMIN/MEMBER role, joinedAt.
- `RenameGroupRequest`: groupId, name.
- `GroupMembersRequest`: groupId, unique non-empty user IDs.
- `GroupMemberTarget`: groupId, userId.
- `GroupRoleUpdateRequest`: groupId, userId, role.

Group IDs are conversation IDs whose type is GROUP. Minimum membership is a Server business rule.

## Administration and moderation

- `AdminUserFilter`: query/status, sort, page.
- `AdminUserMutation`: editable account fields; passwords belong only to create/reset requests.
- `AccountStatusUpdate`: userId and ACTIVE/LOCKED.
- `LoginHistoryEntry`: record ID, user ID, login/logout instants.
- `FriendNetworkInfo`: user ID, direct friends, direct count, second-degree count.
- `GroupInspection`: group information and member count.
- `SpamReportInfo`: report ID, reporter/reported users, message ID, reason, status, reviewer, resolution, timestamps.
- `SpamReportFilter`: date range, username, status, sort, page.
- `SpamReportUpdate`: report ID, status, optional resolution note.

## Statistics

- `RegistrationCount`: date range and count.
- `MonthlyCount`: year, month 1–12, count.
- `FriendStatistic`: user, direct count, second-degree count.
- `UserActivityStatistic`: user, range, app opens, chatted contacts, chatted groups.
- List requests combine criteria with `NumericFilter`, `SortSpec`, and `PageRequest`.

## Writing assistance

- `WritingAssistRequest`: SUGGEST or REVISE, optional draft, optional instruction.
- `WritingAssistResponse`: suggestedText.

No provider credentials or provider-specific objects appear.

## Encryption

- `PublicKeyInfo`: key ID, user ID, positive version, fixed algorithm/format, 32-byte public key, timestamps.
- `RegisterPublicKeyRequest`: version, algorithm/format, 32-byte public key.
- `RecipientKeyEnvelope`: recipient key ID, fixed HPKE algorithm, 32-byte encapsulated key, 48-byte encrypted group key.
- `GroupKeyRotationRequest`: group ID, positive key version, AES algorithm, non-empty recipient envelopes.
- `GroupKeyEpochInfo`: epoch ID, group ID, key version, algorithm, timestamps.
- `EncryptedMessageContent`: key version, non-empty ciphertext, 12-byte nonce, 16-byte authentication tag.

Algorithms are `DHKEM-X25519-HKDF-SHA256`, `RFC9180-RAW`, `AES-256-GCM`, and `HPKE-BASE-X25519-HKDF-SHA256-AES-256-GCM`. No private-key or plaintext-group-key type exists.

## Model boundaries

- Server domain objects may contain authorization/session state that is never serialized.
- Repositories translate wire values to V1 tables; wire names do not expose SQL names as compatibility guarantees.
- Server transactions enforce existence, permissions, canonical pairs, membership, recall ownership, and atomic epoch distribution.
