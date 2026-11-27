# Feature Specification: [A9] Biểu đồ số lượng người dùng hoạt động theo tháng trong năm (Issue #16)

**Feature Branch**: `feature/admin-client-v2-a9`

**Created**: 2026-11-27

**Status**: Completed

**Input**: User description: "Issue #16: [A9] Biểu đồ số lượng người dùng hoạt động theo từng tháng trong năm (Requirement 1.2.9). Action: ADMIN_GET_MONTHLY_ACTIVE_USERS với YearRequest(int year). Trả về danh sách 12 tháng List<MonthlyCount> đếm số lượng người dùng duy nhất (distinct users) mở ứng dụng (activity_type = 'app_open') trong từng tháng. Hiển thị biểu đồ đường JFreeChart (Line Chart) và các thẻ KPI tổng quan (tổng người dùng theo tháng, tháng cao điểm, trung bình tháng). Hỗ trợ chọn năm, nút làm mới và fallback ngoại tuyến mượt mà."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View 12-Month Distinct Active Users Line Chart for Selected Year (Priority: P1) 🎯 MVP

As a system administrator, I need to select a calendar year (e.g. 2024, 2025, 2026) and view a 12-month visual line chart representing the count of distinct users who opened the application at least once in each month (January through December) so that I can analyze annual user engagement trends (Requirement 1.2.9).

**Why this priority**: Core deliverable of Requirement 1.2.9 / [A9], providing visual metrics on distinct platform active users across time.

**Independent Test**: An authenticated administrator opens the Visual Charts view and selects year `2026`. The system dispatches `ADMIN_GET_MONTHLY_ACTIVE_USERS` with `{ "year": 2026 }`. The server aggregates `count(DISTINCT user_id)` from `user_activity_logs` where `activity_type = 'app_open'` for months 1 to 12 in Vietnam timezone (UTC+7) and returns `List<MonthlyCount>`. The JFreeChart renders 12 points on a connected line chart with count labels displayed above each point.

**Acceptance Scenarios**:
1. **Given** an authenticated administrator session, **When** requesting monthly active users for year `2026`, **Then** the server aggregates distinct active users for months 1..12 and returns `List<MonthlyCount>` with exactly 12 items. Months with zero activity have `count = 0`.
2. **Given** the returned monthly active user list, **When** rendered in `ActivityChartPanel`, **Then** 12 month nodes (`Jan`..`Dec`) display accurately with values, and the KPI summary cards display Annual Active Total, Peak Activity Month, and Monthly Average.

---

### User Story 2 - Year Switching & Dynamic Refresh (Priority: P2)

As a system administrator, I need to switch the selected year via the dropdown or click the "Làm mới" (Refresh) button so that the chart updates dynamically with the newly selected year's data without restarting the application.

**Why this priority**: Allows immediate comparison of user retention and activity across different operational years.

**Independent Test**: Switch dropdown between `2024`, `2025`, and `2026`. Verify that a new request is sent with the selected year and the chart canvas and summary cards update synchronously.

**Acceptance Scenarios**:
1. **Given** year `2024` selected, **When** user switches to `2025`, **Then** a request for year `2025` is dispatched and the line series title updates to `Active Users (2025)`.
2. **Given** the "Làm mới" button is clicked, **Then** the active user metrics are re-fetched from the server and the UI updates in place.

---

### User Story 3 - Offline Demo Resilience & Error Handling (Priority: P3)

As a system administrator or evaluator testing without an active PostgreSQL instance, I need the chart to gracefully fall back to local mock data and show clear error feedback if a network error occurs.

**Why this priority**: Ensures self-contained evaluation and zero application crashes during grading or offline demonstrations.

**Independent Test**: Disconnect from the backend server and select a year. The UI falls back to `AdminMockDataStore` data, keeping the chart responsive and interactive.

**Acceptance Scenarios**:
1. **Given** an unreachable server socket, **When** year is selected, **Then** fallback mock data is loaded and no unhandled exception is thrown on Swing EDT.
2. **Given** an unauthenticated request or non-admin role, **When** processed by server, **Then** server returns `UNAUTHORIZED` and rejects access.

---

### Edge Cases
- **Years with zero app opens**: Returns 12 `MonthlyCount` entries each with `count = 0`; chart renders flat zero baselines without errors.
- **Multiple logins by the same user within the same month**: Counted only ONCE per month via `count(DISTINCT user_id)`.
- **Negative or unreasonable years**: Year must be a positive 4-digit calendar year (1970–2100); otherwise server returns `VALIDATION_FAILED`.
