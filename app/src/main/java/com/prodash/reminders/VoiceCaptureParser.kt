package com.prodash.reminders

import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

enum class VoiceCaptureType {
    TASK, EVENT, HABIT, EXPENSE, INCOME, TRANSFER, NOTE,
}

data class ParsedVoiceCapture(
    val type: VoiceCaptureType,
    val title: String,
    val body: String,
    val dueAtMillis: Long? = null,
    val endAtMillis: Long? = null,
    val allDay: Boolean = false,
    val repeatEveryDays: Int = 0,
    val habitDayPeriod: String = "day",
    val amount: Double? = null,
    val accountId: String? = null,
    val toAccountId: String? = null,
    val category: String = "",
    val location: String = "",
)

object VoiceCaptureParser {
    private val taskTriggers = listOf(
        "add task", "create task", "new task", "remind me", "reminder",
        "todo", "to-do", "to do", "buy", "pick up", "call", "email", "send",
        "fix", "check", "review", "submit", "book", "reserve", "order",
        "clean", "wash", "cook", "prepare", "finish", "complete", "update",
        "cancel", "change", "replace", "install", "set up", "sign up", "register",
        "apply", "follow up", "contact", "meet", "attend", "go to",
        "make sure", "don't forget", "need to", "have to", "must",
    )

    private val eventTriggers = listOf(
        "add event", "new event", "calendar event", "appointment", "meeting",
        "schedule", "block off", "dentist", "doctor visit",
    )

    private val habitTriggers = listOf(
        "add habit", "new habit", "track habit", "habit to", "start habit",
        "every day", "every morning", "every night",
    )

    private val expenseTriggers = listOf(
        "spent", "expense", "paid for", "bought", "purchase", "cost me",
        "charge", "debit", "pay for",
    )

    private val incomeTriggers = listOf(
        "income", "earned", "got paid", "received", "deposit", "paycheck", "salary",
    )

    private val transferTriggers = listOf(
        "transfer", "move money", "send money", "move from", "transfer from",
    )

    private val noteTriggers = listOf(
        "add note", "add a note", "new note", "create note", "create a note",
        "write note", "write a note", "write down",
        "take a note", "take note", "take notes", "take note of", "take notes of",
        "make a note", "make note", "make notes",
        "note to self", "note that", "note about",
        "jot down", "journal entry", "voice note", "voice notes",
        "remember that", "remember this",
    )

    private val noteIntentPattern = Regex(
        """\b(?:""" +
            """tak(?:e|ing)\s+(?:a\s+)?notes?(?:\s+(?:of|about|that))?|""" +
            """mak(?:e|ing)\s+(?:a\s+)?notes?(?:\s+(?:of|about|that))?|""" +
            """writ(?:e|ing)\s+(?:a\s+)?notes?(?:\s+(?:of|about|that|down))?|""" +
            """creat(?:e|ing)\s+(?:a\s+)?(?:new\s+)?notes?|""" +
            """add(?:ing)?\s+(?:a\s+)?(?:new\s+)?notes?|""" +
            """notes?\s+(?:to\s+self|that|about)|""" +
            """jot\s+down|voice\s+notes?|""" +
            """write\s+down|remember\s+(?:that|this)""" +
            """)\b""",
        RegexOption.IGNORE_CASE,
    )

    private val noteCommandPatterns = listOf(
        Regex("""^take\s+a\s+note\s+(?:of|about|that)?\s*(?:this|that)?[,:]?\s*""", RegexOption.IGNORE_CASE),
        Regex("""^take\s+(?:a\s+)?notes?\s*(?:of|about|that)?\s*(?:this|that)?[,:]?\s*""", RegexOption.IGNORE_CASE),
        Regex("""^(?:tak(?:e|ing)|mak(?:e|ing)|writ(?:e|ing)|creat(?:e|ing)|add(?:ing)?)\s+(?:a\s+)?notes?\s*(?:of|about|that)?\s*(?:this|that)?[,:]?\s*""", RegexOption.IGNORE_CASE),
        Regex("""^(please )?(?:add|create|write|make)\s+(?:a )?(?:new )?notes? (?:about|that|of|to self)? ?(?:this|that)?[,:]?\s*""", RegexOption.IGNORE_CASE),
        Regex("""^note (to self )?(that|about|of)? (this|that)?[,:]?\s*""", RegexOption.IGNORE_CASE),
        Regex("""^jot down (this|that)?[,:]?\s*""", RegexOption.IGNORE_CASE),
        Regex("""^(please )?remember (that|this)[,:]?\s*""", RegexOption.IGNORE_CASE),
        Regex("""^voice note[,:]?\s*""", RegexOption.IGNORE_CASE),
    )

    private val fillerPatterns = listOf(
        Regex("""\bum+\b""", RegexOption.IGNORE_CASE),
        Regex("""\buh+\b""", RegexOption.IGNORE_CASE),
        Regex("""\badd (a |an )?(task|reminder|note|event|habit|expense|income) (for |to )?(me )?""", RegexOption.IGNORE_CASE),
        Regex("""\bremind me to\b""", RegexOption.IGNORE_CASE),
        Regex("""\bcreate (a |an )?(new )?(task|reminder|note|event|habit)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bschedule (a |an )?""", RegexOption.IGNORE_CASE),
        Regex("""\bplease\b""", RegexOption.IGNORE_CASE),
        Regex("""\bcan you\b""", RegexOption.IGNORE_CASE),
    )

    fun parse(raw: String, accounts: List<BoopAccount>): ParsedVoiceCapture {
        val text = raw.trim()
        val lower = text.lowercase(Locale.US)
        if (text.isBlank()) {
            return ParsedVoiceCapture(VoiceCaptureType.NOTE, "New note", "")
        }

        val amount = extractAmount(lower)
        val dueAt = extractDueDate(lower)
        val (fromAccount, toAccount) = matchAccounts(lower, accounts)
        val type = when {
            isNoteIntent(lower) -> VoiceCaptureType.NOTE
            else -> detectTypeNonNote(lower, amount, fromAccount, toAccount, dueAt)
        }
        val cleaned = cleanText(text)
        val (title, body) = if (type == VoiceCaptureType.NOTE) {
            extractNoteTitleAndBody(text)
        } else {
            extractTitle(cleaned, type) to cleaned
        }
        val category = extractCategory(lower, type)
        val location = extractLocation(lower)
        val repeatDays = extractRepeatDays(lower)
        val habitPeriod = extractHabitDayPeriod(lower)
        val allDay = lower.contains("all day") || lower.contains("all-day")

        val start = when (type) {
            VoiceCaptureType.NOTE -> null
            else -> dueAt ?: defaultDueMillis(type)
        }
        val end = when {
            start == null -> null
            allDay -> start + 24 * 60 * 60_000L
            type == VoiceCaptureType.EVENT -> start + 60 * 60_000L
            else -> null
        }

        return ParsedVoiceCapture(
            type = type,
            title = title,
            body = body,
            dueAtMillis = start,
            endAtMillis = end,
            allDay = allDay,
            repeatEveryDays = repeatDays,
            habitDayPeriod = habitPeriod,
            amount = amount,
            accountId = fromAccount?.id,
            toAccountId = toAccount?.id,
            category = category,
            location = location,
        )
    }

    private fun detectTypeNonNote(
        lower: String,
        amount: Double?,
        fromAccount: BoopAccount?,
        toAccount: BoopAccount?,
        dueAt: Long?,
    ): VoiceCaptureType {
        if (transferTriggers.any { lower.contains(it) } || (fromAccount != null && toAccount != null)) {
            return VoiceCaptureType.TRANSFER
        }
        if (incomeTriggers.any { containsWord(lower, it) }) return VoiceCaptureType.INCOME
        if (expenseTriggers.any { lower.contains(it) } || (amount != null && lower.contains("dollar"))) {
            return VoiceCaptureType.EXPENSE
        }
        if (habitTriggers.any { lower.contains(it) } || containsWord(lower, "habit")) {
            return VoiceCaptureType.HABIT
        }
        if (eventTriggers.any { lower.contains(it) }) return VoiceCaptureType.EVENT
        if (taskTriggers.any { lower.contains(it) }) return VoiceCaptureType.TASK
        if (dueAt != null) return VoiceCaptureType.TASK
        return VoiceCaptureType.TASK
    }

    private fun containsWord(text: String, word: String): Boolean {
        return Regex("""\b${Regex.escape(word)}\b""", RegexOption.IGNORE_CASE).containsMatchIn(text)
    }

    private fun isNoteIntent(lower: String): Boolean {
        if (noteTriggers.any { lower.contains(it) }) return true
        if (noteIntentPattern.containsMatchIn(lower)) return true
        if (Regex("""^(?:please\s+)?(?:tak(?:e|ing)|mak(?:e|ing)|writ(?:e|ing)|creat(?:e|ing)|add(?:ing)?)\s+(?:a\s+)?notes?\b""", RegexOption.IGNORE_CASE)
                .containsMatchIn(lower.trim())) {
            return true
        }
        return false
    }

    private fun extractAmount(lower: String): Double? {
        val patterns = listOf(
            Regex("""\$\s*(\d+(?:\.\d{1,2})?)"""),
            Regex("""(\d+(?:\.\d{1,2})?)\s*dollars?"""),
            Regex("""(\d+(?:\.\d{1,2})?)\s*bucks?"""),
            Regex("""(?:for|of)\s+(\d+(?:\.\d{1,2})?)"""),
        )
        for (pattern in patterns) {
            val match = pattern.find(lower) ?: continue
            return match.groupValues[1].toDoubleOrNull()
        }
        return null
    }

    private fun matchAccounts(
        lower: String,
        accounts: List<BoopAccount>,
    ): Pair<BoopAccount?, BoopAccount?> {
        if (accounts.isEmpty()) return null to null
        val sorted = accounts.sortedByDescending { it.name.length }
        var from: BoopAccount? = null
        var to: BoopAccount? = null

        val fromTo = Regex("""from\s+(.+?)\s+to\s+(.+)""", RegexOption.IGNORE_CASE).find(lower)
        if (fromTo != null) {
            val fromText = fromTo.groupValues[1]
            val toText = fromTo.groupValues[2]
            from = sorted.firstOrNull { fromText.contains(it.name.lowercase(Locale.US)) }
            to = sorted.firstOrNull { toText.contains(it.name.lowercase(Locale.US)) }
            return from to to
        }

        for (account in sorted) {
            val name = account.name.lowercase(Locale.US)
            if (name.length < 3) continue
            if (lower.contains(name)) {
                if (from == null) from = account else if (to == null && account.id != from.id) to = account
            }
        }
        return from to to
    }

    fun extractDueDate(lower: String): Long? {
        val now = Calendar.getInstance()
        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        var base = Calendar.getInstance()

        val dayNames = listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
        var foundDay = false

        when {
            lower.contains("next week") -> {
                base = todayMidnight.clone() as Calendar
                base.add(Calendar.DAY_OF_MONTH, 7)
                foundDay = true
            }
            lower.contains("next month") -> {
                base = todayMidnight.clone() as Calendar
                base.add(Calendar.DAY_OF_MONTH, 30)
                foundDay = true
            }
            lower.contains("day after tomorrow") -> {
                base = todayMidnight.clone() as Calendar
                base.add(Calendar.DAY_OF_MONTH, 2)
                foundDay = true
            }
            lower.contains("tomorrow") -> {
                base = todayMidnight.clone() as Calendar
                base.add(Calendar.DAY_OF_MONTH, 1)
                foundDay = true
            }
            lower.contains("today") || lower.contains("tonight") -> {
                base = todayMidnight.clone() as Calendar
                foundDay = true
            }
            else -> {
                for (day in dayNames) {
                    val next = "next $day"
                    if (lower.contains(next) || lower.contains(day)) {
                        val target = dayNames.indexOf(day)
                        val current = now.get(Calendar.DAY_OF_WEEK) - 1
                        var diff = target - current
                        if (lower.contains(next) || diff <= 0) diff += 7
                        base = todayMidnight.clone() as Calendar
                        base.add(Calendar.DAY_OF_MONTH, diff)
                        foundDay = true
                        break
                    }
                }
            }
        }

        val timeMatch = Regex(
            """\bat\s*(\d{1,2})(?::(\d{2}))?\s*(a\.?m\.?|p\.?m\.?)?\b""" +
                """|\b(\d{1,2}):(\d{2})\s*(a\.?m\.?|p\.?m\.?)?\b""" +
                """|\b(\d{1,2})\s*(a\.?m\.?|p\.?m\.?)\b""" +
                """|\b(\d{1,2})(?::(\d{2}))?\s+(?:in\s+the\s+)?(morning|afternoon|evening)\b""",
            RegexOption.IGNORE_CASE,
        ).find(lower)

        if (timeMatch != null) {
            val g = timeMatch.groupValues
            val hours = listOf(1, 4, 7, 9).firstNotNullOfOrNull { g.getOrNull(it)?.toIntOrNull() } ?: 9
            val minutes = listOf(2, 5, 10).firstNotNullOfOrNull { g.getOrNull(it)?.toIntOrNull() } ?: 0
            val explicitMeridiem = listOf(3, 6, 8).firstNotNullOfOrNull { g.getOrNull(it)?.takeIf { it.isNotBlank() } }.orEmpty()
            val contextWord = g.getOrNull(11).orEmpty()
            val resolvedHours = resolveHourWithMeridiem(
                hours = hours,
                explicitMeridiem = explicitMeridiem,
                contextWord = contextWord,
                fullText = lower,
            )
            if (!foundDay) base = Calendar.getInstance()
            base.set(Calendar.HOUR_OF_DAY, resolvedHours)
            base.set(Calendar.MINUTE, minutes)
            base.set(Calendar.SECOND, 0)
            base.set(Calendar.MILLISECOND, 0)
            return base.timeInMillis
        }

        if (lower.contains("noon") || lower.contains("midday")) {
            if (!foundDay) base = Calendar.getInstance()
            base.set(Calendar.HOUR_OF_DAY, 12)
            base.set(Calendar.MINUTE, 0)
            base.set(Calendar.SECOND, 0)
            base.set(Calendar.MILLISECOND, 0)
            return base.timeInMillis
        }
        if (lower.contains("midnight")) {
            if (!foundDay) base = Calendar.getInstance()
            base.set(Calendar.HOUR_OF_DAY, 0)
            base.set(Calendar.MINUTE, 0)
            base.set(Calendar.SECOND, 0)
            base.set(Calendar.MILLISECOND, 0)
            return base.timeInMillis
        }

        if (foundDay) {
            base.set(Calendar.HOUR_OF_DAY, 9)
            base.set(Calendar.MINUTE, 0)
            base.set(Calendar.SECOND, 0)
            base.set(Calendar.MILLISECOND, 0)
            return base.timeInMillis
        }

        val inMatch = Regex("""\bin\s+(\d+)\s+(minute|hour|day|week)s?\b""").find(lower)
        if (inMatch != null) {
            val n = inMatch.groupValues[1].toLongOrNull() ?: return null
            val unit = inMatch.groupValues[2]
            val ms = when (unit) {
                "minute" -> 60_000L
                "hour" -> 3_600_000L
                "day" -> 86_400_000L
                else -> 604_800_000L
            }
            return System.currentTimeMillis() + n * ms
        }
        return null
    }

    private fun normalizeMeridiemToken(raw: String): String? {
        val token = raw.lowercase(Locale.US).replace(".", "").trim()
        return when (token) {
            "am" -> "am"
            "pm" -> "pm"
            else -> null
        }
    }

    private fun meridiemFromTimeOfDayWord(word: String): String? = when (word.lowercase(Locale.US)) {
        "morning" -> "am"
        "afternoon", "evening" -> "pm"
        else -> null
    }

    private fun meridiemFromSpeechContext(lower: String): String? = when {
        lower.contains("midnight") -> "am"
        lower.contains("noon") || lower.contains("midday") || lower.contains("lunch time") || lower.contains("lunchtime") -> "pm"
        lower.contains("in the morning") || Regex("""\bmorning\b""").containsMatchIn(lower) -> "am"
        lower.contains("in the afternoon") || Regex("""\bafternoon\b""").containsMatchIn(lower) -> "pm"
        lower.contains("in the evening") || lower.contains("tonight") ||
            Regex("""\bevening\b""").containsMatchIn(lower) || Regex("""\bnight\b""").containsMatchIn(lower) -> "pm"
        else -> null
    }

    private fun resolveHourWithMeridiem(
        hours: Int,
        explicitMeridiem: String,
        contextWord: String,
        fullText: String,
    ): Int {
        val meridiem = normalizeMeridiemToken(explicitMeridiem)
            ?: meridiemFromTimeOfDayWord(contextWord)
            ?: meridiemFromSpeechContext(fullText)

        var h = hours.coerceIn(0, 23)
        return when (meridiem) {
            "am" -> if (h == 12) 0 else h.coerceAtMost(11)
            "pm" -> when {
                h == 12 -> 12
                h < 12 -> h + 12
                else -> h
            }
            else -> h
        }
    }

    private fun extractRepeatDays(lower: String): Int = when {
        lower.contains("every year") || lower.contains("yearly") -> 365
        lower.contains("every month") || lower.contains("monthly") -> 30
        lower.contains("every week") || lower.contains("weekly") -> 7
        lower.contains("every day") || lower.contains("daily") -> 1
        else -> 0
    }

    private fun extractHabitDayPeriod(lower: String): String = when {
        lower.contains("night") || lower.contains("evening") -> "night"
        lower.contains("afternoon") || lower.contains("midday") || lower.contains("lunch") -> "mid"
        else -> "day"
    }

    private fun extractCategory(lower: String, type: VoiceCaptureType): String {
        if (type !in setOf(VoiceCaptureType.EXPENSE, VoiceCaptureType.INCOME)) return ""
        val onMatch = Regex("""(?:on|for)\s+([a-z][a-z\s]{2,30})""", RegexOption.IGNORE_CASE).find(lower)
        val phrase = onMatch?.groupValues?.getOrNull(1)?.trim().orEmpty()
        if (phrase.isBlank()) return ""
        val stopWords = listOf("from", "to", "my", "the", "account", "tomorrow", "today")
        val words = phrase.split(" ").filter { it.isNotBlank() && it !in stopWords }
        return words.take(3).joinToString(" ").replaceFirstChar { it.uppercase() }
    }

    private fun extractLocation(lower: String): String {
        val atMatch = Regex("""\bat\s+([a-z0-9][a-z0-9\s'.-]{2,40})""", RegexOption.IGNORE_CASE).find(lower)
            ?: return ""
        val phrase = atMatch.groupValues[1].trim()
        if (phrase.matches(Regex("""\d{1,2}(:\d{2})?\s*(am|pm)?""", RegexOption.IGNORE_CASE))) return ""
        return phrase.split(" ").take(5).joinToString(" ").replaceFirstChar { it.uppercase() }
    }

    private fun cleanText(text: String): String {
        var cleaned = text
        for (pattern in fillerPatterns) {
            cleaned = cleaned.replace(pattern, " ")
        }
        cleaned = cleaned.replace(Regex("""\s{2,}"""), " ").trim()
        if (cleaned.isNotEmpty()) {
            cleaned = cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        }
        return cleaned
    }

    private val titleSchedulingPatterns = listOf(
        Regex("""\bat\s+\d{1,2}(?::\d{2})?\s*(?:a\.?m\.?|p\.?m\.?)?\b""", RegexOption.IGNORE_CASE),
        Regex("""\b\d{1,2}:\d{2}\s*(?:a\.?m\.?|p\.?m\.?)?\b""", RegexOption.IGNORE_CASE),
        Regex("""\b\d{1,2}\s*(?:a\.?m\.?|p\.?m\.?)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b\d{1,2}(?::\d{2})?\s+(?:in\s+the\s+)?(morning|afternoon|evening)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:in\s+the\s+)?(morning|afternoon|evening|night)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(tomorrow|today|tonight|day after tomorrow|next week|next month)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bnext\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(monday|tuesday|wednesday|thursday|friday|saturday|sunday)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(noon|midday|midnight)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:a\.?m\.?|p\.?m\.?)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bin\s+\d+\s+(minute|hour|day|week)s?\b""", RegexOption.IGNORE_CASE),
    )

    private fun stripSchedulingPhrases(text: String): String {
        var result = text
        for (pattern in titleSchedulingPatterns) {
            result = result.replace(pattern, " ")
        }
        return result.replace(Regex("""\s{2,}"""), " ").trim()
    }

    private fun stripNoteCommandPhrases(text: String): String {
        var result = text.trim()
        for (pattern in noteCommandPatterns) {
            result = pattern.replace(result, "").trim()
        }
        for (pattern in fillerPatterns) {
            result = pattern.replace(result, " ").trim()
        }
        return result.replace(Regex("""\s{2,}"""), " ").trim()
    }

    private fun formatNoteField(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""
        return trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }

    private fun extractExplicitNoteTitle(content: String): Pair<String, String>? {
        val text = content.trim()
        val twoPartPatterns = listOf(
            Regex("""title\s+is\s+(.+?)\s+(?:body|content|description)\s+(?:is\s+)?(.+)""", RegexOption.IGNORE_CASE),
            Regex("""title\s*[:]\s*(.+?)\s*[-–—,:]\s+(.+)""", RegexOption.IGNORE_CASE),
            Regex("""titled\s+(.+?)\s+(?:body|content|description)\s+(?:is\s+)?(.+)""", RegexOption.IGNORE_CASE),
            Regex("""titled\s+(.+?)\s+that\s+(.+)""", RegexOption.IGNORE_CASE),
            Regex("""titled\s+(.+?)[,.]\s+(.+)""", RegexOption.IGNORE_CASE),
            Regex("""^about\s+(.+?)\s+that\s+(.+)""", RegexOption.IGNORE_CASE),
            Regex("""^about\s+(.+?)[,:]\s+(.+)""", RegexOption.IGNORE_CASE),
            Regex("""call(?:\s+it)?\s+(.+?)[,:]\s+(.+)""", RegexOption.IGNORE_CASE),
            Regex("""named\s+(.+?)[,:]\s+(.+)""", RegexOption.IGNORE_CASE),
            Regex("""subject\s*[:]\s*(.+?)[,:]\s+(.+)""", RegexOption.IGNORE_CASE),
            Regex("""with (?:the )?title\s+(.+?)\s+and (?:the )?(?:body|content|description)\s+(.+)""", RegexOption.IGNORE_CASE),
        )
        for (pattern in twoPartPatterns) {
            val match = pattern.find(text) ?: continue
            val title = match.groupValues[1].trim()
            val body = match.groupValues[2].trim()
            if (title.isNotBlank() && body.isNotBlank()) return title to body
        }

        val delimiter = Regex("""^(.{2,60}?)[,:]\s+(.{2,})$""").find(text) ?: return null
        val title = delimiter.groupValues[1].trim()
        val body = delimiter.groupValues[2].trim()
        val titleWords = title.split(Regex("""\s+""")).filter { it.isNotBlank() }
        if (titleWords.size in 1..8 && body.isNotBlank()) return title to body
        return null
    }

    private fun extractNoteTitleAndBody(raw: String): Pair<String, String> {
        val content = stripNoteCommandPhrases(raw)
        if (content.isBlank()) return "" to ""

        extractExplicitNoteTitle(content)?.let { (title, body) ->
            return formatNoteField(title) to formatNoteField(body)
        }

        return "" to formatNoteField(content)
    }

    private fun extractTitle(cleaned: String, type: VoiceCaptureType): String {
        var title = stripSchedulingPhrases(cleaned)
            .replace(Regex("""\$\s*\d+(?:\.\d{1,2})?"""), "")
            .replace(Regex("""\d+(?:\.\d{1,2})?\s*dollars?""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s{2,}"""), " ")
            .trim()
        // Drop orphaned single letters left from partial am/pm recognition (e.g. "p" from "p.m.")
        title = title.replace(Regex("""\s+[ap]\s*$""", RegexOption.IGNORE_CASE), "").trim()

        val dot = title.indexOfFirst { it == '.' || it == '!' || it == '?' }
        if (dot in 11..79) title = title.substring(0, dot)

        if (title.length > 70) title = title.take(67) + "…"

        if (title.isBlank()) {
            title = when (type) {
                VoiceCaptureType.EVENT -> "New event"
                VoiceCaptureType.HABIT -> "New habit"
                VoiceCaptureType.EXPENSE -> "Expense"
                VoiceCaptureType.INCOME -> "Income"
                VoiceCaptureType.TRANSFER -> "Transfer"
                VoiceCaptureType.TASK -> "New task"
                VoiceCaptureType.NOTE -> "New note"
            }
        }
        return title
    }

    private fun defaultDueMillis(type: VoiceCaptureType): Long? = when (type) {
        VoiceCaptureType.TASK, VoiceCaptureType.EVENT -> System.currentTimeMillis() + 30 * 60_000L
        else -> null
    }
}
