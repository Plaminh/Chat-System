# Implementation Plan: Admin Client User Interface (Version 1)

**Branch**: `001-admin-client-ui` | **Date**: 2026-10-26 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-admin-client-ui/spec.md`

## Summary

Design and implement the complete **Version 1 Admin Client Desktop UI** (`admin-client` module) covering all 9 required university administrative functional groups across **10 modular Swing UI deliverables (10 independent Git commits)**. The implementation uses Java 21, Java Swing, FlatLaf (`FlatLightLaf` customized with the *Desktop Admin Studio* color tokens), JFreeChart for 12-month trend visualization, and an in-memory mock dataset (`AdminMockDataStore`) without direct database connections or premature backend socket dependencies. All extraneous DevOps/infrastructure telemetry from the raw Stitch mockups (`TCP Live`, `JVM Heap`, `PostgreSQL Pool`, `GraphBLAS`, etc.) is stripped to maintain strict domain focus on Chat System administration.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Java Swing / AWT, FlatLaf (`com.formdev:flatlaf`), JFreeChart (`org.jfree:jfreechart`), JUnit 5 (`org.junit.jupiter:junit-jupiter` for test scope)

**Storage**: In-memory mock data (`com.chat.admin.model.AdminMockDataStore`) for Version 1 (N/A for client database access)

**Testing**: JUnit 5 + Maven (`mvn test-compile`, `mvn verify`, `mvn spotless:check`)

**Target Platform**: Windows Desktop Application (`1280x800` base viewport, responsive resizing)

**Project Type**: desktop-app (Multi-module Maven Desktop Client: `admin-client`)

**Performance Goals**: Instant UI navigation (`CardLayout` < 16ms) and table filtering/sorting (`TableRowSorter` < 50ms) on Swing EDT

**Constraints**: Strictly within `admin-client` module; no WebSocket/REST/JPA; no database credentials or backend connections in Version 1 UI; preserve Google Java Format / Spotless conventions

**Scale/Scope**: 10 UI screens/components (1 Main Frame, 2 Modal Dialogs, 6 Management/Analytics Panels, 2 JFreeChart Panels) + supporting Mock DTOs and UI theme/icon helpers

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Project Scope (PASS)**: Implements exact 9 Admin functional areas from `YeuCauDoAn` (1.2). Strips out non-requested DevOps/telemetry widgets from Stitch mockups.
- **II. Technology Constraints (PASS)**: Windows desktop Swing application; no WebSocket or web framework introduced.
- **III. Existing Technology Stack (PASS)**: Uses Java 21, Maven, Swing, FlatLaf, and JFreeChart already declared in `admin-client/pom.xml`.
- **IV. Module Boundaries (PASS)**: All code resides strictly inside `admin-client`. No new Maven modules created; no dependency on `server`.
- **V. Architecture Responsibilities (PASS)**: `admin-client` handles presentation, filtering, sorting, and mock state only. Zero PostgreSQL access.
- **VI. Code Quality (PASS)**: Clean separation of `model`, `view`, and `chart` packages inside `com.chat.admin`.
- **VII. Testing (PASS)**: Preserves `mvn clean test-compile` and unit test execution.
- **VIII. Development Workflow (PASS)**: Structured into 10 atomic tasks matching the 10-commit plan in `docs/Plan_Admin_UI_Version_1.md`.
- **IX. AI Usage (PASS)**: Meaningful AI-assisted work will be logged in `AI-LOG.md`.
- **X. Project Delivery (PASS)**: Directly fulfills Version 1 UI source code and screenshot preparation requirements.

## Project Structure

### Documentation (this feature)

```text
specs/001-admin-client-ui/
├── plan.md              # This file ($speckit-plan command output)
├── research.md          # Phase 0 output ($speckit-plan command)
├── data-model.md        # Phase 1 output ($speckit-plan command)
├── quickstart.md        # Phase 1 output ($speckit-plan command)
├── contracts/           # Phase 1 output ($speckit-plan command)
│   └── ui-components.md
└── tasks.md             # Phase 2 output ($speckit-tasks command - NOT created by $speckit-plan)
```

### Source Code (repository root)

```text
admin-client/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/chat/admin/
    │   │   ├── AdminApplication.java                 # Application entry point (launches Login -> MainFrame)
    │   │   ├── model/
    │   │   │   ├── AdminUser.java                    # User account & statistics record
    │   │   │   ├── LoginRecord.java                  # Login timestamp audit record
    │   │   │   ├── FriendConnection.java             # Direct friend connection record
    │   │   │   ├── GroupChatInfo.java                # Group chat & member roster model
    │   │   │   ├── SpamReportRecord.java             # Spam report incident model
    │   │   │   └── AdminMockDataStore.java           # Centralized realistic mock dataset & state operations
    │   │   ├── view/
    │   │   │   ├── AdminUIStyle.java                 # Shared colors, fonts, vector icons, badge renderers
    │   │   │   ├── AdminMainFrame.java               # [Screen 1/Task 1] Main shell with dark sidebar & CardLayout
    │   │   │   ├── AdminLoginDialog.java             # [Screen 2/Task 2] Admin login modal dialog with validation
    │   │   │   ├── UserManagementPanel.java          # [Screen 3/Task 3] User CRUD, Lock/Unlock, Password reset
    │   │   │   ├── UserDetailHistoryDialog.java      # [Screen 4/Task 4] Modal for Login History & Friend Network
    │   │   │   ├── GroupManagementPanel.java         # [Screen 5/Task 5] Group list + right-side Member/Admin viewer
    │   │   │   ├── SpamReportPanel.java              # [Screen 6/Task 6] Spam reports filtering + Lock Account
    │   │   │   ├── NewRegistrationPanel.java         # [Screen 7/Task 7] Date-range new registrations list & filter
    │   │   │   ├── FriendStatsPanel.java             # [Screen 8/Task 8] Direct & 2nd-degree friends (=, >, <)
    │   │   │   └── UserActivityPanel.java            # [Screen 9/Task 9] App opens, chatted users/groups (=, >, <)
    │   │   └── chart/
    │   │       ├── RegistrationChartPanel.java       # [Screen 10/Task 10] JFreeChart 12-month bar chart
    │   │       └── ActivityChartPanel.java           # [Screen 10/Task 10] JFreeChart 12-month line chart
    │   └── resources/
    │       ├── icons/
    │       └── images/
    └── test/
        └── java/com/chat/admin/
```

**Structure Decision**: Multi-module Maven desktop client structure inside `admin-client/src/main/java/com/chat/admin/` (`model`, `view`, `chart`) as defined by the repository layout and `docs/Plan_Admin_UI_Version_1.md`.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
