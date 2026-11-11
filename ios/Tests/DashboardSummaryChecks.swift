import Foundation

// Public presentation results are checked against fixed expectations, so the
// home text and operational rows cannot silently drift after sharing calculations.
func summaryFixture() -> [String: Any] {
    let rows: [[Any]] = [
        ["FFB cost YTD reporting period", "2026-09"],
        ["FFB cost budget year", "2026"],
        ["FFB cost operation actual YTD (RM/tonne)", 123],
        ["FFB cost general charges actual YTD (RM/tonne)", 45]
    ]
    return [
        "schemaVersion": 1, "estateId": "selendang-3", "revision": 1,
        "reportingPeriod": "Fictional summary checks", "exportedAt": "2026-10-01T00:00:00Z",
        "sections": ["overview", "fields", "ages", "soil", "rainfall", "abw", "roads"].map {
            ["id": $0, "columns": ["Item", "Details"], "rows": $0 == "overview" ? rows : []] as [String: Any]
        },
        "yph": ["reportingYear": 2026, "throughMonth": 9, "matureAreaHa": 1000,
                "actualYtdTonnes": 25000, "budgetYtdTonnes": NSNull(),
                "fullYearBudgetTonnes": 30000, "history": []] as [String: Any]
    ]
}

func checkSummary(
    _ input: [String: Any]?, date: String = "2026-10-22T00:00:00Z",
    current: String = "Oct 2026", previous: String = "Sep 2026",
    ytd: String = "25.00", achievement: String = "83.33", yieldPeriod: String = "YTD through Sep 2026",
    operation: String = "123.00", charges: String = "45.00", costPeriod: String = "YTD through Sep 2026"
) throws {
    let reference = try input.map { try EstateReference.parse(JSONSerialization.data(withJSONObject: $0)) }
    let model = EstateDashboardData(reference: reference, today: ISO8601DateFormatter().date(from: date)!)
    let home = model.homeRows
    let cards = model.operationalCards
    let percent = achievement == "Pending" ? achievement : achievement + "%"
    precondition(home[0].note == previous + ": Pending · " + current + " MTD: Pending\n" +
        yieldPeriod + ": " + ytd + "\nYTD vs Year Budget: " + percent)
    precondition(cards[0].rows == [[previous, "Pending", "mt/ha"], [current + " MTD", "Pending", "mt/ha"],
        ["Actual YTD", ytd, "mt/ha"], ["YTD / Year Budget", achievement, "%"]])
    precondition(cards[0].note == yieldPeriod)
    precondition(home[2].note == "Operation: " + operation + " · GC: " + charges + "\n" + costPeriod)
    precondition(cards[6].rows == [["Operation Cost", operation, "RM/mt"], ["General Charges", charges, "RM/mt"]])
    precondition(cards[6].note == costPeriod)
}

try checkSummary(summaryFixture())
try checkSummary(nil, ytd: "Pending", achievement: "Pending", yieldPeriod: "YTD 2026",
    operation: "Pending", charges: "Pending", costPeriod: "YTD 2026")

// Null values are Pending, while measured zero and achievement above budget stay visible.
let achievementCases: [(Any, Any, String, String)] = [
    (0, 30000, "0.00", "0.00"),
    (60000, 30000, "60.00", "200.00"),
    (25000, 0, "25.00", "Pending"),
    (25000, NSNull(), "25.00", "Pending"),
    (NSNull(), 30000, "Pending", "Pending")
]
for (actual, annual, ytd, achievement) in achievementCases {
    var input = summaryFixture()
    var source = input["yph"] as! [String: Any]
    source["actualYtdTonnes"] = actual; source["fullYearBudgetTonnes"] = annual
    input["yph"] = source
    try checkSummary(input, ytd: ytd, achievement: achievement,
        yieldPeriod: actual is NSNull ? "YTD 2026" : "YTD through Sep 2026")
}

// Both outputs use the estate timezone, including the exact UTC instant of New Year.
try checkSummary(summaryFixture(), date: "2026-12-31T15:59:59Z", current: "Dec 2026", previous: "Nov 2026")
try checkSummary(summaryFixture(), date: "2026-12-31T16:00:00Z", current: "Jan 2027", previous: "Dec 2026",
    ytd: "Pending", achievement: "Pending", yieldPeriod: "YTD 2027",
    operation: "Pending", charges: "Pending", costPeriod: "YTD 2027")
try checkSummary(summaryFixture(), date: "2026-03-31T00:00:00Z", current: "Mar 2026", previous: "Feb 2026",
    ytd: "Pending", achievement: "Pending", yieldPeriod: "YTD 2026",
    operation: "Pending", charges: "Pending", costPeriod: "YTD 2026")
try checkSummary(summaryFixture(), date: "2024-03-31T00:00:00Z", current: "Mar 2024", previous: "Feb 2024",
    ytd: "Pending", achievement: "Pending", yieldPeriod: "YTD 2024",
    operation: "Pending", charges: "Pending", costPeriod: "YTD 2024")

// A future source month and a mismatched budget year must not leak into summaries.
var futureSummary = summaryFixture()
var futureYield = futureSummary["yph"] as! [String: Any]
futureYield["throughMonth"] = 12; futureSummary["yph"] = futureYield
try checkSummary(futureSummary, ytd: "Pending", achievement: "Pending", yieldPeriod: "YTD 2026")
var wrongBudgetSummary = summaryFixture()
var wrongBudgetSections = wrongBudgetSummary["sections"] as! [[String: Any]]
var wrongBudgetRows = wrongBudgetSections[0]["rows"] as! [[Any]]
wrongBudgetRows[1][1] = "2025"; wrongBudgetSections[0]["rows"] = wrongBudgetRows
wrongBudgetSummary["sections"] = wrongBudgetSections
try checkSummary(wrongBudgetSummary, operation: "Pending", charges: "Pending", costPeriod: "YTD 2026")

// Detailed panels may still show their dated historical source after the summary year expires.
let historicalSummary = EstateDashboardData(
    reference: try EstateReference.parse(JSONSerialization.data(withJSONObject: summaryFixture())),
    today: ISO8601DateFormatter().date(from: "2027-01-01T00:00:00Z")!
)
precondition(historicalSummary.panels.first { $0.title == "YPH" }!.rows.last! == ["2026 YTD", "25.00", "Pending"])
precondition(historicalSummary.panels.first { $0.title == "Kos FFB/TAN" }!.rows[0] == ["Operation Cost", "123.00", "Pending"])
print("Shared dashboard summaries: values, Pending/zero, budget achievement, source periods and Kuala Lumpur boundaries passed")
