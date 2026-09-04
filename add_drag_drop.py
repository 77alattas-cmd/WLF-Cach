import re

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add drag state at the top of the LazyColumn
pattern_top = re.compile(r'(    var presetNameInput by remember \{ mutableStateOf\(""\) \}\n)')
replacement_top = r"""\1
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
"""
content = pattern_top.sub(replacement_top, content)

# Replace Sales Groups Arrow buttons with Drag Handle + Arrows
pattern_sales_arrows = re.compile(r'(\s*// Move Up / Down\s*IconButton[^}]+\s*\}\s*IconButton[^}]+\s*\})', re.DOTALL)
replacement_sales_arrows = r"""
                                // Drag Handle and Arrows
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = "سحب للإفلات",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(4.dp)
                                        .androidx.compose.ui.input.pointer.pointerInput(Unit) {
                                            androidx.compose.foundation.gestures.detectVerticalDragGestures(
                                                onDragStart = { draggedItemIndex = index },
                                                onDragEnd = { draggedItemIndex = null; dragOffset = 0f },
                                                onDragCancel = { draggedItemIndex = null; dragOffset = 0f }
                                            ) { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount
                                                val threshold = 80f
                                                if (dragOffset > threshold && index < uiState.groups.size - 1) {
                                                    viewModel.moveSalesGroup(index, index + 1)
                                                    dragOffset = 0f
                                                    draggedItemIndex = index + 1
                                                } else if (dragOffset < -threshold && index > 0) {
                                                    viewModel.moveSalesGroup(index, index - 1)
                                                    dragOffset = 0f
                                                    draggedItemIndex = index - 1
                                                }
                                            }
                                        }
                                )
"""
content = pattern_sales_arrows.sub(replacement_sales_arrows, content, count=1)

# Replace Cash Groups Arrow buttons with Drag Handle + Arrows
pattern_cash_arrows = re.compile(r'(\s*IconButton\(\s*onClick = \{ if \(index > 0\) viewModel\.moveCashGroup\(index, index - 1\) \}[^}]+\s*\}[^}]+\s*viewModel\.moveCashGroup\(index, index \+ 1\)[^}]+\s*\})', re.DOTALL)
replacement_cash_arrows = r"""
                                // Drag Handle
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = "سحب للإفلات",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(4.dp)
                                        .androidx.compose.ui.input.pointer.pointerInput(Unit) {
                                            androidx.compose.foundation.gestures.detectVerticalDragGestures(
                                                onDragStart = { draggedItemIndex = index },
                                                onDragEnd = { draggedItemIndex = null; dragOffset = 0f },
                                                onDragCancel = { draggedItemIndex = null; dragOffset = 0f }
                                            ) { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount
                                                val threshold = 80f
                                                if (dragOffset > threshold && index < uiState.cashGroups.size - 1) {
                                                    viewModel.moveCashGroup(index, index + 1)
                                                    dragOffset = 0f
                                                    draggedItemIndex = index + 1
                                                } else if (dragOffset < -threshold && index > 0) {
                                                    viewModel.moveCashGroup(index, index - 1)
                                                    dragOffset = 0f
                                                    draggedItemIndex = index - 1
                                                }
                                            }
                                        }
                                )
"""
content = pattern_cash_arrows.sub(replacement_cash_arrows, content, count=1)

with open('app/src/main/java/com/example/ui/screens/ManagementScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

