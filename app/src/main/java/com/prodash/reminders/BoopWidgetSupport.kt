package com.prodash.reminders

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

object BoopWidgetSupport {
    const val EXTRA_OPEN_TASK_ID = "openTaskId"
    const val EXTRA_OPEN_NOTE_ID = "openNoteId"
    const val EXTRA_OPEN_HABIT_ID = "openHabitId"
    const val EXTRA_HABIT_CHECK_IN_ONLY = "habitCheckInOnly"
    const val EXTRA_OPEN_TAB = "openTab"

    fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        title: String,
        subtitle: String,
        clickIntent: PendingIntent?,
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_tasks).apply {
                setTextViewText(R.id.widget_title, title)
                setTextViewText(R.id.widget_subtitle, subtitle)
                if (clickIntent != null) {
                    setOnClickPendingIntent(R.id.widget_root, clickIntent)
                }
            }
            appWidgetManager.updateAppWidget(id, views)
        }
    }

    fun openAppIntent(context: Context, requestCode: Int, configure: Intent.() -> Unit): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            configure()
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun openTaskIntent(context: Context, taskId: String, requestCode: Int = taskId.hashCode()): PendingIntent =
        openAppIntent(context, requestCode) {
            putExtra(EXTRA_OPEN_TASK_ID, taskId)
        }

    fun openNoteIntent(context: Context, noteId: String, requestCode: Int = noteId.hashCode()): PendingIntent =
        openAppIntent(context, requestCode) {
            putExtra(EXTRA_OPEN_NOTE_ID, noteId)
        }

    fun openHabitCheckInIntent(context: Context, habitId: String, requestCode: Int = habitId.hashCode()): PendingIntent =
        openAppIntent(context, requestCode) {
            putExtra(EXTRA_OPEN_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_CHECK_IN_ONLY, true)
        }

    fun openTabIntent(context: Context, tab: String, requestCode: Int = tab.hashCode()): PendingIntent =
        openAppIntent(context, requestCode) {
            putExtra(EXTRA_OPEN_TAB, tab)
        }
}
