package com.prodash.reminders.data

data class Reminder(
    val id: String,
    val title: String,
    val dueEpochMillis: Long,
    val completed: Boolean,
    val createdEpochMillis: Long,
)
