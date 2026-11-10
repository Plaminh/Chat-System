# Screenshot audit V1

Audit date: 2026-11-10. Branch: `feature/common-v2`.
Scope: running Swing screenshots under `docs/screenshots/admin/` and `docs/screenshots/user/`. Outer desktop, parent-window strips, and drop shadow were cropped from the new and replaced files only. UI content inside each window was not edited.

## A. Summary

| Item | Count |
| --- | ---: |
| Screenshots audited (`admin/` + `user/`) | 30 |
| Cropped | 12 |
| Kept unchanged | 17 |
| Excluded from the report | 13 |

The 13 excluded images are `docs/screenshots/admin/03_user_management_panel.png` plus 12 design comps in `docs/mockups/`. They were inspected only to confirm they must not be cited.

All 12 cropped PNGs reopen, keep their title bars, actions, tables, charts, and proof text, and no longer show a desktop strip outside the window.

## B. Cropped files

- `docs/screenshots/admin/01_main_frame_and_user_management.png` — 1447×793
- `docs/screenshots/admin/10_registration_and_activity_charts.png` — 1273×793
- `docs/screenshots/admin/11_user_form.png` — 704×553
- `docs/screenshots/admin/12_reset_password.png` — 426×161
- `docs/screenshots/admin/13_friend_network.png` — 1086×673
- `docs/screenshots/user/03_forgot_password.png` — 506×354
- `docs/screenshots/user/10_group_chat.png` — 1273×793
- `docs/screenshots/user/13_change_password.png` — 466×383
- `docs/screenshots/user/14_find_people.png` — 1493×793
- `docs/screenshots/user/15_online_friends_create_group.png` — 1273×793
- `docs/screenshots/user/16_create_group.png` — 546×593
- `docs/screenshots/user/17_group_members.png` — 886×513

Unchanged evidence: admin `02`, `04`, `05`, `06`, `07`, `08`, `09`; user `01`, `02`, `04`, `05`, `06`, `07`, `08`, `09`, `11`, `12_writting_agent.png`.

## C. Coverage

### Admin — 9 groups

| ID | Requirement | Evidence | Result |
| --- | --- | --- | --- |
| ADMIN-01 | User directory: list, filter, sort, selected row, management actions | `admin/01_main_frame_and_user_management.png` | FULL |
| ADMIN-01 form | Add / edit user form | `admin/11_user_form.png` | FULL |
| ADMIN-01 reset | Reset-password dialog | `admin/12_reset_password.png` | FULL |
| ADMIN-01 network | Friend network of the selected user | `admin/13_friend_network.png` | FULL |
| ADMIN-02 | Login history | `admin/04_user_detail_history_dialog.png` | FULL |
| ADMIN-03 | Group oversight | `admin/05_group_management_panel.png` | FULL |
| ADMIN-04 | Spam reports | `admin/06_spam_report_panel.png` | FULL |
| ADMIN-05 | New registrations | `admin/07_new_registration_panel.png` | FULL |
| ADMIN-06 | 12-month registration chart | `admin/10_registration_and_activity_charts.png` | FULL |
| ADMIN-07 | Friend statistics | `admin/08_friend_stats_panel.png` | FULL |
| ADMIN-08 | User activity | `admin/09_user_activity_panel.png` | FULL |
| ADMIN-09 | 12-month distinct-active-users chart | `admin/10_registration_and_activity_charts.png` | FULL |

Admin sign-in is also covered by `admin/02_admin_login_dialog.png` (FULL). No admin group is MISSING.

Checklist confirmation for the new and replaced admin shots:

- `01`: user table, filter, status filter, sort, selected `@alex_turner`, Edit Profile, Lock Account, Reset Password, Deactivate User, View History & Network.
- `11`: Edit User Profile with username, full name, email, date of birth, gender, address, Cancel, Save Changes.
- `12`: Update / Reset User Password, one password field, OK, Cancel.
- `13`: Friend Network & Connections, columns friend username, full name, mutual friends, presence status.
- `10`: both 2025 charts, months Jan–Dec, registration and distinct-active-user axes.

### User — 12 groups

| ID | Requirement | Evidence | Result |
| --- | --- | --- | --- |
| USER-01 | Sign in | `user/01_user_login.png` | FULL |
| USER-02 | Registration | `user/02_registration.png` | FULL |
| USER-03 | Password recovery | `user/03_forgot_password.png` | FULL |
| USER-04 | Change password | `user/13_change_password.png` | FULL |
| USER-05 | Profile | `user/04_user_account.png` | FULL |
| USER-06 | Direct chat | `user/07_chat.png` | FULL |
| USER-07 | History search and remove chat | `user/08_search_history_and_remove_chat.png` | FULL |
| USER-08 | Online friends and create-group entry | `user/15_online_friends_create_group.png` | FULL |
| USER-09 | Find people and friend requests | `user/14_find_people.png`, `user/06_friend_request_and_search.png` | FULL |
| USER-10 | Spam report | `user/11_spam_report.png` | FULL |
| USER-11 | Writing assistant | `user/12_writting_agent.png` | FULL |
| USER-12 | Groups: create, members/roles, encrypted prototype chat | `user/16_create_group.png`, `user/17_group_members.png`, `user/10_group_chat.png`, `user/09_groups.png` | FULL |

`user/05_friend_lists.png` remains supporting evidence for the friend list. No user group is MISSING.

Checklist confirmation for the new and replaced user shots:

- `03`: email, future-tense recovery notice, Request Temporary Password, Back to Sign In.
- `13`: Current password, New password, Confirm new password, Cancel, Confirm Sample Change.
- `14`: search `quang`, result Võ Quang, Request Received, Message, Add to Group.
- `15`: Show online only, two online friends, Create Group, Message, Remove Friend, Block.
- `16`: group name, Standard Group and Encrypted Group, selected member, Create Sample Group.
- `10`: Encrypted Study Circle, Encrypted Group (Prototype), V1 display disclaimer, sample message, Send Sample.
- `17`: Alex Morgan ADMIN, Trần Minh MEMBER, Remove Admin Role, Make Admin, Remove from Group, Add Member.

## D. Exclusions

- `docs/screenshots/admin/03_user_management_panel.png` is a superseded duplicate of the old user-directory shot. Do not use it in the report.
- Every image under `docs/mockups/` is a design comp, not a capture of the running client. Do not use those 12 files as evidence.

## E. Result

`READY FOR REPORT WITH WARNINGS`

Warnings:

- Cite only the files in section C. The excluded duplicate and the mockups are still in the tree.
- Friend Network shows sample KPI text “18 Users” while the table lists 6 friends. The columns and rows are the evidence; the two numbers come from the sample data.
- Find People proves search plus Request Received, Message, and Add to Group. Create Group is on the Friends screen, not on that search row.
- Reset password is the real one-field dialog with OK and Cancel.
- A few preserved sidebar labels still truncate “Online Session”. Those files were not recropped.
