package com.example.oflineorm.data

import android.content.Context
import android.util.Log
import com.example.oflineorm.model.ReceiptData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

private const val JSON_FILE_NAME = "receipts.json"

fun saveReceiptsAsJson(context: Context, receipts: List<ReceiptData>) {
    val gson = Gson()
    val jsonString = gson.toJson(receipts)
    try {
        val file = File(context.filesDir, JSON_FILE_NAME)
        file.writeText(jsonString)
        Log.d("STORAGE", "Saved ${receipts.size} receipts to $JSON_FILE_NAME")
    } catch (e: Exception) {
        Log.e("STORAGE", "Error saving receipts to JSON: ${e.message}")
    }
}

fun loadReceiptsFromJson(context: Context): MutableList<ReceiptData> {
    try {
        val file = File(context.filesDir, JSON_FILE_NAME)
        if (!file.exists()) {
            return mutableListOf()
        }
        val jsonString = file.readText()
        if (jsonString.isBlank()) {
            return mutableListOf()
        }
        val type = object : TypeToken<List<ReceiptData>>() {}.type
        val receipts = Gson().fromJson<List<ReceiptData>>(jsonString, type)
        return receipts.toMutableList()
    } catch (e: Exception) {
        Log.e("STORAGE", "Error loading receipts from JSON: ${e.message}")
        return mutableListOf()
    }
}
