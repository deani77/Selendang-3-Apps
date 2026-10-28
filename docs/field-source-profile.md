# Field Details source profile

Field Details can display an optional `fieldProfiles` snapshot extension. Existing PM buttons and operational tables remain intact. The extension adds a source summary and individually identified blocks under each PM code.

Selected facts: planted and unplanted hectares, division stands per hectare and palm height, maturity, block identifier/name, soil code, harvesting year, and source discrepancies. Report year is 2027, dated 28 October 2026. Do not infer planting year, age, or total palms. Missing soils remain pending. Zero palm height is a source zero.

Private extracted records are in `private-data/field-details-2027.json`, excluded from Git. Do not commit source records. `scripts/prepare-field-details.py` merges them into a verified local snapshot using its expected SHA-256, checks PM code mapping, refuses existing profiles/output, and preserves all existing sections and extensions. It neither increments a revision nor publishes. Obtain the current private snapshot before preparing a publication candidate.

The supplied division records are not a verified whole-estate control total. Unplanted areas use block totals because the division headers incorrectly report zero. Block harvesting years remain separate from conflicting stage dates.
