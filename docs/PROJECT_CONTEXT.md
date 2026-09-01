# Blindfold Chess Trainer — PROJECT CONTEXT

**Last updated:** September 1, 2026

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

## ⚠️ Working-tree note (2026-09-01)

Everything below the last commit (`effded9`, *"Update README and project context for the current app state"*) is currently **uncommitted** — chesslib migration, pieces on the board, 4 new drills, the native Stockfish engine, and `LICENSE`/`NOTICE`. It's all in the working tree on `D:\CodingProject\blindfold-chess-trainer` and verified building/passing tests, but nothing has been committed yet. Commit in logical chunks before doing more work on top.

---

## Current Status (September 1, 2026)

### Done

| Area | Status | Notes |
|---|---|---|
| Android project (Kotlin + Compose) | ✅ | AGP 8.9.1, Kotlin 2.1.10, compileSdk 35, NDK 28.2.13676358 |
| Module `core:chess` | ✅ | Now backed by `chesslib` end-to-end (rules, SAN, legality) via `ChessSession` |
| Module `app` | ✅ | UI Compose, dark calm theme |
| Home screen | ✅ | 6 drill cards, scrollable |
| Drill « Find the Square » | ✅ | Coordinate shown → tap the matching square on the board |
| Drill « Square Colors » | ✅ | Random square → Light / Dark ; board flash green/red 0.5 s |
| Drill « Piece Path » | ✅ | Choose piece, start → target, file-then-rank pad ; illegal move resets |
| Drill « Famous Games » | ✅ | Browse 6 historical games, play each move on the board, step navigation |
| Drill « Free Board » | ✅ | Free legal play, notation pad (file/rank, capture, castle), disambiguation, undo/reset |
| Mode « Play the Bot » | ✅ | Full game vs. Stockfish, 7 Elo levels (1350–2850), choose color |
| Pieces rendered on the board | ✅ | Unicode glyphs, toggle in board panel |
| Board flip (white/black side) | ✅ | Toggle in board panel |
| Native Stockfish engine | ✅ | Vendored sf_15 source, compiled via NDK/CMake to `libstockfishjni.so`, UCI over JNI pipes |
| Chess board UI | ✅ | Compact square (not half the screen), global via `AppShell` |
| Coordinates | ✅ | Ranks left, files bottom only ; grid does not jump |
| Arrows | ✅ | Sunset-orange path arrows + center circles on visited squares |
| Hide / Show board | ✅ | Hide = small button bottom-right **of the board panel** ; Show = full-width when hidden |
| LICENSE / NOTICE | ✅ | GPLv3 `LICENSE` + `NOTICE` added (required — Stockfish is GPLv3, vendored and statically linked) |
| CI GitHub Actions | ✅ | `.github/workflows/ci.yml` — installs NDK 28.2.13676358 + CMake 3.22.1, `test` + `assembleDebug`, timeout 50 min |
| Docker / Windows scripts | ✅ | Optional Docker ; primary workspace is Windows `D:\` |
| Tests | ✅ | 91 JVM unit tests, all passing (`:core:chess:test` + `:app:testDebugUnitTest`) |
| Web preview (`preview/`) | ⚠️ | Square Colors demo only — **stale**, does not reflect any drill added since |

### Not started yet

- More visualization drills (square names as a distinct drill from Find the Square, diagonals, knight tours)
- Room (session history, progress) — no persistence yet, everything resets on process death
- ktlint / detekt
- Voice input
- iOS / KMP

**Deliberately out of scope:** a tactical "blindfold puzzle" drill (isolated positions) — decided against, no clear value over the existing drills (Free Board / Famous Games / Play the Bot already cover applied visualization).

---

## Architecture

```
MainActivity
└── AppScreen: Home | FindSquare | SquareColor | PiecePath | FamousGames | FreeBoard | PlayBot
    └── AppShell                 ← board (wrap height), then page content
        ├── BoardPanel           ← ChessBoard + Coordinates / Arrows / Pieces / Flip / Hide
        └── content slot
            ├── HomeScreen
            ├── FindSquareScreen
            ├── SquareColorDrillScreen
            ├── PiecePathDrillScreen
            ├── FamousGamesScreen
            ├── FreeBoardScreen
            └── PlayBotScreen
```

**Modules:**
- `:app` — UI Compose, features, theme, chess engine bridge (Kotlin + native)
- `:core:chess` — chess logic (no UI), fully on `chesslib` 1.3.3

**Patterns in use:**
- ViewModel + StateFlow for drills; `FreeBoardViewModel` is `open` and subclassed by `PlayBotViewModel` (adds bot turn handling, Elo, color)
- `ChessSession` (core:chess) wraps a `chesslib` `Board` and exposes SAN/UCI/square-pair move APIs, ply-indexed history, undo, and disambiguation
- `rememberSaveable` for board/navigation state in `MainActivity`
- Highlight / arrows / pieces / selected square lifted to `MainActivity`, passed into `AppShell`
- Feature folders: `feature/home`, `feature/drills`, `feature/board`
- Native chess engine behind a `ChessEngine` interface (`engine/`), implemented by `StockfishChessEngine` talking UCI to the JNI bridge

**Key files:**
```
app/src/main/kotlin/com/blindfoldchess/trainer/
├── MainActivity.kt
├── engine/
│   ├── ChessEngine.kt           # interface + UCI helpers (parseBestMove, Elo→movetime)
│   ├── NativeStockfish.kt       # JNI externals (startEngine/sendCommand/readLine)
│   └── StockfishChessEngine.kt  # UCI session over the native bridge
├── feature/
│   ├── board/
│   │   ├── AppShell.kt
│   │   ├── BoardPanel.kt      # height = board square; toggles: Hide, Flip, Coordinates, Arrows, Pieces
│   │   └── ChessBoard.kt      # squares, pieces (glyphs), highlight, arrows, waypoint circles, flip
│   ├── home/HomeScreen.kt
│   └── drills/
│       ├── CoordinatePad.kt          # shared file/rank input pad
│       ├── DrillBackButton.kt        # shared back-to-home button
│       ├── FindSquareScreen.kt / FindSquareViewModel.kt
│       ├── SquareColorDrillScreen.kt / SquareColorDrillViewModel.kt
│       ├── PiecePathDrillScreen.kt / PiecePathDrillViewModel.kt
│       ├── FamousGamesScreen.kt / FamousGamesViewModel.kt
│       ├── FreeBoardScreen.kt / FreeBoardViewModel.kt / FreeBoardPlayPad.kt
│       └── PlayBotScreen.kt / PlayBotViewModel.kt   # extends FreeBoardViewModel
└── ui/theme/

app/src/main/cpp/                # vendored Stockfish sf_15 + JNI bridge (see Tech Stack)
├── CMakeLists.txt
├── bridge.cpp                    # JNI glue: pipes stdin/stdout of UCI::loop
└── stockfish/                    # upstream source, unmodified

core/chess/src/main/kotlin/.../
├── Square.kt / SquareColor.kt / SquareColorDrill.kt
├── PieceType.kt                  # ChessMan-facing move helpers (still used by drills)
├── PiecePathDrill.kt
├── FindSquareDrill.kt
├── OccupiedSquare.kt              # board occupancy snapshot type
├── ChessSession.kt                # chesslib-backed session: SAN/UCI/square play, history, undo
├── ChesslibMapping.kt             # internal Square/Move/PieceType <-> chesslib mapping
├── FamousGame.kt / FamousGamesCatalog.kt   # 6 historical games (Opera, Légal's Mate, Immortal, Evergreen, Réti, Game of the Century)
└── GameFollowDrill.kt             # parses a FamousGame into replayable positions/moves
```

**Tests (91 total, all passing):**
- `core:chess` (40 tests) — `SquareColorTest`, `PieceMovesTest`, `FindSquareDrillTest`, `ChessSessionTest`, `GameFollowDrillTest`
- `app` (51 tests) — `BoardArrowTest`, `PiecePathDrillViewModelTest`, `FindSquareViewModelTest`, `FamousGamesViewModelTest`, `FreeBoardViewModelTest`, `PlayBotViewModelTest`
- Run with: `./gradlew :core:chess:test :app:testDebugUnitTest`
- A full `assembleDebug` (including the native Stockfish build) has succeeded on this machine — `app/build/outputs/apk/debug/app-debug.apk` and `libstockfishjni.so` (arm64-v8a, x86_64) exist in `app/build/`.

---

## UI Behaviour (board)

- **Board zone height** = chessboard square (width minus 72 dp side column). Must **not** use `fillMaxHeight()` in a way that expands to the phone screen.
- **Side column (top to bottom):** Hide board, Flip, Coordinates, Arrows, Pieces.
- **Show board:** full-width outlined button above content, only when the board is hidden.
- **Coordinates:** ranks 1–8 left, files a–h bottom only. Notation slot is always reserved (grid does not resize). Flips with the board.
- **Pieces:** Unicode glyphs (♔♕♖♗♘♙), drawn from the current `OccupiedSquare` list; toggle hides them without losing board state.
- **Flip:** swaps which side is at the bottom; applies to squares, pieces, arrows, highlight, and the tap overlay.
- **Arrows:** consecutive legal moves of the current attempt/replay. Illegal reset clears arrows. Square Colors and Find the Square have none.
- **Answer flash:** green / red overlay 0.5 s on the relevant square.
- **Tap input:** `Free Board`, `Famous Games`, and `Play the Bot` also accept direct square taps (`onSquareClick`) in addition to the coordinate pad.

---

## Drill / mode rules

**Piece Path** — Pieces: bishop, knight, rook, queen (default knight). Empty-board legality via `PieceType.canMove`. Bishop puzzles stay on the same square color. Input: tap file then rank. Illegal move: immediate message, flash red, same start/target, piece back to start, path cleared. Reaching target: success, Next for a new puzzle.

**Find the Square** — inverse of Square Colors: a coordinate is shown, the player taps that square directly on the board.

**Famous Games** — pick a game from the library (title, players, event, year, result, move count); the app steps through it move by move, and the player must tap the piece and destination for the *given* move before it advances. Wrong attempt: message + retry, no penalty.

**Free Board** — full legal chess from the start position (or any loaded FEN via `ChessSession`). Input via `CoordinatePad` (file → rank, capture toggle, castle buttons) or direct board taps; ambiguous moves prompt a file/rank disambiguation chip row. Step back/forward through history, undo the last move, reset the game. Check/checkmate/stalemate are surfaced in the UI.

**Play the Bot** — same engine as Free Board (`PlayBotViewModel extends FreeBoardViewModel`), plus: pick Elo (1350/1500/1700/1900/2100/2300/2500) and color before starting, bot move requested automatically via `StockfishChessEngine.bestMove(fen, elo)` on the bot's turn, "thinking…" / "bot could not move" states surfaced in the description line.

---

## Tech Stack (confirmed)

| Layer | Choice |
|---|---|
| Mobile UI | Kotlin + Jetpack Compose (Material 3, dark theme) |
| Chess rules | `com.github.bhlangonijr:chesslib:1.3.3` — used throughout `core:chess` (`ChessSession`) |
| Chess engine | Stockfish sf_15, vendored C++ source in `app/src/main/cpp/stockfish`, compiled to a JNI shared lib via CMake/NDK; driven over UCI through pipes (`bridge.cpp` / `NativeStockfish` / `StockfishChessEngine`) |
| State | ViewModel + Kotlin Flow |
| DB (future) | Room — not started |
| Tests | JUnit in `:core:chess` and `:app` (91 tests) |
| CI | GitHub Actions `.github/workflows/ci.yml` — installs NDK 28.2.13676358 + CMake 3.22.1 for the native build |
| DevOps optional | Docker + `docker-compose.yml` |
| Licensing | GPLv3 (`LICENSE`), required by the vendored Stockfish; `NOTICE` documents Stockfish (GPLv3) and chesslib (Apache 2.0) provenance. App is intended for **free Play Store distribution** — publishing the APK is "conveying" under GPLv3 §6, so the repo must become public (or an equivalent written source offer) before release, plus an in-app license/source link |

**Versions** (`gradle/libs.versions.toml`):
- AGP 8.9.1, Kotlin 2.1.10, Compose BOM 2025.02.00
- minSdk 26, targetSdk 35, JDK 21
- NDK 28.2.13676358, CMake 3.22.1 (native build only, needed for `assembleDebug`/`installDebug`, not for `test`)

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
- Native build needs the NDK side-by-side package `28.2.13676358` and CMake `3.22.1` installed via the SDK Manager (Android Studio installs these automatically on first native sync/build).

**Run app:** open project on `D:\` → **Run ▶** on module `app`.

```powershell
cd D:\CodingProject\blindfold-chess-trainer
powershell -ExecutionPolicy Bypass -File .\scripts\setup-windows.ps1
```

**Gotcha — running Gradle from Git Bash on Windows:** `./gradlew` fails with `JAVA_HOME is not set and no 'java' command could be found` unless `JAVA_HOME` is exported for that shell. Android Studio's bundled JBR works:
```bash
JAVA_HOME="C:\Program Files\Android\Android Studio\jbr" ./gradlew test
```
(PowerShell / Android Studio itself don't need this — they already resolve a JDK.)

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
- [x] **chesslib as the sole rules engine** in `core:chess` (superseded the empty-board-only legality)
- [x] **Pieces rendered on squares** — Unicode glyphs, toggleable
- [x] **Board orientation (flip)** — implemented
- [x] **Stockfish vendored + compiled natively** (NDK/CMake, JNI/UCI bridge) — powers Play the Bot
- [x] **GPLv3 licensing** adopted (forced by vendoring/statically-linking Stockfish)
- [ ] Hidden board state for blindfold mode (FEN + mental tracking without visual aid)
- [ ] Voice input optional
- [ ] Session persistence (Room)

---

## MVP Roadmap (priority order)

1. ~~Visualization drill: square colors~~ ✅
2. ~~Piece path drill (B/N/R/Q)~~ ✅
3. ~~Pieces (or at least the moving piece) on the board~~ ✅
4. ~~Find the Square drill~~ ✅
5. ~~Famous games (guided replay)~~ ✅
6. ~~Free board + Play vs. Stockfish~~ ✅
7. Make the repo public (or written source offer) + in-app license/source link — required by GPLv3 before Play Store publication
8. Room: gentle session history + stats (no streaks)
9. PGN import (post-MVP)

---

## Next Session — Suggested Tasks

Pick **one** feature at a time:

1. **Commit the current working tree** in logical chunks (chesslib migration / pieces+flip / new drills / native Stockfish / LICENSE) before adding more scope.
2. **Make the repo public (or written source offer) + in-app license/source link** — required by GPLv3 before publishing on the Play Store; see the Licensing row above.
3. **True blindfold mode** — a way to hide the board mid-drill/game while still tracking the position mentally (distinct from the existing Hide-board toggle, which currently just stops rendering, not a graded blindfold challenge).
4. **Room** — gentle session history (no streaks), once the current drill set feels stable.
5. **Square names drill** — distinct from Find the Square (name a shown square, rather than tap a named one), reusing `CoordinatePad`.
6. **Cap Piece Path / Find the Square difficulty** (e.g. knight 1–3 moves) so blindfold attempts stay fair.
7. Add `ktlint`/`detekt`.
8. `preview/index.html` is stale — update or remove it now that there are 6 drills, not 1.

Not planned: a tactical "blindfold puzzle" drill — deliberately dropped, see "Deliberately out of scope" above.

---

## Rules for Working with Grok

- Read this file and the status section of `README.md` at the start of a session.
- **Modify code on `D:\CodingProject\blindfold-chess-trainer`** unless explicitly told otherwise.
- One feature at a time; no scope creep.
- No dark patterns, no streaks, no pressure UI.
- Run/build yourself; don't just tell the user what to run.
- Board panel height must stay the chessboard square — do not `fillMaxHeight()` it to the phone.
- Commit at natural checkpoints rather than letting multiple features pile up uncommitted (see the working-tree note at the top of this file).

---

## Troubleshooting Quick Reference

| Symptom | Fix |
|---|---|
| Gradle sync « fail » (WSL project) | Wrong SDK/JDK mix — see README |
| `javaHome invalid … wsl.localhost` on `D:\` | Gradle JDK must be `jbr-21`, not WSL |
| `JAVA_HOME is not set` running `./gradlew` from Git Bash | Export it: `JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"` (see Development Environment) |
| Board not visible after changes | Ensure editing `D:\` copy, then Run ▶ |
| Board / Hide covers the whole screen | BoardPanel height must be wrap/square, not max parent height |
| `local.properties` wrong SDK | Run `setup-windows.ps1` on Windows |
| Native build fails / missing NDK | Install NDK `28.2.13676358` + CMake `3.22.1` via SDK Manager (Android Studio does this automatically on sync) |
| Docker broke local build | `sudo ./scripts/fix-build-permissions.sh` (WSL only) |

Full details: `README.md`
