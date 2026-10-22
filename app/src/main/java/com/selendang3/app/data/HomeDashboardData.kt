package com.selendang3.app.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeDashboardRow(val title: String, val detail: String)

fun homeDashboardRows(update: EstatePackage?, today: LocalDate): List<HomeDashboardRow> {
    val month = YearMonth.from(today)
    val monthFormat = DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH)
    fun label(period: YearMonth) = period.format(monthFormat)
    fun number(value: Double?) = value?.takeIf { it.isFinite() && it >= 0 }
        ?.let { String.format(Locale.US, "%.2f", it) } ?: "Pending"
    val yph = update?.yph?.takeIf {
        it.reportingYear == today.year && it.throughMonth in 1..12 &&
            YearMonth.of(it.reportingYear, it.throughMonth) <= month &&
            it.matureAreaHa.isFinite() && it.matureAreaHa > 0
    }
    val annualBudgetProgress = yph?.let { source ->
        val actual = source.actualYtdTonnes?.takeIf { it.isFinite() && it >= 0 } ?: return@let null
        val budget = source.fullYearBudgetTonnes?.takeIf { it.isFinite() && it > 0 } ?: return@let null
        (actual / budget * 100).takeIf { it.isFinite() }
    }
    val yieldPeriod = yph?.let { "YTD through " + label(YearMonth.of(it.reportingYear, it.throughMonth)) } ?: "YTD " + today.year
    val costs = estateFfbCosts(update)?.takeIf { it.actualPeriod.year == today.year && it.actualPeriod <= month }
    return listOf(
        HomeDashboardRow("Crop data · t/ha",
            label(month.minusMonths(1)) + ": Pending · " + label(month) + " MTD: Pending\n" +
                yieldPeriod + ": " + number(yph?.actualYtdYph) + "\nYTD vs Year Budget: " +
                (annualBudgetProgress?.let { number(it) + "%" } ?: "Pending")),
        HomeDashboardRow("Upkeep and Manuring · %",
            "Weeding: Pending · Manuring: Pending"),
        HomeDashboardRow("Estate cost · Actual YTD · RM/tonne",
            "Operation: " + number(costs?.groups?.firstOrNull { it.label == "Operation Cost" }?.actualYtdRmPerTonne) +
                " · GC: " + number(costs?.groups?.firstOrNull { it.label == "General Charges" }?.actualYtdRmPerTonne) +
                "\n" + (costs?.let { "YTD through " + label(it.actualPeriod) } ?: "YTD " + today.year))
    )
}
