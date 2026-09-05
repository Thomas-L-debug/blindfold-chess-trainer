# Blindfold Chess Trainer — PROJECT CONTEXT

**Last updated:** September 5, 2026

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

## ⚠️ Working-tree note (2026-09-05)

Last commit on `main`: `520f4ca` (*update readme et project_context*). Uncommitted on top: voice input, `ActionCard` home/Famous Games cards, board show/hide rules, Play the Bot resume/Play again/last-move UI, `ChessSession.playSan` legal-move gate, speech parser (FR/EN, pawn takes, S→F). Commit before piling more work.

---

## Current Status (September 5, 2026)

### Done

| Area | Status | Notes |
|---|---|---|
| Android project (Kotlin + Compose) | ✅ | AGP 8.9.1, Kotlin 2.1.10, compileSdk 35, NDK 28.2.13676358 |
| Module `core:chess` | ✅ | `chesslib` via `ChessSession` ; SAN only applied if it matches `legalMoves()` |
| Module `app` | ✅ | UI Compose, dark calm theme |
| Home screen | ✅ | 6 shared `ActionCard`s |
| Drill « Find the Square » | ✅ | Coordinate → tap square ; **board forced visible** on start |
| Drill « Square Colors » | ✅ | Random square → Light / Dark ; flash 0.5 s |
| Drill « Piece Path » | ✅ | B/N/R/Q ; pad **or voice** ; illegal → reset + message ; unclear speech → message, no reset |
| Drill « Famous Games » | ✅ | 6 games as home-style cards ; **Play this game** shows the board if hidden |
| Drill « Free Board » | ✅ | Legal play, pad, taps, **voice** ; illegal move rejected, last legal position kept |
| Mode « Play the Bot » | ✅ | Stockfish, Elo 1350–2500 ; Continue/Discard if a game is in progress ; last bot move large ; Play again |
| Voice input | ✅ | Android `SpeechRecognizer` (free) ; Speak + FR/EN ; Free Board, Play the Bot, Piece Path |
| Pieces / Flip / Arrows / Coordinates | ✅ | Unicode glyphs, orientation, path arrows |
| Board visibility | ✅ | Hidden at launch and on Home ; Find the Square + Famous Games play open it |
| Native Stockfish | ✅ | Vendored sf_15, JNI/UCI |
| LICENSE / NOTICE | ✅ | GPLv3 (Stockfish) |
| CI | ✅ | NDK + CMake, tests + APK debug |
| Docker / Windows scripts | ✅ | Primary workspace Windows `D:\` |
| Tests | ✅ | **116** JVM unit tests, all passing |
| Web preview (`preview/`) | ⚠️ | Square Colors only — stale |

### Not started yet

- More visualization drills (square names as a distinct drill from Find the Square, diagonals, knight tours)
- Room (session history, progress) — voice language is the only persistence (SharedPreferences)
- ktlint / detekt
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
- Shared `ActionCard` for home drills and Famous Games library
- Voice: `SpeechRecognizer` + `ChessSpeechParser` (no paid STT)

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
│       ├── CoordinatePad.kt / DrillBackButton.kt
│       ├── VoiceMoveInput.kt        # SpeechRecognizer, FR/EN toggle, Speak row
│       ├── FindSquareScreen.kt / FindSquareViewModel.kt
│       ├── SquareColorDrillScreen.kt / SquareColorDrillViewModel.kt
│       ├── PiecePathDrillScreen.kt / PiecePathDrillViewModel.kt
│       ├── FamousGamesScreen.kt / FamousGamesViewModel.kt
│       ├── FreeBoardScreen.kt / FreeBoardViewModel.kt / FreeBoardPlayPad.kt
│       └── PlayBotScreen.kt / PlayBotViewModel.kt
└── ui/ActionCard.kt + ui/theme/

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
├── FamousGame.kt / FamousGamesCatalog.kt
├── GameFollowDrill.kt
└── ChessSpeechParser.kt           # FR/EN speech → SAN / Piece Path square (S→F, pawn takes dest, castle aliases)
```

**Tests (116 total, all passing):**
- `core:chess` (50) — `ChessSessionTest` (18), `ChessSpeechParserTest` (8), `GameFollowDrillTest` (8), `PieceMovesTest` (7), `SquareColorTest` (6), `FindSquareDrillTest` (3)
- `app` (66) — `FreeBoardViewModelTest` (21), `FamousGamesViewModelTest` (16), `PiecePathDrillViewModelTest` (9), `PlayBotViewModelTest` (8), `FindSquareViewModelTest` (6), `BoardArrowTest` (5), `ParseBestMoveTest` (1)
- Run: `./gradlew :core:chess:testDebugUnitTest :app:testDebugUnitTest`

---

## UI Behaviour (board)

- **Board zone height** = chessboard square (width minus 72 dp side column). Must **not** use `fillMaxHeight()` in a way that expands to the phone screen.
- **Side column (top to bottom):** Hide board, Flip, Coordinates, Arrows, Pieces.
- **Show board:** full-width outlined button above content, only when the board is hidden.
- **Default hidden:** board starts hidden ; going Home hides it again. Find the Square start and Famous Games « Play this game » set it visible (no-op if already shown).
- **Coordinates:** ranks 1–8 left, files a–h bottom only. Notation slot is always reserved (grid does not resize). Flips with the board.
- **Pieces:** Unicode glyphs (♔♕♖♗♘♙), drawn from the current `OccupiedSquare` list; toggle hides them without losing board state.
- **Flip:** swaps which side is at the bottom; applies to squares, pieces, arrows, highlight, and the tap overlay.
- **Arrows:** consecutive legal moves of the current attempt/replay. Illegal reset clears arrows. Square Colors and Find the Square have none.
- **Answer flash:** green / red overlay 0.5 s on the relevant square.
- **Tap input:** `Free Board`, `Famous Games`, and `Play the Bot` also accept direct square taps (`onSquareClick`) in addition to the coordinate pad.
- **Voice:** Speak + FR/EN on Free Board, Play the Bot, and Piece Path (`VoiceMoveInput.kt` + `ChessSpeechParser`).

---

## Drill / mode rules

**Piece Path** — Bishop, knight, rook, queen (default knight). Empty-board legality via `PieceType.canMove`. Input: file then rank **or Speak**. Saying a piece name + square is preferred (`cavalier f 3`) ; a bare square (`h6`, `S5`→f5) uses the selected piece. Illegal move: *Illegal move — starting over*, path reset. Unclear speech (`P5`): *Couldn't understand that move*, position unchanged.

**Find the Square** — a coordinate is shown, the player taps that square. Starting the drill shows the board.

**Famous Games** — library as home-style cards. **Play this game** shows the board if hidden, then the player taps the given move's from/to. Wrong attempt: retry, no penalty.

**Free Board** — legal chess. Pad (lowercase files, 50% frame opacity), board taps, or **Speak** (FR/EN). Status line: `White to move - NF3`. Illegal SAN/tap/voice: rejected, last legal position kept (`ChessSession.playSan` gated on `legalMoves()`). Voice: `e4`, `knight f3`, `pion prend F4` / `pawn takes F4`, `petit rock` / `castle`, `grand rock` / `long castle`. English STT `S5` is accepted as **f5**.

**Play the Bot** — `PlayBotViewModel` extends `FreeBoardViewModel`, **one** engine instance. Setup Elo (1350–2500) + color. Re-entering with a live game: Continue / Discard. Last bot move shown large above the pad. Thinking on the side-to-move line. Checkmate / Stalemate (short labels) + **Play again** on the same row. Voice same as Free Board.

**Voice (shared)** — Android `SpeechRecognizer`, no paid API. `RECORD_AUDIO`. Speak + FR/EN chips (persisted in SharedPreferences). Parser: `ChessSpeechParser`.

---

## Tech Stack (confirmed)

| Layer | Choice |
|---|---|
| Mobile UI | Kotlin + Jetpack Compose (Material 3, dark theme) |
| Chess rules | `com.github.bhlangonijr:chesslib:1.3.3` — used throughout `core:chess` (`ChessSession`) |
| Chess engine | Stockfish sf_15, vendored C++ source in `app/src/main/cpp/stockfish`, compiled to a JNI shared lib via CMake/NDK; driven over UCI through pipes (`bridge.cpp` / `NativeStockfish` / `StockfishChessEngine`) |
| State | ViewModel + Kotlin Flow |
| DB (future) | Room — not started |
| Tests | JUnit in `:core:chess` and `:app` (116 tests) |
| CI | GitHub Actions `.github/workflows/ci.yml` — installs NDK 28.2.13676358 + CMake 3.22.1 for the native build |
| DevOps optional | Docker + `docker-compose.yml` |
| Licensing | GPLv3 (`LICENSE`), required by the vendored Stockfish; `NOTICE` documents Stockfish (GPLv3) and chesslib (Apache 2.0) provenance. App is intended for **free Play Store distribution** — publishing the APK is "conveying" under GPLv3 §6, so recipients must be able to get the source; the GitHub repo is already public, which covers that. Still missing: an in-app license/source link |

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
- [x] **Voice input** — Android SpeechRecognizer, FR/EN, Free Board / Play the Bot / Piece Path
- [ ] Hidden board state for blindfold mode (FEN + mental tracking without visual aid)
- [ ] Session persistence (Room)

---

## MVP Roadmap (priority order)

1. ~~Visualization drill: square colors~~ ✅
2. ~~Piece path drill (B/N/R/Q)~~ ✅
3. ~~Pieces on the board~~ ✅
4. ~~Find the Square drill~~ ✅
5. ~~Famous games (guided replay)~~ ✅
6. ~~Free board + Play vs. Stockfish~~ ✅
7. ~~Voice input (Speak, FR/EN)~~ ✅
8. Add an in-app license/source link — remaining gap before Play Store publication
9. Room: gentle session history + stats (no streaks)
10. PGN import (post-MVP)

---

## Next Session — Suggested Tasks

Pick **one** feature at a time:

1. **Commit** the current uncommitted voice / UI / Play-the-Bot / SAN-legality work.
2. **In-app license/source link** (About screen) before Play Store publication.
3. **True blindfold mode** — hide the board *during* a drill as a graded challenge (distinct from Hide, which only stops rendering).
4. **Room** — gentle session history (no streaks).
5. **Square names drill** — name a shown square, reusing `CoordinatePad`.
6. **Cap Piece Path / Find the Square difficulty** (e.g. knight 1–3 moves).
7. `ktlint` / `detekt`.
8. `preview/index.html` is stale — update or remove.

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
