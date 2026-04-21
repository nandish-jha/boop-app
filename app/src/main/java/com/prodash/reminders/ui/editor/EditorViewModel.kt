package com.prodash.reminders.ui.editor

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prodash.reminders.data.Reminder
import com.prodash.reminders.data.ReminderRepository
import com.prodash.reminders.data.ReminderType
import com.prodash.reminders.schedule.ReminderScheduler
import kotlinx.coroutines.launch

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ReminderRepository()
    private var hasSaved = false

    var title by mutableStateOf("")
        private set

    var body by mutableStateOf("")
        private set

    var imageUri by mutableStateOf<String?>(null)
        private set

    var type by mutableStateOf(ReminderType.TASK)
        private set

    var dueEpochMillis by mutableStateOf(System.currentTimeMillis() + 3_600_000L)
        private set

    private var createdEpochMillis: Long = System.currentTimeMillis()
    var loaded by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set

    fun updateTitle(value: String) {
        title = value
    }

    fun updateBody(value: String) {
        body = value
    }

    fun updateType(value: ReminderType) {
        type = value
        if (value == ReminderType.TASK) {
            imageUri = null
        }
    }

    fun updateImageUri(value: String?) {
        imageUri = value
    }

    fun updateDue(value: Long) {
        dueEpochMillis = value
    }

    fun load(reminderId: String?) {
        viewModelScope.launch {
            loaded = false
            if (reminderId.isNullOrBlank()) {
                hasSaved = false
                title = ""
                body = ""
                imageUri = null
                type = ReminderType.TASK
                dueEpochMillis = System.currentTimeMillis() + 3_600_000L
                createdEpochMillis = System.currentTimeMillis()
                loaded = true
                return@launch
            }

            val existing = repo.fetchOne(reminderId)
            if (existing != null) {
                hasSaved = false
                title = existing.title
                body = existing.body
                imageUri = existing.imageUri
                type = existing.type
                dueEpochMillis = existing.dueEpochMillis
                createdEpochMillis = existing.createdEpochMillis
            }
            loaded = true
        }
    }

    fun save(existingId: String?, onSaved: () -> Unit) {
        if (isSaving || hasSaved) return
        viewModelScope.launch {
            val trimmed = title.trim()
            if (trimmed.isEmpty()) return@launch
            isSaving = true

            try {
                val id = repo.upsert(
                    Reminder(
                        id = existingId.orEmpty(),
                        type = type,
                        title = trimmed,
                        body = body.trim(),
                        imageUri = imageUri,
                        dueEpochMillis = dueEpochMillis,
                        completed = false,
                        createdEpochMillis = createdEpochMillis,
                    ),
                )

                if (type == ReminderType.TASK) {
                    ReminderScheduler.schedule(
                        getApplication(),
                        Reminder(
                            id = id,
                            type = type,
                            title = trimmed,
                            body = body.trim(),
                            imageUri = null,
                            dueEpochMillis = dueEpochMillis,
                            completed = false,
                            createdEpochMillis = createdEpochMillis,
                        ),
                    )
                } else {
                    ReminderScheduler.cancel(getApplication(), id)
                }
                hasSaved = true
                onSaved()
            } finally {
                isSaving = false
            }
        }
    }
}
