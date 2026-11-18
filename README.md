# Next Up: Deliverable 4, Second Prototype + Tests

CS 2114, Fall 2026, Project 3. Team: Vishnu, Luke, Jai, Gavin.

Next Up is a console office-hours queue. Students `join`, anyone can see the `line`, a student can `leave` the line, and the TA enters TA mode with a PIN to `peek`, call the `next` student, or remove a student by name with `leave`. The change feature for this deliverable is `leave`, the one we named in our Deliverable 3 Next Steps.

## Build and run

You need JDK 17+ and JUnit 5. The test suite has 97 tests (LinkedQueueTest 26, OfficeHoursTest 38, ConsoleViewTest 10, OfficeHoursControllerTest 23), and all of them pass.

### In Eclipse (the course setup)

Import the project with File → Import → Existing Projects into Workspace. The included `.classpath` sets `src/main/java` and `src/test/java` as source folders and adds JUnit 5.

- Run the program: right-click `src/main/java/nextup/controller/ProjectRunner.java` → Run As → Java Application, then type commands in the Console tab (start with `help`). The TA PIN is 2114. Eclipse's console has no `System.console()`, so the PIN is echoed there.
- Run the tests: right-click `src/test/java` → Run As → JUnit Test.

### From a terminal (macOS, Linux, or Git Bash)

Program:

```
mkdir -p out
javac -d out $(find src/main -name '*.java')
java -cp out nextup.controller.ProjectRunner          # TA PIN defaults to 2114
java -cp out nextup.controller.ProjectRunner 9876     # or pass your own PIN
```

Tests, using the JUnit 5 console launcher jar (`junit-platform-console-standalone-1.10.2.jar` from Maven Central, saved in the project folder):

```
mkdir -p out
javac -d out -cp junit-platform-console-standalone-1.10.2.jar $(find src -name '*.java')
java -jar junit-platform-console-standalone-1.10.2.jar --class-path out --scan-classpath
```

The model tests (`LinkedQueueTest`, `OfficeHoursTest`) build `LinkedQueue` and `OfficeHours` directly. They use no view, no `Scanner`, and no `System.in`.

## What is in this prototype

| Layer | Files |
|---|---|
| Model (`nextup.model`) | `LinkedQueue`, `Ticket`, `OfficeHours`, `EmptyQueueException`, `AlreadyInLineException` |
| View (`nextup.view`) | `OfficeHoursView` (interface), `ConsoleView` |
| Controller (`nextup.controller`) | `OfficeHoursController`, `ProjectRunner` |

Every command goes through `OfficeHoursController.handleCommand`, which calls one model method and passes the result (or the caught exception's message) to one view method. `ConsoleView` has two fields, a `Scanner` and a `PrintStream`. It keeps no copy of the line, the mode, or anything else from the model. It prints whatever `Ticket[]`, `Ticket`, `int`, or `String` the controller hands it.

### The change feature: `leave`

`leave` does different things depending on the mode.

In student mode it asks for nothing. The console can't tell who is typing, so "you" means the student from the most recent successful join at this keyboard. That ticket comes out of the line, and everyone behind it moves up one spot.

```
> join
Name (or cancel): Ben
Topic (or cancel): lists
You're #2 in line.
> leave
Ben, you left the line.
```

In TA mode it asks for a name and removes that student from anywhere in the line, which is how the TA clears a no-show. `cancel` at the name prompt backs out.

```
[TA] > leave
Name to remove (or cancel): ann
Removed Ann from the line.
```

Model changes:

- `LinkedQueue.remove(T entry)` returns `boolean`. It walks from `head` with a `previous` pointer and unlinks the first node whose data equals `entry`. It handles the head, a middle node, the tail (moving `tail` back to `previous`), and the only node (both pointers become `null`). `remove(null)` throws `IllegalArgumentException`, matching `enqueue(null)`.
- `OfficeHours` has a new field, `lastJoined`, which `join` sets after each successful join.
- `OfficeHours.leave()` removes the `lastJoined` ticket and returns it. If nobody has joined here, or that student already left, was called, or was removed by the TA, it throws `IllegalStateException("You're not in line.")`.
- `OfficeHours.removeStudent(String name)` is TA only and returns the removed ticket. It runs `cleanName`, finds the student with `getPosition`, takes that exact `Ticket` object out of `getLine()`, and passes it to `line.remove`. In student mode it throws `IllegalStateException("Only the TA can remove a student by name.")`. A name that isn't in line throws `IllegalArgumentException("That student isn't in line.")`.

## Bad input

Every bad-input case from our scope doc and every new one that `leave` introduces is handled without a crash, and each has at least one test.

| Bad input | Handled in | Test(s) |
|---|---|---|
| Blank name or topic | `OfficeHours.cleanName` / `cleanTopic` | `OfficeHoursTest.cleanNameRejectsBlankLongAndBadCharacters`, `cleanTopicRejectsBlankAndLong`; `OfficeHoursControllerTest.badNameAndTopicReprompt` |
| Typo'd command (`nxt`) | `handleCommand` | `OfficeHoursControllerTest.messyCommandsAndTypos` |
| Extra spaces or caps (`  NEXT `) | `handleCommand` | `OfficeHoursControllerTest.capsAndSpacesAroundNextStillWork`, `messyCommandsAndTypos` |
| `next` on an empty line | `OfficeHours.callNext` | `OfficeHoursTest.callNextEmptyAndStudentModeThrow`; `OfficeHoursControllerTest.nextOnEmptyLineDoesNotCrash` |
| Same student joins twice (`Vishnu` / `vishnu`) | `OfficeHours.join` | `OfficeHoursTest.joinRejectsDuplicateWithExistingPosition`; `OfficeHoursControllerTest.duplicateJoinShowsExistingPosition` |
| Student types `next` or `peek` to skip ahead | `OfficeHours.callNext` / `peekNext` | `OfficeHoursTest.callNextEmptyAndStudentModeThrow`, `peekNextStudentModeAndEmptyThrow`; `OfficeHoursControllerTest.studentCannotSkipAhead` |
| 10,000-character name or topic | `cleanName` / `cleanTopic` | `OfficeHoursControllerTest.tenThousandCharacterNameAndTopicAreRejected` |
| Control characters in a name | `cleanName` | `OfficeHoursTest.cleanNameRejectsBlankLongAndBadCharacters` |
| Control characters in a topic | `cleanTopic` | `OfficeHoursTest.cleanTopicTrimsAndAllowsPunctuation` |
| Spam joins (51st student) | `OfficeHours.join` | `OfficeHoursTest.fullLineRejectsNewStudentButTellsExistingTheirSpot` |
| Wrong TA PIN | `OfficeHours.enterTaMode` | `OfficeHoursTest.enterTaModeChecksPin`; `OfficeHoursControllerTest.wrongPinStaysStudentAndPromptShowsTaTag` |
| `null` passed to `enqueue` | `LinkedQueue.enqueue` | `LinkedQueueTest.enqueueNullThrowsAndLeavesQueueUnchanged` |
| End of input (Ctrl+D) | `OfficeHoursController.run` | `OfficeHoursControllerTest.endOfInputIsQuit` |
| New: student `leave` with nobody joined, or after being called, removed, or already leaving | `OfficeHours.leave` | `OfficeHoursTest.leaveWithNobodyJoinedThrows`, `leaveTwiceThrowsTheSecondTimeAndLineIsUnchanged`, `leaveAfterBeingCalledThrows`, `leaveAfterTaRemovedYouThrows`; `OfficeHoursControllerTest.studentLeaveWithNobodyJoinedShowsNotInLine`, `studentLeaveAfterBeingCalledShowsNotInLine` |
| New: a failed join (duplicate or blank name) right before `leave` | `OfficeHours.join` only sets `lastJoined` on success | `OfficeHoursTest.failedJoinsDoNotChangeWhoLeaves` |
| New: a student calls `removeStudent` to remove someone else | `OfficeHours.removeStudent` | `OfficeHoursTest.removeStudentInStudentModeThrowsAndLineIsUnchanged` |
| New: TA `leave` with a name that isn't in line | `OfficeHours.removeStudent` | `OfficeHoursTest.removeStudentNotInLineThrowsAndLineIsUnchanged`; `OfficeHoursControllerTest.taLeaveForANameNotInLineShowsMessageAndKeepsRunning` |
| New: TA `leave` with a null, blank, 10,000-character, or control-character name | `cleanName` (via `removeStudent`) | `OfficeHoursTest.removeStudentRejectsNullBlankLongAndBadCharacters`; `OfficeHoursControllerTest.taLeaveRepromptsOnBadNamesThenWorks` |
| New: `cancel` at the TA's name prompt | `OfficeHoursController.promptUntilValid` | `OfficeHoursControllerTest.cancelAtTaLeavePromptShowsRemoveMessageNotJoinMessage` |
| New: end of input at the TA's name prompt | `promptUntilValid` | `OfficeHoursControllerTest.endOfInputDuringTaLeaveIsHandled` |
| New: `remove(null)`, `remove` on an empty queue, `remove` of a missing entry | `LinkedQueue.remove` | `LinkedQueueTest.removeNullThrowsAndLeavesQueueUnchanged`, `removeOnEmptyReturnsFalse`, `removeNotFoundReturnsFalseAndLeavesQueueUnchanged` |

## Reflection

### How did the design absorb the change?

Classes `leave` touched, compared with what we predicted in D3:

| Class | Predicted | Actual |
|---|---|---|
| `LinkedQueue` | New `boolean remove(T entry)` with head, tail, only-node, and middle cases | Matched. One 24-line method. |
| `OfficeHours` | New `void leave(String name)` reusing `cleanName` and the `getPosition` matching | Changed more than predicted. Two methods instead of one: `Ticket leave()` for a student and `Ticket removeStudent(String name)` for the TA. One new field, `lastJoined`, and one new line in `join` to set it. |
| `OfficeHoursController` | New `leave` case and `doLeave()`, plus a change to `promptUntilValid` for the cancel message | Matched, plus one helper. The `leave` case checks `isTaMode()` and calls `doLeave()` or a new `doRemove()`. `promptUntilValid` gained a third parameter, `String cancelMessage`. The controller now imports `Ticket` so it can put the removed student's name in the message. |
| `ConsoleView` | One new line in `showHelp` | Two lines: one in the student list and one in the TA list. |
| `OfficeHoursView`, `Ticket`, `ProjectRunner`, `EmptyQueueException`, `AlreadyInLineException` | Should not change | None changed. |
| Existing methods (`enqueue`, `dequeue`, `peek`, `toArray`, `join`, `callNext`, `peekNext`, `getPosition`, `cleanName`, `getLine`) | Should not change | All unchanged except `join`, which now records the ticket it just created. |

The class-level prediction held: every class we said would stay the same did. The method-level prediction missed in one place, and the reason is the biggest surprise of this deliverable.

1. Asking for a name made `leave` unsafe. Our first build of `leave` matched the D3 prediction exactly: type `leave`, type a name, and that ticket comes out. Running it, we saw that any student at the shared keyboard could remove any other student by typing their name. So we split it. In student mode `leave` asks for nothing and removes "you", meaning the student from the most recent successful join at this keyboard. In TA mode it asks for a name, and the model refuses a by-name removal outside TA mode. That needed something the D3 design didn't have: the model has to remember who joined last. That's why `join` changed and `OfficeHours` gained a field.
2. `remove` matches by identity, and that only works because of how `getLine()` was written. `Ticket` doesn't override `equals`, so `remove(ticket)` falls back to `==`. For us that is the right rule, since two tickets with the same name and topic would still be two different spots in line. Student `leave` passes the exact object `join` created. TA `removeStudent` takes its ticket from `getLine()`, which copies the array but hands back the same `Ticket` objects that sit in the queue. If someone later "hardens" `getLine()` to copy each ticket too, `removeStudent` would find nothing, and `OfficeHoursTest.removeStudentFromMiddleMovesLaterStudentsUp` would fail.
3. Removal interacts with the 50-ticket cap. A full line has room again after someone leaves. We hadn't thought about that in D3 and added `fullLineHasRoomAgainAfterARemoval`.
4. An old test constrained the help wording. `ConsoleViewTest.helpShowsTaCommandsOnlyInTaMode` checks that student help never contains the word "next", so the student help line for `leave` couldn't mention it. That test is coupled to exact wording, which is fragile.

### Did the change cross the model/view line?

No. `OfficeHoursView` has the same ten methods it had in D3, and `ConsoleView` imports nothing new. The view learned nothing about the model: `leave` reports success with the existing `showInfo` and failure with the existing `showError`, and the line it displays afterward is the same `Ticket[]` from `getLine()`. `ConsoleViewTest.showLineAfterARemovalRenumbersFromWhatItIsGiven` checks that the view renumbers correctly because it numbers whatever array it receives.

The "who joined last" state lives in the model. We considered keeping it in the controller, since only the console has a shared keyboard. We put it in `OfficeHours` because the rule "a student may only remove their own ticket" is a rule of the system. A GUI controller would need the same rule, and keeping it in the model means it is enforced once and tested without any view, like the TA checks from D2.

Two small ripples did reach outside the model and controller:

- The help text lives in `ConsoleView`, so every new command has to edit the view, even one that is purely model logic. Here the view got two lines of text, and "add a command" still touches three layers.
- "You're not in line.", "That student isn't in line.", and "Only the TA can remove a student by name." are more user-facing sentences living in the model, the same issue we flagged in D3 for "Line is full." and the TA messages.

### What changed from the D3 plan?

- Behavior and signatures: D3 planned `void leave(String name)`, open to anyone. We shipped `Ticket leave()` for students and `Ticket removeStudent(String name)` for the TA, for the reason in surprise 1. Both return the removed `Ticket` so the controller can say whose ticket came out. `remove(T)` is exactly as written in D3. For `promptUntilValid` we picked the "cancel message as a parameter" option over writing a second variant, because one loop is easier to keep correct than two.
- Open design question: decided. D3 asked whether `leave` should accept any name or whether the TA should get a separate removal. Students can only remove themselves, and only the TA removes by name.
- Validation: as planned, the by-name path reuses `cleanName`, so bad names fail with the same messages as `join`. From the console the controller re-prompts before the model ever sees a bad name. The model check still matters for any future view, and the model tests cover it directly. Student `leave` takes no input, so its only bad cases are states: nobody joined here, or that student is already gone.
- Tests: we wrote everything on the D3 list, adjusted for the split, and added more. The additions are order checks after interleaved operations (`orderHoldsAfterInterleavedEnqueuesAndDequeues`, `drainingToEmptyMidSequenceThenRefillingKeepsOrder`, `orderHoldsWhenRemoveIsInterleavedWithEnqueueAndDequeue`, `orderHoldsAcrossInterleavedJoinsCallsAndRemovals`), the full-line case above, failed joins not changing who leaves, the TA no-show case, a 10,000-character name at the TA prompt, and first-match-only for `remove` with duplicates. The suite went from 56 tests to 97. The interleaved tests came from the assignment's GenAI tip: we asked for candidate Queue tests and kept the ones that test the FIFO contract.
- Not done from the D3 plan: the lab walkthrough on a real terminal, the PIN-masking check, and the `ConsoleView` polish that was supposed to follow from that walkthrough. The wait-time estimate was conditional on `leave` finishing early, and we haven't started it.

### What doesn't work yet?

- No failing tests and no known crash on the MVP path or either `leave` path.
- Only the most recent joiner can leave on their own. If Ann joins and then Ben joins at the same keyboard, Ann can't remove herself anymore. She has to ask the TA. This follows from the console having no logins.
- Identity is still unverified. Whoever sits down right after Ben joined can type `leave` and remove Ben. The window is much smaller than before (only the last join, and only until someone else joins), but it isn't zero.
- PIN masking in a real terminal hasn't been checked. Our tests only exercise the fallback path, where the PIN is echoed.
- We haven't run the program on a lab machine.
- `ProjectRunner` and `System.console()` behavior have no tests. `ProjectRunner` has no logic, and we ran it by hand with piped input.
- Names allow only letters, spaces, hyphens, and apostrophes, so "Jean-Luc 2nd" is rejected.
- Wait-time estimates and the GUI are not built.

### What design change would make the next feature cheaper?

Our next feature is the wait-time estimate (`wait` shows how long until you're called). As the code stands, it would touch `OfficeHours` (new state and logic), `OfficeHoursController` (new case), and `ConsoleView` (new help line). One change would make it and every later command cheaper: move the command list out of `ConsoleView` and into the controller. The controller would keep each command's name and one-line description in a single table, and `showHelp` would take that list (for example `showHelp(String[] lines)`) and print it. That costs one `OfficeHoursView` signature change, once. After it, adding `wait` would touch only the model and the controller, and the help text could never drift out of sync with the commands the controller actually accepts. `wait` would also reuse `lastJoined`, so "how long until I'm called" works without asking the student for their name.

## Next Steps

### What is left for the final build, in order

1. Run the full walkthrough, including both versions of `leave`, on a real terminal in lab. Check PIN masking. (Gavin runs it, everyone watches.)
2. Fix whatever step 1 turns up. (Owner of the affected class.)
3. Move the command list into the controller and change `showHelp` to print the list it is given, as described above. (Gavin for the controller, Jai for the view interface and `ConsoleView`, Jai updates `ConsoleViewTest`.)
4. Build the wait-time estimate: `OfficeHours` records the time of each `callNext` through the injected `Clock` and estimates the last joiner's wait from their position and the average time between calls. Tests use `Clock.fixed` and `Clock.offset`, so nothing has to sleep. (Vishnu for the model and its tests, Gavin for the `wait` command and controller test.)
5. Polish `ConsoleView` output based on step 1. (Jai)
6. Rehearse the demo below with a timer, at least twice. (All; Luke keeps time.)
7. Cross-review: each person reads another person's class and tests. Gavin does the final merge.

### Stretch goals

- Done: leave the line (the change feature), including a TA-only removal by name.
- Committing to: the wait-time estimate. The `Clock` has been injected since D2, so it is testable without waiting, and step 3 keeps it from touching the view.
- Dropping: the GUI. The design still allows it (a GUI would replace `ConsoleView` and `OfficeHoursController` and leave the model alone), but we won't build it.
- Dropping: the quick-question lane. It needs a second queue, a routing rule, and alternation logic in `callNext`, which is a second change feature's worth of work.

### The 3-minute demo

| Time | What we show | Commands |
|---|---|---|
| 0:00 to 0:15 | Start `ProjectRunner`, show the welcome and help (note `leave` in the list) | `help` |
| 0:15 to 0:50 | Three students join (Ann, Ben, Cy). Bad input on the way: a blank name, `Ann123`, and a duplicate `ann` that answers "You're already #1." | `join` ×4 |
| 0:50 to 1:00 | Show the line | `line` |
| 1:00 to 1:25 | New feature, student side: Cy types `leave` with no name and is removed. A second `leave` shows "You're not in line." | `leave`, `line`, `leave` |
| 1:25 to 1:35 | A student tries to skip ahead | `next` |
| 1:35 to 2:20 | New feature, TA side: wrong PIN, right PIN, `peek`, then `leave` asks for a name and the TA removes Ann as a no-show. `next` calls Ben. | `ta`, `ta`, `peek`, `leave`, `next` |
| 2:20 to 2:30 | `next` on the empty line, then quit | `next`, `quit` |
| 2:30 to 2:45 | Run `src/test/java` in Eclipse and show 97 green tests | |
| 2:45 to 3:00 | Buffer for questions or a slow keyboard | |

### Who does what

| Item | Owner |
|---|---|
| Lab walkthrough on a real terminal, PIN masking check | Gavin (all attend) |
| Fixes from the walkthrough | Owner of each affected class |
| Command table in the controller | Gavin |
| `showHelp` signature change, `ConsoleView` and `ConsoleViewTest` updates, output polish | Jai |
| Wait-time estimate in `OfficeHours` + `OfficeHoursTest` cases | Vishnu |
| `wait` command + controller test | Gavin |
| `LinkedQueue` review and any queue changes the estimate needs | Luke |
| Demo timing and rehearsal | Luke (all present) |
| Final merge | Gavin |
| Final README (one reflection section each) | All |

## Attribution

Owners come from the Division of Work in our specification and the D3 Next Steps. Every member has substantive work on at least one class and one test file.

| File | Who | How |
|---|---|---|
| `LinkedQueue.java` | Luke | D3: GenAI-assisted first draft, Luke reviewed/revised. D4: `remove` GenAI-assisted, Luke reviewed and tested the four unlink cases. |
| `EmptyQueueException.java` | Luke | GenAI-assisted; Luke reviewed/revised. Unchanged in D4. |
| `AlreadyInLineException.java` | Vishnu | GenAI-assisted; Vishnu reviewed/revised. Unchanged in D4. |
| `Ticket.java` | Vishnu | GenAI-assisted; Vishnu reviewed/revised. Unchanged in D4. |
| `OfficeHours.java` | Vishnu | D3: GenAI-assisted first draft, Vishnu reviewed/revised. D4: `leave`, `removeStudent`, and `lastJoined` GenAI-assisted; Vishnu chose the student/TA split, reviewed, and tested. |
| `OfficeHoursView.java` | Jai | GenAI-assisted; Jai reviewed/revised. Unchanged in D4. |
| `ConsoleView.java` | Jai | GenAI-assisted; Jai reviewed/revised. D4: two `leave` help lines, Jai reviewed. |
| `OfficeHoursController.java` | Gavin, Vishnu | GenAI-assisted; Gavin, Vishnu reviewed/revised. D4: `leave` case, `doLeave`, `doRemove`, and the `cancelMessage` parameter GenAI-assisted, Gavin reviewed/tested. |
| `ProjectRunner.java` | Gavin | GenAI-assisted; Gavin reviewed/revised. Unchanged in D4. |
| `LinkedQueueTest.java` | Luke | GenAI-assisted from the spec's test plan; Luke reviewed/revised. D4: `remove` and interleaved-order tests GenAI-assisted, Luke reviewed. |
| `OfficeHoursTest.java` | Vishnu | GenAI-assisted from the spec's test plan; Vishnu reviewed/revised. D4: 18 `leave` and `removeStudent` tests GenAI-assisted, Vishnu reviewed. |
| `ConsoleViewTest.java` | Jai | GenAI-assisted; Jai reviewed/revised. D4: two help/line tests GenAI-assisted, Jai reviewed. |
| `OfficeHoursControllerTest.java` | Gavin, Vishnu | GenAI-assisted; Gavin, Vishnu reviewed/revised. D4: ten `leave` tests GenAI-assisted, Gavin reviewed. |
| `README.md` | All | GenAI-assisted draft; team reviewed/revised. |

### GenAI note

We used Claude for this deliverable. We gave it our D2 specification, our D3 code and README, and the D4 instructions, and asked it to implement the `leave` feature as predicted in our D3 Next Steps. It wrote `LinkedQueue.remove`, the first version of `OfficeHours.leave`, the controller changes, and the help line. After seeing that a by-name `leave` let any student remove anyone, we asked it to split the feature: student `leave` with no prompt, and a name prompt only in TA mode. It then wrote `lastJoined`, the new `leave()`, `removeStudent`, and the matching controller and help changes. It wrote the 41 new tests, compiled everything, and ran the four test classes. It couldn't download JUnit in its environment, so it ran them with a small stand-in runner and checked that runner by deliberately breaking the tail-pointer line in `remove` (eight tests failed, as they should). We then ran the suite under real JUnit 5 in Eclipse. It also ran `ProjectRunner` with piped input through join, student leave, line, TA mode, TA leave, and quit. Following the assignment's tip, we asked it for candidate Queue tests (dequeue and peek on empty, order after interleaved enqueues) and kept the ones that check the FIFO contract rather than the current output text. It drafted this README, including the prediction-versus-actual comparison, which we checked against the diff. Claude couldn't run the program on a real terminal or a lab machine, and those are still on our list.
