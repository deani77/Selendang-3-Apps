import SwiftUI

struct ProductionView: View {
    @StateObject private var store = ProductionStore.shared
    @State private var section = "Today Crop Production"
    @State private var field = productionFields[0]
    @State private var date = Date()
    @State private var dirty = false
    @State private var pendingAction: (() -> Void)?
    @State private var showDiscard = false
    private let sections = ["Crop History by Field", "Today Crop Production", "Month Crop Production", "Year Todate Crop Production"]
    private var dateKey: String { productionDateKey(date) }

    private func change(_ action: @escaping () -> Void) {
        if dirty { pendingAction = action; showDiscard = true } else { action() }
    }

    var body: some View {
        Form {
            Section {
                Text("Production by field").font(.headline)
                Text(store.status).font(.footnote)
                Text("Drafts are saved on this device. Use Settings → Sync Now to upload and download production. PDF capture is not connected yet.")
                Picker("Section", selection: Binding(get: { section }, set: { value in change { section = value; dirty = false } })) {
                    ForEach(sections, id: \.self) { Text($0).tag($0) }
                }
                if section == "Today Crop Production" || section == "Crop History by Field" {
                    Picker("Field", selection: Binding(get: { field }, set: { value in change { field = value; dirty = false } })) {
                        ForEach(productionFields, id: \.self) { Text($0).tag($0) }
                    }
                }
                DatePicker("Reporting date / summary cutoff", selection: Binding(get: { date }, set: { value in change { date = value; dirty = false } }), displayedComponents: .date)
            }
            if let error = store.storageError { Section { Text(error).foregroundStyle(.red) } }
            if section == "Crop History by Field" {
                history
            } else if section == "Today Crop Production" {
                if store.storageError == nil {
                    ProductionEditorView(record: store.rows.first { $0.date == dateKey && $0.field == field }, date: dateKey, field: field, dirty: $dirty, store: store)
                        .id("\(dateKey)/\(field)")
                }
                Section("PDF capture and submission") {
                    Button("Upload PDF · coming next") {}.disabled(true)
                    Text("PDF field aliases are defined. Pengeluaran Hasil means Crop Sent to Mill. Harvested crop and backlog require manual entry or another source.")
                    Text("Use Settings → Sync Now to upload saved drafts and review conflicting edits.")
                }
            } else {
                summaries
            }
        }
        .navigationTitle("Estate Production")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.visible, for: .navigationBar)
        .alert("Discard unsaved changes?", isPresented: $showDiscard) {
            Button("Discard changes", role: .destructive) {
                let action = pendingAction; pendingAction = nil; dirty = false; action?()
            }
            Button("Keep editing", role: .cancel) { pendingAction = nil }
        } message: { Text("Save the draft before switching if you want to keep these changes.") }
        .environment(\.calendar, Calendar(identifier: .gregorian))
    }

    private var history: some View {
        let year = Calendar(identifier: .gregorian).component(.year, from: date)
        return Group {
            Section("\(field) · five years including \(String(year)) · read-only") {
                ForEach(Array((year - 4)...year), id: \.self) { value in
                    VStack(alignment: .leading, spacing: 6) {
                        Text(String(value)).font(.headline)
                        Text("Crop (t): Pending\nYield (t/ha): Pending")
                    }
                }
                Text("Verified field history is not connected. Estate-wide YPH is not substituted for field history.")
            }
        }
    }

    private var summaries: some View {
        let calendar = Calendar(identifier: .gregorian)
        let monthly = section == "Month Crop Production"
        let from = calendar.dateInterval(of: monthly ? .month : .year, for: date)!.start
        let expectedDays = calendar.dateComponents([.day], from: calendar.startOfDay(for: from), to: calendar.startOfDay(for: date)).day! + 1
        return Group {
            Section {
                Text("\(productionDateKey(from)) through \(dateKey) · provisional totals from local saved drafts only. Missing dates are not assumed to be zero.")
                if monthly {
                    Text("Harvested hectares accumulate work across rounds. Harvester-days add daily attendance; they are not distinct workers. Backlog shows the latest saved balance.")
                } else {
                    Text("Only Crop Sent to Mill carries forward from January through this date. Other measures are shown for the selected month only.")
                }
            }
            if store.storageError == nil {
                ForEach(productionFields, id: \.self) { code in
                    let result = productionSummary(store.rows, field: code, from: productionDateKey(from), through: dateKey)
                    Section(code) {
                        Text("\(result.days) of \(expectedDays) dates saved")
                        Text("Crop sent to mill: \(result.sentToMill.label("t"))")
                        if monthly {
                            Text("Crop harvested: \(result.harvested.label("t"))")
                            Text("Hectares harvested: \(result.hectares.label("ha"))")
                            Text("Harvester-days: \(result.harvesterDays.label(""))")
                            Text("Backlog: \(result.backlog.map { String(format: "%.2f t", locale: Locale(identifier: "en_US_POSIX"), $0) } ?? "Pending")\(result.backlogDate.map { " · as of \($0)" } ?? "")")
                        }
                    }
                }
            }
        }
    }
}

private struct ProductionEditorView: View {
    let record: ProductionDraft?
    let date: String
    let field: String
    @Binding var dirty: Bool
    @ObservedObject var store: ProductionStore
    @State private var values: [String]
    @State private var notice: String?
    private let labels = ["Crop harvested (t)", "Crop sent to mill (t)", "Crop backlog balance (t)", "Hectares harvested (ha)", "Number of harvesters present"]

    init(record: ProductionDraft?, date: String, field: String, dirty: Binding<Bool>, store: ProductionStore) {
        self.record = record; self.date = date; self.field = field; self._dirty = dirty; self._store = ObservedObject(wrappedValue: store)
        _values = State(initialValue: [(record?.harvested).map { String($0) } ?? "", (record?.sentToMill).map { String($0) } ?? "",
            (record?.backlog).map { String($0) } ?? "", (record?.hectares).map { String($0) } ?? "", (record?.harvesters).map { String($0) } ?? ""])
    }

    var body: some View {
        Section("\(field) · \(date) · \(record == nil ? "New local draft" : "Saved on this device")") {
            Text("Leave unknown values blank. Enter 0 only when zero is confirmed. Saving again replaces this field's draft for this date.")
            ForEach(labels.indices, id: \.self) { index in
                VStack(alignment: .leading) {
                    Text(labels[index]).font(.caption)
                    TextField(labels[index], text: Binding(get: { values[index] }, set: { values[index] = $0; dirty = true; notice = nil }))
                        .keyboardType(index == 4 ? .numberPad : .decimalPad)
                }
            }
            Button("Save Draft") {
                do {
                    func number(_ text: String) throws -> Double? {
                        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
                        if trimmed.isEmpty { return nil }
                        guard let value = Double(trimmed), value.isFinite, value >= 0 else {
                            throw ProductionError.invalid("Enter non-negative numbers using a decimal point.")
                        }
                        return value
                    }
                    let count = values[4].trimmingCharacters(in: .whitespacesAndNewlines)
                    let workers = count.isEmpty ? nil : Int(count)
                    if !count.isEmpty && (workers == nil || workers! < 0) {
                        throw ProductionError.invalid("Harvesters must be a non-negative whole number.")
                    }
                    let draft = ProductionDraft(date: date, field: field, harvested: try number(values[0]), sentToMill: try number(values[1]),
                        backlog: try number(values[2]), hectares: try number(values[3]), harvesters: workers)
                    try store.save(draft)
                    dirty = false
                    notice = "Draft saved on this device. Pending upload in Settings → Sync Now."
                } catch { notice = error.localizedDescription }
            }
            if let notice = notice { Text(notice) }
        }
    }
}
