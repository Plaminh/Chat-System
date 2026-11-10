# Tasks: Common V2 Foundation

**Input**: Design documents from `specs/004-common-v2/`
**Tests**: Required by FR-029 and the user request.
**Organization**: Exactly six dependency-ordered logical phases requested for this feature.

## Format: `[ID] [P?] [Story?] Description`

- `[P]` means different files with no incomplete same-file dependency.
- Story labels trace tasks to the independently testable scenarios in spec.md.

## Phase 1: Audit & API Contract

**Goal**: Establish the source-of-truth contract before changing Java APIs.

- [x] T001 Audit Git baseline, Spec Kit configuration, constitution, Maven modules, and existing Common protocol in `specs/004-common-v2/plan.md`
- [x] T002 Audit Server/Admin/User consumers and PING compatibility in `specs/004-common-v2/plan.md`
- [x] T003 Audit V1 database identities, lifecycles, statistics, and encryption policy in `specs/004-common-v2/research.md`
- [x] T004 [P] Map 12 User and 9 Admin functional groups in `specs/004-common-v2/contracts/protocol-contract.md`
- [x] T005 [P] Define action access/request/response/error/event/dependency contracts in `specs/004-common-v2/contracts/action-matrix.md`
- [x] T006 Complete spec, plan, wire model, quickstart, and requirement-quality checklists under `specs/004-common-v2/`

**Checkpoint**: Contract decisions are internally consistent and ready for enum implementation.

---

## Phase 2: Actions & Enums

**Goal**: Provide stable operation, error, event, and domain-state identifiers.

- [x] T007 [US1] Add account/social/admin operation identifiers to `common/src/main/java/com/chat/common/enums/ActionType.java`
- [x] T008 [US2] Add chat/group/encryption/report/writing operation identifiers to `common/src/main/java/com/chat/common/enums/ActionType.java`
- [x] T009 [P] [US5] Extend error taxonomy in `common/src/main/java/com/chat/common/enums/ErrorCode.java`
- [x] T010 [P] [US3] Extend realtime event identifiers in `common/src/main/java/com/chat/common/enums/MessageType.java`
- [x] T011 [P] Add typed domain enums under `common/src/main/java/com/chat/common/enums/`
- [x] T012 Add enum JSON regression coverage in `common/src/test/java/com/chat/common/enums/ProtocolEnumJsonTest.java`

**Checkpoint**: Identifiers serialize by stable names and retain V1 constants.

---

## Phase 3: Business DTOs

**Goal**: Deliver immutable shared wire payloads for account, social, chat, group, admin, statistics, and writing-assistance flows.

- [x] T013 [P] [US1] Implement shared paging/filter records in `common/src/main/java/com/chat/common/dto/common/`
- [x] T014 [P] [US1] Implement authentication records in `common/src/main/java/com/chat/common/dto/auth/`
- [x] T015 [P] [US1] Implement user/profile records in `common/src/main/java/com/chat/common/dto/user/`
- [x] T016 [P] [US1] Implement social records in `common/src/main/java/com/chat/common/dto/social/`
- [x] T017 [P] [US2] Implement conversation/message/search records in `common/src/main/java/com/chat/common/dto/chat/`
- [x] T018 [P] [US2] Implement group/member/role records in `common/src/main/java/com/chat/common/dto/group/`
- [x] T019 [P] [US1] Implement administration/moderation records in `common/src/main/java/com/chat/common/dto/admin/`
- [x] T020 [P] [US1] Implement required statistic records in `common/src/main/java/com/chat/common/dto/statistics/`
- [x] T021 [P] [US1] Implement writing-assistance records in `common/src/main/java/com/chat/common/dto/writing/`
- [x] T022 Add representative business DTO round-trip tests in `common/src/test/java/com/chat/common/dto/BusinessDtoJsonTest.java`

**Checkpoint**: Business payloads round-trip and expose no password hash or internal entity.

---

## Phase 4: Realtime & Encryption Contracts

**Goal**: Type unsolicited events and opaque client-side encrypted-group metadata.

- [ ] T023 [P] [US4] Implement public-key and encrypted-content records in `common/src/main/java/com/chat/common/dto/encryption/`
- [ ] T024 [P] [US4] Implement recipient-envelope and key-rotation records in `common/src/main/java/com/chat/common/dto/encryption/`
- [ ] T025 [P] [US3] Implement typed event payload records in `common/src/main/java/com/chat/common/dto/event/`
- [ ] T026 [US4] Add encryption invariant tests in `common/src/test/java/com/chat/common/dto/EncryptionDtoTest.java`
- [ ] T027 [US3] Add event envelope/payload round-trip tests in `common/src/test/java/com/chat/common/dto/EventEnvelopeJsonTest.java`

**Checkpoint**: Realtime and encryption payloads serialize and secret key material has no representation.

---

## Phase 5: Protocol & Validation

**Goal**: Enforce predictable framing/envelope behavior while preserving PING.

- [ ] T028 [P] [US5] Add contract validation utilities/exceptions in `common/src/main/java/com/chat/common/protocol/ProtocolValidator.java` and `common/src/main/java/com/chat/common/exception/ProtocolValidationException.java`
- [ ] T029 [P] [US5] Harden envelope invariants in `common/src/main/java/com/chat/common/dto/RequestEnvelope.java`, `ResponseEnvelope.java`, and `EventEnvelope.java`
- [ ] T030 [P] [US5] Add LF/CRLF/EOF/limit tests in `common/src/test/java/com/chat/common/protocol/JsonLineReaderTest.java`
- [ ] T031 [US5] Standardize malformed/unknown/unsupported errors in `server/src/main/java/com/chat/server/handler/RequestHandler.java`
- [ ] T032 [US5] Add server protocol regressions in `server/src/test/java/com/chat/server/handler/RequestHandlerTest.java`
- [ ] T033 Relocate legacy Swing theme out of Common into `admin-client/src/main/java/com/chat/admin/ui/ChatTheme.java` and `user-client/src/main/java/com/chat/user/ui/ChatTheme.java`, updating imports/tests in both clients

**Checkpoint**: Invalid input fails predictably, PING remains compatible, and Common contains no presentation code.

---

## Phase 6: Testing, Documentation & Handover

**Goal**: Verify integration, reconcile artifacts, and prepare Server/User/Admin handover.

- [ ] T034 Expand envelope/null/empty/payload serialization coverage in `common/src/test/java/com/chat/common/dto/`
- [ ] T035 Reconcile implemented actions/DTOs/examples with `specs/004-common-v2/contracts/` and `specs/004-common-v2/data-model.md`
- [ ] T036 Update integration/verification instructions in `specs/004-common-v2/quickstart.md`
- [ ] T037 Record meaningful AI work and actual verification evidence in `AI-LOG.md`
- [ ] T038 Run `.tools/apache-maven-3.9.9/bin/mvn.cmd -pl common -am test` and record actual result
- [ ] T039 Run `.tools/apache-maven-3.9.9/bin/mvn.cmd test` and `clean verify` and record actual results
- [ ] T040 Run `.tools/apache-maven-3.9.9/bin/mvn.cmd spotless:check`, `git diff --check`, dependency/boundary scans, and record actual results

**Checkpoint**: Artifacts, code, tests, and handover agree; all results are evidence-based.

## Dependencies & Execution Order

- Phase 1 blocks Phase 2.
- Phase 2 blocks DTO and event implementation.
- Phase 3 precedes Phase 4 because events reuse business DTOs.
- Phase 4 precedes Phase 5 validation integration.
- Phase 6 requires Phases 1–5.
- `[P]` tasks may be implemented concurrently only when they do not touch the same file.

## Independent Test Criteria

- **US1**: Account/social/admin payload families round-trip and expose only safe fields.
- **US2**: Chat/group payloads preserve content mode, deletion intent, membership, roles, search, and paging.
- **US3**: Events remain distinguishable from responses and route by event ID/type.
- **US4**: Encryption types enforce V1 algorithms/lengths and cannot carry private/plaintext group keys.
- **US5**: Malformed, unknown, missing, and over-limit input produce stable results while PING still succeeds.

## Implementation Strategy

Complete phases sequentially and commit each verified phase with the message specified by the user. Documentation evolves with code rather than claiming deferred Server/User/Admin runtime behavior.
