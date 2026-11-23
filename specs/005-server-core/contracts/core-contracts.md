# Core Protocol Contracts & Error Code Matrix

**Feature**: `specs/005-server-core` | **Date**: 2026-11-23

## 1. Request / Response Framing Contract

- Messages are transported as single-line JSON strings terminated by `\n` or `\r\n`.
- Maximum length of any line: `ProtocolConstants.MAX_MESSAGE_CHARS = 65536` (64 KB).
- When a client sends a payload exceeding 64 KB without a newline:
  - Server immediately writes:
    ```json
    {"requestId":null,"success":false,"data":null,"errorCode":"MESSAGE_TOO_LARGE","message":"Protocol message is too large"}
    ```
  - Server closes the TCP socket.

## 2. Core Actions & Authorization Matrix

| ActionType | Security Level | Required Role | Default Response if Unhandled | Handled in CORE |
| :--- | :--- | :--- | :--- | :--- |
| `PING` | Public | None | `{"message":"PONG"}` | Yes |
| `LOGIN` | Public | None | `UNSUPPORTED_OPERATION` | Deferred (Issue #4 / U5) |
| `ADMIN_LOGIN` | Public | None | `UNSUPPORTED_OPERATION` | Deferred (Issue #7 / A0) |
| `REGISTER` | Public | None | `UNSUPPORTED_OPERATION` | Deferred (Issue #5 / U1) |
| `RECOVER_PASSWORD` | Public | None | `UNSUPPORTED_OPERATION` | Deferred (Issue #12 / U3) |
| `RESET_PASSWORD` | Public | None | `UNSUPPORTED_OPERATION` | Deferred (Issue #11 / U4) |
| *All other User actions* | Protected | Authenticated (`USER` or `ADMIN`) | If unauthenticated: `UNAUTHENTICATED`<br/>If authenticated: `UNSUPPORTED_OPERATION` | Dispatcher Security |
| *All `ADMIN_*` actions* | Protected (Admin) | Authenticated (`ADMIN`) | If unauthenticated: `UNAUTHENTICATED`<br/>If role != ADMIN: `UNAUTHORIZED`<br/>If ADMIN: `UNSUPPORTED_OPERATION` | Dispatcher Security |

## 3. Server Error Code Mapping Contract

| Internal Cause | HTTP/REST Analogy | Protocol `ErrorCode` | Safe Client Message |
| :--- | :--- | :--- | :--- |
| Non-JSON string or malformed syntax | 400 Bad Request | `INVALID_JSON` | `"Malformed JSON protocol message"` |
| Missing `requestId` or missing `action` | 400 Bad Request | `INVALID_REQUEST` | `"requestId and action are required"` |
| Payload shape mismatch for DTO | 422 Unprocessable | `INVALID_PAYLOAD` | `"Payload failed validation: ..."` |
| Calling protected action without login | 401 Unauthorized | `UNAUTHENTICATED` | `"Authentication required"` |
| Non-admin calling `ADMIN_*` action | 403 Forbidden | `UNAUTHORIZED` | `"Admin privileges required"` |
| `ActionType` has no registered handler | 501 Not Implemented | `UNSUPPORTED_OPERATION` | `"Operation not implemented yet"` |
| `InvalidSortFieldException` thrown | 400 Bad Request | `VALIDATION_FAILED` | `"Invalid sort field: <fieldName>"` |
| `DuplicateKeyException` thrown | 409 Conflict | `ALREADY_EXISTS` or `CONFLICT` | Safe contextual message |
| Unhandled / Unexpected runtime error | 500 Internal Error | `INTERNAL_ERROR` | `"An internal server error occurred"` |

## 4. Push Event Contract

Events sent from server to client use `EventEnvelope`:
```json
{
  "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "type": "PRESENCE_CHANGED",
  "data": {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "status": "ONLINE",
    "lastSeen": "2026-11-23T06:18:00Z"
  }
}
```
Client reader routes lines containing `eventId` and `type` directly to the `EventListener`, bypassing `requestId` correlation.
