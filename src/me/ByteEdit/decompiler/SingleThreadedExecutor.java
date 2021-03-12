package me.ByteEdit.decompiler;

import java.util.concurrent.LinkedBlockingQueue;

public class SingleThreadedExecutor {

	// We don't need to run every Runnable,
	// we only care about the last submitted Runnable.
	private static final Object lock = new Object();
	private static Runnable runnable;
	private static final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

	private static final Thread thread = new Thread(() -> {
		try {
			while (true) {
				Runnable r = null;
				synchronized (lock) {
					while (runnable == null)
						lock.wait();
					r = runnable;
					runnable = null;
				}
				try {
					r.run();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
		}
	}, "SingleThreadedExecutor");

	static {
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	public static void submit(Runnable r) {
		synchronized (lock) {
			runnable = r;
			lock.notifyAll();
		}
	}
}
