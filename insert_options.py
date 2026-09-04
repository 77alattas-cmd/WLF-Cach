import re

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

pattern = re.compile(r'(\s*\}\s*\}\s*if \(selectedSectionTab == 0\) \{)', re.DOTALL)

replacement = r"""
        }
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("خيار العرض كـ (مطوية) بدلاً من تبويبات", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("تغيير طريقة العرض في الشاشة الرئيسية", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                    }
                    if (selectedSectionTab == 0) {
                        Switch(
                            checked = uiState.isSalesAccordionMode,
                            onCheckedChange = { viewModel.toggleSalesAccordionMode(it) }
                        )
                    } else {
                        Switch(
                            checked = uiState.isCashBoxAccordionMode,
                            onCheckedChange = { viewModel.toggleCashBoxAccordionMode(it) }
                        )
                    }
                }
            }
        }
        if (selectedSectionTab == 0) {"""

new_content = pattern.sub(replacement, content, count=1)

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

