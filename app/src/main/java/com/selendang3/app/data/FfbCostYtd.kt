package com.selendang3.app.data

import java.time.YearMonth

data class FfbCostYtd(val period: YearMonth, val actualRmPerTonne: Double?, val budgetRmPerTonne: Double?)

fun estateFfbCostYtd(update: EstatePackage?): FfbCostYtd? {
    val overview = update?.tables?.singleOrNull { it.id == "overview" } ?: return null
    val item = overview.columns.indexOf("Item")
    val details = overview.columns.indexOf("Details")
    if (minOf(item, details) < 0) return null
    fun value(label: String): String? = overview.rows.singleOrNull { it.getOrNull(item) == label }?.getOrNull(details)
    val period = runCatching { YearMonth.parse(value("FFB cost YTD reporting period")) }.getOrNull() ?: return null
    fun rate(label: String): Double? = value(label)?.trim()?.replace(",", "")?.toDoubleOrNull()
        ?.takeIf { it.isFinite() && it >= 0 }
    // Annual budget rates have a different comparison basis and are never substituted for YTD.
    val actual = rate("FFB cost actual YTD (RM/tonne)")
    val budget = rate("FFB cost budget YTD (RM/tonne)")
    return FfbCostYtd(period, actual, budget)
}
