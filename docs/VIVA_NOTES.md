# Viva & Demo Notes

## 5-minute demo order
(Start with `run.bat`; browser opens at localhost:8080. Press **Reset** before the demo. The **Run demo scenario** button creates 7 deliveries automatically and shows the pending queue.)

1. **Dashboard**: 5 drones, 5 available, 0 deliveries.
2. **Drones tab**: click In-order / Pre-order / Post-order (BST). Search D104. Add a drone. Explain: key = drone ID, average O(log n).
3. **Route Network tab**: click *Components* (2 clusters, {F,G} isolated), *Prim MST* then *Kruskal MST* (both 14 km), then *Dijkstra* with Hub -> E (red path Hub>B>C>E = 12 km).
4. **Deliveries tab**: create Hub -> E, URGENT. Watch drone get assigned (Dashboard counters change). Create 4 more until all drones are busy. The next one goes to the **pending queue**. Click *Complete Selected* on delivery #1: the freed drone automatically takes the **URGENT pending** delivery first.
5. **Optimization tab**: Knapsack (show DP table + "naive vs DP"), Resource Allocation.
6. **Backtracking tab**: TSP branch & bound (27 km tour, pruning counts).

## Likely viva questions
**Why a BST for drone records?** Searching/updating by ID is O(log n) on average vs O(n) for a list; in-order traversal gives sorted output for free. Weakness: sorted insertion makes it a linked list (height n) - that is why the Analysis tab compares it with an AVL tree (height ~log n, rotations keep balance).

**Why a heap for pending deliveries?** Always need the most urgent delivery next. Max-heap gives insert and extract-max in O(log n); sorting a list each time would be O(n log n).

**Why adjacency list not matrix?** Route networks are sparse (E << V^2). List uses O(V+E) space, matrix O(V^2). Matrix only wins for O(1) edge lookup in dense graphs.

**Dijkstra vs Bellman-Ford vs Floyd-Warshall?** Dijkstra: single source, non-negative weights, O((V+E) log V) with heap. Bellman-Ford: handles negative edges (energy gain/downhill) and detects negative cycles, O(VE). Floyd-Warshall: all pairs, O(V^3) - used here as a pre-computed distance table so drone assignment is O(1) per distance lookup.

**Prim vs Kruskal?** Both give 14 km. Prim grows one tree from a start node using a heap (good for dense graphs, O(E log V)); Kruskal sorts edges and uses Union-Find to avoid cycles (good for sparse graphs, O(E log E)).

**How does assignment work?** Greedy: among AVAILABLE drones with enough capacity and battery (1.5% per km plus 15% reserve) choose the one nearest to the pickup. Greedy is fast but not guaranteed globally optimal (that would be an assignment problem) - say this honestly.

**Where is DP used and why better?** Knapsack picks payload under weight limit; naive recursion is O(2^n), DP O(nW). LCS compares two flight plans; matrix chain finds cheapest multiplication order (4500 vs 27000 for the other order); resource allocation splits charging slots.

**Why does TSP use branch & bound?** n! tours explode. Bound = cost so far + minimum outgoing edge of every unvisited node; branches that cannot beat the best tour are pruned.

**Why no Hamiltonian cycle?** Hub has only two neighbours (A, B) and A has only Hub and B, so any cycle through them is the triangle Hub-A-B; also F,G are disconnected.

**Unit 5 (analysis only):** Red-Black = balanced BST with fewer rotations (Java TreeMap); B/B+ trees = disk-based indexes, B+ best for range queries; Binomial heap = mergeable heap; Fibonacci heap = amortised O(1) decrease-key, speeds Dijkstra to O(E + V log V) on huge graphs. Recommendation: AVL/Red-Black in memory, B+ tree for flight-log archive, binary heap for mission queue.

## Known limits (say them before they ask)
- Simulation, no real drone hardware / GPS.
- Greedy assignment, not optimal matching.
- Fixed sample network (8 nodes); graph is not editable from the GUI.
- Threaded trees and Unit 5 structures are discussed, not coded.

## If asked "why a web dashboard?"
The Java program is the engine (all DSA in Java classes). It exposes a small JSON API using the JDK built-in HttpServer; the browser page only draws results. This separates logic from UI and is how real fleet-management software is structured. Simulation only, no real drones.
