package nextup.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Scanner;
import nextup.model.OfficeHours;
import nextup.view.ConsoleView;
import org.junit.jupiter.api.Test;

/** Drives the whole stack with a scripted keyboard. No model or view internals touched. */
class OfficeHoursControllerTest {

    private OfficeHours model;

    private String run(String script) {
        model = new OfficeHours("2114", Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(new Scanner(script), new PrintStream(bytes));
        new OfficeHoursController(model, view).run();
        return bytes.toString().replace("\r\n", "\n");
    }

    @Test
    void mvpWalkthrough() {
        String out = run("join\nAnn\nrecursion question\njoin\nBen\nlists\nline\n"
                + "ta\n2114\npeek\nnext\nnext\nnext\nquit\n");
        assertTrue(out.contains("You're #1 in line."));
        assertTrue(out.contains("You're #2 in line."));
        assertTrue(out.contains("1. Ann (recursion question)"));
        assertTrue(out.contains("TA mode on."));
        assertTrue(out.contains("Up next: Ann (recursion question)"));
        assertTrue(out.contains("Next up: Ann (recursion question)"));
        assertTrue(out.contains("Next up: Ben (lists)"));
        assertTrue(out.contains("No one is waiting."));
        assertTrue(out.contains("Session ended."));
        assertTrue(model.isEmpty());
    }

    @Test
    void studentCannotSkipAhead() {
        String out = run("join\nAnn\nx\nnext\npeek\nquit\n");
        assertTrue(out.contains("Only the TA can call the next student."));
        assertTrue(out.contains("Only the TA can see who's next."));
        assertEquals(1, model.lineSize());
    }

    @Test
    void duplicateJoinShowsExistingPosition() {
        String out = run("join\nVishnu\nx\njoin\nvishnu\ny\nquit\n");
        assertTrue(out.contains("You're already #1."));
        assertEquals(1, model.lineSize());
    }

    @Test
    void badNameAndTopicReprompt() {
        String out = run("join\n   \nAnn123\nAnn\n\nreal topic\nquit\n");
        assertTrue(out.contains("Name can't be blank."));
        assertTrue(out.contains("Names can only use letters, spaces, hyphens, and apostrophes."));
        assertTrue(out.contains("Topic can't be blank."));
        assertTrue(out.contains("You're #1 in line."));
    }

    @Test
    void cancelBacksOutOfJoin() {
        String out = run("join\ncancel\nquit\n");
        assertTrue(out.contains("Join cancelled."));
        assertTrue(model.isEmpty());
    }

    @Test
    void messyCommandsAndTypos() {
        String out = run("nxt\n\nHELP\n   QUIT  \n");
        assertEquals(2, out.split("Unknown command, type help.", -1).length - 1);
        assertTrue(out.contains("Commands:"));
        assertTrue(out.contains("Session ended."));
    }

    @Test
    void wrongPinStaysStudentAndPromptShowsTaTag() {
        String out = run("ta\n0000\nta\n2114\nstudent\nquit\n");
        assertTrue(out.contains("Incorrect PIN."));
        assertTrue(out.contains("[TA] > "));
        assertTrue(out.contains("Back to student mode."));
        assertFalse(model.isTaMode());
    }

    @Test
    void nextOnEmptyLineDoesNotCrash() {
        String out = run("ta\n2114\nnext\nquit\n");
        assertTrue(out.contains("No one is waiting."));
    }

    @Test
    void endOfInputIsQuit() {
        assertDoesNotThrow(() -> run("join\nAnn\n"));
        assertDoesNotThrow(() -> run(""));
    }

    @Test
    void constructorRejectsNulls() {
        assertThrows(IllegalArgumentException.class, () -> new OfficeHoursController(null, null));
    }
}
