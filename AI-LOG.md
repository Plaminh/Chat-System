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

