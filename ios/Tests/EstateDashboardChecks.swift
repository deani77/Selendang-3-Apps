import Foundation

func panel(_ title: String, _ values: [EstateDashboardPanel]) -> EstateDashboardPanel { values.first { $0.title == title }! }
func section(_ id: String, _ columns: [String], _ rows: [[Any]]) -> [String:Any] { ["id":id,"columns":columns,"rows":rows] }
let months = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"]
var overview: [[Any]] = [
    ["Workforce reporting period","2026-09"],["Workforce staff",2],["Workforce local workers",7],["Workforce foreign workers",3],
    ["Workforce total workers",10],["Workforce contract workers",4],["Ratios reporting period","2026-09"],
    ["Ratios immature area (ha)",0],["Ratios mature area (ha)",1000],["Ratios mature operations workers",8],
    ["FFB cost YTD reporting period","2026-09"],["FFB cost budget year",2026],
    ["FFB cost operation actual YTD (RM/tonne)",123],["FFB cost operation budget year (RM/tonne)",100],
    ["FFB cost general charges actual YTD (RM/tonne)",45],["FFB cost general charges budget year (RM/tonne)",40]
]
let rainfall: [[Any]] = months.prefix(9).map { [2026,$0,10] } + [[2026,"Oct",999],[2026,"TOTAL (source)",9999]] + months.map { [2025,$0,1] }
var sections: [[String:Any]] = [
    section("overview",["Item","Details"],overview),
    section("fields",["Field code","Area (ha)","Total palms"],[["A",100,1000],["B",900,9000]]),
    section("ages",["Field / category","Age / years"],[["A","3 years"],["B","24 years"]]),
    section("soil",["Type","Category","Area (ha)","Share (%)","Suitability class","Class reference"],[
        ["Flat","Terrain",1000,100,NSNull(),NSNull()],["Hilly","Terrain",0,0,NSNull(),NSNull()],["Undulating","Terrain",0,0,NSNull(),NSNull()],
        ["Example series","Soil series",1000,100,"III","Fictional reference"]]),
    section("rainfall",["Year","Month / total","Rainfall (mm)"],rainfall),section("abw",["Value"],[]),section("roads",["Value"],[])
]
var fixture: [String:Any] = ["schemaVersion":1,"estateId":"selendang-3","revision":1,"reportingPeriod":"Fictional data",
    "exportedAt":"2026-10-01T00:00:00Z","sections":sections,"yph":["reportingYear":2026,"throughMonth":9,"matureAreaHa":1000,
    "actualYtdTonnes":25000,"budgetYtdTonnes":20000,"fullYearBudgetTonnes":30000,
    "history":[["year":2025,"actualYph":18],["year":2024,"actualYph":15],["year":2021,"actualYph":999]]]]
let october = ISO8601DateFormatter().date(from:"2026-10-22T00:00:00Z")!
func dashboard(_ fixture: [String:Any], _ date: Date = october) throws -> [EstateDashboardPanel] {
    let data = try JSONSerialization.data(withJSONObject:fixture)
    return EstateDashboardData(reference:try EstateReference.parse(data),today:date).panels
}
let panels = try dashboard(fixture)
precondition(panel("Age Profile",panels).rows[0][1] == "100.00")
precondition(panel("Age Profile",panels).rows[2][2] == "90.00%")
precondition(panel("Average SPH",panels).rows[0][1] == "10.00")
precondition(panel("Topography",panels).rows.count == 3)
precondition(panel("Soil Type",panels).rows[0][3] == "Class III*")
precondition(panel("Rainfall",panels).rows.count == 5 && panel("Rainfall",panels).rows[0][2] == "90.00")
precondition(panel("Rainfall",panels).rows[1][2] == "12.00")
precondition(panel("Workforce",panels).rows.map { $0[1] } == ["2","7","3","4","16"])
precondition(panel("Ratios",panels).rows[1][2] == "1:125.00")
precondition(panel("YPH",panels).rows[0][0] == "2022" && panel("YPH",panels).rows.last![1] == "25.00")
precondition(panel("Kos FFB/TAN",panels).rows[0] == ["Operation Cost","123.00","100.00"])
precondition(panel("Kos FFB/TAN",panels).bars.count == 4)
let november = ISO8601DateFormatter().date(from:"2026-11-01T00:00:00Z")!
let novemberPanels = try dashboard(fixture,november)
precondition(panel("Rainfall",novemberPanels).rows[0][2] == "1089.00")
let january = ISO8601DateFormatter().date(from:"2027-01-01T00:00:00Z")!
let januaryPanels = try dashboard(fixture,january)
precondition(panel("Rainfall",januaryPanels).rows[0][2] == "Pending")
sections[4] = section("rainfall",["Year","Month / total","Rainfall (mm)"],rainfall + [[2026,"Jan",0]])
fixture["sections"] = sections
let duplicatePanels = try dashboard(fixture)
precondition(panel("Rainfall",duplicatePanels).rows[0][2] == "Pending")
overview[9] = ["Ratios mature operations workers",0]
sections[0] = section("overview",["Item","Details"],overview); fixture["sections"] = sections
let zeroWorkerPanels = try dashboard(fixture)
precondition(panel("Ratios",zeroWorkerPanels).rows[1][2] == "Pending")
print("Dashboard model checks passed")
