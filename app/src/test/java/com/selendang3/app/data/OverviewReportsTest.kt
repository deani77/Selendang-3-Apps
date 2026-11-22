package com.selendang3.app.data

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class OverviewReportsTest {
    private val today = LocalDate.of(2026, 10, 22)
    private val overview = EstateTable("overview", listOf("Item", "Details"), listOf(
        listOf("Estate area reporting period", "2026-09"),
        listOf("Perimeter area (ha)", "100"), listOf("Mature area (ha)", "100"),
        listOf("Workforce reporting period", "2026-09"), listOf("Workforce staff", "2"),
        listOf("Workforce local workers", "7"), listOf("Workforce foreign workers", "3"),
        listOf("Workforce total workers", "10"), listOf("Workforce contract workers", "0"),
        listOf("Ratios reporting period", "2026-09"), listOf("Ratios immature area (ha)", "0"),
        listOf("Ratios mature area (ha)", "100"), listOf("Ratios mature operations workers", "8"),
        listOf("FFB cost YTD reporting period", "2026-09"), listOf("FFB cost budget year", "2026"),
        listOf("FFB cost operation actual YTD (RM/tonne)", "0"),
    ))
    private fun fixture(tables: List<EstateTable>) = EstatePackage(1, "Fictional", "", tables +
        EstateTable("fields", listOf("Area (ha)"), listOf(listOf("100"))), "")

    @Test fun reorderedOverviewColumnsKeepEveryReportAndMeasuredZero() {
        val reordered = overview.copy(columns = overview.columns.reversed(), rows = overview.rows.map { it.reversed() })
        val normal = fixture(listOf(overview))
        val input = fixture(listOf(reordered))
        assertEquals(estateAreaSummary(normal, today), estateAreaSummary(input, today))
        assertEquals(estateWorkforce(normal), estateWorkforce(input))
        assertEquals(estateWorkerRatios(normal), estateWorkerRatios(input))
        assertEquals(estateFfbCosts(normal), estateFfbCosts(input))
        assertEquals(12, estateWorkforce(input)!!.totalWorkforce!!)
        assertEquals(0.0, estateWorkerRatios(input)!!.immatureArea, 0.0)
        assertEquals(0.0, estateFfbCosts(input)!!.groups.first().actualYtdRmPerTonne!!, 0.0)
        assertNull(estateFfbCosts(input)!!.groups.last().actualYtdRmPerTonne)
    }

    @Test fun missingOrAmbiguousOverviewCannotSupplyAnyReport() {
        val variants = listOf(
            emptyList(),
            listOf(overview, overview),
            listOf(overview.copy(columns = listOf("Item", "Other"))),
            listOf(overview.copy(rows = overview.rows + overview.rows)),
            listOf(overview.copy(rows = overview.rows.map { it.take(1) })),
        )
        for (tables in variants) {
            val input = fixture(tables)
            assertNull(estateAreaSummary(input, today))
            assertNull(estateWorkforce(input))
            assertNull(estateWorkerRatios(input))
            assertNull(estateFfbCosts(input))
        }
    }
}
