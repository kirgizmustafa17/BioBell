# BioBell — Pitfalls Research

## 🔴 Critical Pitfalls (will silently break alarms)

### 1. Using `setExact()` or `set()` instead of `setAlarmClock()`
**Problem:** Standard `setExact()` alarms are deferred by Doze mode until the next maintenance window (could be hours). Users wake up late. This is the #1 silent killer of alarm apps.  
**Detection:** Test with device unplugged, screen off, in Doze for 30+ minutes.  
**Prevention:** Always use `AlarmManager.setAlarmClock()` for user-facing wake-up alarms. Reserve `setExactAndAllowWhileIdle()` only for non-critical secondary triggers.  
**Phase:** Phase — Alarm System implementation

---

### 2. Not Rescheduling After Reboot
**Problem:** `AlarmManager` alarms don't survive device restarts. Users who reboot their phone before the alarm won't be woken up.  
**Detection:** Schedule alarm → reboot device → verify alarm still fires.  
**Prevention:** Register `BroadcastReceiver` for `BOOT_COMPLETED`, query all active alarms from Room, reschedule all via `AlarmManager.setAlarmClock()`.  
**Phase:** Phase — Alarm System implementation (BootReceiver)

---

### 3. Using `SCHEDULE_EXACT_ALARM` Instead of `USE_EXACT_ALARM`
**Problem:** `SCHEDULE_EXACT_ALARM` (API 31+) is NOT pre-granted on Android 13/14. Users will have missed alarms with no explanation. `USE_EXACT_ALARM` is auto-granted on install for alarm clock category apps.  
**Detection:** Install fresh on Android 14 device; attempt to set alarm; check if `SecurityException` thrown.  
**Prevention:** Declare `USE_EXACT_ALARM` in manifest (not `SCHEDULE_EXACT_ALARM`). Verify category is `android.intent.category.LAUNCHER` with correct app category. Check `canScheduleExactAlarms()` at runtime as safety net.  
**Phase:** Phase — Project Foundation (manifest setup)

---

### 4. OEM Battery Optimization Killing the ForegroundService
**Problem:** Samsung, Xiaomi, Huawei, OnePlus, and others have their own battery optimization layers *on top of* Android's DOZE. Even with `setAlarmClock()` + ForegroundService, some OEMs aggressively kill services.  
**Detection:** Test on physical Xiaomi/Samsung device (not just emulator). Use `dontkillmyapp.com` as reference.  
**Prevention:** Add battery optimization exemption request dialog (direct user to Settings → Battery → Not Optimized for BioBell). Show this onboarding prompt once after first alarm is set.  
**Phase:** Phase — Alarm System + Onboarding

---

### 5. PendingIntent Without `FLAG_IMMUTABLE`
**Problem:** On API 31+, `PendingIntent.getActivity()` / `getBroadcast()` without `FLAG_IMMUTABLE` throws `IllegalArgumentException` and crashes on alarm scheduling.  
**Detection:** Lint warning; also crashes at runtime on API 31+.  
**Prevention:** Always set `PendingIntent.FLAG_IMMUTABLE` (or `FLAG_MUTABLE` only if mutation is needed).  
**Phase:** Phase — Alarm System (all PendingIntent calls)

---

## 🟡 Significant Pitfalls (cause data loss or bad UX)

### 6. Alarm Time Math Crossing Midnight
**Problem:** If wake time is 6:00 AM and bedtime math subtracts 7h30m, crossing midnight produces wrong LocalTime arithmetic (Android's time APIs don't handle date context automatically on `LocalTime`).  
**Detection:** Set wake time to 02:00, duration 7h — verify bedtime shows previous day correctly.  
**Prevention:** Use `LocalDateTime` internally; display only the time component to users. Track "tonight vs tomorrow night" context in the domain model.  
**Phase:** Phase — SleepMathEngine

---

### 7. Room Migration Omission
**Problem:** Adding columns to `AlarmEntity` without a migration crashes the app with `IllegalStateException` for existing users. Silent data loss.  
**Detection:** Upgrade app install with existing data.  
**Prevention:** Every schema change must include `Migration(from, to)`. Use `fallbackToDestructiveMigration()` only in debug builds.  
**Phase:** Phase — Data Layer (and every schema change thereafter)

---

### 8. Sleep Onset Time Missing from Calculator
**Problem:** If bedtime = wakeTime - 6h, users actually only get 5h45m of sleep (ignoring ~15 min to fall asleep). Makes the health score misleading.  
**Detection:** Calculator shows "6 cycles = 7h30m" → verify actual sleep time accounts for onset.  
**Prevention:** Add `SLEEP_ONSET_MINUTES = 15` constant to all bedtime calculations. Communicate this to users ("add 15 min to fall asleep").  
**Phase:** Phase — SleepMathEngine

---

### 9. Chronotype Offset Not Applied Consistently
**Problem:** Chronotype picker changes the setting, but AlarmSetter forgets to re-trigger validation when chronotype changes in Settings. User sees stale health score.  
**Detection:** Change chronotype mid-session; verify AlarmSetter badge updates.  
**Prevention:** Expose chronotype as `StateFlow` from SettingsRepository; collect in AlarmSetterViewModel; re-run validation on every emission.  
**Phase:** Phase — Settings + AlarmSetter integration

---

### 10. Auto Backup Including Sensitive or Invalid Data
**Problem:** If Room DB grows large or includes cached/transient state, Auto Backup will bloat user's Google Drive and potentially restore stale alarm states.  
**Detection:** Check backup size after a few weeks of use.  
**Prevention:** Create `backup_rules.xml` that explicitly includes only `alarms.db` and excludes temp files. Set `android:fullBackupContent` in manifest. On restore, always validate alarm times against current time and reschedule or discard past alarms.  
**Phase:** Phase — Backup configuration

---

## 🟢 Watch-Out Tips

| Issue | Quick Fix |
|-------|-----------|
| Compose recomposition on every keystroke in time picker | Use `derivedStateOf` to memoize expensive sleep math computations |
| `Flow.collect` in `LaunchedEffect` with wrong lifecycle | Use `collectAsStateWithLifecycle` instead of `LaunchedEffect + collect` |
| Snooze re-alarm not cancellable | Store snooze PendingIntent ID in DB; cancel by ID on dismiss |
| `setAlarmClock()` requires non-null show-intent | Always provide a valid `PendingIntent` for the clock info intent |
| Time display 12h vs 24h | Respect system `DateFormat.is24HourFormat()` setting; don't hardcode |
| Alarm fires while screen is on | Handle `KeyguardManager` / `turnScreenOn` flags for full-screen intent on API 27+ |

---
*Research date: 2026-03-17*
