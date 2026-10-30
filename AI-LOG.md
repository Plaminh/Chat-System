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
