package com.interview.dsa;

import java.util.*;

/**
 * Dynamic Programming patterns: Coin Change, LCS, Climbing Stairs, House Robber, Knapsack.
 */
public class DPPatterns {

    // Climbing Stairs — O(n) time, O(1) space
    public static int climbStairs(int n) {
        if (n <= 2) return n;
        int prev2 = 1, prev1 = 2;
        for (int i = 3; i <= n; i++) {
            int curr = prev1 + prev2;
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // Coin Change — O(amount × coins) time, O(amount) space
    public static int coinChange(int[] coins, int amount) {
        int[] dp = new int[amount + 1];
        Arrays.fill(dp, amount + 1);
        dp[0] = 0;
        for (int i = 1; i <= amount; i++) {
            for (int coin : coins) {
                if (coin <= i) dp[i] = Math.min(dp[i], dp[i - coin] + 1);
            }
        }
        return dp[amount] > amount ? -1 : dp[amount];
    }

    // Longest Common Subsequence — O(m×n) time, O(m×n) space
    public static int longestCommonSubsequence(String text1, String text2) {
        int m = text1.length(), n = text2.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (text1.charAt(i - 1) == text2.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                else
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
        return dp[m][n];
    }

    // House Robber — O(n) time, O(1) space
    public static int rob(int[] nums) {
        if (nums.length == 1) return nums[0];
        int prev2 = 0, prev1 = 0;
        for (int num : nums) {
            int curr = Math.max(prev1, prev2 + num);
            prev2 = prev1;
            prev1 = curr;
        }
        return prev1;
    }

    // 0/1 Knapsack — O(n×W) time, O(W) space (optimized)
    public static int knapsack(int[] weights, int[] values, int capacity) {
        int[] dp = new int[capacity + 1];
        for (int i = 0; i < weights.length; i++) {
            for (int w = capacity; w >= weights[i]; w--) { // reverse to avoid reuse
                dp[w] = Math.max(dp[w], dp[w - weights[i]] + values[i]);
            }
        }
        return dp[capacity];
    }

    // Longest Increasing Subsequence — O(n log n) with patience sorting
    public static int lengthOfLIS(int[] nums) {
        List<Integer> tails = new ArrayList<>();
        for (int num : nums) {
            int pos = Collections.binarySearch(tails, num);
            if (pos < 0) pos = -(pos + 1);
            if (pos == tails.size()) tails.add(num);
            else tails.set(pos, num);
        }
        return tails.size();
    }

    public static void main(String[] args) {
        System.out.println("=== Dynamic Programming Patterns ===\n");

        System.out.println("Climb stairs (5): " + climbStairs(5));
        System.out.println("Coin change [1,5,10] amount=27: " + coinChange(new int[]{1, 5, 10}, 27));
        System.out.println("LCS 'abcde' vs 'ace': " + longestCommonSubsequence("abcde", "ace"));
        System.out.println("House robber [2,7,9,3,1]: " + rob(new int[]{2, 7, 9, 3, 1}));
        System.out.println("Knapsack w=[1,3,4,5] v=[1,4,5,7] cap=7: " +
            knapsack(new int[]{1, 3, 4, 5}, new int[]{1, 4, 5, 7}, 7));
        System.out.println("LIS [10,9,2,5,3,7,101,18]: " + lengthOfLIS(new int[]{10, 9, 2, 5, 3, 7, 101, 18}));
    }
}
