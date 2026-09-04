package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.model.CASH_GROUP_MAIN_ID
import com.example.ui.model.SalesGroupType
import com.example.ui.theme.vibrant3d
import com.example.ui.viewmodel.TicketAccountingViewModel

private data class ReviewFieldTarget(
    val key: String,
    val title: String,
    val subtitle: String = "",
    val groupIndex: Int,
    val rowIndex: Int,
    val colIndex: Int,
    val fieldType: String,
    val currentValue: String,
    val originalValue: String,
    val allowPlusMinus: Boolean = false,
    val onConfirmSingle: (String) -> Unit
)

private fun findNextRowIndex(targets: List<ReviewFieldTarget>, currentIdx: Int): Int? {
    if (currentIdx !in targets.indices) return null
    val curr = targets[currentIdx]
    val sameGroupNextRow = targets.indices.filter {
        targets[it].groupIndex == curr.groupIndex && targets[it].rowIndex == curr.rowIndex + 1
    }
    if (sameGroupNextRow.isNotEmpty()) {
        return sameGroupNextRow.find { targets[it].colIndex == curr.colIndex } ?: sameGroupNextRow.first()
    }
    val nextGroups = targets.indices.filter { targets[it].groupIndex > curr.groupIndex }
    if (nextGroups.isNotEmpty()) {
        val minNextGroupIndex = nextGroups.minOf { targets[it].groupIndex }
        val firstRowInNextGroup = targets.indices.filter { targets[it].groupIndex == minNextGroupIndex && targets[it].rowIndex == 0 }
        if (firstRowInNextGroup.isNotEmpty()) {
            return firstRowInNextGroup.find { targets[it].colIndex == curr.colIndex } ?: firstRowInNextGroup.first()
        }
        return nextGroups.first()
    }
    return null
}

private fun findPrevRowIndex(targets: List<ReviewFieldTarget>, currentIdx: Int): Int? {
    if (currentIdx !in targets.indices) return null
    val curr = targets[currentIdx]
    val sameGroupPrevRow = targets.indices.filter {
        targets[it].groupIndex == curr.groupIndex && targets[it].rowIndex == curr.rowIndex - 1
    }
    if (sameGroupPrevRow.isNotEmpty()) {
        return sameGroupPrevRow.find { targets[it].colIndex == curr.colIndex } ?: sameGroupPrevRow.first()
    }
    val prevGroups = targets.indices.filter { targets[it].groupIndex < curr.groupIndex }
    if (prevGroups.isNotEmpty()) {
        val maxPrevGroupIndex = prevGroups.maxOf { targets[it].groupIndex }
        val maxRowInPrevGroup = targets.indices.filter { targets[it].groupIndex == maxPrevGroupIndex }
        val maxRowIdx = maxRowInPrevGroup.maxOfOrNull { targets[it].rowIndex } ?: 0
        val lastRowFields = targets.indices.filter { targets[it].groupIndex == maxPrevGroupIndex && targets[it].rowIndex == maxRowIdx }
        if (lastRowFields.isNotEmpty()) {
            return lastRowFields.find { targets[it].colIndex == curr.colIndex } ?: lastRowFields.first()
        }
        return prevGroups.last()
    }
    return null
}

@Composable
fun ReviewModeScreen(
    viewModel: TicketAccountingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val numpad = com.example.ui.components.LocalNumpadController.current

    // State for managing edited values in memory before global confirmation
    var editedValues by remember { mutableStateOf(mapOf<String, String>()) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDisabledGroups by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var activeFieldKey by remember { mutableStateOf<String?>(null) }

    val tabs = listOf("جدول المبيعات", "الصندوق")

    // Construct ordered list of review field targets for keypad navigation
    val groupsToReview = if (showDisabledGroups) uiState.groups else uiState.groups.filter { it.isEnabled }
    val cashGroupsToReview = if (showDisabledGroups) uiState.cashGroups.filter { it.id != CASH_GROUP_MAIN_ID } else uiState.cashGroups.filter { it.id != CASH_GROUP_MAIN_ID && it.isEnabled }

    val fieldTargets = remember(selectedTabIndex, showDisabledGroups, uiState, editedValues) {
        val list = mutableListOf<ReviewFieldTarget>()
        if (selectedTabIndex == 0) {
            groupsToReview.forEachIndexed { gIdx, group ->
                if (group.type == SalesGroupType.DENOMINATIONS || group.type == SalesGroupType.CUSTOM_FIELDS) {
                    group.rows.forEachIndexed { rIdx, row ->
                        val givenKey = "${group.id}:${row.denomination}:given"
                        list.add(ReviewFieldTarget(
                            key = givenKey,
                            title = "المعطى (فئة ${row.denomination})",
                            subtitle = group.name,
                            groupIndex = gIdx,
                            rowIndex = rIdx,
                            colIndex = 0,
                            fieldType = "given",
                            currentValue = editedValues[givenKey] ?: row.givenInput,
                            originalValue = row.givenInput,
                            onConfirmSingle = { viewModel.updateGiven(group.id, row.denomination, it) }
                        ))
                        val addedKey = "${group.id}:${row.denomination}:added"
                        list.add(ReviewFieldTarget(
                            key = addedKey,
                            title = "الإضافي (فئة ${row.denomination})",
                            subtitle = group.name,
                            groupIndex = gIdx,
                            rowIndex = rIdx,
                            colIndex = 1,
                            fieldType = "added",
                            currentValue = editedValues[addedKey] ?: row.addedInput,
                            originalValue = row.addedInput,
                            onConfirmSingle = { viewModel.updateAdded(group.id, row.denomination, it) }
                        ))
                        val remainingKey = "${group.id}:${row.denomination}:remaining"
                        list.add(ReviewFieldTarget(
                            key = remainingKey,
                            title = "المتبقي (فئة ${row.denomination})",
                            subtitle = group.name,
                            groupIndex = gIdx,
                            rowIndex = rIdx,
                            colIndex = 2,
                            fieldType = "remaining",
                            currentValue = editedValues[remainingKey] ?: row.remainingInput,
                            originalValue = row.remainingInput,
                            onConfirmSingle = { viewModel.updateRemaining(group.id, row.denomination, it) }
                        ))
                    }
                } else if (group.type == SalesGroupType.DIRECT_ENTRY) {
                    group.directEntries.forEachIndexed { rIdx, entry ->
                        val entryKey = "${group.id}:${entry.id}:direct"
                        list.add(ReviewFieldTarget(
                            key = entryKey,
                            title = "المبلغ (${entry.title})",
                            subtitle = group.name,
                            groupIndex = gIdx,
                            rowIndex = rIdx,
                            colIndex = 0,
                            fieldType = "direct",
                            currentValue = editedValues[entryKey] ?: entry.amountInput,
                            originalValue = entry.amountInput,
                            onConfirmSingle = { viewModel.updateDirectEntry(group.id, entry.id, entry.title, it, entry.notes) }
                        ))
                    }
                }
            }
        } else {
            // CASH & EXPENSES
            val yerKey = "cash:yer"
            list.add(ReviewFieldTarget(
                key = yerKey,
                title = "النقد الرئيسي (YER)",
                subtitle = "الصندوق",
                groupIndex = 0,
                rowIndex = 0,
                colIndex = 0,
                fieldType = "cash_yer",
                currentValue = editedValues[yerKey] ?: uiState.cashInBoxYerInput,
                originalValue = uiState.cashInBoxYerInput,
                onConfirmSingle = { viewModel.updateCashInBoxYer(it) }
            ))
            
            cashGroupsToReview.forEachIndexed { cgIdx, cashGroup ->
                if (cashGroup.type == com.example.ui.model.CashGroupType.DENOMINATIONS) {
                    val activeRows = if (cashGroup.isEnabled) cashGroup.activeDenomRows else cashGroup.denomRows
                    activeRows.forEachIndexed { rIdx, row ->
                        val denomKey = "cash_denom:${cashGroup.id}:${row.denomination}"
                        list.add(ReviewFieldTarget(
                            key = denomKey,
                            title = "العدد (${row.denomination} ${row.currency})",
                            subtitle = cashGroup.name,
                            groupIndex = cgIdx + 1,
                            rowIndex = rIdx,
                            colIndex = 0,
                            fieldType = "denom_count",
                            currentValue = editedValues[denomKey] ?: row.countInput,
                            originalValue = row.countInput,
                            onConfirmSingle = { viewModel.updateCashDenomCount(cashGroup.id, row.denomination, it) }
                        ))
                    }
                } else {
                    cashGroup.directEntries.forEachIndexed { rIdx, entry ->
                        val entryKey = "cash_direct:${cashGroup.id}:${entry.id}"
                        list.add(ReviewFieldTarget(
                            key = entryKey,
                            title = "المبلغ (${entry.title.ifBlank { "بند فرعي" }})",
                            subtitle = cashGroup.name,
                            groupIndex = cgIdx + 1,
                            rowIndex = rIdx,
                            colIndex = 0,
                            fieldType = "direct_entry",
                            currentValue = editedValues[entryKey] ?: entry.amountInput,
                            originalValue = entry.amountInput,
                            onConfirmSingle = { viewModel.updateCashDirectEntryAmount(cashGroup.id, entry.id, it) }
                        ))
                    }
                }
            }
        }
        list
    }

    fun openNumpadForIndex(idx: Int) {
        if (idx !in fieldTargets.indices) return
        val item = fieldTargets[idx]
        activeFieldKey = item.key
        val curVal = editedValues[item.key] ?: item.currentValue
        numpad.show(
            initialValue = curVal,
            targetTitle = item.title,
            targetSubtitle = item.subtitle,
            allowPlusMinus = item.allowPlusMinus,
            onValueChanged = { newValue ->
                editedValues = editedValues + (item.key to newValue)
            },
            onNextCell = if (idx + 1 in fieldTargets.indices) { { openNumpadForIndex(idx + 1) } } else null,
            onPrevCell = if (idx - 1 in fieldTargets.indices) { { openNumpadForIndex(idx - 1) } } else null,
            onNextRow = findNextRowIndex(fieldTargets, idx)?.let { nextIdx -> { openNumpadForIndex(nextIdx) } },
            onPrevRow = findPrevRowIndex(fieldTargets, idx)?.let { prevIdx -> { openNumpadForIndex(prevIdx) } },
            onDone = { 
                activeFieldKey = null
                numpad.hide() 
            }
        )
    }

    fun openNumpadForKey(key: String) {
        val idx = fieldTargets.indexOfFirst { it.key == key }
        if (idx != -1) {
            openNumpadForIndex(idx)
        }
    }

    // Global Confirm & Approve ALL inputs logic
    val confirmAllEdits = {
        if (editedValues.isNotEmpty()) {
            val copy = editedValues.toMap()
            val changeLogs = mutableListOf<String>()

            copy.forEach { (key, value) ->
                val target = fieldTargets.find { it.key == key }
                if (target != null && target.originalValue != value) {
                    changeLogs.add("${target.subtitle} • ${target.title}: ${target.originalValue} ➔ $value")
                }

                when {
                    key == "cash:yer" -> viewModel.updateCashInBoxYer(value)
                    key == "cash:sar" -> viewModel.updateCashInBoxSar(value)
                    key == "cash:rate" -> viewModel.updateExchangeRate(value)
                    
                    key.startsWith("cash_denom:") -> {
                        val parts = key.split(":")
                        if (parts.size >= 3) {
                            val gId = parts[1]
                            val denom = parts[2].toIntOrNull() ?: 0
                            viewModel.updateCashDenomCount(gId, denom, value)
                        }
                    }
                    
                    key.startsWith("cash_direct:") -> {
                        val parts = key.split(":")
                        if (parts.size >= 3) {
                            val gId = parts[1]
                            val entryId = parts[2]
                            viewModel.updateCashDirectEntryAmount(gId, entryId, value)
                        }
                    }
                    
                    key.endsWith(":given") -> {
                        val parts = key.split(":")
                        if (parts.size >= 3) {
                            val gId = parts[0]
                            val denom = parts[1].toIntOrNull() ?: 0
                            viewModel.updateGiven(gId, denom, value)
                        }
                    }
                    
                    key.endsWith(":added") -> {
                        val parts = key.split(":")
                        if (parts.size >= 3) {
                            val gId = parts[0]
                            val denom = parts[1].toIntOrNull() ?: 0
                            viewModel.updateAdded(gId, denom, value)
                        }
                    }
                    
                    key.endsWith(":remaining") -> {
                        val parts = key.split(":")
                        if (parts.size >= 3) {
                            val gId = parts[0]
                            val denom = parts[1].toIntOrNull() ?: 0
                            viewModel.updateRemaining(gId, denom, value)
                        }
                    }
                    
                    key.endsWith(":direct") -> {
                        val parts = key.split(":")
                        if (parts.size >= 3) {
                            val gId = parts[0]
                            val entryId = parts[1]
                            val grp = uiState.groups.find { it.id == gId }
                            val entryTitle = grp?.directEntries?.find { it.id == entryId }?.title ?: "بند"
                            viewModel.updateDirectEntry(gId, entryId, entryTitle, value, "")
                        }
                    }
                }
            }
            val count = changeLogs.size
            editedValues = emptyMap()
            numpad.hide()
            if (count > 0) {
                feedbackMessage = "تم اعتماد وتأكيد المدخلات المعدلة التالية بنجاح ($count تعديلات):\n" + changeLogs.joinToString("\n• ", prefix = "• ")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Mode Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .vibrant3d(
                    shape = RoundedCornerShape(16.dp),
                    elevation = 4.dp,
                    isDark = isDark,
                    baseColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = "مراجعة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "وضع المراجعة والتدقيق الشامل",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "تنقل بين الخانات والصفوف بزر لوحة المفاتيح واعتمد كافة المدخلات بنقرة واحدة.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    )
                }
                IconButton(
                    onClick = { viewModel.toggleReviewMode() },
                    modifier = Modifier.testTag("btn_close_review_mode")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق وضع المراجعة",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Global Feedback Message Banner
        AnimatedVisibility(visible = feedbackMessage != null) {
            feedbackMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        IconButton(onClick = { feedbackMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Global Confirmation & Approval Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .vibrant3d(
                    shape = RoundedCornerShape(12.dp),
                    elevation = if (editedValues.isNotEmpty()) 4.dp else 1.dp,
                    isDark = isDark,
                    baseColor = if (editedValues.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
            shape = RoundedCornerShape(12.dp),
            color = if (editedValues.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (editedValues.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.FactCheck,
                        contentDescription = "اعتماد المدخلات",
                        tint = if (editedValues.isNotEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (editedValues.isNotEmpty()) "اعتماد وتأكيد كافة التعديلات" else "جميع المدخلات معتمدة",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (editedValues.isNotEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = if (editedValues.isNotEmpty()) "${editedValues.size} تعديل بانتظار الاعتماد النهائي" else "لا توجد تعديلات غير معتمدة حالياً",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (editedValues.isNotEmpty()) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.outline,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Button(
                    onClick = { confirmAllEdits() },
                    enabled = editedValues.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_global_confirm_all")
                ) {
                    Text("اعتماد الكل ✓", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        // Tab selection for sales vs cash/expenses
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                )
            }
        }

        // Show disabled toggle for both tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = showDisabledGroups,
                onCheckedChange = { showDisabledGroups = it },
                modifier = Modifier.testTag("chk_show_disabled_groups")
            )
            Text(
                text = "إظهار المعطلة",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // List contents
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTabIndex == 0) {
                // SALES REVIEW
                if (groupsToReview.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد أقسام مبيعات.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    items(groupsToReview) { group ->
                        SalesGroupReviewCard(
                            group = group,
                            editedValues = editedValues,
                            activeFieldKey = activeFieldKey,
                            onValueChange = { key, value ->
                                editedValues = editedValues + (key to value)
                            },
                            onConfirm = { key, fieldType, newValue ->
                                when (fieldType) {
                                    "given" -> viewModel.updateGiven(group.id, key.split(":")[1].toInt(), newValue)
                                    "added" -> viewModel.updateAdded(group.id, key.split(":")[1].toInt(), newValue)
                                    "remaining" -> viewModel.updateRemaining(group.id, key.split(":")[1].toInt(), newValue)
                                    "direct" -> viewModel.updateDirectEntry(group.id, key.split(":")[1], "بند", newValue, "")
                                }
                                editedValues = editedValues.filterKeys { it != key }
                            },
                            onClickField = { key -> openNumpadForKey(key) }
                        )
                    }
                }
            } else {
                // CASH & EXPENSES REVIEW
                item {
                    MainBoxReviewCard(
                        uiState = uiState,
                        editedValues = editedValues,
                        activeFieldKey = activeFieldKey,
                        onValueChange = { key, value ->
                            editedValues = editedValues + (key to value)
                        },
                        onConfirm = { key, fieldType, newValue ->
                            when (fieldType) {
                                "cash_yer" -> viewModel.updateCashInBoxYer(newValue)
                                "cash_sar" -> viewModel.updateCashInBoxSar(newValue)
                                "rate" -> viewModel.updateExchangeRate(newValue)
                            }
                            editedValues = editedValues.filterKeys { it != key }
                        },
                        onClickField = { key -> openNumpadForKey(key) }
                    )
                }

                // Cash Groups Section
                if (cashGroupsToReview.isNotEmpty()) {
                    items(cashGroupsToReview) { cashGroup ->
                        CashGroupReviewCard(
                            group = cashGroup,
                            editedValues = editedValues,
                            activeFieldKey = activeFieldKey,
                            onValueChange = { key, value ->
                                editedValues = editedValues + (key to value)
                            },
                            onConfirm = { key, fieldType, newValue ->
                                when (fieldType) {
                                    "denom_count" -> {
                                        val parts = key.split(":")
                                        val denom = parts[2].toIntOrNull() ?: 0
                                        viewModel.updateCashDenomCount(cashGroup.id, denom, newValue)
                                    }
                                    "direct_entry" -> {
                                        val parts = key.split(":")
                                        val entryId = parts[2]
                                        viewModel.updateCashDirectEntryAmount(cashGroup.id, entryId, newValue)
                                    }
                                }
                                editedValues = editedValues.filterKeys { it != key }
                            },
                            onClickField = { key -> openNumpadForKey(key) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SalesGroupReviewCard(
    group: com.example.ui.model.SalesGroupUiState,
    editedValues: Map<String, String>,
    activeFieldKey: String? = null,
    onValueChange: (String, String) -> Unit,
    onConfirm: (String, String, String) -> Unit,
    onClickField: ((String) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .vibrant3d(
                shape = RoundedCornerShape(12.dp),
                elevation = 2.dp,
                isDark = isDark,
                baseColor = MaterialTheme.colorScheme.surface
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Group Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = if (group.type == SalesGroupType.DENOMINATIONS) "فئات نقدية" else "إدخال حر",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (group.type == SalesGroupType.DENOMINATIONS || group.type == SalesGroupType.CUSTOM_FIELDS) {
                val activeRows = group.rows
                
                // Table Headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الفئة", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(54.dp), textAlign = TextAlign.Center)
                    Text("المعطى", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("الإضافي", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("المتبقي", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("المباع", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Table Rows
                activeRows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Denom
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.width(54.dp)
                        ) {
                            Text(
                                text = "${row.denomination}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Given field
                        val givenKey = "${group.id}:${row.denomination}:given"
                        val givenVal = editedValues[givenKey] ?: row.givenInput
                        InlineReviewTextField(
                            value = givenVal,
                            originalValue = row.givenInput,
                            title = "المعطى (فئة ${row.denomination})",
                            subtitle = group.name,
                            isFocused = activeFieldKey == givenKey,
                            onValueChange = { onValueChange(givenKey, it) },
                            onConfirm = { onConfirm(givenKey, "given", it) },
                            onClickField = onClickField?.let { { it(givenKey) } },
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Added field
                        val addedKey = "${group.id}:${row.denomination}:added"
                        val addedVal = editedValues[addedKey] ?: row.addedInput
                        InlineReviewTextField(
                            value = addedVal,
                            originalValue = row.addedInput,
                            title = "الإضافي (فئة ${row.denomination})",
                            subtitle = group.name,
                            isFocused = activeFieldKey == addedKey,
                            onValueChange = { onValueChange(addedKey, it) },
                            onConfirm = { onConfirm(addedKey, "added", it) },
                            onClickField = onClickField?.let { { it(addedKey) } },
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Remaining field
                        val remainingKey = "${group.id}:${row.denomination}:remaining"
                        val remainingVal = editedValues[remainingKey] ?: row.remainingInput
                        val isRemainingFocused = activeFieldKey == remainingKey
                        
                        Row(
                            modifier = Modifier.weight(1.3f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    val currentRem = row.remaining
                                    val newRem = (currentRem - 1).coerceAtLeast(0)
                                    onValueChange(remainingKey, newRem.toString())
                                    onConfirm(remainingKey, "remaining", newRem.toString())
                                },
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("-", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                            Spacer(modifier = Modifier.width(2.dp))
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .background(
                                        color = when {
                                            isRemainingFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            remainingVal != row.remainingInput -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        width = if (isRemainingFocused) 2.dp else 1.dp,
                                        color = when {
                                            isRemainingFocused -> MaterialTheme.colorScheme.primary
                                            remainingVal != row.remainingInput -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onClickField?.invoke(remainingKey) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (remainingVal.isBlank()) "0" else remainingVal,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (remainingVal != row.remainingInput || isRemainingFocused) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (remainingVal != row.remainingInput || isRemainingFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(2.dp))
                            Surface(
                                onClick = {
                                    val currentRem = row.remaining
                                    val newRem = currentRem + 1
                                    onValueChange(remainingKey, newRem.toString())
                                    onConfirm(remainingKey, "remaining", newRem.toString())
                                },
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("+", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Sold indicator
                        Text(
                            text = "${row.sold}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (row.sold > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (group.type == SalesGroupType.DIRECT_ENTRY) {
                // Direct Entries
                if (group.directEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد بنود حرّة مسجلة.", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    group.directEntries.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1.2f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            val entryKey = "${group.id}:${entry.id}:direct"
                            val entryVal = editedValues[entryKey] ?: entry.amountInput
                            InlineReviewTextField(
                                value = entryVal,
                                originalValue = entry.amountInput,
                                title = "المبلغ (${entry.title})",
                                subtitle = group.name,
                                isFocused = activeFieldKey == entryKey,
                                onValueChange = { onValueChange(entryKey, it) },
                                onConfirm = { onConfirm(entryKey, "direct", it) },
                                onClickField = onClickField?.let { { it(entryKey) } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainBoxReviewCard(
    uiState: com.example.ui.viewmodel.DailyDirectSalesUiState,
    editedValues: Map<String, String>,
    activeFieldKey: String? = null,
    onValueChange: (String, String) -> Unit,
    onConfirm: (String, String, String) -> Unit,
    onClickField: ((String) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .vibrant3d(
                shape = RoundedCornerShape(12.dp),
                elevation = 2.dp,
                isDark = isDark,
                baseColor = MaterialTheme.colorScheme.surface
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "النقد",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Cash YER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("النقد الرئيسي:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1.5f))
                val yerKey = "cash:yer"
                val yerVal = editedValues[yerKey] ?: uiState.cashInBoxYerInput
                InlineReviewTextField(
                    value = yerVal,
                    originalValue = uiState.cashInBoxYerInput,
                    title = "النقد الرئيسي (YER)",
                    subtitle = "الصندوق",
                    isFocused = activeFieldKey == yerKey,
                    onValueChange = { onValueChange(yerKey, it) },
                    onConfirm = { onConfirm(yerKey, "cash_yer", it) },
                    onClickField = onClickField?.let { { it(yerKey) } },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InlineReviewTextField(
    value: String,
    originalValue: String,
    modifier: Modifier = Modifier,
    isNumeric: Boolean = true,
    isFocused: Boolean = false,
    title: String = "تعديل القيمة",
    subtitle: String = "",
    allowPlusMinus: Boolean = false,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onClickField: (() -> Unit)? = null
) {
    val isChanged = value.trim() != originalValue.trim()
    val numpad = com.example.ui.components.LocalNumpadController.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isFocused) {
        if (isFocused) {
            kotlinx.coroutines.delay(100)
            try { bringIntoViewRequester.bringIntoView() } catch (_: Exception) {}
        }
    }

    Row(
        modifier = modifier.bringIntoViewRequester(bringIntoViewRequester),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isNumeric) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when {
                    isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    isChanged -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                border = BorderStroke(
                    if (isFocused) 2.dp else 1.dp,
                    when {
                        isFocused -> MaterialTheme.colorScheme.primary
                        isChanged -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clickable {
                        coroutineScope.launch {
                            try { bringIntoViewRequester.bringIntoView() } catch (_: Exception) {}
                        }
                        if (onClickField != null) {
                            onClickField()
                        } else {
                            numpad.show(
                                initialValue = value,
                                targetTitle = title,
                                targetSubtitle = subtitle,
                                allowPlusMinus = allowPlusMinus,
                                onValueChanged = onValueChange,
                                onDone = { numpad.hide() }
                            )
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (value.isBlank()) "0" else value,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isChanged || isFocused) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isChanged || isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1
                    )
                }
            }
        } else {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isChanged || isFocused) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (isChanged || isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = when {
                            isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            isChanged -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = when {
                            isFocused -> MaterialTheme.colorScheme.primary
                            isChanged -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            )
        }

        if (isChanged) {
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                onClick = { onConfirm(value) },
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "تأكيد التعديل",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CashGroupReviewCard(
    group: com.example.ui.model.CashBoxGroupUiState,
    editedValues: Map<String, String>,
    activeFieldKey: String? = null,
    onValueChange: (String, String) -> Unit,
    onConfirm: (String, String, String) -> Unit,
    onClickField: ((String) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .vibrant3d(
                shape = RoundedCornerShape(12.dp),
                elevation = 2.dp,
                isDark = isDark,
                baseColor = MaterialTheme.colorScheme.surface
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                if (!group.isEnabled) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "معطل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (group.type == com.example.ui.model.CashGroupType.DENOMINATIONS) {
                val activeRows = if (group.isEnabled) group.activeDenomRows else group.denomRows
                activeRows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.width(70.dp)
                        ) {
                            Text(
                                text = "${row.denomination} ${row.currency}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val denomKey = "cash_denom:${group.id}:${row.denomination}"
                        val denomVal = editedValues[denomKey] ?: row.countInput
                        InlineReviewTextField(
                            value = denomVal,
                            originalValue = row.countInput,
                            title = "العدد (${row.denomination} ${row.currency})",
                            subtitle = group.name,
                            isFocused = activeFieldKey == denomKey,
                            onValueChange = { onValueChange(denomKey, it) },
                            onConfirm = { onConfirm(denomKey, "denom_count", it) },
                            onClickField = onClickField?.let { { it(denomKey) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                if (group.directEntries.isEmpty()) {
                    Text("لا توجد بنود مسجلة.", style = MaterialTheme.typography.bodySmall)
                } else {
                    group.directEntries.forEach { entry ->
                        val curr = entry.currency
                        val isForeign = entry.isForeign

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            // Line 1: Title on right, Currency badge on left
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.title.ifBlank { "بند فرعي" },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isForeign) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${curr.flag} ${curr.symbol}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isForeign) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Line 2: InlineReviewTextField for full-width amount input
                            val entryKey = "cash_direct:${group.id}:${entry.id}"
                            val entryVal = editedValues[entryKey] ?: entry.amountInput
                            InlineReviewTextField(
                                value = entryVal,
                                originalValue = entry.amountInput,
                                title = "المبلغ (${entry.title.ifBlank { "بند فرعي" }})",
                                subtitle = group.name,
                                isFocused = activeFieldKey == entryKey,
                                onValueChange = { onValueChange(entryKey, it) },
                                onConfirm = { onConfirm(entryKey, "direct_entry", it) },
                                onClickField = onClickField?.let { { it(entryKey) } },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
