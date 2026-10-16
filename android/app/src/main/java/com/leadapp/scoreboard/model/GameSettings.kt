package com.leadapp.scoreboard.model

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

/** 上次比赛的设置（新比赛时默认沿用），对应 iOS 的 UserDefaults */
class GameSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var playerCount: Int
        get() = prefs.getInt("playerCount", 2).coerceIn(2, 4)
        set(v) = prefs.edit().putInt("playerCount", v).apply()

    var names: List<String>
        get() = getStringList("names", listOf("红方", "蓝方", "绿方", "紫方"))
        set(v) = putStringList("names", v)

    var colorHexes: List<String>
        get() = getStringList("colorHexes", playerPalette.take(4))
        set(v) = putStringList("colorHexes", v)

    var pointsPerTap: Int
        get() = prefs.getInt("pointsPerTap", 1)
        set(v) = prefs.edit().putInt("pointsPerTap", v).apply()

    var startScore: Int
        get() = prefs.getInt("startScore", 0)
        set(v) = prefs.edit().putInt("startScore", v).apply()

    var targetScoreText: String
        get() = prefs.getString("targetScoreText", "") ?: ""
        set(v) = prefs.edit().putString("targetScoreText", v).apply()

    var timerMode: TimerMode
        get() = runCatching { TimerMode.valueOf(prefs.getString("timerMode", "NONE") ?: "NONE") }
            .getOrDefault(TimerMode.NONE)
        set(v) = prefs.edit().putString("timerMode", v.name).apply()

    var countdownMinText: String
        get() = prefs.getString("countdownMinText", "5") ?: "5"
        set(v) = prefs.edit().putString("countdownMinText", v).apply()

    var countdownSecText: String
        get() = prefs.getString("countdownSecText", "0") ?: "0"
        set(v) = prefs.edit().putString("countdownSecText", v).apply()

    private fun getStringList(key: String, default: List<String>): List<String> {
        val json = prefs.getString(key, null) ?: return default
        return runCatching {
            val arr = JSONArray(json)
            List(arr.length()) { arr.getString(it) }
        }.getOrDefault(default)
    }

    private fun putStringList(key: String, value: List<String>) {
        val arr = JSONArray()
        value.forEach { arr.put(it) }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "scoreboard.lastSettings"
    }
}
