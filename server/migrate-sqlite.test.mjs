import test from 'node:test';
import assert from 'node:assert/strict';
import {mkdtemp, writeFile, rm} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {DatabaseSync} from 'node:sqlite';
import {prepareSqlite} from './migrate-sqlite.mjs';
import {SECTION_IDS} from './validate.mjs';

test('prepares a valid isolated SQLite copy and never overwrites it', async () => {
  const dir = await mkdtemp(join(tmpdir(), 's3-sqlite-'));
  const snapshotPath = join(dir, 'snapshot.json');
  const productionPath = join(dir, 'records.json');
  const databasePath = join(dir, 'estate.sqlite');
  const snapshot = {schemaVersion:1,estateId:'selendang-3',revision:2,
    reportingPeriod:'Fictional test',exportedAt:'2026-10-21T00:00:00Z',
    sections:SECTION_IDS.map(id => ({id,columns:['Value'],rows:[[null],[0]]}))};
  const entry = {record:{date:'2026-10-21',field:'PM04D',harvested:1,sentToMill:1,
    backlog:0,hectares:10,harvesters:2},version:1,mutationId:'fictional-12345678',
    updatedAt:'2026-10-21T00:00:00Z',updatedBy:'test'};
  try {
    await writeFile(snapshotPath, JSON.stringify(snapshot));
    await writeFile(productionPath, JSON.stringify({schemaVersion:1,revision:1,records:[entry],
      audit:[{key:'2026-10-21/PM04D',previous:null,current:entry}]}));
    const result = await prepareSqlite({snapshotPath,productionPath,databasePath});
    assert.deepEqual(result,{snapshotRevision:2,productionRevision:1,productionRecords:1,auditEntries:1});
    const db = new DatabaseSync(databasePath, {readOnly:true});
    assert.equal(db.prepare('SELECT count(*) AS n FROM snapshots').get().n,1);
    assert.equal(db.prepare('SELECT count(*) AS n FROM production_records').get().n,1);
    assert.equal(db.prepare('SELECT count(*) AS n FROM production_audit').get().n,1);
    assert.equal(JSON.parse(db.prepare('SELECT payload FROM snapshots').get().payload).revision,2);
    db.close();
    await assert.rejects(prepareSqlite({snapshotPath,productionPath,databasePath}),/overwrite/);
  } finally {await rm(dir,{recursive:true,force:true});}
});
