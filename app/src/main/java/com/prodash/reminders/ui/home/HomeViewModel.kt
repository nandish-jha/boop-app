package com.prodash.reminders.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.prodash.reminders.data.Reminder
import com.prodash.reminders.data.ReminderRepository
import com.prodash.reminders.data.ReminderType
import com.prodash.reminders.notification.ReminderNotificationManager
import com.prodash.reminders.schedule.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ReminderRepository()
    private val auth = FirebaseAuth.getInstance()

    val reminders = repo.remindersFlow()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            reminders.collect { list ->
                ReminderScheduler.rescheduleAll(getApplication(), list)
            }
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            val snapshot = reminders.value
            for (reminder in snapshot) {
                ReminderScheduler.cancel(getApplication(), reminder.id)
            }
            auth.signOut()
            com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
                getApplication(),
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN,
                ).build(),
            ).signOut()
            onDone()
        }
    }

    fun setCompleted(reminder: Reminder, completed: Boolean) {
        viewModelScope.launch {
            repo.setCompleted(reminder.id, completed)
            if (completed) {
                ReminderNotificationManager.cancel(getApplication(), reminder.id)
            } else if (reminder.type == ReminderType.TASK) {
                ReminderScheduler.schedule(
                    getApplication(),
                    reminder.copy(completed = false),
                )
            }
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            val completedTaskIds = reminders.value
                .filter { it.type == ReminderType.TASK && it.completed }
                .map { it.id }
            completedTaskIds.forEach { id ->
                ReminderNotificationManager.cancel(getApplication(), id)
            }
            repo.deleteCompletedTaskItems(completedTaskIds)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            ReminderScheduler.cancel(getApplication(), reminder.id)
            ReminderNotificationManager.cancel(getApplication(), reminder.id)
            repo.delete(reminder.id)
        }
    }
}
