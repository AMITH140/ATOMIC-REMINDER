package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.Todo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoAddEditDialog(
    todoToEdit: Todo? = null,
    currentDateString: String,
    onDismiss: () -> Unit,
    onSave: (Todo) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(todoToEdit?.title ?: "") }
    var description by remember { mutableStateOf(todoToEdit?.description ?: "") }
    var timeOfDay by remember { mutableStateOf(todoToEdit?.timeOfDay ?: "Morning") }
    var estimatedMinutes by remember { mutableStateOf(todoToEdit?.estimatedMinutes?.toString() ?: "") }
    var priorityTag by remember { mutableStateOf(todoToEdit?.priorityTag ?: "") }
    var customColor by remember { mutableStateOf(todoToEdit?.customColor ?: 0xFF4CAF50) }
    var deadlineTimeStr by remember { 
        mutableStateOf(
            if (todoToEdit != null && todoToEdit.deadline > 0) {
                java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(todoToEdit.deadline))
            } else {
                "12:00"
            }
        ) 
    }
    
    val colors = listOf(
        0xFF4CAF50, // Green
        0xFF2196F3, // Blue
        0xFFFFC107, // Amber
        0xFFF44336, // Red
        0xFF9C27B0, // Purple
        0xFF00BCD4, // Cyan
        0xFFFF9800  // Orange
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (todoToEdit == null) "New Task" else "Edit Task") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priorityTag,
                    onValueChange = { priorityTag = it },
                    label = { Text("Tag (e.g. work, personal)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = estimatedMinutes,
                        onValueChange = { estimatedMinutes = it },
                        label = { Text("Duration (mins)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = deadlineTimeStr,
                            onValueChange = {},
                            label = { Text("Deadline") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    val timeParts = deadlineTimeStr.split(":")
                                    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 12
                                    val min = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                                    android.app.TimePickerDialog(
                                        context,
                                        { _, selectedHour, selectedMin ->
                                            deadlineTimeStr = String.format(java.util.Locale.US, "%02d:%02d", selectedHour, selectedMin)
                                        },
                                        hour,
                                        min,
                                        true
                                    ).show()
                                }
                        )
                    }
                }
                
                Text("Time of Day", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Morning", "Afternoon", "Evening").forEach { time ->
                        FilterChip(
                            selected = timeOfDay == time,
                            onClick = { timeOfDay = time },
                            label = { Text(time) }
                        )
                    }
                }
                
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { colorVal ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { customColor = colorVal }
                                .border(
                                    2.dp,
                                    if (customColor == colorVal) Color.White else Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        val deadlineLong = try { format.parse("$currentDateString $deadlineTimeStr")?.time ?: 0L } catch(e: Exception) { 0L }
                        
                        onSave(
                            Todo(
                                id = todoToEdit?.id ?: 0,
                                title = title,
                                description = description,
                                timeOfDay = timeOfDay,
                                deadline = deadlineLong,
                                estimatedMinutes = estimatedMinutes.toIntOrNull() ?: 0,
                                priorityTag = priorityTag,
                                customColor = customColor,
                                completed = todoToEdit?.completed ?: false,
                                scheduledDate = currentDateString
                            )
                        )
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (todoToEdit != null && onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Delete", color = Color.Red) }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
