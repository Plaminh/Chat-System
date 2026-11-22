# Common V2 Protocol Contract

## Transport and envelopes

- UTF-8 NDJSON over the existing TCP connection; one JSON object per line.
- Maximum line content is 65,536 Java characters, excluding CR/LF terminators.
- Requests use `requestId`, `action`, and `data`; successful responses reuse that non-blank `requestId`; an error for input whose ID cannot be read may use a null response `requestId`; unsolicited events use `eventId`, `type`, and `data`.
- A client keeps each `requestId` unique among requests currently in flight on one connection. Responses echo the ID unchanged; interleaved events never complete or consume a pending request. The Server generates a distinct `eventId` for each published event.
- Payload-bearing actions/events require object `data`. Payload-free actions may use `{}`.
- Successful empty results use `[]` inside a valid page/result object, never null collections.

## Identity, time, and paging

- Domain IDs are positive integers. Display IDs such as `USR-1001` are UI-only.
- Instants are ISO-8601 offset date-times; birth/range dates are ISO dates.
- Paging is zero-based, sizes are 1–100, totals are non-negative, and ordering is explicit.
- Unknown action/event/enum values are protocol errors, not forward-compatible no-ops.
- Throughout V2, existing envelope field names, enum wire names, and PING/PONG behavior are stable and must not be renamed or removed. Additive optional payload fields require consumer compatibility tests; new required fields or enum values require a coordinated Common/Server/client release.

## Functional-group coverage

### User groups (12)

| # | Functional group | Actions/events |
|---|---|---|
| U1 | Login/logout | LOGIN, LOGOUT |
| U2 | Registration | REGISTER |
| U3 | Password recovery/change | REQUEST_PASSWORD_RESET (email-only random-password trigger), CHANGE_PASSWORD; deprecated RESET_PASSWORD compatibility identifier |
| U4 | Profile/account | GET_PROFILE, UPDATE_PROFILE |
| U5 | Friend lists/presence | LIST_FRIENDS, LIST_ONLINE_FRIENDS with optional name query; PRESENCE_CHANGED |
| U6 | Friend requests/search | SEARCH_USERS, SEND/LIST/RESPOND/CANCEL_FRIEND_REQUEST |
| U7 | Unfriend/block | REMOVE_FRIEND, BLOCK_USER, UNBLOCK_USER |
| U8 | Direct chat/realtime/offline history | LIST_CONVERSATIONS, OPEN_DIRECT_CONVERSATION, SEND_MESSAGE, LIST_MESSAGES, DIRECT_MESSAGE_RECEIVED |
| U9 | History/search/deletion | SEARCH_CONVERSATION_MESSAGES, SEARCH_ALL_MESSAGES, DELETE_MESSAGE_FOR_ME, UNSEND_MESSAGE, CLEAR_CONVERSATION_HISTORY |
| U10 | Groups/membership/roles | CREATE/GET/LIST/RENAME_GROUP, ADD/REMOVE/LEAVE, UPDATE_GROUP_MEMBER_ROLE, membership/role events |
| U11 | Spam reporting | CREATE_SPAM_REPORT |
| U12 | Writing assistance | REQUEST_WRITING_ASSISTANCE |

### Admin groups (9)

| # | Functional group | Actions |
|---|---|---|
| A1 | Admin authentication/navigation | LOGIN, LOGOUT |
| A2 | User directory and management | ADMIN_LIST/GET/CREATE/UPDATE/DELETE_USER, ADMIN_SET_ACCOUNT_STATUS, ADMIN_RESET_PASSWORD; ACCOUNT_STATUS_CHANGED |
| A3 | Global/per-user login history and friend inspection | ADMIN_LIST_LOGIN_HISTORY, ADMIN_GET_FRIEND_NETWORK |
| A4 | Group inspection | ADMIN_LIST_GROUPS, ADMIN_GET_GROUP |
| A5 | Spam triage and account lock | ADMIN_LIST_SPAM_REPORTS, ADMIN_UPDATE_SPAM_REPORT, ADMIN_SET_ACCOUNT_STATUS |
| A6 | Newly registered user list and range count | ADMIN_LIST_USERS with createdAtRange; ADMIN_GET_REGISTRATIONS aggregate |
| A7 | Friend statistics | ADMIN_GET_FRIEND_STATISTICS |
| A8 | User activity by date | ADMIN_GET_USER_ACTIVITY |
| A9 | Monthly charts | ADMIN_GET_MONTHLY_REGISTRATIONS, ADMIN_GET_MONTHLY_ACTIVE_USERS |

## Error contract

| Category | Codes | Client behavior |
|---|---|---|
| Framing/JSON | INVALID_JSON, MESSAGE_TOO_LARGE | Correct input or reconnect after over-limit closure |
| Envelope/action | INVALID_REQUEST, UNKNOWN_ACTION, UNSUPPORTED_OPERATION | Treat as client/protocol mismatch |
| Payload | INVALID_PAYLOAD, VALIDATION_FAILED | Correct fields; field name may appear, sensitive value may not |
| Authentication/account | UNAUTHENTICATED, INVALID_CREDENTIALS, ACCOUNT_LOCKED, ACCOUNT_DELETED | Prompt/login or show account state |
| Authorization | UNAUTHORIZED, INVALID_MEMBERSHIP, INVALID_GROUP_ROLE | Disable/reload affected operation |
| Domain state | NOT_FOUND, CONFLICT, ALREADY_EXISTS, BLOCKED_RELATIONSHIP, ENCRYPTION_REQUIRED, KEY_ROTATION_REQUIRED | Refresh state and present relevant outcome |
| Server | INTERNAL_ERROR | Safe generic message; no implementation detail |

Failures require `success=false`, an `errorCode`, null data unless documented, and safe text. Success requires `success=true` and null error fields.

## Common validation boundary

- `ProtocolValidator.parseRequest` distinguishes malformed JSON, invalid envelope shape, missing/blank identifiers, unknown actions, and missing/non-object data.
- `ProtocolValidator.parseEvent` applies the equivalent event identity, type, and object-data checks.
- `ProtocolValidator.decodePayload` maps JSON type mismatches to `INVALID_PAYLOAD` and DTO constructor invariant failures to `VALIDATION_FAILED` without copying exception internals or sensitive values into its message.
- Envelope constructors reject contradictory response states and invalid request/event correlation fields.
- DTO constructors remain the single source for context-free positive-ID, required-text, paging, date-range, collection, message-state, and encryption-metadata rules.
- Optional name queries reject blank text when present. Count-comparison thresholds are non-negative. Server allow-lists action-specific sort field names.
- Existence, credentials, permissions, resource state, persistence, handler-to-response mapping, and socket lifecycle remain Server V2 responsibilities.

## Event identifiers

| MessageType | Payload/meaning |
|---|---|
| SERVER_EVENT | Preserved legacy generic event identifier |
| DIRECT_MESSAGE_RECEIVED | `MessageEvent` for a direct conversation |
| GROUP_MESSAGE_RECEIVED | `MessageEvent` for a group conversation |
| PRESENCE_CHANGED | `PresenceUpdate` |
| FRIEND_REQUEST_CHANGED | `FriendRequestEvent` |
| GROUP_MEMBERSHIP_CHANGED | `GroupMembershipEvent` |
| GROUP_ROLE_CHANGED | `GroupRoleEvent` |
| GROUP_KEY_ROTATION_REQUIRED | `GroupKeyRotationEvent` |
| ACCOUNT_STATUS_CHANGED | `AccountStatusEvent` with userId, ACTIVE/LOCKED/DELETED status, and changedAt |

`ACTIVE` means the account became active or was unlocked. For `LOCKED`, the affected client terminates its authenticated session and returns to Login. For `DELETED`, it terminates the session and presents an account-deleted outcome. Common defines only the safe event payload. Server owns authorization, publication, revocation, and connection lifecycle; clients own UI reactions and must also handle `ACCOUNT_LOCKED`/`ACCOUNT_DELETED` responses when the event cannot be delivered.

## Message lifecycle

- `DELETE_MESSAGE_FOR_ME` creates per-user invisibility and does not alter other users' history.
- `UNSEND_MESSAGE` globally recalls the sender's message, removes original content, and preserves identity/timestamp with recall metadata.
- `CLEAR_CONVERSATION_HISTORY` advances one member's history boundary and does not remove messages for others.
- Encrypted history is searched after authorized client decryption; the server stores no plaintext search derivative.

## Encryption invariants

- Group encryption mode is chosen at creation and immutable; direct conversations are never encrypted.
- Public keys are per-user versioned X25519 keys; private keys never enter Common response models or Server storage.
- Group keys are client-generated. Each epoch has one HPKE envelope per authorized recipient public key.
- HPKE profile: base mode, KEM `0x0020`, KDF `0x0001`, AEAD `0x0002`, info `JavaChatSystem-V1-GroupKey`.
- Envelope AAD is `JavaChatSystem-V1|conversation=<id>|epoch=<version>|recipient-user=<id>|recipient-key=<version>`.
- Encrypted messages use AES-256-GCM, fresh 12-byte nonce, 16-byte tag, positive epoch, and no plaintext body.
- Membership/key replacement rotates future epochs; new/rejoining members receive no older epochs by default.

## Representative JSON

```json
{"requestId":"req-1","action":"LOGIN","data":{"username":"alice","password":"request-only"}}
```

```json
{"requestId":"req-1","success":false,"data":null,"errorCode":"INVALID_CREDENTIALS","message":"Invalid username or password"}
```

```json
{"eventId":"evt-42","type":"PRESENCE_CHANGED","data":{"userId":7,"presence":"ONLINE","changedAt":"2026-11-10T09:15:00+07:00"}}
```

```json
{"keyVersion":3,"ciphertext":"AQID","nonce":"AAECAwQFBgcICQoL","authenticationTag":"AAECAwQFBgcICQoLDA0ODw=="}
```

## Access conventions

- Public: REGISTER, LOGIN, REQUEST_PASSWORD_RESET, PING. `RESET_PASSWORD` remains a deprecated compatibility identifier whose legacy handling is a Server integration decision.
- Authenticated user: profile, password change, social, chat, groups, encryption directory/rotation, reports, writing assistance.
- Administrator: all `ADMIN_*` operations; normal user sessions are rejected.
- Resource authorization is Server V2 work; Common expresses identifiers and stable error outcomes only.

## Corrected query and recovery semantics

- `CANCEL_FRIEND_REQUEST` uses `FriendRequestIdTarget(friendRequestId)`, not the target-user contract used to send a request. `GET_GROUP`, `LEAVE_GROUP`, and `ADMIN_GET_GROUP` reuse `GroupTarget(groupId)`.
- `LoginHistoryRequest.userId` is optional: null means global system history; a positive value filters one user. `sort.field` must be `loggedInAt`, with ASC/DESC chronology enforced by Server. Each `LoginHistoryEntry` supplies username and full name without email/address or other unnecessary identity fields.
- `AdminUserFilter.createdAtRange` is the newly registered user-list filter. `ADMIN_GET_REGISTRATIONS` remains a range count, and `ADMIN_GET_MONTHLY_REGISTRATIONS` remains a twelve-month chart series.
- `FriendListRequest.query` and `FriendRequestListRequest.query` match username or full name; request direction is INCOMING or OUTGOING. `SEARCH_USERS` uses the same identity fields, and Server must exclude blocks in both directions and enforce access.
- Friend/activity statistic `nameQuery` matches username/full name. Server allow-lists `name` and `createdAt` sorting; existing EQ/LT/GT numeric filters apply to non-negative counts. Results expose `accountCreatedAt` for deterministic display/sort verification.
- `REQUEST_PASSWORD_RESET` carries only email and always receives an empty, non-enumerating response. The Client never supplies or receives the random password. Server owns generation, hashing, persistence, email delivery, and rollback. `ADMIN_RESET_PASSWORD` remains a separate explicit Admin operation.
- Credential-bearing record diagnostics redact values, and `RequestEnvelope.toString()` never renders `data`. Raw sensitive NDJSON/request payload logging is forbidden.
