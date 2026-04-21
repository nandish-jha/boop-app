package com.nandish.productivity

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class ProDashBridge(private val host: Host) {

    interface Host {
        fun onToggleTask(id: String)
        fun onOpenMenu()
        fun onOpenSearch()
        fun onNavigate(tab: String)
        fun onToast(message: String)
        fun onOpenEditor(kind: String, id: String)
        fun onToggleHabitToday(id: String)
        fun onToggleSupplementLog(id: String)
        fun onStreamCreate()
        fun onSetSetting(key: String, value: String)
    }

    private val main = Handler(Looper.getMainLooper())

    private fun runMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block()
        else main.post(block)
    }

    @JavascriptInterface
    fun toggleTask(id: String) {
        runMain { host.onToggleTask(id) }
    }

    @JavascriptInterface
    fun openMenu() {
        runMain { host.onOpenMenu() }
    }

    @JavascriptInterface
    fun openSearch() {
        runMain { host.onOpenSearch() }
    }

    @JavascriptInterface
    fun navigate(tab: String) {
        runMain { host.onNavigate(tab) }
    }

    @JavascriptInterface
    fun toast(message: String) {
        runMain { host.onToast(message) }
    }

    @JavascriptInterface
    fun openEditor(kind: String, id: String) {
        runMain { host.onOpenEditor(kind, id) }
    }

    @JavascriptInterface
    fun toggleHabitToday(id: String) {
        runMain { host.onToggleHabitToday(id) }
    }

    @JavascriptInterface
    fun toggleSupplementLog(id: String) {
        runMain { host.onToggleSupplementLog(id) }
    }

    @JavascriptInterface
    fun openStreamCreate() {
        runMain { host.onStreamCreate() }
    }

    @JavascriptInterface
    fun setSetting(key: String, value: String) {
        runMain { host.onSetSetting(key, value) }
    }
}
