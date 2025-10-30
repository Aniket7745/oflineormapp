package com.example.oflineorm.utils

import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.model.SpendingMetrics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Double.toCurrencyString(): String {
    // Correctly formats to Indian Rupees (₹) with two decimal places
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

    val now = Calendar.getInstance()

    // --- 1. Today's Spending (This logic was correct) ---
    val startOfToday = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val endOfToday = (now.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val todaysSpending = validReceipts
        .filter { it.timestamp >= startOfToday.timeInMillis && it.timestamp <= endOfToday.timeInMillis }
        .sumOf { it.transactionAmount!! }

    // --- 2. This Week's Spending (Logic was correct, but is often misunderstood. Keeping it.) ---
    // Calculates spending from the beginning of the week (e.g., Sunday 00:00) until NOW.
    val startOfWeek = (now.clone() as Calendar).apply {
        firstDayOfWeek = Calendar.SUNDAY // Assumes week starts Sunday, matching the original code
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val weeklySpend = validReceipts
        // Filter receipts only from the start of the week up to the current timestamp (now)
        .filter { it.timestamp >= startOfWeek.timeInMillis }
        .sumOf { it.transactionAmount!! }

    // --- 3. This Month's Spending (Logic was correct) ---
    val startOfMonth = (now.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val monthlySpend = validReceipts
        // Filter receipts only from the start of the month up to the current timestamp (now)
        .filter { it.timestamp >= startOfMonth.timeInMillis }
        .sumOf { it.transactionAmount!! }

    // Re-added the daily average calculation, assuming SpendingMetrics requires it.
    val dailyAverage = if (now.get(Calendar.DAY_OF_MONTH) > 0) {
        monthlySpend / now.get(Calendar.DAY_OF_MONTH).toDouble()
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