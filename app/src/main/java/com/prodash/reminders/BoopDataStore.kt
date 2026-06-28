package com.prodash.reminders

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

object BoopSyncState {
    var lastSyncMillis: Long = 0L
    var lastSyncError: String? = null
    var lastSyncOk: Boolean = false
    var signedInUid: String? = null
}

enum class PaletteFamily(val storageKey: String, val label: String) {
    AMOLED("amoled", "AMOLED"),
    TERRACOTTA("terracotta", "Terracotta"),
    ;

    companion object {
        fun fromStorage(value: String?) = entries.find { it.storageKey == value } ?: AMOLED
    }
}

enum class ThemeMode(val storageKey: String, val label: String) {
    DARK("dark", "Dark"),
    LIGHT("light", "Light"),
    SYSTEM("system", "System"),
    ;

    companion object {
        fun fromStorage(value: String?) = entries.find { it.storageKey == value } ?: SYSTEM
    }
}

fun normalizeHabitCategory(raw: String): String = when (raw.lowercase(Locale.getDefault())) {
    "night" -> "night"
    else -> "day"
}

object AppContextHolder {
    lateinit var context: Context
}

object LocalStore {
    private const val PREFS = "boop_store"

    fun init(context: Context) {
        AppContextHolder.context = context.applicationContext
    }

    private fun pref() = AppContextHolder.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(key: String, payload: String) = pref().edit().putString(key, payload).apply()
    fun read(key: String): String = pref().getString(key, "[]").orEmpty()

    fun readThemeMode(): ThemeMode = ThemeMode.fromStorage(pref().getString("theme_mode", null))
    fun saveThemeMode(mode: ThemeMode) = pref().edit().putString("theme_mode", mode.storageKey).apply()

    fun readPaletteFamily(): PaletteFamily = PaletteFamily.fromStorage(pref().getString("palette_family", null))
    fun savePaletteFamily(family: PaletteFamily) = pref().edit().putString("palette_family", family.storageKey).apply()

    fun readShowHabitsPage(): Boolean = pref().getBoolean("show_habits_page", true)
    fun saveShowHabitsPage(show: Boolean) = pref().edit().putBoolean("show_habits_page", show).apply()

    fun readShowWalletPage(): Boolean = pref().getBoolean("show_wallet_page", true)
    fun saveShowWalletPage(show: Boolean) = pref().edit().putBoolean("show_wallet_page", show).apply()
}

object BoopData {
    fun repository(context: Context): BoopRepository {
        LocalStore.init(context)
        return BoopRepository(LocalStore)
    }
}
data class BoopTask(
    val id: String,
    val title: String,
    val reminderAt: Long,
    val done: Boolean,
    val repeatEveryDays: Int = 0,
    val linkedNoteId: String? = null,
    /** Filed away from the main list (separate from [done]). */
    val archived: Boolean = false,
)
data class BoopNote(
    val id: String,
    val title: String,
    val body: String,
    val attachmentUri: String?,
    val audioUri: String? = null,
    val tagsCsv: String = "",
    val ocrText: String = "",
    val linkedTaskId: String? = null,
    val archived: Boolean = false,
    /** First save time (local). */
    val createdAtMillis: Long = 0L,
    /** Last save time (local), used for week strip & search ordering. */
    val updatedAtMillis: Long = 0L,
)
/** [dayKeys] comma-separated yyyyMMdd calendar days marked done (dashboard strip). */
data class BoopHabit(
    val id: String,
    val title: String,
    val dayPeriodCategory: String = "day",
    val goal: Int,
    val progress: Int,
    val dayKeys: String = "",
    val quantityMode: Boolean = false,
    val quantityUnit: String = "",
    val quantityDailyTarget: Int = 30,
    val quantityDayValues: String = "",
)
data class BoopAccount(
    val id: String,
    val name: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
data class BoopLedgerEntry(
    val id: String,
    val type: String, // income | expense | transfer
    val accountId: String,
    val toAccountId: String? = null,
    val amount: Double,
    val title: String,
    val category: String = "",
    val subcategory: String = "",
    val note: String = "",
    val dueAtMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
class BoopRepository(private val store: LocalStore) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun currentUserId(): String? = auth.currentUser?.uid?.also { BoopSyncState.signedInUid = it }

    private fun friendlyAuthError(error: Exception): String {
        val raw = error.message.orEmpty()
        return when {
            raw.contains("ADMIN_RESTRICTED", ignoreCase = true) ||
                raw.contains("operation is not allowed", ignoreCase = true) ->
                "Anonymous sign-in is disabled in Firebase. Local data still works."
            raw.contains("network", ignoreCase = true) ->
                "Network error — check your connection"
            else -> raw.ifBlank { "Sign-in failed" }
        }
    }

    private fun friendlyFirestoreError(error: Exception): String {
        val raw = error.message.orEmpty()
        return when {
            raw.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Cloud permission denied — deploy Firestore rules for boopUsers. Local data is safe."
            raw.contains("network", ignoreCase = true) ->
                "Network error — could not reach cloud"
            else -> raw.ifBlank { "Cloud sync failed" }
        }
    }

    private fun jsonArrayLength(raw: String): Int =
        runCatching { JSONArray(raw).length() }.getOrDefault(0)

    /** Only apply remote data when local store for that key is empty — avoids wiping local tasks. */
    private fun mergeRemoteField(key: String, remoteValue: String?) {
        if (remoteValue.isNullOrBlank()) return
        val localRaw = store.read(key)
        val localEmpty = jsonArrayLength(localRaw) == 0 || localRaw == "[]"
        if (localEmpty) {
            store.save(key, remoteValue)
        }
    }

    fun ensureAnonymousAuth(onComplete: (Boolean, String?) -> Unit) {
        store.init(AppContextHolder.context)
        val existing = auth.currentUser
        if (existing != null) {
            BoopSyncState.signedInUid = existing.uid
            BoopSyncState.lastSyncError = null
            onComplete(true, null)
            return
        }
        auth.signInAnonymously()
            .addOnSuccessListener {
                BoopSyncState.signedInUid = auth.currentUser?.uid
                BoopSyncState.lastSyncError = null
                onComplete(true, null)
            }
            .addOnFailureListener { error ->
                val msg = friendlyAuthError(error)
                BoopSyncState.lastSyncOk = false
                BoopSyncState.lastSyncError = msg
                BoopSyncState.signedInUid = null
                onComplete(false, msg)
            }
    }

    fun ensureSession(onRemoteLoaded: () -> Unit, onFailure: ((String) -> Unit)? = null) {
        ensureAnonymousAuth { signedIn, authError ->
            if (!signedIn) {
                authError?.let { onFailure?.invoke(it) }
                onRemoteLoaded()
                return@ensureAnonymousAuth
            }
            val uid = auth.currentUser?.uid
            if (uid == null) {
                onRemoteLoaded()
                return@ensureAnonymousAuth
            }
            db.collection("boopUsers").document(uid).get()
                .addOnSuccessListener { snap ->
                    mergeRemoteField("tasks", snap.getString("tasks"))
                    mergeRemoteField("notes", snap.getString("notes"))
                    mergeRemoteField("habits", snap.getString("habits"))
                    mergeRemoteField("accounts", snap.getString("accounts"))
                    mergeRemoteField("ledgerEntries", snap.getString("ledgerEntries"))
                    BoopSyncState.lastSyncMillis = System.currentTimeMillis()
                    BoopSyncState.lastSyncOk = true
                    BoopSyncState.lastSyncError = null
                    onRemoteLoaded()
                }
                .addOnFailureListener { error ->
                    val msg = friendlyFirestoreError(error)
                    BoopSyncState.lastSyncOk = false
                    BoopSyncState.lastSyncError = msg
                    onFailure?.invoke(msg)
                    onRemoteLoaded()
                }
        }
    }

    fun pushAllToCloud(onComplete: (Boolean, String?) -> Unit) {
        ensureAnonymousAuth { signedIn, authError ->
            if (!signedIn) {
                onComplete(false, authError ?: "Not signed in")
                return@ensureAnonymousAuth
            }
            val uid = auth.currentUser?.uid ?: run {
                onComplete(false, "Not signed in")
                return@ensureAnonymousAuth
            }
            val payload = mapOf(
                "tasks" to store.read("tasks"),
                "notes" to store.read("notes"),
                "habits" to store.read("habits"),
                "accounts" to store.read("accounts"),
                "ledgerEntries" to store.read("ledgerEntries"),
            )
            db.collection("boopUsers").document(uid)
                .set(payload, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    BoopSyncState.lastSyncMillis = System.currentTimeMillis()
                    BoopSyncState.lastSyncOk = true
                    BoopSyncState.lastSyncError = null
                    onComplete(true, null)
                }
                .addOnFailureListener { error ->
                    val msg = friendlyFirestoreError(error)
                    BoopSyncState.lastSyncOk = false
                    BoopSyncState.lastSyncError = msg
                    onComplete(false, msg)
                }
        }
    }

    fun exportBackupJson(): String = JSONObject()
        .put("version", 1)
        .put("exportedAt", System.currentTimeMillis())
        .put("tasks", store.read("tasks"))
        .put("notes", store.read("notes"))
        .put("habits", store.read("habits"))
        .put("accounts", store.read("accounts"))
        .put("ledgerEntries", store.read("ledgerEntries"))
        .toString()

    fun importBackupJson(raw: String): Boolean = try {
        val root = JSONObject(raw)
        root.optString("tasks").takeIf { it.isNotBlank() }?.let { store.save("tasks", it) }
        root.optString("notes").takeIf { it.isNotBlank() }?.let { store.save("notes", it) }
        root.optString("habits").takeIf { it.isNotBlank() }?.let { store.save("habits", it) }
        root.optString("accounts").takeIf { it.isNotBlank() }?.let { store.save("accounts", it) }
        root.optString("ledgerEntries").takeIf { it.isNotBlank() }?.let { store.save("ledgerEntries", it) }
        pushAllToCloud { _, _ -> }
        true
    } catch (_: Throwable) {
        false
    }

    /** Returns rescheduled task when a repeating reminder should fire again. */
    fun completeTaskFromNotification(taskId: String): BoopTask? {
        val tasks = readTasks().toMutableList()
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index < 0) return null
        val current = tasks[index]
        if (current.repeatEveryDays > 0) {
            val nextAt = nextRepeatReminderMillis(current.reminderAt, current.repeatEveryDays)
            val updated = current.copy(reminderAt = nextAt, done = false)
            tasks[index] = updated
            upsertTasks(tasks, null)
            return updated
        }
        if (current.done) return null
        val updated = current.copy(done = true)
        tasks[index] = updated
        upsertTasks(tasks, null)
        return null
    }

    fun readTasks(): List<BoopTask> {
        return parseArray(store.read("tasks")) { item ->
            val hasArchivedKey = item.has("archived")
            val archived = if (hasArchivedKey) item.optBoolean("archived", false) else false
            val done = item.optBoolean("done", false)
            BoopTask(
                id = item.getString("id"),
                title = item.getString("title"),
                reminderAt = item.getLong("reminderAt"),
                done = done,
                repeatEveryDays = item.optInt("repeatEveryDays", 0),
                linkedNoteId = item.optString("linkedNoteId").ifBlank { null },
                archived = archived,
            )
        }.sortedBy { it.reminderAt }
    }

    fun readNotes(): List<BoopNote> {
        val json = store.read("notes")
        val arr = JSONArray(json)
        val out = mutableListOf<BoopNote>()
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            val rawU = item.optLong("updatedAt", 0L)
            val u = if (rawU == 0L) {
                System.currentTimeMillis() - i * 3_600_000L
            } else {
                rawU
            }
            val createdRaw = item.optLong("createdAt", 0L)
            val createdAt = if (createdRaw > 0L) createdRaw else u
            out.add(
                BoopNote(
                    id = item.getString("id"),
                    title = item.optString("title"),
                    body = item.optString("body"),
                    attachmentUri = item.optString("attachmentUri").ifBlank { null },
                    audioUri = item.optString("audioUri").ifBlank { null },
                    tagsCsv = item.optString("tags"),
                    ocrText = item.optString("ocrText"),
                    linkedTaskId = item.optString("linkedTaskId").ifBlank { null },
                    archived = item.optBoolean("archived", false),
                    createdAtMillis = createdAt,
                    updatedAtMillis = u,
                ),
            )
        }
        return out.sortedByDescending { it.createdAtMillis + it.updatedAtMillis }
    }

    fun readHabits(): List<BoopHabit> {
        return parseArray(store.read("habits")) { item ->
            BoopHabit(
                item.getString("id"),
                item.getString("title"),
                normalizeHabitCategory(item.optString("dayPeriodCategory", "day")),
                item.getInt("goal"),
                item.getInt("progress"),
                item.optString("dayKeys"),
                item.optBoolean("quantityMode", false),
                item.optString("quantityUnit"),
                item.optInt("quantityDailyTarget", 30),
                item.optString("quantityDayValues"),
            )
        }.sortedBy { it.title.lowercase(Locale.getDefault()) }
    }

    fun readAccounts(): List<BoopAccount> {
        return parseArray(store.read("accounts")) { item ->
            BoopAccount(
                id = item.getString("id"),
                name = item.optString("name"),
                createdAtMillis = item.optLong("createdAt", System.currentTimeMillis()),
            )
        }.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    fun readLedgerEntries(): List<BoopLedgerEntry> {
        return parseArray(store.read("ledgerEntries")) { item ->
            BoopLedgerEntry(
                id = item.getString("id"),
                type = item.optString("type", "expense"),
                accountId = item.optString("accountId"),
                toAccountId = item.optString("toAccountId").ifBlank { null },
                amount = item.optDouble("amount", 0.0),
                title = item.optString("title"),
                category = item.optString("category"),
                subcategory = item.optString("subcategory"),
                note = item.optString("note"),
                dueAtMillis = item.optLong("dueAt", 0L).takeIf { it > 0L },
                createdAtMillis = item.optLong("createdAt", System.currentTimeMillis()),
            )
        }.sortedByDescending { it.createdAtMillis }
    }

    fun saveTask(task: BoopTask) {
        upsertTasks(readTasks(), task)
    }

    fun deleteTask(id: String) {
        val updated = readTasks().filterNot { it.id == id }
        upsertTasks(updated, null)
    }

    fun saveNote(note: BoopNote) {
        val existing = readNotes().firstOrNull { it.id == note.id }
        val created = when {
            note.createdAtMillis > 0L -> note.createdAtMillis
            existing != null && existing.createdAtMillis > 0L -> existing.createdAtMillis
            existing != null && existing.updatedAtMillis > 0L -> existing.updatedAtMillis
            else -> System.currentTimeMillis()
        }
        val stamped = note.copy(createdAtMillis = created, updatedAtMillis = System.currentTimeMillis())
        val updated = readNotes().toMutableList().apply {
            removeAll { it.id == stamped.id }
            add(0, stamped)
        }
        val arr = JSONArray()
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("body", it.body)
                    .put("attachmentUri", it.attachmentUri ?: "")
                    .put("audioUri", it.audioUri ?: "")
                    .put("tags", it.tagsCsv)
                    .put("ocrText", it.ocrText)
                    .put("linkedTaskId", it.linkedTaskId ?: "")
                    .put("archived", it.archived)
                    .put("createdAt", it.createdAtMillis)
                    .put("updatedAt", it.updatedAtMillis),
            )
        }
        store.save("notes", arr.toString())
        sync("notes", arr.toString())
    }

    fun deleteNote(id: String) {
        val updated = readNotes().filterNot { it.id == id }
        val arr = JSONArray()
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("body", it.body)
                    .put("attachmentUri", it.attachmentUri ?: "")
                    .put("audioUri", it.audioUri ?: "")
                    .put("tags", it.tagsCsv)
                    .put("ocrText", it.ocrText)
                    .put("linkedTaskId", it.linkedTaskId ?: "")
                    .put("archived", it.archived)
                    .put("createdAt", it.createdAtMillis)
                    .put("updatedAt", it.updatedAtMillis),
            )
        }
        store.save("notes", arr.toString())
        sync("notes", arr.toString())
    }

    fun saveHabit(habit: BoopHabit) {
        val updated = readHabits().toMutableList().apply {
            removeAll { it.id == habit.id }
            add(0, habit.copy(dayPeriodCategory = normalizeHabitCategory(habit.dayPeriodCategory)))
        }
        val arr = JSONArray()
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("dayPeriodCategory", normalizeHabitCategory(it.dayPeriodCategory))
                    .put("goal", it.goal)
                    .put("progress", it.progress)
                    .put("dayKeys", it.dayKeys)
                    .put("quantityMode", it.quantityMode)
                    .put("quantityUnit", it.quantityUnit)
                    .put("quantityDailyTarget", it.quantityDailyTarget)
                    .put("quantityDayValues", it.quantityDayValues),
            )
        }
        store.save("habits", arr.toString())
        sync("habits", arr.toString())
    }

    fun deleteHabit(id: String) {
        val updated = readHabits().filterNot { it.id == id }
        val arr = JSONArray()
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("dayPeriodCategory", normalizeHabitCategory(it.dayPeriodCategory))
                    .put("goal", it.goal)
                    .put("progress", it.progress)
                    .put("dayKeys", it.dayKeys)
                    .put("quantityMode", it.quantityMode)
                    .put("quantityUnit", it.quantityUnit)
                    .put("quantityDailyTarget", it.quantityDailyTarget)
                    .put("quantityDayValues", it.quantityDayValues),
            )
        }
        store.save("habits", arr.toString())
        sync("habits", arr.toString())
    }

    fun saveAccount(account: BoopAccount) {
        val updated = readAccounts().toMutableList().apply {
            removeAll { it.id == account.id }
            add(0, account)
        }
        val arr = JSONArray()
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("createdAt", it.createdAtMillis),
            )
        }
        store.save("accounts", arr.toString())
        sync("accounts", arr.toString())
    }

    fun deleteAccount(accountId: String) {
        val updatedAccounts = readAccounts().filterNot { it.id == accountId }
        val accountsArr = JSONArray()
        updatedAccounts.forEach {
            accountsArr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("createdAt", it.createdAtMillis),
            )
        }
        store.save("accounts", accountsArr.toString())
        sync("accounts", accountsArr.toString())

        val updatedEntries = readLedgerEntries().filterNot { it.accountId == accountId || it.toAccountId == accountId }
        val entriesArr = JSONArray()
        updatedEntries.forEach {
            entriesArr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("type", it.type)
                    .put("accountId", it.accountId)
                    .put("toAccountId", it.toAccountId ?: "")
                    .put("amount", it.amount)
                    .put("title", it.title)
                    .put("category", it.category)
                    .put("subcategory", it.subcategory)
                    .put("note", it.note)
                    .put("dueAt", it.dueAtMillis ?: 0L)
                    .put("createdAt", it.createdAtMillis),
            )
        }
        store.save("ledgerEntries", entriesArr.toString())
        sync("ledgerEntries", entriesArr.toString())
    }

    fun saveLedgerEntry(entry: BoopLedgerEntry) {
        val updated = readLedgerEntries().toMutableList()
        val index = updated.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            updated[index] = entry
        } else {
            updated.add(0, entry)
        }
        val arr = JSONArray()
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("type", it.type)
                    .put("accountId", it.accountId)
                    .put("toAccountId", it.toAccountId ?: "")
                    .put("amount", it.amount)
                    .put("title", it.title)
                    .put("category", it.category)
                    .put("subcategory", it.subcategory)
                    .put("note", it.note)
                    .put("dueAt", it.dueAtMillis ?: 0L)
                    .put("createdAt", it.createdAtMillis),
            )
        }
        store.save("ledgerEntries", arr.toString())
        sync("ledgerEntries", arr.toString())
    }

    fun deleteLedgerEntry(id: String) {
        val updated = readLedgerEntries().filter { it.id != id }
        val arr = JSONArray()
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("type", it.type)
                    .put("accountId", it.accountId)
                    .put("toAccountId", it.toAccountId ?: "")
                    .put("amount", it.amount)
                    .put("title", it.title)
                    .put("category", it.category)
                    .put("subcategory", it.subcategory)
                    .put("note", it.note)
                    .put("dueAt", it.dueAtMillis ?: 0L)
                    .put("createdAt", it.createdAtMillis),
            )
        }
        store.save("ledgerEntries", arr.toString())
        sync("ledgerEntries", arr.toString())
    }

    private fun upsertTasks(tasks: List<BoopTask>, task: BoopTask?) {
        val updated = tasks.toMutableList().apply {
            task?.let {
                removeAll { item -> item.id == it.id }
                add(0, it)
            }
        }
        val arr = JSONArray()
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("reminderAt", it.reminderAt)
                    .put("done", it.done)
                    .put("repeatEveryDays", it.repeatEveryDays)
                    .put("linkedNoteId", it.linkedNoteId ?: "")
                    .put("archived", it.archived),
            )
        }
        store.save("tasks", arr.toString())
        sync("tasks", arr.toString())
    }

    private fun sync(key: String, value: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("boopUsers").document(uid)
            .set(mapOf(key to value), com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                BoopSyncState.lastSyncMillis = System.currentTimeMillis()
                BoopSyncState.lastSyncOk = true
                BoopSyncState.lastSyncError = null
            }
            .addOnFailureListener { error ->
                BoopSyncState.lastSyncOk = false
                BoopSyncState.lastSyncError = friendlyFirestoreError(error)
            }
    }

    private fun <T> parseArray(json: String, mapper: (JSONObject) -> T): List<T> {
        val array = JSONArray(json)
        val result = mutableListOf<T>()
        for (i in 0 until array.length()) result.add(mapper(array.getJSONObject(i)))
        return result
    }
}

fun nextRepeatReminderMillis(currentReminderAt: Long, repeatEveryDays: Int): Long {
    val step = repeatEveryDays * 24L * 60L * 60L * 1000L
    var next = currentReminderAt + step
    while (next <= System.currentTimeMillis()) next += step
    return next
}
