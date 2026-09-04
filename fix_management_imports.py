import re

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports
content = content.replace("import androidx.compose.ui.unit.sp\n", "import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.input.pointer.pointerInput\nimport androidx.compose.foundation.gestures.detectVerticalDragGestures\n")

# Remove "androidx.compose.ui.input.pointer." and "androidx.compose.foundation.gestures."
content = content.replace(".androidx.compose.ui.input.pointer.pointerInput", ".pointerInput")
content = content.replace("androidx.compose.foundation.gestures.detectVerticalDragGestures", "detectVerticalDragGestures")

# Fix the broken cash groups row
# Find:
"""
                                // Drag Handle
                                Icon(
                                    Icons.Default.DragHandle,
...
                                        }
                                ),
                                    enabled = index < uiState.cashGroups.size - 1,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "أسفل", modifier = Modifier.size(16.dp))
                                }
"""

# Wait, using regex to fix this:
pattern = re.compile(r'\),\s*enabled = index < uiState\.cashGroups\.size - 1,\s*modifier = Modifier\.size\(30\.dp\)\s*\)\s*\{\s*Icon\(Icons\.Default\.ArrowDownward, contentDescription = "أسفل", modifier = Modifier\.size\(16\.dp\)\)\s*\}', re.DOTALL)
content = pattern.sub(r')', content)


with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
