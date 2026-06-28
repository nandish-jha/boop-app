package com.prodash.reminders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

enum class VoiceCaptureStyle {
    Sheet,
    Floating,
}

private enum class VoiceSavePhase {
    None,
    Saving,
    Saved,
}

private fun voiceTypeLabel(type: VoiceCaptureType): String =
    type.name.lowercase().replaceFirstChar { it.titlecase() }

private fun voicePreviewTitle(parsed: ParsedVoiceCapture): String {
    val title = parsed.title.trim()
    if (title.isNotBlank()) return title
    val body = parsed.body.trim()
    if (body.isNotBlank()) return body.take(48)
    return ""
}

private fun voiceSaveSuccessStatus(parsed: ParsedVoiceCapture): String {
    val type = voiceTypeLabel(parsed.type)
    val preview = voicePreviewTitle(parsed)
    return if (preview.isNotBlank()) "$type saved · $preview" else "$type saved"
}

@Composable
private fun FloatingVoicePanel(
    status: String,
    listening: Boolean,
    savePhase: VoiceSavePhase,
    partial: String,
    parsedPreview: ParsedVoiceCapture?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val liveText = partial.trim()
    val previewTitle = parsedPreview?.let { voicePreviewTitle(it) }.orEmpty()
    val previewType = parsedPreview?.type?.let { voiceTypeLabel(it) }
    val showSaveUi = savePhase != VoiceSavePhase.None

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, colors.onSurfaceVariant.copy(alpha = 0.14f)),
        shadowElevation = 18.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Boop",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                if (!showSaveUi) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            FloatingMicOrb(
                listening = listening,
                savePhase = savePhase,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                status,
                color = if (savePhase == VoiceSavePhase.Saved) colors.primary else colors.onBackground,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            if (liveText.isNotBlank() && !showSaveUi) {
                Spacer(Modifier.height(10.dp))
                Text(
                    liveText,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.55f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            } else if (showSaveUi && previewType != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            previewType,
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        if (previewTitle.isNotBlank()) {
                            Text(
                                previewTitle,
                                color = colors.onBackground,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
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
private fun VoiceSaveFeedbackPanel(
    savePhase: VoiceSavePhase,
    status: String,
    parsedPreview: ParsedVoiceCapture?,
) {
    val colors = MaterialTheme.colorScheme
    val previewTitle = parsedPreview?.let { voicePreviewTitle(it) }.orEmpty()
    val previewType = parsedPreview?.type?.let { voiceTypeLabel(it) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FloatingMicOrb(listening = false, savePhase = savePhase)
        Spacer(Modifier.height(16.dp))
        Text(
            status,
            color = if (savePhase == VoiceSavePhase.Saved) colors.primary else colors.onBackground,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (previewType != null) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        previewType,
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    if (previewTitle.isNotBlank()) {
                        Text(
                            previewTitle,
                            color = colors.onBackground,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingMicOrb(listening: Boolean, savePhase: VoiceSavePhase) {
    val colors = MaterialTheme.colorScheme
    val saving = savePhase == VoiceSavePhase.Saving
    val saved = savePhase == VoiceSavePhase.Saved
    val checkScale by animateFloatAsState(
        targetValue = if (saved) 1f else 0.72f,
        animationSpec = spring(stiffness = 420f, dampingRatio = 0.62f),
        label = "checkScale",
    )
    val pulseTransition = rememberInfiniteTransition(label = "micPulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    val ringAlpha by pulseTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ringAlpha",
    )
    val savePulse by pulseTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "savePulse",
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(84.dp)) {
        when {
            listening -> {
                Box(
                    Modifier
                        .size(84.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = ringAlpha)),
                )
            }
            saving -> {
                Box(
                    Modifier
                        .size(84.dp)
                        .scale(savePulse)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.22f)),
                )
            }
            saved -> {
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.16f)),
                )
            }
        }
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    when {
                        saved -> colors.primary
                        saving -> colors.surfaceVariant
                        else -> colors.surfaceVariant
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                saved -> Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier
                        .size(28.dp)
                        .scale(checkScale),
                )
                saving -> CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = colors.primary,
                    strokeWidth = 2.5.dp,
                )
                listening -> CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = colors.primary,
                    strokeWidth = 2.5.dp,
                )
                else -> Icon(
                    Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = colors.onBackground,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Composable
fun VoiceCaptureSheet(
    accounts: List<BoopAccount>,
    autoStart: Boolean,
    startSignal: Int,
    stopSignal: Int,
    onDismiss: () -> Unit,
    onParsed: (ParsedVoiceCapture) -> Unit,
    onListeningChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onRequestMicPermission: (() -> Unit)? = null,
    style: VoiceCaptureStyle = VoiceCaptureStyle.Sheet,
) {
    if (onRequestMicPermission == null) {
        VoiceCaptureSheetWithActivityLauncher(
            accounts = accounts,
            autoStart = autoStart,
            startSignal = startSignal,
            stopSignal = stopSignal,
            onDismiss = onDismiss,
            onParsed = onParsed,
            onListeningChanged = onListeningChanged,
            modifier = modifier,
            style = style,
        )
    } else {
        val context = LocalContext.current
        VoiceCaptureSheetCore(
            accounts = accounts,
            autoStart = autoStart,
            startSignal = startSignal,
            stopSignal = stopSignal,
            onDismiss = onDismiss,
            onParsed = onParsed,
            onListeningChanged = onListeningChanged,
            modifier = modifier,
            style = style,
            ensureMicPermission = { onGranted ->
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                ) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    onGranted()
                } else {
                    onListeningChanged(false)
                    onRequestMicPermission()
                }
            },
        )
    }
}

@Composable
private fun VoiceCaptureSheetWithActivityLauncher(
    accounts: List<BoopAccount>,
    autoStart: Boolean,
    startSignal: Int,
    stopSignal: Int,
    onDismiss: () -> Unit,
    onParsed: (ParsedVoiceCapture) -> Unit,
    onListeningChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    style: VoiceCaptureStyle = VoiceCaptureStyle.Sheet,
) {
    val context = LocalContext.current
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var pendingStart by remember { mutableStateOf<(() -> Unit)?>(null) }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
        if (granted) {
            pendingStart?.invoke()
        } else {
            onListeningChanged(false)
        }
        pendingStart = null
    }

    VoiceCaptureSheetCore(
        accounts = accounts,
        autoStart = autoStart,
        startSignal = startSignal,
        stopSignal = stopSignal,
        onDismiss = onDismiss,
        onParsed = onParsed,
        onListeningChanged = onListeningChanged,
        modifier = modifier,
        style = style,
        ensureMicPermission = { onGranted ->
            if (micGranted) {
                onGranted()
            } else {
                pendingStart = onGranted
                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
    )
}

@Composable
private fun VoiceCaptureSheetCore(
    accounts: List<BoopAccount>,
    autoStart: Boolean,
    startSignal: Int,
    stopSignal: Int,
    onDismiss: () -> Unit,
    onParsed: (ParsedVoiceCapture) -> Unit,
    onListeningChanged: (Boolean) -> Unit,
    ensureMicPermission: (onGranted: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    style: VoiceCaptureStyle = VoiceCaptureStyle.Sheet,
) {
    val colors = MaterialTheme.colorScheme
    val floating = style == VoiceCaptureStyle.Floating
    var transcript by remember { mutableStateOf("") }
    var partial by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var parsedPreview by remember { mutableStateOf<ParsedVoiceCapture?>(null) }
    var pendingCommit by remember { mutableStateOf<ParsedVoiceCapture?>(null) }
    var savePhase by remember { mutableStateOf(VoiceSavePhase.None) }
    var restartSignal by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf(if (autoStart) "Starting…" else "Listening when you speak.") }
    var lastHandledStart by remember { mutableIntStateOf(0) }
    var lastHandledStop by remember { mutableIntStateOf(stopSignal) }

    val context = LocalContext.current
    val speechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun releaseRecognizer() {
        val sr = recognizer ?: return
        recognizer = null
        try {
            sr.stopListening()
        } catch (_: Throwable) {
        }
        try {
            sr.cancel()
        } catch (_: Throwable) {
        }
        try {
            sr.destroy()
        } catch (_: Throwable) {
        }
    }

    fun createRecognizer(): SpeechRecognizer? {
        releaseRecognizer()
        if (!speechAvailable) return null
        return SpeechRecognizer.createSpeechRecognizer(context).also { recognizer = it }
    }

    fun setListening(active: Boolean) {
        listening = active
        onListeningChanged(active)
    }

    fun scheduleAutoRestart() {
        if (submitted) return
        restartSignal++
    }

    fun submitTranscript(text: String) {
        val trimmed = text.trim()
        if (submitted) return
        if (trimmed.isBlank()) {
            status = "Didn't catch that — listening again…"
            scheduleAutoRestart()
            return
        }
        val parsed = VoiceCaptureParser.parse(trimmed, accounts)
        submitted = true
        transcript = trimmed
        partial = ""
        parsedPreview = parsed
        pendingCommit = parsed
        savePhase = VoiceSavePhase.Saving
        status = "Saving…"
    }

    fun startListening() {
        if (submitted) return
        val sr = createRecognizer()
        if (sr == null) {
            status = "Speech recognition is not available on this device."
            setListening(false)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        setListening(true)
        partial = ""
        transcript = ""
        parsedPreview = null
        status = "Listening…"
        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                setListening(false)
                status = "Processing…"
            }
            override fun onError(error: Int) {
                setListening(false)
                releaseRecognizer()
                when (error) {
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        status = "Microphone permission required."
                    }
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                    -> {
                        status = "Didn't catch that — listening again…"
                        scheduleAutoRestart()
                    }
                    else -> {
                        status = "Voice error — listening again…"
                        scheduleAutoRestart()
                    }
                }
            }
            override fun onResults(results: Bundle?) {
                setListening(false)
                releaseRecognizer()
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                submitTranscript(text)
            }
            override fun onPartialResults(partialResults: Bundle?) {
                partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        try {
            sr.startListening(intent)
        } catch (_: Throwable) {
            setListening(false)
            releaseRecognizer()
            status = "Could not start listening — trying again…"
            scheduleAutoRestart()
        }
    }

    fun stopListening() {
        setListening(false)
        try {
            recognizer?.stopListening()
        } catch (_: Throwable) {
        }
        try {
            recognizer?.cancel()
        } catch (_: Throwable) {
        }
        releaseRecognizer()
        if (transcript.isBlank() && partial.isBlank() && !submitted) {
            status = "Stopped."
        }
    }

    fun resetForNewSession() {
        submitted = false
        transcript = ""
        partial = ""
        parsedPreview = null
        pendingCommit = null
        savePhase = VoiceSavePhase.None
        restartSignal = 0
        status = if (autoStart) "Starting…" else "Listening when you speak."
    }

    fun cancelPendingCommit() {
        pendingCommit = null
        submitted = false
        parsedPreview = null
        savePhase = VoiceSavePhase.None
    }

    fun requestAndStart() {
        if (submitted) return
        ensureMicPermission(::startListening)
    }

    LaunchedEffect(autoStart) {
        if (autoStart) requestAndStart()
    }

    LaunchedEffect(startSignal) {
        if (startSignal <= lastHandledStart) return@LaunchedEffect
        lastHandledStart = startSignal
        resetForNewSession()
        requestAndStart()
    }

    LaunchedEffect(stopSignal) {
        if (stopSignal <= lastHandledStop) return@LaunchedEffect
        lastHandledStop = stopSignal
        stopListening()
    }

    LaunchedEffect(restartSignal) {
        if (restartSignal == 0 || submitted) return@LaunchedEffect
        delay(550)
        if (!submitted) requestAndStart()
    }

    LaunchedEffect(pendingCommit) {
        val parsed = pendingCommit ?: return@LaunchedEffect
        delay(520)
        savePhase = VoiceSavePhase.Saved
        status = voiceSaveSuccessStatus(parsed)
        delay(980)
        if (pendingCommit == parsed) {
            onParsed(parsed)
            pendingCommit = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            releaseRecognizer()
            onListeningChanged(false)
        }
    }

    if (floating) {
        FloatingVoicePanel(
            status = status,
            listening = listening,
            savePhase = savePhase,
            partial = partial,
            parsedPreview = parsedPreview,
            onDismiss = {
                cancelPendingCommit()
                stopListening()
                onDismiss()
            },
            modifier = modifier,
        )
        return
    }

    if (submitted) {
        VoiceSaveFeedbackPanel(
            savePhase = savePhase,
            status = status,
            parsedPreview = parsedPreview,
        )
        return
    }

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Voice capture",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onBackground,
            )
            if (listening) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = colors.primary,
                    strokeWidth = 2.dp,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(status, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

        val displayText = when {
            partial.isNotBlank() -> partial
            transcript.isNotBlank() -> transcript
            else -> ""
        }
        if (displayText.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    displayText,
                    color = colors.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        TextButton(
            onClick = {
                stopListening()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Cancel", color = colors.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
        }
    }
}
