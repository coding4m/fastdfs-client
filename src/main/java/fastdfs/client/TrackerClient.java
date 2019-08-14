/**
 *
 */
package fastdfs.client;

import fastdfs.client.codec.*;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;


final class TrackerClient implements Closeable {

    private final FastdfsExecutor executor;
    private final TrackerSelector selector;
    private final TrackerMonitor monitor;

    TrackerClient(FastdfsExecutor executor, FastdfsScheduler scheduler, TrackerSelector selector, List<TrackerServer> servers, int fall, int rise, long checkTimeout, long checkInterval) {
        this.executor = executor;
        this.selector = servers.size() == 1 ? TrackerSelector.FIRST : selector;
        this.monitor = new TrackerMonitor(executor, scheduler, servers, fall, rise, checkTimeout, checkInterval);
    }

    private CompletableFuture<InetSocketAddress> trackerSelect() {
        CompletableFuture<InetSocketAddress> promise = new CompletableFuture<>();
        try {
            promise.complete(monitor.trackerSelect(selector).toInetAddress());
        } catch (Exception e) {
            promise.completeExceptionally(e);
        }
        return promise;
    }

    /**
     * @return
     */
    CompletableFuture<StorageServer> uploadStorageGet() {
        return uploadStorageGet(null);
    }

    /**
     * @param group
     * @return
     */
    CompletableFuture<StorageServer> uploadStorageGet(String group) {
        return trackerSelect().thenCompose(addr -> executor.execute(addr, new UploadStorageGetEncoder(group), StorageServerDecoder.INSTANCE));
    }

    /**
     * @param fileId
     * @return
     */
    CompletableFuture<StorageServer> downloadStorageGet(FileId fileId) {
        CompletableFuture<List<StorageServer>> result = trackerSelect().thenCompose((addr -> executor.execute(addr, new DownloadStorageGetEncoder(fileId), StorageServerListDecoder.INSTANCE)));
        return result.thenApply(FastdfsUtils::first);
    }

    /**
     * 获取更新存储服务器地址
     *
     * @param fileId
     */
    CompletableFuture<StorageServer> updateStorageGet(FileId fileId) {
        CompletableFuture<List<StorageServer>> result = trackerSelect().thenCompose(addr -> executor.execute(addr, new UpdateStorageGetEncoder(fileId), StorageServerListDecoder.INSTANCE));
        return result.thenApply(FastdfsUtils::first);
    }

    /**
     * @param fileId
     * @return
     */
    CompletableFuture<List<StorageServer>> downloadStorageList(FileId fileId) {
        return trackerSelect().thenCompose(addr -> executor.execute(addr, new DownloadStorageListEncoder(fileId), StorageServerListDecoder.INSTANCE));
    }

    @Override
    public void close() throws IOException {
        try {
            monitor.close();
        } catch (Exception e) {
            // do nothing.
        }
    }
}
