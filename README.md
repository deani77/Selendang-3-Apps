# S3 Estate Info

Native Android and iOS apps for viewing Selendang 3 estate information, with a small Node.js service for private snapshot downloads.

## Current behavior

- Estate Dashboard, Estate Details, Field Details, Crop Production, and Upkeep and Cultivation display saved estate reports. Missing records remain Pending.
- Settings downloads a complete snapshot from an administrator-configured HTTPS VPS. Users review and confirm a newer revision before it replaces saved data.
- Downloaded records remain available offline. Current mobile screens do not expose operational data entry, production uploads, or manual estate-file import. Android supports exporting the saved snapshot as a private backup.
- Planter Tools provides six offline calculators. Calculator inputs do not change estate records.
- Appearance and user profile preferences stay on the device; a local profile is not a server account.

Legacy production draft and synchronization code remains in the repository, and the server implements scoped production writes. Those capabilities are not exposed by the current mobile screens. Code availability does not establish that a VPS deployment is live.

## Repository layout

| Path | Contents |
| --- | --- |
| `app/` | Android app: Kotlin, Jetpack Compose, Room, unit and instrumented tests |
| `ios/` | iOS app: SwiftUI, XcodeGen project configuration, standalone calculation checks |
| `server/` | Node.js API, snapshot publisher, token administration, tests and deployment examples |
| `docs/` | Feature requirements, data contracts and development guidance |
| `scripts/` | Source-data preparation and migration checks |
| `sample-data/` | Fictional snapshot example |
| `.github/workflows/` | Android, iOS and server build/check workflows |

## Build and check

Android requires Java 17 and Android SDK 35. See [Android development](docs/android-development.md) for setup and APK instructions.

```sh
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
```

On macOS with Xcode and XcodeGen installed:

```sh
cd ios
xcodegen generate
xcodebuild -project Selendang3.xcodeproj -scheme Selendang3 -configuration Debug -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO build
```

See the [iOS guide](ios/README.md) for device signing and feature details. The iOS workflow also runs standalone Swift calculation and parser checks.

With Node.js 24, run server tests from the repository root:

```sh
node --test server/*.test.mjs
python3 scripts/test-crop-history.py
```

Android instrumentation tests require a device or emulator: `./gradlew connectedDebugAndroidTest`.

## Documentation

Start with the [documentation index](docs/README.md), [snapshot and sync guide](docs/data-updates.md), or [server operations guide](server/README.md). App versions are defined in [Android build configuration](app/build.gradle.kts) and [iOS project configuration](ios/project.yml). Early screen requirements are preserved in the [historical design reference](docs/design/home-screen-v0.2.md).

## Public repository, private estate data

Keep actual estate operational records, financial information, personal information, credentials and private exports outside this public repository. Use fictional sample data for tests and examples. Distribute access tokens privately; do not embed them in app builds, URLs or logs.
