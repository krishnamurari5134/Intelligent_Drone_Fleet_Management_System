package drone;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Web dashboard: JDK built-in HttpServer + JSON API on top of the same DSA classes. Run: java -jar DroneFleet.jar --web */
public class WebServer {
    private FleetManager fm = new FleetManager();
    private RouteGraph g = fm.graph;

    public static void start(int port) throws IOException {
        WebServer w = new WebServer();
        HttpServer s = null;
        for (int p = port; p < port + 20 && s == null; p++) {
            try { s = HttpServer.create(new InetSocketAddress("127.0.0.1", p), 0); port = p; } catch (IOException ignored) { }
        }
        if (s == null) throw new IOException("No free port found.");
        s.createContext("/", w::handle);
        s.start();
        String url = "http://localhost:" + port + "/";
        System.out.println("Drone Fleet dashboard running at " + url + "   (press Ctrl+C to stop)");
        try { if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().browse(java.net.URI.create(url)); }
        catch (Exception e) { System.out.println("Open this link in your browser: " + url); }
    }

    // ------------------------------------------------------------------ routing
    private synchronized void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        try {
            if (path.equals("/") || path.equals("/index.html")) { serveIndex(ex); return; }
            if (!path.startsWith("/api/")) { send(ex, 404, "text/plain", "Not found"); return; }
            Map<String, String> q = query(ex.getRequestURI().getRawQuery());
            String json = api(path.substring(5), q);
            send(ex, 200, "application/json; charset=utf-8", json);
        } catch (IllegalArgumentException | IllegalStateException e) {
            send(ex, 400, "application/json; charset=utf-8", "{\"error\":" + str(e.getMessage()) + "}");
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, 500, "application/json; charset=utf-8", "{\"error\":" + str("Server error: " + e) + "}");
        }
    }

    private String api(String name, Map<String, String> q) {
        switch (name) {
            case "state": return state();
            case "reset": fm = new FleetManager(); g = fm.graph; return "{\"ok\":true}";
            case "delivery": {
                Delivery d = fm.createDelivery(req(q, "from"), req(q, "to"), num(q, "kg"), (int) num(q, "priority"));
                return "{\"ok\":true,\"status\":\"" + d.status + "\",\"id\":" + d.id + "}";
            }
            case "complete": fm.complete((int) num(q, "id")); return "{\"ok\":true}";
            case "dispatch": return "{\"ok\":true,\"assigned\":" + fm.dispatch() + "}";
            case "charge": fm.charge((int) num(q, "id")); return "{\"ok\":true}";
            case "drone/add": {
                int b = (int) num(q, "battery");
                if (b < 0 || b > 100) throw new IllegalArgumentException("Battery must be 0-100.");
                g.idx(req(q, "loc"));
                Drone d = new Drone((int) num(q, "id"), req(q, "model"), b, num(q, "cap"), req(q, "loc"));
                if (!fm.drones.insert(d)) throw new IllegalArgumentException("Drone ID already exists.");
                fm.log.add(d.label() + " added to the fleet.");
                fm.dispatch();
                return "{\"ok\":true}";
            }
            case "drone/delete": {
                int id = (int) num(q, "id");
                Drone d = fm.drones.search(id);
                if (d == null) throw new IllegalArgumentException("Drone D" + id + " not found.");
                if (d.status == Drone.Status.BUSY) throw new IllegalStateException("Drone is on a mission.");
                fm.drones.delete(id);
                fm.log.add("D" + id + " removed from the fleet.");
                return "{\"ok\":true}";
            }
            case "drone/search": {
                Drone d = fm.drones.search((int) num(q, "id"));
                return d == null ? "{\"found\":false}" : "{\"found\":true,\"text\":" + str(d.toString()) + "}";
            }
            case "drone/traverse": {
                String o = req(q, "order");
                List<Drone> l = o.equals("pre") ? fm.drones.preorder() : o.equals("post") ? fm.drones.postorder() : fm.drones.inorder();
                StringBuilder sb = new StringBuilder("{\"order\":[");
                for (int i = 0; i < l.size(); i++) sb.append(i > 0 ? "," : "").append(str(l.get(i).label()));
                return sb.append("],\"height\":").append(fm.drones.height()).append("}").toString();
            }
            case "route": return route(q);
            case "algo": return "{\"text\":" + str(algo(req(q, "name"))) + "}";
            default: throw new IllegalArgumentException("Unknown API: " + name);
        }
    }

    // ------------------------------------------------------------------ route algorithms
    private String route(Map<String, String> q) {
        String kind = req(q, "kind");
        String from = q.getOrDefault("from", "Hub"), to = q.getOrDefault("to", "E");
        g.idx(from); g.idx(to);
        switch (kind) {
            case "bfs": return order(g.bfs(from), "BFS from " + from);
            case "dfs": return order(g.dfs(from), "DFS from " + from);
            case "components": {
                StringBuilder sb = new StringBuilder("{\"title\":\"Connected components\",\"groups\":[");
                List<List<String>> cs = g.components();
                for (int i = 0; i < cs.size(); i++) sb.append(i > 0 ? "," : "").append(strList(cs.get(i)));
                return sb.append("]}").toString();
            }
            case "spanning": {
                List<RouteGraph.Edge> t = g.dfsSpanningTree(from);
                return edges("DFS spanning tree from " + from, t, -1);
            }
            case "prim": { RouteGraph.Mst m = g.prim(from); return edges("Prim MST", m.edges, m.cost); }
            case "kruskal": { RouteGraph.Mst m = g.kruskal(from); return edges("Kruskal MST (Union-Find)", m.edges, m.cost); }
            case "dijkstra": {
                RouteGraph.Paths p = g.dijkstra(from);
                List<String> path = g.pathTo(p, to);
                StringBuilder sb = new StringBuilder("{\"title\":\"Dijkstra " + from + " to " + to + "\",\"path\":");
                sb.append(path == null ? "[]" : strList(path));
                sb.append(",\"distance\":").append(path == null ? -1 : p.dist[g.idx(to)]).append(",\"dists\":{");
                for (int i = 0; i < g.size(); i++) {
                    int d = p.dist[i];
                    sb.append(i > 0 ? "," : "").append(str(g.name(i))).append(':').append(d >= RouteGraph.INF ? -1 : d);
                }
                return sb.append("}}").toString();
            }
            case "bellman": {
                RouteGraph.BF r = RouteGraph.bellmanFord(g.size(), SampleData.energyEdges(g), g.idx(from));
                StringBuilder sb = new StringBuilder("BELLMAN-FORD on directed ENERGY graph (edge D->C = -2 : downhill energy gain)\nSource " + from + "\n");
                for (int i = 0; i < g.size(); i++) sb.append(String.format("  %-4s %s%n", g.name(i), r.dist[i] >= RouteGraph.INF ? "unreachable" : String.valueOf(r.dist[i])));
                sb.append("Negative cycle detected: ").append(r.negativeCycle).append("\nDijkstra cannot be used here because of the negative edge.");
                return "{\"text\":" + str(sb.toString()) + "}";
            }
            case "floyd": return "{\"text\":" + str("FLOYD-WARSHALL all-pairs shortest distance (km)\n" + g.matrixText(g.floydWarshall())) + "}";
            case "closure": {
                boolean[][] r = g.transitiveClosure();
                int[][] m = new int[g.size()][g.size()];
                for (int i = 0; i < m.length; i++) for (int j = 0; j < m.length; j++) m[i][j] = r[i][j] ? 1 : RouteGraph.INF;
                return "{\"text\":" + str("TRANSITIVE CLOSURE (1 = reachable)\n" + g.matrixText(m)) + "}";
            }
            case "matrix": return "{\"text\":" + str("ADJACENCY MATRIX\n" + g.matrixText(g.adjacencyMatrix()) + "\nAdjacency list:\n" + g.adjacencyListText()) + "}";
            default: throw new IllegalArgumentException("Unknown route tool " + kind);
        }
    }
    private String order(List<String> l, String title) { return "{\"title\":" + str(title) + ",\"order\":" + strList(l) + "}"; }
    private String edges(String title, List<RouteGraph.Edge> es, int cost) {
        StringBuilder sb = new StringBuilder("{\"title\":" + str(title) + ",\"edges\":[");
        for (int i = 0; i < es.size(); i++) {
            RouteGraph.Edge e = es.get(i);
            sb.append(i > 0 ? "," : "").append("{\"u\":").append(str(g.name(e.u))).append(",\"v\":").append(str(g.name(e.v))).append(",\"w\":").append(e.w).append("}");
        }
        return sb.append("],\"cost\":").append(cost).append("}").toString();
    }

    private String algo(String n) {
        switch (n) {
            case "knapsack": return DPModules.knapsack(SampleData.ITEMS, SampleData.ITEM_W, SampleData.ITEM_V, 50);
            case "lcs": return DPModules.lcs(SampleData.PLAN1, SampleData.PLAN2);
            case "matrixchain": return DPModules.matrixChain(SampleData.MATRIX_DIMS);
            case "resource": return DPModules.resourceAllocation(SampleData.MISSIONS, SampleData.PROFIT, 6);
            case "tsp": return Backtracking.tsp(g, "Hub", Arrays.asList("A", "C", "D", "E"));
            case "coloring": return Backtracking.graphColoring(g);
            case "nqueens": return Backtracking.nQueens(6);
            case "hamiltonian": return Backtracking.hamiltonian(g);
            case "subsets": return Backtracking.sumOfSubsets(new int[]{2, 3, 5, 7, 8}, 10);
            case "heapsort": return Analysis.heapSortDemo(fm.drones.inorder());
            case "avl": return Analysis.bstVsAvl(15);
            case "complexity": return Analysis.complexityTable();
            case "unit5": return Analysis.enterpriseComparison();
            default: throw new IllegalArgumentException("Unknown algorithm " + n);
        }
    }

    // ------------------------------------------------------------------ state JSON
    private String state() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"stats\":{\"total\":").append(fm.drones.size())
          .append(",\"available\":").append(fm.availableDrones())
          .append(",\"active\":").append(fm.count(Delivery.Status.ASSIGNED))
          .append(",\"pending\":").append(fm.pending.size())
          .append(",\"completed\":").append(fm.count(Delivery.Status.COMPLETED)).append("},");
        sb.append("\"drones\":[");
        List<Drone> ds = fm.drones.inorder();
        for (int i = 0; i < ds.size(); i++) {
            Drone d = ds.get(i);
            sb.append(i > 0 ? "," : "").append("{\"id\":").append(d.id).append(",\"label\":").append(str(d.label()))
              .append(",\"model\":").append(str(d.model)).append(",\"battery\":").append(d.battery)
              .append(",\"cap\":").append(d.capacityKg).append(",\"loc\":").append(str(d.location))
              .append(",\"status\":").append(str(d.status.name())).append("}");
        }
        sb.append("],\"deliveries\":[");
        for (int i = 0; i < fm.deliveries.size(); i++) {
            Delivery d = fm.deliveries.get(i);
            sb.append(i > 0 ? "," : "").append("{\"id\":").append(d.id).append(",\"from\":").append(str(d.source))
              .append(",\"to\":").append(str(d.destination)).append(",\"kg\":").append(d.weightKg)
              .append(",\"priority\":").append(d.priority).append(",\"priorityName\":").append(str(d.priorityName()))
              .append(",\"status\":").append(str(d.status.name())).append(",\"drone\":").append(d.droneId)
              .append(",\"km\":").append(d.distanceKm).append(",\"battery\":").append(d.batteryCost)
              .append(",\"route\":").append(str(d.route)).append("}");
        }
        sb.append("],\"queue\":[");
        List<Delivery> pq = fm.pending.sortedView();
        for (int i = 0; i < pq.size(); i++) sb.append(i > 0 ? "," : "").append(pq.get(i).id);
        sb.append("],\"log\":[");
        int start = Math.max(0, fm.log.size() - 40);
        for (int i = fm.log.size() - 1; i >= start; i--) sb.append(i < fm.log.size() - 1 ? "," : "").append(str(fm.log.get(i)));
        sb.append("],\"graph\":{\"nodes\":").append(strList(g.names())).append(",\"edges\":[");
        for (int i = 0; i < g.edges().size(); i++) {
            RouteGraph.Edge e = g.edges().get(i);
            sb.append(i > 0 ? "," : "").append("{\"u\":").append(str(g.name(e.u))).append(",\"v\":").append(str(g.name(e.v))).append(",\"w\":").append(e.w).append("}");
        }
        return sb.append("]}}").toString();
    }

    // ------------------------------------------------------------------ helpers
    private static String str(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': break;
                case '\t': sb.append("\\t"); break;
                default: if (c < 32) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
    private static String strList(List<String> l) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < l.size(); i++) sb.append(i > 0 ? "," : "").append(str(l.get(i)));
        return sb.append("]").toString();
    }
    private static String req(Map<String, String> q, String k) {
        String v = q.get(k);
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException("Missing field: " + k);
        return v.trim();
    }
    private static double num(Map<String, String> q, String k) {
        String v = req(q, k).toUpperCase();
        if (k.equals("id") && v.startsWith("D")) v = v.substring(1);
        try { return Double.parseDouble(v); } catch (NumberFormatException e) { throw new IllegalArgumentException("'" + k + "' must be a number."); }
    }
    private static Map<String, String> query(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw == null) return m;
        for (String kv : raw.split("&")) {
            int i = kv.indexOf('=');
            if (i < 0) continue;
            m.put(URLDecoder.decode(kv.substring(0, i), StandardCharsets.UTF_8), URLDecoder.decode(kv.substring(i + 1), StandardCharsets.UTF_8));
        }
        return m;
    }
    private void serveIndex(HttpExchange ex) throws IOException {
        try (InputStream in = WebServer.class.getResourceAsStream("/web/index.html")) {
            if (in == null) { send(ex, 500, "text/plain", "web/index.html missing from jar"); return; }
            send(ex, 200, "text/html; charset=utf-8", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
    private void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}
