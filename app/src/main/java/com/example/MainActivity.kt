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
import androidx.compose.foundation.layout.consumeWindowInsets

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

    com.google.android.gms.ads.MobileAds.initialize(this) {}
    
    val requestConfiguration = com.google.android.gms.ads.RequestConfiguration.Builder()
        .setTestDeviceIds(listOf("6B7A5D74-6D7C-4F9E-A00A-10CA1AD1ABE1", com.google.android.gms.ads.AdRequest.DEVICE_ID_EMULATOR))
        .build()
    com.google.android.gms.ads.MobileAds.setRequestConfiguration(requestConfiguration)

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val blockedApp = intent.getStringExtra("BLOCKED_APP")
        val guardType = intent.getStringExtra("GUARD_TYPE")
        val guardEndTime = intent.getStringExtra("guard_end_time")
        
        // Clear extras so they don't trigger the block screen on subsequent normal app launches
        intent.removeExtra("BLOCKED_APP")
        intent.removeExtra("GUARD_TYPE")
        intent.removeExtra("guard_end_time")
        
        AtomicReminderApp(blockedApp, guardType, guardEndTime)
      }
    }
  }
}

@Composable
fun AtomicReminderApp(blockedApp: String? = null, guardType: String? = null, guardEndTime: String? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPreferences = androidx.compose.runtime.remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            val serviceIntent = android.content.Intent(context, com.example.service.AppBlockerService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        val morningEnabled = sharedPreferences.getBoolean("morning_guard_enabled", true)
        val eveningEnabled = sharedPreferences.getBoolean("evening_guard_enabled", true)
        
        if (morningEnabled) {
            val morningStart = sharedPreferences.getString("morning_start", "06:00 AM") ?: "06:00 AM"
            com.example.util.NotificationHelper.scheduleDailyReminder(
                context, 
                1001, 
                "Morning Guard", 
                "Your morning focus session has started.", 
                morningStart
            )
        }
        if (eveningEnabled) {
            val eveningStart = sharedPreferences.getString("evening_start", "10:00 PM") ?: "10:00 PM"
            com.example.util.NotificationHelper.scheduleDailyReminder(
                context, 
                1002, 
                "Evening Guard", 
                "Your evening wind-down session has started.", 
                eveningStart
            )
        }
        
        // Schedule daily summaries
        val habitsSummaryEnabled = sharedPreferences.getBoolean("habits_summary_enabled", true)
        val todosSummaryEnabled = sharedPreferences.getBoolean("todos_summary_enabled", false)
        val summaryTime = sharedPreferences.getString("summary_time", "08:00 AM") ?: "08:00 AM"
        
        if (habitsSummaryEnabled) {
            com.example.util.NotificationHelper.scheduleDailySummary(context, 100, "habits", summaryTime)
        } else {
            com.example.util.NotificationHelper.cancelReminder(context, 100)
        }
        
        if (todosSummaryEnabled) {
            com.example.util.NotificationHelper.scheduleDailySummary(context, 101, "todos", summaryTime)
        } else {
            com.example.util.NotificationHelper.cancelReminder(context, 101)
        }
    }

    val userProfileState = androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf(
            com.example.ui.state.UserProfile(
                name = sharedPreferences.getString("user_name", "User") ?: "User",
                isOnboardingComplete = sharedPreferences.getBoolean("is_onboarding_complete", false)
            )
        ) 
    }
    val habitState = androidx.compose.runtime.remember { com.example.ui.state.HabitState(sharedPreferences) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(60000L) // 1 minute
            habitState.lastBreakMins.intValue = ((System.currentTimeMillis() - habitState.lastBreakTimestamp.value) / 60000).toInt()
            
            // Trigger widget update
            val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
            val thisWidget = android.content.ComponentName(context, com.example.widget.HabitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (appWidgetIds.isNotEmpty()) {
                val intent = android.content.Intent(context, com.example.widget.HabitWidgetProvider::class.java).apply {
                    action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(
        habitState.currentWaterMl.intValue, 
        habitState.lastBreakTimestamp.value, 
        habitState.cupSizeMl.intValue,
        habitState.totalGoalLiters.floatValue,
        habitState.sedentaryMinutes.intValue,
        habitState.premiumDaysRemaining.intValue,
        habitState.adsWatched.intValue,
        habitState.waterHistory.value,
        habitState.movementHistory.value
    ) {
        sharedPreferences.edit()
            .putInt("current_water_ml", habitState.currentWaterMl.intValue)
            .putLong("last_break_timestamp", habitState.lastBreakTimestamp.value)
            .putInt("cup_size_ml", habitState.cupSizeMl.intValue)
            .putFloat("total_goal_liters", habitState.totalGoalLiters.floatValue)
            .putInt("sedentary_minutes", habitState.sedentaryMinutes.intValue)
            .putInt("premium_days", habitState.premiumDaysRemaining.intValue)
            .putInt("ads_watched", habitState.adsWatched.intValue)
            .putString("water_history", habitState.waterHistory.value.joinToString(","))
            .putString("movement_history", habitState.movementHistory.value.joinToString(","))
            .apply()
            
        // Trigger widget update
        val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(context)
        val thisWidget = android.content.ComponentName(context, com.example.widget.HabitWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        if (appWidgetIds.isNotEmpty()) {
            val intent = android.content.Intent(context, com.example.widget.HabitWidgetProvider::class.java).apply {
                action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            // Do nothing on result
        }
    )
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    
    androidx.compose.runtime.CompositionLocalProvider(
        com.example.ui.state.LocalUserProfile provides userProfileState,
        com.example.ui.state.LocalHabitState provides habitState
    ) {
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
            val prefListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                if (key == "current_water_ml") {
                    habitState.currentWaterMl.intValue = prefs.getInt("current_water_ml", habitState.currentWaterMl.intValue)
                } else if (key == "last_break_mins") {
                    habitState.lastBreakMins.intValue = prefs.getInt("last_break_mins", habitState.lastBreakMins.intValue)
                }
            }
            sharedPreferences.registerOnSharedPreferenceChangeListener(prefListener)
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                    habitState.checkNewDay(sharedPreferences)
                    habitState.currentWaterMl.intValue = sharedPreferences.getInt("current_water_ml", habitState.currentWaterMl.intValue)
                    habitState.lastBreakMins.intValue = sharedPreferences.getInt("last_break_mins", habitState.lastBreakMins.intValue)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(prefListener)
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        
        val navController = rememberNavController()
        
        val context = androidx.compose.ui.platform.LocalContext.current
        val activity = context as? android.app.Activity
        val currentIntent = activity?.intent
        
        androidx.compose.runtime.LaunchedEffect(currentIntent) {
            val navigateTo = currentIntent?.getStringExtra("navigate_to")
            if (navigateTo == "add_todo") {
                navController.navigate(Screen.Todo.route)
                currentIntent.removeExtra("navigate_to")
            }
        }
        
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            com.example.ui.components.PremiumBackground()
            
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                if (currentRoute in listOf(Screen.Dashboard.route, Screen.Progress.route, Screen.Premium.route, Screen.Todo.route)) {
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
                startDestination = if (blockedApp != null) "blocked" else if (userProfileState.value.isOnboardingComplete) Screen.Dashboard.route else "onboarding",
                modifier = Modifier
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .consumeWindowInsets(androidx.compose.foundation.layout.PaddingValues(bottom = innerPadding.calculateBottomPadding()))
            ) {
                composable("blocked") {
                    com.example.ui.screens.BlockedAppScreen(
                        blockedApp = blockedApp ?: "App",
                        guardType = guardType ?: "Morning Guard",
                        guardEndTime = guardEndTime ?: "09:00 AM",
                        onClose = { 
                            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
                            intent.addCategory(android.content.Intent.CATEGORY_HOME)
                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                            (context as? android.app.Activity)?.finishAffinity()
                        }
                    )
                }
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
                        },
                        onNavigateToDailyHabits = { navController.navigate("dailyHabits") }
                    ) 
                }
                composable(Screen.Focus.route) { FocusScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Screen.Progress.route) { ProgressScreen() }
                composable(Screen.Todo.route) { com.example.ui.screens.TodoScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Screen.Premium.route) { PremiumScreen() }
                composable("settings") { SettingsScreen() }
                composable("hydration") { com.example.ui.screens.HydrationScreen(onNavigateBack = { navController.popBackStack() }) }
                composable("movement") { com.example.ui.screens.MovementScreen(onNavigateBack = { navController.popBackStack() }) }
                composable("dailyHabits") { com.example.ui.screens.DailyHabitsScreen(onNavigateBack = { navController.popBackStack() }) }
            }
        }
        }
    }
}
