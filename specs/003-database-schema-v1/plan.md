# Implementation Plan: Database Schema Version 1

**Branch**: `feature/database-schema-v1` | **Date**: 2026-10-30 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-database-schema-v1/spec.md`

## Summary

Deliver a normalized PostgreSQL Version 1 data foundation for the Java Chat System as fifteen required tables: the original twelve plus per-user public keys, encrypted-group key epochs, and recipient envelopes. Implementation is limited to the modular schema, seed, safe initialization, validation fixtures, and design/setup documentation. JDBC repositories, cryptographic services, protocol changes, and UI integration remain Version 2 work.

## Technical Context

**Language/Version**: PostgreSQL SQL (compatible with PostgreSQL 14+) and PowerShell 5.1+; existing Java 25 project remains unchanged

**Primary Dependencies**: PostgreSQL command-line tools (`psql`, `createdb`) for runtime setup; GitHub Spec Kit 1.2.2 for artifacts

**Storage**: PostgreSQL, UTF-8 database encoding, normalized relational schema

**Testing**: Focused static PowerShell assertions for entry-point/artifact synchronization; PostgreSQL transactional SQL tests against an isolated PostgreSQL 16 database

**Target Platform**: PostgreSQL server reachable from Windows; initialization helper runs in Windows PowerShell

**Project Type**: Multi-module desktop client/server application with database deliverables at repository root

**Performance Goals**: Index all required foreign-key lookup, chronological history, account search, message search, moderation filter, and activity aggregation paths for the expected university-project workload

**Constraints**: Fifteen justified tables; no triggers, stored procedures, migration framework, live repositories, UI/protocol changes, embedded credentials, destructive existing-database behavior, plaintext/private key storage, or claim that SQL implements/proves E2EE

**Scale/Scope**: Fifteen tables, one modular schema entry point, one development seed script, focused static/runtime validation, one initialization script, and the associated ERD/DBML/setup/review documentation

## Constitution Check

*GATE: Passed before research and re-checked against the planned design.*

| Principle | Planned compliance |
|---|---|
| Project scope and delivery | Version 1 contains only database scripts/design/docs; Version 2 application behavior is explicitly deferred. |
| Technology constraints | PostgreSQL is preserved; TCP/desktop architecture is untouched. |
| Module boundaries | No Maven module or Java dependency changes are planned. |
| Architecture responsibilities | Only future server-side code may access this schema; clients receive no database access. |
| Code quality | Constraints and names are explicit; speculative tables, triggers, procedures, and abstractions are excluded. |
| Testing | Focused static checks are mandatory; isolated PostgreSQL runtime tests are used only when safe and available. |
| Development workflow | Work stays on `feature/database-schema-v1`; one authorized atomic commit is created per completed roadmap phase; no push or merge occurs. |
| AI transparency | Every phase updates `AI-LOG.md` with actual validation and limitations. |

**Pre-design gate result**: PASS — no constitutional exception is required.

**Post-design gate result**: PASS — the twelve-entity design and deferred transaction rules preserve scope and architecture boundaries.

## Post-Review Remediation Scope

Preserve the original entity names while replacing server-controlled key wrapping with `user_public_keys`, metadata-only `conversation_key_versions`, and recipient-specific `conversation_key_envelopes`. Compose initializes only an empty `public` schema and otherwise skips without compatibility claims; PowerShell keeps its existing-database refusal and explicit `-UseExistingEmpty` override. No Java, protocol, UI, module, or database-credential change is in scope.

## Project Structure

### Documentation (this feature)

```text
specs/003-database-schema-v1/
├── spec.md
├── plan.md
├── tasks.md
├── research.md
├── data-model.md
├── quickstart.md
├── checklists/requirements.md
└── contracts/schema-contract.md
```

### Source and delivery files (repository root)

```text
script/
├── schema.sql
├── schema/
├── seed.sql
└── tests/
    ├── validate-schema.ps1
    └── validate-schema.sql

scripts/
└── init-database.ps1

docs/database/
├── IMPLEMENTATION_PLAN_V1.md
├── ERD_V1.md
├── schema-v1.dbml
├── UI_DATA_MAPPING_V1.md
├── SETUP_V1.md
├── VALIDATION_V1.md
└── COMPLETION_REPORT_V1.md

AI-LOG.md
```

**Structure Decision**: Preserve the existing four Maven modules without Java changes. Database assets remain in the established singular `script/` directory; operational helpers remain in `scripts/`; review documentation remains in `docs/database/`.

## Complexity Tracking

No constitution violations or additional complexity exceptions are required.
