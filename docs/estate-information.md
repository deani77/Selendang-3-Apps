# Estate Details and source records

Estate Details displays saved estate panels, including Estate Area, age profile, topography, yield, stand density, soil, rainfall, workforce, worker ratios and FFB costs. Missing or unusable records remain Pending. Charts and calculations retain their reporting periods; the feature guides below define the source rules.

All Estate Records opens the seven reference sections in the snapshot. Android routes are defined in [EstateSections.kt](../app/src/main/java/com/selendang3/app/navigation/EstateSections.kt); iOS reads the saved sections through [EstateRecordsView](../ios/Selendang3/EstateModuleViews.swift).

| Snapshot section | Contents |
| --- | --- |
| `overview` | Estate profile, cultivation and maturity status, location and dated summary measures |
| `fields` | Field and alternate codes, hectarage and palm population |
| `ages` | Field ages and age-category records, with reporting year |
| `soil` | Soil series, terrain types, percentages and corresponding area |
| `rainfall` | Monthly rainfall, rainy days and historical comparisons |
| `abw` | Current-period and cumulative average bunch weight, in kg/bunch |
| `roads` | Estate road types and widths |

Settings → Estate Data Sync downloads a validated snapshot for review and confirmation. Saved records remain available offline. See the [snapshot guide](data-updates.md) for the schema, revision rules and current import/export capabilities. Repository contents do not establish live VPS deployment or data freshness.

## Source handling

Keep actual records, coordinates, financial data and original operational documents outside this public repository. Preserve source values and provenance; resolve conflicting area bases, palm densities, age profiles and source totals before publishing an official update. Do not substitute estate totals for field-level records.

Missing or pending months remain Pending, not zero. Every dataset retains its reporting period. ABW measures weight per bunch; it does not establish bunch counts or total production. An extract without a confirmed reporting cutoff must not acquire an inferred month from its extraction date.

## Related requirements

- [Age profile](age-profile.md), [topography](topography.md), [soil suitability](soil-suitability-reference.md) and [YPH](yph.md)
- [Rainfall](dashboard-rainfall.md), [workforce](dashboard-workforce.md), [worker ratios](dashboard-worker-ratios.md) and [FFB cost](dashboard-ffb-cost.md)
- [Field source profiles](field-source-profile.md), [field ABW](field-abw.md) and [crop history](crop-history.md)

Earlier local production-entry and upload requirements are retained in the [historical production design](estate-production.md). Current Crop Production screens show saved reports.
