# S3 Estate Info for iPhone and iPad

Native SwiftUI app, iOS 16 or later. The iOS project is separate from Android and uses the same private estate snapshot and daily production API.

## Build locally

Install Xcode and XcodeGen, run `xcodegen generate` in this folder, then open Selendang3.xcodeproj. For an iPhone, choose the owner's Personal Team under Signing & Capabilities, enable Developer Mode on the phone and run. Keep team IDs, signing identities, profiles and credentials local. Never commit generated build folders or private estate records. Use Derived Data outside cloud-managed Documents folders if Finder metadata causes signing errors.

## Home navigation

Estate Dashboard is the home heading; S3 Estate Info is a small footer below the navigation buttons. The large Estate Dashboard button contains three summary rows: Crop data (previous month, MTD and YTD, all t/ha), Upkeep and Manuring (weeding and manuring percentages), and Estate cost (actual YTD Operation and GC in RM/tonne). Saved YTD crop and cost values retain their explicit source periods. Monthly estate crop totals and upkeep progress are Pending until approved complete inputs are connected; sparse daily drafts are not assumed to be complete totals. Prior-year and future-period YTD values are not displayed as current figures. The six smaller buttons retain two columns on iPhone and iPad. The six smaller buttons are Estate Details, Field Details, Upkeep and Cultivation, Crop Production, Settings and Planter Tools. Crop Production replaces the former Reports tab; Planter Tools replaces About on the home screen. About is now accessed through Settings. Crop Production contains all production screens previously opened through Field Details: history by field, daily entry, month totals and year-to-date totals. Existing saved records, drafts and sync are retained. Field Details and Planter Tools are placeholders pending their contents.

## Sync and offline data

Settings Sync Now uploads pending daily production drafts and downloads the estate reference snapshot with visible progress and separate results. Review and confirm a newer estate revision before import. Saved estate tables, dashboard inputs and production drafts remain on the device offline. The access token remains only in the Settings session. Existing production edits and conflict handling are preserved separately from estate imports.

Estate Details replaces the former source-table browser with all estate panels previously shown in Estate Dashboard. Estate Dashboard is a placeholder for future content. The former Estate Production tab is labelled Field Details; its production features now open under Crop Production. Saved source records remain available to calculations and sync offline. Estate Details reads those records for Age Profile, Topography, YPH, Average SPH, Soil Type, Rainfall, Workforce, Ratios and Kos FFB/TAN. Missing or inconsistent values remain Pending. FFB Quality, the additional YTD performance measures, Cost Breakdown and Checkroll remain pending approved data.

Age categories are Young 1–5, Prime 6–23 and Old >23 years, shown in hectares and percentage. SPH divides total field palms by total field hectares. Rainfall uses five calendar years, newest first, excluding the current month in Malaysia time. Workforce counts contractors separately; ratios use only assigned operations workers. YPH shows four previous annual actuals plus current-year actual/budget YTD and a separately labelled annual budget. Cost bars compare Operation Cost and General Charges actual YTD with the annual budget benchmark and label both periods.

## Validation

CI runs production, estate parser and dashboard model checks using fictional records, then an unsigned simulator build. Physical-device builds require the owner's Apple signing setup. The icon source is Artwork/S3.jpg and project generation converts it with macOS sips. No private data or credentials are bundled.
