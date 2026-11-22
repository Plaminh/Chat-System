# Java Desktop Chat System

A clean foundation for a university Java final project. Two Java Swing desktop clients communicate with one non-web Spring Boot backend through newline-delimited JSON over TCP. PostgreSQL access is isolated in the server. The current scope is infrastructure and PING/PONG connectivity—not a completed chat product.

## Technology stack

- Java 25 LTS, Maven, JUnit 5
- Swing and FlatLaf; JFreeChart is available to the admin client
- Spring Boot in non-web mode, Java `ServerSocket`, Spring JDBC
- PostgreSQL JDBC, Jackson, BCrypt

No REST, WebSocket, JPA/Hibernate, JavaFX, or client-side database access is used.

## Modules and dependencies

```text
admin-client ─┐
user-client  ─┼──> common
server       ─┘
```

- `common`: envelopes, protocol enums, framing utilities, and limits
- `admin-client`: executable Admin Swing application
- `user-client`: executable User Swing application
- `server`: executable non-web Spring Boot TCP server and JDBC configuration

The clients never depend on `server`, and `common` contains no UI, database, or Spring code.

## Layout

```text
chat-system/
├── common/
├── admin-client/
├── user-client/
├── server/
├── config/             # tracked examples
├── script/             # database placeholders
├── scripts/package.ps1
└── docs/
```

Source code follows the standard `src/main/java`, `src/main/resources`, and `src/test/java` Maven layout. Reserved packages contain only documentation until their features are specified.

## Prerequisites and build

Install a Java 25 LTS JDK and Apache Maven 3.9+ and ensure `java` and `mvn` are on `PATH`.

```powershell
mvn clean verify
```

Tests are headless and do not need PostgreSQL or external services. They cover request JSON round trips, handler PING/PONG, malformed JSON, and a real localhost TCP exchange on a dynamically selected port.

To build a distributable directory:

```powershell
./scripts/package.ps1
```

This produces `release/admin.jar`, `release/user.jar`, and `release/server.jar`. Existing configuration in `release` is preserved, so local credentials are not overwritten.

## Configuration

There are two configuration files in `release`, created by `./scripts/package.ps1` from the examples in `config/`:

| File | Read by | Contains |
|---|---|---|
| `server.properties` | `server.jar` | PostgreSQL connection (`POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`) and `CHAT_SERVER_PORT` |
| `client.properties` | `admin.jar`, `user.jar` | `chat.server.host`, `chat.server.port` only; clients never receive database settings |

To run the packaged jars, edit only `release/server.properties`. Existing files in `release` are preserved when packaging again.

### Developer setup: edit only `.env`

Developers keep one file, `.env` at the repository root, shared with Docker Compose:

```powershell
Copy-Item .env.example .env   # then set POSTGRES_PASSWORD
```

The server reads `./server.properties` first and then `../.env` and `./.env`; a later file overrides an earlier one. So whenever the repository `.env` exists (running from the repository root, `server/`, or `release/`), it overrides `release/server.properties`, and developers never edit `server.properties`. Do not create a `.env` inside `release`. `.env` is ignored by Git; never commit it.

Packages created before this layout may contain an old `release/server.properties` with `spring.datasource.*` keys, which would override `.env`. Delete it and run `./scripts/package.ps1` again.

Override configuration locations without rebuilding (the repository `.env` still wins when present):

```powershell
java '-Dchat.server.config=C:/secure/chat-server.properties' -jar server.jar
java '-Dchat.client.config=C:/secure/chat-client.properties' -jar admin.jar
java '-Dchat.client.config=C:/secure/chat-client.properties' -jar user.jar
```

## Run and test connectivity

From `release`, start the server first, then either client in a separate terminal:

```powershell
java -jar server.jar
java -jar admin.jar
java -jar user.jar
```

Click **Test server connection**. The client sends a `PING` request in one UTF-8 JSON line, and the status changes to `connected (PONG)` when the correlated response arrives. The default endpoint is `localhost:5050`.

## Current status

Implemented: executable application entry points, FlatLaf windows, nonblocking client connection checks, bounded concurrent TCP handling, maximum message size enforcement, malformed-message responses, safe cleanup and graceful shutdown, external configuration, Spring JDBC infrastructure, packaging, and automated foundation tests.

Intentionally deferred: accounts, authentication/authorization, conversations, message delivery, friends, groups, admin statistics/charts, database tables and CRUD, email, and LLM integration. Their packages are reserved without fake implementations.

## Architecture notes

- TCP framing is newline-delimited JSON; a TCP read is never treated as a complete message by itself.
- Server push messages use `EventEnvelope`, while request replies use `ResponseEnvelope` and retain the request ID.
- The simplest configuration assumption is one shared client endpoint file for both desktop clients.

## Spec-Driven Development

The project uses the official GitHub Spec Kit to turn approved requirements into reviewed specifications, plans, and tasks before implementation. Shared Spec Kit infrastructure is stored in `.specify/`; future feature artifacts are created by the Spec Kit workflow rather than manually invented in advance. Project rules are defined in `.specify/memory/constitution.md`.

Codex discovers the installed skills under `.agents/skills/`. Invoke them in Codex chat using names such as `$speckit-constitution`, `$speckit-specify`, `$speckit-clarify`, `$speckit-plan`, `$speckit-tasks`, `$speckit-analyze`, and `$speckit-implement`. Run one stage at a time and review its output before continuing; do not start implementation until the specification, plan, and tasks are approved.

For team work, create `feature/*` branches from `dev`, keep specification artifacts with their feature changes, merge reviewed and verified work into `dev`, and reserve `main` for stable submission versions. Spec Kit does not replace the existing Git workflow and must not perform branch, commit, push, or merge operations without explicit developer direction.

Verify the setup from the repository root:

```powershell
specify version
specify check
mvn clean verify
```

## Formatting and AI guardrails

Java formatting is configured centrally in the parent POM with Spotless and Google Java Format. Formatting is intentionally independent of the normal Maven lifecycle:

```powershell
mvn spotless:check
mvn spotless:apply
```

Use `spotless:check` to report differences. Run `spotless:apply` only when reformatting is explicitly intended because it rewrites Java source files. Root `.editorconfig` defines encoding, line endings, whitespace, and indentation defaults for editors.

Codex follows the repository rules in `AGENTS.md`. Those concise operational guardrails complement the authoritative Spec Kit constitution in `.specify/memory/constitution.md`; they do not replace or duplicate the full constitution.
