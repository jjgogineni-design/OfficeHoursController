package nextup.model;

/**
 * A generic singly linked FIFO queue with head and tail pointers.
 * Knows nothing about office hours.
 */
public class LinkedQueue<T> {

    private static class Node<T> {
        private final T data;
        private Node<T> next;

        private Node(T data) {
            this.data = data;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public LinkedQueue() {
        head = null;
        tail = null;
        size = 0;
    }

    /** Adds entry at the tail in O(1). */
    public void enqueue(T entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Entry must not be null.");
        }
        Node<T> node = new Node<>(entry);
        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    /** Removes and returns the head entry in O(1). */
    public T dequeue() {
        if (head == null) {
            throw new EmptyQueueException();
        }
        T data = head.data;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return data;
    }

    /** Returns the head entry without removing it. */
    public T peek() {
        if (head == null) {
            throw new EmptyQueueException();
        }
        return head.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Removes the first entry equal to the given one (head to tail) and
     * returns whether anything was removed. Handles the head, the tail, the
     * only node, and a middle node, and keeps the tail pointer correct.
     *
     * @param entry the entry to remove
     * @return true if an entry was removed, false if none matched
     * @throws IllegalArgumentException if entry is null
     */
    public boolean remove(T entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Entry must not be null.");
        }
        Node<T> previous = null;
        Node<T> current = head;
        while (current != null) {
            if (current.data.equals(entry)) {
                if (previous == null) {
                    head = current.next;       // removing the head
                } else {
                    previous.next = current.next;
                }
                if (current == tail) {
                    tail = previous;           // removing the tail (null if it was the only node)
                }
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    /** Returns a new array of entries from head to tail. The queue is unchanged. */
    public Object[] toArray() {
        Object[] result = new Object[size];
        Node<T> current = head;
        int i = 0;
        while (current != null) {
            result[i++] = current.data;
            current = current.next;
        }
        return result;
    }
}
