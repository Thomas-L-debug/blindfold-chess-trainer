# Blindfold Chess Trainer — PROJECT CONTEXT

**Last updated:** June 18, 2026

---

## Project Goal

Mobile application (Android first, iOS possible later) to help people learn and train **chess blindfolded** — pure mental visualization.

The app must feel calm, respectful, and non-addictive.  
No streaks, fake urgency, dark patterns, or manipulation.  
Goal: peaceful, pleasant training at the user's own pace.

---

## Core Philosophy

- No pressure gamification (no daily streaks, no FOMO).
- Calm, focused training sessions.
- User in control at all times.
- Natural progress, not forced.
- Long-term, low-stress improvement.

---

## Current Status (June 2026)

### Done

| Area | Status | Notes |
|---|---|---|
| Android project (Kotlin + Compose) | ✅ | AGP 8.9.1, Kotlin 2.1.10, compileSdk 35 |
| Module `core:chess` | ✅ | Square, SquareColor, SquareColorDrill + unit tests |
| Module `app` | ✅ | UI Compose, dark theme |
| Drill « Square Colors » | ✅ | Random square → Light / Dark, local score |
| Chess board UI | ✅ | 8×8 grid, global on all screens via `AppShell` |
| Board controls | ✅ | Show/hide board, toggle coordinates (ranks left + files bottom) |
| CI GitHub Actions | ✅ | `./gradlew test assembleDebug` on push/PR |
| Docker dev env | ✅ | Optional, for WSL/CI-like builds |
| Web preview (`preview/`) | ✅ | Static HTML demo (not the real app) |
| Windows dev setup | ✅ | Primary workspace: `D:\CodingProject\blindfold-chess-trainer` |

### Not started yet

- More visualization drills (square names, diagonals, knight tours…)
- Blindfold puzzles
- Stockfish / blindfold games
- Room (session history, progress)
- Piece rendering on the board
- ktlint / detekt
- Voice input

---

## Architecture

```
MainActivity
└── AppShell                    ← top: board (optional), bottom: page content
    ├── BoardPanel              ← ChessBoard + coordinates checkbox
    ├── [Hide/Show board button]
    └── content slot
        ├── HomeScreen          ← drill launcher
        └── SquareColorDrillScreen
```

**Modules:**
- `:app` — UI Compose, features, theme
- `:core:chess` — chess logic (no UI), chesslib dependency ready

**Patterns in use:**
- ViewModel + StateFlow for drills
- `rememberSaveable` for board/drill navigation state in `MainActivity`
- Feature folders: `feature/home`, `feature/drills`, `feature/board`

**Key files:**
```
app/src/main/kotlin/com/blindfoldchess/trainer/
├── MainActivity.kt
├── feature/
│   ├── board/
│   │   ├── AppShell.kt         # Layout global (board + content)
│   │   ├── BoardPanel.kt       # Board + checkbox coordinates
│   │   └── ChessBoard.kt       # Canvas 8×8, fixed-size notation slots
│   ├── home/HomeScreen.kt
│   └── drills/
│       ├── SquareColorDrillScreen.kt
│       └── SquareColorDrillViewModel.kt
└── ui/theme/                   # Dark calm palette

core/chess/src/main/kotlin/.../
├── Square.kt
├── SquareColor.kt
└── SquareColorDrill.kt
```

---

## UI Behaviour (board)

- **Top half of screen:** chess board (when visible), on **every page** (home + drills).
- **Checkbox « Coordinates »:** toggles rank labels (1–8, left) and file labels (a–h, **bottom only**).
- **Board stays fixed** when toggling coordinates — notation space is always reserved.
- **« Hide board » / « Show board »:** toggles entire top panel; button stays above content on all pages.
- Board shows empty squares only (no pieces yet). Colors use `SquareColor.of()` from `core:chess`.

---

## Tech Stack (confirmed)

| Layer | Choice |
|---|---|
| Mobile UI | Kotlin + Jetpack Compose (Material 3, dark theme) |
| Chess lib | `com.github.bhlangonijr:chesslib:1.3.3` (integrated, not yet used in UI) |
| State | ViewModel + Kotlin Flow (drills); expand to MVI later if needed |
| DB (future) | Room |
| Engine (future) | Stockfish via JNI |
| Tests | JUnit4/5 unit tests in `core:chess` |
| CI | GitHub Actions (`.github/workflows/ci.yml`) |
| DevOps optional | Docker + `docker-compose.yml` |

**Versions** (`gradle/libs.versions.toml`):
- AGP 8.9.1, Kotlin 2.1.10, Compose BOM 2025.02.00
- minSdk 26, targetSdk 35, JDK 21

---

## Development Environment

### Primary workspace (recommended)

```
D:\CodingProject\blindfold-chess-trainer
```

- Open in **Android Studio (Windows)**.
- **Gradle JDK:** `jbr-21` (NOT « 21 (WSL) »).
- **Android SDK:** `C:\Users\thoma\AppData\Local\Android\Sdk`
- **`local.properties`:**
  ```properties
  sdk.dir=C\:\\Users\\thoma\\AppData\\Local\\Android\\Sdk
  ```

**Run app:** open project on `D:\` → **Run ▶** on module `app`.  
Code changes (Kotlin/Compose): Run ▶ is enough (no Gradle sync needed unless `build.gradle.kts` changes).

**Setup script (if config breaks):**
```powershell
cd D:\CodingProject\blindfold-chess-trainer
powershell -ExecutionPolicy Bypass -File .\scripts\setup-windows.ps1
```

### Secondary workspace (WSL — optional)

```
/home/thomas/Projects/blindfold-chess-trainer
```

Same git repo origin; may be out of sync with `D:\` if not merged regularly.  
WSL requires Linux SDK (`/home/thomas/Android/Sdk`) + WSL JDK in Android Studio. More friction — avoid unless needed for Docker/terminal.

`settings.gradle.kts` auto-fixes `local.properties` to Linux SDK path **only when Gradle runs on Linux**.

### Useful scripts

| Script | Purpose |
|---|---|
| `scripts/setup-windows.ps1` | Fix SDK + Gradle JDK for Windows |
| `scripts/fix-local-properties.sh` | Fix SDK path for WSL |
| `scripts/fix-build-permissions.sh` | Fix root-owned `build/` after Docker |
| `scripts/diagnose-sync.sh` | Debug Gradle sync from terminal |
| `preview/index.html` | Quick UI preview in browser |

---

## Key Decisions Made

- [x] **Kotlin + Jetpack Compose** (not Flutter, not KMP for now)
- [x] **Global board** via `AppShell` — visible on all screens, optional hide
- [x] **Fixed board layout** — coordinates toggle without resizing the grid
- [x] **File notation bottom only** — no a–h row above the board
- [x] **Windows `D:\` as primary dev path** — simpler Android Studio workflow
- [x] **chesslib** added to `core:chess` for future game/puzzle logic
- [ ] Hidden board state for blindfold mode (FEN + mental tracking)
- [ ] Stockfish strength levels
- [ ] Voice input optional

---

## MVP Roadmap (priority order)

1. ~~Visualization drill: square colors~~ ✅
2. Visualization drills: square names, diagonals, knight tours
3. Blindfold puzzles
4. Blindfold games vs Stockfish (board hidden or optional reference board)
5. Room: gentle session history + stats (no streaks)
6. PGN import (post-MVP)

---

## Next Session — Suggested Tasks

Pick one feature at a time:

1. **Pieces on the board** — render starting position (optional toggle alongside coordinates)
2. **Square name drill** — show coordinate, user types or picks the square
3. **Sync `D:\` ↔ git** — commit current `D:\` state to `main` so WSL/CI match
4. **Room setup** — save drill scores per session
5. **Board orientation toggle** — flip white/black perspective

---

## Rules for Working with Grok

- Paste relevant sections of this file at the start of important messages.
- **Modify code on `D:\CodingProject\blindfold-chess-trainer`** unless explicitly told otherwise.
- One feature at a time; no scope creep.
- No dark patterns, no streaks, no pressure UI.
- Run/build yourself; don't just tell the user what to run.

---

## Troubleshooting Quick Reference

| Symptom | Fix |
|---|---|
| Gradle sync « fail » (WSL project) | Wrong SDK/JDK mix — see README |
| `javaHome invalid … wsl.localhost` on `D:\` | Gradle JDK must be `jbr-21`, not WSL |
| Board not visible after changes | Ensure editing `D:\` copy, then Run ▶ |
| `local.properties` wrong SDK | Run `setup-windows.ps1` on Windows |
| Docker broke local build | `sudo ./scripts/fix-build-permissions.sh` (WSL only) |

Full details: `README.md`