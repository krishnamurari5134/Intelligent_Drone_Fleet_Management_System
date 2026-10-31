package drone;

import java.io.PrintStream;
import java.util.*;

/** Console demonstration of every module (run: java -jar DroneFleet.jar --demo). */
public class Demo {
    public static void run(PrintStream out) {
        FleetManager fm = new FleetManager();
        RouteGraph g = fm.graph;
        line(out, "UNIT 1 - DRONE RECORDS (BST)");
        for (Drone d : fm.drones.inorder()) out.println("  " + d);
        out.println("Pre-order : " + ids(fm.drones.preorder()));
        out.println("In-order  : " + ids(fm.drones.inorder()));
        out.println("Post-order: " + ids(fm.drones.postorder()));
        out.println("Search D104: " + fm.drones.search(104));
        out.println(Analysis.heapSortDemo(fm.drones.inorder()));
        out.println(Analysis.bstVsAvl(15));

        line(out, "UNIT 2 - ROUTE NETWORK");
        out.println(g.adjacencyListText());
        out.println("BFS from Hub : " + String.join(" > ", g.bfs("Hub")));
        out.println("DFS from Hub : " + String.join(" > ", g.dfs("Hub")));
        out.println("Components   : " + g.components());
        StringBuilder st = new StringBuilder();
        for (RouteGraph.Edge e : g.dfsSpanningTree("Hub")) st.append(g.edgeStr(e)).append(' ');
        out.println("DFS spanning tree: " + st);
        RouteGraph.Mst pm = g.prim("Hub"), km = g.kruskal("Hub");
        out.println("Prim MST cost    : " + pm.cost + " km");
        out.println("Kruskal MST cost : " + km.cost + " km");
        RouteGraph.Paths p = g.dijkstra("Hub");
        for (String n : g.names()) {
            int d = p.dist[g.idx(n)];
            out.println("  Dijkstra Hub->" + n + " = " + (d >= RouteGraph.INF ? "unreachable" : d + " km  via " + g.pathTo(p, n)));
        }
        RouteGraph.BF bf = RouteGraph.bellmanFord(g.size(), SampleData.energyEdges(g), g.idx("Hub"));
        out.println("Bellman-Ford (energy graph) negative cycle: " + bf.negativeCycle + ", dist to C = " + bf.dist[g.idx("C")] + ", E = " + bf.dist[g.idx("E")]);
        out.println("Floyd-Warshall all-pairs:\n" + g.matrixText(g.floydWarshall()));

        line(out, "UNIT 3 - DYNAMIC PROGRAMMING");
        out.println(DPModules.knapsack(SampleData.ITEMS, SampleData.ITEM_W, SampleData.ITEM_V, 50));
        out.println(DPModules.lcs(SampleData.PLAN1, SampleData.PLAN2));
        out.println(DPModules.matrixChain(SampleData.MATRIX_DIMS));
        out.println(DPModules.resourceAllocation(SampleData.MISSIONS, SampleData.PROFIT, 6));

        line(out, "UNIT 4 - BACKTRACKING / BRANCH & BOUND");
        out.println(Backtracking.nQueens(6));
        out.println(Backtracking.sumOfSubsets(new int[]{2, 3, 5, 7, 8}, 10));
        out.println(Backtracking.graphColoring(g));
        out.println(Backtracking.hamiltonian(g));
        out.println(Backtracking.tsp(g, "Hub", Arrays.asList("A", "C", "D", "E")));

        line(out, "FLEET SIMULATION (priority queue + greedy assignment)");
        fm.createDelivery("Hub", "E", 2.0, 3);
        fm.createDelivery("A", "C", 1.0, 1);
        fm.createDelivery("B", "D", 5.5, 2);
        fm.createDelivery("Hub", "D", 1.0, 2);
        fm.createDelivery("B", "A", 1.0, 2);      // last free drone
        fm.createDelivery("Hub", "C", 1.0, 3);    // urgent, but all drones busy -> pending
        fm.createDelivery("Hub", "A", 1.0, 1);    // low, pending
        for (String s : fm.log) out.println("  " + s);
        out.println("Pending: " + fm.pending.size() + ", Active: " + fm.count(Delivery.Status.ASSIGNED));
        out.println("--- completing delivery #1: freed drone should go to the URGENT pending delivery first ---");
        fm.complete(1);
        for (String s : fm.log.subList(fm.log.size() - 2, fm.log.size())) out.println("  " + s);
        for (Delivery d : fm.deliveries) out.println("  " + d + " drone=" + (d.droneId < 0 ? "-" : "D" + d.droneId));
        line(out, "UNIT 5 - ANALYSIS");
        out.println(Analysis.enterpriseComparison());
        out.println(Analysis.complexityTable());
    }
    private static String ids(List<Drone> l) { StringBuilder sb = new StringBuilder(); for (Drone d : l) sb.append(d.label()).append(' '); return sb.toString(); }
    private static void line(PrintStream o, String t) { o.println("\n==================== " + t + " ===================="); }
}
