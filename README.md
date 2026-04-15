# GSplit

GSplit is an Android app for split-screen and multi-window presets: pair two apps and launch them in freeform or split-style layouts, with scheduling, boot autostart, overlays, and optional ADB-assisted configuration. It is built with Jetpack Compose, a multi-module architecture, and background services for boot handling and split orchestration.

## Support the project

If this app helps you, you can support further development, maintenance, and new features.

[![Donate](https://img.shields.io/badge/Donate-CloudTips-orange?style=for-the-badge)](https://pay.cloudtips.ru/p/19d38600)

### Crypto
- **BTC:** `bc1q37z3d7avhsq3ehpsjm2wldj86ajsnsd6gqnkzm`
- **ETH:** `0x69C73C422FEBBf12F47C29C51501Ad659fcdf74A`

Thanks for supporting the project.

## What This Project Does

- Manages **presets** (two apps per preset) and launches them via `core:splitLauncher` and `core:splitPresets`.
- Offers **preset list and create/edit** flows in `feature:split:list` and `feature:split:add`, plus a **stub / dark filler** screen in `feature:split:stub`.
- Splits **settings** across dedicated feature modules (general, scheduler, autostart, presets, UI, ADB, overlays, dark screen, window shift, app tasks, replacement apps, API, and shared `feature:settings:common`) and a **system overlay** in `feature:overlay`.
- Persists global state and schedules through `core:stateKeeper`, `core:schedulerStorage`, and related storage modules.
- Integrates **Firebase** (analytics, remote configuration, Crashlytics) and optional **ADB shell** usage from `core:adb`.
- Uses **accessibility**, **foreground service**, and **boot completed** receivers where configured for autostart and experimental native split behavior.

## Tech Stack

- Kotlin + Coroutines + Flow
- Jetpack Compose (Material 3, Navigation Compose)
- Hilt (DI)
- AndroidX DataStore
- Timber (logging)
- Coil (image loading)
- Firebase (Google services, Crashlytics)
- Detekt + Ktlint (static analysis and formatting)
- Baseline Profile

## Module Structure

This is a multi-module project. Gradle includes container projects `core` and `feature` (no code; they group subprojects). Notable modules:

**Application**

- `app` — application entry point (`App`, `MainActivity`), Compose `NavHost`, `BootReceiver`, `WakeUpForegroundService`, `AutoLaunchAccessibilityService`.

**Feature modules**

- `feature:split` — split presets UX; submodules include `feature:split:list` (preset list), `feature:split:add` (create/edit), `feature:split:stub` (dark filler).
- `feature:settings` — settings umbrella; concrete screens live in `feature:settings:general`, `scheduler`, `autostart`, `presets`, `ui`, `adb`, `closingOverlay`, `appSwitchOverlay`, `darkScreenMode`, `windowShiftMode`, `appTasks`, `replacementApps`, `api`, `common`, and related packages.
- `feature:overlay` — system overlay service integration.

**Core — split, apps, and device**

- `core:splitLauncher` — launch apps in split / multi-window.
- `core:splitPresets` — preset domain and data.
- `core:systemApps` — installed apps discovery and management.
- `core:screenSpecs` — screen metrics and layout.
- `core:mediaMonitor` — media playback state.
- `core:launchHistory` — launch history tracking.
- `core:adb` — ADB shell helpers for advanced settings.

**Core — state and storage**

- `core:stateKeeper` — global app state.
- `core:schedulerStorage` — scheduled launches.
- `core:replacementAppsStorage` — replacement app mappings.
- `core:preferences` — DataStore-backed preferences.

**Core — cloud and downloads**

- `core:firebase` — Firebase integration.
- `core:remoteConfig` — remote configuration.
- `core:fileDownloader` — file download by URL.

**Core — shared infrastructure**

- `core:base` — shared Android base types and dependencies.
- `core:navigation` — navigation graphs, typed routes, and transitions (`SplitNavGraph` hosts the main split flow).
- `core:resources` — shared resources (strings, assets).
- `core:ui` / `core:uikit` — shared and app-specific UI.
- `core:coil` — Coil setup for image loading.

**Tooling**

- `baselineprofile` — Baseline Profile generation for startup performance (used with the Baseline Profile Gradle plugin and the `prepareRelease` task chain in `app`).

## Requirements

- JDK 17
- Android SDK (compileSdk 34, targetSdk 34, minSdk 24)
- Gradle Wrapper (`gradle-8.6`)
- Android Studio / IntelliJ with Android support

## Quick Start

1. Clone the repository.
2. Open the project in Android Studio.
3. Make sure Android SDK API 34 is installed and configured.
4. Build debug:

```bash
./gradlew :app:assembleDebug
```

For Windows PowerShell:

```powershell
.\gradlew :app:assembleDebug
```

Debug, internal, and car build types reference a **dummy** keystore at `app/dummy.jks`. If that file is not present in your checkout, add a compatible keystore or adjust `signingConfigs` in `app/build.gradle.kts` before building those variants.

## Useful Commands

- Build:
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:assembleRelease`
  - `./gradlew :app:assembleInternal` / `./gradlew :app:assembleCar` (additional product flavors)
- Lint / style checks:
  - `./gradlew ktlint`
  - `./gradlew detekt` (Detekt also runs as part of the app build)
- Auto-format:
  - `./gradlew ktlintFormat`
- Release preparation (Baseline Profile for release + release APK):
  - `./gradlew prepareRelease`
- Other flavor Baseline Profile chains (see `app/build.gradle.kts`):
  - `./gradlew prepareInternal`
  - `./gradlew prepareCar`

## Release Signing

The project supports external signing configuration:

1. Copy `_secure.signing.gradle` to `secure.signing.gradle` (or point to a custom path via `secure.signing=...` in `gradle.properties`).
2. Add signing parameters in that file.
3. Run:

```bash
./gradlew prepareRelease
```

Release outputs are produced under `app/build/outputs/apk/` and `app/build/outputs/bundle/` as configured by your signing and build types.

> Debug, internal, and car builds use the dummy signing config when `app/dummy.jks` is available; configure `secure.signing` for production release signing as needed.

## Navigation and UI Flow

- `MainActivity` sets up Compose content and a `NavHost` with **`startDestination = SplitNavGraph`** (`core:navigation`), registering the main graph via `splitGraph(...)`.
- `PresetLauncherActivity`, `ShortcutActivity`, and `MultiWindowHeatingActivity` provide additional entry points for presets and multi-window behavior (see `AndroidManifest.xml`).

## Permissions and System Notes

The app uses (among others) the following permissions from `AndroidManifest.xml`:

- `INTERNET`
- `QUERY_ALL_PACKAGES`
- `SYSTEM_ALERT_WINDOW`
- `WRITE_SETTINGS`
- `WRITE_SECURE_SETTINGS`
- `ACCESS_NETWORK_STATE`
- `RECEIVE_BOOT_COMPLETED`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- Accessibility service (`AutoLaunchAccessibilityService`)

Freeform multi-window and secure settings often require explicit user grants or a **privileged / automotive** firmware context. An optional experimental path launches split layouts via the Accessibility API on supported devices (see in-app “experimental native split” settings).

## Notes

- GSplit targets workflows on large Android devices and in-vehicle head units where running two apps side by side is a primary use case.
- Vendor- or device-specific behavior may limit freeform windows or accessibility-based split on some OEM builds.
