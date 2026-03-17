# BioBell — Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-03-17)

**Core value:** Smart alarm setting that respects sleep biology — the bidirectional alarm calculator backed by sleep-cycle math and chronotype awareness must always work correctly and be instantly accessible.
**Current focus:** Not started — ready for Phase 1

---

## Current Status

**Milestone:** v1.0 — Core Biology-Aware Alarm App
**Active Phase:** None (initialization complete)
**Next Action:** `/gsd-discuss-phase 1` or `/gsd-plan-phase 1`

---

## Phase Status

| Phase | Name | Status |
|-------|------|--------|
| 1 | Project Foundation | ⬜ Not started |
| 2 | Sleep Math Engine | ⬜ Not started |
| 3 | Data Layer | ⬜ Not started |
| 4 | Alarm System | ⬜ Not started |
| 5 | Alarm Setter UI | ⬜ Not started |
| 6 | Alarm List & Management | ⬜ Not started |
| 7 | Settings, Chronotype & Backup | ⬜ Not started |

---

## Decisions Log

| Date | Decision | Reason |
|------|----------|--------|
| 2026-03-17 | Kotlin + Jetpack Compose + M3 | Native Android; Material You support; user specified |
| 2026-03-17 | Min SDK 31 | Material You dynamic color requires API 31 |
| 2026-03-17 | `USE_EXACT_ALARM` permission | Auto-granted for alarm clock apps; avoids user dialog |
| 2026-03-17 | `AlarmManager.setAlarmClock()` | Most reliable alarm method; survives Doze mode |
| 2026-03-17 | Android Auto Backup | No backend needed; zero-infra cloud backup |
| 2026-03-17 | 90-minute cycle model + 15-min onset | Standard sleep science; simple, understandable to users |
| 2026-03-17 | Self-select chronotype (🦉/🐦/🐓) | Lowest friction; user can change anytime |

---

## Planning Artifacts

| Artifact | Path | Status |
|----------|------|--------|
| Project context | `.planning/PROJECT.md` | ✅ Complete |
| Config | `.planning/config.json` | ✅ Complete |
| Research — Stack | `.planning/research/STACK.md` | ✅ Complete |
| Research — Features | `.planning/research/FEATURES.md` | ✅ Complete |
| Research — Architecture | `.planning/research/ARCHITECTURE.md` | ✅ Complete |
| Research — Pitfalls | `.planning/research/PITFALLS.md` | ✅ Complete |
| Research — Summary | `.planning/research/SUMMARY.md` | ✅ Complete |
| Requirements | `.planning/REQUIREMENTS.md` | ✅ Complete |
| Roadmap | `.planning/ROADMAP.md` | ✅ Complete |

---
*State initialized: 2026-03-17*
