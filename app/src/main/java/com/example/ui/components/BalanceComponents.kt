package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AccountingFormatter
import com.example.ui.model.BalanceStatus
import com.example.ui.theme.DesignSystem
import com.example.ui.theme.vibrant3d
import androidx.compose.foundation.isSystemInDarkTheme
import kotlin.math.abs

@Composable
fun BalanceStatusBadge(
    status: BalanceStatus,
    balanceAmount: Double,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val gradient: Brush
    val borderColor: Color
    val contentColor: Color
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val statusText: String
    val explanation: String

    when (status) {
        BalanceStatus.MATCHED -> {
            gradient = DesignSystem.emeraldGradient()
            borderColor = Color(0xFF34D399)
            contentColor = Color.White
            icon = Icons.Default.CheckCircle
            statusText = "حالة الموازنة: متطابق تماماً ✓"
            explanation = "المبيعات متطابقة مع مبلغ الصندوق الفعلي (لا يوجد عجز أو فائض)"
        }
        BalanceStatus.DEFICIT -> {
            gradient = DesignSystem.roseGradient()
            borderColor = Color(0xFFF87171)
            contentColor = Color.White
            icon = Icons.Default.TrendingDown
            statusText = "حالة الموازنة: عجز في الصندوق (${AccountingFormatter.formatMoney(balanceAmount)}) ↓"
            explanation = "مبلغ الصندوق الفعلي أقل من مجموع المبيعات بمقدار ${AccountingFormatter.formatMoney(balanceAmount)}"
        }
        BalanceStatus.SURPLUS -> {
            gradient = DesignSystem.amberGradient()
            borderColor = Color(0xFFFBBF24)
            contentColor = Color(0xFF451A03)
            icon = Icons.Default.TrendingUp
            statusText = "حالة الموازنة: فائض في الصندوق (${AccountingFormatter.formatMoney(abs(balanceAmount))}) ↑"
            explanation = "مبلغ الصندوق الفعلي يزيد عن مجموع المبيعات بمقدار ${AccountingFormatter.formatMoney(abs(balanceAmount))}"
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .vibrant3d(
                shape = RoundedCornerShape(14.dp),
                elevation = 6.dp,
                isDark = isDark,
                gradientBrush = gradient
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = contentColor.copy(alpha = 0.95f),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun MiniBalanceStatusBadge(
    status: BalanceStatus,
    balanceAmount: Double,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val gradient: Brush
    val borderColor: Color
    val contentColor: Color
    val text: String

    when (status) {
        BalanceStatus.MATCHED -> {
            gradient = DesignSystem.emeraldGradient()
            borderColor = Color(0xFF34D399)
            contentColor = Color.White
            text = "متطابق ✓"
        }
        BalanceStatus.DEFICIT -> {
            gradient = DesignSystem.roseGradient()
            borderColor = Color(0xFFF87171)
            contentColor = Color.White
            text = "عجز: ${AccountingFormatter.formatMoney(balanceAmount)} ↓"
        }
        BalanceStatus.SURPLUS -> {
            gradient = DesignSystem.amberGradient()
            borderColor = Color(0xFFFBBF24)
            contentColor = Color(0xFF451A03)
            text = "فائض: ${AccountingFormatter.formatMoney(abs(balanceAmount))} ↑"
        }
    }

    Box(
        modifier = modifier
            .vibrant3d(
                shape = RoundedCornerShape(10.dp),
                elevation = 3.dp,
                isDark = isDark,
                gradientBrush = gradient
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

