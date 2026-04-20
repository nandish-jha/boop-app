package com.nandish.productivity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = getColor(R.color.background)
        window.navigationBarColor = getColor(R.color.background)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.toolbar_menu)
        toolbar.setNavigationOnClickListener {
            Toast.makeText(this, "Menu", Toast.LENGTH_SHORT).show()
        }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_settings -> {
                    navController.navigate(R.id.settingsFragment)
                    true
                }
                else -> false
            }
        }

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController
        findViewById<BottomNavigationView>(R.id.bottom_nav).setupWithNavController(navController)
    }
}
