package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path

@Composable
fun WaterPillAnimation(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "wave_transition")
    
    // Animate phase for wave movement
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Animate amplitude to give a gushing effect
    val amplitude by transition.animateFloat(
        initialValue = 0.02f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_amplitude"
    )
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    val waterColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    val waterColorLight = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(modifier = modifier.clip(RoundedCornerShape(percent = 50))) {
        val width = size.width
        val height = size.height
        
        // Draw background
        drawRect(color = backgroundColor, size = size)
        
        // If progress is 0, nothing to draw
        if (animatedProgress == 0f) return@Canvas
        
        val fillHeight = height * animatedProgress
        val startY = height - fillHeight
        
        // Draw back wave (lighter color, offset phase)
        val pathLight = Path().apply {
            moveTo(0f, height)
            lineTo(0f, startY)
            
            for (x in 0..width.toInt() step 5) {
                val relativeX = x / width
                val y = startY + (Math.sin((relativeX * 2 * Math.PI) + phase + Math.PI) * (amplitude * height)).toFloat()
                lineTo(x.toFloat(), y)
            }
            
            lineTo(width, height)
            close()
        }
        drawPath(pathLight, color = waterColorLight)
        
        // Draw front wave
        val path = Path().apply {
            moveTo(0f, height)
            lineTo(0f, startY)
            
            for (x in 0..width.toInt() step 5) {
                val relativeX = x / width
                val y = startY + (Math.sin((relativeX * 2 * Math.PI) + phase) * (amplitude * height)).toFloat()
                lineTo(x.toFloat(), y)
            }
            
            lineTo(width, height)
            close()
        }
        drawPath(path, color = waterColor)
    }
}
