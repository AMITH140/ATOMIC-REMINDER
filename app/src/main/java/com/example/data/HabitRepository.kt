package com.example.data

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<DailyHabit>> = habitDao.getAllHabits()

    suspend fun insertHabit(habit: DailyHabit) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: DailyHabit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(id: Int) {
        habitDao.deleteLogsForHabit(id)
        habitDao.deleteHabit(id)
    }
    
    suspend fun updateHabitOrder(id: Int, order: Int) {
        habitDao.updateHabitOrder(id, order)
    }

    fun getLogsForDate(date: String): Flow<List<HabitLog>> {
        return habitDao.getLogsForDate(date)
    }

    suspend fun toggleHabitCompletion(habitId: Int, date: String, isCompleted: Boolean) {
        if (isCompleted) {
            habitDao.insertLog(HabitLog(habitId = habitId, date = date, completed = true))
        } else {
            habitDao.deleteLog(habitId, date)
        }
    }
}
