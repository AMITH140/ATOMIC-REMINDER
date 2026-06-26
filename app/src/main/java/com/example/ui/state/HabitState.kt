package com.example.ui.state

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class HabitState {
    var currentWaterMl = mutableIntStateOf(750)
    var waterCups = mutableIntStateOf(3)
    var totalGoalLiters = mutableFloatStateOf(2.5f)
    var cupSizeMl = mutableIntStateOf(250)
    
    var lastBreakMins = mutableIntStateOf(45)
    var sedentaryMinutes = mutableIntStateOf(45)
    var movementEnabled = mutableStateOf(true)
    var guardActive = mutableStateOf(true)

    // Streak Feature
    var streakDays = mutableIntStateOf(12)

    // Notification Intervals
    var waterIntervalMins = mutableIntStateOf(60)
    var movementIntervalMins = mutableIntStateOf(45)
    // Premium Feature
    var premiumDaysRemaining = mutableIntStateOf(14)
}

val LocalHabitState = compositionLocalOf<HabitState> {
    error("HabitState not provided")
}
