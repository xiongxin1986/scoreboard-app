import SwiftUI
import UIKit

/// 赛前设置：深色竞技场风格
/// 目标分、倒计时均支持手工输入
struct SetupView: View {
    let match: MatchState
    let onStart: () -> Void

    @State private var playerCount: Int
    @State private var names: [String]
    @State private var colorHexes: [String]
    @State private var pointsPerTap: Int
    @State private var startScore: Int
    @State private var targetScoreText: String
    @State private var timerMode: TimerMode
    @State private var countdownMinText: String
    @State private var countdownSecText: String

    @FocusState private var focusedField: Bool

    private let pointOptions = [1, 2, 3, 5]

    /// 新比赛默认沿用上次的设置
    init(match: MatchState, onStart: @escaping () -> Void) {
        self.match = match
        self.onStart = onStart
        let s = GameSettings.load()
        _playerCount = State(initialValue: min(max(s.playerCount, 2), 4))
        _names = State(initialValue: s.names)
        _colorHexes = State(initialValue: s.colorHexes)
        _pointsPerTap = State(initialValue: s.pointsPerTap)
        _startScore = State(initialValue: s.startScore)
        _targetScoreText = State(initialValue: s.targetScoreText)
        _timerMode = State(initialValue: TimerMode(rawValue: s.timerModeRaw) ?? .none)
        _countdownMinText = State(initialValue: s.countdownMinText)
        _countdownSecText = State(initialValue: s.countdownSecText)
    }

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color(red: 0.04, green: 0.05, blue: 0.10),
                         Color(red: 0.10, green: 0.08, blue: 0.18)],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()
            ScrollView {
                VStack(spacing: 18) {
                    headerView
                    PlayersSection(playerCount: $playerCount, names: $names,
                                   colorHexes: $colorHexes, focusedField: $focusedField)
                    RulesSection(pointsPerTap: $pointsPerTap, startScore: $startScore,
                                 targetScoreText: $targetScoreText,
                                 focusedField: $focusedField, pointOptions: pointOptions)
                    TimerSection(timerMode: $timerMode,
                                 countdownMinText: $countdownMinText,
                                 countdownSecText: $countdownSecText,
                                 focusedField: $focusedField)
                    startButton
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 28)
            }
        }
        .toolbar {
            ToolbarItemGroup(placement: .keyboard) {
                Spacer()
                Button("完成") { focusedField = false }
                    .fontWeight(.semibold)
            }
        }
    }

    private var headerView: some View {
        VStack(spacing: 6) {
            Text("记分牌")
                .font(.system(size: 44, weight: .heavy, design: .rounded))
                .foregroundStyle(.white)
            Text("S C O R E B O A R D")
                .font(.system(size: 13, weight: .semibold, design: .monospaced))
                .foregroundStyle(.white.opacity(0.45))
        }
        .padding(.top, 8)
    }

    private var startButton: some View {
        Button(action: {
            focusedField = false
            start()
        }) {
            Text("开始比赛")
                .font(.title2)
                .fontWeight(.heavy)
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 18)
                .background(
                    LinearGradient(
                        colors: [Color(red: 1.0, green: 0.55, blue: 0.15),
                                 Color(red: 0.95, green: 0.30, blue: 0.15)],
                        startPoint: .leading, endPoint: .trailing
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .shadow(color: .orange.opacity(0.45), radius: 16, y: 8)
        }
        .padding(.top, 6)
    }

    private func start() {
        saveSettings()
        let players = (0..<playerCount).map { i in
            let n = names[i].trimmingCharacters(in: .whitespacesAndNewlines)
            return Player(
                name: n.isEmpty ? "选手 \(i + 1)" : n,
                colorHex: colorHexes[i],
                score: startScore
            )
        }
        let target = Int(targetScoreText) ?? 0
        let mins = Int(countdownMinText) ?? 0
        let secs = Int(countdownSecText) ?? 0
        let totalSeconds = max(1, mins * 60 + secs)
        match.configure(
            players: players,
            pointsPerTap: pointsPerTap,
            targetScore: target,
            timerMode: timerMode,
            countdownSeconds: totalSeconds
        )
        onStart()
    }

    /// 保存本次设置，下次新比赛默认沿用
    private func saveSettings() {
        var s = GameSettings()
        s.playerCount = playerCount
        s.names = names
        s.colorHexes = colorHexes
        s.pointsPerTap = pointsPerTap
        s.startScore = startScore
        s.targetScoreText = targetScoreText
        s.timerModeRaw = timerMode.rawValue
        s.countdownMinText = countdownMinText
        s.countdownSecText = countdownSecText
        s.save()
    }
}

// MARK: - 通用卡片容器
private struct SettingsCard<Content: View>: View {
    let title: String
    let icon: String
    let content: Content

    init(title: String, icon: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.icon = icon
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .foregroundStyle(.orange)
                    .font(.headline)
                Text(title)
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
            }
            content
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white.opacity(0.07))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color.white.opacity(0.10), lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 20))
        .shadow(color: .black.opacity(0.35), radius: 12, y: 6)
    }
}

// MARK: - 自定义步进器
private struct StepperRow: View {
    let title: String
    let value: String
    let onMinus: () -> Void
    let onPlus: () -> Void

    var body: some View {
        HStack {
            Text(title).foregroundStyle(.white)
            Spacer()
            Button(action: onMinus) {
                Image(systemName: "minus.circle.fill")
                    .font(.title2)
                    .foregroundStyle(.white.opacity(0.65))
            }
            Text(value)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundStyle(.white)
                .frame(minWidth: 44)
                .multilineTextAlignment(.center)
            Button(action: onPlus) {
                Image(systemName: "plus.circle.fill")
                    .font(.title2)
                    .foregroundStyle(.orange)
            }
        }
    }
}

// MARK: - 数字输入框
private struct NumberField: View {
    let placeholder: String
    @Binding var text: String
    var width: CGFloat
    var maxDigits: Int
    var isFocused: FocusState<Bool>.Binding

    var body: some View {
        TextField(placeholder, text: $text)
            .focused(isFocused)
            .keyboardType(.numberPad)
            .multilineTextAlignment(.center)
            .font(.headline)
            .foregroundStyle(.white)
            .tint(.orange)
            .frame(width: width)
            .padding(.vertical, 8)
            .background(Color.white.opacity(0.08))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .onChange(of: text) { _, v in
                let digits = v.filter { $0.isNumber }
                text = String(digits.prefix(maxDigits))
            }
    }
}

private struct DarkDivider: View {
    var body: some View {
        Divider().background(Color.white.opacity(0.12))
    }
}

// MARK: - 选手区
private struct PlayersSection: View {
    @Binding var playerCount: Int
    @Binding var names: [String]
    @Binding var colorHexes: [String]
    var focusedField: FocusState<Bool>.Binding

    var body: some View {
        SettingsCard(title: "选手", icon: "person.2.fill") {
            StepperRow(title: "人数", value: "\(playerCount)") {
                if playerCount > 2 { playerCount -= 1 }
            } onPlus: {
                if playerCount < 4 { playerCount += 1 }
            }
            DarkDivider()
            ForEach(0..<playerCount, id: \.self) { i in
                VStack(alignment: .leading, spacing: 10) {
                    TextField("名字", text: $names[i])
                        .focused(focusedField)
                        .textFieldStyle(.plain)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 10)
                        .background(Color.white.opacity(0.08))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .foregroundStyle(.white)
                        .tint(.orange)
                    colorDots(i)
                }
                .padding(.vertical, 4)
            }
        }
    }

    private func colorDots(_ i: Int) -> some View {
        HStack(spacing: 12) {
            ForEach(playerPalette, id: \.self) { hex in
                Circle()
                    .fill(Color(hex: hex))
                    .frame(width: 32, height: 32)
                    .overlay(
                        Circle()
                            .stroke(Color.white, lineWidth: colorHexes[i] == hex ? 3 : 0)
                    )
                    .scaleEffect(colorHexes[i] == hex ? 1.15 : 1.0)
                    .animation(.spring(response: 0.25), value: colorHexes[i])
                    .onTapGesture { colorHexes[i] = hex }
            }
        }
    }
}

// MARK: - 计分规则区
private struct RulesSection: View {
    @Binding var pointsPerTap: Int
    @Binding var startScore: Int
    @Binding var targetScoreText: String
    var focusedField: FocusState<Bool>.Binding
    let pointOptions: [Int]

    var body: some View {
        SettingsCard(title: "计分规则", icon: "plus.circle.fill") {
            VStack(alignment: .leading, spacing: 6) {
                Text("点一下加几分")
                    .font(.subheadline)
                    .foregroundStyle(.white.opacity(0.7))
                HStack(spacing: 10) {
                    ForEach(pointOptions, id: \.self) { p in
                        Button("+\(p)") { pointsPerTap = p }
                            .font(.headline)
                            .fontWeight(.bold)
                            .foregroundStyle(pointsPerTap == p ? .black : .white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(pointsPerTap == p ? Color.orange : Color.white.opacity(0.10))
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
            }
            DarkDivider()
            StepperRow(title: "起始分", value: "\(startScore)") {
                if startScore > 0 { startScore -= 1 }
            } onPlus: {
                if startScore < 99 { startScore += 1 }
            }
            DarkDivider()
            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    Text("目标分").foregroundStyle(.white)
                    Spacer()
                    NumberField(placeholder: "不填=无上限", text: $targetScoreText,
                                width: 110, maxDigits: 4, isFocused: focusedField)
                    Text("分").foregroundStyle(.white.opacity(0.7))
                }
                Text("先到目标分即获胜；不填则只记分不判胜负")
                    .font(.footnote)
                    .foregroundStyle(.white.opacity(0.45))
            }
        }
    }
}

// MARK: - 计时区
private struct TimerSection: View {
    @Binding var timerMode: TimerMode
    @Binding var countdownMinText: String
    @Binding var countdownSecText: String
    var focusedField: FocusState<Bool>.Binding

    var body: some View {
        SettingsCard(title: "计时", icon: "timer") {
            HStack(spacing: 10) {
                ForEach(TimerMode.allCases) { m in
                    Button(m.title) { timerMode = m }
                        .font(.subheadline)
                        .fontWeight(.bold)
                        .foregroundStyle(timerMode == m ? .black : .white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                        .background(timerMode == m ? Color.orange : Color.white.opacity(0.10))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            if timerMode == .countdown {
                HStack {
                    Text("倒计时").foregroundStyle(.white)
                    Spacer()
                    NumberField(placeholder: "分", text: $countdownMinText,
                                width: 64, maxDigits: 3, isFocused: focusedField)
                    Text("分").foregroundStyle(.white.opacity(0.7))
                    NumberField(placeholder: "秒", text: $countdownSecText,
                                width: 64, maxDigits: 2, isFocused: focusedField)
                    Text("秒").foregroundStyle(.white.opacity(0.7))
                }
            }
        }
    }
}
