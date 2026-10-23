import SwiftUI

struct HomeDashboardScreen: View {
    private var isPad: Bool { UIDevice.current.userInterfaceIdiom == .pad }
    var body: some View {
        Group {
            if isPad {
                ScrollView { HomeDashboardSummaryView(horizontal: true).padding(20) }
            } else {
                OperationalDashboardGrid()
            }
        }
        .estateScreen()
        .navigationTitle("Estate Dashboard")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.visible, for: .navigationBar)
    }
}

// Both dashboard layouts read the same saved estate snapshot.
struct HomeDashboardSummaryView: View {
    let horizontal: Bool
    @ObservedObject private var estate = EstateReferenceStore.shared
    private var isPad: Bool { UIDevice.current.userInterfaceIdiom == .pad }
    @ScaledMetric(relativeTo: .title2) private var iPadSummaryHeaderHeight: CGFloat = 80
    var body: some View {
        TimelineView(.periodic(from: Date(), by: 60)) { context in
            let rows = EstateDashboardData(reference: estate.reference, today: context.date).homeRows
            Group {
                if horizontal {
                    HStack(alignment: .top, spacing: 16) {
                        ForEach(Array(rows.enumerated()), id: \.element.id) { index, row in
                            if index > 0 { Divider().overlay(Color.white.opacity(0.35)) }
                            summaryCell(row).frame(maxWidth: .infinity, alignment: .top)
                        }
                    }.fixedSize(horizontal: false, vertical: true)
                } else {
                    VStack(alignment: .leading, spacing: 16) {
                        ForEach(Array(rows.enumerated()), id: \.element.id) { index, row in
                            if index > 0 { Divider().overlay(Color.white.opacity(0.35)) }
                            summaryCell(row)
                        }
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .topLeading)
            .padding(16)
        }
        .background(EstateTheme.hero)
        .foregroundStyle(.white)
        .clipShape(RoundedRectangle(cornerRadius: 24))
    }

    private func summaryTitle(_ row: EstateDashboardPanel) -> String {
        switch row.title {
        case "Crop data · t/ha": return "Crop Data"
        case "Upkeep and Manuring · %": return "Upkeep and Manuring"
        case "Estate cost · Actual YTD · RM/tonne": return "Estate Cost"
        default: return row.title
        }
    }

    private func summaryUnit(_ row: EstateDashboardPanel) -> String {
        switch row.title {
        case "Crop data · t/ha": return "(mt/ha)"
        case "Upkeep and Manuring · %": return "(%)"
        case "Estate cost · Actual YTD · RM/tonne": return "(RM/mt)"
        default: return ""
        }
    }

    private func summaryCell(_ row: EstateDashboardPanel) -> some View {
        VStack(alignment: isPad ? .center : .leading, spacing: isPad ? 8 : 4) {
            if isPad {
                VStack(spacing: 2) {
                    Text(summaryTitle(row)).font(.title2.bold()).lineLimit(2)
                    Text(summaryUnit(row)).font(.subheadline)
                }
                .frame(maxWidth: .infinity, minHeight: iPadSummaryHeaderHeight, alignment: .top)
                .accessibilityElement(children: .combine)
            } else {
                HStack(alignment: .firstTextBaseline, spacing: 6) {
                    Text(summaryTitle(row)).font(.subheadline.bold())
                    Text(summaryUnit(row)).font(.caption)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityElement(children: .combine)
            }
            Text(row.note.replacingOccurrences(of: " · ", with: "\n"))
                .font(isPad ? .body : .footnote)
        }
        .multilineTextAlignment(isPad ? .center : .leading)
    }
}

// The phone hierarchy groups crop operations, cultivation and estate costs.
// Larger accessibility text retains scroll access.
private struct OperationalDashboardGrid: View {
    @ObservedObject private var estate = EstateReferenceStore.shared
    private let green = EstateTheme.accent

    var body: some View {
        ScrollView {
            TimelineView(.periodic(from: Date(), by: 60)) { context in
                let cards = EstateDashboardData(reference: estate.reference, today: context.date).operationalCards
                VStack(spacing: 10) {
                    dashboardCard(cards[0], symbol: "leaf.fill", wide: true)
                    HStack(alignment: .top, spacing: 10) {
                        dashboardCard(cards[1], symbol: "square.grid.3x3.fill", height: 144)
                        dashboardCard(cards[2], symbol: "person.fill", height: 144)
                    }
                    HStack(alignment: .top, spacing: 10) {
                        dashboardCard(cards[3], symbol: "leaf.circle.fill", height: 126)
                        dashboardCard(cards[4], symbol: "sprinkler.and.droplets.fill", height: 126)
                    }
                    dashboardCard(cards[5], symbol: "ant.fill", wide: true)
                    dashboardCard(cards[6], symbol: "banknote.fill", wide: true)
                }
                .padding(12)
            }
        }
        .background { EstateBackground() }
    }

    private func dashboardCard(_ card: EstateDashboardPanel, symbol: String, height: CGFloat = 0, wide: Bool = false) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .firstTextBaseline, spacing: 6) {
                Image(systemName: symbol).font(.caption).foregroundStyle(green)
                Text(card.title).font(.subheadline.weight(.semibold))
                    .fixedSize(horizontal: false, vertical: true)
            }
            .frame(maxWidth: .infinity, minHeight: wide ? 18 : 34, alignment: wide ? .center : .topLeading)
            Rectangle().fill(green.opacity(0.15)).frame(height: 1)
            if card.title == "Crop Production" {
                HStack(alignment: .top, spacing: 8) {
                    ForEach([0, 1, 2], id: \.self) { row in
                        metric(card.rows[row], prominent: true, centered: true)
                            .frame(maxWidth: .infinity, alignment: .top)
                    }
                }
                HStack {
                    Text(card.rows[3][0]).foregroundStyle(.secondary)
                    Spacer(minLength: 4)
                    Text(card.rows[3][1] + " " + card.rows[3][2]).fontWeight(.semibold).monospacedDigit()
                }.font(.caption)
            } else if wide {
                HStack(alignment: .top, spacing: 12) {
                    ForEach(card.rows.indices, id: \.self) { row in
                        metric(card.rows[row], prominent: card.title == "Estate Cost", centered: true)
                            .frame(maxWidth: .infinity, alignment: .top)
                    }
                }
            } else {
                ForEach(card.rows.indices, id: \.self) { row in
                    metric(card.rows[row])
                }
                Spacer(minLength: 0)
            }
            Text(card.note).font(.caption2).foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: wide ? .center : .leading)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(10)
        .frame(maxWidth: .infinity, minHeight: height, alignment: .topLeading)
        .background(EstateTheme.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(EstateTheme.border, lineWidth: 1))
    }

    private func metric(_ row: [String], prominent: Bool = false, centered: Bool = false) -> some View {
        VStack(alignment: centered ? .center : .leading, spacing: 2) {
            Text(row[0]).font(.caption2).foregroundStyle(.secondary)
            (Text(row[1]).font(prominent ? .headline : .caption.weight(.semibold)) + Text(" " + row[2]).font(.caption2))
                .foregroundStyle(prominent ? green : Color.primary)
                .monospacedDigit().fixedSize(horizontal: false, vertical: true)
        }
        .multilineTextAlignment(centered ? .center : .leading)
        .accessibilityElement(children: .combine)
    }
}
