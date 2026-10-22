package com.selendang3.app.data

import java.time.YearMonth

data class EstateWorkforce(val period: YearMonth, val staff: Int, val localWorkers: Int,
    val foreignWorkers: Int, val contractWorkers: Int?, val totalWorkers: Int) {
    // Worker nationality counts already include all reported worker roles.
    val totalWorkforce: Int get() = staff + totalWorkers
}

fun estateWorkforce(update: EstatePackage?): EstateWorkforce? {
    val overview = update?.tables?.singleOrNull { it.id == "overview" } ?: return null
    val item = overview.columns.indexOf("Item")
    val details = overview.columns.indexOf("Details")
    if (minOf(item, details) < 0) return null
    fun value(label: String): String? = overview.rows.singleOrNull { it.getOrNull(item) == label }?.getOrNull(details)
    fun count(label: String): Int? = value(label)?.trim()?.toIntOrNull()?.takeIf { it in 0..1000000 }
    val period = runCatching { YearMonth.parse(value("Workforce reporting period")) }.getOrNull() ?: return null
    val staff = count("Workforce staff") ?: return null
    val local = count("Workforce local workers") ?: return null
    val foreign = count("Workforce foreign workers") ?: return null
    val workers = count("Workforce total workers") ?: return null
    if (local + foreign != workers) return null
    val contractors = count("Workforce contract workers")
    if (contractors != null && contractors > workers) return null
    return EstateWorkforce(period, staff, local, foreign, contractors, workers)
}
