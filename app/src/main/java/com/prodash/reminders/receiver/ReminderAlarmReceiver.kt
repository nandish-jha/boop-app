package com.prodash.reminders.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prodash.reminders.data.ReminderRepository
import com.prodash.reminders.notification.ReminderNotificationManager
import com.prodash.reminders.schedule.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val id = intent?.let { ReminderScheduler.reminderId(it) } ?: return
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val repo = ReminderRepository()
                val reminder = repo.fetchOne(id) ?: return@launch
                if (reminder.completed) return@launch
                ReminderNotificationManager.showReminder(context.applicationContext, reminder)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
