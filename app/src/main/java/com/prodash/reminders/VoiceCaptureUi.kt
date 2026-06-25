package com.prodash.reminders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun VoiceCaptureSheet(
    accounts: List<BoopAccount>,
    autoStart: Boolean,
    startSignal: Int,
    stopSignal: Int,
    onDismiss: () -> Unit,
    onParsed: (ParsedVoiceCapture) -> Unit,
    onListeningChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    var transcript by remember { mutableStateOf("") }
    var partial by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Listening…") }
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val speechAvailable = remember { SpeechRecognizer.isRecognitionAvailable(context) }
    val recognizer = remember {
        if (speechAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }

    fun setListening(active: Boolean) {
        listening = active
        onListeningChanged(active)
    }

    fun startListening() {
        val sr = recognizer
        if (sr == null) {
            status = "Speech recognition is not available on this device."
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
                status = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that — tap the mic to try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected — tap the mic to try again."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                    else -> "Voice error ($error). Tap the mic to try again."
                }
            }
            override fun onResults(results: Bundle?) {
                setListening(false)
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (text.isNotBlank()) {
                    transcript = text
                    partial = ""
                    status = "Got it — tap Use to open the editor."
                } else {
                    status = "No speech heard — tap the mic to try again."
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        sr.startListening(intent)
    }

    fun stopListening() {
        setListening(false)
        recognizer?.stopListening()
        if (transcript.isBlank() && partial.isBlank()) {
            status = "Stopped."
        }
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
        if (granted) {
            startListening()
        } else {
            status = "Microphone permission denied."
            onListeningChanged(false)
        }
    }

    fun requestAndStart() {
        if (!micGranted) {
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        startListening()
    }

    LaunchedEffect(autoStart) {
        if (autoStart) requestAndStart()
    }

    LaunchedEffect(startSignal) {
        if (startSignal > 0) requestAndStart()
    }

    LaunchedEffect(stopSignal) {
        if (stopSignal > 0 && listening) stopListening()
    }

    fun releaseRecognizer() {
        val sr = recognizer ?: return
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

    DisposableEffect(recognizer) {
        onDispose {
            releaseRecognizer()
            onListeningChanged(false)
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Voice capture",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
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

        if (transcript.isNotBlank()) {
            val parsed = remember(transcript, accounts) {
                VoiceCaptureParser.parse(transcript, accounts)
            }
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.secondary,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Detected: ${parsed.type.name.lowercase().replaceFirstChar { it.titlecase() }}",
                        color = colors.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (parsed.title.isNotBlank()) {
                        Text(parsed.title, color = colors.onBackground, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (parsed.type == VoiceCaptureType.NOTE && parsed.body.isNotBlank()) {
                        Text(
                            parsed.body,
                            color = if (parsed.title.isBlank()) colors.onBackground else colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else if (parsed.type != VoiceCaptureType.NOTE && parsed.title.isBlank()) {
                        Text("—", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                    parsed.amount?.let {
                        Text("Amount: $${"%.2f".format(it)}", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    parsed.dueAtMillis?.let {
                        Text(
                            "When: ${java.text.SimpleDateFormat("EEE, MMM d · HH:mm", java.util.Locale.US).format(it)}",
                            color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = {
                    stopListening()
                    onDismiss()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel", color = colors.onSurfaceVariant)
            }
            Surface(
                onClick = {
                    stopListening()
                    val text = transcript.ifBlank { partial }.trim()
                    if (text.isBlank()) {
                        Toast.makeText(context, "Say something first.", Toast.LENGTH_SHORT).show()
                        return@Surface
                    }
                    onParsed(VoiceCaptureParser.parse(text, accounts))
                },
                shape = RoundedCornerShape(12.dp),
                color = colors.primary,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Sync, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(18.dp))
                    Text("  Use", color = colors.onPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
