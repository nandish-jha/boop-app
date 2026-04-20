package com.nandish.productivity

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatMoney(n: Double): String {
    val neg = n < 0
    val v = kotlin.math.abs(n)
    return (if (neg) "-" else "") + "$" + String.format(Locale.US, "%,.2f", v)
}

fun formatDayHeader(iso: String): String {
    return try {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso) ?: return iso
        SimpleDateFormat("EEEE, d MMMM", Locale.US).format(d)
    } catch (_: Exception) {
        iso
    }
}

fun greetingName(): String = "Nandish"
