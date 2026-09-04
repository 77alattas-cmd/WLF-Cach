import re

with open('app/src/main/java/com/example/ui/viewmodel/TicketAccountingViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("    val selectedCashGroupId: String? = null, // CashBox tab selected group\n", "")

pattern_fun = re.compile(r'    fun selectCashGroup\(groupId: String\) \{\n        _uiState\.update \{ it\.copy\(selectedCashGroupId = groupId\) \}\n    \}\n')
# Remove the first occurrence of this function if there are two
content = content.replace(
"""    fun selectCashGroup(groupId: String) {
        _uiState.update { it.copy(selectedCashGroupId = groupId) }
    }
""", "", 1)

with open('app/src/main/java/com/example/ui/viewmodel/TicketAccountingViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
