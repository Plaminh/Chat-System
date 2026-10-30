# PostgreSQL Database V1 Setup

## Prerequisites

- PostgreSQL 14 or later with `psql` and `createdb` available on `PATH`.
- PowerShell 5.1 or later.
- A PostgreSQL role allowed to connect to the maintenance database and create the target database, or access to an explicitly approved empty target database.

The script has no password parameter and contains no password. Authentication is delegated to PostgreSQL's standard mechanisms: interactive prompt, a locally protected `pgpass` file, or a process environment configured by the developer. Never commit local credentials.

## Create a new development database

From the repository root:

```powershell
& .\scripts\init-database.ps1 `
    -HostName localhost `
    -Port 5432 `
    -DatabaseName chat_system_dev `
    -Username postgres
```

This creates a UTF-8 database from `template0` and applies `script/schema.sql` with stop-on-error behavior.

## Create and load fictional data

```powershell
& .\scripts\init-database.ps1 `
    -DatabaseName chat_system_dev `
    -Username postgres `
    -LoadSeed
```

`script/seed.sql` is development-only. It uses fictional profiles, reserved `example.test` email addresses, and BCrypt fixture hashes without a published plaintext password.

## Existing-database safety

By default, an existing database causes the script to stop before making changes:

```text
Database 'chat_system_dev' already exists. No changes were made.
```

The script never drops a database. If the intended development database already exists and is known to be empty, explicitly opt in:

```powershell
& .\scripts\init-database.ps1 `
    -DatabaseName chat_system_dev `
    -Username postgres `
    -UseExistingEmpty
```

Before applying the schema, the script counts all non-system tables. Any user table causes refusal; it does not merge, overwrite, or migrate an existing schema.

## Non-default server and maintenance database

```powershell
& .\scripts\init-database.ps1 `
    -HostName db-dev.internal `
    -Port 55432 `
    -DatabaseName chat_system_dev `
    -MaintenanceDatabase postgres `
    -Username chat_db_admin
```

Database and maintenance names accept only letters, digits, and underscores and must start with a letter. The port must be between 1 and 65535.

## Manual SQL alternative

For a database that was created separately and verified empty:

```powershell
psql -X -v ON_ERROR_STOP=1 -h localhost -p 5432 -U postgres -d chat_system_dev -f .\script\schema.sql
psql -X -v ON_ERROR_STOP=1 -h localhost -p 5432 -U postgres -d chat_system_dev -f .\script\seed.sql
```

Omit the second command when sample data is not wanted.

## Failure behavior

- Missing `psql`/`createdb`, connection errors, authentication errors, database-creation failures, schema failures, and seed failures produce a nonzero terminating error with the PostgreSQL output preserved.
- If a newly created database later encounters a schema error, it is intentionally left in place for inspection. The script does not drop it automatically.
- Reapplying `schema.sql` to a populated V1 database is not a migration. The DDL intentionally fails rather than silently replacing objects.
- Repair, upgrade, backup, restore, and production deployment procedures require a separately reviewed operational plan.

## Verification

Static contract validation:

```powershell
& .\script\tests\validate-schema.ps1
```

For isolated runtime validation, follow `specs/003-database-schema-v1/quickstart.md`. Never run the destructive fixture against production, shared, or unknown data.
