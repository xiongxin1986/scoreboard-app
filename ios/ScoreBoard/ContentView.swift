import SwiftUI

/// 根视图：设置页 <-> 记分页切换
struct ContentView: View {
    @State private var match = MatchState()
    @State private var inMatch = false

    var body: some View {
        if inMatch {
            NavigationStack {
                ScoreboardView(match: match) {
                    match.pauseTimer()
                    inMatch = false
                }
            }
        } else {
            SetupView(match: match) {
                inMatch = true
            }
        }
    }
}
