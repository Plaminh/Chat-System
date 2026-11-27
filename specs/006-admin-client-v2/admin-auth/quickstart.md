# Quickstart: [A0] Admin Authentication & Authorization Validation

**Feature**: `specs/006-admin-client-v2/admin-auth` | **Date**: 2026-11-23

This guide provides step-by-step procedures to run and validate the admin authentication workflow end-to-end.

---

## 1. Prerequisites

- Java 25 LTS installed (`C:\Program Files\Java\jdk-25.0.4.1`)
- Apache Maven 3.9.9 available at `.tools\apache-maven-3.9.9\bin\mvn.cmd`
- Clean working directory on branch `feature/admin-client-v2`

---

## 2. Automated Test Execution

Execute automated verification tests covering the authentication lifecycle:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
& ".tools\apache-maven-3.9.9\bin\mvn.cmd" test
```

### Key Automated Scenarios Tested

1. **`LoginHandlerTest`**
   - Valid credentials return `AuthenticationResult`.
   - Wrong password returns `ErrorCode.INVALID_CREDENTIALS`.
   - Non-existent username returns `ErrorCode.INVALID_CREDENTIALS`.
   - Account status `LOCKED` returns `ErrorCode.ACCOUNT_LOCKED`.
   - Account status `DELETED` returns `ErrorCode.ACCOUNT_DELETED`.
   - Login audit record inserted into `login_history`.

2. **`AdminGetUserHandlerTest`**
   - Admin caller querying valid user returns `AdminUserInfo`.
   - Target user not found returns `ErrorCode.NOT_FOUND`.

3. **`ActionDispatcherAuthorizationTest`**
   - Calling `ADMIN_GET_USER` with `UserRole.USER` session returns `ErrorCode.UNAUTHORIZED`.
   - Calling `ADMIN_GET_USER` unauthenticated returns `ErrorCode.UNAUTHENTICATED`.
   - Calling `ADMIN_GET_USER` with `UserRole.ADMIN` session succeeds and routes to handler.

4. **`LogoutHandlerTest`**
   - `LOGOUT` cleans up `SessionContext` (`session.isAuthenticated() == false`).
   - `OnlineSessionRegistry` removes connection from user bindings.
   - `login_history` row marked with `logged_out_at`.

5. **`AdminAuthControllerTest`**
   - Admin user login and role verification completes with success.
   - Non-admin user login triggers automatic logout and returns `"Tài khoản không có quyền quản trị"`.
   - Socket connection failure gracefully caught and displays readable error message.

---

## 3. Manual Interactive Verification (Swing UI)

### 3.1 Start Server
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
& ".tools\apache-maven-3.9.9\bin\mvn.cmd" -pl server spring-boot:run
```

### 3.2 Launch Admin Desktop Client
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
& ".tools\apache-maven-3.9.9\bin\mvn.cmd" -pl admin-client exec:java -Dexec.mainClass="com.chat.admin.AdminApplication"
```

### 3.3 Verification Checklist
- [ ] **Valid Admin Login**: Enter `admin` / `admin123`. Verify `AdminLoginDialog` closes and `AdminMainFrame` opens with header displaying `System Admin (@admin)`.
- [ ] **Non-Admin Login**: Enter regular user credentials (`user1`). Verify dialog remains open, inputs re-enable, and alert says *"Tài khoản không có quyền quản trị"*.
- [ ] **Wrong Password**: Enter `admin` / `wrongpass`. Verify error *"Tên đăng nhập hoặc mật khẩu không chính xác"*.
- [ ] **Sign Out**: In `AdminMainFrame`, click "Sign Out". Confirm prompt. Verify window closes and `AdminLoginDialog` reappears.
