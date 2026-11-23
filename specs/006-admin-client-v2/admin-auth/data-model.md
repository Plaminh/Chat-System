# Data Model: [A0] Admin Authentication & Authorization

**Feature**: `specs/006-admin-client-v2/admin-auth` | **Date**: 2026-11-23

## 1. Protocol Wire Entities (`common` Module)

All data transfer objects are immutable Java records defined in the `com.chat.common` module.

### 1.1 `LoginRequest` (`com.chat.common.dto.auth.LoginRequest`)
Sent by the client within `RequestEnvelope.payload` for `ActionType.LOGIN`.
- `username`: `String` (Required, non-blank, trimmed)
- `password`: `String` (Required, non-blank, plaintext on wire over TCP stream)

### 1.2 `AuthenticationResult` (`com.chat.common.dto.auth.AuthenticationResult`)
Returned by the server within `ResponseEnvelope.payload` upon successful `LOGIN`.
- `user`: `UserSummary` (Required)
  - `userId`: `long` (Database primary key, positive)
  - `username`: `String` (Required, non-blank)
  - `fullName`: `String` (Required, non-blank)
  - `accountStatus`: `AccountStatus` (`ACTIVE`, `LOCKED`, `DELETED`)
  - `presence`: `PresenceStatus` (`ONLINE`, `OFFLINE`, `BUSY`)
- `authenticatedAt`: `OffsetDateTime` (Timestamp when session was authenticated)

### 1.3 `UserTarget` (`com.chat.common.dto.social.UserTarget`)
Sent by the client within `RequestEnvelope.payload` for `ActionType.ADMIN_GET_USER`.
- `targetUserId`: `long` (User ID to query, positive)

### 1.4 `AdminUserInfo` (`com.chat.common.dto.admin.AdminUserInfo`)
Returned by the server within `ResponseEnvelope.payload` upon successful `ADMIN_GET_USER`.
- `account`: `UserAccountDetail` (Required)
  - `summary`: `UserSummary`
  - `email`: `String` (Case-insensitive email)
  - `address`: `String` (Physical/residence address)
  - `dateOfBirth`: `LocalDate` (Birth date)
  - `gender`: `Gender` (`MALE`, `FEMALE`, `OTHER`)
  - `role`: `UserRole` (`USER`, `ADMIN`) — **Essential field used to gate admin access**
  - `createdAt`: `OffsetDateTime`
  - `updatedAt`: `OffsetDateTime`

---

## 2. In-Memory Session Model (`server` Module)

### `SessionContext` (`com.chat.server.session.SessionContext`)
Stateful context attached to each persistent TCP client connection (`ClientConnection`).

| Field | Type | Description |
|---|---|---|
| `connectionId` | `String` | Unique connection identifier (`conn-UUID`) |
| `userId` | `Long` | Primary key of the authenticated user in PostgreSQL (`users.user_id`) |
| `username` | `String` | Username of the authenticated user |
| `role` | `UserRole` | Granted user role (`USER` or `ADMIN`) |
| `loginHistoryId` | `Long` | ID of the audit row created in `login_history` table |
| `authenticatedAt` | `Instant` | In-memory timestamp when authentication was promoted |

### Session Lifecycle State Transitions

```text
[ Connected ]
      │
      │  Receive LOGIN (Valid credentials)
      ▼
[ Authenticated: SessionContext(userId, role, loginHistoryId) ]
      │
      ├── ADMIN_GET_USER ──► If role == ADMIN ──► Allow & Return AdminUserInfo
      │                  ──► If role == USER  ──► Reject with UNAUTHORIZED
      │
      ├── Receive LOGOUT ──► Clear session & recordLogout(loginHistoryId)
      │                  ──► Unbind from OnlineSessionRegistry
      ▼
[ Unauthenticated / Disconnected ]
```

---

## 3. Database Persistence Models (`server` Module)

### 3.1 `users` Table
Read during authentication and admin profile inspection.

| Column | Type | Nullable | Notes |
|---|---|---|---|
| `user_id` | `BIGSERIAL` | NO | Primary key (`long`) |
| `username` | `VARCHAR(50)` | NO | Unique, case-insensitive index |
| `password_hash` | `VARCHAR(255)`| NO | BCrypt hash (`$2a$...`) |
| `email` | `VARCHAR(255)`| NO | Unique, case-insensitive index |
| `full_name` | `VARCHAR(100)`| NO | Full display name |
| `address` | `VARCHAR(255)`| NO | Address |
| `date_of_birth` | `DATE` | NO | Birthdate |
| `gender` | `VARCHAR(20)` | NO | `male`, `female`, `other` |
| `role` | `VARCHAR(20)` | NO | `user`, `admin` |
| `account_status`| `VARCHAR(20)` | NO | `active`, `locked`, `deleted` |
| `locked_at` | `TIMESTAMPTZ` | YES | Timestamp if locked |
| `deleted_at` | `TIMESTAMPTZ` | YES | Timestamp if soft-deleted |
| `created_at` | `TIMESTAMPTZ` | NO | Creation timestamp |
| `updated_at` | `TIMESTAMPTZ` | NO | Update timestamp |

### 3.2 `login_history` Table
Appended upon successful login; updated upon logout.

| Column | Type | Nullable | Notes |
|---|---|---|---|
| `login_history_id` | `BIGSERIAL` | NO | Primary key |
| `user_id` | `BIGINT` | NO | Foreign key references `users(user_id)` |
| `logged_in_at` | `TIMESTAMPTZ` | NO | `CURRENT_TIMESTAMP` |
| `logged_out_at`| `TIMESTAMPTZ` | YES | Set on `LOGOUT` or connection disconnect |
