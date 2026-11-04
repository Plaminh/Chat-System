# Quickstart: User Client UI Version 1

This guide validates the User UI implementation locally. Screens use sample data only; no live authentication, TCP chat, email, persistence, LLM provider, or encryption service is required.

## Prerequisites

- Java 25 LTS available on `PATH`.
- Maven available on `PATH`.
- Repository checked out on `feature/user-ui-v1`.
- Desktop environment available for visual Swing review. Automated tests/builds can run without opening the Swing application.

## Build and focused tests

From the repository root:

```powershell
mvn -pl user-client -am test
```

Expected: `common` and `user-client` compile, focused tests complete with zero failures, and no server or database is required for mock UI tests.

Then run full root verification:

```powershell
mvn clean verify
```

Expected: all four existing modules build and tests pass. Report the actual command result; do not infer success from compilation alone.

## Launch for visual review

Package the User Client and its dependencies:

```powershell
mvn -pl user-client -am package
```

Launch the packaged User application from the repository root:

```powershell
java -jar user-client\target\user.jar
```

Expected: the User sign-in screen appears without requiring a running TCP server. A successful sample sign-in opens the shared User workspace.

## Manual scenario checklist

1. **Account entry**: Submit blank required fields and confirm validation; use the documented sample flow to open the workspace; open registration and recovery; confirm recovery never displays a generated password.
2. **Profile**: Open profile from navigation; edit project-required fields; save and verify sample display update; cancel an edit and verify no change; open the password-change form.
3. **Friends and discovery**: Filter friends by name; show online-only contacts; review and accept/decline a request; search by username/full name; confirm blocked sample accounts are not shown; cancel a block/unfriend confirmation and verify no state change.
4. **Personal chat**: Open a conversation; inspect history; use the composer mock; search within a conversation and globally; select a result and verify navigation.
5. **Message/history actions**: Verify Delete for Me, Unsend, and Clear Conversation History are separate choices; confirm each prompt; cancel and verify state remains; ensure local mock outcomes do not claim shared deletion.
6. **Groups**: Attempt group creation with no other participant and verify rejection; create a sample group with another username; rename it; inspect roles; exercise add/remove/admin controls; confirm encryption is labelled as a placeholder.
7. **Spam report**: Open the report from an existing sample conversation; verify unrelated users cannot be reported from search; require a reason and confirm sample-only submission.
8. **Writing assistance**: Preview suggestion and revision; discard and verify the original draft is unchanged; accept and verify only the composer draft changes.
9. **Empty states**: Verify empty friend/request/conversation/group lists and no-result searches show an explanatory message and a sensible next action.

## Visual acceptance references

- Approved mockup PNG/HTML and group-level design notes: `docs/mockups/002-user-client-ui/`.
- Version 1 screenshots: `docs/screenshots/user/`.
- Screen-to-view responsibilities: [contracts/ui-components.md](contracts/ui-components.md).
- Sample entities and local state: [data-model.md](data-model.md).

## Out of scope for this quickstart

Do not configure PostgreSQL, SMTP, Groq/API credentials, a persistent chat socket, real user accounts, or encryption keys to validate this UI phase.
