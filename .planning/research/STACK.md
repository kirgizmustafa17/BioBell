# BioBell — Stack Research

## Language & Runtime

| Library | Version | Rationale | Confidence |
|---------|---------|-----------|------------|
| **Kotlin** | 1.9.x / 2.0.x | Only language for modern Android; null-safety critical for alarm scheduling | ⬛⬛⬛⬛⬛ Very High |
| **Kotlin Coroutines** | 1.8.x | Async alarm scheduling, Room queries, backup flows | ⬛⬛⬛⬛⬛ Very High |
| **Kotlin Flow** | (bundled) | Reactive UI updates for alarm state | ⬛⬛⬛⬛⬛ Very High |

## UI Framework

| Library | Version | Rationale | Confidence |
|---------|---------|-----------|------------|
| **Jetpack Compose** | 1.6.x / BOM 2024.x | Modern declarative UI; Material You native support; no XML | ⬛⬛⬛⬛⬛ Very High |
| **Material Design 3 (M3)** | `compose-material3 1.2.x` | Material You spec; dynamic color theming; consistent component library | ⬛⬛⬛⬛⬛ Very High |
| **Accompanist** | 0.34.x | System bars, permissions, pager (where Compose doesn't cover natively) | ⬛⬛⬛⬜⬜ High |

## Architecture

| Component | Choice | Rationale |
|-----------|--------|-----------|
| **Architecture pattern** | MVVM + Clean Architecture | ViewModels hold UI state via StateFlow; domain layer for sleep math logic; testable in isolation |
| **Navigation** | Compose Navigation | Single-activity; type-safe routes; works naturally with Compose |
| **DI** | Hilt (Dagger) | Standard Android DI; lifecycle-aware scoping; reduces boilerplate |
| **State management** | `StateFlow` + `collectAsStateWithLifecycle` | Lifecycle-safe Compose state observation |

## Data Layer

| Library | Version | Rationale | Confidence |
|---------|---------|-----------|------------|
| **Room** | 2.6.x | Local alarm persistence; type-safe queries; Kotlin coroutines support | ⬛⬛⬛⬛⬛ Very High |
| **DataStore (Preferences)** | 1.0.x | User settings (chronotype, preferences); replaces SharedPreferences; coroutine-native | ⬛⬛⬛⬛⬛ Very High |
| **Proto DataStore** | 1.0.x | Typed settings model (if complexity warrants); optional | ⬛⬛⬛⬜⬜ Medium |

## Alarm Scheduling

| API | Use Case | Confidence |
|-----|----------|------------|
| **`AlarmManager.setAlarmClock()`** | Primary wake-up alarm — most reliable; system exits Doze; shows status bar icon | ⬛⬛⬛⬛⬛ Very High |
| **`AlarmManager.setExactAndAllowWhileIdle()`** | Secondary/reminder alarms where clock icon isn't needed | ⬛⬛⬛⬛⬜ High |
| **`BroadcastReceiver` (BOOT_COMPLETED)** | Reschedule all alarms on device reboot | ⬛⬛⬛⬛⬛ Very High |
| **`ForegroundService`** | Play alarm audio/vibrate after receiver fires; prevents system kill | ⬛⬛⬛⬛⬛ Very High |
| **`USE_EXACT_ALARM` permission** | Alarm-clock apps are eligible; granted on install; avoid `SCHEDULE_EXACT_ALARM` issues | ⬛⬛⬛⬛⬛ Very High |

## Permissions Required

```xml
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />  <!-- API 33+ -->
<uses-permission android:name="android.permission.INTERNET" />  <!-- for Google backup -->
```

## Backup

| Approach | Details | Rationale |
|----------|---------|-----------|
| **Android Auto Backup** | Enabled by default (API 23+); backs up Room DB + DataStore to Google Drive | Zero-infrastructure; opt-in for user; up to 25 MB free |
| **Custom backup rules XML** | Exclude transient/large data; include only alarm entities and settings | Prevents unnecessary data from being backed up |
| **`android:allowBackup="true"`** | Manifest flag | Required to enable Auto Backup |

## Testing

| Library | Use |
|---------|-----|
| **JUnit 5** | Unit tests for sleep math / chronotype logic |
| **MockK** | Kotlin-idiomatic mocking |
| **Turbine** | Testing Flow emissions |
| **Compose UI Testing** | Instrumentation tests for alarm-setting UI |
| **Robolectric** | Fast local unit tests with Android context |

## Build

| Tool | Version | Notes |
|------|---------|-------|
| **Gradle (KTS)** | 8.x | Kotlin build scripts |
| **Android Gradle Plugin** | 8.3.x+ | Required for API 34 targets |
| **min SDK** | 31 (Android 12) | Material You dynamic color requires API 31 |
| **target SDK** | 34 (Android 14) | Latest stable; required for Play Store submissions |
| **compile SDK** | 34 | |

## What NOT to Use

- ❌ **React Native / Flutter** — App is Android-only; native required for AlarmManager reliability and Material You
- ❌ **WorkManager for alarms** — WorkManager is not guaranteed to fire at exact times; AlarmManager only
- ❌ `setRepeating()` — Deprecated for exact alarms; always schedule single-fire and reschedule on trigger
- ❌ **SharedPreferences** — Use DataStore instead; SharedPreferences has no coroutine support and known ANR risk
- ❌ **Permanent ForegroundService** — Google Play may reject apps with always-on services; use only during active alarm firing
- ❌ **SCHEDULE_EXACT_ALARM** — Use `USE_EXACT_ALARM` instead for alarm-clock category apps (auto-granted, no user permission dialog needed)

---
*Research date: 2026-03-17*
