package com.example.oflineorm.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.utils.PREDEFINED_TAGS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReceiptDialog(
    receipt: ReceiptData,
    onDismiss: () -> Unit,
    onSave: (originalReceipt: ReceiptData, newAmount: Double?, newTag: String?) -> Unit
) {
    var amountText by remember { mutableStateOf(receipt.transactionAmount?.toString() ?: "") }
    var selectedTag by remember { mutableStateOf(receipt.tag ?: "") }
    var isExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verify & Tag Expense") },
        text = {
            Column {
                // 1. Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Transaction Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // 2. Tag Dropdown
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = selectedTag.takeIf { it.isNotEmpty() } ?: "Select Category",
                        onValueChange = { },
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        PREDEFINED_TAGS.forEach { tag ->
                            DropdownMenuItem(
                                text = { Text(tag) },
                                onClick = {
                                    selectedTag = tag
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Source: ${receipt.sourceAppId}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmount = amountText.toDoubleOrNull()
                    val finalTag = selectedTag.takeIf { it.isNotEmpty() }
                    onSave(receipt, finalAmount, finalTag)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
