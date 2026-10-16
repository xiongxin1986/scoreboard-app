import SwiftUI
import UIKit

/// 单个选手的记分卡：全屏铺色，点大区域加分
/// -1 按钮放在靠屏幕内侧的角（参考图样式）
struct PlayerCard: View {
    let player: Player
    let pointsPerTap: Int
    let onTap: () -> Void
    let onCorrect: () -> Void
    let minusAlignment: Alignment
    let isLandscape: Bool

    var body: some View {
        ZStack(alignment: minusAlignment) {
            Button(action: {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                onTap()
            }) {
                ZStack {
                    LinearGradient(
                        colors: [player.color, player.color.opacity(0.55)],
                        startPoint: .topLeading, endPoint: .bottomTrailing
                    )
                    VStack {
                        LinearGradient(
                            colors: [.white.opacity(0.18), .clear],
                            startPoint: .top, endPoint: .bottom
                        )
                        Spacer()
                    }
                    VStack(spacing: 0) {
                        Text(player.name)
                            .font(.title)
                            .fontWeight(.bold)
                            .foregroundStyle(.white)
                            .lineLimit(1)
                            .shadow(color: .black.opacity(0.25), radius: 4, y: 2)
                            .padding(.top, isLandscape ? 110 : 54)
                        Spacer()
                        ScoreNumber(score: player.score)
                        Spacer()
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            .buttonStyle(.plain)

            Button(action: onCorrect) {
                Text("-1")
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                    .frame(width: 56, height: 56)
                    .background(.white.opacity(0.18))
                    .clipShape(Circle())
                    .overlay(
                        Circle().stroke(Color.white.opacity(0.35), lineWidth: 1.5)
                    )
                    .shadow(color: .black.opacity(0.25), radius: 6, y: 3)
            }
            .padding(18)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

/// 自适应巨大的比分数字
private struct ScoreNumber: View {
    let score: Int

    var body: some View {
        GeometryReader { geo in
            let s = min(geo.size.width * 0.62, geo.size.height * 0.62)
            Text("\(score)")
                .font(.system(size: max(60, s), weight: .heavy, design: .rounded))
                .foregroundStyle(.white)
                .shadow(color: .black.opacity(0.3), radius: 12, y: 6)
                .lineLimit(1)
                .minimumScaleFactor(0.4)
                .frame(width: geo.size.width, height: geo.size.height)
        }
    }
}
