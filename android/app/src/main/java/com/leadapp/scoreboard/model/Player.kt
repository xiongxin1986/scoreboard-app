package com.leadapp.scoreboard.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

/** 选手/队伍 */
data class Player(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var colorHex: String,
    var score: Int = 0
) {
    val color: Color get() = colorFromHex(colorHex)
}

fun colorFromHex(hex: String): Color {
    var h = hex.trim().removePrefix("#")
    // 支持 #RRGGBB
    val rgb = h.toLongOrNull(16) ?: 0L
    val r = ((rgb shr 16) and 0xFF) / 255f
    val g = ((rgb shr 8) and 0xFF) / 255f
    val b = (rgb and 0xFF) / 255f
    return Color(r, g, b)
}

/** 选手颜色备选（与 iOS 版一致） */
val playerPalette = listOf(
    "#FF6B35", // 橙
    "#1A73E8", // 蓝
    "#188038", // 绿
    "#9334E6", // 紫
    "#E91E63", // 粉
    "#0097A7", // 青
    "#F9AB00", // 黄
    "#5F6368", // 灰
)
