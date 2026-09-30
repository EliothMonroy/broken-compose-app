# AGENTS.md

## What this project is

**Broken Calc** is a Jetpack Compose calculator built for **Android interview practice**. It is *intentionally* buggy and *intentionally* written with bad Android/Kotlin/Compose practices. The user fixes these problems themselves to practise debugging, refactoring and explaining their reasoning.

`todo.md` is the source of truth for the planted bugs (numbered 1–87), the bad-practice checklist, and stretch goals.

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
- DI: Hilt 2.57 with KSP, only partly adopted on purpose (see bugs #38–#42)
- More libraries are declared in `app/build.gradle.kts` for the newer bug categories: Room, Navigation Compose, Retrofit/OkHttp with kotlinx.serialization, WorkManager with hilt-work, and test libraries. Each one is used by its own section in `todo.md`.

## Build and run

```bash
./gradlew assembleDebug          # build
./gradlew installDebug           # install on a connected device/emulator
adb shell am start -n com.interviewprep.brokencalc/.MainActivity
adb logcat -s AndroidRuntime     # watch for crashes
```

```bash
./gradlew testDebugUnitTest          # JVM unit tests (app/src/test)
./gradlew connectedDebugAndroidTest  # device/Compose/Hilt tests (app/src/androidTest); uninstalls the app afterwards
```

The test suite is **red on purpose**: some tests fail honestly because of app bugs, and others are themselves buggy (see the Testing section of `todo.md`). Don't "fix" them unless asked.

Some bugs need device tricks: shaking (`adb emu sensor set acceleration …`), process death (`adb shell am kill …`), per-app locales (`adb shell cmd locale set-app-locales …`), fake charging (`adb shell dumpsys battery …`) and forcing WorkManager jobs (`adb shell cmd jobscheduler run -f …`). The relevant `todo.md` sections give the exact commands. Always restore device settings afterwards.

## Layout

Everything is in one flat package on purpose. The groups below are only for reading; there are no sub-packages.

```
app/src/main/java/com/interviewprep/brokencalc/
│  App shell
├── CalcApp.kt              # @HiltAndroidApp Application; WorkManager Configuration.Provider, daily backup
├── MainActivity.kt         # entry point; static Activity/prefs refs; loads history/memory; usage timer, startup tasks
├── App.kt                  # string-based "navigation", top bar ($ converter button, History badge)
├── Theme.kt                # CalcTheme(dark)
├── Globals.kt              # global mutable state: screen, selected history item, memory, appScope
│  Calculator
├── CalculatorScreen.kt     # display, live preview, idle auto-clear, keypad
├── CalculatorViewModel.kt  # button handling, GlobalScope evaluation + preview, copy
├── Calculator.kt           # tokenizer, recursive-descent parser, result formatter
├── ConstantsDb.kt          # Room: Constant entity, DAO, database, Hilt module (bugs #43–#47)
├── ConstantsDialog.kt      # Constants dialog (long-press MR)
├── ConverterDialog.kt      # currency converter dialog (bugs #58–#62)
├── RatesApi.kt             # Retrofit API, @Serializable models, rate cache
├── NetworkModule.kt        # Hilt: OkHttp, Retrofit, RatesApi
├── ShakeToClear.kt         # accelerometer shake-to-clear (lifecycle bugs #63–#67)
├── WelcomeBack.kt          # "Welcome back" toast, lifecycle observer
├── UsageTimer.kt           # "Active time" ticker
├── LcdTexture.kt           # display background bitmap (performance bugs #68–#72)
├── InputLog.kt             # per-keystroke input log file
├── Startup.kt              # startup "warm-up" + debug StrictMode
├── Accessibility.kt        # display semantics, keypad text size (a11y/i18n bugs #78–#82)
│  History
├── History.kt              # HistoryManager (prefs, callbackFlow), HistoryList, async stats
├── HistoryNav.kt           # Navigation Compose NavHost: list → detail (bugs #48–#52)
├── ScrollToTop.kt          # "↑ Top" button in History
├── BackupWorker.kt         # @HiltWorker history backup, BackupStore, BackupScheduler (bugs #73–#77)
│  Settings and DI
├── SettingsScreen.kt       # dark mode, haptics, decimal places, session stats, backup section
├── SettingsViewModel.kt    # @HiltViewModel for Settings
├── BackupSection.kt        # "Back up now" button + status
├── Di.kt                   # Hilt modules, qualifiers, AppEntryPoint service locator
└── Services.kt             # SessionTracker, SettingsStore (+ impls), ClipboardHelper

app/src/test/…              # JVM unit tests (testing bugs #53–#55)
app/src/androidTest/…       # Compose UI + Hilt tests, HiltTestRunner (testing bugs #56–#57)
app/src/main/res/values*/   # strings.xml (+ values-es), used by a few screens only
app/schemas/                # exported Room schemas (v1 is needed for bug #47)
```

## Conventions for fixes

Once the user starts fixing things, follow modern best practices *in the code they're changing*, and leave the rest of the codebase alone:

- State hoisting, unidirectional data flow, and `StateFlow` or Compose state owned by a real ViewModel (`viewModel()`).
- `viewModelScope`, not `GlobalScope`.
- Composables take `modifier: Modifier = Modifier` and state plus lambdas, not whole ViewModels.
- Strings in `strings.xml`, colours from `MaterialTheme`.
- Pure logic (`Calculator`) with no Android dependencies, so it can be unit tested.
