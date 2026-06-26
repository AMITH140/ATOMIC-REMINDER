package com.example.ui.state

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.MutableState

data class UserProfile(
    val name: String = "Amith",
    val email: String = "amith@example.com",
    val isOnboardingComplete: Boolean = false
)

val LocalUserProfile = compositionLocalOf<MutableState<UserProfile>> {
    error("UserProfile not provided")
}
