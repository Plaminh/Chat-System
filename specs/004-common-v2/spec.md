# Feature Specification: Common V2 Foundation

**Feature Branch**: `feature/common-v2`
**Created**: 2026-11-10
**Status**: Ready for Review
**Input**: Shared Version 2 communication contracts for the User Client, Admin Client, and Server.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Integrate Account and Social Features (Priority: P1)

User and Admin developers can exchange typed requests and results for authentication, profiles, password workflows, friend relationships, blocking, and account administration without redefining payloads.

**Why this priority**: Account identity and authorization are prerequisites for every protected flow.
**Independent Test**: Representative account and social payloads serialize, deserialize, and map to documented actions and errors without client or server implementation code.

**Acceptance Scenarios**:
1. **Given** a supported account or social request, **When** it is encoded and decoded, **Then** identifiers, user values, and optional fields retain their meaning.
2. **Given** a protected operation fails, **When** an error is returned, **Then** clients distinguish authentication, authorization, validation, missing-resource, conflict, and account-state failures without internal details.
3. **Given** an administrator filters or sorts records, **When** the request is exchanged, **Then** filter, order, range, and paging semantics are unambiguous.

---

### User Story 2 - Integrate Direct and Group Messaging (Priority: P1)

User developers can exchange typed conversation, message, search, deletion, group membership, and role payloads while preserving established direct/group behavior.

**Why this priority**: Messaging is the central project capability.
**Independent Test**: Direct and group payloads round-trip with numeric identities, chronological metadata, pagination, roles, and deletion state intact.

**Acceptance Scenarios**:
1. **Given** a user sends or reads a message, **When** the payload crosses the protocol boundary, **Then** conversation, sender, content form, timestamp, and correlation data are preserved.
2. **Given** delete-for-me, global recall, or history clearing, **When** represented, **Then** each intent has a distinct action and contract.
3. **Given** conversation or global search, **When** results are returned, **Then** empty-result, context, ordering, and navigation metadata are predictable.
4. **Given** a group administrator changes membership or roles, **When** distributed, **Then** affected clients identify the group, member, role, and effective change.

---

### User Story 3 - Consume Realtime Notifications (Priority: P1)

Connected clients can distinguish solicited responses from unsolicited message, presence, friend-request, membership, and role-change events.

**Why this priority**: Realtime delivery and offline persistence require stable event boundaries.
**Independent Test**: Requests, responses, and events decode into distinct envelope types and route using their own identifiers.

**Acceptance Scenarios**:
1. **Given** an outstanding request, **When** a response arrives, **Then** its request identifier matches the request.
2. **Given** an unsolicited event, **When** decoded, **Then** it has an event identifier and typed category and cannot be mistaken for a response.
3. **Given** persisted offline messages, **When** history is requested, **Then** the realtime message representation also represents stored history.

---

### User Story 4 - Exchange Encrypted Group Data Safely (Priority: P1)

User clients can publish public keys, distribute recipient-specific group-key envelopes, and exchange encrypted group messages while the server treats secrets as opaque.

**Why this priority**: Ambiguous encryption contracts risk plaintext exposure or inaccessible history.
**Independent Test**: Key, epoch, envelope, and encrypted-message payloads round-trip and reject invalid lengths, algorithms, and mixed content.

**Acceptance Scenarios**:
1. **Given** an encrypted group, **When** represented, **Then** immutable encryption mode is explicit and distinct from direct/plaintext chat.
2. **Given** membership or active-key change, **When** a new epoch is distributed, **Then** each envelope identifies its recipient key and epoch without plaintext group keys.
3. **Given** an encrypted message, **When** exchanged, **Then** it has ciphertext, a 12-byte nonce, a 16-byte tag, a positive key version, and no plaintext body.
4. **Given** global recall, **When** returned, **Then** ciphertext metadata is absent and recall state is explicit.

---

### User Story 5 - Reject Invalid Protocol Input Predictably (Priority: P2)

Module developers receive consistent validation for missing fields, unknown identifiers, malformed JSON, invalid payload shape, and oversized NDJSON messages.

**Why this priority**: Predictable boundary failures prevent crashes and internal-detail leakage.
**Independent Test**: Tests cover valid messages, missing fields, unknown enum values, malformed JSON, line endings, EOF behavior, and exact size limits.

**Acceptance Scenarios**:
1. **Given** malformed input or an unknown action, **When** parsed, **Then** a stable client-facing error category is available.
2. **Given** a line at the maximum size, **When** read, **Then** it is accepted; one more character is rejected.
3. **Given** validation failure, **When** an error is produced, **Then** it contains no stack trace, credentials, tokens, keys, or database details.

### Edge Cases

- Unknown action/event names fail predictably rather than mapping silently.
- Empty results use empty collections and complete paging metadata, never `null` collections.
- Optional filters remain distinguishable from required identifiers; blank required text is invalid.
- LF and CRLF are accepted without retaining terminators; immediate EOF returns no line and EOF after content returns the final line.
- Plaintext and encrypted content are mutually exclusive.
- IDs are positive numeric values, timestamps include offsets, and display IDs are never accepted as entity IDs.
- Paging bounds and date ordering are validated before business logic.
- Passwords, hashes, tokens, private keys, group keys, and persistence internals never appear in responses.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Define distinct request, response, and event envelopes with request/event identifiers, typed categories, and structured payloads.
- **FR-002**: Preserve the existing `PING` action and response behavior.
- **FR-003**: Define actions for authentication, profile/password, friends/requests/blocking/presence, direct chat/history/search, groups/members/roles/encryption, spam, writing assistance, and all required Admin operations/statistics.
- **FR-004**: Define non-overlapping client errors for malformed input, validation, authentication, authorization, missing resources, conflicts, account/membership/role state, unsupported operations, size limits, and internal failures.
- **FR-005**: Define events for direct/group messages, presence, friend requests, group membership, group roles, and encryption rotation.
- **FR-006**: Use positive numeric values for domain identities; presentation IDs are out of scope.
- **FR-007**: Use offset-aware instants and calendar dates where time-of-day is not meaningful.
- **FR-008**: Define reusable paging, date-range, sorting, and numeric-comparison contracts.
- **FR-009**: Define registration, login/logout, auth result, password change, and non-enumerating password recovery that asks the Server to generate, hash, persist, and email a random password without accepting that password from the Client.
- **FR-010**: Password requests never contain hashes; recovery responses never contain generated passwords; all responses exclude passwords, hashes, reset secrets, and auth secrets.
- **FR-011**: Define friend, online-friend, request, block, presence, and user-search payloads with explicit state, optional name filtering, paging, and request direction where applicable; Server excludes blocked relationships from user search.
- **FR-012**: Define conversation, message, send, history, delete-for-me, recall, clear-history, search, result, and navigation payloads.
- **FR-013**: Delete-for-me, global recall, and history clearing remain distinct operations matching V1 persistence semantics.
- **FR-014**: Define group creation/detail/member/role/rename/member-change/role-change/group-message payloads.
- **FR-015**: Group creation selects encryption mode; direct chat cannot enable it and group encryption mode is immutable.
- **FR-016**: Define Admin user list/detail/create/edit/soft-delete, lock/unlock, explicit password update, global or per-user login history with username/full name and chronological sorting, friend network, group inspection, report review, filters, sorting, and paging payloads.
- **FR-017**: Define newly registered user lists by date range through the Admin user list contract, retain separate aggregate/monthly registration counts, and define name/numeric/sort filters plus account-creation metadata for direct/second-degree friend and user-activity statistics.
- **FR-018**: Define suggestion/revision writing-assistance payloads without provider integration.
- **FR-019**: Define public-key, key-epoch, recipient-envelope, encrypted-content, and key-rotation payloads matching V1 encryption lifecycle.
- **FR-020**: Encrypted content uses AES-256-GCM metadata with positive version, 12-byte nonce, 16-byte tag, non-empty ciphertext, and no body.
- **FR-021**: Key envelopes use the V1 RFC 9180 profile, 32-byte encapsulated key, 48-byte encrypted group key, recipient-key identity, and no plaintext private/group key.
- **FR-022**: Document that key replacement and membership changes require a new epoch, new/rejoining members receive no old epochs by default, and sending may pause pending distribution.
- **FR-023**: Validate envelope fields, blank IDs, enum values, payload presence/type, positive IDs, required text, paging, date ranges, and encryption invariants inside Common without database-dependent or Server-handler validation.
- **FR-024**: Preserve UTF-8 NDJSON, LF/CRLF handling, the 65,536-character maximum, and EOF behavior.
- **FR-025**: Errors and diagnostic summaries are predictable and never disclose stack traces, credentials, reset codes, raw sensitive request payloads, secrets, keys, or database details.
- **FR-026**: Empty lists use empty collections and stable paging metadata.
- **FR-027**: Contracts remain independent of UI, business services, socket lifecycle, persistence, email, external AI calls, and cryptographic operations.
- **FR-028**: Preserve supported behavior and dependent-module compilation without modifying Admin, User, or Server source.
- **FR-029**: Envelopes, enums, payload families, framing boundaries, and validation rules receive focused automated tests.
- **FR-030**: Integration guidance covers requests, responses, errors, events, and encryption metadata without presenting deferred runtime behavior as implemented.
- **FR-031**: Preserve the existing `common/src/main/java/com/chat/common/ui/ChatTheme.java`, its Swing utilities, client imports, theme initialization, and UI behavior unchanged; do not relocate, rename, duplicate, refactor, or expose it to Server code.
- **FR-032**: Keep every Common V2 DTO, enum, protocol, validation, and exception type independent of Swing even though the approved legacy `common.ui.ChatTheme` remains in the Common module.
- **FR-033**: Defer Server request-handler mapping and Server protocol regression tests to `feature/server-v2`, retaining former task IDs T031 and T032 as traceability references rather than Common V2 implementation tasks.

### Key Entities

- **Protocol Envelope**: Request, response, or event wrapper with correlation identity, typed category, and payload.
- **Account Contract**: Authentication and safe public/administrative account representations.
- **Social Contract**: Friendship, request, block, and presence state.
- **Conversation Contract**: Direct/group metadata, members, messages, history, deletion, and search.
- **Administration Contract**: User management, login audit, group inspection, reports, filters, and statistics.
- **Encryption Contract**: Public key, key epoch, recipient envelope, and encrypted content; never secret material.
- **Validation Result**: Deterministic protocol-level violations suitable for client errors.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All 9 Admin and 12 User functional groups map to documented action contracts with actor, access, request, success, errors, and events.
- **SC-002**: Representative JSON for every envelope and payload family round-trips without loss.
- **SC-003**: Below-limit, exact-limit, and above-limit NDJSON tests produce documented outcomes.
- **SC-004**: Every FR-023 invalid-input category has a deterministic Common test and no Common validation result exposes internal exception text.
- **SC-005**: Common tests and read-only dependent-module compilation checks pass without Admin, User, or Server source changes, or unrelated baseline blockers are reported with exact commands and causes.
- **SC-006**: Developers can find action, payload, error, access, and event implications for each functional group in one contract set.
- **SC-007**: Dependency inspection confirms that Swing references in Common are confined to the unchanged legacy `common.ui.ChatTheme` and its test; DTO, enum, protocol, validation, and exception packages contain no Swing imports or dependencies.
- **SC-008**: Existing PING serialization and handling remain compatible.
- **SC-009**: V1-baseline comparison finds the original shared `ChatTheme` at the same path with identical content, no additional theme implementation, no Admin/User source delta, and no Server import of the shared UI package.

## Assumptions

- The constitution is authoritative: Java 25 and exactly four modules remain unchanged.
- The approved architecture decision retains the pre-existing shared Swing theme as a narrow compatibility exception; it does not authorize additional presentation code in Common.
- V1 numeric IDs and lifecycle rules are authoritative; wire names do not promise physical database names.
- Runtime authentication, authorization, persistence, email, AI calls, encryption/decryption, and key authenticity belong to later features.
- Timestamps use ISO-8601 offsets; dates use ISO calendar dates; paging is zero-based with positive sizes.
- The server stores/forwards opaque encrypted data but owns no private key or plaintext group key.

## Explicit Exclusions

- Changes to Swing/client UI behavior, styling, FlatLaf setup, Look and Feel initialization, or theme utilities. The existing shared `ChatTheme` is retained unchanged rather than treated as new V2 implementation scope.
- Admin Client, User Client, and Server source or test changes. Server request-handler error mapping and regressions are deferred to `feature/server-v2` as former T031 and T032.
- Database changes, repositories, JDBC, migrations, socket/session lifecycle, and delivery scheduling.
- Authentication/authorization implementation, password hashing, email delivery, and token storage.
- Cryptographic operations, key generation, private-key storage, and key authenticity implementation.
- External writing-assistance provider integration.

## Final Contract Corrections

- `ADMIN_LIST_LOGIN_HISTORY` accepts an omitted `userId` for a global query, accepts a required chronological `SortSpec`, and returns login records containing only the required user identity (`userId`, username, full name) plus login/logout times.
- `ADMIN_LIST_USERS` owns the newly registered **user list** requirement through optional `createdAtRange`, name query, sorting, and paging. `ADMIN_GET_REGISTRATIONS` remains the range aggregate, while `ADMIN_GET_MONTHLY_REGISTRATIONS` remains the annual-chart series.
- `LIST_FRIENDS` and `LIST_ONLINE_FRIENDS` use `FriendListRequest`; `LIST_FRIEND_REQUESTS` adds optional name and incoming/outgoing direction filters. Presence remains in `UserSummary`; block visibility remains mandatory Server authorization/filtering work.
- Friend and activity statistic requests add optional `nameQuery`; both result records include `accountCreatedAt`; activity uses distinct `chattedUsers`/`chattedUserCount` rather than a friendship-only contact interpretation; Server allow-lists `name` and `createdAt` sort fields and applies the existing non-negative `NumericFilter` comparisons.
- `REQUEST_PASSWORD_RESET` is the assignment recovery flow: the Client supplies only the registered email and receives an empty, non-enumerating response. Server V2 owns random generation, hashing, persistence, email delivery, and rollback. `RESET_PASSWORD`/`PasswordResetRequest` remain deprecated compatibility contracts and are not the assignment recovery flow. `ADMIN_RESET_PASSWORD` remains a separate explicit administrator update.
- Credential-bearing records and `RequestEnvelope` provide redacted diagnostics while preserving JSON serialization. Raw request JSON must never be logged for sensitive actions.
