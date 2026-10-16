package com.leadapp.scoreboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leadapp.scoreboard.model.Player

/**
 * 单个选手的记分卡：全屏铺色，点大区域加分
 * -1 按钮放在靠屏幕内侧的角（与 iOS 版一致）
 */
@Composable
fun PlayerCard(
    player: Player,
    onTap: () -> Unit,
    onCorrect: () -> Unit,
    minusAlignment: Alignment,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = minusAlignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(player.color, player.color.copy(alpha = 0.55f))
                    )
                )
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTap()
                }
        ) {
            // 顶部高光
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                            endY = 400f
                        )
                    )
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = player.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(top = if (isLandscape) 110.dp else 54.dp)
                        .shadow(4.dp)
                )
                Spacer(Modifier.weight(1f))
                // 自适应巨大的比分数字
                BoxWithConstraints(
                    modifier = Modifier.weight(2f).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val s = minOf(maxWidth * 0.62f, maxHeight * 0.62f)
                    val fontSize = maxOf(60.sp, s.value.sp)
                    Text(
                        text = "${player.score}",
                        fontSize = fontSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.shadow(12.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
        // -1 纠错按钮
        Box(
            modifier = Modifier
                .padding(18.dp)
                .size(56.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
                .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .clickable { onCorrect() },
            contentAlignment = Alignment.Center
        ) {
            Text("-1", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
        }
    }
}
