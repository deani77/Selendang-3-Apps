# HTTPS and security headers — audit Item 2

The public TLS endpoint is Caddy (`server/deploy/Caddyfile`). The Node API listens on `127.0.0.1:8787` only.

## Configured headers

- `Strict-Transport-Security: max-age=86400` — conservative 24-hour HSTS trial, with no `includeSubDomains` or `preload`.
- `X-Content-Type-Options: nosniff`
- `Referrer-Policy: no-referrer`
- `Cache-Control: no-store`
- `Content-Security-Policy: default-src 'none'`

HSTS is sent by the HTTPS reverse proxy, **not** by the local HTTP Node API. The Node API independently sends `no-store` and `nosniff`. The static test verifies the repository configuration; it does not establish that the live VPS has deployed it.

## Deployment checklist (administrator action required)

1. Confirm the intended hostname and certificate, and that all traffic to this hostname can use HTTPS. Check that unrelated services/subdomains are unaffected.
2. Copy the reviewed Caddyfile to `/etc/s3-estate/Caddyfile` on the VPS. Do not overwrite unrelated Caddy configuration.
3. Validate the live configuration with Caddy's config validation command before a reload; preserve a backup and reload only if validation succeeds.
4. Check HTTPS with `curl -I https://YOUR_HOSTNAME/health` (or `curl -sS -D - -o /dev/null https://YOUR_HOSTNAME/health`). Verify HSTS, CSP, no-store, nosniff and referrer policy. Verify HTTP redirects to HTTPS.
5. Confirm Android/iOS snapshot downloads still work and monitor for TLS or routing errors.

Do not raise `max-age` to 1 year, add `includeSubDomains`, or request browser preload until the hostname's complete HTTPS coverage and migration plan are verified.

This PR does not change the live VPS and does not assert a live TLS/certificate test has passed.
