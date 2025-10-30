package com.example.oflineorm.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.utils.PREDEFINED_TAGS

// A palette of soft, distinct colors for the tags
private val tagColors = listOf(
    Color(0xFFE1F5FE), // Light Blue
    Color(0xFFFCE4EC), // Light Pink
    Color(0xFFF3E5F5), // Light Purple
    Color(0xFFE8F5E9), // Light Green
    Color(0xFFFFFDE7), // Light Yellow
    Color(0xFFFBE9E7), // Light Orange
    Color(0xFFEFEBE9)  // Light Brown
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditReceiptDialog(
    receipt: ReceiptData,
    onDismiss: () -> Unit,
    onSave: (originalReceipt: ReceiptData, newAmount: Double?, newDate: String?, newTag: String?) -> Unit
) {
    var amountText by remember { mutableStateOf(receipt.transactionAmount?.toString() ?: "") }
    var dateText by remember { mutableStateOf(receipt.transactionDate ?: "") }
    var selectedTag by remember { mutableStateOf(receipt.tag ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column {
                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // Date Input
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))

                // Tag Selection
                Text("Select a Category", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PREDEFINED_TAGS.forEachIndexed { index, tag ->
                        val colorIndex = index % tagColors.size
                        val isSelected = selectedTag == tag
                        TagChip(
                            text = tag,
                            isSelected = isSelected,
                            color = tagColors[colorIndex],
                            onClick = {
                                selectedTag = if (isSelected) "" else tag // Toggle selection
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmount = amountText.toDoubleOrNull()
                    val finalDate = dateText.takeIf { it.isNotEmpty() }
                    val finalTag = selectedTag.takeIf { it.isNotEmpty() }
                    onSave(receipt, finalAmount, finalDate, finalTag)
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

@Composable
private fun TagChip(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) color.copy(alpha = 0.8f) else color.copy(alpha = 0.5f)
    val backgroundColor = if (isSelected) color else Color.Transparent

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
