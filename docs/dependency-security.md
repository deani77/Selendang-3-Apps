# Dependency security monitoring

This page covers security-audit Item 1 for Selendang 3 Apps.

## Automated checks

- Dependabot proposes **weekly** Gradle and GitHub Actions updates through pull requests. Do not automatically merge major upgrades.
- The dependency-review workflow examines dependency changes on pull requests and fails on newly introduced **high or critical** advisories when GitHub can resolve the dependency graph.
- OSV-Scanner performs a scheduled scan on Mondays at **09:30 Malaysia time**, on demand, and after selected dependency changes reach `main`. Review findings in Actions and, when available, Security > Code scanning.

## Coverage limitations

- These controls are preventive monitoring, **not a completed dependency vulnerability audit**. Existing vulnerabilities are not proven absent.
- OSV-Scanner coverage depends on supported manifests / lockfiles. Kotlin Gradle build files alone may not be sufficient for comprehensive transitive dependency extraction. Assess Gradle dependency locking or SBOM generation separately before treating OSV results as comprehensive.
- The Node.js server currently runs tests directly without a server package manifest. If a package manifest is introduced, configure a matching Dependabot npm ecosystem entry and commit its lockfile.
- Native iOS project code has no third-party package dependency manifest identified in the initial audit. Add appropriate updates/scanning if one is introduced.
- GitHub Dependabot alerts and dependency graph features must be checked in repository Settings > Security & analysis; their account-level settings are not configured by this PR.
- After merging, confirm the workflows actually execute successfully and inspect their first reports. Do not assume a green scan proves there are no vulnerable transitive dependencies.

## Review policy

Keep `main` protected. Review dependency update PRs, run application builds/tests, and approve each upgrade before merging.
