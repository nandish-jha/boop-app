package com.prodash.reminders.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.prodash.reminders.data.ReminderRepository
import com.prodash.reminders.notification.ReminderNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_MARK_DONE) return
        val id = intent.getStringExtra(EXTRA_ID) ?: return
        // Dismiss immediately for responsive action UX, then sync completion.
        NotificationManagerCompat.from(context).cancel(ReminderNotificationManager.notificationId(id))
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                ReminderRepository().setCompleted(id, true)
                ReminderNotificationManager.cancel(context.applicationContext, id)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_MARK_DONE = "com.prodash.reminders.action.MARK_DONE"
        const val EXTRA_ID = "extra_reminder_id"
    }
}
