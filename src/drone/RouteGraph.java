package drone;

import java.util.*;

/** Unit 2: weighted undirected graph (adjacency list) + all graph algorithms. */
public class RouteGraph {
    public static final int INF = Integer.MAX_VALUE / 4;

    public static class Edge {
        public final int u, v, w;
        public Edge(int u, int v, int w) { this.u = u; this.v = v; this.w = w; }
    }
    public static class Mst { public final List<Edge> edges = new ArrayList<>(); public int cost; }
    public static class Paths {
        public int[] dist, prev;
    }
    public static class BF { public int[] dist, prev; public boolean negativeCycle; }

    private final List<String> names = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();
    private final List<List<int[]>> adj = new ArrayList<>();     // {to, weight}
    private final List<Edge> edges = new ArrayList<>();

    public int addNode(String n) {
        if (index.containsKey(n)) return index.get(n);
        index.put(n, names.size());
        names.add(n);
        adj.add(new ArrayList<>());
        return names.size() - 1;
    }
    public void addEdge(String a, String b, int w) {
        int u = addNode(a), v = addNode(b);
        adj.get(u).add(new int[]{v, w});
        adj.get(v).add(new int[]{u, w});
        edges.add(new Edge(u, v, w));
    }
    public int size() { return names.size(); }
    public String name(int i) { return names.get(i); }
    public int idx(String n) { Integer i = index.get(n); if (i == null) throw new IllegalArgumentException("Unknown location " + n); return i; }
    public List<String> names() { return Collections.unmodifiableList(names); }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public String edgeStr(Edge e) { return names.get(e.u) + "-" + names.get(e.v) + "(" + e.w + ")"; }

    // ---------- Representations ----------
    public String adjacencyListText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            sb.append(String.format("%-4s -> ", names.get(i)));
            for (int[] e : adj.get(i)) sb.append(names.get(e[0])).append('(').append(e[1]).append(") ");
            sb.append('\n');
        }
        return sb.toString();
    }
    public int[][] adjacencyMatrix() {
        int n = size();
        int[][] m = new int[n][n];
        for (int[] r : m) Arrays.fill(r, INF);
        for (int i = 0; i < n; i++) m[i][i] = 0;
        for (Edge e : edges) { m[e.u][e.v] = Math.min(m[e.u][e.v], e.w); m[e.v][e.u] = Math.min(m[e.v][e.u], e.w); }
        return m;
    }
    public String matrixText(int[][] m) {
        StringBuilder sb = new StringBuilder(String.format("%-5s", ""));
        for (String n : names) sb.append(String.format("%5s", n));
        sb.append('\n');
        for (int i = 0; i < size(); i++) {
            sb.append(String.format("%-5s", names.get(i)));
            for (int j = 0; j < size(); j++) sb.append(String.format("%5s", m[i][j] >= INF ? "INF" : String.valueOf(m[i][j])));
            sb.append('\n');
        }
        return sb.toString();
    }

    // ---------- Traversal ----------
    public List<String> bfs(String src) {
        int s = idx(src);
        boolean[] seen = new boolean[size()];
        List<String> order = new ArrayList<>();
        Deque<Integer> q = new ArrayDeque<>();
        q.add(s); seen[s] = true;
        while (!q.isEmpty()) {
            int u = q.poll();
            order.add(names.get(u));
            for (int[] e : adj.get(u)) if (!seen[e[0]]) { seen[e[0]] = true; q.add(e[0]); }
        }
        return order;
    }
    public List<String> dfs(String src) {
        List<String> order = new ArrayList<>();
        dfs(idx(src), new boolean[size()], order);
        return order;
    }
    private void dfs(int u, boolean[] seen, List<String> order) {
        seen[u] = true; order.add(names.get(u));
        for (int[] e : adj.get(u)) if (!seen[e[0]]) dfs(e[0], seen, order);
    }
    public List<List<String>> components() {
        boolean[] seen = new boolean[size()];
        List<List<String>> res = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            if (seen[i]) continue;
            List<String> comp = new ArrayList<>();
            Deque<Integer> st = new ArrayDeque<>();
            st.push(i); seen[i] = true;
            while (!st.isEmpty()) {
                int u = st.pop(); comp.add(names.get(u));
                for (int[] e : adj.get(u)) if (!seen[e[0]]) { seen[e[0]] = true; st.push(e[0]); }
            }
            comp.sort(Comparator.comparingInt(this::idx));
            res.add(comp);
        }
        return res;
    }
    /** Spanning tree produced by DFS (ignores weights). */
    public List<Edge> dfsSpanningTree(String src) {
        List<Edge> tree = new ArrayList<>();
        dfsTree(idx(src), new boolean[size()], tree);
        return tree;
    }
    private void dfsTree(int u, boolean[] seen, List<Edge> tree) {
        seen[u] = true;
        for (int[] e : adj.get(u)) if (!seen[e[0]]) { tree.add(new Edge(u, e[0], e[1])); dfsTree(e[0], seen, tree); }
    }

    // ---------- MST ----------
    public Mst prim(String start) {
        int s = idx(start);
        Mst mst = new Mst();
        boolean[] in = new boolean[size()];
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);   // {w, from, to}
        in[s] = true;
        for (int[] e : adj.get(s)) pq.add(new int[]{e[1], s, e[0]});
        while (!pq.isEmpty()) {
            int[] t = pq.poll();
            if (in[t[2]]) continue;
            in[t[2]] = true;
            mst.edges.add(new Edge(t[1], t[2], t[0])); mst.cost += t[0];
            for (int[] e : adj.get(t[2])) if (!in[e[0]]) pq.add(new int[]{e[1], t[2], e[0]});
        }
        return mst;
    }

    private int[] parent, rank;
    private int find(int x) { return parent[x] == x ? x : (parent[x] = find(parent[x])); }   // path compression
    private boolean union(int a, int b) {                                                      // union by rank
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (rank[ra] < rank[rb]) { int t = ra; ra = rb; rb = t; }
        parent[rb] = ra;
        if (rank[ra] == rank[rb]) rank[ra]++;
        return true;
    }
    /** Kruskal restricted to the connected component containing start. */
    public Mst kruskal(String start) {
        Set<String> comp = new HashSet<>(bfs(start));
        parent = new int[size()]; rank = new int[size()];
        for (int i = 0; i < size(); i++) parent[i] = i;
        List<Edge> sorted = new ArrayList<>();
        for (Edge e : edges) if (comp.contains(names.get(e.u))) sorted.add(e);
        sorted.sort(Comparator.comparingInt(e -> e.w));
        Mst mst = new Mst();
        for (Edge e : sorted) if (union(e.u, e.v)) { mst.edges.add(e); mst.cost += e.w; }
        return mst;
    }

    // ---------- Shortest paths ----------
    public Paths dijkstra(String src) {
        int n = size(), s = idx(src);
        Paths p = new Paths();
        p.dist = new int[n]; p.prev = new int[n];
        Arrays.fill(p.dist, INF); Arrays.fill(p.prev, -1);
        p.dist[s] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
        pq.add(new int[]{0, s});
        while (!pq.isEmpty()) {
            int[] t = pq.poll();
            int u = t[1];
            if (t[0] > p.dist[u]) continue;
            for (int[] e : adj.get(u)) {
                int nd = p.dist[u] + e[1];
                if (nd < p.dist[e[0]]) { p.dist[e[0]] = nd; p.prev[e[0]] = u; pq.add(new int[]{nd, e[0]}); }
            }
        }
        return p;
    }
    public List<String> pathTo(Paths p, String dst) {
        int d = idx(dst);
        if (p.dist[d] >= INF) return null;
        LinkedList<String> path = new LinkedList<>();
        for (int v = d; v != -1; v = p.prev[v]) path.addFirst(names.get(v));
        return path;
    }

    /** Bellman-Ford on a DIRECTED edge list {u, v, w}; w may be negative (energy gain). */
    public static BF bellmanFord(int n, int[][] dirEdges, int src) {
        BF r = new BF();
        r.dist = new int[n]; r.prev = new int[n];
        Arrays.fill(r.dist, INF); Arrays.fill(r.prev, -1);
        r.dist[src] = 0;
        for (int i = 0; i < n - 1; i++) {
            boolean changed = false;
            for (int[] e : dirEdges)
                if (r.dist[e[0]] < INF && r.dist[e[0]] + e[2] < r.dist[e[1]]) {
                    r.dist[e[1]] = r.dist[e[0]] + e[2]; r.prev[e[1]] = e[0]; changed = true;
                }
            if (!changed) break;
        }
        for (int[] e : dirEdges)
            if (r.dist[e[0]] < INF && r.dist[e[0]] + e[2] < r.dist[e[1]]) r.negativeCycle = true;
        return r;
    }

    public int[][] floydWarshall() {
        int n = size();
        int[][] d = adjacencyMatrix();
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (d[i][k] < INF && d[k][j] < INF && d[i][k] + d[k][j] < d[i][j]) d[i][j] = d[i][k] + d[k][j];
        return d;
    }
    public boolean[][] transitiveClosure() {
        int n = size();
        boolean[][] r = new boolean[n][n];
        for (int i = 0; i < n; i++) r[i][i] = true;
        for (Edge e : edges) { r[e.u][e.v] = true; r[e.v][e.u] = true; }
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++) r[i][j] |= r[i][k] && r[k][j];
        return r;
    }
}
