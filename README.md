# Blindfold Chess Trainer

A calm Android app for training chess **blindfolded** — pure mental visualization, no pressure, no dark patterns.

**License: GPLv3** (required by vendoring Stockfish — see `NOTICE`). The app is intended for **free Play Store distribution**. Publishing the APK is “conveying” under GPLv3 §6, so anyone who receives it must be able to get the corresponding source. The GitHub repo is **public**, which covers that. An in-app **About & license** screen links to the repo, the `LICENSE` file, and the [privacy policy](docs/PRIVACY.md).

Full session context: [`docs/PROJECT_CONTEXT.md`](docs/PROJECT_CONTEXT.md)

---

## Project status (5 September 2026)

Primary workspace: `D:\CodingProject\blindfold-chess-trainer`  
Branch: `main`.

| Item | Status | Notes |
|---|---|---|
| Android project (Kotlin + Jetpack Compose) | ✅ | AGP 8.9.1, Kotlin 2.1.10, minSdk 26, targetSdk **36**, NDK 28.2.13676358 |
| Module `core:chess` | ✅ | `chesslib` via `ChessSession`; `playSan` only accepts `legalMoves()` |
| Home screen | ✅ | 7 `ActionCard`s, scrollable |
| **Find the Square** drill | ✅ | Coordinate → tap the square; opens the board on start |
| **Name the Square** drill | ✅ | A square lights green → enter coordinates (pad or voice); opens the board on start |
| **Square Colors** drill | ✅ | Random square → Light / Dark, green/red flash |
| **Piece Path** drill | ✅ | Bishop / knight / rook / queen; pad **or voice**; illegal → reset + message; unclear speech → message, no reset |
| **Famous Games** drill | ✅ | Library as home-style cards; **Play this game** shows the board if hidden |
| **Free Board** drill | ✅ | Legal play, pad, tap, **voice** FR/EN; illegal move rejected, last legal position kept |
| **Play the Bot** | ✅ | Local Stockfish, 7 Elo levels (1350–2500); Continue / Discard if a game is in progress; last bot move large; **Play again** |
| Voice input | ✅ | Free Android `SpeechRecognizer`; Speak + FR/EN; Free Board, Play the Bot, Piece Path, Name the Square |
| About / license | ✅ | Link at the bottom of Home; GPLv3 + GitHub sources + privacy policy |
| Pieces / Flip / Arrows / Coordinates | ✅ | Unicode glyphs, orientation, arrows, notation |
| Board visibility | ✅ | Hidden at launch and when returning Home; Find the Square, Name the Square, and Famous Games (Play this game) open it |
| Native Stockfish | ✅ | Vendored sf_15, JNI/UCI, `libstockfishjni.so` |
| `LICENSE` / `NOTICE` | ✅ | GPLv3 (Stockfish) |
| Unit tests | ✅ | **136** tests (`:core:chess:testDebugUnitTest` + `:app:testDebugUnitTest`)
| GitHub Actions CI | ✅ | NDK + CMake, tests + debug APK |
| Room (session history) | ❌ | not started |
| Web preview (`preview/`) | ⚠️ | Square Colors demo only — **stale** |

**What you can try today:**

1. **Find the Square** — a coordinate is shown → tap that square. The board opens on its own.
2. **Name the Square** — a square lights green → name it with the pad or **Speak** (`e4`, `e four`, `S5`→f5). The board opens on its own.
3. **Square Colors** — a square → *Light* / *Dark*. Green 0.5 s / red 0.5 s. Local score, no streak.
4. **Piece Path** — piece, start → target. Pad or **Speak** (`cavalier f 3`, or just `h6` / `S5`→f5). Illegal → reset + message. Unclear speech (`P5`) → *Couldn't understand* without reset.
5. **Famous Games** — game cards, **Play this game** (opens the board), replay each announced move.
6. **Free Board** — pad, tap, or voice (`e4`, `knight f3`, `pion prend F4`, `petit rock` / `castle`). Illegal → message, position unchanged.
7. **Play the Bot** — Elo + color; if a game already exists: Continue / Discard. Last bot move large **and spoken** (FR/EN). Checkmate/stalemate + **Play again**. Voice input same as Free Board.

---

## Browser preview (no Android build)

The Android app **does not run natively in a browser** (Kotlin/Compose, not web). A **static web demo** still exists for a rough visual check:

```bash
cd /home/thomas/Projects/blindfold-chess-trainer
python3 -m http.server 8080 --directory preview
```

Then open: [http://localhost:8080](http://localhost:8080)

You can also open `preview/index.html` in Chrome/Firefox (double-click). It only covers the *Square Colors* drill — it is **not** the APK.

For the real app: Android Studio + emulator, or `./gradlew installDebug` on a phone.

---

## Prerequisites

Pick **one** of the two approaches below.

### Option A — local Gradle (recommended if it already compiles)

- **JDK 21**
- **Android SDK** (API 35, build-tools 35.0.0) — via Android Studio or `sdkmanager`
- `ANDROID_HOME` pointing at the SDK

Quick check:

```bash
java -version          # should show 21
echo $ANDROID_HOME     # e.g. /home/thomas/Android/Sdk
./gradlew --version
```

### Option B — Docker (reproducible build, no local SDK)

- Docker + Docker Compose
- Useful to compile and run tests without installing the SDK on the machine

---

## Testing locally — step by step

### 1. Clone and enter the project

```bash
cd /home/thomas/Projects/blindfold-chess-trainer
```

### 2. Configure the Android SDK (if not already done)

```bash
cp local.properties.example local.properties
# Edit local.properties and set the path to your Android SDK
```

### 3. Confirm it compiles (no phone needed)

```bash
chmod +x gradlew
./gradlew test assembleDebug
```

If that succeeds, the APK is here:

```
app/build/outputs/apk/debug/app-debug.apk
```

This is the fastest check that the current code works on your PC.

### 4. Run the `app` module (the Android application)

#### What is the “app module”?

This project has **2 Gradle modules**:

| Module | Role | Do you run it? |
|---|---|---|
| **`app`** | The Android application (screens, buttons) | **Yes** — this one |
| **`core:chess`** | Chess logic library (no UI) | **No** — only a dependency of `app` |

“Run the app module” means: **compile and install the APK on a phone or emulator**. You never launch `core:chess` on its own.

---

#### Method 1 — Android Studio (recommended under WSL2)

**Where to open the project:** start **Android Studio on Windows** (not from a WSL terminal), then open the project folder:

```
\\wsl$\Ubuntu\home\thomas\Projects\blindfold-chess-trainer
```

*(Change `Ubuntu` if your WSL distro has another name — see File Explorer → “Linux”.)*

**Steps:**

1. **File → Open** → select the `blindfold-chess-trainer` folder (the one that contains `build.gradle.kts`).
2. Wait for **Gradle Sync** (progress bar at the bottom). If Android Studio offers SDK or JDK 21 installs, accept them.
3. Top right, check the toolbar:
   - Configuration dropdown: choose **`app`** (not `core:chess`).
   - Device dropdown: choose an **emulator** or a **phone plugged in over USB**.
4. If no device appears:
   - **Tools → Device Manager → Create Device** (e.g. Pixel 7, API 35).
   - Click ▶ next to the emulator to start it.
5. Click the green **Run ▶** button (or `Shift+F10`).

Android Studio runs the equivalent of:

```bash
./gradlew :app:installDebug
```

and launches the app on the selected device.

**You know it worked if** the emulator opens and shows the “Blindfold Chess Trainer” screen.

---

#### Method 2 — command line (WSL or terminal)

You need a **device already visible** to `adb` before installing.

```bash
cd /home/thomas/Projects/blindfold-chess-trainer

# 1) Check that a phone/emulator is connected
adb devices
# You should see a line like:
#   emulator-5554   device
# or
#   XXXXXXXX        device
```

If the list is **empty** or says `unauthorized`:
- emulator: start it first from Android Studio (Device Manager);
- phone: enable **Developer options → USB debugging**, plug the cable in, accept the prompt on the phone.

```bash
# 2) Build + install the app module on the device
./gradlew :app:installDebug

# 3) Open the app (optional if installDebug already launched it)
adb shell am start -n com.blindfoldchess.trainer/.MainActivity
```

> **Note:** `./gradlew installDebug` and `./gradlew :app:installDebug` do the same thing here, because only `app` is an installable application.

**With no device connected**, you can still **build** the APK (but not install it):

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

#### Method 3 — no Android Studio / no emulator: browser preview

If you only want to **see the UI** without setting up an emulator:

```bash
python3 -m http.server 8080 --directory preview
```

→ [http://localhost:8080](http://localhost:8080) (web demo, not the real Android app).

---

#### WSL2: why `adb devices` is often empty

Under WSL2 the emulator usually runs **on Windows**, while `adb` in WSL does not see it.

**Workarounds:**
- **Simplest:** do everything in **Android Studio on Windows** (method 1 above).
- **Or** use Windows `adb` from PowerShell to install the APK built in WSL:
  ```powershell
  adb devices
  adb install \\wsl$\Ubuntu\home\thomas\Projects\blindfold-chess-trainer\app\build\outputs\apk\debug\app-debug.apk
  ```

### 5. Via Docker (build + tests only)

```bash
# First time: build the image (can take several minutes)
docker compose build

# Run tests and compile the APK in the container
docker compose run --rm android
```

The APK is written to `app/build/outputs/apk/debug/` on your disk (mounted volume).

> **Note:** an Android emulator inside Docker on WSL2 is possible but painful. To test the UI, prefer Android Studio + emulator or a physical phone.

---

## What you should see in the app

1. **Home** — title “Blindfold Chess Trainer”, **7 identical cards** (title + description + Start drill). The board is **hidden** (*Show board*).
2. **Board** — compact; right column: Hide, Flip, Coordinates, Arrows, Pieces. Returning Home hides the board again.
3. **Find the Square** — large coordinate, tap, green/red flash. The board opens when the drill starts.
4. **Name the Square** — a square stays green; pad (file then rank) or **Speak**. Wrong name: *Not quite — try again*. Unclear speech: *Couldn't understand that square*. The board opens when the drill starts.
5. **Square Colors** — large square, Light / Dark, *Next*, score `X / Y`.
6. **Piece Path** — piece selector, `e2 → f4`, pad (lowercase files) + **Speak** (FR/EN). Illegal: *Illegal move — starting over*. Unclear speech: *Couldn't understand that move*.
7. **Famous Games** — 6 cards (same style as Home); Play this game opens the board; one announced move at a time.
8. **Free Board** — pad (lowercase files, frames at 50% opacity), tap, **Speak**. Side to move and draft on one line (`White to move - NF3`). Illegal: rejected, position unchanged.
9. **Play the Bot** — Elo/color setup; if a game is in progress: Continue / Discard. Last bot move large and **spoken** (Speak chips FR/EN); *White to move · Bot is thinking…*; checkmate/stalemate + Play again on the same row. Voice input same as Free Board.
10. **About & license** — quiet link at the bottom of Home: GPLv3, Stockfish, chesslib, buttons to the repo, the LICENSE file, and the privacy policy.
11. **Back / home cube** — return Home (board hidden).

To check Square Colors: `a1` is **dark**, `a2` is **light**, `e4` is **light**.  
To check Piece Path (rook): same file or same rank = legal; otherwise reset.  
To check voice: Speak + **EN** then `knight f 3`; **FR** then `cavalier f trois` / `pion prend F4` / `petit rock`. Name the Square: Speak `e4` / `e four` / `S5`.  
To check Play the Bot: the bot’s first move should arrive within a few seconds; otherwise see `NativeStockfish`/NDK in troubleshooting.

---

## Useful commands

```bash
# Tests only
./gradlew test

# Rebuild the debug APK
./gradlew assembleDebug

# Play upload bundle (signed if keystore.properties exists) + 16 KB check
./gradlew :app:bundleRelease :app:checkReleasePageSize
# AAB: app/build/outputs/bundle/release/app-release.aab

# Clean and rebuild everything
./gradlew clean test assembleDebug

# List available Gradle tasks
./gradlew tasks
```

**Play signing (once, local only):** `powershell -ExecutionPolicy Bypass -File .\scripts\create-upload-keystore.ps1`  
That writes `keystore/upload-keystore.jks` and `keystore.properties` (both gitignored). Back them up offline. Losing the upload key blocks Play updates.

---

## Common troubleshooting

| Problem | Hint |
|---|---|
| `:app:processDebugResources FAILED` | See section below |
| `SDK location not found` | Create `local.properties` with `sdk.dir=/path/to/Android/Sdk` |
| `java: invalid target release: 21` | Install JDK 21 and check `JAVA_HOME` |
| `JAVA_HOME is not set` when running `./gradlew` from Git Bash (Windows) | Export it for the session: `JAVA_HOME="C:\Program Files\Android\Android Studio\jbr" ./gradlew test` |
| `assembleDebug` / `installDebug` very slow or fails on C++ | The build also compiles Stockfish (NDK/CMake). Install NDK `28.2.13676358` + CMake `3.22.1` via SDK Manager if missing; `./gradlew test` alone does not need the NDK |
| `AccessDeniedException` in `app/build` or `core/chess/build` | Files created by Docker as `root` — see section below |
| `adb: command not found` | Install `platform-tools` or use Android Studio |
| `adb devices` empty under WSL2 | Start the emulator from Android Studio (Windows) or plug in a phone over USB |
| `Gradle JVM option is incorrect` (project on `\\wsl$\`) | See section below |
| First Docker build very long | Normal — SDK and Gradle deps are cached in Docker volumes |

### Gradle JVM error with a WSL project (`\\wsl$\Ubuntu\...`)

Typical message:

> Gradle JVM option is incorrect: `C:\Program Files\Android\Android Studio\jbr`  
> The project is located on WSL. Use the JDK installed on the same WSL distribution.

**Why:** the project lives on the Linux disk (WSL), but Gradle is pointed at Android Studio’s **Windows** JDK. You need the **Ubuntu** JDK.

#### Fix (about 2 minutes) — point Gradle at the WSL JDK

1. In Android Studio: **File → Settings** (or **Ctrl+Alt+S**).
2. **Build, Execution, Deployment → Build Tools → Gradle**.
3. **Gradle JDK:** do **not** pick `jbr-21` with a `C:\Program Files\...` path.
4. Pick an entry such as:
   - **`WSL: java-21-openjdk-amd64`** or **`Ubuntu (WSL)`**
   - or **Add JDK from disk…** and browse to:
     ```
     \\wsl.localhost\Ubuntu\usr\lib\jvm\java-21-openjdk-amd64
     ```
     *(equivalent: `\\wsl$\Ubuntu\usr\lib\jvm\java-21-openjdk-amd64`)*
5. Click **Apply → OK**.
6. **File → Sync Project with Gradle Files** (elephant + arrow icon).

If the WSL JDK does not appear, install it in Ubuntu:

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
/usr/lib/jvm/java-21-openjdk-amd64/bin/java -version
```

Then repeat step 4 in Android Studio.

#### Sync that only says “fail” with no detail

Android Studio often hides the real error. In the Windows logs it is usually:

> **Operation result has not been received**

The actual cause (in the WSL Gradle logs) is almost always:

> **sdk.dir = C:\Users\...\Android\Sdk** → Linux Gradle cannot find that path  
> or **Build Tools 35.0.0 is corrupted** (Windows SDK `.exe` seen from Ubuntu)

**Why:** Android Studio **rewrites** `local.properties` with the Windows SDK on every sync, while Gradle runs in **Ubuntu**.

**Automatic fix (already in the project):** `settings.gradle.kts` corrects `local.properties` when Gradle starts. If it still blocks:

```bash
./scripts/fix-local-properties.sh
```

Then **File → Sync Project with Gradle Files** in Android Studio.

**See the hidden error** (if “fail” with no text):
- Android Studio → **Help → Show Log in Explorer** → open `idea.log`
- Search for `Gradle sync failed` or `MODEL_FETCH_FAILED`
- Or in Ubuntu: `grep -i "sync failed\|FAILURE\|corrupted" ~/.gradle/daemon/8.11.1/*.log | tail -5`

**Check in Ubuntu:**

```bash
cd /home/thomas/Projects/blindfold-chess-trainer
./scripts/diagnose-sync.sh
```

**Fix `local.properties`** — it must point at a **Linux SDK inside WSL**:

```properties
sdk.dir=/home/thomas/Android/Sdk
```

**Not** `C:\Users\...` and **not** `/mnt/c/Users/...` (Windows build-tools `.exe` files look “corrupted” to Linux Gradle).

If the WSL SDK does not exist yet, install it once:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
mkdir -p "$ANDROID_HOME"
yes | sdkmanager --sdk_root="$ANDROID_HOME" \
  "platform-tools" "platforms;android-36" "build-tools;35.0.0"
```

*(Replace `sdkmanager` with its full path if needed, e.g. `~/development/android/cmdline-tools/latest/bin/sdkmanager`.)*

Then in Android Studio: **File → Sync Project with Gradle Files**.

**See the real error in Android Studio** (when the message is vague):
- **Build** tab at the bottom → **Sync** or **Build Output**
- Or **View → Tool Windows → Build**
- The red line above “fail” is the real cause (`SDK location`, `Build Tools corrupted`, etc.)

#### Simpler alternative — avoid WSL for Android Studio

If the WSL setup feels fragile, clone the project onto the **Windows** disk:

```
C:\Users\<you>\Projects\blindfold-chess-trainer
```

Open **that** folder in Android Studio. The default JDK (`jbr` from Android Studio) will work with no extra setup. You can keep a copy in WSL for the terminal/Docker if you want.

### `:app:processDebugResources FAILED`

Common causes:

1. **Missing SDK** — create `local.properties` (see `local.properties.example`).
2. **Locked `build/` folders** — often after `docker compose run` (files owned by `root`). Symptom: `AccessDeniedException` or `Could not set file mode 755`.

   ```bash
   # Fix in one command (asks for your sudo password)
   sudo ./scripts/fix-build-permissions.sh

   # Then retry
   ./gradlew test assembleDebug
   ```

3. **Corrupt Gradle cache** — last resort:

   ```bash
   ./gradlew --stop
   rm -rf .gradle build app/build core/chess/build
   ./gradlew test assembleDebug --no-build-cache
   ```

   If `rm -rf app/build` fails with “Permission denied”, run `fix-build-permissions.sh` first.

Example `local.properties` (do not commit this file):

```properties
sdk.dir=/home/thomas/Android/Sdk
```

---

## Project structure

```
app/              → Compose UI (home, drills, board), engine bridge, native Stockfish (cpp/)
core/chess/       → chess logic (chesslib) + unit tests
preview/          → static web preview (Square Colors only — far behind the real app)
scripts/          → helpers (Windows/WSL SDK, build permissions)
.github/workflows → CI (installs NDK + CMake, tests + debug APK)
docs/             → PROJECT_CONTEXT.md (session context)
LICENSE           → GPLv3 (required by vendoring Stockfish)
NOTICE            → Stockfish (GPLv3) and chesslib (Apache 2.0) provenance
docker-compose.yml
Dockerfile
```

UI / drill files:

```
app/.../trainer/
├── MainActivity.kt                 # board visibility, AppScreen
├── engine/                         # ChessEngine, NativeStockfish, StockfishChessEngine
├── feature/board/                  # AppShell, BoardPanel, ChessBoard
├── feature/home/HomeScreen.kt / AboutScreen.kt
├── feature/drills/
│   ├── CoordinatePad.kt / DrillBackButton.kt
│   ├── VoiceMoveInput.kt           # SpeechRecognizer, FR/EN, Speak
│   ├── FindSquare / NameSquare / SquareColor / PiecePath
│   ├── FamousGamesScreen.kt / FamousGamesViewModel.kt
│   ├── FreeBoardScreen.kt / FreeBoardViewModel.kt / FreeBoardPlayPad.kt
│   └── PlayBotScreen.kt / PlayBotViewModel.kt
└── ui/ActionCard.kt + ui/theme/

app/src/main/cpp/                   # vendored Stockfish sf_15 + JNI bridge
├── CMakeLists.txt
├── bridge.cpp                       # JNI glue: pipes stdin/stdout of UCI::loop
└── stockfish/                       # upstream source, unmodified

core/chess/.../
├── Square.kt / SquareColor.kt / SquareColorDrill.kt
├── PieceType.kt                    # move helpers still used by drills
├── PiecePathDrill.kt / FindSquareDrill.kt
├── OccupiedSquare.kt                # board occupancy snapshot
├── ChessSession.kt                  # chesslib session: SAN/UCI/square play, history, undo
├── ChesslibMapping.kt               # internal Square/Move/PieceType <-> chesslib
├── FamousGame.kt / FamousGamesCatalog.kt
├── GameFollowDrill.kt
└── ChessSpeechParser.kt             # FR/EN speech → SAN / Piece Path square
```

---

## Next steps — recommendations

One feature at a time. No streaks, no dark patterns. `chesslib` is used throughout `core:chess`, and Stockfish runs natively (see `docs/PROJECT_CONTEXT.md` for detail).

**Priority (suggested order)**

1. **True blindfold mode** — hide the board *during* a drill while still tracking the position (the current Hide is not a graded challenge).
2. **Room** — calm session history (no streak).
3. **Cap difficulty** on Piece Path / Find the Square (e.g. knight 1–3 moves).
4. **ktlint / detekt**.
5. **Update or drop `preview/`**.

**Later (not now)**

- Compose Navigation (the `AppScreen` enum is enough)
- AGP upgrade
- iOS / KMP

**Rules for the next session**

- Work in `D:\CodingProject\blindfold-chess-trainer`.
- Read `docs/PROJECT_CONTEXT.md` as well as this README.
- Run `./gradlew :core:chess:test :app:testDebugUnitTest` after logic changes (from Git Bash, export `JAVA_HOME` first — see Troubleshooting).
- One drill or board toggle at a time.
- Commit regularly rather than stacking several uncommitted features.
