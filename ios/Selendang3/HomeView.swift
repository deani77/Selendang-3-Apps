import SwiftUI

struct HomeView: View {
    @Environment(\.colorScheme) private var colorScheme
    @ObservedObject private var estate = EstateReferenceStore.shared
    private var isPad: Bool { UIDevice.current.userInterfaceIdiom == .pad }
    @ScaledMetric(relativeTo: .largeTitle) private var iPadHeadingSize: CGFloat = 44
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
                            VStack(alignment: .leading, spacing: 12) {
                                ForEach(Array(rows.enumerated()), id: \.element.id) { index, row in
                                    if index > 0 { Divider().overlay(Color.white.opacity(0.35)) }
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(row.title).font(isPad ? .title2.bold() : .subheadline.bold())
                                        Text(row.note).font(isPad ? .title3 : .footnote)
                                    }
                                }
                            }
                            .frame(maxWidth: .infinity, minHeight: 160, alignment: .leading)
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
                    Text("S3 Estate Info")
                        .font(isPad ? .body : .footnote)
                        .foregroundStyle(.secondary)
                        .padding(.top, 8)
                    if let data = estate.reference { Text("Estate revision \(data.revision) saved").font(isPad ? .body : .footnote) }
                    Text("v0.4 Alpha · iPhone and iPad\nEstate data and production drafts are saved offline")
                        .font(isPad ? .body : .footnote)
                        .multilineTextAlignment(.center)
                        .foregroundStyle(colorScheme == .dark ? Color.white.opacity(0.85) : green)
                        .padding(.top, 24)
                }
                .padding(20)
            }
            .background(LinearGradient(colors: colorScheme == .dark ? [green, .black] : [Color(red: 0.94, green: 0.97, blue: 0.92), .white], startPoint: .top, endPoint: .bottom))
            .toolbar(.hidden, for: .navigationBar)
        }
        .tint(.green)
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

