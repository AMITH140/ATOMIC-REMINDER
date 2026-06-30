package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.components.bounceClick

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHydration: () -> Unit = {},
    onNavigateToMovement: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    onNavigateToDailyHabits: () -> Unit = {}
) {
    val habitState = com.example.ui.state.LocalHabitState.current

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        // Calculate atomic score: water is 50%, movement is 50%
        val totalGoalMl = (habitState.totalGoalLiters.floatValue * 1000).toInt()
        val currentMl = habitState.currentWaterMl.intValue
        val waterScore = if (totalGoalMl > 0) (currentMl.toFloat() / totalGoalMl).coerceIn(0f, 1f) * 50f else 0f
        val moveScore = (habitState.movementHistory.value.size * 10f).coerceIn(0f, 50f)
        val totalScore = (waterScore + moveScore).toInt()

        val context = androidx.compose.ui.platform.LocalContext.current
        val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding() + 8.dp)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        val userProfile = com.example.ui.state.LocalUserProfile.current.value
                        Text(
                            text = "Hello, ${userProfile.name}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Monday, Oct 23",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            item {
                val waterProgress = if (totalGoalMl > 0) (currentMl.toFloat() / totalGoalMl).coerceIn(0f, 1f) else 0f
                val moveProgress = if (habitState.sedentaryMinutes.intValue > 0) {
                    ((habitState.sedentaryMinutes.intValue - habitState.lastBreakMins.intValue).coerceAtLeast(0).toFloat() / habitState.sedentaryMinutes.intValue)
                } else {
                    1f
                }
                AtomicScoreRing(
                    score = totalScore,
                    waterProgress = waterProgress,
                    moveProgress = moveProgress
                )
            }
            
            item {
                HabitsList(
                    currentWaterMl = habitState.currentWaterMl.intValue,
                    totalGoalLiters = habitState.totalGoalLiters.floatValue,
                    cupSizeMl = habitState.cupSizeMl.intValue,
                    onAddWater = {
                        if (habitState.premiumDaysRemaining.intValue > 0) {
                            habitState.currentWaterMl.intValue += habitState.cupSizeMl.intValue
                            habitState.addWaterTimestamp(null)
                            com.example.util.NotificationHelper.scheduleReminder(
                                context,
                                2001,
                                "Hydration Reminder",
                                "Time to drink some water!",
                                habitState.waterIntervalMins.intValue
                            )
                        } else {
                            android.widget.Toast.makeText(context, "Premium required to record activity and get notifications.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    lastBreakMins = habitState.lastBreakMins.intValue,
                    onLogMovement = { 
                        if (habitState.premiumDaysRemaining.intValue > 0) {
                            habitState.lastBreakTimestamp.value = System.currentTimeMillis()
                            habitState.lastBreakMins.intValue = 0
                            habitState.addMovementTimestamp(null)
                            if (habitState.movementEnabled.value) {
                                com.example.util.NotificationHelper.scheduleReminder(
                                    context,
                                    2002,
                                    "Movement Reminder",
                                    "Time to take a break and move!",
                                    habitState.sedentaryMinutes.intValue
                                )
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Premium required to record activity and get notifications.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    guardActive = habitState.guardActive.value,
                    onToggleGuard = onNavigateToFocus,
                    onNavigateToHydration = onNavigateToHydration,
                    onNavigateToMovement = onNavigateToMovement,
                    onNavigateToDailyHabits = onNavigateToDailyHabits
                )
            }
            
            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
        
        if (totalScore >= 100 && !habitState.hasShownGraffiti.value) {
            GraffitiEffect(onComplete = { habitState.setGraffitiShown(prefs) })
        }
    }
}
}

@Composable
fun GraffitiEffect(onComplete: () -> Unit = {}) {
    var isVisible by remember { mutableStateOf(true) }
    
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "confetti"
    )
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        onComplete() // Call immediately so it's registered even if user navigates away
        kotlinx.coroutines.delay(4000)
        isVisible = false
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    // Generate static random positions once
    val particles = remember {
        val list = mutableListOf<Triple<Float, Float, Float>>()
        for (i in 0..150) {
            list.add(Triple(Math.random().toFloat(), Math.random().toFloat(), (Math.random() * 15 + 5).toFloat()))
        }
        list
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)),
        exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            val colors = remember { listOf(primaryColor, secondaryColor, tertiaryColor, Color.White, Color.Yellow, Color.Cyan, Color.Magenta) }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                particles.forEachIndexed { i, particle ->
                    val (xRatio, yRatio, radius) = particle
                    val x = xRatio * width
                    // Add offsetY to make them fall, wrap around with modulo
                    val y = ((yRatio * height) + (offsetY * (1f + (i%5)*0.1f))) % height
                    val color = colors[i % colors.size]
                    
                    if (i % 2 == 0) {
                        drawCircle(color = color, radius = radius, center = androidx.compose.ui.geometry.Offset(x, y))
                    } else {
                        drawRect(
                            color = color,
                            topLeft = androidx.compose.ui.geometry.Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "100%",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 100.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Atomic Score Reached!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AtomicScoreRing(score: Int, moveProgress: Float, waterProgress: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = Color(0xFF00BFA5) // Water (Teal to match category)
            val secondaryColor = MaterialTheme.colorScheme.secondary // Movement
            val atomicColor = Color.White // Premium white for atomic score
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            
            val animatedScore by animateFloatAsState(
                targetValue = score.toFloat(),
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                ),
                label = "score_anim"
            )
            
            val animatedMove by animateFloatAsState(
                targetValue = moveProgress,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                ),
                label = "move_anim"
            )
            
            val animatedWater by animateFloatAsState(
                targetValue = waterProgress,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                ),
                label = "water_anim"
            )
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val baseStrokeWidth = 10.dp.toPx()
                val spacing = 6.dp.toPx()
                
                // Outer ring: Atomic Score
                val outerRadius = (size.width / 2) - (baseStrokeWidth / 2)
                drawCircle(color = trackColor.copy(alpha = 0.5f), radius = outerRadius, style = Stroke(width = baseStrokeWidth))
                drawArc(
                    color = atomicColor,
                    startAngle = -90f,
                    sweepAngle = 360f * (animatedScore / 100f),
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset((size.width / 2) - outerRadius, (size.height / 2) - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = baseStrokeWidth, cap = StrokeCap.Round)
                )
                
                // Middle ring: Movement
                val middleRadius = outerRadius - baseStrokeWidth - spacing
                drawCircle(color = trackColor.copy(alpha = 0.5f), radius = middleRadius, style = Stroke(width = baseStrokeWidth))
                drawArc(
                    color = secondaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedMove,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset((size.width / 2) - middleRadius, (size.height / 2) - middleRadius),
                    size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2),
                    style = Stroke(width = baseStrokeWidth, cap = StrokeCap.Round)
                )
                
                // Inner ring: Water
                val innerRadius = middleRadius - baseStrokeWidth - spacing
                drawCircle(color = trackColor.copy(alpha = 0.5f), radius = innerRadius, style = Stroke(width = baseStrokeWidth))
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedWater,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset((size.width / 2) - innerRadius, (size.height / 2) - innerRadius),
                    size = androidx.compose.ui.geometry.Size(innerRadius * 2, innerRadius * 2),
                    style = Stroke(width = baseStrokeWidth, cap = StrokeCap.Round)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${animatedScore.toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ATOMIC SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (score > 80) "You are maintaining a strong rhythm.\nKeep going." else "Building momentum.\nEvery action counts.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsList(
    currentWaterMl: Int,
    totalGoalLiters: Float,
    cupSizeMl: Int,
    onAddWater: () -> Unit,
    lastBreakMins: Int,
    onLogMovement: () -> Unit,
    guardActive: Boolean,
    onToggleGuard: () -> Unit,
    onNavigateToHydration: () -> Unit = {},
    onNavigateToMovement: () -> Unit = {},
    onNavigateToDailyHabits: () -> Unit = {}
) {
    var showMovementModal by remember { mutableStateOf(false) }
    val habitState = com.example.ui.state.LocalHabitState.current

    var justMovedTime by remember { mutableStateOf(0L) }
    var showJustMoved by remember { mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(lastBreakMins) {
        if (lastBreakMins == 0) {
            justMovedTime = System.currentTimeMillis()
            showJustMoved = true
            kotlinx.coroutines.delay(2000)
            showJustMoved = false
        }
    }

    var justAddedWaterTime by remember { mutableStateOf(0L) }
    var showJustAddedWater by remember { mutableStateOf(false) }
    
    androidx.compose.runtime.LaunchedEffect(currentWaterMl) {
        // If it increased
        if (currentWaterMl > 0) {
            justAddedWaterTime = System.currentTimeMillis()
            showJustAddedWater = true
            kotlinx.coroutines.delay(2000)
            showJustAddedWater = false
        }
    }

    val totalGoalMl = (totalGoalLiters * 1000).toInt()
    val cupsNeeded = if (cupSizeMl > 0) (totalGoalMl + cupSizeMl - 1) / cupSizeMl else 0
    val cupsDrunk = if (cupSizeMl > 0) currentWaterMl / cupSizeMl else 0
    val progress = if (totalGoalMl > 0) currentWaterMl.toFloat() / totalGoalMl.toFloat() else 0f

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Water
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.bounceClick(scaleDown = 0.95f) { onNavigateToHydration() }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.WaterDrop, "Water", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("Water", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            androidx.compose.animation.Crossfade(targetState = showJustAddedWater, label = "water_text_anim") { isJustAdded ->
                                if (isJustAdded) {
                                    Text("Added $cupSizeMl ml!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Text("$cupsDrunk/$cupsNeeded cups logged", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .bounceClick(scaleDown = 0.8f) { onAddWater() }
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Filled.Add, "Add water", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Progress bar
                com.example.ui.components.HabitProgressBar(progress = progress, height = 8.dp)
            }
        }
        
        // Movement
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.bounceClick(scaleDown = 0.95f, onDoubleClick = { showMovementModal = true }) { onNavigateToMovement() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsRun, "Movement", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Column {
                        Text("Movement", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        val nextAlertMins = (habitState.sedentaryMinutes.intValue - lastBreakMins).coerceAtLeast(0)
                        androidx.compose.animation.Crossfade(targetState = showJustMoved, label = "movement_text_anim") { isJustMoved ->
                            if (isJustMoved) {
                                Text("Just moved!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Next alert in $nextAlertMins mins", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                        .bounceClick(scaleDown = 0.8f) { onLogMovement() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PlayArrow, "Play", tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Morning Guard
        val context = androidx.compose.ui.platform.LocalContext.current
        val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        
        var isRealGuardActive by remember { mutableStateOf(false) }
        var realEndTimeStr by remember { mutableStateOf("") }
        var guardNameStr by remember { mutableStateOf("Guard") }
        
        androidx.compose.runtime.LaunchedEffect(Unit) {
            while (true) {
                val manualGuardActive = prefs.getBoolean("guard_active", false)
                if (manualGuardActive) {
                    isRealGuardActive = true
                    guardNameStr = "Focus Guard"
                    realEndTimeStr = "Manual"
                } else {
                    val morningEnabled = prefs.getBoolean("morning_guard_enabled", true)
                    val eveningEnabled = prefs.getBoolean("evening_guard_enabled", true)
                    val mStartStr = prefs.getString("morning_start", "06:00 AM") ?: "06:00 AM"
                    val mEndStr = prefs.getString("morning_end", "09:00 AM") ?: "09:00 AM"
                    val eStartStr = prefs.getString("evening_start", "10:00 PM") ?: "10:00 PM"
                    val eEndStr = prefs.getString("evening_end", "06:00 AM") ?: "06:00 AM"
                    
                    val cal = java.util.Calendar.getInstance()
                    val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                    val currentMin = cal.get(java.util.Calendar.MINUTE)
                    val currentTimeInMinutes = currentHour * 60 + currentMin
                    
                    fun parseTime(timeStr: String): Int {
                        val isPM = timeStr.contains("PM")
                        val parts = timeStr.replace(" AM", "").replace(" PM", "").split(":")
                        if (parts.size < 2) return 0
                        var hour = parts[0].toInt()
                        if (isPM && hour != 12) hour += 12
                        if (!isPM && hour == 12) hour = 0
                        return hour * 60 + parts[1].toInt()
                    }
                    
                    val mStart = parseTime(mStartStr)
                    val mEnd = parseTime(mEndStr)
                    val eStart = parseTime(eStartStr)
                    val eEnd = parseTime(eEndStr)
                    
                    if (morningEnabled && currentTimeInMinutes in mStart..mEnd) {
                        isRealGuardActive = true
                        guardNameStr = "Morning Guard"
                        realEndTimeStr = mEndStr
                    } else if (eveningEnabled) {
                        val isEveningActive = if (eStart > eEnd) {
                            currentTimeInMinutes >= eStart || currentTimeInMinutes <= eEnd
                        } else {
                            currentTimeInMinutes in eStart..eEnd
                        }
                        if (isEveningActive) {
                            isRealGuardActive = true
                            guardNameStr = "Evening Guard"
                            realEndTimeStr = eEndStr
                        } else {
                            isRealGuardActive = false
                            guardNameStr = "Focus Guard"
                        }
                    } else {
                        isRealGuardActive = false
                        guardNameStr = "Focus Guard"
                    }
                }
                kotlinx.coroutines.delay(60000)
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.bounceClick(scaleDown = 0.95f) { onToggleGuard() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Shield, "Morning Guard", tint = MaterialTheme.colorScheme.tertiary)
                    }
                    Column {
                        Text(guardNameStr, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(if (isRealGuardActive) if (realEndTimeStr == "Manual") "Manually Active" else "Active until $realEndTimeStr" else "Inactive", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (isRealGuardActive) {
                    Icon(Icons.Filled.CheckCircle, "Active", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        // Daily Habits
        val dailyHabitsViewModel: com.example.ui.state.DailyHabitsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        val allHabits by dailyHabitsViewModel.allHabits.collectAsStateWithLifecycle()
        val dailyLogs by dailyHabitsViewModel.dailyLogs.collectAsStateWithLifecycle()
        
        // Determine today's day of week (1=Sun, 2=Mon, ..., 7=Sat)
        val calendar = java.util.Calendar.getInstance()
        val todayDow = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        
        val todaysHabits = allHabits.filter { habit -> 
            if (habit.daysOfWeek == "1,2,3,4,5,6,7") true 
            else habit.daysOfWeek.split(",").contains(todayDow.toString())
        }
        val completedCount = todaysHabits.count { habit -> dailyLogs.any { it.habitId == habit.id && it.completed } }
        val totalCount = todaysHabits.size
        
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().clickable { onNavigateToDailyHabits() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, "Daily Habits", tint = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text("Daily Habits", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("$completedCount / $totalCount completed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }

    if (showMovementModal) {
        ModalBottomSheet(
            onDismissRequest = { showMovementModal = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text("Movement Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Yesterday", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("5 breaks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Comparison", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val todayBreaks = habitState.movementHistory.value.size
                            val diff = todayBreaks - 5
                            Text(
                                text = if (diff >= 0) "+$diff breaks" else "$diff breaks",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (diff >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text("Remind me every", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                val timeOptions = listOf(1, 15, 30, 45, 60, 90, 120, 180)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        timeOptions.chunked(4).forEachIndexed { rowIndex, rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowOptions.forEach { minutes ->
                                    val isSelected = habitState.sedentaryMinutes.intValue == minutes
                                    val text = if (minutes >= 60) {
                                        val hrs = minutes / 60
                                        val mins = minutes % 60
                                        if (mins == 0) "$hrs hr" else "$hrs hr $mins m"
                                    } else {
                                        "$minutes min"
                                    }
                                    
                                    Surface(
                                        modifier = Modifier.weight(1f).bounceClick(scaleDown = 0.9f) { if (habitState.movementEnabled.value) habitState.sedentaryMinutes.intValue = minutes },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowIndex < 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
