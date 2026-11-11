# Android database and migration guide

The app uses Room for its local estate cache. Start with [EstateDatabase.kt](../app/src/main/java/com/selendang3/app/data/EstateDatabase.kt) for entities, DAO methods and registered migrations. [EstateUpdates.kt](../app/src/main/java/com/selendang3/app/data/EstateUpdates.kt) applies a newer snapshot in a transaction.

This guide is for developers adapting the app or changing its stored data. Database schema versions describe tables and columns; snapshot `schemaVersion`, snapshot `revision` and the app's release version are separate numbers.

## What is stored

| Table | Purpose |
| --- | --- |
| `estate_fields` | Legacy structured field records. Retained so database upgrades preserve existing installations; current operational screens read snapshots instead. |
| `sync_state` | Timestamp of a successful applied VPS update. It does not contain credentials. |
| `estate_snapshot` | Complete snapshot JSON plus revision, update time and source. Slot 1 is current; slot 2 retains the previous snapshot when a replacement is applied. |

The database is named `estate.db` and starts empty. Room stores snapshot JSON as opaque text; the package parser validates the payload before it is applied. Changing a report field inside that JSON does not by itself require a Room schema change.

Production draft storage is separate from Room; see [ProductionDraftStore.kt](../app/src/main/java/com/selendang3/app/data/ProductionDraftStore.kt) before changing that legacy feature.

## Schema history

Room's generated JSON files live in [app/schemas/com.selendang3.app.data.EstateDatabase/](../app/schemas/com.selendang3.app.data.EstateDatabase/). They describe table definitions, indexes and Room identity hashes, not private records. Commit them with the source that changes the database.

- Version 1 contains `estate_fields` and `sync_state`. Its export was regenerated from the original [version 1 database definition at 7d38e81](https://github.com/deani77/Selendang-3-Apps/blob/7d38e81/app/src/main/java/com/selendang3/app/data/EstateDatabase.kt), using the repository's pinned Room compiler.
- Version 2 adds `estate_snapshot`. `MIGRATION_1_2` creates that table without replacing either existing table. Its export comes from the current database definition.

Historical schema files are reference points for old installations. Do not rewrite an old version to match a new entity definition or remove legacy tables without a migration decision.

[app/build.gradle.kts](../app/build.gradle.kts) sends Room's schema output to `app/schemas/` and includes that directory as instrumentation-test assets. This lets tests create a real old database directly from the committed export.

## How the upgrade is verified

[DatabaseMigrationTest.kt](../app/src/androidTest/java/com/selendang3/app/DatabaseMigrationTest.kt) uses Room's `MigrationTestHelper` to:

1. Create a version 1 database from `1.json` and insert fictional fields and sync state.
2. Run the production `MIGRATION_1_2` and validate the resulting database against `2.json`, including unexpected leftover tables.
3. Open the migrated file through the production Room model and confirm all field properties, nullable values, verification flags and the sync timestamp survive.
4. Confirm the new snapshot table is empty, then write and read a snapshot through the DAO.

[DatabaseUpdateTest.kt](../app/src/androidTest/java/com/selendang3/app/DatabaseUpdateTest.kt) separately verifies that rejected older updates preserve the current snapshot, previous snapshot and sync state. Migration correctness and payload/revision validation are different responsibilities.

## When changing the database in your own app

1. Decide whether the change affects Room tables or only the JSON contract. For a table change, update the entity and increase `@Database(version = ...)`.
2. Add a migration from each previously supported version through a valid upgrade path. Register it with `Room.databaseBuilder(...).addMigrations(...)`; defining a migration alone does not activate it.
3. Build with the pinned compiler to generate the new schema. Review the SQL, nullability, defaults, keys and indexes; commit the new JSON without altering historical exports.
4. Add an upgrade test starting from the previous committed schema. Seed representative values and verify preservation, not just that the database opens.
5. Run the checks below. Keep migration registration, tests and schema exports in the same change.

For a new non-null column, decide how existing rows receive a value. A Kotlin constructor default is not automatically an SQL default. For a table rename or removal, explicitly map or retire the existing records. This project preserves data through registered migrations; destructive fallback would change that behavior and can erase an offline cache.

## Run the checks

With Java 17 and Android SDK 35 installed, run from the repository root:

```sh
./gradlew --no-daemon :app:kspDebugKotlin
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebugAndroidTest
./gradlew --no-daemon connectedDebugAndroidTest
```

The last command requires a connected device or emulator. To run only the migration test:

```sh
./gradlew --no-daemon connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.selendang3.app.DatabaseMigrationTest
```

After generation, inspect `git status --short -- app/schemas` and `git diff -- app/schemas`. CI checks for changed or untracked schema exports after compilation, so forgetting to commit an export fails the build. The full Android workflow runs the migration test on an API 29 emulator; draft PRs run source checks only.
