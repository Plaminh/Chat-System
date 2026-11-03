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

## 2026-10-28 — Plan User Client UI Version 1

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Read `project.txt` and `Plan_Admin_UI_Version_1.md`, then create a corresponding Version 1 implementation plan for the User Client UI.

Kept: The project's User requirements, Java 21/Swing/FlatLaf stack, client/server boundaries, mock-only Version 1 scope, and the Admin plan's staged workflow and screen-by-screen decomposition.

Changed: Added `docs/Plan_User_UI_Version_1.md` with a 14-part UI breakdown covering account, friends, search, direct/group chat, spam reporting, and mock LLM assistance; documented unresolved chat deletion, email, LLM-key, and encryption boundaries without deciding new backend policy.

Rejected: Implementing UI code, adding dependencies/modules, connecting to PostgreSQL or TCP, or integrating SMTP, Groq, or cryptography as part of this documentation task.

By hand: The developer remains responsible for reviewing the proposed screen breakdown, mockups, and any future implementation.

## 2026-10-28 — Specify User Client UI Version 1

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Complete Phase 1 of the User UI plan by analyzing the project requirements, defining user journeys/screen scope, and preparing visual-design prompts.

Kept: The scope and constraints in `project.txt`, the User UI plan, and the project constitution; preserved the requirement that client UI remain separate from server persistence and that mock behavior not be represented as live functionality.

Changed: Created `specs/002-user-client-ui/spec.md`, its requirements quality checklist, and `prompt_Stitch.md`; recorded `specs/002-user-client-ui` as the active Spec Kit feature directory.

Rejected: Creating/switching branches, implementing screens, resolving unspecified deletion semantics, integrating email/LLM/networking/encryption, or changing project modules and dependencies.

By hand: The developer remains responsible for reviewing and approving the feature scope and design prompts before Phase 2.

## 2026-10-28 — Prepare Stitch prompt to align User UI with Admin UI

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Prepare a prompt for the Stitch design agent to visually refine generated User screens so they match the completed Admin UI; place the prompt in a temporary Markdown file beside `project.txt` for later removal.

Kept: Existing User screen scope and mockup requirements; used the completed Admin workspace and login screenshots as visual references and preserved the distinction between Admin and User features.

Changed: Added temporary root-level `PROMPT_STITCH_USER_UI_ALIGN_ADMIN_TEMP.md` with a master visual-alignment prompt, usage instructions, and focused prompts for account, workspace/friends, chat, groups, and dialogs.

Rejected: Editing the Stitch project directly, changing User requirements, or implementing application code.

By hand: The developer will attach the Admin reference screenshots in Stitch, apply the prompts to the existing User screens, review the results, and remove the temporary prompt file when it is no longer needed.

## 2026-10-28 — Document User mockup handoff steps

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Correct the earlier six-folder example and write a Markdown guide for saving the 12 User UI screens as `code.html`, `screen.png`, and `DESIGN.md` inside the User mockup folder already created by the developer.

Kept: The existing six User mockup category folders and the requirement to preserve all 12 project User function groups.

Changed: Added `docs/mockups/002-user-client-ui/HUONG_DAN_LUU_12_SCREENS.md`, clarifying that the six existing folders are categories and documenting how to store each of 12 screen handoffs as a nested folder with the three required files.

Rejected: Renaming or deleting existing mockup folders, creating Stitch exports, or assuming that six categories mean only six screens.

By hand: The developer will export the 12 final screen mockups from Stitch and verify each three-file handoff.

## 2026-10-28 — Plan and task User Client UI Version 1

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit plan/tasks workflows.

Asked for: Complete Phase 4 using the 12 User mockups and approved User requirements; produce implementation planning, design artifacts, and an executable task list without implementing application code.

Kept: The existing four-module architecture, Java 21/Swing/FlatLaf stack, User mockup content, project constitution, and the agreed sample-only Version 1 boundary.

Changed: Generated `specs/002-user-client-ui/plan.md`, `research.md`, `data-model.md`, `contracts/ui-components.md`, `quickstart.md`, and `tasks.md`; mapped all 12 approved screens and grouped implementation work under the five feature user stories with focused test and per-task commit guidance.

Rejected: Modifying Java sources, adding dependencies/modules, extending the protocol, changing database schemas, integrating live services, or creating commits/pushing as part of this planning phase.

By hand: The developer remains responsible for reviewing and approving the plan/tasks before implementation; each future task and commit must be reviewed.

## 2026-10-28 — Implement User UI shared theme

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Begin Phase 5 of the User Client UI Version 1 implementation.

Kept: Admin-aligned navy/blue colors, Java Swing/FlatLaf, existing module boundaries, and sample-only scope.

Changed: Added shared User UI color, spacing, typography, card, button, and text-field styling tokens in `UserUiTheme.java`; marked T001 complete.

Rejected: Adding dependencies or changing the Admin client, protocol, server, or database.

By hand: Review the visual styling against the approved User screenshots while implementing the screens.

## 2026-10-28 — Add User sample profile and session

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Continue Phase 5 with the foundational User profile/session model and focused test coverage.

Kept: Only project-required account fields, in-memory sample state, and no password storage or network integration.

Changed: Added immutable `UserProfile`, `UserSessionState` with a clearly synthetic sample profile, and `UserSessionStateTest`; marked T002 complete.

Rejected: Adding persistence, authentication credentials, or server/protocol behavior.

By hand: Review the sample fields and session flow when connecting the account screens.

## 2026-10-28 — Add User workspace navigation shell

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Continue Phase 5 with a shared User workspace shell and navigable feature destinations.

Kept: Admin-aligned desktop chrome, the five approved User navigation destinations, in-memory presentation state, and replaceable feature panels.

Changed: Added the headless-testable `UserMainFrame` workspace panel, `UserNavigationSidebar`, and navigation/registration tests; marked T003 complete.

Rejected: Adding live networking, business logic, or a new UI framework.

By hand: Review the sidebar and workspace composition against the approved User mockups when replacing placeholders with screens.

## 2026-10-28 — Implement User sample sign-in flow

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Continue Phase 5 with the User sign-in screen, required-field checks, sample workspace entry, and application startup wiring.

Kept: Sample-only sign-in with no credential verification or persistence; removed the starter connection-test screen from the User startup path without changing `SocketClient`.

Changed: Added `LoginPanel`, required-field feedback, password-buffer clearing, navigation into `UserMainFrame`, focused tests, and FlatLaf startup wiring; marked T004 complete.

Rejected: Calling the TCP PING helper from the sign-in flow or claiming authentication succeeded.

By hand: Review the login layout and language against the approved login screenshot; registration and recovery links will be wired to their dialogs in the next tasks.

## 2026-10-28 — Add sample account registration dialog

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Continue US1 with registration, required-field validation, and local sample submission/cancellation.

Kept: The project account fields, no extra organization/security features, no account persistence, and no password storage.

Changed: Added the registration form/dialog surface with email/date/password-confirmation validation, local profile preview, cancellation, focused tests, and login-link wiring; marked T005 complete.

Rejected: Creating a real account, applying an unspecified password policy, or saving credentials.

By hand: Review the registration field list and validate it against the project specification before connecting any future backend.

## 2026-10-28 — Add sample password recovery dialog

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Continue US1 with a validated password-recovery form and sample-only response.

Kept: Email-based input, neutral feedback, no generated password display, and no email integration.

Changed: Added the recovery form/dialog, email validation, explicit no-email-sent confirmation, focused tests, and login-link wiring; marked T006 complete.

Rejected: Generating or exposing a password, sending email, or asserting that a live recovery request occurred.

By hand: Review the neutral recovery wording against the project’s later account-recovery policy before any server integration.

## 2026-10-28 — Add User profile and password settings

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Complete the US1 profile-editing and password-change sample flows.

Kept: Only required User account fields, username as a stable identity, in-memory profile updates, and no password storage or expiry/security claims.

Changed: Added editable profile fields with save/cancel restoration, a separate password-change dialog with confirmation validation, focused tests, and password-dialog launch from Profile; marked T007 complete.

Rejected: Adding mockup-only phone, organization, bio, password-expiry, 2FA, or device-security content, and claiming any real password change.

By hand: Review profile-field scope against the approved project account model.

## 2026-10-28 — Implement User friends list

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Begin US2 with sample friends, filtering, presence, chat navigation, and confirmed relationship actions.

Kept: In-memory friend state, project-required identity details, and explicit local-only actions.

Changed: Added `FriendEntry`, searchable friends panel with online-only filtering, confirmation-gated unfriend/block, shared blocked-username state, focused tests, and marked T008 complete.

Rejected: Live presence, network actions, or modifying server-side friend relationships.

By hand: Review the sample names and relationships against the approved friend-list design.

## 2026-10-28 — Add User friend requests and search

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Continue US2 with request decisions and blocked-aware search by username/full name.

Kept: Sample-only request state, shared blocked usernames, and local navigation callbacks.

Changed: Added `FriendRequest` statuses, request filtering/accept/decline, user search with explicit empty states, chat/group selection callbacks, focused tests, and marked T009 complete.

Rejected: Live account lookup, network friend requests, or surfacing blocked accounts in search results.

By hand: Review request and search controls against the approved discovery screen.

## 2026-10-28 — Implement personal chat workspace

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Begin US3 with personal conversation state, sample history, and a local composer.

Kept: In-memory ordered messages, sample-only send behavior, and no TCP delivery claim.

Changed: Added `Conversation`, `ChatMessage`, `MessageBubble`, the split conversation/chat workspace, sample fixtures, focused tests, and marked T010 complete.

Rejected: Live message delivery, attachments, persistence, or protocol changes.

By hand: Review the chat split layout and message bubble appearance against the approved chat screenshot.

## 2026-10-28 — Add chat history search and local actions

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Complete US3 history search, result navigation, and the three distinct conversation/message actions.

Kept: Search over local sample messages, confirmation before destructive actions, and neutral local-only semantics.

Changed: Added current/global history search, result navigation callbacks, selection-based Delete for Me and Unsend, confirmed Clear Conversation History, action model, focused tests, and marked T011 complete.

Rejected: Claiming server-side deletion, cross-user visibility, retention, or message withdrawal.

By hand: Review the three action confirmations and their neutral sample wording before future backend integration.

## 2026-10-28 — Implement group creation and management

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Begin US4 with sample groups, participant validation, membership management, and role display.

Kept: In-memory group/member state, minimum one additional participant, and local-only management actions.

Changed: Added `GroupConversation`, `GroupMember`, group listing/creation, member management dialog with add/remove/admin controls, focused tests, and marked T012 complete.

Rejected: Group persistence, invitations over the network, organization features, or real encryption claims.

By hand: Review group membership controls and role semantics against the required group workflows.

## 2026-10-28 — Implement User group chat screen

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Complete US4 with a group message view, local composer, member roles, and a clear encryption placeholder.

Kept: Sample-only messages, current group membership roles, and no cryptographic implementation.

Changed: Added `GroupChatPanel`, group message rendering and local composition, role summary, explicit non-functional encryption wording, focused tests, and marked T013 complete.

Rejected: Real E2EE, key management, live group delivery, or unsupported server semantics.

By hand: Review the group chat split layout and placeholder wording against the approved group-chat screen.

## 2026-10-28 — Add sample spam-report dialog

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Begin US5 with an account report form available only for a prior sample conversation.

Kept: Required report reason, identified target account, and a clearly local-only submission result.

Changed: Added `SpamReportDialog` with prior-interaction and required-reason checks, focused tests, and marked T014 complete.

Rejected: Automatic blocking, report storage, server submission, or claims that a moderator received the report.

By hand: Review the report form wording and any future report categories with the project owner.

## 2026-10-28 — Add writing-assistance preview

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Complete US5 with sample suggestion/revision previews and explicit accept/discard.

Kept: Draft preservation until acceptance, deterministic local fixtures, and user control over insertion.

Changed: Added `WritingAssistDialog` with suggestion/revision modes, local preview generation, explicit accept callback, discard behavior, focused tests, and marked T015 complete.

Rejected: Calling an LLM/API, sending draft text externally, or claiming AI processing occurred.

By hand: Review preview copy and confirm whether future live assistance requires separate privacy and provider decisions.

## 2026-10-28 — Integrate User Client UI Version 1

Tool: AI assistant (Copilot SDK in VS Code), Spec Kit implementation workflow.

Asked for: Integrate the completed User UI screens, wire contextual actions and navigation, and verify the end-to-end sample flows.

Kept: Swing/FlatLaf, local sample state, existing four-module boundaries, and explicit mock-only behavior.

Changed: Connected Chats, Friends, Requests/Search, Groups, and Profile in `UserMainFrame`; wired chat-history search/results, report and writing-assistance dialogs, group chat/back navigation, and group creation from a selected search result. Added integration/regression coverage and fixed recursive test-button lookup helpers.

Rejected: Live authentication, TCP messaging, persistence, email, LLM calls, or claims of real encryption/report submission.

By hand: Reviewed the integration and test results. The VS Code test runner passed all 43 tests; direct `javac --release 21` compilation of the integrated workspace and `git diff --check` passed. Maven verification and GUI launch were not completed because Maven and a Maven wrapper are unavailable; the Java launch tool consequently failed at its Maven build step. No implemented-UI screenshots were created. T016 remains open until Maven verification and visual acceptance evidence can be completed.

## 2026-10-29 — Refine Admin Client UI layout and auto-filtering from review feedback

Tool: Antigravity.

Asked for: Address UI review feedback on `feature/admin-ui-v1`: remove temporary Stitch mockup/plan files, fix login dialog footer text overflow, remove the right-side version text from the main status footer, remove requirement reference notes from panel footers, widen date input fields, enforce fixed `/` separators (`yyyy/MM/dd`) with strict calendar/range validation and error highlighting, replace redundant manual filter/refresh buttons with automatic filtering and chart updates while preserving reset buttons, and improve responsive behavior when resizing windows.

Kept: All existing Version 1 business logic, mock data models, navigation structure, and reset actions across all panels.

Changed: Updated `AdminLoginDialog`, `AdminMainFrame`, `AdminUIStyle`, `UserManagementPanel`, `UserDetailHistoryDialog`, `GroupManagementPanel`, `SpamReportPanel`, `NewRegistrationPanel`, `FriendStatsPanel`, `UserActivityPanel`, `RegistrationChartPanel`, `ActivityChartPanel`, and regenerated `docs/screenshots/admin/`.

Rejected: Modifying functional requirements, changing layout architecture, or removing reset controls.

By hand: Provided the detailed UI review feedback in `feedback.txt`, removed the mockup/plan files from `docs/`, and reviewed the incremental commits.

## 2026-10-30 — Phase 00: Database V1 planning

Tool: OpenAI Codex

Asked for: Inspected the target branch, repository governance, four-module architecture, SQL/documentation placeholders, Spec Kit configuration, AI history, and Admin/User UI requirements from read-only Git refs; initialized the Database V1 specification, plan, research, task tracker, quality checklist, and implementation roadmap.

Kept: Fixed scope at exactly twelve core tables; preserved numeric database identities and UI display-ID mappings; separated recall, per-user deletion, and history clearing; deferred cross-table authorization and E2EE key management to Version 2. Accepted the specification-driven roadmap and native PostgreSQL constraint strategy.

Changed: Files: `specs/003-database-schema-v1/spec.md`, `specs/003-database-schema-v1/plan.md`, `specs/003-database-schema-v1/research.md`, `specs/003-database-schema-v1/tasks.md`, `specs/003-database-schema-v1/checklists/requirements.md`, `docs/database/IMPLEMENTATION_PLAN_V1.md`, `AI-LOG.md`.  
Verification reported: Confirmed branch `feature/database-schema-v1`, clean initial working tree, four Maven modules, placeholder SQL state, complete requirements checklist (16/16 checked), required planning artifacts, and no SQL implementation in this phase; reviewed `git diff --check` and staged content before commit.  
Test results: Planning-only phase; no runtime tests were applicable. Artifact and checklist validation passed.  
Remaining limitations: Physical data model, SQL schema, runtime database validation, seed data, initialization script, and final review remain for Phases 01–15.  
Corresponding commit message: `docs(db): establish database v1 implementation plan`

Rejected: Additional tables, triggers, stored procedures, Java changes, protocol changes, branch changes, and Version 2 behavior.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 01: Database design

Tool: OpenAI Codex

Asked for: Defined the conceptual, logical, and planned physical model for exactly twelve core tables; produced an ERD, DBML, detailed field/constraint model, schema contract, validation quickstart, and Admin/User UI mappings.

Kept: Adopted canonical low/high IDs for unordered relationships; specialized direct conversations without adding a table; separated unsending, delete-for-me, and history clearing; used source activity events and derived counts; documented restricted account-deletion cases and Version 2 transaction rules. Accepted the twelve-table normalized model and explicit delete behavior.

Changed: Files: `specs/003-database-schema-v1/data-model.md`, `specs/003-database-schema-v1/contracts/schema-contract.md`, `specs/003-database-schema-v1/quickstart.md`, `docs/database/ERD_V1.md`, `docs/database/schema-v1.dbml`, `docs/database/UI_DATA_MAPPING_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Cross-checked all twelve tables across ERD, DBML, data model, schema contract, plan, and UI mappings; confirmed all identifiers/states and lifecycle semantics are documented; reviewed `git diff --check` and staged content before commit.  
Test results: Documentation consistency checks passed; no SQL existed yet for runtime validation.  
Remaining limitations: Cross-table business rules require Version 2 backend transactions; the encryption flag is metadata only; PostgreSQL runtime validation remains pending until schema implementation.  
Corresponding commit message: `docs(db): define database model and ERD`

Rejected: Stored friend/activity counters, presence persistence, report/account-status conflation, triggers, procedures, extra key-management tables, and UI source changes.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 02: Users schema

Tool: OpenAI Codex

Asked for: Replaced the schema placeholder with PostgreSQL session settings and the `users` table, including authentication/profile fields, roles, account state, timestamps, constraints, and query indexes.

Kept: Used numeric identity keys; case-insensitive unique indexes for username/email; normalized nullable gender; explicit `active`/`locked` consistency with `locked_at`; retained only source attributes and no friend/activity counters. Accepted native checks and expression indexes.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked one `users` table definition, named constraints, five expected indexes, absence of other core tables, and PostgreSQL CLI availability; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed. PostgreSQL runtime execution was not performed in this phase and remains scheduled for Phase 12.  
Remaining limitations: `updated_at` maintenance and password hashing are Version 2 server responsibilities; later tables and integrated runtime validation remain pending.  
Corresponding commit message: `feat(db): add users schema`

Rejected: Plaintext passwords, UI display IDs as keys, client credentials, stored metrics, and database triggers for `updated_at`.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 03: Login history schema

Tool: OpenAI Codex

Asked for: Added `login_history` with numeric identity, user ownership, login/logout timestamps, chronological indexes, and explicit cascading cleanup.

Kept: Kept user name/full name normalized in `users` rather than duplicated; allowed open sessions with `logged_out_at = NULL`; rejected logout times before login. Accepted compact session-boundary history.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked two cumulative table definitions, the user foreign key with `ON DELETE CASCADE`, the timestamp constraint, and both login-history indexes; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed. Runtime execution remains deferred to the isolated Phase 12 fixture.  
Remaining limitations: Login/logout recording behavior and authentication are Version 2 server work.  
Corresponding commit message: `feat(db): add login history schema`

Rejected: Client/device metadata not required by Version 1 and denormalized display fields.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 04: Friend requests schema

Tool: OpenAI Codex

Asked for: Added directed `friend_requests` with sender/receiver references, normalized lifecycle states, response timestamps, self-request prevention, and pending-request duplicate protection.

Kept: Preserved historical final requests while allowing only one pending request for an unordered user pair, preventing simultaneous reverse requests through a partial expression index. Accepted the `LEAST`/`GREATEST` pending-pair index.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked three cumulative tables, both user foreign keys, four allowed states, self-request and response-time constraints, partial unique pending-pair index, and sender/receiver query indexes; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed. Runtime constraint tests remain scheduled for Phase 12.  
Remaining limitations: Accepting/declining requests and atomic friendship creation are Version 2 transaction responsibilities.  
Corresponding commit message: `feat(db): add friend requests schema`

Rejected: Permanent uniqueness across all request history and extra request-message fields.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 05: Friendships and blocking schema

Tool: OpenAI Codex

Asked for: Added canonical unordered `friendships` and directed `user_blocks`, including explicit user/request references, self-relationship prevention, duplicate safeguards, and reverse-lookup indexes.

Kept: Required `user_low_id < user_high_id` for friendships; allowed at most one friendship per accepted request; kept blocks directional and independent from friendship/request cleanup policy. Accepted relational source facts and canonical pairs.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked five cumulative tables, all relationship foreign keys/delete actions, canonical/self checks, pair/request uniqueness, and four supporting indexes; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed. Runtime duplicate/self-reference tests remain scheduled for Phase 12.  
Remaining limitations: Version 2 must canonicalize IDs and atomically reconcile requests, friendships, and blocks.  
Corresponding commit message: `feat(db): add friendship and blocking schemas`

Rejected: Duplicated bidirectional friendship rows, stored friend counts, and automatic trigger-based block reconciliation.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 06: Conversations schema

Tool: OpenAI Codex

Asked for: Added shared `conversations` metadata for direct and group chats, including conditional name/ownership rules, encryption-status metadata, lifecycle timestamps, and filter indexes.

Kept: Required direct conversations to have no group name/owner and groups to have both; restricted deletion of a group owner until Version 2 transfers or removes ownership; treated encryption as a status flag only. Accepted one shared conversation supertype.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked six cumulative tables, type/shape/timestamp constraints, owner foreign-key restriction, encryption default, and three conversation indexes; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed. Runtime shape tests remain scheduled for Phase 12.  
Remaining limitations: Group ownership/admin membership synchronization and effective encryption are Version 2 responsibilities.  
Corresponding commit message: `feat(db): add conversations schema`

Rejected: Separate direct/group message tables, embedded member arrays, E2EE key columns, and automatic timestamp triggers.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 07: Conversation membership schema

Tool: OpenAI Codex

Asked for: Added `direct_conversations` for unique canonical one-to-one pairs and `conversation_members` for role, membership interval, and per-user history-clearing state.

Kept: Used the parent conversation ID as the direct specialization primary key; restricted deletion of users still named in a direct pair; allowed one membership row per conversation/user with explicit leave and clear-history timestamps. Accepted specialization plus shared membership.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked eight cumulative tables, canonical and unique direct pairs, all foreign-key delete actions, membership roles/timestamp bounds/uniqueness, and four supporting indexes; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed. Runtime pair/membership tests remain scheduled for Phase 12.  
Remaining limitations: Parent type, exactly-two direct members, group owner/admin guarantees, and rejoin policy require Version 2 transactions.  
Corresponding commit message: `feat(db): add conversation membership schemas`

Rejected: Participant arrays, duplicate direct pairs, a separate history-clear table, and silent orphaning on account deletion.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 08: Messages schema

Tool: OpenAI Codex

Asked for: Added `messages` with conversation/sender references, non-blank body, chronological timestamps, global unsend metadata, and history/search indexes.

Kept: Retained messages when sender accounts are removed by setting authorship null; required recall time before allowing an optional recall actor; added PostgreSQL simple-text GIN search for plaintext V1 content. Accepted explicit recall metadata and source body retention.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked nine cumulative tables, all message foreign-key delete actions, body/unsend constraints, chronological/sender indexes, and GIN search index; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed. Runtime send/unsend and search-index validation remain scheduled for Phase 12.  
Remaining limitations: Sender membership, unsend authorization/window, recalled-body access, and encrypted-message search policy require Version 2 decisions.  
Corresponding commit message: `feat(db): add messages schema`

Rejected: Per-user deletion flags on messages, physical deletion for unsend, message encryption keys, and speculative attachment fields.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 09: Message deletions schema

Tool: OpenAI Codex

Asked for: Added `message_deletions` for per-user delete-for-me state with explicit message/user ownership, deletion time, and unique deletion records.

Kept: Kept user-specific hiding separate from `messages.unsent_at` and from membership `history_cleared_at`; cascaded records when either their message or user ceases to exist. Accepted one junction record per user/message.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked ten cumulative tables, message/user cascading foreign keys, unique pair constraint, and user deletion-history index; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed. Runtime uniqueness/visibility tests remain scheduled for Phase 12.  
Remaining limitations: Participant authorization for delete-for-me is a Version 2 transaction rule.  
Corresponding commit message: `feat(db): add message deletion schema`

Rejected: A shared deleted flag, duplicate deletion events, and physical message removal.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 10: Spam reports schema

Tool: OpenAI Codex

Asked for: Added `spam_reports` with reporter/reported account references, optional message evidence, reason, independent review workflow, reviewer/note fields, resolution time, and moderation indexes.

Kept: Used `SET NULL` to retain moderation history after related account/message deletion; separated report statuses from account lock state; required resolution time only for terminal statuses. Accepted nullable retained-audit references and four-state workflow.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked eleven cumulative tables, four foreign keys/delete actions, self-report/reason/status/resolution constraints, and five moderation indexes; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed. Runtime workflow and retained-history tests remain scheduled for Phase 12.  
Remaining limitations: Prior-interaction eligibility, reviewer authorization, and sanction transactions are Version 2 responsibilities.  
Corresponding commit message: `feat(db): add spam reports schema`

Rejected: An `account_locked` report status, implicit sanctions, self-reporting, and mandatory message evidence.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 11: User activity schema

Tool: OpenAI Codex

Asked for: Added `user_activity_logs` for app-open, direct-chat, and group-chat source events with user/conversation context, timestamp, consistency checks, and analytics indexes.

Kept: Stored source events rather than calculated user counters; required no conversation for app opens and a conversation for chat activity; indexed user, event type, conversation, and time for range/distinct aggregation. Accepted event-level facts supporting `COUNT(DISTINCT ...)`.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Checked all twelve cumulative tables, activity foreign keys/delete actions, three event values, context consistency, and three analytics indexes; confirmed no thirteenth table; reviewed `git diff --check` and staged content before commit.  
Test results: Focused static schema assertions passed with exactly twelve table definitions. Integrated runtime and normalization validation remain scheduled for Phase 12.  
Remaining limitations: Event deduplication and conversation-type matching require Version 2 transaction logic; activity events do not constitute live presence.  
Corresponding commit message: `feat(db): add user activity schema`

Rejected: Monthly summary tables, counters on `users`, and a durable online/offline flag.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 12: Schema validation

Tool: OpenAI Codex

Asked for: Added portable static schema validation, a transactional PostgreSQL fixture with valid and invalid cases, and a validation report covering constraints, indexes, referential behavior, and normalization.

Kept: Required static validation everywhere and real SQL execution only in a newly created isolated database; tested cascade, set-null, and restrict semantics; rolled back all fixture rows. Accepted a disposable local PostgreSQL cluster after the normal endpoint was unavailable.

Changed: Files: `script/tests/validate-schema.ps1`, `script/tests/validate-schema.sql`, `docs/database/VALIDATION_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Ran static contract checks and loaded the full schema plus transactional fixture into PostgreSQL 18.4 on a trust-only loopback disposable cluster; dropped the database, stopped the server, and removed the verified cluster directory.  
Test results: PASS — 12 tables, 12 primary keys, 25 foreign keys, 34 checks, 33 explicit indexes; all valid integrated inserts succeeded; 14 representative invalid/delete-action cases behaved as specified; final transaction rolled back.  
Remaining limitations: No separate PostgreSQL 14–17 matrix or production-scale benchmark; documented cross-table authorization rules remain Version 2 work.  
Corresponding commit message: `test(db): add schema validation tests`

Rejected: Modifying/starting an unknown existing cluster, using production data, or claiming unavailable validation.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 13: Development seed data

Tool: OpenAI Codex

Asked for: Replaced the seed placeholder with deterministic fictional development data covering administrators, normal/locked users, every relationship state, direct/group conversations, membership roles, messages and lifecycle actions, reports, and activity events across all twelve tables.

Kept: Used reserved `example.test` emails, fictional profiles, BCrypt fixture hashes without published plaintext, explicit IDs for readable relationships, and sequence resets for safe subsequent inserts. Accepted representative development-only coverage.

Changed: Files: `script/seed.sql`, `docs/database/VALIDATION_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Loaded schema and seed with `ON_ERROR_STOP=1` into a fresh PostgreSQL 18.4 disposable database; queried every table count and verified identity sequences advance beyond fixture IDs; dropped the database, stopped the server, and removed the cluster.  
Test results: PASS — all twelve tables populated (56 total rows), all constraints/foreign keys accepted the data, and identity-sequence checks passed.  
Remaining limitations: Seed data is for local demonstration only; authentication workflows and credential provisioning remain Version 2 responsibilities.  
Corresponding commit message: `feat(db): add development seed data`

Rejected: Real personal data, production credentials, plaintext passwords, secrets, and partial/conflict-ignoring seed behavior.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 14: Database initialization

Tool: OpenAI Codex

Asked for: Added a configurable PowerShell initializer and setup guide for new or explicitly approved empty PostgreSQL databases, with optional fictional seed loading and clear failure handling.

Kept: Delegated authentication to PostgreSQL; accepted only validated database names/ports; refused existing databases by default; permitted `-UseExistingEmpty` only after counting zero non-system tables; never auto-dropped a database. Accepted explicit empty-database opt-in and preserved native tool output.

Changed: Files: `scripts/init-database.ps1`, `docs/database/SETUP_V1.md`, `docs/database/VALIDATION_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Parsed the script, scanned for embedded password assignment, and executed new+seed, existing refusal, approved-empty, and nonempty-refusal scenarios in a disposable PostgreSQL 18.4 cluster; cleaned both databases and the cluster.  
Test results: PASS — new database produced 12 tables/56 seed rows; default existing refusal left 12 tables unchanged; approved empty database produced 12 tables; nonempty opt-in was refused; cleanup passed.  
Remaining limitations: Backup/restore, migration, production deployment, and secure local credential provisioning are operational responsibilities outside V1.  
Corresponding commit message: `build(db): add PostgreSQL initialization script`

Rejected: Embedded/password parameters, automatic drop/recreate, silent schema merging, and production migration behavior.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 15: Final documentation and review

Tool: OpenAI Codex

Asked for: Completed cross-artifact, schema, seed, initialization, UI compatibility, build, formatting, scope, commit-history, and working-tree review; finalized Spec Kit status and produced the Database V1 completion report.

Kept: Declared Database V1 ready for technical review while preserving explicit Version 2 boundaries and reporting the unrelated baseline Spotless failure rather than modifying Java source. Accepted the evidence-based completion report and final status updates.

Changed: Files: `docs/database/COMPLETION_REPORT_V1.md`, `specs/003-database-schema-v1/spec.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`.  
Verification reported: Ran Spec Kit analysis, static schema validation, `mvn clean verify`, `mvn spotless:check`, module/file/table/task/log/commit-history checks, `git diff --check`, staged diff review, and final branch/status inspection.  
Test results: PASS — schema static checks (12 tables, 12 PKs, 25 FKs, 34 checks, 33 indexes), prior PostgreSQL runtime/seed/initializer suites, Maven reactor build, and 7 Java tests. FAIL — Spotless found 10 pre-existing formatting violations in `common`; no unrelated formatting was applied.  
Remaining limitations: Baseline Java formatting violations, no PostgreSQL 14–17 matrix, no production-scale benchmark, and all documented Version 2 transaction/runtime responsibilities remain outstanding outside this feature.  
Corresponding commit message: `docs(db): finalize database v1 documentation`

Rejected: Unrelated Java formatting, performance claims without benchmarks, Version 2 implementation, remote Git operations, and fabricated test/commit evidence.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

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

## 2026-10-31 — Balance User login layout
Tool: AI assistant (Copilot SDK in VS Code).
Asked for: Center and size the login card consistently, align the form into one 360 px column, and reduce uneven spacing without changing behavior or Vietnamese text.
Kept: Login submission and validation logic, handlers, test getters, displayed text, theme colors, and fonts.
Changed: Centered a fixed 480x560 card with GridBagLayout; rebuilt the form with fixed-size BorderLayout wrappers, left-aligned field labels, a stable feedback row, and vertical glue for balanced spacing. Added layout sizing assertions.
Rejected: Business-logic, text, dependency, and shared-theme changes.
By hand: Review the resized login UI visually after relaunching the User client.

## 2026-11-03 — Reorder AI log chronologically
Tool: OpenAI Codex (VS Code).
Asked for: Finalize the existing `AI-LOG.md` merge resolution by preserving all 46 historical entries and ordering them chronologically.
Changed: Reordered complete historical entry blocks and normalized excessive blank lines between entries; no historical entry content or date was changed.
By hand: The developer requested the reordering and remains responsible for final review.
