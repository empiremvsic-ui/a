package com.unreel.app

import android.content.Context

/** All settings live in a private file on the phone. Nothing is synced or backed up. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("unreel", Context.MODE_PRIVATE)

    fun isEnabled(rule: Rule): Boolean =
        sp.getBoolean("on_${rule.key}", rule.defaultOn)

    fun setEnabled(rule: Rule, on: Boolean) = sp.edit().putBoolean("on_${rule.key}", on).apply()

    fun hints(rule: Rule): List<String> =
        sp.getString("hints_${rule.key}", null)
            ?.split(',', '\n')?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?: rule.defaultHints

    fun setHints(rule: Rule, text: String) = sp.edit().putString("hints_${rule.key}", text).apply()
    fun resetHints(rule: Rule) = sp.edit().remove("hints_${rule.key}").apply()

    var pausedUntil: Long
        get() = sp.getLong("paused_until", 0L)
        set(v) = sp.edit().putLong("paused_until", v).apply()

    fun isPaused() = System.currentTimeMillis() < pausedUntil

    var blockedCount: Int
        get() = sp.getInt("blocked_count", 0)
        set(v) = sp.edit().putInt("blocked_count", v).apply()

    /** When true, the service saves the view IDs on screen so you can find new ones after an app update. */
    var capture: Boolean
        get() = sp.getBoolean("capture", false)
        set(v) = sp.edit().putBoolean("capture", v).apply()

    var captured: String
        get() = sp.getString("captured", "") ?: ""
        set(v) = sp.edit().putString("captured", v).apply()
}
