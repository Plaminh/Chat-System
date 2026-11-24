# Feature Specification: [A2] Danh sách đăng nhập theo thứ tự thời gian (Admin Login History)

**Feature Branch**: `feature/admin-client-v2`

**Created**: 2026-11-24

**Status**: Draft

**Input**: User description: "Issue #8: [A2] Danh sách đăng nhập theo thứ tự thời gian tại specs/006-admin-client-v2/admin-login-history. Action: ADMIN_LIST_LOGIN_HISTORY. Xem danh sách đăng nhập theo thứ tự thời gian (Requirement 1.2.2) và theo từng người dùng (Requirement 1.2.1). Hỗ trợ phân trang và sắp xếp theo loggedInAt."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Global Chronological Login History (Priority: P1) 🎯 MVP

As a system administrator, I need to view the system-wide login audit history in chronological order (newest logins first by default) so that I can monitor platform access activity across all users (Requirement 1.2.2).

**Why this priority**: Core deliverable of Issue #8 [A2], providing platform-wide visibility into user login events.

**Independent Test**: An authenticated administrator opens the login history audit stream (via `UserDetailHistoryDialog` with scope "All System Users (Chronological)" or dedicated audit view), sending an `ADMIN_LIST_LOGIN_HISTORY` request with `userId = null`. The system displays a table containing login timestamp (`loggedInAt`), username, full name, and session status/logout timestamp.

**Acceptance Scenarios**:
1. **Given** an authenticated administrator session, **When** requesting global login history with `userId = null` and `sort = { field: "loggedInAt", direction: DESC }`, **Then** the server queries `login_history` joined with `users` and returns a `PageResult<LoginHistoryEntry>` sorted chronologically.
2. **Given** the returned login entries, **When** displayed in the admin UI table, **Then** each row displays the login timestamp formatted nicely, `@username`, full name, and logout status (`Success` or active session indicator).

---

### User Story 2 - Inspect User-Specific Login History (Priority: P2)

As a system administrator, I need to filter the login history audit log for a specific target user so that I can audit that individual's authentication events and track suspicious account access (Requirement 1.2.1).

**Why this priority**: Required for detailed security investigation of specific user accounts from the User Management directory.

**Independent Test**: In the User Directory, select a user and open the inspection dialog with scope "Selected User (@username)". The client dispatches `ADMIN_LIST_LOGIN_HISTORY` with the user's `userId`. The table displays only login sessions belonging to that specific user.

**Acceptance Scenarios**:
1. **Given** a valid target `userId`, **When** the administrator requests login history with `userId = <targetUserId>`, **Then** the server validates the user's existence and returns only login history records associated with that `userId`.
2. **Given** a non-existent `userId`, **When** the administrator requests login history, **Then** the server returns error code `NOT_FOUND` with message `"User not found"`.

---

### User Story 3 - Pagination & Chronological Sorting Order (Priority: P3)

As a system administrator, I need to navigate paginated results and toggle between descending (newest first) and ascending (oldest first) chronological order so that I can effectively browse large volumes of historical audit records without system performance degradation.

**Why this priority**: Prevents loading excessive records into memory and allows flexible audit investigation timelines.

**Independent Test**: Request page 0 with size 20, verify total items and total pages. Request sort ascending (`direction: ASC`), verify that earlier logins appear before later logins.

**Acceptance Scenarios**:
1. **Given** a large set of login records, **When** requesting `page = 0, size = 20`, **Then** the response contains at most 20 items and includes accurate `totalItems` and `totalPages`.
2. **Given** a sort specification with `direction = ASC` for `field = "loggedInAt"`, **Then** the records are returned ordered from oldest to newest.

---

### Edge Cases

- **Non-existent Target User**: When filtering by `userId` and the user does not exist in the database, the server returns `NOT_FOUND` with message `"User not found"`.
- **Zero Login Records**: When a user or system has no recorded logins, the server returns an empty list (`items: []`) with `totalItems: 0` and `totalPages: 0`.
- **Invalid Sort Field**: If a client requests a sort field other than `"loggedInAt"`, the server rejects the request with `VALIDATION_FAILED` (via `InvalidSortFieldException`).
- **Invalid Pagination Arguments**: If `page < 0` or `size < 1 || size > 100`, the server rejects the request with `VALIDATION_FAILED` (via `IllegalArgumentException`).
- **Unauthorized / Non-Admin Access**: If a regular user or unauthenticated connection attempts `ADMIN_LIST_LOGIN_HISTORY`, the server returns `UNAUTHORIZED` or `UNAUTHENTICATED`.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Server MUST implement `AdminListLoginHistoryHandler` for `ActionType.ADMIN_LIST_LOGIN_HISTORY` accepting `LoginHistoryRequest(userId, sort, page)`.
- **FR-002**: Server `ActionDispatcher` MUST restrict `ADMIN_LIST_LOGIN_HISTORY` to authenticated administrators with role `ADMIN`, returning `ErrorCode.UNAUTHORIZED` for non-admin users.
- **FR-003**: Server MUST support querying global login history across all users when `userId` is `null`.
- **FR-004**: Server MUST support filtering login history for a specific user when `userId` is provided, and return `ErrorCode.NOT_FOUND` if the user does not exist.
- **FR-005**: Server MUST enforce an allow-list for sort fields, permitting only `loggedInAt` with either `ASC` or `DESC` direction.
- **FR-006**: Server MUST return paginated results as `PageResult<LoginHistoryEntry>` including `loginHistoryId`, `userId`, `username`, `fullName`, `loggedInAt`, and `loggedOutAt`.
- **FR-007**: Admin Client MUST integrate `ADMIN_LIST_LOGIN_HISTORY` into `UserDetailHistoryDialog` to fetch and render real login audit data from the server.
- **FR-008**: Admin Client MUST handle network errors and empty results gracefully, presenting clear status notifications to the administrator.

### Key Entities

- **LoginHistoryRequest**: `{ "userId": Long (optional), "sort": SortSpec ("loggedInAt", ASC|DESC), "page": PageRequest (page >= 0, size 1..100) }`
- **LoginHistoryEntry**: `{ "loginHistoryId": long, "userId": long, "username": String, "fullName": String, "loggedInAt": OffsetDateTime, "loggedOutAt": OffsetDateTime (nullable) }`
- **PageResult<LoginHistoryEntry>**: `{ "items": List<LoginHistoryEntry>, "page": int, "size": int, "totalItems": long, "totalPages": int }`
- **SortSpec**: `{ "field": "loggedInAt", "direction": "ASC" | "DESC" }`
- **PageRequest**: `{ "page": int, "size": int }`

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Chronological login history queries complete in under 150 ms for paginated page sizes up to 100 on localhost.
- **SC-002**: 100% of non-admin requests to `ADMIN_LIST_LOGIN_HISTORY` are rejected with `UNAUTHORIZED`.
- **SC-003**: 100% of invalid sort field requests are rejected with `VALIDATION_FAILED`.
- **SC-004**: System-wide login events logged during authentication (`LOGIN`) are immediately queryable in chronological order.
- **SC-005**: Full reactor automated test suite (`mvn clean test`) passes with zero failures and zero errors across all modules.

## Assumptions

- Database table `login_history` and `LoginHistoryRepository.find(userId, sort, page)` already exist in `server` and use PostgreSQL `TIMESTAMPTZ` with proper indexes.
- Default sort order in the admin client is `loggedInAt` descending (newest first).
- Default page size is 50 items.
