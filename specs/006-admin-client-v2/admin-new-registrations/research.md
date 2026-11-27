# Research: [A5] Danh sách người dùng đăng ký mới (Issue #14)

**Feature**: `specs/006-admin-client-v2/admin-new-registrations` | **Date**: 2026-11-27

## Decision 1: Protocol Action & DTO Mapping

- **Decision**: Use existing action `ActionType.ADMIN_LIST_USERS` with payload `AdminUserFilter(query, accountStatus, createdAtRange, sort, page)`.
- **Rationale**: 
  - `ActionType.ADMIN_LIST_USERS` and `AdminUserFilter` already exist in `common`.
  - `AdminUserFilter` includes `DateRange createdAtRange`, `String query` (name/username), `SortSpec sort`, and `PageRequest page`.
  - This perfectly matches GitHub Issue #14: `"Action / DTO: ADMIN_LIST_USERS với createdAtRange / AdminUserFilter trả về PageResult<AdminUserInfo>"`.

## Decision 2: Backend Query & Timezone Handling

- **Decision**: Delegate directly to `UserRepository.findForAdmin(query, status, createdAtRange, sort, page)`.
- **Rationale**:
  - `UserRepository.findForAdmin` already implements:
    ```sql
    u.created_at >= :from AND u.created_at < :to
    ```
  - `SqlSupport.rangeStart` and `SqlSupport.rangeEndExclusive` automatically interpret dates in Vietnam timezone (Asia/Ho_Chi_Minh, UTC+7), ensuring inclusive date boundary semantics (e.g. `2026-11-01` to `2026-11-27` covers all timestamps up to `2026-11-28T00:00:00+07:00`).
  - Allowed sort fields: `UserRepository.ADMIN_SORT` supports `"name"` (`lower(u.full_name)`) and `"createdAt"` (`u.created_at`). Any other sort field throws `InvalidSortFieldException` which translates to `ErrorCode.VALIDATION_FAILED`.

## Decision 3: Client Data Binding & UI Updates

- **Decision**: Update `NewRegistrationPanel` to invoke `SocketClient.sendAsync(ActionType.ADMIN_LIST_USERS, filter)` asynchronously on a worker thread / `CompletableFuture`.
- **Rationale**:
  - `NewRegistrationPanel` already has the complete Swing layout (table, date fields, sort dropdown, search input, KPI summary cards).
  - Removing dependency on `AdminMockDataStore` and binding the response `PageResult<AdminUserInfo>` populates the table and updates the KPI summary cards with actual database results.
