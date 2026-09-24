package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.DesignSystem
import com.example.ui.theme.vibrant3d
import com.example.ui.components.BalanceStatusBadge
import com.example.ui.components.MiniBalanceStatusBadge
import com.example.ui.model.AccountingFormatter
import com.example.ui.util.AppStrings
import com.example.ui.util.LocalAppLanguage
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TicketAccountingViewModel

@Composable
fun HomeScreen(
    viewModel: TicketAccountingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = TicketAccountingViewModel.calculateSalesSummary(state)
    val lang = LocalAppLanguage.current
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Global Quick Actions (أزرار الوصول السريع)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Lock Given & Extra Toggle (قفل المعطى والإضافي)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .vibrant3d(
                        shape = RoundedCornerShape(12.dp),
                        elevation = 4.dp,
                        isDark = isDark,
                        gradientBrush = if (state.isLockGivenExtraMode) DesignSystem.primaryGradient() else DesignSystem.cardGradient(isDark)
                    )
                    .clickable { viewModel.toggleLockGivenExtraMode() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (state.isLockGivenExtraMode) Icons.Default.Block else Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (state.isLockGivenExtraMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isLockGivenExtraMode) "المعطى: مقفل" else "المعطى: متاح",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (state.isLockGivenExtraMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Read-Only Mode Toggle (وضع القراءة)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .vibrant3d(
                        shape = RoundedCornerShape(12.dp),
                        elevation = 4.dp,
                        isDark = isDark,
                        gradientBrush = if (state.isReadOnlyMode) DesignSystem.primaryGradient() else DesignSystem.cardGradient(isDark)
                    )
                    .clickable { viewModel.toggleReadOnlyMode() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (state.isReadOnlyMode) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (state.isReadOnlyMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isReadOnlyMode) "وضع القراءة" else "وضع الإدخال",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (state.isReadOnlyMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Budget Header (ترويسة الموازنة)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .vibrant3d(
                    shape = RoundedCornerShape(20.dp),
                    elevation = 6.dp,
                    isDark = isDark,
                    gradientBrush = DesignSystem.cardGradient(isDark)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "الموازنة الحالية وتطابق الصندوق",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                BalanceStatusBadge(
                    status = summary.balanceStatus,
                    balanceAmount = summary.balance
                )
            }
        }

        // Auto Daily Reset Status Banner (شريط حالة التصفير التلقائي اليومي)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.setAutoDailyResetEnabled(!state.isAutoDailyResetEnabled)
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (state.isAutoDailyResetEnabled) Color(0xFF10B981).copy(alpha = if (isDark) 0.22f else 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(
                1.dp,
                if (state.isAutoDailyResetEnabled) Color(0xFF10B981).copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (state.isAutoDailyResetEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = if (state.isAutoDailyResetEnabled) "التصفير التلقائي: مُفعّل (يومياً عند الساعة ${state.scheduledResetTime})" else "التصفير التلقائي: معطّل (انقر للتفعيل)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (state.isAutoDailyResetEnabled) (if (isDark) Color(0xFF34D399) else Color(0xFF065F46)) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (state.isAutoDailyResetEnabled) "يقوم بأرشفة السند وتصفير الوردية آلياً في الموعد المحدد" else "انقر هنا أو توجه للإعدادات لجدولة وقت التصفير",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Switch(
                    checked = state.isAutoDailyResetEnabled,
                    onCheckedChange = { viewModel.setAutoDailyResetEnabled(it) },
                    modifier = Modifier.scale(0.8f)
                )
            }
        }

        // 3. Tabbed Groups Interface (المجموعات كتبويبات)
        val enabledSalesGroups = state.groups.filter { it.isEnabled }
        val enabledCashGroups = state.cashGroups.filter { it.isEnabled }
        val exchangeRate = state.exchangeRateInput.toDoubleOrNull() ?: 380.0
        val cashYerInput = state.cashInBoxYerInput.toDoubleOrNull() ?: 0.0
        val totalCashYer = state.cashGroups.sumOf { it.getTotalYer(exchangeRate, cashYerInput) }

        var selectedMainTab by remember { mutableIntStateOf(0) }
        var selectedSalesGroupId by remember(enabledSalesGroups) { mutableStateOf<String?>(enabledSalesGroups.firstOrNull()?.id) }
        var selectedCashGroupId by remember(enabledCashGroups) { mutableStateOf<String?>(enabledCashGroups.firstOrNull()?.id) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Category TabRow (تبويبات الأقسام الرئيسية)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .vibrant3d(
                        shape = RoundedCornerShape(16.dp),
                        elevation = 5.dp,
                        isDark = isDark,
                        gradientBrush = DesignSystem.cardGradient(isDark, accent = MaterialTheme.colorScheme.primary)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = BorderStroke(
                    1.2.dp,
                    if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
            ) {
                TabRow(
                    selectedTabIndex = selectedMainTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedMainTab < tabPositions.size) {
                            Box(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedMainTab])
                                    .height(4.dp)
                                    .padding(horizontal = 24.dp)
                                    .background(
                                        color = when (selectedMainTab) {
                                            0 -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.secondary
                                        },
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                    },
                    divider = {}
                ) {
                    // Tab 0: Sales Groups
                    val isTab0Selected = selectedMainTab == 0
                    Tab(
                        selected = isTab0Selected,
                        onClick = { selectedMainTab = 0 },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
                        text = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isTab0Selected) {
                                    if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                                } else Color.Transparent,
                                border = if (isTab0Selected) {
                                    BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.6f else 0.75f))
                                } else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp),
                                        tint = if (isTab0Selected) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "المبيعات",
                                        fontWeight = if (isTab0Selected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        color = if (isTab0Selected) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFF94A3B8) else Color(0xFF334155))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isTab0Selected) MaterialTheme.colorScheme.primary else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = "${enabledSalesGroups.size}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTab0Selected) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                                        )
                                    }
                                }
                            }
                        }
                    )

                    // Tab 1: Cash Box Groups
                    val isTab1Selected = selectedMainTab == 1
                    Tab(
                        selected = isTab1Selected,
                        onClick = { selectedMainTab = 1 },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
                        text = {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isTab1Selected) {
                                    if (isDark) MaterialTheme.colorScheme.secondary.copy(alpha = 0.30f)
                                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                                } else Color.Transparent,
                                border = if (isTab1Selected) {
                                    BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = if (isDark) 0.6f else 0.75f))
                                } else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp),
                                        tint = if (isTab1Selected) MaterialTheme.colorScheme.secondary else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "الصندوق",
                                        fontWeight = if (isTab1Selected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        color = if (isTab1Selected) MaterialTheme.colorScheme.secondary else (if (isDark) Color(0xFF94A3B8) else Color(0xFF334155))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isTab1Selected) MaterialTheme.colorScheme.secondary else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = "${enabledCashGroups.size}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTab1Selected) Color.White else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155))
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // TAB 0 CONTENT: SALES GROUPS (مجموعات المبيعات)
            if (selectedMainTab == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .vibrant3d(
                            shape = RoundedCornerShape(20.dp),
                            elevation = 6.dp,
                            isDark = isDark,
                            gradientBrush = DesignSystem.cardGradient(isDark, accent = MaterialTheme.colorScheme.primary)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .vibrant3d(
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = 4.dp,
                                            isDark = isDark,
                                            baseColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = AppStrings.get("nav_direct_sales", lang),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "إجمالي: ${AccountingFormatter.formatYer(summary.totalRevenue)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            // Quick Actions: Full screen, Shortcut to Cash Box, & Direct Organize
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Shortcut to Cash Box
                                FilledTonalButton(
                                    onClick = { viewModel.navigateTo(AppScreen.CASH_BOX) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("الصندوق 💼", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }

                                // Direct Navigation to Sales Management Screen
                                IconButton(
                                    onClick = { viewModel.navigateTo(AppScreen.MANAGEMENT_SALES) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "تنظيم الأقسام",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.navigateTo(AppScreen.DIRECT_SALES) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "فتح شاشة المبيعات",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Dropdown Selector & Horizontal Group Chips (قائمة منسدلة وتبويبات أفقية للمجموعات)
                        if (enabledSalesGroups.isNotEmpty()) {
                            var showSalesGroupDropdown by remember { mutableStateOf(false) }
                            val currentSelectedGroup = enabledSalesGroups.find { it.id == selectedSalesGroupId } ?: enabledSalesGroups.first()

                            // 1. Dropdown Group Selector
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    onClick = { showSalesGroupDropdown = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val currentGroupColor = DesignSystem.getSalesGroupColor(currentSelectedGroup.id, currentSelectedGroup.color)
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(currentGroupColor)
                                            )
                                            Text(
                                                text = "القسم الحالي: ${currentSelectedGroup.name}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "تغيير القسم",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                DropdownMenu(
                                    expanded = showSalesGroupDropdown,
                                    onDismissRequest = { showSalesGroupDropdown = false }
                                ) {
                                    enabledSalesGroups.forEach { group ->
                                        val isGrpSelected = group.id == currentSelectedGroup.id
                                        val grpColor = DesignSystem.getSalesGroupColor(group.id, group.color)
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .clip(CircleShape)
                                                                .background(grpColor)
                                                        )
                                                        Text(
                                                            text = group.name,
                                                            fontWeight = if (isGrpSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isGrpSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Text(
                                                        text = AccountingFormatter.formatYer(group.totalRevenue),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = grpColor
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedSalesGroupId = group.id
                                                showSalesGroupDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // 2. Horizontal Group Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                enabledSalesGroups.forEach { group ->
                                    val isSelected = selectedSalesGroupId == group.id
                                    val groupColor = DesignSystem.getSalesGroupColor(group.id, group.color)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedSalesGroupId = group.id },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(groupColor)
                                                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                                )
                                                Column {
                                                    Text(
                                                        text = group.name,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        ),
                                                        maxLines = 2,
                                                        softWrap = true
                                                    )
                                                    Text(
                                                        text = AccountingFormatter.formatYer(group.totalRevenue),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isSelected) groupColor else (if (isDark) MaterialTheme.colorScheme.outline else Color(0xFF64748B))
                                                        )
                                                    )
                                                }
                                            }
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = groupColor, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) groupColor else (if (isDark) groupColor.copy(alpha = 0.35f) else Color(0xFFCBD5E1))
                                        ),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = if (isDark) Color.Transparent else Color.White,
                                            selectedContainerColor = groupColor.copy(alpha = if (isDark) 0.32f else 0.16f),
                                            labelColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF334155),
                                            selectedLabelColor = if (isDark) Color.White else groupColor
                                        )
                                    )
                                }
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )

                            // Content: Selected group card
                            val selectedGroup = enabledSalesGroups.find { it.id == selectedSalesGroupId } ?: enabledSalesGroups.firstOrNull()
                            if (selectedGroup != null) {
                                val groupColor = DesignSystem.getSalesGroupColor(selectedGroup.id, selectedGroup.color)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, groupColor.copy(alpha = if (isDark) 0.5f else 0.4f), RoundedCornerShape(16.dp))
                                        .vibrant3d(
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = 4.dp,
                                            isDark = isDark,
                                            gradientBrush = DesignSystem.cardGradient(isDark, accent = groupColor)
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Column {
                                        // Top vibrant gradient accent
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(groupColor, groupColor.copy(alpha = 0.4f), Color.Transparent)
                                                    )
                                                )
                                        )
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(groupColor)
                                                            .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                                    )
                                                    Text(
                                                        text = selectedGroup.name,
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        softWrap = true,
                                                        maxLines = 3
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                val isDirect = selectedGroup.type == com.example.ui.model.SalesGroupType.DIRECT_ENTRY
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = groupColor.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, groupColor.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = if (isDirect) "إدخال مباشر" else "فئات تذاكر",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        color = groupColor
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column {
                                                    val isDirect = selectedGroup.type == com.example.ui.model.SalesGroupType.DIRECT_ENTRY
                                                    val categoryCount = if (isDirect) selectedGroup.directEntries.size else selectedGroup.rows.size
                                                    val countLabel = if (isDirect) "بنود إدخال مسجلة" else "فئات تذاكر معتمدة"
                                                    Text(
                                                        text = "$categoryCount $countLabel",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = AccountingFormatter.formatYer(selectedGroup.totalRevenue),
                                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = groupColor
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.selectGroup(selectedGroup.id)
                                                        viewModel.navigateTo(AppScreen.DIRECT_SALES)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = groupColor),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("فتح وبدء الإدخال", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Full-width grid of all groups
                                val chunkedGroups = enabledSalesGroups.chunked(2)
                                chunkedGroups.forEach { rowGroups ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowGroups.forEach { group ->
                                            val groupColor = DesignSystem.getSalesGroupColor(group.id, group.color)
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(1.5.dp, groupColor.copy(alpha = if (isDark) 0.5f else 0.35f), RoundedCornerShape(16.dp))
                                                    .vibrant3d(
                                                        shape = RoundedCornerShape(16.dp),
                                                        elevation = 5.dp,
                                                        isDark = isDark,
                                                        gradientBrush = DesignSystem.cardGradient(isDark, accent = groupColor)
                                                    )
                                                    .clickable {
                                                        viewModel.selectGroup(group.id)
                                                        viewModel.navigateTo(AppScreen.DIRECT_SALES)
                                                    },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                            ) {
                                                Column {
                                                    // Top vibrant group bar
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(3.5.dp)
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    listOf(groupColor, groupColor.copy(alpha = 0.4f), Color.Transparent)
                                                                )
                                                            )
                                                    )
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = group.name,
                                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                softWrap = true,
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clip(CircleShape)
                                                                    .background(groupColor.copy(alpha = if (isDark) 0.3f else 0.15f))
                                                                    .border(1.dp, groupColor.copy(alpha = 0.4f), CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.ConfirmationNumber,
                                                                    contentDescription = null,
                                                                    tint = groupColor,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                        val isDirect = group.type == com.example.ui.model.SalesGroupType.DIRECT_ENTRY
                                                        val categoryCount = if (isDirect) group.directEntries.size else group.rows.size
                                                        val countLabel = if (isDirect) "بنود" else "فئات"
                                                        Text(
                                                            text = "$categoryCount $countLabel",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = AccountingFormatter.formatYer(group.totalRevenue),
                                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                            color = groupColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (rowGroups.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "لا توجد مجموعات مبيعات مفعلة",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        // Footer Summary
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إجمالي مبيعات الأنشطة",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AccountingFormatter.formatYer(summary.totalRevenue),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // TAB 1 CONTENT: CASH BOX GROUPS (مجموعات الصندوق)
            if (selectedMainTab == 1) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .vibrant3d(
                            shape = RoundedCornerShape(20.dp),
                            elevation = 6.dp,
                            isDark = isDark,
                            gradientBrush = DesignSystem.cardGradient(isDark, accent = MaterialTheme.colorScheme.secondary)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .vibrant3d(
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = 4.dp,
                                            isDark = isDark,
                                            baseColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = AppStrings.get("nav_cash_box", lang),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "إجمالي: ${AccountingFormatter.formatYer(totalCashYer)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }
                            }

                            // Quick Actions: Full screen, Shortcut to Direct Sales, & Direct Organize
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Shortcut to Direct Sales
                                FilledTonalButton(
                                    onClick = { viewModel.navigateTo(AppScreen.DIRECT_SALES) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ConfirmationNumber,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("المبيعات 🎟️", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }

                                // Direct Navigation to Cash Management Screen
                                IconButton(
                                    onClick = { viewModel.navigateTo(AppScreen.MANAGEMENT_CASH) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "تنظيم الأقسام",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.navigateTo(AppScreen.CASH_BOX) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "فتح شاشة الصندوق",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Dropdown Selector & Horizontal Group Chips (قائمة منسدلة وتبويبات أفقية للمجموعات)
                        if (enabledCashGroups.isNotEmpty()) {
                            var showCashGroupDropdown by remember { mutableStateOf(false) }
                            val currentSelectedCashGroup = enabledCashGroups.find { it.id == selectedCashGroupId } ?: enabledCashGroups.first()

                            // 1. Dropdown Group Selector
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    onClick = { showCashGroupDropdown = true },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val currentGroupColor = DesignSystem.getCashGroupColor(currentSelectedCashGroup.id, currentSelectedCashGroup.color)
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(currentGroupColor)
                                            )
                                            Text(
                                                text = "القسم الحالي: ${currentSelectedCashGroup.name}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "تغيير القسم",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }

                                DropdownMenu(
                                    expanded = showCashGroupDropdown,
                                    onDismissRequest = { showCashGroupDropdown = false }
                                ) {
                                    enabledCashGroups.forEach { group ->
                                        val isGrpSelected = group.id == currentSelectedCashGroup.id
                                        val grpColor = DesignSystem.getCashGroupColor(group.id, group.color)
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .clip(CircleShape)
                                                                .background(grpColor)
                                                        )
                                                        Text(
                                                            text = group.name,
                                                            fontWeight = if (isGrpSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isGrpSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    Text(
                                                        text = AccountingFormatter.formatYer(group.getTotalYer(exchangeRate, cashYerInput)),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = grpColor
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCashGroupId = group.id
                                                showCashGroupDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // 2. Horizontal Group Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                enabledCashGroups.forEach { group ->
                                    val isSelected = selectedCashGroupId == group.id
                                    val groupColor = DesignSystem.getCashGroupColor(group.id, group.color)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCashGroupId = group.id },
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(groupColor)
                                                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                                )
                                                Column {
                                                    Text(
                                                        text = group.name,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        ),
                                                        maxLines = 2,
                                                        softWrap = true
                                                    )
                                                    Text(
                                                        text = AccountingFormatter.formatYer(group.getTotalYer(exchangeRate, cashYerInput)),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isSelected) groupColor else (if (isDark) MaterialTheme.colorScheme.outline else Color(0xFF64748B))
                                                        )
                                                    )
                                                }
                                            }
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = groupColor, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) groupColor else (if (isDark) groupColor.copy(alpha = 0.35f) else Color(0xFFCBD5E1))
                                        ),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = if (isDark) Color.Transparent else Color.White,
                                            selectedContainerColor = groupColor.copy(alpha = if (isDark) 0.32f else 0.16f),
                                            labelColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF334155),
                                            selectedLabelColor = if (isDark) Color.White else groupColor
                                        )
                                    )
                                }
                            }

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            )

                            // Content: Selected cash group card
                            val selectedCashGroup = enabledCashGroups.find { it.id == selectedCashGroupId } ?: enabledCashGroups.firstOrNull()
                            if (selectedCashGroup != null) {
                                val groupColor = DesignSystem.getCashGroupColor(selectedCashGroup.id, selectedCashGroup.color)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, groupColor.copy(alpha = if (isDark) 0.5f else 0.4f), RoundedCornerShape(16.dp))
                                        .vibrant3d(
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = 4.dp,
                                            isDark = isDark,
                                            gradientBrush = DesignSystem.cardGradient(isDark, accent = groupColor)
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Column {
                                        // Top vibrant gradient accent
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(groupColor, groupColor.copy(alpha = 0.4f), Color.Transparent)
                                                    )
                                                )
                                        )
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(groupColor)
                                                            .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                                    )
                                                    Text(
                                                        text = selectedCashGroup.name,
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        softWrap = true,
                                                        maxLines = 3
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                val subtitleText = when {
                                                    selectedCashGroup.id == com.example.ui.model.CASH_GROUP_MAIN_ID -> "النقد الفعلي"
                                                    selectedCashGroup.type == com.example.ui.model.CashGroupType.DENOMINATIONS -> "فئات نقدية"
                                                    else -> "سندات ومقبوضات"
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = groupColor.copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, groupColor.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = subtitleText,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        color = groupColor
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column {
                                                    val detailText = when {
                                                        selectedCashGroup.id == com.example.ui.model.CASH_GROUP_MAIN_ID -> "رصيد الدرج النقدي"
                                                        selectedCashGroup.type == com.example.ui.model.CashGroupType.DENOMINATIONS -> "${selectedCashGroup.activeDenomRows.size} فئات عملات"
                                                        else -> "${selectedCashGroup.directEntries.size} بنود مسجلة"
                                                    }
                                                    Text(
                                                        text = detailText,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = AccountingFormatter.formatYer(selectedCashGroup.getTotalYer(exchangeRate, cashYerInput)),
                                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = groupColor
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        viewModel.selectCashGroup(selectedCashGroup.id)
                                                        viewModel.navigateTo(AppScreen.CASH_BOX)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = groupColor),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("فتح في الصندوق", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Full-width grid of all cash groups
                                val chunkedCashGroups = enabledCashGroups.chunked(2)
                                chunkedCashGroups.forEach { rowGroups ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowGroups.forEach { group ->
                                            val groupColor = DesignSystem.getCashGroupColor(group.id, group.color)
                                            Card(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(1.5.dp, groupColor.copy(alpha = if (isDark) 0.5f else 0.35f), RoundedCornerShape(16.dp))
                                                    .vibrant3d(
                                                        shape = RoundedCornerShape(16.dp),
                                                        elevation = 5.dp,
                                                        isDark = isDark,
                                                        gradientBrush = DesignSystem.cardGradient(isDark, accent = groupColor)
                                                    )
                                                    .clickable {
                                                        viewModel.selectCashGroup(group.id)
                                                        viewModel.navigateTo(AppScreen.CASH_BOX)
                                                    },
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                            ) {
                                                Column {
                                                    // Top vibrant group bar
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(3.5.dp)
                                                            .background(
                                                                Brush.horizontalGradient(
                                                                    listOf(groupColor, groupColor.copy(alpha = 0.4f), Color.Transparent)
                                                                )
                                                            )
                                                    )
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = group.name,
                                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                softWrap = true,
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clip(CircleShape)
                                                                    .background(groupColor.copy(alpha = if (isDark) 0.3f else 0.15f))
                                                                    .border(1.dp, groupColor.copy(alpha = 0.4f), CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.AccountBalanceWallet,
                                                                    contentDescription = null,
                                                                    tint = groupColor,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                        val subtitleText = when {
                                                            group.id == com.example.ui.model.CASH_GROUP_MAIN_ID -> "النقد الفعلي"
                                                            group.type == com.example.ui.model.CashGroupType.DENOMINATIONS -> "${group.activeDenomRows.size} فئات"
                                                            else -> "${group.directEntries.size} بنود"
                                                        }
                                                        Text(
                                                            text = subtitleText,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = AccountingFormatter.formatYer(group.getTotalYer(exchangeRate, cashYerInput)),
                                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                            color = groupColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (rowGroups.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "لا توجد مجموعات صندوق مفعلة",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        // Footer Summary
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إجمالي الصندوق والمقبوضات",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AccountingFormatter.formatYer(totalCashYer),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HomeSectionCard(
    title: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    onOrganizeClick: () -> Unit,
    onAddGroupClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    
    val infiniteTransition = rememberInfiniteTransition(label = "icon_float")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .vibrant3d(
                shape = RoundedCornerShape(20.dp),
                elevation = 6.dp,
                isDark = isDark,
                gradientBrush = DesignSystem.cardGradient(isDark, accent = color)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .scale(scale)
                        .vibrant3d(
                            shape = RoundedCornerShape(12.dp),
                            elevation = 4.dp,
                            isDark = isDark,
                            baseColor = color.copy(alpha = 0.2f)
                        )
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).clickable { onClick() }
                )
                
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "تنظيم",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("تنظيم الأقسام") },
                            onClick = {
                                showMenu = false
                                onOrganizeClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        if (onAddGroupClick != null) {
                            DropdownMenuItem(
                                text = { Text("إضافة مجموعة جديدة") },
                                onClick = {
                                    showMenu = false
                                    onAddGroupClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }
            }
            
            content()
        }
    }
}
