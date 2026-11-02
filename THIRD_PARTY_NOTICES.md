# Third-Party Notices

This file records third-party software and assets identified in the RecentDock Android source tree and Gradle dependency declarations as of 2026-11-02.

The MIT license in the repository root applies to original RecentDock source code and project documentation authored by the project copyright holder. It does not replace or override the separate licenses of third-party libraries, icons, or artwork. Full copies of the Apache License 2.0, Eclipse Public License 1.0 and BSD 3-Clause license are included in `licenses/`. The MIT license and the third-party notice/license texts are also copied into `app/src/main/assets/` so they are included in the APK; the current app UI does not yet provide a dedicated license viewer.

## 1. Runtime libraries

| Component | Gradle coordinate | Resolved version | Purpose in RecentDock | License |
|---|---|---:|---|---|
| Kotlin Standard Library | `org.jetbrains.kotlin:kotlin-stdlib` | 2.3.21 | Kotlin runtime | Apache-2.0 |
| AndroidX Core | `androidx.core:core` | 1.17.0 | Android compatibility APIs | Apache-2.0 |
| AndroidX Activity Compose | `androidx.activity:activity-compose` | 1.12.4 | Compose Activity integration | Apache-2.0 |
| AndroidX Lifecycle Runtime KTX | `androidx.lifecycle:lifecycle-runtime-ktx` | 2.10.0 | Lifecycle-aware coroutine support | Apache-2.0 |
| Jetpack Compose BOM | `androidx.compose:compose-bom` | 2026.06.00 | Aligns Compose artifact versions; it is a version platform, not a runtime library by itself | Apache-2.0 ecosystem |
| Jetpack Compose Material 3 | `androidx.compose.material3:material3` | 1.4.0 | UI components and Material theme | Apache-2.0 |
| Jetpack Compose Foundation | `androidx.compose.foundation:foundation` | 1.11.3 | Layout, gestures and foundation UI | Apache-2.0 |
| Jetpack Compose UI | `androidx.compose.ui:ui` | 1.11.3 | Compose UI primitives | Apache-2.0 |
| Compose UI Tooling Preview | `androidx.compose.ui:ui-tooling-preview` | 1.11.3 | Preview annotations and tooling API | Apache-2.0 |
| Material Icons Extended | `androidx.compose.material:material-icons-extended` | 1.7.8 | Material icon vectors used by the app | Apache-2.0 |
| AndroidX WorkManager | `androidx.work:work-runtime` | 2.11.2 | Scheduled background work | Apache-2.0 |
| Kotlin Coroutines Android | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.10.2 | Asynchronous and background execution | Apache-2.0 |

AndroidX / Jetpack libraries are maintained in the AndroidX project. Kotlin and kotlinx.coroutines are maintained by JetBrains / Kotlin contributors. See the upstream project license files linked below.

## 2. Icons and vector assets

RecentDock uses Material Icons through `material-icons-extended` in `LibraryScreen.kt`, including OpenInNew, Sort, DeleteOutline, Info, PushPin, Settings, StarOutline, Visibility, VisibilityOff, BarChart, Check, Close, DownloadForOffline, DragHandle, History, Menu, MoreHoriz, Search and Star.

The vector path data in these two project resources matches the Material arrow-drop icons:
- `app/src/main/res/drawable/ic_widget_chevron_up.xml` — Material `arrow_drop_up` path.
- `app/src/main/res/drawable/ic_widget_chevron_down.xml` — Material `arrow_drop_down` path.

Material Icons are provided by Google under Apache License 2.0. Google says attribution in an app's About screen is appreciated but not required for normal use. The full Apache 2.0 license is included at `licenses/Apache-2.0.txt`.

Upstream: https://developers.google.com/fonts/docs/material_icons
Source repository: https://github.com/google/material-design-icons

The following vector resources appear to be RecentDock-specific shapes or simple custom chevrons; no external attribution marker was found in the source tree:
- `ic_widget_chevron_left.xml`
- `ic_widget_chevron_right.xml`
- `widget_preview_fallback.xml`
- `widget_*_background.xml`

## 3. Test and debug-only dependencies

These are used for tests or debug tooling and are not part of the release runtime dependency configuration.

| Component | Gradle coordinate | Declared version | License |
|---|---|---:|---|
| JUnit 4 | `junit:junit` | 4.13.2 | EPL-1.0 |
| Hamcrest Core (JUnit transitive) | `org.hamcrest:hamcrest-core` | 1.3 | BSD-3-Clause (New BSD) |
| AndroidX Test JUnit Extension | `androidx.test.ext:junit` | 1.3.0 | Apache-2.0 |
| Espresso Core | `androidx.test.espresso:espresso-core` | 3.7.0 | Apache-2.0 |
| Compose UI Test JUnit4 | `androidx.compose.ui:ui-test-junit4` | Compose BOM-managed | Apache-2.0 |
| Compose UI Test Manifest | `androidx.compose.ui:ui-test-manifest` | Compose BOM-managed; debug only | Apache-2.0 |
| Compose UI Tooling | `androidx.compose.ui:ui-tooling` | Compose BOM-managed; debug only | Apache-2.0 |

JUnit's EPL-1.0 license is included at `licenses/EPL-1.0.md`; its Hamcrest Core 1.3 transitive dependency uses the New BSD / BSD-3-Clause license included at `licenses/BSD-3-Clause.txt`.

## 4. Build tools (not packaged in the app)

- Gradle 9.3.1
- Android Gradle Plugin 9.1.1
- Kotlin Compose Gradle plugin 2.3.21

These are development/build tools and are not RecentDock runtime libraries.

## 5. Transitive dependencies

The resolved runtime graph also contains transitive AndroidX modules (including Lifecycle, Room, SQLite, SavedState, Startup, Tracing, ProfileInstaller and Compose submodules), Kotlin/Kotlinx support artifacts, Guava ListenableFuture 1.0, JSpecify 1.0.0 and JetBrains annotations 23.0.0. The AndroidX, Kotlin/Kotlinx, Guava ListenableFuture, JSpecify and JetBrains annotations artifacts in this graph are Apache-2.0. These are dependency-provided components, not source files copied into the RecentDock source tree. Their versions can change when upstream dependencies or the Compose BOM are updated; regenerate and review the dependency/license inventory before each binary release.

## 6. Project artwork — ownership confirmation required

The following PNG files are present in the source tree:
- `app/src/main/res/drawable/recentdock_app_icon.png`
- `app/src/main/res/drawable/recentdock_widget_all_icon.png`

The files contain no source or license metadata that establishes who created them or what reuse rights apply. Their appearance alone cannot establish provenance. Until the project owner confirms that these are original artwork or identifies the source and license, **these two PNG assets are not included in the MIT license grant** and should not be redistributed or relicensed as MIT.

## 7. Source-code provenance review

A source-tree scan found:
- No explicit external source-code URLs, Stack Overflow links, copyright headers, or copied-snippet attribution comments in the Kotlin source files.
- External API imports are from Android platform APIs, AndroidX / Jetpack, Kotlin / Kotlinx, Java standard libraries, and JUnit in tests.
- The app's Material Icons use is through the declared AndroidX Material Icons dependency; the two arrow-drop vector paths above are also recorded as Material-derived assets.
- The staging Git repository has an initial source-import commit and subsequent maintenance commits; it does not preserve the complete development history of every source file.

This is a source-tree and dependency audit, not a proof that no code was ever adapted from an external tutorial, answer, or repository. Any such manually copied or adapted snippets should be disclosed with their original URL and license before public release.

## Upstream license references

- AndroidX / Jetpack: https://github.com/androidx/androidx
- Kotlin: https://github.com/JetBrains/kotlin
- Kotlin Coroutines: https://github.com/Kotlin/kotlinx.coroutines
- Material Icons: https://developers.google.com/fonts/docs/material_icons
- JUnit 4: https://github.com/junit-team/junit4
- Apache License 2.0: https://www.apache.org/licenses/LICENSE-2.0
- Eclipse Public License 1.0: https://www.eclipse.org/legal/epl-v10.html
