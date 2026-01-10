package com.prodash.reminders.data

data class Reminder(
    val id: String,
    val type: ReminderType,
    val title: String,
    val body: String,
    val imageUri: String?,
    val dueEpochMillis: Long,
    val completed: Boolean,
    val createdEpochMillis: Long,
)

enum class ReminderType {
    NOTE,
    TASK,
}
