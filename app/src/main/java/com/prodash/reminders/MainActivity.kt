@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
)

package com.prodash.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.text.TextUtils
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.text.style.URLSpan
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.io.File
import java.net.URL
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
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
        val repeatEveryDays: Int,
    ) : ItemSheet()

    data class NoteSheet(
        val id: String?,
        /** Stable key for `rememberSaveable` when creating a new note (id is null). */
        val sessionKey: String,
        val title: String,
        val body: String,
        val attachmentUri: String?,
        val audioUri: String?,
        val tagsCsv: String,
    ) : ItemSheet()

    data class HabitSheet(
        val id: String?,
        val title: String,
        val goal: Int,
        val progress: Int,
        val dayKeys: String,
        val quantityMode: Boolean,
        val quantityUnit: String,
        val quantityDailyTarget: Int,
        val quantityDayValues: String,
    ) : ItemSheet()

    data class EventSheet(
        val eventId: Long?,
        val calendarId: Long?,
        val sessionKey: String,
        val title: String,
        val description: String,
        val location: String,
        val allDay: Boolean,
        val startAt: Long,
        val endAt: Long,
        val notifyWeeksBefore: Int,
        val notifyDaysBefore: Int,
        val notifyHoursBefore: Int,
        val repeatEveryDays: Int,
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
    var calendarSyncRequest by rememberSaveable { mutableIntStateOf(0) }

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
            repeatEveryDays = task?.repeatEveryDays ?: 0,
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
            audioUri = note?.audioUri,
            tagsCsv = note?.tagsCsv.orEmpty(),
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
            quantityMode = habit?.quantityMode ?: false,
            quantityUnit = habit?.quantityUnit.orEmpty(),
            quantityDailyTarget = habit?.quantityDailyTarget ?: 30,
            quantityDayValues = habit?.quantityDayValues.orEmpty(),
        )
        speedDialExpanded = false
    }

    val context = LocalContext.current

    fun openEventSheet(startAt: Long = System.currentTimeMillis(), existing: CalendarEventDetail? = null) {
        val start = existing?.startAt ?: startAt
        val endAt = existing?.endAt ?: (start + 60 * 60_000L)
        itemSheet = ItemSheet.EventSheet(
            eventId = existing?.eventId,
            calendarId = existing?.calendarId,
            sessionKey = UUID.randomUUID().toString(),
            title = existing?.title.orEmpty(),
            description = existing?.description.orEmpty(),
            location = existing?.location.orEmpty(),
            allDay = existing?.allDay ?: false,
            startAt = start,
            endAt = endAt,
            notifyWeeksBefore = 0,
            notifyDaysBefore = 0,
            notifyHoursBefore = 0,
            repeatEveryDays = existing?.repeatEveryDays ?: 0,
        )
        speedDialExpanded = false
    }

    fun openEventSheetById(eventId: Long) {
        val detail = readCalendarEventDetail(context, eventId)
        openEventSheet(existing = detail)
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
        val pagerState = rememberPagerState(initialPage = selectedTab, pageCount = { 5 })
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
                        onSyncCalendar = {
                            selectedTab = 2
                            calendarSyncRequest++
                            speedDialExpanded = false
                        },
                        onOpenTask = { openTaskSheet(null) },
                        onOpenEvent = { openEventSheet() },
                        onOpenExternalCalendar = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.formula1.com/")))
                            } catch (_: Throwable) {
                            }
                            speedDialExpanded = false
                        },
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
                            calendarSyncRequest = calendarSyncRequest,
                            onPersistHabit = { habit ->
                                repository.saveHabit(habit)
                                refresh()
                            },
                            onSelectTab = { selectedTab = it },
                            onEditTask = { openTaskSheet(it) },
                            onEditEvent = { openEventSheetById(it) },
                            onEditNote = { openNoteSheet(it) },
                            onEditHabit = { openHabitSheet(it) },
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
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                                .fillMaxHeight(0.9f)
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 28.dp)
                            .imePadding()
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
                                        itemSheet = null
                                    }
                                },
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
                                onDelete = sheet.id?.let { id ->
                                    {
                                        repository.deleteNote(id)
                                        refresh()
                                        itemSheet = null
                                    }
                                },
                                onSave = { note ->
                                    repository.saveNote(note)
                                    refresh()
                                    itemSheet = null
                                },
                            )
                            is ItemSheet.HabitSheet -> HabitEditorSheet(
                                initial = sheet,
                                onDismiss = { itemSheet = null },
                                onDelete = sheet.id?.let { id ->
                                    {
                                        repository.deleteHabit(id)
                                        refresh()
                                        itemSheet = null
                                    }
                                },
                                onSave = { habit ->
                                    repository.saveHabit(habit)
                                    refresh()
                                    itemSheet = null
                                },
                            )
                            is ItemSheet.EventSheet -> EventEditorSheet(
                                initial = sheet,
                                onDismiss = { itemSheet = null },
                                onSave = { ok ->
                                    if (ok) {
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
                        onEditHabit = { habit ->
                            habitCheckInOpen = false
                            openHabitSheet(habit)
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
    calendarSyncRequest: Int,
    onPersistHabit: (BoopHabit) -> Unit,
    onSelectTab: (Int) -> Unit,
    onEditTask: (BoopTask) -> Unit,
    onEditEvent: (Long) -> Unit,
    onEditNote: (BoopNote) -> Unit,
    onEditHabit: (BoopHabit) -> Unit,
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
                onOpenTask = onEditTask,
                onOpenNote = onEditNote,
                onOpenHabit = onEditHabit,
                onOpenHabitCheckIn = onOpenHabitCheckIn,
                onSearchPickTask = { onSelectTab(1); onEditTask(it) },
                onSearchPickNote = { onSelectTab(3); onEditNote(it) },
                onSearchPickHabit = { onSelectTab(4); onEditHabit(it) },
            )
            1 -> TaskListScreen(
                tasks = tasks,
                onOpenTask = onEditTask,
            )
            2 -> CalendarScreen(
                tasks = tasks,
                syncRequest = calendarSyncRequest,
                onOpenTask = onEditTask,
                onOpenEvent = onEditEvent,
            )
            3 -> NotesListScreen(
                notes = notes,
                onOpenNote = onEditNote,
            )
            else -> HabitsListScreen(
                habits = habits,
                onPersistHabit = onPersistHabit,
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
        Triple(2, "Calendar", Icons.Outlined.CalendarMonth),
        Triple(3, "Notes", Icons.Outlined.EditNote),
        Triple(4, "Habits", Icons.Outlined.Flag),
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
                shape = RoundedCornerShape(16.dp),
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
    onSyncCalendar: () -> Unit,
    onOpenTask: () -> Unit,
    onOpenEvent: () -> Unit,
    onOpenExternalCalendar: () -> Unit,
    onOpenNote: () -> Unit,
    onOpenHabit: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 0.dp),
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
                if (selectedTab == 2) {
                    SmallFloatingActionButton(
                        onClick = { onOpenEvent(); onExpandedChange(false) },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ) { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Add event") }
                    SmallFloatingActionButton(
                        onClick = { onSyncCalendar() },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ) { Icon(Icons.Outlined.Sync, contentDescription = "Sync calendar") }
                    SmallFloatingActionButton(
                        onClick = { onOpenExternalCalendar(); onExpandedChange(false) },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ) { Icon(Icons.Outlined.Link, contentDescription = "Add external calendar") }
                } else {
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
        }
        FloatingActionButton(
            onClick = {
                when (selectedTab) {
                    0 -> onExpandedChange(!expanded)
                    1 -> onOpenTask()
                    2 -> onExpandedChange(!expanded)
                    3 -> onOpenNote()
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
            Crossfade(targetState = expanded, label = "fab_icon") { showClose ->
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

private fun parseHabitDayValues(raw: String): Map<String, Int> {
    if (raw.isBlank()) return emptyMap()
    return raw.split(',')
        .mapNotNull { part ->
            val p = part.split(':')
            if (p.size != 2) return@mapNotNull null
            val key = p[0].trim()
            val value = p[1].trim().toIntOrNull() ?: return@mapNotNull null
            if (key.length != 8) return@mapNotNull null
            key to value.coerceAtLeast(0)
        }.toMap()
}

private fun serializeHabitDayValues(values: Map<String, Int>): String =
    values.entries
        .filter { it.key.length == 8 && it.value >= 0 }
        .sortedBy { it.key }
        .joinToString(",") { "${it.key}:${it.value}" }

private fun plainNoteSnippet(html: String, maxLen: Int): String {
    val plain = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
        .replace('\n', ' ')
        .trim()
    if (plain.length <= maxLen) return plain
    return plain.take(maxLen - 1).trimEnd() + "…"
}

private fun extractLinksFromBody(htmlOrText: String): List<String> {
    val plain = HtmlCompat.fromHtml(htmlOrText, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    val regex = Regex("""https?://[^\s<>()]+""")
    return regex.findAll(plain).map { it.value.trim() }.distinct().toList()
}

private fun parseNoteAttachments(raw: String?): List<String> {
    val value = raw.orEmpty().trim()
    if (value.isBlank()) return emptyList()
    if (value.startsWith("[")) {
        return try {
            val arr = JSONArray(value)
            (0 until arr.length()).mapNotNull { idx -> arr.optString(idx).takeIf { it.isNotBlank() } }
        } catch (_: Throwable) {
            listOf(value)
        }
    }
    return listOf(value)
}

private fun serializeNoteAttachments(values: List<String>): String? {
    val clean = values.map { it.trim() }.filter { it.isNotBlank() }.distinct().take(25)
    if (clean.isEmpty()) return null
    if (clean.size == 1) return clean.first()
    val arr = JSONArray()
    clean.forEach { arr.put(it) }
    return arr.toString()
}

private suspend fun fetchWebTitle(url: String): String? = withContext(Dispatchers.IO) {
    try {
        val conn = URL(url).openConnection().apply {
            connectTimeout = 2500
            readTimeout = 2500
        }
        conn.getInputStream().bufferedReader().use { r ->
            val chunk = r.readText().take(16_000)
            Regex("""<title>(.*?)</title>""", RegexOption.IGNORE_CASE)
                .find(chunk)
                ?.groupValues
                ?.getOrNull(1)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
    } catch (_: Throwable) {
        null
    }
}

@Composable
private fun NoteLinkPreviewCard(link: String) {
    val context = LocalContext.current
    var title by remember(link) { mutableStateOf<String?>(null) }
    val host = remember(link) { runCatching { Uri.parse(link).host.orEmpty() }.getOrDefault("") }
    LaunchedEffect(link) {
        title = fetchWebTitle(link)
    }
    val interaction = remember(link) { MutableInteractionSource() }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF202024),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null) {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                } catch (_: Throwable) {
                }
            },
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title ?: "Loading preview...", color = Color(0xFFE2E2E2), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (host.isNotBlank()) {
                Text(host, color = Color(0xFFBFBFBF), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(link, color = Color(0xFF9EC2FF), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun parseNoteTags(raw: String): List<String> =
    raw.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase(Locale.getDefault()) }

private fun normalizeNoteTags(raw: String): String = parseNoteTags(raw).joinToString(", ")

private fun applyRoundedDialog(dialog: android.app.Dialog, backgroundColor: Int = android.graphics.Color.parseColor("#1F1F22")) {
    val shape = GradientDrawable().apply {
        setColor(backgroundColor)
        cornerRadii = floatArrayOf(
            36f, 36f,
            36f, 36f,
            36f, 36f,
            36f, 36f,
        )
    }
    dialog.window?.setBackgroundDrawable(shape)
}

@Composable
private fun DashboardHabitsSectionHeader(onOpenWeekView: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
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
                    "Open week view",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9A9A9A),
                )
            }
        }
        IconButton(onClick = onOpenWeekView) {
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Habits week view", tint = Color(0xFFBFBFBF))
        }
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
    onOpenTask: (BoopTask) -> Unit,
    onOpenNote: (BoopNote) -> Unit,
    onOpenHabit: (BoopHabit) -> Unit,
    onOpenHabitCheckIn: () -> Unit,
    onSearchPickTask: (BoopTask) -> Unit,
    onSearchPickNote: (BoopNote) -> Unit,
    onSearchPickHabit: (BoopHabit) -> Unit,
) {
    val scroll = rememberScrollState()
    val searchScroll = rememberScrollState()
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            delay(48)
            searchFocus.requestFocus()
        }
    }
    val now = System.currentTimeMillis()
    val horizon = now + 86_400_000L
    val upcomingTasks = tasks
        .filter { !it.done && it.reminderAt in now..horizon }
        .sortedBy { it.reminderAt }
    val recentNotes = notes
        .sortedByDescending { it.updatedAtMillis }
        .take(4)
    val greetingSecond = run {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            h < 12 -> "Morning"
            h < 17 -> "Afternoon"
            else -> "Evening"
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(top = 12.dp, bottom = 24.dp),
    ) {
        AnimatedVisibility(
            visible = !searchExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Good", fontSize = 58.sp, lineHeight = 60.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text(greetingSecond, fontSize = 58.sp, lineHeight = 60.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    FloatingActionButton(
                        onClick = { searchExpanded = true },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                    ) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                }
                Spacer(Modifier.height(4.dp))
                DashboardHabitsSectionHeader(onOpenWeekView = onOpenHabitCheckIn)
                if (habits.isEmpty()) {
                    Text("No habits yet — add one from the + menu.", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.bodyMedium)
                } else {
                    habits.forEach { habit ->
                        DashboardHabitCompactCard(habit = habit, onOpenHabit = onOpenHabit)
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
                                modifier = Modifier.fillMaxWidth().clickable { onOpenTask(task) },
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
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
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowNotes.forEach { note ->
                                DashboardNoteTile(note = note, modifier = Modifier.weight(1f), onClick = { onOpenNote(note) })
                            }
                            if (rowNotes.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = searchExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(searchScroll),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocus)
                            .shadow(3.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = { Text("Search tasks, notes, habits…", color = Color(0xFF6E6E70)) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFFBFBFBF)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1A1A1D),
                            unfocusedContainerColor = Color(0xFF1A1A1D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    )
                    IconButton(
                        onClick = {
                            searchExpanded = false
                            searchQuery = ""
                        },
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close search", tint = Color.White)
                    }
                }
                Text(
                    "Text inside note images is not searched.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6E6E70),
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                )
                GlobalSearchResultsInline(
                    query = searchQuery,
                    tasks = tasks,
                    notes = notes,
                    habits = habits,
                    onPickTask = {
                        searchExpanded = false
                        searchQuery = ""
                        onSearchPickTask(it)
                    },
                    onPickNote = {
                        searchExpanded = false
                        searchQuery = ""
                        onSearchPickNote(it)
                    },
                    onPickHabit = {
                        searchExpanded = false
                        searchQuery = ""
                        onSearchPickHabit(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun DashboardHabitCompactCard(
    habit: BoopHabit,
    onOpenHabit: (BoopHabit) -> Unit,
) {
    val todayKey = todayHabitDayKey()
    val doneToday = if (habit.quantityMode) {
        val todayAmount = parseHabitDayValues(habit.quantityDayValues)[todayKey] ?: 0
        todayAmount >= habit.quantityDailyTarget.coerceAtLeast(1)
    } else {
        todayKey in parseHabitDayKeys(habit.dayKeys)
    }
    val doneCount = parseHabitDayKeys(habit.dayKeys).size
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1D)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenHabit(habit) },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
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
                    if (habit.quantityMode) {
                        val todayAmount = parseHabitDayValues(habit.quantityDayValues)[todayKey] ?: 0
                        val unit = habit.quantityUnit.ifBlank { "units" }
                        "$todayAmount/${habit.quantityDailyTarget} $unit · " + if (doneToday) "Logged today" else "Not logged today"
                    } else {
                        "$doneCount/${habit.goal} days · " + if (doneToday) "Logged today" else "Not logged today"
                    },
                    color = Color(0xFFBFBFBF),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.Outlined.Flag, contentDescription = null, tint = Color(0xFF8E8E90))
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

private fun noteEditInsertBulletLine(editText: EditText?) {
    val et = editText ?: return
    val text = et.text as? Editable ?: return
    val len = text.length
    val s = minOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    val e = maxOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    if (e > s) {
        val selected = text.substring(s, e)
        val replaced = selected.split('\n').joinToString("\n") { line ->
            if (line.trim().isBlank()) line else "• ${line.trimStart()}"
        }
        text.replace(s, e, replaced)
        et.setSelection((s + replaced.length).coerceAtMost(text.length))
    } else {
        val pos = s
        val prefix = if (pos == 0 || text.getOrNull(pos - 1) == '\n') "• " else "\n• "
        text.insert(pos, prefix)
        et.setSelection((pos + prefix.length).coerceAtMost(text.length))
    }
}

private fun noteEditInsertNumberedLine(editText: EditText?) {
    val et = editText ?: return
    val text = et.text as? Editable ?: return
    val len = text.length
    val s = minOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    val e = maxOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    if (e > s) {
        val selected = text.substring(s, e)
        var idx = 1
        val replaced = selected.split('\n').joinToString("\n") { line ->
            if (line.trim().isBlank()) line else "${idx++}. ${line.trimStart()}"
        }
        text.replace(s, e, replaced)
        et.setSelection((s + replaced.length).coerceAtMost(text.length))
    } else {
        val pos = s
        val before = text.substring(0, pos)
        val lineMatches = Regex("""(?m)^(\d+)\.\s""").findAll(before).toList()
        val nextNum = (lineMatches.lastOrNull()?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0) + 1
        val prefix = if (pos == 0 || text.getOrNull(pos - 1) == '\n') "$nextNum. " else "\n$nextNum. "
        text.insert(pos, prefix)
        et.setSelection((pos + prefix.length).coerceAtMost(text.length))
    }
}

private fun noteEditInsertLink(editText: EditText?, url: String) {
    val et = editText ?: return
    val text = et.text as? Editable ?: return
    val cleaned = url.trim()
    if (cleaned.isBlank()) return
    val normalized = if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) cleaned else "https://$cleaned"
    val len = text.length
    var s = minOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    var e = maxOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    if (e <= s) {
        val label = normalized
        text.insert(s, label)
        e = s + label.length
    }
    text.getSpans(s, e, URLSpan::class.java).forEach { text.removeSpan(it) }
    text.setSpan(URLSpan(normalized), s, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
        IconButton(onClick = { noteEditApplySpan(editText, UnderlineSpan()) }) {
            Text("U", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        IconButton(onClick = { noteEditInsertBulletLine(editText) }) {
            Text("•", color = Color.White, fontWeight = FontWeight.Bold)
        }
        IconButton(onClick = { noteEditInsertNumberedLine(editText) }) {
            Text("1.", color = Color.White, fontWeight = FontWeight.Bold)
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

private fun extractTextFromAttachment(context: Context, stored: String?): String {
    if (stored.isNullOrBlank()) return ""
    return try {
        val primary = parseNoteAttachments(stored).firstOrNull() ?: return ""
        val image = when {
            primary.startsWith("content:") -> InputImage.fromFilePath(context, Uri.parse(primary))
            primary.startsWith("file:") -> InputImage.fromFilePath(context, Uri.parse(primary))
            else -> {
                val file = File(primary)
                if (!file.exists()) return ""
                InputImage.fromFilePath(context, Uri.fromFile(file))
            }
        }
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = Tasks.await(recognizer.process(image))
        recognizer.close()
        result.text.trim()
    } catch (_: Throwable) {
        ""
    }
}

private fun createNoteAudioFile(context: Context, baseName: String): File {
    val dir = File(context.filesDir, "note_audio").apply { mkdirs() }
    return File(dir, "$baseName.m4a")
}

private data class CalendarEventUi(
    val id: Long,
    val title: String,
    val beginMillis: Long,
    val endMillis: Long,
    val calendarDisplayName: String,
    val allDay: Boolean,
)

private data class DeviceCalendarChoice(
    val id: Long,
    val displayName: String,
)

private data class CalendarEventDetail(
    val eventId: Long,
    val calendarId: Long,
    val title: String,
    val description: String,
    val location: String,
    val allDay: Boolean,
    val startAt: Long,
    val endAt: Long,
    val repeatEveryDays: Int,
)

private fun readGoogleCalendarIds(context: Context): Set<Long> {
    val googleIds = mutableSetOf<Long>()
    val fallbackVisibleIds = mutableSetOf<Long>()
    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.ACCOUNT_TYPE,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT,
    )
    val selection = "${CalendarContract.Calendars.VISIBLE} = 1"
    context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        selection,
        null,
        null,
    )?.use { c ->
        val idIx = c.getColumnIndex(CalendarContract.Calendars._ID)
        val typeIx = c.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE)
        val nameIx = c.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
        val ownerIx = c.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)
        while (c.moveToNext()) {
            if (idIx < 0) continue
            val id = c.getLong(idIx)
            fallbackVisibleIds.add(id)
            val type = if (typeIx >= 0) c.getString(typeIx).orEmpty() else ""
            val accountName = if (nameIx >= 0) c.getString(nameIx).orEmpty() else ""
            val ownerAccount = if (ownerIx >= 0) c.getString(ownerIx).orEmpty() else ""
            val isGoogleCalendar = type.equals("com.google", ignoreCase = true) ||
                accountName.contains("@gmail.com", ignoreCase = true) ||
                accountName.contains("@googlemail.com", ignoreCase = true) ||
                ownerAccount.contains("@gmail.com", ignoreCase = true) ||
                ownerAccount.contains("@googlemail.com", ignoreCase = true)
            if (isGoogleCalendar) {
                googleIds.add(id)
            }
        }
    }
    return fallbackVisibleIds
}

private fun readVisibleCalendars(context: Context): List<DeviceCalendarChoice> {
    val out = mutableListOf<DeviceCalendarChoice>()
    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
    )
    val selection = "${CalendarContract.Calendars.VISIBLE} = 1"
    context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        selection,
        null,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " ASC",
    )?.use { c ->
        val idIx = c.getColumnIndex(CalendarContract.Calendars._ID)
        val nameIx = c.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
        while (c.moveToNext()) {
            if (idIx < 0) continue
            out.add(
                DeviceCalendarChoice(
                    id = c.getLong(idIx),
                    displayName = if (nameIx >= 0) c.getString(nameIx).orEmpty().ifBlank { "Calendar" } else "Calendar",
                ),
            )
        }
    }
    return out
}

private fun readCalendarEventDetail(context: Context, eventId: Long): CalendarEventDetail? {
    val projection = arrayOf(
        CalendarContract.Events._ID,
        CalendarContract.Events.CALENDAR_ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DESCRIPTION,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.ALL_DAY,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.RRULE,
    )
    val sel = "${CalendarContract.Events._ID} = ?"
    val args = arrayOf(eventId.toString())
    context.contentResolver.query(
        CalendarContract.Events.CONTENT_URI,
        projection,
        sel,
        args,
        null,
    )?.use { c ->
        if (!c.moveToFirst()) return null
        return CalendarEventDetail(
            eventId = c.getLong(c.getColumnIndex(CalendarContract.Events._ID)),
            calendarId = c.getLong(c.getColumnIndex(CalendarContract.Events.CALENDAR_ID)),
            title = c.getString(c.getColumnIndex(CalendarContract.Events.TITLE)).orEmpty(),
            description = c.getString(c.getColumnIndex(CalendarContract.Events.DESCRIPTION)).orEmpty(),
            location = c.getString(c.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)).orEmpty(),
            allDay = c.getInt(c.getColumnIndex(CalendarContract.Events.ALL_DAY)) == 1,
            startAt = c.getLong(c.getColumnIndex(CalendarContract.Events.DTSTART)),
            endAt = c.getLong(c.getColumnIndex(CalendarContract.Events.DTEND)),
            repeatEveryDays = parseRepeatDaysFromRRule(c.getString(c.getColumnIndex(CalendarContract.Events.RRULE)).orEmpty()),
        )
    }
    return null
}

private fun readGoogleCalendarEventsInRange(
    context: Context,
    startMillis: Long,
    endMillis: Long,
): List<CalendarEventUi> {
    val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
        .appendPath(startMillis.toString())
        .appendPath((endMillis - 1L).toString())
        .build()
    val projection = arrayOf(
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END,
        CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
        CalendarContract.Instances.CALENDAR_ID,
        CalendarContract.Instances.ALL_DAY,
    )
    val out = mutableListOf<CalendarEventUi>()
    context.contentResolver.query(uri, projection, null, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { c ->
        val idIx = c.getColumnIndex(CalendarContract.Instances.EVENT_ID)
        val titleIx = c.getColumnIndex(CalendarContract.Instances.TITLE)
        val beginIx = c.getColumnIndex(CalendarContract.Instances.BEGIN)
        val endIx = c.getColumnIndex(CalendarContract.Instances.END)
        val calIx = c.getColumnIndex(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)
        val allDayIx = c.getColumnIndex(CalendarContract.Instances.ALL_DAY)
        while (c.moveToNext()) {
            val rawBegin = if (beginIx >= 0) c.getLong(beginIx) else startMillis
            val rawEnd = if (endIx >= 0) c.getLong(endIx) else endMillis
            val isAllDay = allDayIx >= 0 && c.getInt(allDayIx) == 1
            val normalized = if (isAllDay) {
                val utc = TimeZone.getTimeZone("UTC")
                val local = TimeZone.getDefault()
                val beginUtc = Calendar.getInstance(utc).apply { timeInMillis = rawBegin }
                val endUtc = Calendar.getInstance(utc).apply { timeInMillis = rawEnd }
                val beginLocal = Calendar.getInstance(local).apply {
                    set(beginUtc.get(Calendar.YEAR), beginUtc.get(Calendar.MONTH), beginUtc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endLocal = Calendar.getInstance(local).apply {
                    set(endUtc.get(Calendar.YEAR), endUtc.get(Calendar.MONTH), endUtc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                beginLocal.timeInMillis to endLocal.timeInMillis
            } else {
                rawBegin to rawEnd
            }
            out.add(
                CalendarEventUi(
                    id = if (idIx >= 0) c.getLong(idIx) else 0L,
                    title = if (titleIx >= 0) c.getString(titleIx).orEmpty().ifBlank { "Untitled event" } else "Untitled event",
                    beginMillis = normalized.first,
                    endMillis = normalized.second,
                    calendarDisplayName = if (calIx >= 0) c.getString(calIx).orEmpty() else "",
                    allDay = isAllDay,
                ),
            )
        }
    }
    // Fallback pass: pull directly from Events for subscribed feeds that may not always expand in Instances.
    val eventProjection = arrayOf(
        CalendarContract.Events._ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.CALENDAR_ID,
        CalendarContract.Events.ALL_DAY,
    )
    val eventSel = "(${CalendarContract.Events.DTSTART} < ?) AND (${CalendarContract.Events.DTEND} > ?)"
    val eventArgs = arrayOf(endMillis.toString(), startMillis.toString())
    context.contentResolver.query(
        CalendarContract.Events.CONTENT_URI,
        eventProjection,
        eventSel,
        eventArgs,
        "${CalendarContract.Events.DTSTART} ASC",
    )?.use { c ->
        val idIx = c.getColumnIndex(CalendarContract.Events._ID)
        val titleIx = c.getColumnIndex(CalendarContract.Events.TITLE)
        val beginIx = c.getColumnIndex(CalendarContract.Events.DTSTART)
        val endIx = c.getColumnIndex(CalendarContract.Events.DTEND)
        val allDayIx = c.getColumnIndex(CalendarContract.Events.ALL_DAY)
        while (c.moveToNext()) {
            val id = if (idIx >= 0) c.getLong(idIx) else 0L
            if (out.any { it.id == id && it.beginMillis == (if (beginIx >= 0) c.getLong(beginIx) else 0L) }) continue
            out.add(
                CalendarEventUi(
                    id = id,
                    title = if (titleIx >= 0) c.getString(titleIx).orEmpty().ifBlank { "Untitled event" } else "Untitled event",
                    beginMillis = if (beginIx >= 0) c.getLong(beginIx) else startMillis,
                    endMillis = if (endIx >= 0) c.getLong(endIx) else endMillis,
                    calendarDisplayName = "",
                    allDay = allDayIx >= 0 && c.getInt(allDayIx) == 1,
                ),
            )
        }
    }
    return out.sortedBy { it.beginMillis }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TaskListScreen(
    tasks: List<BoopTask>,
    onOpenTask: (BoopTask) -> Unit,
) {
    val activeTasks = remember(tasks) { tasks.filter { !it.done } }
    val archivedTasks = remember(tasks) { tasks.filter { it.done }.sortedByDescending { it.reminderAt } }
    var showArchive by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text("Tasks", fontSize = 58.sp, lineHeight = 60.sp, fontWeight = FontWeight.Black, color = Color.White)
            FloatingActionButton(
                onClick = { showArchive = true },
                containerColor = Color.White,
                contentColor = Color.Black,
            ) {
                Icon(Icons.Outlined.Archive, contentDescription = "Archived tasks")
            }
        }
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (activeTasks.isEmpty()) {
                item {
                    Text("No active tasks. Completed ones are in archive.", color = Color(0xFF8E8E90), style = MaterialTheme.typography.bodyMedium)
                }
            }
            items(activeTasks, key = { it.id }) { task ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTask(task) },
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                        if (task.done) {
                            Text(
                                "Completed",
                                color = Color(0xFFBFBFBF),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(formatTaskReminderLine(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
    if (showArchive) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showArchive = false },
            sheetState = sheetState,
            containerColor = Color(0xFF111113),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF8E8E90)) },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Archived tasks", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleLarge)
                if (archivedTasks.isEmpty()) {
                    Text("No archived tasks yet.", color = Color(0xFF8E8E90), style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(archivedTasks, key = { it.id }) { task ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1D)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showArchive = false
                                        onOpenTask(task)
                                    },
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(task.title, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text(formatTaskReminderLine(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarScreen(
    tasks: List<BoopTask>,
    syncRequest: Int,
    onOpenTask: (BoopTask) -> Unit,
    onOpenEvent: (Long) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val basePage = 1200
    val monthPager = rememberPagerState(initialPage = basePage, pageCount = { 2400 })
    val now = Calendar.getInstance()
    var selectedMillis by rememberSaveable { mutableLongStateOf(now.timeInMillis) }
    val monthCal = remember(monthPager.currentPage) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthPager.currentPage - basePage)
        }
    }
    LaunchedEffect(monthPager.currentPage) {
        val selected = Calendar.getInstance().apply { timeInMillis = selectedMillis }
        if (selected.get(Calendar.YEAR) != monthCal.get(Calendar.YEAR) || selected.get(Calendar.MONTH) != monthCal.get(Calendar.MONTH)) {
            val aligned = (monthCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedMillis = aligned.timeInMillis
        }
    }
    val selectedDay = remember(selectedMillis) {
        Calendar.getInstance().apply {
            timeInMillis = selectedMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val todayNoon = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val nextDay = remember(selectedMillis) { (selectedDay.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) } }
    val dayTasks = remember(tasks, selectedDay.timeInMillis, nextDay.timeInMillis) {
        tasks.filter { !it.done && it.reminderAt >= selectedDay.timeInMillis && it.reminderAt < nextDay.timeInMillis }
            .sortedBy { it.reminderAt }
    }
    val headerLabel = remember(selectedMillis) { SimpleDateFormat("EEE, MMM dd", Locale.US).format(selectedMillis) }
    val syncRangeStart = remember { Calendar.getInstance().apply { add(Calendar.YEAR, -10); set(Calendar.MONTH, 0); set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis }
    val syncRangeEnd = remember { Calendar.getInstance().apply { add(Calendar.YEAR, 10); set(Calendar.MONTH, 11); set(Calendar.DAY_OF_MONTH, 31); set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis }
    var lastSyncStatus by rememberSaveable { mutableStateOf("Tap sync to load Google events into Calendar.") }
    var googleEventsCache by remember { mutableStateOf(emptyList<CalendarEventUi>()) }
    var calendarGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val refreshGoogleEvents: suspend (Boolean) -> Int = { updateStatus ->
        val events = withContext(Dispatchers.IO) {
            readGoogleCalendarEventsInRange(context, syncRangeStart, syncRangeEnd)
        }
        googleEventsCache = events
        if (updateStatus) {
            lastSyncStatus = "Google Calendar synced: ${events.size} events loaded."
        }
        events.size
    }
    val calendarPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        calendarGranted = granted
        if (granted) {
            scope.launch {
                lastSyncStatus = "Syncing Google Calendar..."
                val loaded = refreshGoogleEvents(true)
                Toast.makeText(context, "Google Calendar synced: $loaded events", Toast.LENGTH_SHORT).show()
            }
        } else {
            lastSyncStatus = "Calendar permission denied. Sync cannot run."
        }
    }
    LaunchedEffect(calendarGranted) {
        if (calendarGranted && googleEventsCache.isEmpty()) {
            lastSyncStatus = "Syncing Google Calendar..."
            refreshGoogleEvents(true)
        }
    }
    LaunchedEffect(calendarGranted) {
        if (calendarGranted) {
            while (true) {
                delay(60_000)
                refreshGoogleEvents(false)
            }
        }
    }
    LaunchedEffect(syncRequest, calendarGranted) {
        if (syncRequest <= 0) return@LaunchedEffect
        if (!calendarGranted) {
            calendarPermLauncher.launch(Manifest.permission.READ_CALENDAR)
        } else {
            lastSyncStatus = "Syncing Google Calendar..."
            refreshGoogleEvents(true)
        }
    }
    val googleEvents = remember(googleEventsCache, selectedDay.timeInMillis, nextDay.timeInMillis) {
        googleEventsCache
            .filter { it.beginMillis < nextDay.timeInMillis && it.endMillis > selectedDay.timeInMillis }
            .sortedBy { it.beginMillis }
    }
    val allDayEvents = remember(googleEvents) {
        googleEvents.filter { it.allDay || (it.endMillis - it.beginMillis) >= 23 * 60 * 60 * 1000L }
    }
    val timedGoogleEvents = remember(googleEvents) { googleEvents - allDayEvents.toSet() }
    val timelineState = rememberLazyListState()
    var compactWeekForced by rememberSaveable { mutableStateOf(false) }
    val compactWeekMode by remember {
        derivedStateOf { compactWeekForced || timelineState.firstVisibleItemIndex > 0 || timelineState.firstVisibleItemScrollOffset > 24 }
    }
    data class TimelineEntry(
        val id: String,
        val startMillis: Long,
        val endMillis: Long,
        val title: String,
        val detail: String,
        val isTask: Boolean,
    )
    val timelineItems = remember(timedGoogleEvents, dayTasks, selectedDay.timeInMillis, nextDay.timeInMillis) {
        buildList {
            timedGoogleEvents.forEach { event ->
                add(
                    TimelineEntry(
                        id = "event_${event.id}_${event.beginMillis}",
                        startMillis = maxOf(event.beginMillis, selectedDay.timeInMillis),
                        endMillis = minOf(event.endMillis, nextDay.timeInMillis),
                        title = event.title,
                        detail = if (event.calendarDisplayName.isNotBlank()) event.calendarDisplayName else "Google Calendar",
                        isTask = false,
                    ),
                )
            }
            dayTasks.forEach { task ->
                val end = (task.reminderAt + 30 * 60 * 1000L).coerceAtMost(nextDay.timeInMillis)
                add(
                    TimelineEntry(
                        id = "task_${task.id}",
                        startMillis = task.reminderAt,
                        endMillis = maxOf(end, task.reminderAt + 5 * 60 * 1000L),
                        title = task.title,
                        detail = "Boop task",
                        isTask = true,
                    ),
                )
            }
        }
            .filter { it.endMillis > it.startMillis }
            .sortedBy { it.startMillis }
    }
    data class TimelineRenderEntry(
        val item: TimelineEntry,
        val gapMinutesBefore: Long,
    )
    val timelineRenderItems = remember(timelineItems, selectedDay.timeInMillis) {
        val out = mutableListOf<TimelineRenderEntry>()
        var prevEnd = selectedDay.timeInMillis
        timelineItems.forEach { item ->
            val gap = ((item.startMillis - prevEnd) / 60_000L).coerceAtLeast(0)
            out.add(TimelineRenderEntry(item = item, gapMinutesBefore = gap))
            prevEnd = maxOf(prevEnd, item.endMillis)
        }
        out
    }
    val weekDays = remember(selectedMillis) {
        val start = (selectedDay.clone() as Calendar).apply {
            val shift = (get(Calendar.DAY_OF_WEEK) + 5) % 7
            add(Calendar.DAY_OF_MONTH, -shift)
            set(Calendar.HOUR_OF_DAY, 12)
        }
        List(7) { idx -> (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, idx) }.timeInMillis }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                headerLabel,
                fontSize = 58.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.clickable {
                    selectedMillis = todayNoon
                    scope.launch { monthPager.animateScrollToPage(basePage) }
                },
            )
        }
        Box(Modifier.fillMaxWidth().animateContentSize()) {
            Crossfade(targetState = compactWeekMode, label = "month_week_smooth") { weekMode ->
                if (!weekMode) {
                    HorizontalPager(
                        state = monthPager,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            add(Calendar.MONTH, page - basePage)
                        }
                        val firstDayOffset = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val cells = mutableListOf<Int>().apply {
                            repeat(firstDayOffset) { add(0) }
                            addAll(1..daysInMonth)
                        }
                        while (cells.size % 7 != 0) cells.add(0)
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF151517))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                                    Text(
                                        label,
                                        modifier = Modifier.weight(1f),
                                        color = Color(0xFF8E8E90),
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                            cells.chunked(7).forEach { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    row.forEach { day ->
                                        if (day == 0) {
                                            Spacer(Modifier.weight(1f).height(34.dp))
                                        } else {
                                            val dayCal = (cal.clone() as Calendar).apply {
                                                set(Calendar.DAY_OF_MONTH, day)
                                                set(Calendar.HOUR_OF_DAY, 12)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            val isSelected = SimpleDateFormat("yyyyMMdd", Locale.US).format(dayCal.timeInMillis) ==
                                                SimpleDateFormat("yyyyMMdd", Locale.US).format(selectedMillis)
                                            val interaction = remember(page, day) { MutableInteractionSource() }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) Color.White else Color(0xFF1E1E22))
                                                    .clickable(interactionSource = interaction, indication = null) { selectedMillis = dayCal.timeInMillis },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    day.toString().padStart(2, '0'),
                                                    color = if (isSelected) Color.Black else Color.White,
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF151517))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        weekDays.forEach { dayMillis ->
                            val dayCal = Calendar.getInstance().apply { timeInMillis = dayMillis }
                            val isSelected = SimpleDateFormat("yyyyMMdd", Locale.US).format(dayMillis) ==
                                SimpleDateFormat("yyyyMMdd", Locale.US).format(selectedMillis)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color.White else Color(0xFF1E1E22))
                                    .clickable { selectedMillis = dayMillis }
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(SimpleDateFormat("E", Locale.US).format(dayMillis).take(1), color = if (isSelected) Color.Black else Color(0xFF9A9A9A), style = MaterialTheme.typography.labelSmall)
                                Text(dayCal.get(Calendar.DAY_OF_MONTH).toString(), color = if (isSelected) Color.Black else Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
        if (allDayEvents.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                allDayEvents.forEach { event ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF26262B),
                        modifier = Modifier.clickable {
                            onOpenEvent(event.id)
                        },
                    ) {
                        Text(
                            text = event.title.ifBlank { "All-day event" },
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(timelineState.firstVisibleItemIndex, timelineState.firstVisibleItemScrollOffset) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -3f) compactWeekForced = true
                        if (dragAmount > 3f && timelineState.firstVisibleItemIndex == 0 && timelineState.firstVisibleItemScrollOffset == 0) {
                            compactWeekForced = false
                        }
                    }
                },
            state = timelineState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!calendarGranted) {
                item {
                    Text("Tap sync to allow Calendar access.", color = Color(0xFF8E8E90), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (calendarGranted && timelineItems.isEmpty()) {
                item {
                    Text("No events or tasks for this day.", color = Color(0xFF8E8E90), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                val uniformBlockHeight = 110.dp
                items(timelineRenderItems, key = { it.item.id }) { render ->
                    val item = render.item
                    val isTask = item.isTask
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isTask) Color(0xFF1C2533) else Color(0xFF151517)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(uniformBlockHeight)
                            .clickable {
                                if (isTask) {
                                    val taskId = item.id.removePrefix("task_")
                                    dayTasks.firstOrNull { it.id == taskId }?.let(onOpenTask)
                                } else {
                                    val eventId = item.id.removePrefix("event_").substringBefore('_').toLongOrNull()
                                    if (eventId != null) onOpenEvent(eventId)
                                }
                            },
                    ) {
                        Box(Modifier.fillMaxSize().padding(12.dp)) {
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.US).format(item.startMillis),
                                color = Color(0xFFBFBFBF),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.TopEnd),
                            )
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.US).format(item.endMillis),
                                color = Color(0xFF8E8E90),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.BottomEnd),
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .fillMaxWidth(0.78f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(item.title, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(item.detail, color = Color(0xFFBFBFBF), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (isTask) "Boop task" else "Google Calendar", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NotesListScreen(
    notes: List<BoopNote>,
    onOpenNote: (BoopNote) -> Unit,
) {
    val activeNotes = notes.filter { !it.archived }
    val archivedNotes = notes.filter { it.archived }.sortedByDescending { it.updatedAtMillis }
    var showArchive by rememberSaveable { mutableStateOf(false) }
    var selectedTag by rememberSaveable { mutableStateOf("All") }
    val availableTags = activeNotes.flatMap { parseNoteTags(it.tagsCsv) }.distinctBy { it.lowercase(Locale.getDefault()) }
    val visibleNotes = if (selectedTag == "All") activeNotes else activeNotes.filter { n ->
        parseNoteTags(n.tagsCsv).any { it.equals(selectedTag, ignoreCase = true) }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text("Notes", fontSize = 58.sp, lineHeight = 60.sp, fontWeight = FontWeight.Black, color = Color.White)
            FloatingActionButton(
                onClick = { showArchive = true },
                containerColor = Color.White,
                contentColor = Color.Black,
            ) {
                Icon(Icons.Outlined.Archive, contentDescription = "Archived notes")
            }
        }
        if (availableTags.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (listOf("All") + availableTags).forEach { tag ->
                    val active = selectedTag.equals(tag, ignoreCase = true)
                    val interaction = remember(tag) { MutableInteractionSource() }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (active) Color.White else Color(0xFF1F1F22),
                        modifier = Modifier.clickable(interactionSource = interaction, indication = null) { selectedTag = tag },
                    ) {
                        Text(
                            text = if (tag == "All") "All" else "#$tag",
                            color = if (active) Color.Black else Color(0xFFD3D3D3),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
            }
        }
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visibleNotes, key = { it.id }) { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNote(note) },
                ) {
                    val images = parseNoteAttachments(note.attachmentUri)
                    val hasImage = images.isNotEmpty()
                    val hasAudio = !note.audioUri.isNullOrBlank()
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold, color = Color.White)
                        val tags = parseNoteTags(note.tagsCsv)
                        if (tags.isNotEmpty()) {
                            Text(
                                tags.joinToString("  ") { "#$it" },
                                color = Color(0xFF8E8E90),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (note.body.isNotBlank()) {
                            BoopNoteHtmlSnippet(note.body)
                        }
                        if (hasImage) {
                            val ctx = LocalContext.current
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                images.take(3).forEach { imageUri ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(ctx)
                                            .data(storedAttachmentForCoil(imageUri))
                                            .crossfade(false)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0A0A0B)),
                                    )
                                }
                            }
                        }
                        if (hasAudio) {
                            Text("Audio attached", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
                        }
                        val links = extractLinksFromBody(note.body)
                        if (links.isNotEmpty()) {
                            links.take(2).forEach { link ->
                                NoteLinkPreviewCard(link)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showArchive) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showArchive = false },
            sheetState = sheetState,
            containerColor = Color(0xFF111113),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF8E8E90)) },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Archived notes", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleLarge)
                if (archivedNotes.isEmpty()) {
                    Text("No archived notes yet.", color = Color(0xFF8E8E90), style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(archivedNotes, key = { it.id }) { note ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1D)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showArchive = false
                                        onOpenNote(note)
                                    },
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text(plainNoteSnippet(note.body, 80), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                                }
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
    onPersistHabit: (BoopHabit) -> Unit,
    onOpenHabit: (BoopHabit) -> Unit,
) {
    val todayKey = todayHabitDayKey()
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Habits", fontSize = 58.sp, lineHeight = 60.sp, fontWeight = FontWeight.Black, color = Color.White)
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(habits, key = { it.id }) { habit ->
                val doneCount = parseHabitDayKeys(habit.dayKeys).size
                val todayAmount = parseHabitDayValues(habit.quantityDayValues)[todayKey] ?: 0
                val progressFraction = if (habit.quantityMode) {
                    (todayAmount.toFloat() / habit.quantityDailyTarget.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                } else {
                    (habit.progress.toFloat() / habit.goal.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenHabit(habit) },
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(habit.title, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF242426),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (progressFraction >= 1f) Color(0xFF6BE28F) else Color(0xFF8E8E90)),
                                )
                                Text(
                                    if (habit.quantityMode) {
                                        val unit = habit.quantityUnit.ifBlank { "units" }
                                        "Today ${todayAmount}/${habit.quantityDailyTarget} $unit"
                                    } else {
                                        "Progress ${habit.progress}/${habit.goal} · $doneCount checks"
                                    },
                                    color = Color(0xFFD0D0D0),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = Color.White,
                            trackColor = Color(0xFF2A2A2E),
                        )
                        if (habit.quantityMode) {
                            val dayValues = parseHabitDayValues(habit.quantityDayValues)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (row in 0 until 2) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        for (col in 0 until 7) {
                                            val i = row * 7 + col
                                            val offset = i - 13
                                            val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, offset) }
                                            val key = habitDayKeyFormat.format(cal.time)
                                            val done = (dayValues[key] ?: 0) >= habit.quantityDailyTarget.coerceAtLeast(1)
                                            val isToday = key == todayKey
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (done) Color(0xFF1B5E20) else Color(0xFF222224))
                                                    .then(
                                                        if (isToday) Modifier.border(1.dp, Color.White, RoundedCornerShape(10.dp)) else Modifier,
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    SimpleDateFormat("E", Locale.US).format(cal.time).take(1),
                                                    color = Color(0xFFD0D0D0),
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val unit = habit.quantityUnit.ifBlank { "units" }
                                val quickAdd = listOf(
                                    1,
                                    maxOf(5, habit.quantityDailyTarget / 4),
                                    maxOf(10, habit.quantityDailyTarget / 2),
                                ).distinct()
                                quickAdd.forEach { delta ->
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color(0xFF242426),
                                        modifier = Modifier.clickable {
                                            val map = parseHabitDayValues(habit.quantityDayValues).toMutableMap()
                                            map[todayKey] = todayAmount + delta
                                            onPersistHabit(habit.copy(quantityDayValues = serializeHabitDayValues(map)))
                                        },
                                    ) {
                                        Text(
                                            "+$delta $unit",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(6.dp))
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = Color(0xFF1F1F22),
                                    modifier = Modifier.clickable {
                                        val map = parseHabitDayValues(habit.quantityDayValues).toMutableMap()
                                        map[todayKey] = 0
                                        onPersistHabit(habit.copy(quantityDayValues = serializeHabitDayValues(map)))
                                    },
                                ) {
                                    Text(
                                        "Reset",
                                        color = Color(0xFFBFBFBF),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (row in 0 until 2) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        for (col in 0 until 7) {
                                            val i = row * 7 + col
                                            val offset = i - 13
                                            val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, offset) }
                                            val key = habitDayKeyFormat.format(cal.time)
                                            val done = key in parseHabitDayKeys(habit.dayKeys)
                                            val isToday = key == todayKey
                                            val interaction = remember(habit.id, row, col) { MutableInteractionSource() }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (done) Color(0xFF1B5E20) else Color(0xFF222224))
                                                    .then(
                                                        if (isToday) Modifier.border(1.dp, Color.White, RoundedCornerShape(10.dp)) else Modifier,
                                                    )
                                                    .clickable(
                                                        enabled = isToday,
                                                        interactionSource = interaction,
                                                        indication = null,
                                                    ) {
                                                        val next = parseHabitDayKeys(habit.dayKeys).toMutableSet()
                                                        if (todayKey in next) next.remove(todayKey) else next.add(todayKey)
                                                        onPersistHabit(habit.copy(dayKeys = serializeHabitDayKeys(next)))
                                                    },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    SimpleDateFormat("E", Locale.US).format(cal.time).take(1),
                                                    color = Color(0xFFD0D0D0),
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderPickerDialog(
    visible: Boolean,
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    if (!visible) return
    val context = LocalContext.current
    val onDismissLatest by rememberUpdatedState(onDismiss)
    val onConfirmLatest by rememberUpdatedState(onConfirm)
    LaunchedEffect(visible, initialMillis) {
        if (!visible) return@LaunchedEffect
        val base = Calendar.getInstance().apply { timeInMillis = initialMillis }
        val dateDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, base.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, base.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val timeDialog = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        chosen.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        chosen.set(Calendar.MINUTE, minute)
                        chosen.set(Calendar.SECOND, 0)
                        chosen.set(Calendar.MILLISECOND, 0)
                        onConfirmLatest(chosen.timeInMillis)
                    },
                    base.get(Calendar.HOUR_OF_DAY),
                    base.get(Calendar.MINUTE),
                    true,
                )
                timeDialog.setOnCancelListener { onDismissLatest() }
                applyRoundedDialog(timeDialog)
                timeDialog.show()
            },
            base.get(Calendar.YEAR),
            base.get(Calendar.MONTH),
            base.get(Calendar.DAY_OF_MONTH),
        )
        dateDialog.setOnCancelListener { onDismissLatest() }
        applyRoundedDialog(dateDialog)
        dateDialog.show()
    }
}

private fun taskSearchHaystack(task: BoopTask): String {
    val base = buildString {
        append(task.title)
        append(' ')
        append(formatTaskReminderLine(task.reminderAt))
        append(if (task.done) " done completed" else " scheduled")
    }
    return base.lowercase(Locale.getDefault())
}

private fun noteSearchHaystack(note: BoopNote): String {
    val plain = HtmlCompat.fromHtml(note.body, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    return listOf(note.title, plain, note.attachmentUri.orEmpty(), note.tagsCsv, note.ocrText)
        .joinToString(" ")
        .lowercase(Locale.getDefault())
}

private fun habitSearchHaystack(habit: BoopHabit): String {
    return "${habit.title} ${habit.progress} ${habit.goal} ${habit.dayKeys} ${habit.quantityUnit} ${habit.quantityDailyTarget} ${habit.quantityDayValues}".lowercase(Locale.getDefault())
}

@Composable
private fun GlobalSearchResultsInline(
    query: String,
    tasks: List<BoopTask>,
    notes: List<BoopNote>,
    habits: List<BoopHabit>,
    onPickTask: (BoopTask) -> Unit,
    onPickNote: (BoopNote) -> Unit,
    onPickHabit: (BoopHabit) -> Unit,
) {
    val q = query.trim().lowercase(Locale.getDefault())
    val matchTasks = remember(tasks, q) {
        if (q.isEmpty()) emptyList() else tasks.filter { taskSearchHaystack(it).contains(q) }
    }
    val matchNotes = remember(notes, q) {
        if (q.isEmpty()) emptyList() else notes.filter { noteSearchHaystack(it).contains(q) }
    }
    val matchHabits = remember(habits, q) {
        if (q.isEmpty()) emptyList() else habits.filter { habitSearchHaystack(it).contains(q) }
    }
    val anyMatch = matchTasks.isNotEmpty() || matchNotes.isNotEmpty() || matchHabits.isNotEmpty()
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            q.isEmpty() -> {
                Text("Start typing to search across the app.", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.bodyMedium)
            }
            !anyMatch -> {
                Text("No matches.", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.bodyMedium)
            }
            else -> {
                if (matchTasks.isNotEmpty()) {
                    Text("Tasks", fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
                    matchTasks.take(12).forEach { task ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPickTask(task) },
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(task.title, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(formatTaskReminderLine(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                if (matchNotes.isNotEmpty()) {
                    Text("Notes", fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
                    matchNotes.take(12).forEach { note ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPickNote(note) },
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(note.title.ifBlank { "Untitled note" }, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                val snip = plainNoteSnippet(note.body, 96)
                                if (snip.isNotBlank()) {
                                    Text(snip, color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                if (matchHabits.isNotEmpty()) {
                    Text("Habits", fontWeight = FontWeight.SemiBold, color = Color.White, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
                    matchHabits.take(12).forEach { habit ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF151517)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPickHabit(habit) },
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(habit.title, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${habit.progress} / ${habit.goal} days", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitWeekStripCard(
    habit: BoopHabit,
    onPersist: (BoopHabit) -> Unit,
    onOpenHabit: (BoopHabit) -> Unit,
) {
    val todayKey = todayHabitDayKey()
    val dayValues = parseHabitDayValues(habit.quantityDayValues)
    val todayAmount = dayValues[todayKey] ?: 0
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1D)),
        shape = RoundedCornerShape(14.dp),
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
                    val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, offset) }
                    val key = habitDayKeyFormat.format(cal.time)
                    val done = if (habit.quantityMode) {
                        (dayValues[key] ?: 0) >= habit.quantityDailyTarget.coerceAtLeast(1)
                    } else {
                        key in parseHabitDayKeys(habit.dayKeys)
                    }
                    val isToday = key == todayKey
                    val label = SimpleDateFormat("EEE", Locale.US).format(cal.time)
                    val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
                    val dayAmount = dayValues[key] ?: 0
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
                                enabled = isToday && !habit.quantityMode,
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
                            if (habit.quantityMode) {
                                Text(
                                    if (dayAmount == 0) dayNum else dayAmount.toString(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            } else {
                                Text(dayNum, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            if (habit.quantityMode) {
                val unit = habit.quantityUnit.ifBlank { "units" }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${todayAmount} / ${habit.quantityDailyTarget} $unit today", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(10.dp))
                    val quickAdd = listOf(
                        1,
                        maxOf(5, habit.quantityDailyTarget / 4),
                        maxOf(10, habit.quantityDailyTarget / 2),
                    ).distinct()
                    quickAdd.forEach { delta ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFF242426),
                            modifier = Modifier.clickable {
                                val map = dayValues.toMutableMap()
                                map[todayKey] = todayAmount + delta
                                onPersist(habit.copy(quantityDayValues = serializeHabitDayValues(map)))
                            },
                        ) {
                            Text(
                                "+$delta",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                }
            }
            Text(
                if (habit.quantityMode) "Quick-add amount chips for today · tap title for details." else "Tap today’s column to log · tap the title for details.",
                color = Color(0xFF6E6E70),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun HabitTodayCheckInSheet(
    habits: List<BoopHabit>,
    onPersist: (BoopHabit) -> Unit,
    onEditHabit: (BoopHabit) -> Unit,
    onDismiss: () -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.88f)
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
                BoopSheetHeaderTitle("Habits week")
                Spacer(Modifier.height(4.dp))
                Text("Check-off habits toggle today; quantity habits let you add minutes/mL with +/-.", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (habits.isEmpty()) {
            Text("No habits yet.", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.bodySmall)
        } else {
            habits.forEach { habit ->
                HabitWeekStripCard(
                    habit = habit,
                    onPersist = onPersist,
                    onOpenHabit = onEditHabit,
                )
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

private fun insertDeviceCalendarEvent(
    context: Context,
    calendarId: Long,
    title: String,
    description: String,
    location: String,
    allDay: Boolean,
    startAt: Long,
    endAt: Long,
    repeatEveryDays: Int,
): Long {
    return try {
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.DTSTART, startAt)
            put(CalendarContract.Events.DTEND, maxOf(endAt, startAt + 60_000L))
            put(CalendarContract.Events.ALL_DAY, if (allDay) 1 else 0)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            if (repeatEveryDays > 0) put(CalendarContract.Events.RRULE, calendarRRuleFromRepeatDays(repeatEveryDays))
        }
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        uri?.lastPathSegment?.toLongOrNull() ?: -1L
    } catch (_: Throwable) {
        -1L
    }
}

private fun updateDeviceCalendarEvent(
    context: Context,
    eventId: Long,
    calendarId: Long,
    title: String,
    description: String,
    location: String,
    allDay: Boolean,
    startAt: Long,
    endAt: Long,
    repeatEveryDays: Int,
): Boolean {
    return try {
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.DTSTART, startAt)
            put(CalendarContract.Events.DTEND, maxOf(endAt, startAt + 60_000L))
            put(CalendarContract.Events.ALL_DAY, if (allDay) 1 else 0)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            if (repeatEveryDays > 0) {
                put(CalendarContract.Events.RRULE, calendarRRuleFromRepeatDays(repeatEveryDays))
            } else {
                putNull(CalendarContract.Events.RRULE)
            }
        }
        val rows = context.contentResolver.update(
            CalendarContract.Events.CONTENT_URI,
            values,
            "${CalendarContract.Events._ID} = ?",
            arrayOf(eventId.toString()),
        )
        rows > 0
    } catch (_: Throwable) {
        false
    }
}

private fun calendarRRuleFromRepeatDays(repeatEveryDays: Int): String {
    val days = repeatEveryDays.coerceAtLeast(1)
    return when {
        days == 365 -> "FREQ=YEARLY;INTERVAL=1"
        days % 7 == 0 -> "FREQ=WEEKLY;INTERVAL=${(days / 7).coerceAtLeast(1)}"
        else -> "FREQ=DAILY;INTERVAL=$days"
    }
}

private fun parseRepeatDaysFromRRule(rrule: String): Int {
    val normalized = rrule.uppercase(Locale.US)
    val interval = Regex("INTERVAL=(\\d+)").find(normalized)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
    return when {
        "FREQ=YEARLY" in normalized -> 365 * interval
        "FREQ=MONTHLY" in normalized -> 30 * interval
        "FREQ=WEEKLY" in normalized -> 7 * interval
        "FREQ=DAILY" in normalized -> interval
        else -> 0
    }
}

private fun repeatFrequencyLabel(days: Int): String {
    if (days <= 0) return "Does not repeat"
    if (days == 1) return "Daily"
    if (days == 7) return "Weekly"
    if (days == 365) return "Yearly"
    if (days % 7 == 0) return "Every ${days / 7} weeks"
    return "Every $days days"
}

@Composable
private fun EventEditorSheet(
    initial: ItemSheet.EventSheet,
    onDismiss: () -> Unit,
    onSave: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var title by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.title) }
    var description by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.description) }
    var location by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.location) }
    var allDay by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.allDay) }
    var startAt by rememberSaveable(initial.sessionKey) { mutableLongStateOf(initial.startAt) }
    var endAt by rememberSaveable(initial.sessionKey) { mutableLongStateOf(initial.endAt) }
    var notifyWeeksBefore by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.notifyWeeksBefore.toString()) }
    var notifyDaysBefore by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.notifyDaysBefore.toString()) }
    var notifyHoursBefore by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.notifyHoursBefore.toString()) }
    var repeatEveryDays by rememberSaveable(initial.sessionKey) { mutableIntStateOf(initial.repeatEveryDays.coerceAtLeast(0)) }
    var customRepeatDays by rememberSaveable(initial.sessionKey) {
        mutableStateOf(
            initial.repeatEveryDays.takeIf { it !in setOf(0, 1, 7, 30, 365) }?.toString().orEmpty(),
        )
    }
    var pickStart by rememberSaveable(initial.sessionKey) { mutableStateOf(false) }
    var pickEnd by rememberSaveable(initial.sessionKey) { mutableStateOf(false) }
    var selectedCalId by rememberSaveable(initial.sessionKey) { mutableLongStateOf(initial.calendarId ?: -1L) }
    var calendars by remember { mutableStateOf(emptyList<DeviceCalendarChoice>()) }
    var writeGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_CALENDAR,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val writePermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        writeGranted = granted
        if (!granted) Toast.makeText(context, "Calendar write permission denied.", Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(writeGranted) {
        if (writeGranted) {
            calendars = withContext(Dispatchers.IO) { readVisibleCalendars(context) }
            if (selectedCalId < 0 && calendars.isNotEmpty()) selectedCalId = calendars.first().id
        }
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            BoopSheetHeaderTitle(if (initial.eventId == null) "New event" else "Edit event")
        }
    }
    Spacer(Modifier.height(12.dp))
    BoopFilledTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("All day", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = allDay, onCheckedChange = { allDay = it })
    }
    Spacer(Modifier.height(8.dp))
    Surface(
        onClick = { pickStart = true },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF242426),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Starts", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
            Text(SimpleDateFormat("EEE, MMM dd · HH:mm", Locale.US).format(startAt), color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Text("Tap to edit", color = Color(0xFF6E6E70), style = MaterialTheme.typography.labelSmall)
        }
    }
    Spacer(Modifier.height(8.dp))
    Surface(
        onClick = { pickEnd = true },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF242426),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Ends", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
            Text(SimpleDateFormat("EEE, MMM dd · HH:mm", Locale.US).format(endAt), color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
    }
    ReminderPickerDialog(
        visible = pickStart,
        initialMillis = startAt,
        onDismiss = { pickStart = false },
        onConfirm = { picked ->
            startAt = picked
            if (endAt <= startAt) endAt = startAt + 60 * 60_000L
            pickStart = false
        },
    )
    ReminderPickerDialog(
        visible = pickEnd,
        initialMillis = endAt,
        onDismiss = { pickEnd = false },
        onConfirm = { picked ->
            endAt = maxOf(picked, startAt + 60_000L)
            pickEnd = false
        },
    )
    Spacer(Modifier.height(8.dp))
    if (writeGranted && calendars.isNotEmpty()) {
        val selectedName = calendars.firstOrNull { it.id == selectedCalId }?.displayName ?: calendars.first().displayName
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            calendars.forEach { cal ->
                val active = cal.id == selectedCalId
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (active) Color.White else Color(0xFF242426),
                    modifier = Modifier.clickable { selectedCalId = cal.id },
                ) {
                    Text(
                        cal.displayName,
                        color = if (active) Color.Black else Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("Calendar: $selectedName", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
    }
    Spacer(Modifier.height(8.dp))
    Text("Notifications before start", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.labelMedium)
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BoopFilledTextField(
            value = notifyWeeksBefore,
            onValueChange = { notifyWeeksBefore = it.filter { ch -> ch.isDigit() }.take(2) },
            label = { Text("Weeks") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        BoopFilledTextField(
            value = notifyDaysBefore,
            onValueChange = { notifyDaysBefore = it.filter { ch -> ch.isDigit() }.take(3) },
            label = { Text("Days") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        BoopFilledTextField(
            value = notifyHoursBefore,
            onValueChange = { notifyHoursBefore = it.filter { ch -> ch.isDigit() }.take(3) },
            label = { Text("Hours") },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
    }
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Repeat",
        color = Color(0xFF8E8E90),
        style = MaterialTheme.typography.labelSmall,
    )
    Spacer(Modifier.height(6.dp))
    val repeatOptions = listOf(
        0 to "None",
        1 to "Daily",
        7 to "Weekly",
        30 to "Monthly",
        365 to "Yearly",
    )
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeatOptions.forEach { (days, label) ->
            val active = repeatEveryDays == days
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (active) Color.White else Color(0xFF242426),
                modifier = Modifier.clickable {
                    repeatEveryDays = days
                    if (days in setOf(0, 1, 7, 30, 365)) customRepeatDays = ""
                },
            ) {
                Text(
                    label,
                    color = if (active) Color.Black else Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
        val customActive = repeatEveryDays !in setOf(0, 1, 7, 30, 365)
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (customActive) Color.White else Color(0xFF242426),
            modifier = Modifier.clickable { if (repeatEveryDays in setOf(0, 1, 7, 30, 365)) repeatEveryDays = 2 },
        ) {
            Text(
                "Custom",
                color = if (customActive) Color.Black else Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
    }
    if (repeatEveryDays !in setOf(0, 1, 7, 30, 365)) {
        Spacer(Modifier.height(6.dp))
        BoopFilledTextField(
            value = customRepeatDays,
            onValueChange = {
                val filtered = it.filter { ch -> ch.isDigit() }.take(3)
                customRepeatDays = filtered
                repeatEveryDays = filtered.toIntOrNull()?.coerceAtLeast(1) ?: repeatEveryDays
            },
            label = { Text("Every N days") },
            singleLine = true,
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(
        text = repeatFrequencyLabel(repeatEveryDays),
        color = Color(0xFF8E8E90),
        style = MaterialTheme.typography.labelSmall,
    )
    Spacer(Modifier.height(20.dp))
    BoopWhiteButton("Save event") {
        if (!writeGranted) {
            writePermLauncher.launch(Manifest.permission.WRITE_CALENDAR)
            return@BoopWhiteButton
        }
        if (title.isBlank()) return@BoopWhiteButton
        val calendarId = selectedCalId.takeIf { it >= 0 } ?: calendars.firstOrNull()?.id
        if (calendarId == null) {
            Toast.makeText(context, "No writable calendar found.", Toast.LENGTH_SHORT).show()
            return@BoopWhiteButton
        }
        val eventId = if (initial.eventId == null) {
            insertDeviceCalendarEvent(
                context = context,
                calendarId = calendarId,
                title = title.trim(),
                description = description.trim(),
                location = location.trim(),
                allDay = allDay,
                startAt = startAt,
                endAt = endAt,
                repeatEveryDays = repeatEveryDays.coerceAtLeast(0),
            )
        } else {
            val okUpdate = updateDeviceCalendarEvent(
                context = context,
                eventId = initial.eventId,
                calendarId = calendarId,
                title = title.trim(),
                description = description.trim(),
                location = location.trim(),
                allDay = allDay,
                startAt = startAt,
                endAt = endAt,
                repeatEveryDays = repeatEveryDays.coerceAtLeast(0),
            )
            if (okUpdate) initial.eventId else -1L
        }
        val ok = eventId > 0
        if (ok) {
            EventReminderScheduler.schedule(
                context = context,
                eventId = eventId,
                title = title.trim(),
                eventStartAt = startAt,
                weeksBefore = notifyWeeksBefore.toIntOrNull() ?: 0,
                daysBefore = notifyDaysBefore.toIntOrNull() ?: 0,
                hoursBefore = notifyHoursBefore.toIntOrNull() ?: 0,
            )
        }
        Toast.makeText(context, if (ok) "Event saved to Calendar" else "Failed to save event", Toast.LENGTH_SHORT).show()
        onSave(ok)
    }
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
    var repeatEveryDays by rememberSaveable(sheetKey) { mutableIntStateOf(initial.repeatEveryDays.coerceAtLeast(0)) }
    var customRepeatDays by rememberSaveable(sheetKey) {
        mutableStateOf(
            initial.repeatEveryDays.takeIf { it !in setOf(0, 1, 7, 30, 365) }?.toString().orEmpty(),
        )
    }
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
        }
    }
    Spacer(Modifier.height(12.dp))
    BoopFilledTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Task") },
    )
    Spacer(Modifier.height(8.dp))
    Text("Repeat", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(6.dp))
    val repeatOptions = listOf(
        0 to "None",
        1 to "Daily",
        7 to "Weekly",
        30 to "Monthly",
        365 to "Yearly",
    )
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeatOptions.forEach { (days, label) ->
            val active = repeatEveryDays == days
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (active) Color.White else Color(0xFF242426),
                modifier = Modifier.clickable {
                    repeatEveryDays = days
                    if (days in setOf(0, 1, 7, 30, 365)) customRepeatDays = ""
                },
            ) {
                Text(
                    label,
                    color = if (active) Color.Black else Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
        val customActive = repeatEveryDays !in setOf(0, 1, 7, 30, 365)
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (customActive) Color.White else Color(0xFF242426),
            modifier = Modifier.clickable { if (repeatEveryDays in setOf(0, 1, 7, 30, 365)) repeatEveryDays = 2 },
        ) {
            Text(
                "Custom",
                color = if (customActive) Color.Black else Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
    }
    if (repeatEveryDays !in setOf(0, 1, 7, 30, 365)) {
        Spacer(Modifier.height(6.dp))
        BoopFilledTextField(
            value = customRepeatDays,
            onValueChange = {
                val filtered = it.filter { ch -> ch.isDigit() }.take(3)
                customRepeatDays = filtered
                repeatEveryDays = filtered.toIntOrNull()?.coerceAtLeast(1) ?: repeatEveryDays
            },
            label = { Text("Every N days") },
            singleLine = true,
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(repeatFrequencyLabel(repeatEveryDays), color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
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
                    repeatEveryDays = repeatEveryDays.coerceAtLeast(0),
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
    var tagsCsv by rememberSaveable(session) { mutableStateOf(initial.tagsCsv) }
    var attachmentStored by remember(session) { mutableStateOf(parseNoteAttachments(initial.attachmentUri)) }
    var audioStored by remember(session) { mutableStateOf(initial.audioUri) }
    var bodyEdit by remember(session) { mutableStateOf<EditText?>(null) }
    var recording by remember(session) { mutableStateOf(false) }
    var recordingStartedAt by remember(session) { mutableLongStateOf(0L) }
    var recorder by remember(session) { mutableStateOf<MediaRecorder?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val existing = attachmentStored.toMutableList()
        uris.take((25 - existing.size).coerceAtLeast(0)).forEach { uri ->
            val copied = copyAttachmentToInternalFile(context, uri, UUID.randomUUID().toString())
            existing.add(copied ?: uri.toString())
        }
        attachmentStored = existing.distinct().take(25)
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) return@rememberLauncherForActivityResult
        val out = createNoteAudioFile(context, UUID.randomUUID().toString())
        try {
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setOutputFile(out.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            recording = true
            recordingStartedAt = System.currentTimeMillis()
            audioStored = out.absolutePath
            Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
        } catch (_: Throwable) {
            recorder?.release()
            recorder = null
            recording = false
            recordingStartedAt = 0L
            Toast.makeText(context, "Audio recording failed to start", Toast.LENGTH_SHORT).show()
        }
    }
    DisposableEffect(session) {
        onDispose {
            try {
                recorder?.stop()
            } catch (_: Throwable) {
            }
            try {
                recorder?.release()
            } catch (_: Throwable) {
            }
            recorder = null
            recordingStartedAt = 0L
            bodyEdit = null
        }
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
            if (initial.id != null) {
                IconButton(
                    onClick = {
                        val editable = bodyEdit?.text
                        val bodyHtml = if (editable is Spanned) {
                            Html.toHtml(editable, 0x1).trim()
                        } else {
                            editable?.toString()?.trim().orEmpty()
                        }
                        val serializedAttachments = serializeNoteAttachments(attachmentStored)
                        onSave(
                            BoopNote(
                                id = initial.id,
                                title = title.trim(),
                                body = bodyHtml,
                                attachmentUri = serializedAttachments,
                                audioUri = audioStored,
                                tagsCsv = normalizeNoteTags(tagsCsv),
                                ocrText = extractTextFromAttachment(context, serializedAttachments),
                                archived = true,
                                updatedAtMillis = System.currentTimeMillis(),
                            ),
                        )
                    },
                ) {
                    Icon(Icons.Outlined.Archive, contentDescription = "Archive", tint = Color(0xFFFFD98A))
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A8A))
                }
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
    BoopFilledTextField(
        value = tagsCsv,
        onValueChange = { tagsCsv = it },
        label = { Text("Tags") },
        placeholder = { Text("work, urgent, ideas", color = Color(0xFF8A8A8A)) },
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
    val currentBodyText = remember(bodyEdit?.text?.toString()) { bodyEdit?.text?.toString().orEmpty() }
    val linkRegex = remember { Regex("""https?://[^\s<>()]+""") }
    val typedLinks = remember(currentBodyText) { linkRegex.findAll(currentBodyText).map { it.value }.distinct().toList() }
    val spannedLinks = remember(bodyEdit?.text) {
        val et = bodyEdit
        val editable = et?.text as? Editable
        editable?.getSpans(0, editable.length, URLSpan::class.java)?.map { it.url }?.distinct().orEmpty()
    }
    val previewLinks = remember(typedLinks, spannedLinks) { (typedLinks + spannedLinks).distinct() }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { picker.launch("image/*") }) {
            Icon(Icons.Outlined.Image, contentDescription = "Attach image", tint = Color.White)
        }
        IconButton(
            onClick = {
                val urlInput = EditText(context).apply {
                    hint = "https://example.com"
                    setTextColor(android.graphics.Color.WHITE)
                    setHintTextColor(android.graphics.Color.parseColor("#8A8A8A"))
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setText("https://")
                    setSelection(text.length)
                }
                if (previewLinks.size >= 25) {
                    Toast.makeText(context, "Maximum 25 links per note.", Toast.LENGTH_SHORT).show()
                    return@IconButton
                }
                AlertDialog.Builder(context)
                    .setTitle("Add link preview")
                    .setMessage("Link will be added to the note and shown as a preview card.")
                    .setView(urlInput)
                    .setPositiveButton("Add link") { _, _ ->
                        noteEditInsertLink(bodyEdit, urlInput.text?.toString().orEmpty())
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
        ) {
            Icon(Icons.Outlined.Link, contentDescription = "Insert link", tint = Color.White)
        }
        IconButton(
            onClick = {
                if (recording) {
                    try {
                        recorder?.stop()
                    } catch (_: Throwable) {
                    }
                    try {
                        recorder?.release()
                    } catch (_: Throwable) {
                    }
                    recorder = null
                    recording = false
                    val secs = ((System.currentTimeMillis() - recordingStartedAt) / 1000L).coerceAtLeast(1L)
                    recordingStartedAt = 0L
                    Toast.makeText(context, "Recording saved (${secs}s)", Toast.LENGTH_SHORT).show()
                } else {
                    micPermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
        ) {
            Icon(
                if (recording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                contentDescription = if (recording) "Stop recording" else "Record audio",
                tint = if (recording) Color(0xFFFF8A8A) else Color.White,
            )
        }
        if (!audioStored.isNullOrBlank()) {
            IconButton(
                onClick = {
                    try {
                        val player = MediaPlayer().apply {
                            setDataSource(audioStored)
                            prepare()
                            start()
                            setOnCompletionListener { mp -> mp.release() }
                        }
                    } catch (_: Throwable) {
                    }
                },
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = "Play audio", tint = Color.White)
            }
        }
    }
    if (recording) {
        Spacer(Modifier.height(6.dp))
        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF3A1414)) {
            Text("Recording... tap stop to save", color = Color(0xFFFFB4B4), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
        }
    }
    if (previewLinks.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Text("Links", color = Color(0xFF9A9A9A), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            previewLinks.forEach { link ->
                NoteLinkPreviewCard(link)
            }
        }
    }
    if (attachmentStored.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        attachmentStored.chunked(3).forEach { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { stored ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(storedAttachmentForCoil(stored))
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
    Spacer(Modifier.height(20.dp))
    BoopWhiteButton("Save") {
        val noteId = initial.id ?: UUID.randomUUID().toString()
        val resolvedAttachment = serializeNoteAttachments(attachmentStored)
        val editable = bodyEdit?.text
        val bodyHtml = if (editable is Spanned) {
            Html.toHtml(editable, 0x1 /* Html.TO_HTML_PARCEL_OUTPUT_MODE */).trim()
        } else {
            editable?.toString()?.trim().orEmpty()
        }
        if (title.isNotBlank() || bodyHtml.isNotBlank()) {
            val ocrText = extractTextFromAttachment(context, resolvedAttachment)
            onSave(
                BoopNote(
                    id = noteId,
                    title = title.trim(),
                    body = bodyHtml,
                    attachmentUri = resolvedAttachment,
                    audioUri = audioStored,
                    tagsCsv = normalizeNoteTags(tagsCsv),
                    ocrText = ocrText,
                    archived = initial.id?.let { false } ?: false,
                    updatedAtMillis = System.currentTimeMillis(),
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
    var quantityMode by rememberSaveable(initial.id) { mutableStateOf(initial.quantityMode) }
    var quantityUnit by rememberSaveable(initial.id) { mutableStateOf(initial.quantityUnit) }
    var quantityTarget by rememberSaveable(initial.id) { mutableStateOf(initial.quantityDailyTarget.toString()) }
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
        label = { Text(if (quantityMode) "Daily target amount" else "Target days") },
    )
    Spacer(Modifier.height(8.dp))
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Track quantity (minutes / mL / etc.)", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = quantityMode, onCheckedChange = { quantityMode = it })
    }
    if (quantityMode) {
        Spacer(Modifier.height(8.dp))
        BoopFilledTextField(
            value = quantityUnit,
            onValueChange = { quantityUnit = it },
            label = { Text("Unit") },
            placeholder = { Text("minutes, mL, pages...", color = Color(0xFF8A8A8A)) },
        )
        Spacer(Modifier.height(8.dp))
        BoopFilledTextField(
            value = quantityTarget,
            onValueChange = { quantityTarget = it },
            label = { Text("Daily target") },
        )
    }
    val goalVal = goalText.toIntOrNull() ?: 30
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (quantityMode) "Today is logged in week view / habits page"
            else "Progress: $progress / $goalVal",
            color = Color(0xFFBFBFBF),
            style = MaterialTheme.typography.bodyLarge,
        )
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
                    quantityMode = quantityMode,
                    quantityUnit = quantityUnit.trim(),
                    quantityDailyTarget = (quantityTarget.toIntOrNull() ?: initial.quantityDailyTarget).coerceAtLeast(1),
                    quantityDayValues = initial.quantityDayValues,
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

data class BoopTask(
    val id: String,
    val title: String,
    val reminderAt: Long,
    val done: Boolean,
    val repeatEveryDays: Int = 0,
)
data class BoopNote(
    val id: String,
    val title: String,
    val body: String,
    val attachmentUri: String?,
    val audioUri: String? = null,
    val tagsCsv: String = "",
    val ocrText: String = "",
    val archived: Boolean = false,
    /** Last save time (local), used for week strip & search ordering. */
    val updatedAtMillis: Long = 0L,
)
/** [dayKeys] comma-separated yyyyMMdd calendar days marked done (dashboard strip). */
data class BoopHabit(
    val id: String,
    val title: String,
    val goal: Int,
    val progress: Int,
    val dayKeys: String = "",
    val quantityMode: Boolean = false,
    val quantityUnit: String = "",
    val quantityDailyTarget: Int = 30,
    val quantityDayValues: String = "",
)

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
            BoopTask(
                id = item.getString("id"),
                title = item.getString("title"),
                reminderAt = item.getLong("reminderAt"),
                done = item.getBoolean("done"),
                repeatEveryDays = item.optInt("repeatEveryDays", 0),
            )
        }
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
            out.add(
                BoopNote(
                    item.getString("id"),
                    item.optString("title"),
                    item.optString("body"),
                    item.optString("attachmentUri").ifBlank { null },
                    item.optString("audioUri").ifBlank { null },
                    item.optString("tags"),
                    item.optString("ocrText"),
                    item.optBoolean("archived", false),
                    u,
                ),
            )
        }
        return out
    }

    fun readHabits(): List<BoopHabit> {
        return parseArray(store.read("habits")) { item ->
            BoopHabit(
                item.getString("id"),
                item.getString("title"),
                item.getInt("goal"),
                item.getInt("progress"),
                item.optString("dayKeys"),
                item.optBoolean("quantityMode", false),
                item.optString("quantityUnit"),
                item.optInt("quantityDailyTarget", 30),
                item.optString("quantityDayValues"),
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
        val stamped = note.copy(updatedAtMillis = System.currentTimeMillis())
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
                    .put("archived", it.archived)
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
                    .put("archived", it.archived)
                    .put("updatedAt", it.updatedAtMillis),
            )
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
                    .put("repeatEveryDays", it.repeatEveryDays),
            )
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
            putExtra("taskId", task.id)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (task.done) {
            manager.cancel(pending)
            return
        }
        try {
            if (task.repeatEveryDays > 0) {
                val intervalMillis = task.repeatEveryDays * 24L * 60L * 60L * 1000L
                manager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    task.reminderAt,
                    intervalMillis,
                    pending,
                )
                return
            }
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

object EventReminderScheduler {
    fun schedule(
        context: Context,
        eventId: Long,
        title: String,
        eventStartAt: Long,
        weeksBefore: Int,
        daysBefore: Int,
        hoursBefore: Int,
    ) {
        val offsets = listOf(
            weeksBefore * 7L * 24L * 60L * 60L * 1000L,
            daysBefore * 24L * 60L * 60L * 1000L,
            hoursBefore * 60L * 60L * 1000L,
        ).filter { it > 0L }
        if (offsets.isEmpty()) return
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        offsets.forEachIndexed { idx, offset ->
            val at = eventStartAt - offset
            if (at <= System.currentTimeMillis()) return@forEachIndexed
            val requestCode = ((eventId % Int.MAX_VALUE) + (idx + 1) * 13_337).toInt()
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                putExtra("title", "Event: $title")
                putExtra("id", requestCode)
                putExtra("taskId", "")
            }
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
                } else {
                    @Suppress("DEPRECATION")
                    manager.setExact(AlarmManager.RTC_WAKEUP, at, pending)
                }
            } catch (_: Throwable) {
            }
        }
    }
}

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ReminderNotifier.ACTION_COMPLETE_TASK) {
            val taskId = intent.getStringExtra("taskId").orEmpty()
            val id = intent.getIntExtra("id", 1)
            if (taskId.isNotBlank()) {
                TaskNotificationActions.markTaskCompleted(context, taskId)
            }
            androidx.core.app.NotificationManagerCompat.from(context).cancel(id)
            return
        }
        val title = intent.getStringExtra("title") ?: "Task due"
        val id = intent.getIntExtra("id", 1)
        val taskId = intent.getStringExtra("taskId").orEmpty()
        ReminderNotifier.show(context, id, title, taskId)
    }
}

object ReminderNotifier {
    private const val CHANNEL = "boop_reminders"
    const val ACTION_COMPLETE_TASK = "com.prodash.reminders.ACTION_COMPLETE_TASK"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "Boop Reminders", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        LocalStore.init(context)
    }

    fun show(context: Context, id: Int, title: String, taskId: String) {
        val completeIntent = Intent(context, TaskReminderReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra("id", id)
            putExtra("taskId", taskId)
        }
        val completePending = PendingIntent.getBroadcast(
            context,
            id + 10_000,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification_minimal)
            .setContentTitle("Boop reminder")
            .setContentText(title)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        if (taskId.isNotBlank()) {
            builder.addAction(0, "Mark as completed", completePending)
        }
        val notification = builder.build()
        androidx.core.app.NotificationManagerCompat.from(context).notify(id, notification)
    }
}

private object TaskNotificationActions {
    fun markTaskCompleted(context: Context, taskId: String) {
        val pref = context.getSharedPreferences("boop_store", Context.MODE_PRIVATE)
        val raw = pref.getString("tasks", "[]").orEmpty()
        val arr = JSONArray(raw)
        var changed = false
        var rescheduleTask: BoopTask? = null
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            if (item.optString("id") == taskId) {
                val repeatEveryDays = item.optInt("repeatEveryDays", 0)
                if (repeatEveryDays > 0) {
                    val step = repeatEveryDays * 24L * 60L * 60L * 1000L
                    val base = item.optLong("reminderAt", System.currentTimeMillis())
                    var nextAt = base + step
                    while (nextAt <= System.currentTimeMillis()) nextAt += step
                    item.put("reminderAt", nextAt)
                    item.put("done", false)
                    changed = true
                    rescheduleTask = BoopTask(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        reminderAt = nextAt,
                        done = false,
                        repeatEveryDays = repeatEveryDays,
                    )
                } else if (!item.optBoolean("done", false)) {
                    item.put("done", true)
                    changed = true
                }
                break
            }
        }
        if (changed) {
            pref.edit().putString("tasks", arr.toString()).apply()
            rescheduleTask?.let { ReminderScheduler.schedule(context, it) }
        }
    }
}
