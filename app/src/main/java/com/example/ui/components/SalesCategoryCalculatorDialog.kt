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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Backspace
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

@Composable
fun SalesCategoryCalculatorDialog(
    groups: List<SalesGroupUiState>,
    initialGroupId: String,
    onDismiss: () -> Unit
) {
    var selectedGroupId by remember { mutableStateOf(initialGroupId) }
    val currentGroup = groups.find { it.id == selectedGroupId } ?: groups.firstOrNull()

    // Flat Map of "groupId_denomination" -> entered quantity (as String)
    val quantitiesMap = remember { mutableStateMapOf<String, String>() }

    // Active focused denomination for the numpad in the selected group
    var selectedDenom by remember(selectedGroupId) {
        val firstDenom = currentGroup?.rows?.firstOrNull()?.denomination ?: 500
        mutableStateOf(firstDenom)
    }

    // Calculate totals for the selected group based on scratchpad data
    val currentGroupRows = currentGroup?.rows ?: emptyList()
    val totalTickets = currentGroupRows.sumOf { row ->
        val qtyStr = quantitiesMap["${selectedGroupId}_${row.denomination}"] ?: ""
        qtyStr.toIntOrNull() ?: 0
    }
    val totalRevenue = currentGroupRows.sumOf { row ->
        val qtyStr = quantitiesMap["${selectedGroupId}_${row.denomination}"] ?: ""
        val qty = qtyStr.toIntOrNull() ?: 0
        (qty * row.denomination).toDouble()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "حاسبة الفئات الجانبية 🧮",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            )
                            Text(
                                text = "أداة عرض واحتساب مستقلة لا تؤثر على مبيعاتك المباشرة",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Group Selector Row (Both active and inactive groups are shown)
                Text(
                    text = "المجموعة للتجربة والحساب الجانبي:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    groups.forEach { grp ->
                        val isSelected = grp.id == selectedGroupId
                        val indicator = if (grp.isEnabled) "🟢" else "⚪"
                        val label = "$indicator ${grp.name}"
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGroupId = grp.id },
                            label = { 
                                Text(
                                    text = label, 
                                    fontSize = 10.5.sp, 
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Grand Total Display Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مجموع الحساب الجانبي لهذه المجموعة",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.5.sp)
                            )
                            Text(
                                text = "${AccountingFormatter.formatYer(totalRevenue)} ريال",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "$totalTickets تذكرة",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Category Selection Cards
                Text(
                    text = "اختر الفئة وأدخل الكمية لحسابها:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                )
                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    currentGroupRows.forEach { row ->
                        val isSelected = row.denomination == selectedDenom
                        val qtyKey = "${selectedGroupId}_${row.denomination}"
                        val qty = quantitiesMap[qtyKey] ?: ""
                        val qtyInt = qty.toIntOrNull() ?: 0
                        val rowSubtotal = (qtyInt * row.denomination).toDouble()

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .width(84.dp)
                                .clickable { selectedDenom = row.denomination }
                                .testTag("chip_calc_cat_${row.denomination}")
                        ) {
                            Column(
                                modifier = Modifier.padding(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "فئة ${row.denomination}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (qty.isBlank()) "0" else "$qty س",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = AccountingFormatter.formatYer(rowSubtotal),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Current Selected Category Input Line & Quick Increment Bar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val activeQtyKey = "${selectedGroupId}_$selectedDenom"
                        Text(
                            text = "فئة $selectedDenom: ${quantitiesMap[activeQtyKey] ?: "0"}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        )

                        // Quick increment buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            listOf(1, 5, 10, 50).forEach { inc ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier
                                        .clickable {
                                            val activeKey = "${selectedGroupId}_$selectedDenom"
                                            val cur = quantitiesMap[activeKey]?.toIntOrNull() ?: 0
                                            quantitiesMap[activeKey] = (cur + inc).toString()
                                        }
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text("+$inc", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Built-in Compact Numpad Matrix with fixed, compact heights (solves too large buttons)
                val numpadRows = listOf(
                    listOf("7", "8", "9", "مسح"),
                    listOf("4", "5", "6", "تصفير"),
                    listOf("1", "2", "3", "00"),
                    listOf("0", "000", "التالي ⬇")
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    numpadRows.forEach { rowKeys ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp), // Fixed height for keypad buttons - perfectly compact and elegant!
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            rowKeys.forEach { key ->
                                val weight = if (key == "التالي ⬇") 1.8f else 1f
                                val isAction = key in listOf("مسح", "تصفير", "التالي ⬇")

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (key) {
                                        "مسح" -> MaterialTheme.colorScheme.surfaceVariant
                                        "تصفير" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                                        "التالي ⬇" -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                                    },
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxHeight()
                                        .clickable {
                                            val activeKey = "${selectedGroupId}_$selectedDenom"
                                            val currentVal = quantitiesMap[activeKey] ?: ""
                                            when (key) {
                                                "مسح" -> {
                                                    if (currentVal.isNotEmpty()) {
                                                        quantitiesMap[activeKey] = currentVal.dropLast(1)
                                                    }
                                                }
                                                "تصفير" -> {
                                                    quantitiesMap[activeKey] = ""
                                                }
                                                "التالي ⬇" -> {
                                                    // Move focus to next denomination
                                                    val denoms = currentGroupRows.map { it.denomination }
                                                    val currentIndex = denoms.indexOf(selectedDenom)
                                                    if (currentIndex != -1 && denoms.isNotEmpty()) {
                                                        val nextIndex = (currentIndex + 1) % denoms.size
                                                        selectedDenom = denoms[nextIndex]
                                                    }
                                                }
                                                else -> {
                                                    // Digit
                                                    if (currentVal.length < 6) {
                                                        quantitiesMap[activeKey] = currentVal + key
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
                                        if (key == "مسح") {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "مسح",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = if (isAction) 11.sp else 14.sp,
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Row: Reset, Reset All, and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Current Group button
                    OutlinedButton(
                        onClick = {
                            val keysToRemove = quantitiesMap.keys.filter { it.startsWith("${selectedGroupId}_") }
                            keysToRemove.forEach { quantitiesMap.remove(it) }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصفير المجموعة", fontSize = 10.5.sp)
                    }

                    // Reset All Groups button
                    OutlinedButton(
                        onClick = { quantitiesMap.clear() },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصفير الكل", fontSize = 10.5.sp)
                    }

                    // Close button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("إغلاق", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
