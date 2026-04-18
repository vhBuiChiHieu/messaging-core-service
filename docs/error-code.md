# Error Handling Convention

## Current response contract
- Successful responses use `ApiResponse<T>` with `success=true`, `message=null`, and `data=<payload>`.
- Validation failures return `ApiResponse<Void>` with `success=false`, `message=<validation details>`, and `data=null`.

## Practical note
- This base template does not expose a dedicated `code` field yet.
- If a service needs business error codes later, update both the response contract and this document together.
