package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.model.SalesGroupUiState
import com.example.ui.model.CashBoxGroupUiState
import com.example.ui.model.SalesGroupType

enum class ResetScopeType {
    GROUP_ALL,
    DETAILED_SELECTIVE,
    SPECIFIC_CATEGORY,
    SPECIFIC_FIELD,
    PHYSICAL_CASH_ONLY,
    REVENUE_ONLY,
    ALL_DAY_DATA
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResetScopeDialog(
    onDismiss: () -> Unit,
    onConfirmResetGroup: () -> Unit,
    onConfirmResetCash: () -> Unit = {},
    onConfirmResetRevenue: () -> Unit = {},
    onConfirmResetAll: () -> Unit = {},
    onConfirmResetCategory: (Int) -> Unit = {},
    onConfirmResetField: (String) -> Unit = {},
    onConfirmSelectiveReset: (
        groupId: String?,
        resetGiven: Boolean,
        resetAdded: Boolean,
        resetRemaining: Boolean,
        selectedDenoms: Set<Int>,
        selectedDirectIds: Set<String>,
        resetCash: Boolean,
        resetExpenses: Boolean,
        resetDeposits: Boolean
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    groupName: String = "المجموعة الحالية",
    groupId: String? = null,
    availableCategories: List<Int> = emptyList(),
    allSalesGroups: List<SalesGroupUiState> = emptyList(),
    allCashGroups: List<CashBoxGroupUiState> = emptyList(),
    isSalesSection: Boolean = true
) {
    var selectedScope by remember { mutableStateOf(ResetScopeType.DETAILED_SELECTIVE) }
    var targetGroupId by remember { mutableStateOf(groupId) }
    
    // Selective checkboxes
    var resetGiven by remember { mutableStateOf(true) }
    var resetAdded by remember { mutableStateOf(true) }
    var resetRemaining by remember { mutableStateOf(true) }
    var resetCashInBox by remember { mutableStateOf(!isSalesSection) }
    var resetExpenses by remember { mutableStateOf(false) }
    var resetDeposits by remember { mutableStateOf(false) }

    // Multi-selected denominations
    val currentGroup = allSalesGroups.find { it.id == targetGroupId }
    val groupDenoms = currentGroup?.rows?.map { it.denomination } ?: availableCategories
    var selectedDenomsSet by remember(targetGroupId, groupDenoms) {
        mutableStateOf(groupDenoms.toSet())
    }

    var selectedField by remember { mutableStateOf("REMAINING") }
    var selectedDenom by remember { mutableStateOf(availableCategories.firstOrNull() ?: 100) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .testTag("reset_scope_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "تصفير مخصص وتحديد الخانات",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "حدد المجموعات، الفئات، والخانات المراد تصفيرها",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Scope Selector Chips (التصفير التفصيلي، تصفير المجموعة الحالية، تصفير شامل)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedScope == ResetScopeType.DETAILED_SELECTIVE,
                        onClick = { selectedScope = ResetScopeType.DETAILED_SELECTIVE },
                        label = { Text("تصفير مفصل (مخصص)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = if (selectedScope == ResetScopeType.DETAILED_SELECTIVE) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedScope == ResetScopeType.ALL_DAY_DATA,
                        onClick = { selectedScope = ResetScopeType.ALL_DAY_DATA },
                        label = { Text("تصفير شامل للوردية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = if (selectedScope == ResetScopeType.ALL_DAY_DATA) {
                            { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(13.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (selectedScope == ResetScopeType.DETAILED_SELECTIVE) {
                    // 1. Group Selector
                    if (allSalesGroups.isNotEmpty()) {
                        Text(
                            text = "1. اختر المجموعة المستهدفة:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = targetGroupId == null,
                                onClick = { targetGroupId = null },
                                label = { Text("جميع المجموعات", fontSize = 10.5.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                            allSalesGroups.forEach { grp ->
                                FilterChip(
                                    selected = targetGroupId == grp.id,
                                    onClick = { targetGroupId = grp.id },
                                    label = { Text(grp.name, fontSize = 10.5.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }

                    // 2. Fields Checkboxes
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "2. حدد الخانات المراد تصفيرها:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { resetGiven = !resetGiven }
                                ) {
                                    Checkbox(checked = resetGiven, onCheckedChange = { resetGiven = it }, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("المعطى", fontSize = 11.5.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { resetAdded = !resetAdded }
                                ) {
                                    Checkbox(checked = resetAdded, onCheckedChange = { resetAdded = it }, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("الإضافي", fontSize = 11.5.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { resetRemaining = !resetRemaining }
                                ) {
                                    Checkbox(checked = resetRemaining, onCheckedChange = { resetRemaining = it }, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("المتبقي", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }

                    // 3. Categories / Denominations Checkboxes
                    if (groupDenoms.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "3. حدد فئات التذاكر للتصفير:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp
                                        )
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(
                                            onClick = { selectedDenomsSet = groupDenoms.toSet() },
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("تحديد الكل", fontSize = 10.sp)
                                        }
                                        TextButton(
                                            onClick = { selectedDenomsSet = emptySet() },
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("إلغاء", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    groupDenoms.forEach { denom ->
                                        val isChecked = selectedDenomsSet.contains(denom)
                                        FilterChip(
                                            selected = isChecked,
                                            onClick = {
                                                selectedDenomsSet = if (isChecked) {
                                                    selectedDenomsSet - denom
                                                } else {
                                                    selectedDenomsSet + denom
                                                }
                                            },
                                            label = { Text("فئة $denom", fontSize = 10.5.sp) },
                                            leadingIcon = if (isChecked) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                            } else null,
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Cash & Expenses Options
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "4. خيارات الصندوق الإضافية:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { resetCashInBox = !resetCashInBox }
                                ) {
                                    Checkbox(checked = resetCashInBox, onCheckedChange = { resetCashInBox = it }, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("نقد الصندوق", fontSize = 11.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { resetExpenses = !resetExpenses }
                                ) {
                                    Checkbox(checked = resetExpenses, onCheckedChange = { resetExpenses = it }, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("المصاريف", fontSize = 11.sp)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { resetDeposits = !resetDeposits }
                                ) {
                                    Checkbox(checked = resetDeposits, onCheckedChange = { resetDeposits = it }, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("الإيداعات", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else if (selectedScope == ResetScopeType.ALL_DAY_DATA) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                Text("تصفير الوردية بالكامل مع الرسوم والتأكيد", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            }
                            Text(
                                text = "سيتم فتح شاشة التأكيد البصري والرسوم المتحركة لأرشفة بيانات الوردية، وتصفير عدادات المبيعات والصندوق والمصروفات مع حفظ نسخة احتياطية آمنة تلقائياً.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (selectedScope == ResetScopeType.ALL_DAY_DATA) {
                                onConfirmResetAll()
                            } else {
                                onConfirmSelectiveReset(
                                    targetGroupId,
                                    resetGiven,
                                    resetAdded,
                                    resetRemaining,
                                    selectedDenomsSet,
                                    emptySet(),
                                    resetCashInBox,
                                    resetExpenses,
                                    resetDeposits
                                )
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedScope == ResetScopeType.ALL_DAY_DATA) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (selectedScope == ResetScopeType.ALL_DAY_DATA) "متابعة وتصفير الوردية ⚡" else "تنفيذ التصفير المحدد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
