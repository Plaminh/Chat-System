# Data Model: [A5] Danh sách người dùng đăng ký mới (Issue #14)

**Feature**: `specs/006-admin-client-v2/admin-new-registrations` | **Date**: 2026-11-27

## 1. Wire Entities & DTOs

### 1.1 `AdminUserFilter`
```java
public record AdminUserFilter(
    String query,
    AccountStatus accountStatus,
    DateRange createdAtRange,
    SortSpec sort,
    PageRequest page)
```
- `query`: Optional string (filters full name or username case-insensitively).
- `accountStatus`: Optional `AccountStatus` (null = all statuses).
- `createdAtRange`: Optional `DateRange(LocalDate from, LocalDate to)`.
- `sort`: Required `SortSpec(String field, SortDirection direction)`. Allowed fields: `"name"`, `"createdAt"`.
- `page`: Required `PageRequest(int page, int size)`. Default page size: 50.

### 1.2 `AdminUserInfo`
```java
public record AdminUserInfo(UserAccountDetail account)
```
Where `UserAccountDetail`:
- `summary`: `UserSummary(userId, username, fullName, status, presence)`
- `email`: `String`
- `address`: `String`
- `dateOfBirth`: `LocalDate`
- `gender`: `Gender`
- `role`: `UserRole`
- `createdAt`: `OffsetDateTime`
- `updatedAt`: `OffsetDateTime`

## 2. Database Mapping

Table: `users`
- `user_id` -> `summary.userId`
- `username` -> `summary.username`
- `full_name` -> `summary.fullName`
- `account_status` -> `summary.status`
- `email` -> `email`
- `address` -> `address`
- `date_of_birth` -> `dateOfBirth`
- `gender` -> `gender`
- `role` -> `role`
- `created_at` -> `createdAt`
- `updated_at` -> `updatedAt`
Live presence resolved via `OnlineSessionRegistry.isUserOnline(userId)`.
