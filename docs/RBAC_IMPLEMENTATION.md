RBAC Implementation Summary

Services changed:
- `user-service`: persisted `role` on `users` table, included `roles` claim in JWT, added OpenAPI and method-level annotations.
- `api-gateway`: JWT reactive auth extracts `roles` claim and maps to `ROLE_*`; path matchers enforce role access.
- `appointment-service` / `report-service`: added `@PreAuthorize` annotations on controller methods.

Database migrations (user-service):
- `src/main/resources/db/migration/V1__create_users_table.sql` — creates `users` table with `role` VARCHAR
- `src/main/resources/db/migration/V2__insert_sample_admin.sql` — inserts a sample admin user (placeholder hash)

Notes & next steps:
- Replace the placeholder BCrypt hash in `V2__insert_sample_admin.sql` before production, or create admin via registration endpoint.
- For defense-in-depth, add local JWT validation filters in each downstream service (recommended).

Swagger testing examples:
- Open Swagger UI for each service (e.g., `http://localhost:8080/swagger-ui.html`) and use "Authorize" to paste a JWT (Bearer token).
- Example curl for admin-only endpoint:

  curl -H "Authorization: Bearer <JWT>" http://localhost:8081/users/1

Production best practices and interview Q&A are documented in companion files.
