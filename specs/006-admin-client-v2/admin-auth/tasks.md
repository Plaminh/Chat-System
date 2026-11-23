# Tasks: [A0] Admin Authentication & Authorization (Issue #7)

**Input**: Design documents from `specs/006-admin-client-v2/admin-auth/`
**Prerequisites**: [plan.md](file:///D:/III_HK1/JavaApp/Project/Chat-System/specs/006-admin-client-v2/admin-auth/plan.md), [spec.md](file:///D:/III_HK1/JavaApp/Project/Chat-System/specs/006-admin-client-v2/admin-auth/spec.md), [research.md](file:///D:/III_HK1/JavaApp/Project/Chat-System/specs/006-admin-client-v2/admin-auth/research.md), [data-model.md](file:///D:/III_HK1/JavaApp/Project/Chat-System/specs/006-admin-client-v2/admin-auth/data-model.md), [contracts/auth-contracts.md](file:///D:/III_HK1/JavaApp/Project/Chat-System/specs/006-admin-client-v2/admin-auth/contracts/auth-contracts.md), [quickstart.md](file:///D:/III_HK1/JavaApp/Project/Chat-System/specs/006-admin-client-v2/admin-auth/quickstart.md)

## Format: `[ID] [P?] [Story] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to ([US1], [US2], [US3])
- Clear file paths specified for each task

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Align session model and registry with 64-bit integer (`Long`) database user IDs and audit identifiers.

- [x] T001 Update `SessionContext` to use `Long userId` and track `Long loginHistoryId` in `server/src/main/java/com/chat/server/session/SessionContext.java`
- [x] T002 Update `OnlineSessionRegistry` to map sessions by `Long userId` instead of `UUID` in `server/src/main/java/com/chat/server/session/OnlineSessionRegistry.java`
- [x] T003 [P] Update unit tests to reflect `Long userId` in `server/src/test/java/com/chat/server/session/OnlineSessionRegistryTest.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure and dispatching rules required before implementing user stories.

- [x] T004 Verify and configure `ActionDispatcher` public action whitelist and admin action gating (`ADMIN_*` requires `UserRole.ADMIN`) in `server/src/main/java/com/chat/server/handler/ActionDispatcher.java`
- [x] T005 [P] Create test fixture skeleton and mock repository helper for authentication in `server/src/test/java/com/chat/server/handler/AuthHandlerTestSupport.java`

**Checkpoint**: Foundation ready — user story implementation can proceed.

---

## Phase 3: User Story 1 - Successful Admin Authentication Flow (Priority: P1) 🎯 MVP

**Goal**: An administrator enters valid credentials in `AdminLoginDialog`, server verifies password with BCrypt, establishes `SessionContext`, logs login in `login_history`, returns `AuthenticationResult`. Client queries `ADMIN_GET_USER`, validates `role == ADMIN`, closes dialog, and opens `AdminMainFrame`.

**Independent Test**: Enter `admin` credentials, verify socket `LOGIN` succeeds, `ADMIN_GET_USER` returns `AdminUserInfo`, dialog closes, and `AdminMainFrame` displays admin username.

### Tests for User Story 1
- [ ] T006 [P] [US1] Create unit test for `LoginHandler` covering success, wrong password, locked account, and deleted account in `server/src/test/java/com/chat/server/handler/LoginHandlerTest.java`
- [ ] T007 [P] [US1] Create unit test for `AdminGetUserHandler` covering valid admin retrieval and user-not-found in `server/src/test/java/com/chat/server/handler/AdminGetUserHandlerTest.java`

### Implementation for User Story 1
- [ ] T008 [US1] Implement `LoginHandler` for `ActionType.LOGIN` checking `UserRepository.findByUsername`, verifying with `BCrypt.checkpw`, logging via `LoginHistoryRepository.recordLogin`, and promoting `SessionContext` in `server/src/main/java/com/chat/server/handler/impl/LoginHandler.java`
- [ ] T009 [US1] Implement `AdminGetUserHandler` for `ActionType.ADMIN_GET_USER` querying user profile and returning `AdminUserInfo` in `server/src/main/java/com/chat/server/handler/impl/AdminGetUserHandler.java`
- [ ] T010 [US1] Implement two-step authentication and role verification flow via `SocketClient` in `admin-client/src/main/java/com/chat/admin/controller/AdminAuthController.java`
- [ ] T011 [US1] Wire `AdminLoginDialog` to call `AdminAuthController`, disable submit buttons during request transit, and transition to `AdminMainFrame` on success in `admin-client/src/main/java/com/chat/admin/view/AdminLoginDialog.java`

**Checkpoint**: User Story 1 (MVP) is fully functional and testable independently.

---

## Phase 4: User Story 2 - Non-Admin Role Rejection & Auto-Logout (Priority: P2)

**Goal**: Prevent regular users (`role = USER`) from accessing the Admin Console. When a non-admin logs in, `ADMIN_GET_USER` is rejected by `ActionDispatcher` with `UNAUTHORIZED`. Client sends `LOGOUT`, re-enables login inputs, and displays *"Tài khoản không có quyền quản trị"*.

**Independent Test**: Submit valid credentials for an account with `role = USER`. Verify `LOGIN` succeeds, `ADMIN_GET_USER` returns `UNAUTHORIZED`, client dispatches `LOGOUT`, and dialog displays error message without opening `AdminMainFrame`.

### Tests for User Story 2
- [ ] T012 [P] [US2] Create unit test verifying `ActionDispatcher` blocks `ADMIN_GET_USER` for non-admin sessions with `UNAUTHORIZED` in `server/src/test/java/com/chat/server/handler/ActionDispatcherAuthorizationTest.java`
- [ ] T013 [P] [US2] Create unit test verifying `AdminAuthController` executes auto-logout and returns non-admin error in `admin-client/src/test/java/com/chat/admin/controller/AdminAuthControllerTest.java`

### Implementation for User Story 2
- [ ] T014 [US2] Update `AdminAuthController` to dispatch `LOGOUT` and map `UNAUTHORIZED` error code to *"Tài khoản không có quyền quản trị"* in `admin-client/src/main/java/com/chat/admin/controller/AdminAuthController.java`
- [ ] T015 [US2] Update `AdminLoginDialog` to handle non-admin error rejection, re-enable input fields, and focus username field in `admin-client/src/main/java/com/chat/admin/view/AdminLoginDialog.java`

**Checkpoint**: User Story 2 security boundary is fully enforced.

---

## Phase 5: User Story 3 - Administrator Sign Out (Priority: P3)

**Goal**: Authenticated administrator clicks "Sign Out" in `AdminMainFrame`. Client dispatches `LOGOUT` over socket. Server `LogoutHandler` resets `SessionContext`, updates `logged_out_at` in `login_history`, unbinds from `OnlineSessionRegistry`. Client closes `AdminMainFrame` and re-opens `AdminLoginDialog`.

**Independent Test**: Click "Sign Out" in `AdminMainFrame`, verify `LOGOUT` is processed by server, frame disposes, and `AdminLoginDialog` reappears.

### Tests for User Story 3
- [ ] T016 [P] [US3] Create unit test for `LogoutHandler` verifying session clear, registry unbind, and logout history timestamp in `server/src/test/java/com/chat/server/handler/LogoutHandlerTest.java`

### Implementation for User Story 3
- [ ] T017 [US3] Implement `LogoutHandler` for `ActionType.LOGOUT` in `server/src/main/java/com/chat/server/handler/impl/LogoutHandler.java`
- [ ] T018 [US3] Wire `AdminMainFrame` sign out action to dispatch `LOGOUT` via `SocketClient` before closing window in `admin-client/src/main/java/com/chat/admin/view/AdminMainFrame.java`

**Checkpoint**: All three user stories are complete and independently verifiable.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Exception handling, styling conformity, and whole-project verification.

- [ ] T019 [P] Handle socket connection drop/timeout gracefully with localized error alert in `admin-client/src/main/java/com/chat/admin/view/AdminLoginDialog.java`
- [ ] T020 Run full multi-module build and automated test suite via Maven (`mvn test`)
- [ ] T021 Run `mvn spotless:check` to verify code format compliance across all touched files

---

## Dependencies & Execution Order

### Phase Dependencies
- **Setup (Phase 1)**: No dependencies — start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 — blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Phase 2. Delivers working MVP admin login.
- **User Story 2 (Phase 4)**: Depends on Phase 3 handlers and client controller.
- **User Story 3 (Phase 5)**: Depends on Phase 3 session lifecycle.
- **Polish (Phase 6)**: Depends on Phases 1–5.

### User Story Dependencies
- **US1 (P1)**: Independent after Foundational phase.
- **US2 (P2)**: Builds upon `LOGIN` in US1 to enforce authorization rejection.
- **US3 (P3)**: Builds upon `SessionContext` established in US1.

---

## Parallel Opportunities
- T003 [P] can run in parallel with T001/T002.
- T006 [P] and T007 [P] test cases can be authored in parallel.
- T012 [P] and T013 [P] can be written in parallel once US1 handlers are registered.
- T016 [P] can be written in parallel with Phase 4 tasks.

---

## Implementation Strategy (MVP First)

1. **Foundational & Setup (T001–T005)**: Set up `Long userId` session context and handler wiring.
2. **MVP Delivery (T006–T011)**: Implement `LoginHandler`, `AdminGetUserHandler`, and wire `AdminLoginDialog`. Test admin login to `AdminMainFrame`.
3. **Security Gate (T012–T015)**: Verify regular user rejection, automatic `LOGOUT`, and display *"Tài khoản không có quyền quản trị"*.
4. **Lifecycle Completion (T016–T018)**: Implement `LogoutHandler` and wire "Sign Out".
5. **Quality & Polish (T019–T021)**: Run full reactor test build (`mvn test`) and `mvn spotless:check`.
