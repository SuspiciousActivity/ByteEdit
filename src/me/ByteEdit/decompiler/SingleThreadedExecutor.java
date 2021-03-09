package me.ByteEdit.decompiler;

import java.util.concurrent.LinkedBlockingQueue;

public class SingleThreadedExecutor {


    private static LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private static final Thread thread = new Thread(() -> {
        while(true){
            try {
                queue.take().run();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }, "Single");
    static {
        thread.start();
    }


    public static void execute(Runnable r){
        queue.add(r);
    }
}
