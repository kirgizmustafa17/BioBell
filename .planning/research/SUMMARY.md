# BioBell — Research Summary

## Stack (Recommended)

| Layer | Choice |
|-------|--------|
| **Language** | Kotlin 2.0 + Coroutines + Flow |
| **UI** | Jetpack Compose + Material Design 3 |
| **Architecture** | MVVM + Clean Architecture + Hilt |
| **Local storage** | Room (alarms) + DataStore Preferences (settings) |
| **Alarm scheduling** | `AlarmManager.setAlarmClock()` + ForegroundService + BootReceiver |
| **Backup** | Android Auto Backup → Google Drive (passive, opt-in) |
| **Min SDK** | API 31 (Android 12) — required for Material You |
| **Target SDK** | API 34 (Android 14) |

**Key permission:** `USE_EXACT_ALARM` (auto-granted for alarm clock apps — NOT `SCHEDULE_EXACT_ALARM`)

---

## Table Stakes (Users Expect These in v1)

- ✅ Create / edit / delete alarm with time + repeat days
- ✅ Enable/disable alarm toggle  
- ✅ Reliable alarm ring + dismiss + snooze
- ✅ Dark mode (M3 dynamic color)
- ✅ Next alarm displayed prominently

---

## BioBell Differentiators (v1)

- 🎯 **Bidirectional alarm calculator** — enter wake time OR sleep duration, app resolves the other
- 🎯 **Sleep cycle alignment** — suggestions snap to 90-min cycle boundaries + 15-min sleep onset
- 🎯 **Chronotype adjustment** — 🦉/🐦/🐓 shifts suggestions ±30 min
- 🎯 **Health validation** — warns on < 6h, chronotype mismatch, conflicting inputs
- 🎯 **Education layer** — inline tips, sleep health badge, tappable science info

---

## Watch Out For (Critical Pitfalls)

1. 🔴 **Use `setAlarmClock()` ONLY** — `setExact()` is defeated by Doze; alarms will silently miss
2. 🔴 **Reschedule on BOOT_COMPLETED** — AlarmManager doesn't survive reboots
3. 🔴 **`USE_EXACT_ALARM` not `SCHEDULE_EXACT_ALARM`** — auto-granted for alarm apps; user dialog avoided
4. 🟡 **OEM battery killers** — guide users to disable battery optimization (Samsung, Xiaomi especially)
5. 🟡 **Midnight math** — use `LocalDateTime` internally; `LocalTime` subtraction crosses midnight incorrectly
6. 🟡 **Sleep onset offset** — always subtract 15 min from sleep duration in calculations

---

## Recommended Build Order

```
Phase 1: Foundation      → Gradle project, M3 theme, navigation skeleton
Phase 2: Sleep Engine    → SleepMathEngine (pure Kotlin, fully tested)
Phase 3: Data Layer      → Room, DataStore, AlarmRepository
Phase 4: Alarm System    → AlarmManager, ForegroundService, BootReceiver
Phase 5: Alarm Setter UI → Core bidirectional calculator screen
Phase 6: Alarm List UI   → Manage alarms, toggle, delete
Phase 7: Education Layer → Health badge, inline tips, info sheets
Phase 8: Settings        → Chronotype picker, app preferences
Phase 9: Backup          → Auto Backup config, onboarding battery prompt
```

---
*Synthesized: 2026-03-17 from STACK.md, FEATURES.md, ARCHITECTURE.md, PITFALLS.md*
