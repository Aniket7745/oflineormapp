package com.example.oflineorm.model

data class ReceiptData(
    val sourceAppId: String,
    val timestamp: Long,
    val rawText: String,
    val transactionAmount: Double?,
    val transactionTime: String?,
    val tag: String? = null
)
