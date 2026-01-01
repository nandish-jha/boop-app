package com.prodash.reminders

import android.app.Application
import com.prodash.reminders.notification.ReminderNotificationManager

class RemindersApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderNotificationManager.createChannel(this)
    }
}
