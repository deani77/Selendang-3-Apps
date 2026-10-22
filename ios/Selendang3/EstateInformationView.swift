import SwiftUI

struct EstateInformationView: View {
    @ObservedObject private var estate = EstateReferenceStore.shared
    var body: some View {
        List {
            if let data = estate.reference {
                Section {
                    Text("Saved estate revision \(data.revision)").font(.headline)
                    Text(data.reportingPeriod)
                    Text("Available offline. Missing values remain Pending.").font(.footnote)
                }
                ForEach(data.sections) { table in
                    NavigationLink(table.title) {
                        List {
                            if table.rows.isEmpty { Text("Awaiting estate records") }
                            ForEach(table.rows.indices, id: \.self) { index in
                                Section {
                                    ForEach(table.columns.indices, id: \.self) { column in
                                        VStack(alignment: .leading, spacing: 4) {
                                            Text(table.columns[column]).font(.caption).foregroundStyle(.secondary)
                                            Text(table.rows[index][column]?.text ?? "Pending").textSelection(.enabled)
                                        }
                                    }
                                }
                            }
                        }.navigationTitle(table.title).navigationBarTitleDisplayMode(.inline)
                    }
                }
            } else {
                Text("No estate data saved yet. Open Settings and tap Sync Now, then confirm Import Estate Update.")
            }
            if let error = estate.storageError { Text(error).foregroundStyle(.red) }
        }
        .navigationTitle("Estate Information").navigationBarTitleDisplayMode(.inline)
        .toolbar(.visible, for: .navigationBar)
    }
}
