# Branch Protection Guidelines

Recommended GitHub branch protection rules for `main` and `dev`:

- Require pull request reviews before merging
  - At least 1-2 approving reviews
  - Dismiss stale pull request approvals when new commits are pushed
- Require status checks to pass before merge
  - `MedicalFlow CI/CD / build-and-test`
  - `MedicalFlow CI/CD / flyway-validate`
  - `MedicalFlow CI/CD / docker-validation`
  - `MedicalFlow CI/CD / security-scan`
  - `MedicalFlow CI/CD / sonar-analysis` (if enabled)
- Require branches to be up to date before merging
- Require signed commits (optional, if org policy requires)
- Restrict who can push to protected branches
- Enforce linear history if desired

## Recommended Branch Strategy

- `main` for production-ready releases
- `dev` for integration and feature stabilization
- feature branches named like `feature/<name>` or `fix/<name>`
- use pull requests for all changes
