package com.selendang3.app.data

import java.util.Locale
import kotlin.math.sqrt

enum class PlantingPattern(val label: String) { TRIANGULAR("Triangular"), SQUARE("Square") }
enum class PlantingDistanceUnit(val label: String) { METRES("Metres"), FEET("Feet") }
enum class ProductRateUnit(val label: String, val factor: Double, val liquid: Boolean) {
    LITRES("L/ha", 1000.0, true), MILLILITRES("mL/ha", 1.0, true),
    KILOGRAMS("kg/ha", 1000.0, false), GRAMS("g/ha", 1.0, false);
    val doseUnit: String get() = if (liquid) "mL/pump" else "g/pump"
}
data class PlantDensityResult(val metres: Double, val rowSpacingMetres: Double, val palmsPerHectare: Double)
data class PumpDoseResult(val productPerPump: Double, val hectaresPerPump: Double, val pumpsPerHectare: Double)
object PlanterCalculations {
    fun input(text: String): Double? {
        val clean = text.trim()
        if (!Regex("^([0-9]+([.,][0-9]*)?|[.,][0-9]+)$").matches(clean)) return null
        return clean.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
    }
    fun density(distance: Double, unit: PlantingDistanceUnit, pattern: PlantingPattern): PlantDensityResult? {
        if (!distance.isFinite() || distance <= 0) return null
        val metres = distance * if (unit == PlantingDistanceUnit.FEET) 0.3048 else 1.0
        val rowSpacing = metres * if (pattern == PlantingPattern.TRIANGULAR) sqrt(3.0) / 2.0 else 1.0
        val density = 10000.0 / (metres * rowSpacing)
        if (!listOf(metres, rowSpacing, density).all { it.isFinite() && it > 0 }) return null
        return PlantDensityResult(metres, rowSpacing, density)
    }
    fun dose(rate: Double, unit: ProductRateUnit, tankLitres: Double, sprayLitresPerHectare: Double): PumpDoseResult? {
        if (!listOf(rate, tankLitres, sprayLitresPerHectare).all { it.isFinite() && it > 0 }) return null
        val coverage = tankLitres / sprayLitresPerHectare
        val dose = rate * unit.factor * coverage
        val pumps = sprayLitresPerHectare / tankLitres
        if (!listOf(coverage, dose, pumps).all { it.isFinite() && it > 0 } || (unit.liquid && dose / 1000.0 > tankLitres)) return null
        return PumpDoseResult(dose, coverage, pumps)
    }
    fun number(value: Double): String = String.format(Locale.US, if (value < 0.01 || value >= 1e9) "%.6g" else "%.2f", value)
}
