# RecentDock v0.8.5

Version code: **45**

## Manual ordering and Widget consistency
- Library manual ordering starts from a deliberate long-press on the drag handle.
- Dragging updates an in-memory preview; the order is persisted only when the gesture ends normally.
- Cancelling the gesture discards the draft and leaves the saved order unchanged.
- The draft begins with the displayed pinned-first order, preserving the relative order of pinned and regular apps.
- The home-screen Widget uses the same category-specific manual order and pinned state as Library, filters hidden apps before applying the display limit, and refreshes after reorder, reset, or pin changes.

## Validation
- JVM unit tests and Debug/Release APK builds: pending final v0.8.5 run in both the original project and GitHub release folder.
- Android emulator installation, app launch, version upgrade, and Widget refresh: pending.
- Physical-device and multi-Launcher regression testing remains required before a public release.
- Existing icon artwork ownership and public-use rights still need confirmation before a formal public release.
