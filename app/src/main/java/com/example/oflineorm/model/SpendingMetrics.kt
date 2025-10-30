package com.example.oflineorm.model

data class SpendingMetrics(
    val totalSpend: Double,
    val todaysSpending: Double,
    val dailyAverage: Double,
    val weeklySpend: Double,
    val monthlySpend: Double,
    val tagBreakdown: Map<String, Double> // Added
)
