package com.fiestast.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fiestast.launcher.ui.theme.CardBackground
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.Graphite

@Composable
fun AutomotiveCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CardBorder,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Graphite.copy(alpha = 0.6f),
            CardBackground
        )
    )

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = clickableModifier
            .clip(shape)
            .border(BorderStroke(borderWidth, borderColor), shape)
            .background(brush = backgroundBrush)
            .padding(14.dp)
    ) {
        content()
    }
}
