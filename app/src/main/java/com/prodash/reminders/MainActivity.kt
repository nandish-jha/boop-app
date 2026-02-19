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
import android.text.TextUtils
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import android.widget.TextView
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

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
        /** Stable key for `rememberSaveable` when creating a new task (id is null). */
        val sessionKey: String,
        val title: String,
        val reminderAt: Long,
        val done: Boolean,
    ) : ItemSheet()

    data class NoteSheet(
        val id: String?,
        /** Stable key for `rememberSaveable` when creating a new note (id is null). */
        val sessionKey: String,
        val title: String,
        val body: String,
        val attachmentUri: String?,
    ) : ItemSheet()

    data class HabitSheet(
        val id: String?,
        val title: String,
        val goal: Int,
        val progress: Int,
        val dayKeys: String,
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
    var habitCheckInOpen by remember { mutableStateOf(false) }
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
            sessionKey = task?.id ?: UUID.randomUUID().toString(),
            title = task?.title.orEmpty(),
            reminderAt = task?.reminderAt ?: (System.currentTimeMillis() + 30 * 60_000),
            done = task?.done ?: false,
        )
        speedDialExpanded = false
    }

    fun openNoteSheet(note: BoopNote? = null) {
        itemSheet = ItemSheet.NoteSheet(
            id = note?.id,
            sessionKey = note?.id ?: UUID.randomUUID().toString(),
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
            dayKeys = habit?.dayKeys.orEmpty(),
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
        val pagerScrollPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
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
                        pagerScrollPosition = pagerScrollPosition,
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
                            onEditNote = { openNoteSheet(it) },
                            onEditHabit = { openHabitSheet(it) },
                            onDashboardSaveHabit = { habit ->
                                repository.saveHabit(habit)
                                refresh()
                            },
                            onOpenHabitCheckIn = {
                                itemSheet = null
                                habitCheckInOpen = true
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
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                ModalBottomSheet(
                    onDismissRequest = { itemSheet = null },
                    sheetState = sheetState,
                    containerColor = darkSurface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF8E8E90)) },
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.92f)
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 28.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        when (sheet) {
                            is ItemSheet.TaskSheet -> TaskEditorSheet(
                                initial = sheet,
                                onDismiss = { itemSheet = null },
                                onDelete = sheet.id?.let { id ->
                                    {
                                        repository.deleteTask(id)
                                        refresh()
                                        scope.launch {
                                            delay(48)
                                            itemSheet = null
                                        }
                                    }
                                },
                                onSave = { task ->
                                    repository.saveTask(task)
                                    ReminderScheduler.schedule(AppContextHolder.context, task)
                                    refresh()
                                    scope.launch {
                                        delay(48)
                                        itemSheet = null
                                    }
                                },
                            )
                            is ItemSheet.NoteSheet -> NoteEditorSheet(
                                initial = sheet,
                                onDismiss = { itemSheet = null },
                                onDelete = sheet.id?.let { id ->
                                    {
                                        repository.deleteNote(id)
                                        refresh()
                                        scope.launch {
                                            delay(48)
                                            itemSheet = null
                                        }
                                    }
                                },
                                onSave = { note ->
                                    repository.saveNote(note)
                                    refresh()
                                    scope.launch {
                                        delay(48)
                                        itemSheet = null
                                    }
                                },
                            )
                            is ItemSheet.HabitSheet -> HabitEditorSheet(
                                initial = sheet,
                                onDismiss = { itemSheet = null },
                                onDelete = sheet.id?.let { id ->
                                    {
                                        repository.deleteHabit(id)
                                        refresh()
                                        scope.launch {
                                            delay(48)
                                            itemSheet = null
                                        }
                                    }
                                },
                                onSave = { habit ->
                                    repository.saveHabit(habit)
                                    refresh()
                                    scope.launch {
                                        delay(48)
                                        itemSheet = null
                                    }
                                },
                            )
                        }
                    }
                }
            }
            if (habitCheckInOpen) {
                val habitSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                ModalBottomSheet(
                    onDismissRequest = { habitCheckInOpen = false },
                    sheetState = habitSheetState,
                    containerColor = darkSurface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF8E8E90)) },
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ) {
                    HabitTodayCheckInSheet(
                        habits = habits,
                        onPersist = { habit ->
                            repository.saveHabit(habit)
                            refresh()
                        },
                        onDismiss = { habitCheckInOpen = false },
                    )
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
    onEditNote: (BoopNote) -> Unit,
    onEditHabit: (BoopHabit) -> Unit,
    onDashboardSaveHabit: (BoopHabit) -> Unit,
    onOpenHabitCheckIn: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        when (page) {
            0 -> DashboardScreen(
                tasks = tasks,
                notes = notes,
                habits = habits,
                onPersistHabit = onDashboardSaveHabit,
                onOpenTask = onEditTask,
                onOpenNote = onEditNote,
                onOpenHabit = onEditHabit,
                onOpenHabitCheckIn = onOpenHabitCheckIn,
            )
            1 -> TaskListScreen(
                tasks = tasks,
                searchQuery = taskSearch,
                onSearchChange = onTaskSearchChange,
                onOpenTask = onEditTask,
            )
            2 -> NotesListScreen(
                notes = notes,
                searchQuery = noteSearch,
                onSearchChange = onNoteSearchChange,
                onOpenNote = onEditNote,
            )
            else -> HabitsListScreen(
                habits = habits,
                searchQuery = habitSearch,
                onSearchChange = onHabitSearchChange,
                onOpenHabit = onEditHabit,
            )
        }
    }
}

@Composable
private fun BoopBottomNavBar(
    darkSurface: Color,
    pagerScrollPosition: Float,
    onSelectTab: (Int) -> Unit,
) {
    val tabs = listOf(
        Triple(0, "Home", Icons.Outlined.Dashboard),
        Triple(1, "Tasks", Icons.Outlined.Notifications),
        Triple(2, "Notes", Icons.Outlined.EditNote),
        Triple(3, "Habits", Icons.Outlined.Flag),
    )
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .background(darkSurface)
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 8.dp),
    ) {
        val tabCount = tabs.size
        val tabWidth = maxWidth / tabCount
        val pillInset = 5.dp
        val pillWidth = tabWidth - pillInset * 2
        val coercedPage = pagerScrollPosition.coerceIn(0f, (tabCount - 1).toFloat())
        val pillOffset = tabWidth * coercedPage + pillInset
        val activeTabIndex = pagerScrollPosition.roundToInt().coerceIn(0, tabCount - 1)
        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Surface(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(pillWidth)
                    .fillMaxHeight(0.88f)
                    .align(Alignment.CenterStart),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 2.dp,
            ) {}
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { (index, label, icon) ->
                    val selected = activeTabIndex == index
                    val interaction = remember(index) { MutableInteractionSource() }
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interaction,
                                indication = null,
                            ) { onSelectTab(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
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
            .padding(bottom = 16.dp),
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

private val habitDayKeyFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

private fun todayHabitDayKey(): String = habitDayKeyFormat.format(Calendar.getInstance().time)

private fun parseHabitDayKeys(raw: String): Set<String> =
    raw.split(',').map { it.trim() }.filter { it.length == 8 }.toSet()

private fun serializeHabitDayKeys(keys: Set<String>): String =
    keys.sorted().joinToString(",")

private fun plainNoteSnippet(html: String, maxLen: Int): String {
    val plain = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
        .replace('\n', ' ')
        .trim()
    if (plain.length <= maxLen) return plain
    return plain.take(maxLen - 1).trimEnd() + "…"
}

@Composable
private fun DashboardHabitsSectionHeader(onOpenCheckIn: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onOpenCheckIn)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White),
            )
            Column {
                Text("Your habits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "Slide up to log today for every habit",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9A9A9A),
                )
            }
        }
        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = Color(0xFFBFBFBF))
    }
}

@Composable
private fun DashboardSectionLabel(title: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .width(4.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White),
        )
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun DashboardScreen(
    tasks: List<BoopTask>,
    notes: List<BoopNote>,
    habits: List<BoopHabit>,
    onPersistHabit: (BoopHabit) -> Unit,
    onOpenTask: (BoopTask) -> Unit,
    onOpenNote: (BoopNote) -> Unit,
    onOpenHabit: (BoopHabit) -> Unit,
    onOpenHabitCheckIn: () -> Unit,
) {
    val scroll = rememberScrollState()
    val upcomingTasks = remember(tasks) {
        val n = System.currentTimeMillis()
        val h = n + 86_400_000L
        tasks.filter { !it.done && it.reminderAt >= n && it.reminderAt <= h }
            .sortedBy { it.reminderAt }
    }
    val recentNotes = remember(notes) { notes.take(4) }
    val greetingSecond = run {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            h < 12 -> "Morning"
            h < 17 -> "Afternoon"
            else -> "Evening"
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                "Good",
                fontSize = 58.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Text(
                greetingSecond,
                fontSize = 58.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(4.dp))
        DashboardHabitsSectionHeader(onOpenCheckIn = onOpenHabitCheckIn)
        if (habits.isEmpty()) {
            Text("No habits yet — add one from the + menu.", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.bodyMedium)
        } else {
            habits.forEach { habit ->
                DashboardHabitRow(
                    habit = habit,
                    onPersist = onPersistHabit,
                    onOpenHabit = onOpenHabit,
                )
            }
        }
        DashboardSectionLabel("Next 24 hours")
        if (upcomingTasks.isEmpty()) {
            Text("Nothing scheduled in the next day.", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                upcomingTasks.forEach { task ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1D)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTask(task) },
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(task.title, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(formatTaskReminderLine(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color(0xFF8E8E90))
                        }
                    }
                }
            }
        }
        DashboardSectionLabel("Fresh notes")
        if (recentNotes.isEmpty()) {
            Text("No notes yet.", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.bodyMedium)
        } else {
            recentNotes.chunked(2).forEach { rowNotes ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowNotes.forEach { note ->
                        DashboardNoteTile(
                            note = note,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenNote(note) },
                        )
                    }
                    if (rowNotes.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DashboardHabitRow(
    habit: BoopHabit,
    onPersist: (BoopHabit) -> Unit,
    onOpenHabit: (BoopHabit) -> Unit,
) {
    val todayKey = todayHabitDayKey()
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    habit.title,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenHabit(habit) },
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${habit.progress}/${habit.goal}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF9A9A9A),
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (i in 0 until 7) {
                    val offset = i - 6
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, offset) }
                    val key = habitDayKeyFormat.format(cal.time)
                    val done = key in parseHabitDayKeys(habit.dayKeys)
                    val isToday = key == todayKey
                    val label = SimpleDateFormat("EEE", Locale.US).format(cal.time)
                    val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
                    val interaction = remember(habit.id, i) { MutableInteractionSource() }
                    val cellHeight = if (isToday) 52.dp else 48.dp
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(cellHeight)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (done) Color(0xFF1B5E20) else Color(0xFF222224))
                            .then(
                                if (isToday) {
                                    Modifier.border(2.dp, Color.White, RoundedCornerShape(10.dp))
                                } else {
                                    Modifier
                                },
                            )
                            .clickable(
                                enabled = isToday,
                                interactionSource = interaction,
                                indication = null,
                            ) {
                                val next = parseHabitDayKeys(habit.dayKeys).toMutableSet()
                                if (todayKey in next) next.remove(todayKey) else next.add(todayKey)
                                onPersist(habit.copy(dayKeys = serializeHabitDayKeys(next)))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label, color = Color(0xFFBFBFBF), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            Text(dayNum, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Text(
                "Tap today’s box to log ✓ — tap title for details.",
                color = Color(0xFF6E6E70),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun DashboardNoteTile(note: BoopNote, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val snippet = remember(note.body) { plainNoteSnippet(note.body, 72) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1D)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .heightIn(min = 88.dp, max = 120.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                note.title.ifBlank { "Untitled" },
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                snippet.ifBlank { " " },
                color = Color(0xFFBFBFBF),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
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

private fun noteEditApplySpan(editText: EditText?, span: Any) {
    val et = editText ?: return
    val text = et.text as? Editable ?: return
    val len = text.length
    var s = minOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    var e = maxOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    if (e <= s) {
        if (len == 0) {
            text.append(" ")
            s = 0
            e = 1
        } else {
            e = (s + 1).coerceAtMost(len)
        }
    }
    when (span) {
        is StyleSpan -> text.getSpans(s, e, StyleSpan::class.java).forEach { text.removeSpan(it) }
        is ForegroundColorSpan -> text.getSpans(s, e, ForegroundColorSpan::class.java).forEach { text.removeSpan(it) }
        is AbsoluteSizeSpan -> text.getSpans(s, e, AbsoluteSizeSpan::class.java).forEach { text.removeSpan(it) }
    }
    text.setSpan(span, s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}

private fun noteEditSpToPx(sp: Float, context: Context): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()

@Composable
private fun NoteRichTextToolbar(editText: EditText?, context: Context) {
    val scroll = rememberScrollState()
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconButton(onClick = { noteEditApplySpan(editText, StyleSpan(Typeface.BOLD)) }) {
            Icon(Icons.Outlined.FormatBold, contentDescription = "Bold", tint = Color.White)
        }
        IconButton(onClick = { noteEditApplySpan(editText, StyleSpan(Typeface.ITALIC)) }) {
            Icon(Icons.Outlined.FormatItalic, contentDescription = "Italic", tint = Color.White)
        }
        TextButton(onClick = { noteEditApplySpan(editText, AbsoluteSizeSpan(noteEditSpToPx(22f, context), true)) }) {
            Text("H1", color = Color.White)
        }
        TextButton(onClick = { noteEditApplySpan(editText, AbsoluteSizeSpan(noteEditSpToPx(18f, context), true)) }) {
            Text("H2", color = Color.White)
        }
        TextButton(onClick = { noteEditApplySpan(editText, AbsoluteSizeSpan(noteEditSpToPx(15f, context), true)) }) {
            Text("H3", color = Color.White)
        }
        Row(
            Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF2A2A2E))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(
                0xFFEA4335.toInt(),
                0xFF4285F4.toInt(),
                0xFF34A853.toInt(),
                0xFFFBBD04.toInt(),
            ).forEach { argb ->
                val interaction = remember(argb) { MutableInteractionSource() }
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(argb))
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                        ) { noteEditApplySpan(editText, ForegroundColorSpan(argb)) },
                )
            }
        }
    }
}

@Composable
private fun BoopNoteHtmlSnippet(html: String, maxLines: Int = 8) {
    val payload = html.ifBlank { "&nbsp;" }
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            TextView(ctx).apply {
                setTextColor(android.graphics.Color.parseColor("#CECECE"))
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                textSize = 14f
                this.maxLines = maxLines
                ellipsize = TextUtils.TruncateAt.END
            }
        },
        update = { tv ->
            tv.maxLines = maxLines
            tv.text = HtmlCompat.fromHtml(payload, HtmlCompat.FROM_HTML_MODE_COMPACT)
        },
    )
}

private fun copyAttachmentToInternalFile(context: Context, source: Uri, baseName: String): String? {
    return try {
        val cr = context.contentResolver
        val mime = cr.getType(source).orEmpty()
        val ext = when {
            mime.contains("png", ignoreCase = true) -> "png"
            mime.contains("jpeg", ignoreCase = true) || mime.contains("jpg", ignoreCase = true) -> "jpg"
            mime.contains("webp", ignoreCase = true) -> "webp"
            mime.contains("gif", ignoreCase = true) -> "gif"
            else -> "dat"
        }
        val dir = File(context.filesDir, "note_attachments").apply { mkdirs() }
        val dest = File(dir, "$baseName.$ext")
        cr.openInputStream(source)?.use { input ->
            dest.outputStream().use { out -> input.copyTo(out) }
        } ?: return null
        dest.absolutePath
    } catch (_: Throwable) {
        null
    }
}

private fun storedAttachmentForCoil(stored: String?): Any? {
    if (stored.isNullOrBlank()) return null
    return when {
        stored.startsWith("content:") -> Uri.parse(stored)
        stored.startsWith("file:") -> Uri.parse(stored)
        else -> {
            val f = File(stored)
            if (f.isFile && f.exists()) f else null
        }
    }
}

@Composable
private fun TaskListScreen(
    tasks: List<BoopTask>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onOpenTask: (BoopTask) -> Unit,
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
        Text("Tasks.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTask(task) },
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(
                            if (task.done) "Completed" else "Scheduled",
                            color = Color(0xFFBFBFBF),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(formatTaskReminderLine(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodyMedium)
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
    onOpenNote: (BoopNote) -> Unit,
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
        Text("Notes.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNote(note) },
                ) {
                    val hasImage = !note.attachmentUri.isNullOrBlank()
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold, color = Color.White)
                        if (note.body.isNotBlank()) {
                            BoopNoteHtmlSnippet(note.body)
                        }
                        if (hasImage) {
                            val ctx = LocalContext.current
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0A0A0B)),
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(ctx)
                                        .data(storedAttachmentForCoil(note.attachmentUri))
                                        .crossfade(false)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
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
    onOpenHabit: (BoopHabit) -> Unit,
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
        Text("Habits.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenHabit(habit) },
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(habit.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(
                            "${habit.progress} of ${habit.goal} days",
                            color = Color(0xFFBFBFBF),
                            style = MaterialTheme.typography.bodyMedium,
                        )
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
        is24Hour = true,
    )
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(visible) {
        if (visible) step = 0
    }
    fun combinedMillis(): Long {
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
        return out.timeInMillis
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 480.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E22),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                Text(
                    if (step == 0) "Pick a date" else "Pick a time",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Spacer(Modifier.height(8.dp))
                if (step == 0) {
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
                } else {
                    TimePicker(
                        state = timeState,
                        layoutType = TimePickerLayoutType.Horizontal,
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
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFBFBFBF)) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (step == 1) {
                            TextButton(onClick = { step = 0 }) { Text("Back", color = Color(0xFFBFBFBF)) }
                        }
                        TextButton(
                            onClick = {
                                if (step == 0) {
                                    step = 1
                                } else {
                                    onConfirm(combinedMillis())
                                }
                            },
                        ) {
                            Text(if (step == 0) "Next" else "Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitTodayCheckInSheet(
    habits: List<BoopHabit>,
    onPersist: (BoopHabit) -> Unit,
    onDismiss: () -> Unit,
) {
    val todayKey = remember { todayHabitDayKey() }
    val scroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                BoopSheetHeaderTitle("Today's habits")
                Spacer(Modifier.height(6.dp))
                Text(
                    "Mark each habit done for today, or leave the switch off.",
                    color = Color(0xFF9A9A9A),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
            }
        }
        Spacer(Modifier.height(16.dp))
        if (habits.isEmpty()) {
            Text("No habits yet — add one from the + menu.", color = Color(0xFF9A9A9A))
        } else {
            habits.forEach { habit ->
                val doneToday = todayKey in parseHabitDayKeys(habit.dayKeys)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1D)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                habit.title,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "${habit.progress} / ${habit.goal} days",
                                color = Color(0xFF8E8E90),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        Switch(
                            checked = doneToday,
                            onCheckedChange = { checked ->
                                val next = parseHabitDayKeys(habit.dayKeys).toMutableSet()
                                if (checked) next.add(todayKey) else next.remove(todayKey)
                                onPersist(habit.copy(dayKeys = serializeHabitDayKeys(next)))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2E7D32),
                                uncheckedThumbColor = Color(0xFFBFBFBF),
                                uncheckedTrackColor = Color(0xFF3A3A3E),
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun BoopSheetHeaderTitle(text: String) {
    Text(
        text,
        fontSize = 42.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TaskEditorSheet(
    initial: ItemSheet.TaskSheet,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (BoopTask) -> Unit,
) {
    val sheetKey = initial.sessionKey
    var title by rememberSaveable(sheetKey) { mutableStateOf(initial.title) }
    var reminderAt by remember(sheetKey, initial.reminderAt) { mutableLongStateOf(initial.reminderAt) }
    var done by remember(sheetKey) { mutableStateOf(initial.done) }
    var showReminderPicker by remember(sheetKey) { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            BoopSheetHeaderTitle(if (initial.id == null) "New task" else "Edit task")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A8A))
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
            }
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
                Text(formatTaskReminderLine(reminderAt), color = Color.White, style = MaterialTheme.typography.bodyLarge)
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
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Mark complete", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = done, onCheckedChange = { done = it })
    }
    Spacer(Modifier.height(20.dp))
    BoopWhiteButton("Save") {
        if (title.isNotBlank()) {
            onSave(
                BoopTask(
                    id = initial.id ?: UUID.randomUUID().toString(),
                    title = title.trim(),
                    reminderAt = reminderAt,
                    done = done,
                ),
            )
        }
    }
}

@Composable
private fun NoteEditorSheet(
    initial: ItemSheet.NoteSheet,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (BoopNote) -> Unit,
) {
    val context = LocalContext.current
    val session = initial.sessionKey
    var title by rememberSaveable(session) { mutableStateOf(initial.title) }
    var attachmentStored by remember(session) { mutableStateOf(initial.attachmentUri) }
    var bodyEdit by remember(session) { mutableStateOf<EditText?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val copied = copyAttachmentToInternalFile(context, uri, UUID.randomUUID().toString())
        attachmentStored = copied ?: uri.toString()
    }
    DisposableEffect(session) {
        onDispose { bodyEdit = null }
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            BoopSheetHeaderTitle(if (initial.id == null) "New note" else "Edit note")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A8A))
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    BoopFilledTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Title") },
    )
    Spacer(Modifier.height(8.dp))
    Text("Note", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(4.dp))
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.45f),
            )
            .clip(RoundedCornerShape(14.dp)),
        factory = { ctx ->
            EditText(ctx).apply {
                setBackgroundColor(android.graphics.Color.parseColor("#1F1F22"))
                setTextColor(android.graphics.Color.WHITE)
                setHintTextColor(android.graphics.Color.parseColor("#8A8A8A"))
                hint = "Write your note…"
                minLines = 4
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                gravity = Gravity.TOP or Gravity.START
                setPadding(16, 16, 16, 16)
                setText(
                    HtmlCompat.fromHtml(
                        initial.body.ifBlank { "" },
                        HtmlCompat.FROM_HTML_MODE_COMPACT,
                    ),
                    android.widget.TextView.BufferType.EDITABLE,
                )
                bodyEdit = this
            }
        },
        update = { et ->
            bodyEdit = et
        },
    )
    NoteRichTextToolbar(bodyEdit, context)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BoopWhiteButton("Attach") { picker.launch("image/*") }
    }
    attachmentStored?.let { stored ->
        Spacer(Modifier.height(8.dp))
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(storedAttachmentForCoil(stored))
                .crossfade(false)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp)),
        )
    }
    Spacer(Modifier.height(20.dp))
    BoopWhiteButton("Save") {
        val noteId = initial.id ?: UUID.randomUUID().toString()
        val resolvedAttachment = attachmentStored?.let { att ->
            if (att.startsWith("content:")) copyAttachmentToInternalFile(context, Uri.parse(att), noteId) ?: att else att
        }
        val editable = bodyEdit?.text
        val bodyHtml = if (editable is Spanned) {
            Html.toHtml(editable, 0x1 /* Html.TO_HTML_PARCEL_OUTPUT_MODE */).trim()
        } else {
            editable?.toString()?.trim().orEmpty()
        }
        if (title.isNotBlank() || bodyHtml.isNotBlank() || !resolvedAttachment.isNullOrBlank()) {
            onSave(
                BoopNote(
                    id = noteId,
                    title = title.trim(),
                    body = bodyHtml,
                    attachmentUri = resolvedAttachment,
                ),
            )
        }
    }
}

@Composable
private fun HabitEditorSheet(
    initial: ItemSheet.HabitSheet,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (BoopHabit) -> Unit,
) {
    var label by rememberSaveable(initial.id) { mutableStateOf(initial.title) }
    var goalText by rememberSaveable(initial.id) { mutableStateOf(initial.goal.toString()) }
    var progress by remember(initial.id) { mutableIntStateOf(initial.progress) }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            BoopSheetHeaderTitle(if (initial.id == null) "New habit" else "Edit habit")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A8A))
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
            }
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
    val goalVal = goalText.toIntOrNull() ?: 30
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Progress: $progress / $goalVal", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = { progress = (progress + 1).coerceAtMost(goalVal.coerceAtLeast(1)) }) {
            Text("+1 day", color = Color.White)
        }
    }
    Spacer(Modifier.height(20.dp))
    BoopWhiteButton("Save") {
        val g = goalText.toIntOrNull() ?: 30
        if (label.isNotBlank()) {
            onSave(
                BoopHabit(
                    id = initial.id ?: UUID.randomUUID().toString(),
                    title = label.trim(),
                    goal = g,
                    progress = progress.coerceIn(0, g),
                    dayKeys = initial.dayKeys,
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
/** [dayKeys] comma-separated yyyyMMdd calendar days marked done (dashboard strip). */
data class BoopHabit(val id: String, val title: String, val goal: Int, val progress: Int, val dayKeys: String = "")

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
            BoopHabit(
                item.getString("id"),
                item.getString("title"),
                item.getInt("goal"),
                item.getInt("progress"),
                item.optString("dayKeys"),
            )
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
        updated.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("goal", it.goal)
                    .put("progress", it.progress)
                    .put("dayKeys", it.dayKeys),
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
                    .put("goal", it.goal)
                    .put("progress", it.progress)
                    .put("dayKeys", it.dayKeys),
            )
        }
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

private fun formatTaskReminderLine(timeInMillis: Long): String {
    val day = SimpleDateFormat("EEE, MMM d", Locale.US).format(timeInMillis)
    val time = SimpleDateFormat("HH:mm", Locale.US).format(timeInMillis)
    return "$day   $time"
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
        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (manager.canScheduleExactAlarms()) {
                        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.reminderAt, pending)
                    } else {
                        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.reminderAt, pending)
                    }
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.reminderAt, pending)
                }
                else -> {
                    @Suppress("DEPRECATION")
                    manager.setExact(AlarmManager.RTC_WAKEUP, task.reminderAt, pending)
                }
            }
        } catch (_: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.reminderAt, pending)
                } else {
                    @Suppress("DEPRECATION")
                    manager.set(AlarmManager.RTC_WAKEUP, task.reminderAt, pending)
                }
            } catch (_: Exception) {
            }
        }
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
