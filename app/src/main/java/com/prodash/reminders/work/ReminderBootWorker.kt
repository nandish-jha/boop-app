package com.prodash.reminders.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.prodash.reminders.data.ReminderRepository
import com.prodash.reminders.schedule.ReminderScheduler

class ReminderBootWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (FirebaseAuth.getInstance().currentUser == null) {
            return Result.success()
        }
        val repo = ReminderRepository()
        val items = repo.fetchAllOnce()
        ReminderScheduler.rescheduleAll(applicationContext, items)
        return Result.success()
    }

    companion object {
        private const val UNIQUE = "reminder-boot-reschedule"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<ReminderBootWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }
    }
}
