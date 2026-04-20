package com.nandish.productivity

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object StateRepository {
    private const val PREFS = "silent_order_state"
    private const val KEY = "app_state_json"

    private val gson: Gson = GsonBuilder().create()
    private lateinit var appCtx: Context
    private var state: AppState = SeedData.defaultState()

    fun init(context: Context) {
        appCtx = context.applicationContext
        val raw = appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        state = if (raw.isNullOrBlank()) {
            SeedData.defaultState().also { persist() }
        } else {
            try {
                gson.fromJson(raw, AppState::class.java) ?: SeedData.defaultState()
            } catch (_: Exception) {
                SeedData.defaultState()
            }
        }
    }

    fun get(): AppState = state

    fun persist() {
        val json = gson.toJson(state)
        appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, json).apply()
    }

    fun update(block: AppState.() -> Unit) {
        state.block()
        persist()
    }
}
