package com.prodash.reminders

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

class BoopVoiceInteractionService : VoiceInteractionService()

class BoopVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession =
        BoopVoiceInteractionSession(this)
}

private class VoiceSessionLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    init {
        savedStateRegistryController.performRestore(null)
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun start() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun stop() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}

private class BoopVoiceInteractionSession(
    context: Context,
) : VoiceInteractionSession(context) {

    private var lifecycleOwner: VoiceSessionLifecycleOwner? = null

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        setUiEnabled(true)
        configureEdgeToEdgeWindow()

        val owner = VoiceSessionLifecycleOwner().also { lifecycleOwner = it }
        owner.start()

        val micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                val accounts = remember { BoopStoreAccess.readAccounts(context) }
                val colorScheme = darkColorScheme(
                    background = Color(0xFF000000),
                    surface = Color(0xFF141414),
                    surfaceVariant = Color(0xFF222222),
                    onBackground = Color(0xFFF2EFEB),
                    onSurface = Color(0xFFF2EFEB),
                    onSurfaceVariant = Color(0xFF8E8E92),
                    primary = Color(0xFF7FD4C8),
                    onPrimary = Color(0xFF071412),
                    secondary = Color(0xFF1C1C1C),
                )
                MaterialTheme(colorScheme = colorScheme, typography = boopTypography()) {
                    BoopTextTheme {
                        Box(Modifier.fillMaxSize()) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.48f))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) { finishSession() },
                            )
                            Box(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
                            ) {
                                VoiceCaptureSheet(
                                    accounts = accounts,
                                    autoStart = micGranted,
                                    startSignal = 0,
                                    stopSignal = 0,
                                    onDismiss = { finishSession() },
                                    onParsed = { parsed ->
                                        when (val outcome = VoiceCaptureCommit.commit(context, parsed)) {
                                            is VoiceCommitOutcome.Ok -> {
                                                Toast.makeText(context, outcome.message, Toast.LENGTH_SHORT).show()
                                                finishSession()
                                            }
                                            is VoiceCommitOutcome.FinanceNeedsEditor -> {
                                                Toast.makeText(context, outcome.message, Toast.LENGTH_SHORT).show()
                                                BoopVoiceResultHolder.deliver(outcome.parsed)
                                                val intent = Intent(context, MainActivity::class.java).apply {
                                                    action = VoiceAssistantBridge.ACTION_VOICE_RESULT
                                                    putExtra(
                                                        VoiceAssistantBridge.EXTRA_PARSED_JSON,
                                                        with(VoiceAssistantBridge) { outcome.parsed.toJson() },
                                                    )
                                                    addFlags(
                                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                            Intent.FLAG_ACTIVITY_SINGLE_TOP,
                                                    )
                                                }
                                                context.startActivity(intent)
                                                finishSession()
                                            }
                                        }
                                    },
                                    onListeningChanged = {},
                                    onRequestMicPermission = {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                        finishSession()
                                    },
                                    style = VoiceCaptureStyle.Floating,
                                )
                            }
                        }
                    }
                }
            }
        }
        setContentView(composeView)
    }

    private fun configureEdgeToEdgeWindow() {
        val win = window?.window ?: return
        WindowCompat.setDecorFitsSystemWindows(win, false)
        win.statusBarColor = AndroidColor.TRANSPARENT
        win.navigationBarColor = AndroidColor.TRANSPARENT
        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        win.attributes = win.attributes.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        win.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        WindowInsetsControllerCompat(win, win.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    override fun onHide() {
        lifecycleOwner?.stop()
        lifecycleOwner = null
        super.onHide()
    }

    private fun finishSession() {
        hide()
        finish()
    }
}
