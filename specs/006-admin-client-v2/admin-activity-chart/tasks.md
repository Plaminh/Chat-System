# Tasks: [A9] Biểu đồ số lượng người dùng hoạt động (Issue #16)

**Branch**: `feature/admin-client-v2-a9` | **Date**: 2026-11-27 | **Spec**: [spec.md](spec.md)

## Phase 1: Setup & Server Handler Test Suite (TDD)
- [x] T001 Create `AdminGetMonthlyActiveUsersHandlerTest` in `server/src/test/java/com/chat/server/handler/AdminGetMonthlyActiveUsersHandlerTest.java`
  - Valid year query returning 12 `MonthlyCount` entries
  - Non-admin authorization rejection (`UNAUTHORIZED`)
  - Invalid year rejection (`VALIDATION_FAILED`)
  - Distinct user counting per month verification

## Phase 2: Server Handler Implementation
- [x] T002 Implement `AdminGetMonthlyActiveUsersHandler` in `server/src/main/java/com/chat/server/handler/impl/AdminGetMonthlyActiveUsersHandler.java`
  - Map `ActionType.ADMIN_GET_MONTHLY_ACTIVE_USERS`
  - Decode payload to `YearRequest`
  - Call `userActivityLogRepository.countMonthlyActiveUsers(year.year())`
  - Transform `long[12]` to `List<MonthlyCount>`
  - Wrap in `ResponseEnvelope`
- [x] T003 Verify `ActionDispatcher` routes `ADMIN_GET_MONTHLY_ACTIVE_USERS` and enforces admin privileges

## Phase 3: Admin Client UI & Integration
- [x] T004 Wire `ActivityChartPanel` to `SocketClient`
  - Dispatch `SocketClient.sendAsync(ActionType.ADMIN_GET_MONTHLY_ACTIVE_USERS, new YearRequest(year))`
  - On response, map `List<MonthlyCount>` to JFreeChart `DefaultCategoryDataset`
  - Update KPI metrics: Annual Active Total, Peak Activity Month, Monthly Average
  - Maintain offline fallback to `AdminMockDataStore`
  - Add "Làm mới" refresh button to chart header
- [x] T005 Create unit test for `ActivityChartPanel` in `admin-client`

## Phase 4: Reactor Verification & AI-LOG
- [x] T006 Run Maven test suite across all modules (`mvn test -pl server,admin-client -am`)
- [x] T007 Update `AI-LOG.md` recording all changes for Issue #16 [A9]
