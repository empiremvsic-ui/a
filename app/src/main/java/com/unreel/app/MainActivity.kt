package com.unreel.app

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/** Settings screen. Built in code so the whole app stays small and easy to read. */
class MainActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var status: TextView
    private lateinit var enableButton: Button
    private lateinit var counter: TextView
    private lateinit var pauseStatus: TextView
    private lateinit var captured: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = dp(20)
            setPadding(p, p, p, p)
        }

        col.addView(text("Unreel", 28f, bold = true))
        col.addView(text("Blocks Reels and Shorts. Runs only on this phone, with no internet access.", 14f))

        // --- Status ---
        col.addView(header("Status"))
        status = text("", 16f)
        col.addView(status)
        enableButton = button("Turn on in Accessibility settings") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        col.addView(enableButton)
        col.addView(text(
            "If Android says the setting is restricted: open Settings > Apps > Unreel, tap the ⋮ menu, " +
                "choose \"Allow restricted settings\", then try again.", 13f))
        counter = text("", 14f)
        col.addView(counter)

        // --- What to block ---
        col.addView(header("Block"))
        (Rules.shortForm + Rules.wholeApps).forEach { rule ->
            col.addView(Switch(this).apply {
                text = rule.title
                textSize = 16f
                isChecked = prefs.isEnabled(rule)
                setPadding(0, dp(8), 0, dp(8))
                setOnCheckedChangeListener { _, on -> prefs.setEnabled(rule, on) }
            })
        }

        // --- Pause ---
        col.addView(header("Take a break from blocking"))
        pauseStatus = text("", 14f)
        col.addView(pauseStatus)
        col.addView(row(
            button("5 min") { pauseFor(5) },
            button("15 min") { pauseFor(15) },
            button("Resume") { prefs.pausedUntil = 0; refresh() },
        ))

        // --- Detection (advanced) ---
        col.addView(header("Detection (advanced)"))
        col.addView(text(
            "Unreel recognises the Reels/Shorts player by pieces of its view IDs. " +
                "If an app update breaks blocking, use Capture below to find the new IDs, then edit them here.", 13f))
        Rules.shortForm.forEach { rule ->
            col.addView(text(rule.title, 15f, bold = true))
            val edit = EditText(this).apply {
                setText(prefs.hints(rule).joinToString(", "))
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                typeface = Typeface.MONOSPACE
                textSize = 14f
            }
            col.addView(edit)
            col.addView(row(
                button("Save") {
                    prefs.setHints(rule, edit.text.toString())
                    toast("Saved")
                },
                button("Reset") {
                    prefs.resetHints(rule)
                    edit.setText(rule.defaultHints.joinToString(", "))
                    toast("Reset to defaults")
                },
            ))
        }

        col.addView(text("Capture", 15f, bold = true))
        col.addView(text(
            "Turn on, open a Reel or Short, come back here and tap Refresh. " +
                "You'll see the IDs that were on screen. Turn it off when you're done.", 13f))
        col.addView(Switch(this).apply {
            text = "Capture screen IDs"
            isChecked = prefs.capture
            setOnCheckedChangeListener { _, on ->
                prefs.capture = on
                if (!on) prefs.captured = ""
                refresh()
            }
        })
        col.addView(button("Refresh") { refresh() })
        captured = text("", 12f).apply {
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
        }
        col.addView(captured)

        setContentView(ScrollView(this).apply { addView(col) })
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val on = isServiceEnabled()
        status.text = if (on) "✅ Blocker is ON" else "⛔ Blocker is OFF. Turn it on below."
        enableButton.visibility = if (on) View.GONE else View.VISIBLE
        counter.text = "Blocked ${prefs.blockedCount} times so far."

        pauseStatus.text = if (prefs.isPaused()) {
            val mins = ((prefs.pausedUntil - System.currentTimeMillis()) / 60000) + 1
            "Paused. Blocking resumes in about $mins min."
        } else {
            "Blocking is active."
        }
        captured.text = if (prefs.capture) prefs.captured.ifEmpty { "(nothing captured yet)" } else ""
    }

    private fun pauseFor(minutes: Int) {
        prefs.pausedUntil = System.currentTimeMillis() + minutes * 60_000L
        refresh()
    }

    private fun isServiceEnabled(): Boolean {
        val me = ComponentName(this, BlockerService::class.java)
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    // --- tiny UI helpers ---
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun text(s: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = s
        textSize = size
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(4), 0, dp(4))
    }

    private fun header(s: String) = text(s, 20f, bold = true).apply { setPadding(0, dp(24), 0, dp(6)) }

    private fun button(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        setOnClickListener { onClick() }
    }

    private fun row(vararg views: View) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        views.forEach { addView(it, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)) }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
