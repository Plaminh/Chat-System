# Data Model (Version 1 Mock UI Models): Admin Client UI

**Feature**: `001-admin-client-ui`  
**Package**: `com.chat.admin.model`  
**Date**: 2026-10-26

> **Note**: In accordance with Constitution Principle V and Version 1 scope boundaries, these models reside in `admin-client` (`com.chat.admin.model`) to power the Swing tables, filters, dialogs, and JFreeChart datasets using in-memory mock data. No database or network persistence is invoked in Version 1.

---

## 1. Entities & Fields

### 1.1 `AdminUser` (User Profile & Statistics Record)
Represents a platform user account across User Management (Screen 3), New Registrations (Screen 7), Friend Statistics (Screen 8), and User Activity (Screen 9).

| Field | Type | Constraints & Validation Rules |
| :--- | :--- | :--- |
| `id` | `String` | Non-null, unique identifier (e.g., `"USR-1001"`). |
| `username` | `String` | Required, non-blank, unique, 3–30 characters, alphanumeric/underscore. |
| `fullName` | `String` | Required, non-blank, 2–100 characters. |
| `email` | `String` | Required, non-blank, must contain `@` and domain. |
| `address` | `String` | Required, non-blank (e.g., `"Quận 1, TP.HCM"`). |
| `dateOfBirth` | `LocalDate` | Required, must be in the past (`< LocalDate.now()`). |
| `gender` | `String` | Required, one of `"Male"`, `"Female"`, `"Other"`. |
| `createdAt` | `LocalDateTime` | Required, account registration timestamp (`YYYY-MM-DD HH:mm`). |
| `locked` | `boolean` | `false` = `"Active"`, `true` = `"Locked"`. |
| `directFriendCount` | `int` | Non-negative (`>= 0`). Direct (1st-degree) friends count. |
| `friendsOfFriendsCount` | `int` | Non-negative (`>= 0`). Extended (2nd-degree) friends network count. |
| `appOpenCount` | `int` | Non-negative (`>= 0`). Number of times user opened the app in period. |
| `chattedUsersCount` | `int` | Non-negative (`>= 0`). Number of distinct individuals chatted with. |
| `chattedGroupsCount` | `int` | Non-negative (`>= 0`). Number of distinct groups chatted in. |

**State Transitions**:
- `Active (locked = false)` ⇄ `Locked (locked = true)` via Lock/Unlock toggle in `UserManagementPanel` or Lock Offending Account in `SpamReportPanel`.

---

### 1.2 `LoginRecord` (Login History Entry)
Represents a chronological login event displayed in `UserDetailHistoryDialog` (Screen 4) and global login history views (Requirement 1.2.2).

| Field | Type | Constraints & Validation Rules |
| :--- | :--- | :--- |
| `timestamp` | `LocalDateTime` | Required, formatted `YYYY-MM-DD HH:mm:ss`, ordered descending/chronologically. |
| `username` | `String` | Required, matches `AdminUser.username`. |
| `fullName` | `String` | Required, matches `AdminUser.fullName`. |

---

### 1.3 `FriendConnection` (Direct Friend Item)
Represents a friend entry inside `UserDetailHistoryDialog` (Screen 4) Friend Network tab.

| Field | Type | Constraints & Validation Rules |
| :--- | :--- | :--- |
| `friendUsername` | `String` | Required, non-blank. |
| `friendFullName` | `String` | Required, non-blank. |
| `mutualFriendsCount` | `int` | Non-negative (`>= 0`). |
| `online` | `boolean` | Online/Offline indicator badge. |

---

### 1.4 `GroupChatInfo` & `GroupMemberInfo` (Group Oversight Entities)
Represents a group conversation and its member roster in `GroupManagementPanel` (Screen 5).

**`GroupChatInfo`**:
| Field | Type | Constraints & Validation Rules |
| :--- | :--- | :--- |
| `groupId` | `String` | Required, unique (e.g., `"GRP-001"`). |
| `groupName` | `String` | Required, non-blank. |
| `createdAt` | `LocalDate` | Required, group creation date. |
| `members` | `List<GroupMemberInfo>` | Non-null list of group members (at least 1 admin). |

**`GroupMemberInfo`**:
| Field | Type | Constraints & Validation Rules |
| :--- | :--- | :--- |
| `username` | `String` | Required, non-blank. |
| `fullName` | `String` | Required, non-blank. |
| `groupAdmin` | `boolean` | `true` = `"Group Admin"` role badge, `false` = `"Member"` role badge. |

---

### 1.5 `SpamReportRecord` (Spam Moderation Incident)
Represents a user-submitted spam report in `SpamReportPanel` (Screen 6).

| Field | Type | Constraints & Validation Rules |
| :--- | :--- | :--- |
| `reportId` | `String` | Required, unique (e.g., `"REP-8041"`). |
| `reporterUsername` | `String` | Required, username of reporting user. |
| `reportedUsername` | `String` | Required, username of offending user. |
| `reason` | `String` | Required, description/snippet of spam violation. |
| `reportedAt` | `LocalDateTime` | Required, report timestamp. |
| `status` | `String` | `"Pending"`, `"Account Locked"`, or `"Dismissed"`. |

---

### 1.6 `MonthlyMetricSeries` (Annual Chart Dataset)
Represents 12-month integer counts (Months 1–12) keyed by calendar year (`2024`, `2025`, `2026`) for `RegistrationChartPanel` and `ActivityChartPanel` (Screen 10).

| Field | Type | Constraints & Validation Rules |
| :--- | :--- | :--- |
| `year` | `int` | Valid calendar year (e.g., `2024`, `2025`, `2026`). |
| `monthlyRegistrations` | `int[12]` | Array of length 12, each value `>= 0` (index 0 = Jan, 11 = Dec). |
| `monthlyActiveUsers` | `int[12]` | Array of length 12, each value `>= 0` (index 0 = Jan, 11 = Dec). |

---

## 2. Filter & Validation Rules Summary

1. **Date Range Validation** (`NewRegistrationPanel`, `SpamReportPanel`, `UserActivityPanel`):
   - Format: `YYYY-MM-DD`.
   - Start date MUST be `<=` End date. If `startDate.isAfter(endDate)` or format is invalid, show a clear validation warning dialog/banner and do not apply invalid bounds.
2. **Comparison Operator Validation** (`FriendStatsPanel`, `UserActivityPanel`):
   - Operators: `"All"`, `"="`, `">"`, `"<"`.
   - Threshold input: Must parse as a non-negative integer (`>= 0`) when an operator (`=`, `>`, `<`) is active.
3. **User Form Validation** (`UserManagementPanel` Add/Edit/Reset Password dialogs):
   - Username, Full Name, Email, Address, Date of Birth (`YYYY-MM-DD`), and Gender cannot be empty.
   - Password reset requires non-empty new password (minimum 6 characters).
