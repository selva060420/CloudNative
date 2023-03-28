package collections.set;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class SetUsesCases {
	public static void main(String[] args) {
		Set<Integer> hashSet=new HashSet<Integer>();
//		Set<Integer> linkedHashSet=new LinkedHashSet<Integer>();
		Set<Integer> treeSet=new TreeSet<Integer>();
		
//		synchronized
//		Set<Integer> syncSet=Collections.synchronizedSet(<sampleList>);
		
		treeSet.add(5);
		treeSet.add(2);
		treeSet.add(1);
		hashSet.add(1);
		hashSet.add(2);
		System.out.println("initial treeSet: "+treeSet);
		System.out.println("initial hashSet: "+hashSet);
//		treeSet.remove(3);
//		treeSet.remove(5);
//		hashSet.remove(3);
//		hashSet.remove(5);
//		System.out.println("equals :"+treeSet.equals(hashSet));
//		System.out.println("Set: "+treeSet);
		
//		//union
//		System.out.println("union: "+treeSet.addAll(hashSet));
//		System.out.println("after union Set: "+treeSet);
//		
//		//intersection
//		System.out.println("intersection: "+ treeSet.retainAll(hashSet));
//		System.out.println("after intersection Set: "+treeSet);
		
//		//difference
//		System.out.println("difference: "+ treeSet.removeAll(hashSet));
//		System.out.println("after difference Set: "+treeSet);
		
//		`subset
		System.out.println("hashSet is subset of treeSet: "+treeSet.containsAll(hashSet));
	}
}
