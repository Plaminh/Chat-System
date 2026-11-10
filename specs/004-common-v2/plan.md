# Implementation Plan: Common V2 Foundation

**Branch**: `feature/common-v2` | **Date**: 2026-11-10 | **Spec**: [spec.md](spec.md)

## Summary

Extend the PING-only shared protocol into a typed Version 2 contract for all required User/Admin flows. Preserve NDJSON and envelope compatibility, add immutable wire DTOs and Common-only boundary validation, model realtime events and V1 client-side encrypted-group metadata, and verify serialization and dependent-module compilation without modifying Server, Admin, or User source. Common remains contract-only apart from the approved legacy theme exception.

## Technical Context

**Language/Version**: Java 25 LTS
**Primary Dependencies**: Jackson Databind 2.18.3; Java standard library
**Storage**: None in Common; PostgreSQL V1 is read-only design input
**Testing**: JUnit 5.12.1, Maven Surefire 3.5.2
**Target Platform**: Windows desktop clients and non-web server exchanging UTF-8 NDJSON over TCP
**Project Type**: Four-module Maven desktop/client-server application
**Performance Goals**: Deterministic linear framing up to 65,536 characters
**Constraints**: Exactly four modules; preserve the existing shared `common.ui.ChatTheme` unchanged; V2 DTO/protocol/validation code must not depend on Swing; no REST/WebSocket/JPA; no Spring/JDBC/business/crypto implementation in Common
**Scale/Scope**: 9 Admin and 12 User functional groups; shared contracts only

## Constitution Check

### Pre-design gate

- PASS: Scope is limited to assignment communication contracts.
- PASS: Java 25, Maven, TCP/NDJSON, Jackson, and PostgreSQL architecture are preserved.
- PASS: No module is added; dependency direction remains clients/server -> Common.
- PASS: Persistence and business logic remain Server responsibilities.
- PASS: Focused tests and full reactor verification are planned.
- PASS: Git work remains on `feature/common-v2`; AI work will be logged.
- APPROVED EXCEPTION: Preserve the existing `common/ui/ChatTheme.java`, its test, client imports, FlatLaf setup, and Look and Feel behavior exactly. Do not move, duplicate, refactor, or expose it to Server code.
- GOVERNANCE REVIEW REQUIRED: Constitution line 39 says, “Keep shared protocol definitions in `common` without Swing, Spring, or database dependencies.” Common V2 satisfies the protocol-package boundary, but the wording can be read as a module-wide Swing prohibition.

### Post-design gate

The V2 design introduces only records, enums, protocol utilities, validation, exceptions, and tests in Common. Encryption types contain public/opaque material only. The pre-existing `common.ui.ChatTheme` remains byte-identical as the sole approved UI exception; no V2 contract type imports Swing.

### Proposed constitution amendment for review

Do not modify the constitution as part of this feature. Proposed narrow replacement for line 39:

> Keep shared DTO and protocol definitions in `common` independent of Swing, Spring, and database APIs. The existing `common.ui.ChatTheme` is the sole approved Swing exception and must remain presentation-only, unchanged, unused by Server code, and must not authorize additional UI infrastructure in Common.

## Existing Architecture Audit

- Existing envelopes use Jackson `JsonNode`; retain their field names.
- Preserve `ActionType.PING`, existing error codes, and `MessageType.SERVER_EVENT`.
- `JsonLineReader` already strips CR, terminates on LF, returns final unterminated content, and enforces the character limit; tests are incomplete.
- Server PING handling maps malformed input but conflates unknown actions and unsupported operations.
- Client socket classes are placeholders and do not consume shared envelopes yet.
- Database V1 uses positive `BIGINT` IDs, `TIMESTAMPTZ`, distinct deletion semantics, per-user X25519 public keys, HPKE envelopes, and AES-GCM message metadata.

## Design

### Package organization

```text
common/src/main/java/com/chat/common/
├── dto/                         # existing envelopes
│   ├── auth/                    # credentials and authentication results
│   ├── user/                    # profiles and safe account views
│   ├── social/                  # friends, requests, blocks, presence
│   ├── chat/                    # conversations, messages, history, search
│   ├── group/                   # groups, membership, roles
│   ├── admin/                   # moderation and administration views
│   ├── statistics/              # assignment statistics
│   ├── encryption/              # public/opaque crypto metadata
│   ├── writing/                 # writing-assistance contracts
│   └── common/                  # paging, sort, date range, filters
├── enums/                       # actions, events, errors, domain states
├── protocol/                    # framing constants/reader and validation
└── exception/                   # contract validation exceptions
```

The existing `common/ui/ChatTheme.java` remains at its V1 path. Both clients retain their existing `com.chat.common.ui.ChatTheme` imports. No client-local theme implementation is created, and Server must not import `com.chat.common.ui`.

### Protocol design

- Preserve envelope JSON fields: `requestId/action/data`, `requestId/success/data/errorCode/message`, and `eventId/type/data`.
- Use `long` domain IDs and non-blank string request/event IDs.
- Use `OffsetDateTime` for instants and `LocalDate` for dates.
- Keep payload mapping explicit through Jackson; records validate local invariants.
- Use zero-based `PageRequest(page,size)` and non-null `PageResult<T>` lists.
- Keep PING compatible; distinguish unknown action, malformed JSON, invalid payload, and unsupported action.

### Validation strategy

- Record constructors validate context-free invariants.
- `ProtocolValidator` validates envelope shape, payload presence, IDs, date ranges, paging, byte lengths, and exclusive content inside Common.
- Existence, credentials, permissions, and database state remain Server validation.
- Mapping Common validation outcomes to Server responses and adding Server handler regressions are deferred to `feature/server-v2` as former T031 and T032.
- Messages name invalid fields but never include sensitive values or internal exceptions.

### Testing strategy

- Preserve PING regression tests.
- Round-trip every envelope and representative domain DTO.
- Verify enum JSON values and unknown action behavior.
- Test LF, CRLF, EOF, empty line, exact limit, and over-limit framing.
- Verify encryption invariants and absence of secret-bearing response fields.
- Run Common tests, root tests, clean verify, and Spotless check; report blockers exactly.

## Implementation Phases

1. Audit & API Contract
2. Actions & Enums
3. Business DTOs
4. Realtime & Encryption Contracts
5. Common Protocol & Validation
6. Testing, Documentation & Handover

## Dependency Impact

- Common keeps only Jackson plus JUnit test scope.
- Server, Admin, and User source receive no Common V2 changes.
- Server request-handler error mapping and its regression tests are deferred to `feature/server-v2` with traceability to former T031 and T032.
- No database schema or Maven-module changes.

## Architectural Trade-offs

- Records provide wire immutability without adding bean validation.
- Domain payloads avoid map ambiguity while shared query records prevent duplication.
- Byte arrays match database binary values and Jackson Base64; constructors defensively copy them.
- Event payloads reuse business DTOs when semantics match.

## Complexity Tracking

The existing shared theme is a deliberately narrow compatibility exception. Its scope is limited to `common.ui.ChatTheme` and its existing test; DTO/protocol packages remain UI-independent, Server remains UI-independent, no fifth module is introduced, and the proposed constitution wording awaits human review.
