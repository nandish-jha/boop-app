package com.nandish.productivity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.nandish.productivity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var crashPromptPosted: Boolean = false

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.background)
        window.navigationBarColor = getColor(R.color.background)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            binding.root.post { CrashReporter.promptAutoIfPending(this) }
        }

        maybeRequestPostNotifications()

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)

        binding.fabSettings.setOnClickListener { view ->
            if (StateRepository.get().settings.hapticsEnabled) {
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
            val cur = navHost.childFragmentManager.primaryNavigationFragment
            if (cur is StitchWebFragment) cur.showQuickAddMenu()
            else navController.navigate(R.id.settingsFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.fabSettings.visibility =
                if (destination.id == R.id.settingsFragment) View.GONE else View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        if (crashPromptPosted || isFinishing) return
        crashPromptPosted = true
        window.decorView.post {
            if (isFinishing || isDestroyed) return@post
            try {
                CrashReporter.promptAutoIfPending(this@MainActivity)
            } catch (e: Exception) {
                Log.e("MainActivity", "Crash report prompt failed", e)
            }
        }
    }

    private fun maybeRequestPostNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
