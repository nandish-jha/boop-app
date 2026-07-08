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
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.Settings
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.StickyNote2
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
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Label
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalView
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.Dp
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.hypot
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    companion object {
        var pendingShortcutAction: String? = null
        var pendingOpenTaskIdAction: String? = null
        var pendingOpenEventIdAction: Long = -1L
        var openTargetNonce: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ReminderNotifier.createChannel(this)
        pendingShortcutAction = intent?.getStringExtra("boop_action")
        pendingOpenTaskIdAction = intent?.getStringExtra("openTaskId")
        pendingOpenEventIdAction = intent?.getLongExtra("openEventId", -1L) ?: -1L
        if (!pendingOpenTaskIdAction.isNullOrBlank() || pendingOpenEventIdAction > 0L) {
            openTargetNonce++
        }
        setContent { BoopApp() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingShortcutAction = intent.getStringExtra("boop_action")
        pendingOpenTaskIdAction = intent.getStringExtra("openTaskId")
        pendingOpenEventIdAction = intent.getLongExtra("openEventId", -1L)
        if (!pendingOpenTaskIdAction.isNullOrBlank() || pendingOpenEventIdAction > 0L) {
            openTargetNonce++
        }
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
        val details: String = "",
        val subtasksJson: String = "",
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
        val openingBalance: Double = 0.0,
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


enum class BoopTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    NOTES("Notes", Icons.Rounded.StickyNote2),
    REMINDERS("Reminders", Icons.Rounded.Notifications),
    CALENDAR("Calendar", Icons.Rounded.CalendarMonth),
    HABITS("Habits", Icons.Rounded.Flag),
    WALLET("Wallet", Icons.Rounded.AttachMoney),
}

private fun buildVisibleTabs(showHabitsPage: Boolean, showWalletPage: Boolean): List<BoopTab> = buildList {
    add(BoopTab.HOME)
    add(BoopTab.REMINDERS)
    add(BoopTab.NOTES)
    add(BoopTab.CALENDAR)
    if (showHabitsPage) add(BoopTab.HABITS)
    if (showWalletPage) add(BoopTab.WALLET)
}

internal data class BoopPalette(
    val background: Color,
    val phoneBg: Color,
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
    val topbarBg: Color,
    val chipBg: Color,
    val surfaceBorder: Color,
    val sheetBg: Color,
    val overlay: Color,
    val sheetHandle: Color,
)

/** Classic terracotta + rose glow palette pair. */
private fun boopTerracottaDarkPalette() = BoopPalette(
    background = Color(0xFF141413),
    phoneBg = Color(0xFF1A1918),
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
    topbarBg = Color(0xD91A1918),
    chipBg = Color(0xFF252320),
    surfaceBorder = Color(0x1AFAF9F5),
    sheetBg = Color(0xFF30302E),
    overlay = Color(0x8C000000),
    sheetHandle = Color(0x33FAF9F5),
)

private fun boopTerracottaLightPalette() = BoopPalette(
    background = Color(0xFFFAF9F5),
    phoneBg = Color(0xFFFFFDF8),
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
    topbarBg = Color(0xEBFFFDF8),
    chipBg = Color(0xFFE8E6DC),
    surfaceBorder = Color(0x14141313),
    sheetBg = Color(0xFFF5F4ED),
    overlay = Color(0x59141313),
    sheetHandle = Color(0x26141313),
)

/** Unified warm dark — matches Boop Unified.html */
private fun boopDarkPalette() = BoopPalette(
    background = Color(0xFF0C0B10),
    phoneBg = Color(0xFF141210),
    surface = Color(0xFF2E2B28),
    surfaceVariant = Color(0xFF3A3632),
    surfaceElevated = Color(0xFF2E2B28),
    onBackground = Color(0xFFFAF6F0),
    muted = Color(0xFFA8A098),
    accent = Color(0xFFC08078),
    accentGlow = Color(0xFFE8A898),
    accentOn = Color(0xFFFFFFFF),
    navPill = Color(0x33E8A898),
    navSelected = Color(0xFFC08078),
    navUnselected = Color(0xFFA8A098),
    inputField = Color(0xFF242120),
    danger = Color(0xFFFF8A80),
    recording = Color(0xFFC08078),
    quoteFill = Color(0xFF2E2B28),
    quoteStroke = Color(0xFFC08078),
    topbarBg = Color(0xD9141210),
    chipBg = Color(0xFF3A3632),
    surfaceBorder = Color(0x1AFAF6F0),
    sheetBg = Color(0xFF2E2B28),
    overlay = Color(0x8C000000),
    sheetHandle = Color(0x33FAF6F0),
)

/** Unified warm light — matches Boop Unified.html */
private fun boopLightPalette() = BoopPalette(
    background = Color(0xFFEFE9DF),
    phoneBg = Color(0xFFFBF7F1),
    surface = Color(0xFFFFFCF9),
    surfaceVariant = Color(0xFFEDE6DC),
    surfaceElevated = Color(0xFFFFFCF9),
    onBackground = Color(0xFF1A1612),
    muted = Color(0xFF8A8278),
    accent = Color(0xFFC08078),
    accentGlow = Color(0xFFE8A898),
    accentOn = Color(0xFFFFFFFF),
    navPill = Color(0x28E8A898),
    navSelected = Color(0xFFC08078),
    navUnselected = Color(0xFF8A8278),
    inputField = Color(0xFFFBF7F1),
    danger = Color(0xFFC45850),
    recording = Color(0xFFC08078),
    quoteFill = Color(0xFFFFFCF9),
    quoteStroke = Color(0xFFC08078),
    topbarBg = Color(0xEBFBF7F1),
    chipBg = Color(0xFFEDE6DC),
    surfaceBorder = Color(0x141A1612),
    sheetBg = Color(0xFFFFFCF9),
    overlay = Color(0x591A1612),
    sheetHandle = Color(0x261A1612),
)

internal val LocalBoopPalette = staticCompositionLocalOf { boopDarkPalette() }
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
    var createSheetOpen by remember { mutableStateOf(false) }
    var calendarSyncRequest by rememberSaveable { mutableIntStateOf(0) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var dashboardSearchOpen by rememberSaveable { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf(LocalStore.readThemeMode()) }
    var paletteFamily by remember { mutableStateOf(LocalStore.readPaletteFamily()) }
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
    val palette = when (paletteFamily) {
        PaletteFamily.TERRACOTTA -> if (useDarkTheme) boopTerracottaDarkPalette() else boopTerracottaLightPalette()
        PaletteFamily.AMOLED -> if (useDarkTheme) boopDarkPalette() else boopLightPalette()
    }
    val view = LocalView.current
    DisposableEffect(useDarkTheme) {
        val window = (view.context as Activity).window
        androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !useDarkTheme
            isAppearanceLightNavigationBars = !useDarkTheme
        }
        onDispose {}
    }
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
        repository.ensureSession(
            onRemoteLoaded = { refresh() },
            onFailure = { /* status shown in Settings via BoopSyncState */ },
        )
        refresh()
    }

    var pendingShortcut by remember { mutableStateOf(MainActivity.pendingShortcutAction) }

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
            details = task?.details.orEmpty(),
            subtasksJson = task?.subtasksJson.orEmpty(),
        )
        createSheetOpen = false
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
            linkedTaskId = null,
            archived = note?.archived ?: false,
            createdAtMillis = note?.createdAtMillis ?: 0L,
            updatedAtMillis = note?.updatedAtMillis ?: 0L,
        )
        createSheetOpen = false
    }

    fun openHabitSheet(habit: BoopHabit? = null) {
        itemSheet = ItemSheet.HabitSheet(
            id = habit?.id,
            sessionKey = habit?.id ?: UUID.randomUUID().toString(),
            title = habit?.title.orEmpty(),
            dayPeriodCategory = habit?.dayPeriodCategory ?: "morning",
            goal = habit?.goal ?: 30,
            progress = habit?.progress ?: 0,
            dayKeys = habit?.dayKeys.orEmpty(),
            quantityMode = habit?.quantityMode ?: false,
            quantityUnit = habit?.quantityUnit.orEmpty(),
            quantityDailyTarget = habit?.quantityDailyTarget ?: 30,
            quantityDayValues = habit?.quantityDayValues.orEmpty(),
        )
        createSheetOpen = false
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
        createSheetOpen = false
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
        createSheetOpen = false
    }
    fun openAccountSheet(account: BoopAccount? = null) {
        itemSheet = ItemSheet.AccountSheet(
            id = account?.id,
            sessionKey = account?.id ?: UUID.randomUUID().toString(),
            name = account?.name.orEmpty(),
            openingBalance = account?.openingBalance ?: 0.0,
        )
        createSheetOpen = false
    }

    LaunchedEffect(pendingShortcut) {
        when (pendingShortcut) {
            "voice" -> {
                openNoteSheet(null)
                pendingShortcut = null
            }
            "new_task" -> {
                openTaskSheet(null)
                pendingShortcut = null
            }
            "new_note" -> {
                openNoteSheet(null)
                pendingShortcut = null
            }
        }
        MainActivity.pendingShortcutAction = null
    }

    val context = LocalContext.current
    val launchActivity = context as? Activity
    var pendingOpenTaskId by rememberSaveable {
        mutableStateOf(MainActivity.pendingOpenTaskIdAction ?: launchActivity?.intent?.getStringExtra("openTaskId"))
    }
    var pendingOpenEventId by rememberSaveable {
        mutableLongStateOf(
            if (MainActivity.pendingOpenEventIdAction > 0L) {
                MainActivity.pendingOpenEventIdAction
            } else {
                launchActivity?.intent?.getLongExtra("openEventId", -1L) ?: -1L
            },
        )
    }
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
        createSheetOpen = false
    }

    fun openEventSheetById(eventId: Long) {
        val detail = readCalendarEventDetail(context, eventId)
        openEventSheet(existing = detail)
    }


    fun toggleThemeMode() {
        themeMode = when (themeMode) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> if (useDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK
        }
        LocalStore.saveThemeMode(themeMode)
    }

    val openTargetNonce = MainActivity.openTargetNonce
    LaunchedEffect(openTargetNonce) {
        MainActivity.pendingOpenTaskIdAction?.let { pendingOpenTaskId = it }
        if (MainActivity.pendingOpenEventIdAction > 0L) {
            pendingOpenEventId = MainActivity.pendingOpenEventIdAction
        }
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
            background = palette.phoneBg,
            surface = palette.surface,
            surfaceVariant = palette.surfaceVariant,
            onBackground = palette.onBackground,
            onSurface = palette.onBackground,
            onSurfaceVariant = palette.muted,
            primary = palette.accent,
            onPrimary = palette.accentOn,
            secondary = palette.surfaceElevated,
            onSecondary = palette.onBackground,
            outline = palette.surfaceBorder,
        )
    } else {
        lightColorScheme(
            background = palette.phoneBg,
            surface = palette.surface,
            surfaceVariant = palette.surfaceVariant,
            onBackground = palette.onBackground,
            onSurface = palette.onBackground,
            onSurfaceVariant = palette.muted,
            primary = palette.accent,
            onPrimary = palette.accentOn,
            secondary = palette.surfaceElevated,
            onSecondary = palette.onBackground,
            outline = palette.surfaceBorder,
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
                createSheetOpen = false
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
                habitCheckInOpen -> habitCheckInOpen = false
                itemSheet != null -> itemSheet = null
                createSheetOpen -> createSheetOpen = false
                dashboardSearchOpen -> dashboardSearchOpen = false
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
        UnifiedAppChrome(
            bottomNav = {
                if (!settingsOpen) {
                    UnifiedBottomNav(
                        tabs = visibleTabs,
                        selectedIndex = selectedTab.coerceIn(0, visibleTabs.lastIndex.coerceAtLeast(0)),
                        onSelectTab = {
                            selectTabIndex(it)
                            createSheetOpen = false
                        },
                        onAdd = {
                            when (visibleTabs.getOrNull(selectedTab)) {
                                BoopTab.NOTES -> openNoteSheet(null)
                                BoopTab.REMINDERS -> openTaskSheet(null)
                                BoopTab.CALENDAR -> openEventSheet(startAt = calendarCreateAtMillis)
                                BoopTab.HABITS -> openHabitSheet(null)
                                BoopTab.WALLET -> openFinanceEntrySheet("expense")
                                else -> createSheetOpen = true
                            }
                        },
                    )
                }
            },
            overlay = {
                UnifiedCreateSheet(
                    open = createSheetOpen && !settingsOpen,
                    onDismiss = { createSheetOpen = false },
                    options = defaultCreateOptions(
                        showHabits = showHabitsPage,
                        showWallet = showWalletPage,
                        onNote = { openNoteSheet(null) },
                        onReminder = { openTaskSheet(null) },
                        onEvent = { openEventSheet(startAt = calendarCreateAtMillis) },
                        onHabit = { openHabitSheet(null) },
                        onWallet = { openFinanceEntrySheet("expense") },
                        onAccount = { openAccountSheet(null) },
                    ),
                )
            },
        ) {
                Box(
                    Modifier
                        .fillMaxSize()
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
                            onEditAccountBalance = { account ->
                                repository.saveAccount(account)
                                refresh()
                            },
                            onAddAccount = { openAccountSheet(null) },
                            onAddTransaction = { openFinanceEntrySheet("expense") },
                            onOpenSettings = {
                                createSheetOpen = false
                                settingsOpen = true
                            },
                            onToggleTheme = { toggleThemeMode() },
                            darkTheme = useDarkTheme,
                            dashboardSearchOpen = dashboardSearchOpen,
                            onDashboardSearchOpenChange = { dashboardSearchOpen = it },
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
                            repository = repository,
                            themeMode = themeMode,
                            onThemeModeChange = { mode ->
                                themeMode = mode
                                LocalStore.saveThemeMode(mode)
                            },
                            paletteFamily = paletteFamily,
                            onPaletteFamilyChange = { family ->
                                paletteFamily = family
                                LocalStore.savePaletteFamily(family)
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
                            onDataRefresh = { refresh() },
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
                val isKeepEditor = sheet is ItemSheet.NoteSheet ||
                    sheet is ItemSheet.TaskSheet ||
                    sheet is ItemSheet.EventSheet ||
                    sheet is ItemSheet.HabitSheet ||
                    sheet is ItemSheet.AccountSheet
                ModalBottomSheet(
                    onDismissRequest = { itemSheet = null },
                    sheetState = sheetState,
                    containerColor = palette.phoneBg,
                    dragHandle = if (isKeepEditor) null else {
                        { BottomSheetDefaults.DragHandle(color = palette.muted) }
                    },
                    shape = RoundedCornerShape(topStart = if (isKeepEditor) 0.dp else 20.dp, topEnd = if (isKeepEditor) 0.dp else 20.dp),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(if (isKeepEditor) 1f else 0.9f)
                            .padding(horizontal = if (isKeepEditor) 0.dp else 20.dp)
                            .padding(bottom = if (isKeepEditor) 0.dp else 28.dp)
                            .imePadding()
                            .then(
                                if (isKeepEditor) Modifier else Modifier.verticalScroll(rememberScrollState()),
                            ),
                    ) {
                        when (sheet) {
                            is ItemSheet.TaskSheet -> {
                                val persistTask: (BoopTask) -> Unit = { task ->
                                    repository.saveTask(task)
                                    ReminderScheduler.schedule(AppContextHolder.context, task)
                                    refresh()
                                }
                                TaskEditorSheet(
                                initial = sheet,
                                notes = notes.filter { !it.archived }.sortedByDescending { it.createdAtMillis + it.updatedAtMillis },
                                onDismiss = { itemSheet = null },
                                onDelete = sheet.id?.let { id ->
                                    {
                                        ReminderScheduler.cancel(AppContextHolder.context, id)
                                        repository.deleteTask(id)
                                        refresh()
                                        itemSheet = null
                                    }
                                },
                                onSaveNote = { note ->
                                    repository.saveNote(note)
                                },
                                onPersist = persistTask,
                                onSave = { task ->
                                    persistTask(task)
                                    itemSheet = null
                                },
                                onOpenLinkedNote = { note -> openNoteSheet(note) },
                            )
                            }
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
    onEditAccountBalance: (BoopAccount) -> Unit,
    onAddAccount: () -> Unit,
    onAddTransaction: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTheme: () -> Unit,
    darkTheme: Boolean,
    dashboardSearchOpen: Boolean,
    onDashboardSearchOpenChange: (Boolean) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize(),
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
                onSearchPickTask = { onSelectTab(BoopTab.REMINDERS); onEditTask(it) },
                onSearchPickNote = { onSelectTab(BoopTab.NOTES); onEditNote(it) },
                onSearchPickHabit = {
                    if (visibleTabs.contains(BoopTab.HABITS)) {
                        onSelectTab(BoopTab.HABITS)
                    }
                    onEditHabit(it)
                },
                onOpenSettings = onOpenSettings,
                onToggleTheme = onToggleTheme,
                darkTheme = darkTheme,
                searchExpanded = dashboardSearchOpen,
                onSearchExpandedChange = onDashboardSearchOpenChange,
            )
            BoopTab.NOTES -> NotesListScreen(
                notes = notes,
                onOpenNote = onEditNote,
                title = "Notes",
            )
            BoopTab.REMINDERS -> TaskListScreen(
                tasks = tasks,
                onOpenTask = onEditTask,
                onArchiveTask = onArchiveTask,
                onCompleteTask = onCompleteTask,
                onUnarchiveTask = onUnarchiveTask,
                onRestoreCompletedTask = onRestoreCompletedTask,
                title = "Reminders",
            )
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
                onEditAccountBalance = onEditAccountBalance,
                onAddAccount = onAddAccount,
                onAddTransaction = onAddTransaction,
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    repository: BoopRepository,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    paletteFamily: PaletteFamily,
    onPaletteFamilyChange: (PaletteFamily) -> Unit,
    showHabitsPage: Boolean,
    onShowHabitsPageChange: (Boolean) -> Unit,
    showWalletPage: Boolean,
    onShowWalletPageChange: (Boolean) -> Unit,
    onDataRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    val context = LocalContext.current
    var syncBusy by remember { mutableStateOf(false) }
    var authBusy by remember { mutableStateOf(false) }
    var authUid by remember { mutableStateOf(BoopSyncState.signedInUid ?: repository.currentUserId()) }
    var syncStatusTick by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            authUid = auth.currentUser?.uid
            BoopSyncState.signedInUid = auth.currentUser?.uid
            syncStatusTick++
        }
        FirebaseAuth.getInstance().addAuthStateListener(listener)
        onDispose { FirebaseAuth.getInstance().removeAuthStateListener(listener) }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(repository.exportBackupJson().toByteArray(Charsets.UTF_8))
            }
            Toast.makeText(context, "Backup exported", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText().orEmpty()
            if (repository.importBackupJson(raw)) {
                onDataRefresh()
                Toast.makeText(context, "Backup imported", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Invalid backup file", Toast.LENGTH_SHORT).show()
            }
        }.onFailure {
            Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(palette.background)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 24.dp)
            .verticalScroll(rememberScrollState()),
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
        Text("Appearance", style = MaterialTheme.typography.titleMedium, color = palette.onBackground)
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
                        Text(mode.label, color = palette.onBackground, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Color palette", style = MaterialTheme.typography.titleSmall, color = palette.muted)
        Spacer(Modifier.height(6.dp))
        PaletteFamily.entries.forEach { family ->
            val selected = paletteFamily == family
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onPaletteFamilyChange(family) },
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
                        onClick = { onPaletteFamilyChange(family) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = palette.onBackground,
                            unselectedColor = palette.muted,
                        ),
                    )
                    Text(family.label, color = palette.onBackground, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Account & sync", style = MaterialTheme.typography.titleMedium, color = palette.onBackground)
        Spacer(Modifier.height(8.dp))
        val syncLabel = remember(syncStatusTick, BoopSyncState.lastSyncMillis, BoopSyncState.lastSyncError, BoopSyncState.lastSyncOk) {
            when {
                BoopSyncState.lastSyncOk && BoopSyncState.lastSyncMillis > 0L ->
                    "Last synced ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(BoopSyncState.lastSyncMillis)}"
                BoopSyncState.lastSyncError != null -> BoopSyncState.lastSyncError!!
                else -> "Local data ready — tap Sync now to back up"
            }
        }
        Text(syncLabel, color = palette.muted, style = MaterialTheme.typography.bodySmall)
        Text(
            if (authUid != null) "Account ID: ${authUid!!.take(8)}…" else "Not signed in to cloud yet",
            color = palette.muted,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            "Tasks and notes always save on this device, even if cloud sync fails.",
            color = palette.muted,
            style = MaterialTheme.typography.labelSmall,
        )
        Spacer(Modifier.height(8.dp))
        if (authUid == null) {
            BoopWhiteButton(if (authBusy) "Signing in…" else "Retry sign-in") {
                if (authBusy) return@BoopWhiteButton
                authBusy = true
                repository.ensureAnonymousAuth { ok, error ->
                    authBusy = false
                    syncStatusTick++
                    if (ok) {
                        Toast.makeText(context, "Signed in", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, error ?: "Sign-in failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        BoopWhiteButton(if (syncBusy) "Syncing…" else "Sync now") {
            if (syncBusy) return@BoopWhiteButton
            syncBusy = true
            repository.pushAllToCloud { ok, error ->
                syncBusy = false
                syncStatusTick++
                if (ok) {
                    Toast.makeText(context, "Synced to cloud", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, error ?: "Sync failed", Toast.LENGTH_LONG).show()
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        BoopWhiteButton("Export backup") {
            exportLauncher.launch("boop-backup-${System.currentTimeMillis()}.json")
        }
        Spacer(Modifier.height(8.dp))
        BoopWhiteButton("Import backup") {
            importLauncher.launch(arrayOf("application/json", "text/*"))
        }
        Spacer(Modifier.height(24.dp))
        Text("Reminders", style = MaterialTheme.typography.titleMedium, color = palette.onBackground)
        Spacer(Modifier.height(8.dp))
        SettingsActionRow(
            title = "Exact alarm permission",
            subtitle = "Helps reminders fire on time (Android 12+)",
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        })
                    }
                } else {
                    Toast.makeText(context, "Exact alarms are allowed on this Android version", Toast.LENGTH_SHORT).show()
                }
            },
        )
        Spacer(Modifier.height(24.dp))
        Text("Voice", style = MaterialTheme.typography.titleMedium, color = palette.onBackground)
        Spacer(Modifier.height(8.dp))
        AssistantSetupRow()
        Spacer(Modifier.height(24.dp))
        Text("Navigation", style = MaterialTheme.typography.titleMedium, color = palette.onBackground)
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
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(title, color = palette.onBackground, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, color = palette.muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AssistantSetupRow() {
    val palette = LocalBoopPalette.current
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val rowShape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val roleIntent = Intent("android.app.action.REQUEST_ROLE").apply {
                    putExtra("android.app.extra.ROLE_NAME", "android.app.role.ASSISTANT")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    roleIntent.resolveActivity(context.packageManager) != null
                ) {
                    launcher.launch(roleIntent)
                } else {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                }
            },
        shape = rowShape,
        color = palette.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Power button voice",
                style = MaterialTheme.typography.titleSmall,
                color = palette.onBackground,
            )
            Text(
                "Tap above, then choose BOOP in the assistant list. After that, long-press the power button opens BOOP voice capture.",
                color = palette.muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
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
    val rowShape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = rowShape,
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
            .height(76.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = palette.surfaceElevated,
                shadowElevation = 0.dp,
                border = BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.2f)),
                modifier = Modifier,
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
                    .size(52.dp)
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
                            BoopTab.NOTES, BoopTab.REMINDERS -> onOpenTask()
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
            val menuShape = RoundedCornerShape(22.dp)
            Surface(
                shape = menuShape,
                color = palette.surfaceElevated.copy(alpha = 0.98f),
                shadowElevation = 0.dp,
                border = BorderStroke(1.dp, palette.muted.copy(alpha = 0.18f)),
                modifier = Modifier
                    .graphicsLayer {
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
            .size(52.dp)
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
        shadowElevation = 0.dp,
        border = when {
            filled -> BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.45f))
            listening -> BorderStroke(1.dp, palette.recording.copy(alpha = 0.4f))
            else -> BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.18f))
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
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
    Surface(
        modifier = Modifier
            .size(48.dp)
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
        shadowElevation = 0.dp,
        border = if (progress > 0.5f) {
            BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.38f))
        } else {
            BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.16f))
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(23.dp),
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

private fun noteBodyHasVisibleContent(html: String): Boolean =
    plainNoteSnippet(html, 1).isNotBlank()

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
    LaunchedEffect(link) {
        title = fetchWebTitle(link)
    }
    KeepLinkPreviewCard(
        link = link,
        title = title,
        onOpen = {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
            } catch (_: Throwable) {
            }
        },
    )
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
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, palette.surfaceBorder),
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                color = palette.muted,
            )
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = BoopSerifFamily,
                    fontSize = 22.sp,
                ),
                color = palette.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (caption.isNotBlank()) {
                Text(caption, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = palette.muted)
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

private data class HomeCardMeta(
    val type: UnifiedItemType,
    val title: String,
    val meta: String?,
    val body: String?,
)

@Composable
private fun DashboardCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = palette.chipBg,
        border = BorderStroke(1.dp, palette.surfaceBorder),
        modifier = Modifier.size(38.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = palette.onBackground, modifier = Modifier.size(18.dp))
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
    onToggleTheme: () -> Unit,
    darkTheme: Boolean,
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
        accounts.associate { it.id to it.openingBalance }.toMutableMap().apply {
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
    var homeFilter by rememberSaveable { mutableStateOf("all") }
    val homeFilterChips = listOf(
        "all" to "All",
        "note" to "Notes",
        "reminder" to "Reminders",
        "calendar" to "Calendar",
        "habit" to "Habits",
        "wallet" to "Wallet",
    )
    val homeGridItems = remember(homeFilter, tasks, notes, habits, ledgerEntries, epoch) {
        buildList {
            if (homeFilter == "all" || homeFilter == "reminder") {
                tasks.filter { !it.done && !it.archived }.sortedBy { it.reminderAt }.take(6).forEach { task ->
                    val subtaskCount = parseSubtasksJson(task.subtasksJson).size
                    val taskBody = linkedNoteLabelForTask(task)
                        ?: task.details.trim().ifBlank {
                            if (subtaskCount > 0) "$subtaskCount subtask${if (subtaskCount == 1) "" else "s"}" else ""
                        }.takeIf { it.isNotBlank() }
                    add(
                        HomeCardMeta(
                            UnifiedItemType.REMINDER,
                            task.title,
                            formatTaskReminderLine(task.reminderAt),
                            taskBody,
                        ) to { onOpenTask(task) },
                    )
                }
            }
            if (homeFilter == "all" || homeFilter == "note") {
                notes.filter { !it.archived }.sortedByDescending { it.createdAtMillis + it.updatedAtMillis }.take(6).forEach { note ->
                    add(
                        HomeCardMeta(
                            UnifiedItemType.NOTE,
                            note.title.ifBlank { "Untitled" },
                            formatNoteCardTime(note),
                            plainNoteSnippet(note.body, 64).takeIf { noteBodyHasVisibleContent(note.body) },
                        ) to { onOpenNote(note) },
                    )
                }
            }
            if (homeFilter == "all" || homeFilter == "habit") {
                activeHabits.filterNot { habit ->
                    if (habit.quantityMode) {
                        val todayAmount = parseHabitDayValues(habit.quantityDayValues)[todayKey] ?: 0
                        todayAmount >= habit.quantityDailyTarget.coerceAtLeast(1)
                    } else {
                        todayKey in parseHabitDayKeys(habit.dayKeys)
                    }
                }.take(4).forEach { habit ->
                    add(
                        HomeCardMeta(
                            UnifiedItemType.HABIT,
                            habit.title,
                            "${habit.progress}/${habit.goal}",
                            "${habitCategoryLabel(habit.dayPeriodCategory)} · not checked in",
                        ) to { onOpenHabit(habit) },
                    )
                }
            }
            if ((homeFilter == "all" || homeFilter == "wallet") && accounts.isNotEmpty()) {
                ledgerEntries.sortedByDescending { it.createdAtMillis }.take(4).forEach { entry ->
                    val amountText = when (entry.type) {
                        "expense" -> formatSignedCadDelta(entry.amount, positive = false)
                        else -> formatSignedCadDelta(entry.amount, positive = true)
                    }
                    add(
                        HomeCardMeta(
                            UnifiedItemType.WALLET,
                            entry.title.ifBlank { ledgerTypeLabel(entry.type) },
                            amountText,
                            ledgerTypeLabel(entry.type),
                        ) to { /* wallet entries open via tab */ },
                    )
                }
            }
        }.take(12)
    }
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.surfaceVariant,
                    border = BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.14f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = greetingLine,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = BoopSerifFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 27.sp,
                                    ),
                                    color = palette.onBackground,
                                )
                                Text(
                                    text = dateLine,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                    color = palette.muted,
                                )
                            }
                            DashboardCircleButton(
                                icon = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                onClick = onOpenSettings,
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DashboardStatCard(
                                label = "Today",
                                value = tasksDueToday.size.toString(),
                                caption = "due",
                                modifier = Modifier.weight(1f),
                            )
                            DashboardStatCard(
                                label = "Habits",
                                value = if (activeHabits.isEmpty()) "—" else "$habitsDoneToday/${activeHabits.size}",
                                caption = "done",
                                modifier = Modifier.weight(1f),
                            )
                            DashboardStatCard(
                                label = "Balance",
                                value = if (accounts.isNotEmpty()) formatCadAmountNumber(netBalance, decimals = 0) else notes.count { !it.archived }.toString(),
                                caption = if (accounts.isNotEmpty()) "net" else "notes",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                UnifiedFilterChips(
                    chips = homeFilterChips,
                    selected = homeFilter,
                    onSelect = { homeFilter = it },
                )
                if (homeGridItems.isEmpty()) {
                    Text("Nothing here yet.", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        homeGridItems.chunked(2).forEach { rowItems ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowItems.forEach { (meta, onClick) ->
                                    UnifiedTintCard(
                                        type = meta.type,
                                        title = meta.title,
                                        meta = meta.meta,
                                        body = meta.body,
                                        onClick = onClick,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                    )
                                }
                                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
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
    val fieldShape = RoundedCornerShape(14.dp)
    TextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = LocalTextStyle.current.copy(
            fontFamily = BoopSansFamily,
            color = palette.onBackground,
        ),
        modifier = modifier
            .fillMaxWidth(),
        shape = fieldShape,
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
    if (!noteBodyHasVisibleContent(html)) return
    val payload = plainNoteSnippet(html, 8_000)
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
    title: String = "Reminders",
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
            .padding(top = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            BoopPageTitle(title)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
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
        UnifiedSectionLabel("Pending")
        if (activeTasks.isEmpty()) {
            Text("No pending reminders.", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                activeTasks.chunked(2).forEach { rowTasks ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowTasks.forEach { task ->
                            val isCompleting = pendingArchiveTaskId == task.id
                            Box(Modifier.weight(1f)) {
                                UnifiedTintCard(
                                    type = UnifiedItemType.REMINDER,
                                    title = task.title,
                                    meta = formatTaskReminderLine(task.reminderAt),
                                    linkedLabel = linkedNoteLabelForTask(task),
                                    checked = false,
                                    onCheckedChange = {
                                        if (pendingArchiveTaskId != null) return@UnifiedTintCard
                                        pendingArchiveTaskId = task.id
                                        scope.launch {
                                            delay(180)
                                            onCompleteTask(task)
                                            pendingArchiveTaskId = null
                                        }
                                    },
                                    onDelete = {
                                        onArchiveTask(task)
                                    },
                                    onClick = { onOpenTask(task) },
                                    titleDecoration = if (isCompleting) TextDecoration.LineThrough else TextDecoration.None,
                                )
                            }
                        }
                        if (rowTasks.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        if (completedTasks.isNotEmpty()) {
            UnifiedSectionLabel("Completed", modifier = Modifier.padding(top = 8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                completedTasks.take(6).chunked(2).forEach { rowTasks ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowTasks.forEach { task ->
                            UnifiedTintCard(
                                type = UnifiedItemType.REMINDER,
                                title = task.title,
                                meta = formatTaskReminderLine(task.reminderAt),
                                linkedLabel = linkedNoteLabelForTask(task),
                                checked = true,
                                onCheckedChange = { onRestoreCompletedTask(task) },
                                onDelete = { onArchiveTask(task) },
                                onClick = { onOpenTask(task) },
                                titleDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowTasks.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(72.dp))
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
            .padding(top = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            BoopPageTitle("Calendar")
            BoopHeaderIconButton(
                onClick = triggerCalendarSync,
                icon = if (syncSucceeded) Icons.Outlined.CheckCircle else Icons.Outlined.Sync,
                contentDescription = if (syncSucceeded) "Calendar synced" else "Sync with Google Calendar",
                iconTint = palette.accent,
                loading = isSyncing,
            )
        }
        UnifiedWeekStrip(
            selectedMillis = selectedMillis,
            onSelectDay = { selectedMillis = it },
        )
        UnifiedSectionLabel(headerLabel)
        val dayCalendarItems = remember(allDayEvents, dayTasks, timedGoogleEvents) {
            buildList {
                allDayEvents.forEach { event ->
                    add(
                        Triple(
                            UnifiedItemType.CALENDAR,
                            event.title.ifBlank { "All-day event" },
                            "All day · ${event.calendarDisplayName.ifBlank { "Google Calendar" }}",
                        ) to { onOpenEvent(event.id) },
                    )
                }
                dayTasks.forEach { task ->
                    add(
                        Triple(
                            UnifiedItemType.REMINDER,
                            task.title,
                            formatTaskReminderLine(task.reminderAt),
                        ) to { onOpenTask(task) },
                    )
                }
                timedGoogleEvents.forEach { event ->
                    val start = SimpleDateFormat("HH:mm", Locale.US).format(maxOf(event.beginMillis, selectedDay.timeInMillis))
                    val end = SimpleDateFormat("HH:mm", Locale.US).format(minOf(event.endMillis, nextDay.timeInMillis))
                    add(
                        Triple(
                            UnifiedItemType.CALENDAR,
                            event.title.ifBlank { "Event" },
                            "$start – $end · ${event.calendarDisplayName.ifBlank { "Google Calendar" }}",
                        ) to { onOpenEvent(event.id) },
                    )
                }
            }
        }
        if (!calendarGranted) {
            Text("Tap sync to allow Calendar access.", color = palette.muted, style = MaterialTheme.typography.bodySmall)
        } else if (dayCalendarItems.isEmpty()) {
            Text("No events scheduled.", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                dayCalendarItems.chunked(2).forEach { rowItems ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEach { (meta, onClick) ->
                            val (type, title, detail) = meta
                            UnifiedTintCard(
                                type = type,
                                title = title,
                                meta = detail,
                                onClick = onClick,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NotesListScreen(
    notes: List<BoopNote>,
    onOpenNote: (BoopNote) -> Unit,
    title: String = "Notes",
) {
    val palette = LocalBoopPalette.current
    val context = LocalContext.current
    val activeNotes = notes.filter { !it.archived }.sortedByDescending { it.createdAtMillis + it.updatedAtMillis }
    val archivedNotes = notes.filter { it.archived }.sortedByDescending { it.createdAtMillis + it.updatedAtMillis }
    var showArchive by rememberSaveable { mutableStateOf(false) }
    var selectedTag by rememberSaveable { mutableStateOf("All") }
    var previewImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var previewImageIndex by remember { mutableStateOf(-1) }
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
            BoopPageTitle(title)
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
                        color = if (active) palette.accent else palette.surfaceVariant,
                        border = BorderStroke(1.dp, palette.accentGlow.copy(alpha = if (active) 0.22f else 0.1f)),
                        modifier = Modifier.clickable(interactionSource = interaction, indication = null) { selectedTag = tag },
                    ) {
                        Text(
                            text = if (tag == "All") "All" else "#$tag",
                            color = if (active) palette.accentOn else palette.muted,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 4.dp,
                bottom = 72.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (visibleNotes.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text("No notes yet.", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
            items(visibleNotes, key = { it.id }) { note ->
                val images = parseNoteAttachments(note.attachmentUri)
                val tags = parseNoteTags(note.tagsCsv)
                val cardHint = buildString {
                    if (tags.isNotEmpty()) {
                        append(tags.take(2).joinToString(" ") { "#$it" })
                    }
                }.takeIf { it.isNotBlank() }
                UnifiedTintCard(
                    type = UnifiedItemType.NOTE,
                    title = note.title.ifBlank { "Untitled note" },
                    meta = formatNoteCardTime(note),
                    body = plainNoteSnippet(note.body, 72).takeIf { noteBodyHasVisibleContent(note.body) },
                    linkedLabel = cardHint,
                    onClick = { onOpenNote(note) },
                    imageContent = if (images.isNotEmpty()) {
                        {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(76.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        previewImages = images
                                        previewImageIndex = 0
                                    },
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(storedAttachmentForCoil(images.first()))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                if (images.size > 1) {
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = Color.Black.copy(alpha = 0.55f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp),
                                    ) {
                                        Text(
                                            "+${images.size - 1}",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        null
                    },
                )
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
    if (previewImageIndex in previewImages.indices) {
        ImagePreviewOverlay(
            images = previewImages,
            startIndex = previewImageIndex,
            onDismiss = { previewImageIndex = -1 },
        )
    }
}

@Composable
private fun HabitsListScreen(
    habits: List<BoopHabit>,
    onPersistHabit: (BoopHabit) -> Unit,
    onOpenHabit: (BoopHabit) -> Unit,
) {
    val palette = LocalBoopPalette.current
    val epoch = LocalBoopDataEpoch.current
    val sortedHabits = remember(epoch, habits) {
        habits.sortedWith(
            compareBy(
                { habitCategoryOrder(it.dayPeriodCategory) },
                { it.title.lowercase(Locale.getDefault()) },
            ),
        )
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BoopPageTitle("Habits")
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 4.dp,
                bottom = 72.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (sortedHabits.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text("No habits yet.", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(sortedHabits, key = { it.id }) { habit ->
                    HabitWeekStripCard(habit = habit, onPersist = onPersistHabit, onOpenHabit = onOpenHabit)
                }
            }
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
    val cardShape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = cardShape,
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
    val panelShape = RoundedCornerShape(18.dp)
    Surface(
        shape = panelShape,
        color = palette.surface,
        border = BorderStroke(1.dp, palette.muted.copy(alpha = 0.14f)),
        modifier = Modifier
            .fillMaxWidth(),
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
    val rowShape = RoundedCornerShape(16.dp)
    Card(
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        shape = rowShape,
        border = BorderStroke(1.dp, palette.muted.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
private fun WalletAccountRow(
    name: String,
    balance: Double,
    onCorrectBalance: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    val dark = palette.background.red + palette.background.green + palette.background.blue < 0.35f
    val colors = unifiedTypeColors(UnifiedItemType.WALLET, dark)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.bg,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(name, color = palette.onBackground, style = MaterialTheme.typography.titleSmall.copy(fontFamily = BoopSerifFamily), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    formatCadAmount(balance),
                    color = if (balance >= 0) colors.accent else palette.danger,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            KeepToolbarIconButton(onClick = onCorrectBalance, icon = Icons.Outlined.Tune, contentDescription = "Correct balance", tint = palette.muted)
            KeepToolbarIconButton(onClick = onEdit, icon = Icons.Outlined.Edit, contentDescription = "Edit account", tint = palette.muted)
            KeepToolbarIconButton(onClick = onDelete, icon = Icons.Outlined.Delete, contentDescription = "Delete account", tint = palette.danger)
        }
    }
}

@Composable
private fun WalletTransactionRow(
    title: String,
    meta: String,
    amount: String,
    amountColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.surfaceBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = palette.onBackground, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(meta, color = palette.muted, style = MaterialTheme.typography.labelSmall)
            }
            Text(amount, color = amountColor, style = MaterialTheme.typography.titleSmall.copy(fontFamily = BoopSerifFamily))
            KeepToolbarIconButton(onClick = onDelete, icon = Icons.Outlined.Delete, contentDescription = "Delete transaction", tint = palette.danger)
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
    onEditAccountBalance: (BoopAccount) -> Unit,
    onAddAccount: () -> Unit,
    onAddTransaction: () -> Unit,
) {
    val palette = LocalBoopPalette.current
    var reconcileAccountId by rememberSaveable { mutableStateOf("") }
    var reconcileBalanceText by rememberSaveable { mutableStateOf("") }
    var pendingDeleteAccountId by rememberSaveable { mutableStateOf("") }
    var pendingDeleteEntryId by rememberSaveable { mutableStateOf("") }
    val epoch = LocalBoopDataEpoch.current
    val accountNames = remember(epoch) { accounts.associate { it.id to it.name } }
    val balances = remember(epoch) {
        accounts.associate { it.id to it.openingBalance }.toMutableMap().apply {
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BoopPageTitle("Wallet")
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 4.dp, bottom = 84.dp),
        ) {
            item(key = "hero") {
                UnifiedWalletHero(
                    label = "Total balance",
                    balance = formatCadAmount(netTotal),
                    incomeLabel = "${accounts.size} account${if (accounts.size == 1) "" else "s"}",
                    spentLabel = "${entries.size} transaction${if (entries.size == 1) "" else "s"}",
                )
            }
            item(key = "accounts-header") {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UnifiedSectionLabel("Accounts")
                    BoopAccentTextButton(label = "Add account", onClick = onAddAccount)
                }
            }
            if (accounts.isEmpty()) {
                item(key = "accounts-empty") {
                    Text("No accounts yet. Add one to start tracking balances.", color = palette.muted, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(accounts, key = { "acct-${it.id}" }) { account ->
                    WalletAccountRow(
                        name = account.name,
                        balance = balances[account.id] ?: 0.0,
                        onCorrectBalance = {
                            reconcileAccountId = account.id
                            reconcileBalanceText = formatLedgerAmountForEdit(balances[account.id] ?: 0.0)
                        },
                        onEdit = { onEditAccount(account) },
                        onDelete = { pendingDeleteAccountId = account.id },
                    )
                }
            }
            item(key = "tx-header") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UnifiedSectionLabel("Transactions")
                    BoopAccentTextButton(label = "Add transaction", onClick = onAddTransaction)
                }
            }
            if (sortedEntries.isEmpty()) {
                item(key = "tx-empty") {
                    Text("No transactions yet.", color = palette.muted, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                items(sortedEntries, key = { "tx-${it.id}" }) { entry ->
                    val typeColor = ledgerTypeColor(entry.type, palette)
                    val amountText = when (entry.type) {
                        "expense" -> formatSignedCadDelta(entry.amount, positive = false)
                        else -> formatSignedCadDelta(entry.amount, positive = true)
                    }
                    val meta = buildString {
                        append(
                            when (entry.type) {
                                "transfer" -> "${accountNames[entry.accountId] ?: "From"} → ${accountNames[entry.toAccountId] ?: "To"}"
                                else -> accountNames[entry.accountId] ?: "Account"
                            },
                        )
                        append(" · ")
                        append(SimpleDateFormat("MMM d", Locale.US).format(entry.createdAtMillis))
                    }
                    WalletTransactionRow(
                        title = entry.title.ifBlank { ledgerTypeLabel(entry.type) },
                        meta = meta,
                        amount = amountText,
                        amountColor = typeColor,
                        onClick = { onEditEntry(entry) },
                        onDelete = { pendingDeleteEntryId = entry.id },
                    )
                }
            }
        }
    }
    val reconcileAccount = accounts.firstOrNull { it.id == reconcileAccountId }
    if (reconcileAccount != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { reconcileAccountId = "" },
            title = { Text("Correct balance", color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${reconcileAccount.name} · no transaction is created", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
                    BoopFilledTextField(
                        value = reconcileBalanceText,
                        onValueChange = { reconcileBalanceText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == '-' }.take(12) },
                        label = { Text("Actual CAD balance") },
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { reconcileAccountId = "" }) { Text("Cancel", color = palette.muted) }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = reconcileBalanceText.toDoubleOrNull() ?: return@TextButton
                        val computedFromEntries = (balances[reconcileAccount.id] ?: 0.0) - reconcileAccount.openingBalance
                        onEditAccountBalance(reconcileAccount.copy(openingBalance = target - computedFromEntries))
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
    val sheetKey = initial.sessionKey
    val palette = LocalBoopPalette.current
    var name by rememberSaveable(sheetKey) { mutableStateOf(initial.name) }
    var openingText by rememberSaveable(sheetKey) {
        mutableStateOf(if (initial.openingBalance == 0.0) "" else formatLedgerAmountForEdit(initial.openingBalance))
    }
    var hasExplicitSave by remember(sheetKey) { mutableStateOf(false) }
    fun buildAccountOrNull(): BoopAccount? {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return null
        return BoopAccount(
            id = initial.id ?: UUID.randomUUID().toString(),
            name = trimmed,
            openingBalance = openingText.toDoubleOrNull() ?: 0.0,
        )
    }
    DisposableEffect(sheetKey) {
        onDispose {
            if (!hasExplicitSave) {
                buildAccountOrNull()?.let(onSave)
            }
        }
    }
    fun saveAndDismiss() {
        hasExplicitSave = true
        val account = buildAccountOrNull()
        if (account != null) {
            onSave(account)
        } else {
            onDismiss()
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        KeepEditorTopBar(
            onBack = { saveAndDismiss() },
            title = if (initial.id == null) "New Account" else "Edit Account",
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(6.dp))
            KeepOutlinedTitleField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Account name",
            )
            KeepEditorMetaLine("Cash, bank, card, savings…")
            Spacer(Modifier.height(12.dp))
            KeepSectionLabel("Starting balance")
            OutlinedTextField(
                value = openingText,
                onValueChange = { openingText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == '-' }.take(12) },
                label = { Text("CAD balance") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = KeepOutlinedFieldColors(),
            )
            KeepEditorMetaLine("Adjust anytime with the correct-balance button.")
            Spacer(Modifier.height(16.dp))
        }
        KeepFormSaveButton(onClick = { saveAndDismiss() })
        Spacer(Modifier.height(10.dp))
    }
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

private fun habitCategoryLabel(raw: String): String =
    normalizeHabitCategory(raw).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

private fun habitCategoryOrder(raw: String): Int = when (normalizeHabitCategory(raw)) {
    "morning" -> 0
    "afternoon" -> 1
    "evening" -> 2
    "night" -> 3
    else -> 4
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
    val palette = LocalBoopPalette.current
    val dark = palette.background.red + palette.background.green + palette.background.blue < 0.35f
    val habitColors = unifiedTypeColors(UnifiedItemType.HABIT, dark)
    val todayKey = todayHabitDayKey()
    val dayValues = parseHabitDayValues(habit.quantityDayValues)
    val todayAmount = dayValues[todayKey] ?: 0
    val cardShape = RoundedCornerShape(16.dp)
    val weekDots = remember(habit.id, habit.dayKeys, habit.quantityDayValues) {
        List(7) { i ->
            val offset = i - 6
            val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_MONTH, offset) }
            val key = habitDayKeyFormat.format(cal.time)
            if (habit.quantityMode) {
                (dayValues[key] ?: 0) >= habit.quantityDailyTarget.coerceAtLeast(1)
            } else {
                key in parseHabitDayKeys(habit.dayKeys)
            }
        }
    }
    Surface(
        onClick = { onOpenHabit(habit) },
        shape = cardShape,
        color = habitColors.bg,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, habitColors.border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (dark) Color.Black.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.5f),
                ) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            UnifiedItemType.HABIT.icon,
                            contentDescription = null,
                            tint = habitColors.accent,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            habitCategoryLabel(habit.dayPeriodCategory).uppercase(Locale.getDefault()),
                            color = habitColors.accent,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, letterSpacing = 0.6.sp),
                        )
                    }
                }
                Text(
                    "${habit.progress}/${habit.goal}",
                    style = MaterialTheme.typography.labelLarge,
                    color = habitColors.accent,
                )
            }
            Text(
                habit.title,
                modifier = Modifier.clickable { onOpenHabit(habit) },
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = BoopSerifFamily),
                color = palette.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            UnifiedHabitDots(dots = weekDots)
            if (habit.quantityMode) {
                val unit = habit.quantityUnit.ifBlank { "units" }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${todayAmount}/${habit.quantityDailyTarget} $unit today",
                        color = palette.muted,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1, maxOf(5, habit.quantityDailyTarget / 4)).distinct().forEach { delta ->
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = habitColors.bg.copy(alpha = 0.8f),
                                border = BorderStroke(1.dp, habitColors.border),
                                modifier = Modifier.clickable {
                                    val map = dayValues.toMutableMap()
                                    map[todayKey] = todayAmount + delta
                                    onPersist(habit.copy(quantityDayValues = serializeHabitDayValues(map)))
                                },
                            ) {
                                Text(
                                    "+$delta",
                                    color = habitColors.accent,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            } else {
                val todayDone = todayKey in parseHabitDayKeys(habit.dayKeys)
                Surface(
                    onClick = {
                        val next = parseHabitDayKeys(habit.dayKeys).toMutableSet()
                        if (todayKey in next) next.remove(todayKey) else next.add(todayKey)
                        onPersist(habit.copy(dayKeys = serializeHabitDayKeys(next)))
                    },
                    shape = RoundedCornerShape(999.dp),
                    color = if (todayDone) habitColors.accent else habitColors.bg,
                    border = BorderStroke(1.dp, habitColors.border),
                ) {
                    Text(
                        if (todayDone) "Done today" else "Check in today",
                        color = if (todayDone) palette.accentOn else habitColors.accent,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
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
    val palette = LocalBoopPalette.current
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
                Text(
                    "Check-off habits toggle today; quantity habits let you add minutes/mL with +/-.",
                    color = palette.muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (habits.isEmpty()) {
            Text("No habits yet.", color = palette.muted, style = MaterialTheme.typography.bodyMedium)
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
    val sheetKey = initial.sessionKey
    var title by rememberSaveable(sheetKey) { mutableStateOf(initial.title) }
    var description by rememberSaveable(sheetKey) { mutableStateOf(initial.description) }
    var location by rememberSaveable(sheetKey) { mutableStateOf(initial.location) }
    var allDay by rememberSaveable(sheetKey) { mutableStateOf(initial.allDay) }
    var startAt by rememberSaveable(sheetKey) { mutableLongStateOf(initial.startAt) }
    var endAt by rememberSaveable(sheetKey) { mutableLongStateOf(initial.endAt) }
    var notifyWeeksBefore by rememberSaveable(sheetKey) { mutableStateOf(initial.notifyWeeksBefore.toString()) }
    var notifyDaysBefore by rememberSaveable(sheetKey) { mutableStateOf(initial.notifyDaysBefore.toString()) }
    var notifyHoursBefore by rememberSaveable(sheetKey) { mutableStateOf(initial.notifyHoursBefore.toString()) }
    var repeatEveryDays by rememberSaveable(sheetKey) { mutableIntStateOf(initial.repeatEveryDays.coerceAtLeast(0)) }
    var customRepeatDays by rememberSaveable(sheetKey) {
        mutableStateOf(
            initial.repeatEveryDays.takeIf { it !in setOf(0, 1, 7, 30, 365) }?.toString().orEmpty(),
        )
    }
    var pickStart by rememberSaveable(sheetKey) { mutableStateOf(false) }
    var pickEnd by rememberSaveable(sheetKey) { mutableStateOf(false) }
    var selectedCalId by rememberSaveable(sheetKey) { mutableLongStateOf(initial.calendarId ?: -1L) }
    var calendars by remember { mutableStateOf(emptyList<DeviceCalendarChoice>()) }
    var hasExplicitSave by remember(sheetKey) { mutableStateOf(false) }
    var pendingPermissionSave by remember(sheetKey) { mutableStateOf(false) }
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
        if (!granted) {
            Toast.makeText(context, "Calendar write permission denied.", Toast.LENGTH_SHORT).show()
            pendingPermissionSave = false
        }
    }
    LaunchedEffect(writeGranted) {
        if (writeGranted) {
            calendars = withContext(Dispatchers.IO) { readVisibleCalendars(context) }
            if (selectedCalId < 0 && calendars.isNotEmpty()) selectedCalId = calendars.first().id
        }
    }
    val dateFmt = remember(allDay) {
        if (allDay) SimpleDateFormat("EEE, MMM dd", Locale.US) else SimpleDateFormat("EEE, MMM dd · HH:mm", Locale.US)
    }
    fun persistEvent(): Boolean {
        if (title.isBlank()) return false
        if (!writeGranted) {
            pendingPermissionSave = true
            writePermLauncher.launch(Manifest.permission.WRITE_CALENDAR)
            return false
        }
        val calendarId = selectedCalId.takeIf { it >= 0 } ?: calendars.firstOrNull()?.id
        if (calendarId == null) {
            Toast.makeText(context, "No writable calendar found.", Toast.LENGTH_SHORT).show()
            return false
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
            Toast.makeText(context, "Event saved to Calendar", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to save event", Toast.LENGTH_SHORT).show()
        }
        return ok
    }
    LaunchedEffect(writeGranted, pendingPermissionSave) {
        if (writeGranted && pendingPermissionSave && title.isNotBlank()) {
            pendingPermissionSave = false
            val ok = persistEvent()
            if (ok) {
                hasExplicitSave = true
                onSave(true)
            }
        }
    }
    fun commitEventAndClose() {
        if (title.isBlank()) {
            onDismiss()
            return
        }
        val ok = persistEvent()
        if (ok) {
            hasExplicitSave = true
            onSave(true)
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        KeepEditorTopBar(
            onBack = { commitEventAndClose() },
            title = if (initial.eventId == null) "New Event" else "Edit Event",
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(6.dp))
            KeepOutlinedTitleField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Title",
            )
            Spacer(Modifier.height(10.dp))
            KeepEditorSwitchRow(label = "All day", checked = allDay, onCheckedChange = { allDay = it })
            Spacer(Modifier.height(6.dp))
            KeepEditorActionCard(
                label = "Starts",
                value = dateFmt.format(startAt),
                onClick = { pickStart = true },
            )
            Spacer(Modifier.height(8.dp))
            KeepEditorActionCard(
                label = "Ends",
                value = dateFmt.format(endAt),
                onClick = { pickEnd = true },
            )
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
            if (writeGranted && calendars.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                KeepSectionLabel("Calendar")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    calendars.forEach { cal ->
                        KeepPaletteChip(
                            label = cal.displayName,
                            selected = cal.id == selectedCalId,
                            onClick = { selectedCalId = cal.id },
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            KeepEditorRepeatSection(
                repeatEveryDays = repeatEveryDays,
                customRepeatDays = customRepeatDays,
                onRepeatEveryDaysChange = { repeatEveryDays = it },
                onCustomRepeatDaysChange = { customRepeatDays = it },
                frequencyLabel = repeatFrequencyLabel(repeatEveryDays),
            )
            Spacer(Modifier.height(10.dp))
            KeepSectionLabel("Notify before start")
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = notifyWeeksBefore,
                    onValueChange = { notifyWeeksBefore = it.filter { ch -> ch.isDigit() }.take(2) },
                    label = { Text("Weeks") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = KeepOutlinedFieldColors(),
                )
                OutlinedTextField(
                    value = notifyDaysBefore,
                    onValueChange = { notifyDaysBefore = it.filter { ch -> ch.isDigit() }.take(3) },
                    label = { Text("Days") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = KeepOutlinedFieldColors(),
                )
                OutlinedTextField(
                    value = notifyHoursBefore,
                    onValueChange = { notifyHoursBefore = it.filter { ch -> ch.isDigit() }.take(3) },
                    label = { Text("Hours") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = KeepOutlinedFieldColors(),
                )
            }
            Spacer(Modifier.height(10.dp))
            KeepSectionLabel("Location")
            KeepBorderlessBodyField(
                value = location,
                onValueChange = { location = it },
                placeholder = "Add location",
                minLines = 1,
            )
            KeepSectionLabel("Description")
            KeepBorderlessBodyField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Add description",
                minLines = 3,
            )
            Spacer(Modifier.height(16.dp))
        }
        KeepFormSaveButton(onClick = { commitEventAndClose() })
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun TaskEditorSheet(
    initial: ItemSheet.TaskSheet,
    notes: List<BoopNote>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSaveNote: (BoopNote) -> Unit,
    onPersist: (BoopTask) -> Unit,
    onSave: (BoopTask) -> Unit,
    onOpenLinkedNote: (BoopNote) -> Unit,
) {
    val sheetKey = initial.sessionKey
    var title by rememberSaveable(sheetKey) { mutableStateOf(initial.title) }
    var reminderAt by remember(sheetKey, initial.reminderAt) { mutableLongStateOf(initial.reminderAt) }
    var done by remember(sheetKey) { mutableStateOf(initial.done) }
    var repeatEveryDays by rememberSaveable(sheetKey) { mutableIntStateOf(initial.repeatEveryDays.coerceAtLeast(0)) }
    var linkedNoteId by rememberSaveable(sheetKey) { mutableStateOf(initial.linkedNoteId) }
    var customRepeatDays by rememberSaveable(sheetKey) {
        mutableStateOf(
            initial.repeatEveryDays.takeIf { it !in setOf(0, 1, 7, 30, 365) }?.toString().orEmpty(),
        )
    }
    var showReminderPicker by remember(sheetKey) { mutableStateOf(false) }
    var hasExplicitSave by remember(sheetKey) { mutableStateOf(false) }
    var skipAutoSaveOnDispose by remember(sheetKey) { mutableStateOf(false) }
    var details by rememberSaveable(sheetKey) { mutableStateOf(initial.details) }
    var subtasks by remember(sheetKey) {
        mutableStateOf(
            parseSubtasksJson(initial.subtasksJson).ifEmpty { emptyList() },
        )
    }
    var subtasksExpanded by rememberSaveable(sheetKey) { mutableStateOf(initial.subtasksJson.isNotBlank()) }
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
            details = details.trim(),
            subtasksJson = serializeSubtasksJson(subtasks),
        )
    }
    DisposableEffect(sheetKey) {
        onDispose {
            if (!hasExplicitSave && !skipAutoSaveOnDispose) {
                buildTaskForSaveOrNull()?.let(onPersist)
            }
        }
    }
    fun openLinkedNote(note: BoopNote) {
        skipAutoSaveOnDispose = true
        buildTaskForSaveOrNull()?.let(onPersist)
        onOpenLinkedNote(note)
    }
    fun commitTaskAndClose() {
        hasExplicitSave = true
        val taskCandidate = buildTaskForSaveOrNull() ?: run {
            onDismiss()
            return
        }
        onSave(taskCandidate)
    }
    val palette = LocalBoopPalette.current
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        KeepEditorTopBar(
            onBack = { commitTaskAndClose() },
            title = if (initial.id == null) "New Task" else "Edit Task",
            actions = {
                if (initial.id != null) {
                    KeepToolbarIconButton(
                        onClick = {
                            hasExplicitSave = true
                            buildTaskForSaveOrNull()?.let { onSave(it.copy(archived = !initial.archived)) }
                        },
                        icon = if (initial.archived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                        contentDescription = if (initial.archived) "Restore task" else "Archive task",
                        tint = if (initial.archived) palette.accent else palette.muted,
                    )
                }
                if (onDelete != null) {
                    KeepToolbarIconButton(
                        onClick = {
                            skipAutoSaveOnDispose = true
                            onDelete()
                        },
                        icon = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = palette.danger,
                    )
                }
            },
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(6.dp))
            KeepOutlinedTitleField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Title",
            )
            Spacer(Modifier.height(10.dp))
            KeepEditorActionCard(
                label = "Date & time",
                value = formatTaskReminderLine(reminderAt),
                onClick = { showReminderPicker = true },
            )
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
            Spacer(Modifier.height(10.dp))
            KeepEditorRepeatSection(
                repeatEveryDays = repeatEveryDays,
                customRepeatDays = customRepeatDays,
                onRepeatEveryDaysChange = { repeatEveryDays = it },
                onCustomRepeatDaysChange = { customRepeatDays = it },
                frequencyLabel = repeatFrequencyLabel(repeatEveryDays),
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                KeepSectionLabel("Subtasks", modifier = Modifier.padding(0.dp))
                KeepToolbarIconButton(
                    onClick = {
                        subtasksExpanded = true
                        if (subtasks.isEmpty()) subtasks = listOf(ChecklistItem(text = ""))
                    },
                    icon = Icons.Outlined.Add,
                    contentDescription = "Add subtask",
                    tint = palette.muted,
                )
            }
            if (subtasksExpanded || subtasks.isNotEmpty()) {
                KeepChecklistEditor(
                    items = subtasks,
                    onChange = { subtasks = it },
                )
            }
            KeepSectionLabel("Details")
            KeepBorderlessBodyField(
                value = details,
                onValueChange = { details = it },
                placeholder = "Add details",
                minLines = 3,
            )
            Spacer(Modifier.height(10.dp))
            val linkableNotes = remember(notes) {
                notes.filter { !it.archived }
                    .sortedByDescending { it.createdAtMillis + it.updatedAtMillis }
                    .take(12)
            }
            if (linkableNotes.isNotEmpty()) {
                KeepSectionLabel("LINK TO NOTE")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KeepPaletteChip(
                        label = "None",
                        selected = linkedNoteId == null,
                        onClick = { linkedNoteId = null },
                    )
                    linkableNotes.forEach { note ->
                        KeepPaletteChip(
                            label = note.title.ifBlank { "Untitled note" },
                            selected = linkedNoteId == note.id,
                            onClick = { linkedNoteId = note.id },
                        )
                    }
                }
                val linkedNote = linkedNoteId?.let { id -> notes.find { it.id == id } }
                if (linkedNote != null) {
                    Spacer(Modifier.height(8.dp))
                    val linkedContext = LocalContext.current
                    val linkedImages = parseNoteAttachments(linkedNote.attachmentUri)
                    Surface(
                        onClick = { openLinkedNote(linkedNote) },
                        shape = RoundedCornerShape(14.dp),
                        color = palette.surfaceVariant,
                        border = BorderStroke(1.dp, palette.surfaceBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (linkedImages.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(linkedContext)
                                        .data(storedAttachmentForCoil(linkedImages.first()))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = palette.chipBg,
                                    border = BorderStroke(1.dp, palette.surfaceBorder),
                                    modifier = Modifier.size(46.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Outlined.EditNote,
                                            contentDescription = null,
                                            tint = palette.accent,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    linkedNote.title.ifBlank { "Untitled note" },
                                    color = palette.onBackground,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "Linked note · tap to open",
                                    color = palette.muted,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        KeepFormSaveButton(onClick = { commitTaskAndClose() })
        Spacer(Modifier.height(10.dp))
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
    var body by rememberSaveable(session) { mutableStateOf(htmlNoteBodyToPlain(initial.body)) }
    var tagsCsv by rememberSaveable(session) { mutableStateOf(initial.tagsCsv) }
    var attachmentStored by remember(session) { mutableStateOf(parseNoteAttachments(initial.attachmentUri)) }
    var previewImageIndex by remember(session) { mutableStateOf(-1) }
    var hasExplicitSave by remember(session) { mutableStateOf(false) }
    var skipAutoSaveOnDispose by remember(session) { mutableStateOf(false) }
    var isChecklistMode by rememberSaveable(session) { mutableStateOf(isChecklistBody(initial.body)) }
    var checklistItems by remember(session) {
        mutableStateOf(
            parseChecklistBody(initial.body).ifEmpty { listOf(ChecklistItem(text = "")) },
        )
    }
    fun resolveBodyForSave(): String {
        if (isChecklistMode) return serializeChecklistBody(checklistItems)
        return body.trim()
    }
    fun buildNoteForSaveOrNull(): BoopNote? {
        val noteId = initial.id ?: UUID.randomUUID().toString()
        val resolvedAttachment = serializeNoteAttachments(attachmentStored)
        val bodyContent = resolveBodyForSave()
        if (title.isBlank() && bodyContent.isBlank()) return null
        val ocrText = extractTextFromAttachment(context, resolvedAttachment)
        return BoopNote(
            id = noteId,
            title = title.trim(),
            body = bodyContent,
            attachmentUri = resolvedAttachment,
            audioUri = null,
            tagsCsv = normalizeNoteTags(tagsCsv),
            ocrText = ocrText,
            linkedTaskId = null,
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
    DisposableEffect(session) {
        onDispose {
            if (!hasExplicitSave && !skipAutoSaveOnDispose) {
                buildNoteForSaveOrNull()?.let(onSave)
            }
        }
    }
    var tagsExpanded by rememberSaveable(session) { mutableStateOf(initial.tagsCsv.isNotBlank()) }
    var showLinkSheet by remember(session) { mutableStateOf(false) }
    val linkRegex = remember { Regex("""https?://[^\s<>()]+""") }
    val previewLinks = remember(body) { linkRegex.findAll(body).map { it.value }.distinct().toList() }
    fun saveAndDismiss() {
        hasExplicitSave = true
        buildNoteForSaveOrNull()?.let(onSave)
        onDismiss()
    }
    fun appendLinkToBody(url: String) {
        val cleaned = url.trim()
        if (cleaned.isBlank()) return
        val normalized = if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) cleaned else "https://$cleaned"
        body = if (body.isBlank()) normalized else "${body.trimEnd()}\n$normalized"
    }
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        KeepEditorTopBar(
            onBack = { saveAndDismiss() },
            title = if (initial.id == null) "New Note" else "Edit Note",
            actions = {
                if (initial.id != null) {
                    KeepToolbarIconButton(
                        onClick = {
                            hasExplicitSave = true
                            val serializedAttachments = serializeNoteAttachments(attachmentStored)
                            onSave(
                                BoopNote(
                                    id = initial.id,
                                    title = title.trim(),
                                    body = resolveBodyForSave(),
                                    attachmentUri = serializedAttachments,
                                    audioUri = null,
                                    tagsCsv = normalizeNoteTags(tagsCsv),
                                    ocrText = extractTextFromAttachment(context, serializedAttachments),
                                    linkedTaskId = null,
                                    archived = !initial.archived,
                                    createdAtMillis = initial.createdAtMillis,
                                    updatedAtMillis = System.currentTimeMillis(),
                                ),
                            )
                        },
                        icon = if (initial.archived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                        contentDescription = if (initial.archived) "Restore note" else "Archive note",
                        tint = if (initial.archived) palette.accent else palette.muted,
                    )
                }
                if (onDelete != null) {
                    KeepToolbarIconButton(
                        onClick = {
                            skipAutoSaveOnDispose = true
                            onDelete()
                        },
                        icon = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = palette.danger,
                    )
                }
            },
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            if (initial.id != null) {
                KeepEditorMetaLine(
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
                )
            }
            Spacer(Modifier.height(6.dp))
            KeepOutlinedTitleField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Title",
            )
            Spacer(Modifier.height(12.dp))
            if (tagsCsv.isNotBlank()) {
                KeepNoteLabelChips(tagsCsv = tagsCsv)
                Spacer(Modifier.height(8.dp))
            }
            if (isChecklistMode) {
                KeepChecklistEditor(
                    items = checklistItems,
                    onChange = { checklistItems = it.ifEmpty { listOf(ChecklistItem(text = "")) } },
                )
            } else {
                KeepOutlinedBodyField(
                    value = body,
                    onValueChange = { body = it },
                    placeholder = "Write something...",
                    minLines = 6,
                )
            }
            if (attachmentStored.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                attachmentStored.chunked(3).forEach { row ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(108.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        row.forEach { stored ->
                            val imageIndex = attachmentStored.indexOf(stored)
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { if (imageIndex >= 0) previewImageIndex = imageIndex },
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(storedAttachmentForCoil(stored))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                Surface(
                                    onClick = { attachmentStored = attachmentStored.filterNot { it == stored } },
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Outlined.Close,
                                            contentDescription = "Remove image",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp),
                                        )
                                    }
                                }
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
            if (tagsExpanded) {
                TextField(
                    value = tagsCsv,
                    onValueChange = { tagsCsv = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("Add labels, comma separated", color = palette.muted) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = palette.onBackground),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = palette.accent,
                    ),
                    singleLine = true,
                )
            }
            if (previewLinks.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                KeepSectionLabel("Links")
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    previewLinks.forEach { link -> NoteLinkPreviewCard(link) }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Column(Modifier.fillMaxWidth()) {
            KeepEditorBottomBar {
                KeepToolbarIconButton(
                    onClick = {
                        isChecklistMode = !isChecklistMode
                        if (isChecklistMode) {
                            checklistItems = listOf(ChecklistItem(text = ""))
                        } else {
                            body = ""
                        }
                    },
                    icon = if (isChecklistMode) Icons.Outlined.Notes else Icons.Outlined.FormatListBulleted,
                    contentDescription = if (isChecklistMode) "Show note" else "Show checklist",
                    tint = if (isChecklistMode) palette.accent else palette.muted,
                )
                KeepToolbarIconButton(
                    onClick = { picker.launch("image/*") },
                    icon = Icons.Outlined.Image,
                    contentDescription = "Add image",
                    tint = if (attachmentStored.isNotEmpty()) palette.accent else palette.muted,
                )
                KeepToolbarIconButton(
                    onClick = { tagsExpanded = !tagsExpanded },
                    icon = Icons.Outlined.Label,
                    contentDescription = "Add labels",
                    tint = if (tagsCsv.isNotBlank()) palette.accent else palette.muted,
                )
                KeepToolbarIconButton(
                    onClick = {
                        if (previewLinks.size >= 25) {
                            Toast.makeText(context, "Maximum 25 links per note.", Toast.LENGTH_SHORT).show()
                            return@KeepToolbarIconButton
                        }
                        showLinkSheet = true
                    },
                    icon = Icons.Outlined.Link,
                    contentDescription = "Add link",
                    tint = if (previewLinks.isNotEmpty()) palette.accent else palette.muted,
                )
            }
            Spacer(Modifier.height(8.dp))
            KeepFormSaveButton(onClick = { saveAndDismiss() })
            Spacer(Modifier.height(10.dp))
        }
        KeepLinkInputSheet(
            open = showLinkSheet,
            onDismiss = { showLinkSheet = false },
            onAdd = { url -> appendLinkToBody(url) },
        )
        if (previewImageIndex in attachmentStored.indices) {
            ImagePreviewOverlay(
                images = attachmentStored,
                startIndex = previewImageIndex,
                onDismiss = { previewImageIndex = -1 },
            )
        }
    }
}

@Composable
private fun ImagePreviewOverlay(
    images: List<String>,
    startIndex: Int,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val pagerState = rememberPagerState(
            initialPage = startIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
            pageCount = { images.size },
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f)),
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(storedAttachmentForCoil(images[page]))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Surface(
                onClick = onDismiss,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    "${pagerState.currentPage + 1} of ${images.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
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
    val sheetKey = initial.sessionKey
    val palette = LocalBoopPalette.current
    var label by rememberSaveable(sheetKey) { mutableStateOf(initial.title) }
    var dayPeriodCategory by rememberSaveable(sheetKey) { mutableStateOf(normalizeHabitCategory(initial.dayPeriodCategory)) }
    var goalText by rememberSaveable(sheetKey) { mutableStateOf(initial.goal.toString()) }
    var progress by remember(sheetKey) { mutableIntStateOf(initial.progress) }
    var quantityMode by rememberSaveable(sheetKey) { mutableStateOf(initial.quantityMode) }
    var quantityUnit by rememberSaveable(sheetKey) { mutableStateOf(initial.quantityUnit) }
    var quantityTarget by rememberSaveable(sheetKey) { mutableStateOf(initial.quantityDailyTarget.toString()) }
    var hasExplicitSave by remember(sheetKey) { mutableStateOf(false) }
    var skipAutoSaveOnDispose by remember(sheetKey) { mutableStateOf(false) }
    fun buildHabitOrNull(): BoopHabit? {
        val g = goalText.toIntOrNull() ?: 30
        if (label.isBlank()) return null
        return BoopHabit(
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
        )
    }
    DisposableEffect(sheetKey) {
        onDispose {
            if (!hasExplicitSave && !skipAutoSaveOnDispose) {
                buildHabitOrNull()?.let(onSave)
            }
        }
    }
    fun saveAndDismiss() {
        hasExplicitSave = true
        val habit = buildHabitOrNull()
        if (habit != null) {
            onSave(habit)
        } else {
            onDismiss()
        }
    }
    val goalVal = goalText.toIntOrNull() ?: 30
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        KeepEditorTopBar(
            onBack = { saveAndDismiss() },
            title = if (initial.id == null) "New Habit" else "Edit Habit",
            actions = {
                if (onDelete != null) {
                    KeepToolbarIconButton(
                        onClick = {
                            skipAutoSaveOnDispose = true
                            onDelete()
                        },
                        icon = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = palette.danger,
                    )
                }
            },
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(6.dp))
            KeepOutlinedTitleField(
                value = label,
                onValueChange = { label = it },
                placeholder = "Habit name",
            )
            Spacer(Modifier.height(12.dp))
            KeepSectionLabel("Time of day")
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("morning", "afternoon", "evening", "night").forEach { cat ->
                    KeepPaletteChip(
                        label = cat.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        selected = dayPeriodCategory == cat,
                        onClick = { dayPeriodCategory = cat },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            KeepSectionLabel("Tracking")
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KeepPaletteChip(
                    label = "Track days",
                    selected = !quantityMode,
                    onClick = { quantityMode = false },
                )
                KeepPaletteChip(
                    label = "Track quantity",
                    selected = quantityMode,
                    onClick = { quantityMode = true },
                )
            }
            Spacer(Modifier.height(10.dp))
            if (quantityMode) {
                OutlinedTextField(
                    value = quantityUnit,
                    onValueChange = { quantityUnit = it },
                    label = { Text("Unit") },
                    placeholder = { Text("minutes, mL, pages...", color = palette.muted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = KeepOutlinedFieldColors(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityTarget,
                    onValueChange = { quantityTarget = it.filter { ch -> ch.isDigit() }.take(5) },
                    label = { Text("Daily target") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = KeepOutlinedFieldColors(),
                )
            } else {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("Target days") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = KeepOutlinedFieldColors(),
                )
            }
            KeepEditorMetaLine(
                if (quantityMode) {
                    "Today is logged in week view / habits page"
                } else {
                    "Progress: $progress / $goalVal"
                },
            )
            Spacer(Modifier.height(16.dp))
        }
        KeepFormSaveButton(onClick = { saveAndDismiss() })
        Spacer(Modifier.height(10.dp))
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
            .size(34.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (filled) palette.accent else palette.chipBg,
        shadowElevation = 0.dp,
        border = when {
            filled -> BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.45f))
            else -> BorderStroke(1.dp, palette.surfaceBorder)
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
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
                        else -> palette.onBackground
                    },
                    modifier = Modifier.size(18.dp),
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
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = shape,
        color = palette.surfaceElevated,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, palette.accentGlow.copy(alpha = 0.42f)),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = palette.onBackground,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
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
    val shape = RoundedCornerShape(14.dp)
    Button(
        onClick = onClick,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.accent,
            contentColor = palette.accentOn,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp,
        ),
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = palette.accentOn)
    }
}

private fun formatTaskReminderLine(timeInMillis: Long): String {
    val day = SimpleDateFormat("EEE, MMM d", Locale.US).format(timeInMillis)
    val time = SimpleDateFormat("HH:mm", Locale.US).format(timeInMillis)
    return "$day   $time"
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

private fun linkedNoteLabelForTask(task: BoopTask): String? {
    val noteId = task.linkedNoteId ?: return null
    return try {
        val notes = JSONArray(LocalStore.read("notes"))
        for (i in 0 until notes.length()) {
            val item = notes.getJSONObject(i)
            if (item.optString("id") != noteId) continue
            if (item.optBoolean("archived", false)) return null
            val title = item.optString("title").trim().ifBlank { "Untitled" }
            return "Note · $title"
        }
        null
    } catch (_: Throwable) {
        null
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
    fun cancel(context: Context, taskId: String) {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("id", taskId.hashCode())
            putExtra("taskId", taskId)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.cancel(pending)
        androidx.core.app.NotificationManagerCompat.from(context).cancel(taskId.hashCode())
    }

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
        if (taskId.isNotBlank()) {
            val activeTask = runCatching {
                BoopData.repository(context).readTasks().firstOrNull { it.id == taskId }
            }.getOrNull()
            if (activeTask == null || activeTask.done || activeTask.archived) {
                androidx.core.app.NotificationManagerCompat.from(context).cancel(id)
                return
            }
        }
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
        val repo = BoopData.repository(context)
        repo.completeTaskFromNotification(taskId)?.let { ReminderScheduler.schedule(context, it) }
    }
}
