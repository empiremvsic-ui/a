# Unreel for iPhone

One app with Instagram, YouTube and Facebook inside it, with Reels and Shorts taken out.
Delete the real apps and use this instead. TikTok isn't included on purpose.

- Everything runs on your iPhone. Logins are stored on the phone only.
- No third-party code: just Apple's SwiftUI and WebKit.
- The blocking rules are in `Unreel/Web/unreel.js`, `Unreel/Web/unreel.css` and `Unreel/Site.swift`.

## Install on your iPhone (needs a Mac)

1. Install Xcode from the Mac App Store (free).
2. Download `Unreel-ios-project.zip` from this repo's **ios-latest** release and unzip it.
3. Open `Unreel.xcodeproj`. In Xcode, click the Unreel project → Signing & Capabilities,
   pick your Apple ID as the Team, and change the bundle ID to something unique,
   e.g. `com.yourname.unreel`.
4. Plug in your iPhone, pick it at the top of Xcode, and press ▶ Run.
5. On the iPhone: Settings → General → VPN & Device Management → trust your Apple ID.
   Also turn on Developer Mode if asked (Settings → Privacy & Security).

With a free Apple ID the app stops opening after 7 days; press Run again to renew it.
A paid Apple Developer account ($99/year) makes it last a year.

## Known limits

- No push notifications (websites inside apps can't get them on iOS).
- Reels/Shorts can still appear if a site changes its layout. Fix the rules in the files above.
