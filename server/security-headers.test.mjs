import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import {createEstateServer} from './server.mjs';

test('Caddy TLS configuration sets conservative security headers', async () => {
  const config = await readFile(fileURLToPath(new URL('./deploy/Caddyfile', import.meta.url)), 'utf8');
  assert.match(config, /Strict-Transport-Security\s+"max-age=86400"/);
  assert.doesNotMatch(config, /includeSubDomains|preload/i);
  assert.match(config, /X-Content-Type-Options\s+nosniff/);
  assert.match(config, /Referrer-Policy\s+no-referrer/);
  assert.match(config, /Cache-Control\s+no-store/);
  assert.match(config, /Content-Security-Policy\s+"default-src 'none'"/);
  assert.match(config, /reverse_proxy\s+127\.0\.0\.1:8787/);
});

test('Node API sends no-store and nosniff on responses', async () => {
  const server = createEstateServer({dataPath: '/nonexistent/snapshot.json', tokenPath: '/nonexistent/tokens.json'});
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  try {
    const response = await fetch(`http://127.0.0.1:${server.address().port}/health`);
    assert.equal(response.status, 200);
    assert.equal(response.headers.get('cache-control'), 'no-store');
    assert.equal(response.headers.get('x-content-type-options'), 'nosniff');
  } finally {
    await new Promise((resolve, reject) => server.close(error => error ? reject(error) : resolve()));
  }
});
