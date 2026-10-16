import Foundation
import Observation

/// 计时模式
enum TimerMode: String, Codable, CaseIterable, Identifiable {
    case none
    case countdown
    case stopwatch

    var id: String { rawValue }
    var title: String {
        switch self {
        case .none: return "不计时"
        case .countdown: return "倒计时"
        case .stopwatch: return "秒表"
        }
    }
}

/// 上次比赛的设置（新比赛时默认沿用）
struct GameSettings: Codable {
    var playerCount = 2
    var names = ["红方", "蓝方", "绿方", "紫方"]
    var colorHexes = Array(playerPalette.prefix(4))
    var pointsPerTap = 1
    var startScore = 0
    var targetScoreText = ""
    var timerModeRaw = TimerMode.none.rawValue
    var countdownMinText = "5"
    var countdownSecText = "0"

    static let key = "scoreboard.lastSettings"

    static func load() -> GameSettings {
        guard let data = UserDefaults.standard.data(forKey: key),
              let s = try? JSONDecoder().decode(GameSettings.self, from: data) else {
            return GameSettings()
        }
        return s
    }

    func save() {
        if let data = try? JSONEncoder().encode(self) {
            UserDefaults.standard.set(data, forKey: Self.key)
        }
    }
}

/// 整场比赛的状态（比分 + 计时器 + 胜负）
/// 计时器基于墙钟时间（Date），锁屏/切后台后回来能无缝继续
@Observable
final class MatchState {
    var players: [Player] = []
    var pointsPerTap = 1
    var targetScore = 0          // 0 = 无上限
    var timerMode: TimerMode = .none
    var countdownTotal = 300     // 倒计时总秒数
    var timerSeconds = 0         // 倒计时=剩余秒数,秒表=已过秒数
    var timerRunning = false
    var winner: Player?
    var timeUp = false

    private var timer: Timer?
    // 墙钟锚点：运行时有效
    private var countdownEndDate: Date?
    private var stopwatchBaseDate: Date?
    private var stopwatchAccumulated: TimeInterval = 0

    func configure(players: [Player], pointsPerTap: Int, targetScore: Int,
                   timerMode: TimerMode, countdownSeconds: Int) {
        pauseTimer()
        self.players = players
        self.pointsPerTap = pointsPerTap
        self.targetScore = targetScore
        self.timerMode = timerMode
        self.countdownTotal = max(1, countdownSeconds)
        self.timerSeconds = timerMode == .countdown ? max(1, countdownSeconds) : 0
        self.stopwatchAccumulated = 0
        self.countdownEndDate = nil
        self.stopwatchBaseDate = nil
        self.winner = nil
        self.timeUp = false
    }

    // MARK: - 记分
    func tap(_ player: Player) {
        guard winner == nil,
              let i = players.firstIndex(where: { $0.id == player.id }) else { return }
        players[i].score += pointsPerTap
        checkWinner()
    }

    /// 纠错：减 1 分（不低于 0）
    func correct(_ player: Player) {
        guard let i = players.firstIndex(where: { $0.id == player.id }) else { return }
        players[i].score = max(0, players[i].score - 1)
        if let w = winner, w.id == player.id, players[i].score < targetScore {
            winner = nil
        }
    }

    private func checkWinner() {
        guard targetScore > 0, winner == nil else { return }
        if let w = players.first(where: { $0.score >= targetScore }) {
            winner = w
            pauseTimer()
        }
    }

    func resetScores() {
        for i in players.indices { players[i].score = 0 }
        winner = nil
        timeUp = false
        resetTimer()
    }

    // MARK: - 计时器（墙钟方案）
    func startTimer() {
        guard timerMode != .none, !timerRunning else { return }
        timerRunning = true
        anchorDates()
        startTickTimer()
    }

    func pauseTimer() {
        syncFromWallClock()
        timerRunning = false
        timer?.invalidate()
        timer = nil
        countdownEndDate = nil
        stopwatchBaseDate = nil
    }

    func resetTimer() {
        pauseTimer()
        timeUp = false
        stopwatchAccumulated = 0
        timerSeconds = timerMode == .countdown ? countdownTotal : 0
    }

    /// 用当前 timerSeconds / 累计值锚定墙钟
    private func anchorDates() {
        let now = Date()
        if timerMode == .countdown {
            countdownEndDate = now.addingTimeInterval(TimeInterval(timerSeconds))
            stopwatchBaseDate = nil
        } else if timerMode == .stopwatch {
            stopwatchBaseDate = now.addingTimeInterval(-stopwatchAccumulated)
            countdownEndDate = nil
        }
    }

    private func startTickTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            self?.tick()
        }
    }

    private func tick() {
        syncFromWallClock()
        if timerMode == .countdown && timerSeconds <= 0 {
            timerSeconds = 0
            pauseTimer()
            timeUp = true
        }
    }

    /// 从墙钟重新计算（锁屏回来后时间依然准确）
    private func syncFromWallClock() {
        let now = Date()
        if timerMode == .countdown, let end = countdownEndDate {
            timerSeconds = max(0, Int(end.timeIntervalSince(now).rounded()))
        } else if timerMode == .stopwatch, let base = stopwatchBaseDate {
            let elapsed = now.timeIntervalSince(base)
            stopwatchAccumulated = elapsed
            timerSeconds = Int(elapsed)
        }
    }

    // MARK: - 前后台切换（锁屏/解锁）
    /// 进入后台（含锁屏）：停掉每秒 tick，但保留墙钟锚点和运行状态
    func appDidEnterBackground() {
        guard timerRunning else { return }
        syncFromWallClock()
        timer?.invalidate()
        timer = nil
        // 不清 anchorDates，回来继续用；timerRunning 保持 true
    }

    /// 回到前台（含解锁）：按墙钟补上离开的时间，无缝继续
    func appDidBecomeActive() {
        guard timerRunning else { return }
        syncFromWallClock()
        if timerMode == .countdown && timerSeconds <= 0 {
            // 离开期间倒计时走完了
            timerSeconds = 0
            pauseTimer()
            timeUp = true
        } else {
            // 重新锚定并恢复 tick
            anchorDates()
            startTickTimer()
        }
    }

    var timeText: String {
        String(format: "%02d:%02d", timerSeconds / 60, timerSeconds % 60)
    }

    /// 当前领先者（用于时间到时展示）
    var leader: Player? {
        players.max(by: { $0.score < $1.score })
    }
}
