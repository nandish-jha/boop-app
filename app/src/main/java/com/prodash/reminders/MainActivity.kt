package com.prodash.reminders

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.prodash.reminders.ui.editor.EditorScreen
import com.prodash.reminders.ui.home.HomeScreen
import com.prodash.reminders.ui.account.AccountScreen
import com.prodash.reminders.ui.signin.SignInScreen
import com.prodash.reminders.ui.signin.SignInViewModel
import com.prodash.reminders.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val context = LocalContext.current
                val signInViewModel: SignInViewModel = viewModel()
                val googleLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult(),
                ) { result ->
                    signInViewModel.onGoogleActivityResult(result.data)
                }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!granted) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                RootNav(
                    signInViewModel = signInViewModel,
                    onLaunchGoogleSignIn = {
                        val client = signInViewModel.googleClient()
                        googleLauncher.launch(client.signInIntent)
                    },
                )
            }
        }
    }
}

@Composable
private fun RootNav(
    signInViewModel: SignInViewModel,
    onLaunchGoogleSignIn: () -> Unit,
) {
    val navController = rememberNavController()
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(user, currentRoute) {
        val signedIn = user != null
        if (signedIn && currentRoute == "signin") {
            navController.navigate("home") {
                popUpTo("signin") { inclusive = true }
                launchSingleTop = true
            }
        }
        if (!signedIn && currentRoute != null && currentRoute != "signin") {
            navController.navigate("signin") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = "signin") {
        composable("signin") {
            SignInScreen(
                onSignedIn = {
                    navController.navigate("home") {
                        popUpTo("signin") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLaunchGoogleSignIn = onLaunchGoogleSignIn,
                viewModel = signInViewModel,
            )
        }
        composable("home") {
            HomeScreen(
                onAddReminder = { navController.navigate("editor/new") },
                onOpenReminder = { id -> navController.navigate("editor/$id") },
                onOpenAccount = { navController.navigate("account") },
            )
        }
        composable("account") {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onSyncGoogleCalendar = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://calendar.google.com"))
                    navController.context.startActivity(intent)
                },
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("signin") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable("editor/new") {
            EditorScreen(
                reminderId = null,
                onBack = { navController.popBackStack() },
            )
        }
        composable("editor/{id}") { entry ->
            val id = entry.arguments?.getString("id")
            EditorScreen(
                reminderId = id,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
