# ProDash

A personal productivity dashboard — local-only PWA, also packaged as an Android APK. Tracks tasks, habits, budget, supplements, skincare, workouts, notes, and goals.

## Download the APK

- **Latest:** [releases/prodash-latest.apk](releases/prodash-latest.apk)
- **v1.0.13** (current): [releases/prodash-v1.0.13.apk](releases/prodash-v1.0.13.apk) — full Obsidian "Silent Order" redesign pass across Home, Tasks, Finance, and Settings
- **v1.0.12**: [releases/prodash-v1.0.12.apk](releases/prodash-v1.0.12.apk) — Google Drive cloud sync (beta) + obsidian dark theme refinement
- **v1.0.11**: [releases/prodash-v1.0.11.apk](releases/prodash-v1.0.11.apk) — Zenith-inspired visual system + persistent light/dark/system theme modes
- **v1.0.10**: [releases/prodash-v1.0.10.apk](releases/prodash-v1.0.10.apk) — full flat redesign (no glass UI) with improved information hierarchy and auto-sync Android web assets
- **v1.0.9**: [releases/prodash-v1.0.9.apk](releases/prodash-v1.0.9.apk) — migration build with install-safe app ID + aesthetic refresh and backup restore flow
- **v1.0.8**: [releases/prodash-v1.0.8.apk](releases/prodash-v1.0.8.apk) — aesthetic refresh + safe update backup flow (export before install, import if needed)
- **v1.0.6**: [releases/prodash-v1.0.6.apk](releases/prodash-v1.0.6.apk) — lightweight soft-card UI (removed heavy glass blurs for smooth scrolling)
- **v1.0.5**: [releases/prodash-v1.0.5.apk](releases/prodash-v1.0.5.apk) — native Android notification bridge, per-task reminders with date+time, SVG edit/close icons
- **v1.0.4**: [releases/prodash-v1.0.4.apk](releases/prodash-v1.0.4.apk) — full CRUD on habits, supplements, skincare, workouts, accounts, categories + iOS liquid-glass UI
- **v1.0.3**: [releases/prodash-v1.0.3.apk](releases/prodash-v1.0.3.apk) — habit input overhaul (sleep time-range average, water in mL) + Cashew CSV import
- **v1.0.2**: [releases/prodash-v1.0.2.apk](releases/prodash-v1.0.2.apk) — greeting + quote in the top bar
- **v1.0.1**: [releases/prodash-v1.0.1.apk](releases/prodash-v1.0.1.apk) — ProDash rebrand, minimal themed icon, Material 3 nav
- **v1.0.0**: [releases/nandish-productivity-v1.0.0.apk](releases/nandish-productivity-v1.0.0.apk) — initial release

All versioned builds live under [`releases/`](releases/) and are also tagged on the [Releases page](../../releases).

### Install on Android
1. Download the `.apk` on your phone
2. Open it — allow "Install from unknown sources" if prompted
3. Launch **ProDash**

On Android 13+ with a themed icon wallpaper, the launcher icon adopts your system theme (Material You).

## Features
- Premium minimal dark-indigo design with softer contrast and rounded bottom navigation
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
