# Phase 1: Project Foundation — Context

**Gathered:** 2026-03-17
**Status:** Ready for planning

<domain>
## Phase Boundary

Create a buildable, runnable native Android project with Material You (M3) theming, Hilt DI scaffold, and Compose navigation skeleton. No business logic, no data layer, no alarm system — pure structural foundation that every subsequent phase builds on.

</domain>

<decisions>
## Implementation Decisions

### Project Structure
- Single-module app for v1 (simplicity; multi-module adds overhead with no team-size benefit at this stage)
- Package name: `com.biobell.android`
- Gradle KTS build scripts (`build.gradle.kts`) throughout
- Gradle Version Catalog (`libs.versions.toml`) for all dependency versions — keeps deps centralized and upgrade-friendly
- Kotlin 2.0.x, AGP 8.3.x+

### Theme & Branding
- Material Design 3 (M3) with dynamic color enabled on API 31+
- Fallback seed color for devices < API 31: deep indigo/blue-purple (sleep/night-sky palette) — `#5B4FCF`
- Font: Inter (Google Fonts) via downloadable font — used for all text styles
- Dark theme is default; light theme supported via M3 tonal system
- Shapes: slightly rounded (M3 `ShapeDefaults` — small=4dp, medium=8dp, large=16dp)

### Navigation
- Single Activity + Compose Navigation (`NavHost`)
- Bottom navigation bar with 2 destinations: **Alarms** (AlarmListScreen) and **Settings** (SettingsScreen)
- AlarmSetterScreen reached via FAB on AlarmList (no bottom nav tab — it's a detail/creation flow)
- All screens start as placeholder `Box(Modifier.fillMaxSize())` with a centered title `Text`

### Dependency Strategy
- Version Catalog (`gradle/libs.versions.toml`) from day 1
- Compose BOM for all Compose library versions
- Hilt Application class + `@HiltAndroidApp` annotation + base DI module (empty `AppModule`)
- R8/ProGuard: keep default Android rules for now; add rules per-library as needed
- `buildFeatures { compose = true }` + `composeOptions { kotlinCompilerExtensionVersion }` via BOM

### Code Style
- `ktlint` configured in Gradle for consistent formatting
- Conventional commits for all git messages

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Planning
- `.planning/PROJECT.md` — Project vision, constraints, key decisions
- `.planning/ROADMAP.md` — Phase 1 goal and plan breakdown
- `.planning/research/STACK.md` — Full stack recommendations with versions

### No external ADRs yet — decisions captured above.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- None yet — greenfield project

### Established Patterns
- None yet — this phase establishes the patterns

### Integration Points
- `.agent/` directory present (GSD toolkit) — must be excluded from app source sets
- Git repo initialized at project root

</code_context>

<specifics>
## Specific Ideas

- Design inspiration: Sleep Cycle (clean, calming) + Alarmy (bold, functional)
- App icon placeholder acceptable for Phase 1 (real icon in a later phase)
- The dark-first, night-sky color palette should feel premium at first glance

</specifics>

<deferred>
## Deferred Ideas

- None — discussion stayed within phase scope

</deferred>

---

*Phase: 01-project-foundation*
*Context gathered: 2026-03-17*
