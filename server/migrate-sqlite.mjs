import {DatabaseSync} from 'node:sqlite';
import {readFile, stat, rename, unlink, chmod} from 'node:fs/promises';
import {randomUUID} from 'node:crypto';
import {dirname, basename, join} from 'node:path';
import {pathToFileURL} from 'node:url';
import {validateSnapshot} from './validate.mjs';
import {validateRecord, recordKey} from './production.mjs';

/**
 * Offline, one-time migration preparation. It NEVER changes the current server
 * API or JSON data. Database is a new file and is not opened by server.mjs.
 */
export async function prepareSqlite({snapshotPath, productionPath, databasePath}) {
  if (!snapshotPath || !productionPath || !databasePath) throw Error('Three paths required');
  try { await stat(databasePath); throw Error('Refusing to overwrite existing SQLite database'); }
  catch (error) { if (error.code !== 'ENOENT') throw error; }
  const snapshot = validateSnapshot(await readFile(snapshotPath));
  const source = JSON.parse(await readFile(productionPath, 'utf8'));
  if (source.schemaVersion !== 1 || !Number.isSafeInteger(source.revision) ||
      source.revision < 0 || !Array.isArray(source.records) || !Array.isArray(source.audit))
    throw Error('Invalid production storage');
  const keys = new Set();
  for (const entry of source.records) {
    const rec = validateRecord(entry?.record);
    const key = recordKey(rec);
    if (keys.has(key) || !Number.isSafeInteger(entry.version) || entry.version < 1 ||
        entry.version > source.revision || typeof entry.mutationId !== 'string')
      throw Error('Invalid production entry');
    keys.add(key);
  }
  const temporary = join(dirname(databasePath), `.${basename(databasePath)}.${randomUUID()}.tmp`);
  let db;
  try {
    db = new DatabaseSync(temporary);
    await chmod(temporary, 0o600);
    db.exec(`PRAGMA journal_mode=DELETE;
      CREATE TABLE metadata (name TEXT PRIMARY KEY, value TEXT NOT NULL);
      CREATE TABLE snapshots (revision INTEGER PRIMARY KEY, payload TEXT NOT NULL);
      CREATE TABLE production_records (
        record_key TEXT PRIMARY KEY, version INTEGER NOT NULL,
        mutation_id TEXT NOT NULL, payload TEXT NOT NULL
      );
      CREATE TABLE production_audit (seq INTEGER PRIMARY KEY, payload TEXT NOT NULL);`);
    db.exec('BEGIN IMMEDIATE');
    const meta = db.prepare('INSERT INTO metadata (name,value) VALUES (?,?)');
    meta.run('schema_version', '1');
    meta.run('estate_id', 'selendang-3');
    meta.run('production_revision', String(source.revision));
    db.prepare('INSERT INTO snapshots (revision,payload) VALUES (?,?)')
      .run(snapshot.revision, JSON.stringify(snapshot));
    const insert = db.prepare('INSERT INTO production_records (record_key,version,mutation_id,payload) VALUES (?,?,?,?)');
    for (const entry of source.records)
      insert.run(recordKey(entry.record), entry.version, entry.mutationId, JSON.stringify(entry));
    const audit = db.prepare('INSERT INTO production_audit (seq,payload) VALUES (?,?)');
    for (let i = 0; i < source.audit.length; i++) audit.run(i + 1, JSON.stringify(source.audit[i]));
    db.exec('COMMIT');
    const checked = db.prepare('PRAGMA integrity_check').get();
    if (checked.integrity_check !== 'ok') throw Error('SQLite integrity check failed');
    db.close(); db = null;
    // Refuse existing target before publication; this utility must be run while no other importer is active.
    try { await stat(databasePath); throw Error('Destination appeared during migration'); }
    catch (error) { if (error.code !== 'ENOENT') throw error; }
    await rename(temporary, databasePath);
    return {snapshotRevision:snapshot.revision, productionRevision:source.revision,
      productionRecords:source.records.length, auditEntries:source.audit.length};
  } catch (error) {
    if (db) db.close();
    await unlink(temporary).catch(() => {});
    throw error;
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const [snapshotPath, productionPath, databasePath] = process.argv.slice(2);
  prepareSqlite({snapshotPath, productionPath, databasePath})
    .then(result => console.log(JSON.stringify(result)))
    .catch(error => { console.error(error.message); process.exitCode = 1; });
}
