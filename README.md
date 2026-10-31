# Intelligent Drone Fleet Management System (Java + DSA-II)

Individual PBL project - Data Structures and Algorithms. Pure Core Java + Swing, no external libraries.

## How to run (30 seconds)
1. Install JDK/JRE **17 or newer** (check with `java -version`).
2. Extract this ZIP and open the inner `DroneFleet` folder.
3. **Windows:** double-click `run.bat` (keep that black window open). Your browser opens the dashboard at `http://localhost:8080`.
   - Terminal: `java -jar DroneFleet.jar --web`
   - Linux/Mac: `sh run.sh`
4. Old desktop window (Swing): `run_swing.bat` or `java -jar DroneFleet.jar`
5. Console proof of every algorithm: `run_demo.bat` or `java -jar DroneFleet.jar --demo`
6. Rebuild from source (full JDK): `compile.bat` / `sh compile.sh`

The web dashboard is served by Java itself (JDK built-in HTTP server) - no Node, no internet, no extra install. All algorithms run in the Java DSA classes; the browser only displays them.

## What is inside the application (tabs - same in web and Swing)
| Tab | What you can do | DSA used |
|---|---|---|
| Dashboard | Live counters, activity log, pending queue | Heap view |
| Drones (BST) | Add / search / update / delete drones, in-order / pre-order / post-order, charge | BST, traversals |
| Deliveries | Create deliveries, auto-assignment, complete, re-dispatch pending | Max-heap priority queue, greedy, Floyd-Warshall, Dijkstra |
| Route Network | Graph drawing + BFS, DFS, components, spanning tree, Prim, Kruskal, Dijkstra, Bellman-Ford, Floyd-Warshall, transitive closure, adjacency matrix/list | Unit 2 (all) |
| Optimization (DP) | 0/1 Knapsack, LCS, Matrix Chain, Resource Allocation with DP tables + back-tracking | Unit 3 |
| Backtracking / B&B | TSP (branch & bound), graph colouring, N-Queens, Hamiltonian cycle, sum of subsets | Unit 4 |
| Analysis | Complexity table, heap sort, BST vs AVL height, Unit 5 comparison, full console demo | Unit 1 / 5 |

## Honest status per unit
- **Unit 1 (Trees)**: BST (insert/search/modify/delete/3 traversals), AVL (insert/search/height/rotations), binary max-heap priority queue, heap sort - **implemented**. Threaded trees: **not implemented**.
- **Unit 2 (Graphs)**: **fully implemented** (list + matrix, BFS, DFS, components, spanning tree, Prim, Kruskal + Union-Find, Dijkstra, Bellman-Ford, Floyd-Warshall, closure).
- **Unit 3 (DP)**: **fully implemented**, each compared with naive recursion.
- **Unit 4**: TSP (branch & bound), graph colouring, N-Queens, Hamiltonian cycle, sum of subsets - **implemented**.
- **Unit 5** (Red-Black, B/B+, Binomial, Fibonacci): **written analysis only** (Analysis tab / `docs/VIVA_NOTES.md`), not coded.

## Sample data (matches Progress Report 2 numbers)
8 locations, 9 routes. Expected results: MST = 14 km (Prim = Kruskal), Dijkstra from Hub: A=4, B=3, C=9, D=9, E=12, F/G unreachable;
Knapsack 165 (Medical kit + Camera + Sensor pod), LCS = 4 (Hub, A, D, E), Matrix chain = 4500 (order (M1 x M2) x M3), Resource allocation = 28 with [2,3,1].

## Files
```
DroneFleet.jar        ready-to-run application
src/drone/*.java      all Java source code
src/web/index.html    web dashboard front-end (HTML/CSS/JS, no libraries)
docs/VIVA_NOTES.md    demo script + viva questions & answers
run.bat / run.sh      launch web dashboard      compile.bat / compile.sh   rebuild
```
Simulation only - it does not control real drones.