import SwiftUI

/// One tab per site. Each tab keeps its own page, so switching tabs doesn't reload.
/// TikTok isn't here on purpose: it's all short videos.
struct ContentView: View {
    var body: some View {
        TabView {
            SiteView(site: .instagram)
                .tabItem { Label("Instagram", systemImage: "camera") }
            SiteView(site: .youtube)
                .tabItem { Label("YouTube", systemImage: "play.rectangle") }
            SiteView(site: .facebook)
                .tabItem { Label("Facebook", systemImage: "person.2") }
        }
    }
}
