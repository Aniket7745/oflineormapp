package com.example.oflineorm.utils

import androidx.compose.ui.graphics.Color

fun generateRandomColors(count: Int): List<Color> {
    val colors = mutableListOf<Color>()
    for (i in 0 until count) {
        val hue = (i * (360f / count)) % 360f
        colors.add(Color.hsv(hue, 0.8f, 0.9f))
    }
    return colors
}
