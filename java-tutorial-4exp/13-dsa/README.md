# 13 — Data Structures & Algorithms (DSA)

> Interview-ready guide for a 3+ year Java backend engineer targeting mid-senior roles.

---

## 1. Definition

DSA is the study of organizing data efficiently (data structures) and solving problems step-by-step (algorithms). In interviews, it tests problem-solving ability, time/space complexity analysis, and coding fluency.

| Category | Key Structures/Algorithms |
|----------|--------------------------|
| **Arrays & Strings** | Two pointers, sliding window, prefix sum, kadane's |
| **Linked Lists** | Fast/slow pointers, reversal, merge |
| **Stacks & Queues** | Monotonic stack, BFS with queue |
| **Trees** | DFS (pre/in/post), BFS, BST operations |
| **Graphs** | BFS, DFS, topological sort, Dijkstra |
| **Sorting** | QuickSort, MergeSort, counting sort |
| **Searching** | Binary search and its variants |
| **Dynamic Programming** | Memoization, tabulation, common patterns |
| **Hashing** | HashMap patterns, frequency counting |
| **Heap/Priority Queue** | Top-K, merge K sorted, median |

---

## 2. Why This Is Needed

| Interview Context | Why DSA Matters |
|-------------------|-----------------|
| Coding rounds (45-60 min) | Solve 1-2 medium problems under time pressure |
| System design follow-ups | "How would you implement this efficiently?" |
| Backend optimization | Choosing right data structure for caching, routing, indexing |
| Code reviews | Recognizing O(n²) code that should be O(n) |
| Real production | Rate limiters (sliding window), LRU cache (LinkedHashMap), task scheduling (heap) |

---

## 3. How It Works Internally — Big-O Complexity

### Time Complexity Cheat Sheet

| Complexity | Name | Example | 10⁶ inputs |
|-----------|------|---------|------------|
| O(1) | Constant | HashMap get/put | Instant |
| O(log n) | Logarithmic | Binary search | 20 ops |
| O(n) | Linear | Array scan | 1M ops (~1ms) |
| O(n log n) | Linearithmic | MergeSort, TreeMap ops | 20M ops (~20ms) |
| O(n²) | Quadratic | Nested loops, bubble sort | 10¹² ops (TLE!) |
| O(2ⁿ) | Exponential | Subsets, brute-force recursion | Impossible |

### Space Complexity

| Structure | Space |
|-----------|-------|
| Array | O(n) |
| HashMap | O(n) |
| Recursion stack | O(depth) — O(log n) for balanced tree, O(n) worst case |
| DP table | O(n) or O(n×m) |

**Interview rule of thumb:** 10⁸ operations ≈ 1 second in Java. If n = 10⁵, O(n²) = 10¹⁰ → TLE. Need O(n log n) or better.

---

## 4. Real-World Example

### Backend Systems Using DSA

| System | Data Structure / Algorithm |
|--------|---------------------------|
| **LRU Cache** | LinkedHashMap (doubly linked list + HashMap) |
| **Rate Limiter** | Sliding window (deque + timestamp) |
| **Autocomplete** | Trie + DFS |
| **Task Scheduler** | Priority Queue (min-heap) |
| **Service Dependency** | Topological Sort (DAG) |
| **Shortest Path (routing)** | Dijkstra / BFS |
| **Load Balancer (consistent hashing)** | TreeMap (sorted map for ring) |
| **Log Aggregation** | Merge K Sorted Lists (heap) |
| **Duplicate Detection** | Bloom Filter / HashSet |
| **API Throttling** | Token Bucket (queue + timer) |

---

## 5. Common Interview Patterns & Questions

### Pattern 1: Two Pointers

**When:** Sorted array, find pair with target sum, remove duplicates, container with most water.

```java
// Two Sum II (sorted array) — O(n) time, O(1) space
public int[] twoSum(int[] nums, int target) {
    int left = 0, right = nums.length - 1;
    while (left < right) {
        int sum = nums[left] + nums[right];
        if (sum == target) return new int[]{left + 1, right + 1};
        else if (sum < target) left++;
        else right--;
    }
    return new int[]{};
}
```

### Pattern 2: Sliding Window

**When:** Subarray/substring with constraint (max sum, longest without repeating, minimum window).

```java
// Longest substring without repeating characters — O(n)
public int lengthOfLongestSubstring(String s) {
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
```

### Pattern 3: Binary Search

**When:** Sorted data, find boundary, search in rotated array, minimize/maximize answer.

```java
// Find first occurrence of target — O(log n)
public int firstOccurrence(int[] nums, int target) {
    int lo = 0, hi = nums.length - 1, result = -1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        if (nums[mid] == target) { result = mid; hi = mid - 1; } // keep searching left
        else if (nums[mid] < target) lo = mid + 1;
        else hi = mid - 1;
    }
    return result;
}
```

### Pattern 4: DFS/BFS on Trees

```java
// Maximum depth of binary tree — DFS O(n)
public int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
}

// Level order traversal — BFS O(n)
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    while (!queue.isEmpty()) {
        int size = queue.size();
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(level);
    }
    return result;
}
```

### Pattern 5: Graph BFS/DFS

```java
// Number of islands — DFS flood fill O(m×n)
public int numIslands(char[][] grid) {
    int count = 0;
    for (int i = 0; i < grid.length; i++) {
        for (int j = 0; j < grid[0].length; j++) {
            if (grid[i][j] == '1') {
                dfs(grid, i, j);
                count++;
            }
        }
    }
    return count;
}

private void dfs(char[][] grid, int i, int j) {
    if (i < 0 || i >= grid.length || j < 0 || j >= grid[0].length || grid[i][j] != '1') return;
    grid[i][j] = '0'; // mark visited
    dfs(grid, i + 1, j); dfs(grid, i - 1, j);
    dfs(grid, i, j + 1); dfs(grid, i, j - 1);
}
```

### Pattern 6: Dynamic Programming

```java
// Longest Common Subsequence — O(m×n)
public int longestCommonSubsequence(String text1, String text2) {
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

// Coin Change — O(amount × coins.length)
public int coinChange(int[] coins, int amount) {
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
```

### Pattern 7: Heap / Priority Queue

```java
// Top K Frequent Elements — O(n log k)
public int[] topKFrequent(int[] nums, int k) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (int n : nums) freq.merge(n, 1, Integer::sum);

    PriorityQueue<Integer> minHeap = new PriorityQueue<>(
        Comparator.comparingInt(freq::get));

    for (int num : freq.keySet()) {
        minHeap.offer(num);
        if (minHeap.size() > k) minHeap.poll(); // evict least frequent
    }
    return minHeap.stream().mapToInt(i -> i).toArray();
}
```

### Pattern 8: Stack (Monotonic)

```java
// Next Greater Element — O(n)
public int[] nextGreaterElement(int[] nums) {
    int[] result = new int[nums.length];
    Arrays.fill(result, -1);
    Deque<Integer> stack = new ArrayDeque<>(); // stores indices
    for (int i = 0; i < nums.length; i++) {
        while (!stack.isEmpty() && nums[stack.peek()] < nums[i]) {
            result[stack.pop()] = nums[i];
        }
        stack.push(i);
    }
    return result;
}
```

### Pattern 9: Backtracking

```java
// Generate all subsets — O(2^n)
public List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrack(nums, 0, new ArrayList<>(), result);
    return result;
}

private void backtrack(int[] nums, int start, List<Integer> current, List<List<Integer>> result) {
    result.add(new ArrayList<>(current));
    for (int i = start; i < nums.length; i++) {
        current.add(nums[i]);
        backtrack(nums, i + 1, current, result);
        current.remove(current.size() - 1); // undo choice
    }
}
```

### Pattern 10: Topological Sort

```java
// Course Schedule — detect cycle + ordering O(V+E)
public int[] findOrder(int numCourses, int[][] prerequisites) {
    List<List<Integer>> graph = new ArrayList<>();
    int[] inDegree = new int[numCourses];
    for (int i = 0; i < numCourses; i++) graph.add(new ArrayList<>());
    for (int[] pre : prerequisites) {
        graph.get(pre[1]).add(pre[0]);
        inDegree[pre[0]]++;
    }
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < numCourses; i++) if (inDegree[i] == 0) queue.offer(i);

    int[] order = new int[numCourses];
    int idx = 0;
    while (!queue.isEmpty()) {
        int course = queue.poll();
        order[idx++] = course;
        for (int next : graph.get(course)) {
            if (--inDegree[next] == 0) queue.offer(next);
        }
    }
    return idx == numCourses ? order : new int[]{};
}
```

---

## 6. Tricky Edge Cases & Pitfalls

| Pitfall | Example | Fix |
|---------|---------|-----|
| Integer overflow in mid calculation | `(left + right) / 2` overflows for large values | `left + (right - left) / 2` |
| Off-by-one in binary search | Infinite loop with `lo < hi` vs `lo <= hi` | Match loop condition with update logic |
| Modifying collection during iteration | ConcurrentModificationException in BFS | Use index-based loop or copy |
| Forgetting base case in recursion | StackOverflowError | Always handle null/empty/single element |
| Not handling negative numbers | Kadane's algorithm with all negatives | Initialize max to `nums[0]` not `0` |
| HashMap with mutable keys | Lost entries after key mutation | Use immutable keys (String, Integer) |
| Shallow copy in backtracking | All results point to same list | `new ArrayList<>(current)` when adding to result |
| Graph cycles in DFS | Infinite loop | Track visited set (or 3-state: unvisited/in-progress/done) |
| Empty input | NPE on `nums.length` | Check null/empty at start |
| Duplicate elements | Wrong count in two-sum | Use index tracking or sort + skip duplicates |

---

## 7. Comparison — When to Use What

### Data Structure Selection

| Need | Best Choice | Why |
|------|-------------|-----|
| Fast lookup by key | HashMap O(1) | Hash-based |
| Sorted order + range queries | TreeMap O(log n) | Red-Black tree |
| FIFO processing | Queue (ArrayDeque) | O(1) enqueue/dequeue |
| LIFO / undo | Stack (ArrayDeque) | O(1) push/pop |
| Priority-based processing | PriorityQueue (heap) | O(log n) insert/extract-min |
| Unique elements | HashSet O(1) | Hash-based |
| Sorted unique elements | TreeSet O(log n) | Red-Black tree |
| Fast prefix lookup | Trie O(L) | Character-by-character |
| Disjoint sets / union-find | UnionFind O(α(n)) ≈ O(1) | Path compression + rank |

### Algorithm Selection by Problem Type

| Problem Pattern | Algorithm | Time |
|----------------|-----------|------|
| "Find pair/triplet with sum" | Two pointers (sorted) or HashMap | O(n) / O(n²) |
| "Longest/shortest subarray with X" | Sliding window | O(n) |
| "Find in sorted array" | Binary search | O(log n) |
| "All paths / permutations / combinations" | Backtracking | O(2ⁿ) / O(n!) |
| "Shortest path unweighted" | BFS | O(V+E) |
| "Shortest path weighted" | Dijkstra | O((V+E) log V) |
| "Detect cycle in graph" | DFS with coloring | O(V+E) |
| "Ordering with dependencies" | Topological sort (Kahn's BFS) | O(V+E) |
| "Optimal substructure + overlapping subproblems" | Dynamic programming | Varies |
| "Top K / Kth largest" | Heap (PriorityQueue) | O(n log k) |
| "Connected components" | Union-Find or DFS | O(V+E) |
| "Merge K sorted" | Min-heap | O(N log k) |

### Sorting Algorithm Comparison

| Algorithm | Best | Average | Worst | Space | Stable | Use When |
|-----------|------|---------|-------|-------|--------|----------|
| QuickSort | O(n log n) | O(n log n) | O(n²) | O(log n) | No | General purpose (Arrays.sort for primitives) |
| MergeSort | O(n log n) | O(n log n) | O(n log n) | O(n) | Yes | Linked lists, external sort, stability needed |
| HeapSort | O(n log n) | O(n log n) | O(n log n) | O(1) | No | Memory-constrained |
| TimSort | O(n) | O(n log n) | O(n log n) | O(n) | Yes | Java's Arrays.sort for objects (hybrid) |
| Counting Sort | O(n+k) | O(n+k) | O(n+k) | O(k) | Yes | Small range integers |

---

## 8. Performance Impact

### Java-Specific Performance Tips for Interviews

| Tip | Why | Example |
|-----|-----|---------|
| Use `int[]` over `Integer[]` | No autoboxing, cache-friendly | DP tables, frequency arrays |
| `StringBuilder` for string building | String concat creates new objects | Building result strings in loops |
| `ArrayDeque` over `LinkedList` for stack/queue | Better cache locality, less GC | BFS queue, DFS stack |
| Pre-size collections | Avoid resizing/rehashing | `new HashMap<>(expectedSize)` |
| Bit manipulation for sets | O(1) set operations on small domains | `visited |= (1 << node)` for ≤32 nodes |
| `char[]` over `String` for mutation | Strings are immutable | Anagram checks, in-place reversal |

### Complexity Limits by Input Size

| n | Max Acceptable Complexity | Typical Problem |
|---|--------------------------|-----------------|
| n ≤ 10 | O(n!) or O(2ⁿ) | Permutations, brute force |
| n ≤ 20 | O(2ⁿ) | Bitmask DP, subsets |
| n ≤ 500 | O(n³) | Floyd-Warshall, 3D DP |
| n ≤ 5000 | O(n²) | 2D DP, nested loops |
| n ≤ 10⁶ | O(n log n) | Sorting, binary search on answer |
| n ≤ 10⁸ | O(n) | Linear scan, two pointers |

---

## 9. Trade-offs

| Approach | Pros | Cons | When |
|----------|------|------|------|
| HashMap (extra space) | O(1) lookup, simple | O(n) space | Two-sum, frequency counting |
| Sorting first | Enables two pointers, binary search | O(n log n) time, modifies input | When order doesn't matter |
| Recursion + memo | Clean code, top-down thinking | Stack overflow risk, overhead | DP problems, tree traversals |
| Iterative DP | No stack overflow, often faster | Harder to derive, less intuitive | When n is large |
| BFS | Guarantees shortest path (unweighted) | O(V+E) space for queue | Shortest path, level-order |
| DFS | Less memory (stack vs queue) | Doesn't guarantee shortest | Connectivity, cycle detection |
| Union-Find | Near O(1) per operation | Complex implementation | Dynamic connectivity |
| Trie | O(L) prefix operations | High memory usage | Autocomplete, word search |

---

## 10. 30–60 Second Interview Answers

### "How do you approach a coding problem?"

> "I follow a structured approach: First, I clarify constraints — input size tells me the acceptable complexity. Then I identify the pattern — is it sliding window, two pointers, BFS, DP? I talk through my approach before coding, handle edge cases (empty input, single element, duplicates), code the solution, then verify with a test case. If stuck, I start with brute force and optimize."

### "Explain time complexity analysis"

> "Big-O describes how runtime grows with input size. O(n) means linear — doubling input doubles time. O(n²) means quadratic — doubling input quadruples time. In interviews, I target the best possible complexity: if input is sorted, binary search gives O(log n). If I need to find pairs, HashMap gives O(n) vs brute-force O(n²). The key insight is: 10⁸ operations per second in Java, so for n=10⁵, I need O(n log n) or better."

### "HashMap vs TreeMap — when to use which?"

> "HashMap for O(1) average lookup when I don't need ordering — frequency counting, two-sum, caching. TreeMap for O(log n) when I need sorted keys or range queries — finding the closest value, interval problems, implementing a sorted sliding window. In interviews, HashMap is the default; TreeMap only when the problem requires ordering."

---

## 11. Real Production Scenario

### Scenario: API Rate Limiter Using Sliding Window (Ericsson NEF)

**Problem:** 5G API gateway needs to limit each subscriber to 100 requests per minute with a sliding window (not fixed window which has boundary burst issues).

**DSA Solution:** Sliding window with a sorted structure.

```java
public class SlidingWindowRateLimiter {
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequests, long windowMillis) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
    }

    public boolean allowRequest(String clientId) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestLog.computeIfAbsent(clientId, k -> new ConcurrentLinkedDeque<>());

        // Remove expired timestamps — O(expired count)
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
            timestamps.pollFirst();
        }

        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true; // allowed
        }
        return false; // rate limited (HTTP 429)
    }
}
```

**Complexity:** O(1) amortized per request (each timestamp is added once, removed once).
**Production enhancement:** Use Redis sorted sets (`ZRANGEBYSCORE`) for distributed rate limiting across pods.

---

## 12. If This Fails, How to Debug

### Common Interview Debugging

| Symptom | Likely Cause | Fix |
|---------|-------------|-----|
| Time Limit Exceeded (TLE) | Wrong complexity (O(n²) when O(n) needed) | Identify pattern, use HashMap/sorting/binary search |
| Wrong Answer on edge cases | Off-by-one, empty input, single element | Add explicit edge case handling at start |
| Stack Overflow | Recursion too deep (n > 10⁴) | Convert to iterative with explicit stack |
| Memory Limit Exceeded | Storing too much state | Optimize DP space (rolling array), use bit manipulation |
| Infinite loop | Binary search bounds wrong | Verify `lo <= hi` with `lo = mid + 1` / `hi = mid - 1` |
| Wrong answer with duplicates | Not handling duplicate elements | Sort + skip duplicates, or use Set |

### Problem-Solving Framework (When Stuck)

```
1. Can I solve it with brute force? → Code it, then optimize
2. Can I sort the input? → Enables binary search, two pointers
3. Can I use a HashMap? → O(1) lookup often reduces O(n²) to O(n)
4. Is there overlapping subproblems? → DP (memo or tabulation)
5. Is it a graph? → Model as adjacency list, BFS/DFS
6. Can I binary search on the answer? → "Minimum X such that condition holds"
7. Can I use a monotonic stack/queue? → Next greater/smaller element patterns
```

---

## Follow-Up Interview Questions

**Q1:** "Design an in-memory autocomplete system that returns top-5 suggestions as the user types. How would you handle millions of search terms?"

**Answer:**

**Data Structure:** Trie + Max-Heap per node.

```java
class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    List<String> topSuggestions = new ArrayList<>(); // pre-computed top-5
}
```

**Approach:**
1. Build Trie from all search terms with frequency counts
2. At each node, pre-compute and store top-5 suggestions (by frequency) for the subtree
3. On query: traverse Trie character by character → return `topSuggestions` at current node

**Complexity:** O(L) per keystroke where L = prefix length. Pre-computation is O(N × L × log 5) at build time.

**Scaling for millions of terms:**
- Shard Trie by first 2 characters (676 shards for a-z)
- Use Redis sorted sets for distributed top-K
- Background job rebuilds Trie periodically from search logs
- Bloom filter to quickly reject prefixes with no matches

---

**Q2:** "You have a stream of events arriving in real-time. Find the median at any point. How would you handle this efficiently?"

**Answer:**

**Data Structure:** Two heaps — max-heap for lower half, min-heap for upper half.

```java
class MedianFinder {
    PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder()); // lower half
    PriorityQueue<Integer> minHeap = new PriorityQueue<>(); // upper half

    public void addNum(int num) {
        maxHeap.offer(num);
        minHeap.offer(maxHeap.poll()); // balance: move max of lower to upper
        if (minHeap.size() > maxHeap.size()) {
            maxHeap.offer(minHeap.poll()); // keep maxHeap same size or +1
        }
    }

    public double findMedian() {
        if (maxHeap.size() > minHeap.size()) return maxHeap.peek();
        return (maxHeap.peek() + minHeap.peek()) / 2.0;
    }
}
```

**Complexity:** O(log n) per insert, O(1) for median query.

**Production use case:** Real-time latency monitoring — find p50 (median) of API response times in a sliding window. Combine with a time-based eviction (remove events older than 5 minutes).

---

## Practice Task

Implement these 5 classic problems (all in the code demos):

1. **Two Sum** — HashMap approach O(n)
2. **Merge Intervals** — Sort + linear scan O(n log n)
3. **LRU Cache** — HashMap + Doubly Linked List O(1)
4. **Binary Tree Level Order** — BFS with queue O(n)
5. **Coin Change** — Bottom-up DP O(amount × coins)

→ See code in `core-java-examples/src/main/java/com/interview/dsa/`

---

## LeetCode Patterns — Quick Reference

| Pattern | Key Problems | Technique |
|---------|-------------|-----------|
| **Two Pointers** | Two Sum II, 3Sum, Container With Most Water, Trapping Rain Water | Sort + converge from both ends |
| **Sliding Window** | Longest Substring Without Repeating, Minimum Window Substring, Max Subarray Sum of Size K | Expand right, shrink left when constraint violated |
| **Binary Search** | Search in Rotated Array, Find Peak, Koko Eating Bananas | Search on answer space |
| **BFS** | Shortest Path, Word Ladder, Rotten Oranges, Level Order | Queue + visited set |
| **DFS** | Number of Islands, Path Sum, Clone Graph | Recursion or explicit stack |
| **Topological Sort** | Course Schedule, Alien Dictionary | Kahn's (BFS) or DFS post-order |
| **DP** | Climbing Stairs, Coin Change, LCS, Knapsack, House Robber | State definition + transition |
| **Heap** | Top K Frequent, Merge K Sorted, Find Median | Min/max heap for streaming |
| **Monotonic Stack** | Next Greater Element, Largest Rectangle in Histogram, Daily Temperatures | Maintain decreasing/increasing stack |
| **Union-Find** | Number of Connected Components, Redundant Connection | Path compression + union by rank |
| **Backtracking** | Subsets, Permutations, N-Queens, Word Search | Choose → Explore → Unchoose |
| **Prefix Sum** | Subarray Sum Equals K, Range Sum Query | Cumulative sum + HashMap |

---

## Code Examples

All code is in `core-java-examples/src/main/java/com/interview/dsa/`:

| File | Topics |
|------|--------|
| [ArrayStringPatterns.java](../core-java-examples/src/main/java/com/interview/dsa/ArrayStringPatterns.java) | Two Sum, Sliding Window, Kadane's, Two Pointers |
| [TreeGraphPatterns.java](../core-java-examples/src/main/java/com/interview/dsa/TreeGraphPatterns.java) | BFS, DFS, Level Order, Number of Islands, Topological Sort |
| [DPPatterns.java](../core-java-examples/src/main/java/com/interview/dsa/DPPatterns.java) | Coin Change, LCS, Climbing Stairs, House Robber |
| [HeapStackPatterns.java](../core-java-examples/src/main/java/com/interview/dsa/HeapStackPatterns.java) | Top K Frequent, Median Finder, Next Greater Element, Valid Parentheses |
| [SortSearchPatterns.java](../core-java-examples/src/main/java/com/interview/dsa/SortSearchPatterns.java) | QuickSort, MergeSort, Binary Search variants, Merge Intervals |
