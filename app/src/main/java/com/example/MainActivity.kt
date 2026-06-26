package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.BottomNavBar
import com.example.ui.components.Screen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.PremiumScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Request maximum refresh rate (e.g., 120Hz)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val displayObj = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            display
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        }
        val modes = displayObj?.supportedModes
        val maxRefreshRateMode = modes?.maxByOrNull { it.refreshRate }
        if (maxRefreshRateMode != null) {
            val layoutParams = window.attributes
            layoutParams.preferredDisplayModeId = maxRefreshRateMode.modeId
            window.attributes = layoutParams
        }
    }

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AtomicReminderApp()
      }
    }
  }
}

@Composable
fun AtomicReminderApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPreferences = androidx.compose.runtime.remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    
    val userProfileState = androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf(
            com.example.ui.state.UserProfile(
                name = sharedPreferences.getString("user_name", "User") ?: "User",
                isOnboardingComplete = sharedPreferences.getBoolean("is_onboarding_complete", false)
            )
        ) 
    }
    val habitState = androidx.compose.runtime.remember { com.example.ui.state.HabitState() }
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { }
    )
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    androidx.compose.runtime.CompositionLocalProvider(
        com.example.ui.state.LocalUserProfile provides userProfileState,
        com.example.ui.state.LocalHabitState provides habitState
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            com.example.ui.components.PremiumBackground()
            
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                if (currentRoute in listOf(Screen.Dashboard.route, Screen.Focus.route, Screen.Progress.route, Screen.Premium.route)) {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (userProfileState.value.isOnboardingComplete) Screen.Dashboard.route else "onboarding",
                modifier = Modifier
            ) {
                composable("onboarding") {
                    com.example.ui.screens.OnboardingScreen(
                        onComplete = {
                            sharedPreferences.edit()
                                .putBoolean("is_onboarding_complete", true)
                                .putString("user_name", userProfileState.value.name)
                                .apply()
                            userProfileState.value = userProfileState.value.copy(isOnboardingComplete = true)
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Dashboard.route) { 
                    DashboardScreen(
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToHydration = { navController.navigate("hydration") },
                        onNavigateToMovement = { navController.navigate("movement") },
                        onNavigateToFocus = { 
                            navController.navigate(Screen.Focus.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) 
                }
                composable(Screen.Focus.route) { FocusScreen() }
                composable(Screen.Progress.route) { ProgressScreen() }
                composable(Screen.Premium.route) { PremiumScreen() }
                composable("settings") { SettingsScreen() }
                composable("hydration") { com.example.ui.screens.HydrationScreen(onNavigateBack = { navController.popBackStack() }) }
                composable("movement") { com.example.ui.screens.MovementScreen(onNavigateBack = { navController.popBackStack() }) }
            }
        }
        }
    }
}
