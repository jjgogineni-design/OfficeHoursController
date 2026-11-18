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

    // ---- leave and removeStudent (the Deliverable 4 change feature) ----

    private void joinAll(String... names) {
        for (String n : names) {
            oh.join(n, "topic");
        }
    }

    private String[] names() {
        Ticket[] line = oh.getLine();
        String[] result = new String[line.length];
        for (int i = 0; i < line.length; i++) {
            result[i] = line[i].getName();
        }
        return result;
    }

    // leave(): a student removes themselves, no name needed

    @Test
    void leaveRemovesWhoeverJustJoined() {
        joinAll("Ann", "Ben");
        assertEquals("Ben", oh.leave().getName());
        assertArrayEquals(new String[] {"Ann"}, names());
    }

    @Test
    void leaveAfterTaCalledTheOthersRemovesTheHeadAndTailCorrectly() {
        joinAll("Ann", "Ben");
        taOn();
        oh.callNext();
        oh.exitTaMode();
        assertEquals("Ben", oh.leave().getName());
        assertTrue(oh.isEmpty());
        assertEquals(1, oh.join("Cy", "x"));
        taOn();
        assertEquals("Cy", oh.peekNext().getName());
    }

    @Test
    void leaveWithNobodyJoinedThrows() {
        assertEquals("You're not in line.",
                assertThrows(IllegalStateException.class, () -> oh.leave()).getMessage());
    }

    @Test
    void leaveTwiceThrowsTheSecondTimeAndLineIsUnchanged() {
        joinAll("Ann", "Ben");
        oh.leave();
        assertThrows(IllegalStateException.class, () -> oh.leave());
        assertArrayEquals(new String[] {"Ann"}, names());
    }

    @Test
    void leaveAfterBeingCalledThrows() {
        joinAll("Ann");
        taOn();
        oh.callNext();
        oh.exitTaMode();
        assertThrows(IllegalStateException.class, () -> oh.leave());
        assertTrue(oh.isEmpty());
    }

    @Test
    void leaveAfterTaRemovedYouThrows() {
        joinAll("Ann", "Ben");
        taOn();
        oh.removeStudent("Ben");
        oh.exitTaMode();
        assertThrows(IllegalStateException.class, () -> oh.leave());
        assertArrayEquals(new String[] {"Ann"}, names());
    }

    @Test
    void failedJoinsDoNotChangeWhoLeaves() {
        joinAll("Ann", "Ben");
        assertThrows(AlreadyInLineException.class, () -> oh.join("ann", "x"));
        assertThrows(IllegalArgumentException.class, () -> oh.join("", "x"));
        assertEquals("Ben", oh.leave().getName());
        assertArrayEquals(new String[] {"Ann"}, names());
    }

    @Test
    void eachNewJoinCanLeaveInTurn() {
        joinAll("Ann", "Ben");
        oh.leave();
        joinAll("Cy");
        assertEquals("Cy", oh.leave().getName());
        assertArrayEquals(new String[] {"Ann"}, names());
    }

    // removeStudent(name): TA only

    @Test
    void removeStudentFromMiddleMovesLaterStudentsUp() {
        joinAll("Ann", "Ben", "Cy");
        taOn();
        assertEquals("Ben", oh.removeStudent("Ben").getName());
        assertArrayEquals(new String[] {"Ann", "Cy"}, names());
        assertEquals(2, oh.getPosition("Cy"));
        assertEquals(-1, oh.getPosition("Ben"));
    }

    @Test
    void removeStudentIgnoresCaseAndSpacing() {
        joinAll("Ann Lee", "Ben");
        taOn();
        oh.removeStudent("  ann   LEE ");
        assertArrayEquals(new String[] {"Ben"}, names());
    }

    @Test
    void removeStudentHeadMeansTaCallsTheNewHead() {
        joinAll("Ann", "Ben");
        taOn();
        oh.removeStudent("Ann");
        assertEquals("Ben", oh.callNext().getName());
        assertTrue(oh.isEmpty());
    }

    @Test
    void removeStudentTailThenJoinLandsAtTheBack() {
        joinAll("Ann", "Ben", "Cy");
        taOn();
        oh.removeStudent("Cy");
        assertEquals(3, oh.join("Dee", "x"));
        assertArrayEquals(new String[] {"Ann", "Ben", "Dee"}, names());
    }

    @Test
    void removeStudentOnlyStudentThenNewJoinIsFirst() {
        joinAll("Ann");
        taOn();
        oh.removeStudent("Ann");
        assertTrue(oh.isEmpty());
        assertEquals(1, oh.join("Ben", "x"));
        assertEquals("Ben", oh.peekNext().getName());
    }

    @Test
    void removeStudentInStudentModeThrowsAndLineIsUnchanged() {
        joinAll("Ann", "Ben");
        assertEquals("Only the TA can remove a student by name.",
                assertThrows(IllegalStateException.class,
                        () -> oh.removeStudent("Ann")).getMessage());
        assertArrayEquals(new String[] {"Ann", "Ben"}, names());
    }

    @Test
    void removeStudentNotInLineThrowsAndLineIsUnchanged() {
        taOn();
        assertEquals("That student isn't in line.",
                assertThrows(IllegalArgumentException.class,
                        () -> oh.removeStudent("Ann")).getMessage());
        joinAll("Ann", "Ben");
        assertThrows(IllegalArgumentException.class, () -> oh.removeStudent("Zed"));
        assertArrayEquals(new String[] {"Ann", "Ben"}, names());
    }

    @Test
    void removeStudentRejectsNullBlankLongAndBadCharacters() {
        joinAll("Ann");
        taOn();
        assertEquals("Name can't be blank.",
                assertThrows(IllegalArgumentException.class,
                        () -> oh.removeStudent(null)).getMessage());
        assertEquals("Name can't be blank.",
                assertThrows(IllegalArgumentException.class,
                        () -> oh.removeStudent("   ")).getMessage());
        assertEquals("Name must be 30 characters or fewer.",
                assertThrows(IllegalArgumentException.class,
                        () -> oh.removeStudent("a".repeat(10_000))).getMessage());
        assertThrows(IllegalArgumentException.class, () -> oh.removeStudent("Ann\u001B[2J"));
        assertEquals(1, oh.lineSize());
    }

    @Test
    void orderHoldsAcrossInterleavedJoinsCallsAndRemovals() {
        joinAll("Ann", "Ben");
        taOn();
        assertEquals("Ann", oh.callNext().getName());
        joinAll("Cy", "Dee");
        oh.removeStudent("Cy");
        joinAll("Eve");
        oh.exitTaMode();
        assertEquals("Eve", oh.leave().getName());
        joinAll("Fay");
        assertArrayEquals(new String[] {"Ben", "Dee", "Fay"}, names());
        taOn();
        assertEquals("Ben", oh.callNext().getName());
        assertEquals("Dee", oh.callNext().getName());
        assertEquals("Fay", oh.callNext().getName());
        assertTrue(oh.isEmpty());
    }

    @Test
    void fullLineHasRoomAgainAfterARemoval() {
        for (int i = 0; i < OfficeHours.MAX_LINE; i++) {
            oh.join(nameFor(i), "x");
        }
        assertThrows(IllegalStateException.class, () -> oh.join("Zed", "x"));
        taOn();
        oh.removeStudent(nameFor(10));
        assertEquals(OfficeHours.MAX_LINE, oh.join("Zed", "x"));
    }
}
