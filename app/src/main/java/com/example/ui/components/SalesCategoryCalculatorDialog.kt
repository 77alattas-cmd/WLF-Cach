package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.model.SalesGroupUiState
import com.example.ui.model.AccountingFormatter

enum class CalculatorTargetField(val label: String) {
    GIVEN("المعطى"),
    ADDED("الإضافي"),
    REMAINING("المتبقي")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalesCategoryCalculatorDialog(
    groups: List<SalesGroupUiState>,
    initialGroupId: String,
    onDismiss: () -> Unit,
    onCommitSales: (groupId: String, targetField: CalculatorTargetField, quantities: Map<Int, Int>) -> Unit
) {
    var selectedGroupId by remember { mutableStateOf(initialGroupId) }
    val currentGroup = groups.find { it.id == selectedGroupId } ?: groups.firstOrNull()

    var targetField by remember { mutableStateOf(CalculatorTargetField.GIVEN) }

    // Map of denomination -> entered quantity
    val quantities = remember { mutableStateMapOf<Int, String>() }

    // Active focused denomination for the numpad
    var selectedDenom by remember(selectedGroupId) {
        val firstDenom = currentGroup?.rows?.firstOrNull()?.denomination ?: 500
        mutableStateOf(firstDenom)
    }

    // Initialize with current row values if desired
    LaunchedEffect(selectedGroupId, targetField) {
        quantities.clear()
        currentGroup?.rows?.forEach { row ->
            val initial = when (targetField) {
                CalculatorTargetField.GIVEN -> row.givenInput
                CalculatorTargetField.ADDED -> row.addedInput
                CalculatorTargetField.REMAINING -> row.remainingInput
            }
            if (initial.isNotBlank()) {
                quantities[row.denomination] = initial
            }
        }
    }

    // Calculate total choices
    val totalTickets = quantities.values.sumOf { it.toIntOrNull() ?: 0 }
    val totalRevenue = quantities.entries.sumOf { (denom, qtyStr) ->
        val qty = qtyStr.toIntOrNull() ?: 0
        (qty * denom).toDouble()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "آلة حاسبة مبيعات الفئات",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            )
                            Text(
                                text = "احسب الفئات والكميات ثم اعتمد الإدخال بضغطة زر",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Group & Field Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المجموعة:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                    groups.forEach { grp ->
                        FilterChip(
                            selected = grp.id == selectedGroupId,
                            onClick = { selectedGroupId = grp.id },
                            label = { Text(grp.name, fontSize = 11.sp, fontWeight = if (grp.id == selectedGroupId) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "الحقل:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                    CalculatorTargetField.values().forEach { field ->
                        FilterChip(
                            selected = targetField == field,
                            onClick = { targetField = field },
                            label = { Text(field.label, fontSize = 11.sp, fontWeight = if (targetField == field) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grand Total Display Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مجموع الاختيارات المحسوبة",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            )
                            Text(
                                text = "${AccountingFormatter.formatYer(totalRevenue)} ريال",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "$totalTickets تذكرة",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Selection Cards (Horizontal / Multi-row scroll)
                Text(
                    text = "اختر الفئة لإدخال كميتها:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, fontSize = 10.5.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))

                val groupRows = currentGroup?.rows ?: emptyList()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    groupRows.forEach { row ->
                        val isSelected = row.denomination == selectedDenom
                        val qty = quantities[row.denomination] ?: ""
                        val qtyInt = qty.toIntOrNull() ?: 0
                        val rowSubtotal = (qtyInt * row.denomination).toDouble()

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .width(94.dp)
                                .clickable { selectedDenom = row.denomination }
                                .testTag("chip_calc_cat_${row.denomination}")
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "فئة ${row.denomination}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (qty.isBlank()) "0" else "$qty س",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = AccountingFormatter.formatYer(rowSubtotal),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current Selected Category Input Line & Quick Increment Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "كمية فئة $selectedDenom: ${quantities[selectedDenom] ?: "0"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        )

                        // Quick increment buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1, 5, 10, 50).forEach { inc ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier
                                        .clickable {
                                            val cur = quantities[selectedDenom]?.toIntOrNull() ?: 0
                                            quantities[selectedDenom] = (cur + inc).toString()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text("+$inc", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Built-in Numpad Matrix
                val numpadRows = listOf(
                    listOf("7", "8", "9", "مسح"),
                    listOf("4", "5", "6", "تصفير"),
                    listOf("1", "2", "3", "00"),
                    listOf("0", "000", "التالي ⬇")
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    numpadRows.forEach { rowKeys ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowKeys.forEach { key ->
                                val weight = if (key == "التالي ⬇") 2f else 1f
                                val isAction = key in listOf("مسح", "تصفير", "التالي ⬇")

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (key) {
                                        "مسح" -> MaterialTheme.colorScheme.surfaceVariant
                                        "تصفير" -> MaterialTheme.colorScheme.errorContainer
                                        "التالي ⬇" -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
                                    },
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxHeight()
                                        .clickable {
                                            val currentVal = quantities[selectedDenom] ?: ""
                                            when (key) {
                                                "مسح" -> {
                                                    if (currentVal.isNotEmpty()) {
                                                        quantities[selectedDenom] = currentVal.dropLast(1)
                                                    }
                                                }
                                                "تصفير" -> {
                                                    quantities[selectedDenom] = ""
                                                }
                                                "التالي ⬇" -> {
                                                    // Move focus to next denomination
                                                    val denoms = groupRows.map { it.denomination }
                                                    val currentIndex = denoms.indexOf(selectedDenom)
                                                    if (currentIndex != -1 && denoms.isNotEmpty()) {
                                                        val nextIndex = (currentIndex + 1) % denoms.size
                                                        selectedDenom = denoms[nextIndex]
                                                    }
                                                }
                                                else -> {
                                                    // Digit
                                                    if (currentVal.length < 7) {
                                                        quantities[selectedDenom] = currentVal + key
                                                    }
                                                }
                                            }
                                        }
                                        .testTag("btn_calc_key_$key")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = if (isAction) 13.sp else 18.sp,
                                                color = when (key) {
                                                    "تصفير" -> MaterialTheme.colorScheme.error
                                                    "التالي ⬇" -> MaterialTheme.colorScheme.onSecondaryContainer
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action Row: Dismiss & Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val resultMap = quantities.mapNotNull { (denom, qtyStr) ->
                                val qty = qtyStr.toIntOrNull() ?: 0
                                denom to qty
                            }.toMap()
                            onCommitSales(selectedGroupId, targetField, resultMap)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("btn_commit_sales_calculator")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اعتماد وإدخال في المبيعات ✅", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
