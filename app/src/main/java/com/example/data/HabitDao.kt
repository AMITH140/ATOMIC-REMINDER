package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM daily_habits ORDER BY displayOrder ASC")
    fun getAllHabits(): Flow<List<DailyHabit>>

    @Query("SELECT * FROM daily_habits ORDER BY displayOrder ASC")
    suspend fun getAllHabitsSync(): List<DailyHabit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: DailyHabit): Long

    @Update
    suspend fun updateHabit(habit: DailyHabit)

    @Query("DELETE FROM daily_habits WHERE id = :id")
    suspend fun deleteHabit(id: Int)
    
    @Query("UPDATE daily_habits SET displayOrder = :order WHERE id = :id")
    suspend fun updateHabitOrder(id: Int, order: Int)

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    suspend fun getLogsForDateSync(date: String): List<HabitLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog)
    
    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getLogForHabit(habitId: Int, date: String): HabitLog?

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteLog(habitId: Int, date: String)
    
    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Int)
}
