package drone;

import java.util.*;

/** Unit 4: Backtracking and Branch & Bound. */
public class Backtracking {

    // ---------- N-Queens (e.g. non-conflicting charging pads on an n x n grid) ----------
    public static String nQueens(int n) {
        int[] col = new int[n];
        int[] count = {0};
        int[] first = null;
        List<int[]> sol = new ArrayList<>();
        place(0, n, col, count, sol);
        StringBuilder sb = new StringBuilder("N-QUEENS (" + n + " non-attacking pads on " + n + "x" + n + " landing grid)\n");
        sb.append("Total solutions: ").append(count[0]).append('\n');
        if (!sol.isEmpty()) {
            first = sol.get(0);
            sb.append("First solution (row -> column): ").append(Arrays.toString(first)).append('\n');
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) sb.append(first[r] == c ? " Q" : " .");
                sb.append('\n');
            }
        }
        return sb.toString();
    }
    private static void place(int r, int n, int[] col, int[] count, List<int[]> sol) {
        if (r == n) { count[0]++; if (sol.isEmpty()) sol.add(col.clone()); return; }
        for (int c = 0; c < n; c++) {
            boolean ok = true;
            for (int i = 0; i < r; i++) if (col[i] == c || Math.abs(col[i] - c) == r - i) { ok = false; break; }
            if (ok) { col[r] = c; place(r + 1, n, col, count, sol); }   // else: prune
        }
    }

    // ---------- Sum of subsets (choose payload packs that exactly hit a weight) ----------
    public static String sumOfSubsets(int[] w, int target) {
        int[] sorted = w.clone();
        Arrays.sort(sorted);
        List<String> sols = new ArrayList<>();
        int[] nodes = {0};
        subsets(sorted, 0, 0, target, new ArrayDeque<>(), sols, nodes);
        StringBuilder sb = new StringBuilder("SUM OF SUBSETS - packs " + Arrays.toString(sorted) + " kg, exact target " + target + " kg\n");
        if (sols.isEmpty()) sb.append("No subset gives the exact target.\n");
        for (String s : sols) sb.append("  ").append(s).append('\n');
        sb.append("State-space nodes visited: ").append(nodes[0]).append(" (full tree would have ").append((1 << (w.length + 1)) - 1).append(")\n");
        return sb.toString();
    }
    private static void subsets(int[] w, int i, int sum, int target, Deque<Integer> cur, List<String> out, int[] nodes) {
        nodes[0]++;
        if (sum == target) { out.add(cur.toString()); return; }
        if (i == w.length || sum + w[i] > target) return;       // bound: sorted, so everything later is too heavy
        cur.addLast(w[i]); subsets(w, i + 1, sum + w[i], target, cur, out, nodes); cur.removeLast();
        subsets(w, i + 1, sum, target, cur, out, nodes);
    }

    // ---------- Graph colouring (radio channels: neighbouring locations differ) ----------
    public static String graphColoring(RouteGraph g) {
        int n = g.size();
        for (int m = 1; m <= n; m++) {
            int[] color = new int[n];
            if (color(g, 0, m, color)) {
                StringBuilder sb = new StringBuilder("GRAPH COLOURING - radio channel assignment (adjacent locations get different channels)\n");
                sb.append("Minimum channels needed (chromatic number): ").append(m).append('\n');
                for (int i = 0; i < n; i++) sb.append("  ").append(g.name(i)).append(" -> channel ").append(color[i]).append('\n');
                return sb.toString();
            }
        }
        return "No colouring found.\n";
    }
    private static boolean color(RouteGraph g, int v, int m, int[] color) {
        if (v == g.size()) return true;
        for (int c = 1; c <= m; c++) {
            boolean ok = true;
            for (RouteGraph.Edge e : g.edges()) {
                int other = e.u == v ? e.v : e.v == v ? e.u : -1;
                if (other != -1 && other < v && color[other] == c) { ok = false; break; }
            }
            if (ok) { color[v] = c; if (color(g, v + 1, m, color)) return true; color[v] = 0; }
        }
        return false;
    }

    // ---------- Hamiltonian cycle ----------
    public static String hamiltonian(RouteGraph g) {
        int n = g.size();
        int[][] m = g.adjacencyMatrix();
        int[] path = new int[n];
        boolean[] used = new boolean[n];
        path[0] = 0; used[0] = true;
        StringBuilder sb = new StringBuilder("HAMILTONIAN CYCLE - visit every location once and return\n");
        if (ham(m, path, used, 1, n)) {
            sb.append("Cycle found: ");
            for (int v : path) sb.append(g.name(v)).append(" > ");
            sb.append(g.name(path[0])).append('\n');
        } else sb.append("No Hamiltonian cycle exists in this route network (network is also disconnected: {F,G} is isolated).\n");
        return sb.toString();
    }
    private static boolean ham(int[][] m, int[] path, boolean[] used, int pos, int n) {
        if (pos == n) return m[path[pos - 1]][path[0]] < RouteGraph.INF;
        for (int v = 1; v < n; v++) {
            if (!used[v] && m[path[pos - 1]][v] < RouteGraph.INF) {
                used[v] = true; path[pos] = v;
                if (ham(m, path, used, pos + 1, n)) return true;
                used[v] = false;
            }
        }
        return false;
    }

    // ---------- Travelling Salesman - Branch & Bound over shortest-path distances ----------
    public static String tsp(RouteGraph g, String start, List<String> stops) {
        List<String> nodes = new ArrayList<>();
        nodes.add(start);
        for (String s : stops) if (!nodes.contains(s)) nodes.add(s);
        int n = nodes.size();
        int[][] all = g.floydWarshall();
        int[][] d = new int[n][n];
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) d[i][j] = all[g.idx(nodes.get(i))][g.idx(nodes.get(j))];
        StringBuilder sb = new StringBuilder("TRAVELLING SALESMAN (Branch & Bound) - one drone visits all stops and returns to " + start + "\n");
        sb.append("Stops: ").append(nodes).append('\n');
        for (int i = 0; i < n; i++) for (int j = 0; j < n; j++)
            if (d[i][j] >= RouteGraph.INF) return sb.append("Some stop is unreachable from another - no tour possible.\n").toString();
        int[] minOut = new int[n];
        for (int i = 0; i < n; i++) { int mn = RouteGraph.INF; for (int j = 0; j < n; j++) if (i != j) mn = Math.min(mn, d[i][j]); minOut[i] = mn; }
        int[] best = {RouteGraph.INF};
        int[] bestPath = new int[n];
        int[] visited = {0}, pruned = {0};
        int[] cur = new int[n];
        boolean[] used = new boolean[n];
        cur[0] = 0; used[0] = true;
        tspRec(d, minOut, cur, used, 1, 0, best, bestPath, visited, pruned);
        long perms = 1; for (int i = 2; i < n; i++) perms *= i;
        long full = 1, level = 1;                       // nodes in the full permutation tree
        for (int k = 1; k < n; k++) { level *= (n - k); full += level; }
        sb.append("Best tour cost: ").append(best[0]).append(" km\nTour: ");
        for (int v : bestPath) sb.append(nodes.get(v)).append(" > ");
        sb.append(nodes.get(0)).append('\n');
        sb.append("Search nodes expanded: ").append(visited[0]).append(", branches pruned by bound: ").append(pruned[0])
          .append("\nFull search tree (no pruning) has ").append(full).append(" nodes / ").append(perms).append(" complete tours.\n");
        return sb.toString();
    }
    private static void tspRec(int[][] d, int[] minOut, int[] cur, boolean[] used, int pos, int cost,
                               int[] best, int[] bestPath, int[] visited, int[] pruned) {
        visited[0]++;
        int n = d.length;
        if (pos == n) {
            int total = cost + d[cur[n - 1]][0];
            if (total < best[0]) { best[0] = total; System.arraycopy(cur, 0, bestPath, 0, n); }
            return;
        }
        int bound = cost + minOut[cur[pos - 1]];
        for (int i = 0; i < n; i++) if (!used[i]) bound += minOut[i];
        if (bound >= best[0]) { pruned[0]++; return; }
        for (int v = 1; v < n; v++) {
            if (used[v]) continue;
            used[v] = true; cur[pos] = v;
            tspRec(d, minOut, cur, used, pos + 1, cost + d[cur[pos - 1]][v], best, bestPath, visited, pruned);
            used[v] = false;
        }
    }
}
