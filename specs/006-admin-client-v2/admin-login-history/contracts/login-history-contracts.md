# Interface Contracts: [A2] Admin Login History (Issue #8)

**Feature**: `specs/006-admin-client-v2/admin-login-history` | **Date**: 2026-11-24

Communication uses TCP socket stream with newline-delimited JSON (`\n`) adhering to `RequestEnvelope` and `ResponseEnvelope`.

---

## 1. Action: `ADMIN_LIST_LOGIN_HISTORY`

Retrieves paginated login audit records chronologically, either globally or filtered by user.

### 1.1 Request Envelope (Global History)
- **Action**: `ADMIN_LIST_LOGIN_HISTORY`
- **Requires Admin Role**: Yes (`UserRole.ADMIN`)
- **Payload**: `LoginHistoryRequest`

```json
{
  "requestId": "req-login-hist-001",
  "action": "ADMIN_LIST_LOGIN_HISTORY",
  "payload": {
    "userId": null,
    "sort": {
      "field": "loggedInAt",
      "direction": "DESC"
    },
    "page": {
      "page": 0,
      "size": 20
    }
  }
}
```

### 1.2 Request Envelope (User-Specific History)
```json
{
  "requestId": "req-login-hist-002",
  "action": "ADMIN_LIST_LOGIN_HISTORY",
  "payload": {
    "userId": 2,
    "sort": {
      "field": "loggedInAt",
      "direction": "DESC"
    },
    "page": {
      "page": 0,
      "size": 20
    }
  }
}
```

### 1.3 Response Envelope (Success)
- **Success**: `true`
- **Payload**: `PageResult<LoginHistoryEntry>`

```json
{
  "requestId": "req-login-hist-001",
  "success": true,
  "payload": {
    "items": [
      {
        "loginHistoryId": 105,
        "userId": 2,
        "username": "tiennv",
        "fullName": "Nguyen Van Tien",
        "loggedInAt": "2026-11-24T22:30:00+07:00",
        "loggedOutAt": "2026-11-24T23:05:00+07:00"
      },
      {
        "loginHistoryId": 104,
        "userId": 1,
        "username": "admin",
        "fullName": "System Administrator",
        "loggedInAt": "2026-11-24T20:15:00+07:00",
        "loggedOutAt": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalItems": 2,
    "totalPages": 1
  },
  "errorCode": null,
  "errorMessage": null
}
```

### 1.4 Response Envelope (Failures)

#### Case A: User Not Found (When `userId` is provided)
```json
{
  "requestId": "req-login-hist-002",
  "success": false,
  "payload": null,
  "errorCode": "NOT_FOUND",
  "errorMessage": "User not found"
}
```

#### Case B: Invalid Sort Field
```json
{
  "requestId": "req-login-hist-003",
  "success": false,
  "payload": null,
  "errorCode": "VALIDATION_FAILED",
  "errorMessage": "Unknown sort field 'username'; allowed: [loggedInAt]"
}
```

#### Case C: Unauthorized (Non-admin or unauthenticated connection)
```json
{
  "requestId": "req-login-hist-004",
  "success": false,
  "payload": null,
  "errorCode": "UNAUTHORIZED",
  "errorMessage": "Admin privileges required"
}
```
