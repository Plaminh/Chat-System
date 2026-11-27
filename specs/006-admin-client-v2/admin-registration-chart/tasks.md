# Tasks: [A6] Biểu đồ số lượng người dùng đăng ký mới (Issue #15)

**Branch**: `feature/admin-client-v2-a6` | **Date**: 2026-11-27 | **Spec**: [spec.md](spec.md)

## Phase 1: Setup & Server Handler Test Suite (TDD)
- [x] T001 Create `AdminGetMonthlyRegistrationsHandlerTest` in `server/src/test/java/com/chat/server/handler/AdminGetMonthlyRegistrationsHandlerTest.java`
  - Valid year query returning 12 `MonthlyCount` entries
  - Non-admin authorization rejection (`UNAUTHORIZED`)
  - Invalid year rejection (`VALIDATION_FAILED`)
  - Months without signups having `count = 0`

## Phase 2: Server Handler Implementation
- [x] T002 Implement `AdminGetMonthlyRegistrationsHandler` in `server/src/main/java/com/chat/server/handler/impl/AdminGetMonthlyRegistrationsHandler.java`
  - Map `ActionType.ADMIN_GET_MONTHLY_REGISTRATIONS`
  - Decode payload to `YearRequest`
  - Call `userRepository.countMonthlyRegistrations(year.year())`
  - Transform `long[12]` to `List<MonthlyCount>`
  - Wrap in `ResponseEnvelope`
- [x] T003 Verify `ActionDispatcher` routes `ADMIN_GET_MONTHLY_REGISTRATIONS` and enforces admin privileges

## Phase 3: Admin Client UI & Integration
- [x] T004 Wire `RegistrationChartPanel` to `SocketClient`
  - Dispatch `SocketClient.sendAsync(ActionType.ADMIN_GET_MONTHLY_REGISTRATIONS, new YearRequest(year))`
  - On response, map `List<MonthlyCount>` to JFreeChart `DefaultCategoryDataset`
  - Update KPI metrics: Annual Total, Peak Month, Monthly Average
  - Maintain offline fallback to `AdminMockDataStore`
  - Add "Làm mới" refresh button to chart header
- [x] T005 Create unit test for `RegistrationChartPanel` in `admin-client`

## Phase 4: Reactor Verification & AI-LOG
- [x] T006 Run Maven test suite across all modules (`mvn test -pl common,server,admin-client -am`)
- [x] T007 Update `AI-LOG.md` recording all changes for Issue #15 [A6]
