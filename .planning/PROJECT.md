# BioBell

## What This Is

BioBell is a native Android alarm app whose core USP is flexible, biology-aware alarm setting. Users enter a desired sleep duration and a target wake-up time; the app validates the combination against sleep-cycle science and the user's chronotype, warns on unhealthy configurations, and suggests smarter alternatives. It educates users about sleep biology in-context rather than hiding the science.

## Core Value

**Smart alarm setting that respects sleep biology** — the bidirectional alarm calculator (sleep duration ↔ wake-up time) backed by sleep-cycle math and chronotype awareness must always work correctly and be instantly accessible.

## Requirements

### Validated

<!-- Shipped and confirmed valuable. -->

(None yet — ship to validate)

### Active

<!-- Current scope. Building toward these. -->

**Alarm Logic**
- [ ] User can enter both a target wake-up time and a desired sleep duration; app validates and resolves the combination
- [ ] App warns when the configured sleep plan is unhealthy (e.g., < 6 h, misaligned with chronotype)
- [ ] App calculates optimal alarm time using 90-minute sleep cycle model
- [ ] App adjusts suggestions based on selected chronotype (🦉 Night Owl / 🐦 Intermediate / 🐓 Early Bird)
- [ ] App suggests alternative wake-up times aligned to sleep-cycle boundaries

**Chronotype**
- [ ] User can set their chronotype via a simple picker on first launch (🦉 / 🐦 / 🐓)
- [ ] Chronotype setting is accessible from settings at any time

**Education Layer**
- [ ] Inline contextual tips appear when entering conflicting or unhealthy alarm data
- [ ] Sleep health badge / score visible on the alarm-setting screen
- [ ] Tappable info icons explain the science behind each suggestion

**Alarms**
- [ ] User can create, edit, and delete alarms
- [ ] Alarm fires reliably on Android (handles Doze mode, battery restrictions)

**Account & Backup**
- [ ] User can optionally sign in with Google account to back up alarm settings to the cloud
- [ ] Settings restore automatically on reinstall if backup is enabled

### Out of Scope

<!-- Explicit boundaries. Includes reasoning to prevent re-adding. -->

- Gradual wake-up (volume ramp / vibration escalation) — deferred to v2, adds significant complexity
- Sleep debt tracking — requires multi-night history; deferred to v2
- Multiple alarm sets (weekday/weekend profiles) — deferred to v2 to keep v1 lean
- REM-safe snooze (cycle-aligned snooze) — deferred to v2
- Sleep tracking via microphone/accelerometer — out of scope; BioBell is an alarm setter, not a sleep tracker
- Paid/premium features — freemium model planned but no IAP in v1
- iOS / cross-platform — Android-only for v1

## Context

- **Platform:** Native Android, targeting API 31+ (Material You / dynamic color support requires API 31)
- **Design reference:** Material You (M3) with dynamic color theming; visual inspiration from Sleep Cycle (clean, calming) and Alarmy (bold, functional)
- **Sleep science model:** 90-minute NREM/REM sleep cycle model; chronotype adjustments based on self-reported MEQ category
- **Freemium foundation:** Architecture should allow gating future features (e.g., custom soundscapes, advanced sleep analytics) behind a premium tier without major refactors
- **User base:** Both sleep-science-aware power users and mainstream users who just want better sleep — app educates without being condescending

## Constraints

- **Tech Stack:** Native Android (Kotlin + Jetpack Compose) — specified by user; no cross-platform frameworks
- **API Level:** Minimum API 31 (Android 12) — required for Material You dynamic color
- **Architecture:** MVVM with Jetpack Compose; Room for local storage; Hilt for DI — standard modern Android stack
- **Alarm Reliability:** Must handle Android's aggressive battery optimization (Doze, App Standby); requires `USE_EXACT_ALARM` or `SCHEDULE_EXACT_ALARM` permission strategy
- **Privacy:** Alarm data stored locally by default; cloud sync is opt-in Google account backup only
- **Freemium-ready:** Domain layer and data models must support feature flags / entitlement checks without coupling to billing in v1

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Native Android (Kotlin + Compose) | User specified; best performance, full platform API access, Material You native support | — Pending |
| Material You (M3) dynamic color | Modern Android design language; aligns with design references (Sleep Cycle, Alarmy) | — Pending |
| 90-minute sleep cycle model | Widely accepted, simple to implement correctly, immediately understandable to users | — Pending |
| Self-select chronotype (🦉/🐦/🐓) | Lowest friction vs. questionnaire; user can always change in settings | — Pending |
| Optional Google account backup | Privacy-respecting (opt-in), no proprietary backend needed for v1 | — Pending |
| Freemium architecture deferred | No IAP in v1 but models/features designed to be gate-able without refactor | — Pending |
| Minimum API 31 | Material You requires it; covers 80%+ of active Android devices as of 2024 | — Pending |

---
*Last updated: 2026-03-17 after initialization*
