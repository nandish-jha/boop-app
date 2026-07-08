# Deploy Boop Web (public link)

## Option A — Firebase Hosting (recommended)

Public URLs after deploy:
- https://prodash-reminders.web.app
- https://prodash-reminders.firebaseapp.com

### If login failed before (“credentials no longer valid”)

Run these **in order** in your terminal (not in Cursor’s sandbox):

```bash
cd "/home/nandish-jha/Desktop/my_others/Vibe Coded Apps/Boop 2.0"

# 1. Clear broken login
firebase logout

# 2. Sign in again — use the Google account that owns prodash-reminders
firebase login --reauth
```

When the browser opens, pick the **same Google account** you use for [Firebase Console](https://console.firebase.google.com/project/prodash-reminders).

If browser redirect fails, use the **device code** link Firebase prints and paste the code when asked.

```bash
# 3. Deploy
./scripts/deploy-web.sh
# or: npm run deploy:web
```

Fresh-login one-liner:

```bash
./scripts/deploy-web.sh --fresh-login
```

---

## Option B — Netlify Drop (no Firebase CLI)

If Firebase login keeps failing:

1. Open https://app.netlify.com/drop  
2. Drag the folder **`Boop 2.0/web`** onto the page  
3. Netlify gives you a URL like `https://something-random.netlify.app`  
4. In [Firebase Console → Authentication → Settings → Authorized domains](https://console.firebase.google.com/project/prodash-reminders/authentication/settings)  
   click **Add domain** and add your Netlify hostname (e.g. `something-random.netlify.app`)  
5. Share that Netlify link

Google sign-in only works on domains listed in Firebase Authorized domains.

---

## After deploy — for all users

- Anyone opens the link  
- **Sign in with Google** (their own account)  
- **Sync now** — data comes from `boopUsers/{their uid}`  
- Firestore rules keep each user’s data private  

---

## Still stuck?

- Confirm you’re owner/editor on project **prodash-reminders**  
- Try `firebase login --reauth` in a normal terminal (outside IDE)  
- Try Chrome incognito when authorizing  
- Update CLI: `npm install -g firebase-tools@latest`
