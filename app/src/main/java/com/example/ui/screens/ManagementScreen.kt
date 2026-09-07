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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.derivedStateOf
import com.example.ui.components.ColorPickerDialog
import com.example.ui.theme.DesignSystem
import com.example.ui.viewmodel.PRESET_APP_THEMES
import com.example.ui.viewmodel.AppThemePreset
import com.example.ui.viewmodel.TicketAccountingViewModel

/**
 * بطاقة عرض نمط عمل جاهز
 */
@Composable
fun ModeItemCard(
    title: String,
    description: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onApply: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp))
                        if (isActive) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text("نشط", fontSize = 8.5.sp, color = Color.White, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(description, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                }
            }
            if (!isActive) {
                OutlinedButton(
                    onClick = onApply,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("تطبيق", fontSize = 10.5.sp)
                }
            }
        }
    }
}

@Composable
fun ManagementScreen(
    viewModel: TicketAccountingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val isDark = isSystemInDarkTheme()

    var selectedSectionTab by remember { mutableIntStateOf(0) } // 0: الأوضاع والقوالب, 1: المظهر والسمات

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var presetDescInput by remember { mutableStateOf("") }

    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val customNightPrimary by viewModel.customNightPrimary.collectAsStateWithLifecycle()
    val customDayPrimary by viewModel.customDayPrimary.collectAsStateWithLifecycle()

    var showDayColorPicker by remember { mutableStateOf(false) }
    var showNightColorPicker by remember { mutableStateOf(false) }
    var showReportColorPicker by remember { mutableStateOf(false) }
    var showTableCardBgColorPicker by remember { mutableStateOf(false) }
    var showTableHeaderBgColorPicker by remember { mutableStateOf(false) }
    var showTableHeaderTextColorPicker by remember { mutableStateOf(false) }
    var showTableBorderColorPicker by remember { mutableStateOf(false) }
    var showActiveTabColorPicker by remember { mutableStateOf(false) }
    var showInactiveTabColorPicker by remember { mutableStateOf(false) }
    var showAppBgColorPicker by remember { mutableStateOf(false) }

    var themeTabState by remember { mutableIntStateOf(0) }
    var selectedColorOptionIndex by remember { mutableIntStateOf(0) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .testTag("management_screen"),
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
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.DashboardCustomize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الأوضاع التشغيلية والمظهر والسمات",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "تخصيص أنماط العرض، قواعد العمل المحاسبية، وتفعيل أو تعطيل الأوضاع",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }

        // Section Tabs Switch: Modes vs Themes
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedSectionTab == 0,
                onClick = { selectedSectionTab = 0 },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                modifier = Modifier.testTag("seg_modes_management")
            ) {
                Icon(Icons.Default.DashboardCustomize, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("الأوضاع والقوالب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            SegmentedButton(
                selected = selectedSectionTab == 1,
                onClick = { selectedSectionTab = 1 },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                modifier = Modifier.testTag("seg_themes_management")
            ) {
                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("المظهر والسمات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            when (selectedSectionTab) {
                0 -> {
                    // ==================== TAB 0: MODES & PRESETS ====================
                    // 1. Built-in Operating Profiles
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "أوضاع العمل والتشغيل الجاهزة",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    )
                                }
                                Text(
                                    text = "انقر لتفعيل وضع عمل متكامل بضغطة زر واحدة وفقاً لمتطلبات ورديتك الحالية:",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.5.sp)
                                )

                                // Profile 1: Full
                                ModeItemCard(
                                    title = "الوضع الشامل المتكامل",
                                    description = "تفعيل كافة مجموعات المبيعات، الفئات، الصندوق، والعملات.",
                                    isActive = uiState.activePresetId == "standard_full",
                                    icon = Icons.Default.AllInclusive,
                                    onApply = { viewModel.applyBuiltInMode("standard_full") }
                                )

                                // Profile 2: Fast Tickets
                                ModeItemCard(
                                    title = "وضع التذاكر والبيع السريع",
                                    description = "التركيز على فئات التذاكر الأساسية وإخفاء الأقسام الثانوية لتسريع الإدخال.",
                                    isActive = uiState.activePresetId == "fast_tickets",
                                    icon = Icons.Default.FlashOn,
                                    onApply = { viewModel.applyBuiltInMode("fast_tickets") }
                                )

                                // Profile 3: Cash & Auditor
                                ModeItemCard(
                                    title = "وضع المحاسب والتدقيق المالي",
                                    description = "تفعيل النقد، المصروفات، الإيداعات، والعملات الأجنبية لمطابقة الصندوق.",
                                    isActive = uiState.activePresetId == "cash_auditor",
                                    icon = Icons.Default.AccountBalance,
                                    onApply = { viewModel.applyBuiltInMode("cash_auditor") }
                                )
                            }
                        }
                    }

                    // 2.5 Day Closing and Security Card (إغلاق اليوم ووضع القراءة)
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.isDayClosed) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (uiState.isDayClosed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (uiState.isDayClosed) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (uiState.isDayClosed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "إغلاق واعتماد اليوم الحسابي ووضع القراءة",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    )
                                }

                                Text(
                                    text = if (uiState.isDayClosed)
                                        "اليوم مغلق ومُعتمد رسمياً 🔒. كافة الإدخالات والتعديلات متوقفة ومحمية ضد أي تغيير."
                                    else
                                        "عند انتهاء الوردية أو اليوم الحسابي، يمكنك إغلاق اليوم لتجميد كافة الإدخالات ومنع أي تعديل إلا بعد تحذير شديد.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (uiState.isDayClosed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                        fontSize = 11.sp
                                    )
                                )

                                // Close Day Button
                                Button(
                                    onClick = { viewModel.requestCloseDay() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.isDayClosed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isDayClosed) Icons.Default.LockReset else Icons.Default.LockClock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (uiState.isDayClosed) "فك إغلاق اليوم (يتطلب تحذير شديد) ⚠️" else "إغلاق واعتماد اليوم الآن 🔒",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Read Only Mode Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "وضع القراءة فقط",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        )
                                        Text(
                                            text = "تجميد النقر على الحقول ومنع التعديل العرضي للأرقام.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                                        )
                                    }
                                    Switch(
                                        checked = uiState.isReadOnlyMode,
                                        onCheckedChange = { viewModel.toggleReadOnlyMode() },
                                        modifier = Modifier.testTag("switch_read_only_management")
                                    )
                                }
                            }
                        }
                    }

                    // 3. Group Balance & Report Options in Modes Menu
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "خيارات الموازنة والتقرير لكل مجموعة",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    )
                                }
                                Text(
                                    text = "حدد المجموعات التي تدخل في حساب الموازنة المالية وتظهر في التقرير النهائي:",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.5.sp)
                                )

                                uiState.groups.forEach { group ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = group.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                                )
                                                if (group.id == GROUP_INTERNET_ID) {
                                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                                                        Text("معطلة افتراضياً", fontSize = 8.5.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                } else if (group.id == GROUP_GAME_CARDS_ID) {
                                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                                        Text("مفعلة افتراضياً", fontSize = 8.5.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clickable { viewModel.toggleSalesGroupAddToReport(group.id) }
                                                        .padding(2.dp)
                                                ) {
                                                    Checkbox(
                                                        checked = group.addToReport,
                                                        onCheckedChange = { viewModel.toggleSalesGroupAddToReport(group.id, it) },
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("خيار التقرير", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clickable { viewModel.toggleSalesGroupAddToBalance(group.id) }
                                                        .padding(2.dp)
                                                ) {
                                                    Checkbox(
                                                        checked = group.addToBalance && !group.isExcludedFromBalance,
                                                        onCheckedChange = { viewModel.toggleSalesGroupAddToBalance(group.id, it) },
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("خيار الموازنة", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Custom Saved Presets
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "أوضاعك المخصصة المحفوظة",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        )
                                    }
                                    Button(
                                        onClick = { 
                                            presetNameInput = ""
                                            presetDescInput = ""
                                            showSavePresetDialog = true 
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("حفظ الحالي", fontSize = 10.5.sp)
                                    }
                                }

                                if (uiState.presets.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "لا توجد أوضاع مخصصة محفوظة حالياً. احفظ إعداداتك المفضلة للوصول السريع.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.5.sp),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                } else {
                                    uiState.presets.forEach { preset ->
                                        val isActive = uiState.activePresetId == preset.id
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            border = BorderStroke(1.dp, if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Icon(
                                                        imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder,
                                                        contentDescription = null,
                                                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(preset.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp))
                                                        if (preset.description.isNotBlank()) {
                                                            Text(preset.description, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                                                        }
                                                    }
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    if (!isActive) {
                                                        Button(
                                                            onClick = { viewModel.applyPreset(preset.id) },
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Text("تطبيق", fontSize = 10.5.sp)
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.deletePreset(preset.id) },
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
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

                1 -> {
                    // ==================== TAB 1: THEMES & APPEARANCE ====================
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Tab Headers: 1. Day/Night Theme, 2. Presets, 3. App Background, 4. Color Customization Dropdown
                                ScrollableTabRow(
                                    selectedTabIndex = themeTabState,
                                    edgePadding = 0.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Tab(
                                        selected = themeTabState == 0,
                                        onClick = { themeTabState = 0 },
                                        text = { Text("السمة النهارية والليلية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.Brightness4, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    )
                                    Tab(
                                        selected = themeTabState == 1,
                                        onClick = { themeTabState = 1 },
                                        text = { Text("السمات الجاهزة والمعدنية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    )
                                    Tab(
                                        selected = themeTabState == 2,
                                        onClick = { themeTabState = 2 },
                                        text = { Text("خلفية التطبيق", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    )
                                    Tab(
                                        selected = themeTabState == 3,
                                        onClick = { themeTabState = 3 },
                                        text = { Text("خيارات الألوان", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                when (themeTabState) {
                                    // TAB 0: DAY / NIGHT THEME SELECTION & QUICK TOGGLE
                                    0 -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "وضع السمة الحالي (نهاري / ليلي)",
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Text(
                                                        text = if (isDarkTheme) "الوضع الليلي مفعّل حالياً" else "الوضع النهاري مفعّل حالياً",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                                                    )
                                                }
                                                Switch(
                                                    checked = isDarkTheme,
                                                    onCheckedChange = { viewModel.toggleTheme() },
                                                    modifier = Modifier.testTag("switch_theme_mode")
                                                )
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                            OutlinedButton(
                                                onClick = { themeTabState = 3 },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("الانتقال لخيارات تخصيص الألوان المتقدمة", fontSize = 11.5.sp)
                                            }
                                        }
                                    }

                                    // TAB 1: PRESET THEMES LIST (WITH DAY / NIGHT FILTER TABS)
                                    1 -> {
                                        var presetFilterMode by remember { mutableIntStateOf(0) } // 0: All, 1: Day, 2: Night

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "اختر سمة محاسبية أو خلفية معدنية مسبوكة لتطبيقها فوراً على الواجهة والجداول:",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                FilterChip(
                                                    selected = presetFilterMode == 0,
                                                    onClick = { presetFilterMode = 0 },
                                                    label = { Text("جميع السمات", fontSize = 11.sp) }
                                                )
                                                FilterChip(
                                                    selected = presetFilterMode == 1,
                                                    onClick = { presetFilterMode = 1 },
                                                    label = { Text("☀️ سمات نهارية", fontSize = 11.sp) },
                                                    leadingIcon = { Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                                )
                                                FilterChip(
                                                    selected = presetFilterMode == 2,
                                                    onClick = { presetFilterMode = 2 },
                                                    label = { Text("🌙 سمات ليلية", fontSize = 11.sp) },
                                                    leadingIcon = { Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                                )
                                            }

                                            val filteredPresets = PRESET_APP_THEMES.filter { preset ->
                                                when (presetFilterMode) {
                                                    1 -> !preset.isNightMode
                                                    2 -> preset.isNightMode
                                                    else -> true
                                                }
                                            }

                                            // Skin (جلد السمة) Section
                                            Text(
                                                text = "جلد السمة البصري (Skin) لإضفاء الحيوية:",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                            val skinOptions = listOf("كلاسيكي (Classic)", "معدني ناصع (Metallic)", "نيون حيوي (Vibrant Neon)", "ذهبي ملكي (Royal Gold)", "أخضر زمردي (Emerald)")
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                skinOptions.forEach { skin ->
                                                    val isSkinSelected = uiState.appThemeSkin == skin
                                                    FilterChip(
                                                        selected = isSkinSelected,
                                                        onClick = { viewModel.setAppThemeSkin(skin) },
                                                        label = { Text(skin, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                        leadingIcon = {
                                                            if (isSkinSelected) {
                                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                            }
                                                        }
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            filteredPresets.forEach { preset ->
                                                val isApplied = (uiState.selectedThemePresetName == preset.name) ||
                                                    (isDarkTheme == preset.isNightMode && uiState.reportPrimaryColor == preset.primaryColor && uiState.tableHeaderBgColor == preset.headerBgColor)
                                                
                                                Card(
                                                    onClick = { if (!isApplied) viewModel.applyThemePreset(preset) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isApplied) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                                    ),
                                                    border = BorderStroke(
                                                        width = if (isApplied) 2.dp else 1.dp,
                                                        color = if (isApplied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Stable indicator (RadioButton)
                                                        RadioButton(
                                                            selected = isApplied,
                                                            onClick = null, // Handled by Card
                                                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                                        )

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = preset.name,
                                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                                )
                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                Surface(
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    color = if (preset.isNightMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                                                                ) {
                                                                    Text(
                                                                        text = if (preset.isNightMode) "🌙 ليلي" else "☀️ نهاري",
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = if (preset.isNightMode) Color(0xFFFFD54F) else Color(0xFF1E293B),
                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                                    )
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = preset.description,
                                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                                                            )
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(Color(preset.primaryColor)).border(1.dp, Color.Black.copy(alpha=0.1f), CircleShape))
                                                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(Color(preset.headerBgColor)).border(1.dp, Color.Black.copy(alpha=0.1f), CircleShape))
                                                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(Color(preset.borderColor)).border(1.dp, Color.Black.copy(alpha=0.1f), CircleShape))
                                                            }
                                                        }

                                                        // Fixed size "Apply" indicator/button area
                                                        if (!isApplied) {
                                                            Icon(
                                                                imageVector = Icons.Default.ChevronLeft,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.outline,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        } else {
                                                            Icon(
                                                                imageVector = Icons.Default.CheckCircle,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // TAB 2: GLOBAL APP BACKGROUND SELECTION
                                    2 -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "اختر خلفية مخصصة أو معدنية مسبوكة لتظهر خلف كافة شاشات التطبيق:",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                                            )

                                            val bgOptions = listOf(
                                                Triple("DEFAULT", "⚪ الخلفية القياسية (تلقائية)", "تعتمد على ألوان الواجهة والسمة المحددة"),
                                                Triple("TEXTURE_CARBON_FIBER", "🏎️ خلفية ألياف الكربون (كاربون فايبر)", "نسيج كربون فايبر رياضي منسوج فاحم بتباين عالي"),
                                                Triple("TEXTURE_PERFORATED_LEATHER", "💺 خلفية الجلد المخرم الفاخر", "مظهر الجلد الملكي مع تخريمات ناعمة وتطريز أنيق"),
                                                Triple("TEXTURE_GRANULAR_WALL", "🏛️ خلفية الجدار المحبب المعماري", "مظهر جداري إسمنتي حجري محبب بطابع عصري مريح للبصر"),
                                                Triple("TEXTURE_WOOD_GRAIN", "🪵 خلفية الخشب الطبيعي الجميل", "خشب الجوز والأرو الفاخر بتدرجات خشبية دافئة"),
                                                Triple("METALLIC_STEEL", "⚙️ خلفية الفولاذ المصقول المعدني", "تدرجات الستيل والفولاذ الفضي الناصع"),
                                                Triple("METALLIC_GOLD", "🪙 خلفية الذهب المعدني البراق", "تدرجات الذهب الأصفر والكهرمان اللامع"),
                                                Triple("METALLIC_SILVER", "⚙️ خلفية الفضة المعدنية اللامعة", "تصميم ناصع بلمسات الفضة المصقولة"),
                                                Triple("METALLIC_TITANIUM", "🛠️ خلفية التيتانيوم الصلب الداكن", "مظهر معدني صلب داكن مريح للعين"),
                                                Triple("METALLIC_COPPER", "🧱 خلفية البرونز والنحاس المعشق", "تدرجات النحاس والبرونز الفاخرة"),
                                                Triple("CUSTOM_COLOR", "🎨 خلفية بلون مخصص بالكامل", "اختر لوناً مخصصاً خلف أجزاء الشاشة")
                                            )

                                            bgOptions.forEach { (code, title, desc) ->
                                                val isSelected = uiState.appBackgroundStyle == code
                                                OutlinedCard(
                                                    onClick = {
                                                        if (code == "CUSTOM_COLOR") {
                                                            showAppBgColorPicker = true
                                                        } else {
                                                            viewModel.setAppBackgroundStyle(code)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                                    colors = CardDefaults.outlinedCardColors(
                                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp))
                                                            Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp))
                                                        }
                                                        if (isSelected) {
                                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                        } else if (code == "CUSTOM_COLOR") {
                                                            Button(
                                                                onClick = { showAppBgColorPicker = true },
                                                                shape = RoundedCornerShape(8.dp),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                                modifier = Modifier.height(28.dp)
                                                            ) {
                                                                Text("تحديد اللون", fontSize = 10.5.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // TAB 3: DROPDOWN MENU COLOR CUSTOMIZATION
                                    3 -> {
                                        val defaultSurface = MaterialTheme.colorScheme.surface
                                        val defaultOnSurface = MaterialTheme.colorScheme.onSurface
                                        val defaultPrimary = MaterialTheme.colorScheme.primary
                                        val defaultOutlineVariant = MaterialTheme.colorScheme.outlineVariant
                                        val defaultSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant

                                        val colorOptions = remember(uiState, customDayPrimary, customNightPrimary) {
                                            listOf(
                                                ThemeColorOption("سمة النهار", "اللون الأساسي للوضع النهاري", Icons.Default.WbSunny, customDayPrimary ?: defaultPrimary) { showDayColorPicker = true },
                                                ThemeColorOption("سمة الليل", "اللون الأساسي للوضع الليلي", Icons.Default.NightsStay, customNightPrimary ?: defaultPrimary) { showNightColorPicker = true },
                                                ThemeColorOption("التقارير المالية", "عناوين الفواتير المطبوعة", Icons.Default.Assessment, Color(uiState.reportPrimaryColor)) { showReportColorPicker = true },
                                                ThemeColorOption("رؤوس الجداول", "شريط العناوين في الجداول", Icons.Default.TableChart, Color(uiState.tableHeaderBgColor)) { showTableHeaderBgColorPicker = true },
                                                ThemeColorOption("التبويبات النشطة", "لون العنصر المختار حالياً", Icons.Default.Tab, Color(uiState.activeTabColor)) { showActiveTabColorPicker = true },
                                                ThemeColorOption("خلفية الجداول", "الخلفية العامة لبطاقات الجداول", Icons.Default.Dashboard, uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.tableCardBg) ?: defaultSurface) { showTableCardBgColorPicker = true },
                                                ThemeColorOption("نصوص رؤوس الجداول", "لون النص داخل عناوين الأعمدة", Icons.Default.FormatColorText, uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.tableHeaderText) ?: defaultOnSurface) { showTableHeaderTextColorPicker = true },
                                                ThemeColorOption("حدود الجداول", "إطار وخلايا الجداول الحسابية", Icons.Default.BorderAll, uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.tableBorderColor) ?: defaultOutlineVariant) { showTableBorderColorPicker = true },
                                                ThemeColorOption("التبويبات غير النشطة", "لون أزرار التبويبات الثانوية", Icons.Default.TabUnselected, uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupInactiveTabBg) ?: defaultSurfaceVariant) { showInactiveTabColorPicker = true },
                                            )
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(
                                                text = "تخصيص ألوان الواجهة بدقة:",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            )
                                            
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedCard(
                                                    onClick = { isDropdownExpanded = true },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(14.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(colorOptions[selectedColorOptionIndex].icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text(
                                                                text = colorOptions[selectedColorOptionIndex].title,
                                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                                            )
                                                        }
                                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = isDropdownExpanded,
                                                    onDismissRequest = { isDropdownExpanded = false },
                                                    modifier = Modifier.fillMaxWidth(0.9f)
                                                ) {
                                                    colorOptions.forEachIndexed { index, option ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Column {
                                                                    Text(option.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                                                    Text(option.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                                                }
                                                            },
                                                            onClick = {
                                                                selectedColorOptionIndex = index
                                                                isDropdownExpanded = false
                                                            },
                                                            leadingIcon = {
                                                                Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(option.color).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape))
                                                            },
                                                            trailingIcon = if (index == selectedColorOptionIndex) {
                                                                { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                                                            } else null
                                                        )
                                                    }
                                                }
                                            }

                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    ColorSettingRow(
                                                        title = colorOptions[selectedColorOptionIndex].title,
                                                        subtitle = colorOptions[selectedColorOptionIndex].subtitle,
                                                        color = colorOptions[selectedColorOptionIndex].color,
                                                        onClick = colorOptions[selectedColorOptionIndex].onPick
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

    // Dialogs
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("حفظ الوضع الحالي كقالب مخصص") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("اسم الوضع") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = presetDescInput,
                        onValueChange = { presetDescInput = it },
                        label = { Text("وصف مختصر (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            viewModel.saveCurrentAsPreset(presetNameInput, presetDescInput)
                            showSavePresetDialog = false
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Day Color Picker Dialog
    if (showDayColorPicker) {
        ColorPickerDialog(
            title = "تحديد اللون الرئيسي للوضع النهاري",
            initialColor = customDayPrimary ?: MaterialTheme.colorScheme.primary,
            onDismiss = { showDayColorPicker = false },
            onColorSelected = { selected ->
                viewModel.setCustomDayPrimary(selected)
                showDayColorPicker = false
            }
        )
    }

    // Night Color Picker Dialog
    if (showNightColorPicker) {
        ColorPickerDialog(
            title = "تحديد اللون الرئيسي للوضع الليلي",
            initialColor = customNightPrimary ?: MaterialTheme.colorScheme.primary,
            onDismiss = { showNightColorPicker = false },
            onColorSelected = { selected ->
                viewModel.setCustomNightPrimary(selected)
                showNightColorPicker = false
            }
        )
    }

    // Report Color Picker Dialog
    if (showReportColorPicker) {
        ColorPickerDialog(
            title = "تحديد لون التقارير المالية",
            initialColor = Color(uiState.reportPrimaryColor),
            onDismiss = { showReportColorPicker = false },
            onColorSelected = { selected ->
                viewModel.setReportPrimaryColor(selected.value.toLong())
                showReportColorPicker = false
            }
        )
    }

    if (showTableCardBgColorPicker) {
        ColorPickerDialog(
            title = "لون خلفية الجداول",
            initialColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.tableCardBg) ?: MaterialTheme.colorScheme.surface,
            onDismiss = { showTableCardBgColorPicker = false },
            onColorSelected = { selected ->
                viewModel.updateCustomColorTheme(tableCardBg = selected.value.toLong())
                showTableCardBgColorPicker = false
            }
        )
    }

    if (showTableHeaderTextColorPicker) {
        ColorPickerDialog(
            title = "لون نص رؤوس الأعمدة والجداول",
            initialColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.tableHeaderText) ?: MaterialTheme.colorScheme.onSurface,
            onDismiss = { showTableHeaderTextColorPicker = false },
            onColorSelected = { selected ->
                viewModel.updateCustomColorTheme(tableHeaderText = selected.value.toLong())
                showTableHeaderTextColorPicker = false
            }
        )
    }

    if (showTableBorderColorPicker) {
        ColorPickerDialog(
            title = "لون حدود الخلايا والجداول",
            initialColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.tableBorderColor) ?: MaterialTheme.colorScheme.outlineVariant,
            onDismiss = { showTableBorderColorPicker = false },
            onColorSelected = { selected ->
                viewModel.updateCustomColorTheme(tableBorderColor = selected.value.toLong())
                showTableBorderColorPicker = false
            }
        )
    }

    if (showActiveTabColorPicker) {
        ColorPickerDialog(
            title = "لون التبويبات النشطة",
            initialColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupActiveTabBg) ?: MaterialTheme.colorScheme.primary,
            onDismiss = { showActiveTabColorPicker = false },
            onColorSelected = { selected ->
                viewModel.updateCustomColorTheme(groupActiveTabBg = selected.value.toLong())
                showActiveTabColorPicker = false
            }
        )
    }

    if (showInactiveTabColorPicker) {
        ColorPickerDialog(
            title = "لون التبويبات غير النشطة",
            initialColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupInactiveTabBg) ?: MaterialTheme.colorScheme.surfaceVariant,
            onDismiss = { showInactiveTabColorPicker = false },
            onColorSelected = { selected ->
                viewModel.updateCustomColorTheme(groupInactiveTabBg = selected.value.toLong())
                showInactiveTabColorPicker = false
            }
        )
    }

    if (showTableHeaderBgColorPicker) {
        ColorPickerDialog(
            title = "لون خلفية رؤوس الجداول والأعمدة",
            initialColor = Color(uiState.tableHeaderBgColor),
            onDismiss = { showTableHeaderBgColorPicker = false },
            onColorSelected = { selected ->
                viewModel.setTableHeaderBgColor(selected)
                showTableHeaderBgColorPicker = false
            }
        )
    }

    if (showAppBgColorPicker) {
        ColorPickerDialog(
            title = "تحديد لون خلفية التطبيق الشامل المخصص",
            initialColor = Color(uiState.appBackgroundColor),
            onDismiss = { showAppBgColorPicker = false },
            onColorSelected = { selected ->
                viewModel.setAppBackgroundColor(selected)
                showAppBgColorPicker = false
            }
        )
    }
}

@Composable
private fun ColorSettingRow(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
            Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

private data class ThemeColorOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onPick: () -> Unit
)
