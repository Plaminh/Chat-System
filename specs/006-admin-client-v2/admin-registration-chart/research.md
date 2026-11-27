# Research & Architecture Decisions: [A6] Monthly Registration Chart

## Protocol Alignment
- `ActionType.ADMIN_GET_MONTHLY_REGISTRATIONS` is already defined in `common`.
- Request DTO: `com.chat.common.dto.common.YearRequest(int year)`
- Response DTO: `List<com.chat.common.dto.statistics.MonthlyCount>` (12 elements, months 1 to 12).
- Wire format:
  ```json
  [
    { "year": 2026, "month": 1, "count": 10 },
    ...,
    { "year": 2026, "month": 12, "count": 0 }
  ]
  ```

## Backend Database Query
- `UserRepository.countMonthlyRegistrations(int year)` is already implemented in `server`:
  ```sql
  SELECT extract(MONTH FROM created_at AT TIME ZONE :zone)::int AS month, count(*) AS total
  FROM users WHERE created_at >= :from AND created_at < :to GROUP BY 1
  ```
  Returns `long[12]`, which maps cleanly to 12 `MonthlyCount(year, month, count)` instances.

## Client Chart Rendering
- `RegistrationChartPanel` in `admin-client` uses `DefaultCategoryDataset` and `ChartFactory.createBarChart`.
- When user switches year dropdown, dispatch asynchronous request via `SocketClient.sendAsync`.
- On response arrival on Swing EDT:
  - If successful: update dataset with live counts, recalculate annual total, peak month, and monthly average.
  - If failure or offline: fallback to `AdminMockDataStore.getInstance().getMonthlyRegistrations(year)` with logging.
