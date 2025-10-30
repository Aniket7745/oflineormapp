package com.example.oflineorm.utils

import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.model.SpendingMetrics
import java.text.SimpleDateFormat
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

    // NEW: Tag Breakdown
    val tagBreakdown = validReceipts
        .groupBy { it.tag ?: "Untagged" }
        .mapValues { (_, list) -> list.sumOf { it.transactionAmount!! } }
        .toList()
        .sortedByDescending { it.second }
        .toMap()


    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = now }
    val currentDayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)

    val startOfWeek = (calendar.clone() as java.util.Calendar).apply {
        add(java.util.Calendar.DATE, 1 - currentDayOfWeek)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }

    val startOfMonth = (calendar.clone() as java.util.Calendar).apply {
        set(java.util.Calendar.DAY_OF_MONTH, 1)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }

    val monthlySpend = validReceipts.filter { it.timestamp >= startOfMonth.timeInMillis }
        .sumOf { it.transactionAmount!! }

    val weeklySpend = validReceipts.filter { it.timestamp >= startOfWeek.timeInMillis }
        .sumOf { it.transactionAmount!! }

    val dailyAverage = if (currentDayOfMonth > 0) {
        monthlySpend / currentDayOfMonth.toDouble()
    } else {
        0.0
    }

    return SpendingMetrics(
        totalSpend = totalSpend,
        dailyAverage = dailyAverage,
        weeklySpend = weeklySpend,
        monthlySpend = monthlySpend,
        tagBreakdown = tagBreakdown // NEW
    )
}
