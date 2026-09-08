package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.vibrant3d
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.net.Uri
import com.example.ui.model.CalcAppSection
import com.example.ui.components.*
import com.example.ui.model.*
import com.example.ui.viewmodel.TicketAccountingViewModel
import com.example.ui.viewmodel.AppScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CashBoxScreen(
    viewModel: TicketAccountingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary by viewModel.salesSummary.collectAsStateWithLifecycle()
    val numpad = com.example.ui.components.LocalNumpadController.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val currentSelectedCashGroup = uiState.selectedCashGroup
    val activeCashGroups = uiState.cashGroups.filter { it.isEnabled }

    // Auto-scroll when selected cash group changes
    LaunchedEffect(uiState.selectedCashGroupId) {
        listState.animateScrollToItem(0)
    }

    var showExpenseDialog by remember { mutableStateOf(false) }
    var expensePresetTitle by remember { mutableStateOf("") }
    var expenseIsDepositMode by remember { mutableStateOf(false) }
    var editingExpenseItem by remember { mutableStateOf<CashExpenseItem?>(null) }
    var showDirectEntryDialog by remember { mutableStateOf(false) }
    var selectedExpenseIds by remember { mutableStateOf(setOf<String>()) }
    var showMergeDialog by remember { mutableStateOf(false) }
    var showRebalanceDialog by remember { mutableStateOf(false) }
    var showAddCashGroupDialog by remember { mutableStateOf(false) }
    var showManageCashGroupsDialog by remember { mutableStateOf(false) }
    var showOrganizeCenterDialog by remember { mutableStateOf(false) }
    var showResetAllConfirmDialog by remember { mutableStateOf(false) }
    var deletingExpenseItem by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(id, title)
    var isCashTabsReorderEnabled by remember { mutableStateOf(false) }
    var cashIncreaseInput by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("cash_box_screen")
        ) {
            // Day Closed Banner (إغلاق اليوم)
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
                                text = "توقف الإدخال والتعديل للصناديق النقدية لحماية القيود والمطابقة.",
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

            // 1. FIXED TOP SUMMARY HEADER (شاشة مجموع ثابتة في الأعلى)
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
                // Top Row: Date, Time & Balance Status
                val liveTimestamp = com.example.ui.model.rememberLiveTimeMillis()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = AccountingFormatter.formatDisplayDate(liveTimestamp, uiState.showHijriDate, uiState.useEasternArabicNumerals),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = AccountingFormatter.formatTimeAndDay(liveTimestamp, uiState.useEasternArabicNumerals, uiState.use24HourFormat),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    MiniBalanceStatusBadge(
                        status = summary.balanceStatus,
                        balanceAmount = summary.balance
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Compact 4-stat metrics (النقد، مجموع العملات، المصروفات، الإيداعات)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // النقد
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "النقد",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AccountingFormatter.formatYer(summary.netCashYer),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp)
                        )
                    }

                    // مجموع العملات
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "مجموع العملات 🪙",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.secondary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AccountingFormatter.formatYer(summary.otherCurrenciesTotalYer),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                        )
                    }

                    // المصروفات
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "المصروفات",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AccountingFormatter.formatYer(summary.totalExpensesInYer),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        )
                    }

                    // الإيداعات
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "الإيداعات",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.5.sp)
                        )
                        Text(
                            text = AccountingFormatter.formatYer(summary.totalDepositsInYer),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Compact prominent "الإجمالي" Row
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإجمالي",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                        )
                        Text(
                            text = AccountingFormatter.formatYer(summary.grossCashInBox),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                        )
                    }
                }

                if (summary.balance < 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showRebalanceDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "معادلة النقد عند العجز (عجز: ${AccountingFormatter.formatYer(kotlin.math.abs(summary.balance))})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                        )
                    }
                }
            }
        }

        // Toolbar: Action Buttons (Always on top) & Section Tabs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
        ) {
            // 1. Action Buttons Row: Always at the top above group names
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // زر ترتيب الأقسام والصناديق بالسحب والإفلات
                FilledTonalButton(
                    onClick = { isCashTabsReorderEnabled = !isCashTabsReorderEnabled },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isCashTabsReorderEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isCashTabsReorderEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp).testTag("btn_toggle_cash_tabs_reorder")
                ) {
                    Icon(
                        imageVector = if (isCashTabsReorderEnabled) Icons.Default.SwapHoriz else Icons.Default.DragHandle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCashTabsReorderEnabled) "السحب مفعّل ⇄" else "ترتيب الأقسام",
                        fontSize = 11.sp,
                        fontWeight = if (isCashTabsReorderEnabled) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // زر إضافة صندوق جديد
                FilledTonalButton(
                    onClick = { showAddCashGroupDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp).testTag("btn_add_cash_group_toolbar")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "صندوق جديد",
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
                    modifier = Modifier.height(30.dp).testTag("btn_reset_all_groups_cash")
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
            }

            if (isCashTabsReorderEnabled) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "سحب وإفلات تبويبات الصناديق مفعّل: اضغط مطولاً على التبويب واسحبه يميناً أو يساراً لإعادة الترتيب.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                    }
                }
            }

            // 2. Persistent Section Tabs Row (أسماء مجموعات وأقسام الصندوق تحت الأزرار)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(activeCashGroups) { groupIndex, group ->
                    val isSelected = group.id == (uiState.selectedCashGroupId ?: activeCashGroups.firstOrNull()?.id)
                    val activeTabColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupActiveTabBg) ?: MaterialTheme.colorScheme.secondaryContainer
                    val inactiveTabColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupInactiveTabBg) ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    
                    val rateVal = uiState.exchangeRateInput.toDoubleOrNull() ?: 380.0
                    val cashYerVal = uiState.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0
                    val groupTotal = group.getTotalYer(rateVal, cashYerVal)
                    val formattedTotal = when (group.type) {
                        CashGroupType.EXPENSES -> "-" + AccountingFormatter.formatYer(groupTotal)
                        CashGroupType.DEPOSITS -> "+" + AccountingFormatter.formatYer(groupTotal)
                        else -> AccountingFormatter.formatYer(groupTotal)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCashGroup(group.id) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeTabColor,
                            containerColor = inactiveTabColor
                        ),
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = group.name,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = formattedTotal,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                        } else null,
                        modifier = Modifier.reorderableHorizontalItem(
                            index = groupIndex,
                            itemCount = activeCashGroups.size,
                            isDragEnabled = isCashTabsReorderEnabled,
                            onMove = { from, to -> viewModel.moveCashGroup(from, to) }
                        )
                    )
                }
            }
        }

        // MAIN CASH CONTENT
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = if (numpad.isVisible) 360.dp else 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val currentCashGroup = activeCashGroups.find { it.id == (uiState.selectedCashGroupId ?: activeCashGroups.firstOrNull()?.id) }
            if (currentCashGroup != null) {
                item(key = currentCashGroup.id) {
                    val rateVal = uiState.exchangeRateInput.toDoubleOrNull() ?: 380.0
                    val cashYerVal = uiState.cashInBoxYerInput.trim().toDoubleOrNull() ?: 0.0
                    val groupTotal = currentCashGroup.getTotalYer(rateVal, cashYerVal)
                    
                    ExpandableCashGroupCard(
                        group = currentCashGroup,
                        groupTotal = groupTotal,
                        isExpanded = true,
                        onToggleExpand = { },
                        isReadOnly = uiState.isReadOnlyMode,
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = currentCashGroup.notes,
                                    onValueChange = { viewModel.updateCashGroupNotes(currentCashGroup.id, it) },
                                    label = { Text("ملاحظات مجموعة ${currentCashGroup.name}") },
                                    singleLine = false,
                                    minLines = 1,
                                    maxLines = 3,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                                
                                when (currentCashGroup.type) {
                                CashGroupType.DENOMINATIONS -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        if (currentCashGroup.id == CASH_GROUP_MAIN_ID) {
                                            val displayCash = cashYerVal
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                com.example.ui.components.AppNumberField(
                                                    value = uiState.cashInBoxYerInput,
                                                    onValueChange = { viewModel.updateCashInBoxYer(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                                                    enabled = !uiState.isReadOnlyMode,
                                                    label = { Text("النقد الحالي (${AccountingFormatter.formatYer(displayCash)})") },
                                                    placeholder = { Text("0") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                    ),
                                                    modifier = Modifier.weight(1f).testTag("input_cash_yer")
                                                )

                                                if (!uiState.isReadOnlyMode) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                        FilledTonalButton(
                                                            onClick = {
                                                                val currentVal = uiState.cashInBoxYerInput.toDoubleOrNull() ?: 0.0
                                                                val newVal = (currentVal - 1000.0).coerceAtLeast(0.0)
                                                                viewModel.updateCashInBoxYer(if (newVal == 0.0) "" else newVal.toLong().toString())
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.height(42.dp)
                                                        ) {
                                                            Text("-1k", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        FilledTonalButton(
                                                            onClick = {
                                                                val currentVal = uiState.cashInBoxYerInput.toDoubleOrNull() ?: 0.0
                                                                val newVal = currentVal + 1000.0
                                                                viewModel.updateCashInBoxYer(newVal.toLong().toString())
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.height(42.dp)
                                                        ) {
                                                            Text("+1k", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        FilledTonalButton(
                                                            onClick = {
                                                                val currentVal = uiState.cashInBoxYerInput.toDoubleOrNull() ?: 0.0
                                                                val newVal = currentVal + 5000.0
                                                                viewModel.updateCashInBoxYer(newVal.toLong().toString())
                                                            },
                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.height(42.dp)
                                                        ) {
                                                            Text("+5k", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }

                                            // زيادة النقد لتضاف للنقد الحالي
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                com.example.ui.components.AppNumberField(
                                                    value = cashIncreaseInput,
                                                    onValueChange = { cashIncreaseInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                                    enabled = !uiState.isReadOnlyMode,
                                                    label = { Text("زيادة النقد (+)") },
                                                    placeholder = { Text("أدخل المبلغ لإضافته للنقد الحالي") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                                    ),
                                                    modifier = Modifier.weight(1f).testTag("input_cash_increase")
                                                )

                                                Button(
                                                    onClick = {
                                                        val amount = cashIncreaseInput.toDoubleOrNull() ?: 0.0
                                                        if (amount > 0) {
                                                            viewModel.addCashToBoxYer(amount)
                                                            cashIncreaseInput = ""
                                                        }
                                                    },
                                                    enabled = !uiState.isReadOnlyMode && (cashIncreaseInput.toDoubleOrNull() ?: 0.0) > 0,
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(52.dp).testTag("btn_apply_cash_increase")
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("إضافة للنقد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                CashGroupType.DIRECT_ENTRY, CashGroupType.EXPENSES, CashGroupType.DEPOSITS -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        CashDirectGroupTable(
                                            group = currentCashGroup,
                                            exchangeRate = rateVal,
                                            isReadOnly = uiState.isReadOnlyMode,
                                            onAddEntry = { title, amount, currencyCode, isSar, customRate, notes, deductFromCash ->
                                                viewModel.addCashDirectEntry(currentCashGroup.id, title, amount, currencyCode, isSar, customExchangeRateInput = customRate?.toString() ?: "", customExchangeRate = customRate, notes = notes, deductFromCash = deductFromCash)
                                            },
                                            onUpdateEntry = { itemId, title, amount, currencyCode, isSar, customRate, notes, deductFromCash ->
                                                viewModel.updateCashDirectEntry(currentCashGroup.id, itemId, title, amount, currencyCode, isSar, customExchangeRateInput = customRate?.toString() ?: "", customExchangeRate = customRate, notes = notes, deductFromCash = deductFromCash)
                                            },
                                            onRemoveEntry = { itemId ->
                                                val itemTitle = currentCashGroup.directEntries.find { it.id == itemId }?.title ?: "البند المحدد"
                                                deletingExpenseItem = Pair(itemId, itemTitle)
                                            },
                                            onToggleEnabled = { viewModel.toggleCashGroupEnabled(currentCashGroup.id) },
                                            onMergeEntries = { viewModel.mergeDuplicateEntries(currentCashGroup.id) },
                                            onResetGroup = { viewModel.resetCashGroupOnly(currentCashGroup.id) }
                                        )
                                    }
                                }
                                }
                            }
                        }
                    )
                }
            }


            if (numpad.isVisible) {
                item { Spacer(modifier = Modifier.height(280.dp)) }
            }
        }
    }
    }

    // Modal: Merge Expenses / Deposits Dialog
    if (showMergeDialog) {
        val selectedItems = uiState.expenses.filter { it.id in selectedExpenseIds }
        var mergedTitle by remember {
            mutableStateOf("دمج " + selectedItems.joinToString(" + ") { it.title })
        }
        var isDeposit by remember {
            mutableStateOf(selectedItems.count { it.isDeposit } >= selectedItems.count { !it.isDeposit })
        }

        val defaultRate = uiState.exchangeRateInput.toDoubleOrNull() ?: 380.0
        var netYer = 0.0
        selectedItems.forEach { item ->
            val yer = item.getTotalYer(defaultRate)
            if (item.isDeposit) netYer += yer else netYer -= yer
        }
        val absYer = kotlin.math.abs(netYer)

        AlertDialog(
            onDismissRequest = { showMergeDialog = false },
            title = { Text("دمج البنود المحددة (${selectedItems.size})", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "سيتم دمج البنود المحددة واستبدالها ببند واحد بالمبلغ الصافي المعادل.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            selectedItems.forEach { exp ->
                                Text(
                                    "• ${exp.title}: ${exp.amountInput} ${if (exp.isSar) "ر.س." else "ر.ي."} (${if (exp.isDeposit) "إيداع +" else "مصروف -"})",
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = mergedTitle,
                        onValueChange = { mergedTitle = it },
                        label = { Text("عنوان البند المدمج") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isDeposit,
                            onClick = { isDeposit = false },
                            label = { Text("خصم (مصروف)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isDeposit,
                            onClick = { isDeposit = true },
                            label = { Text("إضافة (إيداع)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        "المبلغ الإجمالي الصافي: ${AccountingFormatter.formatYer(absYer)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.mergeExpenses(selectedExpenseIds, mergedTitle, isDeposit)
                        selectedExpenseIds = emptySet()
                        showMergeDialog = false
                    }
                ) {
                    Text("تأكيد الدمج")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMergeDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showRebalanceDialog) {
        val deficitYer = kotlin.math.abs(summary.balance)
        AlertDialog(
            onDismissRequest = { showRebalanceDialog = false },
            title = { Text("تأكيد معادلة النقد بالصندوق", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "هل أنت متأكد من معادلة النقد بالصندوق؟\n\nسيتم إضافة مبلغ العجز (${AccountingFormatter.formatYer(deficitYer)}) تلقائياً إلى النقد الفعلي ليكون الفرق صفراً والموازنة مطابقة.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rebalanceCashBox()
                        showRebalanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("تأكيد المعادلة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebalanceDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (uiState.showCashConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCashConfirmDialog() },
            title = { Text("نتيجة إدخال النقد الجديد بالصندوق", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    uiState.cashConfirmationMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmCashInput() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("تأكيد النقد")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCashConfirmDialog() }) {
                    Text("تعديل")
                }
            }
        )
    }

    deletingExpenseItem?.let { (id, title) ->
        val expense = uiState.expenses.find { it.id == id }
        val isExpense = expense != null
        val cashGroup = uiState.cashGroups.find { g -> g.directEntries.any { it.id == id } }
        val isDeposit = (expense?.isDeposit == true) || (cashGroup?.type == CashGroupType.DEPOSITS) || (cashGroup?.id == "cash_group_deposits")
        val isForeignCurrency = !isExpense && !isDeposit
        var returnToCash by remember { mutableStateOf(false) }
        
        val groupName = uiState.cashGroups.find { g -> g.directEntries.any { it.id == id } }?.name ?: "المصروفات والإيداعات"
        val dialogTitle = if (isDeposit) "هل تريد حذف الإيداع" else "هل تريد حذف من ($groupName)"
        
        AlertDialog(
            onDismissRequest = { deletingExpenseItem = null },
            title = { Text(dialogTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "هل أنت متأكد من حذف البند ($title)؟",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (isForeignCurrency) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = returnToCash,
                                onCheckedChange = { returnToCash = it }
                            )
                            Text(
                                text = "إعادة المبلغ للنقد (عكس الخصم)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeCashExpense(id, returnToCash)
                        deletingExpenseItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف البند")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingExpenseItem = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal: Add Expense / Deposit Dialog
    if (showExpenseDialog) {
        var titleInput by remember { mutableStateOf(expensePresetTitle) }
        var amountInput by remember { mutableStateOf("") }
        var isSar by remember { mutableStateOf(false) }
        var isDeposit by remember { mutableStateOf(expenseIsDepositMode) }
        var customExchangeRateInput by remember { mutableStateOf("") }
        var notesInput by remember { mutableStateOf("") }
        var isSuspended by remember { mutableStateOf(false) }
        var deductFromCash by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = {
                Text(
                    text = if (isDeposit) "إضافة إيداع للصندوق (زيادة 🔺)" else "إضافة مصروف من الصندوق (خصم 🔻)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                CompositionLocalProvider(com.example.ui.components.LocalNumpadController provides numpad) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Type selector: Expense vs Deposit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !isDeposit,
                                onClick = { 
                                    isDeposit = false
                                    deductFromCash = true
                                },
                                label = { Text("مصروف (خصم 🔻)", fontSize = 11.sp, fontWeight = if (!isDeposit) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = isDeposit,
                                onClick = { 
                                    isDeposit = true
                                    deductFromCash = false
                                },
                                label = { Text("إيداع (إضافة 🔺)", fontSize = 11.sp, fontWeight = if (isDeposit) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text(if (isDeposit) "بيان الإيداع / المصدر (اختياري)" else "بيان المصروف (اختياري)") },
                            placeholder = { Text(if (isDeposit) "إيداع" else "مصروف") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Currency Selector Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !isSar,
                                onClick = { isSar = false },
                                label = { Text("ريال يمني (ر.ي.)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = isSar,
                                onClick = { isSar = true },
                                label = { Text("ريال سعودي (ر.س.)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            com.example.ui.components.AppNumberField(
                                value = amountInput,
                                onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text(if (isSar) "المبلغ (ر.س.)" else "المبلغ (ر.ي.)") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Exchange rate field if foreign currency
                        if (isSar) {
                            com.example.ui.components.AppNumberField(
                                value = customExchangeRateInput,
                                onValueChange = { customExchangeRateInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("سعر الصرف (1 ر.س = ر.ي)") },
                                placeholder = { Text(uiState.exchangeRateInput) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("ملاحظات (اختياري)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isDeposit) {
                            var depositImageUri by remember { mutableStateOf<String?>(null) }
                            val galleryLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.PickVisualMedia()
                            ) { uri: Uri? ->
                                if (uri != null) depositImageUri = uri.toString()
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "إلتقاط أو اختيار صورة إيصال الإيداع (اختياري)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                    fontSize = 11.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            galleryLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("من الاستوديو", fontSize = 11.sp)
                                    }
                                }
                                if (depositImageUri != null) {
                                    Text(
                                        text = "تم إرفاق صورة الإيصال بنجاح",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        if (!isDeposit) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isSuspended = !isSuspended }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "موقوف (إيقاف الخصم من النقد)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "عند اختيار هذا الخيار، لا يخصم المبلغ من النقد إلا عند إلغاء التوقيف لاحقاً.",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.5.sp)
                                        )
                                    }
                                    Switch(
                                        checked = isSuspended,
                                        onCheckedChange = { isSuspended = it }
                                    )
                                }
                            }
                        }
                    }
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        val finalTitle = titleInput.ifBlank { if (isDeposit) "إيداع" else "مصروف" }
                        if (amountInput.isNotBlank()) {
                            viewModel.addCashExpense(
                                title = finalTitle,
                                amount = amountInput,
                                isSar = isSar,
                                isDeposit = isDeposit,
                                customExchangeRateInput = customExchangeRateInput,
                                currencyCode = if (isSar) "SAR" else "YER",
                                notes = notesInput,
                                isSuspended = isSuspended,
                                deductFromCash = deductFromCash
                            )
                            showExpenseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeposit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (isDeposit) "إضافة وإيداع" else "إضافة وخصم")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal: Edit Expense / Deposit Dialog
    val currentEditingItem = editingExpenseItem
    if (currentEditingItem != null) {
        var titleInput by remember(currentEditingItem.id) { mutableStateOf(currentEditingItem.title) }
        var amountInput by remember(currentEditingItem.id) { mutableStateOf(currentEditingItem.amountInput) }
        var isSar by remember(currentEditingItem.id) { mutableStateOf(currentEditingItem.isSar) }
        var isDeposit by remember(currentEditingItem.id) { mutableStateOf(currentEditingItem.isDeposit) }
        var customExchangeRateInput by remember(currentEditingItem.id) {
            mutableStateOf(currentEditingItem.customExchangeRateInput)
        }
        var notesInput by remember(currentEditingItem.id) { mutableStateOf(currentEditingItem.notes) }
        var deductFromCash by remember(currentEditingItem.id) { mutableStateOf(currentEditingItem.deductFromCash) }

        AlertDialog(
            onDismissRequest = { editingExpenseItem = null },
            title = {
                Text(
                    text = if (isDeposit) "تعديل إيداع للصندوق" else "تعديل مصروف من الصندوق",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isDeposit,
                            onClick = { isDeposit = false },
                            label = { Text("مصروف (خصم 🔻)", fontSize = 11.sp, fontWeight = if (!isDeposit) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isDeposit,
                            onClick = { isDeposit = true },
                            label = { Text("إيداع (إضافة 🔺)", fontSize = 11.sp, fontWeight = if (isDeposit) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("البيان") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Currency Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isSar,
                            onClick = { isSar = false },
                            label = { Text("ريال يمني (ر.ي.)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isSar,
                            onClick = { isSar = true },
                            label = { Text("ريال سعودي (ر.س.)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    com.example.ui.components.AppNumberField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text(if (isSar) "المبلغ (ر.س.)" else "المبلغ (ر.ي.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isSar) {
                        com.example.ui.components.AppNumberField(
                            value = customExchangeRateInput,
                            onValueChange = { customExchangeRateInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("سعر الصرف (1 ر.س = ر.ي)") },
                            placeholder = { Text(uiState.exchangeRateInput) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("ملاحظات إضافية") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank() && amountInput.isNotBlank()) {
                            viewModel.updateCashExpense(
                                id = currentEditingItem.id,
                                title = titleInput,
                                amount = amountInput,
                                isSar = isSar,
                                isDeposit = isDeposit,
                                customExchangeRateInput = customExchangeRateInput,
                                currencyCode = if (isSar) "SAR" else "YER",
                                notes = notesInput,
                                deductFromCash = deductFromCash
                            )
                            editingExpenseItem = null
                        }
                    }
                ) {
                    Text("حفظ التعديلات")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingExpenseItem = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showAddCashGroupDialog) {
        AddCashGroupDialog(
            onDismiss = { showAddCashGroupDialog = false },
            onConfirm = { name, type, isExcluded ->
                viewModel.createCashGroup(name, type, isExcluded)
                showAddCashGroupDialog = false
            }
        )
    }

    if (showManageCashGroupsDialog) {
        ManageCashGroupsDialog(
            groups = uiState.cashGroups,
            onToggleEnabled = { viewModel.toggleCashGroupEnabled(it) },
            onMoveLeft = { id ->
                val idx = uiState.cashGroups.indexOfFirst { it.id == id }
                if (idx > 0) viewModel.moveCashGroup(idx, idx - 1)
            },
            onMoveRight = { id ->
                val idx = uiState.cashGroups.indexOfFirst { it.id == id }
                if (idx in 0 until uiState.cashGroups.size - 1) viewModel.moveCashGroup(idx, idx + 1)
            },
            onRenameGroup = { id, newName -> viewModel.updateCashGroupName(id, newName) },
            onDeleteGroup = { viewModel.deleteCashGroup(it) },
            onAddGroupClick = {
                showManageCashGroupsDialog = false
                showAddCashGroupDialog = true
            },
            onDismiss = { showManageCashGroupsDialog = false }
        )
    }

    if (showOrganizeCenterDialog) {
        com.example.ui.components.OrganizeCenterDialog(
            viewModel = viewModel,
            isCashBox = true,
            onDismiss = { showOrganizeCenterDialog = false },
            onOpenReports = { viewModel.navigateTo(AppScreen.REPORTS) },
            onOpenBalance = { viewModel.navigateTo(AppScreen.REPORTS) },
            onOpenCategories = { showManageCashGroupsDialog = true },
            onOpenManagement = { viewModel.navigateTo(AppScreen.MANAGEMENT) }
        )
    }

    uiState.expenseWarningMessage?.let { warningMsg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearExpenseWarningMessage() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تنبيه: لا يمكن تنفيذ العملية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            text = { Text(warningMsg, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearExpenseWarningMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حسناً، فهمت")
                }
            }
        )
    }

    if (showResetAllConfirmDialog) {
        val currentCashGroup = uiState.cashGroups.find { it.id == uiState.selectedCashGroupId }
        val denoms = currentCashGroup?.denomRows?.map { it.denomination } ?: emptyList()
        com.example.ui.components.ResetScopeDialog(
            onDismiss = { showResetAllConfirmDialog = false },
            onConfirmResetGroup = { viewModel.resetCashGroupOnly(uiState.selectedCashGroupId) },
            onConfirmResetCategory = { denom -> viewModel.resetCashCategory(uiState.selectedCashGroupId, denom) },
            onConfirmResetCash = { viewModel.resetPhysicalCashOnly() },
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
            groupId = uiState.selectedCashGroupId,
            groupName = currentCashGroup?.name ?: "المجموعة الحالية",
            availableCategories = denoms,
            allSalesGroups = uiState.groups,
            allCashGroups = uiState.cashGroups,
            isSalesSection = false
        )
    }
}

@Composable
fun ExpandableCashGroupCard(
    group: CashBoxGroupUiState,
    groupTotal: Double,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isReadOnly: Boolean,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            )
                            if (group.id == CASH_GROUP_MAIN_ID) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${AccountingFormatter.formatYer(groupTotal)})",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                        val summaryText = when (group.id) {
                            CASH_GROUP_MAIN_ID -> "الفئات الورقية المحلية المتوفرة بالريال اليمني"
                            CASH_GROUP_CURRENCIES_ID -> "العملات الأجنبية المحفوظة بالصندوق بسعر الصرف"
                            CASH_GROUP_EXPENSES_ID -> "إجمالي المبالغ والبنود المخصومة كمصروفات اليوم"
                            else -> "المبالغ المضافة والواردة للصندوق من الخارج"
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = summaryText,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (group.type) {
                        CashGroupType.EXPENSES -> MaterialTheme.colorScheme.errorContainer
                        CashGroupType.DEPOSITS -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                ) {
                    val prefix = when (group.type) {
                        CashGroupType.EXPENSES -> "-"
                        CashGroupType.DEPOSITS -> "+"
                        else -> ""
                    }
                    Text(
                        text = prefix + AccountingFormatter.formatYer(groupTotal),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (group.type) {
                                CashGroupType.EXPENSES -> MaterialTheme.colorScheme.error
                                CashGroupType.DEPOSITS -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
