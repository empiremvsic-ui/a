package com.unreel.app

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Looks at the screen of the supported apps only (see res/xml/blocker_service.xml).
 * If the Reels/Shorts player is visible, presses Back. If Back doesn't get out
 * after a few tries (e.g. the app was opened straight into Reels), goes Home.
 */
class BlockerService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: Prefs
    private var checkScheduled = false
    private var lastActionAt = 0L
    private val recentBacks = ArrayDeque<Long>()

    private val check = Runnable {
        checkScheduled = false
        runCheck()
    }

    override fun onServiceConnected() {
        prefs = Prefs(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!::prefs.isInitialized) prefs = Prefs(this)
        // Coalesce the flood of "content changed" events into one check every ~150 ms.
        if (!checkScheduled) {
            checkScheduled = true
            handler.postDelayed(check, 150)
        }
    }

    private fun runCheck() {
        val root = rootInActiveWindow ?: return
        val pkg = root.packageName?.toString() ?: return

        if (prefs.capture) captureIds(root, pkg)
        if (prefs.isPaused()) return

        val now = SystemClock.uptimeMillis()
        if (now - lastActionAt < 700) {
            // Give the app a moment to react to the last Back, then look again.
            schedule(400)
            return
        }

        Rules.wholeAppFor(pkg)?.let { rule ->
            if (prefs.isEnabled(rule)) {
                act(GLOBAL_ACTION_HOME)
                return
            }
        }

        val rule = Rules.shortFormFor(pkg) ?: return
        if (!prefs.isEnabled(rule)) return
        if (findVisibleId(root, prefs.hints(rule)) != null) escape(now)
    }

    private fun escape(now: Long) {
        recentBacks.addLast(now)
        while (recentBacks.isNotEmpty() && now - recentBacks.first() > 4000) recentBacks.removeFirst()
        if (recentBacks.size >= 3) {
            recentBacks.clear()
            act(GLOBAL_ACTION_HOME)
        } else {
            act(GLOBAL_ACTION_BACK)
        }
        schedule(800) // re-check in case the app bounced us back into Reels
    }

    private fun act(action: Int) {
        performGlobalAction(action)
        lastActionAt = SystemClock.uptimeMillis()
        prefs.blockedCount = prefs.blockedCount + 1
    }

    private fun schedule(delay: Long) {
        handler.removeCallbacks(check)
        checkScheduled = true
        handler.postDelayed(check, delay)
    }

    /** Depth-first walk of the screen, looking for a visible view whose ID contains a hint. */
    private fun findVisibleId(root: AccessibilityNodeInfo, hints: List<String>): String? {
        if (hints.isEmpty()) return null
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        var visited = 0
        while (stack.isNotEmpty() && visited < MAX_NODES) {
            val node = stack.removeLast()
            visited++
            val id = node.viewIdResourceName
            if (id != null && node.isVisibleToUser && hints.any { id.contains(it) }) return id
            for (i in 0 until node.childCount) node.getChild(i)?.let { stack.addLast(it) }
        }
        return null
    }

    private fun captureIds(root: AccessibilityNodeInfo, pkg: String) {
        val ids = sortedSetOf<String>()
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(root)
        var visited = 0
        while (stack.isNotEmpty() && visited < MAX_NODES && ids.size < 300) {
            val node = stack.removeLast()
            visited++
            val id = node.viewIdResourceName
            if (id != null && node.isVisibleToUser) ids.add(id.substringAfter(":id/"))
            for (i in 0 until node.childCount) node.getChild(i)?.let { stack.addLast(it) }
        }
        prefs.captured = "$pkg\n\n" + ids.joinToString("\n")
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        private const val MAX_NODES = 3000
    }
}
