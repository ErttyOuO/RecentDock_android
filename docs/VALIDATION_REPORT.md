# Validation Report — RecentDock v0.8.4

## Source checks
- Version: `0.8.4`, versionCode `44`.
- Analytics clear operation still targets only the two analytics event tables.
- Existing Star, Pin, Hidden and Custom Order stores are not referenced by clear-data code.
- `LAST_USED` continues to use cached `RecentApp.lastUsedTime`; no new package scan was added.
- App Detail continues to use existing cache / `AppIconCache` and Android App Info uses `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
- No `QUERY_ALL_PACKAGES` added.

## Widget checks
- Compact layout remains one row; `WidgetLayoutPolicy.layoutFor()` switches to two rows at 124dp height.
- Width policy remains 2/3/4/5 columns, producing 4/6/8/10 visible slots in a two-row layout.
- Narrow two-row rendering uses first-row slot indices and second-row slot indices independently; row 2 no longer incorrectly starts after slot 5.
- A page with no second-row content hides row 2 so the remaining row stays vertically centered.
- Runtime widget code references 38 `widget_*` IDs; all were found in `widget_recent_dock.xml`.
- All 24 XML resource files parse successfully.
- English / zh-TW / zh-CN string resources contain the same 129 keys.
- `WidgetLayoutPolicy.kt` was compiled directly with local `kotlinc` and an executable assertion harness passed.
- Widget control broadcasts now reject IDs not returned by `AppWidgetManager` for the provider.

## Interaction/performance checks
- Long-press code exposes pressed state to Compose, scales/highlights the row while held, performs `HapticFeedbackType.LongPress`, and runs a star pulse when the one-second hold completes.
- Add/remove Starred status is shown through `SnackbarHostState`; Starred package state is updated before background cache/widget work.
- Category change no longer relies on a `LaunchedEffect(category)` that starts after content is already replaced. The transition is coordinated before/after the category mutation, and horizontal drag directly offsets the rendered layer.
- App icon pre-warm runs on `Dispatchers.IO` and is capped at 24 distinct packages.
- Analytics timeline has separate preview and committed indexes; `repository.snapshot(...)` is keyed to the committed index only. Drag events therefore do not trigger one DB query per pointer update.
- Kotlin compiler syntax-only/error scan found no `expecting`/`unclosed`/`unexpected` parser errors in the two modified Compose source files. Full type checking still requires the Android/Compose Gradle classpath.


## Uninstall action checks
- Library row overflow menu includes `Uninstall` / `解除安裝` / `卸载` in all three locales.
- The action uses `Intent.ACTION_DELETE` with a `package:` URI. The manifest now declares `REQUEST_DELETE_PACKAGES`, a normal permission automatically granted at install time and required by the system UninstallerActivity for modern target SDKs. System confirmation remains mandatory; no `QUERY_ALL_PACKAGES`, shell command, or silent uninstall path is added.
- A pending uninstall flow bypasses the normal foreground-cache freshness early return once, so returning from Android's confirmation screen refreshes package state immediately.
- Repository refresh already calls `retainInstalled()` for Starred, Pinned and Hidden stores; custom order is reconciled from refreshed category packages.
- If the system uninstall intent cannot launch, the force-refresh flag is reset and an error Snackbar is shown.
- All 24 XML resource files parse successfully after the new strings; English / zh-TW / zh-CN each contain 129 string keys with exact key parity.
- `kotlinc` parser scan of `LibraryScreen.kt` reports Android/Compose unresolved references only and no `expecting`, `unclosed`, `unexpected tokens`, or syntax diagnostics.

## Gradle verification
Ran `gradlew.bat :app:testDebugUnitTest :app:assembleDebug` for v0.8.4 in the Windows/JDK 17/Android SDK 36 environment.

Result: **BUILD SUCCESSFUL in 1m 21s** from the prepared GitHub staging folder. The XML test reports contain **29 tests, 0 failures, 0 errors, and 0 skipped** (AnalyticsPeriodTest: 3; AppCategoryTest: 3; ManualAppOrderingTest: 3; WidgetLayoutPolicyTest: 20). Both `:app:testDebugUnitTest` and `:app:assembleDebug` completed successfully. The APK contains `assets/LICENSE`, `assets/THIRD_PARTY_NOTICES.md`, and the Apache-2.0, EPL-1.0 and BSD-3-Clause license texts. `local.properties` is ignored by Git. No Android device was connected during this run, so uninstall confirmation/cancel and widget behavior still require on-device regression testing.

## Third-party and source provenance review
- Added repository MIT license for original RecentDock source code and project documentation.
- Added `THIRD_PARTY_NOTICES.md`, a copy of Apache License 2.0, and a copy of Eclipse Public License 1.0 and BSD 3-Clause for identified dependencies. The MIT license and notices are also copied into `app/src/main/assets/` so they are packaged into the APK; no in-app license viewer has been added.
- Gradle runtime dependency graph was reviewed; the direct runtime libraries are AndroidX / Jetpack Compose / Material Icons, Kotlin Standard Library, and Kotlin Coroutines.
- JUnit 4.13.2 is test-only and uses EPL-1.0; AndroidX Test / Espresso and Compose test/debug tooling are test/debug-only.
- Material Icons and the two arrow-drop vector paths are documented as Apache-2.0 assets.
- The source scan found no explicit third-party source-code URLs or copied-snippet attribution headers. This does not prove that no code was ever adapted from an uncredited tutorial or repository; the staging Git history does not preserve the complete original development history.
- The two PNG app/widget artwork files have no embedded source/license metadata and remain excluded from the MIT grant pending owner confirmation.

## Required local verification
```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Then verify on-device:
1. Add the widget at its default two-cell height and confirm two rows appear when the reported height is at least 124dp.
2. Resize downward and confirm it returns to a single row without clipping.
3. Resize horizontally through 2/3/4/5-column widths and check that both rows align correctly.
4. Test 1–15 available apps, including partial last pages and paging wraparound.
5. Test category cycling, RecentDock button, app launching, usage-access empty state, and hidden-app empty state.
6. Repeat on at least two launchers because AppWidget size reporting differs by launcher.
7. In Library, hold an App for one second: verify press-scale/highlight, haptic, star pulse and add/remove Snackbar; release before one second should still launch normally.
8. Swipe Library left/right slowly and quickly; verify content follows the finger, short swipes return smoothly, and threshold swipes switch exactly one adjacent category.
9. Drag Analytics timeline continuously and verify the cursor/summary tracks the finger without ranking/stat cards stuttering; the detailed snapshot should refresh after release.
10. Open an App row `⋯` menu, choose Uninstall, cancel once and confirm the App remains; repeat and confirm removal, then verify the row disappears after returning and no stale Star/Pin/Hidden entry remains.
