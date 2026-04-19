# Nandish Productivity

Local-only PWA (also packaged as an Android APK) for tracking tasks, habits, budget, supplements, skincare, workouts, notes, and goals.

## Download the APK

**Latest:** [releases/nandish-productivity-latest.apk](releases/nandish-productivity-latest.apk)

Versioned releases live under [`releases/`](releases/) and are also tagged on the [Releases page](../../releases).

### Install on Android
1. Download the `.apk` on your phone
2. Open it — allow "Install from unknown sources" if prompted
3. Launch "Productivity"

## Features
- Pure-black AMOLED Material 3 design, bottom navigation
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
