package com.prodash.reminders

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

object BoopSyncState {
    var lastSyncMillis: Long = 0L
    var lastSyncError: String? = null
    var lastSyncOk: Boolean = false
    var signedInUid: String? = null
    var signedInEmail: String? = null
    var isGoogleLinked: Boolean = false
}

enum class PaletteFamily(val storageKey: String, val label: String) {
    AMOLED("amoled", "Material You"),
    TERRACOTTA("terracotta", "Terracotta"),
    ROSE("rose", "Rose"),
    SLATE("slate", "Slate"),
    FOREST("forest", "Forest"),
    OCEAN("ocean", "Ocean"),
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
    "afternoon" -> "afternoon"
    "evening" -> "evening"
    "night" -> "night"
    else -> "morning"
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
    /** Google Tasks–style notes/details. */
    val details: String = "",
    /** JSON array of subtasks: [{id,text,done}, ...] */
    val subtasksJson: String = "",
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
    val dayPeriodCategory: String = "morning",
    val goal: Int,
    val progress: Int,
    val dayKeys: String = "",
    val quantityMode: Boolean = false,
    val quantityUnit: String = "",
    val quantityDailyTarget: Int = 30,
    val quantityDayValues: String = "",
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
)
data class BoopAccount(
    val id: String,
    val name: String,
    val openingBalance: Double = 0.0,
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

    fun currentUserId(): String? = auth.currentUser?.uid?.also { refreshAuthMeta() }

    fun refreshAuthMeta() {
        val user = auth.currentUser
        BoopSyncState.signedInUid = user?.uid
        BoopSyncState.signedInEmail = user?.email ?: user?.providerData
            ?.firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID }
            ?.email
        BoopSyncState.isGoogleLinked = user?.providerData
            ?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true
    }

    private fun friendlyAuthError(error: Exception): String {
        val raw = error.message.orEmpty()
        return when {
            raw.contains("ADMIN_RESTRICTED", ignoreCase = true) ||
                raw.contains("operation is not allowed", ignoreCase = true) ||
                raw.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
                "Anonymous sign-in is turned off in Firebase. Use Sign in with Google."
            raw.contains("network", ignoreCase = true) ->
                "Network error — check your connection"
            raw.contains("credential-already-in-use", ignoreCase = true) ->
                "This Google account is already used on another Boop profile"
            raw.contains("provider-already-linked", ignoreCase = true) ->
                "Google account already linked"
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

    private fun parseIdArray(raw: String): JSONArray =
        runCatching { JSONArray(raw) }.getOrElse { JSONArray() }

    private fun mergeJsonArraysById(
        localRaw: String,
        remoteRaw: String?,
        preferNewerKey: String? = null,
    ): String {
        if (remoteRaw.isNullOrBlank()) return localRaw
        val byId = linkedMapOf<String, JSONObject>()
        fun ingest(raw: String, preferIncoming: Boolean) {
            val arr = parseIdArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id")
                if (id.isBlank()) continue
                val existing = byId[id]
                if (existing == null) {
                    byId[id] = obj
                    continue
                }
                if (!preferIncoming) continue
                if (preferNewerKey != null) {
                    val incomingTs = obj.optLong(preferNewerKey, 0L)
                    val existingTs = existing.optLong(preferNewerKey, 0L)
                    if (incomingTs >= existingTs) byId[id] = obj
                } else {
                    byId[id] = obj
                }
            }
        }
        // Remote first, then local wins on conflicts (device is source of truth when both exist).
        ingest(remoteRaw, preferIncoming = true)
        ingest(localRaw, preferIncoming = true)
        val out = JSONArray()
        byId.values.forEach { out.put(it) }
        return out.toString()
    }

    private fun mergeTasksJson(localRaw: String, remoteRaw: String?): String {
        if (remoteRaw.isNullOrBlank()) return localRaw
        val byId = linkedMapOf<String, JSONObject>()
        fun ingest(raw: String) {
            val arr = parseIdArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val id = obj.optString("id")
                if (id.isBlank()) continue
                val existing = byId[id]
                if (existing == null) {
                    byId[id] = obj
                    continue
                }
                val incomingDone = obj.optBoolean("done", false)
                val existingDone = existing.optBoolean("done", false)
                when {
                    !incomingDone && existingDone -> byId[id] = obj
                    incomingDone && !existingDone -> Unit
                    else -> {
                        if (obj.optLong("reminderAt", 0L) >= existing.optLong("reminderAt", 0L)) {
                            byId[id] = obj
                        }
                    }
                }
            }
        }
        ingest(remoteRaw)
        ingest(localRaw)
        val out = JSONArray()
        byId.values.sortedBy { it.optLong("reminderAt", 0L) }.forEach { out.put(it) }
        return out.toString()
    }

    private fun applyMergedCloud(remote: Map<String, Any?>) {
        val keys = listOf("tasks", "notes", "habits", "accounts", "ledgerEntries")
        keys.forEach { key ->
            val remoteRaw = remote[key] as? String
            val localRaw = store.read(key)
            val merged = when (key) {
                "tasks" -> mergeTasksJson(localRaw, remoteRaw)
                "notes" -> mergeJsonArraysById(localRaw, remoteRaw, preferNewerKey = "updatedAt")
                "ledgerEntries" -> mergeJsonArraysById(localRaw, remoteRaw, preferNewerKey = "createdAt")
                else -> mergeJsonArraysById(localRaw, remoteRaw)
            }
            store.save(key, merged)
        }
    }

    /**
     * Sign in / link with Google so phone and web share the same Firebase UID.
     * - Anonymous user: try link; if Google already exists elsewhere, switch to that account and keep local data.
     * - Already signed in: refresh session with Google credential.
     */
    fun signInOrLinkGoogle(idToken: String, onComplete: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val current = auth.currentUser

        fun afterGoogleAuth(successMessage: String?) {
            refreshAuthMeta()
            syncBidirectional { ok, error ->
                if (ok) onComplete(true, successMessage)
                else onComplete(true, successMessage ?: "Signed in — sync had an issue: ${error ?: "unknown"}")
            }
        }

        fun signIntoGoogleAccount() {
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    afterGoogleAuth("Signed in with Google — phone and web can now sync")
                }
                .addOnFailureListener { error ->
                    onComplete(false, friendlyAuthError(error))
                }
        }

        if (current == null) {
            signIntoGoogleAccount()
            return
        }

        if (current.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }) {
            // Already linked — re-auth then sync.
            current.reauthenticate(credential)
                .addOnSuccessListener {
                    afterGoogleAuth("Already signed in with Google — synced")
                }
                .addOnFailureListener {
                    // Reauth failed (token mismatch) — still try sync with current session.
                    afterGoogleAuth("Google already linked — synced")
                }
            return
        }

        // Anonymous (or other) → link Google to keep the same UID when possible.
        current.linkWithCredential(credential)
            .addOnSuccessListener {
                afterGoogleAuth("Google linked — use the same account on the web app")
            }
            .addOnFailureListener { error ->
                val alreadyInUse = error.message?.contains("credential-already-in-use", ignoreCase = true) == true ||
                    error.message?.contains("email-already-in-use", ignoreCase = true) == true
                if (alreadyInUse) {
                    // Google account already has a Boop profile — switch to it; local SharedPreferences stay on device.
                    signIntoGoogleAccount()
                } else {
                    onComplete(false, friendlyAuthError(error))
                }
            }
    }

    fun ensureAnonymousAuth(onComplete: (Boolean, String?) -> Unit) {
        store.init(AppContextHolder.context)
        val existing = auth.currentUser
        if (existing != null) {
            refreshAuthMeta()
            BoopSyncState.lastSyncError = null
            onComplete(true, null)
            return
        }
        // Anonymous auth is often disabled in Firebase ("operation is not allowed to admins/restricted").
        // Do not attempt it — Google sign-in is required for cloud sync.
        onComplete(false, "Sign in with Google to enable cloud sync")
    }

    fun ensureSession(onRemoteLoaded: () -> Unit, onFailure: ((String) -> Unit)? = null) {
        store.init(AppContextHolder.context)
        refreshAuthMeta()
        val uid = auth.currentUser?.uid
        if (uid == null) {
            // Local-only until the user signs in with Google.
            onRemoteLoaded()
            return
        }
        db.collection("boopUsers").document(uid).get()
            .addOnSuccessListener { snap ->
                if (snap.exists()) {
                    applyMergedCloud(snap.data ?: emptyMap())
                }
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

    fun syncBidirectional(onComplete: (Boolean, String?) -> Unit) {
        refreshAuthMeta()
        val user = auth.currentUser
        if (user == null) {
            onComplete(false, "Sign in with Google first, then tap Sync now")
            return
        }
        if (!BoopSyncState.isGoogleLinked) {
            onComplete(false, "Sign in with Google first — guest/anonymous sync is disabled")
            return
        }
        val uid = user.uid
        db.collection("boopUsers").document(uid).get()
            .addOnSuccessListener { snap ->
                if (snap.exists()) {
                    applyMergedCloud(snap.data ?: emptyMap())
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
            .addOnFailureListener { error ->
                val msg = friendlyFirestoreError(error)
                BoopSyncState.lastSyncOk = false
                BoopSyncState.lastSyncError = msg
                onComplete(false, msg)
            }
    }

    fun pushAllToCloud(onComplete: (Boolean, String?) -> Unit) {
        syncBidirectional(onComplete)
    }

    fun signOutCloud(onComplete: () -> Unit) {
        auth.signOut()
        BoopSyncState.signedInUid = null
        BoopSyncState.signedInEmail = null
        BoopSyncState.isGoogleLinked = false
        BoopSyncState.lastSyncError = null
        onComplete()
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
                details = item.optString("details"),
                subtasksJson = item.optString("subtasksJson"),
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
                normalizeHabitCategory(item.optString("dayPeriodCategory", "morning")),
                item.getInt("goal"),
                item.getInt("progress"),
                item.optString("dayKeys"),
                item.optBoolean("quantityMode", false),
                item.optString("quantityUnit"),
                item.optInt("quantityDailyTarget", 30),
                item.optString("quantityDayValues"),
                item.optBoolean("reminderEnabled", false),
                item.optInt("reminderHour", 9).coerceIn(0, 23),
                item.optInt("reminderMinute", 0).coerceIn(0, 59),
            )
        }.sortedBy { it.title.lowercase(Locale.getDefault()) }
    }

    fun readAccounts(): List<BoopAccount> {
        return parseArray(store.read("accounts")) { item ->
            BoopAccount(
                id = item.getString("id"),
                name = item.optString("name"),
                openingBalance = item.optDouble("openingBalance", 0.0),
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

    private fun habitToJson(habit: BoopHabit): JSONObject =
        JSONObject()
            .put("id", habit.id)
            .put("title", habit.title)
            .put("dayPeriodCategory", normalizeHabitCategory(habit.dayPeriodCategory))
            .put("goal", habit.goal)
            .put("progress", habit.progress)
            .put("dayKeys", habit.dayKeys)
            .put("quantityMode", habit.quantityMode)
            .put("quantityUnit", habit.quantityUnit)
            .put("quantityDailyTarget", habit.quantityDailyTarget)
            .put("quantityDayValues", habit.quantityDayValues)
            .put("reminderEnabled", habit.reminderEnabled)
            .put("reminderHour", habit.reminderHour.coerceIn(0, 23))
            .put("reminderMinute", habit.reminderMinute.coerceIn(0, 59))

    fun saveHabit(habit: BoopHabit) {
        val updated = readHabits().toMutableList().apply {
            removeAll { it.id == habit.id }
            add(0, habit.copy(dayPeriodCategory = normalizeHabitCategory(habit.dayPeriodCategory)))
        }
        val arr = JSONArray()
        updated.forEach { arr.put(habitToJson(it)) }
        store.save("habits", arr.toString())
        sync("habits", arr.toString())
    }

    fun deleteHabit(id: String) {
        val updated = readHabits().filterNot { it.id == id }
        val arr = JSONArray()
        updated.forEach { arr.put(habitToJson(it)) }
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
                    .put("openingBalance", it.openingBalance)
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
                    .put("openingBalance", it.openingBalance)
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
                    .put("archived", it.archived)
                    .put("details", it.details)
                    .put("subtasksJson", it.subtasksJson),
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
