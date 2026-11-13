# Changelog

All notable changes to this fork of QuickTiles. The format loosely follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## 2.1.1 - 2026-11-02

The same app in a third of the download size: **6.42 MB -> 2.02 MB**.

### Size

* R8 (`isMinifyEnabled`) and the resource shrinker (`isShrinkResources`) are enabled for release
  builds. Keep rules cover the preference classes that are inflated from XML, SearchPreference and
  aboutlibraries; our own class names and line numbers are kept, because the tile diagnostics record
  stack traces and those have to stay readable.
* Resource configurations are limited to the four locales the app is translated into (en, de, tr,
  zh-rTW). The support libraries ship translations for every locale there is and that table was the
  biggest single thing in the apk - it cost 858 KiB of `resources.arsc`, now 540 KiB. Anyone whose
  system language is not one of those four sees English for the library strings, exactly like the
  app's own strings already were.
* Play dependency metadata and `META-INF` licence/notice files are no longer packaged.

### Build

* Every build now reports what the apk is made of (largest entries and the split between code,
  resources and metadata) as an annotation of the run, so the next size change can be judged instead
  of guessed. The R8 mapping file is uploaded as an artifact.

## 2.1.0 - 2026-11-02

A whole new look: Material 3, wallpaper colours and a settings screen that reads as sections.

### Look and feel

* Theme is now `Theme.Material3.DayNight.NoActionBar`. On Android 12+ the app takes its palette from
  the wallpaper (Material You); every older version keeps the brand violet.
* The settings screen has an app bar with a large title and a live "x of 55 tiles enabled" subtitle.
  The rows of each category sit on a rounded card, so the 16 sections are visible at a glance instead
  of looking like one endless list.
* Icons are tinted with the accent colour, which means they follow the wallpaper as well.
* All permission, credits, diagnostics and self check dialogs are Material 3 dialogs.
* The app picker is a bottom sheet with a search field instead of a list dialog you have to scroll.
* One-time hint after the first enabled tile: enabling a tile and adding it to Quick Settings are two
  different steps, and only the second one puts it in the shade.
* Status and navigation bar icons now follow the theme; they used to be forced dark, which made them
  invisible in dark mode.

### Internals

* `PreferenceCardDecoration` draws the cards, so no preference layout had to be replaced.
* Theme colours are resolved for both resource and dynamic (runtime) colour values.

## 2.0.1 - 2026-11-02

Maintenance release: tests, a CI check for the tile plumbing, and a cheaper diagnostics recorder.

### Tests

* JVM unit tests for the three places that have to agree for a tile to work: the switch in
  `root_preferences.xml`, the key to service table and the `<service>` entry in the manifest. A tile
  that is missing in one of them silently does nothing on the device, which is exactly the class of
  bug this project kept hitting.
* Unit tests for the selection tile value cycling (`ValueCycle`) and the diagnostics helpers.
* New `tests` job in the workflow (`./gradlew testDebugUnitTest`); lint runs next to it without
  blocking the release. The generated `ExampleUnitTest`/`ExampleInstrumentedTest` are gone.

### Diagnostics

* Recording is now opt-in (**Troubleshooting → Record tile diagnostics**) and switches itself on as
  soon as an error or a crash is recorded, so failures are never lost while normal use stays silent.
* Entries are written on a single background thread instead of rewriting the whole buffer on the
  calling thread for every tile callback.
* New **Troubleshooting → Self check**: one screen that lists enabled tiles with missing
  permissions, tile services the system disabled, the battery state and the last process exits.
* The diagnostics file is excluded from cloud backup.

### Internals

* The tile key to service table moved out of `SettingsActivity` into the plain Java class
  `TileRegistry`, which the tests can load without an Android runtime.
* Release notes are generated from this file instead of from pull request titles.

## 2.0.0 - 2026-11-02

The release that makes Quick Settings tiles work while QuickTiles is **not** running, and that
turns the CI into a release pipeline.

### Android 17 support

* Tile services no longer depend on in-memory state. The current value of a tile is read from the
  system on every tap instead of being cached between clicks, so a tile service that the system
  creates fresh (which happens on every tap once the app process is gone) still has everything it
  needs.
* `getQsTile()` is never dereferenced blindly anymore. SystemUI is allowed to bind a tile service
  without handing out a tile; that used to throw and kill the whole app process, which made every
  tile of the app dead until it was opened again.
* `onClick` is final and wraps every tile action in a catch-all. A failing tile now shows a toast
  with the exception instead of silently taking the process down.
* `unlockAndRun` is only used when the device is actually locked. The keyguard callback is not
  reliable on every OEM build, and it is never invoked when no secure lock is set - tiles that
  relied on it did nothing at all.
* Tiles that start an activity use `startActivityAndCollapse(PendingIntent)` on Android 14+, which
  is the only variant that still gets the background-activity-start exemption.

### Volume panel tile

* **Fixed the tile that did nothing at all.** It called
  `adjustStreamVolume(STREAM_MUSIC, ADJUST_SAME, FLAG_SHOW_UI)`; since Android 13 the platform
  drops the UI request for apps that are not in the foreground, so the call succeeded without
  showing anything. The panel is now started as an activity instead.
* The tile shows the **short media volume slider** again (like a volume key press) through a
  transparent, instantly finishing activity, instead of the all-streams settings panel.
* **Miscellaneous → Expanded volume panel** brings back the big panel with every stream (media,
  ring, notification, alarm, call).
* **Miscellaneous → Keep Quick Settings open** asks the system for the slider straight from the tile
  service, so the Quick Settings panel is not collapsed first. Works only on ROMs that still honour
  background volume-UI requests.

### Diagnostics

* New **Settings → Troubleshooting → Tile diagnostics**: a ring buffer of the last 120 tile events
  (`onCreate`, `onBind`, `onClick`, `onStartListening`, `onStopListening`, `onDestroy`), newest
  first, with **Copy** and **Share**.
* The app records how its previous process ended (`ApplicationExitInfo`), whether it is in the
  foreground, and every uncaught exception with its first stack frames - so a background crash,
  which is invisible otherwise, can be read after the next start.
* A one line verdict on top of the log separates "the system never reached the app" from "the tile
  itself failed".
* **Settings → Troubleshooting → Why do my tiles stop working?**: battery, autostart, lock in the
  recent apps and Developer options (Don't keep activities / Background process limit), with
  shortcuts to the app info and battery pages.
* Tile callbacks are mirrored to logcat with the tag `QuickTiles`.

### Builds and releases

* CI builds a **signed release APK** instead of a debug APK.
* Every push to `master` publishes the APK as a GitHub release `v<versionName>`; other branches and
  pull requests still upload it as a run artifact. Tag pushes and manual runs can publish any tag.
* All builds share one cached keystore, so a new APK installs over the previous version without
  uninstalling.
* `compileSdk`/`targetSdk` 36, `minSdk` 24, versionCode 26.

## 1.9.0

* First attempt at making tiles work with the app closed: state handling, `getQsTile()` guards,
  permission dialogs instead of crashes, `ACTIVE_TILE` metadata removed.
* Debug APK workflow for GitHub Actions.

## Earlier

See the upstream project history: <https://github.com/flxholle/QuickTiles>.
