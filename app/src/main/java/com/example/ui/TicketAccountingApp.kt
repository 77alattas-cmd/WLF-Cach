package com.example.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.example.R
import com.example.ui.components.GlobalCustomNumpad
import com.example.ui.components.LocalNumpadController
import com.example.ui.components.NumpadController
import com.example.ui.screens.*
import com.example.ui.theme.DesignSystem
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.vibrant3d
import com.example.ui.util.AppStrings
import com.example.ui.util.LocalAppLanguage
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.TicketAccountingViewModel

val LocalCompactMode = compositionLocalOf { false }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketAccountingApp(
    viewModel: TicketAccountingViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val customNightPrimary by viewModel.customNightPrimary.collectAsStateWithLifecycle()
    val customDayPrimary by viewModel.customDayPrimary.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary by viewModel.salesSummary.collectAsStateWithLifecycle()

    val numpadController = remember { NumpadController() }
    val context = LocalContext.current
    val activity = context as? Activity

    // Full-Screen System Bars Control (خيار ملء الشاشة للتطبيق)
    LaunchedEffect(uiState.isFullScreenMode) {
        activity?.window?.let { window ->
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            if (uiState.isFullScreenMode) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    MyApplicationTheme(
        darkTheme = isDarkTheme,
        customDayPrimary = customDayPrimary,
        customNightPrimary = customNightPrimary
    ) {
        val layoutDir = if (uiState.appLanguage.equals("en", ignoreCase = true)) LayoutDirection.Ltr else LayoutDirection.Rtl

        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDir,
            LocalAppLanguage provides uiState.appLanguage,
            LocalCompactMode provides uiState.showCompactMode,
            LocalNumpadController provides numpadController
        ) {
            val isDark = isSystemInDarkTheme()
            val lang = uiState.appLanguage

            val customColorState = uiState.customColorThemeState
            val outerBorderColor = customColorState.getColorOrNull(customColorState.appBorderColor)

            var showGlobalResetDialog by remember { mutableStateOf(false) }
            var showCategoryCalculatorDialog by remember { mutableStateOf(false) }

            if (showCategoryCalculatorDialog) {
                com.example.ui.components.SalesCategoryCalculatorDialog(
                    groups = uiState.groups,
                    initialGroupId = uiState.selectedGroupId,
                    onDismiss = { showCategoryCalculatorDialog = false },
                    onCommitSales = { viewModel.commitSalesFromCalculator(it) }
                )
            }

            if (showGlobalResetDialog) {
                com.example.ui.components.ResetScopeDialog(
                    onDismiss = { showGlobalResetDialog = false },
                    onConfirmResetGroup = { viewModel.resetSalesGroupOnly(uiState.selectedGroupId) },
                    onConfirmResetCash = { viewModel.resetPhysicalCashOnly() },
                    onConfirmResetRevenue = { viewModel.resetAllInputs() },
                    onConfirmResetAll = {
                        showGlobalResetDialog = false
                        viewModel.openShiftResetDialog()
                    },
                    groupName = uiState.groups.find { it.id == uiState.selectedGroupId }?.name ?: "المجموعة الحالية"
                )
            }

            if (uiState.showShiftResetDialog) {
                com.example.ui.components.ShiftResetConfirmationDialog(
                    summary = summary,
                    useEasternDigits = uiState.useEasternArabicNumerals,
                    onDismiss = { viewModel.closeShiftResetDialog() },
                    onConfirmReset = { shiftNotes ->
                        viewModel.confirmAndResetShift(shiftNotes)
                    }
                )
            }

            val appBgModifier = when (uiState.appBackgroundStyle) {
                "TEXTURE_CARBON_FIBER" -> Modifier.drawBehind {
                    drawRect(Color(0xFF131518))
                    val tileSize = 20.dp.toPx()
                    val width = size.width
                    val height = size.height
                    var y = 0f
                    var rowIndex = 0
                    while (y < height) {
                        var x = 0f
                        var colIndex = 0
                        while (x < width) {
                            val isAlt = (rowIndex + colIndex) % 2 == 0
                            val color = if (isAlt) Color(0xFF1E2228) else Color(0xFF0D0F12)
                            drawRect(
                                color = color,
                                topLeft = Offset(x, y),
                                size = Size(tileSize, tileSize)
                            )
                            drawLine(
                                color = if (isAlt) Color(0x33FFFFFF) else Color(0x15FFFFFF),
                                start = Offset(x, y),
                                end = Offset(x + tileSize, y + tileSize),
                                strokeWidth = 1.5f
                            )
                            drawLine(
                                color = Color(0x22000000),
                                start = Offset(x + tileSize, y),
                                end = Offset(x, y + tileSize),
                                strokeWidth = 1f
                            )
                            x += tileSize
                            colIndex++
                        }
                        y += tileSize
                        rowIndex++
                    }
                }
                "TEXTURE_PERFORATED_LEATHER" -> Modifier.drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF181513), Color(0xFF221E1A), Color(0xFF1A1614))
                        )
                    )
                    val spacing = 22.dp.toPx()
                    val radius = 2.2.dp.toPx()
                    val width = size.width
                    val height = size.height
                    var y = spacing / 2f
                    var row = 0
                    while (y < height) {
                        val xOffset = if (row % 2 == 1) spacing / 2f else 0f
                        var x = xOffset
                        while (x < width) {
                            val center = Offset(x, y)
                            drawCircle(
                                color = Color(0xFF0A0807),
                                radius = radius + 1f,
                                center = center
                            )
                            drawCircle(
                                color = Color(0xFF000000),
                                radius = radius,
                                center = center
                            )
                            drawCircle(
                                color = Color(0x28A89A84),
                                radius = radius,
                                center = Offset(x, y + 1.2f),
                                style = Stroke(width = 1f)
                            )
                            x += spacing
                        }
                        y += spacing * 0.866f
                        row++
                    }
                }
                "TEXTURE_GRANULAR_WALL" -> Modifier.drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFF3F1EC), Color(0xFFE8E4DC), Color(0xFFDFDBD2))
                        )
                    )
                    val width = size.width
                    val height = size.height
                    val grainSpacing = 12.dp.toPx()
                    val dotColor1 = Color(0x18000000)
                    val dotColor2 = Color(0x35FFFFFF)
                    var y = 0f
                    var r = 0
                    while (y < height) {
                        var x = 0f
                        var c = 0
                        while (x < width) {
                            val pseudoRandomOffset = ((r * 7 + c * 13) % 7).toFloat()
                            val pt = Offset(x + pseudoRandomOffset, y + ((r * 11 + c * 5) % 5).toFloat())
                            drawCircle(
                                color = if ((r + c) % 2 == 0) dotColor1 else dotColor2,
                                radius = if ((r * c) % 3 == 0) 1.5f else 1f,
                                center = pt
                            )
                            x += grainSpacing
                            c++
                        }
                        y += grainSpacing
                        r++
                    }
                }
                "TEXTURE_WOOD_GRAIN" -> Modifier.drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF382319),
                                Color(0xFF4A3022),
                                Color(0xFF3D271B),
                                Color(0xFF593928),
                                Color(0xFF422A1E),
                                Color(0xFF2F1D14),
                                Color(0xFF4E3324),
                                Color(0xFF3B251A)
                            )
                        )
                    )
                    val width = size.width
                    val height = size.height
                    val lineCount = 35
                    val step = height / lineCount
                    for (i in 0..lineCount) {
                        val yPos = i * step
                        val waveColor = if (i % 2 == 0) Color(0x1A000000) else Color(0x14FFE0B2)
                        val strokeW = if (i % 3 == 0) 2.5f else 1.2f
                        val path = Path().apply {
                            moveTo(0f, yPos)
                            val waveHeight = 8.dp.toPx()
                            cubicTo(
                                width * 0.25f, yPos + waveHeight,
                                width * 0.75f, yPos - waveHeight,
                                width, yPos + (waveHeight * 0.5f)
                            )
                        }
                        drawPath(
                            path = path,
                            color = waveColor,
                            style = Stroke(width = strokeW)
                        )
                    }
                }
                "METALLIC_STEEL" -> Modifier.background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8), Color(0xFFCBD5E1), Color(0xFFF1F5F9))
                    )
                )
                "METALLIC_GOLD" -> Modifier.background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFEF3C7), Color(0xFFFDE047), Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFFFFFBEB))
                    )
                )
                "METALLIC_SILVER" -> Modifier.background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFFE2E8F0), Color(0xFFFFFFFF))
                    )
                )
                "METALLIC_TITANIUM" -> Modifier.background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                "METALLIC_COPPER" -> Modifier.background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFEDD5), Color(0xFFFB923C), Color(0xFF9A3412), Color(0xFFC2410C), Color(0xFFFFF7ED))
                    )
                )
                "CUSTOM_COLOR" -> Modifier.background(Color(uiState.appBackgroundColor))
                else -> Modifier.background(DesignSystem.appBackgroundGradient(isDark))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(appBgModifier)
                    .then(
                        if (outerBorderColor != null) {
                            Modifier.border(
                                width = customColorState.appBorderWidthDp.dp,
                                color = outerBorderColor
                            )
                        } else Modifier
                    )
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                    if (currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.ONBOARDING) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .vibrant3d(
                                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                                    elevation = 8.dp,
                                    isDark = isDark,
                                    gradientBrush = DesignSystem.topBarGradient(isDark, accent = MaterialTheme.colorScheme.primary)
                                ),
                            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                            color = Color.Transparent,
                            border = BorderStroke(
                                1.dp,
                                if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                        ) {
                            TopAppBar(
                                title = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_app_icon_main),
                                            contentDescription = "WLF Cash",
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "WLF Cash",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                actions = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(end = 6.dp)
                                    ) {
                                        // Reset Shift & Scope Options Button (تصفير الوردية والبيانات)
                                        var showTopBarResetMenu by remember { mutableStateOf(false) }
                                        Box {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isDark) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                                       else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.70f),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.error.copy(alpha = if (isDark) 0.5f else 0.7f)
                                                ),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { showTopBarResetMenu = true },
                                                    modifier = Modifier.size(38.dp).testTag("top_bar_reset_toggle")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.RestartAlt,
                                                        contentDescription = "خيارات تصفير الوردية والبيانات",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = showTopBarResetMenu,
                                                onDismissRequest = { showTopBarResetMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text("تصفير الوردية الحالية", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.error)
                                                            Text("مع الرسوم المتحركة والتأكيد البصري", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                                        }
                                                    },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                                    },
                                                    onClick = {
                                                        showTopBarResetMenu = false
                                                        viewModel.openShiftResetDialog()
                                                    }
                                                )
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text("تصفير مخصص وتحديد الخانات", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                            Text("تحديد مجموعات أو فئات معينة فقط", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                                        }
                                                    },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                    },
                                                    onClick = {
                                                        showTopBarResetMenu = false
                                                        showGlobalResetDialog = true
                                                    }
                                                )
                                            }
                                        }

                                        // Side Category Calculator Button
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                   else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.80f),
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.35f else 0.65f)
                                            ),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            IconButton(
                                                onClick = { showCategoryCalculatorDialog = true },
                                                modifier = Modifier.size(38.dp).testTag("top_bar_calculator_toggle")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Calculate,
                                                    contentDescription = "آلة حاسبة الفئات الجانبية",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        // Lock Given & Extra Button (زر إقفال المعطى والإضافي)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (uiState.isLockGivenExtraMode) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                                                   else if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                   else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.80f),
                                            border = BorderStroke(
                                                1.dp,
                                                if (uiState.isLockGivenExtraMode) MaterialTheme.colorScheme.tertiary
                                                else MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.35f else 0.65f)
                                            ),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.toggleLockGivenExtraMode() },
                                                modifier = Modifier.size(38.dp).testTag("top_bar_lock_given_toggle")
                                            ) {
                                                Icon(
                                                    imageVector = if (uiState.isLockGivenExtraMode) Icons.Default.Lock else Icons.Default.LockOpen,
                                                    contentDescription = if (uiState.isLockGivenExtraMode) "المعطى مقفل" else "قفل المعطى",
                                                    tint = if (uiState.isLockGivenExtraMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        // Read-Only / Block Input Button (زر منع الإدخال والقراءة فقط)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (uiState.isReadOnlyMode) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                                                   else if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                   else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.80f),
                                            border = BorderStroke(
                                                1.dp,
                                                if (uiState.isReadOnlyMode) MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.35f else 0.65f)
                                            ),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.toggleReadOnlyMode() },
                                                modifier = Modifier.size(38.dp).testTag("top_bar_read_only_toggle")
                                            ) {
                                                Icon(
                                                    imageVector = if (uiState.isReadOnlyMode) Icons.Default.EditOff else Icons.Default.Edit,
                                                    contentDescription = if (uiState.isReadOnlyMode) "الإدخال ممنوع (قراءة فقط)" else "منع الإدخال",
                                                    tint = if (uiState.isReadOnlyMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        // Close Day Button / Day Closed Indicator (زر إغلاق اليوم / حالة إغلاق اليوم)
                                        if (uiState.isDayClosed) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.errorContainer,
                                                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.error),
                                                modifier = Modifier
                                                    .height(38.dp)
                                                    .clickable { viewModel.requestCloseDay() }
                                                    .testTag("top_bar_day_closed_badge")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "اليوم مغلق",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "اليوم مغلق",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                       else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.80f),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.35f else 0.65f)
                                                ),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.requestCloseDay() },
                                                    modifier = Modifier.size(38.dp).testTag("top_bar_close_day_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LockClock,
                                                        contentDescription = AppStrings.get("close_day", lang),
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                },
                bottomBar = {
                    if (currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.ONBOARDING) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 10.dp) // Raised slightly
                                .vibrant3d(
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = 8.dp,
                                    isDark = isDark,
                                    gradientBrush = DesignSystem.navBarGradient(isDark, accent = MaterialTheme.colorScheme.primary)
                                ),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Transparent,
                            border = BorderStroke(
                                1.2.dp,
                                if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
                            )
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(72.dp)
                            ) {
                                val navItemColors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.90f),
                                    unselectedIconColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                    unselectedTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF334155)
                                )

                                // 1. الرئيسية (تجمع المبيعات والصندوق)
                                val homeLabel = AppStrings.get("nav_home", lang)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.HOME,
                                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = homeLabel
                                        )
                                    },
                                    label = { Text(homeLabel, fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.HOME) FontWeight.Bold else FontWeight.Normal) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag("nav_tab_home")
                                )

                                // 2. الأوضاع والقوالب
                                val modesLabel = AppStrings.get("nav_modes", lang)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.MANAGEMENT,
                                    onClick = { viewModel.navigateTo(AppScreen.MANAGEMENT) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.MANAGEMENT) Icons.Filled.DashboardCustomize else Icons.Outlined.DashboardCustomize,
                                            contentDescription = modesLabel
                                        )
                                    },
                                    label = { Text(modesLabel, fontSize = 9.5.sp, maxLines = 1, fontWeight = if (currentScreen == AppScreen.MANAGEMENT) FontWeight.Bold else FontWeight.Normal) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag("nav_tab_modes")
                                )

                                // 4. البيان المالي (التقرير) - بجانب الإعدادات
                                val reportsLabel = AppStrings.get("nav_reports", lang)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.REPORTS,
                                    onClick = { viewModel.navigateTo(AppScreen.REPORTS) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.REPORTS) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                                            contentDescription = reportsLabel
                                        )
                                    },
                                    label = { Text(reportsLabel, fontSize = 9.5.sp, maxLines = 1, fontWeight = if (currentScreen == AppScreen.REPORTS) FontWeight.Bold else FontWeight.Normal) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag("nav_tab_reports")
                                )

                                // 5. الإعدادات
                                val settingsLabel = AppStrings.get("nav_settings", lang)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.SETTINGS,
                                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = settingsLabel
                                        )
                                    },
                                    label = { Text(settingsLabel, fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag("nav_tab_settings")
                                )
                            }
                        }
                    }
                },
                content = { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(DesignSystem.surfaceGradient())
                    ) {
                        // Floating Visual Confirmation Banner for Shift Reset
                        AnimatedVisibility(
                            visible = uiState.shiftResetSuccessBannerMessage != null,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(Alignment.TopCenter)
                                .zIndex(100f)
                        ) {
                            uiState.shiftResetSuccessBannerMessage?.let { msg ->
                                LaunchedEffect(msg) {
                                    delay(4500)
                                    viewModel.dismissShiftResetBanner()
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF065F46),
                                    shadowElevation = 8.dp,
                                    border = BorderStroke(1.dp, Color(0xFF34D399)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = msg,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.dismissShiftResetBanner() },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "إغلاق",
                                                tint = Color.White.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 1200.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            Crossfade(
                                targetState = currentScreen,
                                label = "screen_transition",
                                modifier = Modifier.fillMaxSize()
                            ) { screen ->
                                when (screen) {
                                    AppScreen.SPLASH -> {
                                        SplashScreen(viewModel = viewModel)
                                    }
                                    AppScreen.ONBOARDING -> {
                                        OnboardingScreen(viewModel = viewModel)
                                    }
                                    AppScreen.HOME -> {
                                        HomeScreen(viewModel = viewModel)
                                    }
                                    AppScreen.DIRECT_SALES -> {
                                        DirectSalesScreen(viewModel = viewModel)
                                    }
                                    AppScreen.CASH_BOX -> {
                                        CashBoxScreen(viewModel = viewModel)
                                    }
                                    AppScreen.REPORTS -> {
                                        ReportsScreen(viewModel = viewModel)
                                    }
                                    AppScreen.MANAGEMENT -> {
                                        ManagementScreen(viewModel = viewModel)
                                    }
                                    AppScreen.MANAGEMENT_SALES -> {
                                        ManagementSalesGroupsScreen(viewModel = viewModel)
                                    }
                                    AppScreen.MANAGEMENT_CASH -> {
                                        ManagementCashGroupsScreen(viewModel = viewModel)
                                    }
                                    AppScreen.SETTINGS -> {
                                        SettingsScreen(viewModel = viewModel)
                                    }
                                    else -> {
                                        DirectSalesScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }

                        // Global Numpad overlay
                        GlobalCustomNumpad()

                        // Dialog: Confirm Closing the Day (حوار تأكيد إغلاق اليوم)
                        if (uiState.showCloseDayConfirmDialog) {
                            AlertDialog(
                                onDismissRequest = { viewModel.dismissCloseDayDialog() },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.LockClock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                },
                                title = {
                                    Text(
                                        text = "تأكيد إغلاق واعتماد اليوم الحسابي",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "هل أنت متأكد من رغبتك في إغلاق اليوم الحسابي الحالي واعتماده؟",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "• سيتم تجميد كافة الجداول، الفئات، والصناديق النقدية.",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "• لن يُقبل أي إدخال أو تعديل لهذا اليوم إلا بعد تحذير أمني شديد اللهجة وتحمل كامل المسؤولية.",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = "• يظل بإمكانك معاينة البيان المالي وطباعة وتصدير التقارير.",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { viewModel.confirmCloseDay() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("تأكيد إغلاق اليوم الآن 🔒", fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { viewModel.dismissCloseDayDialog() }
                                    ) {
                                        Text("تراجع")
                                    }
                                }
                            )
                        }

                        // Dialog: Stern Warning When Attempting Input or Unlocking Closed Day (تحذير شديد اللهجة للمستخدم)
                        if (uiState.showUnlockDaySternWarningDialog) {
                            AlertDialog(
                                onDismissRequest = { viewModel.dismissUnlockDaySternWarningDialog() },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                },
                                title = {
                                    Text(
                                        text = "⚠️ تحذير أمني شديد اللهجة: اليوم مغلق ومُعتمد!",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.error
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "⚠️ تنبيه محاسبي ورقابي بالغ الأهمية وعالي الخطورة:",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "هذا اليوم الحسابي قد تم إغلاقه واعتماده رسمياً وحساب إيراداته وأرصدته ومطابقة صناديقه النقدية.",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                                Text(
                                                    text = "⛔ لا يقبل النظام أي إدخال أو تعديل في الأرقام أو الفئات لهذا اليوم المغلق.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                                Text(
                                                    text = "• أي تعديل بالأرقام سيكسر سلامة السجلات والمطابقة المحاسبية المعتمدة.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "• قد يتسبب ذلك في حدوث عجز أو فروقات نقدية غير مبررة في قيود الصندوق.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "• سيتم تسجيل محاولة وتأكيد هذا الإجراء في سجل الرقابة والتدقيق (Audit Log) كإجراء استثنائي عالي الخطورة.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Text(
                                            text = "هل أنت متأكد تماماً وعلى مسؤوليتك الإدارية الكاملة من رغبتك في فك قفل هذا اليوم والسماح بالتعديل؟",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { viewModel.confirmUnlockDayWithSternWarning() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        )
                                    ) {
                                        Text("أتحمل المسؤولية وفك القفل ⚠️", fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    OutlinedButton(
                                        onClick = { viewModel.dismissUnlockDaySternWarningDialog() }
                                    ) {
                                        Text("تراجع (إبقاء اليوم مغلقاً)")
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}
}
