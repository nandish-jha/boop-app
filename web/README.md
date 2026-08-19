# Boop Web — full productivity dashboard

Use Boop on your laptop with the same tasks, notes, habits, and wallet as the Android app.

## Public link (after deploy)

- **https://prodash-reminders.web.app**
- **https://prodash-reminders.firebaseapp.com**

## Sync with your phone

1. On Android: **Settings → Link Google account** (same Google account you'll use on web)
2. Tap **Sync now** on the phone
3. On web: sign in with Google, then tap **Sync**

Or use **Import backup** / **Export backup** in web Settings (profile icon) to move data manually.

## Local development

Static files — no build step. From repo root:

```bash
npm run web
```

Or serve the `web/` folder with any static server on port 5173.

Open http://localhost:5173

## Features

| Module | Web support |
|--------|-------------|
| Tasks / Reminders | Create, edit, complete, archive, delete, subtasks, repeat |
| Notes | Create, edit, delete, tags |
| Habits | Check-in, quantity +/- , week dots, create/edit |
| Wallet | Accounts, income, expense, transfer |
| Calendar | Month grid + week strip of task due dates |
| Home dashboard | Stats, search, filters, unified cards |
| Layout | Fluid: 1–4 card columns, bottom nav on phones, sidebar on desktop |

Not on web (Android-only): device calendar, voice assistant, widgets, notifications, image attachments.

## Firebase requirements (project owner, one-time)

1. **Authentication → Google** enabled  
2. **Authentication → Anonymous** enabled (Android default)  
3. **Firestore rules** for `boopUsers/{userId}`  
4. Hosting deploy completes successfully  

## Deploy

```bash
npm run deploy:web
```
