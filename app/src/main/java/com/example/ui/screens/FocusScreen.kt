package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MusicVideo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    
    var morningGuardEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("morning_guard_enabled", true)) }
    var eveningGuardEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("evening_guard_enabled", true)) }
    
    var morningStartTime by remember { mutableStateOf(sharedPrefs.getString("morning_start", "06:00 AM") ?: "06:00 AM") }
    var morningEndTime by remember { mutableStateOf(sharedPrefs.getString("morning_end", "09:00 AM") ?: "09:00 AM") }
    var eveningStartTime by remember { mutableStateOf(sharedPrefs.getString("evening_start", "10:00 PM") ?: "10:00 PM") }
    var eveningEndTime by remember { mutableStateOf(sharedPrefs.getString("evening_end", "07:00 AM") ?: "07:00 AM") }
    
    val blockedApps = remember { mutableStateListOf<Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>>() }
    
    val packageManager = context.packageManager
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val savedBlocked = sharedPrefs.getStringSet("blocked_packages", setOf()) ?: setOf()
        savedBlocked.forEach { packageName ->
            val appName = try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }
            blockedApps.add(Triple(appName, packageName, Icons.Filled.Add))
        }
    }

    var showAddAppDialog by remember { mutableStateOf(false) }
    var installedApps by remember { mutableStateOf<List<android.content.pm.ResolveInfo>>(emptyList()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        installedApps = packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_ALL)
    }

    if (showAddAppDialog) {
        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            title = { Text("Select App to Block") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    items(installedApps.size) { index ->
                        val appInfo = installedApps[index]
                        val appName = appInfo.loadLabel(packageManager).toString()
                        val packageName = appInfo.activityInfo.packageName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    blockedApps.add(Triple(appName, packageName, Icons.Filled.Add))
                                    val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                    val currentBlocked = sharedPrefs.getStringSet("blocked_packages", mutableSetOf()) ?: mutableSetOf()
                                    val newBlocked = currentBlocked.toMutableSet()
                                    newBlocked.add(packageName)
                                    sharedPrefs.edit().putStringSet("blocked_packages", newBlocked).apply()
                                    showAddAppDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(appName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddAppDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        item {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Morning Guard", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text("Block early morning doomscrolling.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = morningGuardEnabled,
                            onCheckedChange = { 
                                morningGuardEnabled = it
                                sharedPrefs.edit().putBoolean("morning_guard_enabled", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                    if (morningGuardEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(onClick = { 
                                val cal = java.util.Calendar.getInstance()
                                android.app.TimePickerDialog(context, { _, hour, min ->
                                    val formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", if (hour % 12 == 0) 12 else hour % 12, min, if (hour < 12) "AM" else "PM")
                                    morningStartTime = formattedTime
                                    sharedPrefs.edit().putString("morning_start", formattedTime).apply()
                                }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false).show()
                            }) { Text(morningStartTime) }
                            Text("to", modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(onClick = { 
                                val cal = java.util.Calendar.getInstance()
                                android.app.TimePickerDialog(context, { _, hour, min ->
                                    val formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", if (hour % 12 == 0) 12 else hour % 12, min, if (hour < 12) "AM" else "PM")
                                    morningEndTime = formattedTime
                                    sharedPrefs.edit().putString("morning_end", formattedTime).apply()
                                }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false).show()
                            }) { Text(morningEndTime) }
                        }
                    }
                }
            }
        }
        
        item {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Evening Guard", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                            Text("Wind down without distractions.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = eveningGuardEnabled,
                            onCheckedChange = { 
                                eveningGuardEnabled = it
                                sharedPrefs.edit().putBoolean("evening_guard_enabled", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onSecondary, checkedTrackColor = MaterialTheme.colorScheme.secondary)
                        )
                    }
                    if (eveningGuardEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(onClick = { 
                                val cal = java.util.Calendar.getInstance()
                                android.app.TimePickerDialog(context, { _, hour, min ->
                                    val formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", if (hour % 12 == 0) 12 else hour % 12, min, if (hour < 12) "AM" else "PM")
                                    eveningStartTime = formattedTime
                                    sharedPrefs.edit().putString("evening_start", formattedTime).apply()
                                }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false).show()
                            }) { Text(eveningStartTime) }
                            Text("to", modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(onClick = { 
                                val cal = java.util.Calendar.getInstance()
                                android.app.TimePickerDialog(context, { _, hour, min ->
                                    val formattedTime = String.format(java.util.Locale.getDefault(), "%02d:%02d %s", if (hour % 12 == 0) 12 else hour % 12, min, if (hour < 12) "AM" else "PM")
                                    eveningEndTime = formattedTime
                                    sharedPrefs.edit().putString("evening_end", formattedTime).apply()
                                }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), false).show()
                            }) { Text(eveningEndTime) }
                        }
                    }
                }
            }
        }
        
        item {
            Column {
                Text(
                    "APPS TO BLOCK",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                )
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        blockedApps.forEachIndexed { index, app ->
                            AppBlockItem(
                                name = app.first, 
                                icon = app.third, 
                                initialChecked = true, 
                                enabled = true,
                                onDelete = { 
                                    val pkg = blockedApps[index].second
                                    blockedApps.removeAt(index)
                                    val currentBlocked = sharedPrefs.getStringSet("blocked_packages", mutableSetOf()) ?: mutableSetOf()
                                    val newBlocked = currentBlocked.toMutableSet()
                                    newBlocked.remove(pkg)
                                    sharedPrefs.edit().putStringSet("blocked_packages", newBlocked).apply()
                                }
                            )
                            if (index < blockedApps.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { 
                        if (blockedApps.size < 20) {
                            showAddAppDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Filled.Add, "Add App")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add App")
                }
            }
        }
        
        item {
            Column {
                Text(
                    "ALWAYS ALLOWED",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) // Should be dashed in real life but plain border is okay
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(50), shadowElevation = 1.dp) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Call, "Calls", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Calls", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(50), shadowElevation = 1.dp) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Sms, "SMS", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SMS", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                        Icon(Icons.Outlined.Info, "Info", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun AppBlockItem(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, initialChecked: Boolean, enabled: Boolean = true, onDelete: () -> Unit = {}) {
    var checked by remember { mutableStateOf(initialChecked) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = name, tint = MaterialTheme.colorScheme.outline)
            }
            Text(name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = checked,
                onCheckedChange = { if (enabled) checked = it },
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                )
            )
            IconButton(onClick = onDelete, enabled = enabled) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
