# Contracts: [A6] Monthly Registration Chart

## Action Contract: `ADMIN_GET_MONTHLY_REGISTRATIONS`

### Request
```json
{
  "requestId": "req-chart-001",
  "action": "ADMIN_GET_MONTHLY_REGISTRATIONS",
  "payload": {
    "year": 2026
  }
}
```

### Success Response
```json
{
  "requestId": "req-chart-001",
  "success": true,
  "payload": [
    { "year": 2026, "month": 1, "count": 25 },
    { "year": 2026, "month": 2, "count": 18 },
    { "year": 2026, "month": 3, "count": 30 },
    { "year": 2026, "month": 4, "count": 15 },
    { "year": 2026, "month": 5, "count": 22 },
    { "year": 2026, "month": 6, "count": 40 },
    { "year": 2026, "month": 7, "count": 35 },
    { "year": 2026, "month": 8, "count": 50 },
    { "year": 2026, "month": 9, "count": 28 },
    { "year": 2026, "month": 10, "count": 32 },
    { "year": 2026, "month": 11, "count": 45 },
    { "year": 2026, "month": 12, "count": 10 }
  ],
  "errorCode": null,
  "errorMessage": null
}
```

### Error Responses
- Non-admin: `UNAUTHORIZED` ("Admin privileges required")
- Unauthenticated: `UNAUTHENTICATED` ("Authentication required")
- Invalid year: `VALIDATION_FAILED` ("Year must be between 1970 and 2100")
