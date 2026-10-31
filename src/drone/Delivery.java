package drone;

public class Delivery {
    public enum Status { PENDING, ASSIGNED, COMPLETED }

    public final int id;
    public final String source, destination;
    public final double weightKg;
    public final int priority;          // 1 = low, 2 = normal, 3 = urgent
    public Status status = Status.PENDING;
    public int droneId = -1;
    public int distanceKm;
    public int batteryCost;
    public String route = "-";

    public Delivery(int id, String source, String destination, double weightKg, int priority) {
        this.id = id; this.source = source; this.destination = destination;
        this.weightKg = weightKg; this.priority = priority;
    }

    public String priorityName() { return priority == 3 ? "URGENT" : priority == 2 ? "NORMAL" : "LOW"; }

    @Override public String toString() {
        return "Delivery#" + id + " " + source + "->" + destination + " " + weightKg + "kg " + priorityName() + " " + status;
    }
}
