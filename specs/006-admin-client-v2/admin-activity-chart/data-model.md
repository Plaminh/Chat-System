# Data Model: [A9] Monthly Active Users Chart

## Wire DTOs (Module: `common`)

### Request: `YearRequest`
```java
package com.chat.common.dto.common;

public record YearRequest(int year) {
  public YearRequest {
    if (year < 1970 || year > 2100) {
      throw new IllegalArgumentException("Year must be between 1970 and 2100");
    }
  }
}
```

### Response: `List<MonthlyCount>`
```java
package com.chat.common.dto.statistics;

public record MonthlyCount(int year, int month, long count) {
  public MonthlyCount {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("Month must be 1..12");
    }
    if (count < 0) {
      throw new IllegalArgumentException("Count cannot be negative");
    }
  }
}
```

## Database Schema (`user_activity_logs` table)
- Columns:
  - `user_id BIGINT NOT NULL`
  - `activity_type VARCHAR(32) NOT NULL` (`'app_open'`)
  - `occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP`
- Filter: `activity_type = 'app_open' AND occurred_at >= :from AND occurred_at < :to`.
