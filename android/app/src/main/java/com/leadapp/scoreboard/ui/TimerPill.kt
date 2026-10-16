package com.leadapp.scoreboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leadapp.scoreboard.model.MatchState
import com.leadapp.scoreboard.model.TimerMode

/** 悬浮计时胶囊（与 iOS 版一致） */
@Composable
fun TimerPill(match: MatchState, onMenu: () -> Unit) {
    val timeColor = if (match.timerMode == TimerMode.COUNTDOWN && match.timerSeconds <= 60) {
        Color.Red
    } else {
        Color.White
    }
    Row(
        modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(PillBg)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.clickable { onMenu() }.padding(4.dp)) {
            Icon(Icons.Filled.Settings, contentDescription = "菜单",
                tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(24.dp))
        }
        Text(
            text = match.timeText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = timeColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 110.dp)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable {
                    if (match.timerRunning) match.pauseTimer() else match.startTimer()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (match.timerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (match.timerRunning) "暂停" else "开始",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable { match.resetTimer() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "重置",
                tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(24.dp))
        }
    }
}
