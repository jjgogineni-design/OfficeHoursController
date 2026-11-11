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
}
