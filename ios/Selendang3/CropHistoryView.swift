import SwiftUI
import Charts

struct CropHistoryView: View {
    let history: EstateCropHistory?
    @State private var selected = "PM04D"
    private let codes = ["PM04D", "PM05E", "PM06F", "PM08G", "PM10K", "PM11M", "PM15P"]
    private var currentYear: Int { Calendar.current.component(.year, from: Date()) }
    var body: some View {
        GroupBox("Crop History · t/ha") {
            VStack(alignment: .leading, spacing: 12) {
                ScrollView(.horizontal) {
                    HStack { ForEach(codes, id: \.self) { code in
                        Button(code) { selected = code }.buttonStyle(.bordered)
                            .tint(selected == code ? .accentColor : .secondary)
                            .accessibilityAddTraits(selected == code ? .isSelected : [])
                    } }
                }
                if let history, let field = history.fields.first(where: { $0.fieldCode == selected }) {
                    Text("Production cutoff: " + history.cutoff + " · Completed years and partial YTD are not directly comparable.")
                        .font(.footnote)
                    Chart {
                        ForEach(field.years(currentYear: currentYear), id: \.self) { year in
                            if let value = field.yield(year: year) {
                                BarMark(x: .value("Year", String(year)), y: .value("t/ha", value))
                                    .foregroundStyle(year == field.productionYear ? Color.orange : Color.accentColor)
                            }
                        }
                    }.frame(height: 180).chartYAxisLabel("t/ha")
                    ForEach(field.years(currentYear: currentYear), id: \.self) { year in
                        HStack {
                            Text(String(year) + (year == field.productionYear ? " YTD" : ""))
                            Spacer()
                            Text(field.yield(year: year).map { String(format: "%.2f", $0) } ?? "N/A")
                        }
                    }
                    Text("Actual cumulative production: \(String(format: "%.2f", field.productionTonnes)) tonnes · Planted area: \(field.plantedAreaHa.map { String(format: "%.2f ha", $0) } ?? "N/A")")
                        .font(.footnote)
                    Text("YTD = actual cumulative tonnes ÷ planted ha. " + field.areaSource)
                        .font(.footnote).foregroundStyle(.secondary)
                    if field.plantedAreaHa == nil { Text("Planted hectares unavailable; YTD yield is N/A.") }
                } else {
                    Text("Crop history unavailable. Refresh Estate Data Sync.")
                }
            }.padding(.top, 8)
        }
    }
}
