import SwiftUI
import PDFKit

struct EstateMapCard: View {
    @State private var showsMap = false

    var body: some View {
        Button { showsMap = true } label: {
            HStack(spacing: 12) {
                Image(systemName: "map.fill").font(.title2)
                Text("Estate Map").font(.headline)
                Spacer()
                Image(systemName: "chevron.right").font(.body.weight(.semibold))
            }
            .foregroundStyle(EstateTheme.accent)
            .padding(16)
            .frame(maxWidth: .infinity, minHeight: 56)
            .background(EstateTheme.surface, in: RoundedRectangle(cornerRadius: 18))
            .overlay(RoundedRectangle(cornerRadius: 18).stroke(EstateTheme.border, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityHint("Opens the Selendang 3 estate map full screen")
        .fullScreenCover(isPresented: $showsMap) { EstateMapView() }
    }
}

private struct EstateMapView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var resetID = UUID()
    private let mapURL = Bundle.main.url(forResource: "Selendang3EstateMap", withExtension: "pdf")

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button { dismiss() } label: {
                    Label("Back", systemImage: "chevron.left").frame(minHeight: 44)
                }
                Spacer()
                Text("Estate Map").font(.headline)
                Spacer()
                Button("Reset View") { resetID = UUID() }
                    .frame(minHeight: 44)
                    .disabled(mapURL == nil)
            }
            .padding(.horizontal, 16)
            .background(EstateTheme.surface)
            Divider()
            if let mapURL {
                EstateMapPDF(url: mapURL)
                    .id(resetID)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                Text("Pinch to zoom · Drag to explore")
                    .font(.footnote).foregroundStyle(.secondary).padding(8)
            } else {
                Spacer()
                Text("Estate map unavailable").font(.headline)
                Text("Return to the dashboard and try again after updating the app.")
                    .multilineTextAlignment(.center).padding()
                Spacer()
            }
        }
        .background(EstateTheme.background)
        .tint(EstateTheme.accent)
    }
}

private struct EstateMapPDF: UIViewRepresentable {
    let url: URL

    func makeUIView(context: Context) -> PDFView {
        let view = PDFView()
        view.displayMode = .singlePage
        view.document = PDFDocument(url: url)
        view.autoScales = true
        view.backgroundColor = .secondarySystemBackground
        return view
    }

    func updateUIView(_ uiView: PDFView, context: Context) {}
}
