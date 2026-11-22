package com.selendang3.app.data

/** Exact source labels; duplicate tables or rows remain unavailable rather than choosing a value. */
internal class OverviewValues(
    private val table: EstateTable,
    private val itemColumn: Int,
    private val detailsColumn: Int,
) {
    operator fun get(label: String): String? =
        table.rows.singleOrNull { it.getOrNull(itemColumn) == label }?.getOrNull(detailsColumn)
}

internal fun EstatePackage.overviewValues(): OverviewValues? {
    val table = tables.singleOrNull { it.id == "overview" } ?: return null
    val item = table.columns.indexOf("Item")
    val details = table.columns.indexOf("Details")
    if (minOf(item, details) < 0) return null
    return OverviewValues(table, item, details)
}
