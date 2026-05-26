package com.interview.dsa;

import java.util.*;

/**
 * Sort & Search patterns: QuickSort, MergeSort, Binary Search variants, Merge Intervals.
 */
public class SortSearchPatterns {

    // QuickSort — O(n log n) avg, O(n²) worst
    public static void quickSort(int[] arr, int lo, int hi) {
        if (lo >= hi) return;
        int pivot = partition(arr, lo, hi);
        quickSort(arr, lo, pivot - 1);
        quickSort(arr, pivot + 1, hi);
    }

    private static int partition(int[] arr, int lo, int hi) {
        int pivot = arr[hi], i = lo;
        for (int j = lo; j < hi; j++) {
            if (arr[j] < pivot) { swap(arr, i++, j); }
        }
        swap(arr, i, hi);
        return i;
    }

    // MergeSort — O(n log n) guaranteed, O(n) space
    public static void mergeSort(int[] arr, int lo, int hi) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergeSort(arr, lo, mid);
        mergeSort(arr, mid + 1, hi);
        merge(arr, lo, mid, hi);
    }

    private static void merge(int[] arr, int lo, int mid, int hi) {
        int[] temp = Arrays.copyOfRange(arr, lo, hi + 1);
        int i = 0, j = mid - lo + 1, k = lo;
        while (i <= mid - lo && j <= hi - lo) {
            arr[k++] = temp[i] <= temp[j] ? temp[i++] : temp[j++];
        }
        while (i <= mid - lo) arr[k++] = temp[i++];
        while (j <= hi - lo) arr[k++] = temp[j++];
    }

    // Binary Search — first occurrence O(log n)
    public static int firstOccurrence(int[] nums, int target) {
        int lo = 0, hi = nums.length - 1, result = -1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] == target) { result = mid; hi = mid - 1; }
            else if (nums[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return result;
    }

    // Search in Rotated Sorted Array — O(log n)
    public static int searchRotated(int[] nums, int target) {
        int lo = 0, hi = nums.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if (nums[mid] == target) return mid;
            if (nums[lo] <= nums[mid]) { // left half sorted
                if (target >= nums[lo] && target < nums[mid]) hi = mid - 1;
                else lo = mid + 1;
            } else { // right half sorted
                if (target > nums[mid] && target <= nums[hi]) lo = mid + 1;
                else hi = mid - 1;
            }
        }
        return -1;
    }

    // Merge Intervals — O(n log n)
    public static int[][] mergeIntervals(int[][] intervals) {
        Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
        List<int[]> merged = new ArrayList<>();
        merged.add(intervals[0]);
        for (int i = 1; i < intervals.length; i++) {
            int[] last = merged.get(merged.size() - 1);
            if (intervals[i][0] <= last[1]) {
                last[1] = Math.max(last[1], intervals[i][1]);
            } else {
                merged.add(intervals[i]);
            }
        }
        return merged.toArray(new int[0][]);
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
    }

    public static void main(String[] args) {
        System.out.println("=== Sort & Search Patterns ===\n");

        // QuickSort
        int[] arr1 = {5, 3, 8, 1, 9, 2, 7};
        quickSort(arr1, 0, arr1.length - 1);
        System.out.println("QuickSort: " + Arrays.toString(arr1));

        // MergeSort
        int[] arr2 = {5, 3, 8, 1, 9, 2, 7};
        mergeSort(arr2, 0, arr2.length - 1);
        System.out.println("MergeSort: " + Arrays.toString(arr2));

        // Binary Search — first occurrence
        int[] sorted = {1, 2, 2, 2, 3, 4, 5};
        System.out.println("First occurrence of 2: index " + firstOccurrence(sorted, 2));

        // Search in Rotated Array
        int[] rotated = {4, 5, 6, 7, 0, 1, 2};
        System.out.println("Search 0 in rotated [4,5,6,7,0,1,2]: index " + searchRotated(rotated, 0));

        // Merge Intervals
        int[][] intervals = {{1,3},{2,6},{8,10},{15,18}};
        int[][] merged = mergeIntervals(intervals);
        System.out.println("Merge intervals: " + Arrays.deepToString(merged));
    }
}
