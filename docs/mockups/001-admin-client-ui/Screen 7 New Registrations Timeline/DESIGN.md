---
name: Desktop Admin Studio
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
  on-surface-variant: '#424754'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#727785'
  outline-variant: '#c2c6d6'
  surface-tint: '#005ac2'
  primary: '#0058be'
  on-primary: '#ffffff'
  primary-container: '#2170e4'
  on-primary-container: '#fefcff'
  inverse-primary: '#adc6ff'
  secondary: '#565e74'
  on-secondary: '#ffffff'
  secondary-container: '#dae2fd'
  on-secondary-container: '#5c647a'
  tertiary: '#006947'
  on-tertiary: '#ffffff'
  tertiary-container: '#00855b'
  on-tertiary-container: '#f5fff6'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc6ff'
  on-primary-fixed: '#001a42'
  on-primary-fixed-variant: '#004395'
  secondary-fixed: '#dae2fd'
  secondary-fixed-dim: '#bec6e0'
  on-secondary-fixed: '#131b2e'
  on-secondary-fixed-variant: '#3f465c'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: -0.01em
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
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.02em
  code-sm:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  code-xs:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-dense: 0.5rem
  margin: 1rem
  space-xxs: 0.125rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system is engineered specifically for dense, mission-critical desktop admin tools—capturing the refined, high-efficiency ergonomics of JetBrains IntelliJ IDEA, modern macOS utility apps, and FlatLaf dark-sidebar desktop workbenches.

The visual direction pairs a high-contrast dark slate navigation anchor with an ultra-clean, clinical light slate canvas. The operational mood is precise, responsive, and predictable:
- **Style Archetype:** Modern Desktop / Tooling Minimalist. Structured layout with crisp 1px borders, subtle surface elevations, tight information density, and instant legibility under high-frequency administrative workflows.
- **Tone:** Professional, reliable, system-native, and utilitarian. Form fields, menus, and data grids behave like native desktop controls rather than floaty web marketing components.
- **Viewport Target:** 1280×800 base desktop canvas, scaling fluidly across expanded multi-monitor setups without sacrificing compact vertical rhythm.

## Colors

The palette establishes strict functional separation between navigation controls, structural framing, working surfaces, and operational states.

### Core Canvas & Surfaces
- **Canvas Base:** `#F8FAFC` (Light Slate) — neutral background for all primary viewports and panel backdrops.
- **Surface Card/Panel:** `#FFFFFF` (Pure White) — work areas, data tables, dialog bodies, and inspectors.
- **Sidebar & Title Frame:** `#0F172A` (Deep Slate) — creates an intentional anchor for the app navigation, active server clusters, and system status indicators.
- **Sidebar Surface Muted:** `#1E293B` — hover states and secondary tiers in dark frame contexts.
- **Sidebar Active Item:** `#2563EB` with subtle 10% opacity backdrop wash for selection indicators.

### Accents & Semantic Tones
- **Primary Interactive:** `#3B82F6` (Indigo/Cobalt Blue), transitioning to `#2563EB` on hover and `#1D4ED8` on click.
- **Success / Healthy State:** `#10B981` (Emerald) with soft pill surface `#ECFDF5` and stroke `#A7F3D0` (used for active socket connections, operational channels, and online operators).
- **Danger / Locked / Terminated:** `#EF4444` (Rose Red) with soft pill surface `#FEF2F2` and stroke `#FECACA` (used for banned users, force-killed channels, and destructive resets).
- **Warning / Degraded:** `#F59E0B` (Amber) with soft pill surface `#FFFBEB` and stroke `#FDE68A` (used for rate-limiting triggers, pending approvals, and latency alerts).

### Borders & Structural Lines
- **Primary Line:** `#E2E8F0` — 1px divider for table rows, panel splits, card outlines, and text inputs.
- **Subtle Line:** `#F1F5F9` — alternate zebra table row striping and internal sub-headers.
- **Sidebar Divider:** `#334155` — crisp 1px separation within dark utility bars.

## Typography

Typography prioritizes information density and zero visual lag during scanning. **Inter** serves as the primary desktop typeface for UI controls, navigation, and tabular text. **JetBrains Mono** is specified for IP addresses, room hash identifiers, socket port badges, timestamp logs, and audit payloads.

### Rules of Application
- **Headers (16–18px Semi-bold):** Reserved for viewport titles, section splits, and dialog headers. Never scale above 18px within main content to prevent displacing data rows.
- **Body & Data Grids (13px Regular / Medium):** The baseline workhorse size. Renders table cells, dropdown options, and form fields with crisp vertical alignment at standard DPI.
- **Status Pills & Captions (11–12px Medium):** Used on status tags, metadata annotations, table header columns (uppercase with subtle tracking), and helper text.
- **Monospace Tokens (11–12px):** Applied systematically to all raw IDs (e.g., `usr_892b11`, `127.0.0.1:8080`, `#general-ops`).

## Layout & Spacing

The layout is architected around a fixed multi-pane desktop shell optimized for a standard 1280×800 canvas:
- **Left Navigation Rail (Dark):** Fixed width of 220px to 240px. Hosts system health, main sections (Channels, Users, Moderation, Audit, Gateway Settings), and admin profile.
- **Top Utility & Filter Toolbar:** Fixed 48px height directly above main content. Contains persistent search, filter chips, action button groups, and refresh triggers.
- **Master-Detail Splitting:** Data tables occupy the left/center workspace (flexible ratio), while inspector drawers or contextual side panels occupy a collapsible 320px–380px right panel.
- **Grid & Gutters:** 12-column layout when displaying dashboard card overviews, utilizing 16px (`1rem`) gutters. In high-density mode (active chat monitoring / raw socket inspection), gutters compress to 8px (`gutter-dense`).
- **Inner Padding Rules:** Inputs and table cells maintain an 8px vertical by 12px horizontal padding rhythm to optimize mouse navigation and click targets on desktop cursors.

## Elevation & Depth

This system avoids heavy, atmospheric consumer shadows in favor of structural panel demarcation, crisp 1px borders, and tactile modal elevation typical of high-end desktop environments (macOS Finder / IntelliJ):

- **Base Surfaces (Level 0):** Canvas (`#F8FAFC`) and embedded panels sit flush. Visual separation is accomplished entirely via 1px border lines (`#E2E8F0`).
- **Cards & Data Tables (Level 1):** Solid white surface (`#FFFFFF`) with a 1px border (`#E2E8F0`). Optional ambient lift on interactive card hover: `0 1px 3px 0 rgba(15, 23, 42, 0.06), 0 1px 2px -1px rgba(15, 23, 42, 0.04)`.
- **Dropdowns & Flyouts (Level 2):** Elevated menus, filter pickers, and tooltips use crisp white surfaces bordered by `#CBD5E1`, cast with: `0 4px 6px -1px rgba(15, 23, 42, 0.08), 0 2px 4px -2px rgba(15, 23, 42, 0.06)`.
- **Modal Dialogs & Sheets (Level 3):** Modal windows feature a distinct title bar, pure white body, and a focused desktop ambient shadow: `0 20px 25px -5px rgba(15, 23, 42, 0.16), 0 8px 10px -6px rgba(15, 23, 42, 0.1)`. Modals display over a 40% opacity slate backdrop (`rgba(15, 23, 42, 0.40)`).

## Shapes

The design system maintains a consistent desktop-native radius:
- **Base Components:** Standard 6px radius (`0.375rem`) applied to input fields, buttons, action chips, table selection bounds, and dropdown lists.
- **Panels & Cards:** 8px radius (`0.5rem`) on internal card containers and sub-windows.
- **Status Badges & Action Pills:** Full pill radius (9999px) for operational state tags (Online, Idle, Locked, Banned) to instantly distinguish metadata tags from rectangular interactive buttons.
- **Window Title Bars & Modals:** 8px top radius with crisp 0px internal joins where title bars weld to content surfaces.

## Components

### 1. Window Frames & Title Bars
- **Desktop Window Bar:** 40px height. In modal dialogs, features a subtle bottom border (`#E2E8F0`), bold 14px centered or left-aligned title, and native-styled window action controls (macOS style close/minimize dots or clean top-right 16px hover close icon).
- **Draggable Header Area:** Background `#F8FAFC` on modals, `#0F172A` on the main application shell.

### 2. Buttons & Button Groups
- **Primary Action:** Solid `#3B82F6` background, white text, 6px border radius, 32px height (desktop compact). Hover `#2563EB`, active `#1D4ED8`. Font weight 500 at 13px.
- **Secondary / Outline:** White background with 1px `#E2E8F0` border, `#334155` text. Hover brings surface to `#F8FAFC` and border to `#CBD5E1`.
- **Destructive Action:** `#EF4444` background or outline with hover state `#DC2626`.
- **Segmented Button Sets:** Joined buttons with 1px internal dividers for switching view layouts (e.g., Grid / Table / Split View).

### 3. Desktop Toolbar & Filters
- **Filter Bar Container:** 44px height, light background `#FFFFFF` with 1px bottom border `#E2E8F0`.
- **Search Field:** 30px height, inset search icon (14px), placeholder "Filter users, IPs, or channels...", keyboard shortcut badge (`⌘K` or `Ctrl+F`) positioned on the trailing edge.
- **Segmented Tabs:** Flat horizontal indicators with a 2px active bottom stroke in `#3B82F6` and bold active text.

### 4. Data Tables
- **Header Row:** 32px height, uppercase 11px font (`Inter`), weight 600, color `#64748B`, background `#F8FAFC`, bottom border 1px `#E2E8F0`. Includes sort indicators (arrows).
- **Body Rows:** 36px standard row height (compact 30px option for log viewing). Font size 13px.
- **Zebra Striping:** Alternating white (`#FFFFFF`) and slate-tinted rows (`#F8FAFC / 50%`).
- **Interactive State:** Hover row turns into `#F1F5F9`. Selected row adopts soft primary tint `#EFF6FF` with a 2px left accent border `#3B82F6`.
- **Alignment:** Numbers, ports, and timestamps right-aligned; text and identities left-aligned; status pills center-aligned.

### 5. Status Badges & Action Pills
- **Online / Active:** Background `#ECFDF5`, text `#065F46`, 1px border `#A7F3D0`, leading 6px glowing green indicator dot.
- **Muted / Inactive:** Background `#F1F5F9`, text `#475569`, 1px border `#E2E8F0`.
- **Banned / Terminated:** Background `#FEF2F2`, text `#991B1B`, 1px border `#FECACA`.
- **Warning / Throttled:** Background `#FFFBEB`, text `#92400E`, 1px border `#FDE68A`.

### 6. Inputs, Dropdowns & Controls
- **Text Inputs:** 32px height, 6px radius, 1px border `#CBD5E1`, background `#FFFFFF`. Focus ring: 2px `#93C5FD` with `#3B82F6` border.
- **Checkboxes & Radios:** 16px square/circle with 1px border `#CBD5E1`. Checked state fills `#3B82F6` with crisp white checkmark/dot.
- **Native-Style Dropdowns:** Inset chevron, hover highlight, 32px height. Dropdown flyout matches font size 13px with 4px inner item padding and keyboard navigation support.