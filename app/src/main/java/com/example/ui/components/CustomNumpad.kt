package com.example.ui.components

import com.example.ui.theme.vibrant3d
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

/**
 * Robust Numpad Controller providing multi-directional navigation:
 * - Between Cells (الخانة السابقة / الخانة التالية)
 * - Between Rows (الصف السابق ⬆ / الصف التالي ⬇)
 * - Active Target Context (اسم ، الفئة، نوع الحقل)
 */
fun cleanLeadingZeros(input: String): String {
    if (input.isEmpty()) return input
    val isNegative = input.startsWith("-")
    val absVal = if (isNegative) input.substring(1) else input
    if (absVal.startsWith("0.") || absVal == "0") return input
    if (absVal.startsWith("0") && absVal.length > 1) {
        val trimmed = absVal.replaceFirst("^0+".toRegex(), "")
        val result = if (trimmed.isEmpty()) "0" else if (trimmed.startsWith(".")) "0$trimmed" else trimmed
        return if (isNegative && result != "0") "-$result" else result
    }
    return input
}

data class NumpadRowPreview(
    val groupName: String = "",
    val denomination: Int = 0,
    val given: String = "",
    val added: String = "",
    val remaining: String = "",
    val sold: Int = 0,
    val total: Double = 0.0,
    val activeCell: String = "GIVEN", // "GIVEN", "ADDED", "REMAINING"
    val isAddedEnabled: Boolean = false,
    val isLocked: Boolean = false,
    val onSelectCell: ((String) -> Unit)? = null
)

class NumpadController {
    var isVisible by mutableStateOf(false)
    var value by mutableStateOf("")
    var targetTitle by mutableStateOf("")
    var targetSubtitle by mutableStateOf("")
    var initialValueOnShow by mutableStateOf("")
    var rowPreview by mutableStateOf<NumpadRowPreview?>(null)
    
    var onValueChanged by mutableStateOf<((String) -> Unit)?>(null)
    var onNextCell by mutableStateOf<(() -> Unit)?>(null)
    var onPrevCell by mutableStateOf<(() -> Unit)?>(null)
    var onNextRow by mutableStateOf<(() -> Unit)?>(null)
    var onPrevRow by mutableStateOf<(() -> Unit)?>(null)
    var onDone by mutableStateOf<(() -> Unit)?>(null)
    var shouldReplaceOnFirstDigit by mutableStateOf(false)
    var allowPlusMinus by mutableStateOf(false)

    fun show(
        initialValue: String,
        targetTitle: String = "",
        targetSubtitle: String = "",
        allowPlusMinus: Boolean = false,
        rowPreview: NumpadRowPreview? = null,
        onValueChanged: (String) -> Unit,
        onNextCell: (() -> Unit)? = null,
        onPrevCell: (() -> Unit)? = null,
        onNextRow: (() -> Unit)? = null,
        onPrevRow: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null
    ) {
        val cleanedInit = cleanLeadingZeros(initialValue)
        this.value = cleanedInit
        this.initialValueOnShow = cleanedInit
        this.shouldReplaceOnFirstDigit = cleanedInit.isNotEmpty() && cleanedInit != "0"
        this.targetTitle = targetTitle
        this.targetSubtitle = targetSubtitle
        this.allowPlusMinus = allowPlusMinus
        this.rowPreview = rowPreview
        this.onValueChanged = onValueChanged
        this.onNextCell = onNextCell
        this.onPrevCell = onPrevCell
        this.onNextRow = onNextRow
        this.onPrevRow = onPrevRow
        this.onDone = onDone
        this.isVisible = true
    }

    fun updateValue(newValue: String) {
        val cleaned = cleanLeadingZeros(newValue)
        this.value = cleaned
        this.shouldReplaceOnFirstDigit = cleaned.isNotEmpty() && cleaned != "0"
    }

    fun updateTargetInfo(title: String, subtitle: String = "", rowPreview: NumpadRowPreview? = null) {
        this.targetTitle = title
        this.targetSubtitle = subtitle
        if (rowPreview != null) {
            this.rowPreview = rowPreview
        }
    }

    fun hide() {
        this.isVisible = false
        this.shouldReplaceOnFirstDigit = false
        this.rowPreview = null
        this.onValueChanged = null
        this.onNextCell = null
        this.onPrevCell = null
        this.onNextRow = null
        this.onPrevRow = null
        this.onDone = null
    }
}

val LocalNumpadController = compositionLocalOf { NumpadController() }

/**
 * Global Custom Numpad Overlay with:
 * 1. Target Row & Field Indicator Banner
 * 2. Dedicated Row Navigation Buttons (⬆ الصف السابق / ⬇ الصف التالي)
 * 3. Dedicated Cell Navigation Buttons (➡ الخانة السابقة / ⬅ الخانة التالية)
 * 4. High-contrast Gray/Slate Theme
 */
@Composable
fun GlobalCustomNumpad() {
    val controller = LocalNumpadController.current
    if (!controller.isVisible) return

    val cancelAndClose = {
        controller.onValueChanged?.invoke(controller.initialValueOnShow)
        controller.hide()
    }

    Dialog(
        onDismissRequest = { cancelAndClose() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.20f))
                .clickable { cancelAndClose() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {} // Prevent click-through
                    .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 38.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_numpad_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. HEADER: Target Row / Field Banner + Close Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = controller.targetTitle.ifEmpty { "إدخال رقمي" },
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 13.sp
                                            ),
                                            maxLines = 1
                                        )
                                        if (controller.targetSubtitle.isNotEmpty()) {
                                            Text(
                                                text = controller.targetSubtitle,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.outline,
                                                    fontSize = 10.sp
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Current Value Preview
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    ) {
                                        Text(
                                            text = controller.value.ifEmpty { "0" },
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 16.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { cancelAndClose() },
                                        modifier = Modifier.size(32.dp).testTag("btn_numpad_close")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "إغلاق",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // 1.2 Denomination Row Cells Dashboard Preview
                        controller.rowPreview?.let { preview ->
                            val activeGiven = if (preview.activeCell == "GIVEN") controller.value.ifEmpty { "0" } else preview.given.ifEmpty { "0" }
                            val activeAdded = if (preview.activeCell == "ADDED") controller.value.ifEmpty { "0" } else preview.added.ifEmpty { "0" }
                            val activeRemaining = if (preview.activeCell == "REMAINING") controller.value.ifEmpty { "0" } else preview.remaining.ifEmpty { "0" }

                            val gVal = activeGiven.toIntOrNull() ?: 0
                            val aVal = activeAdded.toIntOrNull() ?: 0
                            val rVal = activeRemaining.toIntOrNull() ?: 0
                            val liveSold = (gVal + aVal - rVal).coerceAtLeast(0)
                            val liveTotal = liveSold * preview.denomination.toDouble()

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Denomination Card
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.weight(0.9f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("الفئة", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                                Text("${preview.denomination}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }

                                        // 2. Given Card
                                        val isGivenActive = preview.activeCell == "GIVEN"
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isGivenActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, if (isGivenActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(enabled = !preview.isLocked) { preview.onSelectCell?.invoke("GIVEN") }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    "المعطى",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = if (isGivenActive) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isGivenActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                                                )
                                                Text(
                                                    activeGiven,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isGivenActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        // 3. Added Card (Optional)
                                        if (preview.isAddedEnabled) {
                                            val isAddedActive = preview.activeCell == "ADDED"
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isAddedActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                border = BorderStroke(1.dp, if (isAddedActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable(enabled = !preview.isLocked) { preview.onSelectCell?.invoke("ADDED") }
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        "الإضافي",
                                                        fontSize = 9.5.sp,
                                                        fontWeight = if (isAddedActive) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isAddedActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                                                    )
                                                    Text(
                                                        activeAdded,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isAddedActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        // 4. Remaining Card
                                        val isRemActive = preview.activeCell == "REMAINING"
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isRemActive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, if (isRemActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { preview.onSelectCell?.invoke("REMAINING") }
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    "المتبقي",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = if (isRemActive) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isRemActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                                )
                                                Text(
                                                    activeRemaining,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isRemActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        // 5. Sold Summary Card
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                            modifier = Modifier.weight(0.9f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("المباع", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                                                Text("$liveSold", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }

                                        // 6. Total Summary Card
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            modifier = Modifier.weight(1.1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("الإجمالي", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.outline)
                                                Text(com.example.ui.model.AccountingFormatter.formatMoney(liveTotal), fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 1.5 Live Tafqeet Banner (عرض القيم كتابيا عند كل رقم وعند الإدخال)
                        val numericVal = controller.value.toDoubleOrNull() ?: 0.0
                        val tafqeetText = if (controller.value.isEmpty()) "" else {
                            val currencyCode = if (controller.targetTitle.contains("سعودي") || controller.targetSubtitle.contains("سعودي")) "SAR" 
                                               else if (controller.targetTitle.contains("يمني") || controller.targetSubtitle.contains("يمني")) "YER"
                                               else "NONE"
                            com.example.util.TafqeetHelper.convert(numericVal, currency = currencyCode, withPrefixAndSuffix = true)
                        }

                        if (tafqeetText.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = tafqeetText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // 2. DEDICATED NAVIGATION CONTROLS BAR (التنقل بين الصفوف والخانات)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Row Navigation: Prev Row ⬆
                            Button(
                                onClick = { controller.onPrevRow?.invoke() },
                                enabled = controller.onPrevRow != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_numpad_prev_row"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الصف السابق", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Row Navigation: Next Row ⬇
                            Button(
                                onClick = { controller.onNextRow?.invoke() },
                                enabled = controller.onNextRow != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_numpad_next_row"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("الصف التالي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }

                            // Cell Navigation: Prev Cell ➡
                            Button(
                                onClick = { controller.onPrevCell?.invoke() },
                                enabled = controller.onPrevCell != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_numpad_prev_cell"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الخانة السابقة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Cell Navigation: Next Cell ⬅
                            Button(
                                onClick = { controller.onNextCell?.invoke() },
                                enabled = controller.onNextCell != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("btn_numpad_next_cell"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("الخانة التالية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }

                        // 3. NUMPAD DIGIT & ACTION KEYS MATRIX (4 Rows)
                        val rows = if (controller.allowPlusMinus) listOf(
                            listOf("7", "8", "9", "⌫"),
                            listOf("4", "5", "6", "C"),
                            listOf("1", "2", "3", "+"),
                            listOf("0", "-", "✓")
                        ) else listOf(
                            listOf("7", "8", "9", "⌫"),
                            listOf("4", "5", "6", "C"),
                            listOf("1", "2", "3", "00"),
                            listOf("0", "000", "✓")
                        )

                        rows.forEach { keyRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                keyRow.forEach { key ->
                                    val isDone = key == "✓"
                                    val isBackspace = key == "⌫"
                                    val isClear = key == "C"
                                    val isAction = isDone || isBackspace || isClear
                                    val weight = if (isDone) 2f else 1f

                                    val isDark = isSystemInDarkTheme()
                                    val baseColor = when {
                                        isDone -> MaterialTheme.colorScheme.primary
                                        isClear -> MaterialTheme.colorScheme.errorContainer
                                        isBackspace -> MaterialTheme.colorScheme.surfaceVariant
                                        else -> MaterialTheme.colorScheme.surface
                                    }

                                    Button(
                                        onClick = {
                                            when (key) {
                                                "✓" -> {
                                                    controller.onDone?.invoke()
                                                    controller.hide()
                                                }
                                                "⌫" -> {
                                                    controller.shouldReplaceOnFirstDigit = false
                                                    if (controller.value.isNotEmpty()) {
                                                        controller.value = controller.value.dropLast(1)
                                                        controller.onValueChanged?.invoke(controller.value)
                                                    }
                                                }
                                                "C" -> {
                                                    controller.shouldReplaceOnFirstDigit = false
                                                    controller.value = ""
                                                    controller.onValueChanged?.invoke("")
                                                }
                                                "." -> {
                                                    if (controller.shouldReplaceOnFirstDigit) {
                                                        controller.value = "0."
                                                        controller.shouldReplaceOnFirstDigit = false
                                                        controller.onValueChanged?.invoke(controller.value)
                                                    } else if (!controller.value.contains(".")) {
                                                        controller.value = if (controller.value.isEmpty()) "0." else controller.value + "."
                                                        controller.onValueChanged?.invoke(controller.value)
                                                    }
                                                }
                                                "-" -> {
                                                    controller.shouldReplaceOnFirstDigit = false
                                                    if (controller.value.startsWith("-")) {
                                                        controller.value = controller.value.removePrefix("-")
                                                    } else {
                                                        controller.value = "-" + controller.value.removePrefix("+")
                                                    }
                                                    controller.onValueChanged?.invoke(controller.value)
                                                }
                                                "+" -> {
                                                    controller.shouldReplaceOnFirstDigit = false
                                                    if (controller.value.startsWith("-")) {
                                                        controller.value = controller.value.removePrefix("-")
                                                        controller.onValueChanged?.invoke(controller.value)
                                                    }
                                                }
                                                else -> {
                                                    val rawVal = if (controller.shouldReplaceOnFirstDigit) {
                                                        key
                                                    } else {
                                                        controller.value + key
                                                    }
                                                    controller.shouldReplaceOnFirstDigit = false
                                                    val cleaned = cleanLeadingZeros(rawVal)
                                                    controller.value = cleaned
                                                    controller.onValueChanged?.invoke(cleaned)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(weight)
                                            .height(52.dp)
                                            .padding(2.dp)
                                            .vibrant3d(
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = if (isDone) 8.dp else 4.dp,
                                                isDark = isDark,
                                                baseColor = baseColor
                                            )
                                            .testTag("btn_numpad_key_$key"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = when {
                                                isDone -> MaterialTheme.colorScheme.onPrimary
                                                isClear -> MaterialTheme.colorScheme.onErrorContainer
                                                isBackspace -> MaterialTheme.colorScheme.onSurfaceVariant
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        when (key) {
                                            "✓" -> {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = "تم", modifier = Modifier.size(20.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("تم", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            "⌫" -> Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "مسح رقم", modifier = Modifier.size(20.dp))
                                            "C" -> Text("مسح C", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                            else -> Text(text = key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

/**
 * Standard Numeric Input Field connected to the robust NumpadController
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    useCustomNumpadOnly: Boolean = true,
    targetTitle: String = "",
    targetSubtitle: String = "",
    onNextCell: (() -> Unit)? = null,
    onPrevCell: (() -> Unit)? = null,
    onNextRow: (() -> Unit)? = null,
    onPrevRow: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
    textStyle: TextStyle = LocalTextStyle.current,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    shape: androidx.compose.ui.graphics.Shape = OutlinedTextFieldDefaults.shape,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    val numpadController = LocalNumpadController.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    Box(
        modifier = modifier.bringIntoViewRequester(bringIntoViewRequester)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (!useCustomNumpadOnly) {
                    onValueChange(newValue)
                }
            },
            enabled = enabled,
            readOnly = if (useCustomNumpadOnly) true else readOnly,
            label = label,
            placeholder = placeholder,
            keyboardOptions = keyboardOptions,
            textStyle = textStyle,
            colors = colors,
            shape = shape,
            singleLine = singleLine,
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )

        if (enabled && !readOnly && useCustomNumpadOnly) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        keyboardController?.hide()
                        coroutineScope.launch {
                            try {
                                bringIntoViewRequester.bringIntoView()
                            } catch (_: Exception) {}
                        }
                        numpadController.show(
                            initialValue = value,
                            targetTitle = targetTitle,
                            targetSubtitle = targetSubtitle,
                            onValueChanged = onValueChange,
                            onNextCell = onNextCell,
                            onPrevCell = onPrevCell,
                            onNextRow = onNextRow,
                            onPrevRow = onPrevRow,
                            onDone = onDone ?: { numpadController.hide() }
                        )
                    }
            )
        }
    }
}
