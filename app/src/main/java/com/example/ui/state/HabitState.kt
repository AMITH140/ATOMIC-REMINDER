package com.example.ui.state

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

import android.content.SharedPreferences
import java.util.Calendar

class HabitState(prefs: SharedPreferences? = null) {
    var currentWaterMl = mutableIntStateOf(0)
    var waterCups = mutableIntStateOf(0)
    var totalGoalLiters = mutableFloatStateOf(2.5f)
    var cupSizeMl = mutableIntStateOf(250)
    
    var lastBreakTimestamp = mutableStateOf(System.currentTimeMillis())
    var lastBreakMins = mutableIntStateOf(0)
    var sedentaryMinutes = mutableIntStateOf(45)
    var movementEnabled = mutableStateOf(true)
    var movementStartTime = mutableStateOf("08:00 AM")
    var movementEndTime = mutableStateOf("10:00 PM")
    var waterStartTime = mutableStateOf("08:00 AM")
    var waterEndTime = mutableStateOf("10:00 PM")
    var guardActive = mutableStateOf(true)
    var hasShownGraffiti = mutableStateOf(false)

    var habitsSummaryEnabled = mutableStateOf(true)
    var todosSummaryEnabled = mutableStateOf(false)
    var summaryTime = mutableStateOf("08:00 AM")
    
    // Streak Feature
    var streakDays = mutableIntStateOf(12)

    // Notification Intervals
    var waterIntervalMins = mutableIntStateOf(60)
    var movementIntervalMins = mutableIntStateOf(45)
    // Premium Feature
    var premiumDaysRemaining = mutableIntStateOf(1)
    var adsWatched = mutableIntStateOf(0)

    var waterHistory = mutableStateOf(listOf<String>())
    var movementHistory = mutableStateOf(listOf<String>())

    init {
        if (prefs != null) {
            val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val lastDay = prefs.getInt("last_opened_day", -1)
            
            cupSizeMl.intValue = prefs.getInt("cup_size_ml", 250)
            premiumDaysRemaining.intValue = prefs.getInt("premium_days", 1)
            
            // Fix bug where 14 or 3 was saved previously
            if (premiumDaysRemaining.intValue == 14 || premiumDaysRemaining.intValue == 3) {
                premiumDaysRemaining.intValue = 1
                prefs.edit().putInt("premium_days", 1).apply()
            }
            
            adsWatched.intValue = prefs.getInt("ads_watched", 0)
            
            streakDays.intValue = prefs.getInt("streak_days", 0)
            
            if (lastDay != today && lastDay != -1) {
                if (lastDay == today - 1 || (today == 1 && lastDay >= 365)) {
                    val wasWaterMet = prefs.getInt("current_water_ml", 0) >= (prefs.getFloat("total_goal_liters", 2.5f) * 1000).toInt()
                    // Simple check for if they tracked anything yesterday
                    if (wasWaterMet) {
                        streakDays.intValue++
                    } else {
                        streakDays.intValue = 0
                    }
                } else {
                    streakDays.intValue = 0
                }
                
                currentWaterMl.intValue = 0
                lastBreakTimestamp.value = System.currentTimeMillis()
                lastBreakMins.intValue = 0
                movementStartTime.value = prefs.getString("movement_start_time", "08:00 AM") ?: "08:00 AM"
                movementEndTime.value = prefs.getString("movement_end_time", "10:00 PM") ?: "10:00 PM"
                waterStartTime.value = prefs.getString("water_start_time", "08:00 AM") ?: "08:00 AM"
                waterEndTime.value = prefs.getString("water_end_time", "10:00 PM") ?: "10:00 PM"
                habitsSummaryEnabled.value = prefs.getBoolean("habits_summary_enabled", true)
                todosSummaryEnabled.value = prefs.getBoolean("todos_summary_enabled", false)
                summaryTime.value = prefs.getString("summary_time", "08:00 AM") ?: "08:00 AM"
                hasShownGraffiti.value = false
                waterHistory.value = emptyList()
                movementHistory.value = emptyList()
                if (premiumDaysRemaining.intValue > 0) {
                    premiumDaysRemaining.intValue--
                }
                prefs.edit()
                    .putInt("current_water_ml", 0)
                    .putLong("last_break_timestamp", lastBreakTimestamp.value)
                    .putString("water_history", "")
                    .putString("movement_history", "")
                    .putInt("premium_days", premiumDaysRemaining.intValue)
                    .putInt("last_opened_day", today)
                    .putInt("streak_days", streakDays.intValue)
                    .putBoolean("has_shown_graffiti", false)
                    .apply()
            } else {
                currentWaterMl.intValue = prefs.getInt("current_water_ml", 750) // Default for preview
                lastBreakTimestamp.value = prefs.getLong("last_break_timestamp", System.currentTimeMillis())
                lastBreakMins.intValue = ((System.currentTimeMillis() - lastBreakTimestamp.value) / 60000).toInt()
                movementStartTime.value = prefs.getString("movement_start_time", "08:00 AM") ?: "08:00 AM"
                movementEndTime.value = prefs.getString("movement_end_time", "10:00 PM") ?: "10:00 PM"
                waterStartTime.value = prefs.getString("water_start_time", "08:00 AM") ?: "08:00 AM"
                waterEndTime.value = prefs.getString("water_end_time", "10:00 PM") ?: "10:00 PM"
                habitsSummaryEnabled.value = prefs.getBoolean("habits_summary_enabled", true)
                todosSummaryEnabled.value = prefs.getBoolean("todos_summary_enabled", false)
                summaryTime.value = prefs.getString("summary_time", "08:00 AM") ?: "08:00 AM"
                hasShownGraffiti.value = prefs.getBoolean("has_shown_graffiti", false)
                val wHistory = prefs.getString("water_history", "") ?: ""
                waterHistory.value = if (wHistory.isNotEmpty()) wHistory.split(",") else emptyList()
                val mHistory = prefs.getString("movement_history", "") ?: ""
                movementHistory.value = if (mHistory.isNotEmpty()) mHistory.split(",") else emptyList()
                prefs.edit().putInt("last_opened_day", today).apply()
            }
        } else {
            currentWaterMl.intValue = 750
            lastBreakTimestamp.value = System.currentTimeMillis()
            lastBreakMins.intValue = 0
            waterHistory.value = listOf("09:00 AM", "11:30 AM", "01:15 PM")
            movementHistory.value = listOf("10:00 AM", "12:00 PM")
        }
    }

    fun checkNewDay(prefs: SharedPreferences) {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val lastDay = prefs.getInt("last_opened_day", -1)
        
        if (lastDay != today && lastDay != -1) {
            if (lastDay == today - 1 || (today == 1 && lastDay >= 365)) {
                val wasWaterMet = prefs.getInt("current_water_ml", 0) >= (prefs.getFloat("total_goal_liters", 2.5f) * 1000).toInt()
                if (wasWaterMet) {
                    streakDays.intValue++
                } else {
                    streakDays.intValue = 0
                }
            } else {
                streakDays.intValue = 0
            }

            currentWaterMl.intValue = 0
            lastBreakTimestamp.value = System.currentTimeMillis()
            lastBreakMins.intValue = 0
            movementStartTime.value = prefs.getString("movement_start_time", "08:00 AM") ?: "08:00 AM"
            movementEndTime.value = prefs.getString("movement_end_time", "10:00 PM") ?: "10:00 PM"
            waterStartTime.value = prefs.getString("water_start_time", "08:00 AM") ?: "08:00 AM"
            waterEndTime.value = prefs.getString("water_end_time", "10:00 PM") ?: "10:00 PM"
            habitsSummaryEnabled.value = prefs.getBoolean("habits_summary_enabled", true)
            todosSummaryEnabled.value = prefs.getBoolean("todos_summary_enabled", false)
            summaryTime.value = prefs.getString("summary_time", "08:00 AM") ?: "08:00 AM"
            hasShownGraffiti.value = false
            waterHistory.value = emptyList()
            movementHistory.value = emptyList()
            cupSizeMl.intValue = prefs.getInt("cup_size_ml", 250)
            prefs.edit()
                .putInt("current_water_ml", 0)
                .putLong("last_break_timestamp", lastBreakTimestamp.value)
                .putString("water_history", "")
                .putString("movement_history", "")
                .putInt("last_opened_day", today)
                .putInt("streak_days", streakDays.intValue)
                .putBoolean("has_shown_graffiti", false)
                .apply()
        } else {
            currentWaterMl.intValue = prefs.getInt("current_water_ml", currentWaterMl.intValue)
            lastBreakTimestamp.value = prefs.getLong("last_break_timestamp", lastBreakTimestamp.value)
            lastBreakMins.intValue = ((System.currentTimeMillis() - lastBreakTimestamp.value) / 60000).toInt()
            movementStartTime.value = prefs.getString("movement_start_time", "08:00 AM") ?: "08:00 AM"
            movementEndTime.value = prefs.getString("movement_end_time", "10:00 PM") ?: "10:00 PM"
            waterStartTime.value = prefs.getString("water_start_time", "08:00 AM") ?: "08:00 AM"
            waterEndTime.value = prefs.getString("water_end_time", "10:00 PM") ?: "10:00 PM"
            habitsSummaryEnabled.value = prefs.getBoolean("habits_summary_enabled", true)
            todosSummaryEnabled.value = prefs.getBoolean("todos_summary_enabled", false)
            summaryTime.value = prefs.getString("summary_time", "08:00 AM") ?: "08:00 AM"
            hasShownGraffiti.value = prefs.getBoolean("has_shown_graffiti", false)
            cupSizeMl.intValue = prefs.getInt("cup_size_ml", cupSizeMl.intValue)
            
            val wHistory = prefs.getString("water_history", "") ?: ""
            waterHistory.value = if (wHistory.isNotEmpty()) wHistory.split(",") else emptyList()
            val mHistory = prefs.getString("movement_history", "") ?: ""
            movementHistory.value = if (mHistory.isNotEmpty()) mHistory.split(",") else emptyList()
            
            prefs.edit().putInt("last_opened_day", today).apply()
        }
    }

    fun saveMovementTimes(prefs: SharedPreferences) {
        prefs.edit()
            .putString("movement_start_time", movementStartTime.value)
            .putString("movement_end_time", movementEndTime.value)
            .apply()
    }

    fun saveWaterTimes(prefs: SharedPreferences) {
        prefs.edit()
            .putString("water_start_time", waterStartTime.value)
            .putString("water_end_time", waterEndTime.value)
            .apply()
    }

    fun saveSummarySettings(prefs: SharedPreferences) {
        prefs.edit()
            .putBoolean("habits_summary_enabled", habitsSummaryEnabled.value)
            .putBoolean("todos_summary_enabled", todosSummaryEnabled.value)
            .putString("summary_time", summaryTime.value)
            .apply()
    }

    fun setGraffitiShown(prefs: SharedPreferences?) {
        hasShownGraffiti.value = true
        prefs?.edit()?.putBoolean("has_shown_graffiti", true)?.apply()
    }

    fun addWaterTimestamp(prefs: SharedPreferences?) {
        if (premiumDaysRemaining.intValue <= 0) return
        val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val time = formatter.format(java.util.Date())
        waterHistory.value = waterHistory.value + time
        prefs?.edit()?.putString("water_history", waterHistory.value.joinToString(","))?.apply()
    }

    fun removeWaterTimestamp(prefs: SharedPreferences?) {
        if (premiumDaysRemaining.intValue <= 0) return
        if (waterHistory.value.isNotEmpty()) {
            waterHistory.value = waterHistory.value.dropLast(1)
            prefs?.edit()?.putString("water_history", waterHistory.value.joinToString(","))?.apply()
        }
    }

    fun addMovementTimestamp(prefs: SharedPreferences?) {
        if (premiumDaysRemaining.intValue <= 0) return
        val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val time = formatter.format(java.util.Date())
        movementHistory.value = movementHistory.value + time
        prefs?.edit()?.putString("movement_history", movementHistory.value.joinToString(","))?.apply()
    }
}

val LocalHabitState = compositionLocalOf<HabitState> {
    error("HabitState not provided")
}
