# RecentDock v0.8.4

Version code: **44**

## Uninstall permission fix
- Added `android.permission.REQUEST_DELETE_PACKAGES` to the app manifest.
- This is a normal Android permission and is automatically granted at install time; it does not require a runtime permission prompt.
- Keeps the system-managed uninstall confirmation flow. RecentDock does not silently uninstall other apps.
- Retains the existing post-return cache refresh and cleanup of stale Starred / Pinned / Hidden package state.

## Validation status
- Manifest declaration and source-level checks updated.
- Full Gradle build and on-device uninstall confirmation/cancel regression tests are still pending.
- Test on Android 9+ devices with both a user-installed app and a protected/system app. System policy may prevent removal of certain packages.
