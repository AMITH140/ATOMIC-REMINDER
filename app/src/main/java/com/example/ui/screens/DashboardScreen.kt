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
    onNavigateToFocus: () -> Unit = {}
) {
    val habitState = com.example.ui.state.LocalHabitState.current

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        // Calculate atomic score: water is 50%, movement is 50%
        val totalGoalMl = (habitState.totalGoalLiters.floatValue * 1000).toInt()
        val currentMl = habitState.currentWaterMl.intValue
        val waterScore = if (totalGoalMl > 0) (currentMl.toFloat() / totalGoalMl).coerceIn(0f, 1f) * 50f else 0f
        val moveScore = if (habitState.lastBreakMins.intValue < 60) 50f else 0f
        val totalScore = (waterScore + moveScore).toInt()

        var hasShownGraffiti by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

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
                AtomicScoreRing(score = totalScore)
            }
            
            item {
                HabitsList(
                    currentWaterMl = habitState.currentWaterMl.intValue,
                    totalGoalLiters = habitState.totalGoalLiters.floatValue,
                    cupSizeMl = habitState.cupSizeMl.intValue,
                    onAddWater = { habitState.currentWaterMl.intValue += habitState.cupSizeMl.intValue },
                    lastBreakMins = habitState.lastBreakMins.intValue,
                    onLogMovement = { habitState.lastBreakMins.intValue = 0 },
                    guardActive = habitState.guardActive.value,
                    onToggleGuard = onNavigateToFocus,
                    onNavigateToHydration = onNavigateToHydration,
                    onNavigateToMovement = onNavigateToMovement
                )
            }
            
            item { Spacer(modifier = Modifier.height(120.dp)) }
        }
        
        if (totalScore >= 100 && !hasShownGraffiti) {
            GraffitiEffect(onComplete = { hasShownGraffiti = true })
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
        kotlinx.coroutines.delay(4000)
        isVisible = false
        kotlinx.coroutines.delay(500) // Wait for fade out
        onComplete()
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
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val colors = listOf(primaryColor, secondaryColor, tertiaryColor, Color.White, Color.Yellow, Color.Cyan, Color.Magenta)
                
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
fun AtomicScoreRing(score: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            
            val animatedScore by animateFloatAsState(
                targetValue = score.toFloat(),
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                ),
                label = "score_anim"
            )
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = strokeWidth)
                )
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * (animatedScore / 100f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${animatedScore.toInt()}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ATOMIC SCORE",
                    style = MaterialTheme.typography.labelLarge,
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
    onNavigateToMovement: () -> Unit = {}
) {
    var showMovementModal by remember { mutableStateOf(false) }
    val habitState = com.example.ui.state.LocalHabitState.current

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
                            Text("$cupsDrunk/$cupsNeeded cups logged", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text(if (lastBreakMins == 0) "Just moved!" else "Last break $lastBreakMins mins ago", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("Morning Guard", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(if (guardActive) "Active until 8:30 AM" else "Inactive", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (guardActive) {
                    Icon(Icons.Filled.CheckCircle, "Active", tint = MaterialTheme.colorScheme.primary)
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
                            val todayBreaks = if (lastBreakMins == 0) 1 else 0 // Dummy calculation
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
