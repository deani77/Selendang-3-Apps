import SwiftUI

struct HomeView: View {
    @Environment(\.colorScheme) private var colorScheme
    @ObservedObject private var estate = EstateReferenceStore.shared
    private var isPad: Bool { UIDevice.current.userInterfaceIdiom == .pad }
    @ScaledMetric(relativeTo: .largeTitle) private var iPadHeadingSize: CGFloat = 44
    @ScaledMetric(relativeTo: .body) private var minimumPhoneHomeHeight: CGFloat = 520
    private let green = Color(red: 0.08, green: 0.22, blue: 0.15)
    private let sections = [
        "Estate Details", "Field Details", "Upkeep and Cultivation",
        "Crop Production", "Settings", "Planter Tools"
    ]

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if isPad {
                    homeHeading.padding(.horizontal, 20)
                    HomeDashboardSummaryView(horizontal: true)
                        .padding(.horizontal, 20).padding(.bottom, 16)
                    ScrollView { navigationGrid.padding(20) }
                } else {
                    GeometryReader { geometry in
                        ScrollView {
                            VStack(spacing: 8) {
                                homeHeading.fixedSize(horizontal: false, vertical: true).padding(.bottom, 12)
                                NavigationLink { HomeDashboardScreen() } label: {
                                    Text("Estate Dashboard").font(.title2.bold())
                                        .frame(maxWidth: .infinity, minHeight: 88, maxHeight: .infinity)
                                        .background(Color(red: 0.16, green: 0.35, blue: 0.24))
                                        .foregroundStyle(.white)
                                        .clipShape(RoundedRectangle(cornerRadius: 24))
                                }
                                .frame(maxHeight: .infinity).padding(.bottom, 12)
                                ForEach(0..<3, id: \.self) { row in
                                    HStack(spacing: 8) {
                                        navigationButton(sections[row * 2], stretches: true)
                                        navigationButton(sections[row * 2 + 1], stretches: true)
                                    }.frame(maxHeight: .infinity)
                                }
                            }
                            .padding(20)
                            .frame(height: max(geometry.size.height, minimumPhoneHomeHeight))
                        }
                    }
                }
            }
            .safeAreaInset(edge: .bottom, spacing: 0) { homeFooter }
            .background(LinearGradient(colors: colorScheme == .dark ? [green, .black] : [Color(red: 0.94, green: 0.97, blue: 0.92), .white], startPoint: .top, endPoint: .bottom))
            .toolbar(.hidden, for: .navigationBar)
        }
        .tint(.green)
    }

    private var homeHeading: some View {
        Text("Planter’s Log")
            .font(isPad ? .system(size: iPadHeadingSize, weight: .bold) : .largeTitle.bold())
            .multilineTextAlignment(.center)
            .foregroundStyle(colorScheme == .dark ? Color.white : green)
            .padding(.vertical, 24)
    }

    private var navigationGrid: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
            ForEach(sections, id: \.self) { title in navigationButton(title) }
        }
    }

    private func navigationButton(_ title: String, stretches: Bool = false) -> some View {
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
                .frame(maxWidth: .infinity, minHeight: isPad ? 64 : 48,
                    maxHeight: stretches ? .infinity : nil)
                .padding(isPad ? 8 : 4)
                .background(Color(red: 0.85, green: 0.89, blue: 0.82))
                .foregroundStyle(green)
                .clipShape(RoundedRectangle(cornerRadius: 18))
        }
        .frame(maxWidth: .infinity, maxHeight: stretches ? .infinity : nil)
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
        .background(colorScheme == .dark ? Color.black : Color.white)
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

