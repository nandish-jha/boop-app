package com.nandish.productivity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {

    private const val REQUEST_ALARM = 94_001
    private const val REQUEST_SHOW = 94_002

    fun schedule(context: Context) {
        try {
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
        } catch (_: Exception) {
            // Do not block app launch if alarm APIs reject scheduling (policy / OEM quirks).
        }
    }

    /**
     * Next wall-clock trigger for [timeStr] like "8:30" or "08:30:00", using the device default
     * timezone. Uses [Calendar] only (no java.time) so minSdk 23 devices without API 26+ libraries
     * do not crash at startup.
     */
    fun nextReminderMillis(timeStr: String): Long? {
        return try {
            val parts = timeStr.trim().split(":", limit = 3)
            if (parts.isEmpty()) return null
            val hour = parts[0].trim().toInt().coerceIn(0, 23)
            val minute = parts.getOrNull(1)?.trim()?.toIntOrNull()?.coerceIn(0, 59) ?: 0
            val cal = Calendar.getInstance()
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            cal.timeInMillis
        } catch (_: Exception) {
            null
        }
    }
}
