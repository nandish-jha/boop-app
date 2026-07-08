# Deploy Boop Web WITHOUT local Firebase login

Local `firebase login` often fails in IDEs, WSL, or with expired tokens. Use one of these instead.

---

## Easiest: Netlify Drop (~2 minutes)

**No account required for a trial URL.**

1. Download or locate this zip on your machine:  
   **`Boop 2.0/boop-web-netlify.zip`**

2. Open **https://app.netlify.com/drop** in your browser.

3. Drag **`boop-web-netlify.zip`** onto the page (or unzip and drag the `web` folder).

4. Netlify shows a live URL, e.g. `https://cheerful-cupcake-abc123.netlify.app`

5. **Required for Google sign-in** — add that domain in Firebase:  
   https://console.firebase.google.com/project/prodash-reminders/authentication/settings  
   → **Authorized domains** → **Add domain** → paste `cheerful-cupcake-abc123.netlify.app` (your exact hostname)

6. Share the Netlify link with anyone. Each user signs in with their own Google account.

---

## Best long-term URL: Firebase Hosting via Cloud Shell

Uses your browser Google login (no local CLI). You’re already signed into Firebase in the browser.

### Step 1 — Open Cloud Shell

1. Go to https://console.firebase.google.com/project/prodash-reminders  
2. Click the **`>_`** icon (top right) → **Open Cloud Shell**

### Step 2 — Upload the zip

1. In Cloud Shell, click the **⋮** menu → **Upload**  
2. Upload **`boop-web-firebase.zip`** from `Boop 2.0/` on your laptop

### Step 3 — Paste these commands in Cloud Shell

```bash
rm -rf ~/boop-hosting && mkdir -p ~/boop-hosting && cd ~/boop-hosting
unzip -o ~/boop-web-firebase.zip
# If unzip put files in web/ only, ensure firebase.json is here:
ls -la firebase.json web/

npm install -g firebase-tools
firebase login --no-localhost
# Follow the link it prints; authorize with the SAME Google account as Firebase.

firebase deploy --only hosting --project prodash-reminders
```

### Step 4 — Done

Public links (no extra domain setup needed for auth):

- **https://prodash-reminders.web.app**
- **https://prodash-reminders.firebaseapp.com**

---

## If Cloud Shell `firebase login` also fails

In Cloud Shell try:

```bash
firebase login --reauth --no-localhost
```

Or use **Netlify Drop** (first section) — it always works without CLI.

---

## After the site is live

1. User opens your link  
2. **Sign in with Google** (same account as Boop on phone)  
3. **Sync now**  

Each user only sees their own data (`boopUsers/{their uid}`).

---

## Recreate zip files (optional)

```bash
cd "/home/nandish-jha/Desktop/my_others/Vibe Coded Apps/Boop 2.0"
zip -r boop-web-netlify.zip web/index.html web/app.js web/styles.css
zip -r boop-web-firebase.zip firebase.json .firebaserc web/index.html web/app.js web/styles.css
```
