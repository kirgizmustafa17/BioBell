# Requirements: BioBell

**Defined:** 2026-03-17
**Core Value:** Smart alarm setting that respects sleep biology — the bidirectional alarm calculator backed by sleep-cycle math and chronotype awareness must always work correctly and be instantly accessible.

## v1 Requirements

### Alarms (ALARM)

- [ ] **ALARM-01**: User can create an alarm with a target wake-up time and desired sleep duration
- [ ] **ALARM-02**: User can enable or disable an existing alarm
- [ ] **ALARM-03**: User can edit an existing alarm
- [ ] **ALARM-04**: User can delete an alarm
- [ ] **ALARM-05**: Alarm fires reliably at the scheduled time (survives Doze mode and device reboot)
- [ ] **ALARM-06**: User can dismiss a firing alarm
- [ ] **ALARM-07**: User can snooze a firing alarm (fixed duration)
- [ ] **ALARM-08**: User can set alarm repeat schedule (daily, weekdays, or custom days)
- [ ] **ALARM-09**: User can select a ringtone from system sounds or set vibration-only

### Sleep Intelligence (SLEEP)

- [ ] **SLEEP-01**: App calculates optimal bedtime suggestions from a target wake-up time using the 90-minute sleep cycle model (accounting for 15-min sleep onset)
- [ ] **SLEEP-02**: App calculates optimal alarm time from a desired bedtime + sleep duration using the 90-minute sleep cycle model
- [ ] **SLEEP-03**: App validates the configured sleep plan and warns when it is unhealthy (e.g., < 6 hours, mid-cycle wake, chronotype mismatch)
- [ ] **SLEEP-04**: App adjusts bedtime/wake time suggestions based on the user's chronotype (🦉 +30 min later, 🐦 baseline, 🐓 −30 min earlier)
- [ ] **SLEEP-05**: App displays a sleep health badge/score for the current alarm configuration

### Education (EDU)

- [ ] **EDU-01**: Contextual inline tips appear when the user's alarm configuration is unhealthy or suboptimal
- [ ] **EDU-02**: Tappable info icons on the alarm setter explain the science behind suggestions (sleep cycles, chronotype)

### Chronotype (CHRON)

- [ ] **CHRON-01**: User can select their chronotype (🦉 Night Owl / 🐦 Intermediate / 🐓 Early Bird) during first-launch onboarding
- [ ] **CHRON-02**: User can change their chronotype at any time in Settings

### Backup (BACKUP)

- [ ] **BACKUP-01**: Alarm settings are automatically backed up to the user's Google account via Android Auto Backup
- [ ] **BACKUP-02**: Alarm settings restore automatically on reinstall if a backup is available

---

## v2 Requirements

### Smart Wake-Up
- **SMART-01**: App wakes user during their lightest sleep phase within a configurable window (requires microphone or accelerometer sensor tracking)
- **SMART-02**: Snooze extends alarm to the next sleep-cycle boundary rather than a fixed duration

### Gradual Wake
- **GRAD-01**: Alarm volume ramps up gradually over a 5–10 minute window
- **GRAD-02**: Optional sunrise simulation light trigger (via screen brightness)

### Sleep History
- **HIST-01**: App tracks actual wake times over multiple nights to refine suggestions
- **HIST-02**: App shows sleep debt accumulated over the past 7 days
- **HIST-03**: User can view a weekly sleep summary

### Multiple Alarm Profiles
- **PROF-01**: User can create named alarm sets (e.g., Weekday, Weekend, Travel)
- **PROF-02**: User can switch between active alarm profiles

### Premium / Freemium
- **PREM-01**: In-app purchase flow for premium features
- **PREM-02**: Feature flags to gate premium-only features

---

## Out of Scope

| Feature | Reason |
|---------|--------|
| Sleep tracking via microphone / accelerometer | Different product category; BioBell is a scheduler not a tracker |
| Heart rate / SpO2 / wearable integration | High complexity; different product entirely |
| Social features (share sleep stats) | Not aligned with core value |
| Alarm games / mission dismissal | Alarmy's territory; different user intent |
| Ads | Degrades premium experience |
| iOS / cross-platform | Android-only for v1; native required for AlarmManager reliability |
| Custom backend / proprietary sync | Auto Backup sufficient for v1; no infra maintenance |

---

## Traceability

*(Populated during roadmap creation)*

| Requirement | Phase | Status |
|-------------|-------|--------|
| ALARM-01 | Phase 3 + Phase 5 | Pending |
| ALARM-02 | Phase 3 + Phase 6 | Pending |
| ALARM-03 | Phase 3 + Phase 6 | Pending |
| ALARM-04 | Phase 3 + Phase 6 | Pending |
| ALARM-05 | Phase 4 | Pending |
| ALARM-06 | Phase 4 | Pending |
| ALARM-07 | Phase 4 | Pending |
| ALARM-08 | Phase 6 | Pending |
| ALARM-09 | Phase 4 | Pending |
| SLEEP-01 | Phase 2 + Phase 5 | Pending |
| SLEEP-02 | Phase 2 + Phase 5 | Pending |
| SLEEP-03 | Phase 2 + Phase 5 | Pending |
| SLEEP-04 | Phase 2 + Phase 5 | Pending |
| SLEEP-05 | Phase 2 + Phase 5 | Pending |
| EDU-01 | Phase 5 | Pending |
| EDU-02 | Phase 5 | Pending |
| CHRON-01 | Phase 7 | Pending |
| CHRON-02 | Phase 7 | Pending |
| BACKUP-01 | Phase 7 | Pending |
| BACKUP-02 | Phase 7 | Pending |

**Coverage:**
- v1 requirements: 20 total
- Mapped to phases: 20 ✓
- Unmapped: 0 ✓

---
*Requirements defined: 2026-03-17*
*Last updated: 2026-03-17 after initial definition*
