# Research: [A0] Admin Authentication & Authorization (Issue #7)

**Feature**: `specs/006-admin-client-v2/admin-auth` | **Date**: 2026-11-23

## Research Questions & Architectural Decisions

### 1. Password Verification & Cryptography Stack

- **Context**: Passwords in PostgreSQL `users.password_hash` are salted hashes. Plain text passwords submitted over TCP socket `LoginRequest` must be securely verified without timing leaks or plain text storage.
- **Decision**: Use `org.mindrot.jbcrypt.BCrypt`:
  ```java
  boolean matches = BCrypt.checkpw(password, user.passwordHash());
  ```
  `jbcrypt:0.4` is already managed in the root POM and included in `server/pom.xml`.
- **Rationale**: Complies with Constitution Principle III (preserving existing dependencies) and Principle X (never inventing custom cryptography).

---

### 2. Two-Step Admin Role Verification (The `common` Constraint)

- **Context**: In `common`, `AuthenticationResult` only wraps `UserSummary(userId, username, fullName, accountStatus, presence)`. It does NOT expose `UserRole`.
- **Constraint**: University and team rules strictly forbid altering `common` module contracts or inventing new DTO fields.
- **Decision**: Follow the assignment's designed two-step verification workflow:
  1. Client sends `LOGIN` with `username` and `password`.
  2. Server verifies credentials, promotes socket `SessionContext` with `userId` and `role`, logs the login in `login_history`, and returns `AuthenticationResult`.
  3. Client immediately issues `ADMIN_GET_USER` with `UserTarget(userId)` on the same socket connection.
  4. Server `ActionDispatcher` checks role:
     - If socket session has `UserRole.USER`, `ActionDispatcher` rejects the request with `ErrorCode.UNAUTHORIZED`.
     - If socket session has `UserRole.ADMIN`, `AdminGetUserHandler` fetches the full `AdminUserInfo` (which contains `role: ADMIN`) and returns it.
  5. If Client receives `UNAUTHORIZED`: It dispatches `LOGOUT` to clean up the session on the server and displays: *"Tài khoản không có quyền quản trị"*.
  6. If Client receives `AdminUserInfo`: It stores the admin profile and launches `AdminMainFrame`.
- **Rationale**: 100% compliant with existing protocol contracts; eliminates the need to change `common`.

---

### 3. Account Status Gating (`LOCKED` and `DELETED`)

- **Context**: A user may know valid credentials, but their account might be locked by an administrator or soft-deleted.
- **Decision**:
  - Before checking password, or immediately after fetching user:
    - If `user.accountStatus() == AccountStatus.LOCKED`: Immediately return `ErrorCode.ACCOUNT_LOCKED` with message `"Account is locked by an administrator"`.
    - If `user.accountStatus() == AccountStatus.DELETED`: Immediately return `ErrorCode.ACCOUNT_DELETED` with message `"Account has been deactivated"`.
  - When credentials do not match or user is not found: Return `ErrorCode.INVALID_CREDENTIALS` with message `"Invalid username or password"`. Never reveal whether the username exists.
- **Rationale**: Adheres to OWASP authentication security practices and protocol error code specifications.

---

### 4. Database User ID Alignment (`Long` vs `UUID`)

- **Context**: The `users.user_id` column in PostgreSQL is a `BIGSERIAL` (64-bit integer, mapped to `long`/`Long` in Java). `UserSummary` and `UserTarget` both use `long userId`. Earlier `SessionContext` had `UUID userId`.
- **Decision**: Update `SessionContext` to use `Long userId` (or `long`), ensuring type consistency with `UserRepository.findById(long)` and `LoginHistoryRepository.recordLogin(long)`.
- **Rationale**: Avoids unnecessary string conversions or type mismatches between database models, session context, and wire DTOs.

---

### 5. Client UI Threading & Swing Responsiveness

- **Context**: Socket I/O takes time (DNS, network latency, database query). Blocking the Swing Event Dispatch Thread (EDT) causes the GUI window to freeze or show "Not Responding".
- **Decision**:
  - In `AdminLoginDialog`, when the user clicks "Sign In":
    1. Immediately disable the "Sign In" button, username text field, and password field.
    2. Update status label to "Connecting to server...".
    3. Call `socketClient.sendAsync(...)` which executes asynchronously via `CompletableFuture`.
    4. Upon completion (success or error), use `SwingUtilities.invokeLater(...)` to transition the UI back on the EDT (either displaying the error label and re-enabling inputs, or disposing the dialog and opening `AdminMainFrame`).
- **Rationale**: Guarantees responsive desktop UX without UI thread blocking.
