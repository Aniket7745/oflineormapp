package com.example.oflineorm.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.model.SpendingMetrics
import com.example.oflineorm.utils.generateRandomColors
import com.example.oflineorm.utils.parseDate
import com.example.oflineorm.utils.toCurrencyString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedSpendingScreen(metrics: SpendingMetrics, receipts: List<ReceiptData>, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detailed Spending Overview") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Summary cards
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Recorded Spend", style = MaterialTheme.typography.titleMedium)
                    Text(metrics.totalSpend.toCurrencyString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard("This Month", metrics.monthlySpend.toCurrencyString(), Modifier.weight(1f))
                MetricCard("This Week", metrics.weeklySpend.toCurrencyString(), Modifier.weight(1f))
                MetricCard("Daily Average", metrics.dailyAverage.toCurrencyString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))

            // Weekly spending bar chart
            Text("Weekly Spending", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            WeeklySpendingBarChart(receipts = receipts)
            Spacer(Modifier.height(24.dp))

            // Category Breakdown Chart
            Text("Category Breakdown", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            if (metrics.tagBreakdown.isNotEmpty()) {
                CategoryPieChart(metrics.tagBreakdown)
            } else {
                Text("No tagged transactions yet.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun WeeklySpendingBarChart(receipts: List<ReceiptData>) {
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.SUNDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val weekStart = calendar.time
    val weekDays = (0..6).map {
        val date = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        date
    }
    val weekEnd = calendar.time

    val weeklyReceipts = receipts.filter { receipt ->
        receipt.transactionAmount != null
    }.filter { receipt ->
        val transactionCal = Calendar.getInstance().apply {
            val parsedDate = parseDate(receipt.transactionDate)
            if (parsedDate != null) {
                time = parsedDate
            } else {
                timeInMillis = receipt.timestamp
            }
        }
        !transactionCal.before(Calendar.getInstance().apply { time = weekStart }) && transactionCal.before(Calendar.getInstance().apply { time = weekEnd })
    }

    val dailyTotals = weekDays.map { date ->
        val dayCal = Calendar.getInstance().apply { time = date }
        val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)

        weeklyReceipts
            .filter { receipt ->
                val transactionCal = Calendar.getInstance().apply {
                    val parsedDate = parseDate(receipt.transactionDate)
                    if (parsedDate != null) {
                        time = parsedDate
                    } else {
                        timeInMillis = receipt.timestamp
                    }
                }
                transactionCal.get(Calendar.DAY_OF_WEEK) == dayOfWeek &&
                        transactionCal.get(Calendar.WEEK_OF_YEAR) == dayCal.get(Calendar.WEEK_OF_YEAR)
            }
            .sumOf { it.transactionAmount ?: 0.0 }
    }

    val maxAmount = dailyTotals.maxOrNull() ?: 1.0
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dailyTotals.forEachIndexed { index, amount ->
                val dayName = dayFormat.format(weekDays[index])
                val barPercentage = if (maxAmount > 0) (amount / maxAmount).toFloat() else 0f
                DaySpendingBar(dayName = dayName, amount = amount, barPercentage = barPercentage)
            }
        }
    }
}

@Composable
private fun DaySpendingBar(dayName: String, amount: Double, barPercentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(dayName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.15f))
        Row(modifier = Modifier.weight(0.85f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barPercentage)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
            Text(
                amount.toCurrencyString(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CategoryPieChart(tagBreakdown: Map<String, Double>) {
    val total = tagBreakdown.values.sum()
    val sortedBreakdown = tagBreakdown.toList().sortedByDescending { it.second }
    val colors = remember { generateRandomColors(sortedBreakdown.size) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(200.dp)) {
            Canvas(modifier = Modifier.size(200.dp)) {
                var startAngle = -90f
                sortedBreakdown.forEachIndexed { index, (_, amount) ->
                    val sweepAngle = (amount / total).toFloat() * 360f
                    drawArc(
                        color = colors[index],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 60f)
                    )
                    startAngle += sweepAngle
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sortedBreakdown.forEachIndexed { index, (tag, amount) ->
                val percentage = (amount / total) * 100
                CategoryLegendItem(tag = tag, amount = amount, percentage = percentage, color = colors[index])
            }
        }
    }
}

@Composable
private fun CategoryLegendItem(tag: String, amount: Double, percentage: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, CircleShape)
            )
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text(tag, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = "${amount.toCurrencyString()} (${String.format("%.1f", percentage)}%)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
