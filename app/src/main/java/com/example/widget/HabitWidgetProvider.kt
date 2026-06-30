package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.R
import android.content.ComponentName

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        if (action == ACTION_ADD_WATER) {
            val currentWater = prefs.getInt("current_water_ml", 0)
            val cupSize = prefs.getInt("cup_size_ml", 250) // Fallback if missing
            prefs.edit().putInt("current_water_ml", currentWater + cupSize).apply()
            
            // Trigger UI update in app widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, HabitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        } else if (action == ACTION_LOG_MOVEMENT) {
            prefs.edit().putInt("last_break_mins", 0).apply()
            
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, HabitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_ADD_WATER = "com.example.widget.ACTION_ADD_WATER"
        const val ACTION_LOG_MOVEMENT = "com.example.widget.ACTION_LOG_MOVEMENT"

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val currentWater = prefs.getInt("current_water_ml", 0)
            val totalGoalLiters = prefs.getFloat("total_goal_liters", 2.5f)
            val totalGoalMl = (totalGoalLiters * 1000).toInt()
            val waterProgress = if (totalGoalMl > 0) ((currentWater.toFloat() / totalGoalMl) * 100).toInt().coerceIn(0, 100) else 0

            val lastBreakMins = prefs.getInt("last_break_mins", 0)
            val sedentaryMinutes = prefs.getInt("sedentary_minutes", 45)
            val moveProgress = if (sedentaryMinutes > 0) ((lastBreakMins.toFloat() / sedentaryMinutes) * 100).toInt().coerceIn(0, 100) else 0
            
            val views = RemoteViews(context.packageName, R.layout.habit_widget)
            views.setTextViewText(R.id.widget_water_text, "$currentWater ml")
            views.setProgressBar(R.id.widget_water_progress, 100, waterProgress, false)
            
            views.setTextViewText(R.id.widget_move_text, "Move")
            views.setProgressBar(R.id.widget_move_progress, 100, moveProgress, false)

            val waterIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                action = ACTION_ADD_WATER
            }
            val waterPendingIntent = PendingIntent.getBroadcast(
                context, 0, waterIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_water_btn, waterPendingIntent)

            val moveIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                action = ACTION_LOG_MOVEMENT
            }
            val movePendingIntent = PendingIntent.getBroadcast(
                context, 1, moveIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_move_btn, movePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
