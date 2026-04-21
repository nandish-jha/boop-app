package com.prodash.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material3.Button
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
    val accent = Color(0xFFE4E4E4)
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = darkBg,
            surface = darkSurface,
            primary = accent,
            onSurface = Color(0xFFF3F3F3),
            onBackground = Color(0xFFF3F3F3),
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
                    NavigationBar(containerColor = darkSurface) {
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
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                            )
                        }
                    }
                },
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                    when (selectedTab) {
                        0 -> DashboardScreen(tasks, notes, habits)
                        1 -> TaskScreen(
                            tasks = tasks,
                            onAddTask = { title, timeMs ->
                                val newTask = BoopTask(UUID.randomUUID().toString(), title, timeMs, false)
                                repository.saveTask(newTask)
                                ReminderScheduler.schedule(AppContextHolder.context, newTask)
                                refresh()
                            },
                            onToggle = { task ->
                                repository.saveTask(task.copy(done = !task.done))
                                refresh()
                            },
                        )
                        2 -> NotesScreen(
                            notes = notes,
                            onAddNote = { title, body, attachment ->
                                repository.saveNote(
                                    BoopNote(
                                        id = UUID.randomUUID().toString(),
                                        title = title,
                                        body = body,
                                        attachmentUri = attachment?.toString(),
                                    ),
                                )
                                refresh()
                            },
                        )
                        else -> HabitScreen(
                            habits = habits,
                            onAddHabit = { label, goal ->
                                repository.saveHabit(BoopHabit(UUID.randomUUID().toString(), label, goal, 0))
                                refresh()
                            },
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
private fun DashboardScreen(tasks: List<BoopTask>, notes: List<BoopNote>, habits: List<BoopHabit>) {
    val completedTasks = tasks.count { it.done }
    val activeGoals = habits.count { it.progress < it.goal }
    val completion = if (habits.isEmpty()) 0 else habits.sumOf { (it.progress * 100) / it.goal } / habits.size
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Boop Dashboard", style = MaterialTheme.typography.titleLarge)
        DashboardCard("Tasks done", "$completedTasks / ${tasks.size}", Icons.Outlined.CheckCircle)
        DashboardCard("Notes", "${notes.size} captured", Icons.Outlined.EditNote)
        DashboardCard("Active goals", "$activeGoals running", Icons.Outlined.Flag)
        DashboardCard("Habit completion", "$completion%", Icons.Outlined.Dashboard)
        Text("Minimal. Dark. Synced.", color = Color(0xFFBFBFBF))
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
    onAddTask: (String, Long) -> Unit,
    onToggle: (BoopTask) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var minutes by rememberSaveable { mutableStateOf("30") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tasks + Reminders", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task") })
        OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text("Remind in minutes") })
        Button(onClick = {
            val mins = minutes.toLongOrNull() ?: 30L
            if (title.isNotBlank()) {
                onAddTask(title.trim(), System.currentTimeMillis() + mins * 60_000)
                title = ""
            }
        }) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Text(" Add")
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
                        }
                        IconButton(onClick = { onToggle(task) }) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = "Toggle task")
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
    onAddNote: (String, String, Uri?) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var attachment by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        attachment = it
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Notes + Files", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Note") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { picker.launch("*/*") }) {
                Icon(Icons.Outlined.Photo, contentDescription = null)
                Text(" Attach")
            }
            Button(onClick = {
                if (title.isNotBlank() || body.isNotBlank()) {
                    onAddNote(title.trim(), body.trim(), attachment)
                    title = ""
                    body = ""
                    attachment = null
                }
            }) { Text("Save note") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notes, key = { it.id }) { note ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151517))) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold)
                        if (note.body.isNotBlank()) Text(note.body, color = Color(0xFFCECECE))
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
    onAddHabit: (String, Int) -> Unit,
    onProgress: (BoopHabit) -> Unit,
) {
    var label by rememberSaveable { mutableStateOf("") }
    var goal by rememberSaveable { mutableStateOf("30") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Habits + Goals", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Habit / Goal") })
        OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text("Target days") })
        Button(onClick = {
            val target = goal.toIntOrNull() ?: 30
            if (label.isNotBlank()) {
                onAddHabit(label.trim(), target)
                label = ""
            }
        }) { Text("Create goal") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(habits, key = { it.id }) { habit ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151517))) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(habit.title)
                            Text("${habit.progress}/${habit.goal} complete", color = Color(0xFFBFBFBF))
                        }
                        IconButton(onClick = { onProgress(habit) }) {
                            Icon(Icons.Outlined.Add, contentDescription = "Progress")
                        }
                    }
                }
            }
        }
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

    private fun upsertTasks(tasks: List<BoopTask>, task: BoopTask) {
        val updated = tasks.toMutableList().apply {
            removeAll { it.id == task.id }
            add(0, task)
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
