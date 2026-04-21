# Silent Order (ProDash)

Native **Android** app (not a PWA). The five main tabs (**Home · Hub · Goals · Vault · Logs**) render **bundled Stitch HTML** in a **WebView**, with **Kotlin** bridges for persistence, editors, **Google Drive** sync, and navigation.

## Download the APK

- **Latest:** [releases/prodash-latest.apk](releases/prodash-latest.apk) (same binary as [releases/prodash-v3.3.0.apk](releases/prodash-v3.3.0.apk))
- **GitHub release:** [v3.3.0](https://github.com/nandish-jha/prodash-android-app/releases/tag/v3.3.0) (APK attached)
- **v3.4.x:** Adds **Google Drive** backup/restore (app data folder) via the in-app menu; local file export/import removed. Still includes Gson `SharedPreferences`, **daily reminder**, and **settings** (Obsidian appearance, haptics, reminder time).

Older APKs remain under [`releases/`](releases/) for history.

## Tech stack

- **Kotlin**, **Material 3**, **Navigation**, **View Binding**, **WebView** + **JavaScript** hydration (`android/app/src/main/assets/vendor/`)
- **Gson** persistence in **SharedPreferences** (`prodash_state`, key `app_state_json`)
- **Google Sign-In** + **Drive API** (`drive.appdata`): single JSON file `prodash_cloud_state.json` in the hidden app-data folder (not shown in the user’s normal Drive file list)
- **ItemEditors** (Material dialogs) for CRUD on tasks, notes, goals, habits, supplements, accounts, transactions, and daily reminder time

## Run / build

1. Open `android/` in Android Studio (JDK 17, Android SDK 34), or from CLI:
   ```bash
   cd android && gradle assembleRelease
   ```
2. Release APK: `android/app/build/outputs/apk/release/app-release.apk`

## Data & Google Drive

Primary copy of state stays **on device**. Use **Menu → Google Drive…** to **Back up now** (upload JSON) or **Restore from Drive** (replace local state). The file lives in Drive’s **app data** scope for this app; you must use the same Google account and the same installed app (package + signing) to read it.

### Google Cloud Console (for your own builds)

1. Create or pick a Google Cloud project; enable **Google Drive API**.
2. Configure the **OAuth consent screen** (test users while in testing).
3. **Credentials → Create credentials → OAuth client ID → Android** for each package you ship:
   - Release: `com.nandish.productivity.v2` with the **SHA-1** of your **release** keystore.
   - Debug: `com.nandish.productivity.v2.debug` with the **SHA-1** of your **debug** keystore (`./gradlew signingReport`).

Without matching OAuth clients, Google sign-in or Drive calls will fail at runtime.

On **Android 13+**, the app requests **notification permission** so daily reminders can post.

## Repository root `index.html`

Placeholder: the shipping product is the **Android app**, not an installable web client.

## UI assets

Screen HTML and references live under `android/app/src/main/assets/stitch/` (e.g. `home_dashboard_dark`, `settings_dark`).
