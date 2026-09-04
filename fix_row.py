import re

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = re.compile(r'Row\(verticalAlignment = Alignment\.CenterVertically\) \{\s*// Drag Handle.*?Switch\(\s*checked = cashGroup\.isEnabled,\s*onCheckedChange = \{ viewModel\.toggleCashGroupEnabled\(cashGroup\.id\) \},\s*modifier = Modifier\.testTag\("switch_cash_group_\$\{cashGroup\.id\}"\)\s*\)\s*\}\s*\}', re.DOTALL)

replacement = """Row(verticalAlignment = Alignment.CenterVertically) {
                                // Move Up / Down
                                IconButton(
                                    onClick = { if (index > 0) viewModel.moveCashGroup(index, index - 1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "أعلى", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { if (index < uiState.cashGroups.size - 1) viewModel.moveCashGroup(index, index + 1) },
                                    enabled = index < uiState.cashGroups.size - 1,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "أسفل", modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                if (cashGroup.id != CASH_GROUP_MAIN_ID) {
                                    Switch(
                                        checked = cashGroup.isEnabled,
                                        onCheckedChange = { viewModel.toggleCashGroupEnabled(cashGroup.id) },
                                        modifier = Modifier.testTag("switch_cash_group_${cashGroup.id}")
                                    )
                                }
                            }"""

content = pattern.sub(replacement, content)

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
