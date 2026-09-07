package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.model.SalesGroupUiState
import com.example.ui.model.SalesGroupType
import com.example.ui.model.AccountingFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalesCategoryCalculatorDialog(
    groups: List<SalesGroupUiState>,
    initialGroupId: String,
    onDismiss: () -> Unit,
    onCommitSales: ((Map<String, String>) -> Unit)? = null
) {
    var selectedGroupId by remember { mutableStateOf(initialGroupId) }
    val currentGroup = groups.find { it.id == selectedGroupId } ?: groups.firstOrNull()

    // Flat Map of "groupId_denomination" -> entered quantity (as String)
    // Or "${groupId}_amount" -> entered direct amount for direct entry groups
    val quantitiesMap = remember { mutableStateMapOf<String, String>() }

    // Active focused denomination for the numpad in the selected group
    var selectedDenom by remember(selectedGroupId) {
        val firstDenom = currentGroup?.rows?.firstOrNull()?.denomination ?: 500
        mutableStateOf(firstDenom)
    }

    var showConfirmCommitDialog by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(warningMessage) {
        if (warningMessage != null) {
            delay(3000)
            warningMessage = null
        }
    }

    val isDirectGroup = currentGroup?.type == SalesGroupType.DIRECT_ENTRY
    val currentGroupRows = currentGroup?.rows ?: emptyList()

    // Selected Group Totals
    val currentGroupTickets = if (isDirectGroup) 0 else currentGroupRows.sumOf { row ->
        val qtyStr = quantitiesMap["${selectedGroupId}_${row.denomination}"] ?: ""
        qtyStr.toIntOrNull() ?: 0
    }
    val currentGroupRevenue = if (isDirectGroup) {
        val amountStr = quantitiesMap["${selectedGroupId}_amount"] ?: ""
        amountStr.toDoubleOrNull() ?: 0.0
    } else {
        currentGroupRows.sumOf { row ->
            val qtyStr = quantitiesMap["${selectedGroupId}_${row.denomination}"] ?: ""
            val qty = qtyStr.toIntOrNull() ?: 0
            (qty * row.denomination).toDouble()
        }
    }

    // ALL Groups Grand Totals (Summation across groups)
    val grandTotalTickets = groups.sumOf { grp ->
        if (grp.type == SalesGroupType.DIRECT_ENTRY) 0
        else grp.rows.sumOf { row ->
            val qtyStr = quantitiesMap["${grp.id}_${row.denomination}"] ?: ""
            qtyStr.toIntOrNull() ?: 0
        }
    }
    val grandTotalRevenue = groups.sumOf { grp ->
        if (grp.type == SalesGroupType.DIRECT_ENTRY) {
            val amountStr = quantitiesMap["${grp.id}_amount"] ?: ""
            amountStr.toDoubleOrNull() ?: 0.0
        } else {
            grp.rows.sumOf { row ->
                val qtyStr = quantitiesMap["${grp.id}_${row.denomination}"] ?: ""
                val qty = qtyStr.toIntOrNull() ?: 0
                (qty * row.denomination).toDouble()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .wrapContentHeight()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // 1. Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "حاسبة الفئات الجانبية 🧮",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            )
                            Text(
                                text = "احتساب جانبي لجميع المجموعات مع إمكانية اعتماد البيع",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.5.sp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (warningMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = warningMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 2. All Groups FlowRow (No horizontal scroll needed, shows all groups active & inactive)
                Text(
                    text = "المجموعات (منشطة وغير منشطة):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, fontSize = 9.5.sp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    groups.forEach { grp ->
                        val isSelected = grp.id == selectedGroupId
                        val indicator = if (grp.isEnabled) "🟢" else "⚪"
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGroupId = grp.id },
                            label = { 
                                Text(
                                    text = "$indicator ${grp.name}", 
                                    fontSize = 10.sp, 
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Dual Totals Display Card (Grand Total across all groups + Current Group)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "الإجمالي التراكمي لكافة المجموعات",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${AccountingFormatter.formatYer(grandTotalRevenue)} ريال ($grandTotalTickets تذكرة)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.5.sp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "مجموع: ${currentGroup?.name ?: ""}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontSize = 9.sp)
                            )
                            Text(
                                text = "${AccountingFormatter.formatYer(currentGroupRevenue)} ريال",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.5.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 4. Categories Display (FlowRow, no scrolling!)
                if (isDirectGroup) {
                    // Direct Entry group like Chinese (صيني)
                    val activeKey = "${selectedGroupId}_amount"
                    val enteredAmount = quantitiesMap[activeKey] ?: ""
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("إدخال مباشر لمجموعة ${currentGroup?.name}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("المبلغ المحتسب: ${if (enteredAmount.isBlank()) "0" else enteredAmount} ريال", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                            if (enteredAmount.isNotBlank()) {
                                IconButton(onClick = { quantitiesMap[activeKey] = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Backspace, contentDescription = "مسح", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                } else {
                    // Denomination Groups: سند, بطائق ألعاب, شرابات, انترنت
                    Text(
                        text = "فئات ${currentGroup?.name}:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline, fontSize = 9.5.sp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        currentGroupRows.forEach { row ->
                            val isSelected = row.denomination == selectedDenom
                            val qtyKey = "${selectedGroupId}_${row.denomination}"
                            val qty = quantitiesMap[qtyKey] ?: ""
                            val qtyInt = qty.toIntOrNull() ?: 0
                            val rowSubtotal = (qtyInt * row.denomination).toDouble()

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .clickable { selectedDenom = row.denomination }
                                    .testTag("chip_calc_cat_${row.denomination}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${row.denomination}:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (qty.isBlank()) "0" else qty,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.5.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    )
                                    if (rowSubtotal > 0) {
                                        Text(
                                            text = "(${AccountingFormatter.formatYer(rowSubtotal)})",
                                            fontSize = 9.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Selected Category Input Status Bar
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val activeQtyKey = "${selectedGroupId}_$selectedDenom"
                            Text(
                                text = "الفئة المحددة: $selectedDenom (الكمية: ${quantitiesMap[activeQtyKey] ?: "0"})",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            )

                            // Quick increment buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                listOf(1, 5, 10, 50).forEach { inc ->
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier
                                            .clickable {
                                                val activeKey = "${selectedGroupId}_$selectedDenom"
                                                val cur = quantitiesMap[activeKey]?.toIntOrNull() ?: 0
                                                val newVal = (cur + inc).toString()
                                                
                                                if (!isDirectGroup) {
                                                    val row = currentGroup?.rows?.find { it.denomination == selectedDenom }
                                                    if (row != null) {
                                                        if ((cur + inc) > row.remaining) {
                                                            warningMessage = "المخزون غير كافٍ! المتبقي لهذه الفئة هو ${row.remaining} فقط."
                                                            return@clickable
                                                        }
                                                    }
                                                }
                                                
                                                warningMessage = null
                                                quantitiesMap[activeKey] = newVal
                                            }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("+$inc", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 5. Compact Numpad Matrix with properly scaled buttons (non-oversized)
                val numpadRows = listOf(
                    listOf("7", "8", "9", "مسح"),
                    listOf("4", "5", "6", "تصفير"),
                    listOf("1", "2", "3", "00"),
                    listOf("0", "000", "التالي ⬇")
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(2.5.dp)
                ) {
                    numpadRows.forEach { rowKeys ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp), // Compact height solves oversized buttons
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                        ) {
                            rowKeys.forEach { key ->
                                val weight = if (key == "التالي ⬇") 1.8f else 1f
                                val isAction = key in listOf("مسح", "تصفير", "التالي ⬇")

                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = when (key) {
                                        "مسح" -> MaterialTheme.colorScheme.surfaceVariant
                                        "تصفير" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                                        "التالي ⬇" -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                                    },
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier
                                        .weight(weight)
                                        .fillMaxHeight()
                                        .clickable {
                                            val activeKey = if (isDirectGroup) "${selectedGroupId}_amount" else "${selectedGroupId}_$selectedDenom"
                                            val currentVal = quantitiesMap[activeKey] ?: ""
                                            
                                            when (key) {
                                                "مسح" -> {
                                                    if (currentVal.isNotEmpty()) {
                                                        quantitiesMap[activeKey] = currentVal.dropLast(1)
                                                    }
                                                }
                                                "تصفير" -> {
                                                    quantitiesMap[activeKey] = ""
                                                }
                                                "التالي ⬇" -> {
                                                    if (!isDirectGroup && currentGroupRows.isNotEmpty()) {
                                                        val denoms = currentGroupRows.map { it.denomination }
                                                        val currentIndex = denoms.indexOf(selectedDenom)
                                                        if (currentIndex != -1) {
                                                            val nextIndex = (currentIndex + 1) % denoms.size
                                                            selectedDenom = denoms[nextIndex]
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    val newVal = currentVal + key
                                                    if (!isDirectGroup) {
                                                        val row = currentGroup?.rows?.find { it.denomination == selectedDenom }
                                                        if (row != null) {
                                                            val enteredQty = newVal.toIntOrNull() ?: 0
                                                            if (enteredQty > row.remaining) {
                                                                warningMessage = "المخزون غير كافٍ! المتبقي لهذه الفئة هو ${row.remaining} فقط."
                                                                return@clickable
                                                            }
                                                        }
                                                    }
                                                    
                                                    warningMessage = null
                                                    if (newVal.length < 7) {
                                                        quantitiesMap[activeKey] = newVal
                                                    }
                                                }
                                            }
                                        }
                                        .testTag("btn_calc_key_$key")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (key == "مسح") {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "مسح",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        } else {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = if (isAction) 10.sp else 13.sp,
                                                    color = when (key) {
                                                        "تصفير" -> MaterialTheme.colorScheme.error
                                                        "التالي ⬇" -> MaterialTheme.colorScheme.onSecondaryContainer
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 6. Action Row: Reset, Reset All, Confirm Sale & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Current Group button
                    OutlinedButton(
                        onClick = {
                            val keysToRemove = quantitiesMap.keys.filter { it.startsWith("${selectedGroupId}_") }
                            keysToRemove.forEach { quantitiesMap.remove(it) }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 3.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text("تصفير المجموعة", fontSize = 9.5.sp)
                    }

                    // Reset All Groups button
                    OutlinedButton(
                        onClick = { quantitiesMap.clear() },
                        modifier = Modifier.weight(0.9f),
                        contentPadding = PaddingValues(vertical = 3.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Text("تصفير الكل", fontSize = 9.5.sp)
                    }

                    // Confirm Sale button (اعتماد البيع)
                    if (onCommitSales != null) {
                        Button(
                            onClick = {
                                if (grandTotalRevenue > 0) {
                                    showConfirmCommitDialog = true
                                }
                            },
                            enabled = grandTotalRevenue > 0,
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(vertical = 3.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("اعتماد البيع", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Close button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f),
                        contentPadding = PaddingValues(vertical = 3.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("إغلاق", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Confirmation Dialog before Committing Sales to Record
    if (showConfirmCommitDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmCommitDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("تأكيد اعتماد مبيعات الآلة الحاسبة", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "هل أنت متأكد من رغبتك في ترحيل هذه المبيعات المحسوبة إلى سجل مبيعات اليوم المباشرة؟",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "إجمالي المبلغ المرحل: ${AccountingFormatter.formatYer(grandTotalRevenue)} ريال",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                            if (grandTotalTickets > 0) {
                                Text(
                                    text = "إجمالي التذاكر: $grandTotalTickets تذكرة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmCommitDialog = false
                        onCommitSales?.invoke(quantitiesMap.toMap())
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("نعم، ترحيل واعتماد الآن", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCommitDialog = false }) {
                    Text("تراجع", fontSize = 12.sp)
                }
            }
        )
    }
}
