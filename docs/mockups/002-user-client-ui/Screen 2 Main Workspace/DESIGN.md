---
name: Admin Workstation
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#434655'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#006329'
  on-tertiary: '#ffffff'
  tertiary-container: '#007f36'
  on-tertiary-container: '#c7ffca'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#7ffc97'
  tertiary-fixed-dim: '#62df7d'
  on-tertiary-fixed: '#002109'
  on-tertiary-fixed-variant: '#005320'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: -0.005em
  headline-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  body-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  body-md-medium:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.04em
  code-sm:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 0.75rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system is tailored for high-density, mission-critical desktop enterprise software. Drawing heritage from modern flat desktop application runtimes (such as FlatLaf and modern Java Swing enterprise clients), it fuses native desktop ergonomics with crisp modern web interface fidelity. 

The aesthetic is functional, restrained, disciplined, and utilitarian. It deliberately rejects excessive consumer-app fluff, hyperbolic whitespace, and floaty glassmorphism in favor of structural clarity, high information throughput, and sub-pixel tactile control.

### Core Visual Principles
- **Predictable Rigidity:** Tool windows, fixed panels, splitters, sidebars, and tabular grids form an architectural workbench. Elements snap to structural boundaries.
- **High-Density Focus:** Desktop interfaces operate under mouse and keyboard mechanics. Tight row heights, 32px standard input targets, and immediate access to filter toolbars reduce friction for operators handling high volumes of records.
- **High-Contrast Differentiation:** Deep navy chrome anchors navigational hierarchy, while cool slate canvases frame brilliant white panels. Distinct functional highlights (royal blue selection, emerald connectivity flags, crimson destructive actions) direct attention without visual noise.

## Colors

The palette establishes an unmistakable separation between window-level administrative navigation chrome and active data manipulation workspaces.

### Palette Architecture
- **Navigation Chrome (Dark Navy/Slate):**
  - Surface Sidebar Root: `#09101d`
  - Sidebar Sub-surface / Section Borders: `#0d1726`
  - Inactive Item Text: `#94a3b8`
  - Hover Fill: `#162238`
  - Active Nav Item Fill: `#2563eb` (with pure `#ffffff` text)
- **Application Canvas & Toolbars:**
  - Workspace Canvas: `#f1f5f9`
  - Window Chrome / Status Strip: `#f8fafc`
  - Content Panels & Table Bodies: `#ffffff`
  - Table Header Fill: `#f8fafc`
  - Table Row Hover: `#f8fafc`
  - Table Row Selected: `#e0edff` (with subtle active border `#bfdbfe`)
- **Action & Accent Hierarchy:**
  - Primary Action / Callout: `#2563eb` (Hover: `#1d4ed8`, Active: `#1e40af`)
  - Outline & Secondary Action Surfaces: `#ffffff` with `#cbd5e1` structural border and `#1e293b` text
  - Active Action Footer Bar: `#0f172a` (High-contrast floating contextual dock)
- **Status & Diagnostics:**
  - Operational / Active Badge: `#16a34a` (with `#dcfce7` tinted chip backing)
  - Warning / Quarantine: `#d97706` (with `#fef3c7` backing)
  - Destructive / Ban / Purge: `#dc2626` (Hover: `#b91c1c`, tinted chip backing: `#fee2e2`)
- **Structural Dividers & Outlines:**
  - Border Subtle: `#e2e8f0`
  - Border Default: `#cbd5e1`
  - Form Input Border: `#94a3b8` (Focus: `#2563eb` with 2px ring `#bfdbfe`)

## Typography

Typography prioritizes extreme data density, tabular scannability, and structural hierarchy. Standard desktop viewports avoid bloated scale jumps; the entire interface thrives within a tightly tuned 11px to 18px range.

### Typography Guidelines
- **Primary Font Family:** Inter is configured with neutral tabular figures enabled (`font-feature-settings: "tnum" 1, "cv05" 1`) to guarantee numerical columns, IP addresses, dates, and account identifiers align perfectly across grids.
- **Section Headers (`headline-lg`, 18px):** Reserved for primary panel titling and window header labels.
- **Operational Body & Tables (`body-md`, 13px):** The core driver of table cells, forms, dialogue queries, and navigational lists. Delivers optimal legibility without scrolling fatigue.
- **Metadata & Section Tags (`label-sm`, 11px):** Rendered in uppercase bold for category dividers in the navy navigation pane (e.g., `OVERSIGHT & MANAGEMENT`, `ANALYTICS & INTELLIGENCE`), and for breadcrumb trails.
- **Technical Readouts (`code-sm`, 12px):** Monospaced tokens applied to generated IDs (`USR-10...`), session tokens, hashes, and terminal breadcrumbs.

## Layout & Spacing

The layout model simulates a structured desktop workstation shell tailored for 1280x800 and 1920x1080 display viewports.

### Shell Architecture
- **Window Title / Top Metadata Bar:** 32px height fixed header, anchoring connection state and build revision.
- **Sidebar Shell:** Fixed 220px to 240px left-hand dark navigation column spanning full application height.
- **Work Area Canvas:** Fluid interior flex column nested within the right frame:
  - **Breadcrumb & Viewport Header Strip:** 44px to 52px height.
  - **Tool & Filter Bar:** 40px height containing horizontal filter inputs, sort selectors, and quick-action triggers.
  - **Tabular Data Grid:** Auto-flexing viewport with internal row virtualization and vertical scroll.
  - **Selected Record Action Shelf (Dock):** 48px to 52px dark contextual bar docked at the base of the table containing batch and item-level operations.
  - **Status Bar:** Fixed 24px bottom footer containing diagnostic text, telemetry counters, and readiness status dots.

### Spacing Tokens
- `space-xs` (4px): Micro gaps between icons and labels, button grouping gutters, cell internal padding.
- `space-sm` (8px): Standard gap between toolbar elements, form field label margins, chip horizontal margins.
- `space-md` (12px): Inner padding for panels, card containers, dialog insets.
- `space-lg` (16px): Outer margins for main stage modules.
- `space-xl` (24px): Boundary separations for authentication screens or wide-format dashboards.

## Elevation & Depth

This system avoids blurred, floaty shadows in favor of authentic desktop workstation depth: **low-contrast outlines, high-contrast flat layering, and crisp 1px borders**.

### Layering Rules
- **Base Canvas:** Flat `#f1f5f9` backplate.
- **Panel Containment:** Pure white (`#ffffff`) surfaces defined by a continuous `1px solid #cbd5e1` perimeter. No drop shadows are cast under normal desktop lighting conditions.
- **Contextual Action Dock:** The bottom selection bar (`#0f172a`) uses a sharp, crisp line (`border-top: 1px solid #334155`) with a minimal directional offset: `box-shadow: 0 -2px 6px rgba(0, 0, 0, 0.08)`.
- **Modals & Dialogs (Swing Window Simulation):** Centered floating dialog windows carry an intentional desktop-style border: `1px solid #94a3b8` backed by an ambient occlusion shadow `0 8px 24px -4px rgba(15, 23, 42, 0.25)`.
- **Active Navigation Highlights:** In the sidebar, active states do not float; they sit completely flat as solid blocks (`#2563eb`) flush against the grid.

## Shapes

The design system employs **compact desktop angularity**. Soft curves are dialed back to preserve standard desktop application geometry, maximizing information area and maintaining crisp alignment with 1px border lines.

### Shape Standards
- **Default Corner Radius (4px / 0.25rem):** Form inputs, buttons, table cell indicators, toolbar dropdowns, and alert boxes utilize a uniform 4px corner.
- **Panels & Window Containers (6px / 0.375rem):** Outer application cards, dialog enclosures, and tabular frame edges.
- **Indicators & Status Pills:** Full 9999px pills are used exclusively for compact status tags (such as `Online Session` or `Admin Role`) to provide instant functional differentiation from square operational buttons.

## Components

### Buttons
- **Primary Desktop Action:** Height 32px (standard) or 36px (auth call-to-action). Background `#2563eb`, border `1px solid #1d4ed8`, text `#ffffff` (font size 13px, weight 600). Hover `#1d4ed8`, Active `#1e40af`.
- **Secondary / Toolbar Button:** Height 32px. Background `#ffffff`, border `1px solid #cbd5e1`, text `#1e293b` (font size 13px, weight 500). Hover `#f8fafc` with border `#94a3b8`.
- **Destructive Button:** Height 32px. Background `#dc2626`, border `1px solid #b91c1c`, text `#ffffff`. Hover `#b91c1c`.
- **Contextual Dock Actions:** Mounted inside the `#0f172a` bottom bar. White flat buttons with 1px borders contrasting against the dark slate substrate.

### Form Inputs & Selectors
- **Text Inputs:** Height 32px. Background `#ffffff`, border `1px solid #94a3b8`, padding `0 8px`, font size 13px. Focus ring: `outline: none; border-color: #2563eb; box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2)`.
- **Dropdown Filters:** Height 32px. Clean rectangular appearance with crisp chevron icon, 8px padding, matching form input border tokens.
- **Checkboxes:** 14px x 14px crisp square, 2px border radius. Checked state: `#2563eb` with crisp white tick.

### Tabular Grid (Desktop Table)
- **Header Row:** Height 32px. Background `#f8fafc`, border bottom `1px solid #cbd5e1`, typography `label-sm` in `#475569`.
- **Body Rows:** Height 36px fixed density. Alternate striping is avoided; differentiation is handled via `1px solid #f1f5f9` cell dividers.
- **Row Hover:** Background `#f8fafc`.
- **Row Selected:** Background `#e0edff` with `#1d4ed8` active indicator highlight on the leading column border.

### Status Indicators & Badges
- **Status Dot & Pill:** 20px height, 8px horizontal padding, pill border radius. Green badge: background `#dcfce7`, text `#15803d`, leading 6px circular dot `#16a34a`. Red badge: background `#fee2e2`, text `#b91c1c`.
- **Workspace Ready Telemetry:** Status strip indicator featuring a pulsing 6px `#16a34a` dot followed by `11px` muted gray metadata (`#64748b`).

### Navigation Sidebar Menu
- **Group Labels:** 11px uppercase bold tracking (`letter-spacing: 0.05em`), `#64748b` color, 12px vertical padding.
- **Navigation Row:** 36px height, 8px horizontal padding, 6px icon spacing, 13px font size. Neutral state `#94a3b8`. Active state `#ffffff` on `#2563eb` fill with 4px border radius.