@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
)

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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

private sealed class ItemSheet {
    data class TaskSheet(
        val id: String?,
        val title: String,
        val reminderAt: Long,
        val done: Boolean,
    ) : ItemSheet()

    data class NoteSheet(
        val id: String?,
        val title: String,
        val body: String,
        val attachmentUri: String?,
    ) : ItemSheet()

    data class HabitSheet(
        val id: String?,
        val title: String,
        val goal: Int,
        val progress: Int,
    ) : ItemSheet()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoopApp() {
    val repository = remember { BoopRepository(LocalStore) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tasks = remember { mutableStateListOf<BoopTask>() }
    val notes = remember { mutableStateListOf<BoopNote>() }
    val habits = remember { mutableStateListOf<BoopHabit>() }

    var itemSheet by remember { mutableStateOf<ItemSheet?>(null) }
    var speedDialExpanded by remember { mutableStateOf(false) }

    var taskSearch by rememberSaveable { mutableStateOf("") }
    var noteSearch by rememberSaveable { mutableStateOf("") }
    var habitSearch by rememberSaveable { mutableStateOf("") }

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

    fun openTaskSheet(task: BoopTask? = null) {
        itemSheet = ItemSheet.TaskSheet(
            id = task?.id,
            title = task?.title.orEmpty(),
            reminderAt = task?.reminderAt ?: (System.currentTimeMillis() + 30 * 60_000),
            done = task?.done ?: false,
        )
        speedDialExpanded = false
    }

    fun openNoteSheet(note: BoopNote? = null) {
        itemSheet = ItemSheet.NoteSheet(
            id = note?.id,
            title = note?.title.orEmpty(),
            body = note?.body.orEmpty(),
            attachmentUri = note?.attachmentUri,
        )
        speedDialExpanded = false
    }

    fun openHabitSheet(habit: BoopHabit? = null) {
        itemSheet = ItemSheet.HabitSheet(
            id = habit?.id,
            title = habit?.title.orEmpty(),
            goal = habit?.goal ?: 30,
            progress = habit?.progress ?: 0,
        )
        speedDialExpanded = false
    }

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
        val scope = rememberCoroutineScope()
        var pullRefreshing by remember { mutableStateOf(false) }
        val pullRefreshState = rememberPullRefreshState(
            refreshing = pullRefreshing,
            onRefresh = {
                scope.launch {
                    pullRefreshing = true
                    refresh()
                    delay(280)
                    pullRefreshing = false
                }
            },
        )
        val pagerState = rememberPagerState(initialPage = selectedTab, pageCount = { 4 })
        LaunchedEffect(pagerState.isScrollInProgress, pagerState.currentPage) {
            if (!pagerState.isScrollInProgress && pagerState.currentPage != selectedTab) {
                selectedTab = pagerState.currentPage
                speedDialExpanded = false
            }
        }
        LaunchedEffect(selectedTab) {
            if (pagerState.currentPage != selectedTab) {
                pagerState.animateScrollToPage(selectedTab)
            }
        }
        Surface(Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = darkBg,
                floatingActionButton = {
                    BoopSpeedDialFab(
                        selectedTab = selectedTab,
                        expanded = speedDialExpanded,
                        onExpandedChange = { speedDialExpanded = it },
                        onOpenTask = { openTaskSheet(null) },
                        onOpenNote = { openNoteSheet(null) },
                        onOpenHabit = { openHabitSheet(null) },
                    )
                },
                floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
                bottomBar = {
                    BoopBottomNavBar(
                        darkSurface = darkSurface,
                        selectedTab = selectedTab,
                        onSelectTab = {
                            selectedTab = it
                            speedDialExpanded = false
                        },
                    )
                },
            ) { padding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .pullRefresh(pullRefreshState),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        BoopPagerPage(
                            page = page,
                            tasks = tasks,
                            notes = notes,
                            habits = habits,
                            taskSearch = taskSearch,
                            onTaskSearchChange = { taskSearch = it },
                            noteSearch = noteSearch,
                            onNoteSearchChange = { noteSearch = it },
                            habitSearch = habitSearch,
                            onHabitSearchChange = { habitSearch = it },
                            onEditTask = { openTaskSheet(it) },
                            onDeleteTask = { id -> repository.deleteTask(id); refresh() },
                            onToggleTask = { task ->
                                repository.saveTask(task.copy(done = !task.done))
                                refresh()
                            },
                            onEditNote = { openNoteSheet(it) },
                            onDeleteNote = { id -> repository.deleteNote(id); refresh() },
                            onEditHabit = { openHabitSheet(it) },
                            onDeleteHabit = { id -> repository.deleteHabit(id); refresh() },
                            onProgressHabit = { habit ->
                                repository.saveHabit(habit.copy(progress = (habit.progress + 1).coerceAtMost(habit.goal)))
                                refresh()
                            },
                        )
                    }
                    PullRefreshIndicator(
                        refreshing = pullRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        contentColor = Color.White,
                        backgroundColor = Color(0xFF2A2A2C),
                    )
                }
            }

            itemSheet?.let { sheet ->
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { itemSheet = null },
                    sheetState = sheetState,
                    containerColor = darkSurface,
                    dragHandle = null,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.92f)
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 28.dp),
                    ) {
                        when (sheet) {
                            is ItemSheet.TaskSheet -> TaskEditorSheet(
                                initial = sheet,
                                onDismiss = { itemSheet = null },
                                onSave = { task ->
                                    repository.saveTask(task)
                                    ReminderScheduler.schedule(AppContextHolder.context, task)
                                    refresh()
                                    itemSheet = null
                                },
                            )
                            is ItemSheet.NoteSheet -> NoteEditorSheet(
                                initial = sheet,
                                onDismiss = { itemSheet = null },
                                onSave = { note ->
                                    repository.saveNote(note)
                                    refresh()
                                    itemSheet = null
                                },
                            )
                            is ItemSheet.HabitSheet -> HabitEditorSheet(
                                initial = sheet,
                                onDismiss = { itemSheet = null },
                                onSave = { habit ->
                                    repository.saveHabit(habit)
                                    refresh()
                                    itemSheet = null
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoopPagerPage(
    page: Int,
    tasks: List<BoopTask>,
    notes: List<BoopNote>,
    habits: List<BoopHabit>,
    taskSearch: String,
    onTaskSearchChange: (String) -> Unit,
    noteSearch: String,
    onNoteSearchChange: (String) -> Unit,
    habitSearch: String,
    onHabitSearchChange: (String) -> Unit,
    onEditTask: (BoopTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onToggleTask: (BoopTask) -> Unit,
    onEditNote: (BoopNote) -> Unit,
    onDeleteNote: (String) -> Unit,
    onEditHabit: (BoopHabit) -> Unit,
    onDeleteHabit: (String) -> Unit,
    onProgressHabit: (BoopHabit) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        when (page) {
            0 -> DashboardScreen(tasks = tasks, notes = notes, habits = habits)
            1 -> TaskListScreen(
                tasks = tasks,
                searchQuery = taskSearch,
                onSearchChange = onTaskSearchChange,
                onEdit = onEditTask,
                onDelete = onDeleteTask,
                onToggle = onToggleTask,
            )
            2 -> NotesListScreen(
                notes = notes,
                searchQuery = noteSearch,
                onSearchChange = onNoteSearchChange,
                onEdit = onEditNote,
                onDelete = onDeleteNote,
            )
            else -> HabitsListScreen(
                habits = habits,
                searchQuery = habitSearch,
                onSearchChange = onHabitSearchChange,
                onEdit = onEditHabit,
                onDelete = onDeleteHabit,
                onProgress = onProgressHabit,
            )
        }
    }
}

@Composable
private fun BoopBottomNavBar(
    darkSurface: Color,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
) {
    val tabs = listOf(
        Triple(0, "Home", Icons.Outlined.Dashboard),
        Triple(1, "Tasks", Icons.Outlined.Notifications),
        Triple(2, "Notes", Icons.Outlined.EditNote),
        Triple(3, "Habits", Icons.Outlined.Flag),
    )
    Row(
        Modifier
            .fillMaxWidth()
            .background(darkSurface)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { (index, label, icon) ->
            val selected = selectedTab == index
            Surface(
                onClick = { onSelectTab(index) },
                shape = RoundedCornerShape(8.dp),
                color = if (selected) Color.White else Color.Transparent,
            ) {
                Column(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = if (selected) Color.Black else Color(0xFFBFBFBF),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Color.Black else Color(0xFFBFBFBF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoopSpeedDialFab(
    selectedTab: Int,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onOpenTask: () -> Unit,
    onOpenNote: () -> Unit,
    onOpenHabit: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { onOpenTask(); onExpandedChange(false) },
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ) { Icon(Icons.Outlined.Notifications, contentDescription = "Add task") }
                SmallFloatingActionButton(
                    onClick = { onOpenNote(); onExpandedChange(false) },
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ) { Icon(Icons.Outlined.EditNote, contentDescription = "Add note") }
                SmallFloatingActionButton(
                    onClick = { onOpenHabit(); onExpandedChange(false) },
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ) { Icon(Icons.Outlined.Flag, contentDescription = "Add habit") }
            }
        }
        FloatingActionButton(
            onClick = {
                when (selectedTab) {
                    0 -> onExpandedChange(!expanded)
                    1 -> onOpenTask()
                    2 -> onOpenNote()
                    else -> onOpenHabit()
                }
            },
            modifier = Modifier.pointerInput(selectedTab) {
                if (selectedTab != 0) {
                    detectTapGestures(onLongPress = { onExpandedChange(true) })
                }
            },
            containerColor = Color.White,
            contentColor = Color.Black,
            elevation = FloatingActionButtonDefaults.elevation(),
        ) {
            Crossfade(targetState = expanded && selectedTab == 0, label = "fab_icon") { showClose ->
                Icon(
                    imageVector = if (showClose) Icons.Outlined.Close else Icons.Outlined.Add,
                    contentDescription = if (showClose) "Close" else "Add",
                )
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    tasks: List<BoopTask>,
    notes: List<BoopNote>,
    habits: List<BoopHabit>,
) {
    val completedTasks = tasks.count { it.done }
    val activeGoals = habits.count { it.progress < it.goal }
    val completion = if (habits.isEmpty()) 0 else habits.sumOf { (it.progress * 100) / it.goal } / habits.size
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Boop Dashboard", style = MaterialTheme.typography.titleLarge)
        DashboardCard("Tasks done", "$completedTasks / ${tasks.size}", Icons.Outlined.CheckCircle)
        DashboardCard("Notes", "${notes.size} captured", Icons.Outlined.EditNote)
        DashboardCard("Active goals", "$activeGoals running", Icons.Outlined.Flag)
        DashboardCard("Habit completion", "$completion%", Icons.Outlined.Dashboard)
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
private fun BoopFilledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.45f),
            ),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color(0xFF262628),
            unfocusedContainerColor = Color(0xFF1F1F22),
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFFBFBFBF),
            unfocusedLabelColor = Color(0xFF9A9A9A),
        ),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        minLines = minLines,
    )
}

@Composable
private fun BoopSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    BoopFilledTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Search") },
        placeholder = { Text(placeholder, color = Color(0xFF8A8A8A)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFFBFBFBF)) },
    )
}

@Composable
private fun TaskListScreen(
    tasks: List<BoopTask>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onEdit: (BoopTask) -> Unit,
    onDelete: (String) -> Unit,
    onToggle: (BoopTask) -> Unit,
) {
    val q = searchQuery.trim().lowercase(Locale.getDefault())
    val filtered = remember(tasks, q) {
        if (q.isEmpty()) tasks
        else tasks.filter { it.title.lowercase(Locale.getDefault()).contains(q) }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Tasks", style = MaterialTheme.typography.titleLarge)
        BoopSearchField(searchQuery, onSearchChange, "Search tasks")
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(filtered, key = { it.id }) { task ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(task.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (task.done) "Completed" else "Reminder",
                                color = Color(0xFFBFBFBF),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(formatDateTime(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onEdit(task) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = { onDelete(task.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                        IconButton(onClick = { onToggle(task) }) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = "Done", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesListScreen(
    notes: List<BoopNote>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onEdit: (BoopNote) -> Unit,
    onDelete: (String) -> Unit,
) {
    val q = searchQuery.trim().lowercase(Locale.getDefault())
    val filtered = remember(notes, q) {
        if (q.isEmpty()) notes
        else {
            notes.filter {
                it.title.lowercase(Locale.getDefault()).contains(q) ||
                    it.body.lowercase(Locale.getDefault()).contains(q)
            }
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Notes", style = MaterialTheme.typography.titleLarge)
        BoopSearchField(searchQuery, onSearchChange, "Search notes")
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(filtered, key = { it.id }) { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val hasImage = !note.attachmentUri.isNullOrBlank()
                    Column(Modifier.padding(14.dp)) {
                        if (!hasImage) {
                            Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold)
                            if (note.body.isNotBlank()) {
                                Text(note.body, color = Color(0xFFCECECE), style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            AsyncImage(
                                model = Uri.parse(note.attachmentUri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onEdit(note) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color.White)
                            }
                            IconButton(onClick = { onDelete(note.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitsListScreen(
    habits: List<BoopHabit>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onEdit: (BoopHabit) -> Unit,
    onDelete: (String) -> Unit,
    onProgress: (BoopHabit) -> Unit,
) {
    val q = searchQuery.trim().lowercase(Locale.getDefault())
    val filtered = remember(habits, q) {
        if (q.isEmpty()) habits
        else habits.filter { it.title.lowercase(Locale.getDefault()).contains(q) }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Habits", style = MaterialTheme.typography.titleLarge)
        BoopSearchField(searchQuery, onSearchChange, "Search habits")
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(filtered, key = { it.id }) { habit ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(habit.title, fontWeight = FontWeight.SemiBold)
                            Text("${habit.progress}/${habit.goal} complete", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onEdit(habit) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color.White)
                        }
                        IconButton(onClick = { onDelete(habit.id) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.White)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderPickerDialog(
    visible: Boolean,
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    if (!visible) return
    val zone = Calendar.getInstance().timeZone
    val initialCal = Calendar.getInstance(zone).apply { timeInMillis = initialMillis }
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        initialDisplayMode = DisplayMode.Picker,
    )
    val timeState = rememberTimePickerState(
        initialHour = initialCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCal.get(Calendar.MINUTE),
        is24Hour = false,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E22),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text("Pick a date & time", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(Modifier.height(8.dp))
                DatePicker(
                    state = dateState,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color(0xFF1E1E22),
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Color(0xFFBFBFBF),
                        subheadContentColor = Color(0xFFBFBFBF),
                        navigationContentColor = Color.White,
                        yearContentColor = Color.White,
                        disabledYearContentColor = Color(0xFF666666),
                        currentYearContentColor = Color.White,
                        selectedYearContentColor = Color.Black,
                        selectedYearContainerColor = Color.White,
                        dayContentColor = Color.White,
                        selectedDayContentColor = Color.Black,
                        selectedDayContainerColor = Color.White,
                        todayContentColor = Color.White,
                        todayDateBorderColor = Color.White,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                TimePicker(
                    state = timeState,
                    layoutType = TimePickerLayoutType.Vertical,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFF2A2A2E),
                        selectorColor = Color.White,
                        periodSelectorBorderColor = Color(0xFF5C5C5E),
                        periodSelectorSelectedContainerColor = Color.White,
                        periodSelectorSelectedContentColor = Color.Black,
                        periodSelectorUnselectedContainerColor = Color(0xFF2A2A2E),
                        periodSelectorUnselectedContentColor = Color.White,
                        timeSelectorSelectedContainerColor = Color.White,
                        timeSelectorSelectedContentColor = Color.Black,
                        timeSelectorUnselectedContainerColor = Color(0xFF2A2A2E),
                        timeSelectorUnselectedContentColor = Color.White,
                    ),
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFBFBFBF)) }
                    TextButton(
                        onClick = {
                            val dayMillis = dateState.selectedDateMillis ?: initialMillis
                            val dayCal = Calendar.getInstance(zone).apply { timeInMillis = dayMillis }
                            val out = Calendar.getInstance(zone)
                            out.set(Calendar.YEAR, dayCal.get(Calendar.YEAR))
                            out.set(Calendar.MONTH, dayCal.get(Calendar.MONTH))
                            out.set(Calendar.DAY_OF_MONTH, dayCal.get(Calendar.DAY_OF_MONTH))
                            out.set(Calendar.HOUR_OF_DAY, timeState.hour)
                            out.set(Calendar.MINUTE, timeState.minute)
                            out.set(Calendar.SECOND, 0)
                            out.set(Calendar.MILLISECOND, 0)
                            onConfirm(out.timeInMillis)
                        },
                    ) { Text("Save", color = Color.White) }
                }
            }
        }
    }
}

@Composable
private fun TaskEditorSheet(
    initial: ItemSheet.TaskSheet,
    onDismiss: () -> Unit,
    onSave: (BoopTask) -> Unit,
) {
    val sheetKey = initial.id.orEmpty()
    var title by rememberSaveable(sheetKey) { mutableStateOf(initial.title) }
    var reminderAt by remember(sheetKey, initial.reminderAt) { mutableLongStateOf(initial.reminderAt) }
    var showReminderPicker by remember(sheetKey) { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(if (initial.id == null) "New task" else "Edit task", style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onDismiss) {
            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
        }
    }
    Spacer(Modifier.height(12.dp))
    BoopFilledTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Task") },
    )
    Spacer(Modifier.height(12.dp))
    Surface(
        onClick = { showReminderPicker = true },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF242426),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Set a reminder", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.labelMedium)
                Text(formatDateTime(reminderAt), color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color.White)
        }
    }
    ReminderPickerDialog(
        visible = showReminderPicker,
        initialMillis = reminderAt,
        onDismiss = { showReminderPicker = false },
        onConfirm = {
            reminderAt = it
            showReminderPicker = false
        },
    )
    Spacer(Modifier.height(20.dp))
    BoopWhiteButton("Save") {
        if (title.isNotBlank()) {
            onSave(
                BoopTask(
                    id = initial.id ?: UUID.randomUUID().toString(),
                    title = title.trim(),
                    reminderAt = reminderAt,
                    done = initial.done,
                ),
            )
        }
    }
}

@Composable
private fun NoteEditorSheet(
    initial: ItemSheet.NoteSheet,
    onDismiss: () -> Unit,
    onSave: (BoopNote) -> Unit,
) {
    var title by rememberSaveable(initial.id) { mutableStateOf(initial.title) }
    var body by rememberSaveable(initial.id) { mutableStateOf(initial.body) }
    var attachmentUri by remember(initial.id) { mutableStateOf(initial.attachmentUri) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        attachmentUri = uri?.toString()
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(if (initial.id == null) "New note" else "Edit note", style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onDismiss) {
            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
        }
    }
    Spacer(Modifier.height(12.dp))
    BoopFilledTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Title") },
    )
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(
        value = body,
        onValueChange = { body = it },
        label = { Text("Note") },
        singleLine = false,
        minLines = 3,
    )
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BoopWhiteButton("Attach") { picker.launch("*/*") }
    }
    attachmentUri?.let { uri ->
        Spacer(Modifier.height(8.dp))
        AsyncImage(
            model = Uri.parse(uri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
    Spacer(Modifier.height(20.dp))
    BoopWhiteButton("Save") {
        if (title.isNotBlank() || body.isNotBlank() || !attachmentUri.isNullOrBlank()) {
            onSave(
                BoopNote(
                    id = initial.id ?: UUID.randomUUID().toString(),
                    title = title.trim(),
                    body = body.trim(),
                    attachmentUri = attachmentUri,
                ),
            )
        }
    }
}

@Composable
private fun HabitEditorSheet(
    initial: ItemSheet.HabitSheet,
    onDismiss: () -> Unit,
    onSave: (BoopHabit) -> Unit,
) {
    var label by rememberSaveable(initial.id) { mutableStateOf(initial.title) }
    var goalText by rememberSaveable(initial.id) { mutableStateOf(initial.goal.toString()) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(if (initial.id == null) "New habit" else "Edit habit", style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onDismiss) {
            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
        }
    }
    Spacer(Modifier.height(12.dp))
    BoopFilledTextField(
        value = label,
        onValueChange = { label = it },
        label = { Text("Habit / goal") },
    )
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(
        value = goalText,
        onValueChange = { goalText = it },
        label = { Text("Target days") },
    )
    Spacer(Modifier.height(20.dp))
    BoopWhiteButton("Save") {
        val g = goalText.toIntOrNull() ?: 30
        if (label.isNotBlank()) {
            onSave(
                BoopHabit(
                    id = initial.id ?: UUID.randomUUID().toString(),
                    title = label.trim(),
                    goal = g,
                    progress = initial.progress,
                ),
            )
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
