package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AccountingFormatter
import com.example.ui.model.BalanceStatus
import kotlin.math.abs

@Composable
fun BalanceStatusBadge(
    status: BalanceStatus,
    balanceAmount: Double,
    modifier: Modifier = Modifier
) {
    val bgColor: Color
    val borderColor: Color
    val contentColor: Color
    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val statusText: String
    val explanation: String

    when (status) {
        BalanceStatus.MATCHED -> {
            bgColor = MaterialTheme.colorScheme.surfaceVariant
            borderColor = MaterialTheme.colorScheme.outline
            contentColor = MaterialTheme.colorScheme.onSurface
            icon = Icons.Default.CheckCircle
            statusText = "حالة الموازنة: متطابق تماماً ✓"
            explanation = "المبيعات متطابقة مع مبلغ الصندوق الفعلي (لا يوجد عجز أو فائض)"
        }
        BalanceStatus.DEFICIT -> {
            bgColor = MaterialTheme.colorScheme.primary
            borderColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
            icon = Icons.Default.TrendingDown
            statusText = "حالة الموازنة: عجز في الصندوق (${AccountingFormatter.formatMoney(balanceAmount)}) ↓"
            explanation = "مبلغ الصندوق الفعلي أقل من مجموع المبيعات بمقدار ${AccountingFormatter.formatMoney(balanceAmount)}"
        }
        BalanceStatus.SURPLUS -> {
            bgColor = MaterialTheme.colorScheme.surface
            borderColor = MaterialTheme.colorScheme.outline
            contentColor = MaterialTheme.colorScheme.onSurface
            icon = Icons.Default.TrendingUp
            statusText = "حالة الموازنة: فائض في الصندوق (${AccountingFormatter.formatMoney(abs(balanceAmount))}) ↑"
            explanation = "مبلغ الصندوق الفعلي يزيد عن مجموع المبيعات بمقدار ${AccountingFormatter.formatMoney(abs(balanceAmount))}"
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                        color = contentColor.copy(alpha = 0.9f),
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
    val bgColor: Color
    val borderColor: Color
    val contentColor: Color
    val text: String

    when (status) {
        BalanceStatus.MATCHED -> {
            bgColor = MaterialTheme.colorScheme.surfaceVariant
            borderColor = MaterialTheme.colorScheme.outlineVariant
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            text = "متطابق ✓"
        }
        BalanceStatus.DEFICIT -> {
            bgColor = MaterialTheme.colorScheme.primary
            borderColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
            text = "عجز: ${AccountingFormatter.formatMoney(balanceAmount)} ↓"
        }
        BalanceStatus.SURPLUS -> {
            bgColor = MaterialTheme.colorScheme.surface
            borderColor = MaterialTheme.colorScheme.outline
            contentColor = MaterialTheme.colorScheme.onSurface
            text = "فائض: ${AccountingFormatter.formatMoney(abs(balanceAmount))} ↑"
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 13.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

