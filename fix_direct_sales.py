import re

with open('app/src/main/java/com/example/ui/screens/DirectSalesScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_block = """        // 3. GROUPS TAB BAR OR ACCORDIONS
        if (!uiState.isSalesAccordionMode) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val activeTabColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupActiveTabBg) ?: MaterialTheme.colorScheme.secondaryContainer
                    val inactiveTabColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupInactiveTabBg) ?: Color.Transparent
                    activeGroups.forEach { group ->
                        val isSelected = group.id == uiState.selectedGroupId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectGroup(group.id) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeTabColor,
                                containerColor = inactiveTabColor
                            ),
                            label = {
                                Text(
                                    text = group.name,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            modifier = Modifier.testTag("chip_sales_group_${group.id}")
                        )
                    }
                }
            }
        }

        // 4. MAIN CONTENT LIST
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.isSalesAccordionMode) {
                if (activeGroups.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد مبيعات نشطة حالياً. يمكنك تفعيل المبيعات من تبويب تنظيم.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline)
                            )
                        }
                    }
                } else {
                    activeGroups.forEach { group ->
                        item(key = group.id) {
                            ExpandableSalesGroupCard(
                                group = group,
                                isExpanded = group.isExpanded,
                                onToggleExpand = { viewModel.toggleSalesGroupExpansion(group.id) },
                                content = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = group.notes,
                                            onValueChange = { viewModel.updateGroupNotes(group.id, it) },
                                            label = { Text("ملاحظات ${group.name}") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        )
                                        when (group.type) {
                                            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> {
                                                DirectSalesTable(
                                                    rows = group.activeRows.ifEmpty { group.rows },
                                                    groupName = group.name,
                                                    isEnabled = group.isEnabled && !uiState.isReadOnlyMode,
                                                    isLockGivenExtraMode = uiState.isLockGivenExtraMode,
                                                    isAddedFieldEnabled = group.isAddedFieldEnabled,
                                                    customColorState = uiState.customColorThemeState,
                                                    onGivenChange = { denom, givenStr -> viewModel.updateGiven(group.id, denom, givenStr) },
                                                    onAddedChange = { denom, addedStr -> viewModel.updateAdded(group.id, denom, addedStr) },
                                                    onRemainingChange = { denom, remStr -> viewModel.updateRemaining(group.id, denom, remStr) },
                                                    onNotesChange = { denom, notesStr -> viewModel.updateRowNotes(group.id, denom, notesStr) },
                                                    onZeroOutRemaining = { denom -> viewModel.zeroOutRemaining(group.id, denom) },
                                                    onZeroOutAllRemaining = { viewModel.zeroOutAllRemaining(group.id) },
                                                    onRemoveCategory = { denom -> viewModel.deleteCategoryRow(group.id, denom) },
                                                    onToggleRowEnabled = { denom -> viewModel.toggleRowEnabled(group.id, denom) }
                                                )
                                            }
                                            SalesGroupType.DIRECT_ENTRY -> {
                                                DirectEntryGroupTable(
                                                    group = group,
                                                    customColorState = uiState.customColorThemeState,
                                                    isReadOnlyMode = uiState.isReadOnlyMode,
                                                    onAddEntry = { title, amount, qty, notes -> viewModel.addDirectEntry(group.id, title, amount, notes) },
                                                    onAddDeposit = { title, amount, notes -> viewModel.addChiniDepositEntry(group.id, title, amount, notes) },
                                                    onUpdateEntry = { id, title, amount, qty, notes -> viewModel.updateDirectEntry(group.id, id, title, amount, notes) },
                                                    onRemoveEntry = { id -> viewModel.removeDirectEntry(group.id, id) },
                                                    onQuickAddAmount = { amt -> viewModel.addDirectEntry(group.id, "إضافة سريعة", amt.toString()) },
                                                    onToggleEnabled = { viewModel.toggleSalesGroupEnabled(group.id) }
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                if (currentSelectedGroup != null && currentSelectedGroup.isEnabled) {
                    // Group Notes placed at top
                    item {
                        OutlinedTextField(
                            value = currentSelectedGroup.notes,
                            onValueChange = { viewModel.updateGroupNotes(currentSelectedGroup.id, it) },
                            label = { Text("ملاحظات ${currentSelectedGroup.name}") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )
                    }
                    item {
                        when (currentSelectedGroup.type) {
                            SalesGroupType.DENOMINATIONS, SalesGroupType.CUSTOM_FIELDS -> {
                                DirectSalesTable(
                                    rows = currentSelectedGroup.activeRows.ifEmpty { currentSelectedGroup.rows },
                                    groupName = currentSelectedGroup.name,
                                    isEnabled = currentSelectedGroup.isEnabled && !uiState.isReadOnlyMode,
                                    isLockGivenExtraMode = uiState.isLockGivenExtraMode,
                                    isAddedFieldEnabled = currentSelectedGroup.isAddedFieldEnabled,
                                    customColorState = uiState.customColorThemeState,
                                    onGivenChange = { denom, givenStr -> viewModel.updateGiven(currentSelectedGroup.id, denom, givenStr) },
                                    onAddedChange = { denom, addedStr -> viewModel.updateAdded(currentSelectedGroup.id, denom, addedStr) },
                                    onRemainingChange = { denom, remStr -> viewModel.updateRemaining(currentSelectedGroup.id, denom, remStr) },
                                    onNotesChange = { denom, notesStr -> viewModel.updateRowNotes(currentSelectedGroup.id, denom, notesStr) },
                                    onZeroOutRemaining = { denom -> viewModel.zeroOutRemaining(currentSelectedGroup.id, denom) },
                                    onZeroOutAllRemaining = { viewModel.zeroOutAllRemaining(currentSelectedGroup.id) },
                                    onRemoveCategory = { denom -> viewModel.deleteCategoryRow(currentSelectedGroup.id, denom) },
                                    onToggleRowEnabled = { denom -> viewModel.toggleRowEnabled(currentSelectedGroup.id, denom) }
                                )
                            }
                            SalesGroupType.DIRECT_ENTRY -> {
                                DirectEntryGroupTable(
                                    group = currentSelectedGroup,
                                    customColorState = uiState.customColorThemeState,
                                    isReadOnlyMode = uiState.isReadOnlyMode,
                                    onAddEntry = { title, amount, qty, notes -> viewModel.addDirectEntry(currentSelectedGroup.id, title, amount, notes) },
                                    onAddDeposit = { title, amount, notes -> viewModel.addChiniDepositEntry(currentSelectedGroup.id, title, amount, notes) },
                                    onUpdateEntry = { id, title, amount, qty, notes -> viewModel.updateDirectEntry(currentSelectedGroup.id, id, title, amount, notes) },
                                    onRemoveEntry = { id -> viewModel.removeDirectEntry(currentSelectedGroup.id, id) },
                                    onQuickAddAmount = { amt -> viewModel.addDirectEntry(currentSelectedGroup.id, "إضافة سريعة", amt.toString()) },
                                    onToggleEnabled = { viewModel.toggleSalesGroupEnabled(currentSelectedGroup.id) }
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد مبيعات نشطة حالياً. يمكنك تفعيل المبيعات من تبويب تنظيم.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline)
                            )
                        }
                    }
                }
            }
\n"""

lines_to_keep_before = lines[:290] # 0 to 289
lines_to_keep_after = lines[404:] # 404 to end

with open('app/src/main/java/com/example/ui/screens/DirectSalesScreen.kt', 'w', encoding='utf-8') as f:
    f.writelines(lines_to_keep_before)
    f.write(new_block)
    f.writelines(lines_to_keep_after)

