# Silent Order (ProDash)

Native **Android** productivity app — **not** a PWA. UI follows the bundled **Silent Order / Obsidian** design references (`design-references/`): monochrome panels, editorial type, five-tab shell (**Home · Hub · Goals · Vault · Logs**).

## Download the APK

- **Latest:** [releases/prodash-latest.apk](releases/prodash-latest.apk)
- **v2.0.1** (current): native Kotlin + Material 3 rewrite; local data in `SharedPreferences` (JSON via Gson). Previous WebView / PWA stack removed.

Older versioned APKs remain under [`releases/`](releases/) for history.

## Tech stack

- **Kotlin**, **Material 3**, **Navigation**, **View Binding**, **RecyclerView**
- **Gson** persistence: `SharedPreferences` key `silent_order_state`
- **Java** `AndroidNotifier` kept for future notification hooks

## Run / build

1. Open `android/` in Android Studio (JDK 17, Android SDK 34), or from CLI:
   ```bash
   cd android && gradle assembleRelease
   ```
2. Release APK: `android/app/build/outputs/apk/release/app-release.apk`

## Data note

v2.x does **not** read old WebView `localStorage` data. If you still have a JSON export from the legacy web app, say so and a one-time importer can be added.

## Repository root `index.html`

Placeholder only: explains that the product is the **Android app**, not an installable web app.

## Design references

Screen HTML + PNG references live in `design-references/stitch_comprehensive_life_dashboard/`.
