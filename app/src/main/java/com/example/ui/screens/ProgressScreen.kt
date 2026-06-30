package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.liquidGlass
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen() {
    var selectedPeriod by remember { mutableStateOf("Week") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = datePickerState,
                modifier = Modifier.weight(1f)
            )
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Progress", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Date range", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                PeriodSelector(selectedPeriod = selectedPeriod, onPeriodSelected = { selectedPeriod = it })
            }
            item {
                AtomicScoreGraph(selectedPeriod = selectedPeriod)
            }
            item {
                StreakCardsRow()
            }
            item {
                MonthlyHeatmap()
            }
            item {
                HabitBreakdown()
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun PeriodSelector(selectedPeriod: String, onPeriodSelected: (String) -> Unit) {
    val options = listOf("Week", "Month", "All time")
    
    Surface(
        modifier = Modifier.liquidGlass(RoundedCornerShape(50)),
        color = Color.Transparent,
        shape = RoundedCornerShape(50)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
            options.forEach { option ->
                val isSelected = selectedPeriod == option
                Surface(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).clickable { onPeriodSelected(option) }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            option,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AtomicScoreGraph(selectedPeriod: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Atomic Score", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            
            val average = when (selectedPeriod) {
                "Week" -> "7-day average: 74"
                "Month" -> "30-day average: 68"
                else -> "All-time average: 71"
            }
            Text(average, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Canvas for line chart
            Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                // Line chart implementation
                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp, top = 8.dp, start = 8.dp, end = 8.dp)) {
                    val points = when (selectedPeriod) {
                        "Week" -> listOf(0.5f, 0.7f, 0.6f, 0.9f, 0.8f, 1.0f, 0.74f)
                        "Month" -> listOf(0.4f, 0.5f, 0.7f, 0.65f, 0.8f, 0.7f, 0.85f, 0.9f, 0.88f, 0.75f, 0.82f, 0.78f)
                        else -> listOf(0.3f, 0.5f, 0.4f, 0.6f, 0.8f, 0.7f, 0.9f)
                    }
                    val width = size.width
                    val height = size.height
                    
                    val path = androidx.compose.ui.graphics.Path()
                    points.forEachIndexed { index, value ->
                        val x = index * (width / (points.size - 1).coerceAtLeast(1))
                        val y = height - (value * height)
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    val fillPath = androidx.compose.ui.graphics.Path()
                    fillPath.addPath(path)
                    fillPath.lineTo(width, height)
                    fillPath.lineTo(0f, height)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.5f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                    
                    points.forEachIndexed { index, value ->
                        val x = index * (width / (points.size - 1).coerceAtLeast(1))
                        val y = height - (value * height)
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
                
                // Day labels
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labels = when (selectedPeriod) {
                        "Week" -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        "Month" -> listOf("W1", "W2", "W3", "W4")
                        else -> listOf("2024", "2025", "2026")
                    }
                    labels.forEach {
                        Text(it, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun StreakCardsRow() {
    val habitState = com.example.ui.state.LocalHabitState.current
    Column {
        Text("Your streaks", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StreakCardItem(modifier = Modifier.weight(1f), title = "Atomic Score", streak = "${habitState.streakDays.intValue}")
            StreakCardItem(modifier = Modifier.weight(1f), title = "Hydration", streak = "5")
            StreakCardItem(modifier = Modifier.weight(1f), title = "Morning focus", streak = "21")
        }
    }
}

@Composable
fun StreakCardItem(modifier: Modifier = Modifier, title: String, streak: String) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
            Text(streak, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
            Text(title, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyHeatmap() {
    var displayMonthOffset by remember { mutableStateOf(0) }
    var selectedDayInfo by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) } // Day, Month, Year
    var showDayDetails by remember { mutableStateOf(false) }
    
    val baseMonth = 6 // June
    val baseYear = 2026
    
    val currentMonth = (baseMonth - 1 + displayMonthOffset).mod(12) + 1
    val currentYear = baseYear + (baseMonth - 1 + displayMonthOffset) / 12 - if ((baseMonth - 1 + displayMonthOffset) < 0 && (baseMonth - 1 + displayMonthOffset) % 12 != 0) 1 else 0
    
    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { displayMonthOffset-- }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous")
                }
                Text("${monthNames[currentMonth - 1]} $currentYear", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { displayMonthOffset++ }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                    Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.Center) {
                        Text(it, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            val daysInMonth = when (currentMonth) {
                2 -> if (currentYear % 4 == 0) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
            // Mock starting day of week for simplicity
            val startOffset = (currentMonth + currentYear) % 7
            
            for (row in 0..5) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        if (cellIndex < startOffset || cellIndex >= startOffset + daysInMonth) {
                            Box(modifier = Modifier.size(32.dp))
                        } else {
                            val day = cellIndex - startOffset + 1
                            val heat = (day + currentMonth) % 5 // mock heat based on day and month
                            val premiumColor = Color(0xFF00BFA5) // Teal accent
                            val color = when (heat) {
                                4 -> premiumColor
                                3 -> premiumColor.copy(alpha = 0.75f)
                                2 -> premiumColor.copy(alpha = 0.5f)
                                1 -> premiumColor.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                            val isToday = day == 26 && displayMonthOffset == 0
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, RoundedCornerShape(8.dp))
                                    .border(
                                        if (isToday) 2.dp else 0.dp,
                                        if (isToday) Color.White else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedDayInfo = Triple(day, currentMonth, currentYear)
                                        showDayDetails = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = if (heat > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDayDetails && selectedDayInfo != null) {
        val (day, month, year) = selectedDayInfo!!
        ModalBottomSheet(
            onDismissRequest = { showDayDetails = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${monthNames[month - 1]} $day, $year",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                val mockWaterScore = (day * month) % 8 + 1
                val mockMovementBreaks = (day + month) % 6
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Water Logged", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                    Text("$mockWaterScore cups", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Outlined.DirectionsRun, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Movement Breaks", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                    Text("$mockMovementBreaks breaks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Focus Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                    Text(if (day % 2 == 0) "Active" else "Inactive", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LiquidGlassButton(
                    onClick = { showDayDetails = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HabitBreakdown() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This week breakdown", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            
            HabitBreakdownRow(icon = Icons.Outlined.WaterDrop, name = "Hydration", percent = "85%")
            Spacer(modifier = Modifier.height(12.dp))
            HabitBreakdownRow(icon = Icons.AutoMirrored.Outlined.DirectionsRun, name = "Movement", percent = "60%")
            Spacer(modifier = Modifier.height(12.dp))
            HabitBreakdownRow(icon = Icons.Outlined.Shield, name = "Morning focus", percent = "100%")
        }
    }
}

@Composable
fun HabitBreakdownRow(icon: androidx.compose.ui.graphics.vector.ImageVector, name: String, percent: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
            }
            Text(percent, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val fraction = percent.removeSuffix("%").toFloat() / 100f
        com.example.ui.components.HabitProgressBar(progress = fraction, height = 6.dp)
    }
}
