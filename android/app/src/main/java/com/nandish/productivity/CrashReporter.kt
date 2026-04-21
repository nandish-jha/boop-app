package com.nandish.productivity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Captures uncaught JVM exceptions to a small on-device text file so you can share the stack trace
 * without adb. Installed from [ProDashApp.attachBaseContext].
 */
object CrashReporter {

    private const val FILE_NAME = "prodash_last_crash.txt"
    private const val PREFS = "prodash_crash_prefs"
    private const val KEY_PENDING_AUTO_PROMPT = "pending_auto_prompt"
    private val installed = AtomicBoolean(false)

    fun installOnce(context: Context) {
        if (!installed.compareAndSet(false, true)) return
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrash(app, thread, throwable)
            } catch (_: Throwable) {
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable)
            }
        }
    }

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun writeCrash(app: Context, thread: Thread, throwable: Throwable) {
        val sw = StringWriter()
        PrintWriter(sw).use { pw -> throwable.printStackTrace(pw) }
        val vn = try {
            val pm = app.packageManager
            val pn = app.packageName
            @Suppress("DEPRECATION")
            val pi = if (Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(pn, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                pm.getPackageInfo(pn, 0)
            }
            pi.versionName ?: "?"
        } catch (_: Exception) {
            "?"
        }
        val body = buildString {
            appendLine("Silent Order crash report")
            appendLine("package=${app.packageName}")
            appendLine("versionName=$vn")
            appendLine("sdk=${Build.VERSION.SDK_INT}")
            appendLine("device=${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("timeMillis=${System.currentTimeMillis()}")
            appendLine("thread=${thread.name}")
            appendLine("--- stacktrace ---")
            appendLine(sw.toString().trimEnd())
        }
        File(app.filesDir, FILE_NAME).bufferedWriter().use { it.write(body) }
        prefs(app).edit().putBoolean(KEY_PENDING_AUTO_PROMPT, true).apply()
    }

    fun readReport(context: Context): String? {
        val f = File(context.applicationContext.filesDir, FILE_NAME)
        if (!f.exists()) return null
        val t = f.readText().trim()
        return t.ifBlank { null }
    }

    fun clearReport(context: Context) {
        try {
            File(context.applicationContext.filesDir, FILE_NAME).delete()
        } catch (_: Exception) {
        }
        prefs(context).edit().putBoolean(KEY_PENDING_AUTO_PROMPT, false).apply()
    }

    /**
     * One-time prompt after a crash (next cold start). No-op if there is no new crash since last prompt.
     */
    fun promptAutoIfPending(activity: AppCompatActivity) {
        val sp = prefs(activity)
        if (!sp.getBoolean(KEY_PENDING_AUTO_PROMPT, false)) return
        val text = readReport(activity) ?: run {
            sp.edit().putBoolean(KEY_PENDING_AUTO_PROMPT, false).apply()
            return
        }
        sp.edit().putBoolean(KEY_PENDING_AUTO_PROMPT, false).apply()
        showReportDialog(activity, text)
    }

    /** Opens the saved report if present. Returns true if a dialog was shown. */
    fun showSavedReportFromMenu(activity: AppCompatActivity): Boolean {
        val text = readReport(activity) ?: return false
        showReportDialog(activity, text)
        return true
    }

    private fun showReportDialog(activity: AppCompatActivity, text: String) {
        val maxPx = (activity.resources.displayMetrics.density * 280).toInt().coerceAtLeast(200)
        val preview = ScrollView(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                maxPx
            )
            val tv = TextView(activity).apply {
                textSize = 11f
                setTextIsSelectable(true)
                this.text = text
                val pad = (resources.displayMetrics.density * 12).toInt()
                setPadding(pad, pad, pad, pad)
            }
            addView(
                tv,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        MaterialAlertDialogBuilder(activity)
            .setTitle("Crash report")
            .setMessage("Share or copy this text. It helps find the exact line that failed.")
            .setView(preview)
            .setPositiveButton("Share") { _, _ ->
                shareText(activity, text)
            }
            .setNeutralButton("Copy") { _, _ ->
                copyText(activity, text)
            }
            .setNegativeButton("Clear") { _, _ ->
                clearReport(activity)
                Toast.makeText(activity, "Crash report cleared", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun shareText(activity: AppCompatActivity, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Silent Order crash report")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(send, "Send crash report"))
    }

    private fun copyText(activity: AppCompatActivity, text: String) {
        val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Silent Order crash", text))
        Toast.makeText(activity, "Crash report copied", Toast.LENGTH_SHORT).show()
    }
}
