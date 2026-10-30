# Database V1 Implementation Plan

## Objective

Deliver the complete PostgreSQL Version 1 design and setup package for the Java Chat System without implementing Version 2 repositories, services, networking behavior, or UI integration.

## Inputs inspected

- Project constitution, `AGENTS.md`, Maven module structure, current SQL placeholders, README, and `AI-LOG.md`.
- Admin UI models and requirements from `origin/feature/admin-ui-v1`, inspected through Git without switching branches.
- User UI models and requirements from `origin/feature/user-ui-v1`, inspected through Git without switching branches.
- Existing Spec Kit templates, scripts, and repository naming conventions.

## Scope

Exactly twelve core tables are delivered:

1. `users`
2. `login_history`
3. `friend_requests`
4. `friendships`
5. `user_blocks`
6. `conversations`
7. `direct_conversations`
8. `conversation_members`
9. `messages`
10. `message_deletions`
11. `spam_reports`
12. `user_activity_logs`

## Phase and dependency roadmap

| Phase | Deliverable | Depends on | Validation gate |
|---|---|---|---|
| 00 | Spec Kit artifacts and this plan | Clean target branch | Artifact completeness; no SQL implementation |
| 01 | ERD, DBML, data model, UI mappings | 00 | Twelve-entity relationship review |
| 02 | `users` | 01 | User constraints and indexes |
| 03 | `login_history` | 02 | User foreign key and temporal check |
| 04 | `friend_requests` | 02 | Directed uniqueness/state checks |
| 05 | `friendships`, `user_blocks` | 02, 04 | Canonical/directed duplicate prevention |
| 06 | `conversations` | 02 | Conversation-kind/group metadata checks |
| 07 | `direct_conversations`, `conversation_members` | 05, 06 | Pair uniqueness and membership lifecycle |
| 08 | `messages` | 07 | Sender/conversation/unsend constraints |
| 09 | `message_deletions` | 08 | Per-user deletion uniqueness |
| 10 | `spam_reports` | 08 | Report workflow and optional evidence |
| 11 | `user_activity_logs` | 07 | Event/context consistency and analytics indexes |
| 12 | Static and isolated runtime validation | 02–11 | Twelve-table contract assertions |
| 13 | Fictional development seed data | 12 | Referential and constraint consistency |
| 14 | Safe initialization helper | 12, 13 | Parameter/error/existing-database behavior |
| 15 | Final documentation and review | All | Build, formatting, schema, artifact, Git review |

## Design assumptions

- Numeric database IDs are authoritative; prefixed IDs in UI mock data are display mappings.
- Friend counts and activity metrics are derived, not stored on users.
- UI gender labels map to lowercase normalized values or `NULL`.
- Message recall, per-user deletion, and conversation history clearing are separate lifecycle facts.
- PostgreSQL database creation supplies UTF-8 encoding; scripts contain Unicode-safe text.
- Cross-table authorization and coordination rules are documented for Version 2 backend transactions instead of implemented with triggers.
- E2EE metadata is a forward-compatible indicator only; key management and ciphertext design are deferred.

## Commit policy

Each completed phase produces one Conventional Commit with only phase-related files, the corresponding `tasks.md` progress update, and an `AI-LOG.md` entry. Existing completed phases would be skipped rather than duplicated. No push, merge, rebase, or branch switch is performed.

## Validation strategy

- Review each staged diff and staged file list before committing.
- Run schema-specific static checks after every table phase.
- Run PostgreSQL validation only against an explicitly isolated database when the required tools and safe connection details are available.
- Run `mvn clean verify` and `mvn spotless:check` at final review because no Java changes are expected but repository health must remain intact.
- Record unavailable tools and incomplete runtime validation honestly.

## Explicit exclusions

JDBC repositories, domain services, live authentication, TCP protocol additions, realtime message delivery, desktop UI changes, email/LLM integration, presence services, encryption keys, triggers, stored procedures, migrations frameworks, and additional Maven modules are not part of Database V1.
