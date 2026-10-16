package com.leadapp.scoreboard.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.leadapp.scoreboard.model.MatchState
import com.leadapp.scoreboard.model.Player
import com.leadapp.scoreboard.model.TimerMode

/**
 * 记分主界面：全屏铺色 + 悬浮计时胶囊（与 iOS 版一致）
 * 横屏时胶囊放顶部，竖屏放中间
 */
@Composable
fun ScoreboardScreen(match: MatchState, onExit: () -> Unit) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var showResetConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 全屏铺色，无黑白边
        ScoreGrid(match = match, isLandscape = isLandscape, modifier = Modifier.fillMaxSize())

        // 计时胶囊：横屏放顶部，竖屏放中间
        if (match.timerMode != TimerMode.NONE) {
            if (isLandscape) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        TimerPill(match = match, onMenu = { showMenu = true })
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    TimerPill(match = match, onMenu = { showMenu = true })
                }
            }
        } else {
            // 无计时模式时，右上角小齿轮进菜单
            Box(modifier = Modifier.fillMaxSize().padding(top = 54.dp, end = 16.dp),
                contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.25f))
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "菜单",
                        tint = Color.White.copy(alpha = 0.85f))
                }
            }
        }

        match.winner?.let { winner ->
            WinnerOverlay(winner = winner,
                onRematch = { match.resetScores() },
                onExit = onExit)
        }
    }

    if (showMenu) {
        MenuDialog(
            onDismiss = { showMenu = false },
            onNewMatch = { showMenu = false; onExit() },
            onResetScores = { showMenu = false; showResetConfirm = true }
        )
    }
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("比分清零？") },
            confirmButton = {
                TextButton(onClick = { match.resetScores(); showResetConfirm = false }) {
                    Text("清零", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("取消") }
            }
        )
    }
}

/** 记分卡布局（无缝隙铺满）。3 人时第三人占满底行，不留黑格。 */
@Composable
private fun ScoreGrid(match: MatchState, isLandscape: Boolean, modifier: Modifier = Modifier) {
    val players = match.players
    when {
        players.size <= 2 -> {
            if (isLandscape) {
                Row(modifier = modifier) {
                    players.forEachIndexed { i, p ->
                        PlayerCard(
                            player = p,
                            onTap = { match.tap(p) },
                            onCorrect = { match.correct(p) },
                            minusAlignment = if (i == 0) Alignment.BottomEnd else Alignment.BottomStart,
                            isLandscape = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Column(modifier = modifier) {
                    players.forEachIndexed { i, p ->
                        PlayerCard(
                            player = p,
                            onTap = { match.tap(p) },
                            onCorrect = { match.correct(p) },
                            minusAlignment = if (i == 0) Alignment.BottomEnd else Alignment.TopEnd,
                            isLandscape = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        else -> {
            Column(modifier = modifier) {
                Row(modifier = Modifier.weight(1f)) {
                    players.take(2).forEachIndexed { i, p ->
                        PlayerCard(
                            player = p,
                            onTap = { match.tap(p) },
                            onCorrect = { match.correct(p) },
                            minusAlignment = if (i == 0) Alignment.BottomEnd else Alignment.BottomStart,
                            isLandscape = isLandscape,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f)) {
                    val bottom = players.drop(2)
                    if (bottom.size == 1) {
                        // 3 人：第三人占满整行，不留黑格
                        val p = bottom[0]
                        PlayerCard(
                            player = p,
                            onTap = { match.tap(p) },
                            onCorrect = { match.correct(p) },
                            minusAlignment = Alignment.TopEnd,
                            isLandscape = isLandscape,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        bottom.forEachIndexed { i, p ->
                            PlayerCard(
                                player = p,
                                onTap = { match.tap(p) },
                                onCorrect = { match.correct(p) },
                                minusAlignment = if (i == 0) Alignment.TopEnd else Alignment.TopStart,
                                isLandscape = isLandscape,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuDialog(onDismiss: () -> Unit, onNewMatch: () -> Unit, onResetScores: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1A1A28))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MenuItem("新比赛", onNewMatch)
            MenuItem("比分清零", onResetScores, destructive = true)
            MenuItem("取消", onDismiss)
        }
    }
}

@Composable
private fun MenuItem(text: String, onClick: () -> Unit, destructive: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (destructive) Color.Red else Color.White, fontSize = 17.sp)
    }
}

/** 获胜横幅 */
@Composable
private fun WinnerOverlay(winner: Player, onRematch: () -> Unit, onExit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .shadow(30.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF1A1A29))
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("🏆", fontSize = 72.sp)
            Text(
                "${winner.name} 获胜！",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.verticalGradient(listOf(Color.White, winner.color))
                )
            )
            Text("最终比分 ${winner.score}", fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.7f))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(orangeGradient)
                        .clickable { onRematch() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("再来一局", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onExit() }
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("回主菜单", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}
