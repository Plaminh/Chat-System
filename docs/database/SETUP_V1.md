# PostgreSQL Database V1 Setup

Database V1 uses `script/schema.sql` to load the six schema modules in dependency order; `script/seed.sql` contains optional fictional development data. Both scripts are independently fail-fast and transactional.

The seed's public-key, HPKE-envelope, and ciphertext byte patterns are structural fixtures only. They are not generated/decryptable cryptographic test vectors and do not verify E2EE behavior.

These scripts do not upgrade, repair, validate, reset, or reseed an existing database automatically.

## Docker Compose

Copy the safe environment example and set local credentials:

```powershell
Copy-Item .env.example .env
docker volume create chat-system-v1-data
docker compose up -d
docker compose logs db-init
```

The `postgres` service uses the external `chat-system-v1-data` volume. The one-shot `db-init` service behaves as follows:

- If `public` has no base tables, it runs `schema.sql` and then `seed.sql`.
- If `public` already contains any base table, it skips initialization without changing data.
- Skipping is not proof that the existing schema is complete or compatible. Older or partial development schemas require separate manual review.
- Schema or seed failure exits nonzero and identifies the failed step.

Compose never upgrades, repairs, recreates, or reseeds an existing database.

Use `docker compose stop` to stop services while retaining containers, or `docker compose down` to remove the Compose containers and network. The external database volume remains. Do not add `--volumes` when data must be preserved.

## PowerShell initialization

Requirements: PostgreSQL 14+ command-line tools (`psql`, `createdb`) and PowerShell 5.1+.

Create a new UTF-8 database with schema only:

```powershell
& .\scripts\init-database.ps1 `
    -DatabaseName chat_system_dev `
    -Username postgres
```

Create a new database and load development fixtures:

```powershell
& .\scripts\init-database.ps1 `
    -DatabaseName chat_system_dev `
    -Username postgres `
    -LoadSeed
```

The script accepts no password parameter. Authentication uses PostgreSQL's normal prompt, protected `pgpass`, or process environment behavior.

An existing database is refused by default. To apply the schema to an existing database that the developer has independently confirmed is empty:

```powershell
& .\scripts\init-database.ps1 `
    -DatabaseName chat_system_dev `
    -Username postgres `
    -UseExistingEmpty
```

`-UseExistingEmpty` counts non-system tables and refuses any nonempty target. The script never drops a database or overwrites an existing schema.

## Manual SQL

For a separately created and verified-empty database:

```powershell
psql -X -v ON_ERROR_STOP=1 `
    -h localhost -p 5432 -U postgres -d chat_system_dev `
    -f .\script\schema.sql
```

Load fictional development data only when every application table is empty:

```powershell
psql -X -v ON_ERROR_STOP=1 `
    -h localhost -p 5432 -U postgres -d chat_system_dev `
    -f .\script\seed.sql
```

The seed is intentionally non-idempotent. It does not ignore conflicts or delete existing data.

## Failure behavior

- Schema failure rolls back all schema objects created by that invocation.
- Seed failure rolls back all fixture inserts while leaving a successfully created schema in place.
- After correcting a seed failure, rerun `seed.sql` directly only after confirming every application table is still empty.
- Reapplying `schema.sql` to an existing schema fails at the first existing object; it is not an upgrade command.
- An old or incomplete development database must be handled separately. Recreating it, if desired, is an explicit developer operation and is never performed by these scripts.

## Validation

Static module/DBML consistency:

```powershell
& .\script\tests\validate-schema.ps1
```

Run `script/tests/validate-schema.sql` only against a disposable isolated database after loading `schema.sql`. The fixture performs data-changing tests inside a rollback transaction and must never target the normal application database.
