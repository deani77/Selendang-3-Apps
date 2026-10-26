# Estate name from server data

The home page and Estate Details heading use the `Estate name` row from the synced `overview` section (`Item` and `Details` columns). The app does not contain a fallback estate name.

Before estate data has been synced, home shows `ESTATE INFORMATION` and Estate Details shows `ESTATE DETAILS`. A saved snapshot from an earlier successful sync can supply the name offline. The UI observes snapshot changes, so a later sync can update the name without an app rebuild.

The name must be present once, nonblank, at most 120 UTF-16 code units, and free of internal control characters. Missing or ambiguous names use the neutral headings. Older snapshot formats remain readable.

This changes displayed estate branding on iPhone, iPad and Android. The application name, icon, bundle/package identifiers and trusted sync endpoint remain as configured.
