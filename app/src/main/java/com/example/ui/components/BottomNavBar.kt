package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import androidx.compose.ui.graphics.Brush

import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.GridView, Icons.Outlined.GridView)
    object Focus : Screen("focus", "Morning Guard", Icons.Filled.Shield, Icons.Outlined.Shield)
    object Progress : Screen("progress", "Progress", Icons.Filled.Analytics, Icons.Outlined.Analytics)
    object Premium : Screen("premium", "Premium", Icons.Filled.WorkspacePremium, Icons.Outlined.WorkspacePremium)
    object Todo : Screen("todo", "TO DO", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List)
}

val bottomNavItems = listOf(Screen.Dashboard, Screen.Progress, Screen.Todo, Screen.Premium)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val habitState = com.example.ui.state.LocalHabitState.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .clip(RoundedCornerShape(32.dp))
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route
                val isPremiumEmpty = screen.route == Screen.Premium.route && habitState.premiumDaysRemaining.intValue <= 0
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(screen.route) },
                    icon = {
                        androidx.compose.material3.BadgedBox(
                            badge = {
                                if (screen.route == Screen.Premium.route) {
                                    androidx.compose.material3.Badge(
                                        containerColor = if (isPremiumEmpty) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        contentColor = if (isPremiumEmpty) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text("${habitState.premiumDaysRemaining.intValue}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title,
                                tint = if (isPremiumEmpty && !selected) MaterialTheme.colorScheme.error.copy(alpha = 0.8f) else androidx.compose.material3.LocalContentColor.current
                            )
                        }
                    },
                    label = { 
                        Text(
                            text = screen.title, 
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isPremiumEmpty) MaterialTheme.colorScheme.error else Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        selectedTextColor = if (isPremiumEmpty) MaterialTheme.colorScheme.error else Color.White,
                        unselectedTextColor = Color.White.copy(alpha = 0.7f),
                        indicatorColor = if (isPremiumEmpty) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}
