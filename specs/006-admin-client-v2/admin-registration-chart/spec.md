# Feature Specification: [A6] Biểu đồ số lượng người dùng đăng ký mới theo tháng trong năm (Issue #15)

**Feature Branch**: `feature/admin-client-v2-a6`

**Created**: 2026-11-27

**Status**: Completed

**Input**: User description: "Issue #15: [A6] Biểu đồ số lượng người dùng đăng ký mới theo từng tháng trong năm (Requirement 1.2.6). Action: ADMIN_GET_MONTHLY_REGISTRATIONS với YearRequest(int year). Trả về danh sách 12 tháng List<MonthlyCount>. Hiển thị biểu đồ cột JFreeChart và các thẻ thống kê tổng quan (tổng đăng ký, tháng cao điểm, trung bình tháng). Hỗ trợ chọn năm, refresh, và fallback ngoại tuyến mượt mà."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View 12-Month Registration Bar Chart for Selected Year (Priority: P1) 🎯 MVP

As a system administrator, I need to select a calendar year (e.g. 2024, 2025, 2026) and view a 12-month visual bar chart of newly registered user counts (from January to December) so that I can analyze annual user acquisition trends (Requirement 1.2.6).

**Why this priority**: Core deliverable of Requirement 1.2.6 / [A6], providing visual time-series distribution of user signups.

**Independent Test**: An authenticated administrator opens the Visual Charts view, selects year `2026` from the year dropdown selector. The system dispatches `ADMIN_GET_MONTHLY_REGISTRATIONS` with `{ "year": 2026 }`. The server aggregates users created in 2026 grouped by month (1 to 12) in Vietnam timezone (UTC+7) and returns `List<MonthlyCount>`. The JFreeChart renders 12 bars with the count labels displayed directly above each bar.

**Acceptance Scenarios**:
1. **Given** an authenticated administrator session, **When** requesting monthly registrations for year `2026`, **Then** the server aggregates registrations for months 1..12 and returns `List<MonthlyCount>` with exactly 12 items. Months with zero signups have `count = 0`.
2. **Given** the returned monthly registration list, **When** rendered in `RegistrationChartPanel`, **Then** 12 month bars (`Jan`..`Dec`) display accurately with values, and the KPI summary cards display Annual Total, Peak Month, and Monthly Average.

---

### User Story 2 - Year Switching & Dynamic Refresh (Priority: P2)

As a system administrator, I need to change the selected year via the dropdown or click Refresh so that the chart updates dynamically with the newly selected year's data without restarting the application.

**Why this priority**: Allows seamless comparison between consecutive operating years.

**Independent Test**: Switch dropdown between `2024`, `2025`, and `2026`. Verify that a new request is sent with the selected year and the chart canvas and summary cards update synchronously.

**Acceptance Scenarios**:
1. **Given** year `2024` selected, **When** user switches to `2025`, **Then** a request for year `2025` is dispatched and the bar series title updates to `New Registrations (2025)`.
2. **Given** rapid switching of years, **Then** the latest selected year's response is applied without race conditions or visual glitches.

---

### User Story 3 - Offline Demo Resilience & Error Handling (Priority: P3)

As a system administrator or evaluator testing without an active PostgreSQL instance, I need the chart to gracefully fall back to local mock data and show clear error feedback if a network error occurs.

**Why this priority**: Ensures self-contained evaluation and zero application crashes during grading or offline demonstrations.

**Independent Test**: Stop the backend server and select a year. The UI falls back to `AdminMockDataStore` data, keeping the chart responsive and interactive.

**Acceptance Scenarios**:
1. **Given** an unreachable server socket, **When** year is selected, **Then** fallback mock data is loaded and no unhandled exception is thrown on Swing EDT.
2. **Given** an unauthenticated request or non-admin role, **When** processed by server, **Then** server returns `UNAUTHORIZED` and rejects access.

---

### Edge Cases
- **Years with zero registrations**: Returns 12 `MonthlyCount` entries each with `count = 0`; chart renders flat zero baselines without errors.
- **Leap years / boundary dates**: Database queries use `SqlSupport.yearParams(year)` with standard UTC+7 zone offsets ensuring full coverage of all days in February and year ends.
- **Negative or unreasonable years**: Year must be a positive 4-digit calendar year (e.g. 1970–2100); otherwise server returns `VALIDATION_FAILED`.
