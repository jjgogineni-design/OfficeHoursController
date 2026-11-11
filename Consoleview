package nextup.view;

import java.io.PrintStream;
import java.util.Scanner;

import nextup.model.Ticket;

/**
 * Console implementation of OfficeHoursView. Reads typed lines and prints
 * text. Holds no line data and enforces no rules.
 */
public class ConsoleView implements OfficeHoursView {

    private final Scanner in;
    private final PrintStream out;

    /**
     * @param in  source of typed input
     * @param out destination for all printed text
     * @throws IllegalArgumentException if either argument is null
     */
    public ConsoleView(Scanner in, PrintStream out) {
        if (in == null || out == null) {
            throw new IllegalArgumentException("Scanner and PrintStream must not be null.");
        }
        this.in = in;
        this.out = out;
    }

    @Override
    public String readLine(String prompt) {
        out.print(prompt);
        out.flush();
        return in.hasNextLine() ? in.nextLine() : null;
    }

    @Override
    public String readSecret(String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword("%s", prompt);
            if (chars == null) {
                return null;
            }
            String result = new String(chars);
            java.util.Arrays.fill(chars, ' ');
            return result;
        }
        return readLine(prompt); // IDE fallback: input is echoed
    }

    @Override
    public void showJoined(int position) {
        out.println("You're #" + position + " in line.");
    }

    @Override
    public void showAlreadyInLine(int position) {
        out.println("You're already #" + position + ".");
    }

    @Override
    public void showLine(Ticket[] line) {
        if (line == null || line.length == 0) {
            out.println("No one is waiting.");
            return;
        }
        for (int i = 0; i < line.length; i++) {
            out.println((i + 1) + ". " + describe(line[i]));
        }
    }

    @Override
    public void showNextUp(Ticket ticket) {
        out.println("Up next: " + describe(ticket));
    }

    @Override
    public void showCalled(Ticket ticket) {
        out.println("Next up: " + describe(ticket));
    }

    @Override
    public void showError(String message) {
        out.println(message);
    }

    @Override
    public void showInfo(String message) {
        out.println(message);
    }

    @Override
    public void showHelp(boolean taMode) {
        out.println("Commands:");
        out.println("  join     Get in line");
        out.println("  line     See who is waiting");
        out.println("  ta       Enter TA mode (PIN required)");
        out.println("  help     Show this list");
        out.println("  quit     End the session");
        if (taMode) {
            out.println("TA commands:");
            out.println("  peek     See who is next without removing them");
            out.println("  next     Call the next student");
            out.println("  student  Return to student mode");
        }
    }

    private static String describe(Ticket t) {
        return t.getName() + " (" + t.getTopic() + ")";
    }
}
