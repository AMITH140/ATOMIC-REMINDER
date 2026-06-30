package com.example.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<Todo>> = todoDao.getAllTodos()
    val archivedTodos: Flow<List<Todo>> = todoDao.getArchivedTodos()

    fun getTodosByDate(date: String): Flow<List<Todo>> {
        return todoDao.getTodosByDate(date)
    }
    
    suspend fun archivePastDueTodos(currentTime: Long) {
        todoDao.archivePastDueTodos(currentTime)
    }

    suspend fun insertTodo(todo: Todo): Long {
        return todoDao.insertTodo(todo)
    }

    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }
}
