package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * Drag Handle Component with responsive feedback for reordering items
 */
@Composable
fun ReorderDragHandle(
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String = "إعادة الترتيب بالسحب أو الأسهم"
) {
    Box(
        modifier = modifier
            .size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Modifier that enables drag-and-drop vertical swapping between adjacent items
 */
fun Modifier.reorderableVerticalItem(
    index: Int,
    itemCount: Int,
    isDragEnabled: Boolean = true,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit
): Modifier = this.then(
    if (!isDragEnabled) Modifier
    else Modifier.pointerInput(index, itemCount) {
        var accumulatedDrag = 0f
        val threshold = 70f // Drag distance in px to trigger swap

        detectVerticalDragGestures(
            onDragStart = {
                accumulatedDrag = 0f
            },
            onDragEnd = {
                accumulatedDrag = 0f
            },
            onDragCancel = {
                accumulatedDrag = 0f
            },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                accumulatedDrag += dragAmount
                if (accumulatedDrag > threshold && index < itemCount - 1) {
                    onMove(index, index + 1)
                    accumulatedDrag = 0f
                } else if (accumulatedDrag < -threshold && index > 0) {
                    onMove(index, index - 1)
                    accumulatedDrag = 0f
                }
            }
        )
    }
)

/**
 * Modifier that enables drag-and-drop horizontal swapping for tab bars and chips
 */
fun Modifier.reorderableHorizontalItem(
    index: Int,
    itemCount: Int,
    isDragEnabled: Boolean = true,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit
): Modifier = this.then(
    if (!isDragEnabled) Modifier
    else Modifier.pointerInput(index, itemCount) {
        var accumulatedDrag = 0f
        val threshold = 60f

        detectHorizontalDragGestures(
            onDragStart = {
                accumulatedDrag = 0f
            },
            onDragEnd = {
                accumulatedDrag = 0f
            },
            onDragCancel = {
                accumulatedDrag = 0f
            },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                accumulatedDrag += dragAmount
                // RTL handling: dragging right moves to previous in RTL, left moves to next
                if (accumulatedDrag > threshold && index > 0) {
                    onMove(index, index - 1)
                    accumulatedDrag = 0f
                } else if (accumulatedDrag < -threshold && index < itemCount - 1) {
                    onMove(index, index + 1)
                    accumulatedDrag = 0f
                }
            }
        )
    }
)
