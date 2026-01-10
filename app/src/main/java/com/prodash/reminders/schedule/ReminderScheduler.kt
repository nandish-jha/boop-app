package com.prodash.reminders.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.prodash.reminders.data.Reminder
import com.prodash.reminders.data.ReminderType
import com.prodash.reminders.receiver.ReminderAlarmReceiver

object ReminderScheduler {
    internal const val EXTRA_REMINDER_ID = "extra_reminder_id"

    fun requestCodeForAlarm(reminderId: String): Int = 31_000 + (reminderId.hashCode() and 0x0FFF_FFFF)

    fun cancel(context: Context, reminderId: String) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = alarmPendingIntent(context, reminderId, PendingIntent.FLAG_NO_CREATE or pendingImmutable())
        if (pi != null) {
            am.cancel(pi)
            pi.cancel()
        }
    }

    fun schedule(context: Context, reminder: Reminder) {
        if (reminder.type != ReminderType.TASK) return
        if (reminder.completed) return
        val due = reminder.dueEpochMillis
        if (due <= System.currentTimeMillis()) return

        val am = context.getSystemService(AlarmManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            return
        }

        val intent = alarmIntent(context, reminder.id)
        val show = Intent(context, com.prodash.reminders.MainActivity::class.java)
        val showPi = PendingIntent.getActivity(
            context,
            requestCodeForAlarm(reminder.id) + 1,
            show,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingImmutable(),
        )

        val trigger = AlarmManager.AlarmClockInfo(due, showPi)
        val alarmPi = alarmPendingIntent(context, reminder.id, PendingIntent.FLAG_UPDATE_CURRENT or pendingImmutable())
            ?: return

        am.setAlarmClock(trigger, alarmPi)
    }

    fun rescheduleAll(context: Context, reminders: List<Reminder>) {
        for (reminder in reminders) {
            cancel(context, reminder.id)
        }
        for (reminder in reminders) {
            if (!reminder.completed && reminder.dueEpochMillis > System.currentTimeMillis()) {
                schedule(context, reminder)
            }
        }
    }

    internal fun reminderId(intent: Intent): String? = intent.getStringExtra(EXTRA_REMINDER_ID)

    private fun alarmIntent(context: Context, reminderId: String): Intent {
        return Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
    }

    private fun alarmPendingIntent(context: Context, reminderId: String, flags: Int): PendingIntent? {
        val intent = alarmIntent(context, reminderId)
        return PendingIntent.getBroadcast(
            context,
            requestCodeForAlarm(reminderId),
            intent,
            flags,
        )
    }

    private fun pendingImmutable(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }
}
