package com.nandish.productivity

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext
        NotificationChannels.ensure(app)
        val tap = PendingIntent.getActivity(
            app,
            0,
            Intent(app, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(app, NotificationChannels.REMINDERS_ID)
            .setSmallIcon(R.drawable.ic_notification_24)
            .setContentTitle("Silent Order")
            .setContentText("Daily reminder — open your hub.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(app).notify(NOTIFY_ID, notification)
        } catch (_: SecurityException) {
        }
        ReminderScheduler.schedule(app)
    }

    companion object {
        private const val NOTIFY_ID = 74_001
    }
}
