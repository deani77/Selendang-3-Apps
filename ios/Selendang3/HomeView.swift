import SwiftUI

struct HomeView: View {
    private let green = Color(red: 0.08, green: 0.22, blue: 0.15)
    private let sections = [
        "Estate Information", "Estate Production", "Upkeep and Cultivation",
        "Reports", "Settings", "About"
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    Text("Ladang Selendang 3")
                        .font(.largeTitle.bold())
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.white)
                        .padding(.vertical, 24)
                    NavigationLink {
                        EstateDashboardView()
                    } label: {
                        Text("Estate Dashboard")
                            .font(.title2.bold())
                            .frame(maxWidth: .infinity, minHeight: 110)
                            .background(Color(red: 0.16, green: 0.35, blue: 0.24))
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 24))
                    }
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 140))], spacing: 14) {
                        ForEach(sections, id: \.self) { title in
                            NavigationLink {
                                ModuleView(title: title)
                            } label: {
                                Text(title)
                                    .font(.headline)
                                    .multilineTextAlignment(.center)
                                    .frame(maxWidth: .infinity, minHeight: 88)
                                    .padding(8)
                                    .background(Color(red: 0.85, green: 0.89, blue: 0.82))
                                    .foregroundStyle(green)
                                    .clipShape(RoundedRectangle(cornerRadius: 18))
                            }
                        }
                    }
                    Text("v0.1 Alpha · iPhone starter\nData and synchronization are not configured")
                        .font(.footnote)
                        .multilineTextAlignment(.center)
                        .foregroundStyle(.white.opacity(0.85))
                        .padding(.top, 24)
                }
                .padding(20)
            }
            .background(LinearGradient(colors: [green, .black], startPoint: .top, endPoint: .bottom))
            .toolbar(.hidden, for: .navigationBar)
        }
        .tint(.green)
    }
}

private struct ModuleView: View {
    let title: String
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(title).font(.title.bold())
                Text(title == "About"
                     ? "Ladang Selendang 3\niPhone starter · v0.1 Alpha"
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
