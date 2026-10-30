# Database V1 Validation Quickstart

## Prerequisites

- Java 21 and Maven for repository regression checks.
- PowerShell 5.1 or later for static validation and initialization.
- Optional: PostgreSQL command-line tools on `PATH` and an explicitly isolated empty test database for runtime SQL validation.

Never use the runtime validation fixture against production, shared, or unknown data. It is intentionally destructive within its transaction and assumes the supplied database is isolated for this feature.

## 1. Static contract validation

From the repository root after Phase 12:

```powershell
& .\script\tests\validate-schema.ps1
```

Expected outcome: twelve required table definitions, required constraints/indexes, artifact consistency, and prohibited construct checks all pass.

## 2. Isolated PostgreSQL validation

Create or identify an empty database dedicated to this test, then load the schema and transactional fixture:

```powershell
$env:PGPASSWORD = Read-Host 'Isolated test database password'
psql -X --single-transaction -v ON_ERROR_STOP=1 -h localhost -p 5432 -U postgres -d chat_system_v1_test -f .\script\schema.sql
psql -X -v ON_ERROR_STOP=1 -h localhost -p 5432 -U postgres -d chat_system_v1_test -f .\script\tests\validate-schema.sql
Remove-Item Env:PGPASSWORD
```

Expected outcome: schema creation succeeds; validation notices show representative constraint rejections; the test transaction rolls back fixture rows.

## 3. Development initialization

After Phase 14, initialize a new development database and optionally load fictional data:

```powershell
& .\scripts\init-database.ps1 -DatabaseName chat_system_dev -Username postgres -LoadSeed
```

The script prompts through PostgreSQL tooling as configured by the local environment, does not accept or embed a password parameter, and refuses to overwrite an existing database unless the caller explicitly chooses schema application behavior documented in `docs/database/SETUP_V1.md`.

## 4. Repository regression checks

```powershell
mvn clean verify
mvn spotless:check
```

Expected outcome: the four-module Java build and formatting check remain successful. Database V1 does not change Java source.

## 5. Manual review

- Compare the twelve tables with [data-model.md](data-model.md) and [schema-contract.md](contracts/schema-contract.md).
- Compare Admin/User mappings with `docs/database/UI_DATA_MAPPING_V1.md`.
- Confirm schema table count and validation evidence in `docs/database/VALIDATION_V1.md`.
- Confirm no repositories, services, protocol actions, UI changes, or encryption-key management were introduced.
