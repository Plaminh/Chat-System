# Implementation Plan: User Client UI Version 1

**Branch**: `feature/user-ui-v1` | **Date**: 2026-10-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification and the 12 approved User UI screen mockups under `docs/mockups/002-user-client-ui/`.

## Summary

Deliver the Version 1 User desktop interface in the existing `user-client` Maven module. Replace the starter demonstration window with a navigable sign-in-to-workspace sample flow, and implement the account, friends/search, direct chat/history, group, spam-report, and writing-assistance views represented in the approved mockups. Use in-memory sample data and local UI state only. Follow the established Admin Client visual language for shared application chrome while preserving the User-specific screen layouts and interactions. Do not connect screens to TCP, email, an LLM provider, PostgreSQL, or cryptography in this phase.

## Technical Context

**Language/Version**: Java 25 LTS.

**Primary Dependencies**: Existing Java Swing, FlatLaf, `common`, and JUnit 5 dependencies in `user-client`; no new dependency is planned.

**Storage**: In-memory sample data for the Version 1 UI. No client-side database or durable application persistence.

**Testing**: Existing Maven/JUnit 5 setup; focused UI/model tests that can run headlessly where possible, followed by `mvn -pl user-client -am test` and root `mvn clean verify`.

**Target Platform**: Windows desktop.

**Project Type**: Existing Maven multi-module desktop application; implementation is limited to the `user-client` module and this feature's documentation.

**Performance Goals**: No separate throughput target. Common screen navigation and local sample interactions should remain responsive during ordinary desktop use.

**Constraints**:
- Preserve the four-module architecture and all client/server dependency boundaries.
- Keep presentation and local sample interaction in the client. Do not implement server business logic in UI controllers.
- Do not add a direct PostgreSQL connection, live TCP chat, SMTP, Groq/LLM API call, or cryptographic implementation.
- Keep the existing `SocketClient` PING capability separate from this UI work; do not extend or change protocol contracts.
- Keep all controls and displayed data within the approved User requirements. Exclude unrelated sample content such as organization/department, phone number, password-expiry policy, device security telemetry, and other features not required by the project.
- Do not claim that mock sign-in, email recovery, delivery, persistence, reports, AI assistance, or group encryption are live.
- Preserve cancellation behavior and distinguish Delete for Me, Unsend, and Clear Conversation History without deciding unspecified server-side semantics.
- Use the approved PNGs and HTML exports as screen references; shared navigation/chrome should use the Admin-aligned dark navy and blue visual language. User-specific composition and screen content remain based on the User mockups.

**Scale/Scope**: 12 approved screen mockups, one shared desktop workspace, local sample data, and focused UI tests. Screen 2 is the account/profile workspace view; password change is exposed from account settings as a dialog/action rather than a separate one of the 12 exported screens.

## Constitution Check

*GATE: Must pass before implementation and remain valid after design.*

| Principle | Plan decision | Gate |
|---|---|---|
| Project scope | Implement only the User Client UI functions required by the project specification; exclude unrelated content visible in generated mockups. | PASS |
| Technology | Keep Java 25 LTS, Maven, Swing, FlatLaf and JUnit 5; add no framework or dependency. | PASS |
| Module boundaries | Modify User UI only in `user-client`; depend on `common` only where already appropriate; do not depend on `server`. | PASS |
| Architecture responsibilities | Keep UI/presentation in the client; leave authentication, authorization, networking, persistence, email, LLM and cryptography to later approved backend work. | PASS |
| Code quality | Follow existing `com.chat.user` packages and conventions; avoid speculative abstractions. | PASS |
| Testing | Add focused tests for local UI state/validation where practical; run targeted Maven tests and root verification, reporting actual results. | PASS |
| Workflow | Continue on the explicitly created `feature/user-ui-v1` branch; do not create/switch branches or commit/push without a separate explicit request. | PASS |
| AI transparency | Record meaningful implementation activity in `AI-LOG.md`; leave developer review responsibility explicit. | PASS |
| Delivery | Produce Version 1 desktop UI, sample interactions and design evidence; do not represent it as Version 2 backend functionality. | PASS |

No constitution violations are required by this plan.

## Project Structure

### Documentation (this feature)

```text
specs/002-user-client-ui/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ui-components.md
└── tasks.md
```

### Source Code (`user-client`)

```text
user-client/src/main/java/com/chat/user/
├── UserApplication.java              # Launches the User sign-in flow
├── view/
│   ├── UserMainFrame.java             # Shared workspace and navigation shell
│   ├── LoginPanel.java
│   ├── RegistrationDialog.java
│   ├── PasswordRecoveryDialog.java
│   ├── ProfileSettingsPanel.java
│   ├── ChangePasswordDialog.java
│   ├── FriendsPanel.java
│   ├── FriendRequestsSearchPanel.java
│   ├── ChatWorkspacePanel.java
│   ├── ChatHistorySearchPanel.java
│   ├── GroupsPanel.java
│   ├── GroupChatPanel.java
│   ├── SpamReportDialog.java
│   └── WritingAssistDialog.java
├── component/
│   ├── ConversationListItem.java
│   ├── MessageBubble.java
│   ├── UserListItem.java
│   └── GroupMemberItem.java
├── controller/                        # UI event coordination only
├── model/                             # Presentation models and sample state
└── network/
    └── SocketClient.java              # Existing PING helper; unchanged by this feature

user-client/src/test/java/com/chat/user/
├── model/                             # Sample-state and action tests
└── view/                              # Headless validation/navigation tests as feasible
```

Class names are planning-level targets, not a requirement to create every file verbatim. During implementation, prefer existing conventions and combine or split view classes only when needed to preserve clear responsibilities and the approved screen behavior.

**Structure Decision**: Extend the existing single `user-client` Swing module and its `view`, `component`, `controller`, `model`, and `network` packages. Do not create another Maven module, cross-client shared UI code, or server implementation. Keep mock data local to the client-side presentation layer.

## Screen-to-View Mapping

| Approved mockup | Planned view/surface | Required project scope |
|---|---|---|
| Screen 1.1 User Login | `LoginPanel` | Username/email and password entry; validation and sample route into workspace. |
| Screen 1.2 Registration | `RegistrationDialog` | Required account information and form validation. Exclude any unrelated organization fields. |
| Screen 1.3 Forgot Password | `PasswordRecoveryDialog` | Email entry and sample neutral confirmation; never show a generated password. |
| Screen 2 Main Workspace / Account | `UserMainFrame` + `ProfileSettingsPanel` | Shared navigation shell and editable project-required profile fields; provide password-change dialog/action. Exclude work account, company, phone, expiry, and 2FA/security claims. |
| Screen 3.1 Friends | `FriendsPanel` | Search/filter friends, online/offline state, open chat, unfriend and block. |
| Screen 3.2 Search Friends and Friend Request | `FriendRequestsSearchPanel` | Incoming request filter/accept/decline and user search by username/full name; omit unrelated network claims or unsupported search data. |
| Screen 4.1 Direct Chat | `ChatWorkspacePanel` | Sample personal conversation and message composer; no live delivery. |
| Screen 4.2 History Search and Remove Chats | `ChatHistorySearchPanel` + chat actions | Search current/all conversation history and navigate to result; distinguish delete-for-me, unsend and clear-history confirmations. |
| Screen 5.1 Groups | `GroupsPanel` | Create group with at least one other username, rename, members, and group-admin controls. |
| Screen 5.2 Group Chat | `GroupChatPanel` | Sample group conversation, membership/roles, and explicitly mock encryption indicator. |
| Screen 6.1 Spam Report | `SpamReportDialog` | Report a previously chatted-with account and enter a reason; sample-only confirmation. |
| Screen 6.2 Writing Agent | `WritingAssistDialog` | Preview a suggested/revised draft and accept or discard; no provider call. |

## Implementation Phases

1. Establish reusable User workspace shell, style tokens and sample state.
2. Deliver account entry/settings and friends/discovery journeys.
3. Deliver personal chat, history search and local destructive-action states.
4. Deliver groups, group conversation, spam-report and writing-assistance surfaces.
5. Integrate navigation, exercise the primary journeys, add focused tests, save final implementation captures under `docs/screenshots/user/implemented/` (preserving the existing Stitch reference images), and verify the Maven build.

Each phase will be decomposed into ordered, independently verifiable tasks in `tasks.md`. Complete, review and verify one task before creating its corresponding commit; do not create commits automatically unless explicitly requested.

## Complexity Tracking

No constitution violations or additional architectural complexity are introduced.

## Open Design Decisions

- Deleting all history has no confirmed shared/server retention semantics. The UI will use neutral, local-only mock behavior until the project owner decides otherwise.
- Delete for Me and Unsend are separate user-visible operations; authorization and cross-user visibility remain a Version 2 backend decision.
- LLM provider integration, credentials, data handling and limits are out of scope for this mock UI phase.
- The group encryption badge is illustrative only; actual E2EE design and key management remain outside this UI plan.
