# Interface Contracts: [A0] Admin Authentication & Authorization

**Feature**: `specs/006-admin-client-v2/admin-auth` | **Date**: 2026-11-23

All communications use TCP stream with newline-delimited JSON (`\n`) adhering to `RequestEnvelope` and `ResponseEnvelope` specifications.

---

## 1. Action: `LOGIN`

Authenticates credentials and establishes session identity.

### 1.1 Request Envelope
- **Action**: `LOGIN`
- **Public**: Yes (permitted before authentication)
- **Payload**: `LoginRequest`

```json
{
  "requestId": "req-login-001",
  "action": "LOGIN",
  "payload": {
    "username": "admin",
    "password": "correct_password"
  }
}
```

### 1.2 Response Envelope (Success)
- **Success**: `true`
- **Payload**: `AuthenticationResult`

```json
{
  "requestId": "req-login-001",
  "success": true,
  "payload": {
    "user": {
      "userId": 1,
      "username": "admin",
      "fullName": "System Administrator",
      "accountStatus": "ACTIVE",
      "presence": "ONLINE"
    },
    "authenticatedAt": "2026-11-23T16:00:00+07:00"
  },
  "errorCode": null,
  "errorMessage": null
}
```

### 1.3 Response Envelope (Failures)

#### Case A: Invalid Credentials (Incorrect Password or User Not Found)
```json
{
  "requestId": "req-login-001",
  "success": false,
  "payload": null,
  "errorCode": "INVALID_CREDENTIALS",
  "errorMessage": "Invalid username or password"
}
```

#### Case B: Account Locked
```json
{
  "requestId": "req-login-001",
  "success": false,
  "payload": null,
  "errorCode": "ACCOUNT_LOCKED",
  "errorMessage": "Account is locked by an administrator"
}
```

#### Case C: Account Deactivated / Deleted
```json
{
  "requestId": "req-login-001",
  "success": false,
  "payload": null,
  "errorCode": "ACCOUNT_DELETED",
  "errorMessage": "Account has been deactivated"
}
```

---

## 2. Action: `ADMIN_GET_USER`

Retrieves detailed profile including role to verify administrative privileges.

### 2.1 Request Envelope
- **Action**: `ADMIN_GET_USER`
- **Public**: No (Requires active authentication)
- **Required Role**: `ADMIN`
- **Payload**: `UserTarget`

```json
{
  "requestId": "req-authcheck-002",
  "action": "ADMIN_GET_USER",
  "payload": {
    "targetUserId": 1
  }
}
```

### 2.2 Response Envelope (Success — Admin Role Verified)
- **Success**: `true`
- **Payload**: `AdminUserInfo`

```json
{
  "requestId": "req-authcheck-002",
  "success": true,
  "payload": {
    "account": {
      "summary": {
        "userId": 1,
        "username": "admin",
        "fullName": "System Administrator",
        "accountStatus": "ACTIVE",
        "presence": "ONLINE"
      },
      "email": "admin@campus.edu",
      "address": "Admin Office",
      "dateOfBirth": "1990-01-01",
      "gender": "MALE",
      "role": "ADMIN",
      "createdAt": "2026-01-01T08:00:00+07:00",
      "updatedAt": "2026-01-01T08:00:00+07:00"
    }
  },
  "errorCode": null,
  "errorMessage": null
}
```

### 2.3 Response Envelope (Rejection — Non-Admin User)
- **ErrorCode**: `UNAUTHORIZED`
- Triggered by `ActionDispatcher` when session role is `USER`.

```json
{
  "requestId": "req-authcheck-002",
  "success": false,
  "payload": null,
  "errorCode": "UNAUTHORIZED",
  "errorMessage": "Admin privileges required"
}
```

---

## 3. Action: `LOGOUT`

Terminates server session context, closes login audit row, and unbinds socket.

### 3.1 Request Envelope
- **Action**: `LOGOUT`
- **Public**: No (Requires active session)
- **Payload**: `null`

```json
{
  "requestId": "req-logout-003",
  "action": "LOGOUT",
  "payload": null
}
```

### 3.2 Response Envelope (Success)
- **Success**: `true`
- **Payload**: `null`

```json
{
  "requestId": "req-logout-003",
  "success": true,
  "payload": null,
  "errorCode": null,
  "errorMessage": null
}
```

---

## 4. Error Mapping Catalog (Client Display)

| ErrorCode | Server Error Message | Client User-Facing Display (Vietnamese) |
|---|---|---|
| `INVALID_CREDENTIALS` | "Invalid username or password" | "Tên đăng nhập hoặc mật khẩu không chính xác." |
| `ACCOUNT_LOCKED` | "Account is locked by an administrator" | "Tài khoản đã bị khóa bởi quản trị viên hệ thống." |
| `ACCOUNT_DELETED` | "Account has been deactivated" | "Tài khoản đã bị vô hiệu hóa hoặc xóa." |
| `UNAUTHORIZED` | "Admin privileges required" | "Tài khoản không có quyền quản trị." |
| `UNAUTHENTICATED` | "Authentication required" | "Phiên làm việc đã hết hạn hoặc chưa đăng nhập." |
| `INTERNAL_ERROR` | "An internal server error occurred" | "Đã xảy ra lỗi máy chủ nội bộ. Vui lòng thử lại sau." |
