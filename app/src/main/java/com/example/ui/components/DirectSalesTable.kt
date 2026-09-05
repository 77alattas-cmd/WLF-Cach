package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.AccountingFormatter
import com.example.ui.model.DEFAULT_TICKET_CATEGORIES
import com.example.ui.model.DirectSalesRowUiState
import com.example.ui.model.CustomColorThemeState
import kotlinx.coroutines.launch

enum class SalesCellType(val label: String) {
    GIVEN("المعطى"),
    ADDED("الإضافي"),
    REMAINING("المتبقي")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DirectSalesTable(
    rows: List<DirectSalesRowUiState>,
    groupName: String = "سند",
    isEnabled: Boolean = true,
    isLockGivenExtraMode: Boolean = false,
    isAddedFieldEnabled: Boolean = false,
    mergeAddedWithGiven: Boolean = false,
    showRemainingStepper: Boolean = false,
    customColorState: CustomColorThemeState = CustomColorThemeState(),
    onToggleEnabled: (() -> Unit)? = null,
    onToggleMergeAdded: ((Boolean) -> Unit)? = null,
    onGivenChange: (Int, String) -> Unit,
    onAddedChange: (Int, String) -> Unit,
    onRemainingChange: (Int, String) -> Unit,
    onNotesChange: (Int, String) -> Unit,
    onZeroOutRemaining: (Int) -> Unit,
    onZeroOutAllRemaining: () -> Unit,
    onRemoveCategory: (Int) -> Unit,
    onToggleRowEnabled: (Int) -> Unit,
    onMoveRow: ((Int, Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val effectiveAddedEnabled = isAddedFieldEnabled && !mergeAddedWithGiven
    val totalRevenue = if (isEnabled) rows.sumOf { it.total } else 0.0
    val tableCardBg = customColorState.getColorOrNull(customColorState.tableCardBg) ?: MaterialTheme.colorScheme.surface
    val tableHeaderBg = customColorState.getColorOrNull(customColorState.tableHeaderBg) ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    val tableBorder = customColorState.getColorOrNull(customColorState.tableBorderColor) ?: MaterialTheme.colorScheme.outlineVariant
    val numpad = LocalNumpadController.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var activeFocusRowIndex by remember { mutableStateOf<Int?>(null) }
    var activeFocusCellType by remember { mutableStateOf<SalesCellType?>(null) }
    var showZeroAllConfirmDialog by remember { mutableStateOf(false) }
    var isTableReorderEnabled by remember { mutableStateOf(false) }

    // Synchronize active focus state with numpad visibility
    if (!numpad.isVisible && activeFocusRowIndex != null) {
        activeFocusRowIndex = null
        activeFocusCellType = null
    }

    fun openCell(rowIndex: Int, cellType: SalesCellType) {
        if (rowIndex !in rows.indices) return
        if (isLockGivenExtraMode && (cellType == SalesCellType.GIVEN || cellType == SalesCellType.ADDED)) return
        val row = rows[rowIndex]
        val initialVal = when (cellType) {
            SalesCellType.GIVEN -> row.givenInput
            SalesCellType.ADDED -> row.addedInput
            SalesCellType.REMAINING -> row.remainingInput
        }

        activeFocusRowIndex = rowIndex
        activeFocusCellType = cellType
        keyboardController?.hide()

        val activeCellKey = when (cellType) {
            SalesCellType.GIVEN -> "GIVEN"
            SalesCellType.ADDED -> "ADDED"
            SalesCellType.REMAINING -> "REMAINING"
        }

        val rowPreview = NumpadRowPreview(
            groupName = groupName,
            denomination = row.denomination,
            given = row.givenInput,
            added = row.addedInput,
            remaining = row.remainingInput,
            sold = row.sold,
            total = row.total,
            activeCell = activeCellKey,
            isAddedEnabled = effectiveAddedEnabled,
            isLocked = isLockGivenExtraMode,
            onSelectCell = { targetCell ->
                when (targetCell) {
                    "GIVEN" -> openCell(rowIndex, SalesCellType.GIVEN)
                    "ADDED" -> openCell(rowIndex, SalesCellType.ADDED)
                    "REMAINING" -> openCell(rowIndex, SalesCellType.REMAINING)
                }
            }
        )

        numpad.show(
            initialValue = initialVal,
            targetTitle = "[$groupName] فئة ${row.denomination} - ${cellType.label}",
            targetSubtitle = "الصف ${rowIndex + 1} من ${rows.size} | ${cellType.label}",
            rowPreview = rowPreview,
            onValueChanged = { newVal ->
                val digits = newVal.filter { it.isDigit() }
                when (cellType) {
                    SalesCellType.GIVEN -> onGivenChange(row.denomination, digits)
                    SalesCellType.ADDED -> onAddedChange(row.denomination, digits)
                    SalesCellType.REMAINING -> onRemainingChange(row.denomination, digits)
                }
                numpad.updateTargetInfo(
                    title = "[$groupName] فئة ${row.denomination} - ${cellType.label}",
                    subtitle = "الصف ${rowIndex + 1} من ${rows.size} | ${cellType.label}",
                    rowPreview = rowPreview.copy(
                        given = if (cellType == SalesCellType.GIVEN) digits else row.givenInput,
                        added = if (cellType == SalesCellType.ADDED) digits else row.addedInput,
                        remaining = if (cellType == SalesCellType.REMAINING) digits else row.remainingInput
                    )
                )
            },
            onNextCell = {
                val currentDigits = numpad.value.filter { it.isDigit() }
                when (cellType) {
                    SalesCellType.GIVEN -> onGivenChange(row.denomination, currentDigits)
                    SalesCellType.ADDED -> onAddedChange(row.denomination, currentDigits)
                    SalesCellType.REMAINING -> onRemainingChange(row.denomination, currentDigits)
                }
                when (cellType) {
                    SalesCellType.GIVEN -> {
                        if (!isLockGivenExtraMode && effectiveAddedEnabled) openCell(rowIndex, SalesCellType.ADDED)
                        else openCell(rowIndex, SalesCellType.REMAINING)
                    }
                    SalesCellType.ADDED -> {
                        openCell(rowIndex, SalesCellType.REMAINING)
                    }
                    SalesCellType.REMAINING -> {
                        val nextRow = if (rowIndex < rows.size - 1) rowIndex + 1 else 0
                        val targetType = if (isLockGivenExtraMode) SalesCellType.REMAINING else SalesCellType.GIVEN
                        openCell(nextRow, targetType)
                    }
                }
            },
            onPrevCell = {
                val currentDigits = numpad.value.filter { it.isDigit() }
                when (cellType) {
                    SalesCellType.GIVEN -> onGivenChange(row.denomination, currentDigits)
                    SalesCellType.ADDED -> onAddedChange(row.denomination, currentDigits)
                    SalesCellType.REMAINING -> onRemainingChange(row.denomination, currentDigits)
                }
                when (cellType) {
                    SalesCellType.REMAINING -> {
                        if (!isLockGivenExtraMode && effectiveAddedEnabled) openCell(rowIndex, SalesCellType.ADDED)
                        else if (!isLockGivenExtraMode) openCell(rowIndex, SalesCellType.GIVEN)
                        else {
                            val prevRow = if (rowIndex > 0) rowIndex - 1 else rows.size - 1
                            openCell(prevRow, SalesCellType.REMAINING)
                        }
                    }
                    SalesCellType.ADDED -> {
                        openCell(rowIndex, SalesCellType.GIVEN)
                    }
                    SalesCellType.GIVEN -> {
                        val prevRow = if (rowIndex > 0) rowIndex - 1 else rows.size - 1
                        openCell(prevRow, SalesCellType.REMAINING)
                    }
                }
            },
            onNextRow = {
                val currentDigits = numpad.value.filter { it.isDigit() }
                when (cellType) {
                    SalesCellType.GIVEN -> onGivenChange(row.denomination, currentDigits)
                    SalesCellType.ADDED -> onAddedChange(row.denomination, currentDigits)
                    SalesCellType.REMAINING -> onRemainingChange(row.denomination, currentDigits)
                }
                val nextRowIndex = (rowIndex + 1) % rows.size
                openCell(nextRowIndex, cellType)
            },
            onPrevRow = {
                val currentDigits = numpad.value.filter { it.isDigit() }
                when (cellType) {
                    SalesCellType.GIVEN -> onGivenChange(row.denomination, currentDigits)
                    SalesCellType.ADDED -> onAddedChange(row.denomination, currentDigits)
                    SalesCellType.REMAINING -> onRemainingChange(row.denomination, currentDigits)
                }
                val prevRowIndex = (rowIndex - 1 + rows.size) % rows.size
                openCell(prevRowIndex, cellType)
            },
            onDone = {
                val currentDigits = numpad.value.filter { it.isDigit() }
                when (cellType) {
                    SalesCellType.GIVEN -> onGivenChange(row.denomination, currentDigits)
                    SalesCellType.ADDED -> onAddedChange(row.denomination, currentDigits)
                    SalesCellType.REMAINING -> onRemainingChange(row.denomination, currentDigits)
                }
                activeFocusRowIndex = null
                activeFocusCellType = null
                numpad.hide()
            }
        )
    }

    val effectiveTableCardBg = customColorState.getEffectiveTableCardBg(tableCardBg)
    val effectiveTableHeaderBg = customColorState.getEffectiveTableHeaderBg(tableHeaderBg)

    Column(modifier = modifier.fillMaxWidth()) {
        // Table Container Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isEnabled) effectiveTableCardBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(
                1.dp,
                if (isEnabled) tableBorder else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Table Header Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "$groupName",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "الإجمالي: ${AccountingFormatter.formatYer(totalRevenue)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        )
                    }

                    if (isEnabled && !isLockGivenExtraMode && rows.size > 1) {
                        FilterChip(
                            selected = isTableReorderEnabled,
                            onClick = { isTableReorderEnabled = !isTableReorderEnabled },
                            label = {
                                Text(
                                    text = if (isTableReorderEnabled) "السحب مفعّل ⇅" else "ترتيب الفئات",
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isTableReorderEnabled) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isTableReorderEnabled) Icons.Default.SwapVert else Icons.Default.DragHandle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("btn_table_reorder_${groupName}")
                        )
                    }
                }

                AnimatedVisibility(visible = isTableReorderEnabled) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "وضع السحب مفعّل لهذا الجدول فقط: اضغط مطولاً على أي فئة واسحبها لتغيير ترتيبها.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            )
                        }
                    }
                }

                if (isAddedFieldEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = mergeAddedWithGiven,
                                    onCheckedChange = { onToggleMergeAdded?.invoke(it) },
                                    modifier = Modifier.size(24.dp).testTag("checkbox_merge_added_${groupName}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "دمج الإضافي مع المعطى (إضافة بدون حساب تلقائي كمباع)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.MergeType,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val headerTextColor = customColorState.getColorOrNull(customColorState.tableHeaderText) ?: MaterialTheme.colorScheme.onSurfaceVariant

                // Table Column Headers
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = effectiveTableHeaderBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الفئة",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = headerTextColor),
                            modifier = Modifier.width(if (effectiveAddedEnabled) 42.dp else 48.dp),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (isLockGivenExtraMode) "المعطى 🔒" else "المعطى",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isLockGivenExtraMode) MaterialTheme.colorScheme.error else headerTextColor),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        if (effectiveAddedEnabled) {
                            Text(
                                text = if (isLockGivenExtraMode) "إضافي 🔒" else "إضافي",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isLockGivenExtraMode) MaterialTheme.colorScheme.error else headerTextColor),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }

                        Text(
                            text = "المتبقي",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = headerTextColor),
                            modifier = Modifier.weight(if (effectiveAddedEnabled) 1.6f else 1.8f),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "المباع / الإجمالي",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp, color = headerTextColor),
                            modifier = Modifier.width(if (effectiveAddedEnabled) 70.dp else 80.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Denomination Rows
                rows.forEachIndexed { index, rowState ->
                    val isRowActive = activeFocusRowIndex == index

                    InteractiveSalesRow(
                        row = rowState,
                        isEnabled = isEnabled && rowState.isEnabled,
                        isAddedFieldEnabled = effectiveAddedEnabled,
                        mergeAddedWithGiven = mergeAddedWithGiven,
                        isLockGivenExtraMode = isLockGivenExtraMode,
                        isRowFocused = isRowActive,
                        focusedCellType = if (isRowActive) activeFocusCellType else null,
                        canDelete = !DEFAULT_TICKET_CATEGORIES.contains(rowState.denomination),
                        customColorState = customColorState,
                        showRemainingStepper = showRemainingStepper,
                        onRemainingChange = onRemainingChange,
                        onOpenGiven = { openCell(index, SalesCellType.GIVEN) },
                        onOpenAdded = { openCell(index, SalesCellType.ADDED) },
                        onOpenRemaining = { openCell(index, SalesCellType.REMAINING) },
                        onZeroOut = { onZeroOutRemaining(rowState.denomination) },
                        onToggleRowEnabled = { onToggleRowEnabled(rowState.denomination) },
                        onDelete = { onRemoveCategory(rowState.denomination) },
                        modifier = Modifier.reorderableVerticalItem(
                            index = index,
                            itemCount = rows.size,
                            isDragEnabled = isTableReorderEnabled && isEnabled && !isLockGivenExtraMode,
                            onMove = { from, to -> onMoveRow?.invoke(from, to) }
                        )
                    )

                    if (index < rows.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

/**
 * Single Category Row with clear visual focus when active
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun InteractiveSalesRow(
    row: DirectSalesRowUiState,
    isEnabled: Boolean,
    isAddedFieldEnabled: Boolean,
    mergeAddedWithGiven: Boolean = false,
    isLockGivenExtraMode: Boolean = false,
    isRowFocused: Boolean = false,
    focusedCellType: SalesCellType? = null,
    canDelete: Boolean,
    showRemainingStepper: Boolean = false,
    onRemainingChange: (Int, String) -> Unit = { _, _ -> },
    onOpenGiven: () -> Unit,
    onOpenAdded: () -> Unit,
    onOpenRemaining: () -> Unit,
    onZeroOut: () -> Unit,
    onToggleRowEnabled: () -> Unit,
    onDelete: () -> Unit,
    customColorState: CustomColorThemeState = CustomColorThemeState(),
    modifier: Modifier = Modifier
) {
    val isCompact = com.example.ui.LocalCompactMode.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val numpad = LocalNumpadController.current

    LaunchedEffect(isRowFocused, focusedCellType, numpad.isVisible) {
        if (isRowFocused) {
            kotlinx.coroutines.delay(150)
            try {
                bringIntoViewRequester.bringIntoView()
            } catch (_: Exception) {}
        }
    }

    val cellEffectiveBg = customColorState.getEffectiveTableCellBg(MaterialTheme.colorScheme.surface)

    Surface(
        shape = RoundedCornerShape(if (isCompact) 8.dp else 12.dp),
        color = when {
            isRowFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            isEnabled -> cellEffectiveBg
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        border = BorderStroke(
            if (isRowFocused) 2.dp else 0.8.dp,
            when {
                row.isRemainingExceeded -> MaterialTheme.colorScheme.error
                isRowFocused -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }
        ),
        shadowElevation = if (isRowFocused) 4.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Denomination Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isRowFocused) MaterialTheme.colorScheme.primary else if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.width(if (isAddedFieldEnabled) 42.dp else (if (isCompact) 44.dp else 48.dp))
                ) {
                    Text(
                        text = "${row.denomination}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isRowFocused) MaterialTheme.colorScheme.onPrimary else if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline,
                            fontSize = if (isCompact) 11.sp else 12.sp,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Given Input Box
                val displayedGiven = if (mergeAddedWithGiven) {
                    val sum = row.given + row.added
                    if (sum > 0) sum.toString() else ""
                } else {
                    row.givenInput
                }

                SalesCellButton(
                    value = displayedGiven,
                    placeholder = "0",
                    enabled = isEnabled && !isLockGivenExtraMode,
                    isFocused = isRowFocused && focusedCellType == SalesCellType.GIVEN,
                    onClick = onOpenGiven,
                    modifier = Modifier.weight(1f),
                    testTag = "input_given_${row.denomination}"
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Added Input Box
                if (isAddedFieldEnabled) {
                    SalesCellButton(
                        value = row.addedInput,
                        placeholder = "0",
                        enabled = isEnabled && !isLockGivenExtraMode,
                        isFocused = isRowFocused && focusedCellType == SalesCellType.ADDED,
                        onClick = onOpenAdded,
                        modifier = Modifier.weight(1f),
                        testTag = "input_added_${row.denomination}"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Remaining Input Box + Quick "0" Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(if (isAddedFieldEnabled) 1.6f else 1.8f)
                ) {
                    if (isEnabled && showRemainingStepper) {
                        Surface(
                            onClick = {
                                val currentRemaining = row.remaining
                                val newRemaining = (currentRemaining - 1).coerceAtLeast(0)
                                onRemainingChange(row.denomination, newRemaining.toString())
                            },
                            enabled = isEnabled,
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.size(if (isCompact || isAddedFieldEnabled) 20.dp else 24.dp).testTag("btn_dec_${row.denomination}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "-",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                    }

                    SalesCellButton(
                        value = row.remainingInput,
                        placeholder = "0",
                        enabled = isEnabled,
                        isError = row.isRemainingExceeded,
                        isFocused = isRowFocused && focusedCellType == SalesCellType.REMAINING,
                        onClick = onOpenRemaining,
                        modifier = Modifier.weight(1f),
                        testTag = "input_remaining_${row.denomination}"
                    )

                    if (isEnabled && showRemainingStepper) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Surface(
                            onClick = {
                                val currentRemaining = row.remaining
                                val newRemaining = currentRemaining + 1
                                onRemainingChange(row.denomination, newRemaining.toString())
                            },
                            enabled = isEnabled,
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.size(if (isCompact || isAddedFieldEnabled) 20.dp else 24.dp).testTag("btn_inc_${row.denomination}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Sold Count & Revenue
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(if (isAddedFieldEnabled) 70.dp else 80.dp)
                ) {
                    Text(
                        text = AccountingFormatter.formatYer(row.total),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (row.total > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "مباع: ${row.sold}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (row.sold > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Warning if remaining > (given + added)
            if (row.isRemainingExceeded) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "المتبقي (${row.remaining}) أكبر من المعطى والإضافي (${row.given + row.added})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Clickable Cell Button for high-precision entry without soft keyboard collisions
 */
@Composable
fun SalesCellButton(
    value: String,
    placeholder: String = "0",
    enabled: Boolean = true,
    isError: Boolean = false,
    isFocused: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val isCompact = com.example.ui.LocalCompactMode.current

    Surface(
        shape = RoundedCornerShape(if (isCompact) 6.dp else 8.dp),
        color = when {
            isFocused -> MaterialTheme.colorScheme.surface
            enabled -> MaterialTheme.colorScheme.surface
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        border = BorderStroke(
            if (isFocused) 1.5.dp else 1.dp,
            when {
                isError -> MaterialTheme.colorScheme.error
                isFocused -> MaterialTheme.colorScheme.primary
                value.isNotBlank() && value != "0" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        modifier = modifier
            .height(if (isCompact) 28.dp else 34.dp)
            .testTag(testTag)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (value.isBlank()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        fontSize = if (value.length >= 4) 11.sp else if (value.length >= 3) 12.sp else 13.sp,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
