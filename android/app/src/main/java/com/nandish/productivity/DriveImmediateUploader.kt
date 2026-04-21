package com.nandish.productivity

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * After local data changes, uploads to Google Drive shortly after (debounced so rapid edits become one upload).
 * Uses whatever network is available (Wi‑Fi or mobile). If upload fails, [DriveAutoBackupState] stays dirty so
 * [ProDashApp] can retry when the app goes to the background.
 */
object DriveImmediateUploader {

    private const val DEBOUNCE_MS = 350L

    @Volatile
    private var appContext: Context? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val uploadMutex = Mutex()
    private var debounceJob: Job? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun scheduleAfterChange() {
        val ctx = appContext ?: return
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(DEBOUNCE_MS)
            val acct = GoogleDriveSync.lastSignedInAccount(ctx) ?: return@launch
            if (!DriveAutoBackupState.hasDirty()) return@launch
            try {
                uploadMutex.withLock {
                    GoogleDriveSync.uploadStateJson(ctx, acct, StateRepository.exportJson())
                }
                DriveAutoBackupState.clearDirty()
            } catch (_: Exception) {
                // Leave dirty; ProDashApp may flush on background.
            }
        }
    }
}
