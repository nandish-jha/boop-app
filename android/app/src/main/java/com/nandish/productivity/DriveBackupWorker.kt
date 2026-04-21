package com.nandish.productivity

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Periodically uploads app JSON to Drive when a Google account is linked.
 */
class DriveBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val account = GoogleDriveSync.lastSignedInAccount(applicationContext) ?: return Result.success()
        return try {
            withContext(Dispatchers.IO) {
                GoogleDriveSync.uploadStateJson(
                    applicationContext,
                    account,
                    StateRepository.exportJson()
                )
            }
            Result.success()
        } catch (_: UserRecoverableAuthIOException) {
            Result.success()
        } catch (_: Exception) {
            Result.success()
        }
    }
}
