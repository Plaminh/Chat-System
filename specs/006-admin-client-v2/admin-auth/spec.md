# Feature Specification: [A0] Đăng nhập quản trị (Admin Authentication)

**Feature Branch**: `feature/admin-client-v2`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "Issue #7 [A0] Đăng nhập quản trị tại specs/006-admin-client-v2/admin-auth. Action: LOGIN, LOGOUT, ADMIN_GET_USER. Sau khi LOGIN thành công, admin-client gọi ADMIN_GET_USER với userId của chính mình. Nếu nhận AdminUserInfo (role ADMIN) thì vào AdminMainFrame. Nếu nhận UNAUTHORIZED thì gọi LOGOUT và báo 'Tài khoản không có quyền quản trị'. Nối AdminLoginDialog, bật bước kiểm tra quyền admin cho mọi action ADMIN_*, làm luôn handler ADMIN_GET_USER."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Successful Admin Authentication Flow (Priority: P1) 🎯 MVP

As an administrator, I need to log in through the Admin Login dialog (`AdminLoginDialog`) using my administrative credentials so that I can gain access to the Admin Console (`AdminMainFrame`) to manage the system.

**Why this priority**: Authentication is the foundational prerequisite for all administrative modules in Admin Client V2.

**Independent Test**: Enter valid admin credentials in `AdminLoginDialog`, verify that the client connects to the server via socket, executes `LOGIN`, verifies `role == ADMIN` via `ADMIN_GET_USER`, closes the login dialog, and opens `AdminMainFrame` displaying the admin user's identity.

**Acceptance Scenarios**:
1. **Given** a valid administrator account in PostgreSQL (`role = ADMIN`, `account_status = ACTIVE`), **When** username and password are submitted via `AdminLoginDialog`, **Then** the server authenticates the BCrypt password hash and returns `AuthenticationResult`.
2. **Given** a successful `LOGIN` response, **When** the client queries `ADMIN_GET_USER` with the received `userId`, **Then** the server responds with `AdminUserInfo` containing `role: ADMIN`.
3. **Given** verified admin privileges, **When** the client completes validation, **Then** `AdminLoginDialog` closes and `AdminMainFrame` opens displaying the admin's name and dashboard tabs.

---

### User Story 2 - Non-Admin Role Rejection & Auto-Logout (Priority: P2)

As a security auditor, I need the Admin Client to strictly reject non-admin users (`role = USER`) so that unauthorized users cannot view or manipulate administrative settings.

**Why this priority**: Prevents privilege escalation and unauthorized access to administrative data.

**Independent Test**: Enter credentials of a regular user account (`role = USER`) in `AdminLoginDialog`. Verify that `LOGIN` succeeds, but the subsequent `ADMIN_GET_USER` request is rejected with `ErrorCode.UNAUTHORIZED`. Verify that the client automatically dispatches `LOGOUT` to destroy the server session, re-enables the form, and displays: *"Tài khoản không có quyền quản trị"*.

**Acceptance Scenarios**:
1. **Given** valid credentials for an account with role `USER`, **When** login is submitted through `AdminLoginDialog`, **Then** the initial `LOGIN` succeeds, but `ADMIN_GET_USER` returns `errorCode: UNAUTHORIZED`.
2. **Given** an `UNAUTHORIZED` response on role verification, **When** the client detects lack of admin rights, **Then** it immediately sends a `LOGOUT` request to the server, clears local credentials, and displays: *"Tài khoản không có quyền quản trị"*.

---

### User Story 3 - Administrator Sign Out (Priority: P3)

As an authenticated administrator, I need to sign out from the Admin Console so that my active session is terminated on the server and the application returns to the login screen.

**Why this priority**: Ensures workstation security and clean session lifecycle termination.

**Independent Test**: Click "Sign Out" in `AdminMainFrame`, verify that `LOGOUT` is sent over the socket, the server session is cleared, `AdminMainFrame` is disposed, and `AdminLoginDialog` is displayed.

**Acceptance Scenarios**:
1. **Given** an active authenticated session in `AdminMainFrame`, **When** the admin clicks "Sign Out", **Then** the client sends a `LOGOUT` request to the server.
2. **Given** a `LOGOUT` request received by the server, **When** `LogoutHandler` processes it, **Then** the socket's `SessionContext` is cleared and the connection is unbound from `OnlineSessionRegistry`.
3. **Given** successful logout completion, **When** the client handles the response, **Then** `AdminMainFrame` closes and `AdminLoginDialog` is displayed with empty/default inputs.

---

### Edge Cases

- **Invalid Username or Incorrect Password**: Server returns `success: false`, `errorCode: INVALID_CREDENTIALS`, message `"Invalid username or password"`. Client re-enables form and displays error.
- **Account Locked (`LOCKED`)**: Server returns `errorCode: ACCOUNT_LOCKED`, message `"Account is locked by an administrator"`. Client displays warning alert.
- **Account Deactivated / Deleted (`DELETED`)**: Server returns `errorCode: ACCOUNT_DELETED`, message `"Account has been deactivated"`. Client displays notification dialog.
- **Network Outage / Connection Drop**: If socket connection drops before or during authentication, client catches exception, re-enables login button, and displays: *"Không thể kết nối đến máy chủ"*.
- **Double-click Prevention**: Client disables "Sign In" button and text inputs immediately upon submission until a response is received or timed out.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Server MUST implement `LoginHandler` for `ActionType.LOGIN` accepting `LoginRequest(username, password)`.
- **FR-002**: Server MUST query `userRepository.findByUsername(username)` and compare password using BCrypt password hashing.
- **FR-003**: Server MUST reject invalid credentials with `INVALID_CREDENTIALS` without disclosing whether the username exists.
- **FR-004**: Server MUST reject accounts with status `LOCKED` using `ACCOUNT_LOCKED` and status `DELETED` using `ACCOUNT_DELETED`.
- **FR-005**: Server MUST record successful login in `login_history` table via `loginHistoryRepository.recordLogin(userId)`.
- **FR-006**: Server MUST promote socket `SessionContext` with `userId` (Long), `username`, and `UserRole` upon successful authentication.
- **FR-007**: Server MUST implement `LogoutHandler` for `ActionType.LOGOUT` that resets `SessionContext` via `session.clear()`.
- **FR-008**: Server MUST implement `AdminGetUserHandler` for `ActionType.ADMIN_GET_USER` accepting `UserTarget(targetUserId)` and returning `AdminUserInfo`.
- **FR-009**: Server `ActionDispatcher` MUST enforce admin role authorization for `ADMIN_GET_USER` and all `ADMIN_*` actions, returning `UNAUTHORIZED` if role is not `ADMIN`.
- **FR-010**: Admin Client MUST connect via `SocketClient`, send `LOGIN`, and upon success verify admin role via `ADMIN_GET_USER`.
- **FR-011**: Admin Client MUST automatically call `LOGOUT` and reject login if `ADMIN_GET_USER` returns `UNAUTHORIZED`.
- **FR-012**: Admin Client MUST disable form inputs during request flight to prevent duplicate submissions.

### Key Entities

- **LoginRequest**: `{ "username": String, "password": String }`
- **AuthenticationResult**: `{ "user": UserSummary, "authenticatedAt": OffsetDateTime }`
- **AdminUserInfo**: `{ "account": UserAccountDetail }` containing full user profile including `role: UserRole`.
- **UserTarget**: `{ "targetUserId": long }`

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Admin authentication workflow (`LOGIN` + `ADMIN_GET_USER`) completes in under 200 ms on localhost.
- **SC-002**: 100% of non-admin login attempts are rejected with *"Tài khoản không có quyền quản trị"* and automatic server session teardown.
- **SC-003**: 100% of incorrect password, locked account, and deleted account attempts are rejected with exact protocol error codes.
- **SC-004**: 100% of successful logins generate an audit row in `login_history` database table.
- **SC-005**: Full reactor test suite (`mvn clean test`) passes with zero failures or errors across all modules.

## Assumptions

- PostgreSQL database is initialized via Docker with schema and development seed data containing an admin account (username `admin`).
- Database user ID is `long` (`BIGSERIAL`).
