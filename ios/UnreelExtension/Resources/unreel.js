// Unreel: runs only inside Safari, only on the sites listed in manifest.json.
// It never sends anything anywhere.
(() => {
  const host = location.hostname;

  // TikTok is all short videos: replace the whole site.
  if (host.endsWith("tiktok.com")) {
    const block = () => {
      document.documentElement.innerHTML =
        '<body style="margin:0;height:100vh;display:flex;align-items:center;justify-content:center;' +
        'font:600 20px -apple-system,sans-serif;background:#111;color:#eee;text-align:center;padding:24px">' +
        "TikTok is blocked by Unreel.</body>";
    };
    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", block);
    else block();
    window.stop();
    return;
  }

  // Pages that are the short-video player. If you land on one, go back to the home page.
  const isShortForm = (path) => {
    if (host.endsWith("instagram.com")) return /^\/(reels?|reel)(\/|$)/.test(path);
    if (host.endsWith("youtube.com")) return path.startsWith("/shorts");
    if (host.endsWith("facebook.com")) return /^\/(reel|reels)(\/|$)/.test(path) || path.startsWith("/watch/reels");
    return false;
  };

  let last = "";
  const check = () => {
    if (location.href === last) return;
    last = location.href;
    if (isShortForm(location.pathname)) location.replace("/");
  };

  // These sites change pages without reloading, so watch for URL changes.
  for (const name of ["pushState", "replaceState"]) {
    const orig = history[name];
    history[name] = function (...args) {
      const r = orig.apply(this, args);
      check();
      return r;
    };
  }
  addEventListener("popstate", check);
  setInterval(check, 500);
  check();
})();
