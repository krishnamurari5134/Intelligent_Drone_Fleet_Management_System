package drone;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Draws the route network; algorithm results can be highlighted. */
public class GraphPanel extends JPanel {
    private final RouteGraph g;
    private final Map<String, Point> pos = new HashMap<>();
    private final Set<String> hlEdges = new HashSet<>();
    private final Set<String> hlNodes = new HashSet<>();
    private Color hlColor = new Color(220, 50, 47);

    public GraphPanel(RouteGraph g) {
        this.g = g;
        setPreferredSize(new Dimension(680, 340));
        setBackground(Color.WHITE);
        pos.put("Hub", new Point(70, 170)); pos.put("A", new Point(190, 70)); pos.put("B", new Point(190, 270));
        pos.put("C", new Point(350, 270)); pos.put("D", new Point(350, 70)); pos.put("E", new Point(480, 170));
        pos.put("F", new Point(590, 90)); pos.put("G", new Point(590, 260));
    }
    private String key(int u, int v) { return Math.min(u, v) + "-" + Math.max(u, v); }

    public void clear() { hlEdges.clear(); hlNodes.clear(); repaint(); }
    public void highlightEdges(Collection<RouteGraph.Edge> es, Color c) {
        clear(); hlColor = c;
        for (RouteGraph.Edge e : es) { hlEdges.add(key(e.u, e.v)); hlNodes.add(g.name(e.u)); hlNodes.add(g.name(e.v)); }
        repaint();
    }
    public void highlightPath(List<String> path, Color c) {
        clear(); hlColor = c;
        if (path == null) return;
        hlNodes.addAll(path);
        for (int i = 0; i + 1 < path.size(); i++) hlEdges.add(key(g.idx(path.get(i)), g.idx(path.get(i + 1))));
        repaint();
    }
    public void highlightNodes(Collection<String> nodes, Color c) { clear(); hlColor = c; hlNodes.addAll(nodes); repaint(); }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g2 = (Graphics2D) g0;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        for (RouteGraph.Edge e : g.edges()) {
            Point a = pos.get(g.name(e.u)), b = pos.get(g.name(e.v));
            boolean hl = hlEdges.contains(key(e.u, e.v));
            g2.setStroke(new BasicStroke(hl ? 4f : 2f));
            g2.setColor(hl ? hlColor : new Color(150, 150, 150));
            g2.drawLine(a.x, a.y, b.x, b.y);
            g2.setColor(new Color(30, 60, 120));
            g2.drawString(String.valueOf(e.w), (a.x + b.x) / 2 + 6, (a.y + b.y) / 2 - 6);
        }
        for (String n : g.names()) {
            Point p = pos.get(n);
            boolean hl = hlNodes.contains(n);
            g2.setColor(hl ? hlColor : new Color(35, 80, 150));
            g2.fillOval(p.x - 20, p.y - 20, 40, 40);
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(n, p.x - fm.stringWidth(n) / 2, p.y + 5);
        }
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("Edge labels = distance in km. Red/green/orange edges = last algorithm result.", 10, getHeight() - 8);
    }
}
