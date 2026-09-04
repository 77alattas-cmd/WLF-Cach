import re

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove the broken drag handle from Sales Groups
pattern = re.compile(r'// Drag Handle and Arrows\s*Icon\(\s*Icons\.Default\.DragHandle,.*?\),\s*', re.DOTALL)
replacement = r"""// Move Up / Down
                                """

content = pattern.sub(replacement, content)

# Remove the bad imports
content = content.replace("import androidx.compose.ui.input.pointer.pointerInput\n", "")
content = content.replace("import androidx.compose.foundation.gestures.detectVerticalDragGestures\n", "")

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

