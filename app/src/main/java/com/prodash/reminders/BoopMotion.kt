package com.prodash.reminders

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BoopPressScale(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "boop_press_scale",
    )
    Box(
        modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        content()
    }
}

@Composable
fun rememberFabBottomPadding(createSheetOpen: Boolean): Dp {
    return animateDpAsState(
        targetValue = if (createSheetOpen) 248.dp else 118.dp,
        animationSpec = spring(stiffness = 380f, dampingRatio = 0.82f),
        label = "fab_bottom_padding",
    ).value
}
