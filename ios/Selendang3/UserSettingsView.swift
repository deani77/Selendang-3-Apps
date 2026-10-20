import SwiftUI

struct UserSettingsView: View {
    @AppStorage("theme") private var theme = "system"
    @State private var showSyncNotice = false

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
                Text("Profile and appearance are saved on this device. User accounts and synchronization are not configured.")
            }
            Section("Synchronization") {
                Text("Status: Not configured")
                Text("Last successful sync: Not synced yet")
                    .font(.footnote)
                Button("Sync Now") { showSyncNotice = true }
            }
        }
        .alert("Sync not configured", isPresented: $showSyncNotice) {
            Button("OK", role: .cancel) {}
        } message: {
            Text("The server is not connected yet. Your local data has not been sent or changed.")
        }
        .navigationTitle("Settings")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.visible, for: .navigationBar)
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
