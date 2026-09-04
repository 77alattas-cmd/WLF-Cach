@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.example.ui.components

import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.model.*
import com.example.ui.viewmodel.TicketAccountingViewModel
import com.example.util.TafqeetHelper

/**
 * Full-featured Global In-App Accounting Calculator & Keypad
 * Applies to ALL numeric inputs across ALL sections and groups with live Tafqeet (تفقيط الأرقام).
 */
@Composable
fun AppNumericCalculator(
    viewModel: TicketAccountingViewModel,
    initialSection: CalcAppSection = CalcAppSection.SALES,
    initialGroupId: String = "group_sanad",
    initialDenom: Int = 1000,
    initialSalesField: TargetSalesField = TargetSalesField.GIVEN,
    initialCashField: TargetCashField = TargetCashField.YER_CASH,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val salesGroups: List<SalesGroupUiState> = uiState.groups
    val cashGroups: List<CashBoxGroupUiState> = uiState.cashGroups
    val useEastern = uiState.useEasternArabicNumerals

    var selectedSection by remember(initialSection) { mutableStateOf(initialSection) }
    var selectedSalesGroupId by remember(initialGroupId) { mutableStateOf(initialGroupId) }
    var selectedCashGroupId by remember { mutableStateOf(cashGroups.firstOrNull()?.id ?: CASH_GROUP_MAIN_ID) }

    // Active Sales Group & Denom
    val currentSalesGroup = salesGroups.find { it.id == selectedSalesGroupId } ?: salesGroups.firstOrNull()
    val denomRows = currentSalesGroup?.rows ?: emptyList()
    val availableDenoms = if (denomRows.isNotEmpty()) denomRows.map { it.denomination } else DEFAULT_TICKET_CATEGORIES

    var selectedDenomIndex by remember(availableDenoms) {
        val idx = availableDenoms.indexOf(initialDenom).coerceAtLeast(0)
        mutableIntStateOf(idx)
    }
    val currentDenom = availableDenoms.getOrElse(selectedDenomIndex) { 1000 }
    var currentSalesField by remember(initialSalesField) { mutableStateOf(initialSalesField) }

    // Active Cash Group & Cash Field
    val currentCashGroup = cashGroups.find { it.id == selectedCashGroupId } ?: cashGroups.firstOrNull()
    val cashDenomRows = currentCashGroup?.denomRows ?: emptyList()
    val availableCashDenoms = if (cashDenomRows.isNotEmpty()) cashDenomRows.map { it.denomination } else DEFAULT_CASH_DENOMINATIONS
    var selectedCashDenomIndex by remember(availableCashDenoms) { mutableIntStateOf(0) }
    val currentCashDenom = availableCashDenoms.getOrElse(selectedCashDenomIndex) { 1000 }
    var currentCashField by remember(initialCashField) { mutableStateOf(initialCashField) }

    // Calculator internal state
    var displayExpr by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var lastAppliedFeedback by remember { mutableStateOf<String?>(null) }
    var isFirstDigitInput by remember { mutableStateOf(false) }

    // Load current target value from ViewModel
    fun loadTargetValue(): String {
        return if (selectedSection == CalcAppSection.SALES) {
            val row = currentSalesGroup?.rows?.find { it.denomination == currentDenom }
            when (currentSalesField) {
                TargetSalesField.GIVEN -> row?.givenInput ?: ""
                TargetSalesField.ADDED -> row?.addedInput ?: ""
                TargetSalesField.REMAINING -> row?.remainingInput ?: ""
            }
        } else {
            when (currentCashField) {
                TargetCashField.YER_CASH -> uiState.cashInBoxYerInput
                TargetCashField.SAR_CASH -> uiState.cashInBoxSarInput
                TargetCashField.EXCHANGE_RATE -> uiState.exchangeRateInput
                TargetCashField.GROUP_DENOM_COUNT -> {
                    val row = currentCashGroup?.denomRows?.find { it.denomination == currentCashDenom }
                    row?.countInput ?: ""
                }
            }
        }
    }

    // Sync input when target changes
    LaunchedEffect(selectedSection, selectedSalesGroupId, currentDenom, currentSalesField, selectedCashGroupId, currentCashDenom, currentCashField) {
        val currentVal = loadTargetValue()
        currentInput = currentVal
        displayExpr = currentVal
        isFirstDigitInput = currentVal.isNotBlank() && currentVal != "0"
    }

    // Mathematical expression evaluator
    fun evaluateExpression(expr: String): Double? {
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/").trim()
            if (sanitized.isBlank()) return null
            val tokens = mutableListOf<String>()
            var currentToken = StringBuilder()
            for (ch in sanitized) {
                if (ch in "+-*/") {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString())
                        currentToken = StringBuilder()
                    }
                    tokens.add(ch.toString())
                } else if (ch.isDigit() || ch == '.') {
                    currentToken.append(ch)
                }
            }
            if (currentToken.isNotEmpty()) {
                tokens.add(currentToken.toString())
            }

            if (tokens.isEmpty()) return null

            var result = tokens[0].toDoubleOrNull() ?: return null
            var i = 1
            while (i < tokens.size - 1) {
                val op = tokens[i]
                val nextVal = tokens[i + 1].toDoubleOrNull() ?: 0.0
                when (op) {
                    "+" -> result += nextVal
                    "-" -> result -= nextVal
                    "*" -> result *= nextVal
                    "/" -> if (nextVal != 0.0) result /= nextVal
                }
                i += 2
            }
            result
        } catch (e: Exception) {
            null
        }
    }

    // Apply value to the target in the ViewModel
    fun applyValue(valueStr: String) {
        val cleaned = valueStr.filter { it.isDigit() || it == '.' }
        if (selectedSection == CalcAppSection.SALES) {
            val grpId = currentSalesGroup?.id ?: return
            when (currentSalesField) {
                TargetSalesField.GIVEN -> {
                    viewModel.updateGiven(grpId, currentDenom, cleaned)
                    lastAppliedFeedback = "تم إدخال المعطى لفئة $currentDenom ($cleaned)"
                }
                TargetSalesField.ADDED -> {
                    viewModel.updateAdded(grpId, currentDenom, cleaned)
                    lastAppliedFeedback = "تم إدخال الإضافة (+) لفئة $currentDenom ($cleaned)"
                }
                TargetSalesField.REMAINING -> {
                    viewModel.updateRemaining(grpId, currentDenom, cleaned)
                    lastAppliedFeedback = "تم إدخال المتبقي لفئة $currentDenom ($cleaned)"
                }
            }
        } else {
            when (currentCashField) {
                TargetCashField.YER_CASH -> {
                    viewModel.updateCashInBoxYer(cleaned)
                    lastAppliedFeedback = "تم تحديث النقد اليمني ($cleaned)"
                }
                TargetCashField.SAR_CASH -> {
                    viewModel.updateCashInBoxSar(cleaned)
                    lastAppliedFeedback = "تم تحديث النقد السعودي ($cleaned)"
                }
                TargetCashField.EXCHANGE_RATE -> {
                    viewModel.updateExchangeRate(cleaned)
                    lastAppliedFeedback = "تم تحديث سعر الصرف ($cleaned)"
                }
                TargetCashField.GROUP_DENOM_COUNT -> {
                    viewModel.updateCashDenomCount(selectedCashGroupId, currentCashDenom, cleaned)
                    lastAppliedFeedback = "تم تحديث عدد أوراق فئة $currentCashDenom ($cleaned)"
                }
            }
        }
    }

    fun handleDigitInput(digit: String) {
        if (isFirstDigitInput) {
            displayExpr = if (digit == ".") "0." else digit
            isFirstDigitInput = false
        } else {
            if (digit == ".") {
                if (!displayExpr.contains(".")) {
                    displayExpr = if (displayExpr.isEmpty()) "0." else displayExpr + "."
                }
            } else {
                displayExpr += digit
            }
        }
        applyValue(displayExpr)
    }

    // Field Navigation: Next
    fun navigateToNextField() {
        if (displayExpr.isNotBlank()) {
            val eval = evaluateExpression(displayExpr)
            val valStr = if (eval != null && eval.isFinite() && eval % 1.0 == 0.0 && kotlin.math.abs(eval) <= Long.MAX_VALUE) {
                eval.toLong().toString()
            } else (eval?.toString() ?: currentInput)
            applyValue(valStr)
        }

        if (selectedSection == CalcAppSection.SALES) {
            when (currentSalesField) {
                TargetSalesField.GIVEN -> currentSalesField = TargetSalesField.ADDED
                TargetSalesField.ADDED -> currentSalesField = TargetSalesField.REMAINING
                TargetSalesField.REMAINING -> {
                    currentSalesField = TargetSalesField.GIVEN
                    if (selectedDenomIndex < availableDenoms.size - 1) {
                        selectedDenomIndex++
                    } else {
                        selectedDenomIndex = 0
                    }
                }
            }
        } else {
            when (currentCashField) {
                TargetCashField.YER_CASH -> currentCashField = TargetCashField.SAR_CASH
                TargetCashField.SAR_CASH -> currentCashField = TargetCashField.EXCHANGE_RATE
                TargetCashField.EXCHANGE_RATE -> {
                    if (cashGroups.isNotEmpty()) {
                        currentCashField = TargetCashField.GROUP_DENOM_COUNT
                        selectedCashDenomIndex = 0
                    } else {
                        currentCashField = TargetCashField.YER_CASH
                    }
                }
                TargetCashField.GROUP_DENOM_COUNT -> {
                    if (selectedCashDenomIndex < availableCashDenoms.size - 1) {
                        selectedCashDenomIndex++
                    } else {
                        selectedCashDenomIndex = 0
                        currentCashField = TargetCashField.YER_CASH
                    }
                }
            }
        }
    }

    // Field Navigation: Previous
    fun navigateToPreviousField() {
        if (displayExpr.isNotBlank()) {
            val eval = evaluateExpression(displayExpr)
            val valStr = if (eval != null && eval.isFinite() && eval % 1.0 == 0.0 && kotlin.math.abs(eval) <= Long.MAX_VALUE) {
                eval.toLong().toString()
            } else (eval?.toString() ?: currentInput)
            applyValue(valStr)
        }

        if (selectedSection == CalcAppSection.SALES) {
            when (currentSalesField) {
                TargetSalesField.REMAINING -> currentSalesField = TargetSalesField.ADDED
                TargetSalesField.ADDED -> currentSalesField = TargetSalesField.GIVEN
                TargetSalesField.GIVEN -> {
                    currentSalesField = TargetSalesField.REMAINING
                    if (selectedDenomIndex > 0) selectedDenomIndex-- else selectedDenomIndex = availableDenoms.size - 1
                }
            }
        } else {
            when (currentCashField) {
                TargetCashField.GROUP_DENOM_COUNT -> {
                    if (selectedCashDenomIndex > 0) {
                        selectedCashDenomIndex--
                    } else {
                        currentCashField = TargetCashField.EXCHANGE_RATE
                    }
                }
                TargetCashField.EXCHANGE_RATE -> currentCashField = TargetCashField.SAR_CASH
                TargetCashField.SAR_CASH -> currentCashField = TargetCashField.YER_CASH
                TargetCashField.YER_CASH -> {
                    if (cashGroups.isNotEmpty()) {
                        currentCashField = TargetCashField.GROUP_DENOM_COUNT
                        selectedCashDenomIndex = availableCashDenoms.size - 1
                    } else {
                        currentCashField = TargetCashField.EXCHANGE_RATE
                    }
                }
            }
        }
    }

    // Row Navigation: Next Row
    fun navigateToNextRow() {
        if (displayExpr.isNotBlank()) {
            val eval = evaluateExpression(displayExpr)
            val valStr = if (eval != null && eval.isFinite() && eval % 1.0 == 0.0 && kotlin.math.abs(eval) <= Long.MAX_VALUE) {
                eval.toLong().toString()
            } else (eval?.toString() ?: currentInput)
            applyValue(valStr)
        }
        if (selectedSection == CalcAppSection.SALES) {
            if (availableDenoms.isNotEmpty()) {
                selectedDenomIndex = (selectedDenomIndex + 1) % availableDenoms.size
            }
        } else {
            if (availableCashDenoms.isNotEmpty()) {
                selectedCashDenomIndex = (selectedCashDenomIndex + 1) % availableCashDenoms.size
            }
        }
    }

    // Row Navigation: Previous Row
    fun navigateToPreviousRow() {
        if (displayExpr.isNotBlank()) {
            val eval = evaluateExpression(displayExpr)
            val valStr = if (eval != null && eval.isFinite() && eval % 1.0 == 0.0 && kotlin.math.abs(eval) <= Long.MAX_VALUE) {
                eval.toLong().toString()
            } else (eval?.toString() ?: currentInput)
            applyValue(valStr)
        }
        if (selectedSection == CalcAppSection.SALES) {
            if (availableDenoms.isNotEmpty()) {
                selectedDenomIndex = (selectedDenomIndex - 1 + availableDenoms.size) % availableDenoms.size
            }
        } else {
            if (availableCashDenoms.isNotEmpty()) {
                selectedCashDenomIndex = (selectedCashDenomIndex - 1 + availableCashDenoms.size) % availableCashDenoms.size
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_numeric_calculator"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Title, Section Switcher & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "الآلة الحاسبة المحاسبية الشاملة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "مع ميزة التفقيط المالي التلقائي للأرقام",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_calculator")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Section Toggle: المبيعات vs الصندوق
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcAppSection.values().forEach { sec ->
                    val isSelected = sec == selectedSection
                    Surface(
                        onClick = { selectedSection = sec },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = sec.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Group / Denomination selector
            if (selectedSection == CalcAppSection.SALES) {
                // Sales groups row (Display without scrolling)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    salesGroups.forEach { grp ->
                        val isSel = grp.id == selectedSalesGroupId
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedSalesGroupId = grp.id },
                            label = {
                                Text(
                                    text = grp.name + if (grp.isExcludedFromBalance) " (جانبي)" else "",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Denominations chips (Display ALL categories without scrolling)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableDenoms.forEach { denom ->
                        val isSel = denom == currentDenom
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedDenomIndex = availableDenoms.indexOf(denom)
                            },
                            label = {
                                Text(
                                    text = "فئة ${AccountingFormatter.formatNumber(denom, useEastern)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Field selector row (المعطى، إضافة +، المتبقي)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TargetSalesField.values().forEach { field ->
                        val isSel = field == currentSalesField
                        Surface(
                            onClick = { currentSalesField = field },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = field.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            } else {
                // Cash Box fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TargetCashField.values().forEach { field ->
                        val isSel = field == currentCashField
                        Surface(
                            onClick = { currentCashField = field },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = field.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                if (currentCashField == TargetCashField.GROUP_DENOM_COUNT && cashGroups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableCashDenoms.forEach { denom ->
                            val isSel = denom == currentCashDenom
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCashDenomIndex = availableCashDenoms.indexOf(denom) },
                                label = { Text("فئة ${AccountingFormatter.formatNumber(denom, useEastern)}") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Field & Calculated Result Display Screen
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedSection == CalcAppSection.SALES) {
                                "الحقل النشط: [${currentSalesGroup?.name}] فئة $currentDenom ⟵ ${currentSalesField.label}"
                            } else {
                                "الحقل النشط: قسم الصندوق ⟵ ${currentCashField.label}" + (if (currentCashField == TargetCashField.GROUP_DENOM_COUNT) " (فئة $currentCashDenom)" else "")
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        )

                        // Clear Key
                        Surface(
                            onClick = {
                                displayExpr = ""
                                currentInput = ""
                                applyValue("")
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.testTag("btn_calculator_clear")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("مسح", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error, fontSize = 10.sp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Expression / Input Text
                    val displayedText = if (useEastern) AccountingFormatter.toEasternArabicDigits(displayExpr.ifEmpty { "0" }) else displayExpr.ifEmpty { "0" }
                    Text(
                        text = displayedText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Evaluation preview & Tafqeet
                    val evalResult = evaluateExpression(displayExpr)
                    val numericVal = evalResult ?: displayExpr.toDoubleOrNull()
                    
                    if (evalResult != null && displayExpr.any { it in "+-×÷*/" }) {
                        val evalStr = if (evalResult.isFinite() && evalResult % 1.0 == 0.0 && kotlin.math.abs(evalResult) <= Long.MAX_VALUE) evalResult.toLong().toString() else evalResult.toString()
                        val formattedEval = if (useEastern) AccountingFormatter.toEasternArabicDigits(evalStr) else evalStr
                        Text(
                            text = "= $formattedEval",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Live Tafqeet (كتابة الرقم حرفياً بالعربية)
                    if (numericVal != null && numericVal > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val currency = if (selectedSection == CalcAppSection.CASH_BOX && currentCashField == TargetCashField.SAR_CASH) "SAR" else "YER"
                        Text(
                            text = TafqeetHelper.convert(numericVal, currency = currency, withPrefixAndSuffix = true),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.End
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (lastAppliedFeedback != null) {
                        Text(
                            text = "✓ $lastAppliedFeedback",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Controls: Rows and Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row Navigation (الصف السابق / الصف التالي)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { navigateToPreviousRow() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_calculator_prev_row"),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الصف السابق ⬆", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }

                    OutlinedButton(
                        onClick = { navigateToNextRow() },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_calculator_next_row"),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text("الصف التالي ⬇", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                // Cell/Field Navigation (الخانة السابقة / الخانة التالية)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { navigateToPreviousField() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_calculator_prev_field"),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الخانة السابقة", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }

                    Button(
                        onClick = { navigateToNextField() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_calculator_next_field"),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text("الخانة التالية", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calculator Keypad Matrix (5 Rows)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: C, ⌫, ÷, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcKey(
                        text = "C",
                        color = MaterialTheme.colorScheme.errorContainer,
                        textColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFirstDigitInput = false
                            displayExpr = ""
                            applyValue("")
                        }
                    )
                    CalcKey(
                        icon = Icons.Default.Backspace,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFirstDigitInput = false
                            if (displayExpr.isNotEmpty()) {
                                displayExpr = displayExpr.dropLast(1)
                                applyValue(displayExpr)
                            }
                        }
                    )
                    CalcKey(
                        text = "÷",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFirstDigitInput = false
                            if (displayExpr.isNotEmpty() && displayExpr.last() !in "+-×÷") {
                                displayExpr += "÷"
                            }
                        }
                    )
                    CalcKey(
                        text = "×",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFirstDigitInput = false
                            if (displayExpr.isNotEmpty() && displayExpr.last() !in "+-×÷") {
                                displayExpr += "×"
                            }
                        }
                    )
                }

                // Row 2: 7, 8, 9, -
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcKey(text = if (useEastern) "٧" else "7", modifier = Modifier.weight(1f), onClick = { handleDigitInput("7") })
                    CalcKey(text = if (useEastern) "٨" else "8", modifier = Modifier.weight(1f), onClick = { handleDigitInput("8") })
                    CalcKey(text = if (useEastern) "٩" else "9", modifier = Modifier.weight(1f), onClick = { handleDigitInput("9") })
                    CalcKey(
                        text = "-",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFirstDigitInput = false
                            if (displayExpr.isNotEmpty() && displayExpr.last() !in "+-×÷") {
                                displayExpr += "-"
                            }
                        }
                    )
                }

                // Row 3: 4, 5, 6, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcKey(text = if (useEastern) "٤" else "4", modifier = Modifier.weight(1f), onClick = { handleDigitInput("4") })
                    CalcKey(text = if (useEastern) "٥" else "5", modifier = Modifier.weight(1f), onClick = { handleDigitInput("5") })
                    CalcKey(text = if (useEastern) "٦" else "6", modifier = Modifier.weight(1f), onClick = { handleDigitInput("6") })
                    CalcKey(
                        text = "+",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFirstDigitInput = false
                            if (displayExpr.isNotEmpty() && displayExpr.last() !in "+-×÷") {
                                displayExpr += "+"
                            }
                        }
                    )
                }

                // Row 4: 1, 2, 3, =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcKey(text = if (useEastern) "١" else "1", modifier = Modifier.weight(1f), onClick = { handleDigitInput("1") })
                    CalcKey(text = if (useEastern) "٢" else "2", modifier = Modifier.weight(1f), onClick = { handleDigitInput("2") })
                    CalcKey(text = if (useEastern) "٣" else "3", modifier = Modifier.weight(1f), onClick = { handleDigitInput("3") })
                    CalcKey(
                        text = "=",
                        color = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFirstDigitInput = false
                            val eval = evaluateExpression(displayExpr)
                            if (eval != null) {
                                val finalStr = if (eval.isFinite() && eval % 1.0 == 0.0 && kotlin.math.abs(eval) <= Long.MAX_VALUE) eval.toLong().toString() else eval.toString()
                                displayExpr = finalStr
                                applyValue(finalStr)
                            }
                        }
                    )
                }

                // Row 5: 0, 00, ., ✓ تطبيق وانتقال
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcKey(text = if (useEastern) "٠" else "0", modifier = Modifier.weight(1f), onClick = { handleDigitInput("0") })
                    CalcKey(text = if (useEastern) "٠٠" else "00", modifier = Modifier.weight(1f), onClick = { handleDigitInput("00") })
                    CalcKey(text = if (useEastern) "٠٠٠" else "000", modifier = Modifier.weight(1f), onClick = { handleDigitInput("000") })

                    Button(
                        onClick = {
                            val eval = evaluateExpression(displayExpr)
                            val finalStr = if (eval != null && eval.isFinite() && eval % 1.0 == 0.0 && kotlin.math.abs(eval) <= Long.MAX_VALUE) {
                                eval.toLong().toString()
                            } else (eval?.toString() ?: displayExpr)
                            applyValue(finalStr)
                            navigateToNextField()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_calculator_apply_and_next")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("إدخال", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

/**
 * Universal Dialog for Any Numeric Field across the App
 * Allows direct calculations, quick numeric entry, and live Tafqeet (تفقيط الأرقام).
 */
@Composable
fun UniversalFieldCalculatorDialog(
    title: String,
    initialValue: String,
    currency: String = "YER",
    isCurrency: Boolean = true,
    useEasternDigits: Boolean = false,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var displayExpr by remember(initialValue) { mutableStateOf(initialValue) }
    var isFirstDigitInput by remember(initialValue) { mutableStateOf(initialValue.isNotBlank() && initialValue != "0") }

    fun handleDigit(digit: String) {
        if (isFirstDigitInput) {
            displayExpr = if (digit == ".") "0." else digit
            isFirstDigitInput = false
        } else {
            if (digit == ".") {
                if (!displayExpr.contains(".")) {
                    displayExpr = if (displayExpr.isEmpty()) "0." else displayExpr + "."
                }
            } else {
                displayExpr += digit
            }
        }
    }

    fun evaluateExpression(expr: String): Double? {
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/").trim()
            if (sanitized.isBlank()) return null
            val tokens = mutableListOf<String>()
            var currentToken = StringBuilder()
            for (ch in sanitized) {
                if (ch in "+-*/") {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString())
                        currentToken = StringBuilder()
                    }
                    tokens.add(ch.toString())
                } else if (ch.isDigit() || ch == '.') {
                    currentToken.append(ch)
                }
            }
            if (currentToken.isNotEmpty()) {
                tokens.add(currentToken.toString())
            }

            if (tokens.isEmpty()) return null

            var result = tokens[0].toDoubleOrNull() ?: return null
            var i = 1
            while (i < tokens.size - 1) {
                val op = tokens[i]
                val nextVal = tokens[i + 1].toDoubleOrNull() ?: 0.0
                when (op) {
                    "+" -> result += nextVal
                    "-" -> result -= nextVal
                    "*" -> result *= nextVal
                    "/" -> if (nextVal != 0.0) result /= nextVal
                }
                i += 2
            }
            result
        } catch (e: Exception) {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "حاسبة مخصصة للحقل مع التفقيط الفوري",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Display screen
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            val displayedText = if (useEasternDigits) AccountingFormatter.toEasternArabicDigits(displayExpr.ifEmpty { "0" }) else displayExpr.ifEmpty { "0" }
                            Text(
                                text = displayedText,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            val eval = evaluateExpression(displayExpr)
                            val numericVal = eval ?: displayExpr.toDoubleOrNull()

                            if (eval != null && displayExpr.any { it in "+-×÷*/" }) {
                                val evalStr = if (eval.isFinite() && eval % 1.0 == 0.0 && kotlin.math.abs(eval) <= Long.MAX_VALUE) eval.toLong().toString() else eval.toString()
                                val formattedEval = if (useEasternDigits) AccountingFormatter.toEasternArabicDigits(evalStr) else evalStr
                                Text(
                                    text = "= $formattedEval",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.End
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Live Tafqeet
                            if (numericVal != null && numericVal > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = TafqeetHelper.convert(
                                        numericVal,
                                        currency = if (isCurrency) currency else "NONE",
                                        withPrefixAndSuffix = true
                                    ),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.End
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Keypad
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Row 1: C, ⌫, ÷, ×
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CalcKey(
                                text = "C",
                                color = MaterialTheme.colorScheme.errorContainer,
                                textColor = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isFirstDigitInput = false
                                    displayExpr = ""
                                }
                            )
                            CalcKey(
                                icon = Icons.Default.Backspace,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isFirstDigitInput = false
                                    if (displayExpr.isNotEmpty()) displayExpr = displayExpr.dropLast(1)
                                }
                            )
                            CalcKey(
                                text = "÷",
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isFirstDigitInput = false
                                    if (displayExpr.isNotEmpty() && displayExpr.last() !in "+-×÷") displayExpr += "÷"
                                }
                            )
                            CalcKey(
                                text = "×",
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isFirstDigitInput = false
                                    if (displayExpr.isNotEmpty() && displayExpr.last() !in "+-×÷") displayExpr += "×"
                                }
                            )
                        }

                        // Row 2: 7, 8, 9, -
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CalcKey(text = if (useEasternDigits) "٧" else "7", modifier = Modifier.weight(1f), onClick = { handleDigit("7") })
                            CalcKey(text = if (useEasternDigits) "٨" else "8", modifier = Modifier.weight(1f), onClick = { handleDigit("8") })
                            CalcKey(text = if (useEasternDigits) "٩" else "9", modifier = Modifier.weight(1f), onClick = { handleDigit("9") })
                            CalcKey(
                                text = "-",
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isFirstDigitInput = false
                                    if (displayExpr.isNotEmpty() && displayExpr.last() !in "+-×÷") displayExpr += "-"
                                }
                            )
                        }

                        // Row 3: 4, 5, 6, +
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CalcKey(text = if (useEasternDigits) "٤" else "4", modifier = Modifier.weight(1f), onClick = { handleDigit("4") })
                            CalcKey(text = if (useEasternDigits) "٥" else "5", modifier = Modifier.weight(1f), onClick = { handleDigit("5") })
                            CalcKey(text = if (useEasternDigits) "٦" else "6", modifier = Modifier.weight(1f), onClick = { handleDigit("6") })
                            CalcKey(
                                text = "+",
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isFirstDigitInput = false
                                    if (displayExpr.isNotEmpty() && displayExpr.last() !in "+-×÷") displayExpr += "+"
                                }
                            )
                        }

                        // Row 4: 1, 2, 3, =
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CalcKey(text = if (useEasternDigits) "١" else "1", modifier = Modifier.weight(1f), onClick = { handleDigit("1") })
                            CalcKey(text = if (useEasternDigits) "٢" else "2", modifier = Modifier.weight(1f), onClick = { handleDigit("2") })
                            CalcKey(text = if (useEasternDigits) "٣" else "3", modifier = Modifier.weight(1f), onClick = { handleDigit("3") })
                            CalcKey(
                                text = "=",
                                color = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    isFirstDigitInput = false
                                    val eval = evaluateExpression(displayExpr)
                                    if (eval != null) {
                                        displayExpr = if (eval.isFinite() && eval % 1.0 == 0.0 && kotlin.math.abs(eval) <= Long.MAX_VALUE) eval.toLong().toString() else eval.toString()
                                    }
                                }
                            )
                        }

                        // Row 5: 0, 00, ., تطبيق
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CalcKey(text = if (useEasternDigits) "٠" else "0", modifier = Modifier.weight(1f), onClick = { handleDigit("0") })
                            CalcKey(text = if (useEasternDigits) "٠٠" else "00", modifier = Modifier.weight(1f), onClick = { handleDigit("00") })
                            CalcKey(text = if (useEasternDigits) "٠٠٠" else "000", modifier = Modifier.weight(1f), onClick = { handleDigit("000") })

                            Button(
                                onClick = {
                                    val eval = evaluateExpression(displayExpr)
                                    val finalStr = if (eval != null && eval.isFinite() && eval % 1.0 == 0.0 && kotlin.math.abs(eval) <= Long.MAX_VALUE) {
                                        eval.toLong().toString()
                                    } else (eval?.toString() ?: displayExpr)
                                    onApply(finalStr)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("تطبيق", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Key button for the calculator keypad
 */
@Composable
private fun CalcKey(
    text: String? = null,
    icon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = color,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.height(34.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

/**
 * Dedicated In-App Calculator Dialog for ANY single numeric field in the app.
 * Allows quick calculation, addition, multiplication, Tafqeet and applying to the target field.
 */
@Composable
fun FieldCalculatorDialog(
    title: String,
    initialValue: String,
    unit: String = "",
    useEasternDigits: Boolean = false,
    isCurrency: Boolean = false,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    var displayExpr by remember { mutableStateOf(initialValue) }
    var resultPreview by remember { mutableStateOf("") }

    // Evaluator helper
    fun evaluate(expr: String): String {
        val sanitized = expr.replace("×", "*").replace("÷", "/").trim()
        if (sanitized.isBlank()) return ""
        return try {
            val tokens = mutableListOf<String>()
            var currentToken = StringBuilder()
            for (ch in sanitized) {
                if (ch in "+-*/") {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString())
                        currentToken = StringBuilder()
                    }
                    tokens.add(ch.toString())
                } else if (ch.isDigit() || ch == '.') {
                    currentToken.append(ch)
                }
            }
            if (currentToken.isNotEmpty()) tokens.add(currentToken.toString())
            if (tokens.isEmpty()) return ""

            var result = tokens[0].toDoubleOrNull() ?: return ""
            var i = 1
            while (i < tokens.size - 1) {
                val op = tokens[i]
                val nextVal = tokens[i + 1].toDoubleOrNull() ?: 0.0
                when (op) {
                    "+" -> result += nextVal
                    "-" -> result -= nextVal
                    "*" -> result *= nextVal
                    "/" -> if (nextVal != 0.0) result /= nextVal
                }
                i += 2
            }
            if (result.isFinite() && result % 1.0 == 0.0 && kotlin.math.abs(result) <= Long.MAX_VALUE) result.toLong().toString() else "%.2f".format(Locale.US, result)
        } catch (_: Exception) {
            ""
        }
    }

    LaunchedEffect(displayExpr) {
        val eval = evaluate(displayExpr)
        resultPreview = if (eval.isNotBlank() && eval != displayExpr) eval else ""
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                // Display screen
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (displayExpr.isBlank()) "0" else displayExpr,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp
                            ),
                            maxLines = 1
                        )

                        if (resultPreview.isNotBlank()) {
                            Text(
                                text = "= $resultPreview $unit",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        // Live Tafqeet
                        val activeNum = (if (resultPreview.isNotBlank()) resultPreview else displayExpr).toDoubleOrNull() ?: 0.0
                        if (activeNum > 0) {
                            val curr = if (unit.contains("ر.س") || unit.contains("سعودي")) "SAR" else if (unit.contains("ر.ي") || unit.contains("يمني") || unit.contains("ريال")) "YER" else "NONE"
                            Text(
                                text = TafqeetHelper.convert(activeNum, currency = curr, withPrefixAndSuffix = true),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.End
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Stepper Shortcuts row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("+1", "+5", "+10", "+50", "+100", "+500").forEach { step ->
                        Surface(
                            onClick = {
                                val currentVal = (evaluate(displayExpr).ifBlank { displayExpr }).toDoubleOrNull() ?: 0.0
                                val addVal = step.replace("+", "").toDoubleOrNull() ?: 0.0
                                val newVal = currentVal + addVal
                                displayExpr = if (newVal.isFinite() && newVal % 1.0 == 0.0 && kotlin.math.abs(newVal) <= Long.MAX_VALUE) newVal.toLong().toString() else newVal.toString()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                // Calculator Keypad Grid
                val keypadRows = listOf(
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "-"),
                    listOf("C", "0", "⌫", "+")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    keypadRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { key ->
                                val isOp = key in listOf("+", "-", "×", "÷")
                                val isClear = key == "C"
                                val isBack = key == "⌫"

                                Surface(
                                    onClick = {
                                        when (key) {
                                            "C" -> displayExpr = ""
                                            "⌫" -> if (displayExpr.isNotEmpty()) displayExpr = displayExpr.dropLast(1)
                                            "+", "-", "×", "÷" -> {
                                                if (displayExpr.isNotEmpty() && displayExpr.last() !in "+-×÷") {
                                                    displayExpr += key
                                                }
                                            }
                                            else -> displayExpr += key
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isOp -> MaterialTheme.colorScheme.primaryContainer
                                        isClear -> MaterialTheme.colorScheme.errorContainer
                                        isBack -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isOp -> MaterialTheme.colorScheme.primary
                                                    isClear -> MaterialTheme.colorScheme.error
                                                    isBack -> MaterialTheme.colorScheme.onSecondaryContainer
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Apply / Confirm Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val finalVal = evaluate(displayExpr).ifBlank { displayExpr }.trim()
                            onApply(finalVal)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.6f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تطبيق وحفظ")
                    }
                }
            }
        }
    }
}

/**
 * Dialog wrapper for the in-app numeric calculator
 */
@Composable
fun AppCalculatorDialog(
    viewModel: TicketAccountingViewModel,
    initialSection: CalcAppSection = CalcAppSection.SALES,
    initialGroupId: String = "group_sanad",
    initialDenom: Int = 1000,
    initialSalesField: TargetSalesField = TargetSalesField.GIVEN,
    initialCashField: TargetCashField = TargetCashField.YER_CASH,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            AppNumericCalculator(
                viewModel = viewModel,
                initialSection = initialSection,
                initialGroupId = initialGroupId,
                initialDenom = initialDenom,
                initialSalesField = initialSalesField,
                initialCashField = initialCashField,
                onDismiss = onDismiss
            )
        }
    }
}
