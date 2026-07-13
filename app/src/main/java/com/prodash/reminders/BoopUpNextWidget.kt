package com.prodash.reminders

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Locale

class BoopUpNextWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = BoopData.repository(context)
        val now = System.currentTimeMillis()
        val nextTask = repo.readTasks()
            .filter { !it.archived && !it.done && it.reminderAt >= now }
            .minByOrNull { it.reminderAt }
        val title = nextTask?.title?.ifBlank { "Untitled task" } ?: "Nothing upcoming"
        val subtitle = nextTask?.let {
            SimpleDateFormat("EEE, MMM d · h:mm a", Locale.getDefault()).format(it.reminderAt)
        } ?: "You're all caught up"
        val clickIntent = nextTask?.let { BoopWidgetSupport.openTaskIntent(context, it.id, requestCode = 7101) }
            ?: BoopWidgetSupport.openTabIntent(context, "REMINDERS", requestCode = 7102)
        BoopWidgetSupport.updateWidget(context, appWidgetManager, appWidgetIds, title, subtitle, clickIntent, accentLabel = "UP NEXT")
    }
}
