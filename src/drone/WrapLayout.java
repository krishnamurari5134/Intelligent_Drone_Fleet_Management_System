package drone;

import java.awt.*;

/** FlowLayout that wraps to several rows and reports the right preferred height. */
public class WrapLayout extends FlowLayout {
    public WrapLayout() { super(FlowLayout.LEFT, 6, 4); }
    @Override public Dimension preferredLayoutSize(Container t) { return size(t, true); }
    @Override public Dimension minimumLayoutSize(Container t) { return size(t, false); }
    private Dimension size(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int w = target.getSize().width;
            if (w == 0) w = 1100;
            Insets in = target.getInsets();
            int max = w - in.left - in.right - getHgap() * 2;
            int x = 0, y = in.top + getVgap(), rowH = 0;
            for (Component c : target.getComponents()) {
                if (!c.isVisible()) continue;
                Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                if (x > 0 && x + d.width > max) { y += rowH + getVgap(); x = 0; rowH = 0; }
                x += d.width + getHgap();
                rowH = Math.max(rowH, d.height);
            }
            y += rowH + getVgap() + in.bottom;
            return new Dimension(w, y);
        }
    }
}
