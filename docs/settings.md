# Settings on Android and iOS

Both interfaces expose User Profile, Appearance, Inbox and Suggestions.

Profile fields: display name (required for saving), position and phone (optional). Save Profile stores one local device profile, not an authenticated user identity or an authorization role. Profile edits are private device preferences; they are not uploaded to GitHub or a server. Account-specific profiles and sign-in await the central service. Profile data may be lost when the app is removed or its storage is cleared.

Appearance: Use device setting (default), Light and Dark. Preferences survive app restarts. Theme changes apply to screens and the dashboard, retaining fixed brand accents and readable panel headings.

Inbox and Suggestions are explicit placeholders. No messages or suggestions are sent or received. Online data synchronization remains unconfigured.

Manual checks on each platform: save profile and restart, edit profile without saving, switch each theme and restart, verify dashboard text contrast, open both placeholders, and return to Settings/home. Server functionality is outside this change.
