import SafariServices
import SwiftUI
import WebKit

struct SiteView: UIViewRepresentable {
    let site: Site

    func makeCoordinator() -> Coordinator { Coordinator(site: site) }

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        // Logins and cookies are stored on this iPhone only, shared between the tabs.
        config.websiteDataStore = .default()
        config.allowsInlineMediaPlayback = true
        // Look like Safari so the sites (and Google/Facebook logins) don't refuse the app.
        config.applicationNameForUserAgent = "Version/18.0 Mobile/15E148 Safari/604.1"
        config.userContentController.addUserScript(Self.blockerScript)

        let web = WKWebView(frame: .zero, configuration: config)
        web.navigationDelegate = context.coordinator
        web.uiDelegate = context.coordinator
        web.allowsBackForwardNavigationGestures = true

        let refresh = UIRefreshControl()
        refresh.addTarget(context.coordinator, action: #selector(Coordinator.reload(_:)), for: .valueChanged)
        web.scrollView.refreshControl = refresh

        web.load(URLRequest(url: site.home))
        return web
    }

    func updateUIView(_ web: WKWebView, context: Context) {}

    /// Web/unreel.css + Web/unreel.js, injected into every page before it loads.
    static let blockerScript: WKUserScript = {
        func load(_ name: String, _ ext: String) -> String {
            guard let url = Bundle.main.url(forResource: name, withExtension: ext),
                  let text = try? String(contentsOf: url, encoding: .utf8) else { return "" }
            return text
        }
        let css = load("unreel", "css")
        let js = load("unreel", "js")
        let cssLiteral = (try? String(data: JSONEncoder().encode(css), encoding: .utf8)) ?? "\"\""
        let addStyle = """
        (function add() {
          if (!document.documentElement) { return setTimeout(add, 10); }
          var s = document.createElement('style');
          s.textContent = \(cssLiteral);
          document.documentElement.appendChild(s);
        })();
        """
        return WKUserScript(source: addStyle + "\n" + js, injectionTime: .atDocumentStart, forMainFrameOnly: true)
    }()

    final class Coordinator: NSObject, WKNavigationDelegate, WKUIDelegate {
        let site: Site
        init(site: Site) { self.site = site }

        @objc func reload(_ sender: UIRefreshControl) {
            (sender.superview?.superview as? WKWebView)?.reload()
            sender.endRefreshing()
        }

        func webView(_ webView: WKWebView, decidePolicyFor action: WKNavigationAction,
                     decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            guard let url = action.request.url, let host = url.host?.lowercased() else {
                return decisionHandler(.allow)
            }
            // Only judge whole-page loads; let the page load its own images, frames, etc.
            guard action.targetFrame?.isMainFrame ?? true else { return decisionHandler(.allow) }

            if Site.matches(host, Site.blockedDomains) { return decisionHandler(.cancel) }

            if Site.isShortForm(url) {
                decisionHandler(.cancel)
                if !webView.canGoBack { webView.load(URLRequest(url: site.home)) }
                return
            }

            if url.scheme == "http" || url.scheme == "https" {
                if Site.matches(host, Site.allowedDomains) { return decisionHandler(.allow) }
                openInAppBrowser(url, from: webView) // other websites open in a browser page inside the app
                return decisionHandler(.cancel)
            }
            if url.scheme == "about" || url.scheme == "blob" || url.scheme == "data" {
                return decisionHandler(.allow)
            }
            decisionHandler(.cancel) // ignore "open in the Instagram app" style links
        }

        /// Shows a Safari page on top of the app, with a Done button to come back.
        /// Its cookies are kept apart from the Instagram/YouTube/Facebook tabs.
        func openInAppBrowser(_ url: URL, from webView: WKWebView) {
            guard var top = webView.window?.rootViewController else {
                UIApplication.shared.open(url)
                return
            }
            while let presented = top.presentedViewController { top = presented }
            let browser = SFSafariViewController(url: url)
            browser.dismissButtonStyle = .done
            top.present(browser, animated: true)
        }

        /// Links that try to open a new window: open them in the same tab instead.
        func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration,
                     for action: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
            if action.targetFrame == nil, let url = action.request.url {
                webView.load(URLRequest(url: url))
            }
            return nil
        }
    }
}
