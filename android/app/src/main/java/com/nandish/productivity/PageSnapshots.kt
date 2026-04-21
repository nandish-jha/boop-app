package com.nandish.productivity

import com.google.gson.Gson
import java.util.Locale

object PageSnapshots {

    fun jsonForAsset(assetHtml: String, gson: Gson): String {
        val s = StateRepository.get()
        return gson.toJson(
            when {
                assetHtml.contains("home_dashboard_dark") -> home(s)
                assetHtml.contains("task_notes_hub_dark") -> hub(s)
                assetHtml.contains("habit_goal_tracker_dark") -> goals(s)
                assetHtml.contains("accounts_finance_dark") -> vault(s)
                assetHtml.contains("supplement_logger_dark") -> logs(s)
                assetHtml.contains("settings_dark") -> settings(s)
                else -> mapOf("page" to "none")
            }
        )
    }

    private fun priorityWeight(t: Task): Int = when (t.priority) {
        "high" -> 0
        "medium" -> 1
        else -> 2
    }

    private fun splitMoney(total: Double): Pair<String, String> {
        val formatted = formatMoney(total)
        val neg = formatted.startsWith("-")
        val core = formatted.removePrefix("-").removePrefix("$")
        val idx = core.lastIndexOf('.')
        val wholePart = if (idx >= 0) core.substring(0, idx) else core
        val centsPart = if (idx >= 0) "." + core.substring(idx + 1) else ".00"
        return Pair((if (neg) "-$" else "$") + wholePart, centsPart)
    }

    private fun home(s: AppState): Map<String, Any?> {
        val today = SeedData.today()
        val total = s.accounts.sumOf { it.balance }
        val (whole, cents) = splitMoney(total)

        val doneT = s.tasks.count { it.done }
        val habitsDone = s.habitLogs[today]?.values?.count { it == "true" } ?: 0
        val cadence = if (s.habits.isEmpty()) 82 else (habitsDone * 100 / s.habits.size.coerceAtLeast(1))

        return mapOf(
            "page" to "home",
            "date" to formatDayHeader(today),
            "welcome" to "Welcome,\n${greetingName()}.",
            "vaultWhole" to whole,
            "vaultCents" to cents,
            "vaultTrend" to "+12.4%",
            "cadencePct" to "$cadence%",
            "focusMeta" to "$doneT of ${s.tasks.size} done",
            "tasks" to s.tasks.sortedWith(compareBy({ it.done }, { priorityWeight(it) })).take(5).map { t ->
                mapOf(
                    "id" to t.id,
                    "title" to t.title,
                    "done" to t.done,
                    "priority" to t.priority
                )
            },
            "supplements" to s.supplements.filter { it.time == "morning" }.take(4).map { x ->
                mapOf("id" to x.id, "name" to x.name, "dose" to x.dose.uppercase(Locale.US))
            }
        )
    }

    private fun hub(s: AppState): Map<String, Any?> {
        val tasks = s.tasks.sortedWith(compareBy({ it.done }, { priorityWeight(it) })).map { t ->
            mapOf(
                "kind" to "task",
                "id" to t.id,
                "title" to t.title,
                "done" to t.done,
                "priority" to t.priority,
                "due" to t.due,
                "type" to t.type
            )
        }
        val notes = s.notes.sortedByDescending { it.updated }.map { n ->
            mapOf(
                "kind" to "note",
                "id" to n.id,
                "title" to n.title,
                "body" to n.body,
                "tag" to (n.tags.firstOrNull() ?: "Note")
            )
        }
        return mapOf("page" to "hub", "stream" to (tasks + notes))
    }

    private fun goals(s: AppState): Map<String, Any?> {
        val today = SeedData.today()
        val done = s.habitLogs[today]?.values?.count { it == "true" } ?: 0
        val total = s.habits.size.coerceAtLeast(1)
        val pass = (done * 100 / total).coerceIn(0, 100)
        val logToday = s.habitLogs[today] ?: HashMap()
        val habitsJson = s.habits.map { h ->
            mapOf(
                "id" to h.id,
                "name" to h.name,
                "doneToday" to (logToday[h.id] == "true")
            )
        }
        return mapOf(
            "page" to "goals",
            "habitsMeta" to "$done/${s.habits.size} completed",
            "passPct" to pass,
            "habits" to habitsJson,
            "goals" to s.goals.map { g ->
                mapOf(
                    "id" to g.id,
                    "target" to g.target,
                    "title" to g.title,
                    "progress" to g.progress
                )
            }
        )
    }

    private fun vault(s: AppState): Map<String, Any?> {
        val total = s.accounts.sumOf { it.balance }
        val (whole, cents) = splitMoney(total)
        val acc = s.accounts.map { a ->
            mapOf(
                "id" to a.id,
                "title" to accountTitle(a),
                "subtitle" to "${a.bank} · ${a.type}",
                "balance" to formatMoney(a.balance),
                "tag" to accountTag(a),
                "accent" to (a.type.lowercase(Locale.US) == "tfsa")
            )
        }
        val tx = s.transactions.sortedByDescending { it.date }.take(8).map { t ->
            val title = t.note.ifBlank { t.category }
            mapOf(
                "id" to t.id,
                "title" to title,
                "meta" to "${t.category.uppercase(Locale.US)} · ${t.date}",
                "amount" to formatMoney(if (t.type == "expense") -kotlin.math.abs(t.amount) else t.amount),
                "account" to (s.accounts.find { it.id == t.accountId }?.let { accountTitle(it) } ?: "")
            )
        }
        return mapOf(
            "page" to "vault",
            "netWhole" to whole,
            "netCents" to cents,
            "trend" to "+2.4% THIS MONTH",
            "accounts" to acc,
            "transactions" to tx
        )
    }

    private fun accountTitle(a: Account): String =
        when (a.type.lowercase(Locale.US)) {
            "chequing" -> "CHECKING"
            "savings" -> "SAVINGS"
            "tfsa" -> "INVESTMENT"
            "credit" -> "CREDIT CARD"
            else -> a.name.uppercase(Locale.US)
        }

    private fun accountTag(a: Account): String =
        when (a.type.lowercase(Locale.US)) {
            "chequing" -> "PRIMARY"
            "tfsa" -> "ACTIVE GROWTH"
            "credit" -> "DUE IN 4D"
            else -> ""
        }

    private fun logs(s: AppState): Map<String, Any?> {
        val today = SeedData.today()
        val slog = s.supplementLogs[today] ?: HashMap()
        val morningList = s.supplements.filter { it.time == "morning" }.take(6)
        val morning = morningList.map { x ->
            mapOf(
                "id" to x.id,
                "name" to x.name,
                "detail" to x.dose,
                "taken" to (slog[x.id] == true)
            )
        }
        val takenN = morningList.count { slog[it.id] == true }
        val pct = if (morningList.isEmpty()) 0 else (takenN * 100 / morningList.size)
        return mapOf("page" to "logs", "completionPct" to pct, "morning" to morning)
    }

    private fun settings(s: AppState): Map<String, Any?> {
        val set = s.settings
        return mapOf(
            "page" to "settings",
            "reminderTime" to set.reminderTime,
            "obsidianMode" to set.obsidianMode,
            "hapticsEnabled" to set.hapticsEnabled,
            "appVersion" to BuildConfig.VERSION_NAME
        )
    }
}
