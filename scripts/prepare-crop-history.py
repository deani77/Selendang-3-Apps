#!/usr/bin/env python3
"""Merge private source Markdown into an exact-base snapshot; never store estate data in Git."""
import argparse, calendar, copy, hashlib, json, re
from datetime import datetime, timezone
from pathlib import Path

def prepare(base, source):
    assert base['estateId'] == 'selendang-3'
    def rows(section):
        text = source.split(section, 1)[1].split('\n---', 1)[0]
        return [[c.strip() for c in line.strip().strip('|').split('|')] for line in text.splitlines() if re.match(r'\| PM\d', line)]
    history = rows('## Crop History')
    production = {r[0]: r for r in rows('## Selendang 3 - Crop Production (Total Tonnes) for 2026')}
    fields = next(t for t in base['sections'] if t['id'] == 'fields')
    codeidx, areaidx = fields['columns'].index('Field code'), fields['columns'].index('Area (ha)')
    areas = {}
    for row in fields['rows']:
        assert row[codeidx] not in areas, 'Duplicate field area'
        areas[row[codeidx]] = row[areaidx]
    records=[]; cutoffs=set()
    codes=['PM04D','PM05E','PM06F','PM08G','PM10K','PM11M','PM15P']
    assert [r[0] for r in history] == codes and set(production) == set(codes)
    for row in history:
        assert len(row)==6, 'Expected all five historical years'
        code=row[0]; values=production[code][1:13]
        months=[i+1 for i,v in enumerate(values) if v]
        assert months == list(range(1,max(months)+1)), 'Incomplete cumulative months'
        month=max(months); cutoffs.add(f'2026-{month:02d}-{calendar.monthrange(2026,month)[1]}')
        tonnes=sum(float(v.replace(',','')) for v in values if v)
        total=float(production[code][13].replace(',',''))
        assert abs(tonnes-total)<0.011, f'{code}: monthly sum differs from total'
        area=areas.get(code)
        if area is not None:
            assert isinstance(area,(int,float)) and area>0
        records.append(dict(fieldCode=code,annual={str(y):None if v=='N/A' else float(v) for y,v in zip(range(2021,2026),row[1:])},productionYear=2026,productionTonnes=total,plantedAreaHa=area,areaSource='Current operational fields table · Area (ha); captured from snapshot revision '+str(base['revision'])))
    assert len(cutoffs)==1
    result=copy.deepcopy(base)
    result['cropHistory']=dict(cutoff=cutoffs.pop(),source='Crop_Production.md',fields=records)
    result['revision']+=1
    result['exportedAt']=datetime.now(timezone.utc).isoformat().replace('+00:00','Z')
    assert all(result[k]==v for k,v in base.items() if k not in ('revision','exportedAt','cropHistory'))
    return result

if __name__=='__main__':
    p=argparse.ArgumentParser(); p.add_argument('base'); p.add_argument('source'); p.add_argument('output'); p.add_argument('--expected-sha256',required=True); a=p.parse_args()
    raw=Path(a.base).read_bytes(); assert hashlib.sha256(raw).hexdigest()==a.expected_sha256, 'Base changed'
    result=prepare(json.loads(raw),Path(a.source).read_text())
    with open(a.output,'x') as f: json.dump(result,f,indent=2,allow_nan=False); f.write('\n')
