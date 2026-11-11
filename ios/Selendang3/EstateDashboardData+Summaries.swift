import Foundation

// Values shared by the home summary and operational cards. Detailed estate
// panels retain their own reporting-period rules in EstateDashboardData.swift.
private struct DashboardSummaryValues {
    let previousMonth: String
    let currentMonth: String
    let ytdYield: Double?
    let annualBudgetProgress: Double?
    let yieldPeriod: String
    let operationCost: Double?
    let generalCharges: Double?
    let costLabel: String
}

extension EstateDashboardData {
    var reportCalendar: Calendar {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Asia/Kuala_Lumpur")!
        return calendar
    }
    func reportDate(_ text: String) -> Date? {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.calendar = reportCalendar; formatter.timeZone = reportCalendar.timeZone
        formatter.dateFormat = "yyyy-MM-dd"; formatter.isLenient = false
        guard let date = formatter.date(from: text), formatter.string(from: date) == text,
              date <= reportCalendar.startOfDay(for: today) else { return nil }
        return date
    }
    func nonnegative(_ value: Double?) -> Double? {
        guard let value = value, value.isFinite, value >= 0 else { return nil }
        return value
    }
    private var currentCrop: EstateCropReport? {
        guard let report = reference?.cropProductionReport, let date = reportDate(report.reportDate),
              reportCalendar.isDate(date, equalTo: today, toGranularity: .month) else { return nil }
        return report
    }
    private var previousCrop: EstateCropReport? {
        let start = reportCalendar.date(from: reportCalendar.dateComponents([.year, .month], from: today))!
        let monthEnd = reportCalendar.date(byAdding: .day, value: -1, to: start)!
        let reports = ([reference?.cropProductionReport].compactMap { $0 } + (reference?.cropProductionReportHistory ?? []))
            .filter { report in
                guard report.isCompleteMonth == true, let date = reportDate(report.reportDate) else { return false }
                return reportCalendar.isDate(date, inSameDayAs: monthEnd)
            }
        return reports.count == 1 ? reports[0] : nil
    }
    private var manuringProgress: Double? {
        guard let report = reference?.manuringReport, let date = reportDate(report.reportDate),
              reportCalendar.component(.year, from: date) == reportCalendar.component(.year, from: today),
              let applied = nonnegative(report.appliedYtdMt), let programme = nonnegative(report.annualProgrammeMt),
              programme > 0 else { return nil }
        return nonnegative(applied / programme * 100)
    }
    private var manuringNote: String {
        guard manuringProgress != nil, let report = reference?.manuringReport else { return "Reporting period pending" }
        return "YTD through " + report.reportDate + " · Applied mt / annual programme mt"
    }

    private var summaryValues: DashboardSummaryValues {
        let calendar = reportCalendar
        let year = calendar.component(.year, from: today)
        let month = calendar.component(.month, from: today)
        let current = String(format: "%04d-%02d", year, month)
        let formatter = DateFormatter()
        formatter.calendar = calendar; formatter.timeZone = calendar.timeZone
        formatter.locale = Locale(identifier: "en_US_POSIX"); formatter.dateFormat = "MMM yyyy"
        let monthStart = calendar.date(from: calendar.dateComponents([.year, .month], from: today))!
        let previous = calendar.date(byAdding: .month, value: -1, to: monthStart)!
        var ytd: Double?, annualBudgetProgress: Double?, yieldPeriod = "YTD " + String(year)
        if let source = reference?.yph, source.reportingYear == year,
           (1...month).contains(source.throughMonth), source.matureAreaHa.isFinite, source.matureAreaHa > 0,
           let tonnes = source.actualYtdTonnes, tonnes.isFinite, tonnes >= 0 {
            ytd = tonnes / source.matureAreaHa
            if let budget = source.fullYearBudgetTonnes, budget.isFinite, budget > 0 {
                let percent = tonnes / budget * 100
                if percent.isFinite { annualBudgetProgress = percent }
            }
            yieldPeriod = "YTD through " + formatter.string(from: calendar.date(from: DateComponents(year: year, month: source.throughMonth, day: 1))!)
        }
        let costPeriod = period("FFB cost YTD reporting period")
        let costValid = costPeriod != nil && costPeriod!.hasPrefix(String(year) + "-") && costPeriod! <= current &&
            overview("FFB cost budget year") == String(year)
        let operation = costValid ? numeric(overview("FFB cost operation actual YTD (RM/tonne)")) : nil
        let gc = costValid ? numeric(overview("FFB cost general charges actual YTD (RM/tonne)")) : nil
        var costLabel = "YTD " + String(year)
        if costValid, let date = ISO8601DateFormatter().date(from: costPeriod! + "-01T00:00:00Z") {
            costLabel = "YTD through " + formatter.string(from: date)
        }
        return DashboardSummaryValues(
            previousMonth: formatter.string(from: previous),
            currentMonth: formatter.string(from: today),
            ytdYield: ytd,
            annualBudgetProgress: annualBudgetProgress,
            yieldPeriod: yieldPeriod,
            operationCost: operation,
            generalCharges: gc,
            costLabel: costLabel
        )
    }

    // Sparse daily drafts are not substituted for complete estate monthly totals.
    var homeRows: [EstateDashboardPanel] {
        let summary = summaryValues
        return [
            EstateDashboardPanel(title: "Crop data · t/ha", headers: [], rows: [],
                note: summary.previousMonth + ": " + Self.number(nonnegative(previousCrop?.totals.sentToMillMtdMtPerHa)) + " · " + summary.currentMonth + " MTD: " + Self.number(nonnegative(currentCrop?.totals.sentToMillMtdMtPerHa)) + "\n" +
                    summary.yieldPeriod + ": " + Self.number(summary.ytdYield) + "\nYTD vs Year Budget: " +
                    (summary.annualBudgetProgress.map { Self.number($0) + "%" } ?? "Pending")),
            EstateDashboardPanel(title: "Upkeep and Manuring · %", headers: [], rows: [],
                note: "Weeding: Pending · Manuring: " + (manuringProgress.map { Self.number($0) + "%" } ?? "Pending")),
            EstateDashboardPanel(title: "Estate cost · Actual YTD · RM/tonne", headers: [], rows: [],
                note: "Operation: " + Self.number(summary.operationCost) + " · GC: " + Self.number(summary.generalCharges) + "\n" + summary.costLabel)
        ]
    }

    // Uses the same source periods and validation as the home summaries.
    var operationalCards: [EstateDashboardPanel] {
        let summary = summaryValues
        return [
            EstateDashboardPanel(title: "Crop Production", headers: [], rows: [
                [summary.previousMonth, Self.number(nonnegative(previousCrop?.totals.sentToMillMtdMtPerHa)), "mt/ha"],
                [summary.currentMonth + " MTD", Self.number(nonnegative(currentCrop?.totals.sentToMillMtdMtPerHa)), "mt/ha"],
                ["Actual YTD", Self.number(summary.ytdYield), "mt/ha"],
                ["YTD / Year Budget", Self.number(summary.annualBudgetProgress), "%"]
            ], note: summary.yieldPeriod + (currentCrop.map { " · MTD through " + $0.reportDate } ?? "")),
            EstateDashboardPanel(title: "Harvesting Coverage", headers: [], rows: [
                ["Hectare Completed", Self.number(nonnegative(currentCrop?.totals.hectaresMtd)), "ha"],
                ["Completed rounds", Self.number(nonnegative(currentCrop?.totals.roundsMtd)), "rounds"]
            ], note: currentCrop.map { "Report: " + $0.reportDate } ?? "Reporting period pending"),
            EstateDashboardPanel(title: "Harvester Productivity", headers: [], rows: [
                ["Tonnes per mandays", Self.number(nonnegative(currentCrop?.totals.mtPerHarvesterDayMtd)), "mt/manday"],
                ["Hectare per mandays", Self.number(nonnegative(currentCrop?.totals.haPerHarvesterDayMtd)), "ha/manday"]
            ], note: currentCrop.map { "MTD through " + $0.reportDate } ?? "Reporting period pending"),
            EstateDashboardPanel(title: "Manuring", headers: [], rows: [
                ["Programme progress", Self.number(manuringProgress), "%"]
            ], note: manuringNote),
            EstateDashboardPanel(title: "Weeding", headers: [], rows: [
                ["Circle", "Pending", "%"], ["Selective", "Pending", "%"]
            ], note: "Reporting period pending"),
            EstateDashboardPanel(title: "Pest & Disease", headers: [], rows: [
                ["Affected area", "Pending", "ha"], ["Incidence", "Pending", "%"]
            ], note: "Reporting period pending"),
            EstateDashboardPanel(title: "Estate Cost", headers: [], rows: [
                ["Operation Cost", Self.number(summary.operationCost), "RM/mt"],
                ["General Charges", Self.number(summary.generalCharges), "RM/mt"]
            ], note: summary.costLabel)

        ]
    }

}
