# Tasks: Database Schema Version 1

**Input**: Design documents from `/specs/003-database-schema-v1/`

**Prerequisites**: `spec.md`, `plan.md`, `research.md`; Phase 01 adds `data-model.md`, `contracts/schema-contract.md`, and `quickstart.md`

**Tests**: Schema contract tests and static validation are required by the feature specification and are implemented before seed/setup completion.

**Organization**: Tasks map one-to-one to the authorized 16-phase roadmap. User-story tasks remain dependency ordered because all edit the same cumulative schema file.

## Phase 1: Setup and Planning

**Purpose**: Establish scope, artifacts, dependencies, validation strategy, and atomic-commit roadmap.

- [x] T001 Create the approved Database V1 specification, plan, research, roadmap, quality checklist, and phase tracker in `specs/003-database-schema-v1/`, `docs/database/IMPLEMENTATION_PLAN_V1.md`, and `AI-LOG.md`

---

## Phase 2: Foundational Design

**Purpose**: Freeze the twelve-entity model and UI mappings before SQL implementation.

- [x] T002 Produce the ERD, DBML, detailed data model, schema contract, UI mappings, and validation quickstart in `docs/database/`, `specs/003-database-schema-v1/data-model.md`, `specs/003-database-schema-v1/contracts/schema-contract.md`, and `specs/003-database-schema-v1/quickstart.md`

**Checkpoint**: The complete conceptual and logical design is reviewable before physical schema changes.

---

## Phase 3: User Story 1 - Preserve Accounts and Relationships (Priority: P1) 🎯 MVP

**Goal**: Implement authoritative identity, audit, request, friendship, and blocking structures.

**Independent Test**: Static and runtime checks prove valid identity/relationship records are accepted and invalid state, self-reference, or duplicate records are rejected.

- [x] T003 [US1] Implement the `users` table, constraints, and indexes in `script/schema.sql`
- [x] T004 [US1] Implement the `login_history` table, constraints, and indexes in `script/schema.sql`
- [x] T005 [US1] Implement the directed `friend_requests` table, lifecycle constraints, duplicate safeguards, and indexes in `script/schema.sql`
- [x] T006 [US1] Implement canonical `friendships` and directed `user_blocks` tables with self/duplicate prevention and indexes in `script/schema.sql`

**Checkpoint**: Account and relationship data can be represented without stored calculated metrics.

---

## Phase 4: User Story 2 - Preserve Conversation History (Priority: P1)

**Goal**: Implement direct/group conversation structure and the distinct message lifecycle actions required by the UI.

**Independent Test**: Static and runtime checks prove pair uniqueness, membership lifecycle validity, ordered messages, recall metadata, user-specific deletion, and history-clearing boundaries.

- [x] T007 [US2] Implement the direct/group `conversations` table, ownership/encryption metadata, constraints, and indexes in `script/schema.sql`
- [x] T008 [US2] Implement `direct_conversations` and `conversation_members` with canonical pairs, roles, join/leave state, history clearing, and indexes in `script/schema.sql`
- [ ] T009 [US2] Implement `messages` with searchable content, sender/conversation references, timestamps, unsend metadata, constraints, and indexes in `script/schema.sql`
- [ ] T010 [US2] Implement per-user `message_deletions` with uniqueness and indexes in `script/schema.sql`

**Checkpoint**: Delete-for-me, clear-history, and unsend remain separate persisted facts.

---

## Phase 5: User Story 3 - Support Administration and Analytics (Priority: P2)

**Goal**: Implement moderation workflow facts and source events for reproducible administration metrics.

**Independent Test**: Static and runtime checks prove report/account-state independence and allow date-bounded event and distinct-user aggregation.

- [ ] T011 [US3] Implement `spam_reports` with reporter/reported/reviewer references, optional message evidence, review lifecycle constraints, and indexes in `script/schema.sql`
- [ ] T012 [US3] Implement `user_activity_logs` with event/context consistency constraints and analytics indexes in `script/schema.sql`

**Checkpoint**: All twelve core tables are present and support the required Admin views.

---

## Phase 6: Validation, Development Setup, and Review

**Purpose**: Prove the integrated design, supply safe fictional data/setup, and finalize review evidence.

- [ ] T013 Create static and isolated PostgreSQL schema contract tests and record actual results in `script/tests/` and `docs/database/VALIDATION_V1.md`
- [ ] T014 Add representative referentially consistent fictional data for all twelve tables in `script/seed.sql`
- [ ] T015 Implement safe configurable PostgreSQL initialization and command documentation in `scripts/init-database.ps1` and `docs/database/SETUP_V1.md`
- [ ] T016 Complete the final cross-artifact, UI compatibility, build, formatting, schema, seed, Git, and documentation review in `docs/database/COMPLETION_REPORT_V1.md`

---

## Dependencies and Execution Order

- T001 → T002 establishes planning before design.
- T002 → T003 freezes design before physical implementation.
- T003 → T004, T005, T006 establishes referenced users; T005 precedes the final relationship review in T006.
- T003 → T007 establishes conversation ownership; T007 → T008 → T009 → T010 establishes conversation, membership, message, then deletion dependencies.
- T009 → T011 permits optional report evidence; T008 → T012 permits conversation activity references.
- T003–T012 → T013 validates the integrated twelve-table schema.
- T013 → T014 → T015 → T016 supplies seed data, safe setup, then final evidence.

## Parallel Opportunities

The authorized roadmap requires sequential phase commits and most implementation tasks edit `script/schema.sql`; therefore no implementation task is marked `[P]`. Read-only validation commands may run concurrently when they do not mutate files.

## Implementation Strategy

Complete one task per roadmap phase, validate the phase, update this checkbox and `AI-LOG.md`, inspect the staged diff, and create exactly the specified Conventional Commit. Stop after T016; Version 2 work is excluded.
