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
| ResponseEnvelope | requestId, success, data, errorCode, message | success requires correlation and excludes errors; failure requires errorCode and safe text; an unreadable request may produce an uncorrelated failure |
| EventEnvelope | eventId, type, data | non-blank ID; type and object data required |

## Shared query models

| Model | Fields | Rules |
|---|---|---|
| PageRequest | page, size | page >= 0; size 1–100 |
| PageResult<T> | items, page, size, totalItems, totalPages | items non-null; totals non-negative |
| DateRange | from, to | both required; from <= to |
| SortSpec | field, direction | field non-blank; Server allow-lists per action |
| NumericFilter | operator, value | EQ, GT, or LT |
| YearRequest | year | positive calendar year |

## Authentication and accounts

- `RegisterRequest`: username, password, email, fullName, address, dateOfBirth, gender.
- `LoginRequest`: username, password.
- `PasswordChangeRequest`: currentPassword, newPassword.
- `PasswordRecoveryRequest`: email.
- `PasswordResetRequest`: deprecated compatibility record containing resetCode/newPassword; not the assignment recovery flow and never echoed.
- `AuthenticationResult`: safe user summary and authenticatedAt; connection/session implementation is deferred.
- `UserSummary`: userId, username, fullName, accountStatus, presence.
- `UserAccountDetail`: summary plus email, address, birth date, gender, role, createdAt, updatedAt.
- `UpdateProfileRequest`: fullName, email, address, birth date, gender.
- `UserSearchRequest`: non-blank query plus paging.

## Social

- `FriendInfo`: user summary and friendshipCreatedAt.
- `FriendRequestInfo`: requestId, sender, receiver, status, createdAt, respondedAt.
- `FriendRequestTarget`: targetUserId.
- `FriendRequestDecisionRequest`: requestId and ACCEPT/DECLINE decision.
- `FriendListRequest`: optional username/full-name query plus paging for friend and online-friend lists.
- `FriendRequestListRequest`: optional username/full-name query, status, incoming/outgoing direction, and paging.
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

- `CreateGroupRequest`: name, initial member IDs, and encryptionEnabled. Key rotation is a separate request.
- `GroupInfo`: group/conversation ID, name, owner, encryption flag, members, timestamps.
- `GroupMemberInfo`: user summary, ADMIN/MEMBER role, joinedAt.
- `RenameGroupRequest`: groupId, name.
- `GroupMembersRequest`: groupId, unique non-empty user IDs.
- `GroupMemberTarget`: groupId, userId.
- `GroupRoleUpdateRequest`: groupId, userId, role.

Group IDs are conversation IDs whose type is GROUP. Minimum membership is a Server business rule.

## Administration and moderation

- `AdminUserFilter`: optional username/full-name query, status, creation-date range, sort, and page. Its `PageResult<AdminUserInfo>` is the newly registered user list when a creation range is supplied.
- `AdminCreateUserRequest`: registration fields plus role; passwords are request-only.
- `AdminUpdateUserRequest`: positive user ID, editable profile, and role.
- `AdminPasswordResetRequest`: positive user ID plus request-only new password.
- `AdminUserInfo`: safe `UserAccountDetail` wrapper with no credential material.
- `AdminGroupFilter`: optional query plus required sort and page.
- `AccountStatusUpdate`: userId and ACTIVE/LOCKED.
- `LoginHistoryEntry`: record ID, user ID, username, full name, login/logout instants.
- `LoginHistoryRequest`: optional positive user ID, chronological sort (`loggedInAt`), and page. Omitted user ID requests global history.
- `FriendNetworkInfo`: user ID, direct friends, direct count, second-degree count.
- `GroupInspection`: group information and member count.
- `SpamReportCreateRequest`: reported user, optional message ID, and non-blank reason.
- `SpamReportInfo`: report ID, reporter/reported users, message ID, reason, status, reviewer, resolution, timestamps.
- `SpamReportFilter`: date range, username, status, sort, page.
- `SpamReportUpdate`: report ID, status, optional resolution note.

## Statistics

- `RegistrationCount`: date range and count.
- `MonthlyCount`: year, month 1–12, count.
- `FriendStatistic`: user, account creation instant, direct count, second-degree count.
- `UserActivityStatistic`: user, account creation instant, range, app opens, distinct chatted users, distinct chatted groups.
- `FriendStatisticsRequest`: optional name and direct/second-degree numeric filters plus required sort and page.
- `UserActivityStatisticsRequest`: required range, optional name and numeric filters, required sort, and page.
- `YearRequest`: shared positive-year input for monthly reports.

`NumericFilter.value` is non-negative. Server allow-lists `name` and `createdAt` for both statistic sorts, plus action-specific numeric fields where the assignment requires them.

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
- `PublicKeyDirectoryRequest`: unique, non-empty positive user IDs.
- `GroupKeyEnvelopeRequest`: positive group ID and key version.
- `EncryptionAlgorithms`: fixed wire constants only; it performs no cryptography.

Algorithms are `DHKEM-X25519-HKDF-SHA256`, `RFC9180-RAW`, `AES-256-GCM`, and `HPKE-BASE-X25519-HKDF-SHA256-AES-256-GCM`. No private-key or plaintext-group-key type exists.

## Realtime event payloads

- `MessageEvent`: wraps a validated `MessageInfo` for direct or group delivery.
- `FriendRequestEvent`: wraps a validated `FriendRequestInfo`.
- `GroupMembershipEvent`: group, affected user, ADDED/REMOVED/LEFT change, and occurrence time.
- `GroupRoleEvent`: group, affected user, new role, and occurrence time.
- `GroupKeyRotationEvent`: group, required key version, and occurrence time.

These are Common wire contracts only. Event publication, authorization, offline delivery, and client routing remain Server/User/Admin integration work.

## Model boundaries

- Server domain objects may contain authorization/session state that is never serialized.
- Repositories translate wire values to V1 tables; wire names do not expose SQL names as compatibility guarantees.
- Server transactions enforce existence, permissions, canonical pairs, membership, recall ownership, and atomic epoch distribution.
- User password recovery returns no generated credential. Server V2 generates the random password, hashes and persists it, sends it by email, and handles failure/rollback without account enumeration.
- Record diagnostics redact credential values, and `RequestEnvelope.toString()` omits raw `data`; consuming modules must not log raw JSON for sensitive actions.
