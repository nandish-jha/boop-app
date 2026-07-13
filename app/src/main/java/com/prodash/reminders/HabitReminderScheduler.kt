package com.prodash.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object HabitReminderScheduler {
    private fun requestCode(habitId: String): Int = 40_000 + (habitId.hashCode() and 0x7FFF)

    fun cancel(context: Context, habitId: String) {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("habitId", habitId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode(habitId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pending)
        androidx.core.app.NotificationManagerCompat.from(context).cancel(requestCode(habitId))
    }

    fun schedule(context: Context, habit: BoopHabit) {
        cancel(context, habit.id)
        if (!habit.reminderEnabled) return
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, habit.reminderHour.coerceIn(0, 23))
            set(Calendar.MINUTE, habit.reminderMinute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("title", habit.title)
            putExtra("habitId", habit.id)
            putExtra("itemType", "Habit")
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode(habit.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval = 24L * 60L * 60L * 1000L
        try {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, interval, pending)
        } catch (_: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
                } else {
                    @Suppress("DEPRECATION")
                    manager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pending)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun scheduleAll(context: Context, habits: List<BoopHabit>) {
        habits.forEach { schedule(context, it) }
    }
}

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Habit"
        val habitId = intent.getStringExtra("habitId").orEmpty()
        if (habitId.isBlank()) return
        ReminderNotifier.show(
            context = context,
            id = 40_000 + (habitId.hashCode() and 0x7FFF),
            title = title,
            taskId = "",
            subtitle = "Habit",
            eventId = -1L,
            habitId = habitId,
            itemType = "Habit",
        )
    }
}
