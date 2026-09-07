package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RenameGroupDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل الاسم", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("الاسم الجديد") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onRename(name) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddDenominationDialog(
    onDismiss: () -> Unit,
    onAdd: (Int) -> Unit
) {
    var denomInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة فئة جديدة", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = denomInput,
                onValueChange = { if (it.all { char -> char.isDigit() }) denomInput = it },
                label = { Text("قيمة الفئة") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    denomInput.toIntOrNull()?.let { onAdd(it) }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun EditDenominationDialog(
    currentDenom: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var denomInput by remember { mutableStateOf(currentDenom.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل الفئة", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = denomInput,
                onValueChange = { if (it.all { char -> char.isDigit() }) denomInput = it },
                label = { Text("قيمة الفئة الجديدة") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    denomInput.toIntOrNull()?.let { onConfirm(it) }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("تعديل")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
