# RecentDock Android

一款以 Kotlin 開發的 Android App 與主畫面小工具，協助快速整理、搜尋、啟動最近安裝與最近使用的 App，並提供收藏、釘選、隱藏、手動排序及可選擇啟用的本機使用分析。

**GitHub repository name:** `RecentDock_android`  
**Current source:** `v0.8.4` · **versionCode:** `44` · **Status:** Source candidate

English summary: RecentDock is an Android app library and adaptive home-screen widget for finding and launching apps, with local-only optional usage analytics.

## 功能特色

- **App Library：** 整理最近安裝、最近開啟與收藏的 App，支援以 App 名稱或 package name 搜尋。
- **管理與排序：** 收藏、釘選置頂、隱藏／復原、分類內拖曳排序，並可依最近使用時間排序。
- **App 詳細資訊：** 顯示 package name、安裝時間、最近開啟時間、RecentDock 啟動次數，並可跳轉 Android 系統 App 資訊頁。
- **快速操作：** 長按約一秒即可收藏或取消收藏，提供按壓動畫、震動、星號動畫與 Snackbar 回饋。
- **Adaptive Home-screen Widget：** 使用原生 `AppWidgetProvider` 與 `RemoteViews`；依小工具高度切換單排／雙排，寬度支援 2～5 欄及分頁。
- **Local Analytics：** 使用者可自行開啟每日、每週、每月使用分析；可單獨清除分析資料，不影響收藏、釘選、隱藏與排序。
- **系統解除安裝：** 從 App 項目選單呼叫 Android 系統解除安裝確認流程，不執行靜默解除安裝。
- **多語系：** English、繁體中文與簡體中文資源。

## 技術與需求

| 項目 | 版本／設定 |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose、Material 3 |
| Home-screen widget | Android AppWidgetProvider、RemoteViews |
| Android minimum | Android 8.0（API 26） |
| Android applicationId | `com.example.recentdock` |
| Compile / Target SDK | 36 / 36 |
| JDK | 17 |
| Gradle | 9.3.1 |
| Android Gradle Plugin | 9.1.1 |
| Kotlin Compose plugin | 2.3.21 |

## 開始建置

1. 安裝 Android Studio、JDK 17 與 Android SDK Platform 36。
2. 使用 Android Studio 開啟 repository 根目錄，等待 Gradle 同步完成。
3. 若 Android SDK 未自動偵測，請在根目錄建立本機 `local.properties`，填入自己的 SDK 路徑。此檔案已列入 `.gitignore`，不要提交。
4. 執行以下指令進行單元測試與 Debug APK 建置。

**Windows：**

    .\gradlew.bat :app:testDebugUnitTest
    .\gradlew.bat :app:assembleDebug

**macOS / Linux：**

    chmod +x ./gradlew
    ./gradlew :app:testDebugUnitTest
    ./gradlew :app:assembleDebug

本專案目前使用自訂 Gradle bootstrap scripts，並非包含 `gradle-wrapper.jar` 的標準 Gradle Wrapper。第一次執行會下載 Gradle 9.3.1；建置時也需要網路連線以取得 Gradle plugins 與 Maven dependencies。Windows 腳本會檢查 Gradle distribution 的 SHA-256；Unix 腳本在系統提供 `sha256sum` 時執行檢查。

## 使用權限

- **Usage Access（使用情況存取權）：** Android 系統特殊存取權，需由使用者自行前往系統設定開啟；「最近開啟」與使用分析功能會使用此權限。
- 未開啟 Usage Access 時，最近安裝與收藏等不依賴使用紀錄的功能仍可使用。
- App 僅宣告 Launcher intent query，不要求 `QUERY_ALL_PACKAGES`。

## 隱私與資料處理

- App 不要求 `INTERNET` 權限，不提供帳號或雲端同步，也不會將 App 清單或使用紀錄上傳至伺服器。
- 使用分析預設關閉；開啟後，相關事件儲存在裝置本機 SQLite，保留期限最長約 400 天。
- 使用者可在 Analytics 頁面單獨清除分析資料；收藏、釘選、隱藏與手動排序資料不受影響。
- App 設定為不允許 Android 備份 App 私有資料。

## 專案結構

    app/
      src/main/             Android manifest、Kotlin 程式碼與資源
      src/test/             JVM unit tests
    gradle/wrapper/         Gradle distribution 設定
    docs/                   Release notes、驗證報告與發佈檢查
    build.gradle.kts        Root Gradle build configuration
    settings.gradle.kts     Project modules and repositories
    gradle.properties       Gradle and Android build properties
    gradlew / gradlew.bat   Gradle bootstrap scripts

## 目前版本狀態

目前整理的程式碼為 **v0.8.4 source candidate（versionCode 44）**。本版修正 Android 系統解除安裝權限宣告，加入 `REQUEST_DELETE_PACKAGES`（normal 權限，安裝時自動授予）。XML／資源一致性與部分 Kotlin source checks 已完成；但目前尚未取得可成功下載 Gradle 的建置環境，因此 **尚不能宣稱完整 Gradle build 或單元測試已通過**。正式發佈前仍需：

1. 在可連網的 Android Studio 環境執行 `:app:testDebugUnitTest` 與 `:app:assembleDebug`。
2. 在實機及至少兩種 Launcher 驗證 Widget 尺寸、欄數、分頁與分類切換。
3. 驗證 Library 長按／左右滑動、Analytics timeline 拖曳及系統解除安裝的取消與確認流程。
4. 確認 App 圖示素材的公開使用權利，並決定 repository 的正式授權方式。

更多版本紀錄與已知限制請見 [`docs/`](docs/)。

## License

目前尚未加入 LICENSE，因為程式碼授權方式尚未決定。公開 repository 不代表他人已取得使用、修改或再散布本程式碼的授權；請在公開前確認程式碼與圖示素材的權利，並選定適合的 LICENSE。

---

## English

RecentDock is an Android app library and home-screen widget built with Kotlin. It helps users find and launch recently installed, recently opened, and starred apps. It includes app search, pinning, hiding, manual ordering, app details, an adaptive RemoteViews widget, and optional local usage analytics.

Build requirements: Android Studio, JDK 17, Android SDK 36. Minimum supported Android version: 8.0 (API 26). The current source candidate is v0.8.4 (versionCode 44), including the `REQUEST_DELETE_PACKAGES` manifest permission fix. A full Gradle build and on-device regression testing are still pending; see the status and release documents above before creating a formal release.

No LICENSE file is included yet. Do not assume that public visibility grants permission to use, modify, or redistribute this source.
