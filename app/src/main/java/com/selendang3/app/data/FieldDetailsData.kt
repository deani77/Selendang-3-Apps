package com.selendang3.app.data

fun estateFieldCodes(update: EstatePackage?): List<String> {
    val table = update?.tables?.firstOrNull { it.id == "fields" } ?: return emptyList()
    val column = table.columns.indexOf("Field code")
    if (column < 0) return emptyList()
    return table.rows.mapNotNull { it.getOrNull(column)?.trim()?.takeIf(String::isNotEmpty) }.distinct().sorted()
}

fun estateFieldTables(update: EstatePackage?, code: String): List<EstateTable> {
    if (code !in estateFieldCodes(update)) return emptyList()
    return update?.tables.orEmpty().mapNotNull { table ->
        val column = table.columns.indexOfFirst { it in listOf("Field code", "Field / category", "Field") }
        if (column < 0) return@mapNotNull null
        val rows = table.rows.filter { it.getOrNull(column)?.trim() == code }
        if (rows.isEmpty()) null else table.copy(rows = rows)
    }
}
