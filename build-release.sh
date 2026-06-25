#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# build-release.sh  —  Build Boop APK locally and push a GitHub release
#
# Usage:
#   ./build-release.sh v4.9.23          # builds + uploads APK to new release
#   ./build-release.sh v4.9.23 --build-only   # builds APK, no upload
#
# Requirements (one-time setup already done):
#   ~/android-sdk   — Android SDK  (build-tools;36.0.0 + platforms;android-36)
#   ~/jdk-21        — Temurin JDK 21
#   GITHUB_TOKEN env var  OR  credentials stored via git credential helper
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
PWA_DIR="$REPO_ROOT/pwa-prototype"
ANDROID_DIR="$PWA_DIR/android"
APK_SRC="$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk"

TAG="${1:-}"
BUILD_ONLY="${2:-}"
GH_REPO="nandish-jha/boop-app"

if [[ -z "$TAG" ]]; then
  echo "Usage: $0 <tag>  e.g.  $0 v4.9.23"
  exit 1
fi

APK_NAME="boop-${TAG}.apk"

# ── Env setup ────────────────────────────────────────────────────────────────
export ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export JAVA_HOME="${JAVA_HOME:-$HOME/jdk-21}"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/build-tools/36.0.0:$PATH"

echo "▶ ANDROID_HOME=$ANDROID_HOME"
echo "▶ JAVA_HOME=$JAVA_HOME  ($(java -version 2>&1 | head -1))"

# ── 1. Build web app ──────────────────────────────────────────────────────────
echo ""
echo "── Step 1/4: Building React web app ──────────────────────────────────"
cd "$PWA_DIR"
npm ci --prefer-offline 2>/dev/null || npm install
npm run build

# ── 2. Copy assets into Android project ──────────────────────────────────────
echo ""
echo "── Step 2/4: Copying assets into Android project ─────────────────────"
rm -rf "$ANDROID_DIR/app/src/main/assets/public"
mkdir -p "$ANDROID_DIR/app/src/main/assets/public"
cp -r "$PWA_DIR/dist/." "$ANDROID_DIR/app/src/main/assets/public/"

cat > "$ANDROID_DIR/app/src/main/assets/capacitor.config.json" <<'JSON'
{"appId":"com.prodash.boop","appName":"Boop","webDir":"public","android":{}}
JSON

# Create capacitor-cordova-android-plugins stub (gitignored, must be recreated)
PLUGINS="$ANDROID_DIR/capacitor-cordova-android-plugins"
mkdir -p "$PLUGINS/src/main"

cat > "$PLUGINS/build.gradle" <<'GRADLE'
apply plugin: 'com.android.library'
android {
    namespace "cordova.plugins"
    compileSdkVersion rootProject.ext.compileSdkVersion
    defaultConfig {
        minSdkVersion rootProject.ext.minSdkVersion
        targetSdkVersion rootProject.ext.targetSdkVersion
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_21
        targetCompatibility JavaVersion.VERSION_21
    }
}
repositories { google(); mavenCentral() }
dependencies {}
GRADLE

echo 'ext { cordovaAndroidVersion = "14.0.1" }' > "$PLUGINS/cordova.variables.gradle"

cat > "$PLUGINS/src/main/AndroidManifest.xml" <<'XML'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
XML

# ── 3. Build APK ──────────────────────────────────────────────────────────────
echo ""
echo "── Step 3/4: Building APK with Gradle ────────────────────────────────"
cd "$ANDROID_DIR"
chmod +x gradlew
./gradlew assembleDebug --no-daemon

echo ""
echo "✓ APK built: $APK_SRC  ($(du -sh "$APK_SRC" | cut -f1))"

# Copy to repo root for convenience
cp "$APK_SRC" "$REPO_ROOT/$APK_NAME"
echo "✓ Copied to: $REPO_ROOT/$APK_NAME"

[[ "$BUILD_ONLY" == "--build-only" ]] && { echo "Done (build only)."; exit 0; }

# ── 4. Tag + GitHub release ───────────────────────────────────────────────────
echo ""
echo "── Step 4/4: Publishing GitHub release $TAG ──────────────────────────"
cd "$REPO_ROOT"

# Commit anything new and push
git add -A
git diff --cached --quiet || git commit -m "chore: bump to $TAG"
git push origin main

# Tag (delete local tag first if re-running)
git tag -d "$TAG" 2>/dev/null || true
git tag "$TAG"
git push origin "$TAG" --force

# Get GitHub token
if [[ -z "${GITHUB_TOKEN:-}" ]]; then
  GITHUB_TOKEN=$(printf "protocol=https\nhost=github.com\n" | git credential fill 2>/dev/null | grep ^password | cut -d= -f2 || true)
fi

if [[ -z "$GITHUB_TOKEN" ]]; then
  echo ""
  echo "⚠  No GITHUB_TOKEN found. APK is at $REPO_ROOT/$APK_NAME"
  echo "   Upload it manually at: https://github.com/$GH_REPO/releases/new?tag=$TAG"
  exit 0
fi

# Delete existing release for this tag if it exists
EXISTING=$(curl -s -H "Authorization: token $GITHUB_TOKEN" \
  "https://api.github.com/repos/$GH_REPO/releases/tags/$TAG" | python3 -c "
import sys,json; d=json.load(sys.stdin); print(d.get('id',''))
" 2>/dev/null || true)

if [[ -n "$EXISTING" && "$EXISTING" != "None" ]]; then
  curl -s -X DELETE -H "Authorization: token $GITHUB_TOKEN" \
    "https://api.github.com/repos/$GH_REPO/releases/$EXISTING"
fi

# Create release
RELEASE_BODY="## Boop $TAG\n\n**Install on Android:**\n1. Download \`$APK_NAME\` below\n2. Settings → Apps → Install unknown apps → allow Files / Browser\n3. Open the APK and tap Install\n\n> Debug build — safe to install, Android will show an unverified warning."

RELEASE_ID=$(curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Content-Type: application/json" \
  "https://api.github.com/repos/$GH_REPO/releases" \
  -d "$(python3 -c "
import json, sys
print(json.dumps({'tag_name':'$TAG','name':'Boop $TAG','body':'$RELEASE_BODY','draft':False,'prerelease':False,'make_latest':True}))
")" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

# Upload APK
curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Content-Type: application/vnd.android.package-archive" \
  --data-binary @"$REPO_ROOT/$APK_NAME" \
  "https://uploads.github.com/repos/$GH_REPO/releases/$RELEASE_ID/assets?name=$APK_NAME" \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print('✓ Uploaded:', d.get('browser_download_url','?'))"

echo ""
echo "✅ Release published: https://github.com/$GH_REPO/releases/tag/$TAG"
