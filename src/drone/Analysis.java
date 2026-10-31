package drone;

import java.util.*;

/** Unit 1/5 helper text: heap sort, BST vs AVL, complexity tables, enterprise structure comparison. */
public class Analysis {

    public static String heapSortDemo(List<Drone> drones) {
        int n = drones.size();
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = drones.get(i).battery;
        StringBuilder sb = new StringBuilder("HEAP SORT - drones ordered by battery level\nBefore: " + Arrays.toString(a) + "\n");
        for (int i = n / 2 - 1; i >= 0; i--) sift(a, n, i);                // build max-heap O(n)
        for (int end = n - 1; end > 0; end--) { int t = a[0]; a[0] = a[end]; a[end] = t; sift(a, end, 0); }
        sb.append("After : ").append(Arrays.toString(a)).append("\nComplexity: O(n log n) time, O(1) extra space.\n");
        return sb.toString();
    }
    private static void sift(int[] a, int n, int i) {
        while (true) {
            int l = 2 * i + 1, r = l + 1, m = i;
            if (l < n && a[l] > a[m]) m = l;
            if (r < n && a[r] > a[m]) m = r;
            if (m == i) return;
            int t = a[i]; a[i] = a[m]; a[m] = t; i = m;
        }
    }

    public static String bstVsAvl(int count) {
        DroneBST bst = new DroneBST();
        AVLTree avl = new AVLTree();
        for (int i = 1; i <= count; i++) {
            bst.insert(new Drone(100 + i, "X", 50, 1, "Hub"));
            avl.insert(100 + i);
        }
        int minH = 32 - Integer.numberOfLeadingZeros(count);
        return "BST vs AVL - inserting " + count + " drone ids in sorted order (101, 102, ...)\n"
             + "  Plain BST height : " + bst.height() + "  (degenerates into a linked list, search O(n))\n"
             + "  AVL tree height  : " + avl.height() + "  (ideal minimum about " + minH + ", search O(log n)), rotations used: " + avl.rotations() + "\n";
    }

    public static String complexityTable() {
        return String.format("%-34s %-22s %-14s%n", "Algorithm / structure", "Time", "Space")
             + "-".repeat(72) + "\n"
             + String.format("%-34s %-22s %-14s%n", "BST insert/search/delete", "avg O(log n), worst O(n)", "O(n)")
             + String.format("%-34s %-22s %-14s%n", "AVL insert/search", "O(log n)", "O(n)")
             + String.format("%-34s %-22s %-14s%n", "Binary heap insert/extract", "O(log n)", "O(n)")
             + String.format("%-34s %-22s %-14s%n", "Heap sort", "O(n log n)", "O(1)")
             + String.format("%-34s %-22s %-14s%n", "BFS / DFS / components", "O(V+E)", "O(V)")
             + String.format("%-34s %-22s %-14s%n", "Prim (binary heap)", "O(E log V)", "O(V+E)")
             + String.format("%-34s %-22s %-14s%n", "Kruskal (Union-Find)", "O(E log E)", "O(V+E)")
             + String.format("%-34s %-22s %-14s%n", "Dijkstra (binary heap)", "O((V+E) log V)", "O(V)")
             + String.format("%-34s %-22s %-14s%n", "Bellman-Ford", "O(VE)", "O(V)")
             + String.format("%-34s %-22s %-14s%n", "Floyd-Warshall / closure", "O(V^3)", "O(V^2)")
             + String.format("%-34s %-22s %-14s%n", "0/1 Knapsack", "O(nW)", "O(nW)")
             + String.format("%-34s %-22s %-14s%n", "LCS", "O(mn)", "O(mn)")
             + String.format("%-34s %-22s %-14s%n", "Matrix chain", "O(n^3)", "O(n^2)")
             + String.format("%-34s %-22s %-14s%n", "Resource allocation DP", "O(n S^2)", "O(nS)")
             + String.format("%-34s %-22s %-14s%n", "N-Queens / Sum of subsets", "exponential (pruned)", "O(n)")
             + String.format("%-34s %-22s %-14s%n", "TSP branch & bound", "worst O(n!), pruned", "O(n)")
             + String.format("%-34s %-22s %-14s%n", "Graph colouring / Hamiltonian", "worst exponential", "O(V)");
    }

    /** Unit 5 is analysed (not implemented): written comparison for the report/viva. */
    public static String enterpriseComparison() {
        return "UNIT 5 - ADVANCED STRUCTURES (analysis only, not implemented in this code)\n\n"
             + "Red-Black tree : balanced BST, search/insert/delete O(log n), fewer rotations than AVL on\n"
             + "                 update-heavy loads. Fit: in-memory live drone-status index (Java TreeMap uses it).\n"
             + "B-Tree         : multi-way balanced tree, nodes sized to disk pages, O(log n) with very few disk reads.\n"
             + "                 Fit: on-disk flight-log index for millions of records.\n"
             + "B+ Tree        : B-Tree with data only in linked leaves -> fast range queries\n"
             + "                 (e.g. 'all flights between 10:00 and 12:00'). Used by most databases.\n"
             + "Binomial heap  : mergeable heap, insert/extract O(log n), merge O(log n).\n"
             + "                 Fit: merging mission queues when two depots combine.\n"
             + "Fibonacci heap : amortised O(1) insert/decrease-key, O(log n) extract-min; improves Dijkstra to\n"
             + "                 O(E + V log V) on very large route networks, but high constants.\n\n"
             + "Recommendation for this project: AVL / Red-Black for the live drone index, B+ Tree for archived\n"
             + "flight logs, binary heap for the mission queue (simple, fast enough at fleet scale).\n";
    }
}
