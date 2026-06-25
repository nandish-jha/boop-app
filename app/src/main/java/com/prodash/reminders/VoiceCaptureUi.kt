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
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun VoiceCaptureSheet(
    accounts: List<BoopAccount>,
    onDismiss: () -> Unit,
    onParsed: (ParsedVoiceCapture) -> Unit,
) {
    val context = LocalContext.current
    var transcript by remember { mutableStateOf("") }
    var partial by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Tap the mic and speak.") }
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

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
        if (!granted) status = "Microphone permission denied."
    }

    fun startListening() {
        val sr = recognizer
        if (!micGranted) {
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
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
        listening = true
        partial = ""
        status = "Listening…"
        sr.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                listening = false
                status = "Processing…"
            }
            override fun onError(error: Int) {
                listening = false
                status = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that — try again."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected — try again."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                    else -> "Voice error ($error). Try again or type below."
                }
            }
            override fun onResults(results: Bundle?) {
                listening = false
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (text.isNotBlank()) {
                    transcript = text
                    partial = ""
                    status = "Got it — tap Use to open the editor."
                } else {
                    status = "No speech heard — try again."
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
        listening = false
        recognizer?.stopListening()
        status = "Stopped."
    }

    DisposableEffect(recognizer) {
        onDispose {
            recognizer?.destroy()
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Text(
            "Voice capture",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(status, color = Color(0xFF8E8E90), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingActionButton(
                onClick = { if (listening) stopListening() else startListening() },
                containerColor = Color.White,
                contentColor = Color.Black,
            ) {
                if (listening) {
                    Icon(Icons.Outlined.Stop, contentDescription = "Stop")
                } else {
                    Icon(Icons.Outlined.Mic, contentDescription = "Start listening")
                }
            }
        }

        val displayText = when {
            partial.isNotBlank() -> partial
            transcript.isNotBlank() -> transcript
            else -> ""
        }
        if (displayText.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF242426),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    displayText,
                    color = Color.White,
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
                color = Color(0xFF1A1A1E),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Detected: ${parsed.type.name.lowercase().replaceFirstChar { it.titlecase() }}",
                        color = Color(0xFF9AE6B4),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(parsed.title, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    parsed.amount?.let {
                        Text("Amount: $${"%.2f".format(it)}", color = Color(0xFFBFBFBF), style = MaterialTheme.typography.bodySmall)
                    }
                    parsed.dueAtMillis?.let {
                        Text(
                            "When: ${java.text.SimpleDateFormat("EEE, MMM d · HH:mm", java.util.Locale.US).format(it)}",
                            color = Color(0xFFBFBFBF),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Cancel", color = Color(0xFFBFBFBF))
            }
            Surface(
                onClick = {
                    val text = transcript.ifBlank { partial }.trim()
                    if (text.isBlank()) {
                        Toast.makeText(context, "Say something first.", Toast.LENGTH_SHORT).show()
                        return@Surface
                    }
                    onParsed(VoiceCaptureParser.parse(text, accounts))
                },
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Sync, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Text("  Use", color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
