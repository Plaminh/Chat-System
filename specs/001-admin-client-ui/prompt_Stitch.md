    Design a comprehensive, interconnected 10-screen desktop administrative application for a Java Desktop Chat
    System (Admin Client). The interface follows modern desktop UI guidelines (FlatLaf / IntelliJ / macOS clean
    desktop aesthetic) with a consistent, refined light theme, pixel-perfect spacing, and an intuitive user journey.
    
    ================================================================================
    1. GLOBAL DESIGN SYSTEM & UI TOKENS
    ================================================================================
    - Platform: Windows Desktop Application (1280x800 base resolution).
    - Color Palette:
      * Background Canvas: Neutral light slate (#F8FAFC)
      * Surface / Cards / Tables: Pure white (#FFFFFF)
      * Sidebar / Dark Navigation: Deep Slate (#0F172A)
      * Primary Accent: Indigo Blue (#3B82F6, hover #2563EB)
      * Success / Active Badge: Emerald (#10B981 on #ECFDF5)
      * Danger / Locked / Ban Badge: Rose Red (#EF4444 on #FEF2F2)
      * Warning / Pending: Amber (#F59E0B on #FFFBEB)
      * Borders & Dividers: Subtle slate (#E2E8F0)
      * Text: Primary dark slate (#1E293B), muted secondary slate (#64748B)
    - Typography: Inter or Roboto. Headers: 16-18px Semi-bold; Body / Table Text: 13px Regular; Badges & Captions:
    11-12px Medium.
    - Components: Sharp rectangular widgets with gentle 6px rounded corners, 1px crisp borders, subtle drop shadows
    on modals, zebra-striped data tables with hover highlights. Icons: Lucide / Feather icon set (16x16px).
    
    ================================================================================
    2. COMPLETE USER JOURNEY & NAVIGATION FLOW
    ================================================================================
    - [Flow 1: Gateway] Admin opens app -> Screen 1 (Login Dialog) -> Validates -> Enters Screen 2 (Main Workspace
    Shell).
    - [Flow 2: Directory & Modals] From Sidebar, Admin clicks "User Directory" (Screen 3). Selecting a user row and
    clicking "View History & Network" triggers Screen 4 (Modal Inspection Dialog with Login Logs and Friend
    Network).
    - [Flow 3: Community Oversight] Admin navigates to "Group Chats" (Screen 5) with nested member/admin inspection,
    then to "Spam Reports" (Screen 6) where reports can be triaged and offending accounts directly locked.
    - [Flow 4: Metrics & Deep Analytics] Admin reviews "New Registrations" (Screen 7 with date range pickers),
    evaluates "Friend Statistics" (Screen 8 with comparison filters = / > / <), and inspects "User Activity" (Screen
    9 with app open & chat counts).
    - [Flow 5: Visual Intelligence] Admin selects "Visual Analytics" (Screen 10) featuring two 12-month
    JFreeChart-style graphs for annual trends.
    
    ================================================================================
    3. DETAILED SPECIFICATION FOR THE 10 INTERFACES
    ================================================================================
    
    --- SCREEN 1: Admin Authentication Dialog (AdminLoginDialog) ---
    A focused modal dialog (size 420x340) centered on screen.
    - Header: Minimalist shield/lock icon, Title "Chat Admin Console", subtitle "Sign in to access system
    administration".
    - Form Body:
      * Username or Email input with user icon prefix.
      * Password input with eye toggle icon prefix.
      * Remember session checkbox & forgot credentials hint.
    - Action: Full-width primary button "Sign In to Admin Console".
    - Footer: Security watermark "Restricted Access - University Chat System v1.0".
    
    --- SCREEN 2: Main Workspace Shell & Navigation Architecture (AdminMainFrame) ---
    The foundational desktop container (1280x800) hosting all modular panels.
    - Left Sidebar (240px, Dark Slate #0F172A):
      * Top: App logo "ChatAdmin" with pulse status indicator.
      * User profile snippet: Admin avatar, "System Admin", badge "Online".
      * Nav Section 1 (Oversight): [👥 User Directory], [💬 Group Chats], [⚠️ Spam Reports (Badge: 3)].
      * Nav Section 2 (Analytics): [📅 New Registrations], [🌐 Friend Statistics], [📈 Activity Metrics], [📊 Visual
      Charts].
      * Bottom pinned: [⚙️ Settings] and [🚪 Sign Out] button.
    - Top Header Bar: Page title & breadcrumb, quick search bar (Ctrl+K), system date-time widget, notification
    bell.
    - Content Canvas: CardLayout view area dynamically loading Screens 3 to 10.
    
    --- SCREEN 3: User Directory & Management (UserManagementPanel) ---
    Primary account lifecycle panel.
    - Filter Toolbar:
      * Search field ('Search by username, full name, email...').
      * Status Filter dropdown: [All Statuses | Active | Locked].
      * Sort dropdown: [Name A-Z | Name Z-A | Newest First | Oldest First].
      * Buttons: [🔄 Refresh], [+ Add New User].
    - Main Data Table: Checkbox column, Username, Full Name, Email, Gender, Date of Birth, Address, Created Date,
    Status (pill badges: Green 'Active', Red 'Locked').
    - Bottom Floating Action Bar:
      * Quick Actions for selected row: [✏️ Edit Profile], [🔒 Lock / Unlock], [🔑 Reset Password], [👁️ View
      Details/History].
    - Pagination / Summary: "Showing 1-15 of 1,420 accounts".
    
    --- SCREEN 4: User Login Audit & Friend Network Modal (UserDetailHistoryDialog) ---
    Detailed inspection modal (680x540) opened from Screen 3.
    - User Header Banner: Avatar, Full Name, Username (@user01), Email, Status badge.
    - Tabbed Navigation:
      * Tab A [🕒 Login History]: Chronological table displaying Login Timestamp (YYYY-MM-DD HH:mm:ss), IP Address,
      Device / OS, Status ('Success').
      * Tab B [👥 Friend Network]: Two metric cards at top: "Direct Friends: 18" and "Extended Network
      (Friends-of-Friends): 142". Below is a scrollable list of direct friend profiles with avatars, names, and
      mutual friend count.
    - Modal Footer: [Close] button.
    
    --- SCREEN 5: Group Chat Oversight & Member Roles (GroupManagementPanel) ---
    Panel supervising group conversations.
    - Header Toolbar: Search group by name, Sort by [Creation Date | Member Count | Name].
    - Main Table: Group ID, Group Name, Created Date, Total Member Count, Admin Count, Actions.
    - Side/Inspector Drawer: Clicking a group reveals the "Member Roster":
      * Distinct list of members with role tags: Gold badge "Group Admin" vs slate badge "Member".
      * Group metadata summary (Creator username, creation timestamp).
    
    --- SCREEN 6: Spam Reports Moderation (SpamReportPanel) ---
    Abuse triage and community safety dashboard.
    - Filter Bar: Date Range Picker (From Date -> To Date), Search by Reported Username, Sort by [Newest Report |
    Username].
    - Incident Reports Table: Report ID, Reporting User (@alice), Offending User (@spammer99), Reason / Message
    Snippet, Report Timestamp, Status.
    - Action Panel:
      * Prominent Danger button: [🚫 Lock Offending Account] (immediately updates user to Locked in
      database/system).
      * Secondary button: [Dismiss Report].
    
    --- SCREEN 7: New Registrations Timeline (NewRegistrationPanel) ---
    Dedicated growth monitoring panel.
    - Header Controls:
      * Custom Date Range Picker: [Start Date: YYYY-MM-DD] to [End Date: YYYY-MM-DD], [Apply Filter] button.
      * Name / Keyword search input.
      * Quick filter chips: [Today | Last 7 Days | This Month | This Year].
    - Summary Metric Card: "Total Registrations in Selected Period: 84 accounts".
    - Results Table: Username, Full Name, Email, Gender, Registration Timestamp, Initial Status.
    
    --- SCREEN 8: Friend Network Analytics (FriendStatsPanel) ---
    Relational density and connection analysis panel.
    - Filter Controls:
      * Search user by name.
      * Direct Friends Filter: Operator dropdown [ = | > | < ] + Number input field (e.g. "> 10") + [Filter] button.
      * Sort options: Sort by Name or Account Age.
    - Analytics Table: Username, Full Name, Direct Friends Count, Friends-of-Friends Count (2nd-degree network),
    Ratio / Network Reach Score.
    
    --- SCREEN 9: User Engagement & Activity Analytics (UserActivityPanel) ---
    Platform usage and chat volume panel.
    - Filter Controls:
      * Date Interval: [From Date] to [To Date].
      * Activity Volume Filter: Operator dropdown [ = | > | < ] + Activity count threshold.
      * Search by user name.
    - Engagement Table: Username, Full Name, App Open / Launch Count, Chatted Individuals Count, Chatted Group
    Conversations Count, Overall Engagement Status.
    
    --- SCREEN 10: Visual Analytics & Trend Charts (AnalyticsChartsPanel) ---
    High-level visual intelligence dashboard representing JFreeChart components.
    - Top Control Bar: Year Selector dropdown (e.g., '2026', '2025', '2024'), [Refresh Charts] button.
    - Grid Layout (2 side-by-side or stacked chart panels):
      * Chart Panel 1 [New Registrations per Month]: Vertical Bar Chart showing Month 1 to Month 12 on X-axis and
      New User Count on Y-axis. Clear axis labels, gridlines, and value tooltips.
      * Chart Panel 2 [Active Users per Month]: Smooth Line Chart with highlighted data points showing Month 1 to
      Month 12 on X-axis and Total App Openers on Y-axis.
    - Bottom KPI summary: "Peak Month: August (180 registrations) | Annual Active Rate: 82%".