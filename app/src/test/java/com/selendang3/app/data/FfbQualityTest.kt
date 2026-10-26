package com.selendang3.app.data

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class FfbQualityTest {
    private val today = LocalDate.of(2026, 10, 26)
    private fun fixture(month: Any = 1.25, year: Any = JSONObject.NULL): EstatePackage {
        val totals = JSONObject().put("rejectMtdMt",12).put("rejectMtdPercent",month)
            .put("rejectYtdMt",99).put("rejectYtdPercent",year)
        val root = JSONObject().put("ffbRejectReport", JSONObject().put("reportDate","2026-10-21").put("totals",totals))
        return EstatePackage(1,"Example","",emptyList(),root.toString())
    }
    @Test fun usesSourcePercentagesWithoutInventingYearDenominator() {
        val quality = estateFfbQuality(fixture(),today)
        assertEquals(listOf("Reject","1.25","Pending"),quality.rows[3])
        assertTrue(quality.rows.take(3).all { it[1] == "Pending" && it[2] == "Pending" })
        assertTrue(quality.note.contains("99.00 mt"))
        assertEquals("Pending",estateFfbQuality(fixture(),LocalDate.of(2026,11,1)).rows[3][1])
        assertEquals("Awaiting approved FFB quality records.",estateFfbQuality(fixture(),LocalDate.of(2026,8,1)).note)
    }
    @Test fun preservesZeroAndRejectsInvalidOrStalePercentages() {
        assertEquals(listOf("Reject","0.00","2.50"),estateFfbQuality(fixture(0,2.5),today).rows[3])
        assertEquals("Pending",estateFfbQuality(fixture(0,2.5),LocalDate.of(2027,1,1)).rows[3][2])
        assertEquals(listOf("Reject","Pending","Pending"),estateFfbQuality(fixture(101,-1),today).rows[3])
    }
}
