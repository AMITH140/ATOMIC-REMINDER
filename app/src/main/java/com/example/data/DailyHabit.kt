package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_habits")
data class DailyHabit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: Long,
    val timeOfDay: String,
    val daysOfWeek: String,
    val displayOrder: Int = 0
)
