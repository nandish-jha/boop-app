package com.prodash.reminders.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReminderRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun collection() = auth.currentUser?.let { user ->
        firestore.collection("users").document(user.uid).collection("reminders")
    }

    fun remindersFlow(): Flow<List<Reminder>> = callbackFlow {
        val col = collection()
        if (col == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        registration = col.orderBy("dueEpochMillis", Query.Direction.ASCENDING).addSnapshotListener { snap, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull { doc -> doc.toReminder() } ?: emptyList()
            trySend(list)
        }

        awaitClose { registration?.remove() }
    }

    suspend fun upsert(reminder: Reminder): String {
        val col = collection() ?: return reminder.id
        val payload = mapOf(
            "type" to reminder.type.name,
            "title" to reminder.title,
            "body" to reminder.body,
            "imageUri" to reminder.imageUri,
            "dueEpochMillis" to reminder.dueEpochMillis,
            "completed" to reminder.completed,
            "createdEpochMillis" to reminder.createdEpochMillis,
        )
        return if (reminder.id.isBlank()) {
            val ref = col.document()
            ref.set(payload).await()
            ref.id
        } else {
            col.document(reminder.id).set(payload).await()
            reminder.id
        }
    }

    suspend fun setCompleted(reminderId: String, completed: Boolean) {
        val col = collection() ?: return
        col.document(reminderId).update("completed", completed).await()
    }

    suspend fun updateDue(reminderId: String, dueEpochMillis: Long) {
        val col = collection() ?: return
        col.document(reminderId).update("dueEpochMillis", dueEpochMillis).await()
    }

    suspend fun deleteCompletedTaskItems(idsToCancel: List<String>): Int {
        if (idsToCancel.isEmpty()) return 0
        val col = collection() ?: return 0
        var deleted = 0
        var batch: WriteBatch = firestore.batch()
        var count = 0
        idsToCancel.forEachIndexed { index, id ->
            batch.delete(col.document(id))
            count++
            val flush = count == 450 || index == idsToCancel.lastIndex
            if (flush) {
                batch.commit().await()
                deleted += count
                batch = firestore.batch()
                count = 0
            }
        }
        return deleted
    }

    suspend fun fetchAllOnce(): List<Reminder> {
        val col = collection() ?: return emptyList()
        val snap = col.orderBy("dueEpochMillis", Query.Direction.ASCENDING).get().await()
        return snap.documents.mapNotNull { it.toReminder() }
    }

    suspend fun fetchOne(reminderId: String): Reminder? {
        val col = collection() ?: return null
        val doc = col.document(reminderId).get().await()
        if (!doc.exists()) return null
        return doc.toReminder()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReminder(): Reminder? {
        val id = id ?: return null
        val type = getString("type")
            ?.let { value -> runCatching { ReminderType.valueOf(value) }.getOrNull() }
            ?: ReminderType.TASK
        val title = getString("title") ?: return null
        val body = getString("body") ?: ""
        val imageUri = getString("imageUri")
        val due = getLong("dueEpochMillis") ?: return null
        val completed = getBoolean("completed") ?: false
        val created = getLong("createdEpochMillis") ?: System.currentTimeMillis()
        return Reminder(
            id = id,
            type = type,
            title = title,
            body = body,
            imageUri = imageUri,
            dueEpochMillis = due,
            completed = completed,
            createdEpochMillis = created,
        )
    }
}
