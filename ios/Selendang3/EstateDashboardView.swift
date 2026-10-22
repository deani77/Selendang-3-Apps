import SwiftUI

private struct DashboardSection: Identifiable {
    var id: String { title }
    let title: String
    let color: Color
    let headers: [String]
    let rows: [[String]]
    var chart: String? = nil
}

struct EstateDashboardView: View {
    @ObservedObject private var estate = EstateReferenceStore.shared
    private static let yellow = Color(red: 1, green: 0.75, blue: 0)
    private static let teal = Color(red: 0.29, green: 0.67, blue: 0.76)
    private static let red = Color(red: 0.89, green: 0.12, blue: 0.17)
    private let sections: [DashboardSection] = [
        .init(title: "Age Profile", color: EstateDashboardView.yellow, headers: ["Age category", "Share"],
              rows: [["0–3 years · Immature", "—"], ["7–18 years · Prime", "—"],
                     ["19–25 years · Old", "—"], ["Total area (ha)", "—"]],
              chart: "Palm age distribution"),
        .init(title: "Topography", color: EstateDashboardView.teal, headers: ["Terrain", "Share"],
              rows: [["Flat", "—"], ["Hilly", "—"], ["Steep Hilly", "—"]],
              chart: "Terrain distribution"),
        .init(title: "YPH", color: EstateDashboardView.yellow, headers: ["Measure", "Value"],
              rows: [["Actual (t/ha)", "—"], ["Budget (t/ha)", "—"]],
              chart: "Yearly yield comparison · t/ha"),
        .init(title: "Average SPH", color: EstateDashboardView.yellow, headers: ["Measure", "Value"],
              rows: [["Palms/ha", "—"]]),
        .init(title: "Soil Type", color: EstateDashboardView.teal, headers: ["Soil series", "Share"],
              rows: [["Pending estate records", "—"]]),
        .init(title: "Rainfall", color: EstateDashboardView.teal, headers: ["Measure", "Value"],
              rows: [["Monthly rainfall (mm)", "—"]], chart: "Monthly rainfall comparison · mm"),
        .init(title: "Workforce", color: EstateDashboardView.teal, headers: ["Category", "People"],
              rows: ["Staff", "Local Workers", "Foreign Workers", "Contract Workers", "Total Workforce"].map { [$0, "—"] }),
        .init(title: "Ratios", color: EstateDashboardView.teal, headers: ["Maturity", "Area (ha)", "Workers:ha"],
              rows: [["Immature", "—", "—"], ["Mature", "—", "—"]]),
        .init(title: "Kos FFB/TAN", color: EstateDashboardView.yellow, headers: ["Measure", "Value"],
              rows: [["FFB cost (RM/ton)", "—"]]),
        .init(title: "FFB Quality", color: Color(red: 0.87, green: 0.60, blue: 0.60),
              headers: ["Category", "Value"],
              rows: ["Ripe Bunches (%)", "Unripe (%)", "Empty & Rotten (%)", "Stone/Trash (ton)"].map { [$0, "—"] }),
        .init(title: "Actual versus Budget · YTD", color: EstateDashboardView.red, headers: ["Item", "Actual", "Budget"],
              rows: ["OER (%)", "KER (%)", "External Supply (t)", "LF"].map { [$0, "—", "—"] }),
        .init(title: "Cost Breakdown", color: Color(red: 0.74, green: 0.91, blue: 0.60),
              headers: ["Measure", "Upkeep RM/ha", "Har./Coll. RM/ton", "G.C. RM/ha", "Total FFB RM/ton"],
              rows: ["Budget", "Actual", "%"].map { [$0, "—", "—", "—", "—"] }),
        .init(title: "Checkroll Average", color: EstateDashboardView.yellow, headers: ["Category", "RM/Mdy", "% O/Turn"],
              rows: [["Harvester", "—", "—"], ["Field Worker", "—", "—"]])
    ]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text("DASHBOARD · SELENDANG 3").font(.title2.bold())
                Text(estate.reference?.reportingPeriod ?? "Reporting period: pending")
                Text(estate.reference.map { "Estate revision \($0.revision) is saved offline. View the synced records in Estate Information. iOS dashboard calculations are still pending." } ?? "No estate reference data imported. Use Settings → Sync Now.")
                    .font(.footnote).foregroundStyle(.secondary)
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 300), alignment: .top)], alignment: .leading, spacing: 16) {
                    ForEach(sections) { section in
                        panel(section)
                    }
                }
            }
            .padding()
        }
        .toolbar(.visible, for: .navigationBar)
        .navigationTitle("Estate Dashboard")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func panel(_ section: DashboardSection) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(section.title)
                .font(.headline)
                .foregroundStyle(.black)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(12)
                .background(section.color)
            if let chart = section.chart {
                VStack(spacing: 8) {
                    Text(chart)
                    Text("Awaiting estate data").font(.caption).foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, minHeight: 140)
                .padding(.horizontal, 8)
            }
            ScrollView(.horizontal) {
                VStack(alignment: .leading, spacing: 0) {
                    tableRow(section.headers, bold: true)
                    ForEach(section.rows.indices, id: \.self) { index in
                        Divider()
                        tableRow(section.rows[index], bold: false)
                    }
                }
                .padding(8)
            }
        }
        .background(Color(uiColor: .systemBackground))
        .overlay(Rectangle().stroke(Color.secondary.opacity(0.3)))
    }

    private func tableRow(_ cells: [String], bold: Bool) -> some View {
        HStack(alignment: .top, spacing: 12) {
            ForEach(cells.indices, id: \.self) { index in
                Text(cells[index])
                    .font(.subheadline)
                    .fontWeight(bold ? .semibold : .regular)
                    .frame(width: index == 0 ? 155 : 100, alignment: .leading)
            }
        }
        .padding(.vertical, 8)
    }
}
