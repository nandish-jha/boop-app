package com.prodash.reminders

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BoopTasksWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = BoopData.repository(context)
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = start + 24L * 60L * 60L * 1000L
        val todayTasks = repo.readTasks()
            .filter { !it.archived && !it.done && it.reminderAt in start until end }
            .sortedBy { it.reminderAt }
        val firstTask = todayTasks.firstOrNull()
        val title = firstTask?.title?.ifBlank { "Untitled task" } ?: "No tasks today"
        val subtitle = when (todayTasks.size) {
            0 -> "You're clear for today"
            1 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(firstTask!!.reminderAt)
            else -> "${todayTasks.size} tasks due today"
        }
        val clickIntent = firstTask?.let { BoopWidgetSupport.openTaskIntent(context, it.id) }
            ?: BoopWidgetSupport.openTabIntent(context, "REMINDERS")
        BoopWidgetSupport.updateWidget(context, appWidgetManager, appWidgetIds, title, subtitle, clickIntent, accentLabel = "TASK")
    }
}
