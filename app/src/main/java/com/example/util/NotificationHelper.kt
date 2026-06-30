package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val premiumDays = prefs.getInt("premium_days", 0)
        val isTodo = intent.getBooleanExtra("isTodo", false)
        
        if (premiumDays <= 0 && !isTodo) {
            return
        }

        val isDailySummary = intent.getBooleanExtra("isDailySummary", false)
        val summaryType = intent.getStringExtra("summaryType")
        
        var title = intent.getStringExtra("title") ?: "Reminder"
        var message = intent.getStringExtra("message") ?: "Time to check your habits!"
        
        val notificationId = intent.getIntExtra("id", 1)
        val intervalMins = intent.getIntExtra("intervalMins", -1)
        val isDaily = intent.getBooleanExtra("isDaily", false)
        val timeString = intent.getStringExtra("timeString")
        
        if (title == "Movement Reminder" || title == "Water Reminder") {
            val prefix = if (title == "Movement Reminder") "movement" else "water"
            val startTimeStr = prefs.getString("${prefix}_start_time", "08:00 AM") ?: "08:00 AM"
            val endTimeStr = prefs.getString("${prefix}_end_time", "10:00 PM") ?: "10:00 PM"
            
            val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
            val startTime = try { format.parse(startTimeStr) } catch(e: Exception) { null }
            val endTime = try { format.parse(endTimeStr) } catch(e: Exception) { null }
            
            if (startTime != null && endTime != null) {
                val calNow = java.util.Calendar.getInstance()
                val currentMins = calNow.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calNow.get(java.util.Calendar.MINUTE)
                
                val calStart = java.util.Calendar.getInstance().apply { time = startTime }
                val startMins = calStart.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calStart.get(java.util.Calendar.MINUTE)
                
                val calEnd = java.util.Calendar.getInstance().apply { time = endTime }
                val endMins = calEnd.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calEnd.get(java.util.Calendar.MINUTE)
                
                val inRange = if (startMins <= endMins) {
                    currentMins in startMins..endMins
                } else {
                    currentMins >= startMins || currentMins <= endMins
                }
                
                if (!inRange) {
                    if (intervalMins > 0) {
                        NotificationHelper.scheduleReminder(context, notificationId, title, message, intervalMins)
                    }
                    return
                }
            }
        }
        
        if (isDailySummary) {
            title = "Daily Summary"
            kotlinx.coroutines.GlobalScope.launch {
                val db = com.example.data.AppDatabase.getDatabase(context)
                
                if (summaryType == "habits") {
                    val allHabits = db.habitDao().getAllHabitsSync()
                    val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val logs = db.habitDao().getLogsForDateSync(todayStr)
                    
                    val pendingHabits = allHabits.filter { habit ->
                        val log = logs.find { it.habitId == habit.id }
                        log == null || !log.completed
                    }
                    
                    val habitMessage = if (pendingHabits.isNotEmpty()) {
                        "Today you have to - " + pendingHabits.take(3).joinToString(", ") { it.name } + if (pendingHabits.size > 3) " and ${pendingHabits.size - 3} more" else ""
                    } else if (allHabits.isEmpty()) {
                        "No habits created yet. Add some to get started!"
                    } else {
                        "All habits completed for today. Great job!"
                    }
                    
                    showNotification(context, notificationId, title, habitMessage)
                    
                    if (timeString != null) {
                        NotificationHelper.scheduleDailySummary(context, notificationId, summaryType, timeString)
                    }
                } else if (summaryType == "todos") {
                    val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val todayTodos = db.todoDao().getAllTodosSync().filter { it.scheduledDate == todayStr && !it.completed && !it.isArchived }
                    
                    val todoMessage = if (todayTodos.isNotEmpty()) {
                        "Today you have to - " + todayTodos.take(3).joinToString(", ") { it.title } + if (todayTodos.size > 3) " and ${todayTodos.size - 3} more" else ""
                    } else {
                        "No tasks for today. Enjoy your day!"
                    }
                    
                    showNotification(context, notificationId, title, todoMessage)
                    
                    if (timeString != null) {
                        NotificationHelper.scheduleDailySummary(context, notificationId, summaryType, timeString)
                    }
                }
            }
            return
        }
        
        showNotification(context, notificationId, title, message)
        
        // Reschedule
        if (isDailySummary && timeString != null && summaryType != null) {
            NotificationHelper.scheduleDailySummary(context, notificationId, summaryType, timeString)
        } else if (isDaily && timeString != null) {
            NotificationHelper.scheduleDailyReminder(context, notificationId, title, message, timeString)
        } else if (intervalMins > 0) {
            NotificationHelper.scheduleReminder(context, notificationId, title, message, intervalMins)
        }
    }
    
    private fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "atomic_reminders_high",
                "Atomic Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "atomic_reminders_high")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Changed to high
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        notificationManager.notify(notificationId, builder.build())
    }
}

object NotificationHelper {
    fun scheduleDailySummary(context: Context, id: Int, summaryType: String, timeString: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("isDailySummary", true)
            putExtra("summaryType", summaryType)
            putExtra("timeString", timeString)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        val targetTime = try { format.parse(timeString) } catch (e: Exception) { null } ?: return
        
        val targetCalendar = java.util.Calendar.getInstance().apply { time = targetTime }
        
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, targetCalendar.get(java.util.Calendar.HOUR_OF_DAY))
        calendar.set(java.util.Calendar.MINUTE, targetCalendar.get(java.util.Calendar.MINUTE))
        calendar.set(java.util.Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        
        val triggerAtMillis = calendar.timeInMillis

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 60 * 1000L, pendingIntent)
        }
    }

    fun scheduleDailyReminder(context: Context, id: Int, title: String, message: String, timeString: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("id", id)
            putExtra("isDaily", true)
            putExtra("timeString", timeString)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        val targetTime = try { format.parse(timeString) } catch (e: Exception) { null } ?: return
        
        val targetCalendar = java.util.Calendar.getInstance().apply { time = targetTime }
        
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, targetCalendar.get(java.util.Calendar.HOUR_OF_DAY))
        calendar.set(java.util.Calendar.MINUTE, targetCalendar.get(java.util.Calendar.MINUTE))
        calendar.set(java.util.Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        
        val triggerAtMillis = calendar.timeInMillis

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 60 * 1000L, pendingIntent)
        }
    }

    fun scheduleReminder(context: Context, id: Int, title: String, message: String, intervalMins: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("id", id)
            putExtra("intervalMins", intervalMins)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = intervalMins * 60 * 1000L
        val triggerAtMillis = System.currentTimeMillis() + intervalMillis

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 60 * 1000L, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleTodoReminder(context: Context, id: Int, title: String, message: String, triggerAtMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("id", id)
            putExtra("isTodo", true)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 60 * 1000L, pendingIntent)
        }
    }
}
