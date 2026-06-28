package com.prodash.reminders

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

sealed class VoiceCommitOutcome {
    data class Ok(val message: String) : VoiceCommitOutcome()
    data class FinanceNeedsEditor(val parsed: ParsedVoiceCapture, val message: String) : VoiceCommitOutcome()
}

object VoiceCaptureCommit {
    fun commit(context: Context, parsed: ParsedVoiceCapture): VoiceCommitOutcome {
        val repo = BoopData.repository(context)
        return when (parsed.type) {
            VoiceCaptureType.TASK -> commitTask(context, repo, parsed)
            VoiceCaptureType.NOTE -> commitNote(repo, parsed)
            VoiceCaptureType.HABIT -> commitHabit(repo, parsed)
            VoiceCaptureType.EVENT -> commitEvent(context, parsed)
            VoiceCaptureType.EXPENSE,
            VoiceCaptureType.INCOME,
            VoiceCaptureType.TRANSFER,
            -> commitFinance(repo, parsed)
        }
    }

    private fun commitTask(context: Context, repo: BoopRepository, parsed: ParsedVoiceCapture): VoiceCommitOutcome {
        val title = parsed.title.trim()
        if (title.isBlank()) {
            return VoiceCommitOutcome.Ok("Couldn't save task — no title heard.")
        }
        val task = BoopTask(
            id = UUID.randomUUID().toString(),
            title = title,
            reminderAt = parsed.dueAtMillis ?: (System.currentTimeMillis() + 30 * 60_000),
            done = false,
            repeatEveryDays = parsed.repeatEveryDays,
        )
        repo.saveTask(task)
        ReminderScheduler.schedule(context, task)
        return VoiceCommitOutcome.Ok("Task saved")
    }

    private fun commitNote(repo: BoopRepository, parsed: ParsedVoiceCapture): VoiceCommitOutcome {
        val trimmedTitle = parsed.title.trim()
        val trimmedBody = parsed.body.trim()
        if (trimmedTitle.isBlank() && trimmedBody.isBlank()) {
            return VoiceCommitOutcome.Ok("Couldn't save note — nothing heard.")
        }
        val now = System.currentTimeMillis()
        repo.saveNote(
            BoopNote(
                id = UUID.randomUUID().toString(),
                title = trimmedTitle,
                body = trimmedBody,
                attachmentUri = null,
                audioUri = null,
                tagsCsv = "",
                ocrText = "",
                linkedTaskId = null,
                archived = false,
                createdAtMillis = now,
                updatedAtMillis = now,
            ),
        )
        return VoiceCommitOutcome.Ok("Note saved")
    }

    private fun commitHabit(repo: BoopRepository, parsed: ParsedVoiceCapture): VoiceCommitOutcome {
        val title = parsed.title.trim()
        if (title.isBlank()) {
            return VoiceCommitOutcome.Ok("Couldn't save habit — no title heard.")
        }
        repo.saveHabit(
            BoopHabit(
                id = UUID.randomUUID().toString(),
                title = title,
                dayPeriodCategory = parsed.habitDayPeriod,
                goal = 30,
                progress = 0,
                dayKeys = "",
                quantityMode = false,
                quantityUnit = "",
                quantityDailyTarget = 30,
                quantityDayValues = "",
            ),
        )
        return VoiceCommitOutcome.Ok("Habit saved")
    }

    private fun commitEvent(context: Context, parsed: ParsedVoiceCapture): VoiceCommitOutcome {
        val title = parsed.title.trim()
        if (title.isBlank()) {
            return VoiceCommitOutcome.Ok("Couldn't save event — no title heard.")
        }
        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED
        if (!writeGranted) {
            return VoiceCommitOutcome.Ok("Calendar permission required to save events.")
        }
        val calendarId = readFirstVisibleCalendarId(context)
        if (calendarId == null) {
            return VoiceCommitOutcome.Ok("No writable calendar found.")
        }
        val start = parsed.dueAtMillis ?: (startOfDayMillis(System.currentTimeMillis()) + 9 * 60 * 60_000L)
        val end = parsed.endAtMillis ?: (start + 60 * 60_000L)
        val eventId = insertDeviceCalendarEvent(
            context = context,
            calendarId = calendarId,
            title = title,
            description = parsed.body.trim(),
            location = parsed.location.trim(),
            allDay = parsed.allDay,
            startAt = start,
            endAt = end,
            repeatEveryDays = parsed.repeatEveryDays.coerceAtLeast(0),
        )
        return if (eventId > 0) {
            EventReminderScheduler.schedule(
                context = context,
                eventId = eventId,
                title = title,
                eventStartAt = start,
                weeksBefore = 0,
                daysBefore = 0,
                hoursBefore = 0,
            )
            VoiceCommitOutcome.Ok("Event saved to Calendar")
        } else {
            VoiceCommitOutcome.Ok("Failed to save event")
        }
    }

    private fun commitFinance(repo: BoopRepository, parsed: ParsedVoiceCapture): VoiceCommitOutcome {
        val title = parsed.title.trim()
        val amount = parsed.amount
        val accounts = repo.readAccounts()
        val fromAccountId = parsed.accountId ?: accounts.firstOrNull()?.id
        if (title.isBlank() || amount == null || amount <= 0.0 || fromAccountId.isNullOrBlank()) {
            return VoiceCommitOutcome.FinanceNeedsEditor(
                parsed = parsed,
                message = "Couldn't save transaction — say a title and amount.",
            )
        }
        if (parsed.type == VoiceCaptureType.TRANSFER && parsed.toAccountId.isNullOrBlank()) {
            return VoiceCommitOutcome.FinanceNeedsEditor(
                parsed = parsed,
                message = "Couldn't save transfer — say both accounts.",
            )
        }
        repo.saveLedgerEntry(
            BoopLedgerEntry(
                id = UUID.randomUUID().toString(),
                type = when (parsed.type) {
                    VoiceCaptureType.INCOME -> "income"
                    VoiceCaptureType.TRANSFER -> "transfer"
                    else -> "expense"
                },
                accountId = fromAccountId,
                toAccountId = parsed.toAccountId?.takeIf { parsed.type == VoiceCaptureType.TRANSFER },
                amount = amount,
                title = title,
                category = parsed.category.trim(),
                subcategory = "",
                note = parsed.body.trim(),
                dueAtMillis = null,
                createdAtMillis = System.currentTimeMillis(),
            ),
        )
        return VoiceCommitOutcome.Ok("Transaction saved")
    }

    private fun readFirstVisibleCalendarId(context: Context): Long? {
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID),
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " ASC",
        )?.use { cursor ->
            val idIx = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            if (idIx >= 0 && cursor.moveToFirst()) return cursor.getLong(idIx)
        }
        return null
    }

    private fun insertDeviceCalendarEvent(
        context: Context,
        calendarId: Long,
        title: String,
        description: String,
        location: String,
        allDay: Boolean,
        startAt: Long,
        endAt: Long,
        repeatEveryDays: Int,
    ): Long {
        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.EVENT_LOCATION, location)
                put(CalendarContract.Events.DTSTART, startAt)
                put(CalendarContract.Events.DTEND, maxOf(endAt, startAt + 60_000L))
                put(CalendarContract.Events.ALL_DAY, if (allDay) 1 else 0)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                if (repeatEveryDays > 0) {
                    put(CalendarContract.Events.RRULE, calendarRRuleFromRepeatDays(repeatEveryDays))
                }
            }
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.lastPathSegment?.toLongOrNull() ?: -1L
        } catch (_: Throwable) {
            -1L
        }
    }

    private fun calendarRRuleFromRepeatDays(repeatEveryDays: Int): String {
        val days = repeatEveryDays.coerceAtLeast(1)
        return when {
            days == 365 -> "FREQ=YEARLY;INTERVAL=1"
            days % 7 == 0 -> "FREQ=WEEKLY;INTERVAL=${(days / 7).coerceAtLeast(1)}"
            else -> "FREQ=DAILY;INTERVAL=$days"
        }
    }

    private fun startOfDayMillis(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
