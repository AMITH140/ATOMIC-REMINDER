package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.HabitProgressBar
import com.example.ui.components.bounceClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydrationScreen(onNavigateBack: () -> Unit) {
    val habitState = com.example.ui.state.LocalHabitState.current
    val totalGoalLiters = habitState.totalGoalLiters.floatValue
    val cupSizeMl = habitState.cupSizeMl.intValue
    
    val totalGoalMl = (totalGoalLiters * 1000).toInt()
    val currentMl = habitState.currentWaterMl.intValue
    val cupsDrunk = if (cupSizeMl > 0) currentMl / cupSizeMl else 0
    val cupsNeeded = if (cupSizeMl > 0) (totalGoalMl + cupSizeMl - 1) / cupSizeMl else 0
    val progress = if (totalGoalMl > 0) currentMl.toFloat() / totalGoalMl.toFloat() else 0f

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        topBar = {
            TopAppBar(
                title = { Text("Hydration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            com.example.ui.components.WaterPillAnimation(
                                progress = progress,
                                modifier = Modifier.fillMaxSize()
                            )
                            Icon(
                                Icons.Filled.WaterDrop, 
                                contentDescription = "Water", 
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), 
                                modifier = Modifier.size(60.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "$currentMl / $totalGoalMl ml",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$cupsDrunk / $cupsNeeded cups",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                                    .bounceClick(scaleDown = 0.8f) { if (habitState.currentWaterMl.intValue >= cupSizeMl) habitState.currentWaterMl.intValue -= cupSizeMl else habitState.currentWaterMl.intValue = 0 }
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Filled.Remove, "Remove cup", tint = MaterialTheme.colorScheme.primary)
                            }
                            Button(
                                onClick = { habitState.currentWaterMl.intValue += cupSizeMl },
                                modifier = Modifier.bounceClick(scaleDown = 0.95f) {},
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Filled.Add, "Add cup")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Drink ($cupSizeMl ml)")
                            }
                        }
                    }
                }
            }

            item {
                Text("Performance", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
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
                            Text("2.0 L", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Comparison", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val diff = ((currentMl / 1000f) - 2.0f)
                            Text(
                                text = if (diff >= 0) "+${String.format("%.1f", diff)} L" else "${String.format("%.1f", diff)} L",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (diff >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text("Customize Goal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Daily Goal (Liters)")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (habitState.totalGoalLiters.floatValue > 0.5f) habitState.totalGoalLiters.floatValue -= 0.1f }) {
                                    Icon(Icons.Filled.Remove, "Decrease")
                                }
                                Text(String.format("%.1f L", totalGoalLiters), style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(60.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                IconButton(onClick = { habitState.totalGoalLiters.floatValue += 0.1f }) {
                                    Icon(Icons.Filled.Add, "Increase")
                                }
                            }
                        }
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Cup Size (ml)")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (habitState.cupSizeMl.intValue > 50) habitState.cupSizeMl.intValue -= 50 }) {
                                    Icon(Icons.Filled.Remove, "Decrease")
                                }
                                Text("$cupSizeMl ml", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(70.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                IconButton(onClick = { habitState.cupSizeMl.intValue += 50 }) {
                                    Icon(Icons.Filled.Add, "Increase")
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
