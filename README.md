# 📱 Teleflix Streamer - Standalone Android Application

A powerful standalone Android application for streaming media from Telegram channels using Cinemeta metadata and TDLib byte-range streaming proxy engine.

## 🚀 Features

- **Cinemeta Catalogue**: Browse popular movies, top TV shows, and trending titles.
- **Real-Time Search**: Instant multi-search across Cinemeta & Telegram channels.
- **Built-in Player**: ExoPlayer video streaming with full controls.
- **External Players**: One-click launch in **VLC Player** (`org.videolan.vlc`) or **MPV Player** (`is.xyz.mpv`).
- **TDLib Native Engine**: High-performance HTTP byte-range proxy for direct streaming, split files (`.001`), and ZIP archives.

---

## 🛠️ Building the APK

### GitHub Actions (Automatic Cloud Build)
This repository includes a pre-configured GitHub Actions workflow in `.github/workflows/build-apk.yml`.

1. Push this project to GitHub.
2. Go to **Actions** -> **Build Standalone Teleflix APK**.
3. Download the generated `app-debug.apk` artifact!

### Local Build (Linux / macOS / Windows)
```bash
chmod +x gradlew
./gradlew :app:assembleDebug
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
