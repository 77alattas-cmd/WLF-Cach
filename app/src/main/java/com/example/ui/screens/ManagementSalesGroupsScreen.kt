package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddGroupDialog
import com.example.ui.components.reorderableHorizontalItem
import com.example.ui.components.reorderableVerticalItem
import com.example.ui.components.ReorderDragHandle
import com.example.ui.model.*
import com.example.ui.theme.vibrant3d
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.ui.viewmodel.TicketAccountingViewModel
import com.example.ui.components.AddDenominationDialog
import com.example.ui.components.RenameGroupDialog
import com.example.ui.components.EditDenominationDialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManagementSalesGroupsScreen(
    viewModel: TicketAccountingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val listState = rememberLazyListState()

    var showAddGroupDialog by remember { mutableStateOf(false) }
    var showRenameGroupDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAddDenomDialog by remember { mutableStateOf<String?>(null) }
    var showEditDenomDialog by remember { mutableStateOf<Triple<String, Int, Int>?>(null) }
    var isSalesGroupsReorderEnabled by remember { mutableStateOf(false) }

    if (showAddGroupDialog) {
        AddGroupDialog(
            onDismiss = { showAddGroupDialog = false },
            onConfirm = { name, type, denoms ->
                viewModel.createSalesGroup(name, type, denoms, false)
                showAddGroupDialog = false
            }
        )
    }

    if (showRenameGroupDialog != null) {
        RenameGroupDialog(
            initialName = showRenameGroupDialog!!.second,
            onDismiss = { showRenameGroupDialog = null },
            onRename = { newName ->
                viewModel.updateGroupName(showRenameGroupDialog!!.first, newName)
                showRenameGroupDialog = null
            }
        )
    }

    if (showAddDenomDialog != null) {
        AddDenominationDialog(
            onDismiss = { showAddDenomDialog = null },
            onAdd = { denom ->
                viewModel.addCategoryToGroup(showAddDenomDialog!!, denom)
                showAddDenomDialog = null
            }
        )
    }

    if (showEditDenomDialog != null) {
        EditDenominationDialog(
            currentDenom = showEditDenomDialog!!.second,
            onDismiss = { showEditDenomDialog = null },
            onConfirm = { newDenom ->
                viewModel.updateDenominationValue(
                    showEditDenomDialog!!.first,
                    showEditDenomDialog!!.second,
                    newDenom
                )
                showEditDenomDialog = null
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("management_sales_groups_screen"),
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
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تنظيم مجموعات المبيعات",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "تعديل المجموعات، الفئات، والترتيب",
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
                            text = "مجموعات المبيعات",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${uiState.groups.count { it.isEnabled }} نشط من ${uiState.groups.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.groups.size > 1) {
                            FilterChip(
                                selected = isSalesGroupsReorderEnabled,
                                onClick = { isSalesGroupsReorderEnabled = !isSalesGroupsReorderEnabled },
                                label = {
                                    Text(
                                        text = if (isSalesGroupsReorderEnabled) "السحب مفعّل ⇅" else "ترتيب بالسحب",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSalesGroupsReorderEnabled) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isSalesGroupsReorderEnabled) Icons.Default.SwapVert else Icons.Default.DragHandle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("btn_reorder_sales_groups_mgmt")
                            )
                        }

                        Button(
                            onClick = { showAddGroupDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("btn_add_new_group")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("إضافة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isSalesGroupsReorderEnabled) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "سحب وإفلات مجموعات المبيعات مفعّل: اضغط مطولاً على كرت المجموعة واسحبه لتغيير ترتيبه.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                        }
                    }
                }
            }

            itemsIndexed(uiState.groups) { index, group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .reorderableVerticalItem(
                            index = index,
                            itemCount = uiState.groups.size,
                            isDragEnabled = isSalesGroupsReorderEnabled,
                            onMove = { from, to -> viewModel.moveSalesGroup(from, to) }
                        )
                        .testTag("sales_group_card_${group.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (group.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (group.isEnabled) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Group Title, Drag Handle, Switch & Order buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                ReorderDragHandle(
                                    onMoveUp = if (index > 0) { { viewModel.moveSalesGroup(index, index - 1) } } else null,
                                    onMoveDown = if (index < uiState.groups.size - 1) { { viewModel.moveSalesGroup(index, index + 1) } } else null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${index + 1}. ${group.name}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (group.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                        fontSize = 13.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (group.type) {
                                        SalesGroupType.DENOMINATIONS -> MaterialTheme.colorScheme.primaryContainer
                                        SalesGroupType.DIRECT_ENTRY -> MaterialTheme.colorScheme.secondaryContainer
                                        SalesGroupType.CUSTOM_FIELDS -> MaterialTheme.colorScheme.tertiaryContainer
                                    }
                                ) {
                                    Text(
                                        text = when (group.type) {
                                            SalesGroupType.DENOMINATIONS -> "فئات"
                                            SalesGroupType.DIRECT_ENTRY -> "مباشر"
                                            SalesGroupType.CUSTOM_FIELDS -> "مخصص"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { 
                                        if (index > 0) {
                                            viewModel.moveSalesGroup(index, index - 1)
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "أعلى", modifier = Modifier.size(15.dp))
                                }
                                IconButton(
                                    onClick = { 
                                        if (index < uiState.groups.size - 1) {
                                            viewModel.moveSalesGroup(index, index + 1)
                                        }
                                    },
                                    enabled = index < uiState.groups.size - 1,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "أسفل", modifier = Modifier.size(15.dp))
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                if (group.id != GROUP_SANAD_ID) {
                                    Switch(
                                        checked = group.isEnabled,
                                        onCheckedChange = { viewModel.toggleGroupEnabled(group.id) },
                                        modifier = Modifier.testTag("switch_group_${group.id}")
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // خيارات الموازنة والتقرير للمجموعة
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
                                    .clickable { viewModel.toggleSalesGroupAddToReport(group.id) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = group.addToReport,
                                    onCheckedChange = { viewModel.toggleSalesGroupAddToReport(group.id, it) },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "خيار التقرير",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.toggleSalesGroupAddToBalance(group.id) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = group.addToBalance && !group.isExcludedFromBalance,
                                    onCheckedChange = { viewModel.toggleSalesGroupAddToBalance(group.id, it) },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "خيار الموازنة",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Controls Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { showRenameGroupDialog = Pair(group.id, group.name) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("تعديل الاسم", fontSize = 11.sp)
                            }

                            if (group.type == SalesGroupType.DENOMINATIONS || group.type == SalesGroupType.CUSTOM_FIELDS) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clickable { viewModel.toggleGroupAddedField(group.id) }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Checkbox(
                                            checked = group.isAddedFieldEnabled,
                                            onCheckedChange = { viewModel.toggleGroupAddedField(group.id) },
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "حقل الإضافي",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                        )
                                    }

                                    Button(
                                        onClick = { showAddDenomDialog = group.id },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("فئة", fontSize = 10.sp)
                                    }
                                }
                            }

                            if (!group.isDefault && group.id != GROUP_SANAD_ID) {
                                IconButton(
                                    onClick = { viewModel.deleteSalesGroup(group.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Denominations List within Group
                        if (group.type == SalesGroupType.DENOMINATIONS || group.type == SalesGroupType.CUSTOM_FIELDS) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                group.rows.forEach { row ->
                                    val isRowEnabled = row.isEnabled
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isRowEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = BorderStroke(1.dp, if (isRowEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.clickable { 
                                            showEditDenomDialog = Triple(group.id, row.denomination, row.denomination)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = row.denomination.toString(),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isRowEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                )
                                            )
                                            IconButton(
                                                onClick = { viewModel.toggleRowEnabled(group.id, row.denomination) },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isRowEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = if (isRowEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteCategoryRow(group.id, row.denomination) },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
