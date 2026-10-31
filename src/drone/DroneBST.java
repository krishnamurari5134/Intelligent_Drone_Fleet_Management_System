package drone;

import java.util.*;

/** Unit 1: Binary Search Tree of drone records (key = drone id). */
public class DroneBST {
    private static class Node {
        Drone d; Node left, right;
        Node(Drone d) { this.d = d; }
    }
    private Node root;
    private int size;

    public int size() { return size; }

    public boolean insert(Drone d) {
        if (search(d.id) != null) return false;
        root = insert(root, d);
        size++;
        return true;
    }
    private Node insert(Node n, Drone d) {
        if (n == null) return new Node(d);
        if (d.id < n.d.id) n.left = insert(n.left, d);
        else n.right = insert(n.right, d);
        return n;
    }

    public Drone search(int id) {
        Node n = root;
        while (n != null) {
            if (id == n.d.id) return n.d;
            n = id < n.d.id ? n.left : n.right;
        }
        return null;
    }

    /** Modify = search + update the record in place. */
    public boolean modify(int id, Integer battery, String location) {
        Drone d = search(id);
        if (d == null) return false;
        if (battery != null) d.battery = battery;
        if (location != null) d.location = location;
        return true;
    }

    public boolean delete(int id) {
        if (search(id) == null) return false;
        root = delete(root, id);
        size--;
        return true;
    }
    private Node delete(Node n, int id) {
        if (n == null) return null;
        if (id < n.d.id) n.left = delete(n.left, id);
        else if (id > n.d.id) n.right = delete(n.right, id);
        else {
            if (n.left == null) return n.right;
            if (n.right == null) return n.left;
            Node s = n.right;                       // in-order successor
            while (s.left != null) s = s.left;
            n.d = s.d;
            n.right = delete(n.right, s.d.id);
        }
        return n;
    }

    public List<Drone> inorder()   { List<Drone> l = new ArrayList<>(); in(root, l);   return l; }
    public List<Drone> preorder()  { List<Drone> l = new ArrayList<>(); pre(root, l);  return l; }
    public List<Drone> postorder() { List<Drone> l = new ArrayList<>(); post(root, l); return l; }
    private void in(Node n, List<Drone> l)   { if (n == null) return; in(n.left, l);   l.add(n.d); in(n.right, l); }
    private void pre(Node n, List<Drone> l)  { if (n == null) return; l.add(n.d); pre(n.left, l);  pre(n.right, l); }
    private void post(Node n, List<Drone> l) { if (n == null) return; post(n.left, l); post(n.right, l); l.add(n.d); }

    public int height() { return height(root); }
    private int height(Node n) { return n == null ? 0 : 1 + Math.max(height(n.left), height(n.right)); }
}
