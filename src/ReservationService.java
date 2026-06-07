import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ReservationService {

    private final List<Student> students;
    private final List<Equipment> equipmentList;
    private final List<Reservation> reservations;
    private final DiscountPolicy discountPolicy;
    private int reservationCounter = 1;

    public ReservationService(List<Student> students,
                              List<Equipment> equipmentList,
                              DiscountPolicy discountPolicy) {
        this.students = students;
        this.equipmentList = equipmentList;
        this.discountPolicy = discountPolicy;
        this.reservations = new ArrayList<>();
    }

    public Optional<Student> findStudentById(String id) {
        return students.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public Optional<Equipment> findEquipmentById(String id) {
        return equipmentList.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public Optional<Reservation> findReservationById(String id) {
        return reservations.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public List<Student> getStudents() { return students; }
    public List<Equipment> getEquipmentList() { return equipmentList; }

    public Reservation createReservation(String studentId, String equipmentId, int days) {
        Student student = findStudentById(studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student " + studentId + " not found."));

        Equipment equipment = findEquipmentById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Equipment " + equipmentId + " not found."));

        if (!equipment.isAvailable()) {
            throw new IllegalArgumentException(
                    "Equipment " + equipmentId + " is not available.");
        }

        if (days < 1 || days > 14) {
            throw new IllegalArgumentException(
                    "Number of days must be between 1 and 14.");
        }

        String reservationId = String.format("R%03d", reservationCounter++);
        Reservation reservation = new Reservation(reservationId, student, equipment, days);

        equipment.setAvailable(false);
        reservations.add(reservation);
        return reservation;
    }
    public void returnEquipment(String reservationId) {
        Reservation reservation = findReservationById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reservation " + reservationId + " not found."));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Reservation " + reservationId + " is not ACTIVE (status: "
                            + reservation.getStatus() + ").");
        }

        reservation.setStatus(ReservationStatus.RETURNED);
        reservation.getEquipment().setAvailable(true);

        double totalCost = reservation.calculateTotalCost(discountPolicy);
        int pointsEarned = (int) (totalCost / 10);
        reservation.getStudent().addLoyaltyPoints(pointsEarned);

        System.out.printf("Equipment returned. Student '%s' received %d loyalty point%s.%n",
                reservation.getStudent().getFullName(),
                pointsEarned,
                pointsEarned == 1 ? "" : "s");
    }


    public List<Reservation> getActiveReservations() {
        return reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE)
                .toList();
    }

    public List<Reservation> getCompletedReservations() {
        return reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.RETURNED)
                .toList();
    }

    public double getTotalRevenue() {
        return getCompletedReservations().stream()
                .mapToDouble(r -> r.calculateTotalCost(discountPolicy))
                .sum();
    }

    public Optional<Student> getTopLoyaltyStudent() {
        return students.stream()
                .max(Comparator.comparingInt(Student::getLoyaltyPoints));
    }

    public double getReservationCost(Reservation r) {
        return r.calculateTotalCost(discountPolicy);
    }
}
