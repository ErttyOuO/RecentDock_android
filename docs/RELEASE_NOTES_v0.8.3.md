# RecentDock v0.8.3

Version code: **43**

## App-list uninstall action
- Added **Uninstall** to every App row's `⋯` action menu.
- Uses Android's standard system uninstall confirmation (`Intent.ACTION_DELETE` with a `package:` URI). RecentDock does not silently uninstall apps.
- After returning from the system confirmation screen, RecentDock forces a fresh package scan even if its cache was still considered fresh.
- Confirmed removals disappear from the list promptly and existing cleanup removes stale Starred / Pin / Hidden package state; cancelled removals keep the App unchanged.
- If Android cannot open the uninstall screen, RecentDock shows a Snackbar instead of failing silently.

## Retained from v0.8.2
- One-second long-press feedback with scale/highlight, haptic, star pulse and explicit Snackbar.
- Smoother horizontal category swipe and rebound behavior.
- Icon pre-warm and reduced Analytics timeline DB work during drag.
- Adaptive one-/two-row RemoteViews widget from v0.8.1.

## Validation
Source/XML/string consistency checks pass. Full Gradle build remains unverified in the current sandbox because `services.gradle.org` cannot be resolved to download Gradle 9.3.1.
