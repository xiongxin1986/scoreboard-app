import SwiftUI

/// 选手/队伍
struct Player: Identifiable, Codable, Hashable {
    var id = UUID()
    var name: String
    var colorHex: String
    var score: Int = 0

    var color: Color { Color(hex: colorHex) }
}

extension Color {
    init(hex: String) {
        var h = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        if h.hasPrefix("#") { h.removeFirst() }
        var rgb: UInt64 = 0
        Scanner(string: h).scanHexInt64(&rgb)
        let r = Double((rgb >> 16) & 0xFF) / 255
        let g = Double((rgb >> 8) & 0xFF) / 255
        let b = Double(rgb & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}

/// 选手颜色备选
let playerPalette = [
    "#FF6B35", // 橙
    "#1A73E8", // 蓝
    "#188038", // 绿
    "#9334E6", // 紫
    "#E91E63", // 粉
    "#0097A7", // 青
    "#F9AB00", // 黄
    "#5F6368", // 灰
]
