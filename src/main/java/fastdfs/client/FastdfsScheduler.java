package fastdfs.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class FastdfsScheduler implements Closeable {
    private final ScheduledExecutorService loop;

    FastdfsScheduler(int threads) {
        this.loop = Executors.newScheduledThreadPool(threads, new ThreadFactory() {
            final String threadPrefix = "fastdfs-scheduler-";
            final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(null, r, threadPrefix + threadNumber.getAndIncrement());
            }
        });
    }

    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return loop.schedule(command, delay, unit);
    }

    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return loop.schedule(callable, delay, unit);
    }

    ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return loop.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return loop.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public void close() throws IOException {
        try {
            loop.shutdown();
        } catch (Exception e) {
            // do nothing.
        }
    }
}
