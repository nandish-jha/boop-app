package com.prodash.reminders.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.prodash.reminders.MainActivity
import com.prodash.reminders.R
import com.prodash.reminders.RescheduleActivity
import com.prodash.reminders.data.Reminder
import com.prodash.reminders.receiver.ReminderActionReceiver
import com.prodash.reminders.schedule.ReminderScheduler

object ReminderNotificationManager {
    const val CHANNEL_ID = "reminders_v1"

    fun notificationId(reminderId: String): Int = 51_000 + (reminderId.hashCode() and 0x0FFF_FFFF)

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_reminders),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.channel_reminders_desc)
            enableVibration(true)
        }
        nm.createNotificationChannel(channel)
    }

    fun showReminder(context: Context, reminder: Reminder) {
        val mark = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderActionReceiver.ACTION_MARK_DONE
            putExtra(ReminderActionReceiver.EXTRA_ID, reminder.id)
        }
        val markPi = PendingIntent.getBroadcast(
            context,
            41_000 + (reminder.id.hashCode() and 0x0FFF_FFFF),
            mark,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val reschedule = Intent(context, RescheduleActivity::class.java).apply {
            putExtra(RescheduleActivity.EXTRA_REMINDER_ID, reminder.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val resPi = PendingIntent.getActivity(
            context,
            42_000 + (reminder.id.hashCode() and 0x0FFF_FFFF),
            reschedule,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPi = PendingIntent.getActivity(
            context,
            43_000 + (reminder.id.hashCode() and 0x0FFF_FFFF),
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setContentTitle(reminder.title)
            .setContentText(context.getString(R.string.channel_reminders_desc))
            .setStyle(NotificationCompat.BigTextStyle().bigText(reminder.title))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPi)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.action_mark_done), markPi)
            .addAction(0, context.getString(R.string.action_reschedule), resPi)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId(reminder.id), notification)
    }

    fun cancel(context: Context, reminderId: String) {
        NotificationManagerCompat.from(context).cancel(notificationId(reminderId))
        ReminderScheduler.cancel(context, reminderId)
    }
}
