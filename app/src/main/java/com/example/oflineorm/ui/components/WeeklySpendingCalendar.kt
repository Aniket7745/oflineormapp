package com.example.oflineorm.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.utils.toCurrencyString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WeeklySpendingCalendar(receipts: List<ReceiptData>, modifier: Modifier = Modifier) {
    val calendar = Calendar.getInstance()
    // Set to start of the week (Sunday)
    calendar.firstDayOfWeek = Calendar.SUNDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val weekDays = (0..6).map {
        val date = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        date
    }
    val nextWeekStart = calendar.timeInMillis

    val weeklyReceipts = receipts.filter {
        it.transactionAmount != null && it.timestamp >= weekDays.first().time && it.timestamp < nextWeekStart
    }

    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "This Week's Spending",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { date ->
                    val dayStart = date.time
                    val dayEnd = dayStart + 24 * 60 * 60 * 1000

                    val amount = weeklyReceipts
                        .filter { it.timestamp >= dayStart && it.timestamp < dayEnd }
                        .sumOf { it.transactionAmount!! }

                    DayCell(dayName = dayFormat.format(date), amount = amount)
                }
            }
        }
    }
}

@Composable
private fun DayCell(dayName: String, amount: Double) {
    Column(
        modifier = Modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(dayName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            if (amount > 0) amount.toCurrencyString() else "-",
            style = MaterialTheme.typography.bodySmall,
            color = if (amount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
