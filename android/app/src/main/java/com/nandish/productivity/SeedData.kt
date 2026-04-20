package com.nandish.productivity

import java.time.LocalDate
import java.util.UUID

object SeedData {

    fun today(): String = LocalDate.now().toString()

    fun defaultState(): AppState {
        val t = today()
        return AppState(
            tasks = arrayListOf(
                Task(
                    id = uid(),
                    title = "Review structural engineering blueprints",
                    done = false,
                    priority = "high",
                    type = "work",
                    due = t
                ),
                Task(
                    id = uid(),
                    title = "Coordinate with logistics fleet",
                    done = false,
                    priority = "medium",
                    type = "errand",
                    due = t
                ),
                Task(
                    id = uid(),
                    title = "Silent meditation (20 mins)",
                    done = false,
                    priority = "low",
                    type = "personal",
                    due = t
                ),
                Task(
                    id = uid(),
                    title = "Review quarter 4 projections",
                    done = true,
                    priority = "medium",
                    type = "work",
                    due = t
                )
            ),
            goals = arrayListOf(
                Goal(uid(), "Cognitive fortress", "MINDSET", "", 65),
                Goal(uid(), "Zenith protocol", "PHYSICAL", "", 28),
                Goal(uid(), "Vault expansion", "FINANCIAL", "", 92)
            ),
            habits = arrayListOf(
                Habit("h_sleep", "Sleep consistency", "hrs", "timerange", 1, 7.0, 9.0),
                Habit("h_water", "Water intake", "ml", "quant", 2000, null, null),
                Habit("h_read", "Reading", "min", "quant", 30, null, null),
                Habit("h_medit", "Meditation", "", "check", 1, null, null),
                Habit("h_gym", "Gym", "", "check", 1, null, null)
            ),
            supplements = arrayListOf(
                Supplement("s1", "Magnesium Glycinate", "morning", "250mg"),
                Supplement("s2", "Vitamin D3", "morning", "1000 IU"),
                Supplement("s3", "Omega 3", "morning", "1 cap"),
                Supplement("s4", "Rhodiola", "morning", "300 mg")
            ),
            accounts = arrayListOf(
                Account("a1", "Scotiabank Chequing", "chequing", "Scotiabank", 12450.0),
                Account("a2", "Scotiabank Savings", "savings", "Scotiabank", 45000.0),
                Account("a3", "Wealthsimple TFSA", "tfsa", "Wealthsimple", 364120.10),
                Account("a4", "Scotiabank Credit Card", "credit", "Scotiabank", -8679.68)
            ),
            transactions = arrayListOf(
                Transaction(uid(), "expense", 260.0, "Lifestyle", "a1", t, "Equinox Fitness"),
                Transaction(uid(), "income", 1450.20, "Investment", "a3", t, "Dividend Payout"),
                Transaction(uid(), "expense", 112.50, "Dining", "a4", t, "The Nomad Library")
            ),
            notes = arrayListOf(
                Note(
                    id = uid(),
                    title = "Obsidian Design Principles",
                    body = "Hierarchy is not achieved through color, but through the management of light and surface.",
                    tags = arrayListOf("Design System"),
                    updated = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L
                )
            ),
            settings = Settings()
        )
    }

    private fun uid(): String =
        UUID.randomUUID().toString().replace("-", "").take(12)
}
