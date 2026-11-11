# Shared snapshot validation contract

`cases.json` contains one fictional base snapshot and named cases. For each case, replace the base's top-level keys with `overrides`; `valid` is the expected parser result. Null overrides are JSON null, not key deletion. Android, iOS and server tests read this same file. Add regression cases here when changing these rules.

## Common rules

- UTF-8 input, at most 2 MiB (2,097,152 bytes).
- `schemaVersion` is the number 1; `estateId` is `selendang-3`.
- `revision` is a whole number from 1 through 9,007,199,254,740,991, inclusive. This is JavaScript's safe-integer range. Whole-number JSON notation such as `1.0` is accepted; strings and booleans are not numbers.
- `reportingPeriod` is nonblank and at most 100 UTF-16 code units.
- `exportedAt` is a real Gregorian date and time in `YYYY-MM-DDTHH:mm:ss[.fraction](Z|±HH:mm)` form. Years are 0001–9999; seconds are 00–59; fractions have 1–9 digits. Offsets are bounded by ±18:00. Date-only values, normalized invalid dates, leap seconds, trailing text and timestamps without a timezone are rejected.
- Exactly one section each: `overview`, `fields`, `ages`, `soil`, `rainfall`, `abw`, `roads`. Sections have 1–20 unique, nonblank text columns, each at most 80 UTF-16 code units. Rows match column widths; the combined row count is at most 10,000.
- Cells are text (at most 1,000 UTF-16 code units), finite numbers or null. Null means Pending; measured zero remains zero. Booleans and objects are rejected.

## Optional extensions covered here

Omitted or null `yph` and `fieldProfiles` keep legacy snapshots valid.

When supplied, `yph` requires an integer reporting year from 1900–2200, month 1–12, positive finite mature area, and the keys `actualYtdTonnes`, `budgetYtdTonnes`, `fullYearBudgetTonnes`, and `history`. Tonnage values are null or finite nonnegative numbers; YTD budget cannot exceed annual budget when both exist. History has at most 50 unique years, each from 1900 through the year before the reporting year. Each history entry requires an `actualYph` key containing null or a finite nonnegative number. Actual production may exceed budget.

`fieldProfiles` is an array of profiles with unique trimmed field codes. Field codes and maturity are nonblank text; report years are integers 1–9999; areas, stand densities and palm heights are finite and nonnegative. Each profile has a block array with unique trimmed identifiers, nonblank names, integer harvesting years 1–9999, and finite nonnegative areas. Block soil codes and source notes may be omitted or null, otherwise they are text. Empty profile and block arrays are allowed.

This suite covers the shared envelope, tables, YPH and source field profiles. Other report extensions retain their existing platform-specific decoding and display checks; this change does not redefine their contracts. Unknown extension keys remain permitted for forward compatibility.

## Running checks

From the repository root:

- Server: `node --test server/*.test.mjs`.
- Android: `./gradlew testDebugUnitTest`; Gradle includes `test-data/` as test resources.
- iOS: the **Check estate reference validation** step in `.github/workflows/ios.yml` runs the shared fixtures along with existing parser checks on macOS. Its working directory is the repository root.

The three runners also generate byte-size and total-row boundary cases and malformed UTF-8 input without committing oversized fixture files.

The stricter rules reject malformed snapshots that some platforms previously accepted. Valid older snapshots without optional extensions remain supported. No saved files or databases are migrated by this change; verify a private operational snapshot against the contract before rollout.
