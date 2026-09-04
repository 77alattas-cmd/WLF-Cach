package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DesignSystem {
    @Composable
    fun primaryGradient() = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        )
    )

    @Composable
    fun surfaceGradient() = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    )
    
    @Composable
    fun cardGradient() = Brush.linearGradient(
        colors = if (isSystemInDarkTheme()) {
            listOf(Color(0xFF2B2B2B), Color(0xFF1E1E1E))
        } else {
            listOf(Color(0xFFFFFFFF), Color(0xFFFBFBFB))
        }
    )
}

fun Modifier.vibrant3d(
    shape: Shape = RoundedCornerShape(12.dp),
    elevation: Dp = 8.dp,
    baseColor: Color = Color.Transparent,
    isDark: Boolean = false
) = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = Color.Black.copy(alpha = 0.25f),
        spotColor = Color.Black.copy(alpha = 0.35f)
    )
    .background(baseColor, shape)
    .clip(shape)
    .drawBehind {
        // Top highlight (inner glow)
        val highlightColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.3f)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(highlightColor, Color.Transparent),
                startY = 0f,
                endY = size.height * 0.4f
            )
        )
        // Bottom depth shadow (inner)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.1f)),
                startY = size.height * 0.6f,
                endY = size.height
            )
        )
    }
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.15f else 0.4f),
                Color.Black.copy(alpha = if (isDark) 0.3f else 0.1f)
            )
        ),
        shape = shape
    )

fun Modifier.glassCard3d(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 4.dp
) = this
    .shadow(elevation = elevation, shape = shape)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                Color.White.copy(alpha = 0.92f)
            )
        ),
        shape = shape
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(Color.White, Color.White.copy(alpha = 0.3f))
        ),
        shape = shape
    )
