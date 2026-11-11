package com.selendang3.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun PlantationBackground(route: String) {
    // Low-contrast vector palm canopy; the same semantic palette applies to every route.
    val base = MaterialTheme.colorScheme.background
    val accent = MaterialTheme.colorScheme.primary
    val soft = MaterialTheme.colorScheme.secondaryContainer
    Box(Modifier.fillMaxSize().background(base).background(Brush.verticalGradient(listOf(soft.copy(alpha = 0.45f), base)))) {
        Canvas(Modifier.fillMaxSize()) {
            for (row in 0..7) for (col in 0..4) {
                val center = Offset(size.width * (col + 0.2f + (row % 2) * 0.4f) / 4f, size.height * row / 7f)
                val radius = size.width * 0.11f
                for (leaf in 0..7) {
                    val angle = leaf * Math.PI / 4
                    drawLine(accent.copy(alpha = if (route == "home" || route == "dashboard") 0.055f else 0.02f), center,
                        center + Offset((cos(angle) * radius).toFloat(), (sin(angle) * radius).toFloat()),
                        strokeWidth = 12f)
                }
                drawCircle(accent.copy(alpha = if (route == "home" || route == "dashboard") 0.06f else 0.02f), radius * 0.18f, center)
            }
        }

    }
}
