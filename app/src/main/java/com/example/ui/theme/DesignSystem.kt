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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DesignSystem {
    @Composable
    fun primaryGradient(color: Color = MaterialTheme.colorScheme.primary) = Brush.linearGradient(
        colors = listOf(
            color,
            color.copy(alpha = 0.8f),
            color.copy(alpha = 0.95f)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    @Composable
    fun appBackgroundGradient(isDark: Boolean = isSystemInDarkTheme()) = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF000000),
                Color(0xFF060B14),
                Color(0xFF0A0F1D),
                Color(0xFF000000)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF8FAFC),
                Color(0xFFEFF6FF),
                Color(0xFFF1F5F9),
                Color(0xFFFFFFFF)
            )
        )
    }

    @Composable
    fun surfaceGradient(isDark: Boolean = isSystemInDarkTheme()) = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A0F1D),
                Color(0xFF060B14),
                Color(0xFF000000)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFC),
                Color(0xFFEFF6FF)
            )
        )
    }

    @Composable
    fun cardGradient(isDark: Boolean = isSystemInDarkTheme(), accent: Color? = null) = if (isDark) {
        if (accent != null) {
            Brush.linearGradient(
                colors = listOf(
                    accent.copy(alpha = 0.28f),
                    accent.copy(alpha = 0.10f),
                    Color(0xFF0A0F1D).copy(alpha = 0.95f),
                    Color(0xFF000000)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E293B).copy(alpha = 0.6f),
                    Color(0xFF0A0F1D).copy(alpha = 0.95f),
                    Color(0xFF000000)
                )
            )
        }
    } else {
        if (accent != null) {
            Brush.linearGradient(
                colors = listOf(
                    accent.copy(alpha = 0.26f),
                    accent.copy(alpha = 0.10f),
                    Color(0xFFFFFFFF),
                    Color(0xFFF8FAFC)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF8FAFC),
                    Color(0xFFEFF6FF)
                )
            )
        }
    }

    @Composable
    fun groupBannerGradient(color: Color, isDark: Boolean = isSystemInDarkTheme()) = if (isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                color.copy(alpha = 0.45f),
                color.copy(alpha = 0.20f),
                Color.Transparent
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                color.copy(alpha = 0.30f),
                color.copy(alpha = 0.12f),
                Color.Transparent
            )
        )
    }

    fun getSalesGroupColor(groupId: String, customColor: Long?): Color {
        if (customColor != null && customColor != 0L) return Color(customColor)
        return when (groupId) {
            "group_sanad" -> Color(0xFF2563EB)      // ياقوت أزرق ملكي
            "group_chini" -> Color(0xFFE11D48)      // عقيق أحمر قرمزي
            "group_game_cards" -> Color(0xFF059669) // زمرد عشبي مشرق
            "group_sharabat" -> Color(0xFFD97706)   // كهرمان ذهبي
            "group_internet" -> Color(0xFF0891B2)   // فيروز بحري نقي
            else -> {
                val palette = listOf(
                    Color(0xFF2563EB), Color(0xFF059669), Color(0xFFD97706),
                    Color(0xFF7C3AED), Color(0xFFE11D48), Color(0xFF0891B2),
                    Color(0xFFD946EF), Color(0xFF0D9488), Color(0xFF4F46E5)
                )
                palette[Math.abs(groupId.hashCode()) % palette.size]
            }
        }
    }

    fun getCashGroupColor(groupId: String, customColor: Long?): Color {
        if (customColor != null && customColor != 0L) return Color(customColor)
        return when (groupId) {
            "cash_group_yer" -> Color(0xFF10B981)        // زمرد نقدي مشرق
            "cash_group_bank" -> Color(0xFF3B82F6)       // أزرق بنكي كهربائي
            "cash_group_currencies" -> Color(0xFFF59E0B) // ذهب عنبري ناصع
            else -> {
                val palette = listOf(
                    Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFFF59E0B),
                    Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF14B8A6),
                    Color(0xFF6366F1)
                )
                palette[Math.abs(groupId.hashCode()) % palette.size]
            }
        }
    }

    @Composable
    fun topBarGradient(isDark: Boolean = isSystemInDarkTheme(), accent: Color = MaterialTheme.colorScheme.primary) = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E293B).copy(alpha = 0.98f),
                Color(0xFF0F172A).copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                accent.copy(alpha = 0.10f),
                Color(0xFFF8FAFC)
            )
        )
    }

    @Composable
    fun navBarGradient(isDark: Boolean = isSystemInDarkTheme(), accent: Color = MaterialTheme.colorScheme.primary) = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E293B).copy(alpha = 0.95f),
                Color(0xFF0F172A).copy(alpha = 0.98f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                accent.copy(alpha = 0.10f),
                Color(0xFFF8FAFC)
            )
        )
    }

    fun emeraldGradient() = Brush.linearGradient(
        colors = listOf(
            Color(0xFF059669),
            Color(0xFF10B981),
            Color(0xFF34D399)
        )
    )

    fun roseGradient() = Brush.linearGradient(
        colors = listOf(
            Color(0xFFDC2626),
            Color(0xFFEF4444),
            Color(0xFFF87171)
        )
    )

    fun amberGradient() = Brush.linearGradient(
        colors = listOf(
            Color(0xFFD97706),
            Color(0xFFF59E0B),
            Color(0xFFFBBF24)
        )
    )

    fun indigoGradient() = Brush.linearGradient(
        colors = listOf(
            Color(0xFF4F46E5),
            Color(0xFF6366F1),
            Color(0xFF818CF8)
        )
    )

    fun slateMetallicGradient(isDark: Boolean) = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF334155),
                Color(0xFF1E293B),
                Color(0xFF0F172A)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFC),
                Color(0xFFEFF6FF)
            )
        )
    }
}

fun Modifier.vibrant3d(
    shape: Shape = RoundedCornerShape(14.dp),
    elevation: Dp = 6.dp,
    baseColor: Color = Color.Transparent,
    isDark: Boolean = false,
    gradientBrush: Brush? = null
) = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = Color.Black.copy(alpha = if (isDark) 0.35f else 0.15f),
        spotColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.25f)
    )
    .then(
        if (gradientBrush != null) {
            Modifier.background(brush = gradientBrush, shape = shape)
        } else if (baseColor != Color.Transparent) {
            // Apply a subtle 2-stop gradient on the base color for richer depth
            Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseColor,
                        baseColor.copy(alpha = (baseColor.alpha * 0.85f).coerceIn(0f, 1f))
                    )
                ),
                shape = shape
            )
        } else {
            Modifier
        }
    )
    .clip(shape)
    .drawBehind {
        // Top specular highlight gradient
        val highlightColor = if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.35f)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(highlightColor, Color.Transparent),
                startY = 0f,
                endY = size.height * 0.45f
            )
        )
        // Bottom ambient depth shadow gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = if (isDark) 0.18f else 0.08f)),
                startY = size.height * 0.55f,
                endY = size.height
            )
        )
    }
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.22f else 0.8f),
                if (isDark) Color.Black.copy(alpha = 0.35f) else Color(0xFFCBD5E1).copy(alpha = 0.8f)
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
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.80f),
                Color.White.copy(alpha = 0.90f)
            )
        ),
        shape = shape
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                Color.White.copy(alpha = 0.2f)
            )
        ),
        shape = shape
    )

