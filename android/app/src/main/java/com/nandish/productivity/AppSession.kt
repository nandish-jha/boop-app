package com.nandish.productivity

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Set true after [MainActivity] has reached [onPostResume] so background work (e.g. Drive) does not
 * run during the fragile part of cold start.
 */
object AppSession {
    private val interactive = AtomicBoolean(false)

    fun markInteractive() {
        interactive.set(true)
    }

    fun isInteractive(): Boolean = interactive.get()
}
