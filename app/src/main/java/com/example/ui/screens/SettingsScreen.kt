package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val habitState = com.example.ui.state.LocalHabitState.current
    val userProfile = com.example.ui.state.LocalUserProfile.current
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var waterInterval by remember { mutableStateOf(habitState.waterIntervalMins.intValue.toString()) }

    var showMovementGoalDialog by remember { mutableStateOf(false) }
    var movementBreakInterval by remember { mutableStateOf(habitState.movementIntervalMins.intValue.toString()) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userProfile.value.name) }
    var editEmail by remember { mutableStateOf(userProfile.value.email) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasNotificationPermission = granted
        }
    )

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { 
                    userProfile.value = userProfile.value.copy(name = editName, email = editEmail)
                    showEditProfileDialog = false 
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showWaterGoalDialog) {
        AlertDialog(
            onDismissRequest = { showWaterGoalDialog = false },
            title = { Text("Water Reminder Interval") },
            text = {
                OutlinedTextField(
                    value = waterInterval,
                    onValueChange = { waterInterval = it },
                    label = { Text("Interval (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { 
                    waterInterval.toIntOrNull()?.let { 
                        habitState.waterIntervalMins.intValue = it 
                        com.example.util.NotificationHelper.scheduleReminder(
                            context,
                            id = 1,
                            title = "Water Reminder",
                            message = "Time to drink a glass of water!",
                            intervalMins = it
                        )
                    }
                    showWaterGoalDialog = false 
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWaterGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMovementGoalDialog) {
        AlertDialog(
            onDismissRequest = { showMovementGoalDialog = false },
            title = { Text("Movement Goal") },
            text = {
                OutlinedTextField(
                    value = movementBreakInterval,
                    onValueChange = { movementBreakInterval = it },
                    label = { Text("Break interval (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { 
                    movementBreakInterval.toIntOrNull()?.let { 
                        habitState.movementIntervalMins.intValue = it 
                        com.example.util.NotificationHelper.scheduleReminder(
                            context,
                            id = 2,
                            title = "Movement Reminder",
                            message = "Time to get up and stretch!",
                            intervalMins = it
                        )
                    }
                    showMovementGoalDialog = false 
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMovementGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Account Section
        item {
            Column {
                Text(
                    "ACCOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEditProfileDialog = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = userProfile.value.name.take(2).uppercase()
                                Text(initials, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Column {
                                Text(userProfile.value.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(userProfile.value.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        if (!hasNotificationPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notifications Disabled", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Tap to enable reminders for habits.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                        }
                        Icon(androidx.compose.material.icons.Icons.Outlined.NotificationsActive, contentDescription = "Enable", tint = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }

        // Premium Banner
        item {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Column {
                            Text("Atomic Premium", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Text("3 days remaining", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Earn more", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Reminders
        item {
            SettingsSection(title = "REMINDERS") {
                SettingsItem(
                    icon = Icons.Outlined.WaterDrop, 
                    title = "Water reminders", 
                    subtitle = "Every ${habitState.waterIntervalMins.intValue} mins",
                    iconTint = MaterialTheme.colorScheme.secondary, 
                    iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    onClick = { showWaterGoalDialog = true }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.AutoMirrored.Outlined.DirectionsRun, 
                    title = "Movement reminders", 
                    subtitle = "Every ${habitState.movementIntervalMins.intValue} mins",
                    iconTint = MaterialTheme.colorScheme.primary, 
                    iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    onClick = { showMovementGoalDialog = true }
                )
                SettingsDivider()
                SettingsItem(icon = Icons.Outlined.Alarm, title = "Morning alarm", iconTint = MaterialTheme.colorScheme.secondaryContainer, iconBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
            }
        }

        // Data
        item {
            SettingsSection(title = "DATA") {
                SettingsItem(icon = Icons.Outlined.Download, title = "Export data", showChevron = false)
                SettingsDivider()
                SettingsItem(icon = Icons.Outlined.Delete, title = "Reset all habits", showChevron = false, titleColor = MaterialTheme.colorScheme.error, iconTint = MaterialTheme.colorScheme.error, iconBg = MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
            }
        }

        // About
        item {
            SettingsSection(title = "ABOUT") {
                SettingsItemTextValue(title = "Version", value = "2.4.1")
                SettingsDivider()
                SettingsItem(title = "Privacy policy", icon = Icons.AutoMirrored.Outlined.OpenInNew, isTrailingIcon = true, showChevron = false)
                SettingsDivider()
                SettingsItem(title = "Rate Atomic Reminder", icon = Icons.Outlined.StarRate, isTrailingIcon = true, showChevron = false, trailingIconTint = MaterialTheme.colorScheme.secondaryContainer)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector? = null,
    title: String,
    subtitle: String? = null,
    iconTint: androidx.compose.ui.graphics.Color? = null,
    iconBg: androidx.compose.ui.graphics.Color? = null,
    titleColor: androidx.compose.ui.graphics.Color? = null,
    showChevron: Boolean = true,
    isTrailingIcon: Boolean = false,
    trailingIconTint: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (icon != null && !isTrailingIcon) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg ?: MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint ?: MaterialTheme.colorScheme.outline)
                }
            }
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor ?: MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (showChevron) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        } else if (isTrailingIcon && icon != null) {
            Icon(icon, contentDescription = null, tint = trailingIconTint ?: MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun SettingsItemTextValue(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
}
