# Broken Calc

A Jetpack Compose calculator for Android that is **broken on purpose**. It has 102 planted bugs and is written with bad practices throughout, so you can practise the debugging and refactoring you'd be asked to do in an Android interview.

> ⚠️ Don't copy code from this repo into a real app. Almost everything odd in it is deliberate.

## How to use it

1. Open the project in Android Studio (or build from the command line, see below) and run it on an emulator or device.
2. Open [`todo.md`](todo.md). Read the short **"Before you start"** list first: a few bugs block others, and it tells you which to fix early.
3. Pick a bug, reproduce it with the steps given, find the cause, and fix it. Each bug has a hint you can click to open if you get stuck.
4. Tick the bug's checkbox and move on.

## What's inside

| Area | Bugs |
|---|---|
| Core calculator (easy, medium, hard) | #1–#28 |
| Coroutines | #29–#37 |
| Dependency injection (Hilt) | #38–#42 |
| Room | #43–#47 |
| Navigation Compose | #48–#52 |
| Testing | #53–#57 |
| Networking (Retrofit / OkHttp) | #58–#62 |
| Lifecycle and process death | #63–#67 |
| Performance and recomposition | #68–#72 |
| WorkManager | #73–#77 |
| Accessibility and i18n | #78–#82 |
| UI state and one-off events | #83–#87 |
| Flow operators | #88–#92 |
| Permissions, notifications and foreground services | #93–#97 |
| Adaptive UI | #98–#102 |

`todo.md` also has a list of bad practices to refactor and some stretch goals.

## Build and run

You need JDK 17 or newer and the Android SDK (API 36). Android Studio sets both up for you.

```bash
./gradlew installDebug
```

```bash
./gradlew testDebugUnitTest
```

Three things are **meant to fail**, because they are part of the exercise:

- Some unit tests and device tests (see the Testing section of `todo.md`).
- `./gradlew lintDebug`.
- The app itself, in many places. Crashes are expected.

## Working with an AI assistant

[`AGENTS.md`](AGENTS.md) tells AI coding agents how to behave in this repo: act as an interviewer or coach, give hints before answers, and don't fix anything unless you ask for a specific bug. If you want an agent to just fix something, say so explicitly.
