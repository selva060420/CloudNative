package multipleThreading.countDownLatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class countDownLatch {
	private static final CountDownLatch latch = new CountDownLatch(3);

	public static void main(String[] args) {
		LatchAwait runnable1 = new LatchAwait(latch);
		LatchCount runnable2 = new LatchCount(latch);
		ExecutorService service = Executors.newFixedThreadPool(4);
		service.submit(runnable1);
		for(int i=0;i<3;i++) {
			service.submit(runnable2);
		}
		service.shutdown();
	}
}
