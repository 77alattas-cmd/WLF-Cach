import re

with open('app/src/main/java/com/example/ui/screens/CashBoxScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = re.compile(r'(// 3\. MAIN CASH CONTENT - Vertical Accordions\s*LazyColumn.*?\{)(.*?)(\s*if \(numpad\.isVisible\))', re.DOTALL)

replacement = r"""\1
            if (!uiState.isCashBoxAccordionMode) {
                // TABS MODE
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                            val inactiveTabColor = uiState.customColorThemeState.getColorOrNull(uiState.customColorThemeState.groupInactiveTabBg) ?: androidx.compose.ui.graphics.Color.Transparent
                            activeCashGroups.forEach { group ->
                                val isSelected = group.id == (uiState.selectedCashGroupId ?: activeCashGroups.firstOrNull()?.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectCashGroup(group.id) },
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
                                    } else null
                                )
                            }
                        }
                    }
                }
                
                val currentCashGroup = activeCashGroups.find { it.id == (uiState.selectedCashGroupId ?: activeCashGroups.firstOrNull()?.id) }
                if (currentCashGroup != null) {
                    item(key = currentCashGroup.id) {
                        val rateVal = uiState.exchangeRateInput.toDoubleOrNull() ?: 380.0
                        val groupTotal = currentCashGroup.getTotalYer(rateVal)
                        
                        ExpandableCashGroupCard(
                            group = currentCashGroup,
                            groupTotal = groupTotal,
                            isExpanded = true,
                            onToggleExpand = { },
                            isReadOnly = uiState.isReadOnlyMode,
                            content = {
                                when (currentCashGroup.type) {
                                    CashGroupType.DENOMINATIONS -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            if (currentCashGroup.id == CASH_GROUP_MAIN_ID) {
                                                com.example.ui.components.AppNumberField(
                                                    value = uiState.cashInBoxYerInput,
                                                    onValueChange = { viewModel.updateCashInBoxYer(it.filter { ch -> ch.isDigit() || ch == '.' }) },
                                                    enabled = !uiState.isReadOnlyMode,
                                                    label = { Text("إجمالي النقد الفعلي بالصندوق (ر.ي.)") },
                                                    placeholder = { Text("0") },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth().testTag("input_cash_yer")
                                                )
                                            }
                                        }
                                    }
                                    CashGroupType.DIRECT_ENTRY, CashGroupType.EXPENSES, CashGroupType.DEPOSITS -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            CashDirectGroupTable(
                                                group = currentCashGroup,
                                                exchangeRate = rateVal,
                                                isReadOnly = uiState.isReadOnlyMode,
                                                onAddEntry = { title, amount, currencyCode, isSar, customRate, notes ->
                                                    viewModel.addCashDirectEntry(currentCashGroup.id, title, amount, currencyCode, isSar, customRate, notes)
                                                },
                                                onUpdateEntry = { itemId, title, amount, currencyCode, isSar, customRate, notes ->
                                                    viewModel.updateCashDirectEntry(currentCashGroup.id, itemId, title, amount, currencyCode, isSar, customRate, notes)
                                                },
                                                onRemoveEntry = { itemId -> viewModel.removeCashDirectEntry(currentCashGroup.id, itemId) },
                                                onToggleEnabled = { viewModel.toggleCashGroupEnabled(currentCashGroup.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                // ACCORDION MODE
\2
            }
\3"""

new_content = pattern.sub(replacement, content)

with open('app/src/main/java/com/example/ui/screens/CashBoxScreen.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

