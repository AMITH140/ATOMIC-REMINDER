package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DailyHabit
import com.example.ui.state.DailyHabitsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.lazy.LazyRow
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHabitsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailyHabitsViewModel = viewModel()
) {
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val logs by viewModel.dailyLogs.collectAsStateWithLifecycle()
    val currentDateString by viewModel.currentDate.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Habits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            DateSelector(
                currentDateString = currentDateString,
                onDateSelected = { viewModel.setDate(it) }
            )
            
            val format = remember { java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
            val parsedDate = try { format.parse(currentDateString) } catch (e: Exception) { Date() }
            val dow = remember(parsedDate) {
                val cal = Calendar.getInstance()
                if (parsedDate != null) cal.time = parsedDate
                cal.get(Calendar.DAY_OF_WEEK)
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter habits for the selected day of week
                val filteredHabits = habits.filter { habit -> 
                    if (habit.daysOfWeek == "1,2,3,4,5,6,7") true 
                    else habit.daysOfWeek.split(",").contains(dow.toString())
                }

                val morningHabits = filteredHabits.filter { it.timeOfDay == "Morning" }
                val afternoonHabits = filteredHabits.filter { it.timeOfDay == "Afternoon" }
                val nightHabits = filteredHabits.filter { it.timeOfDay == "Night" }

                if (filteredHabits.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxSize().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                            Text("No habits scheduled for this day.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (morningHabits.isNotEmpty()) {
                    item { Text("Morning", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                    items(morningHabits) { habit ->
                        HabitItem(habit, logs.any { it.habitId == habit.id }, viewModel)
                    }
                }
                if (afternoonHabits.isNotEmpty()) {
                    item { Text("Afternoon", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                    items(afternoonHabits) { habit ->
                        HabitItem(habit, logs.any { it.habitId == habit.id }, viewModel)
                    }
                }
                if (nightHabits.isNotEmpty()) {
                    item { Text("Night", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                    items(nightHabits) { habit ->
                        HabitItem(habit, logs.any { it.habitId == habit.id }, viewModel)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, color, timeOfDay, days ->
                viewModel.addHabit(name, color, timeOfDay, days)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DateSelector(
    currentDateString: String,
    onDateSelected: (String) -> Unit
) {
    val dates = remember {
        val list = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -15)
        for (i in 0..30) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    
    val format = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("d", Locale.getDefault()) }
    val dayOfWeekFormat = remember { SimpleDateFormat("E", Locale.getDefault()) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(currentDateString) {
        val index = dates.indexOfFirst { format.format(it) == currentDateString }
        if (index != -1) {
            listState.animateScrollToItem(maxOf(0, index - 3))
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(dates) { date ->
            val dateString = format.format(date)
            val isSelected = dateString == currentDateString
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .clickable { onDateSelected(dateString) }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayOfWeekFormat.format(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dayFormat.format(date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: DailyHabit,
    isCompleted: Boolean,
    viewModel: DailyHabitsViewModel
) {
    val habitColor = Color(habit.color)
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                viewModel.toggleHabitCompletion(habit.id, !isCompleted)
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) habitColor else Color.Transparent)
                    .border(2.dp, habitColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = habit.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = getDaysText(habit.daysOfWeek),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { viewModel.deleteHabit(habit.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showEditDialog) {
        EditHabitDialog(
            habit = habit,
            onDismiss = { showEditDialog = false },
            onUpdate = { updatedHabit ->
                viewModel.updateHabit(updatedHabit)
                showEditDialog = false
            }
        )
    }
}

fun getDaysText(days: String): String {
    if (days == "1,2,3,4,5,6,7") return "Every day"
    val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")
    val selected = days.split(",").mapNotNull { it.toIntOrNull() }.filter { it in 1..7 }
    return selected.joinToString(", ") { dayNames[it - 1] }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Long, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var timeOfDay by remember { mutableStateOf("Morning") }
    var selectedColor by remember { mutableStateOf(0xFF00BFA5) }
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) }

    val colors = listOf(0xFF00BFA5, 0xFFFF4081, 0xFF7C4DFF, 0xFFFFC107, 0xFF03A9F4)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Habit") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { colorVal ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal }
                                .border(
                                    2.dp,
                                    if (selectedColor == colorVal) Color.White else Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }

                Text("Time of Day", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Morning", "Afternoon", "Night").forEach { time ->
                        FilterChip(
                            selected = timeOfDay == time,
                            onClick = { timeOfDay = time },
                            label = { Text(time) }
                        )
                    }
                }

                Text("Days of Week", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    days.forEachIndexed { index, day ->
                        val dayInt = index + 1
                        FilterChip(
                            selected = selectedDays.contains(dayInt),
                            onClick = {
                                selectedDays = if (selectedDays.contains(dayInt)) {
                                    selectedDays - dayInt
                                } else {
                                    selectedDays + dayInt
                                }
                            },
                            label = { Text(day) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (name.isNotBlank() && selectedDays.isNotEmpty()) {
                        onAdd(name, selectedColor, timeOfDay, selectedDays.joinToString(","))
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitDialog(
    habit: DailyHabit,
    onDismiss: () -> Unit,
    onUpdate: (DailyHabit) -> Unit
) {
    var name by remember { mutableStateOf(habit.name) }
    var timeOfDay by remember { mutableStateOf(habit.timeOfDay) }
    var selectedColor by remember { mutableStateOf(habit.color) }
    
    val initialDays = if (habit.daysOfWeek == "1,2,3,4,5,6,7") setOf(1,2,3,4,5,6,7) 
                      else habit.daysOfWeek.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    var selectedDays by remember { mutableStateOf(initialDays) }

    val colors = listOf(0xFF00BFA5, 0xFFFF4081, 0xFF7C4DFF, 0xFFFFC107, 0xFF03A9F4)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Habit") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { colorVal ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .clickable { selectedColor = colorVal }
                                .border(
                                    2.dp,
                                    if (selectedColor == colorVal) Color.White else Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }

                Text("Time of Day", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Morning", "Afternoon", "Night").forEach { time ->
                        FilterChip(
                            selected = timeOfDay == time,
                            onClick = { timeOfDay = time },
                            label = { Text(time) }
                        )
                    }
                }

                Text("Days of Week", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    days.forEachIndexed { index, day ->
                        val dayInt = index + 1
                        FilterChip(
                            selected = selectedDays.contains(dayInt),
                            onClick = {
                                selectedDays = if (selectedDays.contains(dayInt)) {
                                    selectedDays - dayInt
                                } else {
                                    selectedDays + dayInt
                                }
                            },
                            label = { Text(day) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (name.isNotBlank() && selectedDays.isNotEmpty()) {
                        onUpdate(habit.copy(
                            name = name,
                            color = selectedColor,
                            timeOfDay = timeOfDay,
                            daysOfWeek = selectedDays.joinToString(",")
                        ))
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
