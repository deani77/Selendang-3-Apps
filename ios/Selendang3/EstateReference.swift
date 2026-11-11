import Foundation

// Unicode White_Space plus BOM, shared with the Kotlin and server parsers.
private let snapshotWhitespace = CharacterSet(charactersIn: "\u{0009}\u{000a}\u{000b}\u{000c}\u{000d}\u{0020}\u{0085}\u{00a0}\u{1680}\u{2000}\u{2001}\u{2002}\u{2003}\u{2004}\u{2005}\u{2006}\u{2007}\u{2008}\u{2009}\u{200a}\u{2028}\u{2029}\u{202f}\u{205f}\u{3000}\u{feff}")

struct EstateReferenceCell: Decodable {
    let text: String
    init(from decoder: Decoder) throws {
        let value = try decoder.singleValueContainer()
        if let string = try? value.decode(String.self) {
            guard string.utf16.count <= 1000 else { throw ProductionError.invalid("Estate cell is too long.") }
            text = string
        } else {
            let number = try value.decode(Double.self)
            guard number.isFinite else { throw ProductionError.invalid("Invalid estate number.") }
            text = number.rounded() == number ? String(format: "%.0f", locale: Locale(identifier: "en_US_POSIX"), number) : String(number)
        }
    }
}
struct EstateReferenceTable: Decodable, Identifiable {
    let id: String
    let columns: [String]
    let rows: [[EstateReferenceCell?]]
    var title: String {
        ["overview":"Estate Overview", "fields":"Field Information", "ages":"Age Profile", "soil":"Soil and Topography",
         "rainfall":"Rainfall", "abw":"Average Bunch Weight", "roads":"Road Information"][id] ?? id
    }
}
struct EstateReference: Decodable {
    let schemaVersion: Int
    let estateId: String
    let revision: Int
    let reportingPeriod: String
    let exportedAt: String
    let sections: [EstateReferenceTable]
    let cropHistory: EstateCropHistory?
    let fieldProfiles: [EstateSourceFieldProfile]?
    let yph: EstateYph?
    let cropProductionReport: EstateCropReport?
    let cropProductionReportHistory: [EstateCropReport]?
    let manuringReport: EstateManuringReport?
    let cropBudget: EstateCropBudget?
    let ffbRejectReport: EstateFfbRejectReport?
    var estateName: String? {
        guard let overview = sections.first(where: { $0.id == "overview" }),
              let item = overview.columns.firstIndex(of: "Item"),
              let details = overview.columns.firstIndex(of: "Details") else { return nil }
        let matches = overview.rows.filter { $0[item]?.text.trimmingCharacters(in: .whitespacesAndNewlines) == "Estate name" }
        guard matches.count == 1, let value = matches[0][details]?.text.trimmingCharacters(in: .whitespacesAndNewlines),
              !value.isEmpty, value.utf16.count <= 120, value.rangeOfCharacter(from: .controlCharacters) == nil else { return nil }
        return value
    }
    static func parse(_ data: Data) throws -> EstateReference {
        guard data.count <= 2*1024*1024 else { throw ProductionError.invalid("Estate update exceeds 2 MB.") }
        let result = try JSONDecoder().decode(EstateReference.self, from: data)
        let expected: Set<String> = ["overview","fields","ages","soil","rainfall","abw","roads"]
        guard result.schemaVersion == 1, result.estateId == "selendang-3", result.revision > 0, result.revision <= 9_007_199_254_740_991,
              !result.reportingPeriod.trimmingCharacters(in: snapshotWhitespace).isEmpty, result.reportingPeriod.utf16.count <= 100,
              validExportTimestamp(result.exportedAt),
              result.sections.count == 7, Set(result.sections.map(\.id)) == expected,
              result.sections.reduce(0, { $0 + $1.rows.count }) <= 10000 else {
            throw ProductionError.invalid("Invalid estate update or wrong estate.")
        }
        for table in result.sections {
            guard (1...20).contains(table.columns.count), Set(table.columns).count == table.columns.count,
                  table.columns.allSatisfy({ !$0.trimmingCharacters(in: snapshotWhitespace).isEmpty && $0.utf16.count <= 80 }),
                  table.rows.allSatisfy({ $0.count == table.columns.count }) else {
                throw ProductionError.invalid("Invalid estate table.")
            }
        }
        if let profiles = result.fieldProfiles {
            let codes = profiles.map { $0.fieldCode.trimmingCharacters(in: snapshotWhitespace) }
            guard Set(codes).count == codes.count, profiles.allSatisfy({ $0.isValid }) else {
                throw ProductionError.invalid("Invalid or duplicate field profile. Check source field and block details.")
            }
        }
        if let yph = result.yph { try yph.validate() }
        return result
    }

    // Match Android's strict offset date parser; Foundation's lenient date parsing
    // and JavaScript Date.parse can normalize invalid dates instead of rejecting them.
    private static func validExportTimestamp(_ value: String) -> Bool {
        let pattern = #"^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})(?:\.[0-9]{1,9})?(Z|[+-]([0-9]{2}):([0-9]{2}))$"#
        let regex = try! NSRegularExpression(pattern: pattern)
        let range = NSRange(value.startIndex..., in: value)
        guard let match = regex.firstMatch(in: value, range: range), match.range == range else { return false }
        let text = value as NSString
        func part(_ index: Int) -> Int { Int(text.substring(with: match.range(at: index)))! }
        let year = part(1), month = part(2), day = part(3)
        guard (1...9999).contains(year), (1...12).contains(month), (0...23).contains(part(4)),
              (0...59).contains(part(5)), (0...59).contains(part(6)) else { return false }
        let leap = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
        let days = [31, leap ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
        guard (1...days[month - 1]).contains(day) else { return false }
        if text.substring(with: match.range(at: 7)) != "Z" {
            let hours = part(8), minutes = part(9)
            guard hours <= 18, minutes <= 59, hours < 18 || minutes == 0 else { return false }
        }
        return true
    }
}

struct EstateSourceFieldProfile: Decodable {
    let fieldCode: String
    let reportYear: Int
    let plantedAreaHa: Double
    let unplantedAreaHa: Double
    let divisionStandsPerHa: Double
    let divisionPalmHeightM: Double
    let maturity: String
    let blocks: [EstateSourceBlock]
}
struct EstateSourceBlock: Decodable {
    let blockId: String
    let blockName: String
    let plantedAreaHa: Double
    let unplantedAreaHa: Double
    let soilCode: String?
    let harvestingYear: Int
    let sourceNote: String?
}

private func profileText(_ value: String) -> Bool {
    !value.trimmingCharacters(in: snapshotWhitespace).isEmpty
}
private func profileMeasures(_ values: [Double]) -> Bool {
    values.allSatisfy { $0.isFinite && $0 >= 0 }
}
extension EstateSourceFieldProfile {
    var isValid: Bool {
        let ids = blocks.map { $0.blockId.trimmingCharacters(in: snapshotWhitespace) }
        return profileText(fieldCode) && profileText(maturity) && (1...9999).contains(reportYear) &&
            profileMeasures([plantedAreaHa, unplantedAreaHa, divisionStandsPerHa, divisionPalmHeightM]) &&
            Set(ids).count == ids.count && blocks.allSatisfy {
                profileText($0.blockId) && profileText($0.blockName) && (1...9999).contains($0.harvestingYear) &&
                    profileMeasures([$0.plantedAreaHa, $0.unplantedAreaHa])
            }
    }
}

extension EstateYph {
    func validate() throws {
        func valid(_ value: Double?) -> Bool { value == nil || (value!.isFinite && value! >= 0) }
        guard (1900...2200).contains(reportingYear), (1...12).contains(throughMonth),
              matureAreaHa.isFinite, matureAreaHa > 0,
              valid(actualYtdTonnes), valid(budgetYtdTonnes), valid(fullYearBudgetTonnes),
              budgetYtdTonnes == nil || fullYearBudgetTonnes == nil || budgetYtdTonnes! <= fullYearBudgetTonnes!,
              history.count <= 50, Set(history.map(\.year)).count == history.count,
              history.allSatisfy({ $0.year >= 1900 && $0.year < reportingYear && valid($0.actualYph) }) else {
            throw ProductionError.invalid("Invalid YPH period, area, budget or history.")
        }
    }
}

struct EstateSyncEndpoint {
    let snapshot: URL
    let token: String
    init(address: String, token: String) throws {
        let cleanAddress = address.trimmingCharacters(in: .whitespacesAndNewlines)
        let cleanToken = token.trimmingCharacters(in: .whitespacesAndNewlines)
        guard let url = URLComponents(string: cleanAddress), url.scheme == "https", url.host?.isEmpty == false,
              url.user == nil, url.password == nil, url.query == nil, url.fragment == nil,
              url.path == "/v1/estates/selendang-3/snapshot", let snapshot = url.url,
              !cleanToken.isEmpty, !cleanToken.contains("\r"), !cleanToken.contains("\n") else {
            throw ProductionError.invalid("Enter the full HTTPS snapshot address and the access token only (without Bearer).")
        }
        self.snapshot = snapshot; self.token = cleanToken
    }
}

// Optional report extensions keep older offline snapshots compatible.
struct EstateCropReport: Decodable {
    let reportDate: String
    let isCompleteMonth: Bool?
    let fields: [EstateCropField]?
    let totals: EstateCropTotals
}
struct EstateCropTotals: Decodable {
    let sentToMillMtdMtPerHa: Double?
    let hectaresToday: Double?
    let hectaresMtd: Double?
    let roundsMtd: Double?
    let mtPerHarvesterDayMtd: Double?
    let haPerHarvesterDayMtd: Double?
}
struct EstateManuringReport: Decodable {
    let reportDate: String
    let annualProgrammeMt: Double?
    let appliedYtdMt: Double?
}

struct EstateCropField: Decodable {
    let areaHa: Double?
    let roundsMtd: Double?
    let mtPerHarvesterDayMtd: Double?
    let haPerHarvesterDayMtd: Double?
    let field: String
    let sentToMillMtdMt: Double?
    let sentToMillYtdMt: Double?

    private enum CodingKeys: String, CodingKey {
        case areaHa, roundsMtd, mtPerHarvesterDayMtd, haPerHarvesterDayMtd
        case field, sentToMillMtdMt, sentToMillYtdMt
    }
    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        // An unusable optional historical area leaves yield pending, without
        // preventing the rest of an existing estate snapshot from loading.
        areaHa = try? values.decode(Double.self, forKey: .areaHa)
        roundsMtd = try values.decodeIfPresent(Double.self, forKey: .roundsMtd)
        mtPerHarvesterDayMtd = try values.decodeIfPresent(Double.self, forKey: .mtPerHarvesterDayMtd)
        haPerHarvesterDayMtd = try values.decodeIfPresent(Double.self, forKey: .haPerHarvesterDayMtd)
        field = try values.decode(String.self, forKey: .field)
        sentToMillMtdMt = try values.decodeIfPresent(Double.self, forKey: .sentToMillMtdMt)
        sentToMillYtdMt = try values.decodeIfPresent(Double.self, forKey: .sentToMillYtdMt)
    }
}
struct EstateCropBudget: Decodable {
    let year: Int
    let monthlyBudget: [EstateCropBudgetMonth]
}
struct EstateCropBudgetMonth: Decodable {
    let field: String
    let year: Int
    let month: Int
    let tonnes: Double?
}

struct EstateFfbRejectReport: Decodable {
    let reportDate: String
    let totals: EstateFfbRejectTotals
}
struct EstateFfbRejectTotals: Decodable {
    let rejectMtdMt: Double?
    let rejectMtdPercent: Double?
    let rejectYtdMt: Double?
    let rejectYtdPercent: Double?
}

struct EstateCropHistory: Decodable {
    let cutoff: String
    let fields: [EstateCropHistoryField]
}
struct EstateCropHistoryField: Decodable {
    let fieldCode: String
    let annual: [String: Double?]
    let productionYear: Int
    let productionTonnes: Double
    let plantedAreaHa: Double?
    let areaSource: String
    func yield(year: Int) -> Double? {
        if year != productionYear { return annual[String(year)] ?? nil }
        guard let area = plantedAreaHa, area.isFinite, area > 0,
              productionTonnes.isFinite, productionTonnes >= 0 else { return nil }
        return productionTonnes / area
    }
    func years(currentYear: Int) -> [Int] { Array((currentYear - 4)...currentYear) }
}
