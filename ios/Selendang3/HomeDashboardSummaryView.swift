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
        .background(Color(red: 0.16, green: 0.35, blue: 0.24))
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

// Six-card phone preview; larger accessibility text retains scroll access.
private struct OperationalDashboardGrid: View {
    @ObservedObject private var estate = EstateReferenceStore.shared
    private let columns = [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)]
    private let green = Color(red: 0.16, green: 0.35, blue: 0.24)

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                TimelineView(.periodic(from: Date(), by: 60)) { context in
                    let cards = EstateDashboardData(reference: estate.reference, today: context.date).operationalCards
                    LazyVGrid(columns: columns, spacing: 10) {
                        ForEach(Array(cards.enumerated()), id: \.element.id) { index, card in
                            dashboardCard(card, index: index, height: max(218, (geometry.size.height - 44) / 3))
                        }
                    }
                }
                .padding(12)
            }
            .background(Color(uiColor: .systemGroupedBackground))
        }
    }

    private func dashboardCard(_ card: EstateDashboardPanel, index: Int, height: CGFloat) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top, spacing: 6) {
                Image(systemName: ["leaf.fill", "square.grid.3x3.fill", "person.fill", "sprinkler.and.droplets.fill", "chart.bar.fill", "banknote.fill"][index])
                    .font(.caption).foregroundStyle(green).frame(width: 16)
                Text(card.title).font(.subheadline.weight(.semibold))
                    .fixedSize(horizontal: false, vertical: true)
            }
            .frame(maxWidth: .infinity, minHeight: 36, alignment: .topLeading)
            Rectangle().fill(green.opacity(0.15)).frame(height: 1)
            if index == 0 {
                prominentMetric(card.rows[2])
                compactMetric(card.rows[0])
                compactMetric(card.rows[1])
                compactMetric(card.rows[3])
            } else if index >= 4 {
                prominentMetric(card.rows[0])
            } else {
                ForEach(card.rows.indices, id: \.self) { row in
                    compactMetric(card.rows[row])
                }
            }
            Spacer(minLength: 0)
            Text(card.note).font(.caption2).foregroundStyle(.secondary)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(12)
        .frame(maxWidth: .infinity, minHeight: height, alignment: .topLeading)
        .background(Color(uiColor: .secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .overlay(RoundedRectangle(cornerRadius: 18).stroke(green.opacity(0.12), lineWidth: 1))
    }

    private func prominentMetric(_ metric: [String]) -> some View {
        VStack(alignment: .leading, spacing: 3) {
            Text(metric[0]).font(.caption2).foregroundStyle(.secondary)
            (Text(metric[1]).font(.title2.weight(.bold)) + Text(" " + metric[2]).font(.caption))
                .foregroundStyle(green).monospacedDigit()
                .fixedSize(horizontal: false, vertical: true)
        }
        .accessibilityElement(children: .combine)
    }

    private func compactMetric(_ metric: [String]) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(metric[0]).font(.caption2).foregroundStyle(.secondary)
            (Text(metric[1]).font(.caption.weight(.semibold)) + Text(" " + metric[2]).font(.caption2))
                .monospacedDigit().fixedSize(horizontal: false, vertical: true)
        }
        .accessibilityElement(children: .combine)
    }
}
