package com.prodash.reminders

import android.content.Context
import org.json.JSONArray

object BoopStoreAccess {
    fun readAccounts(context: Context): List<BoopAccount> {
        val json = context.getSharedPreferences("boop_store", Context.MODE_PRIVATE)
            .getString("accounts", "[]")
            .orEmpty()
        val arr = JSONArray(json)
        val out = mutableListOf<BoopAccount>()
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            out.add(
                BoopAccount(
                    id = item.getString("id"),
                    name = item.optString("name"),
                ),
            )
        }
        return out.sortedBy { it.name.lowercase() }
    }
}
