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

## 2026-10-26 — Establish Admin UI Version 1 planning and workflow
Tool: Antigravity (Gemini 3.8 Flash).
Asked for: Analyze framework and requirements for Version 1 Admin Client UI (Member C scope), establish collaborative design workflow with Stitch/Figma and Spec Kit, decompose 9 admin screens, document alignment in `Plan_Admin_UI_Version_1.md`, and switch to `feature/admin-ui-v1` branch.
Kept: The collaborative 5-stage workflow (spec prompt generation, Stitch visual mockup, mockup/icon intake, Spec Kit plan & tasks, per-screen implementation with separate commits), dedicated `docs/Plan_Admin_UI_Version_1.md`, and directory scaffolding for `docs/mockups/` and `docs/screenshots/admin/`.
Changed: Created `docs/Plan_Admin_UI_Version_1.md`, added directories for mockups and screenshots, and updated `AI-LOG.md`.
Rejected: Merging all screens into a single commit or single unstructured file, jumping directly to code without visual design alignment, and accessing PostgreSQL directly from the client.
By hand: Reviewed and approved the workflow agreement, decided on per-screen commits, and directed creation of the feature branch.

## 2026-10-26 — Implement Admin Client UI Version 1
Tool: Antigravity.
Asked for: Generate Spec Kit planning artifacts and implement the Version 1 desktop UI for `admin-client` using Java Swing, FlatLaf, and JFreeChart with in-memory mock data across the 10 planned tasks.
Kept: The 10 UI components and mock data layer under `com.chat.admin`, covering login, main navigation, user/group/spam management, registration/friend/activity statistics, and annual charts.
Changed: Adjusted layout details and stripped unnecessary infrastructure metrics from initial mockups to match project requirements.
Rejected: Direct database connections or backend business logic in Version 1.
By hand: Reviewed each screen step-by-step, verified the packaged `admin.jar`, and approved each commit on `feature/admin-ui-v1`.

## 2026-10-29 — Refine Admin Client UI layout and auto-filtering from review feedback
Tool: Antigravity.
Asked for: Address UI review feedback on `feature/admin-ui-v1`: remove temporary Stitch mockup/plan files, fix login dialog footer text overflow, remove the right-side version text from the main status footer, remove requirement reference notes from panel footers, widen date input fields, enforce fixed `/` separators (`yyyy/MM/dd`) with strict calendar/range validation and error highlighting, replace redundant manual filter/refresh buttons with automatic filtering and chart updates while preserving reset buttons, and improve responsive behavior when resizing windows.
Kept: All existing Version 1 business logic, mock data models, navigation structure, and reset actions across all panels.
Changed: Updated `AdminLoginDialog`, `AdminMainFrame`, `AdminUIStyle`, `UserManagementPanel`, `UserDetailHistoryDialog`, `GroupManagementPanel`, `SpamReportPanel`, `NewRegistrationPanel`, `FriendStatsPanel`, `UserActivityPanel`, `RegistrationChartPanel`, `ActivityChartPanel`, and regenerated `docs/screenshots/admin/`.
Rejected: Modifying functional requirements, changing layout architecture, or removing reset controls.
By hand: Provided the detailed UI review feedback in `feedback.txt`, removed the mockup/plan files from `docs/`, and reviewed the incremental commits.



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

## 2026-10-30 — Phase 11: User activity schema

- **Tool**: OpenAI Codex
- **Task performed**: Added `user_activity_logs` for app-open, direct-chat, and group-chat source events with user/conversation context, timestamp, consistency checks, and analytics indexes.
- **Files changed**: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Stored source events rather than calculated user counters; required no conversation for app opens and a conversation for chat activity; indexed user, event type, conversation, and time for range/distinct aggregation.
- **AI-generated changes accepted/rejected**: Accepted event-level facts supporting `COUNT(DISTINCT ...)`. Rejected monthly summary tables, counters on `users`, and a durable online/offline flag.
- **Validation performed**: Checked all twelve cumulative tables, activity foreign keys/delete actions, three event values, context consistency, and three analytics indexes; confirmed no thirteenth table; reviewed `git diff --check` and staged content before commit.
- **Actual test results**: Focused static schema assertions passed with exactly twelve table definitions. Integrated runtime and normalization validation remain scheduled for Phase 12.
- **Remaining limitations**: Event deduplication and conversation-type matching require Version 2 transaction logic; activity events do not constitute live presence.
- **Corresponding commit message**: `feat(db): add user activity schema`

## 2026-10-30 — Phase 12: Schema validation

- **Tool**: OpenAI Codex
- **Task performed**: Added portable static schema validation, a transactional PostgreSQL fixture with valid and invalid cases, and a validation report covering constraints, indexes, referential behavior, and normalization.
- **Files changed**: `script/tests/validate-schema.ps1`, `script/tests/validate-schema.sql`, `docs/database/VALIDATION_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Required static validation everywhere and real SQL execution only in a newly created isolated database; tested cascade, set-null, and restrict semantics; rolled back all fixture rows.
- **AI-generated changes accepted/rejected**: Accepted a disposable local PostgreSQL cluster after the normal endpoint was unavailable. Rejected modifying/starting an unknown existing cluster, using production data, or claiming unavailable validation.
- **Validation performed**: Ran static contract checks and loaded the full schema plus transactional fixture into PostgreSQL 18.4 on a trust-only loopback disposable cluster; dropped the database, stopped the server, and removed the verified cluster directory.
- **Actual test results**: PASS — 12 tables, 12 primary keys, 25 foreign keys, 34 checks, 33 explicit indexes; all valid integrated inserts succeeded; 14 representative invalid/delete-action cases behaved as specified; final transaction rolled back.
- **Remaining limitations**: No separate PostgreSQL 14–17 matrix or production-scale benchmark; documented cross-table authorization rules remain Version 2 work.
- **Corresponding commit message**: `test(db): add schema validation tests`

## 2026-10-30 — Phase 13: Development seed data

- **Tool**: OpenAI Codex
- **Task performed**: Replaced the seed placeholder with deterministic fictional development data covering administrators, normal/locked users, every relationship state, direct/group conversations, membership roles, messages and lifecycle actions, reports, and activity events across all twelve tables.
- **Files changed**: `script/seed.sql`, `docs/database/VALIDATION_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Used reserved `example.test` emails, fictional profiles, BCrypt fixture hashes without published plaintext, explicit IDs for readable relationships, and sequence resets for safe subsequent inserts.
- **AI-generated changes accepted/rejected**: Accepted representative development-only coverage. Rejected real personal data, production credentials, plaintext passwords, secrets, and partial/conflict-ignoring seed behavior.
- **Validation performed**: Loaded schema and seed with `ON_ERROR_STOP=1` into a fresh PostgreSQL 18.4 disposable database; queried every table count and verified identity sequences advance beyond fixture IDs; dropped the database, stopped the server, and removed the cluster.
- **Actual test results**: PASS — all twelve tables populated (56 total rows), all constraints/foreign keys accepted the data, and identity-sequence checks passed.
- **Remaining limitations**: Seed data is for local demonstration only; authentication workflows and credential provisioning remain Version 2 responsibilities.
- **Corresponding commit message**: `feat(db): add development seed data`

## 2026-10-30 — Phase 14: Database initialization

- **Tool**: OpenAI Codex
- **Task performed**: Added a configurable PowerShell initializer and setup guide for new or explicitly approved empty PostgreSQL databases, with optional fictional seed loading and clear failure handling.
- **Files changed**: `scripts/init-database.ps1`, `docs/database/SETUP_V1.md`, `docs/database/VALIDATION_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Delegated authentication to PostgreSQL; accepted only validated database names/ports; refused existing databases by default; permitted `-UseExistingEmpty` only after counting zero non-system tables; never auto-dropped a database.
- **AI-generated changes accepted/rejected**: Accepted explicit empty-database opt-in and preserved native tool output. Rejected embedded/password parameters, automatic drop/recreate, silent schema merging, and production migration behavior.
- **Validation performed**: Parsed the script, scanned for embedded password assignment, and executed new+seed, existing refusal, approved-empty, and nonempty-refusal scenarios in a disposable PostgreSQL 18.4 cluster; cleaned both databases and the cluster.
- **Actual test results**: PASS — new database produced 12 tables/56 seed rows; default existing refusal left 12 tables unchanged; approved empty database produced 12 tables; nonempty opt-in was refused; cleanup passed.
- **Remaining limitations**: Backup/restore, migration, production deployment, and secure local credential provisioning are operational responsibilities outside V1.
- **Corresponding commit message**: `build(db): add PostgreSQL initialization script`

## 2026-10-30 — Phase 15: Final documentation and review

- **Tool**: OpenAI Codex
- **Task performed**: Completed cross-artifact, schema, seed, initialization, UI compatibility, build, formatting, scope, commit-history, and working-tree review; finalized Spec Kit status and produced the Database V1 completion report.
- **Files changed**: `docs/database/COMPLETION_REPORT_V1.md`, `specs/003-database-schema-v1/spec.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.
- **Design decisions**: Declared Database V1 ready for technical review while preserving explicit Version 2 boundaries and reporting the unrelated baseline Spotless failure rather than modifying Java source.
- **AI-generated changes accepted/rejected**: Accepted the evidence-based completion report and final status updates. Rejected unrelated Java formatting, performance claims without benchmarks, Version 2 implementation, remote Git operations, and fabricated test/commit evidence.
- **Validation performed**: Ran Spec Kit analysis, static schema validation, `mvn clean verify`, `mvn spotless:check`, module/file/table/task/log/commit-history checks, `git diff --check`, staged diff review, and final branch/status inspection.
- **Actual test results**: PASS — schema static checks (12 tables, 12 PKs, 25 FKs, 34 checks, 33 indexes), prior PostgreSQL runtime/seed/initializer suites, Maven reactor build, and 7 Java tests. FAIL — Spotless found 10 pre-existing formatting violations in `common`; no unrelated formatting was applied.
- **Remaining limitations**: Baseline Java formatting violations, no PostgreSQL 14–17 matrix, no production-scale benchmark, and all documented Version 2 transaction/runtime responsibilities remain outstanding outside this feature.
- **Corresponding commit message**: `docs(db): finalize database v1 documentation`

## 2026-10-30 — Database V1 — Post-Review Remediation

- **Tool**: OpenAI Codex
- **Task requested**: Investigate and remediate the Database V1 review findings for setup guidance, PostgreSQL restriction-test portability, recalled-message consistency, block-aware user search, atomic initialization, message-search behavior, and group rejoining; synchronize SpecKit/documentation and create one atomic commit.
- **Issues investigated**: Confirmed that the setup guide instructed a contradictory initializer rerun; the restriction test caught only `restrict_violation`; recalled seed/test rows retained source-like content; user-search mapping excluded only blocks created by the searcher; schema and seed were separate `psql` invocations; full-text search did not provide arbitrary substring matching; and unique membership rows lacked documented reactivation semantics.
- **Files changed**: `script/schema.sql`, `script/seed.sql`, `script/tests/validate-schema.sql`, `script/tests/validate-schema.ps1`, `scripts/init-database.ps1`; `docs/database/SETUP_V1.md`, `docs/database/UI_DATA_MAPPING_V1.md`, `docs/database/VALIDATION_V1.md`, `docs/database/COMPLETION_REPORT_V1.md`, `docs/database/ERD_V1.md`, `docs/database/schema-v1.dbml`; `specs/003-database-schema-v1/spec.md`, `plan.md`, `tasks.md`, `data-model.md`, `contracts/schema-contract.md`, `quickstart.md`, `checklists/requirements.md`; and `AI-LOG.md`.
- **Changes accepted/rejected**: Accepted canonical `[Message unsent]` redaction, specific `foreign_key_violation OR restrict_violation` handling, one-transaction schema/seed application, bidirectional block exclusion, documented `LIKE` substring strategy, and membership-row reactivation. Rejected catch-all exception handling, schema/table redesign, `pg_trgm`, Java services, UI changes, automatic database drops, embedded credentials, and a thirteenth membership-history table.
- **Design decisions**: Global recall retains message identity/conversation/sender/timestamps while replacing `body`; delete-for-me and history clearing remain independent. Seed SQL relies on the caller's `--single-transaction` so schema and seed can share one transaction. Version 2 must perform recall redaction atomically, enforce block filtering server-side, escape substring patterns, and reactivate the unique membership row while accepting V1's loss of multiple membership episodes.
- **Tests executed**: Static schema validator; PowerShell parser; isolated PostgreSQL 18.4 seeded/schema-only/separate-seed initialization; runtime SQL fixture; existing-database refusal; deliberate schema and seed failure rollback; recalled-body query; disposable-cluster cleanup; repository-local Maven 3.9.9 `clean verify` and follow-up `verify`; `spotless:check`; Git diff/status checks.
- **Actual test results**: PASS — static validation reported 12 tables, 12 PKs, 25 FKs, 34 checks, and 33 indexes; PostgreSQL 18.4 produced 12 tables/56 seed rows, stored only `[Message unsent]` for the recalled fixture, passed all runtime cases, refused the existing database unchanged, rolled failed schema application back to 0 tables, and rolled failed seed loading back to 0 tested rows; cleanup removed the isolated cluster. PASS — Maven reactor and 7 tests completed with 0 failures/errors/skips. FAIL — Spotless still reports the same 10 pre-existing `common` Java formatting violations; no Java file was modified. The first unqualified `mvn clean verify` attempt did not start because `mvn` was absent from `PATH`; the repository-local Maven executable was used successfully.
- **Remaining limitations**: Only PostgreSQL 18.4 was runtime-tested; PostgreSQL 14–17 portability is implemented through named SQLSTATE conditions but not claimed as version-matrix evidence. Full-text search remains token-based, substring `LIKE` may scan more rows, V1 membership stores only one interval, and Version 2 backend authorization/transaction behavior remains unimplemented by design. The unrelated baseline Spotless violations remain.
- **Corresponding commit message**: `fix(db): address v1 review findings and validation portability`
