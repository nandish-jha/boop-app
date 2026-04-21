package com.nandish.productivity

/**
 * Tracks unsynced local edits so we can flush to Google Drive when the app goes to the background.
 */
object DriveAutoBackupState {

    private val lock = Any()
    private var dirty: Boolean = false

    fun markDirty() {
        synchronized(lock) { dirty = true }
    }

    fun clearDirty() {
        synchronized(lock) { dirty = false }
    }

    fun hasDirty(): Boolean = synchronized(lock) { dirty }
}
