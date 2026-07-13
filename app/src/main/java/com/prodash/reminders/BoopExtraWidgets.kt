package com.prodash.reminders

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BoopOverdueWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = BoopData.repository(context)
        val now = System.currentTimeMillis()
        val overdue = repo.readTasks()
            .filter { !it.archived && !it.done && it.reminderAt < now }
            .sortedBy { it.reminderAt }
        val first = overdue.firstOrNull()
        val title = when {
            overdue.isEmpty() -> "No overdue tasks"
            overdue.size == 1 -> first!!.title.ifBlank { "Untitled task" }
            else -> "${overdue.size} overdue"
        }
        val subtitle = first?.let {
            SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault()).format(it.reminderAt)
        } ?: "You're caught up"
        val click = first?.let { BoopWidgetSupport.openTaskIntent(context, it.id, requestCode = 7301) }
            ?: BoopWidgetSupport.openTabIntent(context, "REMINDERS", requestCode = 7302)
        BoopWidgetSupport.updateWidget(context, appWidgetManager, appWidgetIds, title, subtitle, click, accentLabel = "OVERDUE")
    }
}

class BoopStreakWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = BoopData.repository(context)
        val habits = repo.readHabits()
        val best = habits.maxByOrNull { habitStreakForWidget(it) }
        val streak = best?.let { habitStreakForWidget(it) } ?: 0
        val title = if (habits.isEmpty()) "No habits yet" else "$streak day streak"
        val subtitle = best?.title?.ifBlank { "Untitled habit" }
            ?: "Add a habit to start"
        val click = best?.let { BoopWidgetSupport.openHabitCheckInIntent(context, it.id, requestCode = 7401) }
            ?: BoopWidgetSupport.openTabIntent(context, "HABITS", requestCode = 7402)
        BoopWidgetSupport.updateWidget(context, appWidgetManager, appWidgetIds, title, subtitle, click, accentLabel = "STREAK")
    }
}

class BoopTodaySummaryWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = BoopData.repository(context)
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = start + 24L * 60L * 60L * 1000L
        val todayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Calendar.getInstance().time)
        val tasksDue = repo.readTasks().count { !it.archived && !it.done && it.reminderAt in start until end }
        val habits = repo.readHabits()
        val habitsLeft = habits.count { habit ->
            if (habit.quantityMode) {
                val todayAmount = parseHabitDayValues(habit.quantityDayValues)[todayKey] ?: 0
                todayAmount < habit.quantityDailyTarget.coerceAtLeast(1)
            } else {
                todayKey !in parseHabitDayKeys(habit.dayKeys)
            }
        }
        val title = "Today"
        val subtitle = "$tasksDue task${if (tasksDue == 1) "" else "s"} · $habitsLeft habit${if (habitsLeft == 1) "" else "s"} left"
        val click = BoopWidgetSupport.openTabIntent(context, "HOME", requestCode = 7501)
        BoopWidgetSupport.updateWidget(context, appWidgetManager, appWidgetIds, title, subtitle, click, accentLabel = "TODAY")
    }
}

private fun habitStreakForWidget(habit: BoopHabit): Int {
    val keys = parseHabitDayKeys(habit.dayKeys)
    val values = parseHabitDayValues(habit.quantityDayValues)
    val cal = Calendar.getInstance()
    var streak = 0
    // If today not done, start from yesterday
    fun doneOn(cal: Calendar): Boolean {
        val key = SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.time)
        return if (habit.quantityMode) {
            (values[key] ?: 0) >= habit.quantityDailyTarget.coerceAtLeast(1)
        } else {
            key in keys
        }
    }
    if (!doneOn(cal)) cal.add(Calendar.DAY_OF_MONTH, -1)
    while (doneOn(cal)) {
        streak++
        cal.add(Calendar.DAY_OF_MONTH, -1)
    }
    return streak
}
