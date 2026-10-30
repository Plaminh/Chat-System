# Feature Specification: Database Schema Version 1

**Feature Branch**: `feature/database-schema-v1`

**Created**: 2026-10-30

**Status**: Complete — ready for technical review

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
- A recalled message must remain distinguishable from a message hidden only for one user.
- Clearing a conversation history must affect only the requesting participant and must not delete shared messages.
- Report resolution must not implicitly lock or unlock an account.
- Encrypted-conversation metadata must not claim that key management or message encryption exists in Version 1.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The data model MUST contain exactly twelve core entities: users, login history, friend requests, friendships, user blocks, conversations, direct-conversation pairs, conversation membership, messages, message deletions, spam reports, and user activity logs.
- **FR-002**: Every core entity MUST have a stable numeric identity where an independent identity is appropriate, and UI identifiers such as `USR-1001` MUST remain display conventions rather than primary identities.
- **FR-003**: User records MUST support authentication data, administrator and normal-user roles, profile details, active or locked account state, normalized gender values (`male`, `female`, `other`, or unspecified), and lifecycle timestamps.
- **FR-004**: Login history MUST associate sign-in times and optional sign-out times with an existing user.
- **FR-005**: Friend requests MUST associate distinct sender and receiver accounts and use only `pending`, `accepted`, `declined`, or `canceled` states.
- **FR-006**: Friendships and user blocks MUST reject self-relationships and duplicates, including reversed duplicates where the relationship is unordered.
- **FR-007**: Conversations MUST distinguish direct and group conversations, support group names and ownership where applicable, carry an encryption-status indicator, and retain lifecycle timestamps.
- **FR-008**: Direct conversations MUST identify a unique unordered user pair; conversation membership MUST retain roles, join/leave timestamps, and a per-user history-clearing boundary.
- **FR-009**: Messages MUST retain conversation, sender, body, creation time, and global-unsend metadata including when and optionally by whom the recall occurred.
- **FR-010**: Per-user message deletion MUST be represented independently from global message unsending and MUST reject duplicate deletion records for the same user and message.
- **FR-011**: Spam reports MUST retain reporter, reported account, optional referenced message, reason, review status, optional reviewer, and resolution time without conflating report status with account lock state.
- **FR-012**: Activity records MUST distinguish application opening, direct-chat activity, and group-chat activity and MUST support time-bounded counts of events and distinct active users.
- **FR-013**: The model MUST provide explicit referential, uniqueness, value, and temporal constraints where those rules can be enforced within a table.
- **FR-014**: Cross-entity rules that require coordinated reads or writes MUST be documented for enforcement by backend transactions in Version 2.
- **FR-015**: The deliverable MUST include representative fictional development data covering all twelve entities without real personal data or embedded secrets.
- **FR-016**: The deliverable MUST include a configurable initialization workflow that never embeds a password and never drops an existing database automatically.
- **FR-017**: The deliverable MUST include an ERD, machine-readable model notation, setup guidance, UI-to-data mappings, validation evidence, and a documented limitation when runtime database validation is unavailable.
- **FR-018**: Version 1 MUST NOT implement repositories, business services, live authentication, realtime messaging, UI integration, encryption-key management, or other Version 2 behavior.

### Key Entities *(include if feature involves data)*

- **User**: Account identity, credentials, profile, role, lock state, and lifecycle timestamps.
- **Login History**: A sign-in session boundary associated with one user.
- **Friend Request**: A directed relationship proposal and its response state.
- **Friendship**: A canonical unordered connection between two users.
- **User Block**: A directed decision by one user to block another.
- **Conversation**: A direct or group messaging context with ownership and encryption-status metadata.
- **Direct Conversation**: The canonical unordered pair attached to one direct conversation.
- **Conversation Member**: A user's participation, role, membership interval, and history-clearing boundary.
- **Message**: Content and lifecycle facts within a conversation.
- **Message Deletion**: A user's decision to hide a specific message.
- **Spam Report**: A moderation case linking reporter, reported account, optional message evidence, and review outcome.
- **User Activity Log**: A source event for usage and engagement statistics.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Reviewers can trace 100% of required Admin and User data fields to one of the twelve entities or to a documented derived value.
- **SC-002**: All twelve entity definitions load in dependency order in a clean validation environment with zero unresolved references.
- **SC-003**: Automated validation demonstrates rejection of every documented self-reference, duplicate relationship, invalid state, and inconsistent lifecycle case covered by the schema contract.
- **SC-004**: Representative development data populates all twelve entities and passes all referential and value constraints.
- **SC-005**: A reviewer can initialize an empty development database, optionally load sample data, and identify safe handling of an already existing database using only the documented commands.
- **SC-006**: The final review finds zero Version 2 repositories, services, protocol changes, UI changes, or encryption-key features in the Database V1 commits.

## Assumptions

- Version 1 delivers design, scripts, documentation, and validation fixtures; runtime data access remains Version 2 work.
- PostgreSQL's default UTF-8 database encoding is used; the schema stores Unicode text without database-specific collation changes.
- Usernames and email addresses are unique case-insensitively through normalized indexes, while display casing is preserved.
- Password values in development data are BCrypt-formatted demonstration hashes and are not valid production credentials.
- Presence is a future runtime concern; activity timestamps can support last-activity indicators but do not establish a live presence protocol.
- Cross-table authorization rules, direct-conversation membership synchronization, group-owner/admin guarantees, report eligibility from prior interaction, and unsend authorization are enforced transactionally by the backend in Version 2.
- Future end-to-end encryption requires key-management entities and ciphertext policies that are intentionally excluded from the twelve-table Version 1 scope.
