package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Todo
import com.example.ui.state.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material.icons.filled.Archive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    onNavigateBack: () -> Unit,
    viewModel: TodoViewModel = viewModel()
) {
    val todos by viewModel.todosForDate.collectAsStateWithLifecycle()
    val allTodos by viewModel.allTodos.collectAsStateWithLifecycle()
    val archivedTodos by viewModel.archivedTodos.collectAsStateWithLifecycle()
    val currentDateString by viewModel.currentDate.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showArchivedDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        if (activity?.intent?.getStringExtra("navigate_to") == "add_todo") {
            showAddDialog = true
            activity.intent.removeExtra("navigate_to")
        }
    }

    val format = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val parsedDate = try { format.parse(currentDateString) } catch(e: Exception) { Date() } ?: Date()

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF333333),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Todo")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "today",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val completedCount = todos.count { it.completed }
                    val totalMinutes = todos.sumOf { it.estimatedMinutes }
                    val completedMinutes = todos.filter { it.completed }.sumOf { it.estimatedMinutes }
                    
                    IconButton(onClick = { showArchivedDialog = true }) {
                        Icon(Icons.Default.Archive, contentDescription = "Archived", tint = Color.Gray)
                    }

                    Surface(
                        color = Color(0xFF2C2C2E),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text("$completedCount", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Surface(
                        color = Color(0xFF2C2C2E),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("${completedMinutes / 60.0} of ${totalMinutes / 60.0} hrs", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                placeholder = { Text("Search by title or tag...", color = Color.Gray) },
                leadingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF1C1C1E),
                    unfocusedContainerColor = Color(0xFF1C1C1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Date Selector
            TodoDateSelector(
                currentDateString = currentDateString,
                onDateSelected = { viewModel.setDate(it) }
            )

            // Todo List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                val filteredTodos = if (searchQuery.isBlank()) {
                    todos
                } else {
                    todos.filter { 
                        it.title.contains(searchQuery, ignoreCase = true) || 
                        it.priorityTag.contains(searchQuery, ignoreCase = true) 
                    }
                }

                val morningTodos = filteredTodos.filter { it.timeOfDay == "Morning" }
                val afternoonTodos = filteredTodos.filter { it.timeOfDay == "Afternoon" }
                val eveningTodos = filteredTodos.filter { it.timeOfDay == "Evening" }

                item {
                    TodoTrendChart(allTodos = allTodos)
                }

                if (filteredTodos.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxSize().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                            Text(if (searchQuery.isBlank()) "No tasks for this day." else "No tasks match your search.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (morningTodos.isNotEmpty()) {
                    item {
                        TodoSectionHeader("Morning", Icons.Default.LightMode)
                    }
                    items(morningTodos) { todo ->
                        TodoItemCard(todo, viewModel)
                    }
                }

                if (afternoonTodos.isNotEmpty()) {
                    item {
                        TodoSectionHeader("Afternoon", Icons.Default.WbSunny)
                    }
                    items(afternoonTodos) { todo ->
                        TodoItemCard(todo, viewModel)
                    }
                }

                if (eveningTodos.isNotEmpty()) {
                    item {
                        TodoSectionHeader("Evening", Icons.Default.NightsStay)
                    }
                    items(eveningTodos) { todo ->
                        TodoItemCard(todo, viewModel)
                    }
                }
            }
        }
    }

    if (showArchivedDialog) {
        ArchivedTodosDialog(
            archivedTodos = archivedTodos,
            onDismiss = { showArchivedDialog = false }
        )
    }

    if (showAddDialog) {
        TodoAddEditDialog(
            currentDateString = currentDateString,
            onDismiss = { showAddDialog = false },
            onSave = { newTodo -> 
                viewModel.addTodo(newTodo)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TodoSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Text(title, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TodoItemCard(todo: Todo, viewModel: TodoViewModel) {
    var showEditDialog by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF1C1C1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showEditDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, if (todo.completed) Color.Gray else Color(todo.customColor), CircleShape)
                        .clickable { viewModel.toggleTodoCompletion(todo) }
                        .background(if (todo.completed) Color.Gray.copy(alpha = 0.5f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    if (todo.completed) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                
                Column {
                    Text(
                        text = if (todo.priorityTag.isNotEmpty()) "@${todo.priorityTag}: ${todo.title}" else todo.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (todo.completed) Color.Gray else Color.White,
                        textDecoration = if (todo.completed) TextDecoration.LineThrough else null
                    )
                    if (todo.description.isNotEmpty()) {
                        Text(
                            text = todo.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (todo.estimatedMinutes > 0) {
                Surface(
                    color = Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${todo.estimatedMinutes} min",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        TodoAddEditDialog(
            todoToEdit = todo,
            currentDateString = todo.scheduledDate,
            onDismiss = { showEditDialog = false },
            onSave = { updatedTodo ->
                viewModel.updateTodo(updatedTodo)
                showEditDialog = false
            },
            onDelete = {
                viewModel.deleteTodo(todo)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun TodoDateSelector(
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
    val dayOfWeekFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    
    val listState = rememberLazyListState()
    
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
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        items(dates) { date ->
            val dateString = format.format(date)
            val isSelected = dateString == currentDateString
            
            Column(
                modifier = Modifier
                    .clickable { onDateSelected(dateString) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = dayOfWeekFormat.format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.White else Color.Gray
                )
                Text(
                    text = dayFormat.format(date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color.Gray
                )
            }
        }
    }
}

@Composable
fun TodoTrendChart(allTodos: List<Todo>) {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -29)
    val startDate = cal.time
    
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateMap = mutableMapOf<String, Float>()
    
    val iterCal = Calendar.getInstance()
    iterCal.time = startDate
    for (i in 0..29) {
        dateMap[format.format(iterCal.time)] = 0f
        iterCal.add(Calendar.DAY_OF_YEAR, 1)
    }
    
    allTodos.forEach { todo ->
        if (todo.completed && dateMap.containsKey(todo.scheduledDate)) {
            dateMap[todo.scheduledDate] = (dateMap[todo.scheduledDate] ?: 0f) + 1f
        }
    }
    
    val values = dateMap.values.toList()
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    
    Surface(
        color = Color(0xFF1C1C1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "30-Day Completion Trend",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                values.forEach { value ->
                    val heightPercent = value / maxValue
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(heightPercent.coerceAtLeast(0.05f))
                            .padding(horizontal = 1.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (value > 0) Color(0xFF4CAF50) else Color(0xFF2C2C2E)
                            )
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("30 Days Ago", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("Today", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedTodosDialog(
    archivedTodos: List<Todo>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Archived Tasks",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        },
        text = {
            if (archivedTodos.isEmpty()) {
                Text(
                    "No archived tasks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(archivedTodos) { todo ->
                        Surface(
                            color = Color(0xFF2C2C2E),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = todo.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (todo.completed) Color.Gray else Color.White,
                                    textDecoration = if (todo.completed) TextDecoration.LineThrough else null
                                )
                                Text(
                                    text = "Scheduled: ${todo.scheduledDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF4CAF50))
            }
        },
        containerColor = Color(0xFF1C1C1E),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
