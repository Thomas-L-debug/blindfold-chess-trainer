# Blindfold Chess Trainer — PROJECT CONTEXT

**Last updated:** September 12, 2026

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

## Current Status (September 12, 2026) — app **0.1.1** (`versionCode` 2)

### Done

| Area | Status | Notes |
|---|---|---|
| Android project (Kotlin + Compose) | ✅ | AGP 8.9.1, Kotlin 2.1.10, compileSdk/targetSdk **36**, NDK 28.2.13676358 |
| Module `core:chess` | ✅ | `chesslib` via `ChessSession` ; SAN only applied if it matches `legalMoves()` |
| Module `app` | ✅ | UI Compose, dark calm theme |
| Home screen | ✅ | 8 shared `ActionCard`s (Notation tutorial + 7 drills) ; FR/EN chips switch the whole app |
| Tutorial « Notation » | ✅ | SAN: read / write / speak ; tap square ; Listen uses TTS ; board stays **hidden** on start |
| Drill « Find the Square » | ✅ | Coordinate → tap square ; **board forced visible** on start |
| Drill « Name the Square » | ✅ | Inverse of Find the Square ; asked square stays **green** ; wrong valid square stays **red** until the next answer ; **board forced visible** on start |
| Drill « Square Colors » | ✅ | Random square → Light / Dark ; flash 0.5 s |
| Drill « Piece Path » | ✅ | B/N/R/Q ; pad **or voice** ; illegal → reset + message ; unclear speech → message, no reset |
| Drill « Famous Games » | ✅ | 6 games as home-style cards ; **Play this game** shows the board if hidden |
| Drill « Free Board » | ✅ | Legal play, pad, taps, **voice** ; illegal/unrecognized speech keeps the transcript until the next try |
| Mode « Play the Bot » | ✅ | Stockfish, Elo 1350–2500 ; Continue/Discard ; last bot move large + spoken ; **Undo last move** takes back player + bot ; **Reset the game** |
| Language | ✅ | Device language at first launch (fr → French, else English) ; FR/EN chips change UI, voice, TTS, tutorial |
| About / license | ✅ | Home link ; GPLv3 notice + GitHub source |
| Privacy policy | ✅ | `docs/PRIVACY.md` (EN+FR) ; About screen + Play URL |
| Play listing copy | ✅ | `docs/PLAY_LISTING.md` (EN+FR texts) |
| Play Store graphics | ✅ | `docs/play-store/` — 512 icon, 1024×500 feature graphic, 9:16 phone screenshots |
| Pieces / Flip / Arrows / Coordinates | ✅ | Full-width board ; Hide / Flip / Coordinates / Arrows / Pieces on **one row under** the board ; coordinates **inside** squares (contrast colors) |
| Board visibility | ✅ | Hidden at launch, Home, and **Notation** ; Find the Square, Name the Square, and Famous Games play open it |
| Native Stockfish | ✅ | Vendored sf_15, JNI/UCI |
| LICENSE / NOTICE | ✅ | GPLv3 (Stockfish) |
| CI | ✅ | NDK + CMake, tests + APK debug |
| Docker / Windows scripts | ✅ | Primary workspace Windows `D:\` |
| Error sound | ✅ | Same short beep on a **wrong move** or a **nonexistent move** ; not when Speak hears nothing |
| Tests | ✅ | **142** JVM unit tests, all passing |
| Web preview (`preview/`) | ⚠️ | Square Colors only — stale |

### Not started yet

- More visualization drills (diagonals, knight tours)
- Room (session history, progress) — app language is the only persistence (SharedPreferences)
- ktlint / detekt
- iOS / KMP

**Deliberately out of scope:** a tactical "blindfold puzzle" drill (isolated positions) — decided against, no clear value over the existing drills (Free Board / Famous Games / Play the Bot already cover applied visualization).

---

## Architecture

```
MainActivity
└── AppScreen: Home | About | NotationTutorial | FindSquare | NameSquare | SquareColor | PiecePath | FamousGames | FreeBoard | PlayBot
    └── AppShell                 ← board (wrap height), then page content
        ├── BoardPanel           ← full-width ChessBoard, then Hide / Flip / Coordinates / Arrows / Pieces on one row
        └── content slot
            ├── HomeScreen / AboutScreen / NotationTutorialScreen
            ├── FindSquareScreen
            ├── NameSquareScreen
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
├── AppLanguage.kt               # FR/EN app locale, prefs, Compose provider
├── engine/
│   ├── ChessEngine.kt           # interface + UCI helpers (parseBestMove, Elo→movetime)
│   ├── NativeStockfish.kt       # JNI externals (startEngine/sendCommand/readLine)
│   └── StockfishChessEngine.kt  # UCI session over the native bridge
├── feature/
│   ├── board/
│   │   ├── AppShell.kt
│   │   ├── BoardPanel.kt      # full-width square board; toggles under it: Hide, Flip, Coordinates, Arrows, Pieces
│   │   └── ChessBoard.kt      # squares, inner coordinates, pieces (glyphs), highlight, arrows, flip
│   ├── home/HomeScreen.kt / AboutScreen.kt / NotationTutorialScreen.kt
│   └── drills/
│       ├── CoordinatePad.kt / DrillBackButton.kt
│       ├── InputMethodControls.kt   # Show/Hide pad, Show/Hide voice
│       ├── VoiceMoveInput.kt        # SpeechRecognizer, FR/EN toggle, Speak row; mute Google beeps
│       ├── DrillErrorSound.kt       # shared error beep (ToneGenerator NACK)
│       ├── SpeechBeepSilencer.kt    # mute SYSTEM/NOTIFICATION/MUSIC while listening
│       ├── FindSquareScreen.kt / FindSquareViewModel.kt
│       ├── NameSquareScreen.kt / NameSquareViewModel.kt
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
├── ChessMoveAnnouncer.kt          # SAN → spoken FR/EN phrase (bot TTS + notation tutorial)
├── NotationTutorial.kt            # bilingual SAN tutorial copy + occupancy labels
└── ChessSpeechParser.kt           # FR/EN speech → SAN / square (S→F, j'ai→G, pawn takes dest, castle aliases)
```

`docs/PRIVACY.md` — Play Store privacy policy (English + French).  
`docs/play-store/` — hi-res icon, feature graphic, 9:16 screenshots, `generate_assets.py`.

**Tests (142 total, all passing):**
- `core:chess` (60) — `ChessSessionTest` (18), `ChessSpeechParserTest` (9), `GameFollowDrillTest` (8), `PieceMovesTest` (7), `SquareColorTest` (6), `ChessMoveAnnouncerTest` (6), `NotationTutorialTest` (3), `FindSquareDrillTest` (3)
- `app` (82) — `FreeBoardViewModelTest` (21), `FamousGamesViewModelTest` (16), `NameSquareViewModelTest` (13), `PiecePathDrillViewModelTest` (9), `PlayBotViewModelTest` (8), `FindSquareViewModelTest` (6), `BoardArrowTest` (6), `AppLanguageTest` (2), `ParseBestMoveTest` (1)
- Run: `./gradlew :core:chess:testDebugUnitTest :app:testDebugUnitTest`

---

## UI Behaviour (board)

- **Board** is **full phone width**, 1:1 aspect ratio (`fillMaxWidth` + `aspectRatio(1f)`). Toggles sit **under** the board, not beside it.
- **Toggle row (left to right):** Hide board, Flip, Coordinates, Arrows, Pieces — one compact row, equal weights.
- **Show board:** full-width outlined button above content, only when the board is hidden.
- **Navigation bar:** `enableEdgeToEdge()` draws under the system nav. `ScreenBottomSpace` at the end of each page adds the nav-bar inset plus 16 dp so About / Back can scroll fully into view.
- **Default hidden:** board starts hidden ; going Home hides it again. **Notation stays hidden.** Find the Square, Name the Square, and Famous Games « Play this game » set it visible (no-op if already shown).
- **Coordinates:** drawn **inside** the squares when the Coordinates toggle is on. Ranks in the top-left of the left file; files in the bottom-right of the bottom rank. Color contrasts with the square (`BoardNotationOnLight` / `BoardNotationOnDark`). Flips with the board. No extra margin for notation.
- **Pieces:** Unicode glyphs (♔♕♖♗♘♙), drawn from the current `OccupiedSquare` list; toggle hides them without losing board state.
- **Flip:** swaps which side is at the bottom; applies to squares, pieces, arrows, highlight, coordinates, and the tap overlay.
- **Arrows:** consecutive legal moves of the current attempt/replay. Illegal reset clears arrows. Square Colors, Find the Square, and Name the Square have none.
- **Answer flash:** Find the Square green / red overlay 0.5 s. Name the Square keeps the **asked square green**; a wrong but **valid** square stays **red** until the next complete answer (text + highlight). Unrecognized speech: no red square, transcript stays.
- **Error sound:** one short notification beep (`playDrillErrorSound`) on a **wrong move** (illegal SAN, wrong square, wrong color) or a **nonexistent move** (heard text that is not a chess move, e.g. `P5`). **Not** played when Speak hears nothing (`ERROR_NO_MATCH` / timeout). Google Speak start/error beeps are muted for the listen (`SpeechBeepSilencer`; `MODIFY_AUDIO_SETTINGS`). Some OEMs may still leak a Google beep.
- **Tap input:** `Free Board`, `Famous Games`, and `Play the Bot` also accept direct square taps (`onSquareClick`) in addition to the coordinate pad. The Notation tutorial taps a square to name and speak it.
- **Voice:** Speak + FR/EN on Free Board, Play the Bot, Piece Path, and Name the Square (`VoiceMoveInput.kt` + `ChessSpeechParser`).
- **Pad / voice visibility:** `InputMethodControls` — *Show pad* / *Hide pad*, *Show voice* / *Hide voice*. **Speak** is the actual talk button, only when voice is shown.

---

## Drill / mode rules

**Piece Path** — Bishop, knight, rook, queen (default knight). Empty-board legality via `PieceType.canMove`. Input: file then rank **or Speak**. Saying a piece name + square is preferred (`cavalier f 3`) ; a bare square (`h6`, `S5`→f5) uses the selected piece. Illegal move: *Illegal move — starting over*, path reset. Unclear speech (`P5`): *Couldn't understand that move*, position unchanged.

**Notation tutorial** — first home card. Board stays **hidden** on start (*Show board* if needed). FR/EN chips (same preference as voice). Sections: squares, piece letters, writing SAN, reading SAN, speaking SAN (Listen via `ChessMoveAnnouncer` + TTS). Tap a square to see/hear its name. Copy lives in `NotationTutorial` (`core:chess`).

**Find the Square** — a coordinate is shown, the player taps that square. Starting the drill shows the board.

**Name the Square** — inverse of Find the Square. Starting the drill shows the board. A random square stays **green**; the player names it with the coordinate pad (file then rank) **or Speak** (`e4`, `e four`, `S5`→f5). Wrong but valid square: stays **red** on the board, compact entered text (`29.sp`), *Not quite — try again*, until the next complete answer. Unclear speech (`P5`): *Couldn't understand that square* + transcript, not counted, no red square. Reuses `FindSquareDrill`. Board can show **two** highlights at once (`List<SquareHighlight>`).

**Famous Games** — library as home-style cards. **Play this game** shows the board if hidden, then the player taps the given move's from/to. Wrong attempt: retry, no penalty.

**Free Board** — legal chess. *Show pad* / *Hide pad*, *Show voice* / *Hide voice*; **Speak** to talk. Status line: `White to move - NF3`. Illegal SAN/tap/voice: rejected, last legal position kept; **heard transcript stays** until the next try. **Undo last move** / **Reset the game**. Voice: `e4`, `knight f3`, `pion prend F4` / `pawn takes F4`, `petit rock` / `castle`, `grand rock` / `long castle`. English STT `S5` is accepted as **f5**.

**Play the Bot** — `PlayBotViewModel` extends `FreeBoardViewModel`, **one** engine instance. Setup Elo (1350–2500) + color. Re-entering with a live game: Continue / Discard. Last bot move shown large above the pad **and spoken** (Android TTS, FR/EN chips; `Nf3` → “cavalier F trois” / “knight F three”). Thinking on the side-to-move line. **Undo last move** takes back the player move **and** the bot reply (disabled while the bot is thinking). **Reset the game** starts over. Checkmate / Stalemate + **Play again**. Voice input same as Free Board.

**Voice (shared)** — Android `SpeechRecognizer`, no paid API. `RECORD_AUDIO` + `MODIFY_AUDIO_SETTINGS`. Speak + FR/EN chips. Language is app-wide (`AppLanguage`, persisted); the same chips on Home, tutorial, and voice rows switch UI strings, STT, and TTS. Parser: `ChessSpeechParser`. During listen, `SpeechBeepSilencer` mutes system/notification/music streams so Google’s start/error beeps stay off; volumes are restored on result, error, cancel, or dispose. Unofficial extra `android.speech.extra.BEEP_ENABLED=false` is also set (ignored on some devices).

**Language** — First launch follows the device (`fr` → French, otherwise English). In-app FR/EN overrides that and is remembered. `values/strings.xml` + `values-fr/strings.xml`.

---

## Tech Stack (confirmed)

| Layer | Choice |
|---|---|
| Mobile UI | Kotlin + Jetpack Compose (Material 3, dark theme) |
| Chess rules | `com.github.bhlangonijr:chesslib:1.3.3` — used throughout `core:chess` (`ChessSession`) |
| Chess engine | Stockfish sf_15, vendored C++ source in `app/src/main/cpp/stockfish`, compiled to a JNI shared lib via CMake/NDK; driven over UCI through pipes (`bridge.cpp` / `NativeStockfish` / `StockfishChessEngine`) |
| State | ViewModel + Kotlin Flow |
| DB (future) | Room — not started |
| Tests | JUnit in `:core:chess` and `:app` (142 tests) |
| CI | GitHub Actions `.github/workflows/ci.yml` — SDK 36, NDK 28.2.13676358 + CMake 3.22.1; tests, debug APK, release AAB + 16 KB check |
| DevOps optional | Docker + `docker-compose.yml` |
| Licensing | GPLv3 (`LICENSE`), required by the vendored Stockfish; `NOTICE` documents Stockfish (GPLv3) and chesslib (Apache 2.0) provenance. In-app About screen links to the public GitHub source and LICENSE. Publishing the AAB is "conveying" under GPLv3 §6. |

**Versions** (`gradle/libs.versions.toml`):
- AGP 8.9.1, Kotlin 2.1.10, Compose BOM 2025.02.00
- minSdk 26, compileSdk/targetSdk 36, JDK 21
- NDK 28.2.13676358, CMake 3.22.1 (native build only, needed for `assembleDebug`/`installDebug`/`bundleRelease`, not for `test`)
- Play upload: signed AAB/APK via `keystore.properties` (gitignored) + `:app:assembleRelease` / `:app:bundleRelease`; `checkReleasePageSize` verifies 16 KB ELF alignment of `.so` files. Current store build: **0.1.1** / `versionCode` 2.

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
- [x] **Full-width board** — 1:1 square, toggles in one row underneath
- [x] **Hide inside board panel** — Show stays full-width when hidden
- [x] **Coordinates inside squares** — contrast colors; no external rank/file strip
- [x] **Windows `D:\` as primary dev path**
- [x] **chesslib as the sole rules engine** in `core:chess` (superseded the empty-board-only legality)
- [x] **Pieces rendered on squares** — Unicode glyphs, toggleable
- [x] **Board orientation (flip)** — implemented
- [x] **Stockfish vendored + compiled natively** (NDK/CMake, JNI/UCI bridge) — powers Play the Bot
- [x] **GPLv3 licensing** adopted (forced by vendoring/statically-linking Stockfish)
- [x] **Voice input** — Android SpeechRecognizer, FR/EN, Free Board / Play the Bot / Piece Path / Name the Square; Google beeps muted while listening
- [x] **Name the Square drill** — asked square green, wrong valid square red until next answer, board forced visible
- [x] **Shared error beep** — same tone on wrong move and nonexistent move, not on speech no-match
- [x] **In-app About / license** — GPLv3 notice + GitHub source link from Home
- [x] **Privacy policy** — microphone / SpeechRecognizer disclosed; no accounts or analytics
- [ ] Hidden board state for blindfold mode (FEN + mental tracking without visual aid)
- [ ] Session persistence (Room)

---

## MVP Roadmap (priority order)

1. ~~Visualization drill: square colors~~ ✅
2. ~~Piece path drill (B/N/R/Q)~~ ✅
3. ~~Pieces on the board~~ ✅
4. ~~Find the Square drill~~ ✅
4b. ~~Name the Square drill~~ ✅
5. ~~Famous games (guided replay)~~ ✅
6. ~~Free board + Play vs. Stockfish~~ ✅
7. ~~Voice input (Speak, FR/EN)~~ ✅
8. ~~In-app license/source link (About screen)~~ ✅
9. Room: gentle session history + stats (no streaks)
10. PGN import (post-MVP)

---

## Next Session — Suggested Tasks

Pick **one** feature at a time:

1. **True blindfold mode** — hide the board *during* a drill as a graded challenge (distinct from Hide, which only stops rendering).
2. **Room** — gentle session history (no streaks).
3. **Cap Piece Path / Find the Square difficulty** (e.g. knight 1–3 moves).
4. `ktlint` / `detekt`.
5. `preview/index.html` is stale — update or remove.

Not planned: a tactical "blindfold puzzle" drill — deliberately dropped, see "Deliberately out of scope" above.

---

## Rules for Working with Grok

- Read this file and the status section of `README.md` at the start of a session.
- **Modify code on `D:\CodingProject\blindfold-chess-trainer`** unless explicitly told otherwise.
- One feature at a time; no scope creep.
- No dark patterns, no streaks, no pressure UI.
- Run/build yourself; don't just tell the user what to run.
- Board is full width and square; do not put the toggles back in a side column or add an external notation strip.
- Commit at natural checkpoints rather than letting multiple features pile up uncommitted (see the working-tree note at the top of this file).

---

## Troubleshooting Quick Reference

| Symptom | Fix |
|---|---|
| Gradle sync « fail » (WSL project) | Wrong SDK/JDK mix — see README |
| `javaHome invalid … wsl.localhost` on `D:\` | Gradle JDK must be `jbr-21`, not WSL |
| `JAVA_HOME is not set` running `./gradlew` from Git Bash | Export it: `JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"` (see Development Environment) |
| Board not visible after changes | Ensure editing `D:\` copy, then Run ▶ |
| Board / Hide covers the whole screen | Board is full-width 1:1; page content must still scroll below the toggle row |
| `local.properties` wrong SDK | Run `setup-windows.ps1` on Windows |
| Native build fails / missing NDK | Install NDK `28.2.13676358` + CMake `3.22.1` via SDK Manager (Android Studio does this automatically on sync) |
| Docker broke local build | `sudo ./scripts/fix-build-permissions.sh` (WSL only) |

Full details: `README.md`
