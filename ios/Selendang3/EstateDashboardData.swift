import Foundation

struct EstateYphHistory: Decodable { let year: Int; let actualYph: Double? }
struct EstateYph: Decodable {
    let reportingYear: Int
    let throughMonth: Int
    let matureAreaHa: Double
    let actualYtdTonnes: Double?
    let budgetYtdTonnes: Double?
    let fullYearBudgetTonnes: Double?
    let history: [EstateYphHistory]
}
struct DashboardBar: Identifiable {
    let category: String
    let series: String
    let value: Double
    var id: String { category + "/" + series }
}
struct EstateDashboardPanel: Identifiable {
    let title: String
    let headers: [String]
    let rows: [[String]]
    var note: String = ""
    var bars: [DashboardBar] = []
    var unit: String = ""
    var id: String { title }
}
private struct DashboardField { let code: String; let area: Double; let palms: Double? }

struct EstateDashboardData {
    let reference: EstateReference?
    let today: Date
    static func number(_ value: Double?) -> String {
        guard let value = value, value.isFinite else { return "Pending" }
        return String(format:"%0.2f",locale:Locale(identifier:"en_US_POSIX"),value)
    }
    private func table(_ id: String) -> EstateReferenceTable? { reference?.sections.first { $0.id == id } }
    private func cell(_ row: [EstateReferenceCell?], _ table: EstateReferenceTable, _ column: String) -> String? {
        guard let index = table.columns.firstIndex(of:column), index < row.count else { return nil }
        return row[index]?.text
    }
    private func numeric(_ text: String?) -> Double? {
        guard let value = text?.replacingOccurrences(of:",",with:"").trimmingCharacters(in:.whitespacesAndNewlines),
              let result = Double(value), result.isFinite, result >= 0 else { return nil }; return result
    }
    private func count(_ text: String?) -> Double? {
        guard let value = numeric(text), value.rounded() == value, value <= 1e9 else { return nil }; return value
    }
    private func overview(_ label: String) -> String? {
        guard let t = table("overview") else { return nil }
        let rows = t.rows.filter { cell($0,t,"Item") == label }
        return rows.count == 1 ? cell(rows[0],t,"Details") : nil
    }
    private func period(_ key: String) -> String? {
        guard let value = overview(key), value.range(of:"^\\d{4}-(0[1-9]|1[0-2])$", options:.regularExpression) != nil else { return nil }
        return value
    }
    private var fields: [DashboardField]? {
        guard let t = table("fields"), !t.rows.isEmpty else { return nil }
        var result: [DashboardField] = [], codes: Set<String> = []
        for row in t.rows {
            guard let code = cell(row,t,"Field code"), !code.isEmpty, codes.insert(code.uppercased()).inserted,
                  let area = numeric(cell(row,t,"Area (ha)")), area > 0 else { return nil }
            result.append(DashboardField(code:code,area:area,palms:count(cell(row,t,"Total palms"))))
        }
        return result
    }
    private var area: Double? { fields.map { $0.reduce(0) { $0 + $1.area } }.flatMap { $0.isFinite ? $0 : nil } }
    private func share(_ area: Double?, _ total: Double?) -> String {
        guard let area = area, let total = total, total > 0 else { return "Pending" }
        return Self.number(area/total*100)+"%"
    }
    private var ages: EstateDashboardPanel {
        let names = ["Young · 1–5 years","Prime · 6–23 years","Old · >23 years"]
        var sums = [0.0,0.0,0.0], complete = true
        if let fields = fields, let t = table("ages") {
            for field in fields {
                let rows = t.rows.filter { cell($0,t,"Field / category") == field.code }
                guard rows.count == 1, let text = cell(rows[0],t,"Age / years"),
                      let age = Int(text.replacingOccurrences(of:" years",with:"").trimmingCharacters(in:.whitespaces)), age > 0 else { complete = false; break }
                sums[age <= 5 ? 0 : age <= 23 ? 1 : 2] += field.area
            }
        } else { complete = false }
        return EstateDashboardPanel(title:"Age Profile",headers:["Age category","Area (ha)","Share"],
            rows:names.indices.map { [names[$0],complete ? Self.number(sums[$0]) : "Pending",complete ? share(sums[$0],area) : "Pending"] },
            note:"From saved field ages. Total area: " + Self.number(area) + " ha.")
    }
    private func land(_ category: String, title: String) -> EstateDashboardPanel {
        let soilSeries = category == "Soil series"
        let headers = soilSeries ? ["Soil series","Area (ha)","Share","Reference class"] : ["Terrain","Area (ha)","Share"]
        guard let t = table("soil"), let total = area else { return EstateDashboardPanel(title:title,headers:headers,rows:[],note:"Pending complete estate records") }
        let source = t.rows.filter { cell($0,t,"Category") == category }
        var rows: [[String]] = [], sum = 0.0, shares = 0.0, names: Set<String> = []
        for row in source {
            guard let name = cell(row,t,"Type"), names.insert(name).inserted,
                  let hectares = numeric(cell(row,t,"Area (ha)")), let percent = numeric(cell(row,t,"Share (%)")), percent <= 100,
                  abs(hectares-total*percent/100) <= 0.011 else {
                return EstateDashboardPanel(title:title,headers:headers,rows:[],note:"Pending complete area and percentage records")
            }
            sum += hectares; shares += percent
            var values = [name,Self.number(hectares),Self.number(percent)+"%"]
            if soilSeries {
                let rating = cell(row,t,"Suitability class") ?? ""
                let citation = cell(row,t,"Class reference") ?? ""
                values.append(["I","II","III","IV","V"].contains(rating) && !citation.isEmpty ? "Class "+rating+"*" : "Pending")
            }
            rows.append(values)
        }
        guard !rows.isEmpty, abs(sum-total) <= 0.011, abs(shares-100) <= 0.001,
              soilSeries || names == Set(["Flat","Hilly","Undulating"]) else {
            return EstateDashboardPanel(title:title,headers:headers,rows:[],note:"Pending reconciled area records")
        }
        return EstateDashboardPanel(title:title,headers:headers,rows:rows,
            note:soilSeries ? "* Historical agricultural suitability reference; estate verification pending. See Soil and Topography in Estate Details for sources." : "Total area: "+Self.number(total)+" ha")
    }
    private var sph: EstateDashboardPanel {
        var total: Double?
        if let fields = fields, fields.allSatisfy({ $0.palms != nil }) { total = fields.reduce(0) { $0 + $1.palms! } }
        let density: Double? = { guard let total = total, let area = area, area > 0 else { return nil }; return total/area }()
        return EstateDashboardPanel(title:"Average SPH",headers:["Measure","Value"],rows:[["Palms/ha",Self.number(density)]],note:"Total palms across all fields ÷ total estate hectares")
    }
    private var yph: EstateDashboardPanel {
        guard let data = reference?.yph, (1...12).contains(data.throughMonth), data.reportingYear >= 2000,
              data.matureAreaHa.isFinite, data.matureAreaHa > 0 else {
            return EstateDashboardPanel(title:"YPH",headers:["Year / period","Actual","Budget"],rows:[],note:"Pending YPH records")
        }
        var rows: [[String]] = [], bars: [DashboardBar] = []
        for year in (data.reportingYear-4)..<data.reportingYear {
            let matches = data.history.filter { $0.year == year }
            let value = matches.count == 1 ? matches[0].actualYph.flatMap { $0.isFinite && $0 >= 0 ? $0 : nil } : nil
            rows.append([String(year),Self.number(value),"—"])
            if let value = value { bars.append(DashboardBar(category:String(year),series:"Actual",value:value)) }
        }
        func yield(_ tonnes: Double?) -> Double? { tonnes.flatMap { $0.isFinite && $0 >= 0 ? $0/data.matureAreaHa : nil } }
        let actual = yield(data.actualYtdTonnes), budget = yield(data.budgetYtdTonnes)
        let year = String(data.reportingYear)+" YTD"
        rows.append([year,Self.number(actual),Self.number(budget)])
        if let actual = actual { bars.append(DashboardBar(category:year,series:"Actual",value:actual)) }
        if let budget = budget { bars.append(DashboardBar(category:year,series:"Budget",value:budget)) }
        let month = DateFormatter().monthSymbols[data.throughMonth-1]
        return EstateDashboardPanel(title:"YPH",headers:["Year / period","Actual","Budget"],rows:rows,
            note:"YTD January–\(month) \(data.reportingYear). Full-year budget: \(Self.number(yield(data.fullYearBudgetTonnes))) t/ha.",bars:bars,unit:"t/ha")
    }
    private var rainfall: EstateDashboardPanel {
        var calendar = Calendar(identifier:.gregorian); calendar.timeZone = TimeZone(identifier:"Asia/Kuala_Lumpur")!
        let year = calendar.component(.year,from:today), cutoff = calendar.component(.month,from:today)-1
        let months = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]
        var rows: [[String]] = []
        for y in stride(from:year,through:year-4,by:-1) {
            let end = y == year ? cutoff : 12
            var values: [Int:Double] = [:], duplicate = false
            var seen: Set<Int> = []
            if let t = table("rainfall") {
                for row in t.rows where cell(row,t,"Year") == String(y) {
                    if let name = cell(row,t,"Month / total"), let index = months.firstIndex(where:{ $0.caseInsensitiveCompare(name) == .orderedSame }), index < end {
                        if !seen.insert(index).inserted { duplicate = true }
                        if let value = numeric(cell(row,t,"Rainfall (mm)")) { values[index] = value }
                    }
                }
            }
            let total: Double? = end > 0 && values.count == end && !duplicate ? values.values.reduce(0,+) : nil
            let period = y != year ? "Full year" : end == 0 ? "No completed month" : "YTD Jan–"+months[end-1]
            rows.append([String(y),period,Self.number(total)])
        }
        return EstateDashboardPanel(title:"Rainfall",headers:["Year","Period","Total (mm)"],rows:rows,note:"Five calendar years, newest first. Current month excluded; missing months remain Pending.")
    }
    private var workforce: EstateDashboardPanel {
        let staff = count(overview("Workforce staff")), local = count(overview("Workforce local workers")), foreign = count(overview("Workforce foreign workers"))
        let direct = count(overview("Workforce total workers")), contractors = count(overview("Workforce contract workers"))
        let complete = period("Workforce reporting period") != nil && staff != nil && local != nil && foreign != nil && direct != nil && local!+foreign! == direct!
        func label(_ value: Double?) -> String { guard complete, let value = value else { return "Pending" }; return String(format:"%.0f",value) }
        let total: Double? = complete && contractors != nil ? staff!+direct!+contractors! : nil
        return EstateDashboardPanel(title:"Workforce",headers:["Category","People"],rows:[
            ["Staff",label(staff)],["Local Workers",label(local)],["Foreign Workers",label(foreign)],
            ["Contract Workers",label(contractors)],["Total Workforce",label(total)]],
            note:"Source month: "+(period("Workforce reporting period") ?? "Pending")+". Contractors are separate from local/foreign worker counts.")
    }
    private var ratios: EstateDashboardPanel {
        let immature = numeric(overview("Ratios immature area (ha)")), mature = numeric(overview("Ratios mature area (ha)")), workers = count(overview("Ratios mature operations workers"))
        let complete = period("Ratios reporting period") != nil && immature != nil && mature != nil && workers != nil && area != nil && abs(immature!+mature!-area!) <= 0.011
        let value: String = complete && mature! > 0 && workers! > 0 ? "1:"+Self.number(mature!/workers!) : "Pending"
        return EstateDashboardPanel(title:"Ratios",headers:["Maturity","Area (ha)","Workers:ha"],rows:[
            ["Immature",complete ? Self.number(immature) : "Pending",complete && immature == 0 ? "—" : "Pending"],
            ["Mature",complete ? Self.number(mature) : "Pending",complete && mature == 0 ? "—" : value]],
            note:"Source month: "+(period("Ratios reporting period") ?? "Pending")+". Uses mature-area operations workers only.")
    }
    private var costs: EstateDashboardPanel {
        let actualPeriod = period("FFB cost YTD reporting period"), budgetYear = overview("FFB cost budget year")
        let valid = actualPeriod != nil && budgetYear == String(actualPeriod!.prefix(4))
        let pairs = [("Operation Cost","operation"),("General Charges","general charges")]
        var rows: [[String]] = [], bars: [DashboardBar] = []
        for (label,key) in pairs {
            let actual = valid ? numeric(overview("FFB cost \(key) actual YTD (RM/tonne)")) : nil
            let budget = valid ? numeric(overview("FFB cost \(key) budget year (RM/tonne)")) : nil
            rows.append([label,Self.number(actual),Self.number(budget)])
            if let budget = budget { bars.append(DashboardBar(category:label,series:"Budget Year",value:budget)) }
            if let actual = actual { bars.append(DashboardBar(category:label,series:"Actual YTD",value:actual)) }
        }
        return EstateDashboardPanel(title:"Kos FFB/TAN",headers:["Measure","Actual YTD","Budget Year"],rows:rows,
            note:"Actual YTD through \(actualPeriod ?? "Pending") vs Budget Year \(budgetYear ?? "Pending"). Operation Cost = Penjagaan + Membaja + Pungutan; General Charges = Perbelanjaan Am.",bars:bars,unit:"RM/tonne")
    }
    var panels: [EstateDashboardPanel] {
        [ages,land("Terrain",title:"Topography"),yph,sph,land("Soil series",title:"Soil Type"),rainfall,workforce,ratios,costs,
         EstateDashboardPanel(title:"FFB Quality",headers:["Category","Value"],rows:["Ripe Bunches (%)","Unripe (%)","Empty & Rotten (%)","Stone/Trash (ton)"].map { [$0,"Pending"] }),
         EstateDashboardPanel(title:"Actual versus Budget · YTD",headers:["Item","Actual","Budget"],rows:["OER (%)","KER (%)","External Supply (t)","LF"].map { [$0,"Pending","Pending"] }),
         EstateDashboardPanel(title:"Cost Breakdown",headers:["Measure","Value"],rows:[["Awaiting approved records","Pending"]]),
         EstateDashboardPanel(title:"Checkroll Average",headers:["Category","RM/Mdy","% O/Turn"],rows:[["Harvester","Pending","Pending"],["Field Worker","Pending","Pending"]])]
    }
}
