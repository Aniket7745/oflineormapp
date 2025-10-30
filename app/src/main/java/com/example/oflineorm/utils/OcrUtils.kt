package com.example.oflineorm.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

fun extractGPayData(rawText: String): Triple<Double?, String?, String?> {
    val preCleanedText = rawText.replace("\u00a0", " ")
    val lines = preCleanedText.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
    val lineAmountRegex = Regex("^([0-9]{1,3}(?:,?[0-9]{3})*(?:\\.[0-9]{2})?)\$")
    var amount: Double? = null
    val maxLinesToCheck = minOf(lines.size, 5)

    for (i in 0 until maxLinesToCheck) {
        val line = lines[i]
        val match = lineAmountRegex.find(line)
        if (match != null) {
            val amountString = match.groups[1]?.value
            val cleanAmountString = amountString?.replace(",", "")?.replace(" ", "")
            if (cleanAmountString != null && cleanAmountString.length < 9) {
                amount = cleanAmountString.toDoubleOrNull()
                if (amount != null) break
            }
        }
    }
    val timeRegex = Regex("\\d{1,2}:\\d{2}\\s?(?:[AaPp][Mm])")
    val timeMatch = timeRegex.find(rawText)

    val dateRegex = Pattern.compile("(\\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{1,2},?\\s+\\d{4}\\b)|(\\b\\d{1,2}\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4}\\b)|(\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b)")
    val dateMatcher = dateRegex.matcher(rawText)
    val date = if (dateMatcher.find()) dateMatcher.group() else null

    return Triple(amount, timeMatch?.value, date)
}

fun extractPhonePeData(rawText: String): Triple<Double?, String?, String?> {
    val preCleanedText = rawText.replace("\u00a0", " ")
    val lines = preCleanedText.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
    val lineAmountRegex = Regex("([0-9]{1,3}(?:,?[0-9]{3})*(?:\\.[0-9]{2})?)")
    var amount: Double? = null

    if (lines.size >= 2) {
        val lineIndex = lines.size - 2
        val targetLine = lines[lineIndex]
        val match = lineAmountRegex.find(targetLine)
        val amountString = match?.groups?.get(1)?.value
        val cleanAmountString = amountString?.replace(",", "")?.replace(" ", "")

        if (cleanAmountString != null && cleanAmountString.length < 9) {
            amount = cleanAmountString.toDoubleOrNull()
        }
    }
    val timeRegex = Regex("\\d{1,2}:\\d{2}\\s?(?:[AaPp][Mm])")
    val timeMatch = timeRegex.find(rawText)

    val dateRegex = Pattern.compile("(\\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{1,2},?\\s+\\d{4}\\b)|(\\b\\d{1,2}\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4}\\b)|(\\b\\d{1,2}/\\d{1,2}/\\d{2,4}\\b)")
    val dateMatcher = dateRegex.matcher(rawText)
    val date = if (dateMatcher.find()) dateMatcher.group() else null

    return Triple(amount, timeMatch?.value, date)
}

// A robust date parser that tries multiple common formats
fun parseDate(dateString: String?): Date? {
    if (dateString.isNullOrBlank()) return null
    val formats = listOf(
        SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH),
        SimpleDateFormat("d MMM yyyy", Locale.ENGLISH),
        SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    )
    for (format in formats) {
        try {
            return format.parse(dateString)
        } catch (e: Exception) {
            // Continue to next format
        }
    }
    return null // Return null if all formats fail
}
