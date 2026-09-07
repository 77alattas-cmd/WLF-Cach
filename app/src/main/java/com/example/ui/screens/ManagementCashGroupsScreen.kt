package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.reorderableVerticalItem
import com.example.ui.components.ReorderDragHandle
import com.example.ui.model.*
import com.example.ui.theme.vibrant3d
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.ui.viewmodel.TicketAccountingViewModel
import com.example.ui.components.AddCashGroupDialog
import com.example.ui.components.RenameGroupDialog

@Composable
fun ManagementCashGroupsScreen(
    viewModel: TicketAccountingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val listState = rememberLazyListState()

    var showAddCashGroupDialog by remember { mutableStateOf(false) }
    var showRenameGroupDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isCashGroupsReorderEnabled by remember { mutableStateOf(false) }

    if (showAddCashGroupDialog) {
        AddCashGroupDialog(
            onDismiss = { showAddCashGroupDialog = false },
            onConfirm = { name, type, isExcluded ->
                viewModel.createCashGroup(name, type, isExcluded)
                showAddCashGroupDialog = false
            }
        )
    }

    if (showRenameGroupDialog != null) {
        RenameGroupDialog(
            initialName = showRenameGroupDialog!!.second,
            onDismiss = { showRenameGroupDialog = null },
            onRename = { newName ->
                viewModel.updateCashGroupName(showRenameGroupDialog!!.first, newName)
                showRenameGroupDialog = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("management_cash_groups_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // FIXED TOP HEADER
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .vibrant3d(
                    shape = RoundedCornerShape(16.dp),
                    elevation = 4.dp,
                    isDark = isDark,
                    baseColor = MaterialTheme.colorScheme.surface
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(com.example.ui.viewmodel.AppScreen.HOME) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تنظيم مجموعات الصندوق",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "تخصيص الحسابات النقدية والمصاريف",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }

        // SCROLLABLE CONTENT LIST
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مجموعات الصندوق",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${uiState.cashGroups.count { it.isEnabled }} نشط من ${uiState.cashGroups.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.cashGroups.size > 1) {
                            FilterChip(
                                selected = isCashGroupsReorderEnabled,
                                onClick = { isCashGroupsReorderEnabled = !isCashGroupsReorderEnabled },
                                label = {
                                    Text(
                                        text = if (isCashGroupsReorderEnabled) "السحب مفعّل ⇅" else "ترتيب بالسحب",
                                        fontSize = 11.sp,
                                        fontWeight = if (isCashGroupsReorderEnabled) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isCashGroupsReorderEnabled) Icons.Default.SwapVert else Icons.Default.DragHandle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.testTag("btn_reorder_cash_groups_mgmt")
                            )
                        }

                        Button(
                            onClick = { showAddCashGroupDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_add_new_cash_group")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("إضافة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isCashGroupsReorderEnabled) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "سحب وإفلات مجموعات الصندوق مفعّل: اضغط مطولاً على كرت المجموعة واسحبه لتغيير ترتيبه.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            )
                        }
                    }
                }
            }

            itemsIndexed(uiState.cashGroups) { index, cashGroup ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .reorderableVerticalItem(
                            index = index,
                            itemCount = uiState.cashGroups.size,
                            isDragEnabled = isCashGroupsReorderEnabled,
                            onMove = { from, to -> viewModel.moveCashGroup(from, to) }
                        )
                        .testTag("cash_group_card_${cashGroup.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (cashGroup.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (cashGroup.isEnabled) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                ReorderDragHandle(
                                    onMoveUp = if (index > 0) { { viewModel.moveCashGroup(index, index - 1) } } else null,
                                    onMoveDown = if (index < uiState.cashGroups.size - 1) { { viewModel.moveCashGroup(index, index + 1) } } else null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${index + 1}. ${cashGroup.name}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (cashGroup.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                        fontSize = 13.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (cashGroup.type) {
                                        CashGroupType.DENOMINATIONS -> MaterialTheme.colorScheme.primaryContainer
                                        CashGroupType.EXPENSES -> MaterialTheme.colorScheme.errorContainer
                                        CashGroupType.DEPOSITS -> MaterialTheme.colorScheme.primaryContainer
                                        CashGroupType.DIRECT_ENTRY -> MaterialTheme.colorScheme.secondaryContainer
                                    }
                                ) {
                                    Text(
                                        text = when (cashGroup.type) {
                                            CashGroupType.DENOMINATIONS -> "نقد / فئات"
                                            CashGroupType.EXPENSES -> "مصاريف (خصم تلقائي)"
                                            CashGroupType.DEPOSITS -> "إيداعات (إضافة تلقائية)"
                                            CashGroupType.DIRECT_ENTRY -> "مقبوضات مباشرة"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }

                             Row(verticalAlignment = Alignment.CenterVertically) {
                                 IconButton(
                                     onClick = { if (index > 0) viewModel.moveCashGroup(index, index - 1) },
                                     enabled = index > 0,
                                     modifier = Modifier.size(28.dp)
                                 ) {
                                     Icon(Icons.Default.ArrowUpward, contentDescription = "أعلى", modifier = Modifier.size(15.dp))
                                 }
                                 IconButton(
                                     onClick = { if (index < uiState.cashGroups.size - 1) viewModel.moveCashGroup(index, index + 1) },
                                     enabled = index < uiState.cashGroups.size - 1,
                                     modifier = Modifier.size(28.dp)
                                 ) {
                                     Icon(Icons.Default.ArrowDownward, contentDescription = "أسفل", modifier = Modifier.size(15.dp))
                                 }
                                 Spacer(modifier = Modifier.width(4.dp))
                                 if (cashGroup.id != CASH_GROUP_MAIN_ID) {
                                     Switch(
                                         checked = cashGroup.isEnabled,
                                         onCheckedChange = { viewModel.toggleCashGroupEnabled(cashGroup.id) },
                                         modifier = Modifier.testTag("switch_cash_group_${cashGroup.id}")
                                     )
                                 }
                             }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // خيار الموازنة للصندوق
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.toggleCashGroupAddToBalance(cashGroup.id) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = !cashGroup.isExcludedFromBalance,
                                    onCheckedChange = { viewModel.toggleCashGroupAddToBalance(cashGroup.id, it) },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تضمين في الموازنة الإجمالية للصندوق",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showRenameGroupDialog = Pair(cashGroup.id, cashGroup.name) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("تعديل الاسم", fontSize = 11.sp)
                            }

                            if (!cashGroup.isDefault && cashGroup.id != CASH_GROUP_MAIN_ID) {
                                IconButton(
                                    onClick = { viewModel.deleteCashGroup(cashGroup.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
