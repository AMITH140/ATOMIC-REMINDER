package com.example.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Todo
import com.example.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.util.NotificationHelper

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository

    private val _currentDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    fun setDate(date: String) {
        _currentDate.value = date
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository(database.todoDao())
    }

    val allTodos: StateFlow<List<Todo>> = repository.allTodos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val archivedTodos: StateFlow<List<Todo>> = repository.archivedTodos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.archivePastDueTodos(System.currentTimeMillis())
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todosForDate: StateFlow<List<Todo>> = _currentDate
        .flatMapLatest { date -> repository.getTodosByDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    private fun scheduleReminder(todoId: Int, todo: Todo) {
        if (todo.deadline > 0 && !todo.completed) {
            val triggerTime = todo.deadline - (todo.reminderMinutesBefore * 60 * 1000L)
            if (triggerTime > System.currentTimeMillis()) {
                NotificationHelper.scheduleTodoReminder(
                    getApplication(),
                    todoId + 1000000, // offset to avoid clash with other notifications
                    "Task Reminder",
                    "Time to start: ${todo.title}",
                    triggerTime
                )
            }
        } else {
            NotificationHelper.cancelReminder(getApplication(), todoId + 1000000)
        }
    }

    fun addTodo(todo: Todo) = viewModelScope.launch {
        val id = repository.insertTodo(todo).toInt()
        scheduleReminder(id, todo)
    }

    fun updateTodo(todo: Todo) = viewModelScope.launch {
        repository.updateTodo(todo)
        scheduleReminder(todo.id, todo)
    }

    fun deleteTodo(todo: Todo) = viewModelScope.launch {
        repository.deleteTodo(todo)
        NotificationHelper.cancelReminder(getApplication(), todo.id + 1000000)
    }

    fun toggleTodoCompletion(todo: Todo) = viewModelScope.launch {
        val updated = todo.copy(completed = !todo.completed)
        repository.updateTodo(updated)
        scheduleReminder(updated.id, updated)
    }
}
