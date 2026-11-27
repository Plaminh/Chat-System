# Requirements Checklist: [A6] Monthly Registration Chart

- [x] Action `ADMIN_GET_MONTHLY_REGISTRATIONS` defined in `ActionType`.
- [x] Request payload `YearRequest` validated (1970..2100).
- [x] Response payload `List<MonthlyCount>` has exactly 12 items (months 1..12).
- [x] Backend handler `AdminGetMonthlyRegistrationsHandler` checks Admin authorization.
- [x] Database query correctly groups by month in UTC+7 timezone.
- [x] Frontend `RegistrationChartPanel` displays JFreeChart bar chart with value labels.
- [x] Switching years updates chart and KPI summary cards dynamically.
- [x] Offline fallback preserves demo usability.
