package com.nandish.productivity

import android.content.Context
import androidx.multidex.MultiDexApplication
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class ProDashApp : MultiDexApplication() {

    override fun attachBaseContext(base: Context) {
        try {
            CrashReporter.installOnce(base)
        } catch (t: Throwable) {
            try {
                File(base.filesDir, "prodash_startup_error.log").appendText(
                    "${System.currentTimeMillis()} attachBaseContext CrashReporter: ${t.stackTraceToString()}\n\n"
                )
            } catch (_: Throwable) {
            }
        }
        super.attachBaseContext(base)
    }

    private val appScope = CoroutineScope(
        SupervisorJob() +
            Dispatchers.Main.immediate +
            CoroutineExceptionHandler { _, throwable ->
                if (throwable is CancellationException) return@CoroutineExceptionHandler
                Log.e("ProDashApp", "Unhandled error in app coroutine", throwable)
            }
    )

    override fun onCreate() {
        super.onCreate()
        try {
            NotificationChannels.ensure(this)
        } catch (t: Throwable) {
            logStartupFailure("NotificationChannels", t)
        }
        try {
            StateRepository.init(this)
        } catch (t: Throwable) {
            logStartupFailure("StateRepository.init", t)
            throw t
        }
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    flushDriveIfNeeded()
                }
            })
        } catch (t: Throwable) {
            logStartupFailure("ProcessLifecycleOwner", t)
        }
    }

    private fun logStartupFailure(where: String, t: Throwable) {
        try {
            File(filesDir, "prodash_startup_error.log").appendText(
                "${System.currentTimeMillis()} $where: ${t.stackTraceToString()}\n\n"
            )
        } catch (_: Throwable) {
        }
        Log.e("ProDashApp", "Startup failure: $where", t)
    }

    private fun flushDriveIfNeeded() {
        try {
            if (!AppSession.isInteractive()) return
            if (!DriveAutoBackupState.hasDirty()) return
            val acct = GoogleDriveSync.lastSignedInAccount(this) ?: return
            if (acct.account == null) return
            appScope.launch(Dispatchers.IO) {
                try {
                    GoogleDriveSync.uploadStateJson(
                        this@ProDashApp,
                        acct,
                        StateRepository.exportJson()
                    )
                    DriveAutoBackupState.clearDirty()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w("ProDashApp", "Drive flush on background failed: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.w("ProDashApp", "flushDriveIfNeeded failed: ${e.message}")
        }
    }
}
