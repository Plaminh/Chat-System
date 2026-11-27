# Feature Specification: [A5] Danh sách người dùng đăng ký mới (Issue #14)

**Feature Branch**: `feature/admin-client-v2-a5`

**Created**: 2026-11-27

**Status**: Draft

**Input**: User description: "Issue #14: [A5] Danh sách người dùng đăng ký mới tại specs/006-admin-client-v2/admin-new-registrations. Action: ADMIN_LIST_USERS với createdAtRange. Xem danh sách người dùng đăng ký mới theo khoảng thời gian (Requirement 1.2.5). Hỗ trợ lọc theo tên, sắp xếp theo tên hoặc thời gian tạo, phân trang và tính cả hai đầu mút theo giờ Việt Nam."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Newly Registered Users in Date Range (Priority: P1) 🎯 MVP

As a system administrator, I need to select a custom start date and end date window and view all platform users who registered within that period (inclusive of both dates in Vietnam timezone UTC+7) so that I can monitor user acquisition and new signup growth (Requirement 1.2.5).

**Why this priority**: Core deliverable of Issue #14 [A5], providing administrative visibility into newly registered user accounts.

**Independent Test**: An authenticated administrator opens the `NewRegistrationPanel`, sets the start date to `2026-11-01` and end date to `2026-11-27`, and clicks Apply. The system dispatches `ADMIN_LIST_USERS` with `createdAtRange`. The results table displays all accounts created within that window with username, full name, email, gender, date of birth, address, registration timestamp, and account status.

**Acceptance Scenarios**:
1. **Given** an authenticated administrator session, **When** requesting users with `createdAtRange = { from: "2026-11-01", to: "2026-11-27" }`, **Then** the server queries users whose `created_at >= 2026-11-01T00:00:00+07:00` and `created_at < 2026-11-28T00:00:00+07:00` and returns `PageResult<AdminUserInfo>`.
2. **Given** the returned account records, **When** rendered in `NewRegistrationPanel`, **Then** the KPI summary cards display the total matching registrations and active signup counts accurately.

---

### User Story 2 - Filter by Name & Search within New Registrations (Priority: P2)

As a system administrator, I need to enter a search query to filter the newly registered user list by full name or username so that I can quickly locate specific accounts among new signups.

**Why this priority**: Allows immediate discovery of specific users without manually scanning through multi-page signup timelines.

**Independent Test**: Type a keyword (e.g., "alice") in the search field while a date range is active. The system sends `ADMIN_LIST_USERS` with `query = "alice"` and `createdAtRange`. The table displays only newly registered users whose username or full name contains "alice" (case-insensitive).

**Acceptance Scenarios**:
1. **Given** an active date range, **When** entering a name/username keyword, **Then** only matching users registered in that period are returned.
2. **Given** an empty query string or whitespace, **When** dispatched, **Then** the filter is ignored and all registered users in the date window are returned.

---

### User Story 3 - Sort by Name or Registration Time & Pagination (Priority: P3)

As a system administrator, I need to toggle sort order between creation time (newest/oldest) and full name (A-Z / Z-A) and browse through pages of new accounts so that I can inspect high-volume signup bursts efficiently.

**Why this priority**: Supports large cohorts of registrations and flexible audit timelines.

**Independent Test**: Select "Sort: Created Time (Newest First)", "Sort: Created Time (Oldest First)", "Sort: Full Name (A-Z)", or "Sort: Full Name (Z-A)". Verify that the server returns the accounts in the requested order.

**Acceptance Scenarios**:
1. **Given** sort `field = "createdAt"` and `direction = "DESC"`, **Then** accounts registered most recently appear first.
2. **Given** sort `field = "name"` and `direction = "ASC"`, **Then** accounts are ordered alphabetically by full name.
3. **Given** page `0` and size `50`, **Then** the server returns up to 50 items with accurate `totalItems` and `totalPages`.

---

### Edge Cases

- **Invalid Date Range (Start Date > End Date)**: If `startDate.isAfter(endDate)`, the client prevents request submission and shows a warning dialog. If such a request reaches the server, the server rejects it with `ErrorCode.VALIDATION_FAILED`.
- **Zero Matching Registrations**: If no accounts were created within the selected range, the server returns an empty list (`items: []`, `totalItems: 0`, `totalPages: 0`) and the UI displays "Showing 0 newly registered accounts".
- **Invalid Sort Field**: If a client requests a sort field other than `"name"` or `"createdAt"`, the server returns `VALIDATION_FAILED` (via `InvalidSortFieldException`).
- **Non-Admin Access**: If an unauthenticated user or user with role `USER` attempts `ADMIN_LIST_USERS`, the server rejects with `ErrorCode.UNAUTHORIZED` or `UNAUTHENTICATED`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Server MUST implement `AdminListUsersHandler` for `ActionType.ADMIN_LIST_USERS` accepting `AdminUserFilter(query, accountStatus, createdAtRange, sort, page)`.
- **FR-002**: Server `ActionDispatcher` MUST restrict `ADMIN_LIST_USERS` to authenticated administrators with role `ADMIN`, returning `ErrorCode.UNAUTHORIZED` for regular users.
- **FR-003**: Server MUST query `users` table via `UserRepository.findForAdmin` applying `createdAtRange` bounds with timezone UTC+7 (inclusive of both endpoints).
- **FR-004**: Server MUST enforce allow-list for sort fields, accepting only `"name"` and `"createdAt"` with `ASC` or `DESC`.
- **FR-005**: Server MUST return `PageResult<AdminUserInfo>` with full `UserAccountDetail` and `UserSummary` (including live presence status from `OnlineSessionRegistry`).
- **FR-006**: Admin Client `NewRegistrationPanel` MUST call `SocketClient.sendAsync(ADMIN_LIST_USERS, ...)` to load live data, replacing `AdminMockDataStore`.
- **FR-007**: Admin Client MUST validate date ranges (`startDate <= endDate`) before dispatching network requests, presenting clear error notifications for invalid inputs.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Date-range queries return in under 150 ms for page sizes up to 100 on localhost.
- **SC-002**: 100% of non-admin requests to `ADMIN_LIST_USERS` are rejected with `UNAUTHORIZED`.
- **SC-003**: 100% of invalid sort field requests are rejected with `VALIDATION_FAILED`.
- **SC-004**: When start date > end date, validation error is cleanly returned.
- **SC-005**: Full reactor automated test suite (`mvn clean test`) passes with zero failures and zero errors across all modules.
