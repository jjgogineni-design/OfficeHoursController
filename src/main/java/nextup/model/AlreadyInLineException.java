package nextup.model;

/** Thrown on a duplicate join. Carries the student's existing 1-based position. */
public class AlreadyInLineException extends RuntimeException {

    private final int position;

    public AlreadyInLineException(int position) {
        super("You're already #" + position + ".");
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
