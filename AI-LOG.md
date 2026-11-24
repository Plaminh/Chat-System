# AI-LOG.md — Java Chat System (CSC13008)

## 2026-11-23 — Implement Server Core Platform (Issue #3)

Tool: Antigravity (Gemini 3.8 Flash).

Asked for: Implement the Server Core Platform networking infrastructure (Issue #3), including `ProtocolConfig`, `SessionContext`, `ClientConnection`, `ActionHandler`, `ActionDispatcher`, `PingHandler`, `OnlineSessionRegistry`, and reusable `SocketClient` adapters for `admin-client` and `user-client`.

Kept: Four-module architecture, raw TCP socket communication, Spring Boot non-web mode, Jackson with `JavaTimeModule` for ISO-8601 formatting, and pre-existing repository/DAO structure.

Changed:
- Configured Jackson `ProtocolConfig` with `JavaTimeModule`, disabled `WRITE_DATES_AS_TIMESTAMPS`, disabled `ADJUST_DATES_TO_CONTEXT_TIME_ZONE`, and strict enum parsing.
- Created `SessionContext` and `ClientConnection` (with synchronized `writeLock`) to bind authentication identity to active sockets.
- Created `ActionHandler` interface and `ActionDispatcher` with role-based security gating (`UNAUTHENTICATED`, `UNAUTHORIZED`, `UNSUPPORTED_OPERATION`) and exception mapping.
- Implemented `PingHandler` (PING -> PONG) and refactored `RequestHandler` and `TcpSocketServer` to enforce 64 KB framing and connection lifecycle management.
- Implemented `OnlineSessionRegistry` for thread-safe user session tracking and push event delivery (`sendToUser`, `broadcast`).
- Implemented non-blocking `SocketClient` with asynchronous `CompletableFuture` request-response correlation and background event reader threads for both `admin-client` and `user-client`.
- Added unit and loopback integration tests (`ProtocolConfigTest`, `ActionDispatcherTest`, `OnlineSessionRegistryTest`, `TcpSocketServerIntegrationTest`, `SocketClientTest`).
- Marked all 20 tasks in `specs/005-server-core/tasks.md` as completed.

Verification: Maven 3.9.9 and Oracle JDK 25.0.4.1 were used. Root `mvn test` passed across all five reactor modules (Common 67, Admin Client 3, User Client 16, Server 22 passed; 31 DB tests cleanly skipped without live DB). `mvn spotless:check` was audited.

By hand: The developer requested implementation and instructed Git commits on `feature/server-core`.

## 2026-11-23 — Generate Tasks for Server Core Platform (Issue #3)

Tool: Antigravity (Gemini 3.8 Flash).

Asked for: Generate dependency-ordered tasks (`tasks.md`) for the Server Core Platform (Issue #3) following Spec Kit conventions.

Kept: Four-module architecture, raw TCP socket communication, phased execution ordering (Setup -> Foundational -> US1 -> US2/US3 -> Polish), and non-breaking incremental verification.

Changed: Created `specs/005-server-core/tasks.md` with 20 actionable tasks (T001-T020) spanning Jackson ISO-8601 config, session management, action dispatching, online presence registry, socket client adapter, and loopback integration testing.

Rejected: Combining multiple distinct concerns into ambiguous monolithic tasks or skipping test verification tasks.

By hand: The developer requested the task breakdown and instructed per-phase Git commits.

## 2026-11-23 — Plan Server Core Platform Architecture (Issue #3)

Tool: Antigravity (Gemini 3.8 Flash).

Asked for: Execute the Spec Kit implementation planning workflow for the Server Core Platform (Issue #3), generating design artifacts (`plan.md`, `research.md`, `data-model.md`, `contracts/core-contracts.md`, and `quickstart.md`).

Kept: Four-module architecture, raw TCP socket communication, Spring Boot non-web mode, Jackson with `JavaTimeModule` for ISO-8601 timestamps, thread-safe connection pooling, and in-memory session tracking.

Changed: Created `specs/005-server-core/plan.md`, `research.md`, `data-model.md`, `contracts/core-contracts.md`, and `quickstart.md`. Designed modular `ActionHandler` / `ActionDispatcher` pattern, `SessionContext` security gating, `OnlineSessionRegistry` event push router, and reusable `SocketClient` networking adapter.

Rejected: Introducing WebSocket, HTTP/REST endpoints, external message brokers (RabbitMQ/Kafka), or unnecessary ORM abstractions.

By hand: The developer approved the planning phase and instructed per-phase Git commits.

## 2026-11-23 — Specify Server Core Platform (Issue #3)

Tool: Antigravity (Gemini 3.8 Flash).

Asked for: Create the Spec Kit specification and quality checklist for the Server Core Platform (Issue #3) on branch `feature/server-core`.

Kept: Four-module architecture, TCP socket server infrastructure, Jackson ISO-8601 time handling, modular action dispatching, session management, and online event notification patterns aligned with `specs/004-common-v2`.

Changed: Created `specs/005-server-core/spec.md` with prioritized user stories (P1 Request Dispatching & Session Management, P2 Event Notification & Presence Tracking, P3 Client Socket Adapter) and `specs/005-server-core/checklists/requirements.md`. Updated `.specify/feature.json`.

Rejected: Merging directly into `feature/admin-client-v2` without a dedicated feature branch, introducing WebSocket or HTTP/REST APIs, changing database schemas, or modifying application code before planning.

By hand: The developer instructed the creation and checkout of `feature/server-core` from `dev` and requested a commit upon completing the specification.


## 2026-11-10 — Complete missing User Client V1 requirement states

Tool: OpenAI Codex.

Asked for: Complete the Version 1 UI representation of password recovery and encrypted group chat while preserving existing User behavior and deferring backend email, persistence, networking, and cryptography to Version 2.

Changed: Corrected password-recovery prototype wording; added Standard/Encrypted selection during group creation; stored the choice as immutable in-memory group state; displayed truthful per-group prototype status and guidance in group chat; constrained member-row height. Profile gender input was left unchanged because its current non-null model and Registration flow require a coordinated follow-up rather than a local dropdown replacement.

By hand: No build, automated test, or GUI verification was performed for this task. The developer must review and verify the source changes before any commit.

## 2026-11-10 — Correct confirmed Admin V1 layout defects

Tool: OpenAI Codex.

Asked for: Apply only the confirmed Admin V1 fixes for Friend Network column widths, selected-user action-bar responsiveness, and minimum-size User Activity table height, without verification or business-logic changes.

Changed: Added local Friend Network column widths; separated selected-user information from its wrapping action row; reduced accumulated vertical spacing around the User Activity table; clarified the activity-chart monthly sum label; renamed the visible soft-delete action to Deactivate User.

By hand: No build, automated test, or GUI verification was performed for this task. The developer must review and verify the changes before any commit.

## 2026-11-10 — Finalize Admin Client V1 UI wording and layout

Tool: OpenAI Codex.

Asked for: Review the completed Admin Client V1 and apply only minimal corrections for soft-delete wording, shared Sign Out styling, monthly active-user chart semantics, and minimum-size layout safety without changing mock behavior or Version 2 logic.

Kept: All nine Admin requirement groups, existing controllers and in-memory mock interactions, the four Maven modules, shared theme structure, database and User Client files, and Git history.

Changed: Reworded account-deletion confirmation as soft deactivation; reused existing shared theme colors for Sign Out; clarified that the activity chart represents monthly distinct active users; enabled horizontal scrolling while retaining designed table column widths; added tooltips for long table and selected-user text; made the User toolbar wrap at 980 px; kept chart month/metric labels readable at minimum size; corrected the user-dialog date-format hint to `YYYY-MM-DD`.

By hand: The developer must manually review the Admin UI before any commit. No commit was created.

## 2026-11-07 — Refine User Client sample interactions

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Apply the User portion of `Chinh-sua.md` only, leave Admin files untouched, and do not commit or push before the developer reviews the app.

Kept: The four Maven modules, the existing User Client sample-only behavior, Swing/FlatLaf, and all Admin Client files.

Changed: Added sample friend-request states and online-friend group creation; wired Sign Out back to login with a fresh workspace on the next sign-in; enforced group-admin-only member management; made Unsend available only for the current user's messages; separated both block directions in search/request filtering; made the group member area scrollable and adjusted its split sizing; clipped long sidebar profile names with a tooltip.

Rejected: Initially deferred: shared `ChatTheme` integration affected `common` and Admin, so it was held until the developer explicitly approved the expanded scope. It was implemented in the follow-up entry below.

By hand: Reviewed `Chinh-sua.md`, the project constitution, and the User Client source. `mvn -pl user-client -am test` passed (51 User Client tests and 2 Common tests). No Admin files were changed, and no commit or push was performed. The developer should visually review the packaged app before requesting a commit.

## 2026-11-07 — Consolidate Admin and User themes

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: After clarifying scope, create the shared theme in `common` and migrate both clients to it.

Kept: Exactly four Maven modules; `common` has no Admin/User business logic; Admin-only date validation, tables, badge renderers, and vector icons remain in `admin-client`.

Changed: Added `common/ui/ChatTheme` as the shared source for palette, typography, spacing, Swing defaults, card/button/text-field helpers, navigation icons, and date/time formats. Migrated User and Admin references to it, renamed the remaining Admin-specific helper class to `AdminUIComponents`, updated Admin date entry/display to `yyyy-MM-dd` and date-time display to `yyyy-MM-dd HH:mm`, and refreshed the affected Spec Kit path references.

By hand: Verified `mvn package` passed across the reactor (62 tests, 0 failures), including shared-theme and Admin date-format tests; both client JARs were packaged. No commit or push was performed; the developer should inspect both clients before requesting one.

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

Changed: Files: `specs/003-database-schema-v1/spec.md`, `specs/003-database-schema-v1/plan.md`, `specs/003-database-schema-v1/research.md`, `specs/003-database-schema-v1/tasks.md`, `specs/003-database-schema-v1/checklists/requirements.md`, `docs/database/IMPLEMENTATION_PLAN_V1.md`, `AI-LOG.md`. Codex reported verification: Confirmed branch `feature/database-schema-v1`, clean initial working tree, four Maven modules, placeholder SQL state, complete requirements checklist (16/16 checked), required planning artifacts, and no SQL implementation in this phase; reviewed `git diff --check` and staged content before commit. Reported test results: Planning-only phase; no runtime tests were applicable. Artifact and checklist validation passed. Remaining limitations: Physical data model, SQL schema, runtime database validation, seed data, initialization script, and final review remain for Phases 01–15. Recorded commit message: `docs(db): establish database v1 implementation plan`.

Rejected: Additional tables, triggers, stored procedures, Java changes, protocol changes, branch changes, and Version 2 behavior.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 01: Database design

Tool: OpenAI Codex

Asked for: Defined the conceptual, logical, and planned physical model for exactly twelve core tables; produced an ERD, DBML, detailed field/constraint model, schema contract, validation quickstart, and Admin/User UI mappings.

Kept: Adopted canonical low/high IDs for unordered relationships; specialized direct conversations without adding a table; separated unsending, delete-for-me, and history clearing; used source activity events and derived counts; documented restricted account-deletion cases and Version 2 transaction rules. Accepted the twelve-table normalized model and explicit delete behavior.

Changed: Files: `specs/003-database-schema-v1/data-model.md`, `specs/003-database-schema-v1/contracts/schema-contract.md`, `specs/003-database-schema-v1/quickstart.md`, `docs/database/ERD_V1.md`, `docs/database/schema-v1.dbml`, `docs/database/UI_DATA_MAPPING_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Cross-checked all twelve tables across ERD, DBML, data model, schema contract, plan, and UI mappings; confirmed all identifiers/states and lifecycle semantics are documented; reviewed `git diff --check` and staged content before commit. Reported test results: Documentation consistency checks passed; no SQL existed yet for runtime validation. Remaining limitations: Cross-table business rules require Version 2 backend transactions; the encryption flag is metadata only; PostgreSQL runtime validation remains pending until schema implementation. Recorded commit message: `docs(db): define database model and ERD`.

Rejected: Stored friend/activity counters, presence persistence, report/account-status conflation, triggers, procedures, extra key-management tables, and UI source changes.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 02: Users schema

Tool: OpenAI Codex

Asked for: Replaced the schema placeholder with PostgreSQL session settings and the `users` table, including authentication/profile fields, roles, account state, timestamps, constraints, and query indexes.

Kept: Used numeric identity keys; case-insensitive unique indexes for username/email; normalized nullable gender; explicit `active`/`locked` consistency with `locked_at`; retained only source attributes and no friend/activity counters. Accepted native checks and expression indexes.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked one `users` table definition, named constraints, five expected indexes, absence of other core tables, and PostgreSQL CLI availability; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed. PostgreSQL runtime execution was not performed in this phase and remains scheduled for Phase 12. Remaining limitations: `updated_at` maintenance and password hashing are Version 2 server responsibilities; later tables and integrated runtime validation remain pending. Recorded commit message: `feat(db): add users schema`.

Rejected: Plaintext passwords, UI display IDs as keys, client credentials, stored metrics, and database triggers for `updated_at`.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 03: Login history schema

Tool: OpenAI Codex

Asked for: Added `login_history` with numeric identity, user ownership, login/logout timestamps, chronological indexes, and explicit cascading cleanup.

Kept: Kept user name/full name normalized in `users` rather than duplicated; allowed open sessions with `logged_out_at = NULL`; rejected logout times before login. Accepted compact session-boundary history.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked two cumulative table definitions, the user foreign key with `ON DELETE CASCADE`, the timestamp constraint, and both login-history indexes; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed. Runtime execution remains deferred to the isolated Phase 12 fixture. Remaining limitations: Login/logout recording behavior and authentication are Version 2 server work. Recorded commit message: `feat(db): add login history schema`.

Rejected: Client/device metadata not required by Version 1 and denormalized display fields.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 04: Friend requests schema

Tool: OpenAI Codex

Asked for: Added directed `friend_requests` with sender/receiver references, normalized lifecycle states, response timestamps, self-request prevention, and pending-request duplicate protection.

Kept: Preserved historical final requests while allowing only one pending request for an unordered user pair, preventing simultaneous reverse requests through a partial expression index. Accepted the `LEAST`/`GREATEST` pending-pair index.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked three cumulative tables, both user foreign keys, four allowed states, self-request and response-time constraints, partial unique pending-pair index, and sender/receiver query indexes; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed. Runtime constraint tests remain scheduled for Phase 12. Remaining limitations: Accepting/declining requests and atomic friendship creation are Version 2 transaction responsibilities. Recorded commit message: `feat(db): add friend requests schema`.

Rejected: Permanent uniqueness across all request history and extra request-message fields.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 05: Friendships and blocking schema

Tool: OpenAI Codex

Asked for: Added canonical unordered `friendships` and directed `user_blocks`, including explicit user/request references, self-relationship prevention, duplicate safeguards, and reverse-lookup indexes.

Kept: Required `user_low_id < user_high_id` for friendships; allowed at most one friendship per accepted request; kept blocks directional and independent from friendship/request cleanup policy. Accepted relational source facts and canonical pairs.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked five cumulative tables, all relationship foreign keys/delete actions, canonical/self checks, pair/request uniqueness, and four supporting indexes; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed. Runtime duplicate/self-reference tests remain scheduled for Phase 12. Remaining limitations: Version 2 must canonicalize IDs and atomically reconcile requests, friendships, and blocks. Recorded commit message: `feat(db): add friendship and blocking schemas`.

Rejected: Duplicated bidirectional friendship rows, stored friend counts, and automatic trigger-based block reconciliation.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 06: Conversations schema

Tool: OpenAI Codex

Asked for: Added shared `conversations` metadata for direct and group chats, including conditional name/ownership rules, encryption-status metadata, lifecycle timestamps, and filter indexes.

Kept: Required direct conversations to have no group name/owner and groups to have both; restricted deletion of a group owner until Version 2 transfers or removes ownership; treated encryption as a status flag only. Accepted one shared conversation supertype.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked six cumulative tables, type/shape/timestamp constraints, owner foreign-key restriction, encryption default, and three conversation indexes; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed. Runtime shape tests remain scheduled for Phase 12. Remaining limitations: Group ownership/admin membership synchronization and effective encryption are Version 2 responsibilities. Recorded commit message: `feat(db): add conversations schema`.

Rejected: Separate direct/group message tables, embedded member arrays, E2EE key columns, and automatic timestamp triggers.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 07: Conversation membership schema

Tool: OpenAI Codex

Asked for: Added `direct_conversations` for unique canonical one-to-one pairs and `conversation_members` for role, membership interval, and per-user history-clearing state.

Kept: Used the parent conversation ID as the direct specialization primary key; restricted deletion of users still named in a direct pair; allowed one membership row per conversation/user with explicit leave and clear-history timestamps. Accepted specialization plus shared membership.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked eight cumulative tables, canonical and unique direct pairs, all foreign-key delete actions, membership roles/timestamp bounds/uniqueness, and four supporting indexes; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed. Runtime pair/membership tests remain scheduled for Phase 12. Remaining limitations: Parent type, exactly-two direct members, group owner/admin guarantees, and rejoin policy require Version 2 transactions. Recorded commit message: `feat(db): add conversation membership schemas`.

Rejected: Participant arrays, duplicate direct pairs, a separate history-clear table, and silent orphaning on account deletion.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 08: Messages schema

Tool: OpenAI Codex

Asked for: Added `messages` with conversation/sender references, non-blank body, chronological timestamps, global unsend metadata, and history/search indexes.

Kept: Retained messages when sender accounts are removed by setting authorship null; required recall time before allowing an optional recall actor; added PostgreSQL simple-text GIN search for plaintext V1 content. Accepted explicit recall metadata and source body retention.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked nine cumulative tables, all message foreign-key delete actions, body/unsend constraints, chronological/sender indexes, and GIN search index; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed. Runtime send/unsend and search-index validation remain scheduled for Phase 12. Remaining limitations: Sender membership, unsend authorization/window, recalled-body access, and encrypted-message search policy require Version 2 decisions. Recorded commit message: `feat(db): add messages schema`.

Rejected: Per-user deletion flags on messages, physical deletion for unsend, message encryption keys, and speculative attachment fields.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 09: Message deletions schema

Tool: OpenAI Codex

Asked for: Added `message_deletions` for per-user delete-for-me state with explicit message/user ownership, deletion time, and unique deletion records.

Kept: Kept user-specific hiding separate from `messages.unsent_at` and from membership `history_cleared_at`; cascaded records when either their message or user ceases to exist. Accepted one junction record per user/message.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked ten cumulative tables, message/user cascading foreign keys, unique pair constraint, and user deletion-history index; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed. Runtime uniqueness/visibility tests remain scheduled for Phase 12. Remaining limitations: Participant authorization for delete-for-me is a Version 2 transaction rule. Recorded commit message: `feat(db): add message deletion schema`.

Rejected: A shared deleted flag, duplicate deletion events, and physical message removal.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 10: Spam reports schema

Tool: OpenAI Codex

Asked for: Added `spam_reports` with reporter/reported account references, optional message evidence, reason, independent review workflow, reviewer/note fields, resolution time, and moderation indexes.

Kept: Used `SET NULL` to retain moderation history after related account/message deletion; separated report statuses from account lock state; required resolution time only for terminal statuses. Accepted nullable retained-audit references and four-state workflow.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked eleven cumulative tables, four foreign keys/delete actions, self-report/reason/status/resolution constraints, and five moderation indexes; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed. Runtime workflow and retained-history tests remain scheduled for Phase 12. Remaining limitations: Prior-interaction eligibility, reviewer authorization, and sanction transactions are Version 2 responsibilities. Recorded commit message: `feat(db): add spam reports schema`.

Rejected: An `account_locked` report status, implicit sanctions, self-reporting, and mandatory message evidence.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 11: User activity schema

Tool: OpenAI Codex

Asked for: Added `user_activity_logs` for app-open, direct-chat, and group-chat source events with user/conversation context, timestamp, consistency checks, and analytics indexes.

Kept: Stored source events rather than calculated user counters; required no conversation for app opens and a conversation for chat activity; indexed user, event type, conversation, and time for range/distinct aggregation. Accepted event-level facts supporting `COUNT(DISTINCT ...)`.

Changed: Files: `script/schema.sql`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Checked all twelve cumulative tables, activity foreign keys/delete actions, three event values, context consistency, and three analytics indexes; confirmed no thirteenth table; reviewed `git diff --check` and staged content before commit. Reported test results: Focused static schema assertions passed with exactly twelve table definitions. Integrated runtime and normalization validation remain scheduled for Phase 12. Remaining limitations: Event deduplication and conversation-type matching require Version 2 transaction logic; activity events do not constitute live presence. Recorded commit message: `feat(db): add user activity schema`.

Rejected: Monthly summary tables, counters on `users`, and a durable online/offline flag.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 12: Schema validation

Tool: OpenAI Codex

Asked for: Added portable static schema validation, a transactional PostgreSQL fixture with valid and invalid cases, and a validation report covering constraints, indexes, referential behavior, and normalization.

Kept: Required static validation everywhere and real SQL execution only in a newly created isolated database; tested cascade, set-null, and restrict semantics; rolled back all fixture rows. Accepted a disposable local PostgreSQL cluster after the normal endpoint was unavailable.

Changed: Files: `script/tests/validate-schema.ps1`, `script/tests/validate-schema.sql`, `docs/database/VALIDATION_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Ran static contract checks and loaded the full schema plus transactional fixture into PostgreSQL 18.4 on a trust-only loopback disposable cluster; dropped the database, stopped the server, and removed the verified cluster directory. Reported test results: PASS — 12 tables, 12 primary keys, 25 foreign keys, 34 checks, 33 explicit indexes; all valid integrated inserts succeeded; 14 representative invalid/delete-action cases behaved as specified; final transaction rolled back. Remaining limitations: No separate PostgreSQL 14–17 matrix or production-scale benchmark; documented cross-table authorization rules remain Version 2 work. Recorded commit message: `test(db): add schema validation tests`.

Rejected: Modifying/starting an unknown existing cluster, using production data, or claiming unavailable validation.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 13: Development seed data

Tool: OpenAI Codex

Asked for: Replaced the seed placeholder with deterministic fictional development data covering administrators, normal/locked users, every relationship state, direct/group conversations, membership roles, messages and lifecycle actions, reports, and activity events across all twelve tables.

Kept: Used reserved `example.test` emails, fictional profiles, BCrypt fixture hashes without published plaintext, explicit IDs for readable relationships, and sequence resets for safe subsequent inserts. Accepted representative development-only coverage.

Changed: Files: `script/seed.sql`, `docs/database/VALIDATION_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Loaded schema and seed with `ON_ERROR_STOP=1` into a fresh PostgreSQL 18.4 disposable database; queried every table count and verified identity sequences advance beyond fixture IDs; dropped the database, stopped the server, and removed the cluster. Reported test results: PASS — all twelve tables populated (56 total rows), all constraints/foreign keys accepted the data, and identity-sequence checks passed. Remaining limitations: Seed data is for local demonstration only; authentication workflows and credential provisioning remain Version 2 responsibilities. Recorded commit message: `feat(db): add development seed data`.

Rejected: Real personal data, production credentials, plaintext passwords, secrets, and partial/conflict-ignoring seed behavior.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 14: Database initialization

Tool: OpenAI Codex

Asked for: Added a configurable PowerShell initializer and setup guide for new or explicitly approved empty PostgreSQL databases, with optional fictional seed loading and clear failure handling.

Kept: Delegated authentication to PostgreSQL; accepted only validated database names/ports; refused existing databases by default; permitted `-UseExistingEmpty` only after counting zero non-system tables; never auto-dropped a database. Accepted explicit empty-database opt-in and preserved native tool output.

Changed: Files: `scripts/init-database.ps1`, `docs/database/SETUP_V1.md`, `docs/database/VALIDATION_V1.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Parsed the script, scanned for embedded password assignment, and executed new+seed, existing refusal, approved-empty, and nonempty-refusal scenarios in a disposable PostgreSQL 18.4 cluster; cleaned both databases and the cluster. Reported test results: PASS — new database produced 12 tables/56 seed rows; default existing refusal left 12 tables unchanged; approved empty database produced 12 tables; nonempty opt-in was refused; cleanup passed. Remaining limitations: Backup/restore, migration, production deployment, and secure local credential provisioning are operational responsibilities outside V1. Recorded commit message: `build(db): add PostgreSQL initialization script`.

Rejected: Embedded/password parameters, automatic drop/recreate, silent schema merging, and production migration behavior.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Phase 15: Final documentation and review

Tool: OpenAI Codex

Asked for: Completed cross-artifact, schema, seed, initialization, UI compatibility, build, formatting, scope, commit-history, and working-tree review; finalized Spec Kit status and produced the Database V1 completion report.

Kept: Declared Database V1 ready for technical review while preserving explicit Version 2 boundaries and reporting the unrelated baseline Spotless failure rather than modifying Java source. Accepted the evidence-based completion report and final status updates.

Changed: Files: `docs/database/COMPLETION_REPORT_V1.md`, `specs/003-database-schema-v1/spec.md`, `specs/003-database-schema-v1/tasks.md`, `AI-LOG.md`. Codex reported verification: Ran Spec Kit analysis, static schema validation, `mvn clean verify`, `mvn spotless:check`, module/file/table/task/log/commit-history checks, `git diff --check`, staged diff review, and final branch/status inspection. Reported test results: PASS — schema static checks (12 tables, 12 PKs, 25 FKs, 34 checks, 33 indexes), prior PostgreSQL runtime/seed/initializer suites, Maven reactor build, and 7 Java tests. FAIL — Spotless found 10 pre-existing formatting violations in `common`; no unrelated formatting was applied. Remaining limitations: Baseline Java formatting violations, no PostgreSQL 14–17 matrix, no production-scale benchmark, and all documented Version 2 transaction/runtime responsibilities remain outstanding outside this feature. Recorded commit message: `docs(db): finalize database v1 documentation`.

Rejected: Unrelated Java formatting, performance claims without benchmarks, Version 2 implementation, remote Git operations, and fabricated test/commit evidence.

By hand: No manual source edits are documented. Codex reported the validation; manual review and commit actions are not independently confirmed.

## 2026-10-30 — Database V1 — Post-Review Remediation

Tool: OpenAI Codex.

Asked for: Investigate and remediate the Database V1 review findings involving setup guidance, PostgreSQL restriction-test portability, recalled-message consistency, block-aware user search, atomic initialization, message-search behavior, and group rejoining; synchronize Spec Kit documentation and prepare one atomic commit.

Kept: Accepted canonical `[Message unsent]` redaction; specific `foreign_key_violation OR restrict_violation` handling; transactional schema/seed initialization; exclusion of users blocked in either direction; documented `LIKE` substring-search strategy; and reactivation of existing membership rows. Preserved message identity, conversation, sender, and timestamps upon global recall while replacing `body`; kept delete-for-me separate from history clearing. Seed execution relies on the caller's `--single-transaction`. Version 2 remains responsible for atomic recall, server-side block enforcement, escaping substring patterns, and group rejoin policy; Version 1 retains one membership interval per user/conversation.

Changed: Investigated contradictory initializer rerun guidance, overly narrow restriction-test exception handling, source-like content in recalled message fixtures, one-direction block filtering, separate schema/seed `psql` invocations, token-only full-text search, and missing membership reactivation semantics. Updated `script/schema.sql`, `script/seed.sql`, `script/tests/validate-schema.sql`, `script/tests/validate-schema.ps1`, `scripts/init-database.ps1`, `docs/database/SETUP_V1.md`, `docs/database/UI_DATA_MAPPING_V1.md`, `docs/database/VALIDATION_V1.md`, `docs/database/COMPLETION_REPORT_V1.md`, `docs/database/ERD_V1.md`, `docs/database/schema-v1.dbml`, the affected files under `specs/003-database-schema-v1/` (`spec.md`, `plan.md`, `tasks.md`, `data-model.md`, `contracts/schema-contract.md`, `quickstart.md`, `checklists/requirements.md`), and `AI-LOG.md`. Codex reported running the static schema validator, PowerShell parser, isolated PostgreSQL 18.4 schema/seed and rollback scenarios, runtime SQL fixtures, existing-database refusal, recalled-body query, cluster cleanup, repository-local Maven 3.9.9 `clean verify`/`verify`, `spotless:check`, and Git diff/status checks. Reported PASS: 12 tables, 12 PKs, 25 FKs, 34 checks, 33 indexes; 56 seed rows; `[Message unsent]` redaction; successful initialization/refusal/rollback cases (0 tables after failed schema and 0 tested rows after failed seed); cleanup; and 7 Java tests with no failures, errors, or skips. Reported FAIL: 10 pre-existing Spotless violations in `common`; no Java files were changed. An initial `mvn clean verify` invocation could not start because Maven was absent from `PATH`; the repository-local Maven executable subsequently succeeded. Limitations remain: no PostgreSQL 14–17 runtime matrix, potentially expensive substring `LIKE` search, token-based full-text search, one membership interval in V1, unimplemented V2 authorization/transactions, and existing formatting violations. Recorded commit message: `fix(db): address v1 review findings and validation portability`.

Rejected: Catch-all exception handling, redesigning the database/tables, adding `pg_trgm` or a thirteenth membership-history table, implementing Java services or UI changes in the database task, automatically dropping databases, and embedding credentials.

By hand: This entry does not document manual source edits or independently confirmed manual review/commit actions; the verification and test results above were reported by Codex.

## 2026-10-31 — Refine User login input sizing

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Make the username and password inputs less tall and improve text proportions.

Kept: The existing User login layout, field widths, and shared input styling for other screens.

Changed: Reverted an initial out-of-scope change to the Admin login dialog. Reduced both User login input heights from 36 px to 32 px and increased their font size from 13 pt to 14 pt; added a focused sizing test.

Rejected: Changes to Admin login or shared input styling across other screens.

By hand: Review the updated User login screen visually; no runtime GUI verification is claimed.

## 2026-10-31 — Upgrade project runtime to Java 25 LTS

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Upgrade the Java runtime to the latest LTS version in auto-execution mode.

Kept: The existing four Maven modules, application architecture, dependencies, and Java source behavior.

Changed: Set the root Maven compiler release to Java 25. The installed JDK is already 25.0.4.1 LTS.

Rejected: Installing another JDK or upgrading unrelated dependencies.

By hand: Review the Java upgrade and run the verified Maven build/tests. Admin-specific source code was not changed; this entry was narrowed to the actual compiler/runtime change.

## 2026-10-31 — Restore Admin documentation scope

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Keep changes limited to User-related work, explain the login field width setting, and undo Java-version edits to Admin-specific planning files.

Kept: Java 25 compiler target requested earlier; existing User login changes and unrelated pre-existing work.

Changed: Restored the Admin UI plan, quickstart, research, and Admin project-plan Java-version references. Confirmed no tracked files under `admin-client` were modified.

Rejected: Changes to Admin source code or unrelated User UI layout.

By hand: Review the User login field width before making a manual adjustment.

## 2026-10-31 — Reduce User login field width

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Make the username and password input boxes narrower in `LoginPanel.java`.

Kept: Existing User login layout, field height, and shared UI styling.

Changed: Increased each field to 32 text columns, capped its maximum width at 360 px, and centered both in the form; added focused width assertions.

Rejected: Changes to Admin UI or shared input styling.

By hand: Review the updated login screen visually after rebuilding and restarting the User client.

## 2026-10-31 — Balance User login layout

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Center and size the login card consistently, align the form into one 360 px column, and reduce uneven spacing without changing behavior or Vietnamese text.

Kept: Login submission and validation logic, handlers, test getters, displayed text, theme colors, and fonts.

Changed: Centered a fixed 480x560 card with GridBagLayout; rebuilt the form with fixed-size BorderLayout wrappers, left-aligned field labels, a stable feedback row, and vertical glue for balanced spacing. Added layout sizing assertions.

Rejected: Business-logic, text, dependency, and shared-theme changes.

By hand: Review the resized login UI visually after relaunching the User client.

## 2026-11-03 — Align User Client UI with Admin

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Update the User Client UI to use English and match the Admin Client theme, then replace the 12 User screenshots.

Kept: Swing/FlatLaf, the existing sample-only behavior, English proper nouns/user names in sample data, the existing 12 screenshot filenames, and all four Maven modules.

Changed: Matched the workspace shell to the Admin navy/blue layout with account/status chrome; translated User-facing controls, dialogs, feedback, and sample message text to English; updated related tests and the User UI plan.

Rejected: Adding new business behavior, changing protocol or persistence, formatting unrelated code, and deleting the existing screenshots before replacements were available.

By hand: Reviewed the Admin screenshot and current User UI source. The focused User tests passed (44 tests). The debugger first failed to resolve the sibling `common` artifact from the module-only build; after installing the reactor artifacts, the User window was still unavailable for capture. The developer subsequently captured and replaced all 12 User screenshots manually.

## 2026-11-03 — Refine User navigation sidebar

Tool: AI assistant (Copilot SDK in VS Code).

Asked for: Make the User sidebar visually match the Admin sidebar while keeping navigation and other functionality unchanged.

Kept: Existing User destinations, routes, sample session behavior, four Maven modules, and screenshot files until verified replacements are available.

Changed: Matched the sidebar width and navy styling; added the version badge, profile card and online indicator, aligned section labels, local line icons, active/hover states, and a visual-only Sign Out button. Renamed the visible Requests destination label without changing its enum or routing. Added sidebar-focused regression checks.

Rejected: Admin-module dependencies, shared-module changes, sign-out behavior, route changes, and deleting screenshots without replacements.

By hand: Reviewed the User/Admin sidebar patterns and tested the User reactor. `mvn -pl user-client -am test` passed (45 User tests, 2 Common tests). A GUI launch was attempted, but the debugger reported no active session afterward and the desktop showed no User Client window. The developer then captured and replaced the 12 User screenshots manually.

## 2026-11-05 — Finalize Database V1 design and validation

Tool: OpenAI Codex.

Asked for: Review and directly finalize the Version 1 PostgreSQL design against the assignment/UI requirements, add safe migrations and validation, complete encrypted-group storage, and document Version 2 responsibilities without implementing services.

Kept: The four Maven modules, existing Admin/User interfaces and Java source, original twelve database entities/names, account soft deletion, canonical relationships, one-row membership model, and separate unsend/delete-for-me/clear-history semantics.

Changed: Added one justified `conversation_key_versions` table and encrypted-message metadata for server-side AES-GCM envelope-encryption storage; retained activity logs when conversations are deleted; removed the misleading token-search index; added strict migration 002; updated seed, schema/DBML/ERD/setup/spec artifacts, Compose/initializer behavior, and focused static/runtime validation. Deleted accounts continue reserving username/email.

Rejected: E2EE claims, plaintext/committed secret keys, triggers, extensions, aggregate tables, DAO/service/socket/UI work, schema resets, destructive data operations, and automatic Git actions.

By hand: Codex used a disposable PostgreSQL 18.4 cluster on `127.0.0.1:55441`. Fresh schema, seed, runtime fixture, data-bearing predecessor migration, migration reruns, initializer, static validation, and Compose parsing passed. The server was stopped; environment policy blocked recursive removal of the ignored validation-cluster directory. `mvn clean verify` could not compile because only Java 21 was available for the Java 25 target. `mvn spotless:check` still reports the 10 pre-existing `common` violations. Developers must review the security/lifecycle policy and implement/verify cryptographic and authorization behavior in V2.

## 2026-11-05 — Correct recalled-body constraint and seed recovery

Tool: OpenAI Codex.

Asked for: Reproduce and correct the nullable recalled-message body defect, provide an additive upgrade for databases that already ran migration 002, make development-seed recovery explicit and safe, and validate the focused correction on PostgreSQL 16 without changing Java/UI/protocol code.

Kept: The thirteen-table design, encryption/activity model, separate schema and seed transactions, development-only fixture, four Maven modules, and all application source. Preserved populated databases and unrelated working-tree file `a.txt`.

Changed: Added explicit `body IS NOT NULL` enforcement to the fresh schema and migration 002 plus additive migration `003_fix_unsent_body_constraint.sql`; made validation scans null-safe; added INSERT/UPDATE regressions that identify `chk_messages_content_state`; added exact seed-signature state detection and explicit `-RetrySeed`/`RETRY_DEVELOPMENT_SEED=true` recovery with populated-table refusal; synchronized setup, contract, model, quickstart, validation, completion, ERD/DBML/UI mapping, plan/spec/tasks, Compose, and static validation artifacts.

Rejected: New tables, marker metadata, automatic seed merging/retry, destructive database/volume actions, Java/Maven/protocol changes, Git history operations, and treating PostgreSQL 18 as PostgreSQL 16 evidence.

By hand: Codex reproduced the pre-fix `CHECK` unknown-value acceptance, then used an isolated `postgres:16-alpine` PostgreSQL 16.15 container on `127.0.0.1:55443` with no project volume. Fresh schema/seed/runtime, constraint-specific INSERT/UPDATE cases, predecessor migrations 001/002/003, upgrade from the pre-fix migration-002 shape, migration reruns, invalid-row refusal/repair, shared-schema tolerance, unsafe-encryption rollback, PowerShell initializer recovery/refusal, Compose recovery/refusal/forced seed-failure rollback, PowerShell parsing, static validation, and Compose config passed. The container was stopped and removed. The older stopped `.tools/db-v1-final-validation` cluster was inspected and left for manual deletion because equivalent recursive cleanup was previously blocked. Maven and Spotless were not rerun; the carried-forward Java 21/Java 25 blocker and 10 baseline formatting violations remain unchanged.

## 2026-11-09 — Consolidate Database V1 as fresh-install-only

Tool: OpenAI Codex.

Asked for: Integrate every final database definition into the modular fresh schema, remove obsolete development migrations, replace their active dependencies with non-destructive validation, preserve safe seed recovery, and verify on isolated PostgreSQL 16 without modifying an existing database or application volume.

Kept: The thirteen-table structure, all table/column names and semantics, six schema modules, atomic schema/seed transactions, fictional fixtures, runtime constraint tests, ERD/DBML, four Maven modules, and V2 server responsibility boundaries.

Changed: Removed migrations 001/002/003 after confirming their final definitions already exist in the direct `CREATE TABLE` baseline; added read-only catalog validation for existing finalized schemas; routed Compose and PowerShell seed recovery through that validator; strengthened seed completeness to exact per-table fixture counts plus sentinels; removed migration-specific static assertions; and updated active setup, README, validation, completion, and Spec Kit documentation. Historical entries above were not rewritten.

Rejected: Automatic upgrades, schema resets, destructive repairs, seed merging, new tables/frameworks, Java/UI/protocol changes, application-volume access, and Git history operations.

By hand: Codex used disposable `postgres:16-alpine` 16.15 containers only. Fresh schema, 57-row seed, runtime integrity tests, PowerShell schema-only/retry/refusal, isolated Compose fresh/schema-only-retry/rerun, deliberate incompatible-schema refusal without repair, schema reapplication refusal, static validation, PowerShell parsing, Compose configuration, and active migration-path scans passed. The uniquely named verification containers/network/volumes were removed. No application database or application volume was attached or reset.

## 2026-11-09 — Simplify Database V1 fresh installation

Tool: OpenAI Codex.

Asked for: Reduce the uncommitted database work to a clear fresh-install schema, optional development seed, and focused tests without compatibility, repair, migration, or seed-state machinery.

Kept: The finalized thirteen-table design and names, six schema modules, atomic schema/seed transactions, soft deletion, encrypted-message storage, activity snapshots, corrected unsent constraints, fictional seed, runtime integrity fixture, and existing ERD/DBML.

Changed: Removed the existing-schema catalog validator and its active dependencies, seed signatures/count gates, automatic retry branches, and duplicated static assertions. Compose now initializes schema plus seed only for an empty `public` schema and otherwise skips without claiming compatibility. PowerShell keeps schema-only, `-LoadSeed`, existing-target refusal, and `-UseExistingEmpty`.

By hand: On disposable PostgreSQL 16.15 only, fresh schema, 57-row seed, runtime SQL tests, PowerShell schema-only initialization, Compose fresh/rerun behavior, and a late failing seed with zero partial rows passed. PowerShell parsing, focused static validation, Compose configuration, active-reference scans, and diff checks passed. All uniquely named verification resources were removed; Maven and Spotless were not run.

## 2026-11-09 — Correct encrypted-group storage toward client-side E2EE

Tool: OpenAI Codex.

Asked for: Replace the server-decryptable group-key model with database storage supporting client-side E2EE, define lifecycle/search/moderation boundaries, remove unnecessary fresh-install terminology, and keep implementation limited to V1 database artifacts.

Kept: The six schema modules, account/social/conversation/moderation/statistics model, soft deletion, activity snapshots, message ciphertext fields, explicit non-null unsent placeholder, atomic schema/seed workflow, and runtime integrity tests.

Changed: Added versioned per-user X25519 public keys and recipient-specific RFC 9180 HPKE envelopes; reduced conversation key versions to epoch metadata; removed server-controlled wrapped-key fields; updated seed/runtime tests, ERD/DBML, setup/mapping/spec artifacts, E2EE lifecycle/search/LLM/spam limitations, and active terminology. The supported recipient model is per-user, not per-device.

By hand: Disposable PostgreSQL 16.15 schema creation, 63-row seed, runtime integrity fixture, PowerShell schema-only initialization, isolated Compose initialization/rerun, static schema/DBML consistency, PowerShell parsing, Compose configuration, active-reference scans, and diff checks passed. All named verification resources were removed. SQL storage supports the intended E2EE boundary but does not implement or prove cryptography; Maven/Spotless were not run because no Java code changed.

## 2026-11-10 - Approve shared ChatTheme exception

Tool: OpenAI Codex.

Asked for: Record the approved decision to preserve the existing shared `common.ui.ChatTheme`, keep both clients and Server source untouched, keep V2 contracts Swing-independent, and propose any required constitution clarification without changing governance text.

Kept: The V1 `ChatTheme` source/test, Admin/User imports and initialization, FlatLaf and Look and Feel behavior, four-module structure, Server UI isolation, and all implementation source files.

Changed: Updated only Common V2 specification, plan, research, task, quickstart, and AI transparency documentation. Replaced the relocation strategy with a baseline-verification task and documented a proposed narrow amendment to constitution line 39 for human review.

Rejected: Changing the constitution governance text directly; commit and push.

By hand: Compared source against V1 baseline `67107ac`, checked theme blob identity and uniqueness, scanned Server and Common V2 packages for prohibited UI imports, and compiled both clients. The documentation diff remains for developer review.

## 2026-11-10 - Restrict Common V2 Phase 05 scope

Tool: OpenAI Codex.

Asked for: Remove Server implementation and test work from Common V2, preserve former T031/T032 traceability for `feature/server-v2`, and reconcile the Common V2 specification, plan, tasks, and quickstart without touching implementation files.

Kept: The approved shared `ChatTheme` decision and evidence, pending constitution amendment, Common V2 feature requirements, existing source, Maven configuration, UI, and database artifacts.

Changed: Phase 05 now contains only Common implementation/tests. Former T031 and T032 are recorded in a traceability-only Server V2 deferral table; Server response mapping and handler regressions are explicitly excluded from Common V2. Updated the plan, specification, and quickstart to require no Admin, User, or Server source changes.

Rejected: Server response mapping and handler regressions in Common V2 (deferred to `feature/server-v2`); commit and push.

By hand: Spec Kit prerequisites passed; cross-artifact scope scans found no remaining Common V2 task requiring consumer/server source changes; source and configuration files remain untouched; `git diff --check` passed.

## 2026-11-10 - Complete Common V2 protocol and handover

Tool: OpenAI Codex.

Asked for: Implement, test, document, commit, and push the remaining Common V2 validation, envelope, framing, coverage, and handover work on `feature/common-v2` without changing Admin, User, Server, database, script, or configuration source.

Kept: Exactly four Maven modules, Java 25, Jackson/NDJSON field names, the 65,536-character limit, PING compatibility, numeric IDs, offset-aware time, client-side encryption metadata only, and the shared ChatTheme/client behavior. DTO constructors remain the single source of context-free domain invariants. No credentials, persistence, sockets, Swing/AWT dependencies in V2 contracts, cryptographic operations, or business workflows were introduced.

Changed: Added typed `ProtocolValidationException` and `ProtocolValidator` request/event parsing plus safe DTO payload decoding; hardened request, response, and event envelope invariants; added complete NDJSON boundary tests and focused envelope/payload coverage; expanded pagination, null/empty collection, date/time, recall, group, Admin/User, enum, and encryption-copy regressions; reconciled the wire model and protocol contract; finalized the integration quickstart; and marked only evidence-backed Spec Kit tasks complete. Added a narrow Spotless exclusion for the immutable legacy `ChatTheme.java` and `ChatThemeTest.java`, which remain byte-identical to V1, and formatted only flagged Common V2/framing files.

Rejected: Deferred to later work: Server V2 former T031/T032 still own handler error mapping and regressions. Authentication, authorization, persistence, event delivery, email, external writing assistance, client routing/UI behavior, and actual key generation/encryption/decryption remain later Server/User/Admin work. Reformatting Admin source, which is forbidden scope.

By hand: Temurin JDK 25.0.5 and bundled Maven 3.9.9 were used. `mvn -pl common -am test` passed 48 tests; `mvn test` and `mvn clean verify` passed 107 reactor tests (Common 48, Admin 2, User 52, Server 5), with zero failures/errors/skips. Common-only `mvn -pl common spotless:check` passed 110 files, and `git diff --check` plus module/dependency/theme boundary scans passed. Root `mvn spotless:check` reached a clean Common module but failed on 29 pre-existing Admin Client formatting violations. Human requirements-checklist sign-off and the proposed constitution wording clarification remain pending.

## 2026-11-10 - Correct final Common V2 assignment contracts

Tool: OpenAI Codex.

Asked for: Perform a final assignment-to-contract audit, surgically correct confirmed Common V2 gaps, reconcile the existing Spec Kit artifacts, run the full verification/scope/security gates, commit, and push `feature/common-v2` without changing ChatTheme or Admin/User/Server source.

Kept: The correction changes only Common DTO/enum/validation/tests, `specs/004-common-v2/`, and this AI log. The complete feature branch has no Admin/User/Server source delta from merge-base `67107ac`. `ChatTheme.java` and `ChatThemeTest.java` match their approved V1 Git blobs (`2a500d02fcf294e34271786f37f37e4f87f6a78e` and `70380feee7a144276a31f021408a3f4dc31b5076`), exactly one production `ChatTheme.java` exists, clients retain their imports, Server/Common V2 packages have no UI imports, and Common contains no cryptographic implementation. Existing root-POM Java-time management and immutable-theme Spotless exclusions were inspected and not changed. Existing action names, PING, envelope field names, registration aggregates, and the deprecated `RESET_PASSWORD` identifier remain available. The 20-item reviewer protocol checklist remains intentionally unchecked.

Changed: Login history now supports global or per-user queries, explicit chronology, and the required username/full-name result identity. `ADMIN_LIST_USERS` now supports an optional creation-date range for the newly registered user list while range/monthly registration counts remain separate aggregates. Added reusable friend/online-friend name queries plus friend-request name/direction filtering. Added statistic name filters, account creation timestamps, non-negative numeric thresholds, and corrected activity terminology to distinct chatted users. User recovery is now documented as the email-only trigger for a Server-generated random password; the legacy reset-code action/type is deprecated but retained for compatibility, and the explicit Admin password update remains separate. Credential-bearing records and raw request envelopes now redact diagnostic text without changing legitimate JSON serialization. Compatibility: the corrected fields intentionally change the Java constructors and JSON shapes for affected login-history, Admin-user-filter, social-list, and statistic contracts; `chattedContacts`/`chattedContactCount` are corrected to `chattedUsers`/`chattedUserCount`. No current Admin/User/Server source referenced these constructors; consumer V2 branches must adopt the corrected shapes. Tests: added `FinalContractCorrectionTest` with 6 focused regressions covering global/per-user history, chronological sort metadata, identity fields, empty pages, registration ranges, friend/request filters, presence, statistics filters/results/wire names, invalid inputs, recovery non-disclosure, redacted diagnostics, and preserved password request serialization; updated enum and protocol validation coverage for friend-request direction and the nested friend-list page contract. Spec Kit: reconciled the existing six-phase feature without creating a new spec or phase.

Rejected: Deferred to later work: Server enforces sort allow-lists, block visibility/access, queries, statistics aggregation, random generation/hashing/persistence/email/rollback, and legacy reset compatibility. User/Admin adopt request/result shapes and UI behavior. Modifying the 29 Admin Client files that fail root Spotless.

By hand: Temurin JDK 25.0.5 and bundled Maven 3.9.9 were used. Final `mvn -pl common -am test` passed 54 tests. Final `mvn test` and `mvn clean verify` each passed 113 reactor tests (Common 54, Admin 2, User 52, Server 5), with zero failures/errors/skips. Common-only `mvn -pl common spotless:check` passed all 113 checked files. Root `mvn spotless:check` passed Common and failed on the same 29 Admin Client files; `admin-client/src` is byte-identical to `origin/dev`, so this remains a verified baseline-only external PR check. `git diff --check`, dependency/boundary/secret/crypto scans, and Spec Kit prerequisites passed. Spec Kit analysis found 33 functional requirements, 9 success criteria, 45 executable completed tasks, 100% semantic requirement coverage, no unmapped executable task, no unresolved ambiguity/duplication/constitution conflict, and T031/T032 still deferred to `feature/server-v2`. The unavailable named assignment PDF was searched for across the repository, Codex attachments, user folders, and all available drives; corrections therefore rely on the assignment requirements quoted in the request plus existing V1/spec artifacts, and that limitation is recorded in research.md. Human reviewers own the protocol checklist and pending constitution wording clarification.

## 2026-11-18 - Stabilize final Common V2 action and event contracts

Tool: OpenAI Codex.

Asked for: Resolve remaining ambiguous Common V2 action payloads, define the account-status event contract, verify generic page deserialization, audit the reviewer-owned protocol checklist, synchronize only necessary documentation, and stop after implementation/verification/diff review without Git integration actions.

Kept: Existing action/event identifiers, envelopes, `PageResult<T>`, PING/PONG, dependencies, ChatTheme, consumer source, Server source, and database files. Reviewer checklist markers remain unchecked for human approval. The pre-existing untracked `common-v2-final-diff.txt` was not modified.

Changed: Added `FriendRequestIdTarget`, reusable `GroupTarget`, and safe `AccountStatusEvent` records; added focused target, event, and typed `PageResult<T>` tests; mapped all 60 `ActionType` values to concrete request/success shapes in the action matrix; documented account lifecycle boundaries and `TypeReference` decoding; updated Common V2 traceability tasks.

Rejected: Git integration actions; unrelated line-ending rewrites and `spotless:apply`.

By hand: Temurin JDK 25.0.5 and bundled Maven 3.9.9 were used. `mvn -pl common -am test` passed 66 Common tests. Root `mvn test` and `mvn clean verify` each passed 125 reactor tests (Common 66, Admin 2, User 52, Server 5), with zero failures/errors/skips. The action/matrix audit found 60 exact one-to-one action mappings and no vague request/result cells. Dependency/scope/theme/crypto scans found no new dependency, prohibited Common implementation import, Server UI import, crypto implementation, consumer/server/database source change, enum/envelope change, or ChatTheme baseline change. `mvn -pl common spotless:check` remains failed only because 113 pre-existing tracked Common Java files are checked out with CRLF while Spotless requires LF; all six new Java files pass Google Java Format 1.30.0.

## 2026-11-18 - Final-audit Common V2 readiness

Tool: OpenAI Codex.

Asked for: Independently audit the actual Common V2 repository state, apply only confirmed minimal fixes, diagnose Spotless/CRLF, review all 20 protocol checklist criteria without changing reviewer markers, run the full Java 25 verification matrix, and stop before Git integration.

Kept: Existing DTO design, dependencies, checklist markers, and consumer/server/database implementation.

Changed: Corrected the documented `friendRequestId` fields, made the typed-page quickstart safe for empty results, enforced the documented 1–100 `PageResult.size` bound with regression coverage, added an explicit test separating terminal account-status events from Admin lock/unlock updates, and added a repository Java LF rule in `.gitattributes`.

Rejected: DTO redesign, mass line-ending rewrite, bypassing the root Spotless gate, and Git integration.

By hand: Temurin JDK 25.0.5 and bundled Maven 3.9.9 were used. `mvn -pl common -am test` passed 67 Common tests; root `mvn test` and `mvn clean verify` each passed 126 reactor tests (Common 67, Admin 2, User 52, Server 5). The final action audit found 60 enum values and 60 unique exact matrix rows with no missing, extra, duplicate, vague, malformed, or unresolved type entries. `git diff --check` passed. Common and root Spotless still exit 1 on this checkout because 113 Common Java files have CRLF; raw-LF HEAD passes Common Spotless, proving that part is checkout/configuration rather than Java formatting. Raw-LF HEAD separately exposes pre-existing out-of-scope Google Java Format failures in 24 Admin, 29 User, and 7 Server files, so the root Spotless gate is not green. All seven Common Java files added or touched by this work pass Google Java Format 1.30.0 dry-run.

## 2026-11-22 - Plan V2 work and align example database config

Tool: Claude Code (Claude Opus 5.5).

Asked for: Check V2 readiness against the assignment, split the remaining V2 work between the Admin (A) and User (U) owners with deadlines, publish the issues on GitHub, and review the `.env`/Docker setup.

Kept: All source code, the database schema, and the `common` contracts; the password placeholder in `config/server.properties.example`.

Changed: Added `docs/v2-issues.md` (25 work items: CORE, A0–A9, U1–U12, REL, QA-U, with actions/DTOs from the Common V2 contract and acceptance criteria). Created the matching GitHub issues #3–#27 with assignees, `admin`/`user`/`core`/`v2` labels, and five deadline milestones (25/11, 27/11, 29/11, 1/12, 3/12). Updated `config/server.properties.example` so its URL and username match the Docker Compose database defined by `.env.example` (`localhost:55432/chat_system_v1`, `chat_v1`).

Rejected: Updating `README.md` and `scripts/init-database.ps1`, which still describe the older non-Docker defaults (`5432`, `chat_system` / `chat_system_dev`); commit and push.

By hand: `mvn -pl common -am test` passed 67 Common tests. `docker compose config -q` passed; Docker Desktop was not running, so the containers were not started. Developers remain responsible for reviewing the plan and configuration.

## 2026-11-22 - Make .env the single local configuration file

Tool: Claude Code (Claude Opus 5.5).

Asked for: Let teammates edit only `.env` instead of entering the database password again in `server.properties`.

Kept: Client configuration, so clients never read database credentials.

Changed: `server/src/main/resources/application.properties` now imports `../.env` and then `./.env` (or `-Dchat.server.config`) with the `[.properties]` hint and resolves the port and datasource from `CHAT_SERVER_PORT`, `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD`, defaulting to the Docker Compose database. Added `POSTGRES_HOST` and `CHAT_SERVER_PORT` to `.env.example`. Removed `config/server.properties.example` and its copy step from `scripts/package.ps1`; updated the README configuration section.

Rejected: Deleting the local untracked `release/server.properties`, which is no longer read and can be deleted by the developer; commit and push.

By hand: `mvn -pl server -am verify` passed 72 tests (Common 67, Server 5). The packaged `server.jar` was run from temporary directories: with only a parent `.env` it listened on that file's port (5991), with a nearer `./.env` that file won (5992), and with no `.env` it used the default 5050. A live PostgreSQL connection was not tested because Docker Desktop was not running and the server does not query the database yet.

## 2026-11-22 - Separate server and client configuration files for release

Tool: Claude Code (Claude Opus 5.5).

Asked for: Keep separate `server.properties` and `client.properties` in `release` to match the assignment's "one configuration file with the database connection" requirement, while developers still edit only the repository `.env`.

Kept: The repository `.env` as the only file developers edit; `release` contains no `.env`.

Changed: Restored `config/server.properties.example`, now using the same keys as `.env` (`POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `CHAT_SERVER_PORT`). `application.properties` imports `./server.properties` (or `-Dchat.server.config`) first and then `../.env` and `./.env`, so a repository `.env` overrides the release file. `scripts/package.ps1` again creates `release/server.properties` alongside `client.properties`. Updated `.env.example` comments and the README configuration section. This supersedes the earlier same-day `.env`-only layout. The unmodified old-format local `release/server.properties` was replaced with the new example.

Rejected: Commit and push.

By hand: `mvn -pl server -am verify` passed 72 tests (Common 67, Server 5). The packaged `server.jar` was run from temporary directories: from `release/` with a parent `.env` and a placeholder `server.properties` it used the `.env` port (5991); from the repository root it used `.env` (5991); with only `release/server.properties` (assignment grading layout) it used that file (5995); with `-Dchat.server.config` and no `.env` it used the given file (5996). A live PostgreSQL connection was not tested because Docker Desktop was not running and the server does not query the database yet.

## 2026-11-22 - Drop V1 naming from the Docker database

Tool: Claude Code (Claude Opus 5.5).

Asked for: Create a new Docker database container that no longer uses V1 names.

Kept: Historical V1 documents under `docs/database/`; the local passwords.

Changed: `compose.yaml` container `chat-system-v1-db` -> `chat-system-db`; volume `chat-system-v1-data` -> `chat-system-data`, no longer `external`, so Docker Compose creates it. Database `chat_system_v1` -> `chat_system` and role `chat_v1` -> `chat_app` in `.env.example`, `config/server.properties.example`, and the defaults in `server/src/main/resources/application.properties`; the local untracked `.env` and `release/server.properties` were updated the same way. Removed "V1" from the completion message in `scripts/init-database.ps1`.

Rejected: Removing the old `chat-system-v1-db` container and `chat-system-v1-data` volume, and migrating their data; the new volume starts empty and `db-init` loads `script/schema.sql` and `script/seed.sql`. Commit and push.

By hand: `docker compose config --quiet` succeeded. The container was not started because Docker Desktop was not running.

## 2026-11-22 - Run the Docker database and remove V1 leftovers

Tool: Claude Code (Claude Opus 5.5).

Asked for: Run the Docker Compose database, then remove the old V1 container and volume and confirm the environment is clean.

Kept: Historical V1 documents under `docs/database/`, so `docs/database/SETUP_V1.md` still describes the removed external `chat-system-v1-data` volume; containers, volumes, and networks of other Docker projects.

Changed: No repository files besides this log. Docker only: `docker compose up -d` replaced the running `chat-system-v1-db` container with `chat-system-db` and created the `chat-system-data` volume; the stopped `chat-system-v1-db-legacy` container and the `chat-system-v1-data` volume were then removed. The V1 data was discarded, not migrated. This completes what the earlier same-day "Drop V1 naming from the Docker database" entry left undone.

Rejected: Migrating the V1 data; starting the Java server against the new database; commit and push.

By hand: `chat-system-db` is healthy on `127.0.0.1:55432`; `db-init` exited 0 after loading `script/schema.sql` and `script/seed.sql` (15 tables, e.g. 6 users, 7 messages). A second `docker compose up -d` skipped initialization as expected. Only `chat-system-db`, `chat-system-db-init-1`, `chat-system-data`, and `chat-system_default` remain for this project. The developer requested the cleanup and remains responsible for reviewing the result.

## 2026-11-22 - Add Spring JDBC repositories for the server

Tool: Claude Code (Claude Opus 5.5).

Asked for: Build the server DAO layer for all 15 database tables on a new `feature/server-dao` branch, as `*Repository` classes in `com.chat.server.repository` with models in `com.chat.server.model`.

Kept: The database schema, `common` DTOs/enums, existing server classes (`DatabaseConfig`, `RequestHandler`, `TcpSocketServer`), Maven dependencies, and the four-module structure. Repositories use only Spring JDBC (`NamedParameterJdbcTemplate`) and contain no authorization or business workflow.

Changed: Added 21 server-only records in `server/.../model` and 12 repositories plus `SqlSupport`/`Rows` helpers in `server/.../repository`: `UserRepository`, `UserPublicKeyRepository`, `LoginHistoryRepository`, `FriendRequestRepository`, `FriendshipRepository`, `UserBlockRepository`, `ConversationRepository` (also `direct_conversations`), `ConversationMemberRepository`, `ConversationKeyRepository` (key versions and envelopes), `MessageRepository` (also `message_deletions`), `SpamReportRepository`, `UserActivityLogRepository`. Queries follow `docs/database/UI_DATA_MAPPING_V1.md`: message visibility (membership interval, cleared history, per-user deletion), bidirectional block exclusion in user search, canonical friendship pairs, distinct friends-of-friends, membership reactivation, escaped literal `LIKE` search, and half-open date ranges and monthly charts always in Vietnam time (`Asia/Ho_Chi_Minh`). Client sort fields go through per-query allow-lists limited to the assignment (users and groups: `name`, `createdAt`; spam reports: `createdAt`, `username`; login history: `loggedInAt`; statistics: `name`, `createdAt`); any other field throws the new `com.chat.server.exception.InvalidSortFieldException`, which carries `ErrorCode.VALIDATION_FAILED` per `protocol-contract.md` and lists the allowed fields. `User`/`NewUser.toString()` redact the password hash. Friend and activity statistics include locked and soft-deleted accounts. Added 31 integration tests in `server/src/test/.../repository` that run against the configured PostgreSQL inside rolled-back transactions and are skipped when the database is unreachable, plus 3 `SqlSupportTest` unit tests (Vietnam-time ranges, sort allow-list, `LIKE` escaping).

Rejected: Services, request handlers, BCrypt hashing, session/presence handling, and error-code mapping (later CORE/feature work); schema changes; Testcontainers or other new dependencies; push (the developer asked for commits only).

By hand: `mvn clean verify` with JDK 25.0.5 passed 160 tests (Common 67, Admin 2, User 52, Server 39), including the 31 repository tests against the Docker database with none skipped. With an unreachable database port the 31 repository tests were skipped, not failed. Seed row counts were unchanged after the test runs. All new Java files pass Google Java Format 1.30.0 dry-run. The developer chose Vietnam time for date filters, keeping soft-deleted accounts in statistics, and the assignment-only sort fields, and must review the queries before committing.

## 2026-11-24 - Fix server CORE defects after merging issue #3

Tool: Claude Code (Claude Opus 5.5).

Asked for: Review the merged `feature/server-core` work against issue #3 and fix the defects found.

Changed: `RequestHandler` now marks its two-argument constructor `@Autowired`, because Spring could not choose between the two constructors and `server.jar` stopped at startup with "No default constructor found". `ActionDispatcher` maps `DuplicateKeyException` to `ALREADY_EXISTS` for REGISTER, UPDATE_PROFILE, SEND_FRIEND_REQUEST, BLOCK_USER, ADMIN_CREATE_USER and ADMIN_UPDATE_USER, and to `CONFLICT` otherwise. It also maps `IllegalArgumentException` to `VALIDATION_FAILED`. `SessionContext` and `OnlineSessionRegistry` use the database `long` user id instead of `UUID`. The registry gains `bindSession`/`unbindSession`, so a handler that only receives a `SessionContext` can mark its socket online. `TcpSocketServer` gives each connection its own worker thread (up to 256), because the previous pool served only 4 connections at once. Tests: updated user ids, added dispatcher tests for both duplicate-key mappings and for the illegal-argument mapping, plus socket integration tests for a targeted event with two clients and for six simultaneous clients.

Kept: `common`, repositories, schema, client `SocketClient` classes, and the existing 4-space style of the touched files (no Spotless apply).

By hand: `mvn clean verify` passed with the Docker database running: 182 tests, none skipped, including all 31 repository tests. The six-client test was confirmed to time out with the old pool. `server.jar` started and answered PING with PONG, malformed JSON with INVALID_JSON, an unknown action with UNKNOWN_ACTION, ADMIN_LIST_USERS before login with UNAUTHENTICATED, and REGISTER with UNSUPPORTED_OPERATION. Not committed; the issue owner must review and commit with their own Git account.
