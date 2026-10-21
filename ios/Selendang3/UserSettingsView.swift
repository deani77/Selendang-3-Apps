import SwiftUI

struct UserSettingsView: View {
    @AppStorage("theme") private var theme = "system"
    @StateObject private var production = ProductionStore.shared
    @AppStorage("server_address") private var address = ""
    @State private var accessToken = ""
    @State private var syncMessage: String?

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
            Section("Production synchronization") {
                TextField("Full HTTPS snapshot address", text: $address).textInputAutocapitalization(.never).autocorrectionDisabled()
                SecureField("Access token", text: $accessToken)
                Text(production.status)
                Text("Uploads saved daily drafts and downloads production from other phones. Estate reference download is currently available in Android only.").font(.footnote)
                Button(production.syncing ? "Syncing…" : "Sync Now") {
                    Task {
                        do { try await production.sync(address: address, token: accessToken); syncMessage = production.status }
                        catch { syncMessage = "Sync failed: \(error.localizedDescription) Local drafts are retained." }
                    }
                }.disabled(production.syncing || accessToken.isEmpty || address.isEmpty || production.storageError != nil)
                if let error = production.storageError { Text(error).foregroundStyle(.red) }
                if let message = syncMessage { Text(message) }
            }
            ForEach(production.entries.filter { $0.conflict != nil }) { entry in
                Section("Conflict: \(entry.record.field) · \(entry.record.date)") {
                    Text("Both copies are preserved. Compare all values before choosing.")
                    Text("Phone: " + productionValues(entry.record))
                    if let remote = entry.conflict { Text("Server: " + productionValues(remote.record)) }
                    Button("Use server copy") { resolve(entry.id, false) }.disabled(production.syncing)
                    Button("Keep phone copy · upload on next Sync") { resolve(entry.id, true) }.disabled(production.syncing)
                }
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
