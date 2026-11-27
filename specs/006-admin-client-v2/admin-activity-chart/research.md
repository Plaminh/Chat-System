# Research & Architecture Decisions: [A9] Monthly Active Users Chart

## Protocol Alignment
- `ActionType.ADMIN_GET_MONTHLY_ACTIVE_USERS` is already defined in `common`.
- Request DTO: `com.chat.common.dto.common.YearRequest(int year)`
- Response DTO: `List<com.chat.common.dto.statistics.MonthlyCount>` (12 elements, months 1 to 12).
- Wire format:
  ```json
  [
    { "year": 2026, "month": 1, "count": 15 },
    ...,
    { "year": 2026, "month": 12, "count": 0 }
  ]
  ```

## Backend Database Query
- `UserActivityLogRepository.countMonthlyActiveUsers(int year)` is already implemented in `server`:
  ```sql
  SELECT extract(MONTH FROM occurred_at AT TIME ZONE :zone)::int AS month,
         count(DISTINCT user_id) AS total
  FROM user_activity_logs
  WHERE activity_type = 'app_open' AND occurred_at >= :from AND occurred_at < :to
  GROUP BY 1
  ```
  Returns `long[12]`, mapping directly to 12 `MonthlyCount(year, month, count)` instances.
- Crucially, `count(DISTINCT user_id)` guarantees that a user who opens the app multiple times in a month is counted as exactly 1 active user for that month.

## Client Line Chart Rendering
- `ActivityChartPanel` in `admin-client` uses `DefaultCategoryDataset` and `ChartFactory.createLineChart`.
- Configured with `LineAndShapeRenderer`: `#10B981` line color, circular point shapes, and integer value labels above points.
- Connects asynchronously via `SocketClient.sendAsync`.
- On response arrival on Swing EDT:
  - If successful: update dataset with live counts, recalculate Annual Total, Peak Month, and Monthly Average.
  - If failure or offline: fallback to `AdminMockDataStore.getInstance().getMonthlyActiveUsers(year)` with logging.
