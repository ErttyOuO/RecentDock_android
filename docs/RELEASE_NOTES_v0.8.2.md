# RecentDock v0.8.2 — Release Notes

## Library interaction feedback

- Long-pressing an App row now gives immediate press feedback: the card subtly compresses and brightens while held.
- Completing the one-second hold still toggles Starred, but now also triggers haptic feedback plus a brief star pulse over the row.
- A Snackbar explicitly confirms whether the App was added to or removed from Starred.
- Star state is updated optimistically in Compose before the Starred snapshot/cache and widget are refreshed, so the UI no longer waits on cache work to show the result.

## Smoother horizontal switching

- The library list now follows horizontal drag input and can switch to the adjacent category after the swipe threshold is reached.
- Category transitions are handled by one controlled two-phase GPU translation/fade instead of changing the category first and starting an animation on the next composition frame.
- Up to 24 cached App icons are pre-warmed off the main thread to reduce first-frame icon decoding when changing categories.
- Cancelled/short swipes animate back instead of snapping.

## Analytics timeline drag optimization

- Timeline dragging now updates only the visual preview while the finger is moving.
- SQLite snapshot/ranking work is committed only when the drag ends (or on a tap), eliminating repeated database queries for every pointer event.

## Version

- `versionName = 0.8.2`
- `versionCode = 42`
