package com.leadapp.scoreboard.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 计时模式 */
enum class TimerMode(val title: String) {
    NONE("不计时"),
    COUNTDOWN("倒计时"),
    STOPWATCH("秒表")
}

/**
 * 整场比赛的状态（比分 + 计时器 + 胜负）
 * 计时器基于墙钟时间（System.currentTimeMillis），锁屏/切后台后回来能无缝继续
 */
class MatchState(private val scope: CoroutineScope) {
    var players by mutableStateOf(listOf<Player>())
        private set
    var pointsPerTap by mutableIntStateOf(1)
    var targetScore by mutableIntStateOf(0) // 0 = 无上限
    var timerMode by mutableStateOf(TimerMode.NONE)
    var countdownTotal by mutableIntStateOf(300) // 倒计时总秒数
    var timerSeconds by mutableIntStateOf(0)     // 倒计时=剩余秒数，秒表=已过秒数
        private set
    var timerRunning by mutableStateOf(false)
        private set
    var winner by mutableStateOf<Player?>(null)
        private set
    var timeUp by mutableStateOf(false)
        private set

    private var tickJob: Job? = null
    // 墙钟锚点：运行时有效
    private var countdownEndMillis: Long? = null
    private var stopwatchBaseMillis: Long? = null
    private var stopwatchAccumulated: Long = 0L

    fun configure(
        players: List<Player>,
        pointsPerTap: Int,
        targetScore: Int,
        timerMode: TimerMode,
        countdownSeconds: Int
    ) {
        pauseTimer()
        this.players = players
        this.pointsPerTap = pointsPerTap
        this.targetScore = targetScore
        this.timerMode = timerMode
        this.countdownTotal = maxOf(1, countdownSeconds)
        this.timerSeconds = if (timerMode == TimerMode.COUNTDOWN) maxOf(1, countdownSeconds) else 0
        this.stopwatchAccumulated = 0L
        this.countdownEndMillis = null
        this.stopwatchBaseMillis = null
        this.winner = null
        this.timeUp = false
    }

    // MARK: - 记分
    fun tap(player: Player) {
        if (winner != null) return
        val idx = players.indexOfFirst { it.id == player.id }
        if (idx < 0) return
        val updated = players.toMutableList()
        updated[idx] = updated[idx].copy(score = updated[idx].score + pointsPerTap)
        players = updated
        checkWinner()
    }

    /** 纠错：减 1 分（不低于 0） */
    fun correct(player: Player) {
        val idx = players.indexOfFirst { it.id == player.id }
        if (idx < 0) return
        val updated = players.toMutableList()
        updated[idx] = updated[idx].copy(score = maxOf(0, updated[idx].score - 1))
        players = updated
        val w = winner
        if (w != null && w.id == player.id && updated[idx].score < targetScore) {
            winner = null
        }
    }

    private fun checkWinner() {
        if (targetScore <= 0 || winner != null) return
        val w = players.firstOrNull { it.score >= targetScore }
        if (w != null) {
            winner = w
            pauseTimer()
        }
    }

    fun resetScores() {
        players = players.map { it.copy(score = 0) }
        winner = null
        timeUp = false
        resetTimer()
    }

    // MARK: - 计时器（墙钟方案）
    fun startTimer() {
        if (timerMode == TimerMode.NONE || timerRunning) return
        timerRunning = true
        anchorDates()
        startTick()
    }

    fun pauseTimer() {
        syncFromWallClock()
        timerRunning = false
        tickJob?.cancel()
        tickJob = null
        countdownEndMillis = null
        stopwatchBaseMillis = null
    }

    fun resetTimer() {
        pauseTimer()
        timeUp = false
        stopwatchAccumulated = 0L
        timerSeconds = if (timerMode == TimerMode.COUNTDOWN) countdownTotal else 0
    }

    /** 用当前 timerSeconds / 累计值锚定墙钟 */
    private fun anchorDates() {
        val now = System.currentTimeMillis()
        if (timerMode == TimerMode.COUNTDOWN) {
            countdownEndMillis = now + timerSeconds * 1000L
            stopwatchBaseMillis = null
        } else if (timerMode == TimerMode.STOPWATCH) {
            stopwatchBaseMillis = now - stopwatchAccumulated
            countdownEndMillis = null
        }
    }

    private fun startTick() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                delay(1000)
                tick()
            }
        }
    }

    private fun tick() {
        syncFromWallClock()
        if (timerMode == TimerMode.COUNTDOWN && timerSeconds <= 0) {
            timerSeconds = 0
            pauseTimer()
            timeUp = true
        }
    }

    /** 从墙钟重新计算（锁屏回来后时间依然准确） */
    private fun syncFromWallClock() {
        val now = System.currentTimeMillis()
        countdownEndMillis?.let { end ->
            if (timerMode == TimerMode.COUNTDOWN) {
                timerSeconds = maxOf(0, ((end - now + 500) / 1000).toInt())
            }
        }
        stopwatchBaseMillis?.let { base ->
            if (timerMode == TimerMode.STOPWATCH) {
                val elapsed = now - base
                stopwatchAccumulated = elapsed
                timerSeconds = (elapsed / 1000).toInt()
            }
        }
    }

    // MARK: - 前后台切换（锁屏/解锁）
    /** 进入后台（含锁屏）：停掉每秒 tick，但保留墙钟锚点和运行状态 */
    fun appDidEnterBackground() {
        if (!timerRunning) return
        syncFromWallClock()
        tickJob?.cancel()
        tickJob = null
        // 不清锚点，回来继续用；timerRunning 保持 true
    }

    /** 回到前台（含解锁）：按墙钟补上离开的时间，无缝继续 */
    fun appDidBecomeActive() {
        if (!timerRunning) return
        syncFromWallClock()
        if (timerMode == TimerMode.COUNTDOWN && timerSeconds <= 0) {
            timerSeconds = 0
            pauseTimer()
            timeUp = true
        } else {
            anchorDates()
            startTick()
        }
    }

    val timeText: String
        get() = "%02d:%02d".format(timerSeconds / 60, timerSeconds % 60)

    /** 当前领先者（用于时间到时展示） */
    val leader: Player?
        get() = players.maxByOrNull { it.score }
}
