import re

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I need to fix the brackets around `item {` and `SingleChoiceSegmentedButtonRow`.
# The current code looks like:
"""
                    Text("الصندوق (${uiState.cashGroups.count { it.isEnabled }})")
                }
        }
        item {
            Card(
"""
# Wait, let's fix it by adding the missing brackets. The segmented button ends, then the row ends, then the item ends.
# In my replacement, I missed one bracket for `item` block.
# Let's see how it was before:
"""
                SegmentedButton(...) {
                    Text(...)
                }
            } // ends SingleChoiceSegmentedButtonRow
        } // ends item
        if (selectedSectionTab == 0) {
"""
# I replaced `} } if` with `} item { ... } if`. So the first `item` was NOT closed! It was missing the third `}`!
content = content.replace(
"""                    Text("الصندوق (${uiState.cashGroups.count { it.isEnabled }})")
                }
        }
        item {""",
"""                    Text("الصندوق (${uiState.cashGroups.count { it.isEnabled }})")
                }
            }
        }
        item {""")

# Also I need to fix `draggedItemIndex` placement.
# `    var presetNameInput by remember { mutableStateOf("") }` was there.
content = content.replace(
"""    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
""", "")
content = content.replace(
"""    var presetNameInput by remember { mutableStateOf("") }""",
"""    var presetNameInput by remember { mutableStateOf("") }
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
""")

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
