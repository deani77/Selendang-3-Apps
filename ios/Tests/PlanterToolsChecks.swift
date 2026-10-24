import Foundation
func close(_ actual: Double?, _ expected: Double) { precondition(actual != nil && abs(actual! - expected) < 1e-8) }
close(PlanterCalculations.density(distance: 10, unit: .metres, pattern: .square)?.palmsPerHectare, 100)
close(PlanterCalculations.density(distance: 9, unit: .metres, pattern: .triangular)?.palmsPerHectare, 142.5556220221309)
let feet = PlanterCalculations.density(distance: 30, unit: .feet, pattern: .triangular)!
let metres = PlanterCalculations.density(distance: 9.144, unit: .metres, pattern: .triangular)!
close(feet.palmsPerHectare, metres.palmsPerHectare)
let liquid = PlanterCalculations.dose(rate: 2, unit: .litres, tankLitres: 16, sprayLitresPerHectare: 200)!
close(liquid.productPerPump, 160); close(liquid.hectaresPerPump, 0.08); close(liquid.pumpsPerHectare, 12.5)
close(PlanterCalculations.dose(rate: 2000, unit: .millilitres, tankLitres: 16, sprayLitresPerHectare: 200)?.productPerPump, 160)
close(PlanterCalculations.dose(rate: 1.5, unit: .kilograms, tankLitres: 20, sprayLitresPerHectare: 400)?.productPerPump, 75)
close(PlanterCalculations.dose(rate: 1500, unit: .grams, tankLitres: 20, sprayLitresPerHectare: 400)?.productPerPump, 75)
for value in [0.0, -1.0, Double.infinity, Double.nan] {
    precondition(PlanterCalculations.density(distance: value, unit: .feet, pattern: .triangular) == nil)
    precondition(PlanterCalculations.dose(rate: value, unit: .litres, tankLitres: 16, sprayLitresPerHectare: 200) == nil)
    precondition(PlanterCalculations.dose(rate: 2, unit: .litres, tankLitres: value, sprayLitresPerHectare: 200) == nil)
    precondition(PlanterCalculations.dose(rate: 2, unit: .litres, tankLitres: 16, sprayLitresPerHectare: value) == nil)
}
precondition(PlanterCalculations.dose(rate: 201, unit: .litres, tankLitres: 16, sprayLitresPerHectare: 200) == nil)
precondition(PlanterCalculations.input("9,5") == 9.5)
for text in ["", "0", "-1", "nan", "infinity", "2,000.50"] { precondition(PlanterCalculations.input(text) == nil) }
precondition(PlanterCalculations.number(0.00001) != "0.00")
print("Planter calculators: geometry, feet conversion, rate-unit conversions, tank coverage and invalid-input checks passed")
