package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.InTubePink
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun HeartBurstOverlay(
    isVisible: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val scale = remember { Animatable(0.2f) }
    val alpha = remember { Animatable(1f) }
    val rotation = remember { Animatable(-15f) }

    LaunchedEffect(isVisible) {
        coroutineScope {
            launch {
                scale.animateTo(
                    targetValue = 1.35f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            launch {
                rotation.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing)
                )
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(350, delayMillis = 150)
                )
                onAnimationEnd()
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Drop shadow / glow backing
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0x66FF4DA6),
            modifier = Modifier
                .size(118.dp)
                .scale(scale.value * 1.15f)
                .rotate(rotation.value)
        )
        // Foreground heart
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = InTubePink.copy(alpha = alpha.value),
            modifier = Modifier
                .size(100.dp)
                .scale(scale.value)
                .rotate(rotation.value)
        )
    }
}
