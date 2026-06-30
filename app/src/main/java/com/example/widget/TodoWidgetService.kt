package com.example.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.Todo
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class TodoWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TodoRemoteViewsFactory(this.applicationContext, intent)
    }
}

class TodoRemoteViewsFactory(
    private val context: Context,
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var todoItems = listOf<TodoItemModel>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        // Fetch data from database
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val filter = prefs.getString("widget_filter", "Today") ?: "Today"

        val db = AppDatabase.getDatabase(context)
        val todoDao = db.todoDao()
        val allTodos = runBlocking { todoDao.getAllTodosSync() }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val items = mutableListOf<TodoItemModel>()
        
        if (filter == "Habits") {
            // Add habits
            val totalGoalLiters = prefs.getFloat("total_goal_liters", 2.5f)
            val currentWaterMl = prefs.getInt("current_water_ml", 0)
            items.add(TodoItemModel(-1, "Drink Water", "Habits • $currentWaterMl / ${(totalGoalLiters * 1000).toInt()} ml", currentWaterMl >= totalGoalLiters * 1000))
            
            val lastBreakMins = prefs.getInt("last_break_mins", 0)
            val sedentaryMinutes = prefs.getInt("sedentary_minutes", 45)
            items.add(TodoItemModel(-2, "Move Around", "Habits • $lastBreakMins / $sedentaryMinutes mins", false))
        } else {
            val filteredTodos = when (filter) {
                "Today" -> allTodos.filter { it.scheduledDate == todayStr && !it.isArchived }
                "Pending" -> allTodos.filter { !it.completed && !it.isArchived }
                "All" -> allTodos.filter { !it.isArchived }
                else -> allTodos.filter { it.scheduledDate == todayStr && !it.isArchived }
            }

            filteredTodos.forEach { todo ->
                items.add(TodoItemModel(
                    id = todo.id,
                    title = todo.title,
                    subtitle = "Tasks • " + if (todo.scheduledDate == todayStr) "Today" else todo.scheduledDate,
                    isCompleted = todo.completed
                ))
            }
        }
        
        todoItems = items
    }

    override fun onDestroy() {}

    override fun getCount(): Int = todoItems.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= todoItems.size) return RemoteViews(context.packageName, R.layout.todo_widget_item)
        val item = todoItems[position]

        val views = RemoteViews(context.packageName, R.layout.todo_widget_item)
        views.setTextViewText(R.id.widget_item_title, item.title)
        views.setTextViewText(R.id.widget_item_subtitle, item.subtitle)

        if (item.isCompleted) {
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_check_circle)
        } else {
            views.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_circle_outline)
        }
        
        views.setImageViewResource(R.id.widget_item_star, R.drawable.ic_star_outline)

        // Setup fill-in intent for item click
        val fillInIntent = Intent()
        fillInIntent.putExtra("task_id", item.id)
        views.setOnClickFillInIntent(R.id.widget_item_checkbox, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}

data class TodoItemModel(
    val id: Int,
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean
)
