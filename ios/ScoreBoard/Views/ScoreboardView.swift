import SwiftUI
import UIKit

/// 记分主界面：全屏铺色 + 中央悬浮计时胶囊（参考图样式）
struct ScoreboardView: View {
    let match: MatchState
    let onExit: () -> Void

    @State private var showResetConfirm = false
    @State private var showMenu = false
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        GeometryReader { geo in
            let isLandscape = geo.size.width > geo.size.height
            ZStack {
                // 全屏铺色，无黑白边
                scoreGrid(isLandscape: isLandscape)
                    .ignoresSafeArea()

                // 计时胶囊：横屏放顶部，竖屏放中间（参考图）
                if match.timerMode != .none {
                    if isLandscape {
                        VStack {
                            TimerPill(match: match, onMenu: { showMenu = true })
                                .padding(.top, 8)
                            Spacer()
                        }
                    } else {
                        TimerPill(match: match, onMenu: { showMenu = true })
                    }
                } else {
                    // 无计时模式时，右上角小齿轮进菜单
                    VStack {
                        HStack {
                            Spacer()
                            Button(action: { showMenu = true }) {
                                Image(systemName: "gearshape.fill")
                                    .font(.title3)
                                    .foregroundStyle(.white.opacity(0.85))
                                    .frame(width: 44, height: 44)
                                    .background(.black.opacity(0.25))
                                    .clipShape(Circle())
                            }
                            .padding(.top, 54)
                            .padding(.trailing, 16)
                        }
                        Spacer()
                    }
                    .ignoresSafeArea()
                }

                if let winner = match.winner {
                    winnerOverlay(winner)
                }
            }
            .frame(width: geo.size.width, height: geo.size.height)
        }
        .ignoresSafeArea()
        .toolbar(.hidden)
        .onAppear {
            UIApplication.shared.isIdleTimerDisabled = true
        }
        .onDisappear {
            UIApplication.shared.isIdleTimerDisabled = false
        }
        .onChange(of: scenePhase) { _, newPhase in
            if newPhase == .background {
                match.appDidEnterBackground()
            } else if newPhase == .active {
                match.appDidBecomeActive()
            }
        }
        .confirmationDialog("菜单", isPresented: $showMenu, titleVisibility: .hidden) {
            Button("新比赛", action: onExit)
            Button("比分清零", role: .destructive) { showResetConfirm = true }
            Button("取消", role: .cancel) {}
        }
        .alert("比分清零？", isPresented: $showResetConfirm) {
            Button("清零", role: .destructive) { match.resetScores() }
            Button("取消", role: .cancel) {}
        }
    }

    // MARK: - 记分卡布局（无缝隙铺满）
    private func scoreGrid(isLandscape: Bool) -> some View {
        GeometryReader { geo in
            Group {
                if match.players.count <= 2 {
                    if isLandscape {
                        HStack(spacing: 0) {
                            ForEach(Array(match.players.enumerated()), id: \.element.id) { i, player in
                                card(for: player, minus: i == 0 ? .bottomTrailing : .bottomLeading, isLandscape: isLandscape)
                            }
                        }
                    } else {
                        VStack(spacing: 0) {
                            ForEach(Array(match.players.enumerated()), id: \.element.id) { i, player in
                                card(for: player, minus: i == 0 ? .bottomTrailing : .topTrailing, isLandscape: isLandscape)
                            }
                        }
                    }
                } else {
                    VStack(spacing: 0) {
                        HStack(spacing: 0) {
                            ForEach(Array(match.players.prefix(2).enumerated()), id: \.element.id) { i, player in
                                card(for: player, minus: i == 0 ? .bottomTrailing : .bottomLeading, isLandscape: isLandscape)
                            }
                        }
                        HStack(spacing: 0) {
                            ForEach(Array(match.players.dropFirst(2).enumerated()), id: \.element.id) { i, player in
                                let align: Alignment = (i == 0 && match.players.count == 4) || (i == 0 && match.players.count == 3)
                                    ? .topTrailing : .topLeading
                                card(for: player, minus: align, isLandscape: isLandscape)
                            }
                            if match.players.count == 3 {
                                Color.black
                                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                            }
                        }
                    }
                }
            }
            .frame(width: geo.size.width, height: geo.size.height)
        }
    }

    private func card(for player: Player, minus: Alignment, isLandscape: Bool) -> some View {
        PlayerCard(
            player: player,
            pointsPerTap: match.pointsPerTap,
            onTap: { match.tap(player) },
            onCorrect: { match.correct(player) },
            minusAlignment: minus,
            isLandscape: isLandscape
        )
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // MARK: - 获胜横幅
    private func winnerOverlay(_ winner: Player) -> some View {
        ZStack {
            Color.black.opacity(0.65).ignoresSafeArea()
            VStack(spacing: 18) {
                Text("🏆")
                    .font(.system(size: 72))
                    .shadow(color: .orange.opacity(0.6), radius: 20)
                Text("\(winner.name) 获胜！")
                    .font(.system(size: 40, weight: .heavy, design: .rounded))
                    .foregroundStyle(
                        LinearGradient(colors: [.white, winner.color],
                                       startPoint: .top, endPoint: .bottom)
                    )
                Text("最终比分 \(winner.score)")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundStyle(.white.opacity(0.7))
                HStack(spacing: 16) {
                    Button("再来一局") { match.resetScores() }
                        .fontWeight(.bold)
                        .foregroundStyle(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(
                            LinearGradient(colors: [Color(red: 1.0, green: 0.55, blue: 0.15),
                                                    Color(red: 0.95, green: 0.30, blue: 0.15)],
                                           startPoint: .leading, endPoint: .trailing)
                        )
                        .clipShape(Capsule())
                    Button("回主菜单", action: onExit)
                        .fontWeight(.semibold)
                        .foregroundStyle(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.white.opacity(0.12))
                        .clipShape(Capsule())
                }
            }
            .padding(36)
            .background(Color(red: 0.10, green: 0.10, blue: 0.16))
            .overlay(
                RoundedRectangle(cornerRadius: 28)
                    .stroke(winner.color.opacity(0.5), lineWidth: 2)
            )
            .clipShape(RoundedRectangle(cornerRadius: 28))
            .shadow(color: winner.color.opacity(0.35), radius: 30)
            .padding()
        }
    }
}

/// 中央悬浮计时胶囊（参考图样式）
private struct TimerPill: View {
    let match: MatchState
    let onMenu: () -> Void

    var body: some View {
        HStack(spacing: 16) {
            Button(action: onMenu) {
                Image(systemName: "gearshape.fill")
                    .font(.title3)
                    .foregroundStyle(.white.opacity(0.75))
            }
            Text(match.timeText)
                .font(.system(size: 30, weight: .heavy, design: .monospaced))
                .foregroundStyle(timeColor)
                .frame(minWidth: 110)
            Button(action: {
                match.timerRunning ? match.pauseTimer() : match.startTimer()
            }) {
                Image(systemName: match.timerRunning ? "pause.fill" : "play.fill")
                    .font(.title3)
                    .foregroundStyle(.white.opacity(0.85))
                    .frame(width: 36, height: 36)
            }
            Button(action: { match.resetTimer() }) {
                Image(systemName: "arrow.clockwise")
                    .font(.title3)
                    .foregroundStyle(.white.opacity(0.75))
                    .frame(width: 36, height: 36)
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(Color(red: 0.16, green: 0.16, blue: 0.22).opacity(0.92))
        .clipShape(Capsule())
        .shadow(color: .black.opacity(0.4), radius: 12, y: 6)
    }

    private var timeColor: Color {
        if match.timerMode == .countdown && match.timerSeconds <= 60 {
            return .red
        }
        return .white
    }
}
