import Foundation
import Combine

final class ProductionStore: ObservableObject {
    @Published private(set) var rows: [ProductionDraft] = []
    @Published private(set) var storageError: String?
    private let url: URL

    init() {
        url = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("production-drafts-v1.json")
        do {
            if FileManager.default.fileExists(atPath: url.path) {
                let file = try JSONDecoder().decode(ProductionDraftFile.self, from: Data(contentsOf: url))
                try file.validate()
                rows = file.rows
            }
        } catch {
            storageError = "Saved production drafts could not be read. Editing is unavailable to protect your data."
        }
    }

    func save(_ draft: ProductionDraft) throws {
        guard storageError == nil else { throw ProductionError.invalid(storageError!) }
        try draft.validate()
        let updated = (rows.filter { $0.id != draft.id } + [draft]).sorted { $0.id < $1.id }
        let file = ProductionDraftFile(version: 1, rows: updated)
        try file.validate()
        let data = try JSONEncoder().encode(file)
        try FileManager.default.createDirectory(at: url.deletingLastPathComponent(), withIntermediateDirectories: true)
        try data.write(to: url, options: .atomic)
        rows = updated
    }
}
