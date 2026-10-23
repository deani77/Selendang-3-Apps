import SwiftUI

struct HomeView: View {
    @Environment(\.colorScheme) private var colorScheme
    @ObservedObject private var estate = EstateReferenceStore.shared
    private var isPad: Bool { UIDevice.current.userInterfaceIdiom == .pad }
    @ScaledMetric(relativeTo: .largeTitle) private var iPadHeadingSize: CGFloat = 44
    @ScaledMetric(relativeTo: .title2) private var iPadSummaryHeaderHeight: CGFloat = 80
    private let green = Color(red: 0.08, green: 0.22, blue: 0.15)
    private let sections = [
        "Estate Details", "Field Details", "Upkeep and Cultivation",
        "Crop Production", "Settings", "Planter Tools"
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    Text("Estate Dashboard")
                        .font(isPad ? .system(size: iPadHeadingSize, weight: .bold) : .largeTitle.bold())
                        .multilineTextAlignment(.center)
                        .foregroundStyle(colorScheme == .dark ? Color.white : green)
                        .padding(.vertical, 24)
                    NavigationLink {
                        ModuleView(title: "Estate Dashboard")
                    } label: {
                        TimelineView(.periodic(from: Date(), by: 60)) { context in
                            let rows = EstateDashboardData(reference: estate.reference, today: context.date).homeRows
                            Group {
                                if isPad {
                                    HStack(alignment: .top, spacing: 16) {
                                        ForEach(Array(rows.enumerated()), id: \.element.id) { index, row in
                                            if index > 0 { Divider().overlay(Color.white.opacity(0.35)) }
                                            summaryCell(row)
                                                .frame(maxWidth: .infinity, alignment: .top)
                                        }
                                    }
                                    .fixedSize(horizontal: false, vertical: true)
                                } else {
                                    VStack(alignment: .leading, spacing: 12) {
                                        ForEach(Array(rows.enumerated()), id: \.element.id) { index, row in
                                            if index > 0 { Divider().overlay(Color.white.opacity(0.35)) }
                                            summaryCell(row)
                                        }
                                    }
                                }
                            }
                            .frame(maxWidth: .infinity, minHeight: 160, alignment: isPad ? .top : .leading)
                            .padding(16)
                        }
                            .background(Color(red: 0.16, green: 0.35, blue: 0.24))
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 24))
                            .accessibilityElement(children: .combine)
                            .accessibilityHint("Opens Estate Dashboard")
                    }
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                        ForEach(sections, id: \.self) { title in
                            NavigationLink {
                                if title == "Settings" {
                                    UserSettingsView()
                                } else if title == "Estate Details" {
                                    EstateInformationView()
                                } else if title == "Field Details" {
                                    FieldDetailsView()
                                } else if title == "Upkeep and Cultivation" {
                                    UpkeepCultivationView()
                                } else if title == "Crop Production" {
                                    ProductionView()
                                } else {
                                    ModuleView(title: title)
                                }
                            } label: {
                                Text(title)
                                    .font(isPad ? .title3.weight(.semibold) : .subheadline.weight(.semibold))
                                    .multilineTextAlignment(.center)
                                    .frame(maxWidth: .infinity, minHeight: 64)
                                    .padding(8)
                                    .background(Color(red: 0.85, green: 0.89, blue: 0.82))
                                    .foregroundStyle(green)
                                    .clipShape(RoundedRectangle(cornerRadius: 18))
                            }
                        }
                    }

                }
                .padding(20)
            }
            .safeAreaInset(edge: .bottom, spacing: 0) { homeFooter }
            .background(LinearGradient(colors: colorScheme == .dark ? [green, .black] : [Color(red: 0.94, green: 0.97, blue: 0.92), .white], startPoint: .top, endPoint: .bottom))
            .toolbar(.hidden, for: .navigationBar)
        }
        .tint(.green)
    }

    private var homeFooter: some View {
        VStack(spacing: 4) {
            Text("S3 Estate Info").foregroundStyle(.secondary)
            if let data = estate.reference { Text("Estate revision \(data.revision) saved") }
            Text("v0.4 Alpha · iPhone and iPad\nEstate data and production drafts are saved offline")
                .foregroundStyle(colorScheme == .dark ? Color.white.opacity(0.85) : green)
        }
        .font(isPad ? .body : .footnote)
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 20)
        .padding(.vertical, 8)
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
                    Text(summaryTitle(row))
                        .font(.title2.bold())
                        .lineLimit(2)
                    Text(summaryUnit(row)).font(.subheadline)
                }
                .frame(maxWidth: .infinity, minHeight: iPadSummaryHeaderHeight, alignment: .top)
                .accessibilityElement(children: .combine)
            } else {
                Text(row.title).font(.subheadline.bold())
            }
            Text(isPad ? row.note.replacingOccurrences(of: " · ", with: "\n") : row.note)
                .font(isPad ? .body : .footnote)
        }
        .multilineTextAlignment(isPad ? .center : .leading)
    }
}

struct ModuleView: View {
    let title: String
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(title).font(.title.bold())
                Text(title == "About"
                     ? "S3 Estate Info\niPhone and iPad · v0.4 Alpha"
                     : "This section is awaiting implementation and verified estate records.")
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()
        }
        .toolbar(.visible, for: .navigationBar)
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
    }
}

