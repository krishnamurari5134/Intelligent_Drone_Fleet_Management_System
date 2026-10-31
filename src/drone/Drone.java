package drone;

/** A single drone record (stored in the BST, keyed by id). */
public class Drone {
    public enum Status { AVAILABLE, BUSY, CHARGING }

    public final int id;
    public String model;
    public int battery;          // percent 0-100
    public double capacityKg;
    public String location;
    public Status status = Status.AVAILABLE;

    public Drone(int id, String model, int battery, double capacityKg, String location) {
        this.id = id;
        this.model = model;
        this.battery = battery;
        this.capacityKg = capacityKg;
        this.location = location;
    }

    public String label() { return "D" + id; }

    @Override public String toString() {
        return label() + " [" + model + ", " + battery + "%, " + capacityKg + "kg, @" + location + ", " + status + "]";
    }
}
