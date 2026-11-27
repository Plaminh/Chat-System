# Tasks: [A5] Danh sách người dùng đăng ký mới (Issue #14)

**Branch**: `feature/admin-client-v2-a5` | **Date**: 2026-11-27 | **Spec**: [spec.md](spec.md)

## Phase 1: Setup & Handler Test Suite (TDD)
- [x] T001 Create `AdminListUsersHandlerTest` in `server/src/test/java/com/chat/server/handler/AdminListUsersHandlerTest.java` with tests for:
  - Valid date range query returning `PageResult<AdminUserInfo>`
  - Name keyword filtering
  - Non-admin authorization rejection (`UNAUTHORIZED`)
  - Invalid sort field rejection (`VALIDATION_FAILED`)
  - Start date after end date rejection (`VALIDATION_FAILED`)
  - Empty result handling

## Phase 2: Server Handler Implementation
- [x] T002 Implement `AdminListUsersHandler` in `server/src/main/java/com/chat/server/handler/impl/AdminListUsersHandler.java`
  - Map `ActionType.ADMIN_LIST_USERS`
  - Decode payload to `AdminUserFilter`
  - Enforce sort field allow-list (`"name"`, `"createdAt"`)
  - Call `userRepository.findForAdmin(...)`
  - Map each `User` to `AdminUserInfo` with live presence from `OnlineSessionRegistry`
  - Wrap in `ResponseEnvelope`
- [x] T003 Verify `ActionDispatcher` routes `ADMIN_LIST_USERS` and enforces admin privileges

## Phase 3: Admin Client UI & Integration
- [x] T004 Wire `NewRegistrationPanel` to `SocketClient`
  - Replace `AdminMockDataStore` calls with `SocketClient.sendAsync(ActionType.ADMIN_LIST_USERS, ...)`
  - Map UI sort combobox to `SortSpec` (`createdAt` / `name`)
  - Map UI date pickers to `DateRange`
  - Validate date range (`startDate <= endDate`) before dispatching
  - Bind returned `PageResult<AdminUserInfo>` to `JTable`
  - Update KPI summary badges (`matchingRegistrationsValueLabel`, `activeSignupsValueLabel`, `footerSummaryLabel`)

## Phase 4: Reactor Verification & AI-LOG
- [x] T005 Run Maven test suite across all 5 modules (`mvn clean test`)
- [x] T006 Run `mvn spotless:check` to ensure formatting compliance
- [x] T007 Update `AI-LOG.md` recording all changes for Issue #14 [A5]
