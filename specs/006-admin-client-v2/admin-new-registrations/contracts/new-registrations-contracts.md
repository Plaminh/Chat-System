# Interface Contracts: [A5] Danh sách người dùng đăng ký mới (Issue #14)

**Feature**: `specs/006-admin-client-v2/admin-new-registrations` | **Date**: 2026-11-27

Communication uses TCP socket stream with newline-delimited JSON (`\n`) adhering to `RequestEnvelope` and `ResponseEnvelope`.

---

## 1. Action: `ADMIN_LIST_USERS` (with `createdAtRange`)

Retrieves paginated user accounts created within a specific date range, optionally filtered by name search, and sorted by name or creation time.

### 1.1 Request Envelope
- **Action**: `ADMIN_LIST_USERS`
- **Requires Admin Role**: Yes (`UserRole.ADMIN`)
- **Payload**: `AdminUserFilter`

```json
{
  "requestId": "req-reg-001",
  "action": "ADMIN_LIST_USERS",
  "data": {
    "query": null,
    "accountStatus": null,
    "createdAtRange": {
      "from": "2026-11-01",
      "to": "2026-11-27"
    },
    "sort": {
      "field": "createdAt",
      "direction": "DESC"
    },
    "page": {
      "page": 0,
      "size": 50
    }
  }
}
```

### 1.2 Response Envelope (Success)
- **Success**: `true`
- **Payload**: `PageResult<AdminUserInfo>`

```json
{
  "requestId": "req-reg-001",
  "success": true,
  "data": {
    "items": [
      {
        "account": {
          "summary": {
            "userId": 1001,
            "username": "alice",
            "fullName": "Alice Smith",
            "status": "ACTIVE",
            "presence": "OFFLINE"
          },
          "email": "alice@example.com",
          "address": "123 Campus Way",
          "dateOfBirth": "2000-01-15",
          "gender": "FEMALE",
          "role": "USER",
          "createdAt": "2026-11-20T10:15:30Z",
          "updatedAt": "2026-11-20T10:15:30Z"
        }
      }
    ],
    "page": 0,
    "size": 50,
    "totalItems": 1,
    "totalPages": 1
  },
  "errorCode": null,
  "errorMessage": null
}
```

### 1.3 Response Envelope (Validation Failure)
When `from` is after `to` or sort field is invalid:
```json
{
  "requestId": "req-reg-002",
  "success": false,
  "data": null,
  "errorCode": "VALIDATION_FAILED",
  "errorMessage": "Invalid date range: start date must be before or equal to end date"
}
```

### 1.4 Response Envelope (Unauthorized Non-Admin)
```json
{
  "requestId": "req-reg-003",
  "success": false,
  "data": null,
  "errorCode": "UNAUTHORIZED",
  "errorMessage": "Admin privileges required"
}
```
