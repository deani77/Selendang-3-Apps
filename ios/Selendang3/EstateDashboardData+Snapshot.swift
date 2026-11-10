import Foundation

struct DashboardField { let code: String; let area: Double; let palms: Double? }

extension EstateDashboardData {
    func table(_ id: String) -> EstateReferenceTable? { reference?.sections.first { $0.id == id } }
    func cell(_ row: [EstateReferenceCell?], _ table: EstateReferenceTable, _ column: String) -> String? {
        guard let index = table.columns.firstIndex(of:column), index < row.count else { return nil }
        return row[index]?.text
    }
    func numeric(_ text: String?) -> Double? {
        guard let value = text?.replacingOccurrences(of:",",with:"").trimmingCharacters(in:.whitespacesAndNewlines),
              let result = Double(value), result.isFinite, result >= 0 else { return nil }; return result
    }
    func count(_ text: String?) -> Double? {
        guard let value = numeric(text), value.rounded() == value, value <= 1e9 else { return nil }; return value
    }
    func overview(_ label: String) -> String? {
        guard let t = table("overview") else { return nil }
        let rows = t.rows.filter { cell($0,t,"Item") == label }
        return rows.count == 1 ? cell(rows[0],t,"Details") : nil
    }
    func period(_ key: String) -> String? {
        guard let value = overview(key), value.range(of:"^\\d{4}-(0[1-9]|1[0-2])$", options:.regularExpression) != nil else { return nil }
        return value
    }
    var fields: [DashboardField]? {
        guard let t = table("fields"), !t.rows.isEmpty else { return nil }
        var result: [DashboardField] = [], codes: Set<String> = []
        for row in t.rows {
            guard let code = cell(row,t,"Field code"), !code.isEmpty, codes.insert(code.uppercased()).inserted,
                  let area = numeric(cell(row,t,"Area (ha)")), area > 0 else { return nil }
            result.append(DashboardField(code:code,area:area,palms:count(cell(row,t,"Total palms"))))
        }
        return result
    }
}
