import SwiftUI

struct EstateRecordsView: View {
    @ObservedObject private var estate = EstateReferenceStore.shared
    var body: some View {
        List {
            if let reference = estate.reference {
                ForEach(reference.sections) { table in
                    NavigationLink(table.title) { EstateRecordTableView(table: table) }
                }
            } else { Text("No estate data imported. Open Settings → Estate Data Sync.") }
        }
        .estateScreen().navigationTitle("All Estate Records")
    }
}

struct EstateRecordTableView: View {
    let table: EstateReferenceTable
    var body: some View {
        List {
            if table.rows.isEmpty { Text("Pending verified records") }
            ForEach(table.rows.indices, id: \.self) { index in
                Section {
                    ForEach(table.columns.indices, id: \.self) { column in
                        LabeledContent(table.columns[column],
                            value: column < table.rows[index].count ? (table.rows[index][column]?.text ?? "Pending") : "Pending")
                    }
                }
            }
        }
        .estateScreen().navigationTitle(table.title)
    }
}

struct FieldDetailsView: View {
    @ObservedObject private var estate = EstateReferenceStore.shared
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                GroupBox("Field Dashboard") {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Fields available: " + (estate.reference == nil ? "Pending" : String(estateFieldCodes(estate.reference).count)))
                        Text("Basic field profiles · area, palms, planting and age records")
                        Text("Choose a field for its basic information. Crop and harvesting records are under Crop Production.")
                            .font(.footnote).foregroundStyle(.secondary)
                    }.frame(maxWidth: .infinity, alignment: .leading)
                }
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                    ForEach(estateFieldCodes(estate.reference), id: \.self) { code in
                        NavigationLink {
                            List {
                                let tables = estateFieldTables(estate.reference, code: code)
                                ForEach(tables) { table in
                                    Section(table.title) {
                                        ForEach(table.rows.indices, id: \.self) { index in
                                            ForEach(table.columns.indices, id: \.self) { column in
                                                LabeledContent(table.columns[column],
                                                    value: table.rows[index][column]?.text ?? "Pending")
                                            }
                                        }
                                    }
                                }
                                Text("Basic records for this field only. Crop, harvesting and productivity records are under Crop Production.")
                                    .font(.footnote)
                            }.estateScreen().navigationTitle(code)
                        } label: {
                            Text(code).font(.headline).frame(maxWidth: .infinity, minHeight: 60)
                        }.buttonStyle(.borderedProminent)
                    }
                }
                if estateFieldCodes(estate.reference).isEmpty {
                    Text("Import estate records through Settings → Estate Data Sync to see field buttons.")
                }
            }.padding()
        }
        .estateScreen().navigationTitle("Field Details").navigationBarTitleDisplayMode(.inline)
        .toolbar(.visible, for: .navigationBar)
    }
}

struct UpkeepCultivationView: View {
    @ObservedObject private var estate = EstateReferenceStore.shared
    private let fieldWork = ["Weeding", "Manuring", "Pest and Disease", "Water management", "Road management"]
    var body: some View {
        List {
            Section("Upkeep and Cultivation Dashboard") {
                LabeledContent("Weeding progress", value: "Pending")
                let manuring = EstateDashboardData(reference: estate.reference, today: Date()).operationalCards[3]
                LabeledContent("Manuring progress", value: manuring.rows[0][1] + " %")
                Text(manuring.note).font(.footnote).foregroundStyle(.secondary)
                LabeledContent("Pest and Disease", value: "Pending")
                LabeledContent("Water management", value: "Pending")
                LabeledContent("Road management", value: "Pending")
            }
            Section("Major Upkeep Work") {
                ForEach(fieldWork, id: \.self) { work in
                    NavigationLink(work) { UpkeepFieldWorkView(work: work) }
                }
                NavigationLink("Others") {
                    List {
                        Section("Other Necessary Work") {
                            Text("Additional upkeep work will appear here when verified records are available.")
                            LabeledContent("Work records", value: "Pending")
                        }
                    }
                    .estateScreen().navigationTitle("Others")
                    .navigationBarTitleDisplayMode(.inline)
                }
            }
        }
        .estateScreen().navigationTitle("Upkeep and Cultivation").navigationBarTitleDisplayMode(.inline)
        .toolbar(.visible, for: .navigationBar)
    }
}

private struct UpkeepFieldWorkView: View {
    let work: String
    @ObservedObject private var estate = EstateReferenceStore.shared
    @State private var selectedField: String?
    var body: some View {
        let codes = estateFieldCodes(estate.reference)
        let field = selectedField.flatMap { codes.contains($0) ? $0 : nil } ?? codes.first
        List {
            Section("Fields") {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(codes, id: \.self) { code in
                            Button { selectedField = code } label: {
                                Text(code)
                                    .font(.subheadline.weight(.semibold))
                                    .padding(.horizontal, 16).padding(.vertical, 10)
                                    .foregroundStyle(field == code ? EstateTheme.background : EstateTheme.accent)
                                    .background(field == code ? EstateTheme.accent : EstateTheme.softAccent, in: Capsule())
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Field " + code)
                            .accessibilityAddTraits(field == code ? .isSelected : [])
                        }
                    }.padding(.vertical, 4)
                }
                if codes.isEmpty {
                    Text("Import estate records through Settings → Estate Data Sync to see fields.")
                        .foregroundStyle(.secondary)
                } else if field == nil {
                    Text("Select a field to view its details and upkeep and cultivation records.")
                        .font(.footnote).foregroundStyle(.secondary)
                }
            }
            if let code = field {
                Section("Field " + code) {
                    ForEach(estateFieldTables(estate.reference, code: code)) { table in
                        DisclosureGroup(table.title) {
                            ForEach(table.rows.indices, id: \.self) { row in
                                ForEach(table.columns.indices, id: \.self) { column in
                                    LabeledContent(table.columns[column], value:
                                        column < table.rows[row].count ? (table.rows[row][column]?.text ?? "Pending") : "Pending")
                                }
                            }
                        }
                    }
                }
                Section(work + " · " + code) {
                    LabeledContent(work + " work", value: "Pending")
                    Text("Awaiting verified \(work.lowercased()) records for this field.")
                        .font(.footnote).foregroundStyle(.secondary)
                }
            }
        }
        .estateScreen().navigationTitle(work).navigationBarTitleDisplayMode(.inline)
        .toolbar(.visible, for: .navigationBar)
    }
}
