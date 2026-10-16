# 记分牌 ScoreBoard

一个通用的比赛记分 App（对标 App Store 上的 Scoard: Scoreboard & Points，功能原创实现）。

## 功能

- **2~4 人同屏记分**：点选手的大色块就加分（每次加几分可设 +1/+2/+3/+5）
- **纠错**：每张卡右上角 "−1" 按钮，点错了随时减 1 分
- **自定义**：每人名字 + 8 种颜色随便配
- **起始分 / 让分**：开局比分可设，不都从 0 开始也行
- **目标分**：设了目标分后，先到者自动判胜，弹出获胜横幅
- **计时器**：倒计时 / 秒表 / 不计时，最后一分钟变红提醒
- **防锁屏**：比赛界面保持屏幕常亮，场边用不黑屏
- **全屏大字**：比分数字巨大，远处也看得清；支持横竖屏、iPhone/iPad

## 安装（Mac + Xcode）

1. 解压，用 Xcode 打开 `ScoreBoard.xcodeproj`
2. 选目标设备（iPhone/iPad），点 Run
3. 免费 Apple ID 签名即可（参考车展 App 的安装流程）

## 工程结构

```
ScoreBoard/
├── ScoreBoardApp.swift      App 入口
├── ContentView.swift         设置页 <-> 记分页切换
├── Info.plist
├── Assets.xcassets          全套 App 图标（18 槽位）
├── Models/
│   ├── Player.swift         选手模型 + 颜色扩展
│   └── MatchState.swift     比赛状态：记分、计时器、胜负判定
└── Views/
    ├── SetupView.swift      赛前设置
    ├── ScoreboardView.swift 记分主界面
    ├── PlayerCard.swift     选手记分卡
    └── TimerBar.swift       计时条
```

要求：iOS 17+，Xcode 16+。
