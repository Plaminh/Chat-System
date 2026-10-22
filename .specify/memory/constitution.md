# Java Chat System Constitution

## Core Principles

### I. Project Scope
Implement only functionality required by the university Final Project specification. Additional business features require explicit approval, and unspecified rules must not be resolved by silently expanding scope.

### II. Technology Constraints
The frontend MUST remain a Windows desktop application. The backend MUST use a Java framework and TCP sockets. WebSocket is prohibited, and PostgreSQL is the required database.

### III. Existing Technology Stack
Preserve Java 21, Maven, Swing, FlatLaf, JFreeChart where applicable, Spring Boot in non-web mode, Spring JDBC, Jackson, PostgreSQL, and BCrypt. Replacing this stack requires an explicit project decision.

### IV. Module Boundaries
The project MUST contain exactly four Maven modules: `common`, `admin-client`, `user-client`, and `server`. Both clients and the server may depend on `common`; clients MUST NOT depend on `server`, and `server` MUST NOT depend on either client.

### V. Architecture Responsibilities
Client modules handle presentation and user interaction. The server handles authentication, authorization, business logic, networking, and persistence. Only the server may access PostgreSQL.

### VI. Code Quality
Code MUST be readable and maintainable, follow existing Java and package conventions, and avoid unnecessary abstractions, speculative implementations, and additional frameworks.

### VII. Testing
Every implemented behavior requires focused tests appropriate to its risk. `mvn clean verify` MUST remain successful, and test or runtime success MUST NOT be claimed unless the relevant command was actually executed.

### VIII. Development Workflow
Feature branches are based on `dev`; reviewed and verified work is merged into `dev`; `main` is reserved for stable submission versions. Automated agents MUST NOT create or switch branches, commit, push, or merge unless explicitly instructed.

### IX. AI Usage
Meaningful AI-assisted work MUST be recorded in `AI-LOG.md`. Developers remain responsible for understanding, reviewing, and verifying AI-generated configuration, documentation, and code.

### X. Project Delivery
Delivery MUST respect the university Version 1 and Version 2 requirements. Version 1 focuses on desktop UI source, database scripts, and design documentation. Version 2 implements working business functionality, networking, database integration, and packaged applications. No additional university requirements may be invented.

## Technical and Scope Guardrails

- Do not introduce WebSocket, REST as the client protocol, JPA/Hibernate, microservices, JavaFX, or a fifth Maven module without an approved project decision.
- Do not move database access into either desktop client.
- Keep shared protocol definitions in `common` without Swing, Spring, or database dependencies.
- Treat placeholder packages as reserved structure, not incomplete features.
- Keep credentials and local configuration out of version control.

## Quality Gates and Review

Each change must remain within an approved specification, respect module boundaries, include focused verification, and preserve a successful root Maven build. Reviews must distinguish implemented infrastructure from deferred business functionality. Changes affecting the protocol, persistence, concurrency, security, or packaging require proportionate automated or runtime verification.

## Governance

This constitution governs project specifications, plans, tasks, implementation, and review. Amendments require explicit team approval, a documented reason, and updates to dependent Spec Kit artifacts where applicable. Compliance is reviewed before work is merged into `dev`; exceptions must be recorded rather than assumed.

**Version**: 1.0.0 | **Ratified**: 2026-10-22 | **Last Amended**: 2026-10-22
