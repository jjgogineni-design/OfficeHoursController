# Next Up: Deliverable 3, First Working Slice

CS 2114, Fall 2026, Project 3. Team: Vishnu, Luke, Jai, Gavin.

Next Up is a console office-hours queue. Students `join`, anyone can see the `line`, and the TA enters TA mode with a PIN to `peek` and call the `next` student.

## Build and run

Needs JDK 17+ and JUnit 5. All 56 tests pass in Eclipse 2023-09 (the course setup).

```
mkdir -p out
javac -d out $(find src/main -name '*.java')
java -cp out nextup.controller.ProjectRunner          # TA PIN defaults to 2114
java -cp out nextup.controller.ProjectRunner 9876     # or pass your own PIN
```

Tests (JUnit 5 console launcher): compile `src/main` and `src/test` together with the launcher jar on the classpath, then run it with `--scan-classpath out`. In an IDE, just run the `src/test` folder.

**In Eclipse:** import the project (File → Import → Existing Projects into Workspace). The included `.classpath` already sets `src/main/java` and `src/test/java` as source folders and adds JUnit 5.
- Thin view: right-click `src/main/java/nextup/controller/ProjectRunner.java` → Run As → Java Application, then type commands in the Console tab (start with `help`). The TA PIN is 2114. Eclipse's console has no `System.console()`, so the PIN is echoed there.
- Model and tests: right-click `src/test/java` → Run As → JUnit Test. The model tests (`LinkedQueueTest`, `OfficeHoursTest`) use no view, `Scanner`, or `System.in`.

MVP walkthrough: `join`, `line`, `ta` (PIN), `peek`, `next` until the line is empty, `quit`. Last run: **56 tests, 0 failed** (LinkedQueueTest 15, OfficeHoursTest 20, ConsoleViewTest 8, OfficeHoursControllerTest 13), and the walkthrough above was also run by hand through `ProjectRunner` with piped input without crashing.

## What is in this slice

| Layer | Files |
|---|---|
| Model (`nextup.model`) | `LinkedQueue`, `Ticket`, `OfficeHours`, `EmptyQueueException`, `AlreadyInLineException` |
| View (`nextup.view`) | `OfficeHoursView` (interface), `ConsoleView` |
| Controller (`nextup.controller`) | `OfficeHoursController`, `ProjectRunner` |

The model tests need no view, no `Scanner`, and no `System.in`.

## Reflection

### What changed from the specification?

No public signature from section 5 changed. The class list, ownership of rules, and validation locations in section 6 were built as written. What differs is mostly detail the spec left open, plus the tests:

- **Exception types.** The spec said "thrown" but not checked or unchecked. Both `EmptyQueueException` and `AlreadyInLineException` extend `RuntimeException`, so the controller can catch them in one place without every model method declaring `throws`.
- **Two empty-queue messages.** `LinkedQueue` throws `EmptyQueueException("Queue is empty.")` on its own. `OfficeHours` checks `isEmpty()` first so the student-facing text is "No one is waiting." as the spec says. The queue's own message should never reach a user.
- **Name matching is a bit broader than the spec.** `getPosition` strips control characters, trims, collapses spaces, and lowercases before comparing, so "  ANN " finds "Ann". The spec said "case and spacing ignored" and this is how we read it.
- **Constants are public** (`OfficeHours.MAX_LINE`, `MAX_NAME_LENGTH`, `MAX_TOPIC_LENGTH`). The spec gave the values but not the access level. Tests use them.
- **Controller wording the spec didn't pin down.** The join prompts read "Name (or cancel): " and "Topic (or cancel): ", and cancelling (or end of input mid-join) prints "Join cancelled." and returns to the main prompt. On startup the controller prints "Welcome to Next Up, the CS 2114 office hours line." and "Type help to see the commands." before the first prompt, so the program doesn't look frozen when it starts. **Prompted by:** running `ProjectRunner` in Eclipse, where the bare `> ` prompt looked like nothing had happened. `OfficeHoursControllerTest.introPrintsBeforeFirstPrompt` checks this.
- **Tests.** The spec's optional view/controller tests used a `FakeView`. We did not build one. Instead `OfficeHoursControllerTest` drives the real `ConsoleView` with a scripted `Scanner` and checks the printed text, which covers the full MVP path and the console-facing bad-input rows from section 6 (blank and oversized input, typos, caps and spaces, duplicate joins, a student typing `next`, `next` on an empty line, wrong PIN, end of input) in one place. The model-only rows (control characters, the 50-ticket cap, `enqueue(null)`) are tested in `OfficeHoursTest` and `LinkedQueueTest`. The trade-off is that a controller bug and a view bug can show up as the same failure. We chose this because the spec's `FakeView` would only confirm which show method was called, and the text a student actually sees would still be unchecked. We also added `ConsoleViewTest` as its own file. **Prompted by:** the spec's optional test section already described building `ConsoleView` with a `Scanner` and a `ByteArrayOutputStream`, and once the controller tests ran through the real view, a formatting bug (numbering in `showLine`, the TA-only lines in `showHelp`) would have shown up as a vague controller failure. Testing the view directly tells the two apart. We also added a few cases the spec's test table didn't list: exactly 30 characters accepted, `null` constructor arguments, and `getPosition("  ")`. **Prompted by:** the "On bad input" column in section 5 of our spec. It promises IAE for a `null` PIN or clock and -1 for a blank name, and says names over 30 characters fail, but the test plan only tested a blank PIN, `null`/"Zed", and 31 characters. Each extra case tests one of those promises, and the 30-character case checks the boundary from the passing side.
- **Test setup consequence of the design.** Because `Ticket`'s constructor is package-private, `ConsoleViewTest` can't construct a `Ticket` directly. It builds them by joining through `OfficeHours`. So a view test depends on the model even though the view itself doesn't know `OfficeHours` exists. We kept the package-private constructor because the boundary matters more than test convenience, but it is a real cost of that decision.
- **Layout.** Standard `src/main/java` and `src/test/java` folders, one test package per MVC package. **Prompted by:** the spec named the three packages but never said where tests go. Mirroring the packages puts each test next to the class it tests (`OfficeHoursTest` sits in `nextup.model`), and the standard layout is one Eclipse and the command-line JUnit launcher both understand without extra setup.

### Did the model really stand alone?

Yes. A search of `nextup.model` for `import nextup` finds nothing, and the model tests run with no view. The view imports only `Ticket`, as the spec promised. Nothing had to move across the line to make the slice run.

One thing in the model still leans toward the view: **it carries user-facing wording.** Exceptions carry sentences like "Only the TA can call the next student." and "Line is full.", and `AlreadyInLineException`'s message ("You're already #3.") duplicates the text `ConsoleView.showAlreadyInLine` prints. The controller shows `e.getMessage()` straight to the user, so a different front end (or another language) would inherit this wording. It doesn't break the boundary, since the model never calls the view, but a stricter design would return error codes and let the view write the sentences. We left it as is for now.

### What happened to our unknowns?

These are the six questions from "What we don't know yet" in our Deliverable 1 scope doc, in the same order.

- **Swappable views ("If we replace ConsoleView with a GUI tomorrow, which classes have to change?"): answered on paper, unproven in code.** The spec's answer holds in the build: a GUI would replace `ConsoleView` and `OfficeHoursController`, and `LinkedQueue`, `Ticket`, and `OfficeHours` would stay the same. Nothing in `nextup.model` imports the view. We haven't built a second view, so this is still a prediction.
- **Showing the line without emptying it: figured out.** `LinkedQueue.toArray()` plus `OfficeHours.getLine()` returning a fresh `Ticket[]`. `Node` never leaves `LinkedQueue`. Tested, including that editing the returned array doesn't change the line.
- **Testing console code: figured out.** All rules live in the model, so model tests never touch `System.in`. The console pieces get an injected `Scanner` and `PrintStream`, so `ConsoleViewTest` and `OfficeHoursControllerTest` drive them with a string and check the printed text.
- **TA mode on a shared keyboard: figured out.** `ta` asks for the PIN and `student` logs out. The PIN is held only in the model, and the prompt shows `[TA] >` so a TA who forgets to log out is visible. Covered by model and controller tests.
- **Middle removal and the tail pointer (stretch): still open.** `LinkedQueue` has no remove yet. This is our Deliverable 4 change feature, and the tail case is listed in the prediction and the new tests below.
- **Testing time-based code for wait estimates (stretch): approach figured out, feature not built.** `OfficeHours` takes an injected `Clock`, and a test checks `getJoinedAt()` against a fixed instant. Wait estimates themselves aren't implemented.

Two more came up while building that weren't on the scope list:

- **Hiding the PIN: partly open.** `readSecret` uses `System.console().readPassword()` when a real console exists, but our automated tests only exercise the fallback path, where the PIN is echoed (Eclipse's console is one of those). Someone needs to check it by hand in a real terminal.
- **Identity: still open.** The scope already named this as a known limit under spam joins. The console can't verify who a student is, so someone could join under a fake name or (once leave exists) remove someone else.

### What doesn't work yet?

- No known failing tests and no known crash on the MVP path.
- **Bad input:** all nine bad-input cases from our scope doc are handled and each has at least one test, using the scope's own examples (`nxt`, `  NEXT `, `Vishnu`/`vishnu`, a 10,000-character name).
- **Not implemented:** leave the line, wait-time estimates, any GUI.
- **Not verified:** PIN masking in a real terminal (see above), and a run on a lab machine. So far it has been run in Eclipse on one teammate's laptop.
- **Not covered by tests:** `ProjectRunner` itself (it has no logic, we ran it by hand), and `System.console()` behavior.
- Names allow only letters, spaces, hyphens, and apostrophes, so a name like "Jean-Luc 2nd" or one with a period is rejected. That is what the spec says, but students may find it strict.

## Next Steps (Deliverable 4)

### 1. Finish the MVP, in order

Every MVP feature from the scope (join, line, peek, next, the empty-line message, quit, and PIN-protected TA mode) is implemented. What's left is verification and polish:

1. Run the full MVP walkthrough as a team in lab, on a real terminal, and record anything surprising. Check PIN masking specifically. *(Gavin runs, everyone watches.)*
2. Fix whatever step 1 turns up. *(Owner of the affected class.)*
3. Polish `ConsoleView` output from what we saw in lab (prompt spacing, help text). *(Jai)*
4. Cross-review: each person reads another person's class and tests before the merge. *(All; Gavin does the final merge, per the spec.)*

### 2. Change feature: leave the line

A student can remove their own spot with `leave`. This changes model state and logic (removing from the middle of a linked queue, including head and tail cases) and is reachable through the controller and view, so it isn't just a new label or a button wired to existing logic. It is also the stretch goal our spec said needs a remove-by-name operation `LinkedQueue` doesn't have.

**Prediction: what should change**

- `LinkedQueue`: new method `boolean remove(T entry)`. Walks from `head` keeping a reference to the previous node, unlinks the first node whose data equals `entry`, decrements `size`, and returns whether anything was removed. It has to handle four cases: removing `head` (move `head` forward), removing `tail` (move `tail` back to the previous node), removing the only node (set both `head` and `tail` to `null`), and a middle node. `remove(null)` throws `IllegalArgumentException`, matching `enqueue(null)`.
- `OfficeHours`: new method `void leave(String name)`. It calls `cleanName(name)` (so bad input throws the same IAE as `join`), uses the same lowercase matching as `getPosition(String)` to find the `Ticket`, and passes that exact object to `line.remove(ticket)`. If the name isn't in line it throws `IllegalArgumentException("You're not in line.")`. No TA check, since any student can leave their own spot (see the open question below).
- `OfficeHoursController`: a new `case "leave":` in `handleCommand(String)` that calls a new `private void doLeave()`. `doLeave()` reuses `promptUntilValid(String, boolean)` for the name, then calls `model.leave(name)` and `view.showInfo("You left the line.")`. The not-in-line IAE is already caught by the existing `catch (RuntimeException e)` → `view.showError(...)`. We also predict `promptUntilValid` will need a small change: it currently prints "Join cancelled." on `cancel`, so it needs the cancel message as a parameter (or a second variant) for `leave`.
- `ConsoleView`: one new line in `showHelp(boolean taMode)` listing `leave`. This is a text change, not new view logic.

**Prediction: what should not need to change**

- `OfficeHoursView`: no new methods. The existing `showInfo(String)` and `showError(String)` are enough.
- `Ticket`: no changes. `leave` removes the existing object and never builds a new one.
- `ProjectRunner`, `EmptyQueueException`, `AlreadyInLineException`: no changes.
- Existing model methods: `LinkedQueue.enqueue`, `dequeue`, `peek`, and `toArray`, and `OfficeHours.join`, `callNext`, `peekNext`, `getPosition`, `cleanName`, and `getLine` should stay as they are. `leave` should only reuse them.

If `OfficeHoursView` or `Ticket` does end up changing, that tells us our boundary was wrong, and we'll say so in the Deliverable 4 reflection.

Open design question for the group: since the console can't verify identity, should `leave` be allowed for any name, or should there also be a TA-only removal? We lean toward any name for now and listing it as a known limit.

### 3. Stretch goals

- **Committing to:** leave the line (it is the change feature).
- **Only if leave is done early:** wait-time estimate per student. The `Clock` is already injected, so it should be testable without waiting.
- **Dropping:** the GUI. We keep the design that would allow it, but won't build it.
- **Dropping:** the quick-question lane. It needs a second queue, a routing rule in `OfficeHours`, and alternation logic in `callNext`, which is a second change feature's worth of work on top of `leave`.

### 4. New tests needed

- `LinkedQueueTest` for `remove`: head, middle, tail (then `enqueue` to prove the tail pointer is right), only element, not found, `null`, empty queue, and `size()` after each.
- `OfficeHoursTest` for `leave`: middle leave shifts later positions up, case and spacing ignored, not in line, `null` and blank name, leave then rejoin works, last student leaves then a new student joins, and the line is unchanged after a failed leave.
- `OfficeHoursControllerTest`: `leave` reaches the model and prints the result, the not-in-line message appears, `leave` appears in `help`, and typing `cancel` at the leave prompt prints the leave-specific message (not "Join cancelled.").
- `ConsoleViewTest`: help text includes `leave`.

### 5. Who does what

| Item | Owner |
|---|---|
| Lab walkthrough and fixes (step 1 and 2) | Gavin runs it; owners fix their own classes |
| `LinkedQueue.remove` + its tests | Luke |
| `OfficeHours.leave` + its tests | Vishnu |
| `ConsoleView` polish, help text, view tests | Jai |
| Controller `leave` case + end-to-end tests, final merge | Gavin |
| Wait-time estimate, only if `leave` is done early | Vishnu (the `Clock` lives in `OfficeHours`) |
| Deliverable 4 reflection (prediction vs. actual) | All, one section each |

## Attribution

Owners come from the Division of Work in our specification. Every member needs substantive work on at least one class or test file.

| File | Who | How |
|---|---|---|
| `LinkedQueue.java` | Luke | GenAI-assisted first draft from the spec; Luke reviewed/revised |
| `EmptyQueueException.java` | Luke | GenAI-assisted; Luke reviewed/revised |
| `AlreadyInLineException.java` | Vishnu | GenAI-assisted; Vishnu reviewed/revised |
| `Ticket.java` | Vishnu | GenAI-assisted; Vishnu reviewed/revised |
| `OfficeHours.java` | Vishnu | GenAI-assisted first draft from the spec; Vishnu reviewed/revised |
| `OfficeHoursView.java` | Jai | GenAI-assisted; Jai reviewed/revised |
| `ConsoleView.java` | Jai | GenAI-assisted; Jai reviewed/revised |
| `OfficeHoursController.java` | Gavin, Vishnu | GenAI-assisted; Gavin, Vishnu reviewed/revised |
| `ProjectRunner.java` | Gavin | GenAI-assisted; Gavin reviewed/revised |
| `LinkedQueueTest.java` | Luke | GenAI-assisted from the spec's test plan; Luke reviewed/revised |
| `OfficeHoursTest.java` | Vishnu | GenAI-assisted from the spec's test plan; Vishnu reviewed/revised |
| `ConsoleViewTest.java` | Jai | GenAI-assisted; added beyond the spec; Jai reviewed/revised |
| `OfficeHoursControllerTest.java` | Gavin, Vishnu | GenAI-assisted; added beyond the spec; Gavin, Vishnu reviewed/revised |
| `README.md` | All | GenAI-assisted draft; team reviewed/revised |

### GenAI note

We used Claude to turn our Deliverable 2 specification into code. It wrote the first draft of every class and test file above, compiled them, ran the 53 tests (all passed), and ran the MVP walkthrough once through `ProjectRunner` with piped input. It later added the startup welcome text and one test for it, plus two controller tests that use the exact bad inputs from our scope doc (a 10,000-character name and topic, and `  NEXT ` with caps and spaces), bringing the suite to 56. It also cross-checked the unknowns and stretch goals against the scope doc. Following the assignment's tip, we gave it our current classes and asked which ones the `leave` feature would force us to edit. Its method-level answer is the prediction in Next Steps. The list stays in the model and controller and only touches the view's help text, so it doesn't cross our model/view line, and we kept `leave` as the change feature. It also drafted this README, including the reflection comparisons. Things Claude could not do and we still need to: run on a real terminal (PIN masking), run on a lab machine, and decide the open design questions marked above.
