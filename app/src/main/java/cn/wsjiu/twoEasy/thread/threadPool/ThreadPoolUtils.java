package cn.wsjiu.twoEasy.thread.threadPool;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 全局线程池对象
 */
public class ThreadPoolUtils {
    private static ThreadPoolExecutor tp;
    private static synchronized void init() {
        if(tp == null) {
            int coreSize = 10;
            int maxSize = 40;
            int keepAliveTime = 3;
            int queueSize = 10;
            TimeUnit unit = TimeUnit.MINUTES;
            BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueSize, true);
            tp = new ThreadPoolExecutor(coreSize, maxSize, keepAliveTime, unit, workQueue);
        }
    }

    public static ThreadPoolExecutor get() {
        if(tp == null) {
            init();
        }
        return tp;
    }

    public static void asynExecute(Runnable runnable) {
        if(tp == null) {
            init();
        }
        tp.execute(runnable);
    }

    public static void asynExecute(Callable callable) {

    }

    public static void synExecute(Runnable runnable) {
        if(tp == null) {
            init();
        }
        try {
            Future future = tp.submit(runnable);
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
