# Research: User Client UI Version 1

**Date**: 2026-10-28

This design phase has no unresolved technology questions: the repository and constitution already establish the platform, Java version, UI toolkit, module boundaries, and the Version 1 mockup scope. The decisions below resolve implementation planning choices using the checked-in project and approved User screen exports.

## Decision 1: Implement inside the existing `user-client` module

- **Decision**: Add the User views and presentation models only under `user-client/src/main/java/com/chat/user/` and focused tests under that module's existing test source tree.
- **Rationale**: The repository already has the required `view`, `component`, `controller`, `model`, and `network` packages, a `UserApplication` entry point, FlatLaf, and JUnit 5. The root POM already defines exactly four modules.
- **Alternatives considered**:
  - Creating a new shared UI module: rejected because the constitution requires exactly four modules and these screens are User-specific.
  - Moving views into `common` or `server`: rejected because it violates module responsibilities and introduces UI/backend coupling.

## Decision 2: Use local sample state for Version 1 interactions

- **Decision**: Use deterministic in-memory presentation data for sign-in navigation, list filtering, request actions, message selection/removal, group edits, report confirmation, and writing-assistance preview.
- **Rationale**: The Version 1 handoff requires User UI source and explicitly defers working backend flows. Current `SocketClient` is a short-lived PING helper, not a persistent chat client.
- **Alternatives considered**:
  - Connect the screens to the TCP PING helper: rejected because PING does not implement these user-facing business flows and would misrepresent the mock UI as integrated.
  - Add local database/file persistence: rejected because it is not needed to demonstrate the approved screens and risks creating behavior outside the agreed phase.

## Decision 3: Keep the application shell common and feature views User-specific

- **Decision**: Use one User workspace frame/navigation shell for the signed-in views and focused panels/dialogs for individual tasks. Keep the existing Admin-aligned navy/blue desktop chrome and compact controls; preserve the User-specific content and layouts from the screenshots.
- **Rationale**: This supports a consistent application family while allowing chat, friends, and group views to use their required multi-pane content. The login remains a separate entry surface, matching the desktop reference.
- **Alternatives considered**:
  - Copying the Admin application menus or data grids into the User client: rejected because those workflows are Admin-only.
  - Building every mockup as an independent top-level window: rejected because it fragments the User journey and duplicates navigation.
  - Forcing one identical content layout across all features: rejected because chat, lists, and profile have different information structures.

## Decision 4: Treat mockup HTML and `DESIGN.md` as supporting references, not production implementation

- **Decision**: Use `screen.png` as the primary visual acceptance reference, inspect `code.html` for content/layout detail, and use repeated `DESIGN.md` files as shared group-level design-system documentation. Reconcile all of them against the approved feature specification.
- **Rationale**: The mockup directory contains 12 screen triplets, but several group-level `DESIGN.md` files are intentionally duplicated. Generated UI copy also contains content beyond the university project scope.
- **Alternatives considered**:
  - Treating generated HTML as the delivered UI: rejected because the deliverable must be Java Swing desktop source.
  - Implementing every detail and business claim in the mockups: rejected because extra items such as company information, phone number, password expiry, organizational settings, device security metrics, or claims of active encryption are not approved User requirements.

## Decision 5: Keep ambiguous chat deletion semantics neutral

- **Decision**: Present Delete for Me, Unsend, and Clear Conversation History as different confirmed sample actions, without asserting server-side retention, authorization, or visibility semantics.
- **Rationale**: `project.txt` explicitly identifies the effects of deleting a message/history as unresolved. A UI implementation cannot silently decide the server contract.
- **Alternatives considered**:
  - Treating all deletion operations as the same action: rejected because the project describes distinct behaviors.
  - Claiming an action deletes data for all participants or removes it from the server: rejected because that behavior has not been agreed.

## Decision 6: Use only existing dependencies and test/build tooling

- **Decision**: Keep Swing and FlatLaf for production UI and use the configured JUnit 5/Maven test lifecycle. Add no component library, icon package, or UI test framework in this phase.
- **Rationale**: Dependencies are already in `user-client/pom.xml`; test and build conventions are established at the root.
- **Alternatives considered**:
  - Adding JavaFX, web-rendered UI, or a new widget framework: rejected by the constitution and unnecessary for the approved screens.
  - Depending on a new GUI automation package before validating its need: rejected to avoid unnecessary tooling; manual desktop review supplements focused automated tests.
