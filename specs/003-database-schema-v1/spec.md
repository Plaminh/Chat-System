# Feature Specification: Database Schema Version 1

**Feature Branch**: `feature/database-schema-v1`

**Created**: 2026-10-30

**Status**: Complete — ready for another technical review after post-review remediation

**Input**: User description: "Design and deliver the Version 1 relational data model, schema scripts, development data, initialization tooling, and documentation required by the Admin and User desktop interfaces without implementing Version 2 persistence services."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Preserve Accounts and Relationships (Priority: P1)

As a chat-system reviewer, I want account, login, friendship, request, and blocking information represented consistently so that all identity and relationship screens have an authoritative data foundation.

**Why this priority**: Accounts are referenced by every other feature and relationship integrity is required before conversations or administration records can be meaningful.

**Independent Test**: Load only the identity and relationship portion of the schema and verify that valid users, login events, requests, friendships, and blocks can be recorded while duplicate or self-referential relationships are rejected.

**Acceptance Scenarios**:

1. **Given** a valid user profile, **When** the account is recorded, **Then** its credentials, profile, role, account state, and audit timestamps are retained without storing calculated friend counts.
2. **Given** two distinct users, **When** request, friendship, or block records are created, **Then** valid states are retained and duplicate or self-directed relationships are rejected.
3. **Given** login activity for a user, **When** the history is reviewed, **Then** chronological login and optional logout times are available.

---

### User Story 2 - Preserve Conversation History (Priority: P1)

As a chat-system reviewer, I want direct and group conversations, memberships, messages, and user-specific deletion state represented so that messaging screens can distinguish conversation types and message lifecycle actions.

**Why this priority**: Direct and group messaging are the core communication journeys and depend on durable, unambiguous history semantics.

**Independent Test**: Load the conversation portion of the schema with valid users and verify direct-pair uniqueness, group membership roles, ordered message history, global unsending, per-user message deletion, and per-user history clearing.

**Acceptance Scenarios**:

1. **Given** two users, **When** they establish a direct conversation, **Then** only one conversation for that unordered pair can exist.
2. **Given** a group conversation, **When** membership is recorded, **Then** active and former members, their roles, join times, leave times, and personal history-clearing boundaries are distinguishable.
3. **Given** a sent message, **When** it is unsent or deleted for one user, **Then** global recall and user-specific hiding remain separate facts.

---

### User Story 3 - Support Administration and Analytics (Priority: P2)

As an administrator, I want moderation and activity facts available independently from account state so that reports, sanctions, registrations, friend statistics, and engagement charts can be derived accurately.

**Why this priority**: Administrative oversight relies on the account and conversation foundation but must preserve review history and support reproducible statistics.

**Independent Test**: Record spam reports and activity events, then verify report workflow fields, referenced users/messages, account-lock independence, and distinct-user activity aggregation by time period and conversation type.

**Acceptance Scenarios**:

1. **Given** a report about an account, **When** it is reviewed, **Then** report status, reviewer, resolution time, and the reported account's lock status remain separately queryable.
2. **Given** application-open and chat activity events, **When** an administrator selects a time range, **Then** launches, distinct direct-chat partners, distinct group activity, and active-user totals can be derived.
3. **Given** account creation and friendship records, **When** growth and network statistics are requested, **Then** monthly registration and relationship counts can be calculated from source facts.

### Edge Cases

- Repeating a friendship or block in reversed user order must not create a duplicate relationship.
- A user must not request friendship with, befriend, or block their own account.
- Deleting an account must not leave dangling login, relationship, membership, message-deletion, report, or activity references.
- A recalled message must retain lifecycle metadata, replace original content with a non-null canonical non-sensitive placeholder, and remain distinguishable from a message hidden only for one user.
- If one user blocks another, neither account may discover the other through normal user search while the directed block remains.
- Clearing a conversation history must affect only the requesting participant and must not delete shared messages.
- Report resolution must not implicitly lock or unlock an account.
- Encrypted groups use storage supporting client-side E2EE: per-user public recipient keys, client-generated group-key epochs, recipient-specific HPKE envelopes, and AES-GCM message ciphertext. V1 does not implement or prove the cryptography.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The data model MUST contain the twelve original entities plus the minimum `user_public_keys`, `conversation_key_versions`, and `conversation_key_envelopes` storage required for client-side encrypted groups.
- **FR-002**: Every core entity MUST have a stable numeric identity where an independent identity is appropriate, and UI identifiers such as `USR-1001` MUST remain display conventions rather than primary identities.
- **FR-003**: User records MUST support authentication data, administrator and normal-user roles, profile details, active or locked account state, normalized gender values (`male`, `female`, `other`, or unspecified), and lifecycle timestamps.
- **FR-004**: Login history MUST associate sign-in times and optional sign-out times with an existing user.
- **FR-005**: Friend requests MUST associate distinct sender and receiver accounts and use only `pending`, `accepted`, `declined`, or `canceled` states.
- **FR-006**: Friendships and user blocks MUST reject self-relationships and duplicates, including reversed duplicates where the relationship is unordered.
- **FR-007**: Conversations MUST distinguish direct and group conversations, support group names and ownership where applicable, carry a creation-time encryption-mode indicator, and retain lifecycle timestamps. V2 MUST treat a group's selected mode as immutable; another mode requires another group and no history conversion occurs.
- **FR-008**: Direct conversations MUST identify a unique unordered user pair; conversation membership MUST retain roles, join/leave timestamps, and a per-user history-clearing boundary.
- **FR-009**: Messages MUST retain conversation, sender, creation time, and global-unsend metadata including when and optionally by whom the recall occurred; recall MUST replace the original body with the non-null canonical `[Message unsent]` placeholder.
- **FR-010**: Per-user message deletion MUST be represented independently from global message unsending and MUST reject duplicate deletion records for the same user and message.
- **FR-011**: Spam reports MUST retain reporter, reported account, optional referenced message, reason, review status, optional reviewer, and resolution time without conflating report status with account lock state.
- **FR-012**: Activity records MUST distinguish application opening, direct-chat activity, and group-chat activity and MUST support time-bounded counts of events and distinct active users.
- **FR-013**: The model MUST provide explicit referential, uniqueness, value, and temporal constraints where those rules can be enforced within a table.
- **FR-014**: Cross-entity rules that require coordinated reads or writes MUST be documented for enforcement by backend transactions in Version 2.
- **FR-015**: The deliverable MUST include representative fictional development data covering all fifteen tables without real personal data, private keys, plaintext group keys, or embedded secrets.
- **FR-016**: The deliverable MUST include a configurable initialization workflow that never embeds a password and never drops an existing database automatically.
- **FR-017**: The deliverable MUST include an ERD, machine-readable model notation, setup guidance, UI-to-data mappings, validation evidence, and a documented limitation when runtime database validation is unavailable.
- **FR-018**: Version 1 MUST NOT implement repositories, business services, live authentication, realtime messaging, UI integration, cryptographic operations, external key management, or other Version 2 behavior.
- **FR-019**: User search MUST exclude both accounts blocked by the searching user and accounts that blocked the searching user; the Version 2 backend MUST enforce this before returning results.
- **FR-020**: The complete V1 baseline MUST be defined by `script/schema.sql` and its modules. Compose MUST skip initialization when `public` already contains base tables without claiming compatibility; the PowerShell helper MUST refuse an existing database by default and allow only an explicitly selected existing empty database. Neither path upgrades, repairs, or reseeds existing data.
- **FR-021**: Encrypted-group storage MUST use versioned per-user public keys, client-generated group-key epochs, one encrypted envelope per recipient key/epoch, and message ciphertext metadata without storing client private keys, plaintext group keys, or a server decryption key.
- **FR-022**: Encrypted messages MUST use a fresh 12-byte Java `SecureRandom` nonce for each new AES-GCM operation and MUST NOT reuse a nonce with the same group key. V1 MUST add a retained-row duplicate safeguard without claiming absolute random uniqueness or database retry idempotency.
- **FR-023**: Public-key replacement MUST preserve retired key rows and historical envelopes, require V2 authenticity verification, and rotate every affected encrypted group to a complete current-recipient epoch before sending resumes, without plaintext fallback or automatic history rewriting.

### Key Entities *(include if feature involves data)*

- **User**: Account identity, credentials, profile, role, lock state, and lifecycle timestamps.
- **Login History**: A sign-in session boundary associated with one user.
- **Friend Request**: A directed relationship proposal and its response state.
- **Friendship**: A canonical unordered connection between two users.
- **User Block**: A directed decision by one user to block another.
- **Conversation**: A direct or group messaging context with ownership and encryption-status metadata.
- **Direct Conversation**: The canonical unordered pair attached to one direct conversation.
- **Conversation Member**: A user's participation, role, membership interval, and history-clearing boundary.
- **User Public Key**: A versioned per-user X25519 public recipient key; the private key remains on the User Client.
- **Conversation Key Version**: Metadata identifying a client-generated group-key epoch and its message encryption algorithm.
- **Conversation Key Envelope**: An opaque HPKE envelope carrying one epoch's group key to one recipient public key.
- **Message**: Content and lifecycle facts within a conversation.
- **Message Deletion**: A user's decision to hide a specific message.
- **Spam Report**: A moderation case linking reporter, reported account, optional message evidence, and review outcome.
- **User Activity Log**: A source event for usage and engagement statistics.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Reviewers can trace 100% of required Admin and User data fields to the documented V1 tables or to a documented derived value.
- **SC-002**: All fifteen required table definitions load in dependency order in an isolated validation environment with zero unresolved references.
- **SC-003**: Automated validation demonstrates rejection of every documented self-reference, duplicate relationship, invalid state, and inconsistent lifecycle case covered by the schema contract.
- **SC-004**: Representative development data populates all fifteen required tables and passes all referential/value constraints without plaintext encrypted-group content, private keys, or plaintext group keys.
- **SC-005**: A reviewer can initialize an empty development database, optionally load sample data, and identify safe handling of an already existing database using only the documented commands.
- **SC-006**: The final review finds zero Version 2 repositories, services, protocol changes, UI changes, or implemented cryptographic/key-management behavior in the Database V1 commits.

## Assumptions

- Version 1 delivers design, scripts, documentation, and validation fixtures; runtime data access remains Version 2 work.
- PostgreSQL's default UTF-8 database encoding is used; the schema stores Unicode text without database-specific collation changes.
- Usernames and email addresses are unique case-insensitively through normalized indexes, while display casing is preserved.
- Password values in development data are BCrypt-formatted demonstration hashes and are not valid production credentials.
- Presence is a future runtime concern; activity timestamps can support last-activity indicators but do not establish a live presence protocol.
- Cross-table authorization rules, direct-conversation membership synchronization, group-owner/admin guarantees, report eligibility from prior interaction, block-aware search, membership reactivation, and unsend authorization/atomic redaction are enforced transactionally by the backend in Version 2.
- Plaintext substring search uses escaped case-insensitive `LIKE` without a required extension. Encrypted-group search occurs on authorized User Clients after decryption and stores no server-readable plaintext search copy.
- The assignment requests encrypted group chat but does not prescribe an E2EE protocol. This database adopts E2EE as the intended design direction using a per-user recipient model; independent per-device keys are not supported.
- Private-key persistence, authenticated public-key registration/replacement, key-substitution defenses, client encryption/decryption, membership authorization, send pausing, retry idempotency, message-volume limits, and atomic envelope distribution/rotation are Version 2 responsibilities. Password reset does not recover lost encryption keys.
