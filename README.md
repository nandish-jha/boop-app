# ProDash

A personal productivity dashboard — local-only PWA, also packaged as an Android APK. Tracks tasks, habits, budget, supplements, skincare, workouts, notes, and goals.

## Download the APK

- **Latest:** [releases/prodash-latest.apk](releases/prodash-latest.apk)
- **v1.0.1** (current): [releases/prodash-v1.0.1.apk](releases/prodash-v1.0.1.apk) — rebrand to ProDash, minimal themed icon, Material 3 nav
- **v1.0.0**: [releases/nandish-productivity-v1.0.0.apk](releases/nandish-productivity-v1.0.0.apk) — initial release

All versioned builds live under [`releases/`](releases/) and are also tagged on the [Releases page](../../releases).

### Install on Android
1. Download the `.apk` on your phone
2. Open it — allow "Install from unknown sources" if prompted
3. Launch **ProDash**

On Android 13+ with a themed icon wallpaper, the launcher icon adopts your system theme (Material You).

## Features
- Pure-black AMOLED Material 3 design, rounded bottom navigation with active pill indicator
- Home greeting with a time-of-day message and a daily rotating motivational quote
- **Tasks** — priority, type, due dates, recurrence
- **Habits** — quantified + checkbox, streaks + 90-day heatmap (9 habits pre-seeded)
- **Budget** — 15 accounts across Scotia/TD/Affinity/Wealthsimple/Loblaw/Loan, pie + bar charts, monthly savings goal
- **Supplements** — 14 items grouped morning/workout/night, with dosages
- **Skincare** — morning + night routines
- **Workouts** — 8 templates seeded from Notion (Hybrid 2026, John Abraham, CrossFit, Hrithik, etc.)
- **Notes** — tag-based filtering
- **Goals** — progress tracking
- **Data** — localStorage only; JSON export/import, CSV export of transactions
- **PWA** — installable in a browser, offline-capable

## Run the web version locally
```bash
python3 -m http.server 8000
# open http://localhost:8000
```
