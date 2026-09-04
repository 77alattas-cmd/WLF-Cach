import re

with open('app/src/main/java/com/example/ui/screens/CashBoxScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("viewModel.addCashDirectEntry(currentCashGroup.id, title, amount, currencyCode, isSar, customRate, notes)", "viewModel.addCashDirectEntry(currentCashGroup.id, title, amount, currencyCode, isSar, customExchangeRate = customRate, notes = notes)")
content = content.replace("viewModel.updateCashDirectEntry(currentCashGroup.id, itemId, title, amount, currencyCode, isSar, customRate, notes)", "viewModel.updateCashDirectEntry(currentCashGroup.id, itemId, title, amount, currencyCode, isSar, customExchangeRate = customRate, notes = notes)")

with open('app/src/main/java/com/example/ui/screens/CashBoxScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
