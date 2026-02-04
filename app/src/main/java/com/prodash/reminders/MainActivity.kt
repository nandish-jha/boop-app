package com.prodash.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ReminderNotifier.createChannel(this)
        setContent { BoopApp() }
    }
}

@Composable
private fun BoopApp() {
    val repository = remember { BoopRepository(LocalStore) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tasks = remember { mutableStateListOf<BoopTask>() }
    val notes = remember { mutableStateListOf<BoopNote>() }
    val habits = remember { mutableStateListOf<BoopHabit>() }

    fun refresh() {
        tasks.clear()
        tasks.addAll(repository.readTasks())
        notes.clear()
        notes.addAll(repository.readNotes())
        habits.clear()
        habits.addAll(repository.readHabits())
    }

    LaunchedEffect(Unit) {
        repository.ensureSession { refresh() }
        refresh()
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                AppContextHolder.context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val darkBg = Color(0xFF0C0C0D)
    val darkSurface = Color(0xFF151517)
    val accent = Color(0xFFFFFFFF)
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = darkBg,
            surface = darkSurface,
            primary = accent,
            onSurface = Color(0xFFF3F3F3),
            onBackground = Color(0xFFF3F3F3),
            onPrimary = Color.Black,
        ),
        typography = MaterialTheme.typography.copy(
            titleLarge = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
            ),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
        ),
    ) {
        Surface(Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = darkBg,
                bottomBar = {
                    NavigationBar(containerColor = Color.White) {
                        listOf("Home", "Tasks", "Notes", "Habits").forEachIndexed { index, label ->
                            val icon = when (label) {
                                "Home" -> Icons.Outlined.Dashboard
                                "Tasks" -> Icons.Outlined.Notifications
                                "Notes" -> Icons.Outlined.EditNote
                                else -> Icons.Outlined.Flag
                            }
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { Icon(icon, contentDescription = label, tint = Color.Black) },
                                label = { Text(label, color = Color.Black) },
                            )
                        }
                    }
                },
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                    when (selectedTab) {
                        0 -> DashboardScreen(
                            tasks = tasks,
                            notes = notes,
                            habits = habits,
                            onQuickAdd = { selectedTab = it },
                        )
                        1 -> TaskScreen(
                            tasks = tasks,
                            onSaveTask = { task ->
                                repository.saveTask(task)
                                ReminderScheduler.schedule(AppContextHolder.context, task)
                                refresh()
                            },
                            onDeleteTask = { id -> repository.deleteTask(id); refresh() },
                            onToggle = { task ->
                                repository.saveTask(task.copy(done = !task.done))
                                refresh()
                            },
                        )
                        2 -> NotesScreen(
                            notes = notes,
                            onSaveNote = { note -> repository.saveNote(note); refresh() },
                            onDeleteNote = { id -> repository.deleteNote(id); refresh() },
                        )
                        else -> HabitScreen(
                            habits = habits,
                            onSaveHabit = { habit -> repository.saveHabit(habit); refresh() },
                            onDeleteHabit = { id -> repository.deleteHabit(id); refresh() },
                            onProgress = { habit ->
                                repository.saveHabit(habit.copy(progress = (habit.progress + 1).coerceAtMost(habit.goal)))
                                refresh()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    tasks: List<BoopTask>,
    notes: List<BoopNote>,
    habits: List<BoopHabit>,
    onQuickAdd: (Int) -> Unit,
) {
    val completedTasks = tasks.count { it.done }
    val activeGoals = habits.count { it.progress < it.goal }
    val completion = if (habits.isEmpty()) 0 else habits.sumOf { (it.progress * 100) / it.goal } / habits.size
    var showQuickAdd by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Boop Dashboard", style = MaterialTheme.typography.titleLarge)
        DashboardCard("Tasks done", "$completedTasks / ${tasks.size}", Icons.Outlined.CheckCircle)
        DashboardCard("Notes", "${notes.size} captured", Icons.Outlined.EditNote)
        DashboardCard("Active goals", "$activeGoals running", Icons.Outlined.Flag)
        DashboardCard("Habit completion", "$completion%", Icons.Outlined.Dashboard)
        BoopWhiteButton(label = if (showQuickAdd) "Close add menu" else "Add item") { showQuickAdd = !showQuickAdd }
        if (showQuickAdd) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BoopWhiteButton(label = "Task") { onQuickAdd(1) }
                BoopWhiteButton(label = "Note") { onQuickAdd(2) }
                BoopWhiteButton(label = "Habit") { onQuickAdd(3) }
            }
        }
    }
}

@Composable
private fun DashboardCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = Color(0xFFE4E4E4))
            Spacer(Modifier.padding(8.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color(0xFFBFBFBF))
            }
        }
    }
}

@Composable
private fun TaskScreen(
    tasks: List<BoopTask>,
    onSaveTask: (BoopTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onToggle: (BoopTask) -> Unit,
) {
    val context = AppContextHolder.context
    var title by rememberSaveable { mutableStateOf("") }
    var taskToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis() + 30 * 60_000) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tasks + Reminders", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task") },
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Reminder: ${formatDateTime(selectedMillis)}", color = Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BoopWhiteButton(label = "Pick date") {
                val calendar = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val next = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                        next.set(year, month, day)
                        selectedMillis = next.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                ).show()
            }
            BoopWhiteButton(label = "Pick time") {
                val calendar = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val next = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                        next.set(Calendar.HOUR_OF_DAY, hour)
                        next.set(Calendar.MINUTE, minute)
                        next.set(Calendar.SECOND, 0)
                        selectedMillis = next.timeInMillis
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false,
                ).show()
            }
        }
        BoopWhiteButton(label = if (taskToEditId == null) "Add task" else "Update task") {
            if (title.isNotBlank()) {
                onSaveTask(BoopTask(taskToEditId ?: UUID.randomUUID().toString(), title.trim(), selectedMillis, false))
                title = ""
                taskToEditId = null
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks, key = { it.id }) { task ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151517))) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(task.title)
                            Text(
                                if (task.done) "Completed" else "Reminder pending",
                                color = Color(0xFFBFBFBF),
                            )
                            Text(formatDateTime(task.reminderAt), color = Color(0xFFBFBFBF))
                        }
                        IconButton(onClick = {
                            taskToEditId = task.id
                            title = task.title
                            selectedMillis = task.reminderAt
                        }) { Icon(Icons.Outlined.Edit, contentDescription = "Edit task", tint = Color.White) }
                        IconButton(onClick = { onDeleteTask(task.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete task", tint = Color.White)
                        }
                        IconButton(onClick = { onToggle(task) }) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = "Toggle task", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesScreen(
    notes: List<BoopNote>,
    onSaveNote: (BoopNote) -> Unit,
    onDeleteNote: (String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var noteToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    var attachment by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        attachment = it
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Notes + Files", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Note") },
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BoopWhiteButton(label = "Attach") { picker.launch("*/*") }
            BoopWhiteButton(label = if (noteToEditId == null) "Save note" else "Update note") {
                if (title.isNotBlank() || body.isNotBlank()) {
                    onSaveNote(
                        BoopNote(
                            id = noteToEditId ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            body = body.trim(),
                            attachmentUri = attachment?.toString(),
                        ),
                    )
                    title = ""
                    body = ""
                    attachment = null
                    noteToEditId = null
                }
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notes, key = { it.id }) { note ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151517))) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold)
                        if (note.body.isNotBlank()) Text(note.body, color = Color(0xFFCECECE))
                        Row {
                            IconButton(onClick = {
                                noteToEditId = note.id
                                title = note.title
                                body = note.body
                                attachment = note.attachmentUri?.let(Uri::parse)
                            }) { Icon(Icons.Outlined.Edit, contentDescription = "Edit note", tint = Color.White) }
                            IconButton(onClick = { onDeleteNote(note.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete note", tint = Color.White)
                            }
                        }
                        note.attachmentUri?.let { uri ->
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(model = uri, contentDescription = "Attachment", modifier = Modifier.height(120.dp).fillMaxWidth().background(Color.Black))
                            Text(uri, color = Color(0xFF9B9B9B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitScreen(
    habits: List<BoopHabit>,
    onSaveHabit: (BoopHabit) -> Unit,
    onDeleteHabit: (String) -> Unit,
    onProgress: (BoopHabit) -> Unit,
) {
    var label by rememberSaveable { mutableStateOf("") }
    var goal by rememberSaveable { mutableStateOf("30") }
    var habitToEditId by rememberSaveable { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Habits + Goals", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Habit / Goal") },
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text("Target days") },
            shape = RoundedCornerShape(999.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        BoopWhiteButton(label = if (habitToEditId == null) "Create goal" else "Update goal") {
            val target = goal.toIntOrNull() ?: 30
            if (label.isNotBlank()) {
                onSaveHabit(BoopHabit(habitToEditId ?: UUID.randomUUID().toString(), label.trim(), target, 0))
                label = ""
                habitToEditId = null
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(habits, key = { it.id }) { habit ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151517))) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(habit.title)
                            Text("${habit.progress}/${habit.goal} complete", color = Color(0xFFBFBFBF))
                        }
                        IconButton(onClick = {
                            habitToEditId = habit.id
                            label = habit.title
                            goal = habit.goal.toString()
                        }) { Icon(Icons.Outlined.Edit, contentDescription = "Edit habit", tint = Color.White) }
                        IconButton(onClick = { onDeleteHabit(habit.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete habit", tint = Color.White)
                        }
                        IconButton(onClick = { onProgress(habit) }) {
                            Icon(Icons.Outlined.Add, contentDescription = "Progress", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoopWhiteButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
        ),
    ) {
        Text(label)
    }
}

data class BoopTask(val id: String, val title: String, val reminderAt: Long, val done: Boolean)
data class BoopNote(val id: String, val title: String, val body: String, val attachmentUri: String?)
data class BoopHabit(val id: String, val title: String, val goal: Int, val progress: Int)

private object AppContextHolder {
    lateinit var context: Context
}

private object LocalStore {
    fun init(context: Context) {
        AppContextHolder.context = context.applicationContext
    }

    private fun pref() = AppContextHolder.context.getSharedPreferences("boop_store", Context.MODE_PRIVATE)

    fun save(key: String, payload: String) = pref().edit().putString(key, payload).apply()
    fun read(key: String): String = pref().getString(key, "[]").orEmpty()
}

private class BoopRepository(private val store: LocalStore) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun ensureSession(onRemoteLoaded: () -> Unit) {
        store.init(AppContextHolder.context)
        val continueWithSync = sync@{
            val uid = auth.currentUser?.uid ?: return@sync
            db.collection("boopUsers").document(uid).get().addOnSuccessListener { snap ->
                snap.getString("tasks")?.let { store.save("tasks", it) }
                snap.getString("notes")?.let { store.save("notes", it) }
                snap.getString("habits")?.let { store.save("habits", it) }
                onRemoteLoaded()
            }
        }
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnSuccessListener { continueWithSync() }
        } else {
            continueWithSync()
        }
    }

    fun readTasks(): List<BoopTask> {
        return parseArray(store.read("tasks")) { item ->
            BoopTask(item.getString("id"), item.getString("title"), item.getLong("reminderAt"), item.getBoolean("done"))
        }
    }

    fun readNotes(): List<BoopNote> {
        return parseArray(store.read("notes")) { item ->
            BoopNote(item.getString("id"), item.optString("title"), item.optString("body"), item.optString("attachmentUri").ifBlank { null })
        }
    }

    fun readHabits(): List<BoopHabit> {
        return parseArray(store.read("habits")) { item ->
            BoopHabit(item.getString("id"), item.getString("title"), item.getInt("goal"), item.getInt("progress"))
        }
    }

    fun saveTask(task: BoopTask) {
        upsertTasks(readTasks(), task)
    }

    fun deleteTask(id: String) {
        val updated = readTasks().filterNot { it.id == id }
        upsertTasks(updated, null)
    }

    fun saveNote(note: BoopNote) {
        val updated = readNotes().toMutableList().apply {
            removeAll { it.id == note.id }
            add(0, note)
        }
        val arr = JSONArray()
        updated.forEach {
            arr.put(JSONObject().put("id", it.id).put("title", it.title).put("body", it.body).put("attachmentUri", it.attachmentUri ?: ""))
        }
        store.save("notes", arr.toString())
        sync("notes", arr.toString())
    }

    fun deleteNote(id: String) {
        val updated = readNotes().filterNot { it.id == id }
        val arr = JSONArray()
        updated.forEach {
            arr.put(JSONObject().put("id", it.id).put("title", it.title).put("body", it.body).put("attachmentUri", it.attachmentUri ?: ""))
        }
        store.save("notes", arr.toString())
        sync("notes", arr.toString())
    }

    fun saveHabit(habit: BoopHabit) {
        val updated = readHabits().toMutableList().apply {
            removeAll { it.id == habit.id }
            add(0, habit)
        }
        val arr = JSONArray()
        updated.forEach { arr.put(JSONObject().put("id", it.id).put("title", it.title).put("goal", it.goal).put("progress", it.progress)) }
        store.save("habits", arr.toString())
        sync("habits", arr.toString())
    }

    fun deleteHabit(id: String) {
        val updated = readHabits().filterNot { it.id == id }
        val arr = JSONArray()
        updated.forEach { arr.put(JSONObject().put("id", it.id).put("title", it.title).put("goal", it.goal).put("progress", it.progress)) }
        store.save("habits", arr.toString())
        sync("habits", arr.toString())
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
            arr.put(JSONObject().put("id", it.id).put("title", it.title).put("reminderAt", it.reminderAt).put("done", it.done))
        }
        store.save("tasks", arr.toString())
        sync("tasks", arr.toString())
    }

    private fun sync(key: String, value: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("boopUsers").document(uid).set(mapOf(key to value), com.google.firebase.firestore.SetOptions.merge())
    }

    private fun <T> parseArray(json: String, mapper: (JSONObject) -> T): List<T> {
        val array = JSONArray(json)
        val result = mutableListOf<T>()
        for (i in 0 until array.length()) result.add(mapper(array.getJSONObject(i)))
        return result
    }
}

private fun formatDateTime(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(timeInMillis)
}

object ReminderScheduler {
    fun schedule(context: Context, task: BoopTask) {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("title", task.title)
            putExtra("id", task.id.hashCode())
        }
        val pending = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.reminderAt, pending)
    }
}

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Task due"
        val id = intent.getIntExtra("id", 1)
        ReminderNotifier.show(context, id, title)
    }
}

object ReminderNotifier {
    private const val CHANNEL = "boop_reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "Boop Reminders", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        LocalStore.init(context)
    }

    fun show(context: Context, id: Int, title: String) {
        val notification = androidx.core.app.NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Boop reminder")
            .setContentText(title)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .build()
        androidx.core.app.NotificationManagerCompat.from(context).notify(id, notification)
    }
}
