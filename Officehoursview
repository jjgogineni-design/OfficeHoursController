package nextup.view;

import nextup.model.Ticket;

/**
 * Every input and display operation the controller may ask of a view.
 * Implementations hold no line data and know nothing about OfficeHours,
 * LinkedQueue, or any rule. The controller pushes everything they show.
 */
public interface OfficeHoursView {

    /** Prints the prompt and returns the typed line, or null at end of input. */
    String readLine(String prompt);

    /** Like readLine, but hides what is typed when a real console exists. */
    String readSecret(String prompt);

    /** Shows "You're #N in line." */
    void showJoined(int position);

    /** Shows "You're already #N." */
    void showAlreadyInLine(int position);

    /** Shows a numbered list, or "No one is waiting." when empty. */
    void showLine(Ticket[] line);

    /** Shows who is up next (peek). */
    void showNextUp(Ticket ticket);

    /** Shows who was just called (next). */
    void showCalled(Ticket ticket);

    /** Prints an error message. */
    void showError(String message);

    /** Prints an informational message. */
    void showInfo(String message);

    /** Lists commands; adds peek, next, and student when taMode is true. */
    void showHelp(boolean taMode);
}
