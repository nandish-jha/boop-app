package com.nandish.productivity

import android.webkit.JavascriptInterface

class ProDashBridge(private val onMutate: () -> Unit) {

    @JavascriptInterface
    fun toggleTask(id: String) {
        StateRepository.update {
            val t = tasks.find { it.id == id } ?: return@update
            t.done = !t.done
        }
        onMutate()
    }
}
