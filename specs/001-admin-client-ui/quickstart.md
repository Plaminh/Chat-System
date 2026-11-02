# Quickstart & Verification Guide: Admin Client UI (Version 1)

**Feature**: `001-admin-client-ui`  
**Module**: `admin-client`  
**Date**: 2026-10-26

---

## 1. Prerequisites

- **JDK**: Java 21
- **Build Tool**: Apache Maven 3.9+
- **OS**: Windows Desktop environment (1280x800 or higher display resolution recommended)
- **Database / Server**: Not required for Version 1 UI verification (runs in self-contained Mock Data mode).

---

## 2. Build & Compile Verification

From the repository root (`D:\III_HK1\JavaApp\Project\Chat-System`):

```powershell
# Compile all modules and run unit tests
mvn clean test-compile

# Check code formatting compliance (Spotless + Google Java Format)
mvn spotless:check

# Package the shaded Admin Client JAR
mvn package -DskipTests
```

---

## 3. Running the Admin Client Application

Launch the Admin Desktop UI from the repository root:

```powershell
java -jar admin-client/target/admin.jar
```

Or run `com.chat.admin.AdminApplication` directly from your IDE.

---

## 4. End-to-End Manual Walkthrough (10 Screens)

1. **Login Dialog (`AdminLoginDialog`)**:
   - Application opens the centered login dialog (`Chat Admin Console`).
   - Click **Sign In** with default mock credentials (`admin` / `admin123`) to enter the main workspace.
2. **Main Frame & Navigation (`AdminMainFrame`)**:
   - Verify the dark slate left sidebar switches smoothly across all views using `CardLayout`.
3. **User Management (`UserManagementPanel`)**:
   - Test instant search, Status filter (`Active` / `Locked`), and Sort (`Name`, `Created Date`).
   - Test **Add User**, **Edit Profile**, **Delete User**, **Lock/Unlock**, and **Reset Password**.
4. **Login History & Friends (`UserDetailHistoryDialog`)**:
   - Select a user and click **View History & Friends** to inspect chronological login timestamps and direct/2nd-degree friend counts.
   - Verify toggle to view global chronological login history across all users.
5. **Group Management (`GroupManagementPanel`)**:
   - Filter groups by name, sort by name or creation date, and click rows to inspect the right-hand member roster with `Group Admin` and `Member` badges.
6. **Spam Reports (`SpamReportPanel`)**:
   - Filter spam reports by date range (`YYYY-MM-DD`) and username; sort by time or username; click **Lock Offending Account** and verify status changes to `Locked`.
7. **New Registrations (`NewRegistrationPanel`)**:
   - Enter start and end dates, filter by name, and sort by name or registration timestamp.
8. **Friend Statistics (`FriendStatsPanel`)**:
   - Test comparison operators (`=`, `>`, `<`) on direct friend count and sort by name or account creation date.
9. **User Activity (`UserActivityPanel`)**:
   - Filter by date range, name, and comparison operators (`=`, `>`, `<`) on activity count; sort by name or creation date.
10. **Visual Charts (`RegistrationChartPanel` & `ActivityChartPanel`)**:
    - Switch calendar years (`2024`, `2025`, `2026`) and verify both 12-month JFreeChart graphs update immediately.
