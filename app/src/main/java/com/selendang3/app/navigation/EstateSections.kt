package com.selendang3.app.navigation

data class Section(val route: String, val title: String, val description: String)

val estateSections = listOf(
    Section("overview", "Estate Overview", "Estate profile, location, cultivation and maturity status."),
    Section("fields", "Field Details", "Field codes, hectarage, palm population and density."),
    Section("ages", "Palm Age Profile", "Palm ages by field and age-category distribution."),
    Section("soil", "Soil & Topography", "Soil composition, terrain and corresponding areas."),
    Section("rainfall", "Rainfall", "Monthly rainfall, rainy days and historical comparisons."),
    Section("abw", "Average Bunch Weight", "Current-period and cumulative ABW in kg/bunch, with year-on-year comparison. Field-level figures are pending the supporting Excel file."),
    Section("roads", "Roads & Infrastructure", "Main, secondary and subsidiary road specifications."),
)

val mainSections = listOf(
    Section("information", "Estate Details", "Estate metrics, rainfall, workforce and costs"),
    Section("production", "Field Details", "Field dashboard and records by field."),
    Section("upkeep", "Upkeep and Cultivation", "Estate work dashboard, excluding crop production."),
    Section("reports", "Crop Production", "Field production, daily drafts and period totals."),
    Section("settings", "Settings", "Offline data synchronization"),
    Section("tools", "Planter Tools", "Offline plantation calculators."),
)

internal fun estateRouteTitle(route: String): String = when (route) {
    "home" -> "Planter’s Log"
    "dashboard" -> "Estate Dashboard"
    "estate-records" -> "All Estate Records"
    else -> (mainSections + estateSections).firstOrNull { it.route == route }?.title ?: "Estate Details"
}
