# Data Model: [A6] Monthly Registration Chart

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

## Database Schema (`users` table)
- Column: `created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP`
- Filter: `created_at >= :from AND created_at < :to` (where from = `year-01-01T00:00:00+07:00`, to = `(year+1)-01-01T00:00:00+07:00`).
