# Contracts: [A9] Monthly Active Users Chart

## Action Contract: `ADMIN_GET_MONTHLY_ACTIVE_USERS`

### Request
```json
{
  "requestId": "req-act-chart-001",
  "action": "ADMIN_GET_MONTHLY_ACTIVE_USERS",
  "payload": {
    "year": 2026
  }
}
```

### Success Response
```json
{
  "requestId": "req-act-chart-001",
  "success": true,
  "payload": [
    { "year": 2026, "month": 1, "count": 42 },
    { "year": 2026, "month": 2, "count": 38 },
    { "year": 2026, "month": 3, "count": 55 },
    { "year": 2026, "month": 4, "count": 40 },
    { "year": 2026, "month": 5, "count": 62 },
    { "year": 2026, "month": 6, "count": 70 },
    { "year": 2026, "month": 7, "count": 65 },
    { "year": 2026, "month": 8, "count": 80 },
    { "year": 2026, "month": 9, "count": 75 },
    { "year": 2026, "month": 10, "count": 85 },
    { "year": 2026, "month": 11, "count": 90 },
    { "year": 2026, "month": 12, "count": 45 }
  ],
  "errorCode": null,
  "errorMessage": null
}
```

### Error Responses
- Non-admin: `UNAUTHORIZED` ("Admin privileges required")
- Unauthenticated: `UNAUTHENTICATED` ("Authentication required")
- Invalid year: `VALIDATION_FAILED` ("Year must be between 1970 and 2100")
