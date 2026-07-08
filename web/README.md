# Boop Web — public sync app

Anyone can open the site, sign in with **their own** Google account, and see their Boop data (same as the Android app).

## Public link (after deploy)

- **https://prodash-reminders.web.app**
- **https://prodash-reminders.firebaseapp.com**

## Deploy / update the site

One-time login:

```bash
npm install -g firebase-tools
firebase login
```

Deploy from Boop 2.0:

```bash
cd "/home/nandish-jha/Desktop/my_others/Vibe Coded Apps/Boop 2.0"
npm run deploy:web
```

Share either URL above with anyone who uses Boop.

## Local development

```bash
npm run web
```

Open http://localhost:5173

## How it works for multiple users

| User | What they see |
|------|----------------|
| Alice (Google account A) | Only `boopUsers/{Alice's uid}` |
| Bob (Google account B) | Only `boopUsers/{Bob's uid}` |

Firestore rules enforce this — users cannot read each other's data.

## Firebase requirements (project owner, one-time)

1. **Authentication → Google** enabled  
2. **Firestore rules** published for `boopUsers/{userId}`  
   (see `boop-existing/firebase/DEPLOY.md`)  
3. **Web app** registered in Project settings (for sign-in)  
4. Hosting deploy completes successfully  

`prodash-reminders.web.app` is automatically allowed for Google sign-in.

## Custom domain (optional)

Firebase Console → Hosting → Add custom domain (e.g. `boop.yourdomain.com`).
