# Requirements Checklist: [A9] Monthly Active Users Chart

- [x] Action `ADMIN_GET_MONTHLY_ACTIVE_USERS` defined in `ActionType`.
- [x] Request payload `YearRequest` validated (1970..2100).
- [x] Response payload `List<MonthlyCount>` has exactly 12 items (months 1..12).
- [x] Backend handler `AdminGetMonthlyActiveUsersHandler` checks Admin authorization.
- [x] Database query correctly aggregates distinct users where `activity_type = 'app_open'`.
- [x] Frontend `ActivityChartPanel` displays JFreeChart line chart with shapes and value labels.
- [x] Switching years updates chart and KPI summary cards dynamically.
- [x] Offline fallback preserves demo usability.
