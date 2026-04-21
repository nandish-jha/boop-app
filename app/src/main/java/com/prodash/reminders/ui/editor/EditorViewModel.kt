package com.prodash.reminders.ui.editor

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prodash.reminders.data.Reminder
import com.prodash.reminders.data.ReminderRepository
import com.prodash.reminders.schedule.ReminderScheduler
import kotlinx.coroutines.launch

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ReminderRepository()

    var title by mutableStateOf("")
        private set

    var dueEpochMillis by mutableStateOf(System.currentTimeMillis() + 3_600_000L)
        private set

    private var createdEpochMillis: Long = System.currentTimeMillis()
    var loaded by mutableStateOf(false)
        private set

    fun updateTitle(value: String) {
        title = value
    }

    fun updateDue(value: Long) {
        dueEpochMillis = value
    }

    fun load(reminderId: String?) {
        viewModelScope.launch {
            loaded = false
            if (reminderId.isNullOrBlank()) {
                title = ""
                dueEpochMillis = System.currentTimeMillis() + 3_600_000L
                createdEpochMillis = System.currentTimeMillis()
                loaded = true
                return@launch
            }

            val existing = repo.fetchOne(reminderId)
            if (existing != null) {
                title = existing.title
                dueEpochMillis = existing.dueEpochMillis
                createdEpochMillis = existing.createdEpochMillis
            }
            loaded = true
        }
    }

    fun save(existingId: String?, onSaved: () -> Unit) {
        viewModelScope.launch {
            val trimmed = title.trim()
            if (trimmed.isEmpty()) return@launch

            val id = repo.upsert(
                Reminder(
                    id = existingId.orEmpty(),
                    title = trimmed,
                    dueEpochMillis = dueEpochMillis,
                    completed = false,
                    createdEpochMillis = createdEpochMillis,
                ),
            )

            ReminderScheduler.schedule(
                getApplication(),
                Reminder(
                    id = id,
                    title = trimmed,
                    dueEpochMillis = dueEpochMillis,
                    completed = false,
                    createdEpochMillis = createdEpochMillis,
                ),
            )
            onSaved()
        }
    }
}
