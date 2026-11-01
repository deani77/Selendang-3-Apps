import SwiftUI
import PDFKit

/// A local reference map; no GPS, field-code mapping or operational overlays are inferred.
struct EstateMapView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var fitRequest = 0
    @State private var document = Bundle.main.url(forResource: "Selendang3EstateMap", withExtension: "pdf")
        .flatMap { PDFDocument(url: $0) }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if UIDevice.current.userInterfaceIdiom == .pad, let document = document {
                    EstateMapPDFView(document: document, fitRequest: fitRequest)
                    Text("Orthomosaic: 6 February 2025 · Pinch to zoom; drag to pan. Reference map only.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(10)
                } else {
                    Text("Estate map unavailable. Reinstall the latest iPad build to restore the bundled map.")
                        .multilineTextAlignment(.center)
                        .padding()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
            .background(EstateTheme.background)
            .navigationTitle("Estate Map")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Done") { dismiss() }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button { fitRequest += 1 } label: {
                        Label("Fit Map", systemImage: "arrow.down.right.and.arrow.up.left")
                    }
                    .disabled(document == nil)
                    .accessibilityHint("Resets zoom to show the whole map")
                }
            }
        }
        .tint(EstateTheme.accent)
    }
}

private struct EstateMapPDFView: UIViewRepresentable {
    let document: PDFDocument
    let fitRequest: Int

    final class Coordinator {
        var lastFitRequest = 0
    }

    func makeCoordinator() -> Coordinator { Coordinator() }

    func makeUIView(context: Context) -> PDFView {
        let view = PDFView()
        view.displayMode = .singlePage
        view.displayBox = .mediaBox
        view.displaysPageBreaks = false
        view.backgroundColor = .secondarySystemBackground
        view.document = document
        view.autoScales = true
        view.accessibilityLabel = "Selendang 3 estate reference map"
        return view
    }

    func updateUIView(_ view: PDFView, context: Context) {
        guard context.coordinator.lastFitRequest != fitRequest else { return }
        context.coordinator.lastFitRequest = fitRequest
        view.autoScales = true
        view.scaleFactor = view.scaleFactorForSizeToFit
        if let page = document.page(at: 0) { view.go(to: page) }
    }
}
