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
                Goal(
                    id = uid(),
                    title = "Cognitive fortress",
                    target = "MINDSET",
                    deadline = "",
                    progress = 85
                ),
                Goal(
                    id = uid(),
                    title = "Zenith protocol",
                    target = "PHYSICAL",
                    deadline = "",
                    progress = 28
                ),
                Goal(
                    id = uid(),
                    title = "Vault expansion",
                    target = "FINANCIAL",
                    deadline = "",
                    progress = 51
                )
            ),
            habits = arrayListOf(
                Habit("h_sleep", "Sleep consistency", "hrs", "timerange", 1, 7.0, 9.0),
                Habit("h_water", "Water intake", "ml", "quant", 2000, null, null),
                Habit("h_read", "Reading", "min", "quant", 30, null, null),
                Habit("h_medit", "Meditation", "", "check", 1, null, null),
                Habit("h_pray", "Pray to god", "", "check", 1, null, null),
                Habit("h_food", "No outside food", "", "check", 1, null, null),
                Habit("h_gym", "Gym", "", "check", 1, null, null),
                Habit("h_wake", "4am wake up", "", "check", 1, null, null),
                Habit("h_grat", "Gratitude (night)", "", "check", 1, null, null)
            ),
            supplements = arrayListOf(
                Supplement("s1", "Vitamin D3", "morning", "1000 IU"),
                Supplement("s2", "Omega 3 Fish Oil", "morning", "1 cap (with food)"),
                Supplement("s3", "Zinc", "morning", "15 mg"),
                Supplement("s4", "Rhodiola Rosea", "morning", "300 mg"),
                Supplement("s5", "Probiotic", "morning", "1 cap"),
                Supplement("s6", "Whey Protein Isolate", "workout", "1 scoop (after)"),
                Supplement("s7", "Electrolytes", "workout", "1 serving"),
                Supplement("s8", "Creatine", "workout", "5 g"),
                Supplement("s9", "L-Citrulline", "workout", "6 g"),
                Supplement("s10", "Magnesium Glycinate", "night", "400 mg"),
                Supplement("s11", "Ashwagandha", "night", "600 mg"),
                Supplement("s12", "Glycine", "night", "3 g"),
                Supplement("s13", "L-Theanine", "night", "200 mg"),
                Supplement("s14", "Inositol", "night", "2 g")
            ),
            accounts = arrayListOf(
                Account("a1", "Scotiabank Chequing", "chequing", "Scotiabank", 12450.0),
                Account("a2", "Scotiabank Savings", "savings", "Scotiabank", 45000.0),
                Account("a3", "Wealthsimple TFSA", "tfsa", "Wealthsimple", 364120.10),
                Account("a4", "Scotiabank Credit Card", "credit", "Scotiabank", -8679.68)
            ),
            categories = arrayListOf(
                "Hanging out with friends", "Shopping", "Phone bill",
                "Gym subscription", "Perplexity AI subscription",
                "Groceries", "Food", "Transport", "Rent", "Utilities",
                "Transfer", "Investment", "Other"
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
            transactions = arrayListOf(
                Transaction(uid(), "expense", 1299.0, "Electronics", "a1", t, "Apple Store Infinite Loop"),
                Transaction(uid(), "expense", 84.5, "Food", "a1", t, "Whole Foods Market"),
                Transaction(uid(), "expense", 42.0, "Lifestyle", "a1", t, "Blue Bottle Coffee"),
                Transaction(uid(), "income", 3200.0, "Transfer", "a1", t, "Payroll deposit")
            ),
            budget = Budget(monthlySavingsGoal = 500.0, monthlyBudget = 3500.0),
            settings = Settings()
        )
    }

    private fun uid(): String =
        UUID.randomUUID().toString().replace("-", "").take(12)
}
