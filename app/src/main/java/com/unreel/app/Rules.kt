package com.unreel.app

/**
 * What to block in each app.
 *
 * [defaultHints] are pieces of Android view IDs that only appear on the
 * full-screen Reels/Shorts player. If an app update renames them, you can
 * change them from the app's "Detection" section without rebuilding.
 */
data class Rule(
    val key: String,
    val title: String,
    val packages: Set<String>,
    val defaultHints: List<String>,
)

object Rules {
    val shortForm = listOf(
        Rule(
            key = "ig_reels",
            title = "Instagram Reels",
            packages = setOf("com.instagram.android"),
            defaultHints = listOf("clips_viewer"),
        ),
        Rule(
            key = "yt_shorts",
            title = "YouTube Shorts",
            packages = setOf("com.google.android.youtube"),
            defaultHints = listOf("reel_player", "reel_recycler", "reel_watch", "shorts_player"),
        ),
    )

    /** Apps that are basically nothing but short videos: optionally block the whole app. */
    val wholeApps = listOf(
        Rule("app_tiktok", "TikTok (whole app)", setOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill"), emptyList()),
        Rule("app_facebook", "Facebook (whole app)", setOf("com.facebook.katana"), emptyList()),
        Rule("app_snapchat", "Snapchat (whole app)", setOf("com.snapchat.android"), emptyList()),
    )

    fun shortFormFor(pkg: String): Rule? = shortForm.firstOrNull { pkg in it.packages }
    fun wholeAppFor(pkg: String): Rule? = wholeApps.firstOrNull { pkg in it.packages }
}
