package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.Calendar

class AppBlockerService : Service() {
    
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1001, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startPolling()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        scope.cancel()
    }

    private fun startPolling() {
        scope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            var lastBlockedApp: String? = null
            var lastBlockTime: Long = 0
            var lastEventTime = System.currentTimeMillis() - 1000
            
            while (isRunning) {
                val time = System.currentTimeMillis()
                val events = usageStatsManager.queryEvents(lastEventTime, time)
                val event = android.app.usage.UsageEvents.Event()
                var currentForegroundApp: String? = null
                
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) {
                        currentForegroundApp = event.packageName
                        lastEventTime = event.timeStamp
                    }
                }
                
                if (currentForegroundApp != null && currentForegroundApp != packageName) {
                    val blockResult = checkAndBlockApp(currentForegroundApp)
                    if (blockResult != null) {
                        if (currentForegroundApp != lastBlockedApp || (time - lastBlockTime) > 3000) {
                            lastBlockedApp = currentForegroundApp
                            lastBlockTime = time
                            
                            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(homeIntent)
                            
                            val blockedIntent = Intent(this@AppBlockerService, com.example.ui.screens.BlockedActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                putExtra("BLOCKED_APP", currentForegroundApp)
                                putExtra("GUARD_TYPE", blockResult.first)
                                putExtra("guard_end_time", blockResult.second)
                            }
                            startActivity(blockedIntent)
                        }
                    } else {
                        if (currentForegroundApp != lastBlockedApp) {
                            lastBlockedApp = null
                        }
                    }
                }
                
                if (time > lastEventTime) {
                    lastEventTime = time
                }
                
                delay(750)
            }
        }
    }

    private var cachedBlockedApps: Set<String>? = null
    private var lastCacheTime: Long = 0

    private fun checkAndBlockApp(topPackage: String): Pair<String, String>? {
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentTime = System.currentTimeMillis()
        
        if (cachedBlockedApps == null || currentTime - lastCacheTime > 5000) {
            cachedBlockedApps = sharedPrefs.getStringSet("blocked_packages", emptySet())
            lastCacheTime = currentTime
        }
        
        val blockedApps = cachedBlockedApps ?: emptySet()
        
        if (blockedApps.contains(topPackage)) {
            var isCurrentlyGuarded = false
            var activeGuardType = ""
            
            val manualGuardActive = sharedPrefs.getBoolean("guard_active", false)
            if (manualGuardActive) {
                isCurrentlyGuarded = true
                activeGuardType = "Focus Guard"
            } else {
                val morningEnabled = sharedPrefs.getBoolean("morning_guard_enabled", true)
                val eveningEnabled = sharedPrefs.getBoolean("evening_guard_enabled", true)
                
                val morningStartStr = sharedPrefs.getString("morning_start", "06:00 AM") ?: "06:00 AM"
                val morningEndStr = sharedPrefs.getString("morning_end", "09:00 AM") ?: "09:00 AM"
                val eveningStartStr = sharedPrefs.getString("evening_start", "10:00 PM") ?: "10:00 PM"
                val eveningEndStr = sharedPrefs.getString("evening_end", "06:00 AM") ?: "06:00 AM"
                
                val cal = Calendar.getInstance()
                val currentHour = cal.get(Calendar.HOUR_OF_DAY)
                val currentMin = cal.get(Calendar.MINUTE)
                val currentTimeInMinutes = currentHour * 60 + currentMin
                
                fun parseTime(timeStr: String): Int {
                    if (timeStr.length < 5) return 0
                    val isPM = timeStr.endsWith("PM")
                    val parts = timeStr.substring(0, 5).trim().split(":")
                    if (parts.size < 2) return 0
                    var hour = parts[0].toIntOrNull() ?: 0
                    val minute = parts[1].toIntOrNull() ?: 0
                    if (isPM && hour != 12) hour += 12
                    if (!isPM && hour == 12) hour = 0
                    return hour * 60 + minute
                }
                
                val mStart = parseTime(morningStartStr)
                val mEnd = parseTime(morningEndStr)
                val eStart = parseTime(eveningStartStr)
                val eEnd = parseTime(eveningEndStr)
                
                if (morningEnabled && currentTimeInMinutes in mStart..mEnd) {
                    isCurrentlyGuarded = true
                    activeGuardType = "Morning Guard"
                } else if (eveningEnabled) {
                    if (eStart > eEnd) {
                        if (currentTimeInMinutes >= eStart || currentTimeInMinutes <= eEnd) {
                            isCurrentlyGuarded = true
                            activeGuardType = "Evening Guard"
                        }
                    } else {
                        if (currentTimeInMinutes in eStart..eEnd) {
                            isCurrentlyGuarded = true
                            activeGuardType = "Evening Guard"
                        }
                    }
                }
            }
            
            if (isCurrentlyGuarded) {
                val endTime = when (activeGuardType) {
                    "Morning Guard" -> sharedPrefs.getString("morning_end", "09:00 AM") ?: "09:00 AM"
                    "Evening Guard" -> sharedPrefs.getString("evening_end", "06:00 AM") ?: "06:00 AM"
                    else -> "Manual"
                }
                return Pair(activeGuardType, endTime)
            }
        }
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "blocker_service",
                "App Blocker Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "blocker_service")
            .setContentTitle("App Blocker")
            .setContentText("Guarding your focus...")
            .setSmallIcon(android.R.drawable.ic_secure)
            .build()
    }
}
