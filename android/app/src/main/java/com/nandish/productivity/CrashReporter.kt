package com.nandish.productivity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Captures uncaught JVM exceptions to a small on-device text file so you can share the stack trace
 * without adb. There is **no automatic popup** on launch (that was destabilizing some devices);
 * use **Menu → Diagnostics (crash report)** after a crash.
 */
object CrashReporter {

    private const val FILE_NAME = "prodash_last_crash.txt"
    private const val MESSAGE_PREVIEW_CHARS = 3500
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
    }

    /** Opens the saved report if present. Returns true if a dialog was shown. */
    fun showSavedReportFromMenu(activity: AppCompatActivity): Boolean {
        val text = readReport(activity) ?: return false
        showReportDialog(activity, text)
        return true
    }

    private fun showReportDialog(activity: AppCompatActivity, text: String) {
        val preview = if (text.length <= MESSAGE_PREVIEW_CHARS) {
            text
        } else {
            text.take(MESSAGE_PREVIEW_CHARS) + "\n\n…truncated in this preview. Share or Copy still sends the full report."
        }
        MaterialAlertDialogBuilder(activity)
            .setTitle("Crash report")
            .setMessage(preview)
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
