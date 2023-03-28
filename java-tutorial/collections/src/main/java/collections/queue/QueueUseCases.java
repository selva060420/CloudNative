package collections.queue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class QueueUseCases {

	public static void main(String[] args) throws InterruptedException {
		Queue<String> linkedQueue=new LinkedList<String>();
		Queue<String> priorityQueue=new PriorityQueue<String>(1);
		Deque<String> deqeue=new ArrayDeque<String>();
		
		//synchronized
		BlockingQueue<String> syncQueue =new ArrayBlockingQueue<String>(5);
//		BlockingQueue<String> syncQueue1 =new LinkedBlockingQueue<String>();
//		BlockingQueue<String> syncQueue2 =new PriorityBlockingQueue<String>(5);
//		BlockingDeque<String> syncQueue3=new LinkedBlockingDeque<String>(5);
//		linkedQueue.offer("selva");
//		linkedQueue.offer("kumar");
//		linkedQueue.offer("kishore");
//		System.out.println("Queue: "+linkedQueue);
//		linkedQueue.poll();
//		System.out.println("Queue: "+linkedQueue);
//		System.out.println("First Element: "+linkedQueue.peek());
		
//		System.out.println("queue start");
//		syncQueue.put("kumar");
//		syncQueue.put("kishore");
//		syncQueue.put("kishore");
//		syncQueue.take();
//		System.out.println(syncQueue);
		
		deqeue.offerFirst("selva");
		deqeue.offer("kishore");
		deqeue.offerLast("kumar");
		deqeue.pollFirst();
		deqeue.pollLast();
		System.out.println(deqeue.getLast());
		System.out.println(deqeue.getFirst());
		System.out.println(deqeue);
	}

}
