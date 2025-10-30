package com.example.oflineorm.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.utils.parseDate
import com.example.oflineorm.utils.toCurrencyString
import java.util.*

@Composable
fun SpendingOverviewCard(
    receipts: List<ReceiptData>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit // 👈 new parameter
) {
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val weekStart = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.SUNDAY
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun getTransactionTime(receipt: ReceiptData): Long {
        val parsed = parseDate(receipt.transactionDate)
        return parsed?.time ?: receipt.timestamp
    }

    val todayTotal = receipts.filter { getTransactionTime(it) >= todayStart }
        .sumOf { it.transactionAmount ?: 0.0 }

    val weeklyTotal = receipts.filter { getTransactionTime(it) >= weekStart }
        .sumOf { it.transactionAmount ?: 0.0 }

    val monthlyTotal = receipts.filter { getTransactionTime(it) >= monthStart }
        .sumOf { it.transactionAmount ?: 0.0 }

    // 🟩 Outer clickable card
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }, // 👈 make the section clickable
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Spending Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OverviewMiniCard(
                    title = "Today",
                    value = todayTotal.toCurrencyString(),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                )

                OverviewMiniCard(
                    title = "This Week",
                    value = weeklyTotal.toCurrencyString(),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                )

                OverviewMiniCard(
                    title = "This Month",
                    value = monthlyTotal.toCurrencyString(),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OverviewMiniCard(
    title: String,
    value: String,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(3.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
