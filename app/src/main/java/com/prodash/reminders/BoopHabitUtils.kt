package com.prodash.reminders

internal fun parseHabitDayKeys(raw: String): Set<String> =
    raw.split(',').map { it.trim() }.filter { it.length == 8 }.toSet()

internal fun parseHabitDayValues(raw: String): Map<String, Int> {
    if (raw.isBlank()) return emptyMap()
    return raw.split(',')
        .mapNotNull { part ->
            val p = part.split(':')
            if (p.size != 2) return@mapNotNull null
            val key = p[0].trim()
            val value = p[1].trim().toIntOrNull() ?: return@mapNotNull null
            if (key.length != 8) return@mapNotNull null
            key to value.coerceAtLeast(0)
        }.toMap()
}
