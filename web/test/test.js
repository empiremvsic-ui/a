// Runs the userscript in real Chromium against fake copies of the sites (no real network).
// Usage: node web/test/test.js
const { chromium } = require("playwright");
const fs = require("fs");
const path = require("path");
const script = fs.readFileSync(path.join(__dirname, "..", "unreel.user.js"), "utf8");

const page_ = (body) => `<!doctype html><html><head></head><body>${body}
<script>window.__go = (p) => history.pushState({}, "", p);</script></body></html>`;

const FEED = page_(`
  <a id="reels" href="/reels/">Reels</a><a id="reel" href="/reel/abc/">a reel</a>
  <a id="shorts" href="/shorts/xyz">Short</a><ytm-reel-shelf-renderer id="shelf">shelf</ytm-reel-shelf-renderer>
  <a id="post" href="/p/123/">normal post</a><a id="video" href="/watch?v=1">normal video</a>`);

let failures = 0;
const ok = (cond, msg) => { console.log((cond ? "PASS " : "FAIL ") + msg); if (!cond) failures++; };

// Static check: the code (comments stripped) must not contain anything that can talk to the
// network, store data, or run strings as code. Also no remote-code headers.
const code = script.replace(/\/\*[\s\S]*?\*\//g, "").replace(/^\s*\/\/(?! @).*$/gm, "");
for (const bad of ["fetch", "XMLHttpRequest", "WebSocket", "sendBeacon", "EventSource", "localStorage",
  "sessionStorage", "indexedDB", "document.cookie", "eval(", "Function(", "innerHTML", "outerHTML",
  "insertAdjacentHTML", "import(", "@require", "@resource", "@updateURL", "@downloadURL", "@connect", "GM_", "GM."]) {
  ok(!code.includes(bad), `Script does not use ${bad}`);
}
ok(/@grant\s+none/.test(script), "Script has @grant none");

(async () => {
  const browser = await chromium.launch();
  const ctx = await browser.newContext();
  const requests = [];
  await ctx.route("**/*", (route) => {
    requests.push(route.request().url());
    route.fulfill({ contentType: "text/html", body: FEED });
  });
  await ctx.addInitScript(script); // same timing as @run-at document-start

  const p = await ctx.newPage();
  const visible = (sel) => p.$eval(sel, (e) => getComputedStyle(e).display !== "none");

  // Instagram
  await p.goto("https://www.instagram.com/");
  ok(!(await visible("#reels")), "Instagram: Reels tab hidden");
  ok(!(await visible("#reel")), "Instagram: reel link hidden");
  ok(await visible("#post"), "Instagram: normal post still visible");
  await p.goto("https://www.instagram.com/reels/abc/");
  await p.waitForURL("https://www.instagram.com/", { timeout: 3000 }).catch(() => {});
  ok(p.url() === "https://www.instagram.com/", "Instagram: opening /reels/ sends you home");
  await p.evaluate(() => window.__go("/reel/xyz/"));
  await p.waitForURL("https://www.instagram.com/", { timeout: 3000 }).catch(() => {});
  ok(p.url() === "https://www.instagram.com/", "Instagram: in-app navigation to a reel sends you home");
  await p.evaluate(() => window.__go("/p/123/"));
  await p.waitForTimeout(600);
  ok(p.url() === "https://www.instagram.com/p/123/", "Instagram: normal posts still open");

  // YouTube
  await p.goto("https://m.youtube.com/");
  ok(!(await visible("#shorts")), "YouTube: Shorts link hidden");
  ok(!(await visible("#shelf")), "YouTube: Shorts shelf hidden");
  ok(await visible("#video"), "YouTube: normal video still visible");
  await p.goto("https://m.youtube.com/shorts/xyz");
  await p.waitForURL("https://m.youtube.com/", { timeout: 3000 }).catch(() => {});
  ok(p.url() === "https://m.youtube.com/", "YouTube: opening a Short sends you home");
  await p.goto("https://m.youtube.com/watch?v=1");
  await p.waitForTimeout(600);
  ok(p.url() === "https://m.youtube.com/watch?v=1", "YouTube: normal videos still open");

  // Facebook
  await p.goto("https://m.facebook.com/reel/123");
  await p.waitForURL("https://m.facebook.com/", { timeout: 3000 }).catch(() => {});
  ok(p.url() === "https://m.facebook.com/", "Facebook: opening a reel sends you home");

  // TikTok
  await p.goto("https://www.tiktok.com/foryou", { waitUntil: "commit" });
  await p.waitForTimeout(500);
  await p.waitForTimeout(300);
  ok((await p.textContent("body")).includes("TikTok is blocked"), "TikTok: site blocked");

  // Not touched on other sites (script would not be installed there anyway)
  await p.goto("https://example.com/reels/");
  await p.waitForTimeout(600);
  ok(p.url() === "https://example.com/reels/", "Other sites: untouched");

  // The script itself must never make a request of its own.
  const unexpected = requests.filter((u) => !/^https:\/\/(www\.instagram|m\.youtube|m\.facebook|www\.tiktok|example)\.com\//.test(u));
  ok(unexpected.length === 0, "No requests to anywhere else" + (unexpected.length ? ": " + unexpected.join(", ") : ""));

  await browser.close();
  console.log(failures ? `\n${failures} failed` : "\nAll passed");
  process.exit(failures ? 1 : 0);
})();
