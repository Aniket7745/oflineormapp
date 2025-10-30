package com.example.oflineorm.ui

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oflineorm.data.loadReceiptsFromJson
import com.example.oflineorm.data.saveReceiptsAsJson
import com.example.oflineorm.model.ReceiptData
import com.example.oflineorm.ui.components.EditReceiptDialog
import com.example.oflineorm.ui.components.ReceiptItem
import com.example.oflineorm.ui.components.SpendingOverviewCard
import com.example.oflineorm.ui.components.WeeklySpendingCalendar
import com.example.oflineorm.utils.GPAY_PACKAGE
import com.example.oflineorm.utils.PHONEPE_PACKAGE
import com.example.oflineorm.utils.calculateSpendingMetrics
import com.example.oflineorm.utils.extractGPayData
import com.example.oflineorm.utils.extractPhonePeData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@Composable
fun MainScreen(
    imageUri: Uri?,
    sourcePackageId: String,
    context: Activity
) {
    val receiptsList = remember { mutableStateListOf<ReceiptData>() }
    var showEditDialog by remember { mutableStateOf(false) }
    var pendingReceipt by remember { mutableStateOf<ReceiptData?>(null) }
    var selectedReceipt by remember { mutableStateOf<ReceiptData?>(null) }
    var showDetailedSpending by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val loadedReceipts = loadReceiptsFromJson(context)
        receiptsList.clear()
        receiptsList.addAll(loadedReceipts)
    }

    val onDeleteReceipt: (ReceiptData) -> Unit = { receiptToDelete ->
        receiptsList.remove(receiptToDelete)
        saveReceiptsAsJson(context, receiptsList.toList())
    }

    val onSaveEditedReceipt: (ReceiptData, Double?, String?, String?) -> Unit = { originalReceipt, newAmount, newDate, newTag ->
        val finalReceipt = originalReceipt.copy(
            transactionAmount = newAmount,
            transactionDate = newDate,
            tag = newTag?.trim()?.takeIf { it.isNotEmpty() }
        )

        val index = receiptsList.indexOf(originalReceipt)
        if (index != -1) {
            receiptsList[index] = finalReceipt
        } else {
            receiptsList.add(0, finalReceipt)
        }

        saveReceiptsAsJson(context, receiptsList.toList())

        showEditDialog = false
        pendingReceipt = null
    }

    if (imageUri != null) {
        LaunchedEffect(imageUri, sourcePackageId) {
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val fullRawText = visionText.text
                        val (extractedAmount, extractedTime, extractedDate) = when {
                            sourcePackageId.contains(GPAY_PACKAGE) -> extractGPayData(fullRawText)
                            sourcePackageId.contains(PHONEPE_PACKAGE) -> extractPhonePeData(fullRawText)
                            else -> extractPhonePeData(fullRawText)
                        }

                        val tempReceipt = ReceiptData(
                            sourceAppId = sourcePackageId,
                            timestamp = System.currentTimeMillis(),
                            rawText = fullRawText,
                            transactionAmount = extractedAmount,
                            transactionTime = extractedTime,
                            transactionDate = extractedDate,
                            tag = null
                        )

                        pendingReceipt = tempReceipt
                        showEditDialog = true
                    }
                    .addOnFailureListener { e ->
                        Log.e("OCR_ERROR", "Text recognition failed: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("IMAGE_LOAD_ERROR", "Failed to load image: ${e.message}")
            }
        }
    }

    val spendingMetrics = calculateSpendingMetrics(receiptsList)

    if (selectedReceipt != null) {
        ReceiptDetailScreen(receipt = selectedReceipt!!, onBack = { selectedReceipt = null })
    } else if (showDetailedSpending) {
        DetailedSpendingScreen(metrics = spendingMetrics, receipts = receiptsList, onBack = { showDetailedSpending = false })
    } else {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    SpendingOverviewCard(
                        metrics = spendingMetrics,
                        modifier = Modifier.clickable { showDetailedSpending = true }
                    )

                    WeeklySpendingCalendar(receipts = receiptsList)

                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(receiptsList) { receipt ->
                            ReceiptItem(
                                receipt = receipt,
                                onDelete = onDeleteReceipt,
                                onEdit = {
                                    pendingReceipt = it
                                    showEditDialog = true
                                },
                                modifier = Modifier.clickable { selectedReceipt = receipt }
                            )
                        }
                    }
                }

                if (showEditDialog && pendingReceipt != null) {
                    EditReceiptDialog(
                        receipt = pendingReceipt!!,
                        onDismiss = {
                            showEditDialog = false
                            pendingReceipt = null
                        },
                        onSave = onSaveEditedReceipt
                    )
                }
            }
        }
    }
}
