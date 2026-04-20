package com.nandish.productivity

import android.app.Application

class ProDashApp : Application() {
    override fun onCreate() {
        super.onCreate()
        StateRepository.init(this)
    }
}
