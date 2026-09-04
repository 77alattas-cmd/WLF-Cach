#!/bin/bash
awk '
/^[ \t]*}$/ && in_else_expenses {
    print $0
    print "                }"
    print "                val cashGroupsToReview = if (showDisabledGroups) uiState.cashGroups else uiState.cashGroups.filter { it.isEnabled }"
    print "                if (cashGroupsToReview.isNotEmpty()) {"
    print "                    items(cashGroupsToReview) {"
    print "                        cashGroup ->"
    print "                        CashGroupReviewCard("
    print "                            group = cashGroup,"
    print "                            editedValues = editedValues,"
    print "                            onValueChange = onValueChange,"
    print "                            onConfirm = { key, fieldType, newValue ->"
    print "                                if (fieldType == \"direct\") {"
    print "                                    viewModel.updateDirectEntry(cashGroup.id, key.split(\":\")[1], \"\", newValue, \"\")"
    print "                                }"
    print "                                editedValues = editedValues.filterKeys { it != key }"
    print "                            }"
    print "                        )"
    print "                    }"
    print "                }"
    in_else_expenses = 0
    next
}
/items\(uiState.expenses\)/ {
    in_else_expenses = 1
}
{ print }
' app/src/main/java/com/example/ui/screens/ReviewModeScreen.kt > temp.kt && mv temp.kt app/src/main/java/com/example/ui/screens/ReviewModeScreen.kt
