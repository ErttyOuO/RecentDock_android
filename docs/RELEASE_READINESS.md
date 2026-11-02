# RecentDock 0.8.4：GitHub 發佈檢查

狀態：**source candidate；v0.8.4 Gradle 建置與 29 項單元測試已通過，但尚未完成真機／多 Launcher 回歸測試，因此不建立正式 release tag。**

## 本次已處理

- v0.8.0：Analytics 可單獨清除、跨分類 `LAST_USED` 排序、App Detail Screen。
- v0.8.1：RemoteViews Widget 支援依高度自動切換單排／雙排，常見寬度雙排最多 10 個 App／頁。
- v0.8.2：List 長按加入按壓／星號特效與 Snackbar；Library 左右拖曳切換改成受控動畫；Analytics 時間線拖曳只在放開後查詢詳細統計。
- v0.8.3：App 列表 `⋯` 選單新增系統解除安裝入口，返回後強制重新整理清單並清理已移除 package 的狀態。
- v0.8.4：修正解除安裝權限缺漏，Manifest 新增 `REQUEST_DELETE_PACKAGES` normal 權限；系統確認畫面仍由 Android 管理。
- 修正較窄雙排（2～4 欄）第二排 slot 錯位問題。
- Widget picker 預覽、設定頁說明與雙排顯示邏輯同步。
- Widget 控制廣播會先確認 widget ID 確實屬於目前 provider，再修改分類／頁碼狀態。
- 沒有增加網路權限或 `QUERY_ALL_PACKAGES`；Analytics 維持純本機。

## 已完成的來源碼檢查

- 24 個 XML 資源均可由 XML parser 成功解析。
- `RecentAppsWidget.kt` 使用的 38 個 `widget_*` ID 均存在於 runtime layout。
- English / zh-TW / zh-CN 三份 `strings.xml` 均為 129 個 key，沒有缺漏。
- `WidgetLayoutPolicy.kt` 已用本機 `kotlinc` 直接編譯並執行 assertion harness，單／雙排、欄數、頁數與循環翻頁檢查通過。
- 單元測試來源已更新，不再引用舊版 `itemsPerPage()` / `MIN_APP_GAP_DP` API。

## 仍須處理／驗證

| 優先 | 項目 | 依據與下一步 |
| --- | --- | --- |
| 發佈門檻 | Android 完整建置 | v0.8.4 已在 Windows/JDK 17/SDK 36 環境成功執行 `:app:testDebugUnitTest :app:assembleDebug`；仍需在其他開發環境確認首次 Gradle 下載與同步。 |
| 發佈門檻 | 真機 Widget 回歸 | AppWidget 的寬高回報由 Launcher 決定。至少用兩個 Launcher 驗證：新增預設尺寸、縮成單排、拉高雙排、2～5 欄、1～15 App、最後一頁不足一排、分類切換與翻頁。 |
| 發佈門檻 | 公開權利與授權 | 確認 PNG 圖示來源與授權；決定程式碼 LICENSE。未定前不要把公開可見等同於已授權修改／再散布。 |
| 發佈門檻 | Library 手勢／回饋真機驗證 | 驗證長按一秒的按壓縮放、震動、星號脈衝與 Snackbar；再測左右拖曳與垂直捲動、手動排序手勢是否互不干擾。 |
| 發佈門檻 | Analytics timeline 真機流暢度 | 連續左右拖曳時間線，確認拖曳過程只更新預覽、放開後才重載詳細統計，且沒有明顯掉幀。 |
| 中 | 使用分析即時性 | 啟用時與排程匯入使用事件；頁面開啟時不保證立即匯入剛發生的外部 App 使用事件。可再評估手動 refresh 與更新時間提示。 |
| 中 | 分析紀錄與實際啟動 | App 列在啟動流程附近記錄 RecentDock launch；仍應用真機確認目標 App 啟動失敗時的統計語意。 |
| 低 | 字體／顯示縮放 | 9sp Widget 標籤與雙排高度門檻需在不同系統字體縮放下檢查截斷與垂直空間。 |
| 低 | 建置腳本 | 現有 `gradlew`/`gradlew.bat` 是下載與 SHA-256 校驗腳本，不是標準 wrapper JAR 流程。可建置環境中可考慮改回官方 wrapper。 |

## 建議 GitHub 節奏

1. 將 v0.8.4 source candidate 提交到候選分支，先跑 CI / 本機 `testDebugUnitTest` 與 `assembleDebug`。
2. 真機測試 Widget 單／雙排與所有寬度，再修正 Launcher 特例。
3. 確認圖示授權、LICENSE、README 截圖與隱私說明後，再標記正式 tag。
4. 若分享 APK，請從已驗證的同一 commit 產生並附 SHA-256 與測試裝置資訊。

GitHub source 發佈與 Google Play 上架是不同流程；本文件只安排 source candidate 到正式 GitHub release 的門檻。


## v0.8.4 uninstall verification
- Library overflow menu launches Android's system uninstall confirmation using `ACTION_DELETE` + `package:` URI.
- Manifest declares `REQUEST_DELETE_PACKAGES` (normal permission, auto-granted at install) as required by Android's UninstallerActivity for modern target SDKs.
- Test both cancel and confirm paths on-device. Confirming should remove the row after returning; cancelling should leave it unchanged.
- No silent-uninstall permission or package-wide query permission was added.
