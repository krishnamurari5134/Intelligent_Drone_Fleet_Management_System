package drone;

import java.util.*;

/** Ties it together: BST of drones, priority queue of deliveries, graph routing, greedy drone assignment. */
public class FleetManager {
    public static final double BATTERY_PER_KM = 1.5;     // % battery used per km
    public static final int RESERVE = 15;                // % that must remain after a mission

    public final DroneBST drones = new DroneBST();
    public final RouteGraph graph = SampleData.graph();
    public final MaxHeap pending = new MaxHeap();
    public final List<Delivery> deliveries = new ArrayList<>();
    public final List<String> log = new ArrayList<>();
    private final int[][] dist = graph.floydWarshall();
    private int nextId = 1;

    public FleetManager() {
        drones.insert(new Drone(103, "Falcon-S", 30, 4.0, "A"));
        drones.insert(new Drone(101, "Falcon-X", 95, 5.0, "Hub"));
        drones.insert(new Drone(105, "Hauler-H", 88, 6.0, "C"));
        drones.insert(new Drone(102, "Falcon-X", 75, 3.0, "Hub"));
        drones.insert(new Drone(104, "Scout-M", 60, 2.0, "B"));
    }

    public int distance(String a, String b) { return dist[graph.idx(a)][graph.idx(b)]; }

    public Delivery createDelivery(String src, String dst, double kg, int priority) {
        if (src.equals(dst)) throw new IllegalArgumentException("Source and destination must differ.");
        if (distance(src, dst) >= RouteGraph.INF) throw new IllegalArgumentException("No route exists between " + src + " and " + dst + ".");
        Delivery d = new Delivery(nextId++, src, dst, kg, priority);
        deliveries.add(d);
        pending.insert(d);
        log.add("Delivery #" + d.id + " created (" + src + "->" + dst + ", " + d.priorityName() + ") and queued.");
        dispatch();
        return d;
    }

    /** Try to assign pending deliveries in priority order; those that cannot be served go back in the queue. */
    public int dispatch() {
        List<Delivery> order = pending.sortedView();
        while (!pending.isEmpty()) pending.extractMax();
        int assigned = 0;
        for (Delivery d : order) {
            if (tryAssign(d)) assigned++; else pending.insert(d);
        }
        return assigned;
    }

    /** Greedy: nearest eligible available drone to the pickup point (ties: higher battery). */
    private boolean tryAssign(Delivery d) {
        Drone best = null;
        int bestToPickup = Integer.MAX_VALUE;
        for (Drone dr : drones.inorder()) {
            if (dr.status != Drone.Status.AVAILABLE || dr.capacityKg < d.weightKg) continue;
            int toPickup = distance(dr.location, d.source);
            if (toPickup >= RouteGraph.INF) continue;
            int total = toPickup + distance(d.source, d.destination);
            int cost = (int) Math.ceil(total * BATTERY_PER_KM);
            if (dr.battery < cost + RESERVE) continue;
            if (best == null || toPickup < bestToPickup || (toPickup == bestToPickup && dr.battery > best.battery)) {
                best = dr; bestToPickup = toPickup;
            }
        }
        if (best == null) return false;
        int total = bestToPickup + distance(d.source, d.destination);
        d.droneId = best.id; d.status = Delivery.Status.ASSIGNED;
        d.distanceKm = total; d.batteryCost = (int) Math.ceil(total * BATTERY_PER_KM);
        RouteGraph.Paths p1 = graph.dijkstra(best.location), p2 = graph.dijkstra(d.source);
        List<String> a = graph.pathTo(p1, d.source), b = graph.pathTo(p2, d.destination);
        d.route = String.join(">", a) + (b.size() > 1 ? ">" + String.join(">", b.subList(1, b.size())) : "");
        best.status = Drone.Status.BUSY;
        log.add("Delivery #" + d.id + " assigned to " + best.label() + " (route " + d.route + ", " + total + " km, -" + d.batteryCost + "% battery).");
        return true;
    }

    public void complete(int deliveryId) {
        for (Delivery d : deliveries) {
            if (d.id != deliveryId) continue;
            if (d.status != Delivery.Status.ASSIGNED) throw new IllegalStateException("Delivery #" + d.id + " is " + d.status + "; only ASSIGNED deliveries can be completed.");
            Drone dr = drones.search(d.droneId);
            d.status = Delivery.Status.COMPLETED;
            if (dr != null) {
                dr.battery = Math.max(0, dr.battery - d.batteryCost);
                dr.location = d.destination;
                dr.status = dr.battery < 20 ? Drone.Status.CHARGING : Drone.Status.AVAILABLE;
                log.add("Delivery #" + d.id + " completed by " + dr.label() + ", battery now " + dr.battery + "%, status " + dr.status + ".");
            }
            dispatch();
            return;
        }
        throw new IllegalArgumentException("No delivery #" + deliveryId);
    }

    public void charge(int droneId) {
        Drone d = drones.search(droneId);
        if (d == null) throw new IllegalArgumentException("No drone D" + droneId);
        if (d.status == Drone.Status.BUSY) throw new IllegalStateException(d.label() + " is on a mission.");
        d.battery = 100; d.status = Drone.Status.AVAILABLE;
        log.add(d.label() + " charged to 100%.");
        dispatch();
    }

    public int count(Delivery.Status s) { int c = 0; for (Delivery d : deliveries) if (d.status == s) c++; return c; }
    public int availableDrones() { int c = 0; for (Drone d : drones.inorder()) if (d.status == Drone.Status.AVAILABLE) c++; return c; }
}
