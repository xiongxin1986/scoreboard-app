# 记分牌 · Android 版

与 iOS 版（SwiftUI）功能 1:1 对齐的 Android 版本，用 Kotlin + Jetpack Compose 实现。

## 功能

- 2～4 人同屏记分，全屏铺选手颜色（无黑白边）
- 点大区域加分（+1/+2/+3/+5 可设），`-1` 按钮纠错
- 选手名字、8 色自定义
- 起始分、目标分（手工输入，不填=无上限，自动判胜）
- 不计时 / 倒计时 / 秒表（手工输入分+秒）
- 计时胶囊：横屏放顶部，竖屏放中间（与 iOS 版一致）
- 新比赛默认沿用上次设置（SharedPreferences）
- 锁屏/切后台不中断计时（墙钟方案，回来按真实经过时间补算）
- 比赛界面防锁屏
- 获胜庆祝横幅
- 3 人时第三人占满底行（iOS 版此处留黑格，安卓版已修正）

## 环境要求

- Android Studio Hedgehog (2023.1.1) 或更新
- JDK 17
- Android SDK：compileSdk 34，最低支持 Android 8.0 (API 26)

## 打开与编译

1. 用 Android Studio 打开 `android/` 目录（认到 `settings.gradle.kts` 即为工程根目录）
2. 等待 Gradle 同步完成（首次需下载依赖，要联网）
3. 连接安卓手机（开 USB 调试），点 Run ▶ 即装到手机上

## 打正式安装包（给妈妈的手机装）

1. Android Studio 菜单：Build → Generate Signed Bundle / APK → 选 APK
2. 新建一个 keystore（记好密码），一路 Next 生成 release APK
3. 把 `app-release.apk` 发到妈妈的手机上，点开安装即可
   - 若提示"未知来源"，按提示允许本次安装

## 包名

`com.leadapp.scoreboard`（与 iOS 版 Bundle ID 一致）
