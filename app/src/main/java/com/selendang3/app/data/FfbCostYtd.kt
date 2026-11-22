package com.selendang3.app.data

import java.time.YearMonth

data class FfbCostGroup(val label: String, val actualYtdRmPerTonne: Double?, val budgetYearRmPerTonne: Double?)
data class FfbCostComparison(val actualPeriod: YearMonth, val budgetYear: Int, val groups: List<FfbCostGroup>)

fun estateFfbCosts(update: EstatePackage?): FfbCostComparison? {
    val overview = update?.overviewValues() ?: return null
    val period = runCatching { YearMonth.parse(overview["FFB cost YTD reporting period"]) }.getOrNull() ?: return null
    val budgetYear = overview["FFB cost budget year"]?.toIntOrNull()
        ?.takeIf { it in 2000..2100 && it == period.year } ?: return null
    fun rate(label: String): Double? = overview[label]?.trim()?.replace(",", "")?.toDoubleOrNull()
        ?.takeIf { it.isFinite() && it >= 0 }
    // These are separate measures. Never use overall expenses or the source's broader OPERATION COST subtotal.
    val groups = listOf(
        FfbCostGroup("Operation Cost", rate("FFB cost operation actual YTD (RM/tonne)"),
            rate("FFB cost operation budget year (RM/tonne)")),
        FfbCostGroup("General Charges", rate("FFB cost general charges actual YTD (RM/tonne)"),
            rate("FFB cost general charges budget year (RM/tonne)"))
    )
    return FfbCostComparison(period, budgetYear, groups)
}
