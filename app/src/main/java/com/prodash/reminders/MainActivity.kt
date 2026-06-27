@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
)

package com.prodash.reminders

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.text.TextUtils
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.graphics.Typeface
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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Search
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Unarchive
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.io.File
import java.net.URL
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.hypot
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ReminderNotifier.createChannel(this)
        setContent { BoopApp() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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
        val linkedNoteId: String? = null,
        val archived: Boolean = false,
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
        val linkedTaskId: String? = null,
        val archived: Boolean = false,
        val createdAtMillis: Long = 0L,
        val updatedAtMillis: Long = 0L,
    ) : ItemSheet()

    data class HabitSheet(
        val id: String?,
        /** Stable key for `rememberSaveable` when creating a new habit (id is null). */
        val sessionKey: String,
        val title: String,
        val dayPeriodCategory: String,
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

    data class AccountSheet(
        val id: String?,
        val sessionKey: String,
        val name: String,
    ) : ItemSheet()

    data class FinanceEntrySheet(
        val sessionKey: String,
        val type: String, // income | expense | transfer
        val entryId: String? = null,
        val createdAtMillis: Long = 0L,
        val prefilledTitle: String = "",
        val prefilledAmount: String = "",
        val prefilledAccountId: String = "",
        val prefilledToAccountId: String = "",
        val prefilledCategory: String = "",
        val prefilledSubcategory: String = "",
        val prefilledNote: String = "",
        val prefilledDueAtMillis: Long = 0L,
    ) : ItemSheet()
}

private enum class ThemeMode(val storageKey: String, val label: String) {
    DARK("dark", "Dark"),
    LIGHT("light", "Light"),
    SYSTEM("system", "System"),
    ;

    companion object {
        fun fromStorage(value: String?) = entries.find { it.storageKey == value } ?: SYSTEM
    }
}

private enum class BoopTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Outlined.Dashboard),
    TASKS("Tasks", Icons.Outlined.Notifications),
    CALENDAR("Calendar", Icons.Outlined.CalendarMonth),
    HABITS("Habits", Icons.Outlined.Flag),
    WALLET("Accounts", Icons.Outlined.AttachMoney),
}

private fun buildVisibleTabs(showHabitsPage: Boolean, showWalletPage: Boolean): List<BoopTab> = buildList {
    add(BoopTab.HOME)
    add(BoopTab.TASKS)
    add(BoopTab.CALENDAR)
    if (showHabitsPage) add(BoopTab.HABITS)
    if (showWalletPage) add(BoopTab.WALLET)
}

private data class BoopPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceElevated: Color,
    val onBackground: Color,
    val muted: Color,
    val accent: Color,
    val accentGlow: Color,
    val accentOn: Color,
    val navPill: Color,
    val navSelected: Color,
    val navUnselected: Color,
    val inputField: Color,
    val danger: Color,
    val recording: Color,
    val quoteFill: Color,
    val quoteStroke: Color,
)

/** Classic parchment · popped terracotta accent · warm rose glow. */
private fun boopDarkPalette() = BoopPalette(
    background = Color(0xFF141413),
    surface = Color(0xFF30302E),
    surfaceVariant = Color(0xFF252320),
    surfaceElevated = Color(0xFF3D3D3A),
    onBackground = Color(0xFFFAF9F5),
    muted = Color(0xFFB0AEA5),
    accent = Color(0xFFE88868),
    accentGlow = Color(0xFFE8A898),
    accentOn = Color(0xFFFFFFFF),
    navPill = Color(0x33E8A898),
    navSelected = Color(0xFFE88868),
    navUnselected = Color(0xFF8A8480),
    inputField = Color(0xFF1E1C1A),
    danger = Color(0xFFE07A6A),
    recording = Color(0xFFE88868),
    quoteFill = Color(0xFF2E2420),
    quoteStroke = Color(0xFFE88868),
)

private fun boopLightPalette() = BoopPalette(
    background = Color(0xFFFAF9F5),
    surface = Color(0xFFF5F4ED),
    surfaceVariant = Color(0xFFE8E6DC),
    surfaceElevated = Color(0xFFEFE9DE),
    onBackground = Color(0xFF141413),
    muted = Color(0xFF87867F),
    accent = Color(0xFFD46E48),
    accentGlow = Color(0xFFE8A898),
    accentOn = Color(0xFFFFFFFF),
    navPill = Color(0x28E8A898),
    navSelected = Color(0xFFD46E48),
    navUnselected = Color(0xFF9A9288),
    inputField = Color(0xFFF0EBE4),
    danger = Color(0xFFC45850),
    recording = Color(0xFFD46E48),
    quoteFill = Color(0xFFF8ECE6),
    quoteStroke = Color(0xFFD46E48),
)

private val LocalBoopPalette = staticCompositionLocalOf { boopDarkPalette() }
private val LocalBoopDataEpoch = staticCompositionLocalOf { 0 }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoopApp() {
    val repository = remember { BoopRepository(LocalStore) }
    val tasks = remember { mutableStateListOf<BoopTask>() }
    val notes = remember { mutableStateListOf<BoopNote>() }
    val habits = remember { mutableStateListOf<BoopHabit>() }
    val accounts = remember { mutableStateListOf<BoopAccount>() }
    val ledgerEntries = remember { mutableStateListOf<BoopLedgerEntry>() }

    var itemSheet by remember { mutableStateOf<ItemSheet?>(null) }
    var habitCheckInOpen by remember { mutableStateOf(false) }
    var speedDialExpanded by remember { mutableStateOf(false) }
    var speedDialAnchor by remember { mutableStateOf(Rect.Zero) }
    var voiceCaptureOpen by remember { mutableStateOf(false) }
    var voiceListening by remember { mutableStateOf(false) }
    var voiceStartSignal by remember { mutableIntStateOf(0) }
    var voiceStopSignal by remember { mutableIntStateOf(0) }
    var calendarSyncRequest by rememberSaveable { mutableIntStateOf(0) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var dashboardSearchOpen by rememberSaveable { mutableStateOf(false) }
    var tasksTabShowNotes by rememberSaveable { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf(LocalStore.readThemeMode()) }
    var showHabitsPage by remember { mutableStateOf(LocalStore.readShowHabitsPage()) }
    var showWalletPage by remember { mutableStateOf(LocalStore.readShowWalletPage()) }
    val visibleTabs = remember(showHabitsPage, showWalletPage) {
        buildVisibleTabs(showHabitsPage, showWalletPage)
    }
    var selectedBoopTab by rememberSaveable { mutableStateOf(BoopTab.HOME.name) }
    val selectedTab = visibleTabs.indexOfFirst { it.name == selectedBoopTab }.let { if (it >= 0) it else 0 }
    fun selectTabIndex(index: Int) {
        visibleTabs.getOrNull(index)?.let { selectedBoopTab = it.name }
    }
    fun selectTab(tab: BoopTab) {
        visibleTabs.indexOf(tab).takeIf { it >= 0 }?.let { selectedBoopTab = tab.name }
    }

    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }
    val palette = if (useDarkTheme) boopDarkPalette() else boopLightPalette()
    var dataEpoch by remember { mutableIntStateOf(0) }

    fun refresh() {
        tasks.clear()
        tasks.addAll(repository.readTasks())
        notes.clear()
        notes.addAll(repository.readNotes())
        habits.clear()
        habits.addAll(repository.readHabits())
        accounts.clear()
        accounts.addAll(repository.readAccounts())
        ledgerEntries.clear()
        ledgerEntries.addAll(repository.readLedgerEntries())
        dataEpoch++
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

    val darkBg = palette.background
    val darkSurface = palette.surface
    val accent = palette.accent

    fun openTaskSheet(task: BoopTask? = null) {
        itemSheet = ItemSheet.TaskSheet(
            id = task?.id,
            sessionKey = task?.id ?: UUID.randomUUID().toString(),
            title = task?.title.orEmpty(),
            reminderAt = task?.reminderAt ?: (System.currentTimeMillis() + 30 * 60_000),
            done = task?.done ?: false,
            repeatEveryDays = task?.repeatEveryDays ?: 0,
            linkedNoteId = task?.linkedNoteId,
            archived = task?.archived ?: false,
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
            linkedTaskId = note?.linkedTaskId,
            archived = note?.archived ?: false,
            createdAtMillis = note?.createdAtMillis ?: 0L,
            updatedAtMillis = note?.updatedAtMillis ?: 0L,
        )
        speedDialExpanded = false
    }

    fun openHabitSheet(habit: BoopHabit? = null) {
        itemSheet = ItemSheet.HabitSheet(
            id = habit?.id,
            sessionKey = habit?.id ?: UUID.randomUUID().toString(),
            title = habit?.title.orEmpty(),
            dayPeriodCategory = habit?.dayPeriodCategory ?: "day",
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
    fun openFinanceEntrySheet(
        type: String,
        prefilledTitle: String = "",
        prefilledAmount: String = "",
        prefilledAccountId: String = "",
        prefilledToAccountId: String = "",
        prefilledCategory: String = "",
        prefilledNote: String = "",
    ) {
        itemSheet = ItemSheet.FinanceEntrySheet(
            sessionKey = UUID.randomUUID().toString(),
            type = type,
            prefilledTitle = prefilledTitle,
            prefilledAmount = prefilledAmount,
            prefilledAccountId = prefilledAccountId,
            prefilledToAccountId = prefilledToAccountId,
            prefilledCategory = prefilledCategory,
            prefilledNote = prefilledNote,
        )
        speedDialExpanded = false
    }
    fun openFinanceEntrySheetForEdit(entry: BoopLedgerEntry) {
        itemSheet = ItemSheet.FinanceEntrySheet(
            sessionKey = entry.id,
            entryId = entry.id,
            type = entry.type,
            createdAtMillis = entry.createdAtMillis,
            prefilledTitle = entry.title,
            prefilledAmount = formatLedgerAmountForEdit(entry.amount),
            prefilledAccountId = entry.accountId,
            prefilledToAccountId = entry.toAccountId.orEmpty(),
            prefilledCategory = entry.category,
            prefilledSubcategory = entry.subcategory,
            prefilledNote = entry.note,
            prefilledDueAtMillis = entry.dueAtMillis ?: 0L,
        )
        speedDialExpanded = false
    }
    fun openAccountSheet(account: BoopAccount? = null) {
        itemSheet = ItemSheet.AccountSheet(
            id = account?.id,
            sessionKey = account?.id ?: UUID.randomUUID().toString(),
            name = account?.name.orEmpty(),
        )
        speedDialExpanded = false
    }

    val context = LocalContext.current
    val launchActivity = context as? Activity
    var pendingOpenTaskId by rememberSaveable { mutableStateOf(launchActivity?.intent?.getStringExtra("openTaskId")) }
    var pendingOpenEventId by rememberSaveable { mutableLongStateOf(launchActivity?.intent?.getLongExtra("openEventId", -1L) ?: -1L) }
    var calendarCreateAtMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }

    fun openEventSheet(startAt: Long = System.currentTimeMillis(), existing: CalendarEventDetail? = null) {
        val start = existing?.startAt ?: (startOfDayMillis(startAt) + 9 * 60 * 60_000L)
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

    var pendingVoiceCapture by remember { mutableStateOf<ParsedVoiceCapture?>(null) }

    fun applyVoiceCapture(parsed: ParsedVoiceCapture) {
        voiceStopSignal++
        voiceListening = false
        voiceCaptureOpen = false
        pendingVoiceCapture = parsed
    }

    fun openVoiceCaptureResult(parsed: ParsedVoiceCapture) {
        when (parsed.type) {
            VoiceCaptureType.TASK -> {
                itemSheet = ItemSheet.TaskSheet(
                    id = null,
                    sessionKey = UUID.randomUUID().toString(),
                    title = parsed.title,
                    reminderAt = parsed.dueAtMillis ?: (System.currentTimeMillis() + 30 * 60_000),
                    done = false,
                    repeatEveryDays = parsed.repeatEveryDays,
                )
            }
            VoiceCaptureType.EVENT -> {
                val start = parsed.dueAtMillis ?: (startOfDayMillis(System.currentTimeMillis()) + 9 * 60 * 60_000L)
                val end = parsed.endAtMillis ?: (start + 60 * 60_000L)
                itemSheet = ItemSheet.EventSheet(
                    eventId = null,
                    calendarId = null,
                    sessionKey = UUID.randomUUID().toString(),
                    title = parsed.title,
                    description = parsed.body,
                    location = parsed.location,
                    allDay = parsed.allDay,
                    startAt = start,
                    endAt = end,
                    notifyWeeksBefore = 0,
                    notifyDaysBefore = 0,
                    notifyHoursBefore = 0,
                    repeatEveryDays = parsed.repeatEveryDays,
                )
            }
            VoiceCaptureType.HABIT -> {
                itemSheet = ItemSheet.HabitSheet(
                    id = null,
                    sessionKey = UUID.randomUUID().toString(),
                    title = parsed.title,
                    dayPeriodCategory = parsed.habitDayPeriod,
                    goal = 30,
                    progress = 0,
                    dayKeys = "",
                    quantityMode = false,
                    quantityUnit = "",
                    quantityDailyTarget = 30,
                    quantityDayValues = "",
                )
            }
            VoiceCaptureType.EXPENSE -> openFinanceEntrySheet(
                type = "expense",
                prefilledTitle = parsed.title,
                prefilledAmount = parsed.amount?.let { "%.2f".format(it) }.orEmpty(),
                prefilledAccountId = parsed.accountId.orEmpty(),
                prefilledCategory = parsed.category,
                prefilledNote = parsed.body,
            )
            VoiceCaptureType.INCOME -> openFinanceEntrySheet(
                type = "income",
                prefilledTitle = parsed.title,
                prefilledAmount = parsed.amount?.let { "%.2f".format(it) }.orEmpty(),
                prefilledAccountId = parsed.accountId.orEmpty(),
                prefilledCategory = parsed.category,
                prefilledNote = parsed.body,
            )
            VoiceCaptureType.TRANSFER -> openFinanceEntrySheet(
                type = "transfer",
                prefilledTitle = parsed.title,
                prefilledAmount = parsed.amount?.let { "%.2f".format(it) }.orEmpty(),
                prefilledAccountId = parsed.accountId.orEmpty(),
                prefilledToAccountId = parsed.toAccountId.orEmpty(),
                prefilledNote = parsed.body,
            )
            VoiceCaptureType.NOTE -> {
                val trimmedTitle = parsed.title.trim()
                val trimmedBody = parsed.body.trim()
                if (trimmedTitle.isNotBlank() || trimmedBody.isNotBlank()) {
                    val noteId = UUID.randomUUID().toString()
                    val now = System.currentTimeMillis()
                    val note = BoopNote(
                        id = noteId,
                        title = trimmedTitle,
                        body = trimmedBody,
                        attachmentUri = null,
                        audioUri = null,
                        tagsCsv = "",
                        ocrText = "",
                        linkedTaskId = null,
                        archived = false,
                        createdAtMillis = now,
                        updatedAtMillis = now,
                    )
                    repository.saveNote(note)
                    refresh()
                    itemSheet = ItemSheet.NoteSheet(
                        id = noteId,
                        sessionKey = noteId,
                        title = trimmedTitle,
                        body = trimmedBody,
                        attachmentUri = null,
                        audioUri = null,
                        tagsCsv = "",
                        createdAtMillis = now,
                        updatedAtMillis = now,
                    )
                } else {
                    itemSheet = ItemSheet.NoteSheet(
                        id = null,
                        sessionKey = UUID.randomUUID().toString(),
                        title = "",
                        body = "",
                        attachmentUri = null,
                        audioUri = null,
                        tagsCsv = "",
                    )
                }
            }
        }
        speedDialExpanded = false
    }

    LaunchedEffect(pendingVoiceCapture) {
        val parsed = pendingVoiceCapture ?: return@LaunchedEffect
        delay(120)
        openVoiceCaptureResult(parsed)
        pendingVoiceCapture = null
    }

    LaunchedEffect(tasks, pendingOpenTaskId) {
        val targetId = pendingOpenTaskId ?: return@LaunchedEffect
        tasks.firstOrNull { it.id == targetId }?.let {
            openTaskSheet(it)
            pendingOpenTaskId = null
        }
    }
    LaunchedEffect(pendingOpenEventId) {
        if (pendingOpenEventId > 0L) {
            openEventSheetById(pendingOpenEventId)
            pendingOpenEventId = -1L
        }
    }

    val colorScheme = if (useDarkTheme) {
        darkColorScheme(
            background = palette.background,
            surface = palette.surface,
            surfaceVariant = palette.surfaceVariant,
            onBackground = palette.onBackground,
            onSurface = palette.onBackground,
            onSurfaceVariant = palette.muted,
            primary = palette.accent,
            onPrimary = palette.accentOn,
            secondary = palette.surfaceElevated,
            onSecondary = palette.onBackground,
            outline = palette.muted,
        )
    } else {
        lightColorScheme(
            background = palette.background,
            surface = palette.surface,
            surfaceVariant = palette.surfaceVariant,
            onBackground = palette.onBackground,
            onSurface = palette.onBackground,
            onSurfaceVariant = palette.muted,
            primary = palette.accent,
            onPrimary = palette.accentOn,
            secondary = palette.surfaceElevated,
            onSecondary = palette.onBackground,
            outline = palette.muted,
        )
    }

    CompositionLocalProvider(
        LocalBoopPalette provides palette,
        LocalBoopDataEpoch provides dataEpoch,
    ) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = boopTypography(),
    ) {
        BoopTextTheme {
        var showLaunchSplash by remember { mutableStateOf(true) }
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
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    refresh()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
        val pagerState = rememberPagerState(
            initialPage = selectedTab.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0)),
            pageCount = { visibleTabs.size },
        )
        val pagerScrollPosition = pagerState.currentPage + pagerState.currentPageOffsetFraction
        val currentTab = visibleTabs.getOrElse(selectedTab.coerceIn(0, visibleTabs.lastIndex)) { BoopTab.HOME }
        LaunchedEffect(showHabitsPage, showWalletPage) {
            if (visibleTabs.none { it.name == selectedBoopTab }) {
                selectedBoopTab = BoopTab.HOME.name
            }
        }
        LaunchedEffect(pagerState.isScrollInProgress, pagerState.currentPage) {
            if (!pagerState.isScrollInProgress) {
                visibleTabs.getOrNull(pagerState.currentPage)?.let { tab ->
                    if (tab.name != selectedBoopTab) selectedBoopTab = tab.name
                }
                speedDialExpanded = false
            }
        }
        LaunchedEffect(selectedTab) {
            if (pagerState.currentPage != selectedTab) {
                pagerState.animateScrollToPage(selectedTab)
            }
        }
        BackHandler(enabled = !showLaunchSplash) {
            when {
                settingsOpen -> settingsOpen = false
                voiceCaptureOpen -> {
                    voiceStopSignal++
                    voiceListening = false
                    voiceCaptureOpen = false
                }
                habitCheckInOpen -> habitCheckInOpen = false
                itemSheet != null -> itemSheet = null
                speedDialExpanded -> speedDialExpanded = false
                dashboardSearchOpen -> dashboardSearchOpen = false
                tasksTabShowNotes -> tasksTabShowNotes = false
                selectedTab != 0 -> {
                    selectTab(BoopTab.HOME)
                    scope.launch { pagerState.animateScrollToPage(0) }
                }
                else -> launchActivity?.finish()
            }
        }
        BoopLaunchReveal(
            active = showLaunchSplash,
            onFinished = { showLaunchSplash = false },
        ) {
        Surface(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
            Scaffold(
                containerColor = darkBg,
                bottomBar = {
                    if (!settingsOpen) {
                        BoopBottomBar(
                            tabs = visibleTabs,
                            pagerScrollPosition = pagerScrollPosition,
                            currentTab = currentTab,
                            expanded = speedDialExpanded,
                            onExpandedChange = { speedDialExpanded = it },
                            onAddAnchorChanged = { speedDialAnchor = it },
                            onSelectTab = {
                                selectTabIndex(it)
                                speedDialExpanded = false
                            },
                            onOpenTask = { openTaskSheet(null) },
                            onOpenHabit = { openHabitSheet(null) },
                            onOpenVoiceCapture = {
                                speedDialExpanded = false
                                if (voiceListening) {
                                    voiceStopSignal++
                                    voiceListening = false
                                } else {
                                    voiceListening = false
                                    voiceCaptureOpen = true
                                    voiceStartSignal++
                                }
                            },
                            voiceListening = voiceListening,
                        )
                    }
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
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                            .coerceIn(-1f, 1f)
                        val pageAlpha = 1f - kotlin.math.abs(pageOffset) * 0.28f
                        Box(
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = pageAlpha },
                        ) {
                        BoopPagerPage(
                            tab = visibleTabs[page],
                            visibleTabs = visibleTabs,
                            tasks = tasks,
                            notes = notes,
                            habits = habits,
                            accounts = accounts,
                            ledgerEntries = ledgerEntries,
                            calendarSyncRequest = calendarSyncRequest,
                            onPersistHabit = { habit ->
                                repository.saveHabit(habit)
                                refresh()
                            },
                            onSelectTab = { tab -> selectTab(tab) },
                            onEditTask = { openTaskSheet(it) },
                            onEditEvent = { openEventSheetById(it) },
                            onEditNote = { openNoteSheet(it) },
                            onArchiveTask = { t ->
                                repository.saveTask(t.copy(archived = true))
                                ReminderScheduler.schedule(AppContextHolder.context, t.copy(archived = true))
                                refresh()
                            },
                            onCompleteTask = { t ->
                                val updated = if (t.repeatEveryDays > 0) {
                                    t.copy(reminderAt = nextRepeatReminderMillis(t.reminderAt, t.repeatEveryDays), done = false)
                                } else {
                                    t.copy(done = true)
                                }
                                repository.saveTask(updated)
                                ReminderScheduler.schedule(AppContextHolder.context, updated)
                                refresh()
                            },
                            onUnarchiveTask = { t ->
                                repository.saveTask(t.copy(archived = false))
                                ReminderScheduler.schedule(AppContextHolder.context, t.copy(archived = false))
                                refresh()
                            },
                            onRestoreCompletedTask = { t ->
                                repository.saveTask(t.copy(done = false))
                                ReminderScheduler.schedule(AppContextHolder.context, t.copy(done = false))
                                refresh()
                            },
                            onCalendarSelectedDayChanged = { dayMillis -> calendarCreateAtMillis = dayMillis },
                            onEditHabit = { openHabitSheet(it) },
                            onOpenHabitCheckIn = {
                                itemSheet = null
                                habitCheckInOpen = true
                            },
                            onDeleteAccount = { accountId ->
                                repository.deleteAccount(accountId)
                                refresh()
                            },
                            onSaveLedgerEntry = { entry ->
                                repository.saveLedgerEntry(entry)
                                refresh()
                            },
                            onEditLedgerEntry = { openFinanceEntrySheetForEdit(it) },
                            onDeleteLedgerEntry = { id ->
                                repository.deleteLedgerEntry(id)
                                refresh()
                            },
                            onEditAccount = { openAccountSheet(it) },
                            onOpenSettings = {
                                speedDialExpanded = false
                                settingsOpen = true
                            },
                            dashboardSearchOpen = dashboardSearchOpen,
                            onDashboardSearchOpenChange = { dashboardSearchOpen = it },
                            tasksTabShowNotes = tasksTabShowNotes,
                            onTasksTabShowNotesChange = { tasksTabShowNotes = it },
                        )
                        }
                    }
                    AnimatedVisibility(
                        visible = settingsOpen,
                        enter = fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(
                                initialOffsetX = { it / 5 },
                                animationSpec = tween(320, easing = FastOutSlowInEasing),
                            ),
                        exit = fadeOut(tween(220, easing = FastOutSlowInEasing)) +
                            slideOutHorizontally(
                                targetOffsetX = { it / 5 },
                                animationSpec = tween(260, easing = FastOutSlowInEasing),
                            ),
                    ) {
                        SettingsScreen(
                            themeMode = themeMode,
                            onThemeModeChange = { mode ->
                                themeMode = mode
                                LocalStore.saveThemeMode(mode)
                            },
                            showHabitsPage = showHabitsPage,
                            onShowHabitsPageChange = { enabled ->
                                showHabitsPage = enabled
                                LocalStore.saveShowHabitsPage(enabled)
                            },
                            showWalletPage = showWalletPage,
                            onShowWalletPageChange = { enabled ->
                                showWalletPage = enabled
                                LocalStore.saveShowWalletPage(enabled)
                            },
                            onBack = { settingsOpen = false },
                        )
                    }
                    PullRefreshIndicator(
                        refreshing = pullRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        contentColor = palette.onBackground,
                        backgroundColor = palette.surfaceVariant,
                    )
                }
            }

            itemSheet?.let { sheet ->
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { itemSheet = null },
                    sheetState = sheetState,
                    containerColor = darkSurface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = palette.muted) },
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
                                notes = notes.filter { !it.archived }.sortedByDescending { it.createdAtMillis + it.updatedAtMillis },
                                onDismiss = { itemSheet = null },
                                onDelete = sheet.id?.let { id ->
                                    {
                                        repository.deleteTask(id)
                                        refresh()
                                        itemSheet = null
                                    }
                                },
                                onSaveNote = { note ->
                                    repository.saveNote(note)
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
                                    calendarSyncRequest++
                                        itemSheet = null
                                    }
                                },
                            )
                            is ItemSheet.FinanceEntrySheet -> FinanceEntrySheet(
                                initial = sheet,
                                accounts = accounts,
                                onDismiss = { itemSheet = null },
                                onSave = { entry ->
                                    repository.saveLedgerEntry(entry)
                                    refresh()
                                    itemSheet = null
                                },
                                onDelete = sheet.entryId?.let { entryId ->
                                    {
                                        repository.deleteLedgerEntry(entryId)
                                        refresh()
                                        itemSheet = null
                                    }
                                },
                            )
                            is ItemSheet.AccountSheet -> AccountEditorSheet(
                                initial = sheet,
                                onDismiss = { itemSheet = null },
                                onSave = { account ->
                                    repository.saveAccount(account)
                                    refresh()
                                    itemSheet = null
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
                    dragHandle = { BottomSheetDefaults.DragHandle(color = palette.muted) },
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

            if (voiceCaptureOpen) {
                val voiceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = {
                        voiceStopSignal++
                        voiceListening = false
                        voiceCaptureOpen = false
                    },
                    sheetState = voiceSheetState,
                    containerColor = darkSurface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = palette.muted) },
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 28.dp)
                            .imePadding(),
                    ) {
                        VoiceCaptureSheet(
                            accounts = accounts,
                            autoStart = false,
                            startSignal = voiceStartSignal,
                            stopSignal = voiceStopSignal,
                            onDismiss = {
                                voiceStopSignal++
                                voiceListening = false
                                voiceCaptureOpen = false
                            },
                            onParsed = { applyVoiceCapture(it) },
                            onListeningChanged = { voiceListening = it },
                        )
                    }
                }
            }

            if (!settingsOpen) {
                BoopSpeedDialOverlay(
                    expanded = speedDialExpanded,
                    anchorBounds = speedDialAnchor,
                    currentTab = currentTab,
                    showHabitsPage = showHabitsPage,
                    showWalletPage = showWalletPage,
                    onDismiss = { speedDialExpanded = false },
                    onSyncCalendar = {
                        visibleTabs.indexOf(BoopTab.CALENDAR).takeIf { it >= 0 }?.let { selectTabIndex(it) }
                        calendarSyncRequest++
                        speedDialExpanded = false
                    },
                    onOpenTask = { openTaskSheet(null) },
                    onOpenEvent = { openEventSheet(startAt = calendarCreateAtMillis) },
                    onOpenExternalCalendar = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.formula1.com/")))
                        } catch (_: Throwable) {
                        }
                        speedDialExpanded = false
                    },
                    onOpenNote = { openNoteSheet(null) },
                    onOpenHabit = { openHabitSheet(null) },
                    onOpenIncome = { openFinanceEntrySheet("income") },
                    onOpenExpense = { openFinanceEntrySheet("expense") },
                    onOpenTransfer = { openFinanceEntrySheet("transfer") },
                    onOpenAccount = { openAccountSheet(null) },
                    onExpandedChange = { speedDialExpanded = it },
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
private fun BoopPagerPage(
    tab: BoopTab,
    visibleTabs: List<BoopTab>,
    tasks: List<BoopTask>,
    notes: List<BoopNote>,
    habits: List<BoopHabit>,
    accounts: List<BoopAccount>,
    ledgerEntries: List<BoopLedgerEntry>,
    calendarSyncRequest: Int,
    onPersistHabit: (BoopHabit) -> Unit,
    onSelectTab: (BoopTab) -> Unit,
    onEditTask: (BoopTask) -> Unit,
    onEditEvent: (Long) -> Unit,
    onEditNote: (BoopNote) -> Unit,
    onArchiveTask: (BoopTask) -> Unit,
    onCompleteTask: (BoopTask) -> Unit,
    onUnarchiveTask: (BoopTask) -> Unit,
    onRestoreCompletedTask: (BoopTask) -> Unit,
    onCalendarSelectedDayChanged: (Long) -> Unit,
    onEditHabit: (BoopHabit) -> Unit,
    onOpenHabitCheckIn: () -> Unit,
    onDeleteAccount: (String) -> Unit,
    onSaveLedgerEntry: (BoopLedgerEntry) -> Unit,
    onEditLedgerEntry: (BoopLedgerEntry) -> Unit,
    onDeleteLedgerEntry: (String) -> Unit,
    onEditAccount: (BoopAccount) -> Unit,
    onOpenSettings: () -> Unit,
    dashboardSearchOpen: Boolean,
    onDashboardSearchOpenChange: (Boolean) -> Unit,
    tasksTabShowNotes: Boolean,
    onTasksTabShowNotesChange: (Boolean) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        when (tab) {
            BoopTab.HOME -> DashboardScreen(
                tasks = tasks,
                notes = notes,
                habits = habits,
                accounts = accounts,
                ledgerEntries = ledgerEntries,
                onOpenTask = onEditTask,
                onOpenNote = onEditNote,
                onOpenHabit = onEditHabit,
                onOpenHabitCheckIn = onOpenHabitCheckIn,
                onSearchPickTask = { onSelectTab(BoopTab.TASKS); onEditTask(it) },
                onSearchPickNote = { onSelectTab(BoopTab.TASKS); onEditNote(it) },
                onSearchPickHabit = {
                    if (visibleTabs.contains(BoopTab.HABITS)) {
                        onSelectTab(BoopTab.HABITS)
                    }
                    onEditHabit(it)
                },
                onOpenSettings = onOpenSettings,
                searchExpanded = dashboardSearchOpen,
                onSearchExpandedChange = onDashboardSearchOpenChange,
            )
            BoopTab.TASKS -> {
                if (tasksTabShowNotes) {
                    NotesListScreen(
                        notes = notes,
                        onOpenNote = onEditNote,
                        title = "Notes",
                        onHeaderTap = { onTasksTabShowNotesChange(false) },
                    )
                } else {
                    TaskListScreen(
                        tasks = tasks,
                        onOpenTask = onEditTask,
                        onArchiveTask = onArchiveTask,
                        onCompleteTask = onCompleteTask,
                        onUnarchiveTask = onUnarchiveTask,
                        onRestoreCompletedTask = onRestoreCompletedTask,
                        title = "Tasks",
                        onHeaderTap = { onTasksTabShowNotesChange(true) },
                    )
                }
            }
            BoopTab.CALENDAR -> CalendarScreen(
                tasks = tasks,
                syncRequest = calendarSyncRequest,
                onOpenTask = onEditTask,
                onOpenEvent = onEditEvent,
                onSelectedDayChanged = onCalendarSelectedDayChanged,
            )
            BoopTab.HABITS -> HabitsListScreen(
                habits = habits,
                onPersistHabit = onPersistHabit,
                onOpenHabit = onEditHabit,
            )
            BoopTab.WALLET -> FinanceScreen(
                accounts = accounts,
                entries = ledgerEntries,
                onDeleteAccount = onDeleteAccount,
                onSaveEntry = onSaveLedgerEntry,
                onEditEntry = onEditLedgerEntry,
                onDeleteEntry = onDeleteLedgerEntry,
                onEditAccount = onEditAccount,
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    showHabitsPage: Boolean,
    onShowHabitsPageChange: (Boolean) -> Unit,
    showWalletPage: Boolean,
    onShowWalletPageChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    Column(
        Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 24.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = palette.onBackground,
                )
            }
            BoopAnimatedEnter {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.onBackground,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Appearance",
            style = MaterialTheme.typography.titleMedium,
            color = palette.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        ThemeMode.entries.forEach { mode ->
            val selected = themeMode == mode
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onThemeModeChange(mode) },
                shape = RoundedCornerShape(14.dp),
                color = if (selected) palette.surface else palette.background,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = { onThemeModeChange(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = palette.onBackground,
                            unselectedColor = palette.muted,
                        ),
                    )
                    Column(Modifier.padding(start = 4.dp)) {
                        Text(
                            mode.label,
                            style = if (selected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                            color = palette.onBackground,
                        )
                        Text(
                            when (mode) {
                                ThemeMode.DARK -> "Always use dark theme"
                                ThemeMode.LIGHT -> "Always use light theme"
                                ThemeMode.SYSTEM -> "Match your device setting"
                            },
                            color = palette.muted,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Navigation",
            style = MaterialTheme.typography.titleMedium,
            color = palette.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        SettingsToggleRow(
            title = "Habits page",
            subtitle = "Show Habits in the bottom navigation",
            checked = showHabitsPage,
            onCheckedChange = onShowHabitsPageChange,
        )
        SettingsToggleRow(
            title = "Wallet page",
            subtitle = "Show Accounts in the bottom navigation",
            checked = showWalletPage,
            onCheckedChange = onShowWalletPageChange,
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val palette = LocalBoopPalette.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = palette.onBackground)
                Text(subtitle, color = palette.muted, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = palette.accentOn,
                    checkedTrackColor = palette.accent,
                    uncheckedThumbColor = palette.muted,
                    uncheckedTrackColor = palette.surfaceVariant,
                    uncheckedBorderColor = palette.muted.copy(alpha = 0.35f),
                ),
            )
        }
    }
}

@Composable
private fun BoopLaunchReveal(
    active: Boolean,
    onFinished: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (!active) {
        content()
        return
    }

    val palette = LocalBoopPalette.current
    val density = LocalDensity.current
    var startReveal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(340)
        startReveal = true
        delay(920)
        onFinished()
    }

    val reveal by animateFloatAsState(
        targetValue = if (startReveal) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "launch_reveal",
    )
    val ringAlpha by animateFloatAsState(
        targetValue = when {
            !startReveal -> 1f
            reveal < 0.5f -> 1f - reveal * 0.35f
            else -> (0.65f - ((reveal - 0.5f) / 0.5f).coerceIn(0f, 1f) * 0.65f)
        },
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "launch_ring_alpha",
    )

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val ringRadius = with(density) { 22.dp.toPx() }
                    val maxRadius = hypot(size.width / 2f, size.height / 2f) * 1.15f
                    val holeRadius = if (startReveal) {
                        ringRadius + (maxRadius - ringRadius) * reveal
                    } else {
                        0f
                    }
                    if (holeRadius > 0.5f) {
                        clipPath(
                            Path().apply {
                                addOval(
                                    Rect(
                                        center.x - holeRadius,
                                        center.y - holeRadius,
                                        center.x + holeRadius,
                                        center.y + holeRadius,
                                    ),
                                )
                            },
                        ) {
                            this@drawWithContent.drawContent()
                        }
                    }
                },
        ) {
            content()
        }

        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val ringRadius = with(density) { 22.dp.toPx() }
            val strokePx = with(density) { 2.dp.toPx() }
            val maxRadius = hypot(size.width / 2f, size.height / 2f) * 1.15f
            val holeRadius = if (startReveal) {
                ringRadius + (maxRadius - ringRadius) * reveal
            } else {
                0f
            }

            if (holeRadius <= 0.5f) {
                drawRect(palette.background)
            } else {
                val maskPath = Path().apply {
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addOval(
                        Rect(
                            center.x - holeRadius,
                            center.y - holeRadius,
                            center.x + holeRadius,
                            center.y + holeRadius,
                        ),
                    )
                    fillType = PathFillType.EvenOdd
                }
                drawPath(maskPath, palette.background)
            }

            if (ringAlpha > 0.02f) {
                val displayRingRadius = if (startReveal) holeRadius else ringRadius
                val ringColor = androidx.compose.ui.graphics.lerp(
                    palette.accent,
                    palette.accentGlow,
                    0.42f,
                )
                drawCircle(
                    color = ringColor.copy(alpha = ringAlpha),
                    radius = displayRingRadius,
                    center = center,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }
    }
}

@Composable
private fun BoopBottomBar(
    tabs: List<BoopTab>,
    pagerScrollPosition: Float,
    currentTab: BoopTab,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddAnchorChanged: (Rect) -> Unit,
    onSelectTab: (Int) -> Unit,
    onOpenTask: () -> Unit,
    onOpenHabit: () -> Unit,
    onOpenVoiceCapture: () -> Unit,
    voiceListening: Boolean,
) {
    val palette = LocalBoopPalette.current
    if (tabs.isEmpty()) return
    val addRotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.72f),
        label = "add_icon_rotation",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(68.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = palette.surfaceElevated,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, palette.muted.copy(alpha = 0.2f)),
            ) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val distance = kotlin.math.abs(pagerScrollPosition - index)
                        val selectionProgress = (1f - distance).coerceIn(0f, 1f)
                        BoopNavTabButton(
                            selectionProgress = selectionProgress,
                            icon = tab.icon,
                            contentDescription = tab.label,
                            onClick = { onSelectTab(index) },
                        )
                    }
                }
            }
            BoopPersistentActionButton(
                onClick = onOpenVoiceCapture,
                icon = if (voiceListening) Icons.Outlined.Stop else Icons.Outlined.Mic,
                contentDescription = if (voiceListening) "Stop recording" else "Voice capture",
                filled = !voiceListening,
                listening = voiceListening,
            )
            Box(
                Modifier
                    .size(44.dp)
                    .onGloballyPositioned { coordinates ->
                        if (!expanded) {
                            onAddAnchorChanged(coordinates.boundsInWindow())
                        }
                    },
            ) {
                BoopPersistentActionButton(
                    onClick = {
                        if (expanded) {
                            onExpandedChange(false)
                            return@BoopPersistentActionButton
                        }
                        when (currentTab) {
                            BoopTab.HOME -> onExpandedChange(true)
                            BoopTab.TASKS -> onOpenTask()
                            BoopTab.CALENDAR -> onExpandedChange(true)
                            BoopTab.HABITS -> onOpenHabit()
                            BoopTab.WALLET -> onExpandedChange(true)
                        }
                    },
                    icon = Icons.Outlined.Add,
                    contentDescription = if (expanded) "Close" else "Add",
                    filled = true,
                    iconRotation = addRotation,
                    modifier = Modifier.pointerInput(currentTab, expanded) {
                        if (currentTab != BoopTab.HOME && !expanded) {
                            detectTapGestures(onLongPress = { onExpandedChange(true) })
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun BoopSpeedDialOverlay(
    expanded: Boolean,
    anchorBounds: Rect,
    currentTab: BoopTab,
    showHabitsPage: Boolean,
    showWalletPage: Boolean,
    onDismiss: () -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onSyncCalendar: () -> Unit,
    onOpenTask: () -> Unit,
    onOpenEvent: () -> Unit,
    onOpenExternalCalendar: () -> Unit,
    onOpenNote: () -> Unit,
    onOpenHabit: () -> Unit,
    onOpenIncome: () -> Unit,
    onOpenExpense: () -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenAccount: () -> Unit,
) {
    var keepMounted by remember { mutableStateOf(false) }
    LaunchedEffect(expanded) {
        if (expanded) {
            keepMounted = true
        } else {
            delay(280)
            keepMounted = false
        }
    }
    if (!keepMounted || anchorBounds.isEmpty) return

    var menuRevealed by remember { mutableStateOf(false) }
    LaunchedEffect(expanded) {
        if (expanded) {
            menuRevealed = false
            delay(16)
            menuRevealed = true
        } else {
            menuRevealed = false
        }
    }

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val palette = LocalBoopPalette.current
    val bottomBarClearance = 108.dp
    val menuOpen = expanded && menuRevealed
    val menuAlpha by animateFloatAsState(
        targetValue = if (menuOpen) 1f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "speed_dial_popup_alpha",
    )
    val menuScale by animateFloatAsState(
        targetValue = if (menuOpen) 1f else 0.86f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.78f),
        label = "speed_dial_popup_scale",
    )
    val anchorLeft = anchorBounds.left
    val anchorTop = anchorBounds.top
    val anchorWidth = anchorBounds.width
    val scrimHeight = (configuration.screenHeightDp.dp - bottomBarClearance).coerceAtLeast(0.dp)

    if (expanded && menuAlpha > 0.01f) {
        Popup(
            alignment = Alignment.TopStart,
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            Box(
                Modifier
                    .size(configuration.screenWidthDp.dp, scrimHeight)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss,
                    ),
            )
        }
    }

    if (menuAlpha > 0.01f) {
        Popup(
            onDismissRequest = onDismiss,
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize,
                ): IntOffset {
                    val gapPx = with(density) { 10.dp.roundToPx() }
                    val x = anchorLeft.toInt() + ((anchorWidth - popupContentSize.width) / 2f).toInt()
                    val y = anchorTop.toInt() - popupContentSize.height - gapPx
                    return IntOffset(x, y.coerceAtLeast(gapPx))
                }
            },
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = palette.surfaceElevated.copy(alpha = 0.98f),
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, palette.muted.copy(alpha = 0.18f)),
                modifier = Modifier.graphicsLayer {
                    alpha = menuAlpha
                    scaleX = menuScale
                    scaleY = menuScale
                    transformOrigin = TransformOrigin(0.5f, 1f)
                },
            ) {
                Column(
                    Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BoopSpeedDialMenu(
                        menuVisible = menuOpen,
                        currentTab = currentTab,
                        showHabitsPage = showHabitsPage,
                        showWalletPage = showWalletPage,
                        onExpandedChange = onExpandedChange,
                        onSyncCalendar = onSyncCalendar,
                        onOpenTask = onOpenTask,
                        onOpenEvent = onOpenEvent,
                        onOpenExternalCalendar = onOpenExternalCalendar,
                        onOpenNote = onOpenNote,
                        onOpenHabit = onOpenHabit,
                        onOpenIncome = onOpenIncome,
                        onOpenExpense = onOpenExpense,
                        onOpenTransfer = onOpenTransfer,
                        onOpenAccount = onOpenAccount,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoopPersistentActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    filled: Boolean = false,
    listening: Boolean = false,
    iconRotation: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "action_press_scale",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val listeningPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "listening_pulse",
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            listening -> palette.surfaceVariant
            filled -> palette.accent
            else -> palette.surfaceElevated
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "action_bg",
    )
    val iconTint by animateColorAsState(
        targetValue = when {
            listening -> palette.recording
            filled -> palette.accentOn
            else -> palette.onBackground
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "action_icon",
    )
    val pulseScale = if (listening) listeningPulse else 1f
    Surface(
        modifier = modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = pressScale * pulseScale
                scaleY = pressScale * pulseScale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    radius = 22.dp,
                    color = palette.accent.copy(alpha = 0.35f),
                ),
                onClick = onClick,
            ),
        shape = CircleShape,
        color = bgColor,
        shadowElevation = when {
            filled || listening -> 6.dp
            else -> 2.dp
        },
        border = when {
            filled -> BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.38f))
            listening -> BorderStroke(1.dp, palette.recording.copy(alpha = 0.35f))
            else -> BorderStroke(1.dp, palette.muted.copy(alpha = 0.2f))
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier
                    .size(21.dp)
                    .graphicsLayer { rotationZ = iconRotation },
            )
        }
    }
}

@Composable
private fun BoopSpeedDialMenu(
    menuVisible: Boolean,
    currentTab: BoopTab,
    showHabitsPage: Boolean,
    showWalletPage: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSyncCalendar: () -> Unit,
    onOpenTask: () -> Unit,
    onOpenEvent: () -> Unit,
    onOpenExternalCalendar: () -> Unit,
    onOpenNote: () -> Unit,
    onOpenHabit: () -> Unit,
    onOpenIncome: () -> Unit,
    onOpenExpense: () -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenAccount: () -> Unit,
) {
    data class SpeedDialEntry(
        val icon: ImageVector,
        val label: String,
        val onClick: () -> Unit,
    )
    val items = buildList {
        if (currentTab == BoopTab.CALENDAR) {
            add(SpeedDialEntry(Icons.Outlined.Notifications, "Add task") { onOpenTask(); onExpandedChange(false) })
            add(SpeedDialEntry(Icons.Outlined.CalendarMonth, "Add event") { onOpenEvent(); onExpandedChange(false) })
            add(SpeedDialEntry(Icons.Outlined.Sync, "Sync calendar") { onSyncCalendar(); onExpandedChange(false) })
            add(SpeedDialEntry(Icons.Outlined.Link, "Add external calendar") { onOpenExternalCalendar(); onExpandedChange(false) })
        } else if (currentTab == BoopTab.WALLET) {
            add(SpeedDialEntry(Icons.Outlined.Add, "Add account") { onOpenAccount(); onExpandedChange(false) })
            add(SpeedDialEntry(Icons.Outlined.AttachMoney, "Add income") { onOpenIncome(); onExpandedChange(false) })
            add(SpeedDialEntry(Icons.Outlined.EditNote, "Add expense") { onOpenExpense(); onExpandedChange(false) })
            add(SpeedDialEntry(Icons.Outlined.Sync, "Add transfer") { onOpenTransfer(); onExpandedChange(false) })
        } else {
            add(SpeedDialEntry(Icons.Outlined.Notifications, "Add task") { onOpenTask(); onExpandedChange(false) })
            add(SpeedDialEntry(Icons.Outlined.CalendarMonth, "Add event") { onOpenEvent(); onExpandedChange(false) })
            add(SpeedDialEntry(Icons.Outlined.EditNote, "Add note") { onOpenNote(); onExpandedChange(false) })
            if (showHabitsPage) {
                add(SpeedDialEntry(Icons.Outlined.AutoGraph, "Add habit") { onOpenHabit(); onExpandedChange(false) })
            }
            if (currentTab == BoopTab.HOME && showWalletPage) {
                add(SpeedDialEntry(Icons.Outlined.Add, "Add account") { onOpenAccount(); onExpandedChange(false) })
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items.forEachIndexed { index, entry ->
            BoopAnimatedSpeedDialItem(
                visible = menuVisible,
                index = index,
                icon = entry.icon,
                contentDescription = entry.label,
                onClick = entry.onClick,
            )
        }
    }
}

@Composable
private fun BoopAnimatedSpeedDialItem(
    visible: Boolean,
    index: Int,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val staggerMs = index * 45
    val itemAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            delayMillis = if (visible) staggerMs else 0,
            easing = FastOutSlowInEasing,
        ),
        label = "speed_dial_item_alpha_$index",
    )
    val itemOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 14f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = 0.8f,
        ),
        label = "speed_dial_item_offset_$index",
    )
    val itemScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.9f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = 0.78f,
        ),
        label = "speed_dial_item_scale_$index",
    )
    if (itemAlpha < 0.01f && !visible) return
    Box(
        Modifier.graphicsLayer {
            alpha = itemAlpha
            translationY = itemOffset
            scaleX = itemScale
            scaleY = itemScale
            transformOrigin = TransformOrigin(0.5f, 1f)
        },
    ) {
        BoopSpeedDialItem(
            icon = icon,
            contentDescription = contentDescription,
            onClick = onClick,
        )
    }
}

@Composable
private fun BoopSpeedDialItem(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    BoopPersistentActionButton(
        onClick = onClick,
        icon = icon,
        contentDescription = contentDescription,
        filled = false,
    )
}

@Composable
private fun BoopNavTabButton(
    selectionProgress: Float,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    val progress = selectionProgress.coerceIn(0f, 1f)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f + progress * 0.06f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "nav_tab_press_scale",
    )
    val bgColor = androidx.compose.ui.graphics.lerp(
        palette.surfaceVariant,
        androidx.compose.ui.graphics.lerp(palette.accent, palette.accentGlow, 0.22f),
        progress,
    )
    val iconTint = androidx.compose.ui.graphics.lerp(
        palette.muted,
        palette.accentOn,
        progress,
    )
    val elevation = 2f + progress * 3f
    Surface(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    radius = 22.dp,
                    color = palette.accentGlow.copy(alpha = if (progress > 0.5f) 0.32f else 0.4f),
                ),
                onClick = onClick,
            ),
        shape = CircleShape,
        color = bgColor,
        shadowElevation = elevation.dp,
        border = if (progress > 0.5f) {
            BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.28f))
        } else {
            BorderStroke(1.dp, palette.muted.copy(alpha = 0.24f))
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(21.dp),
            )
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

@Composable
private fun DashboardHabitsSectionHeader(onOpenWeekView: () -> Unit) {
    val palette = LocalBoopPalette.current
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
                    .background(palette.accent),
            )
            Column {
                Text("Your habits", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "Open week view",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        BoopHeaderIconButton(
            onClick = onOpenWeekView,
            icon = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = "Habits week view",
            iconTint = palette.accent,
        )
    }
}

@Composable
private fun DashboardSectionLabel(
    title: String,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
) {
    val palette = LocalBoopPalette.current
    BoopAnimatedEnter(key = title, animated = animated, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.accent),
            )
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun DashboardCompactSection(
    title: String,
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    val palette = LocalBoopPalette.current
    val interaction = remember(title) { MutableInteractionSource() }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onToggle,
            ),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    Text(summary, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                val chevronRotation by animateFloatAsState(
                    targetValue = if (expanded) 90f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "section_chevron",
                )
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = palette.muted,
                    modifier = Modifier.graphicsLayer { rotationZ = chevronRotation },
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                    expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(tween(160)) + shrinkVertically(animationSpec = tween(180)),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, palette.muted.copy(alpha = 0.12f)),
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = palette.muted)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = palette.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (caption.isNotBlank()) {
                Text(caption, style = MaterialTheme.typography.labelSmall, color = palette.muted)
            }
        }
    }
}

@Composable
private fun DashboardHabitChip(
    habit: BoopHabit,
    onOpenHabit: (BoopHabit) -> Unit,
) {
    val palette = LocalBoopPalette.current
    val todayKey = todayHabitDayKey()
    val doneToday = if (habit.quantityMode) {
        val todayAmount = parseHabitDayValues(habit.quantityDayValues)[todayKey] ?: 0
        todayAmount >= habit.quantityDailyTarget.coerceAtLeast(1)
    } else {
        todayKey in parseHabitDayKeys(habit.dayKeys)
    }
    val interaction = remember(habit.id) { MutableInteractionSource() }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (doneToday) palette.accent.copy(alpha = 0.14f) else palette.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (doneToday) palette.accent.copy(alpha = 0.45f) else palette.muted.copy(alpha = 0.16f),
        ),
        modifier = Modifier.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = { onOpenHabit(habit) },
        ),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (doneToday) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = palette.accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                habit.title,
                style = MaterialTheme.typography.labelMedium,
                color = palette.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DashboardScreen(
    tasks: List<BoopTask>,
    notes: List<BoopNote>,
    habits: List<BoopHabit>,
    accounts: List<BoopAccount>,
    ledgerEntries: List<BoopLedgerEntry>,
    onOpenTask: (BoopTask) -> Unit,
    onOpenNote: (BoopNote) -> Unit,
    onOpenHabit: (BoopHabit) -> Unit,
    onOpenHabitCheckIn: () -> Unit,
    onSearchPickTask: (BoopTask) -> Unit,
    onSearchPickNote: (BoopNote) -> Unit,
    onSearchPickHabit: (BoopHabit) -> Unit,
    onOpenSettings: () -> Unit,
    searchExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit,
) {
    val palette = LocalBoopPalette.current
    val scroll = rememberScrollState()
    val searchScroll = rememberScrollState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchFocus = remember { FocusRequester() }
    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            delay(48)
            searchFocus.requestFocus()
        }
    }
    val now = System.currentTimeMillis()
    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val endOfToday = startOfToday + 86_400_000L
    val horizon = now + 86_400_000L
    val tasksDueToday = tasks
        .filter { !it.done && !it.archived && it.reminderAt in startOfToday until endOfToday }
        .sortedBy { it.reminderAt }
    val upcomingTasks = tasks
        .filter { !it.done && !it.archived && it.reminderAt in now..horizon }
        .sortedBy { it.reminderAt }
    val recentNotes = notes
        .filter { !it.archived }
        .sortedByDescending { it.createdAtMillis + it.updatedAtMillis }
        .take(6)
    val epoch = LocalBoopDataEpoch.current
    val accountBalances = remember(epoch) {
        accounts.associate { it.id to 0.0 }.toMutableMap().apply {
            ledgerEntries.forEach { entry ->
                when (entry.type) {
                    "income" -> this[entry.accountId] = (this[entry.accountId] ?: 0.0) + entry.amount
                    "expense" -> this[entry.accountId] = (this[entry.accountId] ?: 0.0) - entry.amount
                    "transfer" -> {
                        this[entry.accountId] = (this[entry.accountId] ?: 0.0) - entry.amount
                        entry.toAccountId?.let { toId -> this[toId] = (this[toId] ?: 0.0) + entry.amount }
                    }
                }
            }
        }
    }
    val netBalance = accountBalances.values.sum()
    val activeHabits = habits.sortedBy { it.title.lowercase(Locale.getDefault()) }.take(12)
    val todayKey = todayHabitDayKey()
    val habitsDoneToday = activeHabits.count { habit ->
        if (habit.quantityMode) {
            val todayAmount = parseHabitDayValues(habit.quantityDayValues)[todayKey] ?: 0
            todayAmount >= habit.quantityDailyTarget.coerceAtLeast(1)
        } else {
            todayKey in parseHabitDayKeys(habit.dayKeys)
        }
    }
    val greetingLine = run {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            h < 12 -> "Good morning"
            h < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val dateLine = SimpleDateFormat("EEEE, MMMM d", Locale.US).format(now)
    var greetingVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { greetingVisible = true }
    val quotes = remember {
        listOf(
            "Hope is a discipline. Keep showing up." to null,
            "The system may be broken; your next step still matters." to null,
            "Progress is rarely loud, but it is always real." to null,
            "Small consistency beats dramatic intention." to null,
            "You do not need certainty to start." to null,
            "Even in a bad timeline, meaning is handcrafted." to null,
            "First, solve the problem. Then, write the code." to "John Johnson",
            "Success is the sum of small efforts, repeated day in and day out." to "Robert Collier",
        )
    }
    val quoteOfLaunch = remember { quotes.random() }
    Box(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp, bottom = 12.dp),
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        AnimatedVisibility(
                            visible = greetingVisible,
                            enter = fadeIn(tween(420, easing = FastOutSlowInEasing)) +
                                slideInVertically(
                                    initialOffsetY = { it / 5 },
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                ),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = greetingLine,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = dateLine,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BoopHeaderIconButton(
                            onClick = { onSearchExpandedChange(true) },
                            icon = Icons.Outlined.Search,
                            contentDescription = "Search",
                            filled = true,
                        )
                        BoopHeaderIconButton(
                            onClick = onOpenSettings,
                            icon = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = palette.quoteFill,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.5.dp, palette.quoteStroke),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    BoopAnimatedEnter {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Text(
                                text = "\"${quoteOfLaunch.first}\"",
                                color = palette.muted,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = BoopSerifFamily,
                                    fontStyle = FontStyle.Italic,
                                ),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "— ${quoteOfLaunch.second ?: "Unknown"}",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
                BoopAnimatedEnter {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        DashboardStatCard(
                            label = "Today",
                            value = tasksDueToday.size.toString(),
                            caption = if (tasksDueToday.size == 1) "task due" else "tasks due",
                            modifier = Modifier.weight(1f),
                        )
                        DashboardStatCard(
                            label = "Habits",
                            value = if (activeHabits.isEmpty()) "—" else "$habitsDoneToday/${activeHabits.size}",
                            caption = "checked in",
                            modifier = Modifier.weight(1f),
                        )
                        DashboardStatCard(
                            label = if (accounts.isNotEmpty()) "Balance" else "Notes",
                            value = if (accounts.isNotEmpty()) {
                                formatCadAmountNumber(netBalance, decimals = 0)
                            } else {
                                notes.count { !it.archived }.toString()
                            },
                            caption = if (accounts.isNotEmpty()) "CAD" else "active",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                DashboardSectionLabel("Up next")
                if (upcomingTasks.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = palette.surfaceVariant,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "Nothing scheduled in the next 24 hours.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            )
                        }
                    } else {
                        upcomingTasks.take(3).forEach { task ->
                            val taskInteraction = remember(task.id) { MutableInteractionSource() }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                border = BorderStroke(1.dp, palette.muted.copy(alpha = 0.12f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = taskInteraction,
                                        indication = null,
                                    ) { onOpenTask(task) },
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            task.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            formatTaskReminderLine(task.reminderAt),
                                            color = palette.muted,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    Icon(
                                        Icons.Outlined.Notifications,
                                        contentDescription = null,
                                        tint = palette.accent.copy(alpha = 0.85f),
                                    )
                                }
                            }
                        }
                    }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DashboardSectionLabel("Today's habits", modifier = Modifier.weight(1f))
                    BoopAccentTextButton(label = "Week view", onClick = onOpenHabitCheckIn)
                }
                if (activeHabits.isEmpty()) {
                    Text(
                        "No habits yet — add one from the + menu.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        activeHabits.forEach { habit ->
                            DashboardHabitChip(habit = habit, onOpenHabit = onOpenHabit)
                        }
                    }
                }
                DashboardSectionLabel("Recent notes")
                if (recentNotes.isEmpty()) {
                    Text(
                        "No notes yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        val oddCount = recentNotes.size % 2 == 1
                        if (oddCount) {
                            DashboardNoteTile(
                                note = recentNotes.first(),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onOpenNote(recentNotes.first()) },
                                featured = true,
                            )
                        }
                        val gridNotes = if (oddCount) recentNotes.drop(1) else recentNotes
                        gridNotes.chunked(2).forEach { rowNotes ->
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
                                if (rowNotes.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
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
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = BoopSansFamily,
                            color = palette.onBackground,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocus)
                            .shadow(3.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        placeholder = {
                            Text(
                                "Search tasks, notes, habits…",
                                color = palette.muted,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = palette.muted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = palette.inputField,
                            unfocusedContainerColor = palette.surfaceVariant,
                            focusedTextColor = palette.onBackground,
                            unfocusedTextColor = palette.onBackground,
                            cursorColor = palette.accent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    )
                    BoopHeaderIconButton(
                        onClick = {
                            onSearchExpandedChange(false)
                            searchQuery = ""
                        },
                        icon = Icons.Outlined.Close,
                        contentDescription = "Close search",
                    )
                }
                Text(
                    "Text inside note images is not searched.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                )
                GlobalSearchResultsInline(
                    query = searchQuery,
                    tasks = tasks,
                    notes = notes,
                    habits = habits,
                    onPickTask = {
                        onSearchExpandedChange(false)
                        searchQuery = ""
                        onSearchPickTask(it)
                    },
                    onPickNote = {
                        onSearchExpandedChange(false)
                        searchQuery = ""
                        onSearchPickNote(it)
                    },
                    onPickHabit = {
                        onSearchExpandedChange(false)
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
    val interaction = remember(habit.id) { MutableInteractionSource() }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null,
            ) { onOpenHabit(habit) },
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
                    "${habit.title} · ${habitCategoryLabel(habit.dayPeriodCategory)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
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
private fun DashboardNoteTile(
    note: BoopNote,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    onClick: () -> Unit,
) {
    val snippet = remember(note.body) { plainNoteSnippet(note.body, if (featured) 120 else 72) }
    val interaction = remember(note.id) { MutableInteractionSource() }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .heightIn(min = if (featured) 100.dp else 88.dp, max = if (featured) 140.dp else 120.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Column(
            Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                note.title.ifBlank { "Untitled" },
                style = if (featured) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = if (featured) 2 else 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                snippet.ifBlank { " " },
                color = Color(0xFFBFBFBF),
                style = MaterialTheme.typography.bodySmall,
                maxLines = if (featured) 4 else 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                formatNoteCardTime(note),
                color = Color(0xFF8E8E90),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
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
    val palette = LocalBoopPalette.current
    TextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = LocalTextStyle.current.copy(
            fontFamily = BoopSansFamily,
            color = palette.onBackground,
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.16f),
            ),
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = palette.inputField,
            unfocusedContainerColor = palette.surfaceVariant,
            cursorColor = palette.accent,
            focusedTextColor = palette.onBackground,
            unfocusedTextColor = palette.onBackground,
            focusedLabelColor = palette.muted,
            unfocusedLabelColor = palette.muted,
            focusedPlaceholderColor = palette.muted,
            unfocusedPlaceholderColor = palette.muted,
        ),
        label = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodySmall.copy(fontFamily = BoopSansFamily),
            ) {
                label()
            }
        },
        placeholder = placeholder?.let { ph ->
            {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(fontFamily = BoopSansFamily),
                ) {
                    ph()
                }
            }
        },
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

private val bulletLeadPattern = Regex("^\\s*•\\s+")

private fun noteEditInsertBulletLine(editText: EditText?) {
    val et = editText ?: return
    val text = et.text as? Editable ?: return
    val len = text.length
    val s = minOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    val e = maxOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    if (e > s) {
        val selected = text.substring(s, e)
        val lines = selected.split('\n')
        val nonBlank = lines.filter { it.isNotBlank() }
        val stripAll = nonBlank.isNotEmpty() && nonBlank.all { bulletLeadPattern.containsMatchIn(it) }
        val replaced = lines.joinToString("\n") { line ->
            when {
                line.trim().isBlank() -> line
                stripAll -> line.replaceFirst(bulletLeadPattern, "")
                else -> "• ${line.trimStart()}"
            }
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

private val numberedLeadPattern = Regex("^\\s*\\d+\\.\\s+")

private fun noteEditInsertNumberedLine(editText: EditText?) {
    val et = editText ?: return
    val text = et.text as? Editable ?: return
    val len = text.length
    val s = minOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    val e = maxOf(et.selectionStart, et.selectionEnd).coerceIn(0, len)
    if (e > s) {
        val selected = text.substring(s, e)
        val lines = selected.split('\n')
        val nonBlank = lines.filter { it.isNotBlank() }
        val stripAll = nonBlank.isNotEmpty() && nonBlank.all { numberedLeadPattern.containsMatchIn(it) }
        val replaced = if (stripAll) {
            lines.joinToString("\n") { line ->
                when {
                    line.trim().isBlank() -> line
                    else -> line.replaceFirst(numberedLeadPattern, "")
                }
            }
        } else {
            var idx = 1
            lines.joinToString("\n") { line ->
                if (line.trim().isBlank()) line else "${idx++}. ${line.trimStart()}"
            }
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
            Icon(Icons.Outlined.FormatBold, contentDescription = "Bold", tint = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = { noteEditApplySpan(editText, StyleSpan(Typeface.ITALIC)) }) {
            Icon(Icons.Outlined.FormatItalic, contentDescription = "Italic", tint = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = { noteEditApplySpan(editText, UnderlineSpan()) }) {
            Text("U", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = { noteEditInsertBulletLine(editText) }) {
            Text("•", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(onClick = { noteEditInsertNumberedLine(editText) }) {
            Text("1.", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
        }
        TextButton(onClick = { noteEditApplySpan(editText, AbsoluteSizeSpan(noteEditSpToPx(22f, context), true)) }) {
            Text("H1", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        TextButton(onClick = { noteEditApplySpan(editText, AbsoluteSizeSpan(noteEditSpToPx(18f, context), true)) }) {
            Text("H2", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
        }
        TextButton(onClick = { noteEditApplySpan(editText, AbsoluteSizeSpan(noteEditSpToPx(15f, context), true)) }) {
            Text("H3", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
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
                applyBoopSans()
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

private fun startOfDayMillis(timeMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
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

private fun startNoteAudioRecording(
    context: Context,
    onStarted: (MediaRecorder, String, Long) -> Unit,
    onFailed: (String) -> Unit,
) {
    val out = createNoteAudioFile(context, UUID.randomUUID().toString())
    var recorder: MediaRecorder? = null
    try {
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder = r
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioSamplingRate(44_100)
        r.setAudioEncodingBitRate(128_000)
        r.setOutputFile(out.absolutePath)
        r.prepare()
        r.start()
        onStarted(r, out.absolutePath, System.currentTimeMillis())
    } catch (t: Throwable) {
        try {
            recorder?.reset()
        } catch (_: Throwable) {
        }
        try {
            recorder?.release()
        } catch (_: Throwable) {
        }
        if (out.exists() && out.length() == 0L) {
            out.delete()
        }
        onFailed(t.message ?: "recording unavailable")
    }
}

data class CalendarEventUi(
    val id: Long,
    val title: String,
    val beginMillis: Long,
    val endMillis: Long,
    val calendarDisplayName: String,
    val allDay: Boolean,
    val repeatEveryDays: Int = 0,
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
    return if (googleIds.isNotEmpty()) googleIds else fallbackVisibleIds
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
    val calendarIds = readGoogleCalendarIds(context)
    if (calendarIds.isEmpty()) return emptyList()

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
    val selection = "${CalendarContract.Instances.CALENDAR_ID} IN (${calendarIds.joinToString(",")})"
    val out = mutableListOf<CalendarEventUi>()
    context.contentResolver.query(uri, projection, selection, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { c ->
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
                    repeatEveryDays = 0,
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
        CalendarContract.Events.RRULE,
        CalendarContract.Events.CALENDAR_ID,
        CalendarContract.Events.ALL_DAY,
    )
    val eventSel = "(${CalendarContract.Events.DTSTART} < ?) AND (${CalendarContract.Events.DTEND} > ?) AND (${CalendarContract.Events.CALENDAR_ID} IN (${calendarIds.joinToString(",")}))"
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
        val rruleIx = c.getColumnIndex(CalendarContract.Events.RRULE)
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
                    repeatEveryDays = parseRepeatDaysFromRRule(if (rruleIx >= 0) c.getString(rruleIx).orEmpty() else ""),
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
    onArchiveTask: (BoopTask) -> Unit,
    onCompleteTask: (BoopTask) -> Unit,
    onUnarchiveTask: (BoopTask) -> Unit,
    onRestoreCompletedTask: (BoopTask) -> Unit,
    title: String = "Tasks",
    onHeaderTap: (() -> Unit)? = null,
) {
    val palette = LocalBoopPalette.current
    val scope = rememberCoroutineScope()
    val activeTasks = tasks.filter { !it.done && !it.archived }.sortedBy { it.reminderAt }
    val archivedTasks = tasks.filter { it.archived }.sortedByDescending { it.reminderAt }
    val completedTasks = tasks.filter { it.done && !it.archived }.sortedByDescending { it.reminderAt }
    var showArchive by rememberSaveable { mutableStateOf(false) }
    var showCompleted by rememberSaveable { mutableStateOf(false) }
    var pendingArchiveTaskId by remember { mutableStateOf<String?>(null) }
    var pendingUnarchiveTaskId by remember { mutableStateOf<String?>(null) }
    var pendingRestoreTaskId by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            BoopPageTitle(
                title,
                modifier = if (onHeaderTap != null) Modifier.clickable { onHeaderTap() } else Modifier,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                BoopHeaderIconButton(
                    onClick = { showCompleted = true },
                    icon = Icons.Outlined.CheckCircle,
                    contentDescription = "Completed tasks",
                    iconTint = palette.accent,
                )
                BoopHeaderIconButton(
                    onClick = { showArchive = true },
                    icon = Icons.Outlined.Archive,
                    contentDescription = "Archived tasks",
                )
            }
        }
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (activeTasks.isEmpty()) {
                item {
                    Text("No active tasks.", color = Color(0xFF8E8E90), style = MaterialTheme.typography.bodyMedium)
                }
            }
            items(activeTasks, key = { it.id }) { task ->
                val isCompleting = pendingArchiveTaskId == task.id
                val cardScale by animateFloatAsState(
                    targetValue = if (isCompleting) 0.9f else 1f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                    label = "task_complete_scale",
                )
                val cardAlpha by animateFloatAsState(
                    targetValue = if (isCompleting) 0f else 1f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                    label = "task_complete_alpha",
                )
                val cardOffsetX by animateFloatAsState(
                    targetValue = if (isCompleting) 72f else 0f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                    label = "task_complete_offset",
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                            alpha = cardAlpha
                            translationX = cardOffsetX
                        },
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            Modifier
                                .weight(1f)
                                .clickable { onOpenTask(task) }
                                .padding(14.dp),
                        ) {
                            Text(task.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(formatTaskReminderLine(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodyMedium)
                                if (task.repeatEveryDays > 0) {
                                    Icon(
                                        Icons.Outlined.Repeat,
                                        contentDescription = "Repeating task",
                                        tint = Color(0xFF9BC4FF),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                if (!task.linkedNoteId.isNullOrBlank()) {
                                    Icon(
                                        Icons.Outlined.EditNote,
                                        contentDescription = "Linked note",
                                        tint = Color(0xFFB3D5FF),
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                            }
                        }
                        BoopTaskCompleteToggle(
                            enabled = !isCompleting,
                            active = isCompleting,
                            onComplete = {
                                if (pendingArchiveTaskId == null) {
                                    pendingArchiveTaskId = task.id
                                    scope.launch {
                                        delay(460)
                                        onCompleteTask(task)
                                        pendingArchiveTaskId = null
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 10.dp),
                        )
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
            containerColor = palette.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = palette.muted) },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Archived tasks", style = MaterialTheme.typography.titleLarge, color = palette.onBackground)
                if (archivedTasks.isEmpty()) {
                    Text("No archived tasks yet.", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(archivedTasks, key = { it.id }) { task ->
                            val isRestoringArchive = pendingUnarchiveTaskId == task.id
                            val rowScale by animateFloatAsState(
                                targetValue = if (isRestoringArchive) 0.96f else 1f,
                                animationSpec = tween(durationMillis = 180),
                                label = "task_unarchive_scale",
                            )
                            val rowAlpha by animateFloatAsState(
                                targetValue = if (isRestoringArchive) 0.45f else 1f,
                                animationSpec = tween(durationMillis = 180),
                                label = "task_unarchive_alpha",
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = rowScale
                                        scaleY = rowScale
                                        alpha = rowAlpha
                                    },
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(
                                        Modifier
                                            .weight(1f)
                                            .clickable {
                                                showArchive = false
                                                onOpenTask(task)
                                            }
                                            .padding(12.dp),
                                    ) {
                                        Text(task.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                                        Text(formatTaskReminderLine(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(
                                        enabled = !isRestoringArchive,
                                        onClick = {
                                            if (pendingUnarchiveTaskId != null) return@IconButton
                                            pendingUnarchiveTaskId = task.id
                                            scope.launch {
                                                delay(180)
                                                onUnarchiveTask(task)
                                                pendingUnarchiveTaskId = null
                                            }
                                        },
                                    ) {
                                        Icon(Icons.Outlined.Unarchive, contentDescription = "Restore task", tint = palette.accent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showCompleted) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCompleted = false },
            sheetState = sheetState,
            containerColor = palette.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = palette.muted) },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Completed tasks", style = MaterialTheme.typography.titleLarge, color = palette.onBackground)
                if (completedTasks.isEmpty()) {
                    Text("No completed tasks yet.", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(completedTasks, key = { it.id }) { task ->
                            val isRestoringDone = pendingRestoreTaskId == task.id
                            val rowScale by animateFloatAsState(
                                targetValue = if (isRestoringDone) 0.96f else 1f,
                                animationSpec = tween(durationMillis = 180),
                                label = "task_restore_scale",
                            )
                            val rowAlpha by animateFloatAsState(
                                targetValue = if (isRestoringDone) 0.45f else 1f,
                                animationSpec = tween(durationMillis = 180),
                                label = "task_restore_alpha",
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = rowScale
                                        scaleY = rowScale
                                        alpha = rowAlpha
                                    },
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(
                                        Modifier
                                            .weight(1f)
                                            .clickable {
                                                showCompleted = false
                                                onOpenTask(task)
                                            }
                                            .padding(12.dp),
                                    ) {
                                        Text(task.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                                        Text(formatTaskReminderLine(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                                    }
                                    IconButton(
                                        enabled = !isRestoringDone,
                                        onClick = {
                                            if (pendingRestoreTaskId != null) return@IconButton
                                            pendingRestoreTaskId = task.id
                                            scope.launch {
                                                delay(180)
                                                onRestoreCompletedTask(task)
                                                pendingRestoreTaskId = null
                                            }
                                        },
                                    ) {
                                        Icon(Icons.Outlined.Unarchive, contentDescription = "Mark not completed", tint = palette.accent)
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
private fun CalendarScreen(
    tasks: List<BoopTask>,
    syncRequest: Int,
    onOpenTask: (BoopTask) -> Unit,
    onOpenEvent: (Long) -> Unit,
    onSelectedDayChanged: (Long) -> Unit,
) {
    val palette = LocalBoopPalette.current
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
    LaunchedEffect(selectedDay.timeInMillis) {
        onSelectedDayChanged(selectedDay.timeInMillis)
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
    val epoch = LocalBoopDataEpoch.current
    val dayTasks = remember(epoch, selectedDay.timeInMillis, nextDay.timeInMillis) {
        tasks.filter { !it.done && !it.archived && it.reminderAt >= selectedDay.timeInMillis && it.reminderAt < nextDay.timeInMillis }
            .sortedBy { it.reminderAt }
    }
    val headerLabel = remember(selectedMillis) { SimpleDateFormat("EEE, MMM dd", Locale.US).format(selectedMillis) }
    val syncRangeStart = remember(selectedMillis) {
        Calendar.getInstance().apply {
            timeInMillis = selectedMillis
            add(Calendar.DAY_OF_MONTH, -120)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val syncRangeEnd = remember(selectedMillis) {
        Calendar.getInstance().apply {
            timeInMillis = selectedMillis
            add(Calendar.DAY_OF_MONTH, 120)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    var syncSucceeded by remember { mutableStateOf(false) }
    var googleEventsCache by remember { mutableStateOf(emptyList<CalendarEventUi>()) }
    var isSyncing by remember { mutableStateOf(false) }
    LaunchedEffect(syncSucceeded) {
        if (syncSucceeded) {
            delay(2_500)
            syncSucceeded = false
        }
    }
    var calendarGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    fun refreshCalendarPermission() {
        calendarGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED
    }
    val refreshGoogleEvents: suspend (Boolean) -> Int = { updateStatus ->
        if (!calendarGranted) {
            0
        } else {
            isSyncing = true
            try {
                val events = withContext(Dispatchers.IO) {
                    readGoogleCalendarEventsInRange(context, syncRangeStart, syncRangeEnd)
                }
                googleEventsCache = events
                EventReminderScheduler.scheduleFromVisibleEvents(context, events)
                if (updateStatus) {
                    syncSucceeded = true
                }
                events.size
            } catch (t: Throwable) {
                if (updateStatus) {
                    Toast.makeText(
                        context,
                        "Calendar sync failed: ${t.message ?: "unknown error"}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                0
            } finally {
                isSyncing = false
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val wasGranted = calendarGranted
                refreshCalendarPermission()
                if (!wasGranted && calendarGranted) {
                    scope.launch { refreshGoogleEvents(true) }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val calendarPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        calendarGranted = granted
        if (granted) {
            scope.launch { refreshGoogleEvents(true) }
        } else {
            Toast.makeText(context, "Calendar permission denied.", Toast.LENGTH_SHORT).show()
        }
    }
    val triggerCalendarSync: () -> Unit = {
        if (!calendarGranted) {
            calendarPermLauncher.launch(Manifest.permission.READ_CALENDAR)
        } else {
            scope.launch { refreshGoogleEvents(true) }
        }
    }
    LaunchedEffect(calendarGranted) {
        if (calendarGranted && googleEventsCache.isEmpty()) {
            refreshGoogleEvents(false)
        }
    }
    LaunchedEffect(calendarGranted, syncRangeStart, syncRangeEnd) {
        if (calendarGranted) {
            refreshGoogleEvents(false)
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
    LaunchedEffect(syncRequest) {
        if (syncRequest <= 0) return@LaunchedEffect
        triggerCalendarSync()
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
    data class TimelineEntry(
        val id: String,
        val startMillis: Long,
        val endMillis: Long,
        val title: String,
        val kindLabel: String,
        val sourceLabel: String,
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
                        kindLabel = if (event.repeatEveryDays > 0) "Repetitive event" else "One-time event",
                        sourceLabel = if (event.calendarDisplayName.isNotBlank()) event.calendarDisplayName else "Google Calendar",
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
                        kindLabel = if (task.repeatEveryDays > 0) "Repetitive task" else "One-time task",
                        sourceLabel = "BOOP task",
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

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            BoopPageTitle(
                headerLabel,
                modifier = Modifier.clickable {
                    selectedMillis = todayNoon
                    scope.launch { monthPager.animateScrollToPage(basePage) }
                },
            )
            BoopHeaderIconButton(
                onClick = triggerCalendarSync,
                icon = if (syncSucceeded) Icons.Outlined.CheckCircle else Icons.Outlined.Sync,
                contentDescription = if (syncSucceeded) "Calendar synced" else "Sync with Google Calendar",
                iconTint = palette.accent,
                loading = isSyncing,
            )
        }
        HorizontalPager(
            state = monthPager,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 20.dp,
        ) { page ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, page - basePage)
            }
            val firstDayOffset = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val cells = mutableListOf<Int>().apply {
                repeat(firstDayOffset) { add(0) }
                addAll(1..daysInMonth)
            }
            while (cells.size % 7 != 0) cells.add(0)
            val todayKey = todayHabitDayKey()
            val selectedKey = SimpleDateFormat("yyyyMMdd", Locale.US).format(selectedMillis)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, palette.muted.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                    .background(palette.surfaceElevated)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.accent,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                        Text(
                            label,
                            modifier = Modifier.weight(1f),
                            color = palette.muted,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                cells.chunked(7).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { day ->
                            if (day == 0) {
                                Spacer(Modifier.weight(1f).height(36.dp))
                            } else {
                                val dayCal = (cal.clone() as Calendar).apply {
                                    set(Calendar.DAY_OF_MONTH, day)
                                    set(Calendar.HOUR_OF_DAY, 12)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                val dayKey = habitDayKeyFormat.format(dayCal.time)
                                BoopCalendarDayCell(
                                    label = day.toString().padStart(2, '0'),
                                    isSelected = dayKey == selectedKey,
                                    isToday = dayKey == todayKey,
                                    onSelect = { selectedMillis = dayCal.timeInMillis },
                                    modifier = Modifier.weight(1f),
                                )
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
                            color = MaterialTheme.colorScheme.onBackground,
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
                .fillMaxWidth(),
            state = timelineState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(uniformBlockHeight)
                            .drawWithContent {
                                drawContent()
                                val strokePx = 2.dp.toPx()
                                val c = 14.dp.toPx()
                                val dash = if (isTask) null else PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                drawRoundRect(
                                    color = Color(0xFFADADB3),
                                    topLeft = Offset(strokePx / 2f, strokePx / 2f),
                                    size = Size(size.width - strokePx, size.height - strokePx),
                                    cornerRadius = CornerRadius((c - strokePx / 2f).coerceAtLeast(0f), (c - strokePx / 2f).coerceAtLeast(0f)),
                                    style = Stroke(width = strokePx, pathEffect = dash),
                                )
                            }
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
                                Text(item.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(item.kindLabel, color = Color(0xFFBFBFBF), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(item.sourceLabel, color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    title: String = "Notes",
    onHeaderTap: (() -> Unit)? = null,
) {
    val palette = LocalBoopPalette.current
    val activeNotes = notes.filter { !it.archived }.sortedByDescending { it.createdAtMillis + it.updatedAtMillis }
    val archivedNotes = notes.filter { it.archived }.sortedByDescending { it.createdAtMillis + it.updatedAtMillis }
    var showArchive by rememberSaveable { mutableStateOf(false) }
    var selectedTag by rememberSaveable { mutableStateOf("All") }
    val availableTags = activeNotes.flatMap { parseNoteTags(it.tagsCsv) }.distinctBy { it.lowercase(Locale.getDefault()) }
    val visibleNotes = if (selectedTag == "All") activeNotes else activeNotes.filter { n ->
        parseNoteTags(n.tagsCsv).any { it.equals(selectedTag, ignoreCase = true) }
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            BoopPageTitle(
                title,
                modifier = if (onHeaderTap != null) Modifier.clickable { onHeaderTap() } else Modifier,
            )
            BoopHeaderIconButton(
                onClick = { showArchive = true },
                icon = Icons.Outlined.Archive,
                contentDescription = "Archived notes",
            )
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
                        color = if (active) Color.White else MaterialTheme.colorScheme.surfaceVariant,
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visibleNotes, key = { it.id }) { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenNote(note) },
                ) {
                    val images = parseNoteAttachments(note.attachmentUri)
                    val hasImage = images.isNotEmpty()
                    val hasAudio = !note.audioUri.isNullOrBlank()
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(note.title.ifBlank { "Untitled note" }, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            formatNoteCardTime(note),
                            color = Color(0xFF8E8E90),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
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
            containerColor = palette.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = palette.muted) },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Archived notes", style = MaterialTheme.typography.titleLarge, color = palette.onBackground)
                if (archivedNotes.isEmpty()) {
                    Text("No archived notes yet.", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(archivedNotes, key = { it.id }) { note ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showArchive = false
                                        onOpenNote(note)
                                    },
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(note.title.ifBlank { "Untitled note" }, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
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
    val epoch = LocalBoopDataEpoch.current
    val sortedHabits = remember(epoch) { habits.sortedBy { it.title.lowercase(Locale.getDefault()) } }
    val dayHabits = remember(epoch) { sortedHabits.filter { normalizeHabitCategory(it.dayPeriodCategory) == "day" } }
    val nightHabits = remember(epoch) { sortedHabits.filter { normalizeHabitCategory(it.dayPeriodCategory) == "night" } }
    var dayExpanded by rememberSaveable { mutableStateOf(true) }
    var nightExpanded by rememberSaveable { mutableStateOf(true) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BoopPageTitle("Habits")
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DashboardCompactSection(
                title = "Day habits",
                summary = if (dayHabits.isEmpty()) "No day habits" else "${dayHabits.size} habits",
                expanded = dayExpanded,
                onToggle = { dayExpanded = !dayExpanded },
            ) {
                dayHabits.forEach { habit ->
                    key(habit.id, habit.dayKeys, habit.quantityDayValues) {
                        HabitWeekStripCard(habit = habit, onPersist = onPersistHabit, onOpenHabit = onOpenHabit)
                    }
                }
            }
            DashboardCompactSection(
                title = "Night habits",
                summary = if (nightHabits.isEmpty()) "No night habits" else "${nightHabits.size} habits",
                expanded = nightExpanded,
                onToggle = { nightExpanded = !nightExpanded },
            ) {
                nightHabits.forEach { habit ->
                    key(habit.id, habit.dayKeys, habit.quantityDayValues) {
                        HabitWeekStripCard(habit = habit, onPersist = onPersistHabit, onOpenHabit = onOpenHabit)
                    }
                }
            }
            if (sortedHabits.isEmpty()) {
                Text("No habits yet.", color = Color(0xFF8E8E90), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun formatCadAmountNumber(amount: Double, decimals: Int = 2): String {
    val pattern = if (decimals == 0) "#,##0" else "#,##0.00"
    val formatted = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US)).format(kotlin.math.abs(amount))
    val prefix = if (amount < 0) "-" else ""
    return "$prefix$formatted"
}

private fun formatCadAmount(amount: Double, decimals: Int = 2): String {
    return "${formatCadAmountNumber(amount, decimals)} CAD"
}

private fun formatSignedCadDelta(amount: Double, positive: Boolean): String {
    val prefix = if (positive) "+" else "-"
    return "$prefix ${formatCadAmount(amount)}"
}

private fun formatLedgerAmountForEdit(amount: Double): String =
    if (kotlin.math.abs(amount % 1.0) < 0.005) {
        amount.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", amount)
    }

private fun ledgerTypeLabel(type: String): String = when (type) {
    "income" -> "Income"
    "transfer" -> "Transfer"
    else -> "Expense"
}

private fun ledgerTypeColor(type: String, palette: BoopPalette): Color = when (type) {
    "income" -> Color(0xFF7CB88A)
    "transfer" -> palette.accent
    else -> palette.danger
}

@Composable
private fun FinanceAccountBalanceCard(
    name: String,
    balance: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val palette = LocalBoopPalette.current
    val interaction = remember(name) { MutableInteractionSource() }
    Surface(
        modifier = modifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) palette.accent.copy(alpha = 0.1f) else palette.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) palette.accent.copy(alpha = 0.45f) else palette.muted.copy(alpha = 0.12f),
        ),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(name, style = MaterialTheme.typography.labelLarge, color = palette.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                formatCadAmount(balance),
                style = MaterialTheme.typography.titleMedium,
                color = if (balance >= 0) palette.onBackground else palette.danger,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FinanceAccountExpandedPanel(
    account: BoopAccount,
    balance: Double,
    adjustText: String,
    onAdjustTextChange: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onApplyDelta: (Double, Boolean) -> Unit,
) {
    val palette = LocalBoopPalette.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.muted.copy(alpha = 0.14f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Current: ${formatCadAmount(balance)}",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BoopAccentTextButton(label = "Edit", onClick = onEdit, modifier = Modifier.weight(1f))
                TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text("Delete", color = palette.danger, style = MaterialTheme.typography.labelLarge)
                }
            }
            BoopFilledTextField(
                value = adjustText,
                onValueChange = { onAdjustTextChange(it.filter { ch -> ch.isDigit() || ch == '.' }.take(12)) },
                label = { Text("Adjust by amount") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.weight(1f)) {
                    BoopWhiteButton("Add") {
                        val amount = adjustText.toDoubleOrNull() ?: return@BoopWhiteButton
                        if (amount > 0.0) onApplyDelta(amount, true)
                    }
                }
                Box(Modifier.weight(1f)) {
                    BoopWhiteButton("Subtract") {
                        val amount = adjustText.toDoubleOrNull() ?: return@BoopWhiteButton
                        if (amount > 0.0) onApplyDelta(amount, false)
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceTransactionRow(
    entry: BoopLedgerEntry,
    accountNames: Map<String, String>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val typeColor = ledgerTypeColor(entry.type, palette)
    val rowInteraction = remember(entry.id) { MutableInteractionSource() }
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, palette.muted.copy(alpha = 0.1f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = rowInteraction, indication = null, onClick = onEdit),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = typeColor.copy(alpha = 0.16f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when (entry.type) {
                            "income" -> Icons.Outlined.AttachMoney
                            "transfer" -> Icons.AutoMirrored.Outlined.ArrowForward
                            else -> Icons.Outlined.EditNote
                        },
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    entry.title.ifBlank { ledgerTypeLabel(entry.type) },
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    when (entry.type) {
                        "transfer" -> "${accountNames[entry.accountId] ?: "From"} → ${accountNames[entry.toAccountId] ?: "To"}"
                        else -> accountNames[entry.accountId] ?: "Account"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    SimpleDateFormat("MMM d, HH:mm", Locale.US).format(entry.createdAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.muted.copy(alpha = 0.8f),
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    when (entry.type) {
                        "expense" -> formatSignedCadDelta(entry.amount, positive = false)
                        else -> formatSignedCadDelta(entry.amount, positive = true)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = typeColor,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit transaction", tint = palette.muted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete transaction", tint = palette.danger.copy(alpha = 0.85f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceScreen(
    accounts: List<BoopAccount>,
    entries: List<BoopLedgerEntry>,
    onDeleteAccount: (String) -> Unit,
    onSaveEntry: (BoopLedgerEntry) -> Unit,
    onEditEntry: (BoopLedgerEntry) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onEditAccount: (BoopAccount) -> Unit,
) {
    val palette = LocalBoopPalette.current
    var viewMode by rememberSaveable { mutableStateOf("overview") }
    var reconcileAccountId by rememberSaveable { mutableStateOf("") }
    var reconcileBalanceText by rememberSaveable { mutableStateOf("") }
    var expandedAccountId by rememberSaveable { mutableStateOf("") }
    var balanceAdjustText by rememberSaveable { mutableStateOf("") }
    var pendingDeleteAccountId by rememberSaveable { mutableStateOf("") }
    var pendingDeleteEntryId by rememberSaveable { mutableStateOf("") }
    val epoch = LocalBoopDataEpoch.current
    val accountNames = remember(epoch) { accounts.associate { it.id to it.name } }
    val balances = remember(epoch) {
        accounts.associate { it.id to 0.0 }.toMutableMap().apply {
            entries.forEach { entry ->
                when (entry.type) {
                    "income" -> this[entry.accountId] = (this[entry.accountId] ?: 0.0) + entry.amount
                    "expense" -> this[entry.accountId] = (this[entry.accountId] ?: 0.0) - entry.amount
                    "transfer" -> {
                        this[entry.accountId] = (this[entry.accountId] ?: 0.0) - entry.amount
                        entry.toAccountId?.let { toId -> this[toId] = (this[toId] ?: 0.0) + entry.amount }
                    }
                }
            }
        }
    }
    val sortedEntries = remember(epoch) { entries.sortedByDescending { it.createdAtMillis } }
    val netTotal = balances.values.sum()
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BoopPageTitle("Accounts", modifier = Modifier.weight(1f, fill = false))
            when (viewMode) {
                "overview" -> Text(
                    formatCadAmount(netTotal),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (netTotal >= 0) Color(0xFF7CB88A) else palette.danger,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                else -> BoopAccentTextButton(label = "Overview", onClick = { viewMode = "overview" })
            }
        }
        when (viewMode) {
            "accounts" -> {
                if (accounts.isEmpty()) {
                    Text("No accounts yet. Use + menu on Accounts tab.", color = palette.muted, style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(
                        Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 72.dp),
                    ) {
                        items(accounts.chunked(2), key = { row -> row.joinToString { it.id } }) { rowAccounts ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    rowAccounts.forEach { account ->
                                        val balance = balances[account.id] ?: 0.0
                                        val selected = expandedAccountId == account.id
                                        FinanceAccountBalanceCard(
                                            name = account.name,
                                            balance = balance,
                                            selected = selected,
                                            onClick = {
                                                expandedAccountId = if (selected) "" else account.id
                                                balanceAdjustText = ""
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                    if (rowAccounts.size == 1) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                                val expandedAccount = rowAccounts.firstOrNull { it.id == expandedAccountId }
                                if (expandedAccount != null) {
                                    FinanceAccountExpandedPanel(
                                        account = expandedAccount,
                                        balance = balances[expandedAccount.id] ?: 0.0,
                                        adjustText = balanceAdjustText,
                                        onAdjustTextChange = { balanceAdjustText = it },
                                        onEdit = { onEditAccount(expandedAccount) },
                                        onDelete = {
                                            pendingDeleteAccountId = expandedAccount.id
                                            expandedAccountId = ""
                                        },
                                        onApplyDelta = { amount, add ->
                                            onSaveEntry(
                                                BoopLedgerEntry(
                                                    id = UUID.randomUUID().toString(),
                                                    type = if (add) "income" else "expense",
                                                    accountId = expandedAccount.id,
                                                    amount = amount,
                                                    title = if (add) "Balance increase" else "Balance decrease",
                                                    note = "Adjusted from accounts list",
                                                ),
                                            )
                                            balanceAdjustText = ""
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "transactions" -> {
                if (sortedEntries.isEmpty()) {
                    Text("No transactions yet. Use + menu to add income, expense or transfer.", color = palette.muted, style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(
                        Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 72.dp),
                    ) {
                        items(sortedEntries, key = { it.id }) { entry ->
                            FinanceTransactionRow(
                                entry = entry,
                                accountNames = accountNames,
                                onEdit = { onEditEntry(entry) },
                                onDelete = { pendingDeleteEntryId = entry.id },
                            )
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 72.dp),
                ) {
                    if (accounts.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    DashboardSectionLabel("Your accounts", modifier = Modifier.weight(1f))
                                    BoopAccentTextButton(label = "See all", onClick = { viewMode = "accounts" })
                                }
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    accounts.forEach { account ->
                                        val balance = balances[account.id] ?: 0.0
                                        FinanceAccountBalanceCard(
                                            name = account.name,
                                            balance = balance,
                                            onClick = {
                                                reconcileAccountId = account.id
                                                reconcileBalanceText = String.format(Locale.US, "%.2f", balance)
                                            },
                                            modifier = Modifier.width(156.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                DashboardSectionLabel("Recent activity", modifier = Modifier.weight(1f))
                                BoopAccentTextButton(label = "Transactions", onClick = { viewMode = "transactions" })
                            }
                            if (sortedEntries.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = palette.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        "No transactions yet — add income, expense, or transfer from +.",
                                        color = palette.muted,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    sortedEntries.take(5).forEach { entry ->
                                        FinanceTransactionRow(
                                            entry = entry,
                                            accountNames = accountNames,
                                            onEdit = { onEditEntry(entry) },
                                            onDelete = { pendingDeleteEntryId = entry.id },
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
    val reconcileAccount = accounts.firstOrNull { it.id == reconcileAccountId }
    if (reconcileAccount != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { reconcileAccountId = "" },
            title = { Text("Set real balance", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(reconcileAccount.name, color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodyMedium)
                    BoopFilledTextField(
                        value = reconcileBalanceText,
                        onValueChange = { reconcileBalanceText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(12) },
                        label = { Text("Current CAD balance") },
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { reconcileAccountId = "" }) { Text("Cancel", color = Color(0xFFBFBFBF)) }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = reconcileBalanceText.toDoubleOrNull() ?: return@TextButton
                        val current = balances[reconcileAccount.id] ?: 0.0
                        val delta = target - current
                        if (kotlin.math.abs(delta) >= 0.005) {
                            onSaveEntry(
                                BoopLedgerEntry(
                                    id = UUID.randomUUID().toString(),
                                    type = if (delta >= 0) "income" else "expense",
                                    accountId = reconcileAccount.id,
                                    amount = kotlin.math.abs(delta),
                                    title = "Balance adjustment",
                                    note = "Reconciled to CAD ${String.format(Locale.US, "%.2f", target)}",
                                ),
                            )
                        }
                        reconcileAccountId = ""
                    },
                ) { Text("Apply", color = MaterialTheme.colorScheme.onBackground) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
    val pendingDeleteAccount = accounts.firstOrNull { it.id == pendingDeleteAccountId }
    if (pendingDeleteAccount != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDeleteAccountId = "" },
            title = { Text("Delete account", color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("Delete ${pendingDeleteAccount.name}? This also removes related transactions.", color = Color(0xFFBFBFBF)) },
            dismissButton = {
                TextButton(onClick = { pendingDeleteAccountId = "" }) { Text("Cancel", color = Color(0xFFBFBFBF)) }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAccount(pendingDeleteAccount.id)
                    pendingDeleteAccountId = ""
                }) { Text("Delete", color = Color(0xFFEF9A9A)) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
    val pendingDeleteEntry = sortedEntries.firstOrNull { it.id == pendingDeleteEntryId }
    if (pendingDeleteEntry != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDeleteEntryId = "" },
            title = { Text("Delete transaction", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Text(
                    "Delete \"${pendingDeleteEntry.title.ifBlank { ledgerTypeLabel(pendingDeleteEntry.type) }}\"?",
                    color = palette.muted,
                )
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEntryId = "" }) { Text("Cancel", color = palette.muted) }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteEntry(pendingDeleteEntry.id)
                    pendingDeleteEntryId = ""
                }) { Text("Delete", color = palette.danger) }
            },
            containerColor = palette.surface,
        )
    }
}

@Composable
private fun AccountEditorSheet(
    initial: ItemSheet.AccountSheet,
    onDismiss: () -> Unit,
    onSave: (BoopAccount) -> Unit,
) {
    var name by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.name) }
    BoopSheetHeaderTitle(if (initial.id == null) "Add account" else "Edit account")
    Spacer(Modifier.height(12.dp))
    BoopFilledTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Account name") },
        placeholder = { Text("Cash, Bank, Card...", color = Color(0xFF8A8A8A)) },
    )
    Spacer(Modifier.height(16.dp))
    BoopWhiteButton(if (initial.id == null) "Save account" else "Save changes") {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return@BoopWhiteButton
        onSave(
            BoopAccount(
                id = initial.id ?: UUID.randomUUID().toString(),
                name = trimmed,
            ),
        )
    }
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFFBFBFBF)) }
}

@Composable
private fun FinanceEntrySheet(
    initial: ItemSheet.FinanceEntrySheet,
    accounts: List<BoopAccount>,
    onDismiss: () -> Unit,
    onSave: (BoopLedgerEntry) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val palette = LocalBoopPalette.current
    val isEditing = initial.entryId != null
    var title by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.prefilledTitle) }
    var amountText by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.prefilledAmount) }
    var category by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.prefilledCategory) }
    var subcategory by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.prefilledSubcategory) }
    var note by rememberSaveable(initial.sessionKey) { mutableStateOf(initial.prefilledNote) }
    var dueAt by rememberSaveable(initial.sessionKey) { mutableLongStateOf(initial.prefilledDueAtMillis) }
    var showDuePicker by rememberSaveable(initial.sessionKey) { mutableStateOf(false) }
    var fromAccountId by rememberSaveable(initial.sessionKey) {
        mutableStateOf(initial.prefilledAccountId.ifBlank { accounts.firstOrNull()?.id.orEmpty() })
    }
    var toAccountId by rememberSaveable(initial.sessionKey) {
        mutableStateOf(
            initial.prefilledToAccountId.ifBlank {
                accounts.drop(1).firstOrNull()?.id.orEmpty()
            },
        )
    }

    if (accounts.isEmpty()) {
        Text("Add an account first from the + menu.", color = Color(0xFFBFBFBF))
        Spacer(Modifier.height(12.dp))
        BoopWhiteButton("Close") { onDismiss() }
        return
    }
    BoopSheetHeaderTitle(
        when {
            isEditing && initial.type == "income" -> "Edit income"
            isEditing && initial.type == "transfer" -> "Edit transfer"
            isEditing -> "Edit expense"
            initial.type == "income" -> "Add income"
            initial.type == "transfer" -> "Add transfer"
            else -> "Add expense"
        },
    )
    Spacer(Modifier.height(12.dp))
    Text("From account", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(6.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        accounts.forEach { account ->
            val active = fromAccountId == account.id
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (active) Color.White else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { fromAccountId = account.id },
            ) {
                Text(
                    account.name,
                    color = if (active) Color.Black else Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
    }
    if (initial.type == "transfer") {
        Spacer(Modifier.height(8.dp))
        Text("To account", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            accounts.filter { it.id != fromAccountId }.forEach { account ->
                val active = toAccountId == account.id
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (active) Color.White else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { toAccountId = account.id },
                ) {
                    Text(
                        account.name,
                        color = if (active) Color.Black else Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(
        value = amountText,
        onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' }.take(10) },
        label = { Text("Amount (CAD)") },
    )
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(value = subcategory, onValueChange = { subcategory = it }, label = { Text("Subcategory") })
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Due date (optional)", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
        TextButton(onClick = { showDuePicker = true }) {
            Text(if (dueAt > 0L) SimpleDateFormat("MMM d, HH:mm", Locale.US).format(dueAt) else "Set due", color = MaterialTheme.colorScheme.onBackground)
        }
    }
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(value = note, onValueChange = { note = it }, label = { Text("Note (optional)") })
    Spacer(Modifier.height(16.dp))
    BoopWhiteButton(if (isEditing) "Save changes" else "Save transaction") {
        val amount = amountText.toDoubleOrNull() ?: 0.0
        if (title.isBlank() || amount <= 0.0 || fromAccountId.isBlank()) return@BoopWhiteButton
        if (initial.type == "transfer" && toAccountId.isBlank()) return@BoopWhiteButton
        onSave(
            BoopLedgerEntry(
                id = initial.entryId ?: UUID.randomUUID().toString(),
                type = initial.type,
                accountId = fromAccountId,
                toAccountId = toAccountId.takeIf { initial.type == "transfer" },
                amount = amount,
                title = title.trim(),
                category = category.trim(),
                subcategory = subcategory.trim(),
                note = note.trim(),
                dueAtMillis = dueAt.takeIf { it > 0L },
                createdAtMillis = initial.createdAtMillis.takeIf { it > 0L } ?: System.currentTimeMillis(),
            ),
        )
    }
    if (onDelete != null) {
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onDelete) {
            Text("Delete transaction", color = palette.danger, style = MaterialTheme.typography.labelLarge)
        }
    }
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onDismiss) { Text("Cancel", color = palette.muted) }
    ReminderPickerDialog(
        visible = showDuePicker,
        initialMillis = if (dueAt > 0L) dueAt else System.currentTimeMillis(),
        title = "Due date",
        onDismiss = { showDuePicker = false },
        onConfirm = {
            dueAt = it
            showDuePicker = false
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderPickerDialog(
    visible: Boolean,
    initialMillis: Long,
    title: String = "Pick date & time",
    showTime: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    if (!visible) return
    val palette = LocalBoopPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialCal = remember(initialMillis) {
        Calendar.getInstance().apply { timeInMillis = initialMillis }
    }
    var displayMonth by remember(initialMillis) {
        mutableStateOf(
            Calendar.getInstance().apply {
                timeInMillis = initialMillis
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            },
        )
    }
    var selectedYear by remember(initialMillis) { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var selectedMonth by remember(initialMillis) { mutableIntStateOf(initialCal.get(Calendar.MONTH)) }
    var selectedDayOfMonth by remember(initialMillis) { mutableIntStateOf(initialCal.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember(initialMillis) { mutableIntStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember(initialMillis) { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) }

    fun selectedMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
            set(Calendar.HOUR_OF_DAY, if (showTime) hour else 12)
            set(Calendar.MINUTE, if (showTime) minute else 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.muted) },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = palette.onBackground,
            )
            Text(
                if (showTime) {
                    SimpleDateFormat("EEE, MMM d · h:mm a", Locale.US).format(selectedMillis())
                } else {
                    SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(selectedMillis())
                },
                color = palette.accent,
                style = MaterialTheme.typography.bodyMedium,
            )
            BoopMonthCalendarGrid(
                displayMonth = displayMonth,
                selectedYear = selectedYear,
                selectedMonth = selectedMonth,
                selectedDayOfMonth = selectedDayOfMonth,
                onPreviousMonth = {
                    val next = (displayMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    displayMonth = next
                },
                onNextMonth = {
                    val next = (displayMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    displayMonth = next
                },
                onSelectDay = { year, month, day ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDayOfMonth = day
                },
            )
            if (showTime) {
                Text("Time", color = palette.muted, style = MaterialTheme.typography.labelSmall)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(9 to 0, 12 to 0, 18 to 0, 21 to 0).forEach { (presetHour, presetMinute) ->
                        val active = hour == presetHour && minute == presetMinute
                        BoopChoicePill(
                            label = SimpleDateFormat("h:mm a", Locale.US).format(
                                Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, presetHour)
                                    set(Calendar.MINUTE, presetMinute)
                                }.time,
                            ),
                            selected = active,
                            onClick = {
                                hour = presetHour
                                minute = presetMinute
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                BoopTimeStepperRow(
                    label = "Hour",
                    valueLabel = SimpleDateFormat("h a", Locale.US).format(
                        Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, 0)
                        }.time,
                    ),
                    onDecrement = { hour = (hour + 23) % 24 },
                    onIncrement = { hour = (hour + 1) % 24 },
                )
                BoopTimeStepperRow(
                    label = "Minute",
                    valueLabel = minute.toString().padStart(2, '0'),
                    onDecrement = { minute = if (minute < 5) 55 else minute - 5 },
                    onIncrement = { minute = (minute + 5) % 60 },
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (0..55 step 5).forEach { minuteOption ->
                        BoopChoicePill(
                            label = minuteOption.toString().padStart(2, '0'),
                            selected = minute == minuteOption,
                            onClick = { minute = minuteOption },
                        )
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel", color = palette.muted, style = MaterialTheme.typography.labelLarge)
                }
                BoopWhiteButton("Confirm") {
                    onConfirm(selectedMillis())
                }
            }
        }
    }
}

@Composable
private fun BoopMonthCalendarGrid(
    displayMonth: Calendar,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDayOfMonth: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (year: Int, month: Int, day: Int) -> Unit,
) {
    val palette = LocalBoopPalette.current
    val monthCal = remember(displayMonth.timeInMillis) {
        (displayMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val firstDayOffset = (monthCal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val cells = remember(monthCal.timeInMillis) {
        mutableListOf<Int>().apply {
            repeat(firstDayOffset) { add(0) }
            addAll(1..daysInMonth)
            while (size % 7 != 0) add(0)
        }
    }
    val todayKey = todayHabitDayKey()
    val selectedKey = String.format(Locale.US, "%04d%02d%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, palette.muted.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .background(palette.surfaceElevated)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "Previous month", tint = palette.onBackground)
            }
            Text(
                SimpleDateFormat("MMMM yyyy", Locale.US).format(monthCal.time),
                style = MaterialTheme.typography.titleMedium,
                color = palette.accent,
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = "Next month", tint = palette.onBackground)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    color = palette.muted,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
        cells.chunked(7).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { day ->
                    if (day == 0) {
                        Spacer(Modifier.weight(1f).height(36.dp))
                    } else {
                        val dayCal = (monthCal.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, day)
                        }
                        val dayKey = habitDayKeyFormat.format(dayCal.time)
                        val isSelected = dayKey == selectedKey
                        val isToday = dayKey == todayKey
                        BoopCalendarDayCell(
                            label = day.toString(),
                            isSelected = isSelected,
                            isToday = isToday,
                            onSelect = {
                                onSelectDay(
                                    dayCal.get(Calendar.YEAR),
                                    dayCal.get(Calendar.MONTH),
                                    dayCal.get(Calendar.DAY_OF_MONTH),
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoopTimeStepperRow(
    label: String,
    valueLabel: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = palette.muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(52.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) {
                Icon(Icons.Outlined.ChevronLeft, contentDescription = "Decrease $label", tint = palette.accent)
            }
            Text(
                valueLabel,
                color = palette.onBackground,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.widthIn(min = 72.dp),
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = onIncrement) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = "Increase $label", tint = palette.accent)
            }
        }
    }
}

@Composable
private fun BoopChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) palette.accent else palette.surfaceVariant,
        border = if (selected) null else BorderStroke(1.dp, palette.muted.copy(alpha = 0.22f)),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Text(
            label,
            color = if (selected) palette.accentOn else palette.onBackground,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )
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
    return "${habit.title} ${habit.progress} ${habit.goal} ${habit.dayKeys} ${habit.quantityUnit} ${habit.quantityDailyTarget} ${habit.quantityDayValues} ${habitCategoryLabel(habit.dayPeriodCategory)}"
        .lowercase(Locale.getDefault())
}

private fun normalizeHabitCategory(raw: String): String = when (raw.lowercase(Locale.getDefault())) {
    "night" -> "night"
    else -> "day"
}

private fun habitCategoryLabel(raw: String): String =
    normalizeHabitCategory(raw).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

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
    val epoch = LocalBoopDataEpoch.current
    val matchTasks = remember(epoch, q) {
        if (q.isEmpty()) {
            emptyList()
        } else {
            tasks.filter { !it.archived && taskSearchHaystack(it).contains(q) }
        }
    }
    val matchNotes = remember(epoch, q) {
        if (q.isEmpty()) emptyList() else notes.filter { noteSearchHaystack(it).contains(q) }
    }
    val matchHabits = remember(epoch, q) {
        if (q.isEmpty()) emptyList() else habits.filter { habitSearchHaystack(it).contains(q) }
    }
    val anyMatch = matchTasks.isNotEmpty() || matchNotes.isNotEmpty() || matchHabits.isNotEmpty()
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            q.isEmpty() -> {
                Text("Start typing to search across the app.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            !anyMatch -> {
                Text("No matches.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            else -> {
                if (matchTasks.isNotEmpty()) {
                    Text("Tasks", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
                    matchTasks.take(12).forEach { task ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPickTask(task) },
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(task.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(formatTaskReminderLine(task.reminderAt), color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                if (matchNotes.isNotEmpty()) {
                    Text("Notes", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
                    matchNotes.take(12).forEach { note ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPickNote(note) },
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(note.title.ifBlank { "Untitled note" }, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                val snip = plainNoteSnippet(note.body, 96)
                                if (snip.isNotBlank()) {
                                    Text(snip, color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                if (matchHabits.isNotEmpty()) {
                    Text("Habits", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
                    matchHabits.take(12).forEach { habit ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPickHabit(habit) },
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    "${habit.title} · ${habitCategoryLabel(habit.dayPeriodCategory)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${habit.progress}/${habit.goal}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            } else {
                                Text(dayNum, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.labelLarge)
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
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                val map = dayValues.toMutableMap()
                                map[todayKey] = todayAmount + delta
                                onPersist(habit.copy(quantityDayValues = serializeHabitDayValues(map)))
                            },
                        ) {
                            Text(
                                "+$delta",
                                color = MaterialTheme.colorScheme.onBackground,
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
                Text("Check-off habits toggle today; quantity habits let you add minutes/mL with +/-.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        if (habits.isEmpty()) {
            Text("No habits yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        } else {
            habits.forEach { habit ->
                key(habit.id, habit.dayKeys, habit.quantityDayValues) {
                    HabitWeekStripCard(
                        habit = habit,
                        onPersist = onPersist,
                        onOpenHabit = onEditHabit,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
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
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Starts", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
            Text(SimpleDateFormat("EEE, MMM dd · HH:mm", Locale.US).format(startAt), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
            Text("Tap to edit", color = Color(0xFF6E6E70), style = MaterialTheme.typography.labelSmall)
        }
    }
    Spacer(Modifier.height(6.dp))
    val todayRef = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayDateLabel = remember { SimpleDateFormat("EEE, MMM dd", Locale.US).format(todayRef) }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFBFBF)),
        modifier = Modifier.clickable {
            val current = Calendar.getInstance().apply { timeInMillis = startAt }
            val today = Calendar.getInstance().apply { timeInMillis = todayRef }
            current.set(Calendar.YEAR, today.get(Calendar.YEAR))
            current.set(Calendar.MONTH, today.get(Calendar.MONTH))
            current.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH))
            startAt = current.timeInMillis
            if (endAt <= startAt) endAt = startAt + 60 * 60_000L
        },
    ) {
        Text(
            "Today: $todayDateLabel",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
    Spacer(Modifier.height(8.dp))
    Surface(
        onClick = { pickEnd = true },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Ends", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
            Text(SimpleDateFormat("EEE, MMM dd · HH:mm", Locale.US).format(endAt), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
        }
    }
    ReminderPickerDialog(
        visible = pickStart,
        initialMillis = startAt,
        title = "Start date & time",
        showTime = !allDay,
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
        title = "End date & time",
        showTime = !allDay,
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
                    color = if (active) Color.White else MaterialTheme.colorScheme.surfaceVariant,
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
                color = if (active) Color.White else MaterialTheme.colorScheme.surfaceVariant,
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
            color = if (customActive) Color.White else MaterialTheme.colorScheme.surfaceVariant,
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
    notes: List<BoopNote>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSaveNote: (BoopNote) -> Unit,
    onSave: (BoopTask) -> Unit,
) {
    val sheetKey = initial.sessionKey
    var title by rememberSaveable(sheetKey) { mutableStateOf(initial.title) }
    var reminderAt by remember(sheetKey, initial.reminderAt) { mutableLongStateOf(initial.reminderAt) }
    var done by remember(sheetKey) { mutableStateOf(initial.done) }
    var repeatEveryDays by rememberSaveable(sheetKey) { mutableIntStateOf(initial.repeatEveryDays.coerceAtLeast(0)) }
    var linkedNoteId by rememberSaveable(sheetKey) { mutableStateOf(initial.linkedNoteId) }
    var newNoteTitle by rememberSaveable(sheetKey) { mutableStateOf("") }
    var newNoteBody by rememberSaveable(sheetKey) { mutableStateOf("") }
    var customRepeatDays by rememberSaveable(sheetKey) {
        mutableStateOf(
            initial.repeatEveryDays.takeIf { it !in setOf(0, 1, 7, 30, 365) }?.toString().orEmpty(),
        )
    }
    var showReminderPicker by remember(sheetKey) { mutableStateOf(false) }
    var hasExplicitSave by remember(sheetKey) { mutableStateOf(false) }
    var skipAutoSaveOnDispose by remember(sheetKey) { mutableStateOf(false) }
    fun buildTaskForSaveOrNull(): BoopTask? {
        if (title.isBlank()) return null
        val rep = repeatEveryDays.coerceAtLeast(0)
        var outDone = done
        var outRem = reminderAt
        if (done && rep > 0) {
            outRem = nextRepeatReminderMillis(reminderAt, rep)
            outDone = false
        }
        return BoopTask(
            id = initial.id ?: UUID.randomUUID().toString(),
            title = title.trim(),
            reminderAt = outRem,
            done = outDone,
            repeatEveryDays = rep,
            linkedNoteId = linkedNoteId,
            archived = initial.archived,
        )
    }
    DisposableEffect(sheetKey) {
        onDispose {
            if (!hasExplicitSave && !skipAutoSaveOnDispose) {
                buildTaskForSaveOrNull()?.let(onSave)
            }
        }
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            BoopSheetHeaderTitle(if (initial.id == null) "New task" else "Edit task")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (initial.id != null) {
                IconButton(
                    onClick = {
                        hasExplicitSave = true
                        buildTaskForSaveOrNull()?.let { onSave(it.copy(archived = !initial.archived)) }
                    },
                ) {
                    if (initial.archived) {
                        Icon(Icons.Outlined.Unarchive, contentDescription = "Restore task", tint = Color(0xFF9AE6B4))
                    } else {
                        Icon(Icons.Outlined.Archive, contentDescription = "Archive task", tint = Color(0xFFFFD98A))
                    }
                }
            }
            if (onDelete != null) {
                IconButton(
                    onClick = {
                        skipAutoSaveOnDispose = true
                        onDelete()
                    },
                ) {
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
                color = if (active) Color.White else MaterialTheme.colorScheme.surfaceVariant,
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
            color = if (customActive) Color.White else MaterialTheme.colorScheme.surfaceVariant,
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
    Text("Linked note", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(6.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val noneActive = linkedNoteId == null
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (noneActive) Color.White else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { linkedNoteId = null },
        ) {
            Text(
                "None",
                color = if (noneActive) Color.Black else Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            )
        }
        notes.take(20).forEach { note ->
            val active = linkedNoteId == note.id
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (active) Color.White else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { linkedNoteId = note.id },
            ) {
                Text(
                    note.title.ifBlank { "Untitled note" },
                    color = if (active) Color.Black else Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text("Or create a note now", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(6.dp))
    BoopFilledTextField(
        value = newNoteTitle,
        onValueChange = { newNoteTitle = it },
        label = { Text("New note title") },
    )
    Spacer(Modifier.height(6.dp))
    BoopFilledTextField(
        value = newNoteBody,
        onValueChange = { newNoteBody = it },
        label = { Text("New note body") },
        minLines = 2,
    )
    Spacer(Modifier.height(12.dp))
    Surface(
        onClick = { showReminderPicker = true },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
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
                Text(formatTaskReminderLine(reminderAt), color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
            }
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
        }
    }
    ReminderPickerDialog(
        visible = showReminderPicker,
        initialMillis = reminderAt,
        title = "Reminder",
        onDismiss = { showReminderPicker = false },
        onConfirm = {
            reminderAt = it
            showReminderPicker = false
        },
    )
    Spacer(Modifier.height(20.dp))
    BoopWhiteButton("Save") {
        hasExplicitSave = true
        val taskCandidate = buildTaskForSaveOrNull() ?: return@BoopWhiteButton
        val createFreshNote = newNoteTitle.isNotBlank() || newNoteBody.isNotBlank()
        val resolvedTask = if (createFreshNote) {
            val noteId = UUID.randomUUID().toString()
            onSaveNote(
                BoopNote(
                    id = noteId,
                    title = newNoteTitle.trim(),
                    body = newNoteBody.trim(),
                    attachmentUri = null,
                    audioUri = null,
                    tagsCsv = "",
                    ocrText = "",
                    linkedTaskId = taskCandidate.id,
                    archived = false,
                    createdAtMillis = System.currentTimeMillis(),
                    updatedAtMillis = System.currentTimeMillis(),
                ),
            )
            taskCandidate.copy(linkedNoteId = noteId)
        } else {
            taskCandidate
        }
        onSave(resolvedTask)
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
    val palette = LocalBoopPalette.current
    val session = initial.sessionKey
    var title by rememberSaveable(session) { mutableStateOf(initial.title) }
    var tagsCsv by rememberSaveable(session) { mutableStateOf(initial.tagsCsv) }
    var attachmentStored by remember(session) { mutableStateOf(parseNoteAttachments(initial.attachmentUri)) }
    var audioStored by remember(session) { mutableStateOf(initial.audioUri) }
    var bodyEdit by remember(session) { mutableStateOf<EditText?>(null) }
    var editorFocused by remember(session) { mutableStateOf(false) }
    var recording by remember(session) { mutableStateOf(false) }
    var recordingStartedAt by remember(session) { mutableLongStateOf(0L) }
    var recorder by remember(session) { mutableStateOf<MediaRecorder?>(null) }
    var hasExplicitSave by remember(session) { mutableStateOf(false) }
    var skipAutoSaveOnDispose by remember(session) { mutableStateOf(false) }
    fun buildNoteForSaveOrNull(): BoopNote? {
        val noteId = initial.id ?: UUID.randomUUID().toString()
        val resolvedAttachment = serializeNoteAttachments(attachmentStored)
        val editable = bodyEdit?.text
        val bodyHtml = if (editable is Spanned) {
            Html.toHtml(editable, 0x1 /* Html.TO_HTML_PARCEL_OUTPUT_MODE */).trim()
        } else {
            editable?.toString()?.trim().orEmpty()
        }
        if (title.isBlank() && bodyHtml.isBlank()) return null
        val ocrText = extractTextFromAttachment(context, resolvedAttachment)
        return BoopNote(
            id = noteId,
            title = title.trim(),
            body = bodyHtml,
            attachmentUri = resolvedAttachment,
            audioUri = audioStored,
            tagsCsv = normalizeNoteTags(tagsCsv),
            ocrText = ocrText,
            linkedTaskId = initial.linkedTaskId,
            archived = initial.archived,
            createdAtMillis = initial.createdAtMillis,
            updatedAtMillis = System.currentTimeMillis(),
        )
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val existing = attachmentStored.toMutableList()
        uris.take((25 - existing.size).coerceAtLeast(0)).forEach { uri ->
            val copied = copyAttachmentToInternalFile(context, uri, UUID.randomUUID().toString())
            existing.add(copied ?: uri.toString())
        }
        attachmentStored = existing.distinct().take(25)
    }
    var micGranted by remember(session) {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    fun beginAudioRecording() {
        if (recording) return
        startNoteAudioRecording(
            context = context,
            onStarted = { r, path, startedAt ->
                recorder = r
                recording = true
                recordingStartedAt = startedAt
                audioStored = path
                Toast.makeText(context, "Recording… tap stop when done", Toast.LENGTH_SHORT).show()
            },
            onFailed = { reason ->
                recorder = null
                recording = false
                recordingStartedAt = 0L
                Toast.makeText(context, "Could not start recording: $reason", Toast.LENGTH_LONG).show()
            },
        )
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
        if (granted) {
            beginAudioRecording()
        } else {
            Toast.makeText(context, "Microphone permission is required to record audio.", Toast.LENGTH_LONG).show()
        }
    }
    DisposableEffect(session) {
        onDispose {
            if (!hasExplicitSave && !skipAutoSaveOnDispose) {
                buildNoteForSaveOrNull()?.let(onSave)
            }
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
                        hasExplicitSave = true
                        onSave(
                            BoopNote(
                                id = initial.id,
                                title = title.trim(),
                                body = bodyHtml,
                                attachmentUri = serializedAttachments,
                                audioUri = audioStored,
                                tagsCsv = normalizeNoteTags(tagsCsv),
                                ocrText = extractTextFromAttachment(context, serializedAttachments),
                                linkedTaskId = initial.linkedTaskId,
                                archived = !initial.archived,
                                createdAtMillis = initial.createdAtMillis,
                                updatedAtMillis = System.currentTimeMillis(),
                            ),
                        )
                    },
                ) {
                    if (initial.archived) {
                        Icon(Icons.Outlined.Unarchive, contentDescription = "Restore note", tint = Color(0xFF9AE6B4))
                    } else {
                        Icon(Icons.Outlined.Archive, contentDescription = "Archive note", tint = Color(0xFFFFD98A))
                    }
                }
            }
            if (onDelete != null) {
                IconButton(
                    onClick = {
                        skipAutoSaveOnDispose = true
                        onDelete()
                    },
                ) {
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
    if (initial.id != null) {
        Spacer(Modifier.height(4.dp))
        Text(
            formatNoteCardTime(
                BoopNote(
                    id = initial.id,
                    title = "",
                    body = "",
                    attachmentUri = null,
                    tagsCsv = "",
                    ocrText = "",
                    archived = false,
                    createdAtMillis = initial.createdAtMillis,
                    updatedAtMillis = initial.updatedAtMillis,
                ),
            ),
            color = Color(0xFF8E8E90),
            style = MaterialTheme.typography.labelSmall,
        )
    }
    Spacer(Modifier.height(8.dp))
    BoopFilledTextField(
        value = tagsCsv,
        onValueChange = { tagsCsv = it },
        label = { Text("Tags") },
        placeholder = { Text("work, urgent, ideas", color = Color(0xFF8A8A8A)) },
    )
    Spacer(Modifier.height(8.dp))
    Text("Note", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
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
            val fieldBg = palette.inputField
            val fieldFg = palette.onBackground
            val fieldHint = palette.muted
            EditText(ctx).apply {
                applyBoopSans()
                setBackgroundColor(fieldBg.toArgb())
                setTextColor(fieldFg.toArgb())
                setHintTextColor(fieldHint.toArgb())
                hint = "Write your note…"
                minLines = 4
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                gravity = Gravity.TOP or Gravity.START
                setPadding(16, 16, 16, 16)
                onFocusChangeListener = android.view.View.OnFocusChangeListener { _, hasFocus ->
                    editorFocused = hasFocus
                }
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
            editorFocused = et.hasFocus()
        },
    )
    if (editorFocused) {
        Spacer(Modifier.height(6.dp))
        NoteRichTextToolbar(bodyEdit, context)
    }
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
            Icon(Icons.Outlined.Image, contentDescription = "Attach image", tint = MaterialTheme.colorScheme.onBackground)
        }
        IconButton(
            onClick = {
                val urlInput = EditText(context).apply {
                    applyBoopSans()
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
            Icon(Icons.Outlined.Link, contentDescription = "Insert link", tint = MaterialTheme.colorScheme.onBackground)
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
                    val fileOk = !audioStored.isNullOrBlank() && File(audioStored!!).let { it.exists() && it.length() > 0L }
                    if (fileOk) {
                        Toast.makeText(context, "Recording saved (${secs}s)", Toast.LENGTH_SHORT).show()
                    } else {
                        audioStored = null
                        Toast.makeText(context, "Recording failed — try again", Toast.LENGTH_SHORT).show()
                    }
                } else if (micGranted) {
                    beginAudioRecording()
                } else {
                    micPermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
        ) {
            Icon(
                if (recording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                contentDescription = if (recording) "Stop recording" else "Record audio",
                tint = if (recording) palette.recording else MaterialTheme.colorScheme.onBackground,
            )
        }
        if (!audioStored.isNullOrBlank()) {
            IconButton(
                onClick = {
                    val path = audioStored ?: return@IconButton
                    try {
                        MediaPlayer().apply {
                            setDataSource(path)
                            prepare()
                            start()
                            setOnCompletionListener { mp -> mp.release() }
                        }
                    } catch (_: Throwable) {
                        Toast.makeText(context, "Could not play recording", Toast.LENGTH_SHORT).show()
                    }
                },
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = "Play audio", tint = palette.accent)
            }
        }
    }
    if (recording) {
        Spacer(Modifier.height(6.dp))
        Surface(shape = RoundedCornerShape(10.dp), color = palette.surfaceVariant) {
            Text(
                "Recording… tap stop when done",
                color = palette.recording,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    } else if (!audioStored.isNullOrBlank()) {
        Spacer(Modifier.height(6.dp))
        Text(
            "Audio attached — tap play to listen",
            color = palette.muted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
    if (previewLinks.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Text("Links", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
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
        hasExplicitSave = true
        buildNoteForSaveOrNull()?.let(onSave)
    }
}

@Composable
private fun HabitEditorSheet(
    initial: ItemSheet.HabitSheet,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (BoopHabit) -> Unit,
) {
    val sheetKey = initial.sessionKey
    var label by rememberSaveable(sheetKey) { mutableStateOf(initial.title) }
    var dayPeriodCategory by rememberSaveable(sheetKey) { mutableStateOf(normalizeHabitCategory(initial.dayPeriodCategory)) }
    var goalText by rememberSaveable(sheetKey) { mutableStateOf(initial.goal.toString()) }
    var progress by remember(sheetKey) { mutableIntStateOf(initial.progress) }
    var quantityMode by rememberSaveable(sheetKey) { mutableStateOf(initial.quantityMode) }
    var quantityUnit by rememberSaveable(sheetKey) { mutableStateOf(initial.quantityUnit) }
    var quantityTarget by rememberSaveable(sheetKey) { mutableStateOf(initial.quantityDailyTarget.toString()) }
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
    Text("Time category", color = Color(0xFF8E8E90), style = MaterialTheme.typography.labelSmall)
    Spacer(Modifier.height(6.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("day", "night").forEach { cat ->
            val active = dayPeriodCategory == cat
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (active) Color.White else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { dayPeriodCategory = cat },
            ) {
                Text(
                    cat.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    color = if (active) Color.Black else Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }
    }
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
                    dayPeriodCategory = normalizeHabitCategory(dayPeriodCategory),
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
private fun BoopTaskCompleteToggle(
    enabled: Boolean,
    active: Boolean = false,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.88f
            active -> 1.08f
            else -> 1f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "task_complete_scale",
    )
    val fillColor by animateColorAsState(
        targetValue = if (active) palette.accent else Color.Transparent,
        animationSpec = tween(240),
        label = "task_complete_fill",
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) palette.accent else palette.muted.copy(alpha = 0.5f),
        animationSpec = tween(240),
        label = "task_complete_border",
    )
    val checkAlpha by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(180),
        label = "task_complete_check",
    )
    Box(
        modifier = modifier
            .size(30.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .background(fillColor)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onComplete,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checkAlpha > 0.01f) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = "Mark complete",
                tint = palette.accentOn.copy(alpha = checkAlpha),
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { alpha = checkAlpha },
            )
        }
    }
}

@Composable
private fun BoopHeaderIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    loading: Boolean = false,
    filled: Boolean = false,
) {
    val palette = LocalBoopPalette.current
    Surface(
        modifier = modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (filled) palette.accent else palette.surfaceElevated,
        shadowElevation = if (filled) 6.dp else 2.dp,
        border = when {
            filled -> BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.4f))
            else -> BorderStroke(1.dp, palette.muted.copy(alpha = 0.22f))
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = if (filled) palette.accentOn else palette.accent,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    icon,
                    contentDescription = contentDescription,
                    tint = when {
                        filled -> palette.accentOn
                        iconTint != null -> iconTint
                        else -> palette.muted
                    },
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun BoopAccentTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = palette.accentGlow.copy(alpha = 0.16f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, palette.accent.copy(alpha = 0.48f)),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.accent,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun BoopCalendarDayCell(
    label: String,
    isSelected: Boolean,
    isToday: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalBoopPalette.current
    val interaction = remember(label, isSelected) { MutableInteractionSource() }
    val background = when {
        isSelected -> palette.accent
        isToday -> palette.navPill
        else -> palette.surfaceVariant
    }
    val textColor = when {
        isSelected -> palette.accentOn
        isToday -> palette.accent
        else -> palette.onBackground
    }
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.dp, palette.accent.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                } else {
                    Modifier
                },
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onSelect),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun BoopWhiteButton(label: String, onClick: () -> Unit) {
    val palette = LocalBoopPalette.current
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.accent,
            contentColor = palette.accentOn,
        ),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = palette.accentOn)
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

    fun readThemeMode(): ThemeMode = ThemeMode.fromStorage(pref().getString("theme_mode", null))
    fun saveThemeMode(mode: ThemeMode) = pref().edit().putString("theme_mode", mode.storageKey).apply()

    fun readShowHabitsPage(): Boolean = pref().getBoolean("show_habits_page", true)
    fun saveShowHabitsPage(show: Boolean) = pref().edit().putBoolean("show_habits_page", show).apply()

    fun readShowWalletPage(): Boolean = pref().getBoolean("show_wallet_page", true)
    fun saveShowWalletPage(show: Boolean) = pref().edit().putBoolean("show_wallet_page", show).apply()
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
                snap.getString("accounts")?.let { store.save("accounts", it) }
                snap.getString("ledgerEntries")?.let { store.save("ledgerEntries", it) }
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

private fun nextRepeatReminderMillis(currentReminderAt: Long, repeatEveryDays: Int): Long {
    val step = repeatEveryDays * 24L * 60L * 60L * 1000L
    var next = currentReminderAt + step
    while (next <= System.currentTimeMillis()) next += step
    return next
}

private fun formatNoteCardTime(note: BoopNote): String {
    val fmt = SimpleDateFormat("MMM d · h:mm a", Locale.US)
    val created = if (note.createdAtMillis > 0L) note.createdAtMillis else note.updatedAtMillis
    val modified = note.updatedAtMillis
    return if (modified > created + 60_000L) {
        "Modified ${fmt.format(modified)}"
    } else {
        "Created ${fmt.format(modified)}"
    }
}

private fun linkedNotePreviewForTask(task: BoopTask): String {
    val noteId = task.linkedNoteId ?: return ""
    return try {
        val notes = JSONArray(LocalStore.read("notes"))
        for (i in 0 until notes.length()) {
            val item = notes.getJSONObject(i)
            if (item.optString("id") != noteId) continue
            if (item.optBoolean("archived", false)) return ""
            val bodyPlain = HtmlCompat.fromHtml(item.optString("body"), HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()
                .replace('\n', ' ')
                .trim()
            val snippet = bodyPlain.ifBlank { item.optString("title").trim() }
                .take(140)
                .trim()
            return if (snippet.isBlank()) "" else snippet
        }
        ""
    } catch (_: Throwable) {
        ""
    }
}

object ReminderScheduler {
    fun schedule(context: Context, task: BoopTask) {
        val notePreview = linkedNotePreviewForTask(task)
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("title", task.title)
            if (notePreview.isNotBlank()) putExtra("subtitle", notePreview)
            putExtra("id", task.id.hashCode())
            putExtra("taskId", task.id)
            putExtra("eventId", -1L)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (task.done || task.archived) {
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
    private const val DEFAULT_BEFORE_MILLIS = 30L * 60L * 1000L

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
                putExtra("title", title)
                putExtra("id", requestCode)
                putExtra("taskId", "")
                putExtra("eventId", eventId)
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

    /** Schedule one lightweight reminder for each visible calendar event (Boop + Google). */
    fun scheduleFromVisibleEvents(context: Context, events: List<CalendarEventUi>) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        events.forEach { event ->
            val at = event.beginMillis - DEFAULT_BEFORE_MILLIS
            if (at <= now) return@forEach
            val requestCode = (((event.id and 0x7FFFFFFF) * 37L) + (event.beginMillis / 60_000L)).toInt()
            val source = if (event.calendarDisplayName.isNotBlank()) event.calendarDisplayName else "Calendar"
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                putExtra("title", event.title)
                putExtra("subtitle", "From $source")
                putExtra("id", requestCode)
                putExtra("taskId", "")
                putExtra("eventId", event.id)
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
        val title = intent.getStringExtra("title") ?: "Reminder"
        val id = intent.getIntExtra("id", 1)
        val taskId = intent.getStringExtra("taskId").orEmpty()
        val subtitle = intent.getStringExtra("subtitle").orEmpty()
        val eventId = intent.getLongExtra("eventId", -1L)
        ReminderNotifier.show(context, id, title, taskId, subtitle, eventId)
    }
}

object ReminderNotifier {
    private const val CHANNEL = "boop_reminders"
    const val ACTION_COMPLETE_TASK = "com.prodash.reminders.ACTION_COMPLETE_TASK"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "BOOP Reminders", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        LocalStore.init(context)
    }

    fun show(context: Context, id: Int, title: String, taskId: String, subtitle: String = "", eventId: Long = -1L) {
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
            .setContentTitle(title.ifBlank { "Reminder" })
            .setContentText(subtitle.ifBlank { "Tap to open" })
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (taskId.isNotBlank()) putExtra("openTaskId", taskId)
            if (eventId > 0L) putExtra("openEventId", eventId)
        }
        val launchPending = PendingIntent.getActivity(
            context,
            id + 20_000,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        builder.setContentIntent(launchPending)
        builder.setStyle(
            androidx.core.app.NotificationCompat.BigTextStyle().bigText(
                subtitle.ifBlank { title },
            ),
        )
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
                val archived = item.optBoolean("archived", false)
                if (repeatEveryDays > 0) {
                    val base = item.optLong("reminderAt", System.currentTimeMillis())
                    val nextAt = nextRepeatReminderMillis(base, repeatEveryDays)
                    item.put("reminderAt", nextAt)
                    item.put("done", false)
                    changed = true
                    rescheduleTask = BoopTask(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        reminderAt = nextAt,
                        done = false,
                        repeatEveryDays = repeatEveryDays,
                        linkedNoteId = item.optString("linkedNoteId").ifBlank { null },
                        archived = archived,
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
