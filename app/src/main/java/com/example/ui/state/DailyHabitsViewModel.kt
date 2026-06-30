package com.example.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyHabit
import com.example.data.HabitLog
import com.example.data.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first

class DailyHabitsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository
    
    private val _currentDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()
    
    fun setDate(date: String) {
        _currentDate.value = date
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao())
        
        viewModelScope.launch {
            repository.allHabits.first().let { habitsList ->
                if (habitsList.isEmpty()) {
                    addHabit("Drink Water", 0xFF03A9F4, "Morning", "1,2,3,4,5,6,7")
                    addHabit("Read 10 pages", 0xFFFFC107, "Night", "1,2,3,4,5,6,7")
                    addHabit("Morning Workout", 0xFF00BFA5, "Morning", "2,4,6")
                }
            }
        }
    }

    val allHabits: StateFlow<List<DailyHabit>> = repository.allHabits.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val dailyLogs: StateFlow<List<HabitLog>> = _currentDate.flatMapLatest { date -> repository.getLogsForDate(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    fun addHabit(name: String, color: Long, timeOfDay: String, daysOfWeek: String) {
        viewModelScope.launch {
            val habits = allHabits.value
            val order = habits.size
            repository.insertHabit(DailyHabit(
                name = name,
                color = color,
                timeOfDay = timeOfDay,
                daysOfWeek = daysOfWeek,
                displayOrder = order
            ))
        }
    }

    fun updateHabit(habit: DailyHabit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(id: Int) {
        viewModelScope.launch {
            repository.deleteHabit(id)
        }
    }

    fun toggleHabitCompletion(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, _currentDate.value, isCompleted)
        }
    }
    
    fun updateHabitOrder(habits: List<DailyHabit>) {
        viewModelScope.launch {
            habits.forEachIndexed { index, habit ->
                repository.updateHabitOrder(habit.id, index)
            }
        }
    }
}
