package nextup.model;

/** Thrown when something peeks at or dequeues from an empty queue. */
public class EmptyQueueException extends RuntimeException {

    public EmptyQueueException() {
        super("Queue is empty.");
    }

    public EmptyQueueException(String message) {
        super(message);
    }
}
