package drone;

import java.util.*;

/** Unit 1: self-balancing AVL tree (ids only) used to compare height with the plain BST. */
public class AVLTree {
    private static class Node {
        int key, h = 1; Node l, r;
        Node(int k) { key = k; }
    }
    private Node root;
    private int rotations;

    public int rotations() { return rotations; }
    public int height() { return h(root); }
    private int h(Node n) { return n == null ? 0 : n.h; }
    private void upd(Node n) { n.h = 1 + Math.max(h(n.l), h(n.r)); }
    private int bal(Node n) { return n == null ? 0 : h(n.l) - h(n.r); }

    private Node rotR(Node y) { Node x = y.l; y.l = x.r; x.r = y; upd(y); upd(x); rotations++; return x; }
    private Node rotL(Node x) { Node y = x.r; x.r = y.l; y.l = x; upd(x); upd(y); rotations++; return y; }

    public void insert(int k) { root = insert(root, k); }
    private Node insert(Node n, int k) {
        if (n == null) return new Node(k);
        if (k < n.key) n.l = insert(n.l, k);
        else if (k > n.key) n.r = insert(n.r, k);
        else return n;
        upd(n);
        int b = bal(n);
        if (b > 1 && k < n.l.key) return rotR(n);
        if (b < -1 && k > n.r.key) return rotL(n);
        if (b > 1 && k > n.l.key) { n.l = rotL(n.l); return rotR(n); }
        if (b < -1 && k < n.r.key) { n.r = rotR(n.r); return rotL(n); }
        return n;
    }
    public boolean contains(int k) {
        Node n = root;
        while (n != null) { if (k == n.key) return true; n = k < n.key ? n.l : n.r; }
        return false;
    }
}
