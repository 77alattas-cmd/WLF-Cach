package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.model.AccountingFormatter
import com.example.ui.model.SalesGroupType
import com.example.ui.model.SalesGroupUiState

data class CalcFieldRef(
    val groupId: String,
    val groupName: String,
    val denomination: Int?,
    val isDirectEntry: Boolean
)

@Composable
fun MultiCategoryCalculatorDialog(
    groups: List<SalesGroupUiState>,
    onDismiss: () -> Unit,
    onApplyQuantities: (Map<String, Map<Int, String>>, Map<String, String>) -> Unit
) {
    val activeGroupsList = groups.filter { it.isEnabled }

    // Quantities map for DENOMINATIONS: groupId -> (denomination -> quantityInput)
    val quantitiesMap = remember {
        mutableStateMapOf<String, MutableMap<Int, String>>().apply {
            activeGroupsList.forEach { grp ->
                if (grp.type != SalesGroupType.DIRECT_ENTRY) {
                    val denomMap = mutableMapOf<Int, String>()
                    val rows = grp.activeRows.ifEmpty { grp.rows }
                    rows.forEach { row ->
                        denomMap[row.denomination] = row.givenInput
                    }
                    put(grp.id, denomMap)
                }
            }
        }
    }

    // Direct amounts map for DIRECT_ENTRY: groupId -> amountInput
    val directAmountsMap = remember {
        mutableStateMapOf<String, String>().apply {
            activeGroupsList.forEach { grp ->
                if (grp.type == SalesGroupType.DIRECT_ENTRY) {
                    val existingAmt = grp.directEntries.firstOrNull()?.amountInput ?: ""
                    put(grp.id, existingAmt)
                }
            }
        }
    }

    val fieldsList = remember(activeGroupsList) {
        val list = mutableListOf<CalcFieldRef>()
        activeGroupsList.forEach { grp ->
            if (grp.type == SalesGroupType.DIRECT_ENTRY) {
                list.add(CalcFieldRef(grp.id, grp.name, null, true))
            } else {
                val rows = grp.activeRows.ifEmpty { grp.rows }
                rows.forEach { row ->
                    list.add(CalcFieldRef(grp.id, grp.name, row.denomination, false))
                }
            }
        }
        list
    }

    val numpadController = LocalNumpadController.current

    fun showField(index: Int) {
        if (index !in fieldsList.indices) return
        val field = fieldsList[index]
        val currentValue = if (field.isDirectEntry) {
            directAmountsMap[field.groupId] ?: ""
        } else {
            quantitiesMap[field.groupId]?.get(field.denomination!!) ?: ""
        }

        numpadController.show(
            initialValue = currentValue,
            targetTitle = if (field.isDirectEntry) field.groupName else "${field.groupName} - فئة ${field.denomination}",
            targetSubtitle = if (field.isDirectEntry) "إدخال مباشر" else "إدخال الكمية",
            onValueChanged = { newVal ->
                if (field.isDirectEntry) {
                    directAmountsMap[field.groupId] = newVal.filter { it.isDigit() || it == '.' }
                } else {
                    quantitiesMap.getOrPut(field.groupId) { mutableStateMapOf() }[field.denomination!!] = newVal.filter { it.isDigit() }
                }
            },
            onNextCell = if (index < fieldsList.size - 1) { { showField(index + 1) } } else null,
            onPrevCell = if (index > 0) { { showField(index - 1) } } else null,
            onDone = { numpadController.hide() }
        )
    }

    // Grand total calculation
    var grandTotal = 0.0
    activeGroupsList.forEach { grp ->
        if (grp.type == SalesGroupType.DIRECT_ENTRY) {
            val amt = (directAmountsMap[grp.id] ?: "").toDoubleOrNull() ?: 0.0
            grandTotal += amt
        } else {
            val denomMap = quantitiesMap[grp.id] ?: emptyMap()
            val rows = grp.activeRows.ifEmpty { grp.rows }
            rows.forEach { row ->
                val qty = (denomMap[row.denomination] ?: "").toDoubleOrNull() ?: 0.0
                grandTotal += qty * row.denomination.toDouble()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.92f)
                .padding(2.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header (Compact)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(30.dp)
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "آلة حاسبة المبيعات الإجمالية",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            )
                            Text(
                                text = "حساب سريع لمجموعات المبيعات بدون تمرير",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.sp)
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                quantitiesMap.clear()
                                directAmountsMap.clear()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("تصفير الكل", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Grand Total Banner (Compact)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإجمالي المحسوب لكافة المجموعات:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 12.sp)
                        )
                        Text(
                            text = AccountingFormatter.formatMoney(grandTotal) + " " + AccountingFormatter.mainCurrencyCode,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                        )
                    }
                }

                // Grid layout fitting all cards dynamically on screen without scroll
                val columnsCount = if (activeGroupsList.size > 2) 2 else 1
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    userScrollEnabled = false // No scrolling required
                ) {
                    items(activeGroupsList, key = { it.id }) { group ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Group Header & Total
                                val groupTotal = if (group.type == SalesGroupType.DIRECT_ENTRY) {
                                    (directAmountsMap[group.id] ?: "").toDoubleOrNull() ?: 0.0
                                } else {
                                    val denomMap = quantitiesMap[group.id] ?: emptyMap()
                                    var sum = 0.0
                                    val rows = group.activeRows.ifEmpty { group.rows }
                                    rows.forEach { r ->
                                        val q = (denomMap[r.denomination] ?: "").toDoubleOrNull() ?: 0.0
                                        sum += q * r.denomination.toDouble()
                                    }
                                    sum
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = group.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                    )
                                    Text(
                                        text = AccountingFormatter.formatMoney(groupTotal),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp)
                                    )
                                }

                                HorizontalDivider(thickness = 0.4.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                if (group.type == SalesGroupType.DIRECT_ENTRY) {
                                    val currentAmt = directAmountsMap[group.id] ?: ""
                                    val fieldIdx = fieldsList.indexOfFirst { f -> f.groupId == group.id && f.isDirectEntry }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "المبلغ الإجمالي:",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        )
                                        AppNumberField(
                                            value = currentAmt,
                                            onValueChange = { newVal ->
                                                directAmountsMap[group.id] = newVal.filter { it.isDigit() || it == '.' }
                                            },
                                            placeholder = { Text("المبلغ", fontSize = 10.sp) },
                                            useCustomNumpadOnly = true,
                                            targetTitle = group.name,
                                            targetSubtitle = "إدخال مباشر",
                                            onNextCell = if (fieldIdx >= 0 && fieldIdx < fieldsList.size - 1) { { showField(fieldIdx + 1) } } else null,
                                            onPrevCell = if (fieldIdx > 0) { { showField(fieldIdx - 1) } } else null,
                                            textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                            modifier = Modifier
                                                .width(110.dp)
                                                .defaultMinSize(minHeight = 32.dp)
                                        )
                                    }
                                } else {
                                    // Denominations rows
                                    val denomMap = quantitiesMap.getOrPut(group.id) { mutableStateMapOf() }
                                    val rows = group.activeRows.ifEmpty { group.rows }

                                    rows.forEach { row ->
                                        val currentQty = denomMap[row.denomination] ?: ""
                                        val fieldIdx = fieldsList.indexOfFirst { f -> f.groupId == group.id && f.denomination == row.denomination }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "فئة ${row.denomination}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                modifier = Modifier.width(60.dp)
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                // Minus button (-)
                                                IconButton(
                                                    onClick = {
                                                        val q = (currentQty.toDoubleOrNull() ?: 0.0).toInt()
                                                        val newQ = (q - 1).coerceAtLeast(0)
                                                        denomMap[row.denomination] = if (newQ == 0) "" else newQ.toString()
                                                    },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "نقصان", modifier = Modifier.size(12.dp))
                                                }

                                                // App Number Field with Custom Numpad & Navigation
                                                AppNumberField(
                                                    value = currentQty,
                                                    onValueChange = { newVal ->
                                                        val filtered = newVal.filter { it.isDigit() }
                                                        val qty = filtered.toIntOrNull() ?: 0
                                                        if (qty <= row.remaining) {
                                                            denomMap[row.denomination] = filtered
                                                        }
                                                    },
                                                    placeholder = { Text("الكمية", fontSize = 9.sp) },
                                                    useCustomNumpadOnly = true,
                                                    targetTitle = "${group.name} - فئة ${row.denomination}",
                                                    targetSubtitle = "إدخال الكمية",
                                                    onNextCell = if (fieldIdx >= 0 && fieldIdx < fieldsList.size - 1) { { showField(fieldIdx + 1) } } else null,
                                                    onPrevCell = if (fieldIdx > 0) { { showField(fieldIdx - 1) } } else null,
                                                    textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                                    modifier = Modifier
                                                        .width(75.dp)
                                                        .defaultMinSize(minHeight = 32.dp)
                                                )

                                                // Plus button (+)
                                                IconButton(
                                                    onClick = {
                                                        val q = (currentQty.toDoubleOrNull() ?: 0.0).toInt()
                                                        val newQ = q + 1
                                                        if (newQ <= row.remaining) {
                                                            denomMap[row.denomination] = newQ.toString()
                                                        }
                                                    },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(12.dp))
                                                }
                                            }

                                            val lineTotal = (currentQty.toDoubleOrNull() ?: 0.0) * row.denomination.toDouble()
                                            Text(
                                                text = AccountingFormatter.formatMoney(lineTotal),
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.sp),
                                                modifier = Modifier.width(50.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Actions Footer (Compact)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Text("إلغاء", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val resultMap = mutableMapOf<String, Map<Int, String>>()
                            quantitiesMap.forEach { (gId, dMap) ->
                                resultMap[gId] = dMap.toMap()
                            }
                            onApplyQuantities(resultMap, directAmountsMap.toMap())
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اعتماد وتطبيق", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
