package com.selendang3.app.data
import org.junit.Assert.*
import org.junit.Test

class FieldDetailsDataTest {
    private val input = EstatePackage(1, "Fictional", "", listOf(
        EstateTable("fields", listOf("Field code", "Area (ha)"), listOf(listOf("A", "10"), listOf("B", "20"))),
        EstateTable("ages", listOf("Field / category", "Age / years"), listOf(listOf("A", "3 years"), listOf("B", "8 years"))),
        EstateTable("overview", listOf("Item", "Details"), listOf(listOf("Source", "A")))
    ), "")
    @Test fun fieldButtonsUseSavedCodesAndDetailsStayIsolatedToSelectedField() {
        assertEquals(listOf("A", "B"), estateFieldCodes(input))
        val tables = estateFieldTables(input, "A")
        assertEquals(listOf("fields", "ages"), tables.map { it.id })
        assertTrue(tables.all { it.rows.size == 1 && it.rows[0][0] == "A" })
        assertTrue(estateFieldTables(input, "UNKNOWN").isEmpty())
        assertTrue(estateFieldCodes(null).isEmpty())
    }
}
