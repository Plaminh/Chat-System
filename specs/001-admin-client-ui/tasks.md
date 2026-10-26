---

description: "Task list for Admin Client User Interface (Version 1)"
---

# Tasks: Admin Client User Interface (Version 1)

**Input**: Design documents from `/specs/001-admin-client-ui/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Verification is performed via `mvn clean test-compile`, `mvn spotless:check`, and desktop UI walkthrough in `quickstart.md`.

**Organization**: Tasks are grouped by user story (`US1`–`US9`) to enable independent implementation and testing of each story, while also providing the exact 10-screen commit mapping defined in `docs/Plan_Admin_UI_Version_1.md`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Multi-module Maven Desktop App**: `admin-client/src/main/java/com/chat/admin/`
- **Resources**: `admin-client/src/main/resources/`
- **Screenshots**: `docs/screenshots/admin/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Ensure package structure and shared UI theme/icon utilities exist inside `admin-client`.

- [x] T001 Verify `admin-client` Maven dependencies (`flatlaf`, `jfreechart`) and package directories (`model`, `view`, `chart`) in `admin-client/pom.xml`
- [x] T002 [P] Implement Desktop Admin Studio color palette (`#0F172A` sidebar, `#F8FAFC` canvas, `#FFFFFF` surface, `#3B82F6` primary, `#10B981` active emerald, `#EF4444` locked rose), typography tokens (`Inter`/`SansSerif`), programmatic Java2D vector icons, and status pill `TableCellRenderer` in `admin-client/src/main/java/com/chat/admin/view/AdminUIStyle.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core mock data models and in-memory dataset store (`com.chat.admin.model`) that MUST be complete before any User Story screen can render or filter data.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T003 [P] Create `AdminUser` record (constraints: non-null unique `id`, required non-blank `username` 3–30 chars, required `fullName` 2–100 chars, required `email` containing `@`, required `address`, past `dateOfBirth`, `gender` in `["Male", "Female", "Other"]`, `createdAt` timestamp, `locked` boolean, non-negative `directFriendCount`, `friendsOfFriendsCount`, `appOpenCount`, `chattedUsersCount`, `chattedGroupsCount`) in `admin-client/src/main/java/com/chat/admin/model/AdminUser.java`
- [x] T004 [P] Create `LoginRecord` (constraints: required `timestamp`, `username`, `fullName`) and `FriendConnection` (constraints: required `friendUsername`, `friendFullName`, non-negative `mutualFriendsCount`, `online` boolean) in `admin-client/src/main/java/com/chat/admin/model/LoginRecord.java` and `admin-client/src/main/java/com/chat/admin/model/FriendConnection.java`
- [x] T005 [P] Create `GroupChatInfo` (constraints: required unique `groupId`, non-blank `groupName`, `createdAt`, non-null `members` list with `groupAdmin` role flag) and `SpamReportRecord` (constraints: unique `reportId`, `reporterUsername`, `reportedUsername`, `reason`, `reportedAt`, `status`) in `admin-client/src/main/java/com/chat/admin/model/GroupChatInfo.java` and `admin-client/src/main/java/com/chat/admin/model/SpamReportRecord.java`
- [x] T006 Implement `AdminMockDataStore` (preloaded realistic dataset for users, login history, friends, groups, spam reports, and 2024–2026 monthly chart metrics, plus helper methods for Add/Edit/Delete/Lock/ResetPassword) in `admin-client/src/main/java/com/chat/admin/model/AdminMockDataStore.java`

**Checkpoint**: Foundation ready — user story implementation and the 10 screen commits can now begin.

---

## Phase 3: User Story 1 - Admin Authentication & Navigation Shell (Priority: P1) 🎯 MVP

**Goal**: Provide a secure authentication dialog (`AdminLoginDialog`) and the central navigation workspace (`AdminMainFrame`) with a dark slate sidebar and `CardLayout` module switching.

**Independent Test**: Launch `AdminApplication`, verify empty/invalid login credentials show an inline error, sign in with `admin` / `admin123`, and click each sidebar menu item in `AdminMainFrame` to switch views.

### Implementation for User Story 1

- [x] T007 [US1] Implement `AdminMainFrame` (1280x800 frame, `#0F172A` dark sidebar with Oversight & Analytics navigation items, top header bar, clean status footer without DevOps noise, and central `CardLayout` container) in `admin-client/src/main/java/com/chat/admin/view/AdminMainFrame.java` (Commit #1: `feat(admin): create admin main frame with modern sidebar navigation`)
- [x] T008 [US1] Implement `AdminLoginDialog` (centered modal dialog with username input, password input with show/hide toggle, required-field validation, mock admin check, and sign-in button) in `admin-client/src/main/java/com/chat/admin/view/AdminLoginDialog.java` and wire startup in `admin-client/src/main/java/com/chat/admin/AdminApplication.java` (Commit #2: `feat(admin): implement admin login dialog with validation`)

**Checkpoint**: At this point, User Story 1 (Commits #1 and #2) is fully functional and testable independently.

---

## Phase 4: User Story 2 - User Account Directory & Management (Priority: P1)

**Goal**: View, search, filter, sort, add, edit, delete, lock/unlock, and reset passwords for user accounts.

**Independent Test**: Open User Directory, filter by username/full name and status (`All`, `Active`, `Locked`), sort by Name or Created Date, and execute Add User, Edit User, Delete User, Lock/Unlock, and Reset Password actions.

### Implementation for User Story 2

- [x] T009 [US2] Implement `UserManagementPanel` (search box, status filter dropdown, sort selector by Full Name or Created Date, 8-column `JTable` with `Active`/`Locked` pill badges, bottom action bar with Add, Edit, Delete, Lock/Unlock, Reset Password, and View History/Friends buttons + validated form dialogs) in `admin-client/src/main/java/com/chat/admin/view/UserManagementPanel.java` and register in `admin-client/src/main/java/com/chat/admin/view/AdminMainFrame.java` (Commit #3: `feat(admin): implement user management panel with search and action triggers`)

**Checkpoint**: At this point, User Stories 1 AND 2 (Commit #3) work independently.

---

## Phase 5: User Story 3 - User Login Audit Trail & Friend Network Inspection (Priority: P2)

**Goal**: Inspect chronological login history (`timestamp`, `username`, `fullName`) and friend connections (direct friends list and friends-of-friends count).

**Independent Test**: Select a user in `UserManagementPanel` and click "View History & Friends" to inspect the chronological login records (with toggle between Selected User and All Users) and the Friend Network tab.

### Implementation for User Story 3

- [x] T010 [US3] Implement `UserDetailHistoryDialog` (modal dialog with user profile header and 2 tabs: Tab 1 chronological Login History table showing `Timestamp`, `Username`, `Full Name` with Selected User / All Users filter; Tab 2 Friend Network with `Direct Friends` and `Friends-of-Friends` KPI cards and direct friends table) in `admin-client/src/main/java/com/chat/admin/view/UserDetailHistoryDialog.java` and wire action in `admin-client/src/main/java/com/chat/admin/view/UserManagementPanel.java` (Commit #4: `feat(admin): implement user login history and friend network detail dialog`)

**Checkpoint**: User Story 3 (Commit #4) is independently functional.

---

## Phase 6: User Story 4 - Group Chat Oversight & Member Roles (Priority: P2)

**Goal**: Oversee all group chats, filter by group name, sort by name or creation date, and inspect group members and group administrators.

**Independent Test**: Open Group Chats panel, filter by name, sort by Name or Created Date, and click any group row to view its member list and highlighted `Group Admin` badges on the right inspector panel.

### Implementation for User Story 4

- [ ] T011 [US4] Implement `GroupManagementPanel` (search by group name, sort by Name or Creation Date, left `JTable` of groups, and right Master-Detail roster inspector displaying all members and `Group Admin` vs `Member` role badges) in `admin-client/src/main/java/com/chat/admin/view/GroupManagementPanel.java` and register in `admin-client/src/main/java/com/chat/admin/view/AdminMainFrame.java` (Commit #5: `feat(admin): implement group management panel with member and admin viewer`)

**Checkpoint**: User Story 4 (Commit #5) is independently functional.

---

## Phase 7: User Story 5 - Spam Report Triage & Sanctions (Priority: P2)

**Goal**: View, sort, and filter spam reports by date range or username, and lock offending user accounts directly.

**Independent Test**: Open Spam Reports panel, filter by `From Date` - `To Date` (`YYYY-MM-DD`) and username, sort by Time or Username, and click "Lock Offending Account" to verify the reported user is locked.

### Implementation for User Story 5

- [ ] T012 [US5] Implement `SpamReportPanel` (date range filter `From Date` to `To Date` with inverted-date validation, username search filter, sort by Time or Username, spam reports `JTable`, and "Lock Offending Account" button) in `admin-client/src/main/java/com/chat/admin/view/SpamReportPanel.java` and register in `admin-client/src/main/java/com/chat/admin/view/AdminMainFrame.java` (Commit #6: `feat(admin): implement spam report management panel with account ban action`)

**Checkpoint**: User Story 5 (Commit #6) is independently functional.

---

## Phase 8: User Story 6 - New Registration Timeline Filtering (Priority: P3)

**Goal**: Filter new user registrations within a custom start and end date range, filter by name, and sort by name or creation time.

**Independent Test**: Open New Registrations panel, specify start/end dates or select a quick preset, filter by name, and sort by Name or Created Time.

### Implementation for User Story 6

- [ ] T013 [US6] Implement `NewRegistrationPanel` (start/end date inputs `YYYY-MM-DD` with date range validation, quick range buttons, name search filter, sort by Name or Registration Time, total registrations summary badge, and results `JTable`) in `admin-client/src/main/java/com/chat/admin/view/NewRegistrationPanel.java` and register in `admin-client/src/main/java/com/chat/admin/view/AdminMainFrame.java` (Commit #7: `feat(admin): implement new user registration panel with date range filter`)

**Checkpoint**: User Story 6 (Commit #7) is independently functional.

---

## Phase 9: User Story 7 - Direct & Extended Friend Network Analytics (Priority: P3)

**Goal**: View users with their direct friend count and friends-of-friends count, filter by name or comparison operators (`=`, `>`, `<`), and sort by name or creation date.

**Independent Test**: Open Friend Statistics panel, filter by name, test `=`, `>`, `<` comparison filter on direct friends count, and sort by Name or Created Date.

### Implementation for User Story 7

- [ ] T014 [US7] Implement `FriendStatsPanel` (name filter, sort selector by Name or Created Date, Direct Friends comparison filter `All` / `=` / `>` / `<` with non-negative integer validation, summary KPI cards, and full-width `JTable` showing Username, Full Name, Created Date, Direct Friends, and Friends-of-Friends) in `admin-client/src/main/java/com/chat/admin/view/FriendStatsPanel.java` and register in `admin-client/src/main/java/com/chat/admin/view/AdminMainFrame.java` (Commit #8: `feat(admin): implement friend statistics panel with comparison filters`)

**Checkpoint**: User Story 7 (Commit #8) is independently functional.

---

## Phase 10: User Story 8 - User Engagement & Chat Activity Analytics (Priority: P3)

**Goal**: View active users over a date range with app open count, chatted individuals count, and chatted groups count, supporting name filter, comparison filter (`=`, `>`, `<`), and sorting by name or creation date.

**Independent Test**: Open Activity Metrics panel, select a date range, filter by name, apply `=`, `>`, `<` comparison filter on activity count, and sort by Name or Created Date.

### Implementation for User Story 8

- [ ] T015 [US8] Implement `UserActivityPanel` (date range inputs `From Date` - `To Date`, name filter, sort selector by Name or Created Date, Activity count comparison filter `All` / `=` / `>` / `<` with integer validation, summary KPI cards, and `JTable` showing Username, Full Name, Created Date, App Opens, Chatted Individuals, and Chatted Groups) in `admin-client/src/main/java/com/chat/admin/view/UserActivityPanel.java` and register in `admin-client/src/main/java/com/chat/admin/view/AdminMainFrame.java` (Commit #9: `feat(admin): implement user activity panel with custom activity filters`)

**Checkpoint**: User Story 8 (Commit #9) is independently functional.

---

## Phase 11: User Story 9 - Monthly Registration & User Activity Visual Charts (Priority: P3)

**Goal**: Render 12-month JFreeChart bar and line charts for new registrations and active app openers for any selected year.

**Independent Test**: Open Visual Charts panel, select years `2024`, `2025`, `2026`, and verify both `RegistrationChartPanel` (bar chart) and `ActivityChartPanel` (line chart) update their 12-month series accurately.

### Implementation for User Story 9

- [ ] T016 [P] [US9] Implement `RegistrationChartPanel` (JFreeChart 12-month vertical Bar Chart for new user registrations by selected year with `#3B82F6` bar styling and value labels) in `admin-client/src/main/java/com/chat/admin/chart/RegistrationChartPanel.java`
- [ ] T017 [P] [US9] Implement `ActivityChartPanel` (JFreeChart 12-month Line Chart for active users opening the app by selected year with `#10B981` line styling and point markers) in `admin-client/src/main/java/com/chat/admin/chart/ActivityChartPanel.java` and wire both charts into `admin-client/src/main/java/com/chat/admin/view/AdminMainFrame.java` (Commit #10: `feat(admin): implement registration and activity statistics charts using JFreeChart`)

**Checkpoint**: All 9 User Stories and all 10 planned commits are complete and independently functional.

---

## Phase 12: Polish & Cross-Cutting Concerns

**Purpose**: Final build verification, code formatting check, screenshot generation, and AI transparency log update.

- [ ] T018 Run `mvn clean test-compile` and `mvn spotless:check` from repository root to validate zero compile or formatting errors
- [ ] T019 [P] Capture runtime screenshots of all 10 Admin UI screens into `docs/screenshots/admin/` for the Version 1 report
- [ ] T020 [P] Record AI-assisted design and implementation summary in `AI-LOG.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3–11)**: All depend on Foundational phase completion
  - User Story 1 (`AdminMainFrame` & `AdminLoginDialog`) establishes the root container
  - User Stories 2–9 (`US2`–`US9`) plug into `AdminMainFrame` and follow the 10-commit sequence in `docs/Plan_Admin_UI_Version_1.md`
- **Polish (Phase 12)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Plugs into `AdminMainFrame` from US1 - Independently testable
- **User Story 3 (P2)**: Modal dialog triggered from `UserManagementPanel` (US2) or directly - Independently testable
- **User Stories 4–9 (P2–P3)**: Plug into `AdminMainFrame` from US1 - Each panel is self-contained and independently testable

### Parallel Opportunities

- Setup/Foundational tasks `T002`, `T003`, `T004`, `T005` operate on separate files and can run in parallel
- Once `AdminMainFrame` (US1) is in place, panels `GroupManagementPanel` (US4), `SpamReportPanel` (US5), `NewRegistrationPanel` (US6), `FriendStatsPanel` (US7), and `UserActivityPanel` (US8) do not depend on each other
- Within US9, `RegistrationChartPanel` (`T016`) and `ActivityChartPanel` (`T017`) can be implemented in parallel

---

## Parallel Example: User Story 9

```bash
# Launch both JFreeChart panel implementations together:
Task: "Implement RegistrationChartPanel in admin-client/src/main/java/com/chat/admin/chart/RegistrationChartPanel.java"
Task: "Implement ActivityChartPanel in admin-client/src/main/java/com/chat/admin/chart/ActivityChartPanel.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (`T001`–`T002`)
2. Complete Phase 2: Foundational (`T003`–`T006`)
3. Complete Phase 3: User Story 1 (`T007`–`T008` -> Commits #1 & #2)
4. **STOP and VALIDATE**: Test `AdminLoginDialog` and `AdminMainFrame` navigation independently

### Incremental Delivery (10-Commit Workflow)

1. Complete Setup + Foundational → Foundation ready
2. Add US1 (`AdminMainFrame`, `AdminLoginDialog`) → Commits #1 & #2
3. Add US2 (`UserManagementPanel`) → Commit #3
4. Add US3 (`UserDetailHistoryDialog`) → Commit #4
5. Add US4 (`GroupManagementPanel`) → Commit #5
6. Add US5 (`SpamReportPanel`) → Commit #6
7. Add US6 (`NewRegistrationPanel`) → Commit #7
8. Add US7 (`FriendStatsPanel`) → Commit #8
9. Add US8 (`UserActivityPanel`) → Commit #9
10. Add US9 (`RegistrationChartPanel`, `ActivityChartPanel`) → Commit #10

---

## Notes

- `[P]` tasks = different files, no dependencies
- `[Story]` label maps each task to its specific User Story (`[US1]`..`[US9]`) for traceability
- Every task includes exact file paths and respects the strict Spec Kit checklist syntax `- [ ] T### [P?] [US?] Description`
