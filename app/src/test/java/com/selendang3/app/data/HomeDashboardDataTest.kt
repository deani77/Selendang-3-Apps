package com.selendang3.app.data

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class HomeDashboardDataTest {
    private fun fixture(year: Int = 2026, month: Int = 9, area: Double = 1000.0) =
        EstatePackage(1, "Fictional", "", listOf(EstateTable("overview", listOf("Item", "Details"), listOf(
            listOf("FFB cost YTD reporting period", "%04d-%02d".format(year, month)),
            listOf("FFB cost budget year", year.toString()),
            listOf("FFB cost operation actual YTD (RM/tonne)", "123"),
            listOf("FFB cost operation budget year (RM/tonne)", "100"),
            listOf("FFB cost general charges actual YTD (RM/tonne)", "45"),
            listOf("FFB cost general charges budget year (RM/tonne)", "40")
        ))), "", YphData(year, month, area, 25000.0, 20000.0, 30000.0, emptyList()))
    private val today = LocalDate.of(2026, 10, 22)
    @Test fun usesActualYtdWithExplicitPeriodAndDoesNotInventMonthlyOrProgressData() {
        val rows = homeDashboardRows(fixture(), today)
        assertEquals(3, rows.size)
        assertTrue(rows[0].detail.contains("Sep 2026: Pending"))
        assertTrue(rows[0].detail.contains("Oct 2026 MTD: Pending"))
        assertTrue(rows[0].detail.contains("YTD through Sep 2026: 25.00"))
        assertTrue(rows[1].detail.contains("Weeding: Pending"))
        assertTrue(rows[1].detail.contains("Manuring: Pending"))
        assertTrue(rows[2].detail.contains("Operation: 123.00 · GC: 45.00"))
        assertFalse(rows[2].detail.contains("100.00"))
    }
    @Test fun missingFutureAndOldYearRecordsRemainPending() {
        for (source in listOf(null, fixture(2025), fixture(2026, 11))) {
            val rows = homeDashboardRows(source, today)
            assertTrue(rows[0].detail.endsWith("YTD 2026: Pending"))
            assertTrue(rows[2].detail.contains("Operation: Pending · GC: Pending"))
        }
        assertTrue(homeDashboardRows(fixture(area = 0.0), today)[0].detail.endsWith("Pending"))
    }
    @Test fun januaryUsesDecemberOfPreviousYearAndHidesLastYearsYtd() {
        val rows = homeDashboardRows(fixture(), LocalDate.of(2027, 1, 1))
        assertTrue(rows[0].detail.contains("Dec 2026: Pending"))
        assertTrue(rows[0].detail.contains("Jan 2027 MTD: Pending"))
        assertTrue(rows[0].detail.endsWith("YTD 2027: Pending"))
    }
}
