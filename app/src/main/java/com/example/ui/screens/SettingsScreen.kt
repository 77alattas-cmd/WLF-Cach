package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.DesignSystem
import com.example.ui.theme.vibrant3d
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.components.AuditLogDialog
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.ColorPickerDialog
import com.example.ui.components.ExportReportDialog
import com.example.ui.model.AccountingFormatter
import com.example.ui.model.ColorPresetsRegistry
import com.example.ui.model.ColorThemePreset
import com.example.ui.viewmodel.PRESET_APP_THEMES
import com.example.ui.viewmodel.AppThemePreset
import com.example.ui.viewmodel.TicketAccountingViewModel
import com.example.util.TafqeetHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: TicketAccountingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary by viewModel.salesSummary.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val customNightPrimary by viewModel.customNightPrimary.collectAsStateWithLifecycle()
    val customDayPrimary by viewModel.customDayPrimary.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var cacheSizeStr by remember { mutableStateOf("0 KB") }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAuditLogDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_screen"),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        // Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "الإعدادات العامة والخيارات المحاسبية",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "تخصيص السمات، التاريخ الهجري، والتصفير التلقائي",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // 0. Audit Log / Records Section at Top of Settings (سجل العمليات والتغيرات المحاسبية)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "سجل التغيرات والعمليات",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "عدد السجلات المسجلة: ${uiState.auditLogs.size} عملية",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                                )
                            }
                        }

                        Button(
                            onClick = { showAuditLogDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("عرض مكبر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.auditLogs.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "آخر العمليات المسجلة:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                )
                                uiState.auditLogs.takeLast(3).reversed().forEach { log ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• ${log.section} - ${log.field}: ${log.newValue}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = AccountingFormatter.formatTime(log.timestamp),
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Designer Attribution & Google Play Trust Certificate
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "تطبيق WLF Cash الأصلي من المصمم HAS",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                )
                                Text(
                                    text = "موثق بشهادة ثقة وحماية Google Play Protect لتطبيقات المحاسبة المعتمدة",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.5.sp)
                                )
                            }
                        }
                    }
                }
            }
        }



        // 1. Date & Accounting Period Options (التاريخ الهجري وبداية اليوم المحاسبي والتصفير التلقائي)
        item {
            SettingsSectionCard(title = "التاريخ الهجري وأوقات العمل المحاسبي", icon = Icons.Default.CalendarToday) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Hijri Date Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("عرض التاريخ الهجري", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "إظهار التاريخ بنمط التقويم الهجري في شاشات المبيعات والصندوق والتقارير",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = uiState.showHijriDate,
                            onCheckedChange = { viewModel.setShowHijriDate(it) },
                            modifier = Modifier.testTag("switch_hijri_date")
                        )
                    }

                    // Hijri Date Manual Adjustment (تعديل فارق أيام التاريخ الهجري)
                    val liveNow = System.currentTimeMillis()
                    val currentHijriFormatted = AccountingFormatter.formatHijriDate(liveNow, uiState.useEasternArabicNumerals, uiState.hijriAdjustmentDays)
                    
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تعديل التاريخ الهجري",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (currentHijriFormatted.isNotBlank()) currentHijriFormatted else "غير متوفر",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = "معايرة فرق الأيام للتاريخ الهجري وفق رؤية الهلال المحلية:",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(-2, -1, 0, 1, 2).forEach { offset ->
                                    val isSelected = uiState.hijriAdjustmentDays == offset
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setHijriAdjustmentDays(offset) },
                                        label = {
                                            Text(
                                                text = when (offset) {
                                                    -2 -> "-2 يوم"
                                                    -1 -> "-1 يوم"
                                                    0 -> "تلقائي (0)"
                                                    1 -> "+1 يوم"
                                                    2 -> "+2 يوم"
                                                    else -> "$offset"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        modifier = Modifier.weight(1f).testTag("chip_hijri_offset_$offset")
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // 24-Hour Format Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("نظام 24 ساعة للوقت", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "عرض الوقت بنظام 24 ساعة (مثال: 14:30:15) بدلاً من نظام 12 ساعة (02:30:15 م)",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = uiState.use24HourFormat,
                            onCheckedChange = { viewModel.setUse24HourFormat(it) },
                            modifier = Modifier.testTag("switch_24hour_format")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Full Screen Mode Toggle (ملء الشاشة للتطبيق - غير نشط تلقائياً)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("وضع ملء الشاشة للتطبيق", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "إخفاء شريط الحالة وأشرطة النظام لتوفير مساحة عرض كاملة (غير نشط تلقائياً)",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = uiState.isFullScreenMode,
                            onCheckedChange = { viewModel.setFullScreenMode(it) },
                            modifier = Modifier.testTag("switch_fullscreen_mode")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Compact Mode Toggle (الوضع المضغوط)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ميزة الوضع المضغوط", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "تقليص الهوامش والمساحات لعرض أكبر قدر ممكن من البنود والبيانات",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = uiState.showCompactMode,
                            onCheckedChange = { viewModel.setShowCompactMode(it) },
                            modifier = Modifier.testTag("switch_compact_mode")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))



                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("أزرار زيادة ونقصان المتبقي (+/-)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "إظهار زري زيادة (+1) وإنقاص (-1) بجانب حقل المتبقي لتعديله السريع مع تباعد الأمان",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = uiState.showRemainingStepper,
                            onCheckedChange = { viewModel.toggleRemainingStepper() },
                            modifier = Modifier.testTag("switch_remaining_stepper")
                        )
                    }


                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Accounting Day Start Hour (0:00 - 6:00 AM)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("بداية اليوم المحاسبي", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "الساعة التي يبدأ عندها احتساب وردية اليوم الجديد (${uiState.accountingDayStartHour}:00)",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            listOf(0, 3, 5, 6).forEach { hr ->
                                FilterChip(
                                    selected = uiState.accountingDayStartHour == hr,
                                    onClick = { viewModel.setAccountingDayStartHour(hr) },
                                    label = { Text("${hr}ص", fontSize = 10.sp) },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Auto Reset Time
                    OutlinedTextField(
                        value = uiState.scheduledResetTime,
                        onValueChange = { viewModel.setScheduledResetTime(it) },
                        label = { Text("وقت التصفير التلقائي اليومي (HH:mm)") },
                        placeholder = { Text("03:00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 3.5 Report Customization Section
        item {
            SettingsSectionCard(title = "خيارات وتخصيص التقارير", icon = Icons.Default.Assessment) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Include Internet Group Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تضمين انترنت في التقرير", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "إظهار أو إخفاء انترنت في التقرير والملخصات المطبوعة والملفات التصديرية",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = uiState.includeInternetInReport,
                            onCheckedChange = { viewModel.setIncludeInternetInReport(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Report Header Title
                    OutlinedTextField(
                        value = uiState.reportHeaderTitle,
                        onValueChange = { viewModel.setReportHeaderTitle(it) },
                        label = { Text("عنوان رأس التقرير (Header)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Report Header Subtitle
                    OutlinedTextField(
                        value = uiState.reportHeaderSubtitle,
                        onValueChange = { viewModel.setReportHeaderSubtitle(it) },
                        label = { Text("العنوان الفرعي للتقرير") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Shift Closing Reminder Section
        item {
            SettingsSectionCard(title = "التنبيهات والتذكيرات اليومية", icon = Icons.Default.Alarm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيه إغلاق الوردية والحسابات اليومية",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "إرسال تنبيه يومي اختياري لتذكيرك بإغلاق الوردية اليومية ومراجعة الحسابات",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )
                        }
                        Switch(
                            checked = uiState.shiftReminderEnabled,
                            onCheckedChange = { viewModel.toggleShiftReminder(it) },
                            modifier = Modifier.testTag("switch_shift_reminder")
                        )
                    }

                    if (uiState.shiftReminderEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        OutlinedTextField(
                            value = uiState.shiftReminderTime,
                            onValueChange = { viewModel.setShiftReminderTime(it) },
                            label = { Text("وقت التنبيه اليومي (HH:mm)") },
                            placeholder = { Text("21:00") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_shift_reminder_time")
                        )
                        Text(
                            text = "💡 سيتم إظهار تنبيه تذكيري في التطبيق عند حلول هذا الوقت من كل يوم.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
                        )
                    }
                }
            }
        }

        // Currency Management Section (Dropdown & Reorderable List)
        item {
            var showAddCurrencyDialog by remember { mutableStateOf(false) }
            var editingCurrency by remember { mutableStateOf<com.example.ui.model.CustomCurrency?>(null) }
            var isCurrencyListExpanded by remember { mutableStateOf(true) }

            SettingsSectionCard(title = "إعدادات العملة وتنسيق عرض الأرقام", icon = Icons.Default.Paid) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تحديد العملة المعتمدة في النظام وتخصيص نمط تنسيق عرض الأرقام والمبالغ والكسور وفواصل الآلاف وفقاً لها (مثل الدولار أو الريال).",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                    )

                    val mainCurr = uiState.customCurrencies.find { it.isMain } ?: uiState.customCurrencies.first()

                    // 1. Live Interactive Preview Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "معاينة حية لتنسيق الأرقام والعملة",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "${mainCurr.flag} ${mainCurr.code}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Examples Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("مبلغ عادي (15,450):", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.5.sp))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = AccountingFormatter.formatMoney(15450.0, uiState.useEasternArabicNumerals),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("مع كسر عشري (1,250.75):", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.5.sp))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = AccountingFormatter.formatMoney(1250.75, uiState.useEasternArabicNumerals),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }

                            // Balance Difference and Spelling Sample
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("فارق الصندوق:", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.5.sp))
                                        Text(
                                            text = AccountingFormatter.formatBalanceFormatted(15450.0, uiState.useEasternArabicNumerals),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                    Text(
                                        text = "كتابةً: ${AccountingFormatter.formatTafqeetMain(15450.0)}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                    )
                                }
                            }

                            // Active badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val symPosText = when (uiState.currencySymbolPosition) {
                                    "BEFORE" -> "الرمز: قبل المبلغ"
                                    "AFTER" -> "الرمز: بعد المبلغ"
                                    else -> "الرمز: تلقائي"
                                }
                                val decText = when (uiState.currencyDecimalMode) {
                                    "ALWAYS_TWO" -> "الكسور: خانتان (.00)"
                                    "INTEGER_ONLY" -> "الكسور: صحيح فقط"
                                    "DYNAMIC_IF_FRACTION" -> "الكسور: عند الحاجة"
                                    else -> "الكسور: تلقائي"
                                }
                                val sepText = when (uiState.currencyThousandsSeparator) {
                                    " " -> "فواصل: مسافة"
                                    "" -> "فواصل: بدون"
                                    else -> "فواصل: فاصلة (,)"
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)) {
                                    Text(symPosText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)) {
                                    Text(decText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)) {
                                    Text(sepText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    // 2. One-Tap Quick Presets
                    Text(
                        text = "أنماط شائعة جاهزة للتطبيق بنقرة واحدة:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Preset: USD ($)
                        val isUsdSelected = mainCurr.code == "USD"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isUsdSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(if (isUsdSelected) 1.5.dp else 0.8.dp, if (isUsdSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.applyCurrencyPreset("USD") }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("🇺🇸 دولار ($)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                Text("$ 1,250.00", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                                Text("قبل + .00", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.sp))
                            }
                        }

                        // Preset: YER (ر.ي.)
                        val isYerSelected = mainCurr.code == "YER"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isYerSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(if (isYerSelected) 1.5.dp else 0.8.dp, if (isYerSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.applyCurrencyPreset("YER") }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("🇾🇪 ريال (ر.ي.)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                Text("1,250 ر.ي.", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                                Text("بعد + صحيح", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.sp))
                            }
                        }

                        // Preset: SAR (ر.س.)
                        val isSarSelected = mainCurr.code == "SAR"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSarSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(if (isSarSelected) 1.5.dp else 0.8.dp, if (isSarSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.applyCurrencyPreset("SAR") }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("🇸🇦 ريال (ر.س.)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                Text("1,250.00 ر.س.", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                                Text("بعد + .00", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.sp))
                            }
                        }

                        // Preset: EUR (€)
                        val isEurSelected = mainCurr.code == "EUR"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isEurSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(if (isEurSelected) 1.5.dp else 0.8.dp, if (isEurSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.applyCurrencyPreset("EUR") }
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("🇪🇺 يورو (€)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                Text("€ 1,250.00", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                                Text("قبل + .00", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.sp))
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                    // 4. Granular Number & Currency Formatting Controls
                    Text("خيارات تخصيص تنسيق الأرقام والعملة بالتفصيل:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    // (A) Symbol Position
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("موضع رمز العملة بالنسبة للمبلغ:", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = uiState.currencySymbolPosition == "AUTO",
                                onClick = { viewModel.setCurrencySymbolPosition("AUTO") },
                                label = { Text("تلقائي (حسب العملة)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.currencySymbolPosition == "BEFORE",
                                onClick = { viewModel.setCurrencySymbolPosition("BEFORE") },
                                label = { Text("قبل ($ 1,250)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.currencySymbolPosition == "AFTER",
                                onClick = { viewModel.setCurrencySymbolPosition("AFTER") },
                                label = { Text("بعد (1,250 ر.ي)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // (B) Decimal Places & Precision
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("الكسور والخانة العشرية:", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = uiState.currencyDecimalMode == "AUTO",
                                onClick = { viewModel.setCurrencyDecimalMode("AUTO") },
                                label = { Text("تلقائي (الدولار .00 / الريال صحيح)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = uiState.currencyDecimalMode == "ALWAYS_TWO",
                                onClick = { viewModel.setCurrencyDecimalMode("ALWAYS_TWO") },
                                label = { Text("خانتان عشريتان دائماً (.00)", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = uiState.currencyDecimalMode == "INTEGER_ONLY",
                                onClick = { viewModel.setCurrencyDecimalMode("INTEGER_ONLY") },
                                label = { Text("أرقام صحيحة فقط", fontSize = 10.sp) }
                            )
                            FilterChip(
                                selected = uiState.currencyDecimalMode == "DYNAMIC_IF_FRACTION",
                                onClick = { viewModel.setCurrencyDecimalMode("DYNAMIC_IF_FRACTION") },
                                label = { Text("إظهار الكسور عند وجودها فقط", fontSize = 10.sp) }
                            )
                        }
                    }

                    // (C) Thousands Separator
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("فاصلة الآلاف وفصل المراتب:", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = uiState.currencyThousandsSeparator == ",",
                                onClick = { viewModel.setCurrencyThousandsSeparator(",") },
                                label = { Text("فاصلة (,)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.currencyThousandsSeparator == " ",
                                onClick = { viewModel.setCurrencyThousandsSeparator(" ") },
                                label = { Text("مسافة ( )", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.currencyThousandsSeparator == "",
                                onClick = { viewModel.setCurrencyThousandsSeparator("") },
                                label = { Text("بدون فواصل", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // (D) Currency Display Type
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("طريقة عرض مسمى العملة المالي:", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = uiState.currencyDisplayType == "SYMBOL",
                                onClick = { viewModel.setCurrencyDisplayType("SYMBOL") },
                                label = { Text("الرمز ($ / ر.ي)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.currencyDisplayType == "CODE",
                                onClick = { viewModel.setCurrencyDisplayType("CODE") },
                                label = { Text("الكود (USD / YER)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.currencyDisplayType == "NAME",
                                onClick = { viewModel.setCurrencyDisplayType("NAME") },
                                label = { Text("الاسم العربي الكامل", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // (E) Numeral System (123 vs ١٢٣)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("نمط كتابة أرقام العد:", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = !uiState.useEasternArabicNumerals,
                                onClick = { viewModel.setUseEasternArabicNumerals(false) },
                                label = { Text("أرقام غربية قياسية (123)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.useEasternArabicNumerals,
                                onClick = { viewModel.setUseEasternArabicNumerals(true) },
                                label = { Text("أرقام مشرقية عربية (١٢٣)", fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))

                    // Expandable / Condensed Reorderable Currency List
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCurrencyListExpanded = !isCurrencyListExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCurrencyListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "إدارة وترتيب أولويات العملات (${uiState.customCurrencies.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            )
                        }
                        Text(
                            text = if (isCurrencyListExpanded) "إخفاء القائمة" else "عرض وإعادة الترتيب",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }

                    if (isCurrencyListExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Current Active Main Currency Card at the top
                            OutlinedCard(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = mainCurr.flag, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "العملة الرئيسة المعتمدة حالياً:",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                            Text(
                                                text = "${mainCurr.arabicName} (${mainCurr.code}) - رمز: ${mainCurr.symbol}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "سعر الصرف المعتمد: ${mainCurr.defaultRateYer} ر.ي",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "معتمدة فوراً ✓",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "انقر على زر (رئيسية) بجانب أي عملة لتصبح العملة المعتمدة فوراً، أو استخدم الأسهم لترتيب الأولويات:",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                            )

                            uiState.customCurrencies.forEachIndexed { index, curr ->
                                val isThisMain = curr.isMain
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isThisMain) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, if (isThisMain) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            // Reorder buttons (Priority)
                                            IconButton(
                                                onClick = { if (index > 0) viewModel.moveCustomCurrency(index, index - 1) },
                                                enabled = index > 0,
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowUpward, contentDescription = "أعلى", modifier = Modifier.size(14.dp))
                                            }
                                            IconButton(
                                                onClick = { if (index < uiState.customCurrencies.size - 1) viewModel.moveCustomCurrency(index, index + 1) },
                                                enabled = index < uiState.customCurrencies.size - 1,
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowDownward, contentDescription = "أسفل", modifier = Modifier.size(14.dp))
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = curr.flag, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = curr.arabicName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "(${curr.code})",
                                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                                                    )
                                                }
                                                Text(
                                                    text = "سعر الصرف: ${curr.defaultRateYer} ر.ي.",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.5.sp)
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!isThisMain) {
                                                OutlinedButton(
                                                    onClick = { viewModel.setMainCurrency(curr.code) },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("رئيسية", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                                }

                                                IconButton(
                                                    onClick = { editingCurrency = curr },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "تعديل",
                                                        modifier = Modifier.size(14.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                if (curr.isCustom) {
                                                    IconButton(
                                                        onClick = { viewModel.deleteCustomCurrency(curr.code) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "حذف",
                                                            modifier = Modifier.size(14.dp),
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                }
                                            } else {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                ) {
                                                    Text(
                                                        text = "الرئيسية",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimary,
                                                            fontSize = 10.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { showAddCurrencyDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إضافة عملة جديدة مخصصة", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp))
                            }
                        }
                    }
                }
            }

            // Add Currency Dialog
            if (showAddCurrencyDialog) {
                var code by remember { mutableStateOf("") }
                var name by remember { mutableStateOf("") }
                var symbol by remember { mutableStateOf("") }
                var rate by remember { mutableStateOf("") }
                var flag by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddCurrencyDialog = false },
                    title = { Text("إضافة عملة جديدة مخصصة", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it.uppercase() },
                                label = { Text("كود العملة (مثال: USD, CAD)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("اسم العملة بالعربية (مثال: دولار أمريكي)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = symbol,
                                onValueChange = { symbol = it },
                                label = { Text("رمز العملة (مثال: $, ر.س)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            com.example.ui.components.AppNumberField(
                                value = rate,
                                onValueChange = { rate = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("سعر الصرف الافتراضي مقابل الريال اليمني") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                useCustomNumpadOnly = true,
                                targetTitle = "سعر الصرف",
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = flag,
                                onValueChange = { flag = it },
                                label = { Text("أيقونة العملة أو علم الدولة (Emoji)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val rVal = rate.toDoubleOrNull() ?: 1.0
                                if (code.isNotBlank() && name.isNotBlank() && symbol.isNotBlank()) {
                                    viewModel.addCustomCurrency(code, name, symbol, rVal, flag)
                                    showAddCurrencyDialog = false
                                }
                            }
                        ) {
                            Text("إضافة")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCurrencyDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            // Edit Currency Dialog
            editingCurrency?.let { curr ->
                var name by remember { mutableStateOf(curr.arabicName) }
                var symbol by remember { mutableStateOf(curr.symbol) }
                var rate by remember { mutableStateOf(curr.defaultRateYer.toString()) }
                var flag by remember { mutableStateOf(curr.flag) }

                AlertDialog(
                    onDismissRequest = { editingCurrency = null },
                    title = { Text("تعديل العملة (${curr.code})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("اسم العملة بالعربية") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = symbol,
                                onValueChange = { symbol = it },
                                label = { Text("رمز العملة") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            com.example.ui.components.AppNumberField(
                                value = rate,
                                onValueChange = { rate = it.filter { ch -> ch.isDigit() || ch == '.' } },
                                label = { Text("سعر الصرف الافتراضي مقابل الريال اليمني") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                useCustomNumpadOnly = true,
                                targetTitle = "سعر الصرف",
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = flag,
                                onValueChange = { flag = it },
                                label = { Text("أيقونة العملة أو علم الدولة (Emoji)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val rVal = rate.toDoubleOrNull() ?: curr.defaultRateYer
                                if (name.isNotBlank() && symbol.isNotBlank()) {
                                    viewModel.updateCustomCurrency(curr.code, name, symbol, rVal, flag)
                                    editingCurrency = null
                                }
                            }
                        ) {
                            Text("حفظ التعديلات")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingCurrency = null }) {
                            Text("إلغاء")
                        }
                    }
                )
            }
        }

        // 5.5 Backup & Restore (النسخ الاحتياطي واستعادة البيانات)
        item {
            SettingsSectionCard(title = "النسخ الاحتياطي واستعادة البيانات", icon = Icons.Default.CloudSync) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "حفظ نسخة احتياطية لكافة بيانات المبيعات والصندوق والمصروفات بالتاريخ والوقت مع إمكانية الاستعادة في أي وقت.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.createBackup()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_backup")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ نسخة الآن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showBackupRestoreDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("btn_open_backup_restore_dialog")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إدارة واستعادة (${uiState.backupSnapshots.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. Data Management & Reset
        item {
            SettingsSectionCard(title = "إدارة البيانات وتصفير اليوم", icon = Icons.Default.RestartAlt) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "يمكنك تصفير مدخلات اليوم الحالي للبدء بيوم محاسبي جديد.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                    )

                    OutlinedButton(
                        onClick = { viewModel.openShiftResetDialog() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("btn_settings_reset_day")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصفير الوردية الحالية (مع الرسوم المتحركة)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    OutlinedButton(
                        onClick = {
                            var size = 0L
                            try {
                                val cache = context.cacheDir
                                if (cache != null && cache.isDirectory) {
                                    size += getFolderSize(cache)
                                }
                                val extCache = context.externalCacheDir
                                if (extCache != null && extCache.isDirectory) {
                                    size += getFolderSize(extCache)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            val units = arrayOf("B", "KB", "MB", "GB")
                            val digitGroups = if (size > 0) (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt() else 0
                            cacheSizeStr = if (size <= 0) "0 KB" else java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
                            showClearCacheDialog = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_clear_cache")
                    ) {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مسح الذاكرة المؤقتة (Clear Cache)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (showClearCacheDialog) {
                        AlertDialog(
                            onDismissRequest = { showClearCacheDialog = false },
                            title = { Text("تأكيد مسح الذاكرة المؤقتة", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("الحجم الإجمالي للملفات المؤقتة: $cacheSizeStr")
                                    Text(
                                        text = "هل تريد مسح كافة الملفات المؤقتة والذاكرة المؤقتة للتطبيق؟ لن يؤثر ذلك على بياناتك أو سجلاتك المالية.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        try {
                                            context.cacheDir.deleteRecursively()
                                            context.externalCacheDir?.deleteRecursively()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                        showClearCacheDialog = false
                                    }
                                ) {
                                    Text("تأكيد المسح")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showClearCacheDialog = false }) {
                                    Text("إلغاء")
                                }
                            }
                        )
                    }
                }
            }
        }

        // 7. About Application & Version Info Card
        item {
            SettingsSectionCard(title = "حول التطبيق والمعلومات", icon = Icons.Default.Info) {
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
                            text = "إصدار التطبيق الحالي:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "v3.2.0 (WLF1S)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إصدار التطبيق الأصلي:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "v1.0.0 (الأساسي)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "نظام WLF Cash المحاسبي لإدارة النقدية ومطابقة مبيعات التذاكر والصناديق بدقة وسرعة واحترافية.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 11.sp)
                    )
                }
            }
        }

        // 7.5. System Maintenance Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "أداة صيانة النظام التلقائية",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f), thickness = 1.dp)

                    Text(
                        text = "يقوم النظام بفحص سلامة الجداول الحسابية ومطابقة الكميات التالفة/المبيعات وتصحيحها فورياً لحماية دقة حساباتك.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    )

                    Button(
                        onClick = { viewModel.runMaintenanceDiagnostic() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Construction, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بدء تشخيص وصيانة وظائف التطبيق", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 8. Developer & Designer Credit Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "التصميم والتطوير",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Design By : HAS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 1.dp)

                    // Phone text display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "رقم التواصل والدعم:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "+967780776191",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                    }

                    // Contact Action Buttons (Call, WhatsApp, Telegram, Google Meet)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // WhatsApp Button
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/967780776191"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "واتساب", modifier = Modifier.size(14.dp), tint = Color(0xFF25D366))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("واتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Telegram Button
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+967780776191"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "تلجرام", modifier = Modifier.size(14.dp), tint = Color(0xFF0088CC))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تلجرام", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Google Meet Button
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://meet.google.com/new"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = "ميت", modifier = Modifier.size(14.dp), tint = Color(0xFF00897B))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ميت", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Direct Call Button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+967780776191"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "اتصال", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

}

    // Reset Confirmation Dialog (زر تصفير الكل رسالة تنبيه وتأكيد)
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تنبيه وتأكيد: تصفير كافة البيانات",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.error
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تحذير: هل أنت متأكد من تصفير كافة مبيعات اليوم الحالي، وحركات الصندوق، والمصروفات؟",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "هذا الإجراء سيقوم بإعادة تعيين جميع مدخلات الوردية الحالية إلى الصفر. لن يتم المساس بالإعدادات أو السجلات المؤرشفة.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllInputs()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("نعم، تصفير الآن", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إلغاء التصفير")
                }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportReportDialog(
            state = uiState,
            summary = summary,
            onDismiss = { showExportDialog = false }
        )
    }

    if (showAuditLogDialog) {
        AuditLogDialog(
            auditLogs = uiState.auditLogs,
            onDismiss = { showAuditLogDialog = false },
            onClearLogs = { viewModel.clearAuditLogs() }
        )
    }

    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            snapshots = uiState.backupSnapshots,
            viewModel = viewModel,
            useEasternArabic = uiState.useEasternArabicNumerals,
            onDismiss = { showBackupRestoreDialog = false }
        )
    }

    if (uiState.showMaintenanceDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isMaintenanceRunning) viewModel.closeMaintenanceDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isMaintenanceRunning) "جاري الفحص والصيانة..." else "تقرير صيانة وإصلاح النظام",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isMaintenanceRunning) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "جاري التدقيق وفحص العمليات والمطابقة الرقمية حالياً...",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    uiState.maintenanceResults.forEach { log ->
                                        Text(
                                            text = log,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    color = if (log.contains("✅") || log.contains("✨")) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                                    else if (log.contains("🔧") || log.contains("⚠️")) Color(0xFFE91E63).copy(alpha = 0.08f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (log.contains("✅") || log.contains("✨")) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    else if (log.contains("🔧") || log.contains("⚠️")) Color(0xFFE91E63).copy(alpha = 0.2f)
                                                    else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!uiState.isMaintenanceRunning) {
                    Button(
                        onClick = { viewModel.closeMaintenanceDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إغلاق نافذة الصيانة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .vibrant3d(
                shape = RoundedCornerShape(16.dp),
                elevation = 6.dp,
                isDark = isDark
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DesignSystem.primaryGradient())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Box(modifier = Modifier.padding(2.dp)) {
                content()
            }
        }
    }
}

private fun getFolderSize(file: java.io.File): Long {
    var length = 0L
    val files = file.listFiles()
    if (files != null) {
        for (f in files) {
            length += if (f.isDirectory) getFolderSize(f) else f.length()
        }
    }
    return length
}
