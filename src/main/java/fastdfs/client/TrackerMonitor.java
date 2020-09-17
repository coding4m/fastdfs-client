package fastdfs.client;

import fastdfs.client.codec.ActiveTestReplier;
import fastdfs.client.codec.ActiveTestRequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

class TrackerMonitor implements Closeable {

    private final int fall;
    private final int rise;
    private final List<TrackerServer> aliveServers;
    private final List<ScheduledFuture<?>> aliveTasks;

    private final Map<TrackerServer, Integer> ariseServers = new HashMap<>();
    private final Map<TrackerServer, Integer> alivenessServers = new HashMap<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    TrackerMonitor(FastdfsExecutor executor, FastdfsScheduler scheduler, List<TrackerServer> servers, int fall, int rise, long checkTimeout, long checkInterval) {
        this.fall = fall;
        this.rise = rise;

        this.aliveServers = new LinkedList<>(servers);
        this.aliveTasks = new HashSet<>(servers).stream().map(server -> scheduler.scheduleAtFixedRate(() -> {
            CompletableFuture<Boolean> promise = null;
            try {
                promise = executor.execute(server.toInetAddress(), new ActiveTestRequestor(), new ActiveTestReplier());
                if (promise.get(checkTimeout, TimeUnit.MILLISECONDS)) {
                    trackerReachable(server);
                    return;
                }
                trackerUnreachable(server, null);
            } catch (ExecutionException e) {
                trackerUnreachable(server, e.getCause());
            } catch (Exception e) {
                trackerUnreachable(server, e);
            } finally {
                if (null != promise) {
                    promise.cancel(true);
                }
            }
        }, checkInterval, checkInterval, TimeUnit.MILLISECONDS)).collect(Collectors.toList());
    }

    TrackerServer trackerSelect(TrackerSelector selector) {
        try {
            lock.readLock().lock();
            if (aliveServers.isEmpty()) {
                throw new TrackerNotAvailableException();
            }

            return selector.select(aliveServers);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void trackerReachable(TrackerServer server) {
        try {
            lock.writeLock().lock();
            if (aliveServers.contains(server)) {
                ariseServers.remove(server);
                return;
            }

            Integer ariseTimes = ariseServers.getOrDefault(server, 0);
            if (ariseTimes >= rise - 1) {
                trackerUp(server);
                return;
            }
            ariseServers.put(server, ariseTimes + 1);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void trackerUp(TrackerServer server) {
        if (logger.isInfoEnabled()) {
            logger.info("Server[host={}, port={}] Up.", server.host(), server.port());
        }

        ariseServers.remove(server);
        alivenessServers.remove(server);

        aliveServers.remove(server);
        aliveServers.add(server);
    }

    private void trackerUnreachable(TrackerServer server, Throwable e) {
        try {
            lock.writeLock().lock();
            if (!aliveServers.contains(server)) {
                ariseServers.remove(server);
                return;
            }

            Integer alivenessTimes = alivenessServers.getOrDefault(server, 0);
            if (alivenessTimes >= fall - 1) {
                trackerDown(server, e);
                return;
            }
            alivenessServers.put(server, alivenessTimes + 1);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void trackerDown(TrackerServer server, Throwable e) {
        if (logger.isInfoEnabled()) {
            logger.info("Server[host={}, port={}] Down. Cause by: {}.", server.host(), server.port(), e == null ? "" : e.getMessage());
        }
        aliveServers.remove(server);
        ariseServers.remove(server);
        alivenessServers.remove(server);
    }

    @Override
    public void close() throws IOException {
        aliveTasks.forEach(task -> {
            try {
                task.cancel(true);
            } catch (Exception e) {
                // do nothing.
            }
        });
    }
}
