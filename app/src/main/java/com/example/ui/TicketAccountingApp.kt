package com.example.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.font.FontWeight
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
                else -> Modifier
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
                    containerColor = if (uiState.appBackgroundStyle != "DEFAULT") Color.Transparent else MaterialTheme.colorScheme.background,
                    topBar = {
                    if (currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.ONBOARDING) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .vibrant3d(
                                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                                    elevation = 8.dp,
                                    isDark = isDark,
                                    baseColor = MaterialTheme.colorScheme.surface
                                ),
                            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                        ) {
                            TopAppBar(
                                title = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.ConfirmationNumber,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "WLF Cash",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp
                                            )
                                        )
                                    }
                                },
                                actions = {
                                    // Reset Shift & Scope Options Button (تصفير الوردية والبيانات)
                                    var showTopBarResetMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(
                                            onClick = { showTopBarResetMenu = true },
                                            modifier = Modifier.testTag("top_bar_reset_toggle")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RestartAlt,
                                                contentDescription = "خيارات تصفير الوردية والبيانات",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                                            )
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

                                    // Lock Given & Extra Toggle Button (منع إدخال المعطى والإضافي)
                                    IconButton(
                                        onClick = { viewModel.toggleLockGivenExtraMode() },
                                        modifier = Modifier.testTag("top_bar_lock_given_extra_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isLockGivenExtraMode) Icons.Default.Block else Icons.Default.Edit,
                                            contentDescription = AppStrings.get("lock_given_extra", lang),
                                            tint = if (uiState.isLockGivenExtraMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Read-Only Mode Toggle Button (وضع القراءة)
                                    IconButton(
                                        onClick = { viewModel.toggleReadOnlyMode() },
                                        modifier = Modifier.testTag("top_bar_readonly_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isReadOnlyMode) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = AppStrings.get("read_only_mode", lang),
                                            tint = if (uiState.isReadOnlyMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Day / Night Theme Toggle Button
                                    IconButton(
                                        onClick = { viewModel.toggleTheme() },
                                        modifier = Modifier.testTag("top_bar_theme_toggle")
                                    ) {
                                        Icon(
                                             imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = if (isDarkTheme) AppStrings.get("light_mode", lang) else AppStrings.get("dark_mode", lang),
                                            tint = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Review Mode Toggle Button
                                    IconButton(
                                        onClick = { viewModel.toggleReviewMode() },
                                        modifier = Modifier.testTag("top_bar_review_toggle")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FactCheck,
                                            contentDescription = "وضع المراجعة",
                                            tint = if (uiState.isReviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
                                .vibrant3d(
                                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                                    elevation = 8.dp,
                                    isDark = isDark,
                                    baseColor = MaterialTheme.colorScheme.surface
                                ),
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                modifier = Modifier.height(72.dp)
                            ) {
                                // 1. المبيعات (سند)
                                val directSalesLabel = AppStrings.get("nav_direct_sales", lang)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.DIRECT_SALES,
                                    onClick = { viewModel.navigateTo(AppScreen.DIRECT_SALES) },
                                    icon = {
                                        Box(contentAlignment = Alignment.TopEnd) {
                                            Icon(
                                                if (currentScreen == AppScreen.DIRECT_SALES) Icons.Filled.ConfirmationNumber else Icons.Outlined.ConfirmationNumber,
                                                contentDescription = directSalesLabel,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Icon(
                                                Icons.Filled.KeyboardArrowUp,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .offset(x = 6.dp, y = (-6).dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    label = { Text(directSalesLabel, fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.DIRECT_SALES) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_tab_direct_sales")
                                )

                                // 2. نقدي (الصندوق)
                                val cashBoxLabel = AppStrings.get("nav_cash_box", lang)
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.CASH_BOX,
                                    onClick = { viewModel.navigateTo(AppScreen.CASH_BOX) },
                                    icon = {
                                        Icon(
                                            if (currentScreen == AppScreen.CASH_BOX) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                                            contentDescription = cashBoxLabel
                                        )
                                    },
                                    label = { Text(cashBoxLabel, fontSize = 10.sp, fontWeight = if (currentScreen == AppScreen.CASH_BOX) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier.testTag("nav_tab_cash_box")
                                )

                                // 3. الأوضاع والقوالب
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
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
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
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
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
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
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
                                if (uiState.isReviewMode) {
                                    ReviewModeScreen(viewModel = viewModel)
                                } else {
                                    when (screen) {
                                        AppScreen.SPLASH -> {
                                            SplashScreen(viewModel = viewModel)
                                        }
                                        AppScreen.ONBOARDING -> {
                                            OnboardingScreen(viewModel = viewModel)
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
                                        AppScreen.SETTINGS -> {
                                            SettingsScreen(viewModel = viewModel)
                                        }
                                        else -> {
                                            DirectSalesScreen(viewModel = viewModel)
                                        }
                                    }
                                }
                            }
                        }



                        // Global Numpad overlay
                        GlobalCustomNumpad()
                    }
                }
            )
        }
    }
}
}
