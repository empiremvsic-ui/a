import SwiftUI

/// The app itself just explains how to switch on the Safari extension.
/// All the blocking happens in the extension (UnreelExtension/Resources/unreel.js).
struct ContentView: View {
    var body: some View {
        NavigationStack {
            List {
                Section {
                    Text("Unreel hides Instagram Reels, YouTube Shorts, Facebook Reels and all of TikTok in Safari. It runs only on this iPhone and sends nothing anywhere.")
                }
                Section("Turn it on") {
                    step(1, "Open the Settings app.")
                    step(2, "Go to Apps → Safari → Extensions.")
                    step(3, "Tap Unreel and turn it on.")
                    step(4, "Set instagram.com, youtube.com, tiktok.com and facebook.com to Allow.")
                    Button("Open Settings") {
                        if let url = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(url)
                        }
                    }
                }
                Section("Then") {
                    Text("Delete the Instagram, YouTube, TikTok and Facebook apps and use them in Safari instead. Tip: open the site in Safari, tap Share → Add to Home Screen to get an icon that feels like the app.")
                    Text("Want it harder to undo? Settings → Screen Time → set a passcode, and add App Limits for the apps you deleted so they're harder to reinstall.")
                }
            }
            .navigationTitle("Unreel")
        }
    }

    private func step(_ n: Int, _ text: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Text("\(n)").bold().frame(width: 20)
            Text(text)
        }
    }
}
