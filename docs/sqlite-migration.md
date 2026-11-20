# SQLite migration — preparation stage

## Verified existing architecture

- Android: Kotlin / Jetpack Compose frontend with **Room (SQLite)** on-device.
- iOS: SwiftUI frontend and its existing offline store.
- Server: Node.js HTTP API, authenticated snapshot downloads and a separate production endpoint.
- Server persistence today: `snapshot.json` and `production/records.json`. Production audit history lives in the JSON production file.

**No frontend rewrite is needed.** The first stage prepares a *separate* SQLite copy of the server data. The live server continues reading JSON; **SQLite is not yet the authoritative server database**.

## Stage 1: offline migration rehearsal

Use a private copy of the real VPS data, never commit the files. Node.js 24+ provides built-in `node:sqlite`.

```sh
node server/migrate-sqlite.mjs /private/snapshot.json /private/records.json /private/estate.sqlite
node --test server/migrate-sqlite.test.mjs
```

The output SQLite file contains the snapshot JSON payload, versioned production records, production audit, and revision metadata. A missing/invalid input stops the migration; an existing destination is never deliberately replaced. Store it in a private directory with restrictive permissions. Never put database files or access tokens in this public repository.

## Cutover criteria (not yet implemented)

1. Obtain an independent off-VPS backup of BOTH JSON files, archived revisions, and the hashed token registry. Record file sizes, SHA-256 checksums and current revision numbers.
2. Validate row counts, section IDs, snapshot revision, production revision, unique date/field keys and complete audit history after import. Compare representative real records.
3. Implement a SQLite-backed repository adapter behind the **same authenticated API endpoints**; preserve bearer scopes, rate limits, sync-conflict behaviour and JSON response schema.
4. Run old/new contract tests and crash-recovery/rollback tests. Keep JSON untouched until verified.
5. On Mac mini, ensure persistent storage, scheduled backups, safe remote HTTPS access, and a reachable host while it is powered on. Do not expose SQLite or Node's loopback port directly to the internet.
6. Test Android, iPhone, iPad and Android head unit against the replacement service, including offline access and new-revision sync.
7. Switch clients to the new service only after acceptance. Keep VPS and immutable JSON backup until rollback is no longer needed.

**Important:** when Mac mini is switched off, mobile devices still read previously synchronized offline data, but cannot synchronize. Decide the connection method and uptime before cutover. This first stage does not access or modify the VPS, the Mac mini or any production dataset.
