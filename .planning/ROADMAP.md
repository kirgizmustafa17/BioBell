# BioBell — Roadmap

**Project:** BioBell
**Milestone:** v1.0 — Core Biology-Aware Alarm App
**Created:** 2026-03-17
**Total Phases:** 7
**v1 Requirements:** 20 mapped ✓

---

## Phase Overview

| # | Phase | Goal | Requirements | Success Criteria |
|---|-------|------|--------------|-----------------|
| 1 | Project Foundation | Complete    | 2026-03-17 | 3 |
| Complete    | 2026-03-17 | Complete    | 2026-03-17 | 4 |
| Complete    | 2026-03-17 | Alarm persistence (Room) and user settings (DataStore) | ALARM-01, ALARM-02, ALARM-03, ALARM-04 | 3 |
| 4 | Alarm System | Reliable alarm scheduling, firing, dismiss, snooze, reboot recovery | ALARM-05, ALARM-06, ALARM-07, ALARM-09 | 5 |
| 5 | Alarm Setter UI | Core bidirectional alarm calculator screen — BioBell's USP | ALARM-01 (UI), SLEEP-01, SLEEP-02, SLEEP-03, SLEEP-05, EDU-01, EDU-02 | 5 |
| 6 | Alarm List & Management | Full alarm management UX — list, toggle, edit, delete, repeat | ALARM-02, ALARM-03, ALARM-04, ALARM-08 | 3 |
| 7 | Settings, Chronotype & Backup | Chronotype picker, app settings, onboarding, Auto Backup | CHRON-01, CHRON-02, BACKUP-01, BACKUP-02 | 4 |

---

## Phase Details

---

### Phase 1: Project Foundation

**Goal:** Create a buildable, runnable Android project with Material You theming, navigation skeleton, and CI-ready Gradle setup. No business logic yet — just the structural foundation every other phase builds on.

**Requirements mapped:** *(foundational — no v1 requirements directly; enables all phases)*

**Plans:**
5/5 plans complete
2. Add Jetpack Compose BOM, Material3, and Accompanist dependencies
3. Set up M3 theme with dynamic color + fallback light/dark color schemes, typography (Inter font), shapes
4. Create single-Activity Compose app with NavGraph (placeholder screens: AlarmSetter, AlarmList, Settings)
5. Configure Hilt application class and base DI module structure

**Success Criteria:**
1. App builds and launches without error on API 31+ emulator
2. Dynamic color theme applies from wallpaper on API 31 device
3. Navigation between 3 placeholder screens works
4. Hilt injection confirmed (no DI crashes on launch)
5. Dark mode renders correctly with fallback palette

---

### Phase 2: Sleep Math Engine

**Goal:** Implement all sleep science calculations as pure Kotlin — zero Android dependencies, fully unit-tested. This is the intellectual core of BioBell and must be correct before any UI is built on top of it.

**Requirements mapped:** SLEEP-01, SLEEP-02, SLEEP-03, SLEEP-04, SLEEP-05

**Plans:**
0/0 plans complete
2. Implement `SleepMathEngine`:
   - `suggestBedtimes(wakeTime, chronotype)` → list of 3 bedtime options (4/5/6 cycles)
   - `suggestWakeTimes(bedtime, chronotype)` → list of 3 wake time options
   - `validateSleepPlan(bedtime, wakeTime, chronotype)` → list of `SleepWarning`
   - `scoreHealth(duration, wakeTime, chronotype)` → `HealthScore` (0–100 + letter grade)
   - `applyChronotype(time, chronotype)` → adjusted time
3. Constants: `SLEEP_ONSET_MIN = 15`, `CYCLE_DURATION_MIN = 90`, min healthy = 360 min (6h), recommended = 450 min (7h30m)
4. Write comprehensive unit tests (JUnit 5 + Turbine):
   - Midnight crossover math
   - All 3 chronotype offsets
   - Boundary conditions (exactly 6h, exactly 4 cycles)
   - Warning triggers (< 6h, mid-cycle wake)
5. Define `AlarmScheduler` and `AlarmRepository` interfaces (no impl — just contracts)

**Success Criteria:**
1. All unit tests pass (100% of SleepMathEngine paths covered)
2. Midnight crossover case (e.g., wake at 02:00, 7h duration) returns correct previous-day bedtime
3. Chronotype offsets shift suggestions by correct ±30 min delta
4. HealthScore correctly grades: 7h30m → A, 6h → C, 5h → F
5. `SleepWarning` emitted for < 6h, mid-cycle (non-90-min-boundary) wake times

---

### Phase 3: Data Layer

**Goal:** Implement Room database for alarm persistence and DataStore for user preferences (chronotype, settings). Wire up repositories. Alarms survive app restarts.

**Requirements mapped:** ALARM-01 (data), ALARM-02 (data), ALARM-03 (data), ALARM-04 (data)

**Plans:**
0/0 plans complete
2. Migration strategy: `Migration(1, 2)` skeleton + `fallbackToDestructiveMigration()` for debug only
3. Implement `AlarmRepositoryImpl` wrapping `AlarmDao` (Kotlin Flow for reactive list)
4. Set up `DataStore<Preferences>` for: `chronotype`, `onboardingComplete`, `batteryOptimizationPromptShown`
5. Implement `SettingsRepositoryImpl` wrapping DataStore
6. Wire `DatabaseModule` and `RepositoryModule` in Hilt

**Success Criteria:**
1. Create alarm → query → alarm present in Room DB
2. Delete alarm → alarm absent from Room DB
3. Update alarm → changes persisted correctly
4. App restart → all alarms still retrieved from DB
5. DataStore correctly persists and emits chronotype setting changes

---

### Phase 4: Alarm System

**Goal:** Make alarms actually fire reliably. Implement AlarmManager scheduling, BroadcastReceiver, ForegroundService for audio, snooze logic, and reboot recovery. This is the hardest Android-specific phase.

**Requirements mapped:** ALARM-05, ALARM-06, ALARM-07, ALARM-09

**Plans:**
0/0 plans complete
2. Implement `AlarmSchedulerImpl`: wraps `AlarmManager.setAlarmClock()` for scheduling; cancel by `PendingIntent` ID; always use `FLAG_IMMUTABLE`
3. Implement `AlarmReceiver` (BroadcastReceiver): receives alarm trigger → starts `AlarmForegroundService`
4. Implement `AlarmForegroundService`: acquires WakeLock, plays ringtone via `MediaPlayer`, shows full-screen notification intent; handles `ACTION_DISMISS` and `ACTION_SNOOZE` intents
5. Implement `BootReceiver` (BOOT_COMPLETED): queries all enabled alarms from Room → reschedules each via `AlarmSchedulerImpl`
6. Wire ringtone picker (system `RingtoneManager`): store URI in `AlarmEntity.ringtoneUri`; fallback to default system alarm sound

**Success Criteria:**
1. Alarm fires within ±5 seconds of scheduled time on stock Android 12+ emulator
2. Alarm fires correctly after device reboot (BootReceiver reschedules)
3. Dismiss stops audio and cancels notification
4. Snooze re-triggers alarm exactly 9 minutes later
5. Alarm fires with screen locked (full-screen intent shown on lockscreen)
6. `USE_EXACT_ALARM` declared correctly — no `SecurityException` on scheduling

---

### Phase 5: Alarm Setter UI

**Goal:** Build BioBell's core screen — the bidirectional alarm calculator. User enters wake time and/or sleep duration; the app computes suggestions using the Sleep Math Engine, shows health badge, warns on bad configurations, and educates inline. This is the USP made tangible.

**Requirements mapped:** ALARM-01 (UI), SLEEP-01, SLEEP-02, SLEEP-03, SLEEP-05, EDU-01, EDU-02

**Plans:**
1. Design and implement `AlarmSetterScreen` layout: time picker (wake time), duration picker (hours + minutes), suggested bedtimes carousel, health badge component, warning banner
2. Implement `AlarmSetterViewModel`: holds `SleepPlanUiState`; on any input change, calls `SleepMathEngine` and emits updated state (`suggestedBedtimes`, `healthScore`, `warnings`, `resolvedAlarmTime`)
3. Build `SleepHealthBadge` composable: letter grade (A/B/C/D/F) + color-coded ring (green → red) + short description
4. Build `SleepWarningBanner` composable: inline contextual tip (e.g., "Only 5h planned — below your recommended 7h30m")
5. Build `InfoBottomSheet` composable: triggered by ℹ️ icon; explains sleep cycles or chronotype science; soft educational tone
6. Wire `AlarmSetterViewModel.saveAlarm()` → `AlarmRepository.insert()` → `AlarmScheduler.schedule()` → navigate back to AlarmList

**Success Criteria:**
1. Entering wake time 07:00 + duration 7h30m shows bedtime of 23:15 (accounting for 15-min onset) and health badge A
2. Entering wake time 07:00 + duration 5h shows warning banner and badge F
3. Tapping ℹ️ icon opens bottom sheet with sleep cycle explanation
4. Saving alarm persists to Room and schedule is confirmed via AlarmScheduler
5. Chronotype 🦉 shifts suggested bedtime +30 min vs 🐦 baseline

---

### Phase 6: Alarm List & Management

**Goal:** Build the home screen — a list of all scheduled alarms with enable/disable toggle, edit, delete, and repeat display. Users can see their upcoming biologically-optimized alarm at a glance.

**Requirements mapped:** ALARM-02, ALARM-03, ALARM-04, ALARM-08

**Plans:**
1. Implement `AlarmListScreen`: shows list of `AlarmCard` composables; FAB to create new alarm; empty state illustration with call-to-action
2. Build `AlarmCard` composable: displays wake time (large), health badge (small), label, repeat days abbreviations, enable/disable `Switch`
3. Implement `AlarmListViewModel`: observes `AlarmRepository.getAllAlarms()` as `StateFlow<List<AlarmUiModel>>`; handles toggle (update DB + reschedule/cancel AlarmManager), delete (with undo snackbar)
4. Wire edit flow: tap AlarmCard body → navigate to `AlarmSetterScreen` pre-populated with existing alarm data
5. Implement repeat day picker in `AlarmSetterScreen`: M/T/W/T/F/S/S toggle chips; store as bitmask in `AlarmEntity.repeatDays`; schedule or cancel individual day alarms accordingly

**Success Criteria:**
1. Created alarms appear in list with correct wake time and health badge
2. Toggle switch disables alarm (AlarmManager cancelled) and re-enables (re-scheduled)
3. Delete removes from list and cancels AlarmManager; undo snackbar restores within 4 seconds
4. Tapping alarm card opens AlarmSetter pre-filled with existing values
5. Repeat days display correctly (e.g., "Mon, Wed, Fri")

---

### Phase 7: Settings, Chronotype & Backup

**Goal:** Complete the app with chronotype onboarding, settings screen, battery optimization guidance, and Auto Backup configuration. The app should feel complete and production-ready.

**Requirements mapped:** CHRON-01, CHRON-02, BACKUP-01, BACKUP-02

**Plans:**
1. Implement first-launch onboarding flow: single-screen chronotype picker (🦉/🐦/🐓 cards with description); stores to DataStore; sets `onboardingComplete = true`
2. Implement `SettingsScreen`: chronotype picker (re-selectable), 12h/24h time format toggle (respects system default), about/version info, link to GitHub/privacy policy placeholder
3. Implement battery optimization prompt: after first alarm is saved, check `PowerManager.isIgnoringBatteryOptimizations()`; if not ignored, show one-time bottom sheet guiding user to Settings → Battery → BioBell → Don't Optimize; store `batteryOptimizationPromptShown = true` so it never repeats
4. Configure Android Auto Backup: `android:allowBackup="true"` in manifest; create `res/xml/backup_rules.xml` to include `alarms.db` + DataStore preferences file; exclude cache/temp files
5. Ensure chronotype changes in Settings trigger real-time re-evaluation in `AlarmSetterViewModel` (via `SettingsRepository` Flow)

**Success Criteria:**
1. First launch shows chronotype picker; selection persists across app restarts
2. Changing chronotype in Settings immediately updates health badge and suggestions on AlarmSetter screen
3. Battery optimization prompt appears once after first alarm is saved, never again
4. Auto Backup config verified: backup rules XML present, `allowBackup=true` in manifest
5. Uninstall → reinstall with same Google account → alarms restored

---

## Requirement Coverage

All 20 v1 requirements mapped ✓

| Requirement | Phase |
|-------------|-------|
| ALARM-01 | Phase 3 (data) + Phase 5 (UI) |
| ALARM-02 | Phase 3 (data) + Phase 6 (UI) |
| ALARM-03 | Phase 3 (data) + Phase 6 (UI) |
| ALARM-04 | Phase 3 (data) + Phase 6 (UI) |
| ALARM-05 | Phase 4 |
| ALARM-06 | Phase 4 |
| ALARM-07 | Phase 4 |
| ALARM-08 | Phase 6 |
| ALARM-09 | Phase 4 |
| SLEEP-01 | Phase 2 (logic) + Phase 5 (UI) |
| SLEEP-02 | Phase 2 (logic) + Phase 5 (UI) |
| SLEEP-03 | Phase 2 (logic) + Phase 5 (UI) |
| SLEEP-04 | Phase 2 (logic) + Phase 5 (UI) |
| SLEEP-05 | Phase 2 (logic) + Phase 5 (UI) |
| EDU-01 | Phase 5 |
| EDU-02 | Phase 5 |
| CHRON-01 | Phase 7 |
| CHRON-02 | Phase 7 |
| BACKUP-01 | Phase 7 |
| BACKUP-02 | Phase 7 |

---
*Roadmap created: 2026-03-17*
