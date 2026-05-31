# Release and Rollback Strategy

## Release Process

1. Create a feature branch for each work item.
2. Open a pull request against `dev`.
3. CI should verify:
   - Maven build and unit tests
   - Flyway migration validation
   - Docker compose syntax and image build
   - Security dependency scan
   - Sonar analysis (if secrets are configured)
4. Merge PR into `dev` after approvals and passing status checks.
5. Once `dev` is stable, create a release candidate PR targeting `main`.
6. Merge into `main` after all checks pass.

## Rollback Guidance

- If a deployment or production issue occurs, revert the merge commit on `main`.
- If database migrations are part of the release, ensure migration rollback scripts are available before reverting.
- Prefer deploying a hotfix branch from `main` when urgent fixes are needed.

## CI/CD Notes

- Keep migrations idempotent and validated in CI before they reach `main`.
- Ensure `application.properties` and secrets for production databases are not stored in source control.
- Use environment-specific configuration or GitHub Secrets for production DB credentials and external service endpoints.
