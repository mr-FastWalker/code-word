# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Code-Word** — an Android online multiplayer game inspired by Codenames. Two players, real-time sync via Firebase. Developed in Kotlin + Jetpack Compose. The developer is new to Android/Kotlin/Firebase but has frontend experience.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Navigation:** navigation-compose
- **Build:** Gradle with `build.gradle.kts`, version catalog at `gradle/libs.versions.toml`
- **Backend:** Firebase (no custom server)
  - **Firestore** — game state, rooms, profiles, stats (realtime listeners / Kotlin Flow)
  - **Authentication** — Anonymous (guest) + Google Sign-In (permanent account)
- **Local storage:** DataStore (guest stats on device)
- **Async:** kotlinx-coroutines
- **Tests:** JUnit (game engine only)

## Architecture

### Layer Structure

```
core/
  model/      — Card, Room, Player, GameConfig, Color, Role, Team (data classes)
  game/       — GameEngine: pure logic (board generation, moves, win check)

data/
  RoomRepository     — Firestore wrapper (create/join/listen/update)
  WordPackProvider   — word list by locale + packId
  SkinProvider       — skin resources by skinId
  auth/
    AuthRepository   — ensureSignedIn / linkWithGoogle / currentUser / signOut
  stats/
    StatsRepository      — unified interface (guest and user)
    LocalStatsStore      — DataStore, for guest
    RemoteStatsStore     — Firestore, for authenticated user
    StatsRepositoryImpl  — selects source by isAnonymous, merges on account link

feature/
  lobby/   — create room / join by code
  game/    — game screen (grid, clues, turns)
  profile/ — stats, sign-in

ui/
  theme/   — Compose theme
  skin/    — Skin model + skin registry

i18n/      — strings.xml + resource qualifiers
```

### Key Architectural Principles

- **Pure game engine** (`core/game/`) — no Firebase dependencies, fully unit-tested. Board layout, moves, win checking all work without network.
- **Parameterization over hardcoding** — language, word pack, and skin are parameters (`locale`, `wordPackId`, `skinId` in room state), never constants.
- **uid always exists** — both guests (Anonymous Auth) and logged-in users have a uid. Authentication = linking credentials to the existing anonymous uid via `linkWithCredential`, no data loss. All code treats uid the same way.
- **Skins are client-side** — `skinId` in Firestore is only an identifier. Actual `Skin` objects (colors, card back, fonts) live in a client-side `Map<String, Skin>` registry. Adding a skin = adding an entry to the map, no backend changes.
- **Firestore security rules are mandatory** before publishing — the only server-side protection since the app writes to the database directly.

### Game Engine API (`core/game/GameEngine`)

- `generateBoard(words, startingTeam): List<Card>` — pick 25 words, assign 9/8/7-neutral/1-assassin colors, shuffle
- `applyGuess(card): GameResult` — reveal card, recalculate score, check assassin/win, pass turn
- `submitClue(word, count)` — change phase to "guess"
- `checkWinner(): Team?`

## Firestore Data Model

```
/rooms/{roomId}
  schemaVersion: int
  status: "waiting" | "playing" | "finished"
  createdAt, updatedAt
  config:
    locale: "ru"
    wordPackId: "ru_base"
    skinId: "classic"
  board:
    cards: [{ id, word, color, revealed }]   // color: red|blue|neutral|assassin
    startingTeam: "red"
  turn:
    current: "red" | "blue"
    phase: "clue" | "guess"
    clue: { word, count } | null
  players:
    {uid}: { role: "spymaster"|"operative", team: "red"|"blue", name, connected }
  score: { redLeft, blueLeft }
  winner: "red" | "blue" | null

/users/{uid}           // created only on sign-in, never for guests
  isAnonymous: bool
  displayName: string
  createdAt, updatedAt
  stats:
    gamesPlayed, gamesWon, gamesLost: int
    asSpymaster, asOperative: int
    winStreak, bestWinStreak: int

/wordPacks/{packId}    // or stored as client assets for MVP
  locale: "ru"
  title: string
  words: [...]
```

## Guest vs Authenticated Users

- Guest stats are stored **locally** (DataStore only) — no `/users` document created, no Firestore writes.
- `/users/{uid}` is created **only at sign-in time**.
- On Google Sign-In: local stats are merged into Firestore once.
- Stats counting happens **client-side** after game end (for MVP). Cloud Functions path exists if anti-cheat is needed later.

## Implementation Order (Milestones)

1. **Engine + model** — pure logic, unit tests, no UI/network
2. **Game UI on local data** — grid, cards, flip animation, turns on hardcoded room (no Firebase)
3. **Auth layer** — `ensureSignedIn()` + anonymous sign-in
4. **Firebase rooms + sync** — RoomRepository, create/join, realtime listener
5. **Lobby + roles** — room code, teams, key map visible to spymasters only
6. **Stats** — `StatsRepository` + local store, write game result
7. **Account linking + profile screen** — Google sign-in, stats merge
8. **Polish** — UI i18n, word pack selection, result screen, disconnects
9. **Publishing** — Play Console, signing, store listing, internal testing → production

## Fixed Decisions

- **Package name:** choose once, never change (e.g. `com.codeword.app`) — cannot be changed after publishing
- `google-services.json` and keystore must be in `.gitignore` — never commit
- Room codes: 6 characters, generated client-side with collision check
- All UI strings via resources, zero hardcoded strings in code
- Words via `WordPackProvider`, never as constants in the engine
- UI styling via `Skin`, never hardcoded in Composables

## Open Questions (Resolve as You Go)

- Source of Russian word list (need ≥ 60–80 words)
- Store `wordPacks` in Firestore or client assets (assets simpler and cheaper for MVP)
- Where to host privacy policy (GitHub Pages is free)
- Detailed Firestore security rules — define at milestone 4


# 📋 Task Planning System

## Trigger
If the task text contains the word plan, create a plan file in .ai/plan/ before execution.

## Plan Creation
1. Create a file .ai/plan/<short-task-name>.md.
2. File structure:
```md
# Plan: <task name>

**Status:** in progress | done
**Created:** YYYY-MM-DD
**Session:** 1

## Goal
One sentence — what we aim to achieve.

## Steps
- [ ] Step 1 — description
- [ ] Step 2 — description
- [x] Step 3 — description ✅ result/note

## Notes
Any nuances, blockers, or decisions made along the way.
```

## During Execution
- After completing each step, update the checkbox: [ ] → [x] and add a short result.
- If a step produces an unexpected blocker — record it in ## Notes.
- When all steps are completed, change Status: done.

## Complex Tasks & Session Changes
- Handle complex tasks step by step: break them into independent blocks, each completable within one session.
- If the session context is close to overflowing or the task is paused — record current progress in the plan and stop.
- At the start of a new session, first read the plan file (Read .ai/plan/<name>.md) to restore context without reanalyzing the codebase.
- Increment the session number in the Session: field each time work resumes.

## File Naming
Use kebab-case, keep it short and meaningful: auth-roles.md, cart-redux.md, danea-import.md.

