# Database V1 Validation Results

**Date**: 2026-10-30  
**Schema**: `script/schema.sql`  
**PostgreSQL runtime**: PostgreSQL 18.4 disposable local cluster

## Result summary

| Gate | Result | Evidence |
|---|---|---|
| Required base-table count/names | PASS | Exactly 12 approved tables in SQL and DBML |
| Primary keys | PASS | 12 primary keys |
| Foreign keys/delete behavior | PASS | 25 explicit foreign keys; invalid reference, cascade, set-null, and restrict cases executed |
| Check constraints | PASS | 34 checks; representative invalid states rejected |
| Unique constraints/indexes | PASS | Duplicate usernames, pending pairs, and message deletions rejected; 33 explicit indexes |
| Schema syntax/execution | PASS | All DDL loaded with `ON_ERROR_STOP=1` in an empty isolated PostgreSQL 18.4 database |
| Transactional validation fixture | PASS | Valid rows inserted for all 12 tables; invalid cases rejected; fixture rolled back |
| Unsafe or out-of-scope constructs | PASS | No schema triggers, functions, procedures, calculated counter columns, or plaintext password column |
| Temporary environment cleanup | PASS | Test database dropped, disposable server stopped, verified cluster directory removed |

## Commands executed

Static validation:

```powershell
& .\script\tests\validate-schema.ps1
```

Actual output:

```text
Schema validation passed.
Tables: 12
Primary keys: 12
Foreign keys: 25
Check constraints: 34
Explicit indexes: 33
```

Runtime validation used a newly initialized trust-only cluster under the ignored `.tools/db-v1-validation` directory, bound only to `127.0.0.1:55439`. The workflow created a new `chat_system_v1_test` database, loaded `script/schema.sql`, executed `script/tests/validate-schema.sql`, dropped the database, stopped the server, verified it was not accepting connections, and removed the cluster directory. The normal local endpoint at `localhost:5432` was not running and was not modified.

Runtime completion output ended with:

```text
Database V1 runtime schema validation passed.
```

## Runtime cases proven

### Valid integrated fixture

The transactional fixture inserted representative users, login history, friend requests, friendship, block, direct/group conversations, direct pair, memberships, messages, per-user deletion, spam report, and all three activity types. All inserts succeeded in foreign-key dependency order.

### Constraint rejection cases

- Case-insensitive duplicate username.
- Self friend request.
- Reverse duplicate pending friend request.
- Non-canonical friendship pair.
- Self block.
- Direct conversation with group-only metadata.
- Membership ending before joining.
- Blank message body.
- Recall actor without recall time.
- Duplicate per-user message deletion.
- Resolved report without resolution time.
- App-open event with conversation context.
- Login history referencing a nonexistent user.
- Deletion of a user who still owns a group.

### Referential delete cases

- Deleting a temporary user cascaded its login history.
- The same deletion set retained message authorship and report reporter references to null.
- Deleting a direct conversation cascaded its specialization, memberships, messages, message deletions, and activity rows.
- A spam report survived the message cascade with its optional message evidence set to null.

## Normalization review

- **First normal form**: Columns are scalar; no participant, message, role, or metric arrays are stored.
- **Second/third normal form**: Profile facts exist only in `users`; login/report/message rows reference users rather than duplicating names or account state; conversation-wide facts are separated from membership and direct-pair facts.
- **Relationship integrity**: Unordered relationships use canonical pairs; directed relationships retain explicit endpoint roles.
- **Derived values**: Friend counts, second-degree counts, registration totals, app-open totals, distinct chat partners/groups, and live presence are not stored on `users`.
- **Lifecycle separation**: Global recall, delete-for-me, and clear-history are independent; report workflow is independent from account lock state.

## Index review

The 33 explicit indexes cover case-insensitive account identity/search, account/registration filters, login chronology, requests, both relationship directions, conversation filters/ownership, membership rosters/history, message chronology/sender/full-text search, deletion lookup, moderation filters, and activity range/distinct aggregation. Primary-key and unique-constraint indexes are additional PostgreSQL-managed indexes.

## Remaining limitations

- The runtime fixture proves PostgreSQL 18.4 behavior. The schema targets PostgreSQL 14+ native features, but separate runs on each older supported major were not performed.
- Cross-table rules listed in `data-model.md` cannot be proven by same-row SQL constraints and remain mandatory Version 2 transaction tests.
- Performance was not benchmarked with production-scale data; indexes were reviewed structurally for the university-project workload.
- Seed-data consistency and initialization-script behavior are validated in Phases 13 and 14.

## Phase 13 seed validation

`script/seed.sql` was loaded after the schema in a fresh disposable PostgreSQL 18.4 database with `ON_ERROR_STOP=1`. All foreign keys and checks passed. The resulting row counts were:

| Table | Rows |
|---|---:|
| `users` | 6 |
| `login_history` | 5 |
| `friend_requests` | 4 |
| `friendships` | 3 |
| `user_blocks` | 1 |
| `conversations` | 4 |
| `direct_conversations` | 2 |
| `conversation_members` | 10 |
| `messages` | 7 |
| `message_deletions` | 2 |
| `spam_reports` | 2 |
| `user_activity_logs` | 10 |

Identity sequences were advanced past the explicit fixture IDs and a next-value query passed for both users and messages. The data uses fictional names/addresses, reserved `example.test` email addresses, and BCrypt fixture hashes without a published plaintext password. The disposable database/cluster cleanup passed again.

## Phase 14 initialization validation

`scripts/init-database.ps1` passed PowerShell parsing and a scan for embedded password assignment. It was then executed against a fresh disposable PostgreSQL 18.4 cluster:

1. New database plus `-LoadSeed`: PASS — created a UTF-8 database, applied 12 tables, and loaded 56 rows.
2. Existing nonempty database without opt-in: PASS — refused before changes; table count remained 12.
3. Existing empty database with `-UseExistingEmpty`: PASS — safety count was zero and 12 tables were applied.
4. Existing nonempty database with `-UseExistingEmpty`: PASS — refused after detecting 12 user tables.
5. Cleanup: PASS — both test databases were dropped, the disposable server stopped, and the verified cluster directory removed.

No existing developer database, service, credential store, or normal `localhost:5432` cluster was modified.
