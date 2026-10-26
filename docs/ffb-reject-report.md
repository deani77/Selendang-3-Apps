# FFB reject report

An optional private snapshot extension, `ffbRejectReport`, supplies a dated reject report with `reportDate`, `fields` and `totals`. Numeric keys distinguish `rejectTodayMt`, `rejectMtdMt`, `rejectYtdMt`, and their `Percent` counterparts. Percentages are percentage points, not decimal fractions. Missing values are null.

Estate Details shows Reject in **mt**, using `rejectMtdMt` and `rejectYtdMt` in the This Month and YTD columns. Each category label carries its own unit: Ripe, Underipe and Unripe use %, and Reject uses mt. Those other categories remain pending until their own records are provided. This Month requires the current report month; YTD requires the current year. Future reports are ignored. Missing, negative or nonfinite masses remain Pending; zero and valid masses above 100 mt are preserved. The note states the report date. Stored reject percentages are not used as tonnes.

Do not calculate a missing YTD reject percentage from crop or mill-delivery reports with another cutoff. Preserve source field figures and source estate totals independently when they differ, and record the reconciliation issue in private `reviewNotes`. Source images and operational values stay private.
