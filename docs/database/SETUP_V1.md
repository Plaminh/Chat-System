# PostgreSQL Database V1 Setup

## Prerequisites

- Docker Desktop or Docker Engine with Docker Compose for the container workflow.
- PostgreSQL 14 or later with `psql` and `createdb` available on `PATH` for database initialization.
- PowerShell 5.1 or later.
- A PostgreSQL role allowed to connect to the maintenance database and create the target database, or access to an explicitly approved empty target database.

The script has no password parameter and contains no password. Authentication is delegated to PostgreSQL's standard mechanisms: interactive prompt, a locally protected `pgpass` file, or a process environment configured by the developer. Never commit local credentials.

## Docker Compose configuration

Copy the safe example and replace the password placeholder locally:

```powershell
Copy-Item .env.example .env
```

`POSTGRES_USER`, `POSTGRES_PASSWORD`, and `POSTGRES_DB` configure PostgreSQL when a new empty volume is initialized. `POSTGRES_PORT` selects the loopback host port; the default is `55432`. Docker Compose reads these values from `.env`, which is ignored by Git.

The `postgres` service uses the external volume `chat-system-v1-data`. The one-shot `db-init` service mounts the complete `script/` directory read-only, runs the modular `script/schema.sql` entry point followed by `script/seed.sql`, waits for PostgreSQL to become healthy, and checks the number of public tables. When the database has zero tables, it applies schema and seed together in one transaction. When all 12 expected tables already exist, it skips initialization. Any partial or unexpected table count causes a failure instead of modifying the database.

Create the volume once on a new workstation, then start PostgreSQL and inspect the initialization result:

```powershell
docker volume create chat-system-v1-data
docker compose up -d
docker compose ps -a
docker compose logs db-init
```

Successful initialization ends with `Database initialization check passed: 12 tables are available.` The `db-init` container then exits with code 0; PostgreSQL continues running. In DBeaver, refresh the connection and open **Databases → chat_system_v1 → Schemas → public → Tables**.

The Compose workflow loads the fictional development seed automatically only when the database contains zero tables. It never reapplies seed data to an initialized database. Use the standalone initializer described below for non-Compose or schema-only workflows.

## Adopt a legacy Docker volume

If a workstation still has a `chat-system-v1-db` container created with `docker run`, Compose cannot create another container with that fixed name, even if the legacy container is stopped. The legacy container must therefore be stopped and renamed (or removed) manually before its volume can be adopted. This causes downtime from the stop until the Compose-managed container is healthy.

Before the change, set `.env` to the existing database name, username, port, and initialized password. PostgreSQL ignores `POSTGRES_*` initialization values when the data directory is already populated, so changing `POSTGRES_PASSWORD` does not change an existing role's password. The local value must match the password already initialized in the volume for clients that use it.

Take and verify a backup outside the repository before the maintenance window. Protect the dump because it contains database data:

```powershell
New-Item -ItemType Directory -Force ..\chat-system-backups | Out-Null
$backupName = 'chat_system_v1-before-compose-{0}.dump' -f (Get-Date -Format 'yyyyMMdd-HHmmss')
$backupFile = Join-Path '..\chat-system-backups' $backupName
docker exec chat-system-v1-db pg_dump `
    -U chat_v1 -d chat_system_v1 --format=custom `
    --file=/tmp/chat_system_v1-before-compose.dump
docker cp chat-system-v1-db:/tmp/chat_system_v1-before-compose.dump $backupFile
pg_restore --list $backupFile | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Backup verification failed; do not continue.' }
```

After the backup is verified, run the following commands manually during a maintenance window:

```powershell
docker stop chat-system-v1-db
docker rename chat-system-v1-db chat-system-v1-db-legacy
docker compose up -d
docker compose ps
docker compose exec postgres pg_isready -U chat_v1 -d chat_system_v1
```

Keep the renamed legacy container stopped until the Compose-managed service and application connections are verified. Never start both containers against the same PostgreSQL data volume. If rollback is required:

```powershell
docker compose down
docker rename chat-system-v1-db-legacy chat-system-v1-db
docker start chat-system-v1-db
```

`docker compose down` removes the Compose-managed container and network, but the external volume remains. Remove the stopped legacy container only after the adoption and backup are verified.

## Connect with DBeaver

Create a PostgreSQL connection with host `127.0.0.1`, port `55432` (or `POSTGRES_PORT`), database `chat_system_v1`, and user `chat_v1`. Enter the local password from the developer's secret store or `.env`; do not save or publish it in repository documentation. Test the connection before applying any SQL.

## Safe shutdown

Use `docker compose stop` to stop PostgreSQL while retaining its container, or `docker compose down` to remove only the Compose container and network. Because `chat-system-v1-data` is external, neither command deletes the database volume. Do not add `--volumes` or manually delete the named volume when data must be preserved.

## One-step initialization with fictional data

From the repository root:

```powershell
& .\scripts\init-database.ps1 `
    -HostName localhost `
    -Port 5432 `
    -DatabaseName chat_system_dev `
    -Username postgres `
    -LoadSeed
```

This creates a new UTF-8 database from `template0`, then applies `script/schema.sql` and development-only `script/seed.sql` in one PostgreSQL transaction. If either SQL file fails, PostgreSQL rolls back both schema and seed changes. Database creation itself is not transactional; after a SQL failure the newly created empty database is intentionally left in place for inspection.

## Schema-only initialization

```powershell
& .\scripts\init-database.ps1 `
    -DatabaseName chat_system_dev `
    -Username postgres
```

This creates a new database and applies only `script/schema.sql` in one transaction.

`script/seed.sql` is development-only. It uses fictional profiles, reserved `example.test` email addresses, and BCrypt fixture hashes without a published plaintext password.

## Load seed data into an initialized development database

The initializer deliberately refuses a database that already contains tables, so do not rerun it to add sample data. To load the seed into an already initialized development database that has not previously received this fixture, run:

```powershell
psql -X --single-transaction -v ON_ERROR_STOP=1 `
    -h localhost -p 5432 -U postgres -d chat_system_dev `
    -f .\script\seed.sql
```

The seed load is atomic: any error rolls back all fixture rows. It is not idempotent, so loading it again into a database that already contains the fixture will fail rather than silently overwrite data.

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

For a database that was created separately and verified empty, apply schema only:

```powershell
psql -X --single-transaction -v ON_ERROR_STOP=1 `
    -h localhost -p 5432 -U postgres -d chat_system_dev `
    -f .\script\schema.sql
```

To apply schema and seed together atomically to a verified empty database:

```powershell
psql -X --single-transaction -v ON_ERROR_STOP=1 `
    -h localhost -p 5432 -U postgres -d chat_system_dev `
    -f .\script\schema.sql -f .\script\seed.sql
```

## Failure behavior

- Missing `psql`/`createdb`, connection errors, authentication errors, database-creation failures, schema failures, and seed failures produce a nonzero terminating error with the PostgreSQL output preserved.
- Schema-only application is one transaction. With `-LoadSeed`, schema and seed share one transaction; an SQL failure leaves no partial schema or partial fixture data.
- Database creation occurs before that transaction. If a newly created database later encounters an SQL error, the empty database is intentionally left in place for inspection. The script does not drop it automatically.
- Reapplying `schema.sql` to a populated V1 database is not a migration. The DDL intentionally fails rather than silently replacing objects.
- Repair, upgrade, backup, restore, and production deployment procedures require a separately reviewed operational plan.

## Verification

Static contract validation:

```powershell
& .\script\tests\validate-schema.ps1
```

For isolated runtime validation, follow `specs/003-database-schema-v1/quickstart.md`. Never run the destructive fixture against production, shared, or unknown data.
