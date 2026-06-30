package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val timeOfDay: String = "Morning", // "Morning", "Afternoon", "Evening"
    val deadline: Long = 0L, // timestamp
    val endDate: Long = 0L, // end date timestamp
    val estimatedMinutes: Int = 0,
    val reminderMinutesBefore: Int = 20, // reminder, alerts before deadline
    val priorityTag: String = "Normal", // tagging each task under priority
    val customColor: Long = 0xFF4CAF50, // custom color allocation
    val completed: Boolean = false,
    val isArchived: Boolean = false,
    val scheduledDate: String = "" // e.g. "yyyy-MM-dd"
)
