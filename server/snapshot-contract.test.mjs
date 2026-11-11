import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {validateSnapshot, MAX_BYTES} from './validate.mjs';

const corpus = JSON.parse(readFileSync(new URL('../test-data/snapshots/cases.json', import.meta.url), 'utf8'));
for (const {name, valid, overrides} of corpus.cases) {
  test(`shared snapshot: ${name}`, () => {
    const bytes = Buffer.from(JSON.stringify({...corpus.base, ...overrides}));
    if (valid) assert.doesNotThrow(() => validateSnapshot(bytes));
    else assert.throws(() => validateSnapshot(bytes));
  });
}
test('snapshot byte and UTF-8 limits', () => {
  const bytes = Buffer.from(JSON.stringify(corpus.base));
  assert.doesNotThrow(() => validateSnapshot(Buffer.concat([bytes, Buffer.alloc(MAX_BYTES - bytes.length, 32)])));
  assert.throws(() => validateSnapshot(Buffer.alloc(MAX_BYTES + 1, 32)));
  assert.throws(() => validateSnapshot(Buffer.concat([bytes, Buffer.from([0xff])])));
});

test('snapshot total row limit', () => {
  const snapshot = structuredClone(corpus.base);
  snapshot.sections[0].rows = Array.from({length: 10000}, () => ['value', null]);
  snapshot.sections.slice(1).forEach(section => { section.rows = []; });
  assert.doesNotThrow(() => validateSnapshot(Buffer.from(JSON.stringify(snapshot))));
  snapshot.sections[1].rows = [['value', null, 0]];
  assert.throws(() => validateSnapshot(Buffer.from(JSON.stringify(snapshot))));
});
