# AI Account Manager

A production-ready Android application for managing multiple AI assistant accounts (Claude and Gemini) via WebView. Built with Kotlin and Jetpack Compose.

## Features

- **Multi-account management** — add, activate, and delete AI accounts
- **Platform support** — Claude (orange accent) and Gemini (blue accent)
- **Full WebView integration** — JavaScript, DOM storage, cookies, session isolation
- **Session switching** — automatically clears cookies when switching accounts
- **Offline error handling** — friendly retry UI on connection failure
- **Persistent storage** — accounts saved with DataStore Preferences + JSON serialization
- **Dark mode** — follows system theme

## Screenshots

| Account List | WebView |
|---|---|
| Cards with platform badge, status, and message count | Full WebView with progress bar and back button |

## Architecture

```
com.aiaccounts.manager/
├── MainActivity.kt          — App entry point, screen router
├── MainViewModel.kt         — ViewModel with StateFlow
├── model/
│   └── Account.kt           — Data class + Platform enum
├── data/
│   └── AccountRepository.kt — DataStore persistence layer
├── navigation/
│   └── Screen.kt            — Sealed class: AccountList | Web(accountId)
├── ui/
│   ├── AccountListScreen.kt — Account cards, add dialog
│   ├── WebViewScreen.kt     — WebView with progress + error handling
│   └── theme/
│       └── Theme.kt         — Material3 light/dark color schemes
└── webview/
    └── WebViewManager.kt    — WebView configuration (JS, cookies, UA)
```

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| State | ViewModel + StateFlow |
| Storage | DataStore Preferences |
| Serialization | kotlinx.serialization |
| WebView | Android WebView (no wrappers) |
| Navigation | Custom sealed class (no library) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

**No Hilt, Room, Navigation Component, or Retrofit.**

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/ai-account-manager.git
   cd ai-account-manager
   ```

2. Open in Android Studio or build from command line:
   ```bash
   chmod +x gradlew
   ./gradlew assembleDebug
   ```

3. Install on device/emulator:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### CI/CD

GitHub Actions workflow (`.github/workflows/android.yml`) automatically:
- Triggers on push to `main`/`master`/`develop` and pull requests
- Builds debug APK on `ubuntu-latest` with JDK 17
- Uploads the APK as a workflow artifact (retained 30 days)

## Usage

1. **Add account** — tap the **+** button, enter a name, select platform (Claude/Gemini), and provide the URL
2. **Open** — tap **Open** to launch the account in WebView
3. **Activate** — tap **Activate** to mark an account as active (only one at a time)
4. **Switch account** — opening a different account clears cookies and loads the new URL
5. **Delete** — tap the trash icon to remove an account

## WebView Configuration

- JavaScript enabled
- DOM storage enabled
- Third-party cookies accepted
- `setSupportMultipleWindows(true)` + `javaScriptCanOpenWindowsAutomatically = true`
- Mobile Chrome User-Agent
- Redirect and error handling with offline fallback
- Cookie & cache cleared on account switch

## License

MIT
