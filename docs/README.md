# Documentation

## Start here

- [Project overview and repository layout](../README.md)
- [Android setup, checks and code ownership](android-development.md)
- [iOS setup and feature guide](../ios/README.md)
- [iOS architecture](../ios/ARCHITECTURE.md) and [developer checks](../ios/DEVELOPMENT.md)
- [Settings and local preferences](settings.md)
- [Snapshot format and VPS synchronization](data-updates.md)

## Storage, operations and security

- [Android database and migrations](android-database.md)
- [Server API, deployment and backups](../server/README.md)
- [Offline SQLite migration preparation](sqlite-migration.md)
- [Shared snapshot contract and regression fixtures](../test-data/snapshots/README.md)
- [Dependency security checks](dependency-security.md)
- [HTTPS security headers](https-security-headers.md)
- [Upload and import validation](upload-import-security.md)

## Feature and source-data requirements

These guides describe individual features and source-data rules. For current enabled behavior, start with the project overview and platform guides.

- [Estate Details and source records](estate-information.md)
- [Age profile](age-profile.md), [topography](topography.md), [soil suitability](soil-suitability-reference.md) and [YPH](yph.md)
- [Rainfall](dashboard-rainfall.md), [workforce](dashboard-workforce.md), [worker ratios](dashboard-worker-ratios.md) and [FFB cost](dashboard-ffb-cost.md)
- [Crop history](crop-history.md), [prior-year field crop](field-crop-last-year.md), [field ABW](field-abw.md) and [field source profiles](field-source-profile.md)
- [FFB reject report](ffb-reject-report.md) and [synced estate name](synced-estate-name.md)
- [Visual style](visual-style.md), [orientation](orientation.md) and [app icon](app-icon.md)

## Historical designs and retained code

These describe earlier requirements, not enabled mobile workflows.

- [Early v0.2 home-screen requirements](design/home-screen-v0.2.md)
- [Production draft and two-way-sync design](estate-production.md)
