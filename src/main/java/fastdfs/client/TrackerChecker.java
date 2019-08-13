package fastdfs.client;

import fastdfs.client.codec.ActiveTestReplier;
import fastdfs.client.codec.ActiveTestRequestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

class TrackerChecker implements Closeable {
    private final int fall;
    private final int rise;
    private final List<TrackerServer> aliveServers;
    private final List<ScheduledFuture<?>> aliveTasks;
    private final Map<TrackerServer, Integer> ariseServers = new HashMap<>();
    private final Map<TrackerServer, Integer> alivenessServers = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    TrackerChecker(FastdfsExecutor executor, List<TrackerServer> servers, int fall, int rise, long checkTimeout, long checkInterval) {
        this.fall = fall;
        this.rise = rise;

        this.aliveServers = new ArrayList<>(servers);
        this.aliveTasks = servers.stream().map(server -> executor.scheduleAtFixedRate(() -> {
            try {
                CompletableFuture<Boolean> promise = executor.execute(server.toInetAddress(), new ActiveTestRequestor(), new ActiveTestReplier());
                if (promise.get(checkTimeout, TimeUnit.MILLISECONDS)) {
                    trackerReachable(server);
                    return;
                }
                trackerUnreachable(server, null);
            } catch (Exception e) {
                trackerUnreachable(server, e);
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
        logger.info("TrackerServer[host={}, port={}] up.", server.host(), server.port());
        ariseServers.remove(server);
        alivenessServers.remove(server);
        aliveServers.remove(server);
        aliveServers.add(server);
    }

    private void trackerUnreachable(TrackerServer server, Throwable e) {
        try {
            lock.writeLock().lock();
            if (!aliveServers.contains(server)) {
                return;
            }

            Integer alivenessTimes = alivenessServers.getOrDefault(server, 0);
            if (alivenessTimes >= fall - 1) {
                trackerDown(server);
                return;
            }
            alivenessServers.put(server, alivenessTimes + 1);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void trackerDown(TrackerServer server) {
        logger.warn("TrackerServer[host={}, port={}] down.", server.host(), server.port());
        aliveServers.remove(server);
        ariseServers.remove(server);
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
