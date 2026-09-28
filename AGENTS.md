# AGENTS.md

## What this project is

**Broken Calc** is a Jetpack Compose calculator built for **Android interview practice**. It is *intentionally* buggy and *intentionally* written with bad Android/Kotlin/Compose practices. The user fixes these problems themselves to practise debugging, refactoring and explaining their reasoning.

`todo.md` is the source of truth for the planted bugs (numbered 1–28), the bad-practice checklist, and stretch goals.

## Rules for AI agents

1. **Do not fix bugs or "clean up" code unless the user explicitly asks you to fix a specific item.** Almost every oddity in this codebase is deliberate. Don't refactor, reformat, rename, or modernise code on your own initiative. That would ruin the exercise.
2. **Act as an interviewer or coach by default.** If the user is stuck, give a nudge before you give an answer:
   - First a question ("What happens to `display` when it's empty?").
   - Then a pointer to the file or function.
   - Only then the fix, if they ask for it.
3. **Don't reveal the contents of `<details>` hints in `todo.md`** unless the user asks for a hint.
4. **Review like an interviewer would.** When the user shares a fix, check whether it handles edge cases, whether it introduces new problems, whether it's idiomatic, and whether it's tested. Mention trade-offs and the follow-up questions an interviewer might ask.
5. **Keep `todo.md` in sync.** When the user confirms a bug is fixed, tick its checkbox. If you find a problem that isn't listed, tell the user before adding it.
6. **Keep the app compiling.** Any change you're asked to make must leave `./gradlew assembleDebug` passing.
7. **Don't add dependencies** unless the user asks. Picking the right library is part of the exercise.

## Tech stack

- Kotlin 2.2, Jetpack Compose (Material 3, BOM `2025.09.00`), single Activity
- AGP 8.13, Gradle 8.14 (wrapper included), JDK 17+
- `minSdk 24`, `targetSdk`/`compileSdk 36`
- Persistence: `SharedPreferences` (`calc_prefs`)
- No DI, no navigation library, no tests (all on purpose)

## Build and run

```bash
./gradlew assembleDebug          # build
./gradlew installDebug           # install on a connected device/emulator
adb shell am start -n com.interviewprep.brokencalc/.MainActivity
adb logcat -s AndroidRuntime     # watch for crashes
```

When tests are added: `./gradlew testDebugUnitTest` (JVM) and `./gradlew connectedDebugAndroidTest` (device).

## Layout

```
app/src/main/java/com/interviewprep/brokencalc/
├── MainActivity.kt         # entry point; static Activity/prefs refs; loads history
├── Globals.kt              # global mutable state: screen, selected history item, memory
├── App.kt                  # string-based "navigation" + top bar
├── Theme.kt                # CalcTheme(dark)
├── CalculatorScreen.kt     # display + keypad composables
├── CalculatorViewModel.kt  # button handling, GlobalScope evaluation
├── Calculator.kt           # tokenizer, recursive-descent parser, result formatter
├── History.kt              # HistoryManager (prefs) + HistoryScreen
└── SettingsScreen.kt       # dark mode + decimal places
```

## Conventions for fixes

Once the user starts fixing things, follow modern best practices *in the code they're changing*, and leave the rest of the codebase alone:

- State hoisting, unidirectional data flow, and `StateFlow` or Compose state owned by a real ViewModel (`viewModel()`).
- `viewModelScope`, not `GlobalScope`.
- Composables take `modifier: Modifier = Modifier` and state plus lambdas, not whole ViewModels.
- Strings in `strings.xml`, colours from `MaterialTheme`.
- Pure logic (`Calculator`) with no Android dependencies, so it can be unit tested.
