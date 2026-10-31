package drone;

import java.util.*;

/** Unit 3: Dynamic Programming modules. Each returns a printable report. */
public class DPModules {

    // ---------- 0/1 Knapsack: payload selection ----------
    private static long naiveKnap(int i, int c, int[] w, int[] v, long[] calls) {
        calls[0]++;
        if (i == 0 || c == 0) return 0;
        long skip = naiveKnap(i - 1, c, w, v, calls);
        if (w[i - 1] > c) return skip;
        return Math.max(skip, v[i - 1] + naiveKnap(i - 1, c - w[i - 1], w, v, calls));
    }
    /** weights in units of 0.1 kg. */
    public static String knapsack(String[] names, int[] w, int[] v, int cap) {
        int n = names.length;
        int[][] dp = new int[n + 1][cap + 1];
        for (int i = 1; i <= n; i++)
            for (int c = 0; c <= cap; c++) {
                dp[i][c] = dp[i - 1][c];
                if (w[i - 1] <= c) dp[i][c] = Math.max(dp[i][c], dp[i - 1][c - w[i - 1]] + v[i - 1]);
            }
        StringBuilder sb = new StringBuilder("0/1 KNAPSACK - payload selection (capacity " + cap / 10.0 + " kg)\n");
        sb.append("Items:\n");
        for (int i = 0; i < n; i++) sb.append(String.format("  %-14s %4.1f kg  value %d%n", names[i], w[i] / 10.0, v[i]));
        sb.append("\nDP table (rows = items considered, columns = capacity in 0.5 kg steps):\n");
        sb.append(String.format("%-14s", ""));
        for (int c = 0; c <= cap; c += 5) sb.append(String.format("%5.1f", c / 10.0));
        sb.append('\n');
        for (int i = 0; i <= n; i++) {
            sb.append(String.format("%-14s", i == 0 ? "(none)" : names[i - 1]));
            for (int c = 0; c <= cap; c += 5) sb.append(String.format("%5d", dp[i][c]));
            sb.append('\n');
        }
        List<String> chosen = new ArrayList<>();
        int c = cap, wt = 0;
        for (int i = n; i >= 1; i--) if (dp[i][c] != dp[i - 1][c]) { chosen.add(names[i - 1]); c -= w[i - 1]; wt += w[i - 1]; }
        Collections.reverse(chosen);
        long[] calls = {0};
        naiveKnap(n, cap, w, v, calls);
        sb.append("\nBest value : ").append(dp[n][cap]);
        sb.append("\nItems chosen: ").append(String.join(", ", chosen)).append(String.format("  (total %.1f kg)", wt / 10.0));
        sb.append(String.format("%nComparison on this small input: naive recursion = %d calls, DP table = %d cells.%n", calls[0], (long) (n + 1) * (cap + 1)));
        sb.append("Naive recursion doubles with every extra item (O(2^n)); DP stays O(nW).\n");
        sb.append(String.format("Scaling example: 30 items, same capacity -> naive up to %,d calls, DP only %,d cells.%n", (1L << 31) - 1, 30L * (cap + 1)));
        return sb.toString();
    }

    // ---------- LCS: compare two flight plans ----------
    private static int naiveLcs(String[] a, String[] b, int i, int j, long[] calls) {
        calls[0]++;
        if (i == a.length || j == b.length) return 0;
        if (a[i].equals(b[j])) return 1 + naiveLcs(a, b, i + 1, j + 1, calls);
        return Math.max(naiveLcs(a, b, i + 1, j, calls), naiveLcs(a, b, i, j + 1, calls));
    }
    public static String lcs(String[] a, String[] b) {
        int m = a.length, n = b.length;
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++)
            for (int j = 1; j <= n; j++)
                dp[i][j] = a[i - 1].equals(b[j - 1]) ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
        StringBuilder sb = new StringBuilder("LONGEST COMMON SUBSEQUENCE - flight plan comparison\n");
        sb.append("Plan 1: ").append(String.join(" > ", a)).append('\n');
        sb.append("Plan 2: ").append(String.join(" > ", b)).append("\n\nDP table:\n");
        sb.append(String.format("%-6s%-5s", "", ""));
        for (String s : b) sb.append(String.format("%-5s", s));
        sb.append('\n');
        for (int i = 0; i <= m; i++) {
            sb.append(String.format("%-6s", i == 0 ? "" : a[i - 1]));
            for (int j = 0; j <= n; j++) sb.append(String.format("%-5d", dp[i][j]));
            sb.append('\n');
        }
        LinkedList<String> common = new LinkedList<>();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (a[i - 1].equals(b[j - 1])) { common.addFirst(a[i - 1]); i--; j--; }
            else if (dp[i - 1][j] >= dp[i][j - 1]) i--; else j--;
        }
        long[] calls = {0};
        naiveLcs(a, b, 0, 0, calls);
        sb.append("\nLCS length : ").append(dp[m][n]);
        sb.append("\nCommon route: ").append(String.join(" > ", common));
        sb.append(String.format("%nComparison on this small input: naive recursion = %d calls, DP table = %d cells.%n", calls[0], (long) (m + 1) * (n + 1)));
        sb.append("Naive recursion is exponential in plan length (O(2^(m+n))); DP is O(mn). 20-stop plans: ~10^12 calls vs 441 cells.\n");
        return sb.toString();
    }

    // ---------- Matrix chain: order telemetry transforms ----------
    private static long naiveMcm(int[] p, int i, int j, long[] calls) {
        calls[0]++;
        if (i == j) return 0;
        long best = Long.MAX_VALUE;
        for (int k = i; k < j; k++)
            best = Math.min(best, naiveMcm(p, i, k, calls) + naiveMcm(p, k + 1, j, calls) + (long) p[i - 1] * p[k] * p[j]);
        return best;
    }
    public static String matrixChain(int[] p) {
        int n = p.length - 1;
        long[][] m = new long[n + 1][n + 1];
        int[][] s = new int[n + 1][n + 1];
        for (int len = 2; len <= n; len++)
            for (int i = 1; i <= n - len + 1; i++) {
                int j = i + len - 1;
                m[i][j] = Long.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    long cost = m[i][k] + m[k + 1][j] + (long) p[i - 1] * p[k] * p[j];
                    if (cost < m[i][j]) { m[i][j] = cost; s[i][j] = k; }
                }
            }
        StringBuilder sb = new StringBuilder("MATRIX CHAIN MULTIPLICATION - telemetry transform ordering\n");
        for (int i = 1; i <= n; i++) sb.append(String.format("  M%d : %d x %d%n", i, p[i - 1], p[i]));
        sb.append("\nCost table m[i][j]:\n");
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) sb.append(String.format("%10s", j < i ? "" : String.valueOf(m[i][j])));
            sb.append('\n');
        }
        long[] calls = {0};
        naiveMcm(p, 1, n, calls);
        sb.append("\nMinimum multiplications: ").append(m[1][n]);
        sb.append("\nBest order: ").append(paren(s, 1, n));
        sb.append(String.format("%nComparison on this small input: naive recursion = %d calls.%n", calls[0]));
        sb.append("Naive recursion grows exponentially with the number of matrices; DP is O(n^3) and each sub-chain is solved once.\n");
        return sb.toString();
    }
    private static String paren(int[][] s, int i, int j) {
        if (i == j) return "M" + i;
        return "(" + paren(s, i, s[i][j]) + " x " + paren(s, s[i][j] + 1, j) + ")";
    }

    // ---------- Resource allocation: charging slots to missions ----------
    /** profit[mission][slots] for slots 0..S */
    public static String resourceAllocation(String[] missions, int[][] profit, int slots) {
        int n = missions.length;
        int[][] dp = new int[n + 1][slots + 1];
        int[][] pick = new int[n + 1][slots + 1];
        for (int i = 1; i <= n; i++)
            for (int r = 0; r <= slots; r++)
                for (int k = 0; k <= r; k++) {
                    int val = dp[i - 1][r - k] + profit[i - 1][k];
                    if (val > dp[i][r]) { dp[i][r] = val; pick[i][r] = k; }
                }
        StringBuilder sb = new StringBuilder("RESOURCE ALLOCATION - distribute " + slots + " charging slots among missions\n");
        sb.append(String.format("%-12s", "Profit table"));
        for (int k = 0; k <= slots; k++) sb.append(String.format("%5d", k));
        sb.append('\n');
        for (int i = 0; i < n; i++) {
            sb.append(String.format("%-12s", missions[i]));
            for (int k = 0; k <= slots; k++) sb.append(String.format("%5d", profit[i][k]));
            sb.append('\n');
        }
        int[] alloc = new int[n];
        int r = slots;
        for (int i = n; i >= 1; i--) { alloc[i - 1] = pick[i][r]; r -= pick[i][r]; }
        sb.append("\nMaximum profit: ").append(dp[n][slots]).append('\n');
        for (int i = 0; i < n; i++) sb.append("  ").append(missions[i]).append(" -> ").append(alloc[i]).append(" slot(s)\n");
        sb.append("Allocation vector: ").append(Arrays.toString(alloc)).append('\n');
        sb.append("Complexity: O(n * S^2); brute force would try every split of S slots among n missions.\n");
        return sb.toString();
    }
}
