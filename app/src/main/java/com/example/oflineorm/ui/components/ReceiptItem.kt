package com.example.oflineorm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.utils.GPAY_PACKAGE
import com.example.oflineorm.utils.PHONEPE_PACKAGE
import com.example.oflineorm.utils.toDateString
import com.example.oflineorm.utils.toCurrencyString

@Composable
fun ReceiptItem(
    receipt: ReceiptData,
    onDelete: (ReceiptData) -> Unit,
    onEdit: (ReceiptData) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = when {
        receipt.sourceAppId.contains(GPAY_PACKAGE) -> Color(0xFF4285F4)
        receipt.sourceAppId.contains(PHONEPE_PACKAGE) -> Color(0xFF673AB7)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (receipt.sourceAppId.contains(GPAY_PACKAGE) || receipt.sourceAppId.contains(PHONEPE_PACKAGE)) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 1. Amount
                Text(
                    text = receipt.transactionAmount?.toCurrencyString() ?: "Amount UNKNOWN",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))

                // 2. Date and Time
                Row {
                    Text(
                        text = receipt.timestamp.toDateString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = receipt.transactionTime ?: "Time N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Action Buttons
            Row {
                IconButton(onClick = { onEdit(receipt) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Receipt",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { onDelete(receipt) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Receipt",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
