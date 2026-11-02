# Validation Report — RecentDock v0.8.3

## Source checks
- Version: `0.8.3`, versionCode `43`.
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
- English / zh-TW / zh-CN string resources contain the same 127 keys.
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
- The action uses `Intent.ACTION_DELETE` with a `package:` URI; no `REQUEST_DELETE_PACKAGES`, `QUERY_ALL_PACKAGES`, shell command, or silent uninstall path was added.
- A pending uninstall flow bypasses the normal foreground-cache freshness early return once, so returning from Android's confirmation screen refreshes package state immediately.
- Repository refresh already calls `retainInstalled()` for Starred, Pinned and Hidden stores; custom order is reconciled from refreshed category packages.
- If the system uninstall intent cannot launch, the force-refresh flag is reset and an error Snackbar is shown.
- All 24 XML resource files parse successfully after the new strings; English / zh-TW / zh-CN each contain 129 string keys with exact key parity.
- `kotlinc` parser scan of `LibraryScreen.kt` reports Android/Compose unresolved references only and no `expecting`, `unclosed`, `unexpected tokens`, or syntax diagnostics.

## Gradle verification
Attempted `./gradlew :app:testDebugUnitTest :app:assembleDebug` after the v0.8.3 uninstall-menu changes.

Result: **not completed** because the sandbox cannot resolve `services.gradle.org`, so Gradle 9.3.1 cannot be downloaded. This report does not claim a successful Android build.

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
