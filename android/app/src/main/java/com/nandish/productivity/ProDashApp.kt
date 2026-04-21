package com.nandish.productivity

import android.app.Application
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

class ProDashApp : Application() {

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
        NotificationChannels.ensure(this)
        StateRepository.init(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                flushDriveIfNeeded()
            }
        })
    }

    private fun flushDriveIfNeeded() {
        try {
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
