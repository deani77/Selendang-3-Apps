# Settings on Android and iOS

Settings contains Inbox, User Profile, Appearance, About and Estate Data Sync. Android also offers Export Estate Data. These screens are implemented in [UserSettingsScreen.kt](../app/src/main/java/com/selendang3/app/UserSettingsScreen.kt), [EstateDataScreen.kt](../app/src/main/java/com/selendang3/app/EstateDataScreen.kt) and [UserSettingsView.swift](../ios/Selendang3/UserSettingsView.swift).

## Local preferences

User Profile stores a display name, position and phone number on the device. The display name is required to save. This is a local profile, not a signed-in account or an authorization role. Profile edits are not uploaded; removing the app or clearing storage can remove them.

Appearance offers Light, Dark and Follow System, in that order. Follow System is the default. The choice applies immediately and survives restarts. Text, controls and panels should remain readable in all three modes.

Inbox is a placeholder: no messages are sent or received. About displays app information. Suggestions is not exposed in either current interface.

## Estate Data Sync

Enter the administrator's trusted full HTTPS snapshot address and a privately issued access token, then select Sync Now. The address is saved on the device; the token stays in memory for the active Settings session.

A newer snapshot is validated and presented for review. Confirming replaces the offline reference; cancelling keeps the saved data. Failed downloads and rejected updates leave saved records intact. The screen shows saved-revision and check/sync information. See the [snapshot guide](data-updates.md#phone-workflow) for the complete workflow and [server guide](../server/README.md) for administrator operations.

Android's Export Estate Data saves a private copy of the downloaded snapshot through the system file picker. Current screens do not expose manual file import, production entry or production uploads. Retained draft/sync code does not enable those features.

## Manual verification

- Save a profile, restart the app, and confirm the saved fields. Edit without saving and check that the stored profile is unchanged.
- Switch Light, Dark and Follow System; check headings, body text, tables, inputs and secondary notes, then restart to confirm the preference.
- Open Inbox and About and return to Settings/home.
- With a test server and fictional data, check a newer revision's preview, cancellation and confirmed offline availability. Invalid credentials or a failed download must preserve the saved revision.
- On Android, export the saved snapshot and verify its revision and contents in private storage.
