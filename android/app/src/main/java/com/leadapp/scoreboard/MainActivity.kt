package com.leadapp.scoreboard

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.leadapp.scoreboard.model.GameSettings
import com.leadapp.scoreboard.model.MatchState
import com.leadapp.scoreboard.ui.ScoreBoardTheme
import com.leadapp.scoreboard.ui.ScoreboardScreen
import com.leadapp.scoreboard.ui.SetupScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScoreBoardTheme {
                val scope = rememberCoroutineScope()
                val match = remember { MatchState(scope) }
                val settings = remember { GameSettings(applicationContext) }
                var inMatch by remember { mutableStateOf(false) }

                // 前后台切换：锁屏/解锁时按墙钟补时间（与 iOS 版一致）
                remember {
                    lifecycle.addObserver(LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_PAUSE -> match.appDidEnterBackground()
                            Lifecycle.Event.ON_RESUME -> match.appDidBecomeActive()
                            else -> {}
                        }
                    })
                }

                // 比赛界面防锁屏
                if (inMatch) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }

                if (inMatch) {
                    ScoreboardScreen(match = match) {
                        match.pauseTimer()
                        inMatch = false
                    }
                } else {
                    SetupScreen(match = match, settings = settings) {
                        inMatch = true
                    }
                }
            }
        }
    }
}
