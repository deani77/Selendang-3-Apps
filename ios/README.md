# Selendang 3 for iPhone and iPad

Native SwiftUI starter in the same repository as Android. Android stays in its existing root folders; no migration is required. Requires iOS 16 or later.

## Build on a Mac

1. Install Xcode, launch it once and complete its setup.
2. Install XcodeGen using Homebrew: `brew install xcodegen`.
3. From this folder run `xcodegen generate`.
4. Open `Selendang3.xcodeproj`, select the Selendang3 scheme and an iPhone simulator, then Run.
5. To run on an actual iPhone, select your Apple development team in Signing & Capabilities and use a unique bundle identifier if needed. Credentials and team identifiers are not committed here.

XcodeGen is a build-time utility, not an app dependency: https://github.com/yonaskolb/XcodeGen (MIT licence). No third-party application source was copied.

## Included

Home navigation matching the Android modules, a SwiftUI dashboard covering the current Android slide 1 panels, and the S3 oil palm launcher artwork. iPad layouts adapt to wider screens. All figures remain pending. Chart areas are placeholders; data entry, persistence, authentication, calculations and server sync are not implemented. Other modules are labelled placeholders.

The current reference age bands omit some ages; confirm the full classification before data entry. Do not infer LF's meaning or reporting dates.

## Validation and distribution

The iOS workflow generates the project and compiles for the simulator without signing. It does not distribute an installable iPhone app and does not perform device or visual tests. A Mac with Xcode is needed for device testing and signing. No Apple Developer credentials, server endpoints or private estate data are included.

Android and iOS currently share designs and concepts, not executable business logic. A common server and data contract can be added after requirements are agreed.

The 1024px icon source is in Artwork/S3.jpg. Project generation uses macOS sips to convert it to an opaque PNG in the app icon asset catalog before building.
