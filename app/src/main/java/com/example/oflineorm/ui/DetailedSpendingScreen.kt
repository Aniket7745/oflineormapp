package com.example.oflineorm.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.model.SpendingMetrics
import com.example.oflineorm.utils.parseDate
import com.example.oflineorm.utils.toCurrencyString
import java.util.*
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedSpendingScreen(
    receipts: List<ReceiptData>,
    onBack: () -> Unit,
    metrics: SpendingMetrics
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

    val todayReceipts = receipts.filter { getTransactionTime(it) >= todayStart }
    val weekReceipts = receipts.filter { getTransactionTime(it) >= weekStart }
    val monthReceipts = receipts.filter { getTransactionTime(it) >= monthStart }

    val todayTotal = todayReceipts.sumOf { it.transactionAmount ?: 0.0 }
    val weeklyTotal = weekReceipts.sumOf { it.transactionAmount ?: 0.0 }
    val monthlyTotal = monthReceipts.sumOf { it.transactionAmount ?: 0.0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detailed Spending") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Spending Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewMiniCard(
                    title = "Today",
                    value = todayTotal.toCurrencyString(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
                OverviewMiniCard(
                    title = "This Week",
                    value = weeklyTotal.toCurrencyString(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
                OverviewMiniCard(
                    title = "This Month",
                    value = monthlyTotal.toCurrencyString(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Spending by Tag",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            SpendingByTagChart(receipts)
        }
    }
}

@Composable
fun OverviewMiniCard(
    title: String,
    value: String,
    containerColor: Color
) {
    Card(
        modifier = Modifier

            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 8.dp),
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
            Spacer(Modifier.height(4.dp))
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

@Composable
fun SpendingByTagChart(receipts: List<ReceiptData>) {
    val grouped = receipts.groupBy { it.tag ?: "Untagged" }
    val total = grouped.values.flatten().sumOf { it.transactionAmount ?: 0.0 }

    val tagTotals = grouped.mapValues { entry ->
        entry.value.sumOf { it.transactionAmount ?: 0.0 }
    }

    val colors = listOf(
        Color(0xFFEF5350), // Red
        Color(0xFF42A5F5), // Blue
        Color(0xFF66BB6A), // Green
        Color(0xFFFFCA28), // Yellow
        Color(0xFFAB47BC), // Purple
        Color(0xFFFF7043)  // Orange
    )

    val tagList = tagTotals.entries.toList()
    val sweepAngles = tagList.map { (it.value / total * 360f).toFloat() }

    Canvas(
        modifier = Modifier
            .size(250.dp)
            .padding(top = 12.dp)
    ) {
        var startAngle = 0f
        val chartSize = min(size.width, size.height)

        tagList.forEachIndexed { index, entry ->
            val sweep = sweepAngles[index]
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(chartSize, chartSize)
            )
            startAngle += sweep
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tagList.forEachIndexed { index, entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .padding(2.dp)
                        .background(colors[index % colors.size], shape = RoundedCornerShape(4.dp))
                )
                Text(
                    text = "${entry.key}: ${entry.value.toCurrencyString()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
