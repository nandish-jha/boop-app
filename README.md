# Silent Order (ProDash)

Native **Android** app (not a PWA). The five main tabs (**Home · Hub · Goals · Vault · Logs**) render **bundled Stitch HTML** in a **WebView**, with **Kotlin** bridges for persistence, editors, backup, and navigation.

## Download the APK

- **Latest:** [releases/prodash-latest.apk](releases/prodash-latest.apk) (same binary as [releases/prodash-v3.3.0.apk](releases/prodash-v3.3.0.apk))
- **v3.3.x:** WebView UI + Gson `SharedPreferences` (`prodash_state` / `app_state_json`), **export/import JSON backup**, **daily reminder** (`AlarmManager` + notification channel), **settings** (Obsidian mode toggles deep vs calmer dark in the WebView, haptics, reminder time) persisted in app state.

Older APKs remain under [`releases/`](releases/) for history.

## Tech stack

- **Kotlin**, **Material 3**, **Navigation**, **View Binding**, **WebView** + **JavaScript** hydration (`android/app/src/main/assets/vendor/`)
- **Gson** persistence in **SharedPreferences** (`prodash_state`, key `app_state_json`)
- **ItemEditors** (Material dialogs) for CRUD on tasks, notes, goals, habits, supplements, accounts, transactions, and daily reminder time

## Run / build

1. Open `android/` in Android Studio (JDK 17, Android SDK 34), or from CLI:
   ```bash
   cd android && ./gradlew assembleRelease
   ```
2. Release APK: `android/app/build/outputs/apk/release/app-release.apk`

## Data

State is **device-local**. Use **Menu → Export backup… / Import backup…** to move JSON between installs. On **Android 13+**, the app requests **notification permission** so daily reminders can post.

## Repository root `index.html`

Placeholder: the shipping product is the **Android app**, not an installable web client.

## UI assets

Screen HTML and references live under `android/app/src/main/assets/stitch/` (e.g. `home_dashboard_dark`, `settings_dark`).
