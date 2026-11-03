# RecentDock v0.8.4

Version code: **44**

## Uninstall permission fix
- Added `android.permission.REQUEST_DELETE_PACKAGES` to the app manifest.
- This is a normal Android permission and is automatically granted at install time; it does not require a runtime permission prompt.
- Keeps the system-managed uninstall confirmation flow. RecentDock does not silently uninstall other apps.
- Retains the existing post-return cache refresh and cleanup of stale Starred / Pinned / Hidden package state.


## Manual ordering and Widget consistency
- App reordering now starts after a deliberate long-press on the drag handle; the list previews changes while dragging and writes the order only after release.
- Cancelling a drag discards the preview. The draft starts from the displayed pinned-first order so pinned and regular apps keep their own relative ordering.
- The home-screen Widget now reads the same category-specific manual order and pinned state as Library, applies hidden-app filtering before its display limit, and refreshes after reorder, reset, or pin changes.

## Validation status
- `:app:testDebugUnitTest` and `:app:assembleDebug` pass in both the original project and the GitHub release folder; all 34 JVM unit tests pass.
- Real-device drag cancellation, reorder persistence after relaunch, and Widget refresh still require launcher/device regression testing.
- On-device uninstall confirmation/cancel tests remain pending. Test on Android 9+ with both a user-installed app and a protected/system app; system policy may prevent removal of certain packages.
