# S3 Estate Info for iPhone and iPad

Native SwiftUI app for iOS 16 or later, sharing the private snapshot and production API with Android.

## Navigation
- Home: quick-glance crop, upkeep/manuring and estate-cost summaries.
- Estate Details: estate panels and All Estate Records, covering every saved source table.
- Field Details: dashboard, field buttons from saved records, and field-specific source details.
- Upkeep and Cultivation: dashboard for weeding, manuring, pest/disease and other estate work, excluding crop production. Unconnected measures remain Pending.
- Crop Production: dashboard and all production history, entry and period-total screens.
- Planter Tools: reserved for estate-operation calculators to be specified.
- Settings: Inbox, User Profile, Appearance, About, followed by Estate Data Sync. Suggestions removed.

Field details include only rows explicitly keyed to the selected field; estate-wide figures are not substituted for missing field records. Additional field dashboard metrics and work records await approved definitions/inputs.

## Build
Run `xcodegen generate` in ios/, then open Selendang3.xcodeproj. Select the owner's Personal Team locally for physical-device builds. Keep team identifiers, profiles, private estate records and credentials out of Git. Build outside cloud-managed Documents if Finder metadata affects signing.

## Offline and sync
Estate Data Sync uploads pending production drafts and separately checks estate snapshots. Newer estate snapshots require review/confirmation. Saved source records, production drafts and conflict copies remain available offline. Tokens stay in the active Settings session. Navigation changes do not migrate or delete records.

The iPad home summary uses three top-aligned columns, larger titles and smaller data; iPhone typography and stacking stay unchanged. Crop budget achievement is actual YTD tonnes / full-year budget tonnes × 100, with source periods shown. Missing monthly crop or progress data remains Pending. Zero/missing annual budgets do not produce a percentage.

## Validation
CI checks production, snapshot parsing, dashboard calculations and field isolation using fictional fixtures, then compiles for the simulator. Physical-device signing/installation is validated separately.

## Device dashboard behavior
On iPad mini the home summary stays pinned above the independently scrolling navigation buttons. iPhone and Android show a compact Estate Dashboard button; tapping opens the complete Crop Data, Upkeep and Manuring, and Estate Cost summaries. Shared data, titles, metric units and reporting periods are retained. iPhone keeps its smaller fonts. Detailed estate panels remain under Estate Details.
