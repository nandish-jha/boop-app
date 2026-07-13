package com.prodash.reminders

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Locale

class BoopNotesWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = BoopData.repository(context)
        val latestNote = repo.readNotes()
            .filter { !it.archived }
            .maxByOrNull { it.updatedAtMillis + it.createdAtMillis }
        val title = latestNote?.title?.ifBlank { "Untitled note" } ?: "No notes yet"
        val subtitle = latestNote?.let {
            SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()).format(it.updatedAtMillis.coerceAtLeast(it.createdAtMillis))
        } ?: "Tap to add a note"
        val clickIntent = latestNote?.let { BoopWidgetSupport.openNoteIntent(context, it.id) }
            ?: BoopWidgetSupport.openTabIntent(context, "NOTES")
        BoopWidgetSupport.updateWidget(context, appWidgetManager, appWidgetIds, title, subtitle, clickIntent, accentLabel = "NOTE")
    }
}
