# Running checks and understanding sync

## One command for local and CI checks

On macOS with Python 3 and Xcode's Swift tools installed, run from the repository root:

```sh
python3 scripts/check-ios.py
```

The script runs production, reference parsing, dashboard, crop-history, calculator and sync checks, plus the Python crop-history migration checks. CI uses this same command. No device signing or live VPS is needed. The sync checks inspect endpoint derivation and invoke the redirect delegate without starting network requests; they are not a live-server integration test.

Run one suite while changing a feature:

```sh
python3 scripts/check-ios.py dashboard
python3 scripts/check-ios.py sync
python3 scripts/check-ios.py --list
```

`--list` shows the source/test files without invoking Swift. The script finds the repository relative to its own location, so it also works when called from another directory. Temporary combined Swift files are deleted on exit. Failed checks stop the command with a nonzero status.

To add a standalone check, register its production sources and test file in `SUITES` in [check-ios.py](../scripts/check-ios.py). Dashboard source extensions matching `EstateDashboardData*.swift` are included together; the extracted `DashboardSummaryChecks.swift` is included when present. Keep calculation sources free of SwiftUI dependencies so these checks stay lightweight. The [CI workflow](../.github/workflows/ios.yml) separately generates the Xcode project and builds the app for the simulator, which verifies the stores and views compile together.

## Sync ownership

| File | Responsibility |
| --- | --- |
| [EstateReference.swift](Selendang3/EstateReference.swift) | `EstateSyncEndpoint` validates the trusted HTTPS snapshot address and token. |
| [EstateSyncTransport.swift](Selendang3/EstateSyncTransport.swift) | Derives the production endpoint on the same origin and creates ephemeral sessions with a shared redirect-refusal delegate. |
| [EstateReferenceStore.swift](Selendang3/EstateReferenceStore.swift) | Downloads, validates, reviews/applies revisions and persists the offline reference. Review/confirmation UI lives in Settings. |
| [ProductionStore.swift](Selendang3/ProductionStore.swift) | Retained production replica, batching, merging and conflict handling; currently not exposed by operational screens. |

Both stores create a session for an operation and invalidate it afterward. Requests retain their existing bearer headers, timeouts, size limits and response validation. Sharing the transport does not enable uploads in the current UI. The retained production upload path also rejects raw token line breaks before invoking the shared endpoint validator.

For a fork, change the estate ID and endpoint contract consistently across validators, server routes and Android. Add accepted/rejected endpoint cases when changing that contract. Keep record validation and persistence in the stores, separate from session policy, and use fictional fixtures rather than private records or credentials.

See [setup and current app behavior](README.md) for project generation, device signing and server-only operation.
