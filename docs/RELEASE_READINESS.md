# RecentDock 0.8.5：GitHub 發佈檢查

狀態：**source candidate；versionName 0.8.5 / versionCode 45。待升版後重新完成原始專案與 GitHub 發布資料夾的測試、建置及 Android 模擬器回歸；實體裝置／多 Launcher 驗證與圖示公開權利確認仍是正式 release 前置條件。**

## 本次已處理

- v0.8.0：Analytics 可單獨清除、跨分類 `LAST_USED` 排序、App Detail Screen。
- v0.8.1：RemoteViews Widget 支援依高度自動切換單排／雙排，常見寬度雙排最多 10 個 App／頁。
- v0.8.2：List 長按加入按壓／星號特效與 Snackbar；Library 左右拖曳切換改成受控動畫；Analytics 時間線拖曳只在放開後查詢詳細統計。
- v0.8.3：App 列表 `⋯` 選單新增系統解除安裝入口，返回後強制重新整理清單並清理已移除 package 的狀態。
- v0.8.4：修正解除安裝權限缺漏，Manifest 新增 `REQUEST_DELETE_PACKAGES` normal 權限；系統確認畫面仍由 Android 管理。
- v0.8.5：versionName 升至 0.8.5、versionCode 升至 45；納入長按拖曳即時預覽／放開提交、取消復原，以及 Widget 與 Library 手動排序和釘選同步。
- Library 手動排序改為拖曳把手長按啟動、拖曳中只預覽、放開才儲存；取消手勢會捨棄預覽，並以畫面實際的釘選優先順序作為排序草稿基準。
- Widget 套用與 Library 相同的分類手動排序及釘選順序，隱藏篩選後才計算顯示數量；排序提交、重設排序或釘選變更後立即更新 Widget。
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
- 排序回歸測試涵蓋上下移動、拖曳草稿提交／復原、釘選分組，以及 Widget 的手動排序、隱藏篩選與顯示上限；共 34 項 JVM 單元測試通過。

## 仍須處理／驗證

| 優先 | 項目 | 依據與下一步 |
| --- | --- | --- |
| 發佈門檻 | Android 完整建置 | v0.8.5 升版後須在 Windows/JDK 17/SDK 36 環境執行 JVM 測試及 Debug／Release 建置，並確認 APK 版本資訊。 |
| 發佈門檻 | 真機 Widget 回歸 | AppWidget 的寬高回報由 Launcher 決定。至少用兩個 Launcher 驗證：新增預設尺寸、縮成單排、拉高雙排、2～5 欄、1～15 App、最後一頁不足一排、分類切換與翻頁。 |
| 發佈門檻 | 公開權利與授權 | 原創程式碼已採 MIT，並加入 `LICENSE`、`THIRD_PARTY_NOTICES.md` 與第三方授權文字；授權文件已複製至 APK assets。兩個 PNG 圖示的創作來源與公開使用權利仍待確認，確認前不將其納入 MIT 授權。 |
| 發佈門檻 | Library 排序／手勢真機驗證 | 驗證排序把手長按啟動、拖曳即時預覽、放開儲存、取消復原、重開 App 後順序保持，以及 Widget 同步；並確認左右切換與垂直捲動不受影響。 |
| 發佈門檻 | Analytics timeline 真機流暢度 | 連續左右拖曳時間線，確認拖曳過程只更新預覽、放開後才重載詳細統計，且沒有明顯掉幀。 |
| 中 | 使用分析即時性 | 啟用時與排程匯入使用事件；頁面開啟時不保證立即匯入剛發生的外部 App 使用事件。可再評估手動 refresh 與更新時間提示。 |
| 中 | 分析紀錄與實際啟動 | App 列在啟動流程附近記錄 RecentDock launch；仍應用真機確認目標 App 啟動失敗時的統計語意。 |
| 低 | 字體／顯示縮放 | 9sp Widget 標籤與雙排高度門檻需在不同系統字體縮放下檢查截斷與垂直空間。 |
| 低 | 建置腳本 | 現有 `gradlew`/`gradlew.bat` 是下載與 SHA-256 校驗腳本，不是標準 wrapper JAR 流程。可建置環境中可考慮改回官方 wrapper。 |

## 建議 GitHub 節奏

1. 將 v0.8.5 source candidate 與 MIT／第三方授權文件提交到候選分支，先跑 CI / 本機 JVM 測試與 Debug／Release 建置。
2. 真機測試 Widget 單／雙排與所有寬度，再修正 Launcher 特例。
3. 確認兩個 PNG 圖示的權利、README 截圖與隱私說明後，再標記正式 tag。
4. 若分享 APK，請從已驗證的同一 commit 產生並附 SHA-256 與測試裝置資訊。

GitHub source 發佈與 Google Play 上架是不同流程；本文件只安排 source candidate 到正式 GitHub release 的門檻。


## v0.8.4 uninstall verification
- Library overflow menu launches Android's system uninstall confirmation using `ACTION_DELETE` + `package:` URI.
- Manifest declares `REQUEST_DELETE_PACKAGES` (normal permission, auto-granted at install) as required by Android's UninstallerActivity for modern target SDKs.
- Test both cancel and confirm paths on-device. Confirming should remove the row after returning; cancelling should leave it unchanged.
- No silent-uninstall permission or package-wide query permission was added.
