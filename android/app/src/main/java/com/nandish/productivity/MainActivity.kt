package com.nandish.productivity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.nandish.productivity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.background)
        window.navigationBarColor = getColor(R.color.background)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)

        binding.fabSettings.setOnClickListener {
            val cur = navHost.childFragmentManager.primaryNavigationFragment
            if (cur is StitchWebFragment) cur.showQuickAddMenu()
            else navController.navigate(R.id.settingsFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.fabSettings.visibility =
                if (destination.id == R.id.settingsFragment) View.GONE else View.VISIBLE
        }
    }
}
