package com.prodash.reminders.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prodash.reminders.work.ReminderBootWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        ReminderBootWorker.enqueue(context.applicationContext)
    }
}
