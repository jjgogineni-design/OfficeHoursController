package nextup.model;

import java.time.Clock;
import java.util.Locale;

/** Owns the queue and enforces every rule. Knows nothing about any view. */
public class OfficeHours {

    public static final int MAX_LINE = 50;
    public static final int MAX_NAME_LENGTH = 30;
    public static final int MAX_TOPIC_LENGTH = 80;

    private final LinkedQueue<Ticket> line;
    private final String taPin;
    private final Clock clock;
    private boolean taMode;
    /** Ticket from the most recent successful join at this keyboard, or null. */
    private Ticket lastJoined;

    public OfficeHours(String taPin, Clock clock) {
        if (taPin == null || taPin.isBlank() || clock == null) {
            throw new IllegalArgumentException("A PIN and a clock are required.");
        }
        this.line = new LinkedQueue<>();
        this.taPin = taPin;
        this.clock = clock;
        this.taMode = false;
    }

    /** Strips control characters, trims, collapses spaces, then validates. */
    public static String cleanName(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Name can't be blank.");
        }
        String cleaned = normalize(raw, "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Name can't be blank.");
        }
        if (cleaned.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Name must be 30 characters or fewer.");
        }
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (!Character.isLetter(c) && c != ' ' && c != '-' && c != '\'') {
                throw new IllegalArgumentException(
                        "Names can only use letters, spaces, hyphens, and apostrophes.");
            }
        }
        return cleaned;
    }

    /** Replaces control characters with spaces, trims, collapses spaces, then validates. */
    public static String cleanTopic(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Topic can't be blank.");
        }
        String cleaned = normalize(raw, " ");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Topic can't be blank.");
        }
        if (cleaned.length() > MAX_TOPIC_LENGTH) {
            throw new IllegalArgumentException("Topic must be 80 characters or fewer.");
        }
        return cleaned;
    }

    /** Swaps each control character for the replacement, trims, and collapses space runs. */
    private static String normalize(String raw, String controlReplacement) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            sb.append(Character.isISOControl(c) ? controlReplacement : String.valueOf(c));
        }
        return sb.toString().trim().replaceAll(" {2,}", " ");
    }

    /** Joins the line and returns the new 1-based position. */
    public int join(String name, String topic) {
        String cleanedName = cleanName(name);
        String cleanedTopic = cleanTopic(topic);
        int existing = getPosition(cleanedName);
        if (existing != -1) {
            throw new AlreadyInLineException(existing);
        }
        if (line.size() >= MAX_LINE) {
            throw new IllegalStateException("Line is full.");
        }
        Ticket ticket = new Ticket(cleanedName, cleanedTopic, clock.instant());
        line.enqueue(ticket);
        lastJoined = ticket;
        return line.size();
    }

    /** TA only. Returns the head ticket without removing it. */
    public Ticket peekNext() {
        if (!taMode) {
            throw new IllegalStateException("Only the TA can see who's next.");
        }
        if (line.isEmpty()) {
            throw new EmptyQueueException("No one is waiting.");
        }
        return line.peek();
    }

    /** TA only. Removes and returns the head ticket. */
    public Ticket callNext() {
        if (!taMode) {
            throw new IllegalStateException("Only the TA can call the next student.");
        }
        if (line.isEmpty()) {
            throw new EmptyQueueException("No one is waiting.");
        }
        return line.dequeue();
    }

    /**
     * Student self-removal. The console can't tell who is typing, so "you"
     * is the student from the most recent successful join at this keyboard.
     * Their ticket comes out wherever it is, and everyone behind them moves
     * up one spot.
     *
     * @return the ticket that was removed
     * @throws IllegalStateException "You're not in line." if nobody has
     *         joined here, or that student already left, was called, or was
     *         removed by the TA
     */
    public Ticket leave() {
        Ticket mine = lastJoined;
        // Ticket has no equals(), so remove() matches this exact object.
        if (mine == null || !line.remove(mine)) {
            throw new IllegalStateException("You're not in line.");
        }
        lastJoined = null;
        return mine;
    }

    /**
     * TA only. Removes the named student's ticket from anywhere in the line,
     * for example a no-show. Everyone behind them moves up one spot.
     *
     * @param name the student's name (case and spacing ignored)
     * @return the ticket that was removed
     * @throws IllegalStateException in student mode
     * @throws IllegalArgumentException the same messages as cleanName for a
     *         null, blank, too long, or badly formed name, or
     *         "That student isn't in line." if no ticket matches
     */
    public Ticket removeStudent(String name) {
        if (!taMode) {
            throw new IllegalStateException("Only the TA can remove a student by name.");
        }
        String cleanedName = cleanName(name);
        int position = getPosition(cleanedName);
        if (position == -1) {
            throw new IllegalArgumentException("That student isn't in line.");
        }
        // getLine() copies the array but hands back the same Ticket objects,
        // so remove() finds this exact ticket by identity.
        Ticket ticket = getLine()[position - 1];
        line.remove(ticket);
        if (ticket == lastJoined) {
            lastJoined = null;
        }
        return ticket;
    }

    /** A new array of all tickets in order. Does not change the line. */
    public Ticket[] getLine() {
        Object[] raw = line.toArray();
        Ticket[] result = new Ticket[raw.length];
        for (int i = 0; i < raw.length; i++) {
            result[i] = (Ticket) raw[i];
        }
        return result;
    }

    /** 1-based position of the matching name (case and spacing ignored), or -1. */
    public int getPosition(String name) {
        if (name == null) {
            return -1;
        }
        String key = normalize(name, "").toLowerCase(Locale.ROOT);
        if (key.isEmpty()) {
            return -1;
        }
        Ticket[] tickets = getLine();
        for (int i = 0; i < tickets.length; i++) {
            if (tickets[i].getName().toLowerCase(Locale.ROOT).equals(key)) {
                return i + 1;
            }
        }
        return -1;
    }

    public int lineSize() {
        return line.size();
    }

    public boolean isEmpty() {
        return line.isEmpty();
    }

    /** Turns TA mode on and returns true only if the PIN matches. */
    public boolean enterTaMode(String pin) {
        if (pin != null && pin.equals(taPin)) {
            taMode = true;
            return true;
        }
        return false;
    }

    public void exitTaMode() {
        taMode = false;
    }

    public boolean isTaMode() {
        return taMode;
    }
}
