# BioBell — Architecture Research

## Component Map

```
┌─────────────────────────────────────────────────────────────┐
│                        UI LAYER                             │
│  Compose Screens                                            │
│  ┌──────────────────┐  ┌───────────────┐  ┌─────────────┐  │
│  │ AlarmSetterScreen│  │ AlarmListScreen│  │ SettingsScreen│ │
│  └────────┬─────────┘  └───────┬───────┘  └──────┬──────┘  │
│           │                   │                  │          │
│  ┌────────▼─────────┐  ┌───────▼───────┐  ┌──────▼──────┐  │
│  │ AlarmSetterVM    │  │ AlarmListVM   │  │ SettingsVM  │  │
│  └────────┬─────────┘  └───────┬───────┘  └──────┬──────┘  │
└───────────┼───────────────────┼─────────────────┼──────────┘
            │                   │                  │
┌───────────▼───────────────────▼─────────────────▼──────────┐
│                       DOMAIN LAYER                          │
│  ┌──────────────────────────┐  ┌──────────────────────────┐ │
│  │   SleepMathEngine        │  │   AlarmRepository        │ │
│  │  - calculateBedtimes()   │  │  (interface)             │ │
│  │  - validateSleepPlan()   │  └──────────────────────────┘ │
│  │  - scoreHealthBadge()    │                               │
│  │  - applyChronotype()     │  ┌──────────────────────────┐ │
│  └──────────────────────────┘  │   SettingsRepository     │ │
│                                │  (interface)             │ │
│  ┌──────────────────────────┐  └──────────────────────────┘ │
│  │   AlarmScheduler         │                               │
│  │  (interface)             │                               │
│  └──────────────────────────┘                               │
└────────────────────────────────────────────────────────────┘
            │                                │
┌───────────▼────────────────────────────────▼───────────────┐
│                       DATA LAYER                            │
│  ┌─────────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │ Room Database   │  │ DataStore    │  │ AlarmManager  │  │
│  │ - AlarmDao      │  │ (Preferences)│  │ Impl          │  │
│  │ - AlarmEntity   │  └──────────────┘  └───────────────┘  │
│  └─────────────────┘                                        │
└────────────────────────────────────────────────────────────┘
            │
┌───────────▼────────────────────────────────────────────────┐
│                    SYSTEM LAYER                             │
│  ┌──────────────────────┐  ┌──────────────────────────────┐ │
│  │ AlarmBroadcastReceiver│  │ AlarmForegroundService       │ │
│  │  - fires on alarm    │  │  - plays ringtone            │ │
│  │  - starts Service    │  │  - vibrates                  │ │
│  └──────────────────────┘  │  - handles dismiss/snooze    │ │
│  ┌──────────────────────┐  └──────────────────────────────┘ │
│  │ BootReceiver          │                                  │
│  │  - reschedules alarms │                                  │
│  └──────────────────────┘                                   │
└────────────────────────────────────────────────────────────┘
```

## Module Structure

```
app/
├── src/main/kotlin/com/biobell/android/
│   ├── ui/
│   │   ├── theme/          # M3 theme, color, typography, shapes
│   │   ├── alarm/          # AlarmSetterScreen, AlarmListScreen, VMs
│   │   ├── settings/       # SettingsScreen, ChronotypePickerVM
│   │   ├── components/     # Shared composables (SleepBadge, InfoChip, TimePickerDialog)
│   │   └── navigation/     # NavGraph, Screen sealed class
│   ├── domain/
│   │   ├── model/          # Alarm, SleepPlan, Chronotype, HealthScore
│   │   ├── engine/         # SleepMathEngine (pure Kotlin — 0 Android deps, fully testable)
│   │   └── repository/     # AlarmRepository, SettingsRepository interfaces
│   ├── data/
│   │   ├── room/           # AlarmDatabase, AlarmDao, AlarmEntity, mappers
│   │   ├── datastore/      # UserPreferencesDataStore
│   │   └── repository/     # AlarmRepositoryImpl, SettingsRepositoryImpl
│   ├── alarm/
│   │   ├── AlarmSchedulerImpl.kt   # AlarmManager wrapper
│   │   ├── AlarmReceiver.kt        # BroadcastReceiver
│   │   ├── AlarmService.kt         # ForegroundService
│   │   └── BootReceiver.kt         # BOOT_COMPLETED receiver
│   └── di/                 # Hilt modules (DatabaseModule, RepositoryModule, etc.)
└── src/test/               # Unit tests (SleepMathEngine, ViewModel logic)
└── src/androidTest/        # Compose UI tests
```

## Data Flow — Alarm Creation

```
User fills AlarmSetterScreen
    → ViewModel.updateWakeTime() / .updateSleepDuration()
    → SleepMathEngine.validateSleepPlan(wakeTime, duration, chronotype)
    → emits SleepPlanResult(healthScore, warnings, suggestedBedtimes)
    → UI shows badge + warnings inline
    
User taps Save
    → ViewModel.saveAlarm()
    → AlarmRepository.insert(alarm)   → Room DB
    → AlarmScheduler.schedule(alarm)  → AlarmManager.setAlarmClock()
    → Confirmation shown on AlarmListScreen
```

## Data Flow — Alarm Firing

```
AlarmManager fires trigger
    → AlarmReceiver.onReceive()
    → Starts AlarmForegroundService
    → Service: acquires WakeLock, plays ringtone, shows full-screen intent
    → User taps Dismiss → Service: cancels notification, releases WakeLock
    → User taps Snooze  → Service: re-schedules +9 min via AlarmManager
    
Device reboots
    → BootReceiver.onReceive()
    → Queries all active alarms from Room
    → Re-schedules all via AlarmManager
```

## Sleep Math Engine — Key Algorithms

```kotlin
// Bedtime suggestions from wake time
fun suggestBedtimes(wakeTime: LocalTime, chronotype: Chronotype): List<LocalTime> {
    val offset = chronotype.offsetMinutes()  // -30 for 🐓, 0 for 🐦, +30 for 🦉
    return listOf(4, 5, 6).map { cycles ->
        wakeTime.minusMinutes((cycles * 90L) + SLEEP_ONSET_MINUTES + offset)
    }
}

// Health score: 0–100 based on duration + chronotype alignment
fun scoreHealthBadge(duration: Duration, wakeTime: LocalTime, chronotype: Chronotype): HealthScore

// Validate bidirectional plan (returns warnings list)
fun validateSleepPlan(bedtime: LocalTime, wakeTime: LocalTime): List<SleepWarning>
```

**Constants:**
- `SLEEP_ONSET_MINUTES = 15` (average time to fall asleep)
- Minimum healthy duration: 6h (4 cycles)
- Recommended: 7h30m (5 cycles) or 9h (6 cycles)
- Chronotype offsets: 🐓 = -30 min, 🐦 = 0, 🦉 = +30 min (adjusts suggested times)

## Build Order (Phase Dependencies)

1. **Foundation** — Project setup, theme, navigation skeleton
2. **Domain** — SleepMathEngine (pure Kotlin; tests pass before UI exists)
3. **Data** — Room + DataStore; alarm CRUD
4. **Alarm System** — AlarmManager + Receiver + ForegroundService + BootReceiver
5. **UI: Alarm Setter** — The core screen; bidirectional calculator + validation
6. **UI: Alarm List** — Show scheduled alarms; enable/disable toggle
7. **Education Layer** — Health badge, inline tips, info bottom sheets
8. **Settings** — Chronotype picker, general preferences
9. **Backup** — Auto Backup configuration

---
*Research date: 2026-03-17*
