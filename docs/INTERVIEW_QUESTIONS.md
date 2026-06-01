Interview Questions — RBAC, JWT, and Gateway Security

1) Q: How do you store roles for a user in a relational database? A: Use a normalized `roles` table and a join table `user_roles` for many-to-many. If single role per user is sufficient, an ENUM or VARCHAR column may be used.

2) Q: Should the gateway or services validate JWTs? A: Both: gateway for centralized auth and routing; services should also validate tokens for defense-in-depth.

3) Q: How do you include roles in JWTs? A: Add a `roles` claim containing either a list of role strings or a space/comma-separated string. Sign the token securely.

4) Q: When should you use role-based vs permission-based access control? A: Use RBAC for coarse-grained access; switch to permission/attribute-based for fine-grained, per-resource control.

5) Q: How do you implement method-level security in Spring? A: Enable `@EnableMethodSecurity` and use `@PreAuthorize` with Spring Expression Language (SpEL) to check `hasRole(...)`, `#oauth2.hasScope(...)`, or compare `#principal.id`.

6) Q: Best JWT signing strategy in production? A: Use asymmetric keys (RS256) with JWKS for key rotation; keep private keys secure and rotate periodically.

7) Q: How to prevent privilege escalation in tokens? A: Issue tokens with minimal claims, short expiry, and validate audience/issuer. Revoke tokens on role changes by using revocation lists or short-lived tokens and refresh tokens.
