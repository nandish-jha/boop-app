package com.prodash.reminders

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
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
        val title = if (todayTasks.isEmpty()) {
            "No tasks today"
        } else {
            todayTasks.first().title
        }
        val subtitle = when (todayTasks.size) {
            0 -> "You're clear for today"
            1 -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(todayTasks.first().reminderAt)
            else -> "${todayTasks.size} tasks due today"
        }
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_tasks).apply {
                setTextViewText(R.id.widget_title, title)
                setTextViewText(R.id.widget_subtitle, subtitle)
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
