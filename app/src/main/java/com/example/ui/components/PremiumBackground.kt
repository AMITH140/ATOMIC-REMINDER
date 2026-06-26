package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect

@Composable
fun PremiumBackground() {
    val transition = rememberInfiniteTransition(label = "bg_transition")

    // Animate gradients to create a breathing, sweeping curve effect
    val animatedProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_anim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Dark grainy base (simulated with very dark gradient)
        drawRect(color = Color(0xFF0F0F13))

        // Sweep gradient curve
        val glowBrush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE2E2E2).copy(alpha = 0.15f),
                Color(0xFF888888).copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = Offset(width * (0.2f + 0.6f * animatedProgress), height * (0.3f + 0.4f * (1f - animatedProgress))),
            radius = width * 1.2f
        )
        
        drawRect(brush = glowBrush)
        
        // Second subtle arc
        val arcBrush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF5A5A66).copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = Offset(width * (0.8f - 0.4f * animatedProgress), height * (0.7f - 0.2f * animatedProgress)),
            radius = width
        )
        
        drawRect(brush = arcBrush)
    }
}
