# Blindfold Chess Trainer - PROJECT CONTEXT

**Last updated:** April 03, 2026

## Project Goal
Mobile application (Android first, iOS possible later) to help people learn and train **chess blindfolded** — pure mental visualization without seeing the board.

The app must feel calm, respectful, and non-addictive.  
We do **not** want streaks, fake urgency, dark patterns, or anything that makes the user feel manipulated.  
Goal = create a peaceful, pleasant experience where the user learns at their own pace, enjoys the process, and gradually improves their blindfold skills without stress.

## Core Philosophy
- No gamification that creates pressure (no daily streaks, no "don't break your chain", no FOMO).
- Focus on calm, focused training sessions.
- The user should feel respected and in control.
- Progress should feel natural and satisfying, not forced.
- Everything designed for long-term, low-stress improvement.

## MVP Features (in priority order)
1. Visualization drills (square names, colors, diagonals, knight tours, etc.)
2. Blindfold puzzles (tactical positions / endgames where you must find the move mentally)
3. Blindfold games vs AI (board hidden, input via algebraic notation + optional voice)
4. Personal progress tracking (visualization level, solved puzzles, games played, gentle statistics)
5. Simple, calm session history

Future features (after stable MVP): import PGN for blind analysis, custom positions, etc.

## Technical Constraints & Decisions
- Core training (drills, puzzles, local AI) must work **100% offline**.
- Clean, minimal, calm UI (dark mode by default, very few distractions).
- Primary input: algebraic notation (e4, Nf3, O-O…). Optional voice input.
- Performance must be good even on mid-range Android devices.
- Docker + proper CI/CD required for development and builds.

## Recommended Tech Stack (my honest recommendation - 2026)

**Mobile Frontend: Kotlin + Jetpack Compose (strongly preferred over Flutter)**
- Reason: Heavy board state, complex chess logic, and mental visualization features benefit greatly from native performance and fine control.  
  Compose is excellent for this. You learn fast, so investing in Kotlin/Compose will give you the best long-term result and feel.
- Strong alternative if you really want cross-platform early: Kotlin Multiplatform + Compose Multiplatform.

**Chess Logic:**
- Chess library: `com.github.bhlangonijr.chesslib` (solid Kotlin/Java library)
- Engine: Stockfish via JNI for offline AI (controllable strength)
- Internal board representation: Bitboards where performance matters

**State Management:** Orbit-MVI or clean MVI with Kotlin Flow + ViewModel

**Local Database:** Room (SQLite) for saving progress, custom positions, and gentle stats

**Testing:** JUnit5 + Kotest + Compose UI tests. Aim for high coverage on chess core logic.

**DevOps:**
- Docker for the entire development environment (Android SDK, emulator, etc.)
- CI/CD: GitHub Actions (build APK/AAB, run tests, lint, detekt, automated releases to Firebase App Distribution or internal track)
- Code quality: ktlint + detekt

**Other libs:**
- Voice input: Android SpeechRecognizer (or Whisper.cpp local for better offline quality if needed)
- No heavy analytics or tracking that feels invasive

**Architecture:**
- Clean Architecture + MVI pattern
- Feature modules (drills, puzzles, games, progress)
- Core module for chess engine and visualization logic

## Key Decisions Already Made / To Be Confirmed Soon
- [ ] Final choice: pure Kotlin/Compose vs Kotlin Multiplatform
- [ ] How to manage hidden board state in memory for blindfold mode (FEN + mental delta tracking)
- [ ] Stockfish strength levels and how to make it feel fair and educational
- [ ] Input methods: algebraic notation mandatory, voice optional

## Rules for Working with Grok (me)
- Always paste the full current context (or the most relevant sections) at the beginning of important messages.
- I will tell you bluntly what is overkill, what will waste your time, and what is actually the best tool for each feature.
- We move feature by feature, never everything at once.

---

**Next immediate steps (to validate with you):**
1. Create repository + basic Docker setup + GitHub Actions CI
2. Set up Android Compose project + integrate chess library
3. Implement basic board logic + first simple visualization drill
