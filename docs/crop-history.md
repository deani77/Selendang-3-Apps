# Field crop history

Dashboard → Crop Production → Crop History offers seven field tabs, a yield chart and a table. Crop Production also contains the same panel. The window rolls with the current calendar year, including missing years as N/A. The snapshot's `cropHistory` extension is optional; older snapshots show an unavailable message.

The private Markdown source provides completed annual t/ha, including retained 2021 data, and actual 2026 cumulative tonnes. The source's last populated consecutive month determines the explicit cutoff. Blank months are not zero production. The YTD year remains labelled YTD even after the calendar changes; missing later years remain N/A. YTD yield divides cumulative actual tonnes by the captured authoritative operational planted hectares. Missing/nonpositive hectares produce N/A. Do not use the separately dated source profiles to infer historical area.

`cropHistory` has `cutoff`, `source`, and `fields`. Each field stores `fieldCode`, nullable annual values keyed by year, `productionYear`, `productionTonnes`, nullable `plantedAreaHa`, and `areaSource`. Real estate source files and snapshots stay outside Git.

Prepare updates with `scripts/prepare-crop-history.py BASE SOURCE OUTPUT --expected-sha256 HASH`. It checks estate identity, complete field coverage, consecutive production months, totals, area availability, and preserves unrelated content. It advances revision/export time, replaces only the crop-history extension and refuses an existing output. Before publishing, compare the live base hash under the publisher lock, back up snapshot and production, archive the base revision, validate with the deployed validator, set root:s3estate 0640, and rename atomically. Verify authenticated and anonymous HTTPS reads, exact content, all unrelated extensions and unchanged production. This requires no UI deployment.

Checks: `python3 scripts/test-crop-history.py`; iOS CropHistoryChecks in CI; Android CropHistoryTest. All fixtures are fictional.
