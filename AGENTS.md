# Repository Instructions

Use `.specify/memory/constitution.md` as the authoritative project constitution. These instructions summarize the working constraints for Codex without replacing it.

## Scope and architecture

- Follow the university Final Project requirements. Preserve working code, do not invent business features, and do not expand scope without explicit approval.
- Keep exactly four Maven modules: `common`, `admin-client`, `user-client`, and `server`. Do not create `client-common` or another module.
- Keep shared DTOs and protocol definitions in `common`. Clients may depend on `common` but never on `server`; `server` may depend on `common` but never on a client module.
- Frontends handle presentation and interaction. The backend handles business logic, authorization, networking, and persistence. Only the backend accesses PostgreSQL.

## Technology and code changes

- Preserve Java 21, Maven, Swing, FlatLaf, Spring Boot in non-web mode, TCP sockets, Spring JDBC, Jackson, and PostgreSQL.
- WebSocket is prohibited. Do not introduce REST as the main client protocol, JPA/Hibernate, microservices, or unnecessary abstractions.
- Modify only files required by the task. Avoid unrelated refactoring and preserve existing project conventions and Java 21 compatibility.
- Do not silently change protocol contracts or database schemas.

## Formatting and verification

- Follow `.editorconfig` and use Spotless with Google Java Format for Java formatting. Do not introduce another formatter or format unrelated files.
- Do not run `mvn spotless:apply` unless formatting is explicitly requested. Use `mvn spotless:check` to report formatting violations.
- Run checks appropriate to the change and preserve successful root Maven builds. Report only tests actually executed; never weaken tests merely to make failures disappear.

## Security, Git, and AI transparency

- Never hardcode or commit passwords, API keys, credentials, or local secrets. Never expose database credentials to clients.
- Do not create or switch branches, commit, push, merge, rebase, or rewrite history without explicit instructions. Follow the `main`/`dev`/`feature/*` workflow.
- Update `AI-LOG.md` for meaningful AI-assisted work, identify AI-generated changes honestly, and leave final review responsibility with developers.
