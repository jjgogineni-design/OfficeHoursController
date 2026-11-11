package nextup.controller;

import java.time.Clock;
import java.util.Scanner;

import nextup.model.OfficeHours;
import nextup.view.ConsoleView;

/** Builds the model, view, and controller, wires them together, and runs. No logic. */
public class ProjectRunner {

    public static void main(String[] args) {
        String pin = args.length > 0 ? args[0] : "2114";
        OfficeHours model = new OfficeHours(pin, Clock.systemDefaultZone());
        ConsoleView view = new ConsoleView(new Scanner(System.in), System.out);
        new OfficeHoursController(model, view).run();
    }
}
