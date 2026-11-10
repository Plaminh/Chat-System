# Implementation Plan: Common V2 Foundation

**Branch**: `feature/common-v2` | **Date**: 2026-11-10 | **Spec**: [spec.md](spec.md)

## Summary

Extend the PING-only shared protocol into a typed Version 2 contract for all required User/Admin flows. Preserve NDJSON and envelope compatibility, add immutable wire DTOs and boundary validation, model realtime events and V1 client-side encrypted-group metadata, and verify serialization and four-module compilation. Common remains contract-only.

## Technical Context

**Language/Version**: Java 25 LTS
**Primary Dependencies**: Jackson Databind 2.18.3; Java standard library
**Storage**: None in Common; PostgreSQL V1 is read-only design input
**Testing**: JUnit 5.12.1, Maven Surefire 3.5.2
**Target Platform**: Windows desktop clients and non-web server exchanging UTF-8 NDJSON over TCP
**Project Type**: Four-module Maven desktop/client-server application
**Performance Goals**: Deterministic linear framing up to 65,536 characters
**Constraints**: Exactly four modules; no REST/WebSocket/JPA; no Swing/Spring/JDBC/business/crypto implementation in Common
**Scale/Scope**: 9 Admin and 12 User functional groups; shared contracts only

## Constitution Check

### Pre-design gate

- PASS: Scope is limited to assignment communication contracts.
- PASS: Java 25, Maven, TCP/NDJSON, Jackson, and PostgreSQL architecture are preserved.
- PASS: No module is added; dependency direction remains clients/server -> Common.
- PASS: Persistence and business logic remain Server responsibilities.
- PASS: Focused tests and full reactor verification are planned.
- PASS: Git work remains on `feature/common-v2`; AI work will be logged.
- REMEDIATION REQUIRED: Existing `common/ui/ChatTheme.java` is legacy Swing presentation code. Move equivalent helpers into each client and update imports before final acceptance; do not create a fifth module.

### Post-design gate

The design introduces only records, enums, protocol utilities, validation, exceptions, and tests in Common. Encryption types contain public/opaque material only. The planned ChatTheme relocation removes the known boundary violation without changing UI behavior. All gates pass after that task.

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

Client-local `ChatTheme` copies remain in `admin-client` and `user-client`; imports change only to enforce the boundary.

### Protocol design

- Preserve envelope JSON fields: `requestId/action/data`, `requestId/success/data/errorCode/message`, and `eventId/type/data`.
- Use `long` domain IDs and non-blank string request/event IDs.
- Use `OffsetDateTime` for instants and `LocalDate` for dates.
- Keep payload mapping explicit through Jackson; records validate local invariants.
- Use zero-based `PageRequest(page,size)` and non-null `PageResult<T>` lists.
- Keep PING compatible; distinguish unknown action, malformed JSON, invalid payload, and unsupported action.

### Validation strategy

- Record constructors validate context-free invariants.
- `ProtocolValidator` validates envelope shape, payload presence, IDs, date ranges, paging, byte lengths, and exclusive content.
- Existence, credentials, permissions, and database state remain Server validation.
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
5. Protocol & Validation
6. Testing, Documentation & Handover

## Dependency Impact

- Common keeps only Jackson plus JUnit test scope.
- Server receives only the RequestHandler adjustment required by new errors/validation.
- Clients receive ChatTheme relocation/import updates; networking integration remains deferred.
- No database schema or Maven-module changes.

## Architectural Trade-offs

- Records provide wire immutability without adding bean validation.
- Domain payloads avoid map ambiguity while shared query records prevent duplication.
- Byte arrays match database binary values and Jackson Base64; constructors defensively copy them.
- Event payloads reuse business DTOs when semantics match.

## Complexity Tracking

No constitution exception remains after the planned ChatTheme relocation.
