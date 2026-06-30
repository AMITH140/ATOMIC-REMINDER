package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

fun Modifier.bounceClick(
    scaleDown: Float = 0.90f,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    var pressInteraction: PressInteraction.Press? = remember { null }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "bounce_anim"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .indication(interactionSource, androidx.compose.material3.ripple())
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    pressInteraction = press
                    scope.launch { interactionSource.emit(press) }
                    tryAwaitRelease()
                    pressInteraction?.let { 
                        scope.launch { interactionSource.emit(PressInteraction.Release(it)) } 
                    }
                },
                onTap = { onClick() },
                onDoubleTap = onDoubleClick?.let { { it() } }
            )
        }
}
