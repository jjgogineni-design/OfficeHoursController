package nextup.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkedQueueTest {

    private LinkedQueue<String> q;

    @BeforeEach
    void setUp() {
        q = new LinkedQueue<>();
    }

    @Test
    void enqueueAddsAtTail() {
        q.enqueue("a");
        q.enqueue("b");
        assertEquals(2, q.size());
        assertEquals("a", q.peek());
    }

    @Test
    void enqueueNullThrowsAndLeavesQueueUnchanged() {
        assertThrows(IllegalArgumentException.class, () -> q.enqueue(null));
        assertEquals(0, q.size());
    }

    @Test
    void dequeueReturnsHeadInOrder() {
        q.enqueue("a");
        q.enqueue("b");
        assertEquals("a", q.dequeue());
        assertEquals("b", q.peek());
    }

    @Test
    void dequeueOnEmptyThrows() {
        assertThrows(EmptyQueueException.class, () -> q.dequeue());
    }

    @Test
    void peekDoesNotRemove() {
        q.enqueue("a");
        assertEquals("a", q.peek());
        assertEquals("a", q.peek());
        assertEquals(1, q.size());
    }

    @Test
    void peekOnEmptyThrows() {
        assertThrows(EmptyQueueException.class, () -> q.peek());
    }

    @Test
    void isEmptyTracksContents() {
        assertTrue(q.isEmpty());
        q.enqueue("a");
        assertFalse(q.isEmpty());
    }

    @Test
    void tailResetsWhenLastNodeLeaves() {
        q.enqueue("a");
        q.dequeue();
        assertTrue(q.isEmpty());
        q.enqueue("b");
        assertEquals("b", q.peek());
        assertEquals(1, q.size());
    }

    @Test
    void sizeCountsEntries() {
        q.enqueue("a");
        q.enqueue("b");
        q.enqueue("c");
        assertEquals(3, q.size());
    }

    @Test
    void failedDequeueLeavesSizeAtZero() {
        assertThrows(EmptyQueueException.class, () -> q.dequeue());
        assertEquals(0, q.size());
    }

    @Test
    void clearEmptiesAndQueueStillWorks() {
        q.enqueue("a");
        q.enqueue("b");
        q.enqueue("c");
        q.clear();
        assertTrue(q.isEmpty());
        q.enqueue("d");
        assertEquals("d", q.peek());
    }

    @Test
    void clearOnEmptyDoesNothing() {
        assertDoesNotThrow(() -> q.clear());
        assertEquals(0, q.size());
    }

    @Test
    void toArrayReturnsHeadToTailWithoutChangingQueue() {
        q.enqueue("a");
        q.enqueue("b");
        q.enqueue("c");
        assertArrayEquals(new Object[] {"a", "b", "c"}, q.toArray());
        assertEquals(3, q.size());
    }

    @Test
    void toArrayOnEmptyIsLengthZero() {
        assertEquals(0, q.toArray().length);
    }

    @Test
    void toArrayIsACopy() {
        q.enqueue("a");
        Object[] arr = q.toArray();
        arr[0] = "x";
        assertEquals("a", q.peek());
    }

    // ---- order after interleaved enqueues and dequeues ----

    @Test
    void orderHoldsAfterInterleavedEnqueuesAndDequeues() {
        q.enqueue("a");
        q.enqueue("b");
        assertEquals("a", q.dequeue());
        q.enqueue("c");
        q.enqueue("d");
        assertEquals("b", q.dequeue());
        q.enqueue("e");
        assertArrayEquals(new Object[] {"c", "d", "e"}, q.toArray());
        assertEquals("c", q.dequeue());
        assertEquals("d", q.dequeue());
        assertEquals("e", q.dequeue());
        assertTrue(q.isEmpty());
    }

    @Test
    void drainingToEmptyMidSequenceThenRefillingKeepsOrder() {
        q.enqueue("a");
        assertEquals("a", q.dequeue());
        assertThrows(EmptyQueueException.class, () -> q.dequeue());
        q.enqueue("b");
        q.enqueue("c");
        assertEquals("b", q.dequeue());
        q.enqueue("d");
        assertArrayEquals(new Object[] {"c", "d"}, q.toArray());
        assertEquals(2, q.size());
    }

    // ---- remove (added for the leave feature) ----

    private void fill(String... items) {
        for (String s : items) {
            q.enqueue(s);
        }
    }

    @Test
    void removeHeadMovesHeadForward() {
        fill("a", "b", "c");
        assertTrue(q.remove("a"));
        assertEquals("b", q.peek());
        assertEquals(2, q.size());
        assertArrayEquals(new Object[] {"b", "c"}, q.toArray());
    }

    @Test
    void removeMiddleRelinksNeighbors() {
        fill("a", "b", "c");
        assertTrue(q.remove("b"));
        assertArrayEquals(new Object[] {"a", "c"}, q.toArray());
        assertEquals(2, q.size());
    }

    @Test
    void removeTailMovesTailBackSoNextEnqueueLandsAtEnd() {
        fill("a", "b", "c");
        assertTrue(q.remove("c"));
        q.enqueue("d");
        assertArrayEquals(new Object[] {"a", "b", "d"}, q.toArray());
        assertEquals(3, q.size());
    }

    @Test
    void removeOnlyElementResetsHeadAndTail() {
        fill("a");
        assertTrue(q.remove("a"));
        assertTrue(q.isEmpty());
        assertThrows(EmptyQueueException.class, () -> q.peek());
        q.enqueue("b");
        assertEquals("b", q.peek());
        assertEquals(1, q.size());
    }

    @Test
    void removeTakesOnlyTheFirstMatch() {
        fill("a", "b", "a");
        assertTrue(q.remove("a"));
        assertArrayEquals(new Object[] {"b", "a"}, q.toArray());
    }

    @Test
    void removeNotFoundReturnsFalseAndLeavesQueueUnchanged() {
        fill("a", "b");
        assertFalse(q.remove("z"));
        assertArrayEquals(new Object[] {"a", "b"}, q.toArray());
        assertEquals(2, q.size());
    }

    @Test
    void removeOnEmptyReturnsFalse() {
        assertFalse(q.remove("a"));
        assertEquals(0, q.size());
    }

    @Test
    void removeNullThrowsAndLeavesQueueUnchanged() {
        fill("a");
        assertThrows(IllegalArgumentException.class, () -> q.remove(null));
        assertEquals(1, q.size());
        assertEquals("a", q.peek());
    }

    @Test
    void orderHoldsWhenRemoveIsInterleavedWithEnqueueAndDequeue() {
        fill("a", "b", "c");
        assertTrue(q.remove("b"));
        q.enqueue("d");
        assertEquals("a", q.dequeue());
        assertTrue(q.remove("d"));
        q.enqueue("e");
        assertArrayEquals(new Object[] {"c", "e"}, q.toArray());
        assertEquals("c", q.dequeue());
        assertEquals("e", q.dequeue());
        assertTrue(q.isEmpty());
    }
}
