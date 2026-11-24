# Tasks: [A2] Admin Login History (Issue #8)

**Input**: Design documents from `specs/006-admin-client-v2/admin-login-history/`
**Prerequisites**: [plan.md](file:///D:/III_HK1\JavaApp\Project\Chat-System\specs\006-admin-client-v2\admin-login-history\plan.md), [spec.md](file:///D:/III_HK1\JavaApp\Project\Chat-System\specs\006-admin-client-v2\admin-login-history\spec.md), [research.md](file:///D:/III_HK1\JavaApp\Project\Chat-System\specs\006-admin-client-v2\admin-login-history\research.md), [data-model.md](file:///D:/III_HK1\JavaApp\Project\Chat-System\specs\006-admin-client-v2\admin-login-history\data-model.md), [contracts/login-history-contracts.md](file:///D:/III_HK1\JavaApp\Project\Chat-System\specs\006-admin-client-v2\admin-login-history\contracts\login-history-contracts.md), [quickstart.md](file:///D:/III_HK1\JavaApp\Project\Chat-System\specs\006-admin-client-v2\admin-login-history\quickstart.md)

## Format: `[ID] [P?] [Story] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to ([US1], [US2], [US3])
- Clear file paths specified for each task

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Test stubs and fixture setup for login history repository testing on Java 25.

- [x] T001 Verify `LoginHistoryRepository.find(Long userId, SortSpec sort, PageRequest page)` and repository mapping with `LoginHistoryRecord` in `server/src/main/java/com/chat/server/repository/LoginHistoryRepository.java`
- [x] T002 [P] Extend test fixture in `server/src/test/java/com/chat/server/handler/AuthHandlerTestSupport.java` with test helper for `LoginHistoryRepository.find(...)` returning stubbed `PageResult<LoginHistoryRecord>`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core dispatching and authorization verification for `ADMIN_LIST_LOGIN_HISTORY`.

- [x] T003 Verify `ActionDispatcher` correctly maps `ADMIN_LIST_LOGIN_HISTORY` to admin authorization gating in `server/src/main/java/com/chat/server/handler/ActionDispatcher.java` and rejects non-admin requests with `ErrorCode.UNAUTHORIZED`

**Checkpoint**: Foundation ready — user story implementation can proceed.

---

## Phase 3: User Story 1 - Global Chronological Login History (Priority: P1) 🎯 MVP

**Goal**: Administrator queries global login audit history (`userId = null`). Server retrieves chronological records via `LoginHistoryRepository.find(null, sort, page)` and returns `PageResult<LoginHistoryEntry>`. Admin Client displays records in `UserDetailHistoryDialog`.

**Independent Test**: Request `ADMIN_LIST_LOGIN_HISTORY` with `userId = null`. Verify server returns `PageResult<LoginHistoryEntry>` sorted by `loggedInAt DESC`. Verify `UserDetailHistoryDialog` renders login timestamps, usernames, and full names.

### Tests for User Story 1
- [ ] T004 [P] [US1] Create unit tests in `server/src/test/java/com/chat/server/handler/AdminListLoginHistoryHandlerTest.java` verifying `ADMIN_LIST_LOGIN_HISTORY` handles global query (`userId = null`) returning `PageResult<LoginHistoryEntry>` sorted chronologically

### Implementation for User Story 1
- [ ] T005 [US1] Implement `AdminListLoginHistoryHandler` for `ActionType.ADMIN_LIST_LOGIN_HISTORY` in `server/src/main/java/com/chat/server/handler/impl/AdminListLoginHistoryHandler.java` mapping `LoginHistoryRecord` to `LoginHistoryEntry`
- [ ] T006 [US1] Integrate `UserDetailHistoryDialog` in `admin-client/src/main/java/com/chat/admin/view/UserDetailHistoryDialog.java` to fetch real login history via `SocketClient` for global audit scope and render on EDT

**Checkpoint**: User Story 1 (MVP) is fully functional and testable independently.

---

## Phase 4: User Story 2 - User-Specific Login History Inspection (Priority: P2)

**Goal**: Administrator filters login history for a specific user (`userId != null`). Server checks user existence via `userRepository.findById(userId)` (returns `ErrorCode.NOT_FOUND` if missing), then queries `loginHistoryRepository.find(userId, sort, page)`. Admin Client supports scope switching.

**Independent Test**: Request `ADMIN_LIST_LOGIN_HISTORY` with a valid `userId`. Verify only entries for that user are returned. Request with a non-existent `userId`, verify server returns `ErrorCode.NOT_FOUND`.

### Tests for User Story 2
- [ ] T007 [P] [US2] Add unit test in `server/src/test/java/com/chat/server/handler/AdminListLoginHistoryHandlerTest.java` verifying user-specific query returns filtered entries and non-existent `userId` returns `ErrorCode.NOT_FOUND`

### Implementation for User Story 2
- [ ] T008 [US2] Update `AdminListLoginHistoryHandler` in `server/src/main/java/com/chat/server/handler/impl/AdminListLoginHistoryHandler.java` to check `userRepository.findById(userId)` when `userId != null` and return `ErrorCode.NOT_FOUND` if empty
- [ ] T009 [US2] Update `UserDetailHistoryDialog` in `admin-client/src/main/java/com/chat/admin/view/UserDetailHistoryDialog.java` to dynamically query with `userId` when scope is set to "Selected User"

**Checkpoint**: User Story 2 is fully implemented and verified.

---

## Phase 5: User Story 3 - Pagination & Sort Order Validation (Priority: P3)

**Goal**: System enforces sort whitelist (`loggedInAt`) and handles pagination navigation (`page`, `size`) and sort direction (`ASC`, `DESC`).

**Independent Test**: Request sort with `ASC` vs `DESC`. Verify records order. Request invalid sort field, verify `ErrorCode.VALIDATION_FAILED`.

### Tests for User Story 3
- [ ] T010 [P] [US3] Add unit test in `server/src/test/java/com/chat/server/handler/AdminListLoginHistoryHandlerTest.java` verifying sort directions and rejection of invalid sort fields with `ErrorCode.VALIDATION_FAILED`

### Implementation for User Story 3
- [ ] T011 [US3] Update `UserDetailHistoryDialog` in `admin-client/src/main/java/com/chat/admin/view/UserDetailHistoryDialog.java` to support pagination navigation and display total session counts

**Checkpoint**: All user stories are complete and functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verification, error handling, code formatting, and regression testing across modules.

- [ ] T012 [P] Verify graceful error presentation for connection errors or empty results in `admin-client/src/main/java/com/chat/admin/view/UserDetailHistoryDialog.java`
- [ ] T013 Run full multi-module build and automated test suite via Maven (`mvn test`)
- [ ] T014 Run `mvn spotless:check` to verify code format compliance across all touched files

---

## Dependencies & Execution Order

- **Phase 1 (Setup)**: Prepares test helpers.
- **Phase 2 (Foundational)**: Confirms authorization and routing.
- **Phase 3 (User Story 1 - P1 MVP)**: Global login history handler + tests + client integration.
- **Phase 4 (User Story 2 - P2)**: User-specific filtering + not-found handling.
- **Phase 5 (User Story 3 - P3)**: Pagination and sort directions.
- **Phase 6 (Polish)**: Final verification and checks.
