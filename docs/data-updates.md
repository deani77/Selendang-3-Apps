# Estate snapshots and VPS synchronization

Android and iOS download complete estate snapshots through Settings → Estate Data Sync. New revisions are previewed and confirmed before replacing the offline store. Current screens do not offer manual file import or production uploads. Legacy import and production synchronization code is retained for possible future use.

## Phone workflow

1. Obtain the trusted HTTPS snapshot address and a privately issued access token from the administrator.
2. In Settings → Estate Data Sync, enter the address and token, then select Sync Now.
3. Review a newer revision, including its reporting period and section row counts, and confirm the update. Cancelling keeps the saved data.
4. Browse downloaded records offline. Null cells display Pending; measured zero is preserved as zero.
5. On Android, use Export Estate Data to save a private copy of the current snapshot. Current mobile screens cannot re-import that file. Both stores retain a previous snapshot internally, without a user-facing rollback control.

Updates require a strictly higher positive integer revision. Duplicate or older revisions are rejected, including at the database transaction boundary. Invalid files, failed downloads and cancelled previews leave the saved dataset intact. A revision orders snapshots, not individual rows; the administrator must carry forward all current data. Structural validation cannot establish the accuracy of estate figures. Resolve the known source discrepancies before publishing official updates.

## Shared file/API format

See [fictional example](../sample-data/estate-update.example.json). Format version is independent of app version.

- `schemaVersion`: integer 1.
- `estateId`: `selendang-3`.
- `revision`: increasing positive integer, preserved when exporting and migrating hosts.
- `reportingPeriod`: short human-readable description of the supplied records.
- `exportedAt`: ISO-8601 timestamp with timezone.
- `sections`: exactly one each of `overview`, `fields`, `ages`, `soil`, `rainfall`, `abw`, `roads`.
- Each section has `columns` (1–20 unique text labels) and `rows` (arrays with the same number of cells). Include units and reporting year in the labels or rows. Cells are text, numbers or null. No computed formulas, HTML or executable content is interpreted.
- Maximum 2 MB UTF-8, 10,000 rows in total, 1,000 characters per text cell. Pending sections use empty rows. The importer checks format and bounds; it does not recalculate or certify source totals.

The snapshot supplies estate reference tables and optional report extensions used by dashboards and charts. See the feature guides in the [documentation index](README.md) for report-specific fields. Snapshot downloads do not create accounts or enable staff data entry.

## VPS connection contract

The phone accepts an administrator-supplied full HTTPS address, for example `https://YOUR-DOMAIN/v1/estates/selendang-3/snapshot`, plus a privately issued read-only Bearer token. It sends GET with `Authorization: Bearer …` and `Accept: application/json`; the server returns the same JSON file with HTTP 200. It does not upload phone records. Redirects are refused, and ordinary TLS certificate verification remains enabled. Tokens are held only in memory for the Settings session; the non-secret address is saved on-device. They are never put in an APK, URL, public repository, export or log.

The private VPS service is configured separately; reusable code and operating instructions are in [server](../server/README.md). It supports HTTPS, per-user revocable read-only tokens, rate limits, administrator publication and daily local backups. Live credentials are kept outside this repository. Automatic off-server backups still need an external destination. Use an administrator-generated high-entropy read-only token for initial tests, not an account password. Do not expose the database directly. The current local user profile is not server authentication. Staff tokens can be issued or revoked through the server administrator's command line. A web administration/upload page, password-based accounts and automatic background synchronization remain unimplemented. Actual estate figures still require review before publication.

## When VPS service ends

Before ending VPS service, preserve the last verified complete snapshot and private backups outside the VPS. Phones retain previously downloaded information offline. Manual file import would need to be restored and verified before distributing future file-based updates. Server failures do not clear cached data. If migrating instead, retain the domain, API format, estate ID and revision sequence. Restore backups and test the replacement server before switching DNS. The phone's local cache is not a backup strategy.

## Validation

CI runs package-parser unit tests, Android lint and APK compilation, then runs navigation, database migration and rejected-update preservation tests on an Android API 29 emulator. You can also run `./gradlew connectedDebugAndroidTest` on a connected device/emulator. Exercise a real file selection, server download and export on a phone before wider use. Do not claim the VPS is live or synchronized until a confirmed import succeeds.
