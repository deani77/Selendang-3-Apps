package com.selendang3.app.data

import org.junit.Assert.*
import org.junit.Test

class PlanterCalculationsTest {
    @Test fun geometricDensityAndFeetConversion() {
        assertEquals(100.0, PlanterCalculations.density(10.0, PlantingDistanceUnit.METRES, PlantingPattern.SQUARE)!!.palmsPerHectare, 1e-8)
        assertEquals(142.5556220221309, PlanterCalculations.density(9.0, PlantingDistanceUnit.METRES, PlantingPattern.TRIANGULAR)!!.palmsPerHectare, 1e-8)
        assertEquals(PlanterCalculations.density(9.144, PlantingDistanceUnit.METRES, PlantingPattern.TRIANGULAR)!!.palmsPerHectare,
            PlanterCalculations.density(30.0, PlantingDistanceUnit.FEET, PlantingPattern.TRIANGULAR)!!.palmsPerHectare, 1e-8)
    }
    @Test fun productRateUnitsAndCalibratedCoverage() {
        val result = PlanterCalculations.dose(2.0, ProductRateUnit.LITRES, 16.0, 200.0)!!
        assertEquals(160.0, result.productPerPump, 1e-8)
        assertEquals(0.08, result.hectaresPerPump, 1e-8)
        assertEquals(12.5, result.pumpsPerHectare, 1e-8)
        assertEquals(160.0, PlanterCalculations.dose(2000.0, ProductRateUnit.MILLILITRES, 16.0, 200.0)!!.productPerPump, 1e-8)
        assertEquals(75.0, PlanterCalculations.dose(1.5, ProductRateUnit.KILOGRAMS, 20.0, 400.0)!!.productPerPump, 1e-8)
        assertEquals(75.0, PlanterCalculations.dose(1500.0, ProductRateUnit.GRAMS, 20.0, 400.0)!!.productPerPump, 1e-8)
    }
    @Test fun invalidVolumesAndImpossibleLiquidMixturesAreRejected() {
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { value ->
            assertNull(PlanterCalculations.density(value, PlantingDistanceUnit.FEET, PlantingPattern.TRIANGULAR))
            assertNull(PlanterCalculations.dose(value, ProductRateUnit.LITRES, 16.0, 200.0))
            assertNull(PlanterCalculations.dose(2.0, ProductRateUnit.LITRES, value, 200.0))
            assertNull(PlanterCalculations.dose(2.0, ProductRateUnit.LITRES, 16.0, value))
        }
        assertNull(PlanterCalculations.dose(201.0, ProductRateUnit.LITRES, 16.0, 200.0))
        assertEquals(9.5, PlanterCalculations.input("9,5")!!, 0.0)
        assertEquals(0.5, PlanterCalculations.input(".5")!!, 0.0)
        listOf("", "0", "-1", "nan", "infinity", "2,000.50").forEach { assertNull(PlanterCalculations.input(it)) }
        assertNotEquals("0.00", PlanterCalculations.number(0.00001))
    }
}
