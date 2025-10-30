package com.example.oflineorm.data

import android.util.Log
import androidx.activity.ComponentActivity
import com.example.oflineorm.model.ReceiptData
import org.json.JSONObject

fun saveReceiptsToPrefs(context: ComponentActivity, receipts: List<ReceiptData>) {
    val prefs = context.getSharedPreferences("ReceiptsPrefs", ComponentActivity.MODE_PRIVATE)
    val editor = prefs.edit()
    val jsonObject = JSONObject()
    receipts.forEachIndexed { index, receipt ->
        val receiptJson = JSONObject().apply {
            put("sourceAppId", receipt.sourceAppId)
            put("timestamp", receipt.timestamp)
            put("rawText", receipt.rawText)
            put("transactionAmount", receipt.transactionAmount?.toString() ?: "")
            put("transactionTime", receipt.transactionTime ?: "")
            put("tag", receipt.tag ?: "")
        }
        jsonObject.put("receipt_$index", receiptJson)
    }
    editor.putString("receipts_list", jsonObject.toString())
    editor.putInt("receipts_count", receipts.size)
    editor.apply()
    Log.d("STORAGE", "Saved ${receipts.size} receipts to local storage")
}

fun loadReceiptsFromPrefs(context: ComponentActivity): MutableList<ReceiptData> {
    val prefs = context.getSharedPreferences("ReceiptsPrefs", ComponentActivity.MODE_PRIVATE)
    val receiptsJson = prefs.getString("receipts_list", null)
    val count = prefs.getInt("receipts_count", 0)
    val receipts = mutableListOf<ReceiptData>()
    if (receiptsJson != null) {
        try {
            val jsonObject = JSONObject(receiptsJson)
            for (i in 0 until count) {
                val receiptJson = jsonObject.getJSONObject("receipt_$i")
                val amountString = receiptJson.optString("transactionAmount")
                val amount = amountString.toDoubleOrNull()
                val receipt = ReceiptData(
                    sourceAppId = receiptJson.getString("sourceAppId"),
                    timestamp = receiptJson.getLong("timestamp"),
                    rawText = receiptJson.getString("rawText"),
                    transactionAmount = amount,
                    transactionTime = receiptJson.getString("transactionTime").takeIf { it.isNotEmpty() },
                    tag = receiptJson.optString("tag").takeIf { it.isNotEmpty() }
                )
                receipts.add(receipt)
            }
        } catch (e: Exception) {
            Log.e("STORAGE", "Error loading receipts: ${e.message}")
        }
    }
    return receipts
}
