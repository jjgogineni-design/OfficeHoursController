package nextup.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OfficeHoursTest {

    private static final Instant T0 = Instant.parse("2026-11-09T12:00:00Z");
    private OfficeHours oh;

    @BeforeEach
    void setUp() {
        oh = new OfficeHours("2114", Clock.fixed(T0, ZoneOffset.UTC));
    }

    private void taOn() {
        assertTrue(oh.enterTaMode("2114"));
    }

    @Test
    void constructorStartsEmptyInStudentMode() {
        assertTrue(oh.isEmpty());
        assertFalse(oh.isTaMode());
    }

    @Test
    void constructorRejectsBadArguments() {
        Clock c = Clock.fixed(T0, ZoneOffset.UTC);
        assertThrows(IllegalArgumentException.class, () -> new OfficeHours("", c));
        assertThrows(IllegalArgumentException.class, () -> new OfficeHours(null, c));
        assertThrows(IllegalArgumentException.class, () -> new OfficeHours("2114", null));
    }

    @Test
    void cleanNameTrimsAndCollapses() {
        assertEquals("Ann Lee", OfficeHours.cleanName("  Ann   Lee "));
        assertEquals("Mary-Jane O'Neil", OfficeHours.cleanName("Mary-Jane O'Neil"));
    }

    @Test
    void cleanNameRejectsBlankLongAndBadCharacters() {
        assertEquals("Name can't be blank.",
                assertThrows(IllegalArgumentException.class, () -> OfficeHours.cleanName("   ")).getMessage());
        assertThrows(IllegalArgumentException.class, () -> OfficeHours.cleanName(null));
        assertEquals("Name must be 30 characters or fewer.",
                assertThrows(IllegalArgumentException.class,
                        () -> OfficeHours.cleanName("a".repeat(31))).getMessage());
        assertEquals("a".repeat(30), OfficeHours.cleanName("a".repeat(30)));
        assertThrows(IllegalArgumentException.class, () -> OfficeHours.cleanName("Ann\u001B[2J"));
        assertThrows(IllegalArgumentException.class, () -> OfficeHours.cleanName("Ann123"));
    }

    @Test
    void cleanTopicTrimsAndAllowsPunctuation() {
        assertEquals("autograder down?", OfficeHours.cleanTopic(" autograder down? "));
        assertEquals("line1 line2", OfficeHours.cleanTopic("line1\nline2"));
    }

    @Test
    void cleanTopicRejectsBlankAndLong() {
        assertEquals("Topic can't be blank.",
                assertThrows(IllegalArgumentException.class, () -> OfficeHours.cleanTopic(" ")).getMessage());
        assertThrows(IllegalArgumentException.class, () -> OfficeHours.cleanTopic(null));
        assertEquals("Topic must be 80 characters or fewer.",
                assertThrows(IllegalArgumentException.class,
                        () -> OfficeHours.cleanTopic("x".repeat(81))).getMessage());
    }

    @Test
    void joinReturnsPositionsAndStampsTime() {
        assertEquals(1, oh.join("Ann", "recursion"));
        assertEquals(2, oh.join("Ben", "linked lists"));
        assertEquals(T0, oh.getLine()[1].getJoinedAt());
    }

    @Test
    void joinRejectsDuplicateWithExistingPosition() {
        oh.join("Ann", "x");
        AlreadyInLineException e =
                assertThrows(AlreadyInLineException.class, () -> oh.join("ann ", "y"));
        assertEquals(1, e.getPosition());
        assertEquals(1, oh.lineSize());
    }

    @Test
    void joinRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> oh.join("", "x"));
        assertThrows(IllegalArgumentException.class, () -> oh.join("Ann", " "));
        assertEquals(0, oh.lineSize());
    }

    private String nameFor(int i) {
        return "S" + (char) ('a' + i / 26) + (char) ('a' + i % 26);
    }

    @Test
    void fullLineRejectsNewStudentButTellsExistingTheirSpot() {
        for (int i = 0; i < OfficeHours.MAX_LINE; i++) {
            oh.join(nameFor(i), "x");
        }
        assertEquals("Line is full.",
                assertThrows(IllegalStateException.class, () -> oh.join("Zed", "x")).getMessage());
        AlreadyInLineException e =
                assertThrows(AlreadyInLineException.class, () -> oh.join(nameFor(2), "x"));
        assertEquals(3, e.getPosition());
        assertEquals(OfficeHours.MAX_LINE, oh.lineSize());
    }

    @Test
    void peekNextReturnsHeadWithoutRemoving() {
        oh.join("Ann", "x");
        oh.join("Ben", "y");
        taOn();
        assertEquals("Ann", oh.peekNext().getName());
        assertEquals(2, oh.lineSize());
    }

    @Test
    void peekNextStudentModeAndEmptyThrow() {
        oh.join("Ann", "x");
        assertThrows(IllegalStateException.class, () -> oh.peekNext());
        assertEquals(1, oh.lineSize());
        oh.enterTaMode("2114");
        oh.callNext();
        assertEquals("No one is waiting.",
                assertThrows(EmptyQueueException.class, () -> oh.peekNext()).getMessage());
    }

    @Test
    void callNextRemovesHeadAndEveryoneMovesUp() {
        oh.join("Ann", "x");
        oh.join("Ben", "y");
        taOn();
        assertEquals("Ann", oh.callNext().getName());
        assertEquals(1, oh.getPosition("Ben"));
    }

    @Test
    void callNextEmptyAndStudentModeThrow() {
        taOn();
        assertEquals("No one is waiting.",
                assertThrows(EmptyQueueException.class, () -> oh.callNext()).getMessage());
        oh.exitTaMode();
        oh.join("Ann", "x");
        assertThrows(IllegalStateException.class, () -> oh.callNext());
        assertEquals(1, oh.lineSize());
    }

    @Test
    void getLineIsOrderedAndIsACopy() {
        oh.join("Ann", "x");
        oh.join("Ben", "y");
        oh.join("Cy", "z");
        Ticket[] arr = oh.getLine();
        assertEquals("Ann", arr[0].getName());
        assertEquals("Cy", arr[2].getName());
        arr[0] = null;
        assertEquals("Ann", oh.getLine()[0].getName());
        assertEquals(3, oh.lineSize());
    }

    @Test
    void getLineOnEmptyIsLengthZero() {
        assertEquals(0, oh.getLine().length);
    }

    @Test
    void getPositionIgnoresCaseAndSpacing() {
        oh.join("Ann", "x");
        oh.join("Ben", "y");
        assertEquals(2, oh.getPosition("BEN"));
        assertEquals(2, oh.getPosition("  ben "));
        assertEquals(-1, oh.getPosition(null));
        assertEquals(-1, oh.getPosition("  "));
        assertEquals(-1, oh.getPosition("Zed"));
    }

    @Test
    void lineSizeAndIsEmpty() {
        oh.join("Ann", "x");
        assertEquals(1, oh.lineSize());
        assertThrows(AlreadyInLineException.class, () -> oh.join("Ann", "x"));
        assertEquals(1, oh.lineSize());
        assertFalse(oh.isEmpty());
        taOn();
        oh.callNext();
        assertTrue(oh.isEmpty());
    }

    @Test
    void enterTaModeChecksPin() {
        assertFalse(oh.enterTaMode("0000"));
        assertFalse(oh.enterTaMode(null));
        assertFalse(oh.isTaMode());
        assertTrue(oh.enterTaMode("2114"));
        assertTrue(oh.isTaMode());
    }

    @Test
    void exitTaModeReturnsToStudentMode() {
        oh.join("Ann", "x");
        taOn();
        oh.exitTaMode();
        assertFalse(oh.isTaMode());
        assertThrows(IllegalStateException.class, () -> oh.callNext());
        assertDoesNotThrow(() -> oh.exitTaMode());
    }
}
