package com.example.oflineorm.utils

fun extractGPayData(rawText: String): Pair<Double?, String?> {
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
    return Pair(amount, timeMatch?.value)
}

fun extractPhonePeData(rawText: String): Pair<Double?, String?> {
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
    return Pair(amount, timeMatch?.value)
}
