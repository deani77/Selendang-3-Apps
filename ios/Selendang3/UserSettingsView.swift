import SwiftUI

struct UserSettingsView: View {
    @AppStorage("theme") private var theme = "system"
    @StateObject private var production = ProductionStore.shared
    @AppStorage("server_address") private var address = ""
    @State private var accessToken = ""
    @State private var syncMessage: String?
    @StateObject private var estate = EstateReferenceStore.shared
    @State private var syncBusy = false
    @State private var pendingEstateData: Data?
    @State private var pendingEstate: EstateReference?
    @State private var showEstatePreview = false

    var body: some View {
        Form {
            Section {
                NavigationLink("User Profile") { UserProfileView() }
                NavigationLink("Appearance") {
                    Form {
                        Picker("Theme", selection: $theme) {
                            Text("Light").tag("light")
                            Text("Dark").tag("dark")
                            Text("Follow System").tag("system")
                        }
                        .pickerStyle(.inline)
                    }
                    .navigationTitle("Appearance")
                }
                NavigationLink("Inbox") {
                    SettingsPlaceholder(title: "Inbox", heading: "No messages yet",
                        message: "Announcements and replies will appear here once messaging is connected.")
                }
                NavigationLink("Suggestions") {
                    SettingsPlaceholder(title: "Suggestions", heading: "Share an idea",
                        message: "Suggestion submission and replies will be available once the service is connected.")
                }
            } footer: {
                Text("Profile and appearance are saved on this device. The access token is kept only for this Settings session.")
            }
            Section("Estate and production synchronization") {
                TextField("Full HTTPS snapshot address", text: $address).textInputAutocapitalization(.never).autocorrectionDisabled()
                SecureField("Access token", text: $accessToken).textInputAutocapitalization(.never).autocorrectionDisabled()
                Text(production.status)
                Text("Uploads pending daily production drafts and checks estate reference data. New estate updates require confirmation before replacing the offline copy.").font(.footnote)
                if let reference = estate.reference { Text("Saved estate revision \(reference.revision) · available offline") }
                if let checked = estate.lastChecked { Text("Estate last checked: " + checked.formatted()).font(.footnote) }
                if let error = estate.storageError { Text(error).foregroundStyle(.red) }
                Button(syncBusy ? "Syncing…" : "Sync Now") {
                    syncBusy = true
                    syncMessage = "Connecting to the estate server…"
                    Task {
                        var results: [String] = []
                        var endpoint: EstateSyncEndpoint
                        do { endpoint = try EstateSyncEndpoint(address: address, token: accessToken) }
                        catch { syncMessage = error.localizedDescription; syncBusy = false; return }
                        do {
                            try await production.sync(address: endpoint.snapshot.absoluteString, token: endpoint.token)
                            results.append("Production sync completed: " + production.status)
                        } catch { results.append("Production sync failed: " + error.localizedDescription + " Local drafts retained.") }
                        do {
                            let (update, data) = try await estate.download(address: endpoint.snapshot.absoluteString, token: endpoint.token)
                            if update.revision > (estate.reference?.revision ?? 0) {
                                pendingEstate = update; pendingEstateData = data; showEstatePreview = true
                                results.append("Estate revision \(update.revision) downloaded. Review and confirm the import.")
                            } else { results.append("Estate is up to date at revision \(update.revision).") }
                        } catch { results.append("Estate sync failed: " + error.localizedDescription) }
                        syncMessage = results.joined(separator: "\n\n")
                        syncBusy = false
                    }
                }.disabled(syncBusy || production.syncing)
                if syncBusy { ProgressView("Synchronizing…") }
                Text("Your token stays in this Settings session and is never stored in the app.").font(.footnote)
                if let error = production.storageError { Text(error).foregroundStyle(.red) }
                if let message = syncMessage { Text(message) }
            }
            ForEach(production.entries.filter { $0.conflict != nil }) { entry in
                Section("Conflict: \(entry.record.field) · \(entry.record.date)") {
                    Text("Both copies are preserved. Compare all values before choosing.")
                    Text("Phone: " + productionValues(entry.record))
                    if let remote = entry.conflict { Text("Server: " + productionValues(remote.record)) }
                    Button("Use server copy") { resolve(entry.id, false) }.disabled(syncBusy || production.syncing)
                    Button("Keep phone copy · upload on next Sync") { resolve(entry.id, true) }.disabled(syncBusy || production.syncing)
                }
            }
        }
        .alert("Import Estate Update?", isPresented: $showEstatePreview) {
            Button("Import Estate Update") {
                do {
                    guard let data = pendingEstateData else { return }
                    try estate.apply(data)
                    syncMessage = (syncMessage ?? "") + "\nEstate revision \(estate.reference?.revision ?? 0) saved. Open Estate Details; these records are available offline."
                } catch { syncMessage = error.localizedDescription }
                pendingEstateData = nil; pendingEstate = nil
            }
            Button("Cancel", role: .cancel) { pendingEstateData = nil; pendingEstate = nil }
        } message: {
            if let update = pendingEstate {
                Text("Revision \(update.revision)\n\(update.reportingPeriod)\n\(update.sections.reduce(0) { $0 + $1.rows.count }) rows across seven sections. This replaces the saved estate reference data. Production drafts are kept separately.")
            }
        }
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.visible, for: .navigationBar)
    }
    private func productionValues(_ row: ProductionDraft) -> String {
        "Harvested: \(row.harvested.map(String.init(describing:)) ?? "Pending") t; mill: \(row.sentToMill.map(String.init(describing:)) ?? "Pending") t; backlog: \(row.backlog.map(String.init(describing:)) ?? "Pending") t; hectares: \(row.hectares.map(String.init(describing:)) ?? "Pending"); harvesters: \(row.harvesters.map(String.init(describing:)) ?? "Pending")"
    }
    private func resolve(_ id: String, _ keepLocal: Bool) {
        do { try production.resolve(id, keepLocal: keepLocal); syncMessage = production.status }
        catch { syncMessage = error.localizedDescription }
    }
}

private struct UserProfileView: View {
    @AppStorage("profile_name") private var savedName = ""
    @AppStorage("profile_position") private var savedPosition = ""
    @AppStorage("profile_phone") private var savedPhone = ""
    @State private var name = ""
    @State private var position = ""
    @State private var phone = ""
    @State private var saved = false

    var body: some View {
        Form {
            Section {
                TextField("Display name", text: $name)
                TextField("Position (optional)", text: $position)
                TextField("Phone (optional)", text: $phone)
                    .keyboardType(.phonePad)
                Button("Save Profile") {
                    savedName = name.trimmingCharacters(in: .whitespacesAndNewlines)
                    savedPosition = position.trimmingCharacters(in: .whitespacesAndNewlines)
                    savedPhone = phone.trimmingCharacters(in: .whitespacesAndNewlines)
                    saved = true
                }
                .disabled(name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                if saved { Text("Profile saved on this device.") }
            } footer: {
                Text("Your profile on this device. This is not a signed-in account.")
            }
        }
        .navigationTitle("User Profile")
        .onAppear {
            name = savedName
            position = savedPosition
            phone = savedPhone
        }
        .onChange(of: name) { _ in saved = false }
        .onChange(of: position) { _ in saved = false }
        .onChange(of: phone) { _ in saved = false }
    }
}

private struct SettingsPlaceholder: View {
    let title: String
    let heading: String
    let message: String

    var body: some View {
        VStack(spacing: 16) {
            Text(heading).font(.title2.bold())
            Text(message).multilineTextAlignment(.center)
        }
        .padding()
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .navigationTitle(title)
    }
}
