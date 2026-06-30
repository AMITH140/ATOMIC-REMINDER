package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.R
import com.example.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TodoWidgetProvider : AppWidgetProvider() {

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
        if (intent.action == ACTION_CHANGE_FILTER) {
            val filter = intent.getStringExtra(EXTRA_FILTER) ?: "Today"
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("widget_filter", filter).apply()
            
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        } else if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            if (appWidgetIds != null) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view)
            }
        } else if (intent.action == ACTION_TOGGLE_TASK) {
            val taskId = intent.getIntExtra("task_id", -1)
            if (taskId != -1) {
                val db = com.example.data.AppDatabase.getDatabase(context)
                val todoDao = db.todoDao()
                kotlinx.coroutines.GlobalScope.launch {
                    val allTodos = todoDao.getAllTodosSync()
                    val todo = allTodos.find { it.id == taskId }
                    if (todo != null) {
                        todoDao.updateTodo(todo.copy(completed = !todo.completed))
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_CHANGE_FILTER = "com.example.widget.ACTION_CHANGE_FILTER"
        const val ACTION_TOGGLE_TASK = "com.example.widget.ACTION_TOGGLE_TASK"
        const val EXTRA_FILTER = "extra_filter"

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val currentFilter = prefs.getString("widget_filter", "Today") ?: "Today"
            val transparency = prefs.getInt("widget_transparency", 255) // 0-255

            val views = RemoteViews(context.packageName, R.layout.todo_widget)
            
            // Set dynamic background with transparency
            val color = android.graphics.Color.argb(transparency, 30, 30, 30) // Dark background with alpha
            
            // Wait, we can't easily change drawable transparency in RemoteViews directly without setInt.
            // Using setInt to set background color or alpha on background drawable.
            views.setInt(R.id.widget_background, "setBackgroundColor", color)

            views.setTextViewText(R.id.widget_filter_text, "$currentFilter ▼")

            // Intent to open WidgetSettingsActivity
            val settingsIntent = Intent(context, WidgetSettingsActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val settingsPendingIntent = PendingIntent.getActivity(
                context, appWidgetId, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_settings_btn, settingsPendingIntent)

            // Intent to add new task (opens main activity or add activity)
            val addIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "add_todo")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val addPendingIntent = PendingIntent.getActivity(
                context, appWidgetId + 1, addIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_add_btn, addPendingIntent)
            
            // Intent to change filter
            val filterIntent = Intent(context, WidgetFilterActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val filterPendingIntent = PendingIntent.getActivity(
                context, appWidgetId + 2, filterIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_filter_text, filterPendingIntent)
            
            // Intent to sync
            val syncIntent = Intent(context, TodoWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            val syncPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId + 3, syncIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_sync_btn, syncPendingIntent)

            // Setup list view
            val intent = Intent(context, TodoWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list_view, intent)
            views.setEmptyView(R.id.widget_list_view, android.R.id.empty)
            
            // Set pending intent template for list items
            val clickIntent = Intent(context, TodoWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_TASK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val clickPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId + 4, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
