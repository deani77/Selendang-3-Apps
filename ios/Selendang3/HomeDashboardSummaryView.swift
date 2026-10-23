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

// Six-card phone preview; scrolling remains available for larger accessibility text.
private struct OperationalDashboardGrid: View {
    @ObservedObject private var estate = EstateReferenceStore.shared
    private let columns = [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)]

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                TimelineView(.periodic(from: Date(), by: 60)) { context in
                    let cards = EstateDashboardData(reference: estate.reference, today: context.date).operationalCards
                    LazyVGrid(columns: columns, alignment: .center, spacing: 10) {
                        ForEach(cards) { card in
                            VStack(alignment: .leading, spacing: 5) {
                                Text(card.title)
                                    .font(.subheadline.bold())
                                    .frame(maxWidth: .infinity, minHeight: 34, alignment: .topLeading)
                                    .fixedSize(horizontal: false, vertical: true)
                                Text(card.note).font(.caption2).foregroundStyle(.white.opacity(0.85))
                                ForEach(card.rows.indices, id: \.self) { index in
                                    let metric = card.rows[index]
                                    VStack(alignment: .leading, spacing: 1) {
                                        Text(metric[0]).font(.caption2).foregroundStyle(.white.opacity(0.85))
                                        Text(metric[1] + " " + metric[2]).font(.caption.bold())
                                            .fixedSize(horizontal: false, vertical: true)
                                    }
                                    .accessibilityElement(children: .combine)
                                }
                                Spacer(minLength: 0)
                            }
                            .padding(10)
                            .frame(maxWidth: .infinity, minHeight: max(210, (geometry.size.height - 44) / 3), alignment: .topLeading)
                            .foregroundStyle(.white)
                            .background(Color(red: 0.16, green: 0.35, blue: 0.24))
                            .clipShape(RoundedRectangle(cornerRadius: 18))
                        }
                    }
                }
                .padding(12)
            }
        }
    }
}
