package com.leadapp.scoreboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leadapp.scoreboard.model.GameSettings
import com.leadapp.scoreboard.model.MatchState
import com.leadapp.scoreboard.model.Player
import com.leadapp.scoreboard.model.TimerMode
import com.leadapp.scoreboard.model.colorFromHex
import com.leadapp.scoreboard.model.playerPalette

/** 赛前设置：深色竞技场风格（对应 iOS SetupView） */
@Composable
fun SetupScreen(
    match: MatchState,
    settings: GameSettings,
    onStart: () -> Unit
) {
    var playerCount by remember { mutableIntStateOf(settings.playerCount) }
    var names by remember { mutableStateOf(settings.names.toMutableList()) }
    var colorHexes by remember { mutableStateOf(settings.colorHexes.toMutableList()) }
    var pointsPerTap by remember { mutableIntStateOf(settings.pointsPerTap) }
    var startScore by remember { mutableIntStateOf(settings.startScore) }
    var targetScoreText by remember { mutableStateOf(settings.targetScoreText) }
    var timerMode by remember { mutableStateOf(settings.timerMode) }
    var countdownMinText by remember { mutableStateOf(settings.countdownMinText) }
    var countdownSecText by remember { mutableStateOf(settings.countdownSecText) }

    val pointOptions = listOf(1, 2, 3, 5)

    fun start() {
        // 保存本次设置，下次新比赛默认沿用
        settings.playerCount = playerCount
        settings.names = names.toList()
        settings.colorHexes = colorHexes.toList()
        settings.pointsPerTap = pointsPerTap
        settings.startScore = startScore
        settings.targetScoreText = targetScoreText
        settings.timerMode = timerMode
        settings.countdownMinText = countdownMinText
        settings.countdownSecText = countdownSecText

        val players = (0 until playerCount).map { i ->
            val n = names.getOrNull(i)?.trim().orEmpty()
            Player(
                name = n.ifEmpty { "选手 ${i + 1}" },
                colorHex = colorHexes.getOrElse(i) { playerPalette[i % playerPalette.size] },
                score = startScore
            )
        }
        val target = targetScoreText.toIntOrNull() ?: 0
        val mins = countdownMinText.toIntOrNull() ?: 0
        val secs = countdownSecText.toIntOrNull() ?: 0
        val totalSeconds = maxOf(1, mins * 60 + secs)
        match.configure(
            players = players,
            pointsPerTap = pointsPerTap,
            targetScore = target,
            timerMode = timerMode,
            countdownSeconds = totalSeconds
        )
        onStart()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(arenaBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 标题
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("记分牌", fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(
                    "S C O R E B O A R D",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }

            // 选手卡
            SettingsCard(title = "选手", icon = Icons.Filled.Person) {
                StepperRow(title = "人数", value = "$playerCount",
                    onMinus = { if (playerCount > 2) playerCount-- },
                    onPlus = { if (playerCount < 4) playerCount++ })
                DarkDivider()
                for (i in 0 until playerCount) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)) {
                        DarkTextField(
                            value = names.getOrElse(i) { "" },
                            onValueChange = { v ->
                                val m = names.toMutableList()
                                while (m.size <= i) m.add("")
                                m[i] = v
                                names = m
                            },
                            placeholder = "名字"
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            playerPalette.forEach { hex ->
                                val selected = colorHexes.getOrNull(i) == hex
                                Box(
                                    modifier = Modifier
                                        .size(if (selected) 37.dp else 32.dp)
                                        .clip(CircleShape)
                                        .background(colorFromHex(hex))
                                        .border(
                                            width = if (selected) 3.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            val m = colorHexes.toMutableList()
                                            while (m.size <= i) m.add(playerPalette[0])
                                            m[i] = hex
                                            colorHexes = m
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // 计分规则卡
            SettingsCard(title = "计分规则", icon = Icons.Filled.PlayArrow) {
                Text("点一下加几分", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pointOptions.forEach { p ->
                        val selected = pointsPerTap == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) OrangeStart else Color.White.copy(alpha = 0.10f))
                                .clickable { pointsPerTap = p }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+$p", fontWeight = FontWeight.Bold,
                                color = if (selected) Color.Black else Color.White)
                        }
                    }
                }
                DarkDivider()
                StepperRow(title = "起始分", value = "$startScore",
                    onMinus = { if (startScore > 0) startScore-- },
                    onPlus = { if (startScore < 99) startScore++ })
                DarkDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("目标分", color = Color.White)
                    Spacer(Modifier.weight(1f))
                    NumberField(value = targetScoreText,
                        onValueChange = { targetScoreText = it.filter(Char::isDigit).take(4) },
                        placeholder = "不填=无上限", width = 110.dp)
                    Spacer(Modifier.width(4.dp))
                    Text("分", color = Color.White.copy(alpha = 0.7f))
                }
                Text("先到目标分即获胜；不填则只记分不判胜负",
                    fontSize = 12.sp, color = Color.White.copy(alpha = 0.45f))
            }

            // 计时卡
            SettingsCard(title = "计时", icon = Icons.Filled.Timer) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerMode.values().forEach { m ->
                        val selected = timerMode == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) OrangeStart else Color.White.copy(alpha = 0.10f))
                                .clickable { timerMode = m }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(m.title, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color = if (selected) Color.Black else Color.White)
                        }
                    }
                }
                if (timerMode == TimerMode.COUNTDOWN) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("倒计时", color = Color.White)
                        Spacer(Modifier.weight(1f))
                        NumberField(value = countdownMinText,
                            onValueChange = { countdownMinText = it.filter(Char::isDigit).take(3) },
                            placeholder = "分", width = 64.dp)
                        Spacer(Modifier.width(4.dp))
                        Text("分", color = Color.White.copy(alpha = 0.7f))
                        Spacer(Modifier.width(8.dp))
                        NumberField(value = countdownSecText,
                            onValueChange = { countdownSecText = it.filter(Char::isDigit).take(2) },
                            placeholder = "秒", width = 64.dp)
                        Spacer(Modifier.width(4.dp))
                        Text("秒", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            // 开始按钮
            Button(
                onClick = { start() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .shadow(16.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(orangeGradient, RoundedCornerShape(20.dp))
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("开始比赛", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = OrangeStart)
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
        }
        content()
    }
}

@Composable
private fun StepperRow(title: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.White)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onMinus) {
            Icon(Icons.Filled.RemoveCircle, contentDescription = "减",
                tint = Color.White.copy(alpha = 0.65f), modifier = Modifier.size(28.dp))
        }
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White,
            textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
        IconButton(onClick = onPlus) {
            Icon(Icons.Filled.AddCircle, contentDescription = "加",
                tint = OrangeStart, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun DarkDivider() {
    Divider(color = Color.White.copy(alpha = 0.12f), thickness = 1.dp)
}

@Composable
private fun DarkTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.4f)) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = OrangeStart
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    width: androidx.compose.ui.unit.Dp
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = OrangeStart
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(width)
    )
}
