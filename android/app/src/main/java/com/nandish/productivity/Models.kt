package com.nandish.productivity

import com.google.gson.annotations.SerializedName

data class Task(
    var id: String = "",
    var title: String = "",
    var done: Boolean = false,
    var priority: String = "medium",
    var type: String = "personal",
    var due: String = "",
    var recurrence: String = "none",
    var remindAt: String = "",
    var remindFired: Boolean = false
)

data class Goal(
    var id: String = "",
    var title: String = "",
    var target: String = "",
    var deadline: String = "",
    var progress: Int = 0
)

data class Habit(
    var id: String = "",
    var name: String = "",
    var unit: String = "",
    var type: String = "check",
    var target: Int = 1,
    var healthyMin: Double? = null,
    var healthyMax: Double? = null
)

data class Supplement(
    var id: String = "",
    var name: String = "",
    var time: String = "morning",
    var dose: String = ""
)

data class Account(
    var id: String = "",
    var name: String = "",
    var type: String = "",
    var bank: String = "",
    var balance: Double = 0.0
)

data class Transaction(
    var id: String = "",
    var type: String = "expense",
    var amount: Double = 0.0,
    var category: String = "",
    var accountId: String = "",
    var date: String = "",
    var note: String = ""
)

data class Note(
    var id: String = "",
    var title: String = "",
    var body: String = "",
    var tags: ArrayList<String> = arrayListOf(),
    var updated: Long = 0L
)

data class Budget(
    var monthlySavingsGoal: Double = 500.0,
    var monthlyBudget: Double = 0.0
)

data class Settings(
    var reminderTime: String = "21:00",
    var theme: String = "dark"
)

data class AppState(
    var tasks: ArrayList<Task> = arrayListOf(),
    var goals: ArrayList<Goal> = arrayListOf(),
    var habits: ArrayList<Habit> = arrayListOf(),
    var habitLogs: HashMap<String, HashMap<String, String>> = HashMap(),
    var supplements: ArrayList<Supplement> = arrayListOf(),
    var supplementLogs: HashMap<String, HashMap<String, Boolean>> = HashMap(),
    var accounts: ArrayList<Account> = arrayListOf(),
    var categories: ArrayList<String> = arrayListOf(),
    var transactions: ArrayList<Transaction> = arrayListOf(),
    var budget: Budget = Budget(),
    var notes: ArrayList<Note> = arrayListOf(),
    var settings: Settings = Settings(),
    @SerializedName("schemaVersion") var schemaVersion: Int = 4
)
