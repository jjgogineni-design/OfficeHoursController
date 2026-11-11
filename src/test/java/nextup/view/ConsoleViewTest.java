package nextup.view;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Scanner;
import nextup.model.OfficeHours;
import nextup.model.Ticket;
import org.junit.jupiter.api.Test;

class ConsoleViewTest {

    private ByteArrayOutputStream bytes;

    private ConsoleView viewWith(String input) {
        bytes = new ByteArrayOutputStream();
        return new ConsoleView(new Scanner(input), new PrintStream(bytes));
    }

    private String printed() {
        return bytes.toString().replace("\r\n", "\n");
    }

    /** Tickets can only be made by the model, so build them through OfficeHours. */
    private Ticket[] tickets() {
        OfficeHours oh = new OfficeHours("1", Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        oh.join("Ann", "recursion question");
        oh.join("Ben", "linked lists");
        return oh.getLine();
    }

    @Test
    void readLinePrintsPromptAndReturnsLine() {
        ConsoleView v = viewWith("hello\n");
        assertEquals("hello", v.readLine("> "));
        assertEquals("> ", printed());
    }

    @Test
    void readLineReturnsNullAtEndOfInput() {
        ConsoleView v = viewWith("");
        assertNull(v.readLine("> "));
    }

    @Test
    void readSecretFallsBackToReadLineWithoutConsole() {
        ConsoleView v = viewWith("2114\n");
        if (System.console() == null) {
            assertEquals("2114", v.readSecret("PIN: "));
        }
    }

    @Test
    void positionMessages() {
        ConsoleView v = viewWith("");
        v.showJoined(4);
        v.showAlreadyInLine(3);
        assertEquals("You're #4 in line.\nYou're already #3.\n", printed());
    }

    @Test
    void showLineNumbersEntriesOrSaysEmpty() {
        ConsoleView v = viewWith("");
        v.showLine(new Ticket[0]);
        v.showLine(tickets());
        assertEquals("No one is waiting.\n1. Ann (recursion question)\n2. Ben (linked lists)\n",
                printed());
    }

    @Test
    void showNextUpAndCalled() {
        ConsoleView v = viewWith("");
        Ticket t = tickets()[0];
        v.showNextUp(t);
        v.showCalled(t);
        assertEquals("Up next: Ann (recursion question)\nNext up: Ann (recursion question)\n",
                printed());
    }

    @Test
    void helpShowsTaCommandsOnlyInTaMode() {
        ConsoleView v = viewWith("");
        v.showHelp(false);
        assertFalse(printed().contains("next"));
        v.showHelp(true);
        assertTrue(printed().contains("peek"));
        assertTrue(printed().contains("student"));
    }

    @Test
    void constructorRejectsNulls() {
        assertThrows(IllegalArgumentException.class, () -> new ConsoleView(null, System.out));
        assertThrows(IllegalArgumentException.class, () -> new ConsoleView(new Scanner(""), null));
    }
}
