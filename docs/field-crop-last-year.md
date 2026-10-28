# Previous-year crop achievement

Each Crop Production field report adds two rows under Crop Budget and Actual after Actual YTD:

- Last Year Full Year · YEAR (mt/ha)
- Last Year Same-Period YTD · through DATE (mt/ha)

The current, non-future crop report defines the comparison year and cutoff. Full year uses 31 December of the previous year. Same-period YTD uses exactly the same month/day one year earlier (29 February maps to 28 February). The labels include the year/date to distinguish a completed year from YTD.

Source: `cropProductionReportHistory`, with exactly one report for each required date and exactly one matching field. Yield = `sentToMillYtdMt` / that historical record's `areaHa`. Tonnes must be finite and nonnegative; historical hectares must be finite and positive. Confirmed zero tonnes displays 0.00. Missing/duplicate reports or fields, invalid values and unavailable current report dates display Pending. Current field area and estate-wide YPH history are never substituted for a field's historical yield. Existing budgets and actuals are preserved. Old snapshots without historical field data remain compatible.

No private historical records are included in this change. Supply verified historical field reports through the existing estate snapshot workflow.
