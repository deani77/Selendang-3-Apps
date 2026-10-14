# Estate Information

Content plan for the Estate Information button in Ladang Selendang 3, v0.1 Alpha. This is a design reference; no app screens or data services are implemented.

## Sections

| Section | Contents |
| --- | --- |
| Estate Profile | Estate overview, cultivation and maturity status, location |
| Fields and Area | Field and alternate codes, hectarage, historical mature area |
| Palm Population and Age | Palm counts, density, field ages and age profile, with reporting year |
| Soil and Topography | Soil series, terrain types, percentages and corresponding area |
| Rainfall | Monthly rainfall in mm, rainy days, annual and year-to-date totals, historical comparison |
| Infrastructure | Estate road types and widths |

Keep the navigation title **Estate Information**. Rainfall is a section within this module.

## Source handling

The owner supplied Estate_Information.md, Rainfall.md and General_Data.md for private consolidation. Actual records, coordinates and material prices are not included in this public reference. Validate conflicting area bases, palm densities, age profiles and source totals before database import. Preserve source values and provenance; show unresolved values as unverified.

Missing or pending months must be represented as pending, not zero. Deduplicate repeated tables. Display each dataset's reporting period and last successful synchronization time. Previously synchronized data is intended to remain available offline.

## Other modules

Material prices and pest-control product references belong under **Upkeep and Cultivation**. Production records and yield calculations belong under **Estate Production**. Operational records must stay in private storage when those modules are implemented.
