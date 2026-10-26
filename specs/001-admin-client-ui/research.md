# Phase 0 Research: Admin Client User Interface (Version 1)

**Feature**: `001-admin-client-ui`  
**Date**: 2026-10-26  
**Status**: Resolved

---

## 1. Visual Design System & FlatLaf Integration

- **Decision**: Apply `FlatLightLaf` as the base Look & Feel, customized with the *Desktop Admin Studio* color tokens extracted from `docs/mockups/001-admin-client-ui/**/DESIGN.md`, while stripping out non-business DevOps/telemetry noise (`TCP Live`, `JVM Heap`, `PostgreSQL Pool`, `GraphBLAS`).
- **Rationale**:
  - The Stitch mockups use a clean light workspace (`#F8FAFC` canvas, `#FFFFFF` cards/tables, `#E2E8F0` borders, `#3B82F6` primary accent) paired with a dark slate sidebar (`#0F172A`).
  - Removing infrastructure/DevOps noise aligns strictly with the Constitution Principle I (Project Scope) and Principle V (Architecture Responsibilities: clients handle presentation and user interaction, not backend infrastructure monitoring).
  - FlatLaf supports rounded corners (`Component.arc`, `Button.arc`, `TextComponent.arc`) and custom table styling natively without third-party UI libraries.
- **Alternatives Considered**:
  - *Full Dark Theme (`FlatDarkLaf`)*: Rejected because all 10 finalized Stitch mockups use a light content canvas with a dark navigation sidebar.
  - *Raw Metal/Windows Look & Feel*: Rejected by project specification requiring FlatLaf modern styling.

---

## 2. Vector Icons in Java Swing without External Image Dependencies

- **Decision**: Provide programmatic vector icons (`Icon` implementations using Java2D `Graphics2D` with `RenderingHints.KEY_ANTIALIASING`) and optional classpath icon loading from `admin-client/src/main/resources/icons/`.
- **Rationale**:
  - Stitch HTML mockups used web-based Google Material Symbols font icons.
  - Programmatic Java2D vector icons (16x16 / 20x20) guarantee crisp rendering on HiDPI Windows displays, zero missing-asset runtime errors, dynamic color tinting (e.g., muted slate when inactive vs. white when selected on the sidebar), and zero additional Maven dependencies.
- **Alternatives Considered**:
  - *Adding `flatlaf-extras` or `ikonli` Maven dependencies*: Rejected to strictly preserve `admin-client/pom.xml` without introducing unapproved dependencies.

---

## 3. Tabular Filtering, Sorting, and Custom Badge Rendering

- **Decision**: Use standard Swing `JTable` backed by `DefaultTableModel` and `TableRowSorter<DefaultTableModel>`, paired with custom `DefaultTableCellRenderer` implementations for status pills (`Active` emerald badge, `Locked` rose badge, `Group Admin` gold/blue badge).
- **Rationale**:
  - `YeuCauDoAn` mandates instant filtering by name/username/status/date-range/comparison operators (`=`, `>`, `<`) and sorting by name or creation date across screens.
  - `TableRowSorter` with custom `RowFilter` predicates handles multi-criteria filtering (text search + dropdown filter + numeric comparison + date range) in under 10ms on the Swing EDT.
- **Alternatives Considered**:
  - *Manual list rebuilding on every keystroke without `TableRowSorter`*: Less consistent with native column header sorting indicators.

---

## 4. JFreeChart Styling for Screen 10 (Registration & Activity Charts)

- **Decision**: Implement two dedicated `JPanel` classes in `com.chat.admin.chart`:
  1. `RegistrationChartPanel`: Uses `ChartFactory.createBarChart` with `CategoryPlot` and custom `BarRenderer` (`#3B82F6` bars, `#FFFFFF` plot background, `#E2E8F0` dashed gridlines, value labels above bars) for 12-month new user registrations by selected year.
  2. `ActivityChartPanel`: Uses `ChartFactory.createLineChart` with `LineAndShapeRenderer` (`#10B981` emerald line, filled circular data points, `#FFFFFF` background) for 12-month active users (app openers) by selected year.
- **Rationale**:
  - Directly satisfies `Plan_Admin_UI_Version_1.md` Task 10 (`RegistrationChartPanel.java` and `ActivityChartPanel.java`) and `YeuCauDoAn` sections 1.2.6 & 1.2.9.
  - Default JFreeChart styling looks dated (gray background, 3D bevels); customizing `CategoryPlot`, `BarRenderer` (`StandardBarPainter`), and fonts (`Inter`/`SansSerif`) makes JFreeChart match the Stitch Screen 10 mockup closely.
- **Alternatives Considered**:
  - *Custom Java2D canvas drawing instead of JFreeChart*: Rejected because JFreeChart is explicitly required by the project specification and `Plan_Admin_UI_Version_1.md`.

---

## 5. Self-Contained Mock Data Architecture for Version 1

- **Decision**: Create immutable Java 21 `record` / model classes and a centralized in-memory `AdminMockDataStore` in `com.chat.admin.model` to supply consistent, realistic Vietnamese/English university chat dataset across all 10 components.
- **Rationale**:
  - Version 1 strictly forbids PostgreSQL connections from the client and defers TCP socket business integration to Version 2.
  - Having a shared in-memory mock store allows interactive actions in Version 1 (e.g., locking an offending account in `SpamReportPanel` or adding/editing/locking/deleting a user in `UserManagementPanel`) to reflect consistently across tables during demonstration and screenshot capture.
- **Alternatives Considered**:
  - *Hardcoding disconnected String arrays inside each JPanel*: Makes sorting by `LocalDate`/`LocalDateTime`, numeric filtering (`=`, `>`, `<`), and cross-dialog inspection (opening `UserDetailHistoryDialog` for a selected user) brittle and repetitive.
