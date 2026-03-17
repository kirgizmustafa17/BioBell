# BioBell — Features Research

## Competitive Landscape

| App | Core Differentiator | Weakness |
|-----|---------------------|----------|
| **Sleep Cycle** | Smart alarm wakes you in lightest sleep phase (microphone/accelerometer) | Requires sleeping with phone; sensor-dependent |
| **Alarmy** | Mission-based alarm dismissal (photos, math, QR codes) | No sleep science; just dismissal UX |
| **RISE** | Circadian rhythm tracking, energy prediction, chronotype | No alarm scheduling; advisory only |
| **Sleep as Android** | Deep feature set; sleep tracking + smart alarm | Complex UI; overwhelming for casual users |

**Gap BioBell fills:** Pure alarm *setter* with biology intelligence — no sensor-based sleep tracking, just scientifically-grounded scheduling decisions made *before* you sleep.

---

## Features by Category

### 🔴 Table Stakes (Must Have — users expect these)

| Feature | Complexity | Notes |
|---------|------------|-------|
| Set a single alarm (time + repeat days) | Low | Basic alarm creation |
| Enable/disable alarm toggle | Low | |
| Reliable alarm ring + dismiss | Medium | ForegroundService + AlarmManager |
| Snooze | Low | Standard; fixed duration (e.g. 5/9 min) |
| Custom alarm label/name | Low | |
| System ringtone / vibration selection | Medium | RingtoneManager integration |
| Dark mode support | Low | M3 dynamic color handles this |
| Upcoming alarm shown on home screen | Low | Next alarm widget / summary card |

### 🟡 Differentiators (BioBell's USP — what makes it special)

| Feature | Complexity | BioBell Scope |
|---------|------------|----------------|
| **Bidirectional alarm calculator** | Medium | Enter wake time → suggest bedtimes; enter sleep duration → calculate alarm; validate combination | **v1** |
| **Sleep cycle alignment** | Medium | 90-min cycle model; suggest wake times at cycle boundaries | **v1** |
| **Chronotype-aware suggestions** | Medium | Shift suggestions ±30–60 min based on 🦉/🐦/🐓 type | **v1** |
| **Health validation + warnings** | Low-Medium | Warn if < 6h planned, or if wake time misaligns chronotype | **v1** |
| **Contextual inline tips** | Low | Tooltip-style tips on alarm setting form | **v1** |
| **Sleep health badge / score** | Medium | Visual indicator (A/B/C or 1–100) on alarm card | **v1** |
| **Tappable science explanations** | Low | Info icons → bottom sheet explaining sleep cycles | **v1** |
| **Chronotype picker** | Low | Simple 3-option picker on onboarding + settings | **v1** |
| Smart alarm window (wake in lightest phase) | High | Requires sensor (mic/accel); no BioBell v1 | **v2** |
| Cycle-aligned snooze | Low-Medium | Snooze by remaining time to next cycle boundary | **v2** |
| Gradual volume ramp / sunrise alarm | Medium | Escalating alarm over 5–10 min window | **v2** |
| Sleep debt tracking | High | Requires multi-night history + analytics | **v2** |

### 🔵 Account & Backup

| Feature | Complexity | BioBell Scope |
|---------|------------|----------------|
| Android Auto Backup (Google Drive) | Low | No backend needed; just enable in manifest | **v1** |
| Sign in with Google (explicit backup trigger) | Medium | Google Sign-In + Drive API or just Auto Backup passive | **v1** (Auto Backup passive) |
| Manual backup/restore | Medium | | **v2** |

### ⚪ Anti-Features (Deliberately NOT Building)

| Feature | Reason |
|---------|--------|
| Sleep tracking via microphone | Out of scope; BioBell is a scheduler, not a tracker |
| Heart rate / SpO2 / wearable integration | Complexity; different product category |
| Social features (share sleep stats) | Not aligned with core value |
| Alarm games / mission dismissal | Alarmy's territory; different user intent |
| Subscription paywall in v1 | Freemium deferred; build trust first |
| Ads | Degrades experience; avoided entirely |

---

## Feature Dependencies

```
Chronotype picker
    └──> Chronotype-aware suggestions
         └──> Health validation warnings
              └──> Sleep health badge

Bidirectional calculator
    └──> Sleep cycle alignment
         └──> Health validation warnings

Reliable alarm ring
    └──> USE_EXACT_ALARM + AlarmManager
         └──> BOOT_COMPLETED receiver (reschedule)
              └──> ForegroundService (audio playback)
```

---

## UX Notes from Competitors

- **Sleep Cycle**: Clean, calming, minimal — sets the bar for sleep app aesthetics
- **Alarmy**: Bold typography, high-contrast — sets the bar for "alarm is serious" feel
- **BioBell target**: Calm by default (Sleep Cycle energy) but firm on alarm delivery (Alarmy reliability)
- Users appreciate **explaining the why** — apps that say "6h = 4 sleep cycles, here's why that matters" outperform ones that just show a number

---
*Research date: 2026-03-17*
