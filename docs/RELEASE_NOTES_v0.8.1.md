# RecentDock v0.8.1 — Release Notes

## Adaptive two-row widget
RecentDock's RemoteViews widget now preserves the original compact pill design while supporting a taller two-row form. At compact height it stays single-row; when the launcher reports roughly 124dp or more, it can show a second row automatically.

- 2–5 columns per row depending on available width.
- Up to 10 visible apps per page on a normal wide two-row widget.
- Left category / RecentDock controls remain fixed.
- Right paging control stays vertically centered across the full widget.
- Partial pages collapse an unused second row for better balance.
- Widget picker preview and setup copy now explain / display the two-row behavior.

## Fixes and polish
- Fixed slot assignment on narrow two-row widgets (2–4 columns per row).
- Increased app-label readability to 9sp and adjusted row spacing.
- Increased tall pill corner radius for a softer shape without changing the compact pill feel.
- Added validation that ignores widget-control broadcasts for unknown widget IDs.
- Updated widget layout-policy tests for the adaptive layout.

## Validation note
Static XML/resource checks and direct Kotlin checks of the layout policy pass. A full Android Gradle build could not be completed in the current sandbox because `services.gradle.org` cannot be resolved. Run `:app:testDebugUnitTest` and `:app:assembleDebug` on a networked Android development machine before publishing a release APK.
