package com.leadapp.scoreboard.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 与 iOS 版一致的深色竞技场配色
val ArenaTop = Color(0xFF0A0D1A)
val ArenaBottom = Color(0xFF1A142E)
val CardBg = Color.White.copy(alpha = 0.07f)
val CardBorder = Color.White.copy(alpha = 0.10f)
val OrangeStart = Color(1.0f, 0.55f, 0.15f)
val OrangeEnd = Color(0.95f, 0.30f, 0.15f)
val PillBg = Color(0.16f, 0.16f, 0.22f, 0.92f)

val arenaBackground = Brush.verticalGradient(listOf(ArenaTop, ArenaBottom))
val orangeGradient = Brush.horizontalGradient(listOf(OrangeStart, OrangeEnd))

private val DarkColors = darkColorScheme(
    primary = OrangeStart,
    background = ArenaTop,
    surface = Color(0xFF14141F),
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun ScoreBoardTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
