package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY timeOfDay DESC, id ASC")
    suspend fun getAllTodosSync(): List<Todo>

    @Query("SELECT * FROM todos WHERE isArchived = 0 ORDER BY timeOfDay DESC, id ASC")
    fun getAllTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE isArchived = 0 AND scheduledDate = :date ORDER BY timeOfDay DESC, id ASC")
    fun getTodosByDate(date: String): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE isArchived = 1 ORDER BY endDate DESC, id ASC")
    fun getArchivedTodos(): Flow<List<Todo>>

    @Query("UPDATE todos SET isArchived = 1 WHERE endDate > 0 AND endDate < :currentTime AND isArchived = 0")
    suspend fun archivePastDueTodos(currentTime: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)
}
