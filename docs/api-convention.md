# API Convention

## Versioning
- All public HTTP endpoints use version prefix: `/api/v1`.

## DTO location
For each business module under `modules/<module>`:
- Request DTOs: `modules/<module>/api/request`
- Response DTOs: `modules/<module>/api/response`

## Standard response envelope
- Use shared envelope: `common/response/ApiResponse`
- Controllers should return module-specific response data wrapped by `ApiResponse` for consistency across modules.
