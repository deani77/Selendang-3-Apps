# Android development

S3 Estate Info is a Kotlin and Jetpack Compose app for viewing saved estate reports. It supports Android 8.0 (API 26) and later, with compile/target API 35. App version, application ID and dependency versions are defined in [app/build.gradle.kts](../app/build.gradle.kts) and the [root build configuration](../build.gradle.kts).

For Room table ownership, schema exports and safe upgrades, see the [database and migration guide](android-database.md).

## Current behavior

Estate Dashboard, Estate Details, Field Details, Crop Production, and Upkeep and Cultivation display snapshot-backed reports. Planter Tools provides six offline calculators. Appearance and user-profile preferences stay on the device; the profile is not a signed-in server account.

Settings → Estate Data Sync downloads a complete snapshot from an administrator-configured HTTPS address using a privately issued token. Users review and confirm a newer revision before it replaces the saved snapshot. Downloaded records remain available offline. Android can export a private snapshot backup; current screens do not offer manual estate-file import, operational data entry or production uploads. Retained production/import code should not be taken as an enabled UI feature.

Missing or unusable report values remain Pending, while measured zero stays zero. The footer shows connectivity and saved update/sync information. No estate operational records or access tokens are bundled into the app. See the [snapshot guide](data-updates.md) for the data contract and [settings guide](settings.md) for the user workflow.

## Build locally

Install JDK 17, Android SDK platform 35 and build tools 35.0.0. Android Studio can provide them. Set `ANDROID_HOME`, or create an ignored `local.properties` containing your `sdk.dir` path. Open the repository root in Android Studio.

From the repository root:

```sh
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
```

On Windows, use `gradlew.bat`. The checked-in Gradle wrapper downloads its pinned distribution and verifies its checksum. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`; the instrumentation APK is written to `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.

Build success verifies compilation and unit/lint checks. It does not execute device tests. To run those, connect a device or start an emulator, then run:

```sh
./gradlew --no-daemon connectedDebugAndroidTest
```

## What CI runs

The [Android test APK workflow](../.github/workflows/android.yml) runs for main pushes, pull requests and manual dispatch.

| Event | Checks and outputs |
| --- | --- |
| Main push, manual dispatch or non-draft PR | Unit tests, lint, app/test APK compilation, committed Room schema verification, and instrumentation tests on an API 29 emulator. Uploads the debug APK and check reports. |
| Draft PR | Unit tests, lint and committed Room schema verification. Uploads source-check reports; skips APK generation and emulator execution. |

Instrumentation covers offline storage, migrations, navigation and layout. See the [database guide](android-database.md) for the schema-based upgrade test. Emulator coverage complements hands-on checks of appearance, accessibility, file export and a real HTTPS download on a phone.

## Get a test APK

Open a successful full workflow run in GitHub Actions and download its debug APK artifact. The artifact's current name is defined in the workflow; extract the ZIP to obtain `app-debug.apk`. Artifacts are retained for 14 days, and downloading them may require GitHub sign-in. Draft PR source-check runs do not publish APKs.

The APK is debug-signed for testing. A release for real users needs a separately configured, securely retained signing identity. No release signing keys are included in this repo.

## Adapting the app

Start with [MainActivity.kt](../app/src/main/java/com/selendang3/app/MainActivity.kt) for the entry point, [EstateDataScreen.kt](../app/src/main/java/com/selendang3/app/EstateDataScreen.kt) for snapshot controls and review, and [EstatePackage.kt](../app/src/main/java/com/selendang3/app/data/EstatePackage.kt) for parsing. [EstateUpdates.kt](../app/src/main/java/com/selendang3/app/data/EstateUpdates.kt) separates downloading from transactional database replacement.

When using this repo as a base, review the application ID, app name/artwork, estate ID and API paths, snapshot column names, domain formulas, units and reporting timezone. Keep the server, Android and iOS contracts aligned if all remain in your fork. Use the [fictional sample snapshot](../sample-data/estate-update.example.json) and [snapshot contract](data-updates.md) to learn the format; the basic sample does not populate every dashboard extension.

## Private data

Keep actual estate records, financial/personnel information, credentials and database exports outside this public checkout. `private-data/`, local settings, signing files and common secret files are ignored as an extra safeguard. Use fictional records in tests and examples; do not place tokens in source, APK resources, URLs or logs.

## Build references

- [AGP 8.9 compatibility](https://developer.android.com/build/releases/agp-8-9-0-release-notes)
- [Compose compiler Gradle plugin](https://developer.android.com/develop/ui/compose/compiler)
- [Room releases](https://developer.android.com/jetpack/androidx/releases/room)
