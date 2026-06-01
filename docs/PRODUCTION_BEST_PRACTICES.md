Production Best Practices for RBAC and JWT

- Ensure all services validate JWT locally in addition to gateway checks (defense-in-depth).
- Use RS256 (asymmetric keys) for JWTs in production; rotate signing keys and publish JWKS.
- Do not embed sensitive data in JWT claims; include only subject and roles/permissions.
- Short-lived access tokens + refresh tokens pattern.
- Store roles in a normalized way if you expect multiple roles per user (join table `user_roles`).
- Hash passwords using BCrypt/Argon2 with adequate work factor; never store raw passwords in migrations.
- Enforce least privilege: apply `@PreAuthorize` narrowly and check resource ownership inside methods.
- Log authentication/authorization failures to a secure audit log and monitor via Prometheus/Grafana.
- Use TLS everywhere and secure inter-service calls with mTLS or signed tokens.
- Protect admin endpoints behind additional controls (IP allowlist, 2FA, separate admin network).
