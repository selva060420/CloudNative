package com.interview.dsa;

import java.util.*;

/**
 * Heap & Stack patterns: Top K Frequent, Median Finder, Next Greater Element, Valid Parentheses.
 */
public class HeapStackPatterns {

    // Top K Frequent Elements — O(n log k)
    public static int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> freq = new HashMap<>();
        for (int n : nums) freq.merge(n, 1, Integer::sum);

        PriorityQueue<Integer> minHeap = new PriorityQueue<>(Comparator.comparingInt(freq::get));
        for (int num : freq.keySet()) {
            minHeap.offer(num);
            if (minHeap.size() > k) minHeap.poll();
        }
        return minHeap.stream().mapToInt(i -> i).toArray();
    }

    // Find Median from Data Stream — O(log n) insert, O(1) median
    static class MedianFinder {
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        void addNum(int num) {
            maxHeap.offer(num);
            minHeap.offer(maxHeap.poll());
            if (minHeap.size() > maxHeap.size()) maxHeap.offer(minHeap.poll());
        }

        double findMedian() {
            if (maxHeap.size() > minHeap.size()) return maxHeap.peek();
            return (maxHeap.peek() + minHeap.peek()) / 2.0;
        }
    }

    // Next Greater Element — Monotonic Stack O(n)
    public static int[] nextGreaterElement(int[] nums) {
        int[] result = new int[nums.length];
        Arrays.fill(result, -1);
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < nums.length; i++) {
            while (!stack.isEmpty() && nums[stack.peek()] < nums[i]) {
                result[stack.pop()] = nums[i];
            }
            stack.push(i);
        }
        return result;
    }

    // Valid Parentheses — Stack O(n)
    public static boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            if (c == '(') stack.push(')');
            else if (c == '{') stack.push('}');
            else if (c == '[') stack.push(']');
            else if (stack.isEmpty() || stack.pop() != c) return false;
        }
        return stack.isEmpty();
    }

    // Merge K Sorted Lists — Min Heap O(N log k)
    public static int[] mergeKSorted(int[][] lists) {
        PriorityQueue<int[]> heap = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));
        int total = 0;
        for (int i = 0; i < lists.length; i++) {
            if (lists[i].length > 0) {
                heap.offer(new int[]{lists[i][0], i, 0}); // value, listIdx, elemIdx
                total += lists[i].length;
            }
        }
        int[] result = new int[total];
        int idx = 0;
        while (!heap.isEmpty()) {
            int[] curr = heap.poll();
            result[idx++] = curr[0];
            int listIdx = curr[1], elemIdx = curr[2] + 1;
            if (elemIdx < lists[listIdx].length) {
                heap.offer(new int[]{lists[listIdx][elemIdx], listIdx, elemIdx});
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("=== Heap & Stack Patterns ===\n");

        // Top K Frequent
        int[] topK = topKFrequent(new int[]{1, 1, 1, 2, 2, 3}, 2);
        System.out.println("Top 2 frequent [1,1,1,2,2,3]: " + Arrays.toString(topK));

        // Median Finder
        MedianFinder mf = new MedianFinder();
        mf.addNum(1); mf.addNum(2); mf.addNum(3);
        System.out.println("Median after [1,2,3]: " + mf.findMedian());

        // Next Greater Element
        System.out.println("Next greater [2,1,2,4,3]: " + Arrays.toString(nextGreaterElement(new int[]{2, 1, 2, 4, 3})));

        // Valid Parentheses
        System.out.println("Valid '({[]})': " + isValid("({[]})"));
        System.out.println("Valid '([)]': " + isValid("([)]"));

        // Merge K Sorted
        int[] merged = mergeKSorted(new int[][]{{1, 4, 7}, {2, 5, 8}, {3, 6, 9}});
        System.out.println("Merge K sorted: " + Arrays.toString(merged));
    }
}
