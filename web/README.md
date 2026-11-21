# Unreel for Safari (iPhone, iPad, Mac)

Instagram, YouTube and Facebook in Safari without Reels or Shorts, and TikTok blocked.
It never expires and needs no computer or Apple developer account.

It's one file, `unreel.user.js`, about 120 lines. You can read all of it in a few minutes.

## Install (about 5 minutes)

1. Install **Userscripts** from the App Store (free and open source: github.com/quoid/userscripts).
2. Open the Userscripts app, tap **Set Userscripts Directory**, and pick a new empty folder
   in Files (e.g. *On My iPhone → Userscripts*).
3. Settings → **Apps → Safari → Extensions → Userscripts**: turn it on. Under
   **Permissions**, do **not** choose "All Websites". Allow only:
   `instagram.com`, `youtube.com`, `facebook.com`, `tiktok.com`.
4. In Safari, open this link. It's pinned to an exact commit, so its contents can never change:
   https://raw.githubusercontent.com/empiremvsic-ui/a/7c80a67c7d042d1c236d1bdf4608874af35baa8e/web/unreel.user.js
5. Tap the **puzzle icon / AA** in the address bar → **Userscripts** → **Install**.
6. Delete the Instagram, YouTube, Facebook and TikTok apps. In Safari, open each site,
   log in, then Share → **Add to Home Screen** for an app-like icon.

## Verify it's the real file

SHA-256 of version 1.0.0:

```
898a1c427021c29b2453caf496cf322128c6655d323de22ad5ff40da7949e882
```

On a computer: `shasum -a 256 unreel.user.js` (Mac) or `sha256sum unreel.user.js` (Linux)
should print exactly that. On iPhone: the file in your Userscripts folder should match
the one in this repo line for line.

## What the script can and can't do

| | |
| --- | --- |
| Runs on | Only https pages of the 4 sites above, never inside iframes |
| Network | None. It has `@grant none` and never calls fetch, XMLHttpRequest, WebSocket or beacons |
| Storage | None. No cookies, localStorage or IndexedDB |
| Updates | None. No `@updateURL` or `@require`, so it never downloads code. It changes only if you replace the file |
| Dangerous code | No eval, new Function or innerHTML |

`web/test/test.js` checks all of this automatically. It runs the script in a real browser,
fails if the script makes any request of its own, and fails if any of the APIs above
show up in the code. Run it with `node web/test/test.js` (needs Node and Playwright).

## Lock down the rest (recommended)

The script adds no risk, but Instagram, YouTube and Facebook themselves still track you.
These settings cut that down a lot:

**Keep social sites in their own Safari profile (iOS 17+)**
Settings → Apps → Safari → **New Profile** → name it "Social". Use that profile only for
these sites, and turn on Userscripts just for it. Their cookies then can't follow you
around your normal browsing.

**Safari settings** (Settings → Apps → Safari)
- **Prevent Cross-Site Tracking:** on
- **Hide IP Address:** "Trackers and Websites" (needs iCloud+ Private Relay)
- **Advanced → Advanced Tracking and Fingerprinting Protection:** "All Browsing"
- **Require Face ID to Unlock Private Browsing:** on

**Your accounts**
- Use a unique password for each one, saved in Passwords. Use a passkey if offered.
- Turn on two-factor login with an authenticator app or passkey, **not SMS**.
- Instagram: Settings → Accounts Center → Password and security → Where you're logged in.
  Log out of devices you don't recognise.
- Google: myaccount.google.com → Security Checkup.

**Why deleting the official apps matters**
The apps can use your contacts, photos, location, Bluetooth, device IDs and background
activity. In Safari they get only what the website can see. That's the biggest privacy gain
here, bigger than anything the script does.

**What this can't protect you from**
Meta and Google still see everything you do *on* their sites: what you look at, like, post
and message. No script can change that. The only fix is not using them.
