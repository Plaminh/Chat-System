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
