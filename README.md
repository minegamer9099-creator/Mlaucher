# MyLauncher Android Project

A starter native Android launcher project using Kotlin, XML Views, RecyclerView, ViewPager2, Jetpack Palette, and a floating Fluid Cloud overlay.

## Project details

- Application ID: `com.example.mylauncher`
- Minimum Android version: Android 8.0 (API 26)
- Compile SDK: 35
- Target SDK: 34
- JDK: 17
- Android Gradle Plugin: 8.6.1
- Gradle: 8.7

## Build on GitHub

1. Create a new GitHub repository.
2. Extract this ZIP and upload all files and folders to the repository root.
3. Commit the files to `main` (or `master`).
4. Open the repository's **Actions** tab.
5. Select **Build MyLauncher APK** and wait for the workflow to finish.
6. Open the successful workflow run and download the `MyLauncher-debug-apk` artifact.
7. Extract the artifact ZIP to get `app-debug.apk`.

The workflow builds a debug APK. It does not sign a release APK for Play Store distribution.

## Build locally in AndroidIDE

Open the project root in AndroidIDE and run:

```bash
gradle assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

Use JDK 17 and Gradle 8.7.

## Features in this starter

- Android HOME launcher intent filter and normal app-drawer launch icon.
- Installed app discovery and launching.
- Four-column app grids.
- Three-dimensional ViewPager2 page transition.
- Searchable app drawer with translucent glass-style background.
- Wallpaper palette extraction and refresh on resume / wallpaper-change broadcast.
- Optional Fluid Cloud overlay with a foreground notification and spring scale animation.

## Important limitations

- The drawer is translucent; this project does not claim true real-time wallpaper blur behind the drawer.
- Fluid Cloud is a basic floating pill, not a full media controller or notification aggregator.
- Overlay permission must be granted manually in Android Settings.
- Android may stop foreground services due to system policy, battery management, or user action.
- The punch-hole/camera position is not automatically detected.
- 120 FPS is not guaranteed; actual frame rate depends on the device and workload.
- This source has not been built on your phone. Use GitHub Actions or AndroidIDE to verify it, and inspect the build log if it fails.

## Set as default launcher

After installing the APK:

Settings -> Apps -> Default apps -> Home app -> MyLauncher

Menu names vary by phone manufacturer.
