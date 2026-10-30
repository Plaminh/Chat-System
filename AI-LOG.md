# AI-LOG.md — Java Chat System (CSC13008)

## 2026-10-22 — Initialize Java chat framework
Tool: ChatGPT (architecture planning and Codex prompt); OpenAI Codex (VS Code, code generation).
Asked for: Initialize a Java 21 Maven multi-module chat-system foundation with four modules (`common`, `admin-client`, `user-client`, `server`), Swing/FlatLaf clients, a non-web Spring Boot TCP Socket server, Spring JDBC/PostgreSQL configuration, and a minimal PING/PONG test flow.
Kept: The four-module structure, shared JSON protocol/DTOs, separate Admin/User Swing entry points, TCP Socket server skeleton, external configuration examples, Maven build/packaging setup, and executable JAR outputs.
Changed: Follow-up corrections were requested after auditing the generated foundation; these are recorded in the next entry. No manual source-code rewrite was reported for initialization.
Rejected: A separate `client-common` module, WebSocket, REST endpoints, JPA/Hibernate, microservices, and prematurely implementing the full chat business features.
By hand: Chose the scope and technology stack, reviewed the generated folder tree and build output. No hand-written source code is claimed for this task.

## 2026-10-22 — Audit and fix framework initialization
Tool: ChatGPT (audit checklist and fix prompt); OpenAI Codex (VS Code, audit and changes).
Asked for: Audit the four-module project, run the build and basic Socket checks, then fix only the invalid JSON request handling and executor shutdown issues identified by the audit.
Kept: Codex's minimal fixes in `RequestHandler.java`, `TcpSocketServer.java`, and `RequestHandlerTest.java`: JSON `null`/non-object requests now return `INVALID_REQUEST`; shutdown waits up to five seconds per executor and preserves interrupt status.
Changed: Limited the follow-up to the three affected files and regression tests instead of a broader refactor; the code changes were made by Codex, not claimed as manual edits.
Rejected: Optional changes to idle timeouts, future server-push write synchronization, harmless Maven Shade warnings, and any additional features or frameworks at this stage.
By hand: Reviewed the reported audit and verification results. Codex reported `mvn clean verify` passing (7 tests, 0 failures) and packaged TCP PING/PONG passing; manual GUI inspection and a live PostgreSQL connection test are not yet documented. Git baseline commit is still pending.

## 2026-10-22 — Integrate GitHub Spec Kit with Codex
Tool: OpenAI Codex (VS Code)
Asked for: Integrate GitHub Spec Kit with Codex into the existing Java Chat System repository.
Kept: Official Spec Kit 1.2.2 shared infrastructure, PowerShell scripts, Codex skills, project constitution, and concise Spec-Driven Development guidance in the README.
Changed: Upgraded the existing `specify-cli` installation from 0.10.2 to 1.2.2 because the installed version was behind the current official package; replaced only the generated constitution placeholder with project-specific principles and appended documentation without changing application code.
Rejected: Feature specifications, implementation tasks, feature implementation, Git automation, additional frameworks, and changes to the existing four-module Java architecture.
By hand: None. The developer requested and will review the setup; Codex performed the CLI upgrade, initialization, documentation updates, and verification.

## 2026-10-22 — Configure formatting and AI guardrails
Tool: OpenAI Codex (VS Code)
Asked for: Add minimal Java formatting, EditorConfig defaults, and repository guardrails without changing application behavior or introducing broader quality tooling.
Kept: Parent-level Spotless configuration using Google Java Format, root `.editorconfig`, concise root `AGENTS.md`, and usage guidance in the README.
Changed: Added formatter plugin management and inherited configuration without binding it to compilation or verification; existing Java sources were checked but not automatically reformatted.
Rejected: Checkstyle, SpotBugs, PMD, SonarQube, Git hooks, CI/CD, additional formatters, application refactoring, and automatic `spotless:apply`.
By hand: None. Codex performed the configuration, documentation updates, and verification; the developer remains responsible for reviewing and deciding whether to apply reported formatting changes.

## 2026-10-30 — Phase 00: Database V1 planning

- **Tool**: OpenAI Codex
- **Task performed**: Inspected the target branch, repository governance, four-module architecture, SQL/documentation placeholders, Spec Kit configuration, AI history, and Admin/User UI requirements from read-only Git refs; initialized the Database V1 specification, plan, research, task tracker, quality checklist, and implementation roadmap.
- **Files changed**: `specs/003-database-schema-v1/spec.md`, `specs/003-database-schema-v1/plan.md`, `specs/003-database-schema-v1/research.md`, `specs/003-database-schema-v1/tasks.md`, `specs/003-database-schema-v1/checklists/requirements.md`, `docs/database/IMPLEMENTATION_PLAN_V1.md`, `AI-LOG.md`.
- **Design decisions**: Fixed scope at exactly twelve core tables; preserved numeric database identities and UI display-ID mappings; separated recall, per-user deletion, and history clearing; deferred cross-table authorization and E2EE key management to Version 2.
- **AI-generated changes accepted/rejected**: Accepted the specification-driven roadmap and native PostgreSQL constraint strategy. Rejected additional tables, triggers, stored procedures, Java changes, protocol changes, branch changes, and Version 2 behavior.
- **Validation performed**: Confirmed branch `feature/database-schema-v1`, clean initial working tree, four Maven modules, placeholder SQL state, complete requirements checklist (16/16 checked), required planning artifacts, and no SQL implementation in this phase; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Planning-only phase; no runtime tests were applicable. Artifact and checklist validation passed.
- **Remaining limitations**: Physical data model, SQL schema, runtime database validation, seed data, initialization script, and final review remain for Phases 01–15.
- **Corresponding commit message**: `docs(db): establish database v1 implementation plan`

## 2026-10-30 — Phase 01: Database design

- **Tool**: OpenAI Codex
- **Task performed**: Defined the conceptual, logical, and planned physical model for exactly twelve core tables; produced an ERD, DBML, detailed field/constraint model, schema contract, validation quickstart, and Admin/User UI mappings.
- **Files changed**: `specs/003-database-schema-v1/data-model.md`, `specs/003-database-schema-v1/contracts/schema-contract.md`, `specs/003-database-schema-v1/quickstart.md`, `docs/database/ERD_V1.md`, `docs/database/schema-v1.dbml`, `docs/database/UI_DATA_MAPPING_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Adopted canonical low/high IDs for unordered relationships; specialized direct conversations without adding a table; separated unsending, delete-for-me, and history clearing; used source activity events and derived counts; documented restricted account-deletion cases and Version 2 transaction rules.
- **AI-generated changes accepted/rejected**: Accepted the twelve-table normalized model and explicit delete behavior. Rejected stored friend/activity counters, presence persistence, report/account-status conflation, triggers, procedures, extra key-management tables, and UI source changes.
- **Validation performed**: Cross-checked all twelve tables across ERD, DBML, data model, schema contract, plan, and UI mappings; confirmed all identifiers/states and lifecycle semantics are documented; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Documentation consistency checks passed; no SQL existed yet for runtime validation.
- **Remaining limitations**: Cross-table business rules require Version 2 backend transactions; the encryption flag is metadata only; PostgreSQL runtime validation remains pending until schema implementation.
- **Corresponding commit message**: `docs(db): define database model and ERD`

## 2026-10-30 — Phase 02: Users schema

- **Tool**: OpenAI Codex
- **Task performed**: Replaced the schema placeholder with PostgreSQL session settings and the `users` table, including authentication/profile fields, roles, account state, timestamps, constraints, and query indexes.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Used numeric identity keys; case-insensitive unique indexes for username/email; normalized nullable gender; explicit `active`/`locked` consistency with `locked_at`; retained only source attributes and no friend/activity counters.
- **AI-generated changes accepted/rejected**: Accepted native checks and expression indexes. Rejected plaintext passwords, UI display IDs as keys, client credentials, stored metrics, and database triggers for `updated_at`.
- **Validation performed**: Checked one `users` table definition, named constraints, five expected indexes, absence of other core tables, and PostgreSQL CLI availability; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed. PostgreSQL runtime execution was not performed in this phase and remains scheduled for Phase 12.
- **Remaining limitations**: `updated_at` maintenance and password hashing are Version 2 server responsibilities; later tables and integrated runtime validation remain pending.
- **Corresponding commit message**: `feat(db): add users schema`

## 2026-10-30 — Phase 03: Login history schema

- **Tool**: OpenAI Codex
- **Task performed**: Added `login_history` with numeric identity, user ownership, login/logout timestamps, chronological indexes, and explicit cascading cleanup.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Kept user name/full name normalized in `users` rather than duplicated; allowed open sessions with `logged_out_at = NULL`; rejected logout times before login.
- **AI-generated changes accepted/rejected**: Accepted compact session-boundary history. Rejected client/device metadata not required by Version 1 and denormalized display fields.
- **Validation performed**: Checked two cumulative table definitions, the user foreign key with `ON DELETE CASCADE`, the timestamp constraint, and both login-history indexes; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed. Runtime execution remains deferred to the isolated Phase 12 fixture.
- **Remaining limitations**: Login/logout recording behavior and authentication are Version 2 server work.
- **Corresponding commit message**: `feat(db): add login history schema`

## 2026-10-30 — Phase 04: Friend requests schema

- **Tool**: OpenAI Codex
- **Task performed**: Added directed `friend_requests` with sender/receiver references, normalized lifecycle states, response timestamps, self-request prevention, and pending-request duplicate protection.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Preserved historical final requests while allowing only one pending request for an unordered user pair, preventing simultaneous reverse requests through a partial expression index.
- **AI-generated changes accepted/rejected**: Accepted the `LEAST`/`GREATEST` pending-pair index. Rejected permanent uniqueness across all request history and extra request-message fields.
- **Validation performed**: Checked three cumulative tables, both user foreign keys, four allowed states, self-request and response-time constraints, partial unique pending-pair index, and sender/receiver query indexes; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed. Runtime constraint tests remain scheduled for Phase 12.
- **Remaining limitations**: Accepting/declining requests and atomic friendship creation are Version 2 transaction responsibilities.
- **Corresponding commit message**: `feat(db): add friend requests schema`

## 2026-10-30 — Phase 05: Friendships and blocking schema

- **Tool**: OpenAI Codex
- **Task performed**: Added canonical unordered `friendships` and directed `user_blocks`, including explicit user/request references, self-relationship prevention, duplicate safeguards, and reverse-lookup indexes.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Required `user_low_id < user_high_id` for friendships; allowed at most one friendship per accepted request; kept blocks directional and independent from friendship/request cleanup policy.
- **AI-generated changes accepted/rejected**: Accepted relational source facts and canonical pairs. Rejected duplicated bidirectional friendship rows, stored friend counts, and automatic trigger-based block reconciliation.
- **Validation performed**: Checked five cumulative tables, all relationship foreign keys/delete actions, canonical/self checks, pair/request uniqueness, and four supporting indexes; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed. Runtime duplicate/self-reference tests remain scheduled for Phase 12.
- **Remaining limitations**: Version 2 must canonicalize IDs and atomically reconcile requests, friendships, and blocks.
- **Corresponding commit message**: `feat(db): add friendship and blocking schemas`

## 2026-10-30 — Phase 06: Conversations schema

- **Tool**: OpenAI Codex
- **Task performed**: Added shared `conversations` metadata for direct and group chats, including conditional name/ownership rules, encryption-status metadata, lifecycle timestamps, and filter indexes.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Required direct conversations to have no group name/owner and groups to have both; restricted deletion of a group owner until Version 2 transfers or removes ownership; treated encryption as a status flag only.
- **AI-generated changes accepted/rejected**: Accepted one shared conversation supertype. Rejected separate direct/group message tables, embedded member arrays, E2EE key columns, and automatic timestamp triggers.
- **Validation performed**: Checked six cumulative tables, type/shape/timestamp constraints, owner foreign-key restriction, encryption default, and three conversation indexes; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed. Runtime shape tests remain scheduled for Phase 12.
- **Remaining limitations**: Group ownership/admin membership synchronization and effective encryption are Version 2 responsibilities.
- **Corresponding commit message**: `feat(db): add conversations schema`

## 2026-10-30 — Phase 07: Conversation membership schema

- **Tool**: OpenAI Codex
- **Task performed**: Added `direct_conversations` for unique canonical one-to-one pairs and `conversation_members` for role, membership interval, and per-user history-clearing state.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Used the parent conversation ID as the direct specialization primary key; restricted deletion of users still named in a direct pair; allowed one membership row per conversation/user with explicit leave and clear-history timestamps.
- **AI-generated changes accepted/rejected**: Accepted specialization plus shared membership. Rejected participant arrays, duplicate direct pairs, a separate history-clear table, and silent orphaning on account deletion.
- **Validation performed**: Checked eight cumulative tables, canonical and unique direct pairs, all foreign-key delete actions, membership roles/timestamp bounds/uniqueness, and four supporting indexes; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed. Runtime pair/membership tests remain scheduled for Phase 12.
- **Remaining limitations**: Parent type, exactly-two direct members, group owner/admin guarantees, and rejoin policy require Version 2 transactions.
- **Corresponding commit message**: `feat(db): add conversation membership schemas`

## 2026-10-30 — Phase 08: Messages schema

- **Tool**: OpenAI Codex
- **Task performed**: Added `messages` with conversation/sender references, non-blank body, chronological timestamps, global unsend metadata, and history/search indexes.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Retained messages when sender accounts are removed by setting authorship null; required recall time before allowing an optional recall actor; added PostgreSQL simple-text GIN search for plaintext V1 content.
- **AI-generated changes accepted/rejected**: Accepted explicit recall metadata and source body retention. Rejected per-user deletion flags on messages, physical deletion for unsend, message encryption keys, and speculative attachment fields.
- **Validation performed**: Checked nine cumulative tables, all message foreign-key delete actions, body/unsend constraints, chronological/sender indexes, and GIN search index; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed. Runtime send/unsend and search-index validation remain scheduled for Phase 12.
- **Remaining limitations**: Sender membership, unsend authorization/window, recalled-body access, and encrypted-message search policy require Version 2 decisions.
- **Corresponding commit message**: `feat(db): add messages schema`

## 2026-10-30 — Phase 09: Message deletions schema

- **Tool**: OpenAI Codex
- **Task performed**: Added `message_deletions` for per-user delete-for-me state with explicit message/user ownership, deletion time, and unique deletion records.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Kept user-specific hiding separate from `messages.unsent_at` and from membership `history_cleared_at`; cascaded records when either their message or user ceases to exist.
- **AI-generated changes accepted/rejected**: Accepted one junction record per user/message. Rejected a shared deleted flag, duplicate deletion events, and physical message removal.
- **Validation performed**: Checked ten cumulative tables, message/user cascading foreign keys, unique pair constraint, and user deletion-history index; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed. Runtime uniqueness/visibility tests remain scheduled for Phase 12.
- **Remaining limitations**: Participant authorization for delete-for-me is a Version 2 transaction rule.
- **Corresponding commit message**: `feat(db): add message deletion schema`

## 2026-10-30 — Phase 10: Spam reports schema

- **Tool**: OpenAI Codex
- **Task performed**: Added `spam_reports` with reporter/reported account references, optional message evidence, reason, independent review workflow, reviewer/note fields, resolution time, and moderation indexes.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Used `SET NULL` to retain moderation history after related account/message deletion; separated report statuses from account lock state; required resolution time only for terminal statuses.
- **AI-generated changes accepted/rejected**: Accepted nullable retained-audit references and four-state workflow. Rejected an `account_locked` report status, implicit sanctions, self-reporting, and mandatory message evidence.
- **Validation performed**: Checked eleven cumulative tables, four foreign keys/delete actions, self-report/reason/status/resolution constraints, and five moderation indexes; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed. Runtime workflow and retained-history tests remain scheduled for Phase 12.
- **Remaining limitations**: Prior-interaction eligibility, reviewer authorization, and sanction transactions are Version 2 responsibilities.
- **Corresponding commit message**: `feat(db): add spam reports schema`
