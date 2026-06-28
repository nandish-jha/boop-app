package com.prodash.reminders

import org.json.JSONObject

object VoiceAssistantBridge {
    const val ACTION_VOICE_RESULT = "com.prodash.reminders.VOICE_RESULT"
    const val EXTRA_PARSED_JSON = "parsed_json"

    fun ParsedVoiceCapture.toJson(): String = JSONObject().apply {
        put("type", type.name)
        put("title", title)
        put("body", body)
        put("amount", amount ?: JSONObject.NULL)
        put("dueAtMillis", dueAtMillis ?: JSONObject.NULL)
        put("endAtMillis", endAtMillis ?: JSONObject.NULL)
        put("allDay", allDay)
        put("repeatEveryDays", repeatEveryDays)
        put("habitDayPeriod", habitDayPeriod)
        put("accountId", accountId ?: JSONObject.NULL)
        put("toAccountId", toAccountId ?: JSONObject.NULL)
        put("category", category)
        put("location", location)
    }.toString()

    fun parseVoiceCapture(json: String): ParsedVoiceCapture? = runCatching {
        val o = JSONObject(json)
        ParsedVoiceCapture(
            type = VoiceCaptureType.valueOf(o.getString("type")),
            title = o.optString("title"),
            body = o.optString("body"),
            dueAtMillis = if (o.isNull("dueAtMillis")) null else o.getLong("dueAtMillis"),
            endAtMillis = if (o.isNull("endAtMillis")) null else o.getLong("endAtMillis"),
            allDay = o.optBoolean("allDay", false),
            repeatEveryDays = o.optInt("repeatEveryDays", 0),
            habitDayPeriod = o.optString("habitDayPeriod", "day"),
            amount = if (o.isNull("amount")) null else o.getDouble("amount"),
            accountId = o.optString("accountId").ifBlank { null },
            toAccountId = o.optString("toAccountId").ifBlank { null },
            category = o.optString("category"),
            location = o.optString("location"),
        )
    }.getOrNull()
}

object BoopVoiceResultHolder {
    var pending: ParsedVoiceCapture? = null
    val nonce = androidx.compose.runtime.mutableIntStateOf(0)

    fun deliver(parsed: ParsedVoiceCapture) {
        pending = parsed
        nonce.intValue++
    }
}
