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
        HomeDashboardRow("Crop Data · mt/ha",
            label(month.minusMonths(1)) + ": Pending · " + label(month) + " MTD: Pending\n" +
                yieldPeriod + ": " + number(yph?.actualYtdYph) + "\nYTD vs Year Budget: " +
                (annualBudgetProgress?.let { number(it) + "%" } ?: "Pending")),
        HomeDashboardRow("Upkeep and Manuring · %",
            "Weeding: Pending · Manuring: Pending"),
        HomeDashboardRow("Estate Cost · Actual YTD · RM/mt",
            "Operation: " + number(costs?.groups?.firstOrNull { it.label == "Operation Cost" }?.actualYtdRmPerTonne) +
                " · GC: " + number(costs?.groups?.firstOrNull { it.label == "General Charges" }?.actualYtdRmPerTonne) +
                "\n" + (costs?.let { "YTD through " + label(it.actualPeriod) } ?: "YTD " + today.year))
    )
}

data class OperationalMetric(val label: String, val value: String, val unit: String)
data class OperationalCard(val title: String, val metrics: List<OperationalMetric>, val period: String)

fun operationalDashboardCards(update: EstatePackage?, today: LocalDate): List<OperationalCard> {
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
        OperationalCard("Crop Production", listOf(
            OperationalMetric(label(month.minusMonths(1)), "Pending", "mt/ha"),
            OperationalMetric(label(month) + " MTD", "Pending", "mt/ha"),
            OperationalMetric("Actual YTD", number(yph?.actualYtdYph), "mt/ha"),
            OperationalMetric("YTD / Year Budget", number(annualBudgetProgress), "%")
        ), yieldPeriod),
        OperationalCard("Harvesting Coverage", listOf(
            OperationalMetric("Today", "Pending", "ha"),
            OperationalMetric("MTD", "Pending", "ha"),
            OperationalMetric("Completed rounds", "Pending", "rounds")
        ), label(month)),
        OperationalCard("Harvester Productivity", listOf(
            OperationalMetric("Harvested crop", "Pending", "mt/worker-day"),
            OperationalMetric("Covered area", "Pending", "ha/worker-day")
        ), "MTD · " + label(month)),
        OperationalCard("Manuring", listOf(
            OperationalMetric("Programme progress", "Pending", "%")
        ), "Reporting period pending"),
        OperationalCard("Weeding", listOf(
            OperationalMetric("Circle", "Pending", "%"),
            OperationalMetric("Selective", "Pending", "%")
        ), "Reporting period pending"),
        OperationalCard("Pest & Disease", listOf(
            OperationalMetric("Affected area", "Pending", "ha"),
            OperationalMetric("Incidence", "Pending", "%")
        ), "Reporting period pending"),
        OperationalCard("Estate Cost", listOf(
            OperationalMetric("Operation Cost", number(costs?.groups?.firstOrNull { it.label == "Operation Cost" }?.actualYtdRmPerTonne), "RM/mt"),
            OperationalMetric("General Charges", number(costs?.groups?.firstOrNull { it.label == "General Charges" }?.actualYtdRmPerTonne), "RM/mt")
        ), costs?.let { "YTD through " + label(it.actualPeriod) } ?: "YTD " + today.year)
    )
}
