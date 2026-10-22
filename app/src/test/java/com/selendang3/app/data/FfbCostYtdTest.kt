package com.selendang3.app.data

import org.junit.Assert.*
import org.junit.Test

class FfbCostYtdTest {
    private fun fixture(actual: String? = "123.45", budget: String? = null) = EstatePackage(1,"Test","",listOf(
        EstateTable("overview",listOf("Item","Details"),listOf(
            listOf("FFB cost YTD reporting period","2026-09"),
            listOf("FFB cost actual YTD (RM/tonne)",actual),
            listOf("FFB cost budget YTD (RM/tonne)",budget),
            listOf("FFB cost annual budget (RM/tonne)","90.00")
        ))
    ),"")
    @Test fun annualBudgetIsNotSubstitutedForMissingYtdBudget() {
        val result = estateFfbCostYtd(fixture())!!
        assertEquals(123.45,result.actualRmPerTonne!!,0.0)
        assertNull(result.budgetRmPerTonne)
        assertEquals(9,result.period.monthValue)
    }
    @Test fun matchedYtdBudgetAndConfirmedZeroRemainValid() {
        val result = estateFfbCostYtd(fixture("0","111.22"))!!
        assertEquals(0.0,result.actualRmPerTonne!!,0.0)
        assertEquals(111.22,result.budgetRmPerTonne!!,0.0)
    }
    @Test fun invalidRatesStayPendingWithoutInventingValues() {
        for (value in listOf(null,"-1","NaN","Infinity","Pending")) {
            val result = estateFfbCostYtd(fixture(value,value))!!
            assertNull(result.actualRmPerTonne); assertNull(result.budgetRmPerTonne)
        }
        assertNull(estateFfbCostYtd(null))
    }
}
