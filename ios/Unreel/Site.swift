import Foundation

enum Site {
    case instagram, youtube, facebook

    var home: URL {
        switch self {
        case .instagram: return URL(string: "https://www.instagram.com/")!
        case .youtube: return URL(string: "https://m.youtube.com/")!
        case .facebook: return URL(string: "https://m.facebook.com/")!
        }
    }

    /// Sites allowed to open inside the app (the services themselves plus their login pages).
    /// Any other link opens in Safari instead.
    static let allowedDomains = [
        "instagram.com", "cdninstagram.com",
        "facebook.com", "fbcdn.net", "messenger.com",
        "youtube.com", "youtube-nocookie.com", "ytimg.com",
        "google.com", "gstatic.com", "googleusercontent.com",
    ]

    static let blockedDomains = ["tiktok.com"]

    static func matches(_ host: String, _ domains: [String]) -> Bool {
        domains.contains { host == $0 || host.hasSuffix("." + $0) }
    }

    /// True for the Reels / Shorts player pages. Same rules as Web/unreel.js.
    static func isShortForm(_ url: URL) -> Bool {
        guard let host = url.host?.lowercased() else { return false }
        let path = url.path.lowercased()
        if matches(host, ["instagram.com"]) {
            return path == "/reels" || path.hasPrefix("/reels/") || path == "/reel" || path.hasPrefix("/reel/")
        }
        if matches(host, ["youtube.com"]) { return path.hasPrefix("/shorts") }
        if matches(host, ["facebook.com"]) {
            return path.hasPrefix("/reel") || path.hasPrefix("/watch/reels")
        }
        return false
    }
}
