# Quickstart: [A5] Danh sách người dùng đăng ký mới (Issue #14)

**Feature**: `specs/006-admin-client-v2/admin-new-registrations` | **Date**: 2026-11-27

## Verification & Execution

### 1. Run Automated Unit Tests
```powershell
& "D:\III_HK1\JavaApp\Config\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" test -Dtest=AdminListUsersHandlerTest
```

### 2. Run Full Reactor Build
```powershell
& "D:\III_HK1\JavaApp\Config\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" clean verify
```

### 3. Spotless Format Check
```powershell
& "D:\III_HK1\JavaApp\Config\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" spotless:check
```
