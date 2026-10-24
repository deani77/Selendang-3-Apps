import Foundation

enum PlantingPattern: String, CaseIterable { case triangular = "Triangular", square = "Square" }
enum PlantingDistanceUnit: String, CaseIterable { case metres = "Metres", feet = "Feet" }
enum ProductRateUnit: String, CaseIterable {
    case litres = "L/ha", millilitres = "mL/ha", kilograms = "kg/ha", grams = "g/ha"
    var liquid: Bool { self == .litres || self == .millilitres }
    var factor: Double { self == .litres || self == .kilograms ? 1000 : 1 }
    var doseUnit: String { liquid ? "mL/pump" : "g/pump" }
}
struct PlantDensityResult {
    let metres: Double
    let rowSpacingMetres: Double
    let palmsPerHectare: Double
}
struct PumpDoseResult {
    let productPerPump: Double
    let hectaresPerPump: Double
    let pumpsPerHectare: Double
}
enum PlanterCalculations {
    static func input(_ text: String) -> Double? {
        let clean = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard clean.range(of: "^([0-9]+([.,][0-9]*)?|[.,][0-9]+)$", options: .regularExpression) != nil,
              let value = Double(clean.replacingOccurrences(of: ",", with: ".")), value.isFinite, value > 0 else { return nil }
        return value
    }
    static func density(distance: Double, unit: PlantingDistanceUnit, pattern: PlantingPattern) -> PlantDensityResult? {
        guard distance.isFinite, distance > 0 else { return nil }
        let metres = distance * (unit == .feet ? 0.3048 : 1)
        let rowSpacing = metres * (pattern == .triangular ? sqrt(3) / 2 : 1)
        let density = 10000 / (metres * rowSpacing)
        guard metres.isFinite, rowSpacing.isFinite, rowSpacing > 0, density.isFinite, density > 0 else { return nil }
        return PlantDensityResult(metres: metres, rowSpacingMetres: rowSpacing, palmsPerHectare: density)
    }
    static func dose(rate: Double, unit: ProductRateUnit, tankLitres: Double, sprayLitresPerHectare: Double) -> PumpDoseResult? {
        guard [rate, tankLitres, sprayLitresPerHectare].allSatisfy({ $0.isFinite && $0 > 0 }) else { return nil }
        let coverage = tankLitres / sprayLitresPerHectare
        let dose = rate * unit.factor * coverage
        let pumps = sprayLitresPerHectare / tankLitres
        guard [coverage, dose, pumps].allSatisfy({ $0.isFinite && $0 > 0 }),
              !unit.liquid || dose / 1000 <= tankLitres else { return nil }
        return PumpDoseResult(productPerPump: dose, hectaresPerPump: coverage, pumpsPerHectare: pumps)
    }
    static func number(_ value: Double) -> String {
        String(format: value < 0.01 || value >= 1e9 ? "%.6g" : "%.2f", locale: Locale(identifier: "en_US_POSIX"), value)
    }
}
