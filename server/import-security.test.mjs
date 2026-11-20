import test from 'node:test';
import assert from 'node:assert/strict';
import {mkdtemp, writeFile, rm, mkdir} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {createHash, randomUUID} from 'node:crypto';
import {readValidatedSnapshotFile} from './read-snapshot-file.mjs';
import {MAX_BYTES, SECTION_IDS} from './validate.mjs';
import {createEstateServer} from './server.mjs';

const snapshot = () => ({
  schemaVersion: 1, estateId: 'selendang-3', revision: 1,
  reportingPeriod: 'Fictional test', exportedAt: '2026-10-21T00:00:00Z',
  sections: SECTION_IDS.map(id => ({id, columns: ['Value'], rows: []}))
});

test('publisher input accepts valid JSON and rejects oversize, malformed, wrong estate and directories', async () => {
  const dir = await mkdtemp(`${tmpdir()}/s3-import-`);
  const path = `${dir}/snapshot.json`;
  try {
    const valid = Buffer.from(JSON.stringify(snapshot()));
    await writeFile(path, valid);
    assert.deepEqual(await readValidatedSnapshotFile(path), valid);
    await writeFile(path, Buffer.alloc(MAX_BYTES + 1, 0x20));
    await assert.rejects(readValidatedSnapshotFile(path), /2 MB/);
    await writeFile(path, '{');
    await assert.rejects(readValidatedSnapshotFile(path), SyntaxError);
    await writeFile(path, JSON.stringify({...snapshot(), estateId: 'other'}));
    await assert.rejects(readValidatedSnapshotFile(path), /Invalid schema or estate/);
    await assert.rejects(readValidatedSnapshotFile(dir), /regular file/);
  } finally {
    await rm(dir, {recursive: true, force: true});
  }
});

test('production API rejects unauthorized, non-JSON and oversized uploads', async () => {
  const dir = await mkdtemp(`${tmpdir()}/s3-upload-`);
  await mkdir(`${dir}/production`);
  const tokenPath = `${dir}/tokens.json`;
  const dataPath = `${dir}/snapshot.json`;
  const token = 'writer-token';
  await writeFile(tokenPath, JSON.stringify([{id:'writer',sha256:createHash('sha256').update(token).digest('hex'),scopes:['production:write']}]));
  const server = createEstateServer({tokenPath,dataPath});
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const url = `http://127.0.0.1:${server.address().port}/v1/estates/selendang-3/production`;
  const body = JSON.stringify({schemaVersion:1,estateId:'selendang-3',changes:[{record:{date:'2026-01-01',field:'PM04D',sentToMill:1},baseVersion:0,mutationId:randomUUID()}]});
  try {
    assert.equal((await fetch(url,{method:'POST',headers:{'Content-Type':'application/json'},body})).status,401);
    assert.equal((await fetch(url,{method:'POST',headers:{Authorization:`Bearer ${token}`,'Content-Type':'text/plain'},body})).status,415);
    assert.equal((await fetch(url,{method:'POST',headers:{Authorization:`Bearer ${token}`,'Content-Type':'application/json'},body:'{'})).status,400);
    assert.equal((await fetch(url,{method:'POST',headers:{Authorization:`Bearer ${token}`,'Content-Type':'application/json; charset=UTF-8'},body:'null'})).status,400);
    assert.equal((await fetch(url,{method:'POST',headers:{Authorization:`Bearer ${token}`,'Content-Type':'application/json'},body:' '.repeat(MAX_BYTES + 1)})).status,413);
    const state = await (await fetch(url,{headers:{Authorization:`Bearer ${token}`}})).json();
    assert.equal(state.revision,0);
  } finally {
    await new Promise(resolve => server.close(resolve));
    await rm(dir,{recursive:true,force:true});
  }
});
