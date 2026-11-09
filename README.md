# MyLauncher — ColorOS-inspired UI update

A native Android launcher project using Kotlin, XML Views, RecyclerView and ViewPager2.

## Project configuration
- Application ID / namespace: `com.example.mylauncher`
- Minimum SDK: 26 (Android 8.0)
- Compile SDK: 35
- Target SDK: 34
- Android Gradle Plugin: 8.6.1
- Kotlin: 2.0.20
- JDK: 17
- Gradle: 8.7

## UI changes in this revision
- Uses the current system wallpaper as the home background, with a dark gradient overlay for icon readability.
- Large live clock and date header.
- Five-column home app grid and page indicator.
- Rounded translucent dock populated from apps installed on the device (common phone, messaging, browser and photos apps when available; other apps fill remaining spaces).
- Rounded translucent controls and a full-height translucent app drawer with search.
- Existing app discovery, app launching, page transition, wallpaper palette extraction, and Fluid Cloud overlay entry point retained.
- Existing GitHub Actions workflow still builds `:app:assembleDebug` and uploads the Debug APK artifact.

## Important limitations
- This is a ColorOS-inspired independent launcher UI, not OPPO's proprietary ColorOS launcher and not a byte-for-byte recreation.
- The translucent surfaces are not true real-time wallpaper blur. Android does not let a normal launcher blur arbitrary wallpaper behind every view through `RenderEffect` alone.
- Weather, widgets, editable icon placement, icon packs, gestures, and a complete settings screen are not implemented in this revision.
- Fluid Cloud remains a basic overlay prototype and may require Android overlay and foreground-service permissions.
- Clock/date refresh on activity resume; this is not a continuously ticking clock widget.

## Build
Run from the project root:

```bash
gradle assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions: push the project to the `main` or `master` branch, then open **Actions → Build MyLauncher APK**. Download the `MyLauncher-debug-apk` artifact after a successful run.

The source update has not been compiled in this environment. Verify it with GitHub Actions or AndroidIDE before installing. If the build fails, share the complete first Gradle error block for diagnosis.


## Reference-style home screen update
The home screen has been restyled toward the supplied ColorOS 17 reference: centered compact date, oversized thin digital clock, four-column/two-row app pages, a four-icon dock, page dots, and a small app-drawer handle. The default fallback wallpaper is a purple-to-orange gradient; the launcher may display the device wallpaper when Android exposes it. App icons are loaded from installed apps, so their exact artwork depends on the device and installed icon packs. This is a ColorOS-inspired design, not OPPO's proprietary launcher or icon assets.
