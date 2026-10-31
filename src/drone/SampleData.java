package drone;

/** Sample route network: 8 locations, 9 weighted routes (km). Matches Progress Report 2 results. */
public class SampleData {
    public static RouteGraph graph() {
        RouteGraph g = new RouteGraph();
        for (String n : new String[]{"Hub", "A", "B", "C", "D", "E", "F", "G"}) g.addNode(n);
        g.addEdge("Hub", "A", 4);
        g.addEdge("Hub", "B", 3);
        g.addEdge("A", "B", 1);
        g.addEdge("B", "C", 6);
        g.addEdge("B", "D", 6);
        g.addEdge("C", "D", 1);
        g.addEdge("C", "E", 3);
        g.addEdge("D", "E", 4);
        g.addEdge("F", "G", 2);
        return g;
    }
    /** Directed energy graph, negative weight = downhill / tail-wind energy gain. {u,v,w} using graph indices. */
    public static int[][] energyEdges(RouteGraph g) {
        return new int[][]{
            {g.idx("Hub"), g.idx("A"), 4}, {g.idx("Hub"), g.idx("B"), 3},
            {g.idx("B"), g.idx("C"), 6},   {g.idx("A"), g.idx("D"), 5},
            {g.idx("D"), g.idx("C"), -2},  {g.idx("C"), g.idx("E"), 3}
        };
    }
    public static final String[] ITEMS = {"Medical kit", "Camera", "Sensor pod", "Food pack", "Spare battery", "Toolkit"};
    public static final int[] ITEM_W = {20, 15, 15, 30, 25, 20};     // x0.1 kg
    public static final int[] ITEM_V = {100, 40, 25, 60, 55, 30};

    public static final String[] PLAN1 = {"Hub", "A", "B", "D", "E", "C"};
    public static final String[] PLAN2 = {"Hub", "A", "C", "D", "E", "B"};
    public static final int[] MATRIX_DIMS = {10, 30, 5, 60};
    public static final String[] MISSIONS = {"Medical", "Survey", "Parcel"};
    public static final int[][] PROFIT = {
        {0, 5, 9, 12, 14, 15, 15},
        {0, 4, 8, 13, 15, 16, 16},
        {0, 6, 9, 11, 12, 12, 12}
    };
}
