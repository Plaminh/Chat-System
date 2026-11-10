# Research: Common V2 Foundation

## R1 — Runtime version
- **Problem**: Input says Java 21 while governance/build say Java 25.
- **Selected**: Java 25; the constitution and root POM are authoritative.
- **Alternatives**: Java 21 or dual targets were rejected.
- **Consequences**: No preview features; Java 25 compatibility is required.

## R2 — Envelope compatibility
- **Problem**: V1 has stable envelope fields but only PING.
- **Selected**: Extend existing `JsonNode` envelopes without renaming fields.
- **Alternatives**: Generic or versioned replacement envelopes.
- **Consequences**: Payload mapping stays explicit and PING remains compatible.

## R3 — Identity and time
- **Selected**: Positive `long` domain IDs, string correlation IDs, `OffsetDateTime` instants, and `LocalDate` dates.
- **Reason**: Matches V1 BIGINT/TIMESTAMPTZ while keeping protocol correlation independent.
- **Alternatives**: String domain IDs or database-specific types.

## R4 — DTO granularity
- **Selected**: Domain records plus shared target/query types; action-specific requests only where fields differ.
- **Reason**: Strong contracts without one type per UI button.
- **Alternatives**: Untyped maps or one payload per action.

## R5 — Validation
- **Selected**: Compact record constructors plus `ProtocolValidator`.
- **Reason**: No new dependency and clear separation from business validation.
- **Alternatives**: Jakarta Bean Validation or constructors alone.

## R6 — Pagination
- **Selected**: Zero-based page, size 1–100, non-negative totals, immutable non-null items.
- **Reason**: Simple desktop pagination and later Spring JDBC mapping.
- **Alternatives**: Cursor or one-based paging.

## R7 — Message deletion
- **Selected**: Separate delete-for-me, global recall, and clear-history actions.
- **Reason**: Matches `message_deletions`, atomic unsend redaction, and `history_cleared_at`.
- **Alternatives**: One mode-bearing delete action or physical deletion.

## R8 — Encrypted groups
- **Selected**: Typed X25519 public-key, HPKE envelope, AES-GCM content, and epoch-distribution metadata only.
- **Reason**: Matches V1 and keeps keys/crypto operations out of Common.
- **Alternatives**: Server-managed keys or generic blobs.

## R9 — Encrypted search
- **Selected**: Server search for accessible plaintext; client-side search after authorized decryption for encrypted history.
- **Reason**: V1 prohibits plaintext derivatives and server decryption keys.
- **Alternatives**: Server decryption or speculative searchable encryption.

## R10 — Legacy Swing in Common
- **Selected**: Move equivalent ChatTheme helpers/tests into both existing clients.
- **Reason**: Satisfies module boundaries without a fifth module.
- **Alternatives**: Retain violation or add a UI module.
- **Consequences**: Small controlled duplication, unchanged UI behavior.

## R11 — Error taxonomy
- **Selected**: Stable protocol/domain categories only where clients need different behavior.
- **Reason**: Prevent backend-detail leakage while supporting predictable UI decisions.
- **Alternatives**: One generic code or HTTP status vocabulary.
