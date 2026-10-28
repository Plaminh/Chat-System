---
name: Trò Chuyện Hiện Đại
colors:
  surface: '#f8f9ff'
  surface-dim: '#d0dbed'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e6eeff'
  surface-container-high: '#dee9fc'
  surface-container-highest: '#d9e3f6'
  on-surface: '#121c2a'
  on-surface-variant: '#464555'
  inverse-surface: '#27313f'
  inverse-on-surface: '#eaf1ff'
  outline: '#777587'
  outline-variant: '#c7c4d8'
  surface-tint: '#4d44e3'
  primary: '#3525cd'
  on-primary: '#ffffff'
  primary-container: '#4f46e5'
  on-primary-container: '#dad7ff'
  inverse-primary: '#c3c0ff'
  secondary: '#505f76'
  on-secondary: '#ffffff'
  secondary-container: '#d0e1fb'
  on-secondary-container: '#54647a'
  tertiary: '#7e3000'
  on-tertiary: '#ffffff'
  tertiary-container: '#a44100'
  on-tertiary-container: '#ffd2be'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e2dfff'
  primary-fixed-dim: '#c3c0ff'
  on-primary-fixed: '#0f0069'
  on-primary-fixed-variant: '#3323cc'
  secondary-fixed: '#d3e4fe'
  secondary-fixed-dim: '#b7c8e1'
  on-secondary-fixed: '#0b1c30'
  on-secondary-fixed-variant: '#38485d'
  tertiary-fixed: '#ffdbcc'
  tertiary-fixed-dim: '#ffb695'
  on-tertiary-fixed: '#351000'
  on-tertiary-fixed-variant: '#7b2f00'
  background: '#f8f9ff'
  on-background: '#121c2a'
  surface-variant: '#d9e3f6'
  canvas-bg: '#F5F7FB'
  surface-card: '#FFFFFF'
  border-subtle: '#E2E8F0'
  status-online: '#16A34A'
  status-pending: '#D97706'
  status-destructive: '#DC2626'
  bubble-received: '#F1F5F9'
  bubble-sent: '#4F46E5'
typography:
  headline-xl:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
  body-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-md:
    fontFamily: Be Vietnam Pro
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  caption:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes a warm, focused, and dependable desktop workspace crafted specifically for native Vietnamese communication patterns. Balancing the high productivity of desktop messaging with an approachable, calm presence, the design language strips away visual clutter to elevate conversational text and community connection.

### Target Audience & Tone
Engineered for modern desktop users in Vietnam—knowledge workers, creative teams, and social circles—who require continuous, fatigue-free communication during long desktop sessions. The interface communicates reassurance, immediate clarity, and considerate restraint. Vietnamese diacritics and complex character stacking are treated as core typographic structural elements rather than edge cases.

### Aesthetic Paradigm: Modern Tactile Minimalism
The system adopts **Corporate / Modern Minimalism** with subtle tactile micro-details:
- **Calm Neutral Architecture:** Background canvas shifts to a soft, cool neutral (`#F5F7FB`) that cradles crisp, white card surfaces (`#FFFFFF`), avoiding harsh contrast fatigue.
- **Focused Accents:** An assertive indigo-blue tone (`#4F46E5`) steers primary momentum, while purposeful contextual colors indicate presence, alerts, and destructive actions.
- **Structural Legibility:** Defined by clean linear separations (`#E2E8F0`), generous breathing room built on an 8px scale, and subtle soft-shadow depth.
- **Iconic Language:** Crisp, 1.75px to 2px stroke Lucide-style line iconography maintains balance with Vietnamese sans-serif metrics.

## Colors

The palette is tuned for long desktop engagement, balancing high contrast legibility with soft, low-fatigue structural tones. 

### Core Palette Hierarchy
- **Canvas Base (`#F5F7FB`):** Ambient background canvas across all workspace frames and panel gutters.
- **Surface Elevation (`#FFFFFF`):** High-clarity foreground panels, message viewports, cards, and modal dialogs.
- **Primary Brand (`#4F46E5`):** Reserved for primary interactive controls, user-sent message bubbles, active navigation pills, and critical affirmative actions.
- **Dark Slate Body (`#1F2937`):** Primary ink for headings, active conversation titles, and message body copy. Guarantees WCAG AAA contrast against white and light neutral bases.
- **Muted Slate (`#64748B`):** Secondary ink for timestamps, subtle metadata, placeholder copy, offline states, and unselected navigation icons.
- **Subtle Stroke (`#E2E8F0`):** Single-pixel perimeter rules separating navigation rails, conversation master lists, and detail inspectors.

### Functional & Presence Indicators
- **Online Presence (`#16A34A`):** Vivid emerald green signifying active real-time status.
- **Pending & Warning (`#D97706`):** Amber accent for unread badge counters, verification states, and pending friend invitations.
- **Destructive Action (`#DC2626`):** Precision red for block user ("Chặn"), unsend ("Thu hồi tin nhắn"), and delete conversation actions.

## Typography

The type system standardizes on **Be Vietnam Pro**, engineered explicitly to handle the complex diacritics of the Vietnamese alphabet without awkward vertical clipping, baseline drift, or line-height distortion.

### Rhythm & Vertical Metrics
Vietnamese tone marks (hỏi, ngã, sắc, huyền, nặng) and vowel modifiers (ơ, ư, ê, ô) require generous line-height ratios. All standard body line heights are configured at 1.45x–1.6x the font size to prevent overlapping diacritical glyphs across dense multi-line chat bubbles.

### Typographic Roles
- **Headlines (xl, lg, md):** Used for view headers ("Đoạn chat", "Danh sách bạn bè"), modal titles, and conversation partner names.
- **Body Text (lg, md, sm):** `body-md` (14px/22px) serves as the default text size for user chat bubbles and input fields. `body-sm` (13px/18px) governs conversation list previews and system notes.
- **Labels & Captions:** `label-md` and `label-sm` govern buttons, segmented tabs, and avatar badge counters. `caption` handles message delivery timestamps ("10:42", "Đã gửi").

## Layout & Spacing

The desktop application conforms strictly to a 1440 × 900 reference frame, scalable down to 1024 × 768 and adaptable to widescreen monitors.

### Three-Pane Structural Layout
1. **Primary Navigation Rail (Width: 72px fixed):** Anchored to the far left. Hosts system logo, view anchors (Tin nhắn, Bạn bè, Lời mời kết bạn, Nhóm, Cài đặt), user avatar profile chip, and exit affordance.
2. **Master List / Directory Column (Width: 340px fixed):** Contains search fields, status tabs, and scrollable virtual lists (conversations, friends, or search results).
3. **Detail & Thread Workspace (Flex: remaining viewport, min-width: 580px):** Active conversation viewport, pinned conversation header, message bubble stream, composer tray, and collapsible context inspector panel (300px width when open).

### Spacing Scale & Rhythm
Spacing adheres strictly to an 8px grid (using an occasional 4px half-step for micro-elements like badge offsets and tight icon pairings):
- **Micro Gaps (4px / `space-xs`):** Gap between inline icons and text labels; avatar online status offset.
- **Compact Padding (8px / `space-sm`):** Internal padding for small buttons, message bubble vertical margins, and list item spacing.
- **Standard Padding (16px / `space-md`):** Input field internal horizontal padding, conversation list item container padding, and composer outer margins.
- **Generous Insets (24px / `space-lg`):** Modal dialog padding, profile settings cards, and empty state containers.

## Elevation & Depth

Visual hierarchy uses a refined combination of flat structural borders and soft, diffused atmospheric shadows. 

### Elevation Levels
- **Level 0 (Flat Canvas):** Neutral canvas (`#F5F7FB`) with single-pixel border dividers (`1px solid #E2E8F0`). Used across column splitters and conversation headers.
- **Level 1 (Surface Cards & Floating List Items):** Message cards, hovered conversation rows, and active list elements.
  - Shadow: `0 1px 3px 0 rgba(15, 23, 42, 0.05), 0 1px 2px -1px rgba(15, 23, 42, 0.05)`
- **Level 2 (Popovers, Tooltips & Context Menus):** Action menus ("Thu hồi tin nhắn", "Xóa với tôi", "Chuyển tiếp"), emoji pickers, and writing assistant trays.
  - Shadow: `0 4px 6px -1px rgba(15, 23, 42, 0.08), 0 2px 4px -2px rgba(15, 23, 42, 0.05)`
  - Border: `1px solid #E2E8F0`
- **Level 3 (Modal Dialogs & Overlays):** Destructive confirmations, group setup dialogs, and spam reports.
  - Backdrop: `rgba(15, 23, 42, 0.40)` with `backdrop-filter: blur(4px)`
  - Shadow: `0 20px 25px -5px rgba(15, 23, 42, 0.12), 0 8px 10px -6px rgba(15, 23, 42, 0.08)`

## Shapes

The interface embraces a balanced, friendly geometric radius scale (Level 2 - Rounded) that softens technical density without appearing juvenile.

### Shape Geometry Standards
- **Buttons, Inputs & Card Surfaces:** Standard `0.5rem` (8px) corner radius. Provides sharp alignment across dense forms.
- **Message Bubbles:** Asymmetric rounding:
  - **Incoming Bubble:** `16px` top-left, `16px` top-right, `16px` bottom-right, `4px` bottom-left anchor.
  - **Outgoing Bubble:** `16px` top-left, `16px` top-right, `4px` bottom-right anchor, `16px` bottom-left.
- **Pills, Badges & Avatars:** Fully rounded circular geometry (`9999px`) for presence indicators, unread count badges, and user avatar cutouts.

## Components

### Buttons
- **Primary:** Background `#4F46E5`, text `#FFFFFF`, font `label-md`. Hover background `#4338CA`. Active scale `0.98`. Focus ring `2px #4F46E5` with `2px offset`.
- **Secondary / Neutral:** Background `#FFFFFF`, border `1px solid #E2E8F0`, text `#1F2937`. Hover background `#F8FAFC`, border `#CBD5E1`.
- **Destructive:** Background `#DC2626`, text `#FFFFFF`. Hover background `#B91C1C`.
- **Ghost:** Transparent background, text `#64748B`. Hover background `#F1F5F9`, text `#1F2937`.

### Message Bubbles
- **Sent Bubble:** Background `#4F46E5`, text `#FFFFFF`. Hyperlinks underlined in `#C7D2FE`. Timestamp `#E0E7FF` (caption).
- **Received Bubble:** Background `#F1F5F9`, text `#1F2937`, border `1px solid #E2E8F0`. Timestamp `#64748B` (caption).
- **Max Width:** 65% of active conversation panel width to preserve comfortable line length.

### Text Inputs & Search Fields
- Base background `#FFFFFF`, border `1px solid #E2E8F0`, radius `8px`, typography `body-md`.
- Focus state: Border `#4F46E5`, box-shadow `0 0 0 3px rgba(79, 70, 229, 0.15)`.
- Global search includes a leading search icon (16px, `#94A3B8`) and a clear trailing escape icon.

### Avatars & Status Dots
- Standard sizes: 48px (conversation header/inspector), 40px (list rows), 32px (thread messages), 24px (compact mentions).
- Online Indicator: 10px circular badge (`#16A34A`), bordered by a 2px solid `#FFFFFF` mask, positioned bottom-right at 45 degrees.

### Modal Dialogs (Confirmation, Group Setup, Spam Report)
- Fixed widths: 440px (standard prompts) or 520px (multi-step group creator).
- Pinned bottom actions: Right-aligned secondary cancel ("Hủy") and primary confirmation button ("Xác nhận", "Gửi báo cáo", "Tạo nhóm").

### Writing Assistant Drawer ("Trợ Lý Soạn Thảo")
- Positioned directly above the composer input area.
- Card wrapper with subtle indigo glow (`box-shadow: 0 4px 12px rgba(79, 70, 229, 0.08)`).
- Two-column split comparing original user draft ("Bản nháp của bạn") with refined suggestion ("Gợi ý chỉnh sửa").
- Clear action chips: "Chèn vào ô soạn", "Thử lại", and "Đóng".