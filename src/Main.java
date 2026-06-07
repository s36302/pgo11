import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ReservationService service = buildService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================");
        System.out.println("   Welcome to MediaLab Equipment Reservation     ");
        System.out.println("=================================================");

        boolean running = true;
        while (running) {
            printMenu();
            String input = scanner.nextLine().trim();
            System.out.println();

            switch (input) {
                case "1" -> showStudents(service);
                case "2" -> showEquipment(service);
                case "3" -> createReservation(service, scanner);
                case "4" -> returnEquipment(service, scanner);
                case "5" -> showActiveReservations(service);
                case "6" -> showReport(service);
                case "7" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please enter a number 1-7.");
            }
        }

        scanner.close();
    }

    // ── Menu actions ──────────────────────────────────────────────────────────

    private static void showStudents(ReservationService service) {
        System.out.println("--- Students ---");
        System.out.printf("%-6s %-22s %-8s %s%n", "ID", "Full Name", "Group", "Loyalty Pts");
        System.out.println("-".repeat(55));
        for (Student s : service.getStudents()) {
            System.out.println(s);
        }
        System.out.println();
    }

    private static void showEquipment(ReservationService service) {
        System.out.println("--- Equipment ---");
        System.out.printf("%-6s %-10s %-24s %-16s %-12s %s%n",
                "ID", "Type", "Name", "Daily Price", "Status", "Details");
        System.out.println("-".repeat(90));
        for (Equipment e : service.getEquipmentList()) {
            // Polymorphism: getDisplayText() is called on Equipment reference;
            // the actual method executed is from LaptopSet or CameraKit.
            System.out.println(e.getDisplayText());
        }
        System.out.println();
    }

    private static void createReservation(ReservationService service, Scanner scanner) {
        System.out.print("Enter student ID: ");
        String studentId = scanner.nextLine().trim();

        System.out.print("Enter equipment ID: ");
        String equipmentId = scanner.nextLine().trim();

        System.out.print("Enter number of rental days (1-14): ");
        String daysInput = scanner.nextLine().trim();

        int days;
        try {
            days = Integer.parseInt(daysInput);
        } catch (NumberFormatException e) {
            System.out.println("Error: '" + daysInput + "' is not a valid number.\n");
            return;
        }

        try {
            Reservation r = service.createReservation(studentId, equipmentId, days);
            double cost = service.getReservationCost(r);
            System.out.printf("Reservation %s created.%n", r.getId());
            System.out.printf("Equipment : %s%n", r.getEquipment().getName());
            System.out.printf("Student   : %s%n", r.getStudent().getFullName());
            System.out.printf("Days      : %d%n", r.getDays());
            System.out.printf("Cost      : %.2f PLN%n", cost);
            System.out.printf("Status    : %s%n%n", r.getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    private static void returnEquipment(ReservationService service, Scanner scanner) {
        System.out.print("Enter reservation ID: ");
        String reservationId = scanner.nextLine().trim();

        try {
            service.returnEquipment(reservationId);
            System.out.println();
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage() + "\n");
        }
    }

    private static void showActiveReservations(ReservationService service) {
        var active = service.getActiveReservations();
        System.out.println("--- Active Reservations ---");
        if (active.isEmpty()) {
            System.out.println("No active reservations.");
        } else {
            for (Reservation r : active) {
                // Polymorphism via Displayable interface
                System.out.println(r.getDisplayText());
                System.out.printf("         Cost: %.2f PLN%n",
                        service.getReservationCost(r));
            }
        }
        System.out.println();
    }

    private static void showReport(ReservationService service) {
        System.out.println("--- Report: Completed Reservations ---");
        var completed = service.getCompletedReservations();
        if (completed.isEmpty()) {
            System.out.println("No completed reservations yet.");
        } else {
            for (Reservation r : completed) {
                System.out.println(r.getDisplayText());
                System.out.printf("         Cost: %.2f PLN%n",
                        service.getReservationCost(r));
            }
            System.out.printf("%nTotal revenue: %.2f PLN%n", service.getTotalRevenue());
        }

        System.out.println("\n--- Top Loyalty Student ---");
        service.getTopLoyaltyStudent().ifPresentOrElse(
                s -> System.out.printf("%s  (%d points)%n", s.getFullName(), s.getLoyaltyPoints()),
                () -> System.out.println("No students found.")
        );
        System.out.println();
    }

    // ── Menu display ──────────────────────────────────────────────────────────

    private static void printMenu() {
        System.out.println("─────────────────────────────────");
        System.out.println(" 1. Display students");
        System.out.println(" 2. Display equipment");
        System.out.println(" 3. Create a reservation");
        System.out.println(" 4. Return equipment");
        System.out.println(" 5. Show active reservations");
        System.out.println(" 6. Show report (completed + revenue)");
        System.out.println(" 7. Exit");
        System.out.println("─────────────────────────────────");
        System.out.print("Your choice: ");
    }

    // ── Sample data factory ───────────────────────────────────────────────────

    private static ReservationService buildService() {
        List<Student> students = new ArrayList<>();
        students.add(new Student("S001", "Anna Kowalska",   "12c", 120));
        students.add(new Student("S002", "Marek Nowak",     "12c",  40));
        students.add(new Student("S003", "Julia Zielinska", "13a",   0));

        List<Equipment> equipment = new ArrayList<>();
        // E001: LaptopSet – 80 PLN base, 32 GB RAM (+25), docking station (+15) = 120 PLN/day
        equipment.add(new LaptopSet("E001", "Lenovo ThinkPad Lab",  80, 32, true));
        // E002: LaptopSet – 100 PLN base, 16 GB RAM, no docking = 100 PLN/day
        equipment.add(new LaptopSet("E002", "Dell XPS Demo",       100, 16, false));
        // E003: CameraKit – 90 PLN base, 3 lenses (+30), tripod (+15) = 135 PLN/day
        equipment.add(new CameraKit("E003", "Sony Content Kit",     90,  3, true));
        // E004: CameraKit – 70 PLN base, 1 lens (+10), tripod (+15) = 95 PLN/day
        equipment.add(new CameraKit("E004", "Canon Interview Kit",  70,  1, true));

        DiscountPolicy discountPolicy = new LoyaltyDiscountPolicy();
        return new ReservationService(students, equipment, discountPolicy);
    }
}
