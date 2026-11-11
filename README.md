# Next Up: Deliverable 3, First Working Slice

CS 2114, Fall 2026, Project 3. Team: Vishnu, Luke, Jai, Gavin.

Next Up is a console office-hours queue. Students `join`, anyone can see the `line`, and the TA enters TA mode with a PIN to `peek` and call the `next` student.

## Build and run

Needs JDK 17+ (built and tested on 21). JUnit 5 for the tests.

```
mkdir -p out
javac -d out $(find src/main -name '*.java')
java -cp out nextup.controller.ProjectRunner          # TA PIN defaults to 2114
java -cp out nextup.controller.ProjectRunner 9876     # or pass your own PIN
```

Tests (JUnit 5 console launcher): compile `src/main` and `src/test` together with the launcher jar on the classpath, then run it with `--scan-classpath out`. In an IDE, just run the `src/test` folder.

MVP walkthrough: `join`, `line`, `ta` (PIN), `peek`, `next` until the line is empty, `quit`. Last run: **53 tests, 0 failed** (LinkedQueueTest 15, OfficeHoursTest 20, ConsoleViewTest 8, OfficeHoursControllerTest 10), and the walkthrough above was also run by hand through `ProjectRunner` with piped input without crashing.

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
- **Controller wording the spec didn't pin down.** The join prompts read "Name (or cancel): " and "Topic (or cancel): ", and cancelling (or end of input mid-join) prints "Join cancelled." and returns to the main prompt.
- **Tests.** The spec's optional view/controller tests used a `FakeView`. We did not build one. Instead `OfficeHoursControllerTest` drives the real `ConsoleView` with a scripted `Scanner` and checks the printed text, which covers the full MVP path and the bad-input rows from section 6 in one place. The trade-off is that a controller bug and a view bug can show up as the same failure. We also added `ConsoleViewTest`, which wasn't in the plan as a separate file, and a few cases the spec table didn't list (exactly 30 characters accepted, `null` constructor arguments, `getPosition("  ")`).
- **Test setup consequence of the design.** Because `Ticket`'s constructor is package-private, `ConsoleViewTest` can't construct a `Ticket` directly. It builds them by joining through `OfficeHours`. So a view test depends on the model even though the view itself doesn't know `OfficeHours` exists. We kept the package-private constructor because the boundary matters more than test convenience, but it is a real cost of that decision.
- **Layout.** Standard `src/main/java` and `src/test/java` folders, one test package per MVC package.

### Did the model really stand alone?

Yes. A search of `nextup.model` for `import nextup` finds nothing, and the model tests run with no view. The view imports only `Ticket`, as the spec promised. Nothing had to move across the line to make the slice run.

One thing in the model still leans toward the view: **it carries user-facing wording.** Exceptions carry sentences like "Only the TA can call the next student." and "Line is full.", and `AlreadyInLineException`'s message ("You're already #3.") duplicates the text `ConsoleView.showAlreadyInLine` prints. The controller shows `e.getMessage()` straight to the user, so a different front end (or another language) would inherit this wording. It doesn't break the boundary, since the model never calls the view, but a stricter design would return error codes and let the view write the sentences. We left it as is for now.

### What happened to our unknowns?

*(Cross-check this list against the "What we don't know yet" list in the Deliverable 1 scope doc. These are the ones the spec named.)*

- **Showing the line without emptying it: figured out.** `LinkedQueue.toArray()` plus `OfficeHours.getLine()` returning a fresh `Ticket[]`. Tested, including that editing the returned array doesn't change the line.
- **How a TA gets into TA mode: figured out.** `ta` and `student` commands, PIN held only in the model, `[TA] >` prompt. Covered by model and controller tests.
- **Testing console code: figured out, with a caveat.** The rules are all in the model, so model tests never touch a console. The console pieces are tested through an injected `Scanner` and `PrintStream`.
- **Testing time-based code: figured out.** The injected `Clock`; a test checks `getJoinedAt()` equals a fixed instant.
- **Swapping in a GUI without touching the model: still a claim, not a result.** The structure supports it, but we haven't built a second view or controller to prove it.
- **Leave the line: still open.** `LinkedQueue` has no remove-by-name, and the tail-pointer edge case (removing the last node) is untouched.
- **Hiding the PIN: partly open.** `readSecret` uses `System.console().readPassword()` when a real console exists, but our automated tests only exercise the fallback path, where the PIN is echoed. Someone needs to check it by hand in a real terminal.
- **Identity: still open.** The console can't verify who a student is, so someone could join under a fake name or (once leave exists) remove someone else.

### What doesn't work yet?

- No known failing tests and no known crash on the MVP path.
- **Not implemented:** leave the line, wait-time estimates, any GUI.
- **Not verified:** PIN masking in a real terminal (see above), and a run on a lab machine as opposed to the machine that produced this slice.
- **Not covered by tests:** `ProjectRunner` itself (it has no logic, we ran it by hand), and `System.console()` behavior.
- Names allow only letters, spaces, hyphens, and apostrophes, so a name like "Jean-Luc 2nd" or one with a period is rejected. That is what the spec says, but students may find it strict.

## Next Steps (Deliverable 4)

*(Owners and order below are a proposal. Adjust them in the group chat before submitting.)*

### 1. Finish the MVP, in order

1. Run the full MVP walkthrough as a team in lab, on a real terminal, and record anything surprising. Check PIN masking specifically. *(Gavin runs, everyone watches.)*
2. Fix whatever step 1 turns up. *(Owner of the affected class.)*
3. Polish `ConsoleView` output from what we saw in lab (prompt spacing, help text). *(Jai)*
4. Cross-review: each person reads another person's class and tests before the merge. *(All; Gavin does the final merge, per the spec.)*

### 2. Change feature: leave the line

A student can remove their own spot with `leave`. This changes model state and logic (removing from the middle of a linked queue, including head and tail cases) and is reachable through the controller and view, so it isn't just a new label or a button wired to existing logic. It is also the stretch goal our spec said needs a remove-by-name operation `LinkedQueue` doesn't have.

**Prediction: what should change**

- `LinkedQueue`: a new `remove` operation, with the head, middle, tail, and only-element cases.
- `OfficeHours`: a new `leave(String name)` that cleans the name, finds the ticket, and removes it. It must give a clear message when the name isn't in line.
- `OfficeHoursController`: one new `leave` case and a small helper.
- `ConsoleView`: `showHelp` lists the new command. This is a text change, not new view logic.

**Prediction: what should not need to change**

- `OfficeHoursView` (the existing `showInfo` and `showError` are enough), `Ticket`, `ProjectRunner`, `EmptyQueueException`, `AlreadyInLineException`.

If `OfficeHoursView` or `Ticket` does end up changing, that tells us our boundary was wrong, and we'll say so in the Deliverable 4 reflection.

Open design question for the group: since the console can't verify identity, should `leave` be allowed for any name, or should there also be a TA-only removal? We lean toward any name for now and listing it as a known limit.

### 3. Stretch goals

- **Committing to:** leave the line (it is the change feature).
- **Only if leave is done early:** wait-time estimate per student. The `Clock` is already injected, so it should be testable without waiting.
- **Dropping:** the GUI. We keep the design that would allow it, but won't build it.

### 4. New tests needed

- `LinkedQueueTest` for `remove`: head, middle, tail (then `enqueue` to prove the tail pointer is right), only element, not found, `null`, empty queue, and `size()` after each.
- `OfficeHoursTest` for `leave`: middle leave shifts later positions up, case and spacing ignored, not in line, `null` and blank name, leave then rejoin works, last student leaves then a new student joins, and the line is unchanged after a failed leave.
- `OfficeHoursControllerTest`: `leave` reaches the model and prints the result, the not-in-line message appears, and `leave` appears in `help`.
- `ConsoleViewTest`: help text includes `leave`.

### 5. Who does what

| Item | Owner |
|---|---|
| Lab walkthrough and fixes (step 1 and 2) | Gavin runs it; owners fix their own classes |
| `LinkedQueue.remove` + its tests | Luke |
| `OfficeHours.leave` + its tests | Vishnu |
| `ConsoleView` polish, help text, view tests | Jai |
| Controller `leave` case + end-to-end tests, final merge | Gavin |
| Deliverable 4 reflection (prediction vs. actual) | All, one section each |

## Attribution

Owners come from the Division of Work in our specification. **Each "how" cell must be edited to say what that person actually did before we submit.** Every member needs substantive work on at least one class or test file.

| File | Who | How |
|---|---|---|
| `LinkedQueue.java` | Luke | GenAI-assisted first draft from the spec; [Luke reviewed / revised / ran tests — confirm] |
| `EmptyQueueException.java` | Luke | GenAI-assisted; [confirm] |
| `AlreadyInLineException.java` | Vishnu | GenAI-assisted; [confirm] |
| `Ticket.java` | Vishnu | GenAI-assisted; [confirm] |
| `OfficeHours.java` | Vishnu | GenAI-assisted first draft from the spec; [Vishnu reviewed / revised — confirm] |
| `OfficeHoursView.java` | Jai | GenAI-assisted; [Jai reviewed — confirm] |
| `ConsoleView.java` | Jai | GenAI-assisted; [Jai reviewed — confirm] |
| `OfficeHoursController.java` | Gavin | GenAI-assisted; [Gavin reviewed / revised — confirm] |
| `ProjectRunner.java` | Gavin | GenAI-assisted; [confirm] |
| `LinkedQueueTest.java` | Luke | GenAI-assisted from the spec's test plan; [Luke reviewed — confirm] |
| `OfficeHoursTest.java` | Vishnu | GenAI-assisted from the spec's test plan; [Vishnu reviewed — confirm] |
| `ConsoleViewTest.java` | Jai | GenAI-assisted; added beyond the spec; [confirm] |
| `OfficeHoursControllerTest.java` | Gavin | GenAI-assisted; added beyond the spec; [confirm] |
| `README.md` | All | GenAI-assisted draft; [team edited — confirm] |

### GenAI note

We used Claude to turn our Deliverable 2 specification into code. It wrote the first draft of every class and test file above, compiled them, ran the 53 tests (all passed), and ran the MVP walkthrough once through `ProjectRunner` with piped input. It also drafted this README, including the reflection comparisons. Claude could only compare the code against the spec, so the unknowns list needs checking against our scope doc, and the attribution table needs each member to say what they personally did. Things Claude could not do and we still need to: run on a real terminal (PIN masking), run on a lab machine, and decide the open design questions marked above.
