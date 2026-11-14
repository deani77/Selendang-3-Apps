import {open} from 'node:fs/promises';
import {MAX_BYTES, validateSnapshot} from './validate.mjs';

// Bound memory even if a file changes size between inspection and reading.
export async function readValidatedSnapshotFile(path) {
  const handle = await open(path, 'r');
  try {
    const info = await handle.stat();
    if (!info.isFile()) throw new Error('Snapshot source must be a regular file');
    if (info.size > MAX_BYTES) throw new Error('Snapshot exceeds 2 MB');
    const chunks = [];
    const buffer = Buffer.allocUnsafe(8192);
    let size = 0;
    while (true) {
      const {bytesRead} = await handle.read(buffer, 0, Math.min(buffer.length, MAX_BYTES + 1 - size), null);
      if (bytesRead === 0) break;
      size += bytesRead;
      if (size > MAX_BYTES) throw new Error('Snapshot exceeds 2 MB');
      chunks.push(Buffer.from(buffer.subarray(0, bytesRead)));
    }
    const bytes = Buffer.concat(chunks, size);
    validateSnapshot(bytes);
    return bytes;
  } finally {
    await handle.close();
  }
}
