# BioBell — Project Instructions for AI Agents

## Project Overview

BioBell is a **native Android alarm app** with biology-aware sleep scheduling.

| | |
|--|--|
| **App ID** | `com.biobell.android` |
| **Min SDK** | 31 (Android 12) — required for Material You dynamic color |
| **Target SDK** | 34 (Android 14) |
| **Language** | Kotlin 2.0 |
| **UI** | Jetpack Compose + Material Design 3 |
| **DI** | Hilt |
| **State** | MVVM + StateFlow + collectAsStateWithLifecycle |

## Package Structure

```
com.biobell.android/
├── ui/
│   ├── theme/           # Color.kt, Type.kt, Shape.kt, Theme.kt
│   ├── navigation/      # Screen.kt, BioBellNavGraph.kt, BottomNavBar.kt
│   ├── alarm/           # AlarmListScreen, AlarmSetterScreen, ViewModels
│   ├── settings/        # SettingsScreen, SettingsViewModel
│   └── components/      # Shared reusable composables
├── domain/
│   ├── model/           # Alarm, SleepPlan, Chronotype, HealthScore, SleepWarning
│   ├── engine/          # SleepMathEngine (pure Kotlin — zero Android deps)
│   └── repository/      # AlarmRepository, SettingsRepository (interfaces only)
├── data/
│   ├── room/            # AlarmDatabase, AlarmDao, AlarmEntity
│   ├── datastore/       # UserPreferencesDataStore
│   └── repository/      # AlarmRepositoryImpl, SettingsRepositoryImpl
├── alarm/               # AlarmSchedulerImpl, AlarmReceiver, AlarmForegroundService, BootReceiver
└── di/                  # AppModule, DatabaseModule, RepositoryModule, AlarmModule
```

## Architecture Rules

- **Repository pattern**: Interfaces live in `domain/repository/`; implementations in `data/repository/`
- **ViewModels**: Always `@HiltViewModel` + `hiltViewModel()` in composables
- **DI modules**: One module per concern (`DatabaseModule`, `RepositoryModule`, `AlarmModule`)
- **State in Compose**: Always use `collectAsStateWithLifecycle()`, never `collectAsState()`

## 🔴 Critical Alarm Reliability Rules (DO NOT SKIP)

1. **ALWAYS use `AlarmManager.setAlarmClock()`** for user-facing alarms — NEVER `setExact()` alone
   - `setAlarmClock()` is exempt from Doze mode and shows in system clock UI
   - `setExact()` can be deferred in Doze mode → silent missed alarms
2. **ALWAYS use `FLAG_IMMUTABLE`** on all `PendingIntent` creations
3. **ALWAYS reschedule in `BootReceiver`** — `AlarmManager` does not survive device reboots
4. **Use `USE_EXACT_ALARM` permission**, NOT `SCHEDULE_EXACT_ALARM`
   - `USE_EXACT_ALARM` is auto-granted for alarm clock apps (no user dialog)
   - `SCHEDULE_EXACT_ALARM` requires a user permission dialog on Android 12+

## 🧠 Sleep Math Constants

| Constant | Value |
|----------|-------|
| Sleep onset offset | **15 minutes** (always subtract from sleep duration) |
| Sleep cycle duration | **90 minutes** |
| Minimum healthy sleep | **6 hours** (4 cycles) |
| Recommended sleep | **7h 30m** (5 cycles) |
| Chronotype offsets | 🐓 Early Bird = −30 min · 🐦 Intermediate = 0 · 🦉 Night Owl = +30 min |

**Always use `LocalDateTime` for alarm math** (not `LocalTime` — `LocalTime` subtraction breaks across midnight!)

## Design System

| Token | Value |
|-------|-------|
| Seed color | `#5B4FCF` (deep indigo — sleep/night-sky) |
| Dark surfaces | `#0F0E17` background · `#1A1826` surface |
| Dynamic color | Enabled API 31+ via `dynamicDarkColorScheme` / `dynamicLightColorScheme` |
| Default theme | **Dark** (sleep app context) |
| Font | Inter (Google Fonts downloadable font) |
| Shape scale | extraSmall=4dp · small=8dp · medium=12dp · large=16dp · extraLarge=28dp |

**Never hardcode colors** — always use `MaterialTheme.colorScheme.*` semantic roles.

## Code Conventions

- **Conventional commits**: `feat(scope): message`, `fix(scope): message`, `docs(scope): message`
- **Commit granularity**: One commit per logical task — never batch unrelated changes
- **No hardcoded strings**: All user-visible text in `strings.xml`
- **No `TODO` comments in committed code**: Either implement it or leave a GitHub issue reference
- **Compose previews**: Add `@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)` to all screens

## Planning Files

Planning docs live in `.planning/` and are tracked in git. Never modify planning files during implementation — only update `STATE.md` position markers.

## What's Been Implemented (Phase 1 complete)

- ✅ Gradle KTS + Version Catalog (`gradle/libs.versions.toml`)
- ✅ AndroidManifest with all permissions (`USE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, etc.)
- ✅ `BioBellTheme` with dynamic color + indigo fallback, dark/light modes
- ✅ `BioBellApplication` with `@HiltAndroidApp`
- ✅ `MainActivity` with `@AndroidEntryPoint` + edge-to-edge
- ✅ Navigation skeleton: `Screen`, `BioBellNavGraph`, `BioBellBottomBar`
- ✅ Placeholder screens: `AlarmListScreen`, `AlarmSetterScreen`, `SettingsScreen`
- ✅ Auto Backup configuration: `backup_rules.xml`, `data_extraction_rules.xml`
