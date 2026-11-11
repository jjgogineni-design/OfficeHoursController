package nextup.controller;

import nextup.model.AlreadyInLineException;
import nextup.model.OfficeHours;
import nextup.view.OfficeHoursView;

/** Reads one command at a time, calls the model, and pushes the result to the view. */
public class OfficeHoursController {

    private final OfficeHours model;
    private final OfficeHoursView view;
    private boolean running;

    public OfficeHoursController(OfficeHours model, OfficeHoursView view) {
        if (model == null || view == null) {
            throw new IllegalArgumentException("Model and view must not be null.");
        }
        this.model = model;
        this.view = view;
        this.running = false;
    }

    /** Loops until quit or end of input. */
    public void run() {
        running = true;
        while (running) {
            String prompt = model.isTaMode() ? "[TA] > " : "> ";
            String input = view.readLine(prompt);
            if (input == null) {
                running = false; // end of input is quit
            } else {
                running = handleCommand(input);
            }
        }
    }

    /** Runs one command. Returns false only for quit. */
    public boolean handleCommand(String input) {
        String command = input == null ? "" : input.trim().toLowerCase();
        try {
            switch (command) {
                case "join":
                    doJoin();
                    break;
                case "line":
                    view.showLine(model.getLine());
                    break;
                case "peek":
                    view.showNextUp(model.peekNext());
                    break;
                case "next":
                    view.showCalled(model.callNext());
                    break;
                case "ta":
                    doTa();
                    break;
                case "student":
                    model.exitTaMode();
                    view.showInfo("Back to student mode.");
                    break;
                case "help":
                    view.showHelp(model.isTaMode());
                    break;
                case "quit":
                    view.showInfo("Session ended.");
                    return false;
                default:
                    view.showInfo("Unknown command, type help.");
                    break;
            }
        } catch (AlreadyInLineException e) {
            view.showAlreadyInLine(e.getPosition());
        } catch (RuntimeException e) {
            view.showError(e.getMessage());
        }
        return true;
    }

    private void doJoin() {
        String name = promptUntilValid("Name (or cancel): ", true);
        if (name == null) {
            return;
        }
        String topic = promptUntilValid("Topic (or cancel): ", false);
        if (topic == null) {
            return;
        }
        view.showJoined(model.join(name, topic));
    }

    /** Re-prompts until the model accepts the field. Returns null on cancel or end of input. */
    private String promptUntilValid(String prompt, boolean isName) {
        while (true) {
            String raw = view.readLine(prompt);
            if (raw == null || raw.trim().equalsIgnoreCase("cancel")) {
                view.showInfo("Join cancelled.");
                return null;
            }
            try {
                return isName ? OfficeHours.cleanName(raw) : OfficeHours.cleanTopic(raw);
            } catch (IllegalArgumentException e) {
                view.showError(e.getMessage());
            }
        }
    }

    private void doTa() {
        String pin = view.readSecret("PIN: ");
        if (model.enterTaMode(pin)) {
            view.showInfo("TA mode on.");
        } else {
            view.showInfo("Incorrect PIN.");
        }
    }
}
