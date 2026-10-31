package drone;

import java.util.*;

/** Unit 1: array-based binary max-heap = priority queue of pending deliveries. */
public class MaxHeap {
    private final List<Delivery> a = new ArrayList<>();

    /** higher priority first; for equal priority the older (smaller id) delivery first. */
    private boolean before(Delivery x, Delivery y) {
        if (x.priority != y.priority) return x.priority > y.priority;
        return x.id < y.id;
    }
    public int size() { return a.size(); }
    public boolean isEmpty() { return a.isEmpty(); }

    public void insert(Delivery d) {            // O(log n)
        a.add(d);
        int i = a.size() - 1;
        while (i > 0) {
            int p = (i - 1) / 2;
            if (before(a.get(i), a.get(p))) { Collections.swap(a, i, p); i = p; } else break;
        }
    }
    public Delivery extractMax() {              // O(log n)
        if (a.isEmpty()) return null;
        Delivery top = a.get(0);
        Delivery last = a.remove(a.size() - 1);
        if (!a.isEmpty()) {
            a.set(0, last);
            int i = 0;
            while (true) {
                int l = 2 * i + 1, r = l + 1, m = i;
                if (l < a.size() && before(a.get(l), a.get(m))) m = l;
                if (r < a.size() && before(a.get(r), a.get(m))) m = r;
                if (m == i) break;
                Collections.swap(a, i, m); i = m;
            }
        }
        return top;
    }
    /** Non-destructive view in priority order. */
    public List<Delivery> sortedView() {
        MaxHeap copy = new MaxHeap();
        copy.a.addAll(a);
        List<Delivery> out = new ArrayList<>();
        Delivery d;
        while ((d = copy.extractMax()) != null) out.add(d);
        return out;
    }
}
