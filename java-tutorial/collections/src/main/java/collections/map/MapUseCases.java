package collections.map;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class MapUseCases
{

    public static void main(String[] args)
    {
        Map<Integer, String> hashMap = new HashMap<Integer, String>();
        Map<Integer, String> linkedMap = new LinkedHashMap<Integer, String>();
        Map<Integer, String> treeMap = new TreeMap<Integer, String>();
//         synchronized maps
		Map<Integer,String> syncMap=new ConcurrentHashMap<Integer, String>();
		Map<Integer,String> syncMap1=Collections.synchronizedMap(<nonSyncMap>);

        hashMap.put(1, "one");
        hashMap.put(2, "two");
        hashMap.put(3, "three");
        hashMap.remove(1);
        System.out.println("get index: " + hashMap.get(2));
        System.out.println("get keys: " + hashMap.keySet());
        System.out.println("get values: " + hashMap.values());
        System.out.println("contains key or not: " + hashMap.containsKey(2));
        System.out.println("contains value or not: " + hashMap.containsValue("three"));
        for (Entry<Integer, String> entry : hashMap.entrySet())
        {
            System.out.println("key: " + entry.getKey() + " value: " + entry.getValue());
        }
        System.out.println("entryset: " + hashMap.entrySet().toString());
    }

}
