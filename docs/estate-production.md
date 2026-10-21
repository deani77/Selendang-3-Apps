# Estate Production: initial tab and local drafts

Android and iOS expose Crop History by Field, Today Crop Production, Month Crop Production and Year Todate Crop Production. This source-only update does not deploy the VPS or create a release. It retains the current app version.

## Daily drafts

Select a reporting date and original field code. Enter Crop Harvested (t), Crop Sent to Mill (t), Crop Backlog balance (t), Hectares Harvested (ha), and Number of Harvesters Present. Values may be blank when unknown; measured zero remains zero. At least one value is needed to save. Reject negative/non-finite amounts and fractional/negative worker counts.

Save Draft persists locally and replaces only the matching field/date record. Existing saved rows are loaded when a field/date is selected. Draft storage is separate from the seven-section estate reference snapshot, so Settings imports cannot replace it. It is not included in Settings Export Estate Data. Changing devices or clearing app storage can lose these drafts; production backup and cross-device sync are pending. Unreadable draft storage blocks edits rather than silently overwriting records. Switching section, field or date prompts before discarding unsaved edits; save before leaving the production screen.

## Aggregation

Month Crop Production includes the first day of the selected month through the reporting cutoff and shows all five measures. Crop Harvested, Crop Backlog, Hectares Harvested and Number of Harvesters carry forward for that month only. Year Todate Crop Production includes January 1 through the cutoff and displays only Crop Sent to Mill; it does not display year-to-date harvested crop, hectares, attendance or backlog. Both show each field separately, the number of saved dates versus elapsed calendar days, and how many saved dates report each displayed metric. These are provisional local-draft totals, not official synchronized production. No saved values means Pending, not zero. Sum known daily tonnes and hectares; label attendance totals as harvester-days, not unique workers. Harvested hectares are cumulative work across harvest rounds. Backlog is the latest saved day's balance within the selected month and its date, not a sum; a missing balance on that day remains Pending. Do not carry backlog from a previous month without disclosure.

Historical field crop and t/ha are read-only, with five displayed years including the cutoff year. Values remain Pending until verified field-level history is connected. Never substitute estate-wide YPH figures or derive historical yield using an unverified area denominator. Partial current-year figures will need a YTD label when history is connected.

## PDF field aliases

| Report alias | App field |
| --- | --- |
| 0256PM0401 | PM04G |
| 0256PM0502 | PM05E |
| 0256PM0603 | PM06F |
| 0256PM0804 | PM08G |
| 0256PM1006 | PM10K |
| 0256PM1108 | PM11M |
| 0256PM1511 | PM15P |

The owner confirmed Pengeluaran Hasil means Crop Sent to Mill. Area is a consistency check for matching report aliases to current field records, not sufficient identity on its own. Do not bundle the private report, hectares, crop figures, credentials or snapshots in this repository. PDF capture is disabled and labelled coming next. Implement bounded PDF parsing, date/unit checks, area discrepancy review, editable extraction preview and explicit import confirmation in the next phase. Crop Harvested and Backlog require manual entry or another source. PDF MTD/YTD totals are reconciliation/opening-balance candidates; never sum repeated cumulative totals as daily figures. Duplicate dates need replacement/review, not additional records.

## Server follow-up

Submit to VPS is disabled and labelled not connected. Save Draft never implies successful upload, and Settings Sync Now remains a download of estate reference data. No production data is uploaded in this change. Before enabling submission, implement authenticated write permissions, durable server storage, field/date uniqueness, idempotent retries, conflict handling, edit provenance, server receipts, and the owner's chosen approval workflow. Production export/backups and iOS/Android synchronization must use the same server contract. Upkeep and Cultivation is outside this first tab update and still needs its own fields.

## Validation

Android unit tests cover field aliases, date/field filtering, month/year boundaries, missing values, zero, daily attendance totals, latest backlog and duplicate records. Instrumented tests cover local persistence, edit replacement and preservation of unreadable storage. Run the normal Android and iOS CI checks before merging/releasing. For this source-only commit, CI is intentionally skipped at the owner's request; there is no new APK or iOS build.

On both devices, check saving/reopening a draft, selecting a different field/date, discarding/keeping unsaved changes, editing an existing draft, blank versus zero, MTD/YTD coverage, read-only history, and disabled PDF/submission buttons. Verify server estate reference import leaves production drafts untouched. Device/visual verification remains pending.
