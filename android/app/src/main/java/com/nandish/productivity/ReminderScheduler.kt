package com.nandish.productivity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.ZoneId
import java.time.ZonedDateTime

object ReminderScheduler {

    private const val REQUEST_ALARM = 94_001
    private const val REQUEST_SHOW = 94_002

    fun schedule(context: Context) {
        val app = context.applicationContext
        val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val operation = PendingIntent.getBroadcast(
            app,
            REQUEST_ALARM,
            Intent(app, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val trigger = nextReminderMillis(StateRepository.get().settings.reminderTime)
        am.cancel(operation)
        if (trigger == null) return
        val show = PendingIntent.getActivity(
            app,
            REQUEST_SHOW,
            Intent(app, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, show), operation)
        } else {
            @Suppress("DEPRECATION")
            am.setExact(AlarmManager.RTC_WAKEUP, trigger, operation)
        }
    }

    fun nextReminderMillis(timeStr: String): Long? {
        return try {
            val parts = timeStr.trim().split(":", limit = 3)
            if (parts.isEmpty()) return null
            val hour = parts[0].trim().toInt().coerceIn(0, 23)
            val minute = parts.getOrNull(1)?.trim()?.toIntOrNull()?.coerceIn(0, 59) ?: 0
            val zone = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zone)
            var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (!next.isAfter(now)) {
                next = next.plusDays(1)
            }
            next.toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }
}
