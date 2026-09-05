package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.derivedStateOf
import kotlinx.coroutines.launch
import com.example.ui.theme.DesignSystem
import com.example.ui.theme.vibrant3d
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.components.DirectEntryGroupTable
import com.example.ui.components.DirectSalesTable
import com.example.ui.components.MiniBalanceStatusBadge
import com.example.ui.components.reorderableHorizontalItem
import com.example.ui.components.reorderableVerticalItem
import com.example.ui.components.ReorderDragHandle
import com.example.ui.model.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TicketAccountingViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DirectSalesScreen(
    viewModel: TicketAccountingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary by viewModel.salesSummary.collectAsStateWithLifecycle()
    val numpad = com.example.ui.components.LocalNumpadController.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val currentSelectedGroup = uiState.selectedGroup
    val activeGroups = uiState.groups.filter { it.isEnabled }

    var showManageSalesGroupsDialog by remember { mutableStateOf(false) }
    var showAddSalesGroupDialog by remember { mutableStateOf(false) }
    var showResetAllConfirmDialog by remember { mutableStateOf(false) }
    var isGroupTabsReorderEnabled by remember { mutableStateOf(false) }

    // Auto-scroll when selected group changes in Tab mode
    LaunchedEffect(uiState.selectedGroupId) {
        if (!uiState.isSalesAccordionMode) {
            listState.animateScrollToItem(0)
        }
    }

    // Auto-scroll to expanded group in Accordion mode
    LaunchedEffect(activeGroups.map { it.isExpanded }) {
        if (uiState.isSalesAccordionMode) {
            val expandedIndex = activeGroups.indexOfFirst { it.isExpanded }
            if (expandedIndex != -1) {
                listState.animateScrollToItem(expandedIndex)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("direct_sales_screen")
        ) {
        // 1. FIXED TOP SUMMARY HEADER (شاشة ملخص ثابتة في الأعلى)
        val isDark = isSystemInDarkTheme()
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .vibrant3d(
                    shape = RoundedCornerShape(16.dp),
                    elevation = 6.dp,
                    isDark = isDark,
                    baseColor = MaterialTheme.colorScheme.surface
                ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Top Row: Date, Seller, Status Badge
                val liveTimestamp = com.example.ui.model.rememberLiveTimeMillis()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = AccountingFormatter.formatDisplayDate(liveTimestamp, uiState.showHijriDate, uiState.useEasternArabicNumerals),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = AccountingFormatter.formatTimeAndDay(liveTimestamp, uiState.useEasternArabicNumerals, uiState.use24HourFormat),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Balance Status Badge
                    MiniBalanceStatusBadge(
                        status = summary.balanceStatus,
                        balanceAmount = summary.balance
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Numerical Summary Breakdown Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي المبيعات",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                        )
                        Text(
                            text = AccountingFormatter.formatYer(summary.totalRevenue),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "التذاكر المباعة",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                        )
                        Text(
                            text = "${summary.totalSold} تذكرة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "المتبقي الإجمالي",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                        )
                        Text(
                            text = "${summary.totalRemaining} تذكرة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                        )
                    }
                }

                val activeGroups = uiState.groups.filter { it.isEnabled }
                if (activeGroups.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "مبيعات الالنشطة:",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        activeGroups.forEach { group ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${group.name}:",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = AccountingFormatter.formatYer(group.totalRevenue),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1.4 Day Closed Banner (إغلاق اليوم)
        if (uiState.isDayClosed) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔒 اليوم مغلق ومُعتمد رسمياً",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                        Text(
                            text = "توقف الإدخال والتعديل لهذا اليوم نهائياً لحماية الحسابات.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = { viewModel.requestCloseDay() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("فك القفل ⚠️", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // 1.5 Read-Only & Lock Mode Banners
        if (uiState.isReadOnlyMode && !uiState.isDayClosed) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔒 وضع القراءة مفعّل - الإدخال والتعديل متوقف",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }

        if (uiState.isLockGivenExtraMode && !uiState.isReadOnlyMode) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔒 تم منع تعديل المعطى والإضافي لتجنب التغييرات بالخطأ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }
        }

        // 2. Alert Banner for Remaining Constraint Exceeded (المتبقي لا يتجاوز المعطى والإضافي)
        uiState.remainingWarningMessage?.let { warningMsg ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = warningMsg,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.dismissRemainingWarning() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // 2.5 Toolbar: Controls, Accordion, and Reorder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { viewModel.toggleSalesAccordionMode(!uiState.isSalesAccordionMode) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp).testTag("btn_toggle_sales_accordion")
            ) {
                Icon(
                    imageVector = if (uiState.isSalesAccordionMode) Icons.Default.ViewAgenda else Icons.Default.VerticalSplit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (uiState.isSalesAccordionMode) "تبويبات" else "مطوية",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // زر تنظيم خاص بقسم مبيعات التذاكر
            FilledTonalButton(
                onClick = { showManageSalesGroupsDialog = true },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp).testTag("btn_manage_sales_groups")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "تنظيم",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // زر تصفير لكل المجموعات والفئات
            FilledTonalButton(
                onClick = { showResetAllConfirmDialog = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp).testTag("btn_reset_all_groups_sales")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "تصفير",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // زر ترتيب الأقسام (ثابت دائماً ولا يختفي بتغيير نوع العرض)
            FilledTonalButton(
                onClick = {
                    if (uiState.isSalesAccordionMode) {
                        showManageSalesGroupsDialog = true
                    } else {
                        isGroupTabsReorderEnabled = !isGroupTabsReorderEnabled
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isGroupTabsReorderEnabled && !uiState.isSalesAccordionMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isGroupTabsReorderEnabled && !uiState.isSalesAccordionMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp).testTag("btn_toggle_sales_tabs_reorder")
            ) {
                Icon(
                    imageVector = if (isGroupTabsReorderEnabled && !uiState.isSalesAccordionMode) Icons.Default.SwapHoriz else Icons.Default.DragHandle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isGroupTabsReorderEnabled && !uiState.isSalesAccordionMode) "السحب مفعّل ⇄" else "ترتيب الأقسام",
                    fontSize = 11.sp,
                    fontWeight = if (isGroupTabsReorderEnabled && !uiState.isSalesAccordionMode) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // 3. GROUPS TAB BAR OR ACCORDIONS
        if (!uiState.isSalesAccordionMode) {
            if (isGroupTabsReorderEnabled) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "سحب وإفلات تبويبات الأقسام مفعّل: اضغط مطولاً على التبويب واسحبه يميناً أو يساراً لإعادة الترتيب.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val activeTabColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupActiveTabBg) ?: MaterialTheme.colorScheme.secondaryContainer
                    val inactiveTabColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupInactiveTabBg) ?: Color.Transparent
                    activeGroups.forEachIndexed { groupIndex, group ->
                        val isSelected = group.id == uiState.selectedGroupId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectGroup(group.id) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeTabColor,
                                containerColor = inactiveTabColor
                            ),
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = group.name,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = AccountingFormatter.formatYer(group.totalRevenue),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier
                                .testTag("chip_sales_group_${group.id}")
                                .reorderableHorizontalItem(
                                    index = groupIndex,
                                    itemCount = activeGroups.size,
                                    isDragEnabled = isGroupTabsReorderEnabled,
                                    onMove = { from, to -> viewModel.moveSalesGroup(from, to) }
                                )
                        )
                    }
                }
            }
        }

        // 4. MAIN CONTENT LIST
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = if (numpad.isVisible) 360.dp else 56.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.isSalesAccordionMode) {
                if (activeGroups.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد مبيعات نشطة حالياً. يمكنك تفعيل المبيعات من تبويب تنظيم الأقسام.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline)
                            )
                        }
                    }
                } else {
                    activeGroups.forEach { group ->
                        item(key = group.id) {
                            ExpandableSalesGroupCard(
                                group = group,
                                isExpanded = group.isExpanded,
                                onToggleExpand = { viewModel.toggleSalesGroupExpansion(group.id) },
                                content = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = group.notes,
                                            onValueChange = { viewModel.updateGroupNotes(group.id, it) },
                                            label = { Text("ملاحظات ${group.name}") },
                                            singleLine = false,
                                            minLines = 1,
                                            maxLines = 3,
                                            shape = RoundedCornerShape(12.dp),
                                            textStyle = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        when (group.type) {
                                            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> {
                                                DirectSalesTable(
                                                    rows = group.activeRows.ifEmpty { group.rows },
                                                    groupName = group.name,
                                                    isEnabled = group.isEnabled && !uiState.isReadOnlyMode && !uiState.isDayClosed,
                                                    isLockGivenExtraMode = uiState.isLockGivenExtraMode,
                                                    isAddedFieldEnabled = group.isAddedFieldEnabled,
                                                    mergeAddedWithGiven = group.mergeAddedWithGiven,
                                                    onToggleMergeAdded = { viewModel.toggleGroupMergeAdded(group.id) },
                                                    onToggleAddedField = { viewModel.toggleGroupAddedField(group.id) },
                                                    showRemainingStepper = uiState.showRemainingStepper,
                                                    customColorState = uiState.customColorThemeState,
                                                    onGivenChange = { denom, givenStr -> viewModel.updateGiven(group.id, denom, givenStr) },
                                                    onAddedChange = { denom, addedStr -> viewModel.updateAdded(group.id, denom, addedStr) },
                                                    onRemainingChange = { denom, remStr -> viewModel.updateRemaining(group.id, denom, remStr) },
                                                    onNotesChange = { denom, notesStr -> viewModel.updateRowNotes(group.id, denom, notesStr) },
                                                    onZeroOutRemaining = { denom -> viewModel.zeroOutRemaining(group.id, denom) },
                                                    onZeroOutAllRemaining = { viewModel.zeroOutAllRemaining(group.id) },
                                                    onRemoveCategory = { denom -> viewModel.deleteCategoryRow(group.id, denom) },
                                                    onToggleRowEnabled = { denom -> viewModel.toggleRowEnabled(group.id, denom) },
                                                    onMoveRow = { from, to -> viewModel.moveDenominationInGroup(group.id, from, to) }
                                                )
                                            }
                                            SalesGroupType.DIRECT_ENTRY -> {
                                                DirectEntryGroupTable(
                                                    group = group,
                                                    customColorState = uiState.customColorThemeState,
                                                    isReadOnlyMode = uiState.isReadOnlyMode || uiState.isDayClosed,
                                                    onAddEntry = { title, amount, qty, notes -> viewModel.addDirectEntry(group.id, title, amount, notes) },
                                                    onAddDeposit = { title, amount, notes -> viewModel.addChiniDepositEntry(group.id, title, amount, notes) },
                                                    onUpdateEntry = { id, title, amount, qty, notes -> viewModel.updateDirectEntry(group.id, id, title, amount, notes) },
                                                    onRemoveEntry = { id -> viewModel.removeDirectEntry(group.id, id) },
                                                    onQuickAddAmount = { amt -> viewModel.addDirectEntry(group.id, "إضافة سريعة", amt.toString()) },
                                                    onToggleEnabled = { viewModel.toggleSalesGroupEnabled(group.id) },
                                                    onMergeEntries = { viewModel.mergeDuplicateEntries(group.id) },
                                                    onResetGroup = { viewModel.resetSalesGroupOnly(group.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                if (currentSelectedGroup != null && currentSelectedGroup.isEnabled) {
                    val totalSoldTickets = if (currentSelectedGroup.type == SalesGroupType.DENOMINATIONS || currentSelectedGroup.type == SalesGroupType.CUSTOM_FIELDS) {
                        currentSelectedGroup.rows.sumOf { it.sold }
                    } else {
                        currentSelectedGroup.directEntries.size
                    }
                    val soldLabel = if (currentSelectedGroup.type == SalesGroupType.DIRECT_ENTRY) "عملية" else "تذكرة"

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = "إجمالي المباع في ${currentSelectedGroup.name}: $totalSoldTickets $soldLabel",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Group Notes placed at top
                    item {
                        OutlinedTextField(
                            value = currentSelectedGroup.notes,
                            onValueChange = { viewModel.updateGroupNotes(currentSelectedGroup.id, it) },
                            label = { Text("ملاحظات ${currentSelectedGroup.name}") },
                            singleLine = false,
                            minLines = 1,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        when (currentSelectedGroup.type) {
                            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> {
                                DirectSalesTable(
                                    rows = currentSelectedGroup.activeRows.ifEmpty { currentSelectedGroup.rows },
                                    groupName = currentSelectedGroup.name,
                                    isEnabled = currentSelectedGroup.isEnabled && !uiState.isReadOnlyMode && !uiState.isDayClosed,
                                    isLockGivenExtraMode = uiState.isLockGivenExtraMode,
                                    isAddedFieldEnabled = currentSelectedGroup.isAddedFieldEnabled,
                                    mergeAddedWithGiven = currentSelectedGroup.mergeAddedWithGiven,
                                    onToggleMergeAdded = { viewModel.toggleGroupMergeAdded(currentSelectedGroup.id) },
                                    onToggleAddedField = { viewModel.toggleGroupAddedField(currentSelectedGroup.id) },
                                    showRemainingStepper = uiState.showRemainingStepper,
                                    customColorState = uiState.customColorThemeState,
                                    onGivenChange = { denom, givenStr -> viewModel.updateGiven(currentSelectedGroup.id, denom, givenStr) },
                                    onAddedChange = { denom, addedStr -> viewModel.updateAdded(currentSelectedGroup.id, denom, addedStr) },
                                    onRemainingChange = { denom, remStr -> viewModel.updateRemaining(currentSelectedGroup.id, denom, remStr) },
                                    onNotesChange = { denom, notesStr -> viewModel.updateRowNotes(currentSelectedGroup.id, denom, notesStr) },
                                    onZeroOutRemaining = { denom -> viewModel.zeroOutRemaining(currentSelectedGroup.id, denom) },
                                    onZeroOutAllRemaining = { viewModel.zeroOutAllRemaining(currentSelectedGroup.id) },
                                    onRemoveCategory = { denom -> viewModel.deleteCategoryRow(currentSelectedGroup.id, denom) },
                                    onToggleRowEnabled = { denom -> viewModel.toggleRowEnabled(currentSelectedGroup.id, denom) },
                                    onMoveRow = { from, to -> viewModel.moveDenominationInGroup(currentSelectedGroup.id, from, to) }
                                )
                            }
                            SalesGroupType.DIRECT_ENTRY -> {
                                DirectEntryGroupTable(
                                    group = currentSelectedGroup,
                                    customColorState = uiState.customColorThemeState,
                                    isReadOnlyMode = uiState.isReadOnlyMode || uiState.isDayClosed,
                                    onAddEntry = { title, amount, qty, notes -> viewModel.addDirectEntry(currentSelectedGroup.id, title, amount, notes) },
                                    onAddDeposit = { title, amount, notes -> viewModel.addChiniDepositEntry(currentSelectedGroup.id, title, amount, notes) },
                                    onUpdateEntry = { id, title, amount, qty, notes -> viewModel.updateDirectEntry(currentSelectedGroup.id, id, title, amount, notes) },
                                    onRemoveEntry = { id -> viewModel.removeDirectEntry(currentSelectedGroup.id, id) },
                                    onQuickAddAmount = { amt -> viewModel.addDirectEntry(currentSelectedGroup.id, "إضافة سريعة", amt.toString()) },
                                    onToggleEnabled = { viewModel.toggleSalesGroupEnabled(currentSelectedGroup.id) },
                                    onMergeEntries = { viewModel.mergeDuplicateEntries(currentSelectedGroup.id) },
                                    onResetGroup = { viewModel.resetSalesGroupOnly(currentSelectedGroup.id) }
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد مبيعات نشطة حالياً. يمكنك تفعيل المبيعات من تبويب تنظيم الأقسام.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline)
                            )
                        }
                    }
                }
            }

            if (numpad.isVisible) {
                item { Spacer(modifier = Modifier.height(280.dp)) }
            }
        }
    }
    }

    // Modal: Dialog to confirm disabling Read-Only mode
    if (uiState.showDisableReadOnlyConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDisableReadOnlyDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأكيد إلغاء وضع القراءة", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Text("هل أنت متأكد من إلغاء تفعيل وضع القراءة والسماح بالتعديل والإدخال مجدداً؟")
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmDisableReadOnlyMode() }) {
                    Text("إلغاء وضع القراءة")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDisableReadOnlyDialog() }) {
                    Text("تراجع")
                }
            }
        )
    }

    // Modal: Dialog to confirm disabling Lock Given/Extra mode
    if (uiState.showDisableLockGivenExtraConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDisableLockGivenExtraDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأكيد السماح بتعديل المعطى والإضافي", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Text("هل أنت متأكد من فك القفل والسماح بتعديل القيم في حقلي المعطى والإضافي؟")
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmDisableLockGivenExtraMode() }) {
                    Text("فك القفل وتأكيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDisableLockGivenExtraDialog() }) {
                    Text("تراجع")
                }
            }
        )
    }

    // Modal: Confirmation Dialog for Modifying Given Value (تنبيه قبل تأكيد تعديل المعطى)
    if (uiState.showConfirmGivenDialog && uiState.pendingGivenDenom != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissGivenConfirm() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأكيد تعديل المعطى", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Text(
                    text = "هل أنت متأكد من تغيير قيمة المعطى لفئة ${uiState.pendingGivenDenom} من (${uiState.pendingGivenOldValue}) إلى (${uiState.pendingGivenNewValue})؟",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmPendingGiven() }
                ) {
                    Text("تأكيد التعديل")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGivenConfirm() }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showManageSalesGroupsDialog) {
        com.example.ui.components.ManageGroupsDialog(
            groups = uiState.groups,
            onToggleEnabled = { viewModel.toggleGroupEnabled(it) },
            onToggleAddedField = { viewModel.toggleGroupAddedField(it) },
            onToggleMergeAdded = { viewModel.toggleMergeAddedWithGiven(it) },
            onToggleAddToReport = { viewModel.toggleGroupAddToReport(it) },
            onToggleAddToBalance = { viewModel.toggleGroupAddToBalance(it) },
            onMoveLeft = { groupId ->
                val idx = uiState.groups.indexOfFirst { it.id == groupId }
                if (idx > 0) viewModel.moveSalesGroup(idx, idx - 1)
            },
            onMoveRight = { groupId ->
                val idx = uiState.groups.indexOfFirst { it.id == groupId }
                if (idx != -1 && idx < uiState.groups.size - 1) viewModel.moveSalesGroup(idx, idx + 1)
            },
            onRenameGroup = { id, newName -> viewModel.updateGroupName(id, newName) },
            onDeleteGroup = { viewModel.deleteSalesGroup(it) },
            onAddGroupClick = {
                showManageSalesGroupsDialog = false
                showAddSalesGroupDialog = true
            },
            onDismiss = { showManageSalesGroupsDialog = false }
        )
    }

    if (showAddSalesGroupDialog) {
        com.example.ui.components.AddGroupDialog(
            onDismiss = { showAddSalesGroupDialog = false },
            onConfirmWithDetails = { name, type, denoms, isExcluded, given, added, rem, form ->
                viewModel.createSalesGroup(
                    name = name,
                    type = type,
                    denominations = denoms,
                    isExcluded = isExcluded,
                    givenLabel = given,
                    addedLabel = added,
                    remainingLabel = rem,
                    formula = form
                )
                showAddSalesGroupDialog = false
            }
        )
    }

    if (showResetAllConfirmDialog) {
        val currentGroup = uiState.groups.find { it.id == uiState.selectedGroupId }
        val denoms = currentGroup?.rows?.map { it.denomination } ?: emptyList()
        com.example.ui.components.ResetScopeDialog(
            onDismiss = { showResetAllConfirmDialog = false },
            onConfirmResetGroup = { viewModel.resetSalesGroupOnly(uiState.selectedGroupId) },
            onConfirmResetCategory = { denom -> viewModel.resetSalesCategory(uiState.selectedGroupId, denom) },
            onConfirmResetField = { field -> viewModel.resetSalesField(uiState.selectedGroupId, field) },
            onConfirmResetCash = { viewModel.resetPhysicalCashOnly() },
            onConfirmResetRevenue = { viewModel.resetAllInputs() },
            onConfirmResetAll = {
                showResetAllConfirmDialog = false
                viewModel.openShiftResetDialog()
            },
            onConfirmSelectiveReset = { gId, rGiven, rAdded, rRem, sDenoms, sDirect, rCash, rExp, rDep ->
                viewModel.executeTargetedSelectiveReset(
                    groupId = gId,
                    resetGiven = rGiven,
                    resetAdded = rAdded,
                    resetRemaining = rRem,
                    selectedDenominations = sDenoms,
                    selectedDirectEntryIds = sDirect,
                    resetCashInBox = rCash,
                    resetExpenses = rExp,
                    resetDeposits = rDep
                )
            },
            groupId = uiState.selectedGroupId,
            groupName = currentGroup?.name ?: "المجموعة الحالية",
            availableCategories = denoms,
            allSalesGroups = uiState.groups,
            allCashGroups = uiState.cashGroups,
            isSalesSection = true
        )
    }
}

@Composable
fun ExpandableSalesGroupCard(
    group: SalesGroupUiState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                    }
                }
                    val totalSoldTickets = if (group.type == SalesGroupType.DENOMINATIONS || group.type == SalesGroupType.CUSTOM_FIELDS) {
                        group.rows.sumOf { it.sold }
                    } else {
                        group.directEntries.size
                    }
                    val soldLabel = if (group.type == SalesGroupType.DIRECT_ENTRY) "عملية" else "تذكرة"

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = AccountingFormatter.formatYer(group.totalRevenue),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "المباع: $totalSoldTickets $soldLabel",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 10.sp
                            )
                        )
                    }
            }
            if (isExpanded) {
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    content()
                }
            }
        }
    }
}
