package com.nandish.productivity

import android.app.Application

class ProDashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensure(this)
        StateRepository.init(this)
    }
}
