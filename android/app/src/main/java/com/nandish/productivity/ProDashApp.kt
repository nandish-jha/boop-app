package com.nandish.productivity

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ProDashApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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
        if (!DriveAutoBackupState.hasDirty()) return
        val acct = GoogleDriveSync.lastSignedInAccount(this) ?: return
        appScope.launch(Dispatchers.IO) {
            try {
                GoogleDriveSync.uploadStateJson(
                    this@ProDashApp,
                    acct,
                    StateRepository.exportJson()
                )
                DriveAutoBackupState.clearDirty()
            } catch (_: Exception) {
                // Keep dirty for next background exit or the next immediate upload attempt.
            }
        }
    }
}
