package drone;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/** Swing dashboard for the Intelligent Drone Fleet Management System. */
public class FleetApp {
    private final FleetManager fm = new FleetManager();
    private final RouteGraph g = fm.graph;

    private JLabel lblTotal, lblAvail, lblActive, lblPending, lblDone;
    private DefaultTableModel droneModel, deliveryModel;
    private JTable droneTable, deliveryTable;
    private JTextArea droneOut, logOut, pendingOut, routeOut, dpOut, btOut, anOut;
    private GraphPanel graphPanel;
    private JComboBox<String> cbSrc, cbDst, cbFrom, cbTo, cbPriority;
    private JSpinner spWeight;

    public JComponent build() {
        JPanel root = new JPanel(new BorderLayout());
        JLabel title = new JLabel("  Intelligent Drone Fleet Management System   |   DSA-II (Trees, Graphs, DP, Backtracking)");
        title.setOpaque(true);
        title.setBackground(new Color(20, 45, 90));
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        root.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", dashboardTab());
        tabs.addTab("Drones (BST)", dronesTab());
        tabs.addTab("Deliveries", deliveriesTab());
        tabs.addTab("Route Network (Graphs)", routeTab());
        tabs.addTab("Optimization (DP)", dpTab());
        tabs.addTab("Backtracking / B&B", btTab());
        tabs.addTab("Analysis", analysisTab());
        root.add(tabs, BorderLayout.CENTER);
        refreshAll();
        return root;
    }

    // ===================== helpers =====================
    private JTextArea area(int rows) {
        JTextArea t = new JTextArea(rows, 60);
        t.setEditable(false);
        t.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        return t;
    }
    private JButton btn(String text, Runnable r) {
        JButton b = new JButton(text);
        b.addActionListener(e -> {
            try { r.run(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(null, ex.getMessage(), "Notice", JOptionPane.WARNING_MESSAGE); }
        });
        return b;
    }
    private DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    }
    private JLabel card(JPanel parent, String caption, Color c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(c);
        p.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JLabel v = new JLabel("0", SwingConstants.CENTER);
        v.setFont(new Font("SansSerif", Font.BOLD, 40));
        v.setForeground(Color.WHITE);
        JLabel t = new JLabel(caption, SwingConstants.CENTER);
        t.setForeground(Color.WHITE);
        t.setFont(new Font("SansSerif", Font.PLAIN, 14));
        p.add(v, BorderLayout.CENTER); p.add(t, BorderLayout.SOUTH);
        parent.add(p);
        return v;
    }
    private String joinIds(List<Drone> l) { StringBuilder sb = new StringBuilder(); for (Drone d : l) sb.append(d.label()).append("  "); return sb.toString(); }

    // ===================== dashboard =====================
    private JComponent dashboardTab() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel cards = new JPanel(new GridLayout(1, 5, 12, 12));
        lblTotal = card(cards, "Total Drones", new Color(35, 80, 150));
        lblAvail = card(cards, "Available Drones", new Color(40, 140, 80));
        lblActive = card(cards, "Active Deliveries", new Color(200, 120, 20));
        lblPending = card(cards, "Pending Queue", new Color(170, 60, 60));
        lblDone = card(cards, "Completed", new Color(90, 90, 120));
        cards.setPreferredSize(new Dimension(100, 130));
        p.add(cards, BorderLayout.NORTH);
        logOut = area(10);
        pendingOut = area(6);
        JPanel mid = new JPanel(new GridLayout(2, 1, 8, 8));
        JScrollPane s1 = new JScrollPane(logOut); s1.setBorder(BorderFactory.createTitledBorder("Activity log"));
        JScrollPane s2 = new JScrollPane(pendingOut); s2.setBorder(BorderFactory.createTitledBorder("Pending deliveries - priority queue (binary max-heap) order"));
        mid.add(s1); mid.add(s2);
        p.add(mid, BorderLayout.CENTER);
        return p;
    }

    // ===================== drones =====================
    private JComponent dronesTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        droneModel = model("ID", "Model", "Battery %", "Capacity kg", "Location", "Status");
        droneTable = new JTable(droneModel);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(btn("Add Drone", this::addDrone));
        bar.add(btn("Search by ID", () -> {
            Drone d = fm.drones.search(askInt("Drone ID to search (e.g. 104):"));
            droneOut.setText(d == null ? "Not found." : "Found: " + d);
        }));
        bar.add(btn("Update Battery/Location", this::updateDrone));
        bar.add(btn("Delete", () -> {
            int id = askInt("Drone ID to delete:");
            Drone d = fm.drones.search(id);
            if (d != null && d.status == Drone.Status.BUSY) throw new IllegalStateException("Drone is on a mission.");
            droneOut.setText(fm.drones.delete(id) ? "Deleted D" + id : "Not found.");
            refreshAll();
        }));
        bar.add(btn("Charge Selected", () -> {
            int r = droneTable.getSelectedRow();
            if (r < 0) throw new IllegalStateException("Select a drone row first.");
            fm.charge(Integer.parseInt(droneModel.getValueAt(r, 0).toString().substring(1)));
            refreshAll();
        }));
        bar.add(btn("In-order", () -> droneOut.setText("In-order (sorted by ID): " + joinIds(fm.drones.inorder()))));
        bar.add(btn("Pre-order", () -> droneOut.setText("Pre-order: " + joinIds(fm.drones.preorder()))));
        bar.add(btn("Post-order", () -> droneOut.setText("Post-order: " + joinIds(fm.drones.postorder()))));
        droneOut = area(5);
        p.add(bar, BorderLayout.NORTH);
        p.add(new JScrollPane(droneTable), BorderLayout.CENTER);
        JScrollPane so = new JScrollPane(droneOut); so.setBorder(BorderFactory.createTitledBorder("Result  (BST height = see table size; operations avg O(log n))"));
        p.add(so, BorderLayout.SOUTH);
        return p;
    }
    private int askInt(String msg) {
        String s = JOptionPane.showInputDialog(msg);
        if (s == null) throw new IllegalStateException("Cancelled.");
        s = s.trim().toUpperCase();
        if (s.startsWith("D")) s = s.substring(1);
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { throw new IllegalStateException("Enter a number."); }
    }
    private void addDrone() {
        JTextField id = new JTextField(), model = new JTextField("Falcon-X"), bat = new JTextField("100"), cap = new JTextField("3.0");
        JComboBox<String> loc = new JComboBox<>(g.names().toArray(new String[0]));
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Drone ID (number):")); form.add(id);
        form.add(new JLabel("Model:")); form.add(model);
        form.add(new JLabel("Battery %:")); form.add(bat);
        form.add(new JLabel("Capacity kg:")); form.add(cap);
        form.add(new JLabel("Base location:")); form.add(loc);
        if (JOptionPane.showConfirmDialog(null, form, "Add Drone", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            int b = Integer.parseInt(bat.getText().trim());
            if (b < 0 || b > 100) throw new IllegalStateException("Battery must be 0-100.");
            Drone d = new Drone(Integer.parseInt(id.getText().trim()), model.getText().trim(), b, Double.parseDouble(cap.getText().trim()), (String) loc.getSelectedItem());
            if (!fm.drones.insert(d)) throw new IllegalStateException("Drone ID already exists.");
            fm.log.add(d.label() + " added to the fleet.");
            fm.dispatch();
            refreshAll();
        } catch (NumberFormatException e) { throw new IllegalStateException("Please enter valid numbers."); }
    }
    private void updateDrone() {
        int id = askInt("Drone ID to modify:");
        if (fm.drones.search(id) == null) throw new IllegalStateException("Not found.");
        String b = JOptionPane.showInputDialog("New battery % (blank = keep):");
        String l = (String) JOptionPane.showInputDialog(null, "New location:", "Modify", JOptionPane.QUESTION_MESSAGE, null, g.names().toArray(), fm.drones.search(id).location);
        try {
            Integer bat = (b == null || b.trim().isEmpty()) ? null : Integer.valueOf(b.trim());
            if (bat != null && (bat < 0 || bat > 100)) throw new IllegalStateException("Battery must be 0-100.");
            fm.drones.modify(id, bat, l);
        } catch (NumberFormatException e) { throw new IllegalStateException("Battery must be a number."); }
        fm.dispatch();
        refreshAll();
    }

    // ===================== deliveries =====================
    private JComponent deliveriesTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String[] names = g.names().toArray(new String[0]);
        cbFrom = new JComboBox<>(names); cbTo = new JComboBox<>(names); cbTo.setSelectedItem("E");
        spWeight = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.5));
        cbPriority = new JComboBox<>(new String[]{"1 - Low", "2 - Normal", "3 - Urgent"});
        cbPriority.setSelectedIndex(1);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(new JLabel("From:")); bar.add(cbFrom);
        bar.add(new JLabel("To:")); bar.add(cbTo);
        bar.add(new JLabel("Weight kg:")); bar.add(spWeight);
        bar.add(new JLabel("Priority:")); bar.add(cbPriority);
        bar.add(btn("Create Delivery", () -> {
            Delivery d = fm.createDelivery((String) cbFrom.getSelectedItem(), (String) cbTo.getSelectedItem(),
                    ((Number) spWeight.getValue()).doubleValue(), cbPriority.getSelectedIndex() + 1);
            refreshAll();
            if (d.status == Delivery.Status.PENDING)
                JOptionPane.showMessageDialog(null, "No drone can serve this right now (availability, battery or capacity).\nIt stays in the priority queue.", "Queued", JOptionPane.INFORMATION_MESSAGE);
        }));
        bar.add(btn("Complete Selected", () -> {
            int r = deliveryTable.getSelectedRow();
            if (r < 0) throw new IllegalStateException("Select a delivery row first.");
            fm.complete((Integer) deliveryModel.getValueAt(r, 0));
            refreshAll();
        }));
        bar.add(btn("Re-dispatch Pending", () -> {
            int n = fm.dispatch();
            refreshAll();
            JOptionPane.showMessageDialog(null, n + " pending delivery(ies) assigned.");
        }));
        deliveryModel = model("ID", "From", "To", "Kg", "Priority", "Status", "Drone", "Km", "Battery cost %", "Route");
        deliveryTable = new JTable(deliveryModel);
        p.add(bar, BorderLayout.NORTH);
        p.add(new JScrollPane(deliveryTable), BorderLayout.CENTER);
        JTextArea note = area(3);
        note.setText("Assignment = greedy: among AVAILABLE drones with enough capacity and battery (cost = 1.5% per km + 15% reserve),\n"
                   + "pick the one nearest to the pickup point (graph distance via Floyd-Warshall). Pending requests wait in a max-heap by priority.");
        p.add(note, BorderLayout.SOUTH);
        return p;
    }

    // ===================== routes =====================
    private JComponent routeTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        String[] names = g.names().toArray(new String[0]);
        cbSrc = new JComboBox<>(names); cbDst = new JComboBox<>(names); cbDst.setSelectedItem("E");
        graphPanel = new GraphPanel(g);
        routeOut = area(14);
        routeOut.setText("Adjacency list:\n" + g.adjacencyListText());

        JPanel bar = new JPanel(new WrapLayout());
        bar.add(new JLabel("Source:")); bar.add(cbSrc);
        bar.add(new JLabel("Destination:")); bar.add(cbDst);
        bar.add(btn("BFS", () -> { graphPanel.clear(); routeOut.setText("BFS from " + src() + ":\n" + String.join(" > ", g.bfs(src()))); }));
        bar.add(btn("DFS", () -> { graphPanel.clear(); routeOut.setText("DFS from " + src() + ":\n" + String.join(" > ", g.dfs(src()))); }));
        bar.add(btn("Components", () -> {
            List<List<String>> cs = g.components();
            StringBuilder sb = new StringBuilder("Connected components: " + cs.size() + "\n");
            for (List<String> c : cs) sb.append("  ").append(c).append('\n');
            graphPanel.highlightNodes(cs.get(0), new Color(40, 140, 80));
            routeOut.setText(sb.toString());
        }));
        bar.add(btn("DFS Spanning Tree", () -> {
            List<RouteGraph.Edge> t = g.dfsSpanningTree(src());
            StringBuilder sb = new StringBuilder("DFS spanning tree from " + src() + " (" + t.size() + " edges):\n");
            for (RouteGraph.Edge e : t) sb.append("  ").append(g.edgeStr(e)).append('\n');
            graphPanel.highlightEdges(t, new Color(200, 120, 20));
            routeOut.setText(sb.toString());
        }));
        bar.add(btn("Prim MST", () -> showMst("Prim", g.prim(src()))));
        bar.add(btn("Kruskal MST", () -> showMst("Kruskal (Union-Find)", g.kruskal(src()))));
        bar.add(btn("Dijkstra (S to D)", () -> {
            RouteGraph.Paths ps = g.dijkstra(src());
            List<String> path = g.pathTo(ps, dst());
            graphPanel.highlightPath(path, new Color(220, 50, 47));
            StringBuilder sb = new StringBuilder("DIJKSTRA shortest distances from " + src() + ":\n");
            for (String n : g.names()) { int d = ps.dist[g.idx(n)]; sb.append(String.format("  %-4s %s%n", n, d >= RouteGraph.INF ? "unreachable" : d + " km")); }
            sb.append("\nRoute ").append(src()).append(" -> ").append(dst()).append(": ")
              .append(path == null ? "NO ROUTE" : String.join(" > ", path) + "  (" + ps.dist[g.idx(dst())] + " km)");
            routeOut.setText(sb.toString());
        }));
        bar.add(btn("Bellman-Ford (energy)", () -> {
            RouteGraph.BF r = RouteGraph.bellmanFord(g.size(), SampleData.energyEdges(g), g.idx(src()));
            StringBuilder sb = new StringBuilder("BELLMAN-FORD on directed ENERGY graph (negative edge D->C = -2 downhill energy gain)\nSource " + src() + ":\n");
            for (String n : g.names()) { int d = r.dist[g.idx(n)]; sb.append(String.format("  %-4s %s%n", n, d >= RouteGraph.INF ? "unreachable" : String.valueOf(d))); }
            sb.append("Negative cycle detected: ").append(r.negativeCycle).append("\nDijkstra cannot be used here because of the negative edge.");
            graphPanel.clear();
            routeOut.setText(sb.toString());
        }));
        bar.add(btn("Floyd-Warshall", () -> { graphPanel.clear(); routeOut.setText("FLOYD-WARSHALL all-pairs shortest distance (km):\n" + g.matrixText(g.floydWarshall())); }));
        bar.add(btn("Transitive Closure", () -> {
            boolean[][] r = g.transitiveClosure();
            int[][] m = new int[g.size()][g.size()];
            for (int i = 0; i < m.length; i++) for (int j = 0; j < m.length; j++) m[i][j] = r[i][j] ? 1 : RouteGraph.INF;
            graphPanel.clear();
            routeOut.setText("TRANSITIVE CLOSURE (1 = reachable, INF = not reachable):\n" + g.matrixText(m));
        }));
        bar.add(btn("Adjacency Matrix", () -> { graphPanel.clear(); routeOut.setText("ADJACENCY MATRIX:\n" + g.matrixText(g.adjacencyMatrix()) + "\nSpace O(V^2) vs adjacency list O(V+E)."); }));
        JPanel top = new JPanel(new BorderLayout());
        top.add(bar, BorderLayout.NORTH);
        top.add(graphPanel, BorderLayout.CENTER);
        p.add(top, BorderLayout.CENTER);
        JScrollPane sp = new JScrollPane(routeOut);
        sp.setPreferredSize(new Dimension(100, 230));
        p.add(sp, BorderLayout.SOUTH);
        return p;
    }
    private String src() { return (String) cbSrc.getSelectedItem(); }
    private String dst() { return (String) cbDst.getSelectedItem(); }
    private void showMst(String name, RouteGraph.Mst m) {
        StringBuilder sb = new StringBuilder(name + " minimum spanning tree of the component containing " + src() + ":\n");
        for (RouteGraph.Edge e : m.edges) sb.append("  ").append(g.edgeStr(e)).append('\n');
        sb.append("Total cost = ").append(m.cost).append(" km");
        graphPanel.highlightEdges(m.edges, new Color(30, 150, 70));
        routeOut.setText(sb.toString());
    }

    // ===================== DP / backtracking / analysis =====================
    private JComponent dpTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dpOut = area(30);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(btn("0/1 Knapsack (payload)", () -> show(dpOut, DPModules.knapsack(SampleData.ITEMS, SampleData.ITEM_W, SampleData.ITEM_V, 50))));
        bar.add(btn("LCS (flight plans)", () -> show(dpOut, DPModules.lcs(SampleData.PLAN1, SampleData.PLAN2))));
        bar.add(btn("Matrix Chain", () -> show(dpOut, DPModules.matrixChain(SampleData.MATRIX_DIMS))));
        bar.add(btn("Resource Allocation", () -> show(dpOut, DPModules.resourceAllocation(SampleData.MISSIONS, SampleData.PROFIT, 6))));
        p.add(bar, BorderLayout.NORTH);
        p.add(new JScrollPane(dpOut), BorderLayout.CENTER);
        return p;
    }
    private JComponent btTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btOut = area(30);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(btn("TSP (Branch & Bound)", () -> show(btOut, Backtracking.tsp(g, "Hub", Arrays.asList("A", "C", "D", "E")))));
        bar.add(btn("Graph Colouring", () -> show(btOut, Backtracking.graphColoring(g))));
        bar.add(btn("N-Queens (6)", () -> show(btOut, Backtracking.nQueens(6))));
        bar.add(btn("Hamiltonian Cycle", () -> show(btOut, Backtracking.hamiltonian(g))));
        bar.add(btn("Sum of Subsets", () -> show(btOut, Backtracking.sumOfSubsets(new int[]{2, 3, 5, 7, 8}, 10))));
        p.add(bar, BorderLayout.NORTH);
        p.add(new JScrollPane(btOut), BorderLayout.CENTER);
        return p;
    }
    private JComponent analysisTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        anOut = area(30);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(btn("Complexity Table", () -> show(anOut, Analysis.complexityTable())));
        bar.add(btn("Heap Sort (battery)", () -> show(anOut, Analysis.heapSortDemo(fm.drones.inorder()))));
        bar.add(btn("BST vs AVL", () -> show(anOut, Analysis.bstVsAvl(15))));
        bar.add(btn("Unit 5 Comparison", () -> show(anOut, Analysis.enterpriseComparison())));
        bar.add(btn("Run Full Console Demo", () -> {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            Demo.run(new PrintStream(bo));
            show(anOut, bo.toString());
        }));
        p.add(bar, BorderLayout.NORTH);
        p.add(new JScrollPane(anOut), BorderLayout.CENTER);
        return p;
    }
    private void show(JTextArea a, String s) { a.setText(s); a.setCaretPosition(0); }

    // ===================== refresh =====================
    public void refreshAll() {
        List<Drone> ds = fm.drones.inorder();
        lblTotal.setText(String.valueOf(ds.size()));
        lblAvail.setText(String.valueOf(fm.availableDrones()));
        lblActive.setText(String.valueOf(fm.count(Delivery.Status.ASSIGNED)));
        lblPending.setText(String.valueOf(fm.pending.size()));
        lblDone.setText(String.valueOf(fm.count(Delivery.Status.COMPLETED)));
        droneModel.setRowCount(0);
        for (Drone d : ds) droneModel.addRow(new Object[]{d.label(), d.model, d.battery, d.capacityKg, d.location, d.status});
        deliveryModel.setRowCount(0);
        for (Delivery d : fm.deliveries)
            deliveryModel.addRow(new Object[]{d.id, d.source, d.destination, d.weightKg, d.priorityName(), d.status,
                d.droneId < 0 ? "-" : "D" + d.droneId, d.status == Delivery.Status.PENDING ? "-" : d.distanceKm,
                d.status == Delivery.Status.PENDING ? "-" : d.batteryCost, d.route});
        StringBuilder lg = new StringBuilder();
        for (int i = fm.log.size() - 1; i >= 0; i--) lg.append(fm.log.get(i)).append('\n');
        logOut.setText(lg.length() == 0 ? "No activity yet. Go to the Deliveries tab and create a delivery." : lg.toString());
        StringBuilder pq = new StringBuilder();
        for (Delivery d : fm.pending.sortedView()) pq.append(d).append('\n');
        pendingOut.setText(pq.length() == 0 ? "(queue empty)" : pq.toString());
    }
}
