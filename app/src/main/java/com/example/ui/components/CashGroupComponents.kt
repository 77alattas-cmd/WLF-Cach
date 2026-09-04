package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AccountingFormatter
import com.example.ui.model.AppCurrency
import com.example.ui.model.CashBoxGroupUiState
import com.example.ui.model.CashDenomRowUiState
import com.example.ui.model.CashDirectEntryItem
import com.example.ui.model.CashGroupType
import com.example.ui.model.CASH_GROUP_CURRENCIES_ID
import com.example.ui.model.DEFAULT_CASH_DENOMINATIONS

/**
 * Modern Cash Box Groups Tab Bar
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CashGroupTabBar(
    groups: List<CashBoxGroupUiState>,
    selectedGroupId: String,
    exchangeRate: Double,
    onSelectGroup: (String) -> Unit,
    onToggleGroupEnabled: (String) -> Unit,
    onAddGroupClick: () -> Unit,
    onManageGroupsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "وبنود الصندوق",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "تضاف مبالغ المباشرة لإجمالي الصندوق",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = onManageGroupsClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_manage_cash_groups")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "إدارة ",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "إدارة",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onAddGroupClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_add_cash_group")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة صندوق",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "جديدة",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val visibleCashGroups = groups
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                visibleCashGroups.forEach { group ->
                    val isSelected = group.id == selectedGroupId
                    CashGroupTabChip(
                        group = group,
                        isSelected = isSelected,
                        exchangeRate = exchangeRate,
                        onClick = { onSelectGroup(group.id) },
                        onToggleEnabled = { onToggleGroupEnabled(group.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CashGroupTabChip(
    group: CashBoxGroupUiState,
    isSelected: Boolean,
    exchangeRate: Double,
    onClick: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected && group.isEnabled -> MaterialTheme.colorScheme.primaryContainer
            isSelected && !group.isEnabled -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
            !group.isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "cash_tab_color"
    )

    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier.testTag("cash_group_tab_${group.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (group.type == CashGroupType.DENOMINATIONS) {
                    Icons.Default.ConfirmationNumber
                } else {
                    Icons.Default.Payments
                },
                contentDescription = null,
                tint = if (group.isEnabled) {
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (group.isEnabled) {
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    )
                    if (!group.isEnabled) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(معطل)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Text(
                    text = if (group.isEnabled) {
                        AccountingFormatter.formatYer(group.getTotalYer(exchangeRate))
                    } else {
                        "غير محتسب"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = if (group.isEnabled) {
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onToggleEnabled,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (group.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (group.isEnabled) "تعطيل" else "تنشيط",
                    tint = if (group.isEnabled) {
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Cash Denominations Table (فئات النقد بالصندوق)
 */
@Composable
fun CashDenomGroupTable(
    group: CashBoxGroupUiState,
    onCountChange: (denomination: Int, countStr: String) -> Unit,
    onAdjustCount: (denomination: Int, delta: Int) -> Unit,
    onNotesChange: (denomination: Int, notes: String) -> Unit,
    onToggleEnabled: () -> Unit,
    onOpenCalculator: ((denomination: Int) -> Unit)? = null,
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
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
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "جرد فئات الأوراق النقدية بالصندوق",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "الإجمالي: ${AccountingFormatter.formatYer(group.denomRows.sumOf { it.total })}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))

            // Denomination Rows
            group.denomRows.forEachIndexed { index, row ->
                CashDenomRowItem(
                    row = row,
                    onCountChange = { onCountChange(row.denomination, it) },
                    onAdjustCount = { onAdjustCount(row.denomination, it) },
                    onOpenCalculator = { onOpenCalculator?.invoke(row.denomination) },
                    isReadOnly = isReadOnly,
                    onNextRow = if (index < group.denomRows.size - 1) {
                        { onCountChange(group.denomRows[index + 1].denomination, group.denomRows[index + 1].countInput) }
                    } else null,
                    onPrevRow = if (index > 0) {
                        { onCountChange(group.denomRows[index - 1].denomination, group.denomRows[index - 1].countInput) }
                    } else null
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CashDenomRowItem(
    row: CashDenomRowUiState,
    onCountChange: (String) -> Unit,
    onAdjustCount: (Int) -> Unit,
    onOpenCalculator: () -> Unit,
    isReadOnly: Boolean = false,
    onNextRow: (() -> Unit)? = null,
    onPrevRow: (() -> Unit)? = null
) {
    val isCompact = com.example.ui.LocalCompactMode.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val numpad = com.example.ui.components.LocalNumpadController.current

    // Trigger bringIntoView when row is focused or numpad visibility changes
    // This handles the user request to "move screen up" to show target row
    androidx.compose.runtime.LaunchedEffect(numpad.isVisible) {
        if (numpad.isVisible) {
            // We can't easily check focus here without passing focused state,
            // but usually this row is what triggers the numpad.
            // If the numpad is visible, we try to keep this row in view.
            kotlinx.coroutines.delay(150)
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {}
        }
    }

    Surface(
        shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Denomination Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.width(if (isCompact) 64.dp else 76.dp)
            ) {
                Text(
                    text = "فئة ${row.denomination}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontSize = if (isCompact) 11.sp else 12.sp
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Quick +/- controls and Count input
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = { onAdjustCount(-1) },
                    enabled = !isReadOnly,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "إنقاص", modifier = Modifier.size(14.dp))
                }

                com.example.ui.components.AppNumberField(
                    value = row.countInput,
                    onValueChange = onCountChange,
                    enabled = !isReadOnly,
                    placeholder = { Text("0", textAlign = TextAlign.Center) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    targetTitle = "فئة ${row.denomination}",
                    targetSubtitle = "أدخل عدد الأوراق النقدية",
                    onNextRow = onNextRow,
                    onPrevRow = onPrevRow,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = if (isCompact) 13.sp else 14.sp
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .width(if (isCompact) 64.dp else 72.dp)
                        .height(if (isCompact) 32.dp else 38.dp)
                )

                IconButton(
                    onClick = { onAdjustCount(1) },
                    enabled = !isReadOnly,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(14.dp))
                }

                IconButton(
                    onClick = onOpenCalculator,
                    enabled = !isReadOnly,
                    modifier = Modifier.size(if (isCompact) 28.dp else 32.dp)
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = "آلة حاسبة",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Total amount for this denomination
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = AccountingFormatter.formatMoney(row.total.toDouble()),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (row.total > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = "${row.count} ورقة",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

/**
 * Direct Cash Group Table (بنود وحوالات الصندوق)
 */
@Composable
fun CashDirectGroupTable(
    group: CashBoxGroupUiState,
    exchangeRate: Double,
    onAddEntry: (title: String, amount: String, currencyCode: String, isSar: Boolean, customExchangeRate: Double?, notes: String, deductFromCash: Boolean) -> Unit,
    onUpdateEntry: (itemId: String, title: String, amount: String, currencyCode: String, isSar: Boolean, customExchangeRate: Double?, notes: String, deductFromCash: Boolean) -> Unit,
    onRemoveEntry: (itemId: String) -> Unit,
    onToggleEnabled: () -> Unit,
    onOpenCalculator: ((itemId: String?) -> Unit)? = null,
    onMergeEntries: (() -> Unit)? = null,
    onResetGroup: (() -> Unit)? = null,
    isReadOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<CashDirectEntryItem?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog && onResetGroup != null) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("تأكيد تصفير المجموعة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من مسح كافة بنود (${group.name})؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetGroup()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تأكيد التصفير")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    val isForeignGroup = group.id == CASH_GROUP_CURRENCIES_ID || group.name.contains("عملات") || group.name.contains("أجنبية")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "بنود وحوالات ومبالغ نقدية مضافة للصندوق",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        enabled = !isReadOnly,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة بند", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total summary banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
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
                        text = "إجمالي بنود :",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = AccountingFormatter.formatYer(group.getTotalYer(exchangeRate)),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (group.directEntries.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "لا توجد بنود مضافة بعد. اضغط على \"إضافة بند\" لإضافة حوالة أو مبلغ نقدي.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                group.directEntries.forEach { entry ->
                    CashDirectEntryRow(
                        entry = entry,
                        exchangeRate = exchangeRate,
                        onEdit = { editingItem = entry },
                        onDelete = { onRemoveEntry(entry.id) },
                        onOpenCalculator = { onOpenCalculator?.invoke(entry.id) },
                        isReadOnly = isReadOnly
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عدد البنود: ${group.directEntries.size}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (onMergeEntries != null && group.directEntries.size > 1) {
                            OutlinedButton(
                                onClick = onMergeEntries,
                                enabled = !isReadOnly,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("دمج البنود", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (onResetGroup != null) {
                            FilledTonalButton(
                                onClick = { showResetDialog = true },
                                enabled = !isReadOnly,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تصفير البنود", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Entry Dialog
    if (showAddDialog) {
        CashDirectEntryDialog(
            title = "إضافة بند / عملة جديدة",
            initialTitle = "",
            initialAmount = "",
            initialCurrencyCode = if (isForeignGroup) "SAR" else "YER",
            initialIsSar = isForeignGroup,
            initialCustomExchangeRate = null,
            initialNotes = "",
            exchangeRate = exchangeRate,
            allowLocalCurrency = !isForeignGroup,
            showDeductFromCashOption = isForeignGroup,
            initialDeductFromCash = true,
            onConfirm = { t, a, currencyCode, isSar, customRate, notes, deductFromCash ->
                onAddEntry(t, a, currencyCode, isSar, customRate, notes, deductFromCash)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Edit Entry Dialog
    editingItem?.let { entry ->
        CashDirectEntryDialog(
            title = "تعديل البند / العملة",
            initialTitle = entry.title,
            initialAmount = entry.amountInput,
            initialCurrencyCode = entry.currencyCode,
            initialIsSar = entry.isSar,
            initialCustomExchangeRate = entry.customExchangeRate,
            initialNotes = entry.notes,
            exchangeRate = exchangeRate,
            allowLocalCurrency = !isForeignGroup,
            showDeductFromCashOption = isForeignGroup,
            initialDeductFromCash = entry.deductFromCash,
            onConfirm = { t, a, currencyCode, isSar, customRate, notes, deductFromCash ->
                onUpdateEntry(entry.id, t, a, currencyCode, isSar, customRate, notes, deductFromCash)
                editingItem = null
            },
            onDismiss = { editingItem = null }
        )
    }
}

@Composable
private fun CashDirectEntryRow(
    entry: CashDirectEntryItem,
    exchangeRate: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenCalculator: () -> Unit,
    isReadOnly: Boolean = false
) {
    val curr = entry.currency
    val yerEquivalent = entry.getTotalYer(exchangeRate)
    val isForeign = entry.isForeign

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Line 1: Title & Badges on right, Actions on left
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = entry.title.ifBlank { "بند بدون عنوان" },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isForeign) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${curr.flag} ${curr.symbol}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isForeign) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    if (entry.deductFromCash) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "مخصوم 🔻",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenCalculator, enabled = !isReadOnly, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Calculate, contentDescription = "آلة حاسبة", modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onEdit, enabled = !isReadOnly, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, enabled = !isReadOnly, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Line 2: Details / Equiv / Notes on right, Amount on left
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isForeign) {
                        val rateUsed = entry.effectiveRate ?: if (curr == AppCurrency.SAR) exchangeRate else curr.defaultRateYer
                        val isCustom = entry.effectiveRate != null
                        Text(
                            text = "يعادل: ${AccountingFormatter.formatYer(yerEquivalent)}" + if (isCustom) " (صرف: $rateUsed)" else "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    if (entry.notes.isNotBlank()) {
                        Text(
                            text = entry.notes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = "${AccountingFormatter.formatNumber(entry.amount)} ${curr.symbol}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

/**
 * Add / Edit Cash Direct Entry Dialog with Multi-Currency support
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CashDirectEntryDialog(
    title: String,
    initialTitle: String,
    initialAmount: String,
    initialCurrencyCode: String = "YER",
    initialIsSar: Boolean = false,
    initialCustomExchangeRate: Double?,
    initialNotes: String,
    exchangeRate: Double,
    allowLocalCurrency: Boolean = true,
    showDeductFromCashOption: Boolean = false,
    initialDeductFromCash: Boolean = false,
    onConfirm: (title: String, amount: String, currencyCode: String, isSar: Boolean, customExchangeRate: Double?, notes: String, deductFromCash: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var itemTitle by remember { mutableStateOf(initialTitle) }
    var itemAmount by remember { mutableStateOf(initialAmount) }
    var deductFromCash by remember { mutableStateOf(initialDeductFromCash) }
    var showOptionalDetails by remember { mutableStateOf(false) }
    val mainCode = com.example.ui.model.AccountingFormatter.mainCurrencyCode
    var selectedCurrency by remember {
        mutableStateOf(
            if (!allowLocalCurrency && (initialCurrencyCode == mainCode || initialCurrencyCode == "YER" || !initialIsSar)) {
                AppCurrency.entries.firstOrNull { it.code != mainCode } ?: AppCurrency.SAR
            } else if (initialIsSar && initialCurrencyCode == "YER") AppCurrency.SAR
            else AppCurrency.fromCode(initialCurrencyCode)
        )
    }
    var notes by remember { mutableStateOf(initialNotes) }
    var showCalcDialog by remember { mutableStateOf(false) }
    var customRateInput by remember {
        mutableStateOf(
            initialCustomExchangeRate?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: ""
        )
    }
    var useCustomRate by remember {
        mutableStateOf(initialCustomExchangeRate != null && initialCustomExchangeRate > 0)
    }

    val defaultRateForCurr = if (selectedCurrency == AppCurrency.SAR) exchangeRate else selectedCurrency.defaultRateYer
    val effectiveRate = if (useCustomRate && customRateInput.toDoubleOrNull() != null) {
        customRateInput.toDoubleOrNull()!!
    } else defaultRateForCurr

    val amountDouble = itemAmount.toDoubleOrNull() ?: 0.0
    val equivalentYer = if (selectedCurrency != AppCurrency.YER) amountDouble * effectiveRate else amountDouble

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp))
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Display Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "المبلغ (${selectedCurrency.arabicName} - ${selectedCurrency.symbol})",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (itemAmount.isBlank()) "0 ${selectedCurrency.symbol}" else "${AccountingFormatter.formatMoney(amountDouble)} ${selectedCurrency.symbol}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (itemAmount.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp
                            )
                        )
                        if (selectedCurrency != AppCurrency.YER && amountDouble > 0) {
                            Text(
                                text = "يعادل: ${AccountingFormatter.formatYer(equivalentYer)} (بسعر: $effectiveRate)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Currency selector
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val availableCurrencies = if (allowLocalCurrency) AppCurrency.entries.toList() else AppCurrency.entries.filter { it.code != mainCode }
                    items(availableCurrencies) { curr ->
                        FilterChip(
                            selected = selectedCurrency == curr,
                            onClick = {
                                selectedCurrency = curr
                                if (!useCustomRate) {
                                    val defRate = if (curr == AppCurrency.SAR) exchangeRate else curr.defaultRateYer
                                    customRateInput = if (defRate % 1.0 == 0.0) defRate.toLong().toString() else defRate.toString()
                                }
                            },
                            label = {
                                Text("${curr.flag} ${curr.arabicName}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                            },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                // Quick Presets Row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = if (selectedCurrency == AppCurrency.YER) {
                        listOf(1000L, 2000L, 5000L, 10000L, 20000L, 50000L)
                    } else {
                        listOf(10L, 20L, 50L, 100L, 200L, 500L)
                    }
                    presets.forEach { preset ->
                        Surface(
                            onClick = {
                                val current = itemAmount.toLongOrNull() ?: 0L
                                itemAmount = (current + preset).toString()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "+${AccountingFormatter.formatNumber(preset)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Direct On-screen Keypad (1-9, C, 0, ⌫, 00)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val keyRows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "⌫"),
                        listOf("00", "000", ".")
                    )

                    keyRows.forEach { rowKeys ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowKeys.forEach { key ->
                                Button(
                                    onClick = {
                                        when (key) {
                                            "C" -> itemAmount = ""
                                            "⌫" -> if (itemAmount.isNotEmpty()) itemAmount = itemAmount.dropLast(1)
                                            "." -> if (!itemAmount.contains(".")) itemAmount = if (itemAmount.isEmpty()) "0." else "$itemAmount."
                                            else -> {
                                                if (itemAmount == "0" && key != "00" && key != "000") itemAmount = key
                                                else if (itemAmount.length < 12) itemAmount += key
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (key) {
                                            "C" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                                            "⌫" -> MaterialTheme.colorScheme.surfaceVariant
                                            "00", "000" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                        },
                                        contentColor = when (key) {
                                            "C" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    ),
                                    contentPadding = PaddingValues(vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                ) {
                                    if (key == "⌫") {
                                        Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(key, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Checkbox toggle for optional fields (Notes & Statement) - Hidden by default
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOptionalDetails = !showOptionalDetails }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showOptionalDetails,
                        onCheckedChange = { showOptionalDetails = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "إضافة بيان وملاحظات إضافية (اختياري)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                }

                if (showOptionalDetails) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = itemTitle,
                            onValueChange = { itemTitle = it },
                            label = { Text("بيان أو عنوان البند (اختياري)", fontSize = 11.sp) },
                            placeholder = { Text("مثال: سداد حساب، تحويل، صرف...", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات (اختياري)", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (selectedCurrency != AppCurrency.YER) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("سعر صرف مخصص لهذا البند", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                                Switch(
                                    checked = useCustomRate,
                                    onCheckedChange = {
                                        useCustomRate = it
                                        if (it && customRateInput.isBlank()) {
                                            val defRate = if (selectedCurrency == AppCurrency.SAR) exchangeRate else selectedCurrency.defaultRateYer
                                            customRateInput = if (defRate % 1.0 == 0.0) defRate.toLong().toString() else defRate.toString()
                                        }
                                    }
                                )
                            }

                            if (useCustomRate) {
                                OutlinedTextField(
                                    value = customRateInput,
                                    onValueChange = { customRateInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                    label = { Text("سعر الصرف المخصص", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (showDeductFromCashOption) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { deductFromCash = !deductFromCash }
                                    .background(
                                        if (deductFromCash) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = deductFromCash,
                                    onCheckedChange = { deductFromCash = it }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "الخصم من النقد",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                    Text(
                                        text = "يخصم المبلغ من النقد مباشرة بعد الصرف.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.5.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemAmount.isNotBlank() || itemTitle.isNotBlank()) {
                        val finalCustomRate = if (useCustomRate) customRateInput.toDoubleOrNull() else null
                        val finalTitle = itemTitle.ifBlank { "بند نقدي (${selectedCurrency.arabicName})" }
                        onConfirm(
                            finalTitle,
                            itemAmount,
                            selectedCurrency.code,
                            selectedCurrency == AppCurrency.SAR,
                            finalCustomRate,
                            notes,
                            deductFromCash
                        )
                    }
                },
                enabled = itemAmount.isNotBlank() && (itemAmount.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * Dialog to add a new cash box group
 */
@Composable
fun AddCashGroupDialog(
    onAddGroup: (name: String, type: CashGroupType) -> Unit,
    onDismiss: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CashGroupType.DENOMINATIONS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إضافة جديدة لقسم الصندوق", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("اسم ال(مثال: نقدية فرعية، حوالات واردة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("نوع :", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                // Type 1: Denominations
                Surface(
                    onClick = { selectedType = CashGroupType.DENOMINATIONS },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedType == CashGroupType.DENOMINATIONS) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (selectedType == CashGroupType.DENOMINATIONS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == CashGroupType.DENOMINATIONS,
                            onClick = { selectedType = CashGroupType.DENOMINATIONS }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("فئات نقدية (جرد ورقي)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("جدول فئات نقدية (500، 1000، 2000، 5000) مع عدد الأوراق", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Type 2: Direct Entry
                Surface(
                    onClick = { selectedType = CashGroupType.DIRECT_ENTRY },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedType == CashGroupType.DIRECT_ENTRY) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (selectedType == CashGroupType.DIRECT_ENTRY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == CashGroupType.DIRECT_ENTRY,
                            onClick = { selectedType = CashGroupType.DIRECT_ENTRY }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("بنود وحوالات مباشرة", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("إضافة بنود مبالغ وحوالات مباشرة بالريال اليمني أو السعودي", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        onAddGroup(groupName, selectedType)
                    }
                },
                enabled = groupName.isNotBlank()
            ) {
                Text("إضافة ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * Manage Cash Groups Dialog
 */
@Composable
fun ManageCashGroupsDialog(
    groups: List<CashBoxGroupUiState>,
    onToggleEnabled: (String) -> Unit,
    onMoveLeft: (String) -> Unit,
    onMoveRight: (String) -> Unit,
    onRenameGroup: (String, String) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onAddGroupClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var renamingGroupId by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteConfirmGroupId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("تنظيم الصندوق", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                groups.forEachIndexed { index, group ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = group.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (group.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                        )
                                    )
                                    Text(
                                        text = if (group.type == CashGroupType.DENOMINATIONS) "فئات نقدية" else "بنود مباشرة",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Move buttons
                                    IconButton(
                                        onClick = { onMoveLeft(group.id) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowForward, contentDescription = "تقديم", modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { onMoveRight(group.id) },
                                        enabled = index < groups.size - 1,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "تأخير", modifier = Modifier.size(16.dp))
                                    }

                                    // Rename
                                    IconButton(
                                        onClick = {
                                            renamingGroupId = group.id
                                            renameText = group.name
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل الاسم", modifier = Modifier.size(16.dp))
                                    }

                                    // Delete
                                    if (!group.isDefault) {
                                        IconButton(
                                            onClick = { deleteConfirmGroupId = group.id },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(4.dp))

                            // Activation button for Cash Box group
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = group.isEnabled,
                                    onClick = { onToggleEnabled(group.id) },
                                    label = { Text(if (group.isEnabled) "مجموعة مفعلة ✓" else "مجموعة معطلة ✕", fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(
                                            if (group.isEnabled) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (group.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    },
                                    modifier = Modifier.height(30.dp)
                                )

                                Text(
                                    text = if (group.isEnabled) "تظهر في الصندوق" else "مخفية من الصندوق",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (group.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )

    // Rename dialog
    renamingGroupId?.let { gId ->
        AlertDialog(
            onDismissRequest = { renamingGroupId = null },
            title = { Text("تعديل اسم ") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("الاسم الجديد") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onRenameGroup(gId, renameText)
                    renamingGroupId = null
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingGroupId = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Delete confirm dialog
    deleteConfirmGroupId?.let { gId ->
        AlertDialog(
            onDismissRequest = { deleteConfirmGroupId = null },
            title = { Text("تأكيد حذف ") },
            text = { Text("هل أنت متأكد من رغبتك في حذف هذه المن قسم الصندوق؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGroup(gId)
                        deleteConfirmGroupId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmGroupId = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun AddCashGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: CashGroupType, isExcluded: Boolean) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CashGroupType.DIRECT_ENTRY) }
    var isExcludedFromBalance by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مجموعة جديدة للصندوق", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("اسم المجموعة (مثلاً: بنك، عهدة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("نوع المجموعة:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedType = CashGroupType.DIRECT_ENTRY }.fillMaxWidth()
                    ) {
                        RadioButton(selected = selectedType == CashGroupType.DIRECT_ENTRY, onClick = { selectedType = CashGroupType.DIRECT_ENTRY })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بنود مباشرة (قبض / إيداع)")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedType = CashGroupType.EXPENSES }.fillMaxWidth()
                    ) {
                        RadioButton(selected = selectedType == CashGroupType.EXPENSES, onClick = { selectedType = CashGroupType.EXPENSES })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مصروفات")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedType = CashGroupType.DEPOSITS }.fillMaxWidth()
                    ) {
                        RadioButton(selected = selectedType == CashGroupType.DEPOSITS, onClick = { selectedType = CashGroupType.DEPOSITS })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إيداعات")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Include in report toggle / checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExcludedFromBalance = !isExcludedFromBalance }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = !isExcludedFromBalance,
                        onCheckedChange = { isExcludedFromBalance = !it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("إضافة للتقرير (إدراج في حسابات وموازنة الصندوق)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text("عند إلغاء التحديد، تصبح المجموعة مستبعدة من التقرير العام وموازنة الصندوق", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        nameInput.ifBlank { "مجموعة جديدة" },
                        selectedType,
                        isExcludedFromBalance
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_add_cash_group")
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

