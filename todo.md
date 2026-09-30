# Broken Calc — Bug Hunt

This calculator compiles and mostly works, but it's full of bugs and bad practices on purpose.
Use it to practise the kind of debugging and refactoring you'd get in an Android live-coding interview.

**How to use this list**

1. Pick a bug and reproduce it first. Don't open the hint yet.
2. Find the root cause, then fix it with the smallest change that makes sense.
3. Ideally, write a test that fails before the fix and passes after it.
4. Check the box and move on.

Each bug has **Steps**, **Expected** and **Actual** sections. Hints are collapsed so you only see them if you want them.
Some bugs share a root cause, and fixing one can uncover another. That's intentional.

---

## Easy

- [ ] **1. Backspace on an empty display crashes the app**
  - Steps: open the app (or press `C`), then press `⌫`.
  - Expected: nothing happens.
  - Actual: the app crashes with `StringIndexOutOfBoundsException`.
  <details><summary>Hint</summary>Look at the <code>"⌫"</code> branch in <code>CalculatorViewModel.onButton</code>.</details>

- [ ] **2. Leading zeros are allowed**
  - Steps: press `0`, `0`, `7`.
  - Expected: display shows `7`.
  - Actual: display shows `007`.
  <details><summary>Hint</summary>The digit branch appends blindly.</details>

- [ ] **3. A number can have several decimal points, and then `=` crashes the app**
  - Steps: press `1`, `.`, `2`, `.`, `3`, `=`. Pressing `.` then `=` also does it.
  - Expected: the second `.` in the same number is ignored.
  - Actual: display shows `1.2.3`, and `=` crashes the app with `NumberFormatException`.
  <details><summary>Hint</summary>Two problems: the <code>"."</code> branch never checks the current number, and the <code>catch</code> in <code>calculate()</code> only catches <code>IllegalStateException</code>. Also think about what an uncaught exception does inside <code>GlobalScope.launch</code>.</details>

- [ ] **4. The error state isn't cleared when you type**
  - Steps: press `5`, `+`, `×`, `3`, `=` (shows `Error`), then press `7`.
  - Expected: display shows `7`.
  - Actual: display shows `Error7`, and `=` on that "works" and gives `7`.
  <details><summary>Hint</summary>Compare what the success path and the error path in <code>calculate()</code> do with <code>justEvaluated</code>. Why does the tokenizer accept letters at all?</details>

- [ ] **5. `M−` adds to memory instead of subtracting**
  - Steps: `5`, `M+`, `C`, `2`, `M−`, `C`, `MR`.
  - Expected: `3`.
  - Actual: `7`.
  <details><summary>Hint</summary>Copy-paste bug.</details>

- [ ] **6. The "Decimal places" setting does nothing**
  - Steps: Settings → set decimal places to 2 → back to Calc → `1`, `÷`, `3`, `=`.
  - Expected: `0.33`.
  - Actual: `0.3333333333333333`.
  <details><summary>Hint</summary>Compare the SharedPreferences key the setting is <b>written</b> with to the key <code>Calculator.format</code> <b>reads</b>. How do you stop this class of bug for good?</details>

- [ ] **7. In dark mode the display text can't be read**
  - Steps: Settings → enable Dark mode → restart the app (see #18).
  - Expected: light text on a dark background.
  - Actual: the display number is black on a near-black background.
  <details><summary>Hint</summary>Hard-coded colour in <code>CalculatorScreen</code>.</details>

- [ ] **8. System Back on History or Settings closes the app**
  - Steps: open History, then press the system Back button or swipe back.
  - Expected: you return to the calculator.
  - Actual: the app exits.
  <details><summary>Hint</summary>Navigation is a global string, and nothing handles Back. Look into <code>BackHandler</code>, or better, Navigation Compose.</details>

- [ ] **9. Dividing by zero shows `Infinity` or `NaN`**
  - Steps: `5`, `÷`, `0`, `=`. Also try `0`, `÷`, `0`, `=`.
  - Expected: a clear error message, for example `Can't divide by zero`.
  - Actual: `Infinity` or `NaN`, and these also get saved to history.
  <details><summary>Hint</summary><code>Double</code> division by zero doesn't throw.</details>

## Medium

- [ ] **10. `=` crashes on an empty or unfinished expression**
  - Steps: from a fresh start, press `=`. Also try `5`, `+`, `=`, or just `−`, `=`.
  - Expected: nothing happens, or `Error` is shown.
  - Actual: the app crashes with `IndexOutOfBoundsException`, about 300 ms after the tap.
  <details><summary>Hint</summary><code>parseFactor</code> indexes <code>tokens[pos]</code> without a bounds check. Also, why is the crash delayed?</details>

- [ ] **11. Two operators in a row give an error**
  - Steps: `5`, `+`, `×`, `3`, `=`.
  - Expected: the second operator replaces the first, so the result is `15`. `5`, `×`, `−`, `3` should still work as "5 × -3".
  - Actual: `Error`.
  <details><summary>Hint</summary>Fix it at input time in the ViewModel, not in the parser.</details>

- [ ] **12. `%` wipes out an expression**
  - Steps: `5`, `0`, `+`, `1`, `0`, `%`.
  - Expected: the last number becomes a percentage, so the display is `50+0.1`.
  - Actual: display shows `0.0`. Also, `5`, `0`, `%` shows `0.5`, which is correct, but it's formatted differently from `=` results.
  <details><summary>Hint</summary><code>display.toDoubleOrNull() ?: 0.0</code>: the silent fallback hides the problem.</details>

- [ ] **13. `±` flips the wrong number**
  - Steps: `5`, `+`, `3`, `±`.
  - Expected: `5+-3`, because the current number is negated.
  - Actual: `-5+3`.
  <details><summary>Hint</summary>Same idea as #12: both need a way to find the last number in the expression.</details>

- [ ] **14. Subtraction and division are evaluated right to left**
  - Steps: `1`, `0`, `−`, `5`, `−`, `2`, `=`. Also try `8 ÷ 4 ÷ 2` and `2 − 3 + 4`.
  - Expected: `3`, `1`, `3`.
  - Actual: `7`, `4`, `-5`.
  <details><summary>Hint</summary><code>parseExpression</code> and <code>parseTerm</code> recurse on the right-hand side, which makes operators right-associative. Use a loop instead.</details>

- [ ] **15. Floating-point errors show up in results**
  - Steps: `0`, `.`, `1`, `+`, `0`, `.`, `2`, `=`. This is easiest to see before fixing #6.
  - Expected: `0.3`.
  - Actual: `0.30000000000000004`.
  <details><summary>Hint</summary>Rounding when displaying hides it. <code>BigDecimal</code> fixes it. Be ready to explain why in an interview.</details>

- [ ] **16. With 0 decimal places, numbers ending in 0 lose digits** *(you'll only see this after fixing #6)*
  - Steps: Settings → decimal places 0 → `1`, `0`, `.`, `4`, `=`.
  - Expected: `10`.
  - Actual: `1`.
  <details><summary>Hint</summary><code>trimEnd('0')</code> runs even when there's no decimal point.</details>

- [ ] **17. Long numbers are cut off**
  - Steps: type about 15 digits.
  - Expected: the text shrinks or scrolls so the newest digits stay visible.
  - Actual: the end of the number is clipped, so you can't see what you're typing.
  <details><summary>Hint</summary>Fixed <code>56.sp</code>, <code>softWrap = false</code>, <code>TextOverflow.Clip</code>. Look into auto-sizing text or a horizontal scroll pinned to the end.</details>

- [ ] **18. The dark mode toggle only takes effect after a restart or rotation**
  - Steps: Settings → toggle Dark mode.
  - Expected: the theme changes right away.
  - Actual: nothing changes until the Activity is recreated.
  <details><summary>Hint</summary><code>MainActivity.onCreate</code> reads the preference once and passes a plain <code>Boolean</code> to the theme.</details>

- [ ] **19. The top bar sits under the status bar, and the `=` row sits under the navigation bar** *(Android 15+)*
  - Steps: run on an API 35+ device or emulator, ideally with 3-button navigation.
  - Expected: content avoids the system bars.
  - Actual: the title overlaps the clock and icons, and the bottom row can end up behind the nav bar.
  <details><summary>Hint</summary><code>targetSdk = 36</code> turns on edge-to-edge. Handle window insets, for example with <code>Scaffold</code> or <code>Modifier.safeDrawingPadding()</code>.</details>

- [ ] **20. In landscape, buttons are cut off**
  - Steps: rotate the device.
  - Expected: the whole keypad fits on screen, or the layout changes for landscape.
  - Actual: the bottom rows are off-screen and can't be reached. Your input is also lost, see #26.
  <details><summary>Hint</summary>Fixed heights in dp (<code>180</code>, <code>72</code>, <code>56</code>). Use <code>weight</code> instead.</details>

## Hard

- [ ] **21. Using a result of 1,000 or more in the next calculation gives the wrong answer**
  - Steps: `9`, `9`, `9`, `+`, `1`, `=` (shows `1,000`), then `+`, `5`, `=`.
  - Expected: `1005`.
  - Actual: `1`.
  <details><summary>Hint</summary>There are three problems here. (a) <code>format</code> adds grouping separators that end up back in the input. (b) The tokenizer treats <code>,</code> as a separator between two numbers. (c) The parser never checks that it used up every token, so trailing tokens are silently dropped. <code>2(3)</code> and <code>5)</code> show (c) too. Bonus: <code>String.format</code> without a <code>Locale</code> behaves differently in, say, <code>de_DE</code>.</details>

- [ ] **22. History is corrupted after restarting the app**
  - Steps: calculate `999+1` so the result is `1,000`, then force-stop and reopen the app, then open History.
  - Expected: one entry, `999+1 = 1,000`.
  - Actual: two entries, `999+1 = 1` and `000`. Tapping `000` crashes the app.
  <details><summary>Hint</summary>History is saved as one string joined with <code>,</code>. What's a better storage format? Consider JSON, a <code>StringSet</code>, DataStore or Room. Also, <code>split(" = ")[1]</code> assumes the format without checking.</details>

- [ ] **23. History entries are duplicated after rotating**
  - Steps: do a few calculations, rotate the device, then open History.
  - Expected: the same entries as before.
  - Actual: every entry appears twice. Rotate again and it's three times.
  <details><summary>Hint</summary>Look at where <code>HistoryManager.load()</code> is called and what it does to a list that already has items.</details>

- [ ] **24. "Clear history" doesn't update the screen**
  - Steps: History → Clear history.
  - Expected: the list is emptied right away.
  - Actual: the list stays until you leave the screen and come back.
  <details><summary>Hint</summary>Compose can't observe a plain <code>ArrayList</code>. Think about how state should flow.</details>

- [ ] **25. After picking a history entry, the calculator stops responding to input**
  - Steps: History → tap any entry → try typing digits or pressing `C`.
  - Expected: the result loads, then you can keep going from there.
  - Actual: the display is stuck on that value. Every key press is undone straight away.
  <details><summary>Hint</summary><code>CalculatorScreen</code> writes state during composition and never clears <code>selectedHistoryItem</code>. Every recomposition resets the display. Side effects belong in effects or event handlers.</details>

- [ ] **26. Input is lost on rotation, and when switching to History or Settings and back**
  - Steps: type `123+`, then rotate, or open Settings and come back.
  - Expected: `123+` is still there.
  - Actual: the display goes back to `0`.
  <details><summary>Hint</summary><code>remember { CalculatorViewModel() }</code> is not how you get a ViewModel. Look at <code>viewModel()</code> from <code>lifecycle-viewmodel-compose</code>, and at <code>SavedStateHandle</code> for process death.</details>

- [ ] **27. The memory indicator `M` doesn't show up after `M+`**
  - Steps: press `5`, wait a second, then press `M+`.
  - Expected: a small `M` appears above the display right away.
  - Actual: it only appears after you press another key. `MC` has the same problem in reverse.
  <details><summary>Hint</summary><code>Memory.value</code> is a plain <code>var</code>, so Compose has no way to know it changed.</details>

- [ ] **28. Typing right after `=` gets overwritten, and a quick double tap on `=` misbehaves**
  - Steps: `2`, `+`, `2`, `=`, then immediately press `9`, within 300 ms.
  - Expected: the `9` is handled after or instead of the result, and never silently lost.
  - Actual: the `9` shows for a moment, then the result `4` replaces it. Tapping `=` twice quickly can add duplicate history entries.
  <details><summary>Hint</summary><code>GlobalScope</code> plus an artificial <code>delay</code>, results written back without checking whether the input has changed, and a shared mutable parser (<code>Calculator.tokens</code> and <code>pos</code>) with no synchronisation. Use <code>viewModelScope</code>. Does this even need to be async?</details>

## Coroutines

These cover what interviewers usually ask about: scopes, cancellation, dispatchers, `async`/`await`, exception propagation, `SupervisorJob`, and Flow.

> ⚠️ **#31 slows the whole app down** every time you leave the calculator screen, and its log spam hides other log output. If it gets in your way, fix it first.

- [ ] **29. The live preview sometimes shows a stale result**
  - Steps: quickly type `1+2+3+4+5` and watch the gray preview under the display. Try it a few times.
  - Expected: the preview always ends at `= 15`.
  - Actual: it often ends at an older value, like `= 10`, or disappears because the last result to arrive was for `1+2+3+4+`.
  <details><summary>Hint</summary><code>updatePreview()</code> starts a new <code>GlobalScope</code> coroutine on every key press, with a random delay, and never cancels the previous one. Whichever finishes last wins. Keep a <code>Job</code> and cancel it, or model the input as a <code>Flow</code> and use <code>debounce</code> plus <code>mapLatest</code>/<code>collectLatest</code>. Also think about what <code>catch (e: Exception)</code> does once cancellation starts happening here.</details>

- [ ] **30. The display is wiped while you're typing**
  - Steps: type a digit every few seconds for about 30 seconds.
  - Expected: the display only clears after 30 seconds with **no** input.
  - Actual: it clears 30 seconds after the screen opened, even in the middle of typing.
  <details><summary>Hint</summary><code>LaunchedEffect(Unit)</code> never restarts. What should its key be? Once the key is right, you probably don't need the counter at all.</details>

- [ ] **31. Leaving the calculator pegs the CPU, and after a few trips `=` stops working**
  - Steps: open History or Settings, or rotate. Check the CPU in Android Studio's profiler, or run `adb shell top`, and watch `adb logcat -s IdleTimer`. Go back and forth about four times, then try `3`, `+`, `3`, `=`.
  - Expected: nothing runs once the calculator is off screen.
  - Actual: `IdleTimer: tick failed: The coroutine scope left the composition` is logged hundreds of thousands of times per second, CPU goes over 100%, and eventually `=` and the preview never produce anything.
  <details><summary>Hint</summary><code>catch (e: Exception)</code> also catches <code>CancellationException</code>. The loop never ends, and once the job is cancelled <code>delay()</code> throws straight away, so it spins on a <code>Dispatchers.Default</code> thread forever. Each trip adds another spinning thread until the default pool (one thread per CPU core) is full, and then <code>GlobalScope.launch</code> work for <code>=</code> never gets a thread. Rethrow <code>CancellationException</code>, or catch only what you mean to, and use <code>while (isActive)</code>. Why is <code>withContext(Dispatchers.Default)</code> pointless for a timer?</details>

- [ ] **32. Clear history freezes the UI, and the "Saving…" indicator never shows**
  - Steps: History → Clear history. Watch the ripple, or run `adb logcat | grep Choreographer`.
  - Expected: the UI stays responsive and "Saving…" shows while the write happens.
  - Actual: the app freezes for about 0.8 s (`Skipped 47 frames!`), and "Saving…" never appears.
  <details><summary>Hint</summary><code>runBlocking</code> on the main thread blocks it. <code>isSaving</code> goes <code>true</code> and back to <code>false</code> before a frame can be drawn. Use a scope tied to a lifecycle, and move the disk write to <code>Dispatchers.IO</code> with <code>withContext</code>. Why was <code>delay(800)</code> never the right fix for "nothing gets lost"?</details>

- [ ] **33. The History badge leaks a listener every time the top bar recomposes** *(easiest to see once #31 is fixed)*
  - Steps: run `adb logcat -s HistoryManager`. Do a calculation, switch between Calc and Settings a few times, then do another calculation.
  - Expected: `Notifying 1 listeners` every time.
  - Actual: the number keeps growing (1 → 7 → 13 …).
  <details><summary>Hint</summary>Two problems. <code>awaitClose { }</code> doesn't unregister the listener. And <code>HistoryManager.changes()</code> creates a <b>new</b> <code>Flow</code> on every recomposition, so <code>collectAsState</code> restarts the collection each time. Remove the listener in <code>awaitClose</code>, and expose one shared flow (for example <code>stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), …)</code>), or <code>remember</code> it. Bonus: <code>listeners</code> is an <code>ArrayList</code> changed on the main thread and iterated on a background thread.</details>

- [ ] **34. History stats take about 1.5 s to load when they should take about 0.5 s**
  - Steps: do a calculation, then open History and time the "Calculating stats…" text.
  - Expected: roughly the time of one `slowStat` call (about 500 ms), because the three stats are independent.
  - Actual: about 1.5 s. They run one after another.
  <details><summary>Hint</summary><code>async { … }.await()</code> on the same line is just sequential code with extra steps. Start all three, then <code>await</code> them, or use <code>awaitAll</code>. Also, is <code>Dispatchers.IO</code> the right dispatcher for CPU work?</details>

- [ ] **35. History stats never settle: "Calculating stats…" keeps coming back**
  - Steps: open History and watch the stats line for a few seconds.
  - Expected: it's computed once, then only again when the history changes.
  - Actual: it cycles between "Calculating stats…" and the result forever, recomputing all the time.
  <details><summary>Hint</summary><code>scope.launch</code> is called directly in the <code>HistoryStats</code> composable body. Every write to <code>stats</code> causes a recomposition, which starts another coroutine. Side effects belong in <code>LaunchedEffect(key)</code> or in event callbacks, and <code>rememberCoroutineScope</code> is for events such as clicks.</details>

- [ ] **36. Opening History crashes even though the stats code has a `try/catch`** *(reproduce this before fixing #21)*
  - Steps: `9`, `9`, `9`, `+`, `1`, `=` (result `1,000`), then open History.
  - Expected: the stats line shows "Stats unavailable", which is what the `catch` is for, and the app keeps working.
  - Actual: the app crashes with `NumberFormatException: For input string: "1,000"`.
  <details><summary>Hint</summary>An exception inside a child <code>async</code> cancels its <b>parent</b> straight away. That happens whether or not someone calls <code>await()</code>, so a <code>try/catch</code> around <code>await()</code> can't stop it. Handle the error inside the <code>async</code> block, or wrap the children in <code>supervisorScope { }</code>, and know why each works. This is a very common interview question: "what's the difference between exceptions in <code>launch</code> and in <code>async</code>?"</details>

- [ ] **37. After one failed background save, nothing is ever saved again**
  - Steps: `5`, `÷`, `0`, `=` (result `Infinity`), then `M+`. Then do `2`, `+`, `2`, `=`. Force-stop and reopen the app.
  - Expected: the Infinity memory write is rejected, but `2+2 = 4` is still saved to history.
  - Actual: `2+2 = 4` is gone after the restart, and so is every later calculation. Logcat shows a single `AppScope: Background work failed … Can't save Infinity to memory`.
  <details><summary>Hint</summary><code>appScope</code> is built on a plain <code>Job()</code>. When one child fails, the whole scope is cancelled, and every later <code>appScope.launch</code> silently does nothing. The <code>CoroutineExceptionHandler</code> only logs the failure, which hides the problem. <code>SupervisorJob()</code> keeps the siblings alive. Also ask: should a <code>require</code> run inside a fire-and-forget coroutine at all, or should the input be checked before launching?</details>

## Dependency injection (Hilt)

The app is only partly moved to Hilt. `CalcApp` is annotated `@HiltAndroidApp`, and there are modules, qualifiers and an entry point in `Di.kt`, but most of the code still reaches into global objects. These bugs cover scoping, qualifiers, `@Binds`, entry points, and who actually creates an object.

> ⚠️ **#38 crashes the Settings screen.** You'll need to fix it before you can work on #6, #16, #18, #39 or #40.

- [ ] **38. Opening Settings crashes the app**
  - Steps: tap **Settings**.
  - Expected: the Settings screen opens.
  - Actual: `IllegalStateException: Given component holder class MainActivity does not implement interface dagger.hilt.internal.GeneratedComponent …`
  <details><summary>Hint</summary><code>hiltViewModel()</code> needs the hosting Activity to be part of Hilt's component tree. Which annotation is missing, and why does this fail at runtime instead of at compile time?</details>

- [ ] **39. "This session" in Settings always says 0 calculations and 0 errors**
  - Steps: do a calculation and trigger an error (`5`, `+`, `÷`, `2`, `=`), then open Settings.
  - Expected: `This session: 1 calculations, 1 errors`.
  - Actual: `This session: 0 calculations, 0 errors`.
  <details><summary>Hint</summary><code>SessionTracker</code> is annotated <code>@Singleton</code>, so why isn't it one? Look at <code>AppModule</code>: an explicit <code>@Provides</code> takes priority over the <code>@Inject constructor</code>, and that <code>@Provides</code> has no scope. Every injection and every <code>appEntryPoint().sessionTracker()</code> call gets a new instance. The fix can be deleting code. Follow-up questions: what does "scoped" actually mean in Dagger, and what's the difference between <code>Provider&lt;T&gt;</code> and <code>Lazy&lt;T&gt;</code>?</details>

- [ ] **40. The "Haptic feedback" setting resets after restarting the app**
  - Steps: Settings → turn off Haptic feedback → force-stop and reopen → Settings.
  - Expected: it's still off.
  - Actual: it's back on.
  <details><summary>Hint</summary>Look at what <code>SettingsModule</code> <code>@Binds</code> <code>SettingsStore</code> to. Test and preview fakes shouldn't be bound in the production graph. They belong in the test source set, swapped in with <code>@TestInstallIn</code> or <code>@BindValue</code>.</details>

- [ ] **41. Long-pressing the display to copy the result crashes the app**
  - Steps: do a calculation, then long-press the big number.
  - Expected: the result is copied and a "Copied …" toast appears.
  - Actual: `UninitializedPropertyAccessException: lateinit property clipboardHelper has not been initialized`.
  <details><summary>Hint</summary>Putting <code>@Inject</code> on a field does nothing unless <b>Hilt</b> creates the object, or an <code>@AndroidEntryPoint</code> injects it. <code>CalculatorViewModel</code> is created with <code>remember { CalculatorViewModel() }</code>. The proper fix is <code>@HiltViewModel</code> with constructor injection plus <code>hiltViewModel()</code>, which also fixes #26. Why is constructor injection better than field injection?</details>

- [ ] **42. After fixing #41, copying still crashes, now when the toast is shown** *(you'll only see this after fixing #41)*
  - Steps: long-press the result.
  - Expected: the "Copied …" toast appears.
  - Actual: `NullPointerException: Can't toast on a thread that has not called Looper.prepare()`.
  <details><summary>Hint</summary>Read <code>AppModule</code> carefully. The function names say one thing, but the qualifiers say another. <code>@MainDispatcher</code> actually provides <code>Dispatchers.IO</code>, and the clipboard write is running on Main. How could a test catch a swapped binding like this?</details>

## Room

**Long-press `MR`** to open **Constants**: you can save named numbers (for example `tax = 0.16`) and tap one to insert it into the expression. The constants live in a Room database (`ConstantsDb.kt`), and the dialog is in `ConstantsDialog.kt`. These bugs cover column types, threading rules, database instances and invalidation, transactions, and migrations.

> ⚠️ **#45 makes the list lag behind the database.** After you add, edit or delete a constant, close the dialog and open it again to see what's really stored, or fix #45 first. Also, the calculator clears itself 30 seconds after it opens (#30), so do the calculator part of each test quickly.

- [ ] **43. Tapping a saved constant inserts a slightly wrong number**
  - Steps: long-press `MR` → Name `tax`, Value `0.16` → Save → Close. Long-press `MR` again, tap `tax = 0.16`, then press `×`, `1`, `0`, `0`, `=`.
  - Expected: `0.16` is inserted and the result is `16`.
  - Actual: `0.1599999964237213` is inserted and the result is `15.999999642372131`, even though the list says `0.16`. Big values lose digits too: `1234567.89` is saved as `1234567.9`.
  <details><summary>Hint</summary>Look at the type of <code>value</code> in the <code>Constant</code> entity. How many significant digits does a <code>Float</code> have, and why does the list still look right? Follow-up questions: SQLite stores both <code>Float</code> and <code>Double</code> as <code>REAL</code>, so does changing the Kotlin type need a migration, and what happens to rows that are already saved? How would you store money exactly?</details>

- [ ] **44. Long-pressing a constant to delete it crashes the app**
  - Steps: save any constant, reopen the dialog, then long-press the constant.
  - Expected: it's deleted.
  - Actual: `IllegalStateException: Cannot access database on the main thread since it may potentially lock the UI for a long period of time.` (After you fix this, the row may still show until you reopen the dialog. That's #45.)
  <details><summary>Hint</summary>Compare <code>deleteById</code> with <code>insert</code> in <code>ConstantsDao</code>, and look at where <code>Constants.delete</code> is called from. Why does Room check this at runtime? Why is <code>allowMainThreadQueries()</code> the wrong answer? Bonus: a destructive action with no confirmation and no undo.</details>

- [ ] **45. A new constant doesn't show up in the list until you reopen the dialog**
  - Steps: run `adb logcat -s ConstantsDb`. Long-press `MR`, add `tax` = `0.16`, then Save.
  - Expected: `tax = 0.16` appears in the list straight away.
  - Actual: the form clears but the list still says "No constants yet". Close and reopen the dialog and it's there. Logcat prints `Opening constants database` on every open and every save.
  <details><summary>Hint</summary>How many <code>RoomDatabase</code> objects exist? A Room <code>Flow</code> only re-runs its query when a write goes through <b>the same</b> database instance, because change tracking lives inside that instance. Look at <code>ConstantsModule</code> and compare it with bug #39. Follow-up questions: what does <code>enableMultiInstanceInvalidation()</code> do, and why isn't it the fix here? What does each extra instance cost?</details>

- [ ] **46. Renaming a constant to a name that's already taken deletes it**
  - Steps: save `tax` = `0.16` and `vat` = `0.08`. Reopen the dialog, tap **Edit** next to `tax`, change the name to `vat`, then tap Update. Close and reopen the dialog.
  - Expected: a "vat already exists" message, and both constants are unchanged.
  - Actual: the message appears, but `tax` is gone.
  <details><summary>Hint</summary><code>Constants.update</code> does two separate writes. What happens to the first one when the second one throws? Look into <code>@Transaction</code> and <code>withTransaction</code>. Also ask whether "delete, then insert" is the right way to edit a row at all: what happens to its <code>id</code>, and what would <code>@Update</code> do? Interview follow-up: why would switching the insert to <code>OnConflictStrategy.REPLACE</code> hide the message but silently delete <code>vat</code> instead?</details>

- [ ] **47. After updating the app, opening Constants crashes**
  - Steps: this needs an old build installed first. The first release of the feature had database version 1 and no index on `name`. Its schema is in `app/schemas/…ConstantsDatabase/1.json`.
    1. In `ConstantsDb.kt`, temporarily change `version = 2` to `version = 1`, and remove `indices = [Index(value = ["name"], unique = true)]` from `@Entity`.
    2. `./gradlew installDebug`, open the app, and save a constant.
    3. Undo both edits and run `./gradlew installDebug` again. Don't uninstall the app.
    4. Long-press `MR`.
  - Expected: the dialog opens, and the saved constant is still there.
  - Actual: `IllegalStateException: A migration from 1 to 2 was required but not found.`
  <details><summary>Hint</summary>Someone added the unique index and bumped the version, but never told Room how to move existing users from 1 to 2. Look at <code>Migration</code> and <code>addMigrations</code>, or <code>@AutoMigration</code>, which is why the old schema JSON is checked in. Why is <code>fallbackToDestructiveMigration()</code> not a fix for users' data? And what should a migration do if an old database already has two constants with the same name? How would you test a migration without reinstalling by hand? (Look at <code>MigrationTestHelper</code>.)</details>

**Bad practices in this area**

- [ ] `Constants` is a global object that fetches the DAO through an entry point (built on `MainActivity.instance`) on every call.
- [ ] The composable talks to the DAO directly. There's no repository and no ViewModel, and the query `Flow` is created in the UI.
- [ ] `ConstantsDao` mixes `suspend` and blocking functions without a reason.
- [ ] Validation by catching `SQLiteConstraintException` in the UI, instead of checking first or returning a result.
- [ ] `valueText.toFloatOrNull() ?: 0f` silently saves `0` for bad input, and the Value field doesn't use a number keyboard.
- [ ] The Room entity is used directly as the UI model, and the database name is a hard-coded string.
- [ ] `showConstants` is global mutable state, and the only way to open the dialog is a hidden long-press with no accessibility action.

**Stretch goals**

- [ ] Write a `MigrationTestHelper` test for version 1 → 2 using the exported schemas in `app/schemas`.
- [ ] Write DAO tests with `Room.inMemoryDatabaseBuilder`, including one that proves the edit flow (#46) is atomic.
- [ ] Add a `ConstantsRepository` and a `@HiltViewModel` that exposes the list as a `StateFlow` with `stateIn`.
- [ ] Replace delete-then-insert with `@Update` or `@Upsert`, and explain when each one is right.

## Navigation Compose

The top level of the app still uses the `currentScreen` string (see #8). Inside History, though, there is a small Navigation Compose flow: `HistoryScreen` hosts a `NavHost` with the list as the start destination, and the `›` button on each row opens an entry screen with the expression, the result and a "Delete entry" button. Tapping the row text itself still loads the result into the calculator (#25). These bugs cover route arguments, the back stack, `popBackStack`, ViewModel scoping and observing the current destination.

> ⚠️ On the entry screen, #51 means you only ever see the **first** entry you opened since the app started. To try #48 on several entries, close the app with Back or force-stop it between tries, or fix #51 first.

- [ ] **48. The entry screen shows a slightly different result than the list** *(the crash part only shows after fixing #36, and before fixing #21)*
  - Steps: `0`, `.`, `1`, `+`, `0`, `.`, `2`, `=`. Open History and tap `›` on that row. Also try `1`, `÷`, `3`, `=`.
  - Expected: the entry screen shows the same result as the list, `0.30000000000000004`.
  - Actual: it shows `0.30000001192092896`. `1÷3` shows `0.3333333432674408` instead of `0.3333333333333333`. And once #36 is fixed, tapping `›` on `999+1 = 1,000` crashes with `IllegalArgumentException: Navigation destination that matches route detail/999+1/1,000 cannot be found in the navigation graph`.
  <details><summary>Hint</summary>Look at how the route is built in <code>HistoryList</code> and how the <code>result</code> argument is declared in <code>HistoryScreen</code>. The formatted display text is squeezed into a <code>Float</code>, which has about 7 significant digits, and <code>1,000</code> isn't a valid float at all, so the route doesn't match any destination. What should a route carry: display text, or a stable ID the next screen can use to look the data up? Also think about characters like <code>/</code>, <code>?</code>, <code>#</code> and <code>%</code> in a string route, and how type-safe routes (<code>@Serializable</code> classes) change this.</details>

- [ ] **49. A quick double tap on `›` opens the entry screen twice**
  - Steps: History → double-tap `›` on any row quickly → press system Back.
  - Expected: one Back press returns to the list.
  - Actual: you're still on the entry screen. You need to press Back twice.
  <details><summary>Hint</summary>Every tap calls <code>navigate()</code>, and nothing stops the second one while the first transition is still running. Compare <code>launchSingleTop = true</code> with ignoring clicks unless the current back stack entry is <code>RESUMED</code>. Which one fits a detail screen whose argument changes? Interviewers like this one because it's a real crash-report and QA favourite.</details>

- [ ] **50. Deleting an entry leaves a blank History screen**
  - Steps: History → `›` on any row → Delete entry.
  - Expected: you're back on the list, and the entry is gone.
  - Actual: everything under the "All calculations" label is empty. Back exits the app. The list only comes back after you switch to Calc and back to History.
  <details><summary>Hint</summary>Read the <code>popBackStack</code> call in <code>HistoryDetail</code>. What does <code>inclusive = true</code> do to the start destination, and what does a <code>NavHost</code> draw when its back stack is empty? Be ready to explain the difference between <code>popBackStack()</code>, <code>popBackStack(route, inclusive)</code>, <code>navigateUp()</code> and <code>navigate(…) { popUpTo(…) }</code>.</details>

- [ ] **51. The entry screen always shows the first entry you opened**
  - Steps: do `2+2=` and `5×6=`. History → `›` on `2+2 = 4` → Back → `›` on `5×6 = 30`.
  - Expected: the second entry screen shows `5×6` and `30`.
  - Actual: it still shows `2+2` and `4`. Rotating doesn't help. It only resets when you leave the app with Back or the process dies.
  <details><summary>Hint</summary>Where is <code>HistoryDetailViewModel</code> created, and which <code>ViewModelStoreOwner</code> is in scope there? A <code>viewModel()</code> call outside the <code>NavHost</code> is owned by the Activity, so every entry screen shares one instance, and the "don't load twice" guard does the rest. Inside a <code>composable { }</code> destination, the owner is the <code>NavBackStackEntry</code>. When do you want each scope? Also: why is reading the argument from <code>SavedStateHandle</code> better than passing it into a <code>load()</code> function?</details>

- [ ] **52. The "‹ Back" link and the "Calculation" title never appear on the entry screen**
  - Steps: History → `›` on any row. Rotate the device too.
  - Expected: the label above the content changes to "Calculation", and a "‹ Back" link appears next to it.
  - Actual: it always says "All calculations", and there's no "‹ Back" link.
  <details><summary>Hint</summary><code>HistoryHeader</code> reads <code>navController.currentDestination</code> during composition. That's a plain property, not Compose state, so nothing recomposes the header when you navigate. And on the first composition the graph isn't even set yet. Which <code>NavController</code> API gives you the current back stack entry as <code>State</code>?</details>

**Bad practices in this area**

- [ ] String routes (`"detail/{expression}/{result}"`) are built by hand with string concatenation and compared as strings in several places. Use `@Serializable` route classes (type-safe navigation).
- [ ] `NavController` is passed down into `HistoryList` and `HistoryDetail`. Screens should take lambdas such as `onOpenEntry(id)` and `onBack()`, so they can be previewed and tested without navigation.
- [ ] `HistoryDetailViewModel` has no `SavedStateHandle` and gets its data through a `load()` call from a `LaunchedEffect`, with a hand-rolled `loaded` flag.
- [ ] Deleting an entry changes `HistoryManager.items` straight from a click handler in the UI.
- [ ] Two navigation systems live side by side: the global `currentScreen` string and a nested `NavHost`.

**Stretch goals**

- [ ] Replace `currentScreen` with a single app-level `NavHost` (Calc, History, Settings) and keep History's detail as a nested graph. Make sure Back works everywhere (#8).
- [ ] Write a Compose UI test with `TestNavHostController` that opens an entry, deletes it, and checks that the back stack still holds the list.

## Testing

The app now has a small test suite, but the tests have bugs of their own. Some fail for the wrong reason, some pass when they shouldn't, and one can't even start. These bugs are in the **test code and test setup**. The fix is usually a better test, sometimes plus a change that makes the app code testable.

Run the JVM unit tests with `./gradlew testDebugUnitTest` (report: `app/build/reports/tests/testDebugUnitTest/index.html`). Run the device tests with an emulator or device connected: `./gradlew connectedDebugAndroidTest` (report: `app/build/reports/androidTests/connected/debug/index.html`). Add `--tests "*CalculatorTest*"` to run one unit test class, or `-Pandroid.testInstrumentationRunnerArguments.class=com.interviewprep.brokencalc.SessionTrackerTest` to run one device test class.

> ⚠️ **Some failures are honest.** These tests are correct and fail because of app bugs, so they are *not* test bugs: `CalculatorTest.subtractionIsLeftToRight` and `divisionIsLeftToRight` fail until you fix #14, and `CalculatorTest.pointOnePlusPointTwo` fails until you fix #15. `SessionTrackerTest.sessionTrackerIsASingleton` fails until you fix #39, but you'll only get that far after fixing #57.

- [ ] **53. `CalculatorFormatTest` crashes before it checks anything**
  - Steps: `./gradlew testDebugUnitTest --tests "*CalculatorFormatTest*"`.
  - Expected: `format(4.0)` returns `"4"` and `format(0.5)` returns `"0.5"`, so both tests pass.
  - Actual: both tests fail with `kotlin.UninitializedPropertyAccessException: lateinit property prefs has not been initialized`. The fix should let `Calculator.format` run in a plain JVM test with no Activity, and let the test choose the number of decimal places.
  <details><summary>Hint</summary>Follow the stack trace into <code>Calculator.format</code>. What does it read, and who sets that? A function that reaches into a static <code>lateinit</code> has a hidden input the test can't see or control. Make the input explicit, for example a parameter. Resist "fixing" the test by assigning <code>MainActivity.prefs</code> a fake: that global then leaks into every other test in the same JVM. Interview angle: "what makes code hard to unit test?"</details>

- [ ] **54. `divideByZeroIsAnError` passes even though `5 ÷ 0` gives `Infinity`**
  - Steps: `./gradlew testDebugUnitTest --tests "*CalculatorTest.divideByZeroIsAnError*"`. Then press `5`, `÷`, `0`, `=` in the app (#9).
  - Expected: the test fails until #9 is fixed, with the message `5÷0 should not give a number`.
  - Actual: the test passes, although the app is broken. The fix should make the test fail today and pass only once dividing by zero is really an error.
  <details><summary>Hint</summary>What does <code>fail()</code> actually do to stop a test? And what is the <code>catch</code> block catching? A test that can't fail is worse than no test, because it gives you false confidence. Check that a test fails at least once before you trust it. Look at <code>assertThrows</code>, and think about which exact exception #9's fix should throw.</details>

- [ ] **55. `previewShowsResultWhileTyping` fails even though the test "skips" the delay**
  - Steps: `./gradlew testDebugUnitTest --tests "*CalculatorViewModelTest*"`.
  - Expected: after `2`, `+`, `3` and `advanceUntilIdle()`, `vm.preview` is `"5"`.
  - Actual: `org.junit.ComparisonFailure: expected:<[5]> but was:<[]>`. It still fails after you fix #53. The fix should make the test control the preview coroutine, so it passes right away with no real waiting and no `Thread.sleep`.
  <details><summary>Hint</summary>The test sets <code>Dispatchers.setMain</code> and uses <code>runTest</code>, so it looks right. But which scope and dispatcher does <code>updatePreview()</code> actually launch on? Virtual time only controls coroutines that run on the test's scheduler. The ViewModel has to let the test in, for example with <code>viewModelScope</code> or an injected dispatcher. Interview angle: "how do you test code that uses <code>delay</code>?" A common follow-up is a reusable <code>MainDispatcherRule</code>.</details>

- [ ] **56. `tenMinusFiveMinusTwoIsThree` passes even though the app shows `7`**
  - Steps: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.interviewprep.brokencalc.CalculatorScreenTest`. Then do `10 − 5 − 2 =` by hand (#14).
  - Expected: the test fails until #14 is fixed.
  - Actual: both UI tests pass, although the app is broken. The fix should check the number on the **display**, and should wait until the result has arrived.
  <details><summary>Hint</summary>Which node does <code>onNodeWithText("3")</code> find? Look at the screen: how many things say "3"? Once the finder is fixed, try it again: what does the display show at the moment of the check? <code>waitForIdle()</code> only waits for Compose and Espresso. It knows nothing about a <code>GlobalScope</code> coroutine with a <code>delay</code>. Look at <code>Modifier.testTag</code>, <code>onNodeWithTag</code>, <code>waitUntil</code>, and idling resources.</details>

- [ ] **57. The Hilt test can't start: "cannot use a @HiltAndroidApp application"**
  - Steps: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.interviewprep.brokencalc.SessionTrackerTest`.
  - Expected: Hilt injects two `SessionTracker`s and the test runs its assertion.
  - Actual: `IllegalStateException: Hilt test, com.interviewprep.brokencalc.SessionTrackerTest, cannot use a @HiltAndroidApp application but found com.interviewprep.brokencalc.CalcApp.` The fix should let the test's assertion run. It will then fail honestly because of #39.
  <details><summary>Hint</summary>Which <code>Application</code> class does the test runner create? Read <code>HiltTestRunner</code> and the <code>testInstrumentationRunner</code> line in <code>app/build.gradle.kts</code>. Hilt tests need a test application so every test gets a fresh component that it can replace parts of. Heads up: once every test runs on that application, <code>CalculatorScreenTest</code> starts failing with <code>The component was not created</code>. Why? Buttons call <code>appEntryPoint()</code>. What does that tell you about service locators?</details>

**Bad practices in this area**

- [ ] Tests reach into global singletons (`Calculator`, `HistoryManager`, `Memory`) and never reset them, so the order they run in could change the result.
- [ ] No `MainDispatcherRule`: every coroutine test class sets and resets `Dispatchers.Main` by hand.
- [ ] UI tests find nodes by visible text instead of `testTag` or content description, so they break when copy changes and can match the wrong node.
- [ ] `assertEquals(Double, Double, 0.0)`: an exact comparison of floating-point numbers.
- [ ] Only happy paths are tested. There are no tests for edge cases such as empty input, `⌫` on an empty display, or `=` pressed twice.
- [ ] No fakes for storage: nothing in the app can run in a test without a real `SharedPreferences`.

**Stretch goals**

- [ ] Write a `MainDispatcherRule` and use it in every ViewModel test.
- [ ] Parameterise the `Calculator` tests (JUnit `Parameterized`) so each math bug is one row in a table.
- [ ] Make every UI test a `@HiltAndroidTest` and replace `SettingsStore` with `@BindValue` or `@TestInstallIn`.
- [ ] Add screenshot or golden tests for the keypad in light and dark mode.

## Networking (Retrofit / OkHttp)

The `$` button in the top bar opens a small currency converter. It converts the number on the display from USD, EUR or MXN using the free [Frankfurter](https://frankfurter.dev) API. It's built with Retrofit, OkHttp and kotlinx.serialization, and wired up with Hilt in `NetworkModule.kt`. Watch the traffic with `adb logcat -s okhttp.OkHttpClient`. The exact rates change every day, so your numbers will differ from the ones below.

> ⚠️ **#58 crashes the converter as soon as it opens.** Fix it first. #59–#62 only show up after that.

- [ ] **58. Opening the currency converter crashes the app**
  - Steps: type `100`, then tap `$` in the top bar.
  - Expected: a dialog shows 100 USD in EUR and MXN.
  - Actual: the app crashes with `SecurityException: Permission denied (missing INTERNET permission?)`.
  <details><summary>Hint</summary>Which permission does every app that talks to a server need, and where is it declared? It's a "normal" permission, so there's no runtime prompt. Why does this only fail at runtime? Bonus: on older Android versions the same mistake can show up as an <code>IOException</code> instead (for example <code>SocketException: socket failed: EPERM</code>), and then the dialog's <code>catch</code> would show "No internet connection" on a phone that is online. Why is that message misleading?</details>

- [ ] **59. Tapping "Retry" in the converter crashes the app** *(you'll only see this after fixing #58)*
  - Steps: turn on airplane mode (`adb shell cmd connectivity airplane-mode enable`). Type `100`, tap `$`. The dialog shows "No internet connection". Tap **Retry**. Turn airplane mode off again afterwards (`… airplane-mode disable`).
  - Expected: it tries again and shows the error again, without freezing or crashing.
  - Actual: the app crashes with `android.os.NetworkOnMainThreadException`.
  <details><summary>Hint</summary>Compare how the <code>LaunchedEffect</code> calls <code>loadRates</code> with how the Retry button calls it. Which thread runs a click handler? <code>Call.execute()</code> blocks the calling thread. Interview follow-ups: <code>execute()</code> vs <code>enqueue()</code> vs a <code>suspend</code> function in the Retrofit interface, and where retry logic should live (a ViewModel with <code>viewModelScope</code>, not the composable).</details>

- [ ] **60. Converting `0` crashes the app** *(you'll only see this after fixing #58)*
  - Steps: force-stop and reopen the app, so nothing is cached. With the display at `0`, tap `$`. Also try it with an unfinished expression such as `5+3` on the display.
  - Expected: a friendly message such as "Enter a number above 0", ideally without sending a request at all.
  - Actual: the app crashes with `NullPointerException` in `loadRates`. The OkHttp log shows `<-- 422` and `{"message":"invalid amount"}`.
  <details><summary>Hint</summary>What does <code>Response.body()</code> return when the status code isn't 2xx? <code>!!</code> turns that into a crash, and the <code>catch</code> only handles <code>IOException</code>. Also, why is the amount <code>0</code> for <code>5+3</code>? Look at <code>toDoubleOrNull() ?: 0.0</code>. Interview angle: with a <code>suspend</code> Retrofit function you'd get an <code>HttpException</code> instead. How would you turn network errors, HTTP errors and bad input into one UI state (Loading / Success / Error)?</details>

- [ ] **61. Switching the converter to EUR or MXN crashes the app** *(you'll only see this after fixing #58)*
  - Steps: type `100`, tap `$`, then tap **EUR** (or **MXN**) at the top of the dialog.
  - Expected: it shows 100 EUR in USD and MXN.
  - Actual: the app crashes with `kotlinx.serialization.MissingFieldException: Field 'EUR' is required for type with serial name '…Rates', but it was missing at path: $.rates` (`'MXN'` if you tapped MXN).
  <details><summary>Hint</summary>Put the response body from the OkHttp log next to the <code>Rates</code> class. Which currency does the API leave out, and which field is required? Why does it work for USD? Why doesn't the <code>catch (e: IOException)</code> catch it? Interview angle: <code>ignoreUnknownKeys</code> only helps with <i>extra</i> keys, not <i>missing</i> ones. How would you model a JSON object whose keys depend on the request?</details>

- [ ] **62. Converting a different number shows the old result** *(you'll only see this after fixing #58)*
  - Steps: force-stop and reopen the app. Type `100`, tap `$` (shows about `1812 MXN`), then **Close**. Press `C`, type `5`, tap `$` again.
  - Expected: about `90 MXN` for 5 USD.
  - Actual: the title says "Convert 5", but it still shows the result for 100. The OkHttp log shows no new request.
  <details><summary>Hint</summary>The request includes the amount, so what is <code>rateCache</code> keyed by? A cache key has to include everything that changes the response. The other option is to cache the rate for 1 unit and multiply locally. Also: when does this cache ever expire, and is a plain <code>HashMap</code> safe when it's written from <code>Dispatchers.IO</code> and read from the main thread? Interview angle: in-memory cache vs OkHttp's HTTP cache (<code>Cache-Control</code>) vs a database, and cache invalidation.</details>

**Bad practices in this area**

- [ ] The `@Provides` functions in `NetworkModule` aren't `@Singleton`, so every `ratesApi()` call builds a new `Retrofit` and a new `OkHttpClient`, each with its own connection pool and threads.
- [ ] `HttpLoggingInterceptor.Level.BODY` is on in every build, release too. It logs full responses (a privacy risk) and slows requests down. Tie it to `BuildConfig.DEBUG`.
- [ ] A blocking `Call<T>` with `execute()`, wrapped in `withContext(Dispatchers.IO)`, instead of a `suspend` function in the Retrofit interface.
- [ ] The composable fetches its own dependencies with `EntryPointAccessors` and does the networking itself. There's no ViewModel, no repository, and no single UI state (three separate `var`s instead).
- [ ] Global mutable state again: `showConverter`, `converterAmount` (written during composition in `CalculatorScreen`) and a global `rateCache` that never expires.
- [ ] Every `IOException` is shown as "No internet connection". Nothing checks connectivity, and the real cause is only in logcat.
- [ ] `Double` for money, `String.format` without a `Locale`, and a hard-coded base URL, currency list, strings and colours.

**Stretch goals**

- [ ] Write JVM tests for `RatesApi` with `MockWebServer` (already a test dependency): a 200 response, a 422, the EUR-based body from #61, and malformed JSON.
- [ ] Move the converter to a `@HiltViewModel` that exposes `StateFlow<ConverterUiState>`, with `suspend` Retrofit calls and a repository that owns the cache.
- [ ] Show the last cached rates with their date when offline, and use `ConnectivityManager` to tell "offline" apart from other errors.

## Lifecycle and process death

<!-- placeholder: bugs #63–#67 (lifecycle) -->

## Performance and recomposition

<!-- placeholder: bugs #68–#72 (performance) -->

## WorkManager

<!-- placeholder: bugs #73–#77 (workmanager) -->

## Release builds and R8

<!-- placeholder: bugs #78–#82 (r8) -->

## Accessibility and i18n

<!-- placeholder: bugs #83–#87 (a11y-i18n) -->

---

## Bad practices to refactor

These aren't user-visible bugs, but an interviewer will notice them. Being able to explain why each one matters is as important as fixing it.

- [ ] Static `MainActivity.instance` (Activity leak, hidden with `@SuppressLint("StaticFieldLeak")`).
- [ ] Global mutable state (`currentScreen`, `selectedHistoryItem`, `Memory`, `HistoryManager`) instead of state owned by a ViewModel and passed down.
- [ ] `GlobalScope` and `@OptIn(DelicateCoroutinesApi::class)` used to silence the warning.
- [ ] A hand-rolled global `appScope` that's never cancelled, instead of `viewModelScope` or a lifecycle-aware or injected application scope.
- [ ] Dispatchers are hard-coded everywhere (`Dispatchers.IO` for CPU work, `Default` for a timer), so the code can't be tested with a `TestDispatcher`.
- [ ] Fake `delay`s and `Random` latency used to make things "feel" async.
- [ ] `catch (e: Exception)` inside coroutines, which swallows cancellation.
- [ ] `appEntryPoint()` acts as a service locator, and it's built on the static `MainActivity.instance`. Entry points are for code Hilt can't reach, not a shortcut to avoid constructor injection.
- [ ] `EntryPointAccessors` is called on every button tap (`CalcButton`) and every calculation.
- [ ] Half-migrated DI: `HistoryManager`, `Memory`, `appScope` and `MainActivity.prefs` are still global objects, and SharedPreferences is reached two different ways.
- [ ] An `@Provides` that duplicates an `@Inject constructor`, and fakes (`InMemorySettingsStore`) living in the `main` source set.
- [ ] Every module, qualifier and entry point is in one `Di.kt` file. `object` modules and `abstract` modules are mixed without a reason.
- [ ] `SharedPreferences.commit()` on the main thread, and on **every recomposition** in `SettingsScreen`.
- [ ] Side effects (prefs writes, state writes) directly in composable bodies.
- [ ] Navigation with magic strings and an `if/else` chain. No type safety, no back stack.
- [ ] Stringly-typed button handling (`onButton(label: String)` with a giant `when`). Use a sealed class or enum for actions.
- [ ] Business logic, persistence and UI mixed together. `Calculator.format` reads SharedPreferences directly. `History.kt` has both storage and UI.
- [ ] Composables take the whole `CalculatorViewModel` instead of state plus lambdas (hard to preview and test).
- [ ] Composables have no `modifier: Modifier = Modifier` parameter, and `height: Int` is passed around instead of `Dp`.
- [ ] Hard-coded strings (no `strings.xml`), colours, dimensions and font sizes everywhere. The app name is hard-coded in the manifest.
- [ ] Hard-coded colours instead of `MaterialTheme.colorScheme`. No typography or shapes in the theme.
- [ ] `Box` + `clickable` for buttons: no button role for accessibility, no ripple customisation, `⌫` has no content description, touch targets are fixed.
- [ ] `Column` + `verticalScroll` + `for` loop for a list that can grow without limit. Use `LazyColumn` with stable `key`s.
- [ ] `items.reversed()` allocates a new list on every recomposition.
- [ ] `!!` everywhere, `lateinit` statics, and a `catch` that's too narrow in one place and silent fallbacks (`?: 0.0`) in others.
- [ ] Java-style Kotlin: `for (i in 0 until expr.length)`, `"" + x` concatenation, `if (x) return a else return b`, `ArrayList` instead of `List`, `var` where `val` would do.
- [ ] `String.format` without a `Locale`.
- [ ] No `@Preview`s.
- [ ] No tests at all.
- [ ] No version catalog (`libs.versions.toml`). Versions are hard-coded in the Gradle files.
- [ ] Manifest uses a platform `android:style` theme and the default system icon. `allowBackup="true"` with no backup rules.

## Stretch goals

- [ ] Add unit tests for `Calculator` (tokenizer, parser, formatter) covering every math bug above.
- [ ] Add a Compose UI test for the history → calculator flow (#25) and the input-preservation bug (#26).
- [ ] Inject dispatchers into the ViewModel, then test the preview (#29), idle clear (#30) and stats (#34) with `runTest`, `StandardTestDispatcher` and virtual time (`advanceTimeBy`), so the tests don't actually wait.
- [ ] Move to a single `CalculatorUiState` data class exposed as `StateFlow` and collected with `collectAsStateWithLifecycle()`.
- [ ] Replace SharedPreferences with DataStore, and history with Room.
- [ ] Finish the Hilt migration. Constructor-inject everything, including an `@ApplicationScope` `CoroutineScope` in place of `appScope`, a `@HiltViewModel` `CalculatorViewModel`, and repositories for history, memory and settings. Then delete `appEntryPoint()` and `MainActivity.instance`.
- [ ] Write Hilt tests: use `HiltAndroidRule` with a custom test runner, replace `SettingsStore` with `@TestInstallIn` or `@BindValue`, and inject a `TestDispatcher` through the dispatcher qualifiers.
- [ ] Use Navigation Compose with type-safe routes.
- [ ] Add a landscape layout, for example a scientific keypad.
