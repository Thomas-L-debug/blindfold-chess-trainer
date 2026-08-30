# Blindfold Chess Trainer — PROJECT CONTEXT

**Last updated:** August 30, 2026

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

## Current Status (August 30, 2026)

### Done

| Area | Status | Notes |
|---|---|---|
| Android project (Kotlin + Compose) | ✅ | AGP 8.9.1, Kotlin 2.1.10, compileSdk 35 |
| Module `core:chess` | ✅ | Square, SquareColor, PieceType (B/N/R/Q), drills + unit tests |
| Module `app` | ✅ | UI Compose, dark calm theme |
| Drill « Square Colors » | ✅ | Random square → Light / Dark ; board flash green/red 0.5 s |
| Drill « Piece Path » | ✅ | Choose piece, start → target, file-then-rank pad ; illegal move resets |
| Chess board UI | ✅ | Compact square (not half the screen), global via `AppShell` |
| Coordinates | ✅ | Ranks left, files bottom only ; grid does not jump |
| Arrows | ✅ | Sunset-orange path arrows + center circles on visited squares |
| Hide / Show board | ✅ | Hide = small button bottom-right **of the board panel** ; Show = full-width when hidden |
| CI GitHub Actions | ✅ | `.github/workflows/ci.yml` — `test` + `assembleDebug` on push/PR `main` |
| Docker / Windows scripts | ✅ | Optional Docker ; primary workspace is Windows `D:\` |
| Web preview (`preview/`) | ⚠️ | Square Colors demo only — **stale** vs real app |

### Not started yet

- Piece sprites / glyphs on the board
- Start / target markers on Piece Path (besides arrows after moves)
- More visualization drills (square names, diagonals, dedicated knight tours)
- Blindfold puzzles
- Stockfish / blindfold games
- Room (session history, progress)
- ktlint / detekt
- Voice input
- Board orientation (black's side)

---

## Architecture

```
MainActivity
└── AppScreen: Home | SquareColor | PiecePath
    └── AppShell                 ← board (wrap height), then page content
        ├── BoardPanel           ← ChessBoard + Coordinates / Arrows / Hide
        └── content slot
            ├── HomeScreen
            ├── SquareColorDrillScreen
            └── PiecePathDrillScreen
```

**Modules:**
- `:app` — UI Compose, features, theme
- `:core:chess` — chess logic (no UI). `chesslib` 1.3.3 on the classpath, **not used in code yet**

**Patterns in use:**
- ViewModel + StateFlow for drills
- `rememberSaveable` for board/navigation state in `MainActivity`
- Highlight / arrows lifted to `MainActivity`, passed into `AppShell`
- Feature folders: `feature/home`, `feature/drills`, `feature/board`
- Empty-board move legality in `PieceType.canMove` (no blocking pieces)

**Key files:**
```
app/src/main/kotlin/com/blindfoldchess/trainer/
├── MainActivity.kt
├── feature/
│   ├── board/
│   │   ├── AppShell.kt
│   │   ├── BoardPanel.kt      # height = board square; Hide in panel corner
│   │   └── ChessBoard.kt      # highlight, arrows, waypoint circles
│   ├── home/HomeScreen.kt
│   └── drills/
│       ├── SquareColorDrillScreen.kt / SquareColorDrillViewModel.kt
│       └── PiecePathDrillScreen.kt / PiecePathDrillViewModel.kt
└── ui/theme/

core/chess/src/main/kotlin/.../
├── Square.kt
├── SquareColor.kt
├── SquareColorDrill.kt
├── PieceType.kt
└── PiecePathDrill.kt
```

**Tests:**
- `core/chess` — `SquareColorTest`, `PieceMovesTest`
- `app` — `PiecePathDrillViewModelTest`, `BoardArrowTest`

---

## UI Behaviour (board)

- **Board zone height** = chessboard square (width minus 72 dp side column). Must **not** use `fillMaxHeight()` in a way that expands to the phone screen.
- **Side column:** Coordinates, Arrows, then **Hide board** at the bottom of that column (bottom-right of the **board panel**, not the phone).
- **Show board:** full-width outlined button above content, only when the board is hidden.
- **Coordinates:** ranks 1–8 left, files a–h bottom only. Notation slot is always reserved (grid does not resize).
- **Arrows:** consecutive legal moves of the current Piece Path attempt. Illegal reset clears arrows. Square Colors has none.
- **Answer flash:** green / red overlay 0.5 s on the relevant square.
- Board shows empty squares (no piece glyphs yet). Colors use `SquareColor.of()`.

---

## Piece Path rules

- Pieces: bishop, knight, rook, queen (default knight).
- Empty board: bishop diagonal, rook rank/file, queen both, knight L.
- Bishop puzzles stay on the same square color (otherwise impossible).
- Input: tap file (a–h, two columns of four), then rank (1–8, two columns of four, spaced to the right).
- Illegal move: immediate message, flash red, **same** start/target, piece back to start, path cleared.
- Reaching target: success, Next for a new puzzle.

---

## Tech Stack (confirmed)

| Layer | Choice |
|---|---|
| Mobile UI | Kotlin + Jetpack Compose (Material 3, dark theme) |
| Chess lib | `com.github.bhlangonijr:chesslib:1.3.3` (dependency only) |
| State | ViewModel + Kotlin Flow |
| DB (future) | Room |
| Engine (future) | Stockfish via JNI |
| Tests | JUnit in `:core:chess` and `:app` |
| CI | GitHub Actions `.github/workflows/ci.yml` |
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
- Repo-local `core.filemode=false` (Windows). `.gitattributes` forces LF.

**Run app:** open project on `D:\` → **Run ▶** on module `app`.

```powershell
cd D:\CodingProject\blindfold-chess-trainer
powershell -ExecutionPolicy Bypass -File .\scripts\setup-windows.ps1
```

### Secondary workspace (WSL — optional)

```
/home/thomas/Projects/blindfold-chess-trainer
```

Avoid unless needed for Docker/terminal. `settings.gradle.kts` auto-fixes `local.properties` only when Gradle runs on Linux.

---

## Key Decisions Made

- [x] **Kotlin + Jetpack Compose** (not Flutter, not KMP for now)
- [x] **Global board** via `AppShell` — optional hide
- [x] **Compact board** — wrap to square; never half-screen weight
- [x] **Hide inside board panel** — Show stays full-width when hidden
- [x] **File notation bottom only**
- [x] **Windows `D:\` as primary dev path**
- [x] **Empty-board legality** in `core:chess` (no chesslib yet)
- [x] **chesslib** on Gradle for future game/puzzle logic
- [ ] Hidden board state for blindfold mode (FEN + mental tracking)
- [ ] Pieces rendered on squares
- [ ] Stockfish strength levels
- [ ] Voice input optional

---

## MVP Roadmap (priority order)

1. ~~Visualization drill: square colors~~ ✅
2. ~~Piece path drill (B/N/R/Q)~~ ✅
3. Pieces (or at least the moving piece) on the board
4. Visualization drills: square names, diagonals
5. Blindfold puzzles
6. Blindfold games vs Stockfish (board hidden or optional reference board)
7. Room: gentle session history + stats (no streaks)
8. PGN import (post-MVP)

---

## Next Session — Suggested Tasks

Pick **one** feature at a time:

1. **Draw the piece on the current square** during Piece Path (biggest UX gap).
2. **Mark start and target** on the board for the whole puzzle (even before the first move).
3. **Square name drill** — reuse the coordinate pad.
4. **Board orientation** — flip white/black.
5. **Cap Piece Path distance** (e.g. knight 1–3 moves) so blindfold attempts stay fair.
6. Do **not** start Room or Stockfish until 2–3 drills feel solid.
7. `preview/index.html` is stale — update or ignore.

---

## Rules for Working with Grok

- Read this file and the status section of `README.md` at the start of a session.
- **Modify code on `D:\CodingProject\blindfold-chess-trainer`** unless explicitly told otherwise.
- One feature at a time; no scope creep.
- No dark patterns, no streaks, no pressure UI.
- Run/build yourself; don't just tell the user what to run.
- Board panel height must stay the chessboard square — do not `fillMaxHeight()` it to the phone.

---

## Troubleshooting Quick Reference

| Symptom | Fix |
|---|---|
| Gradle sync « fail » (WSL project) | Wrong SDK/JDK mix — see README |
| `javaHome invalid … wsl.localhost` on `D:\` | Gradle JDK must be `jbr-21`, not WSL |
| Board not visible after changes | Ensure editing `D:\` copy, then Run ▶ |
| Board / Hide covers the whole screen | BoardPanel height must be wrap/square, not max parent height |
| `local.properties` wrong SDK | Run `setup-windows.ps1` on Windows |
| Docker broke local build | `sudo ./scripts/fix-build-permissions.sh` (WSL only) |

Full details: `README.md`
