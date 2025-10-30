package com.example.oflineorm.utils

import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.model.SpendingMetrics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Double.toCurrencyString(): String {
    return String.format(Locale.getDefault(), "₹%.2f", this)
}

private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

fun Long.toDateString(): String {
    return dateFormatter.format(Date(this))
}

fun calculateSpendingMetrics(receipts: List<ReceiptData>): SpendingMetrics {
    val validReceipts = receipts.filter { it.transactionAmount != null }
    val totalSpend = validReceipts.sumOf { it.transactionAmount!! }

    val tagBreakdown = validReceipts
        .groupBy { it.tag ?: "Untagged" }
        .mapValues { (_, list) -> list.sumOf { it.transactionAmount!! } }
        .toList()
        .sortedByDescending { it.second }
        .toMap()

    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance().apply { timeInMillis = now }

    // Today's spending
    val startOfToday = (calendar.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todaysSpending = validReceipts
        .filter { it.timestamp >= startOfToday.timeInMillis }
        .sumOf { it.transactionAmount!! }

    // This week's spending
    val startOfWeek = (calendar.clone() as Calendar).apply {
        firstDayOfWeek = Calendar.SUNDAY
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    }
    val weeklySpend = validReceipts
        .filter { it.timestamp >= startOfWeek.timeInMillis }
        .sumOf { it.transactionAmount!! }

    // This month's spending
    val startOfMonth = (calendar.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val monthlySpend = validReceipts
        .filter { it.timestamp >= startOfMonth.timeInMillis }
        .sumOf { it.transactionAmount!! }

    val dailyAverage = if (calendar.get(Calendar.DAY_OF_MONTH) > 0) {
        monthlySpend / calendar.get(Calendar.DAY_OF_MONTH).toDouble()
    } else {
        0.0
    }

    return SpendingMetrics(
        totalSpend = totalSpend,
        todaysSpending = todaysSpending,
        dailyAverage = dailyAverage,
        weeklySpend = weeklySpend,
        monthlySpend = monthlySpend,
        tagBreakdown = tagBreakdown
    )
}
