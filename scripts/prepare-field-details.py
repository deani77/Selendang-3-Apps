"""Merge private profiles into a local snapshot; never publishes or overwrites inputs."""
import argparse
import hashlib
import json
from pathlib import Path

p = argparse.ArgumentParser()
p.add_argument('snapshot', type=Path)
p.add_argument('profiles', type=Path)
p.add_argument('output', type=Path)
p.add_argument('--expected-sha256', required=True)
a = p.parse_args()
raw = a.snapshot.read_bytes()
if hashlib.sha256(raw).hexdigest() != a.expected_sha256:
    p.error('Snapshot hash changed; inspect the current snapshot first.')
base = json.loads(raw)
source = json.loads(a.profiles.read_text())
if base.get('estateId') != 'selendang-3' or source.get('estateId') != base['estateId']:
    p.error('Wrong estate.')
fields = next(t for t in base['sections'] if t['id'] == 'fields')
column = fields['columns'].index('Field code')
codes = {r[column] for r in fields['rows']}
profiles = source['fieldProfiles']
if len({x['fieldCode'] for x in profiles}) != len(profiles):
    p.error('Duplicate source field codes.')
if not {x['fieldCode'] for x in profiles} <= codes:
    p.error('Source PM codes do not match snapshot field codes. Review the mapping.')
if 'fieldProfiles' in base:
    p.error('Snapshot already has fieldProfiles; review before replacing.')
base['fieldProfiles'] = profiles
# Publication owns revision assignment. Keep every existing section and extension intact.
with a.output.open('x') as out:
    json.dump(base, out, indent=2)
    out.write('\n')
print('Prepared local candidate. Revision unchanged; nothing published.')
