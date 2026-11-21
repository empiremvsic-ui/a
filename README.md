# Unreel

Your own Reels/Shorts blocker for Android. Keep using Instagram and YouTube for messages,
posts and normal videos, without falling into Reels or Shorts.

## Why you can trust it

- **No internet permission.** Look at `app/src/main/AndroidManifest.xml`: there's no
  `INTERNET` permission, so Android won't let it send anything anywhere.
- **No third-party libraries.** Only Android and the Kotlin standard library.
- **Only sees the apps it blocks.** `res/xml/blocker_service.xml` limits it to Instagram,
  YouTube, Facebook, TikTok and Snapchat. Android sends it nothing from your other apps.
- **Small enough to read.** All the logic is in 4 files under `app/src/main/java/com/unreel/app/`.

## What it does

| App | What gets blocked |
| --- | --- |
| Instagram | The Reels player (Reels tab, and reels opened from DMs or the feed). It presses Back for you. |
| YouTube | The Shorts player. It presses Back for you. |
| TikTok (and TikTok Lite) | The whole app, since it's all short videos. On by default. It sends you to your home screen. |
| Facebook, Snapchat | Optional: block the whole app (off by default). It goes to your home screen. |

If Back doesn't get you out (for example when the app opened straight into Reels), it sends you
to your home screen instead. There's a 5 or 15 minute pause button if you really need one.

## Install it

1. On your Android phone, open this repo's **Releases** page and download `Unreel.apk`
   from the **latest** release. (Every push rebuilds it with GitHub Actions.)
2. Open the file and allow installing from your browser/files app when asked.
3. Open Unreel and tap **Turn on in Accessibility settings**, then turn on **Unreel blocker**.
4. On Android 13+, if the switch is greyed out ("restricted setting"): go to
   **Settings → Apps → Unreel → ⋮ → Allow restricted settings**, then do step 3 again.

Updating: builds are signed with a temporary debug key, so to install a newer build you may
need to uninstall the old one first.

## If an app update breaks blocking

Instagram and YouTube sometimes rename their internal screen parts. You can fix this without
rebuilding:

1. In Unreel, turn on **Capture screen IDs**.
2. Open a Reel/Short, come back to Unreel, tap **Refresh**.
3. Find an ID that only shows up on the Reels/Shorts player (e.g. something with `clips` or `reel`),
   add it under **Detection**, and tap **Save**.
4. Turn Capture off.

## Build it yourself

With Android Studio: open this folder and press Run. Or from a terminal with the Android SDK
installed: `gradle assembleDebug`.

iPhone isn't supported: iOS doesn't let one app see or control another app's screen.
