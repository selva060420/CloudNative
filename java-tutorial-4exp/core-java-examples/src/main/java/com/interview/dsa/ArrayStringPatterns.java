package com.interview.dsa;

import java.util.*;

/**
 * Array & String patterns: Two Sum, Sliding Window, Kadane's, Two Pointers.
 */
public class ArrayStringPatterns {

    // Two Sum — O(n) time, O(n) space
    public static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (map.containsKey(complement)) return new int[]{map.get(complement), i};
            map.put(nums[i], i);
        }
        return new int[]{};
    }

    // Longest Substring Without Repeating — Sliding Window O(n)
    public static int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int maxLen = 0, start = 0;
        for (int end = 0; end < s.length(); end++) {
            char c = s.charAt(end);
            if (lastSeen.containsKey(c) && lastSeen.get(c) >= start) {
                start = lastSeen.get(c) + 1;
            }
            lastSeen.put(c, end);
            maxLen = Math.max(maxLen, end - start + 1);
        }
        return maxLen;
    }

    // Kadane's Algorithm — Maximum Subarray Sum O(n)
    public static int maxSubArray(int[] nums) {
        int maxSum = nums[0], currentSum = nums[0];
        for (int i = 1; i < nums.length; i++) {
            currentSum = Math.max(nums[i], currentSum + nums[i]);
            maxSum = Math.max(maxSum, currentSum);
        }
        return maxSum;
    }

    // Container With Most Water — Two Pointers O(n)
    public static int maxArea(int[] height) {
        int left = 0, right = height.length - 1, max = 0;
        while (left < right) {
            int area = Math.min(height[left], height[right]) * (right - left);
            max = Math.max(max, area);
            if (height[left] < height[right]) left++;
            else right--;
        }
        return max;
    }

    // 3Sum — Sort + Two Pointers O(n²)
    public static List<List<Integer>> threeSum(int[] nums) {
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) {
            if (i > 0 && nums[i] == nums[i - 1]) continue; // skip duplicates
            int left = i + 1, right = nums.length - 1;
            while (left < right) {
                int sum = nums[i] + nums[left] + nums[right];
                if (sum == 0) {
                    result.add(List.of(nums[i], nums[left], nums[right]));
                    while (left < right && nums[left] == nums[left + 1]) left++;
                    while (left < right && nums[right] == nums[right - 1]) right--;
                    left++; right--;
                } else if (sum < 0) left++;
                else right--;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("=== Array & String Patterns ===\n");

        // Two Sum
        int[] result = twoSum(new int[]{2, 7, 11, 15}, 9);
        System.out.println("Two Sum [2,7,11,15] target=9: " + Arrays.toString(result));

        // Sliding Window
        System.out.println("Longest substring 'abcabcbb': " + lengthOfLongestSubstring("abcabcbb"));

        // Kadane's
        System.out.println("Max subarray [-2,1,-3,4,-1,2,1,-5,4]: " + maxSubArray(new int[]{-2, 1, -3, 4, -1, 2, 1, -5, 4}));

        // Container With Most Water
        System.out.println("Max area [1,8,6,2,5,4,8,3,7]: " + maxArea(new int[]{1, 8, 6, 2, 5, 4, 8, 3, 7}));

        // 3Sum
        System.out.println("3Sum [-1,0,1,2,-1,-4]: " + threeSum(new int[]{-1, 0, 1, 2, -1, -4}));
    }
}
