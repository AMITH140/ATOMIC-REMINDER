package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class AppBlockerAccessibilityService : AccessibilityService() {

    private fun isTimeInGuardInterval(startStr: String, endStr: String): Boolean {
        try {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentCalendar = Calendar.getInstance()
            val currentTimeStr = format.format(currentCalendar.time)
            
            val currentTime = format.parse(currentTimeStr)
            val startTime = format.parse(startStr)
            val endTime = format.parse(endStr)
            
            if (currentTime != null && startTime != null && endTime != null) {
                val startCal = Calendar.getInstance().apply { time = startTime }
                val endCal = Calendar.getInstance().apply { time = endTime }
                val currCal = Calendar.getInstance().apply { time = currentTime }
                
                // Extract hours and minutes
                val startMins = startCal.get(Calendar.HOUR_OF_DAY) * 60 + startCal.get(Calendar.MINUTE)
                val endMins = endCal.get(Calendar.HOUR_OF_DAY) * 60 + endCal.get(Calendar.MINUTE)
                val currMins = currCal.get(Calendar.HOUR_OF_DAY) * 60 + currCal.get(Calendar.MINUTE)
                
                if (startMins <= endMins) {
                    return currMins in startMins..endMins
                } else {
                    // Spans midnight
                    return currMins >= startMins || currMins <= endMins
                }
            }
        } catch (e: Exception) {
            Log.e("AppBlocker", "Error parsing time", e)
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            val sharedPrefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            val blockedPackages = sharedPrefs.getStringSet("blocked_packages", setOf()) ?: setOf()
            
            val morningGuardEnabled = sharedPrefs.getBoolean("morning_guard_enabled", true)
            val morningStart = sharedPrefs.getString("morning_start", "06:00 AM") ?: "06:00 AM"
            val morningEnd = sharedPrefs.getString("morning_end", "09:00 AM") ?: "09:00 AM"
            
            val eveningGuardEnabled = sharedPrefs.getBoolean("evening_guard_enabled", true)
            val eveningStart = sharedPrefs.getString("evening_start", "10:00 PM") ?: "10:00 PM"
            val eveningEnd = sharedPrefs.getString("evening_end", "07:00 AM") ?: "07:00 AM"
            
            val isMorningGuardActive = morningGuardEnabled && isTimeInGuardInterval(morningStart, morningEnd)
            val isEveningGuardActive = eveningGuardEnabled && isTimeInGuardInterval(eveningStart, eveningEnd)
            
            if ((isMorningGuardActive || isEveningGuardActive) && blockedPackages.contains(packageName)) {
                Log.d("AppBlocker", "Blocked app launched: $packageName")
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("blocked_app", packageName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Not used
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        serviceInfo = info
        Log.d("AppBlocker", "Service Connected")
    }
}
