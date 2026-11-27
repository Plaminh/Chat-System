# Quickstart: [A2] Admin Login History Validation

**Feature**: `specs/006-admin-client-v2/admin-login-history` | **Date**: 2026-11-24

This guide details testing and verification procedures for Issue #8 [A2] Admin Login History.

---

## 1. Automated Test Execution

Run the Maven reactor automated test suite:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
& ".tools\apache-maven-3.9.9\bin\mvn.cmd" test
```

### Key Automated Scenarios Tested

1. **`AdminListLoginHistoryHandlerTest`**
   - Query global history (`userId = null`) returns `PageResult<LoginHistoryEntry>` sorted chronologically.
   - Query user-specific history (`userId = 2`) returns entries for that user.
   - Query user-specific history for non-existent user returns `ErrorCode.NOT_FOUND`.
   - Query with invalid sort field throws `InvalidSortFieldException` which results in `ErrorCode.VALIDATION_FAILED`.
   - Non-admin session calling handler is rejected with `ErrorCode.UNAUTHORIZED` by `ActionDispatcher`.

2. **`UserDetailHistoryDialogTest` / Client Integration**
   - Verify scope selection triggers `ADMIN_LIST_LOGIN_HISTORY` request.
   - Verify table model population on EDT with timestamps and usernames.

---

## 2. Interactive Verification (Swing UI)

### 2.1 Start Server
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
& ".tools\apache-maven-3.9.9\bin\mvn.cmd" -pl server spring-boot:run
```

### 2.2 Launch Admin Client
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4.1"
& ".tools\apache-maven-3.9.9\bin\mvn.cmd" -pl admin-client exec:java -Dexec.mainClass="com.chat.admin.AdminApplication"
```

### 2.3 Verification Steps
1. Log in with admin credentials (`admin` / `admin123`).
2. Go to **User Directory**.
3. Select any user row and click **"View History & Audit"**.
4. Observe the `UserDetailHistoryDialog`:
   - Under **Scope: Selected User**, verify that login records for this user are displayed.
   - Switch dropdown to **Scope: All System Users (Chronological)**.
   - Verify that all system login events appear ordered newest first.
