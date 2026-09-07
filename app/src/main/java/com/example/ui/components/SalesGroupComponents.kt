package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
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
import com.example.ui.model.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AccountingFormatter
import com.example.ui.model.DEFAULT_TICKET_CATEGORIES
import com.example.ui.model.DirectEntryItem
import com.example.ui.model.SalesGroupType
import com.example.ui.model.SalesGroupUiState

/**
 * Modern Group Tab Bar shown at the top of the Sales section
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalesGroupTabBar(
    groups: List<SalesGroupUiState>,
    selectedGroupId: String,
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
            // Tab Bar Top Actions & Title
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
                    Text(
                        text = "المبيعات",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Manage & Reorder Button
                    FilledTonalButton(
                        onClick = onManageGroupsClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("manage_groups_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "تنظيم ",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تنظيم",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Add Group Button
                    Button(
                        onClick = onAddGroupClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_group_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة",
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

            // Flow Tab Chips (Display all groups so disabled ones can be easily re-activated)
            val visibleGroups = groups
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                visibleGroups.forEach { group ->
                    val isSelected = group.id == selectedGroupId
                    GroupTabChip(
                        group = group,
                        isSelected = isSelected,
                        onClick = { onSelectGroup(group.id) },
                        onToggleEnabled = { onToggleGroupEnabled(group.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupTabChip(
    group: SalesGroupUiState,
    isSelected: Boolean,
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
        label = "tab_color"
    )

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier.testTag("group_tab_${group.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group Type Icon
            Icon(
                imageVector = if (group.type == SalesGroupType.DENOMINATIONS) {
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
                        AccountingFormatter.formatMoney(group.totalRevenue)
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

            // Activation Eye / Toggle icon
            IconButton(
                onClick = onToggleEnabled,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (group.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (group.isEnabled) "إخفاء وتعطيل الاحتساب" else "تنشيط واحتساب",
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
 * Display-only Direct Entry Component for group "صيني"
 * with compact row heights and embedded calculator.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DirectEntryGroupTable(
    group: SalesGroupUiState,
    onAddEntry: (title: String, amount: String, quantity: String, notes: String) -> Unit,
    onAddDeposit: ((title: String, amount: String, notes: String) -> Unit)? = null,
    onUpdateEntry: (itemId: String, title: String, amount: String, quantity: String, notes: String) -> Unit,
    onRemoveEntry: (itemId: String) -> Unit,
    onQuickAddAmount: (Double) -> Unit,
    onToggleEnabled: () -> Unit,
    onMergeEntries: (() -> Unit)? = null,
    onResetGroup: (() -> Unit)? = null,
    customColorState: CustomColorThemeState = CustomColorThemeState(),
    isReadOnlyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<DirectEntryItem?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog && onResetGroup != null) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("تأكيد تصفير المجموعة", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من مسح كافة بنود مبيعات (${group.name})؟") },
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

    val tableCardBg = customColorState.getColorOrNull(customColorState.tableCardBg) ?: MaterialTheme.colorScheme.surface
    val tableBorder = customColorState.getColorOrNull(customColorState.tableBorderColor) ?: MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (group.isEnabled) tableCardBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(
                1.dp,
                if (group.isEnabled) tableBorder else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(38.dp)
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
                                text = "${group.name}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = if (group.isEnabled) "إدخال مباشر بالآلة الحاسبة بدون تعقيد" else "المعطلة حالياً ولا تحتسب في الإجمالي",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (group.isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }


                }

                Spacer(modifier = Modifier.height(10.dp))



                Spacer(modifier = Modifier.height(10.dp))

                // Group Total Summary Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (group.isEnabled) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        if (group.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مجموع مبيعات ${group.name}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = AccountingFormatter.formatYer(group.totalRevenue),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (group.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "${group.directEntries.size} بنود مبيعات",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dedicated Group Entry Button with +/- controls
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showAddEntryDialog = true },
                            enabled = group.isEnabled,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("btn_group_entry_${group.name}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إدخال مبيعات ${group.name}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Table Header Legend (Compact)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "البيان / الوصف",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "المبلغ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.width(90.dp)
                        )
                        Text(
                            text = "الإجمالي",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                textAlign = TextAlign.End
                            ),
                            modifier = Modifier.width(80.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (group.directEntries.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "لا توجد مبيعات مسجلة في ${group.name}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "اضغط على زر (إدخال مبيعات ${group.name}) أعلاه للإضافة",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                } else {
                    group.directEntries.forEachIndexed { index, entry ->
                        CompactDirectEntryItemCard(
                            item = entry,
                            isEnabled = group.isEnabled,
                            onEdit = { editingEntry = entry },
                            onDelete = { onRemoveEntry(entry.id) }
                        )

                        if (index < group.directEntries.size - 1) {
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
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
                                    enabled = group.isEnabled,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("دمج البنود", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }


                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Add New Direct Entry
    if (showAddEntryDialog) {
        DirectGroupEntryDialog(
            groupName = group.name,
            initialItem = null,
            onDismiss = { showAddEntryDialog = false },
            onConfirm = { title, amt, notes ->
                onAddEntry(title, amt, "1", notes)
                showAddEntryDialog = false
            }
        )
    }

    // Modal Dialog to Edit Existing Entry
    editingEntry?.let { entry ->
        DirectGroupEntryDialog(
            groupName = group.name,
            initialItem = entry,
            onDismiss = { editingEntry = null },
            onConfirm = { title, amt, notes ->
                onUpdateEntry(entry.id, title, amt, "1", notes)
                editingEntry = null
            }
        )
    }
}

/**
 * Compact, low-height Display-Only Row for Direct Entry items
 */
@Composable
private fun CompactDirectEntryItemCard(
    item: DirectEntryItem,
    isEnabled: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onEdit)
            .testTag("entry_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title.ifBlank { "مبيعات مباشرة" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Amount
                Text(
                    text = AccountingFormatter.formatNumber(item.amount),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.width(90.dp)
                )

                // Total Amount & Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.width(95.dp)
                ) {
                    Text(
                        text = AccountingFormatter.formatYer(item.total),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDelete() }
                    )
                }
            }
        }
    }
}

/**
 * Safe Modal Dialog to Add or Edit Direct Entry Items with +/- Stepper and Quick Presets
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DirectGroupEntryDialog(
    groupName: String,
    initialItem: DirectEntryItem?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var amount by remember { mutableStateOf(initialItem?.amountInput ?: "") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var showOptionalDetails by remember { mutableStateOf(initialItem?.title?.isNotBlank() == true || initialItem?.notes?.isNotBlank() == true) }

    val isEditMode = initialItem != null
    val parsedAmount = amount.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Text(
                            text = "المجموعة: $groupName",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = if (isEditMode) "تعديل بند مبيعات" else "إدخال مبيعات مباشر",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    )
                }

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
                // Live Display Box
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
                            text = "المبلغ المدخل ($groupName)",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (amount.isBlank()) "0 ر.ي." else AccountingFormatter.formatYer(parsedAmount),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (amount.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                                fontSize = 22.sp
                            )
                        )
                    }
                }

                // Quick Presets Row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(1000L, 2000L, 5000L, 10000L, 20000L, 50000L).forEach { preset ->
                        Surface(
                            onClick = {
                                val current = amount.toLongOrNull() ?: 0L
                                amount = (current + preset).toString()
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
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Direct Numeric Keypad (1-9, C, 0, ⌫, 00)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val keyRows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "⌫"),
                        listOf("00", "000")
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
                                            "C" -> amount = ""
                                            "⌫" -> if (amount.isNotEmpty()) amount = amount.dropLast(1)
                                            else -> {
                                                if (amount == "0" && key != "00" && key != "000") amount = key
                                                else if (amount.length < 11) amount += key
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
                                    contentPadding = PaddingValues(vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                ) {
                                    if (key == "⌫") {
                                        Icon(Icons.Default.Clear, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(key, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Optional Details Toggle with Checkbox (الملاحظات والبيان اختياريان ومعطلان افتراضياً)
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
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("البيان / الوصف (اختياري)", fontSize = 11.sp) },
                            placeholder = { Text("مبيعات $groupName", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظة (اختياري)", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0) {
                        onConfirm(
                            title.ifBlank { "مبيعات $groupName" },
                            amount,
                            notes
                        )
                    }
                },
                enabled = amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isEditMode) "حفظ التعديل" else "إضافة للمبيعات")
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
 * Dialog to add a new group (either Denominations like Sanad, or Direct Entry like Chini)
 * With pre-selected categories for denomination groups that can be customized.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirmWithDetails: (
        name: String,
        type: SalesGroupType,
        denominations: List<Int>,
        isExcluded: Boolean,
        givenLabel: String,
        addedLabel: String,
        remainingLabel: String,
        formula: CalculationFormula
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SalesGroupType.DENOMINATIONS) }
    var isExcludedFromBalance by remember { mutableStateOf(false) }

    // Custom Fields Configuration
    var givenLabelInput by remember { mutableStateOf("المعطى") }
    var addedLabelInput by remember { mutableStateOf("إضافة") }
    var remainingLabelInput by remember { mutableStateOf("المتبقي") }
    var selectedFormula by remember { mutableStateOf(CalculationFormula.TICKET_STANDARD) }

    // Pre-selected categories by default: 500, 1000, 1500, 2000, 3000, 4000, 5000
    var selectedDenominations: List<Int> by remember { mutableStateOf(DEFAULT_TICKET_CATEGORIES) }
    var customDenomInput by remember { mutableStateOf("") }
    var customDenomError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مبيعات جديدة",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "اسم :",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("مثال: سند 2، صيني كاش، بطائق شحن...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_group_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "نوع الوطريقة الاحتساب:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Option 1: Denominations (سند)
                Surface(
                    onClick = { selectedType = SalesGroupType.DENOMINATIONS },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedType == SalesGroupType.DENOMINATIONS) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        if (selectedType == SalesGroupType.DENOMINATIONS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == SalesGroupType.DENOMINATIONS,
                            onClick = { selectedType = SalesGroupType.DENOMINATIONS }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "جدول حسب الفئة (مثل سند)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "حساب مبيعات التذاكر بالمعادلة القياسية: (معطى + إضافة - متبقي) × الفئة",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 2: Direct Entry (صيني)
                Surface(
                    onClick = { selectedType = SalesGroupType.DIRECT_ENTRY },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedType == SalesGroupType.DIRECT_ENTRY) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        if (selectedType == SalesGroupType.DIRECT_ENTRY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == SalesGroupType.DIRECT_ENTRY,
                            onClick = { selectedType = SalesGroupType.DIRECT_ENTRY }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "إدخال مباشر بدون فئات (مثل صيني)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "إدخال مبالغ وبنود مبيعات مباشرة مع الكميات والملاحظات",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 3: Custom Fields & Calculation Formula
                Surface(
                    onClick = { selectedType = SalesGroupType.CUSTOM_FIELDS },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedType == SalesGroupType.CUSTOM_FIELDS) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        if (selectedType == SalesGroupType.CUSTOM_FIELDS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == SalesGroupType.CUSTOM_FIELDS,
                            onClick = { selectedType = SalesGroupType.CUSTOM_FIELDS }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "حقول مخصصة وطريقة احتساب مخصصة",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "تخصيص مسميات حقول الإدخال واختيار معادلة الاحتساب المناسبة",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Custom Fields & Formulas configuration box
                if (selectedType == SalesGroupType.CUSTOM_FIELDS) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "مسميات الحقول المخصصة للمجموعة:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = givenLabelInput,
                                onValueChange = { givenLabelInput = it },
                                label = { Text("مسمى الحقل الأول (افتراضي: المعطى/المستلم)") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = addedLabelInput,
                                onValueChange = { addedLabelInput = it },
                                label = { Text("مسمى الحقل الثاني (افتراضي: الإضافة/التعزيز)") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = remainingLabelInput,
                                onValueChange = { remainingLabelInput = it },
                                label = { Text("مسمى الحقل الثالث (افتراضي: المتبقي/الراجع)") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "طريقة الاحتساب الرياضية:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            CalculationFormula.values().forEach { form ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedFormula = form }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedFormula == form,
                                        onClick = { selectedFormula = form }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(text = form.label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(text = form.formulaText, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Selection & Customization Section (for DENOMINATIONS and CUSTOM_FIELDS)
                if (selectedType == SalesGroupType.DENOMINATIONS || selectedType == SalesGroupType.CUSTOM_FIELDS) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الفئات المحددة للمجموعة:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        TextButton(
                            onClick = {
                                selectedDenominations = DEFAULT_TICKET_CATEGORIES
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("استعادة الافتراضي", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Chips showing selected / available denominations
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        selectedDenominations.sorted().forEach { denom ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$denom ر.ي.",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                selectedDenominations = selectedDenominations.filter { it != denom }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "حذف الفئة",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedDenominations.isEmpty()) {
                        Text(
                            text = "يرجى إضافة فئة واحدة على الأقل",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input to add a new custom denomination
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        com.example.ui.components.AppNumberField(
                            value = customDenomInput,
                            onValueChange = {
                                customDenomInput = it.filter { ch -> ch.isDigit() }
                                customDenomError = null
                            },
                            placeholder = { Text("أدخل فئة جديدة (مثلاً: 2500)", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val parsed = customDenomInput.toIntOrNull()
                                if (parsed == null || parsed <= 0) {
                                    customDenomError = "أدخل قيمة صحيحة"
                                } else if (selectedDenominations.contains(parsed)) {
                                    customDenomError = "الفئة موجودة مسبقاً"
                                } else {
                                    selectedDenominations = (selectedDenominations + parsed).sorted()
                                    customDenomInput = ""
                                    customDenomError = null
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("إضافة", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (customDenomError != null) {
                        Text(
                            text = customDenomError ?: "",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // Exclude from balance toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExcludedFromBalance = !isExcludedFromBalance }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isExcludedFromBalance,
                        onCheckedChange = { isExcludedFromBalance = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("استبعاد من الموازنة اليومية", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text("تُعرض الوإجمالياتها في التقارير كحساب مستقل دون التأثير على عجز/فائض الصندوق", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val denoms = if (selectedType == SalesGroupType.DENOMINATIONS || selectedType == SalesGroupType.CUSTOM_FIELDS) {
                        selectedDenominations.ifEmpty { DEFAULT_TICKET_CATEGORIES }
                    } else emptyList()
                    onConfirmWithDetails(
                        name.ifBlank { "مبيعات جديدة" },
                        selectedType,
                        denoms,
                        isExcludedFromBalance,
                        givenLabelInput,
                        addedLabelInput,
                        remainingLabelInput,
                        selectedFormula
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_add_group_button")
            ) {
                Text("إنشاء ")
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

@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: SalesGroupType, denominations: List<Int>) -> Unit
) {
    AddGroupDialog(
        onDismiss = onDismiss,
        onConfirmWithDetails = { name, type, denoms, isExcluded, given, added, rem, form ->
            onConfirm(name, type, denoms)
        }
    )
}

/**
 * Dialog to Manage, Reorder, Enable/Disable, Rename, and Delete Groups
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageGroupsDialog(
    groups: List<SalesGroupUiState>,
    onToggleEnabled: (String) -> Unit,
    onToggleAddedField: (String) -> Unit = {},
    onToggleMergeAdded: (String) -> Unit = {},
    onToggleAddToReport: (String) -> Unit = {},
    onToggleAddToBalance: (String) -> Unit = {},
    onMoveLeft: (String) -> Unit,
    onMoveRight: (String) -> Unit,
    onRenameGroup: (String, String) -> Unit,
    onDeleteGroup: (String) -> Unit,
    onAddGroupClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var renamingGroupId by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تنظيم المجموعات والخيارات",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "خيارات كل مجموعة: التفعيل، الحقل الإضافي، دمج الإضافي، إضافة للتقرير، وإضافة للرصيد:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                groups.forEachIndexed { index, group ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (group.isEnabled) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (group.isEnabled) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        ),
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
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (group.type == SalesGroupType.DENOMINATIONS) Icons.Default.ConfirmationNumber else Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = if (group.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = group.name,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (group.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "(${group.type.label})",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                        Text(
                                            text = if (group.isEnabled) {
                                                "المجموع: ${AccountingFormatter.formatMoney(group.totalRevenue)}"
                                            } else {
                                                "غير محتسب في الإجمالي"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = if (group.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                            )
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Move Up / Left
                                    IconButton(
                                        onClick = { onMoveLeft(group.id) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "تحريك للأعلى",
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // Move Down / Right
                                    IconButton(
                                        onClick = { onMoveRight(group.id) },
                                        enabled = index < groups.size - 1,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "تحريك للأسفل",
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // Rename button
                                    IconButton(
                                        onClick = {
                                            renamingGroupId = group.id
                                            renameText = group.name
                                        },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "تعديل الاسم",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    // Delete custom group button
                                    if (!group.isDefault) {
                                        IconButton(
                                            onClick = { onDeleteGroup(group.id) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "حذف",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))

                            // Group Options Row (تفعيل، حقل إضافي، دمج إضافي، إضافة تقرير، إضافة رصيد)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 1. تفعيل
                                FilterChip(
                                    selected = group.isEnabled,
                                    onClick = { onToggleEnabled(group.id) },
                                    label = { Text(if (group.isEnabled) "مفعلة ✓" else "معطلة ✕", fontSize = 10.5.sp) },
                                    leadingIcon = {
                                        Icon(
                                            if (group.isEnabled) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = if (group.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    },
                                    modifier = Modifier.height(28.dp)
                                )

                                // 2. حقل إضافي
                                if (group.type == SalesGroupType.DENOMINATIONS || group.type == SalesGroupType.CUSTOM_FIELDS) {
                                    FilterChip(
                                        selected = group.isAddedFieldEnabled,
                                        onClick = { onToggleAddedField(group.id) },
                                        label = { Text("حقل إضافي", fontSize = 10.5.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                        },
                                        modifier = Modifier.height(28.dp)
                                    )

                                    // 3. دمج إضافي
                                    FilterChip(
                                        selected = group.mergeAddedWithGiven,
                                        onClick = { onToggleMergeAdded(group.id) },
                                        label = { Text("دمج إضافي", fontSize = 10.5.sp) },
                                        leadingIcon = {
                                            Icon(Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(13.dp))
                                        },
                                        modifier = Modifier.height(28.dp)
                                    )
                                }

                                // 4. إضافة تقرير
                                FilterChip(
                                    selected = group.addToReport,
                                    onClick = { onToggleAddToReport(group.id) },
                                    label = { Text("إضافة تقرير", fontSize = 10.5.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(13.dp))
                                    },
                                    modifier = Modifier.height(28.dp)
                                )

                                // 5. إضافة رصيد
                                FilterChip(
                                    selected = group.addToBalance,
                                    onClick = { onToggleAddToBalance(group.id) },
                                    label = { Text("إضافة رصيد", fontSize = 10.5.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(13.dp))
                                    },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onAddGroupClick()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة جديدة")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("تم")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )

    // Rename sub-dialog
    if (renamingGroupId != null) {
        AlertDialog(
            onDismissRequest = { renamingGroupId = null },
            title = { Text("تعديل اسم ") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("الاسم الجديد") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        renamingGroupId?.let { id -> onRenameGroup(id, renameText) }
                        renamingGroupId = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تغيير")
                }
            },
            dismissButton = {
                TextButton(onClick = { renamingGroupId = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
