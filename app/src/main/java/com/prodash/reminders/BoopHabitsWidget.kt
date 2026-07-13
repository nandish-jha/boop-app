package com.prodash.reminders

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BoopHabitsWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = BoopData.repository(context)
        val todayKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(Calendar.getInstance().time)
        val habits = repo.readHabits()
        val pending = habits.filter { habit ->
            if (habit.quantityMode) {
                val todayAmount = parseHabitDayValues(habit.quantityDayValues)[todayKey] ?: 0
                todayAmount < habit.quantityDailyTarget.coerceAtLeast(1)
            } else {
                todayKey !in parseHabitDayKeys(habit.dayKeys)
            }
        }
        val doneCount = habits.size - pending.size
        val title = if (habits.isEmpty()) {
            "No habits yet"
        } else {
            "$doneCount/${habits.size} habits done"
        }
        val subtitle = pending.firstOrNull()?.title?.ifBlank { "Untitled habit" }
            ?: if (habits.isEmpty()) "Tap to add a habit" else "All habits checked in"
        val clickIntent = pending.firstOrNull()?.let { BoopWidgetSupport.openHabitCheckInIntent(context, it.id) }
            ?: BoopWidgetSupport.openTabIntent(context, "HABITS")
        BoopWidgetSupport.updateWidget(context, appWidgetManager, appWidgetIds, title, subtitle, clickIntent)
    }
}
