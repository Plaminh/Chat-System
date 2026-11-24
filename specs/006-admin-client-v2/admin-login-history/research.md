# Research: [A2] Admin Login History (Issue #8)

**Feature**: `specs/006-admin-client-v2/admin-login-history` | **Date**: 2026-11-24

## Research Questions & Architectural Decisions

### 1. Database Query & Entity Mapping

- **Context**: The server needs to return chronological login records for either all users (global) or a specific user. The database table `login_history` stores `login_history_id`, `user_id`, `logged_in_at`, and `logged_out_at`.
- **Existing Repository**: `LoginHistoryRepository.java` already implements:
  ```java
  public PageResult<LoginHistoryRecord> find(Long userId, SortSpec sort, PageRequest page)
  ```
  which performs a SQL query joining `login_history h` with `users u` on `u.user_id = h.user_id`, selecting `login_history_id`, `user_id`, `username`, `full_name`, `logged_in_at`, and `logged_out_at`.
- **Decision**: `AdminListLoginHistoryHandler` delegates data retrieval directly to `loginHistoryRepository.find(request.userId(), request.sort(), request.page())`, and maps each `LoginHistoryRecord` to wire DTO `LoginHistoryEntry`.
- **Rationale**: Reuses well-tested Spring JDBC repository code and eliminates redundant queries or custom SQL writing.

---

### 2. User Existence Verification for Filtered Queries

- **Context**: `LoginHistoryRequest` allows `userId` to be nullable (`Long userId`).
  - When `userId == null`: Indicates a global system-wide login audit query.
  - When `userId != null`: Filters the login history specifically for that user.
- **Decision**:
  - If `request.userId() != null`: First query `userRepository.findById(request.userId())`.
    - If user does not exist, immediately return `ErrorCode.NOT_FOUND` with message `"User not found"`.
  - If `request.userId() == null`: Skip user lookup and proceed directly to `loginHistoryRepository.find(...)`.
- **Rationale**: Complies with `action-matrix.md` specifications which designate `NOT_FOUND` as an expected error code for `ADMIN_LIST_LOGIN_HISTORY` when querying an unknown user.

---

### 3. Sort Field Whitelist & Direction Validation

- **Context**: In SQL queries, dynamically constructed `ORDER BY` clauses pose SQL injection risks if not strictly validated against a whitelist.
- **Existing Mechanism**: `LoginHistoryRepository.SORT` defines:
  ```java
  static final Map<String, String> SORT = Map.of("loggedInAt", "h.logged_in_at");
  ```
  `SqlSupport.orderBy(...)` throws `InvalidSortFieldException` if `sort.field()` is not `"loggedInAt"`.
  `ActionDispatcher` catches `InvalidSortFieldException` and returns `ErrorCode.VALIDATION_FAILED` to the client.
- **Decision**: In `AdminListLoginHistoryHandler`, rely on `loginHistoryRepository.find(...)` and `ActionDispatcher`'s centralized exception translation to enforce the `"loggedInAt"` sort field requirement.
- **Rationale**: Keeps handler logic concise while preventing SQL injection vulnerabilities.

---

### 4. Admin Authorization Gating

- **Context**: Only users with `UserRole.ADMIN` should be permitted to inspect user or system login histories.
- **Decision**: Rely on `ActionDispatcher`'s centralized authorization check:
  ```java
  if (action.name().startsWith("ADMIN_") && !session.hasRole(UserRole.ADMIN)) {
      return error(requestId, ErrorCode.UNAUTHORIZED, "Admin privileges required");
  }
  ```
  `AdminListLoginHistoryHandler` can safely assume the session is authenticated as an administrator.
- **Rationale**: Centralized authorization prevents code duplication and eliminates the risk of missing security checks in individual handlers.

---

### 5. Client Integration in `UserDetailHistoryDialog`

- **Context**: Currently, `UserDetailHistoryDialog` uses `AdminMockDataStore.getInstance()`. It has two scopes in `loginScopeCombo`:
  - `Scope: Selected User (@...)`
  - `Scope: All System Users (Chronological)`
- **Decision**:
  - Inject or provide `SocketClient` to `UserDetailHistoryDialog`.
  - When `populateLoginTable()` is called:
    - Determine `userId`: `allUsers ? null : user.getId()`.
    - Construct `LoginHistoryRequest`:
      `new LoginHistoryRequest(userId, new SortSpec("loggedInAt", SortDirection.DESC), new PageRequest(0, 50))`
    - Dispatch request asynchronously using `socketClient.sendAsync(...)`.
    - On response, populate `loginTableModel` on the Swing Event Dispatch Thread (EDT) via `SwingUtilities.invokeLater(...)`.
- **Rationale**: Preserves the existing UI design and layout while switching the data source from mock storage to the real server TCP socket connection.
