package com.nandish.productivity

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object StateRepository {

    private const val PREFS = "prodash_state"
    private const val KEY_JSON = "app_state_json"

    private val gson: Gson = GsonBuilder().create()
    private val lock = ReentrantReadWriteLock()
    private lateinit var appContext: Context
    private var state: AppState = SeedData.defaultState()

    fun init(context: Context) {
        appContext = context.applicationContext
        lock.write {
            val sp = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val raw = sp.getString(KEY_JSON, null)
            state = if (raw.isNullOrBlank()) {
                SeedData.defaultState()
            } else {
                try {
                    gson.fromJson(raw, AppState::class.java) ?: SeedData.defaultState()
                } catch (_: Exception) {
                    SeedData.defaultState()
                }
            }
            migrateIfNeeded(state)
            persistLocked()
        }
        ReminderScheduler.schedule(context.applicationContext)
    }

    private fun migrateIfNeeded(s: AppState) {
        if (s.schemaVersion < 5) {
            s.settings.obsidianMode = true
            s.settings.hapticsEnabled = true
            s.schemaVersion = 5
        }
    }

    fun get(): AppState = lock.read { copyState(state) }

    fun update(block: AppState.() -> Unit) {
        lock.write {
            block(state)
            persistLocked()
        }
        DriveAutoBackupState.markDirty()
    }

    fun exportJson(): String = lock.read { gson.toJson(state) }

    /**
     * Replaces local state with parsed JSON. Returns false if JSON is invalid.
     */
    fun importReplace(json: String): Boolean = lock.write {
        try {
            val p = gson.fromJson(json, AppState::class.java) ?: return@write false
            state = p
            persistLocked()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun persistLocked() {
        val sp = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_JSON, gson.toJson(state)).apply()
    }

    private fun copyState(s: AppState): AppState =
        gson.fromJson(gson.toJson(s), AppState::class.java)!!
}
