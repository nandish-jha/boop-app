# Silent Order (ProDash)

Native **Android** app (not a PWA). The five main tabs (**Home · Hub · Goals · Vault · Logs**) render **bundled Stitch HTML** in a **WebView**, with **Kotlin** bridges for persistence, editors, **Google Drive** sync, and navigation.

## Download the APK

- **Latest:** [releases/prodash-latest.apk](releases/prodash-latest.apk) (same binary as [releases/prodash-v3.4.7.apk](releases/prodash-v3.4.7.apk))
- **GitHub release:** [v3.4.7](https://github.com/nandish-jha/prodash-android-app/releases/tag/v3.4.7) (APK attached; **fix instant crash on Android 6.0–7.1** by removing `java.time` from cold start; safer alarm scheduling)
- **v3.4.x:** **Google Drive** backup/restore (app data folder) from the menu; **auto-upload soon after each save** when signed in (Wi‑Fi or mobile data); if that fails (e.g. offline), **upload when you leave the app** is retried. **Uncaught crashes** write a text report on the device; **Menu → Diagnostics (crash report)** opens it anytime (no automatic popup on cold start). Gson `SharedPreferences`, **daily reminder**, **settings** (Obsidian appearance, haptics, reminder time).

Older APKs remain under [`releases/`](releases/) for history.

## Tech stack

- **Kotlin**, **Material 3**, **Navigation**, **View Binding**, **WebView** + **JavaScript** hydration (`android/app/src/main/assets/vendor/`)
- **Gson** persistence in **SharedPreferences** (`prodash_state`, key `app_state_json`)
- **Google Sign-In** + **Drive API** (`drive.appdata`): single JSON file `prodash_cloud_state.json` in the hidden app-data folder (not shown in the user’s normal Drive file list)
- **Coroutines** + **ProcessLifecycleOwner**: upload shortly after each local change (debounced), plus on-background retry if a sync was still pending
- **ItemEditors** (Material dialogs) for CRUD on tasks, notes, goals, habits, supplements, accounts, transactions, and daily reminder time

## Run / build

1. Open `android/` in Android Studio (JDK 17, Android SDK 34), or from CLI:
   ```bash
   cd android && gradle assembleRelease
   ```
2. Release APK: `android/app/build/outputs/apk/release/app-release.apk`

## Data & Google Drive

Primary copy of state stays **on device**. Use **Menu → Google Drive…** to **Back up now** (upload JSON) or **Restore from Drive** (replace local state). After you **sign in with Google**, each time you **save a change** the app **uploads to Drive a moment later** (so rapid taps become one upload), over **Wi‑Fi or mobile data**. If the upload could not finish (no network, etc.), it **tries again when you leave the app** with anything still pending.

If the app **stops** (crashes): install **v3.4.7+** (earlier builds could **exit immediately on Android 6.0–7.1** because `java.time` was used at startup without API 26+). Then use **Menu → Diagnostics (crash report)** to **Share** or **Copy** the saved stack trace (only after the UI stays open long enough to open the menu). That report covers **Java/Kotlin** crashes; some **native WebView** faults leave no file—then capture **`adb logcat`** right after reproducing. Non-fatal init issues may append to **`prodash_startup_error.log`** in the app’s private files directory.

The file lives in Drive’s **app data** scope for this app; you must use the same Google account and the same installed app (package + signing) to read it.

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
