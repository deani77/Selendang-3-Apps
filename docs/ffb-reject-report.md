# FFB reject report

An optional private snapshot extension, `ffbRejectReport`, supplies a dated reject report with `reportDate`, `fields` and `totals`. Numeric keys distinguish `rejectTodayMt`, `rejectMtdMt`, `rejectYtdMt`, and their `Percent` counterparts. Percentages are percentage points, not decimal fractions. Missing values are null.

Estate Details uses the source reject percentage in the This Month and YTD quality columns. This Month requires the report month to match the current month; YTD requires the current year. Future reports are ignored. Missing, negative, nonfinite or over-100 percentages remain Pending. Confirmed zero is preserved. The dated note retains the source mass in mt. Ripe, Underipe and Unripe remain pending until their own records are provided.

Do not calculate a missing YTD reject percentage from crop or mill-delivery reports with another cutoff. Preserve source field figures and source estate totals independently when they differ, and record the reconciliation issue in private `reviewNotes`. Source images and operational values stay private.
