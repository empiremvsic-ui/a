// ==UserScript==
// @name         Unreel
// @namespace    unreel.local
// @version      1.0.0
// @description  Hides Instagram Reels, YouTube Shorts and Facebook Reels, and blocks TikTok. No network, no storage, no tracking.
// @match        https://*.instagram.com/*
// @match        https://*.youtube.com/*
// @match        https://*.facebook.com/*
// @match        https://*.tiktok.com/*
// @run-at       document-start
// @noframes
// @grant        none
// ==/UserScript==

/*
 * SECURITY NOTES (read these, it's short):
 * - "@grant none": the script gets no special powers from the userscript app. It cannot
 *   make its own network requests, read other sites, or touch your files.
 * - No @require / @resource / @updateURL / @downloadURL: it never downloads code.
 *   It only changes if YOU replace this file.
 * - It runs only on the four sites in the @match lines, only over https, never in iframes.
 * - It doesn't use fetch, XMLHttpRequest, WebSocket, cookies, localStorage, eval,
 *   new Function or innerHTML. It reads the page address and hides elements. That's all.
 */
(function () {
  "use strict";

  const host = location.hostname;
  const on = (domain) => host === domain || host.endsWith("." + domain);

  // ---------- TikTok: it's all short videos, so block the whole site ----------
  if (on("tiktok.com")) {
    try { window.stop(); } catch (_) {}
    const show = () => {
      const doc = document.documentElement || document.appendChild(document.createElement("html"));
      while (doc.firstChild) doc.removeChild(doc.firstChild);
      const body = document.createElement("body");
      body.setAttribute("style",
        "margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;" +
        "font:600 20px -apple-system,system-ui,sans-serif;background:#111;color:#eee;text-align:center;padding:24px");
      body.textContent = "TikTok is blocked by Unreel.";
      doc.appendChild(body);
    };
    show();
    // TikTok may try to re-render; keep it blocked.
    new MutationObserver(() => {
      if (!document.body || document.body.textContent !== "TikTok is blocked by Unreel.") show();
    }).observe(document, { childList: true, subtree: true });
    return;
  }

  // ---------- Hide Reels / Shorts entry points ----------
  const CSS = [
    // Instagram
    'a[href^="/reels"]', 'a[href^="/reel/"]', 'a[href*="instagram.com/reels"]', 'a[href*="instagram.com/reel/"]',
    // YouTube (mobile + desktop)
    "ytm-reel-shelf-renderer", "ytd-reel-shelf-renderer",
    "ytm-shorts-lockup-view-model", "ytm-shorts-lockup-view-model-v2", "ytd-rich-shelf-renderer[is-shorts]",
    "grid-shelf-view-model", "ytm-pivot-bar-item-renderer:has(.pivot-shorts)",
    'ytd-guide-entry-renderer:has(a[title="Shorts"])', 'ytd-mini-guide-entry-renderer[aria-label="Shorts"]',
    'a[href^="/shorts"]', 'a[href*="youtube.com/shorts"]',
    // Facebook
    'a[href*="/reel/"]', 'a[href*="/reels/"]',
  ].join(",\n") + " { display: none !important; }";

  const addStyle = () => {
    const root = document.head || document.documentElement;
    if (!root) return false;
    const style = document.createElement("style");
    style.textContent = CSS;
    root.appendChild(style);
    // Some sites rebuild <head>; put the style back if it gets removed.
    new MutationObserver(() => { if (!style.isConnected) (document.head || document.documentElement).appendChild(style); })
      .observe(document.documentElement, { childList: true, subtree: true });
    return true;
  };
  if (!addStyle()) {
    const wait = new MutationObserver(() => { if (addStyle()) wait.disconnect(); });
    wait.observe(document, { childList: true });
  }

  // ---------- Leave the Reels / Shorts player if you land on it ----------
  const isShortForm = (path) => {
    path = path.toLowerCase();
    if (on("instagram.com")) return /^\/reels?(\/|$)/.test(path);
    if (on("youtube.com")) return /^\/shorts(\/|$)/.test(path);
    if (on("facebook.com")) return /^\/reels?(\/|$)/.test(path) || path.startsWith("/watch/reels");
    return false;
  };

  let last = "";
  const check = () => {
    if (location.href === last) return;
    last = location.href;
    if (isShortForm(location.pathname)) location.replace("/");
  };

  // These sites switch pages without reloading, so catch every address change.
  for (const name of ["pushState", "replaceState"]) {
    const original = history[name];
    history[name] = function () {
      const result = original.apply(this, arguments);
      check();
      return result;
    };
  }
  window.addEventListener("popstate", check);
  setInterval(check, 400);
  check();
})();
