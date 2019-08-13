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
    private final TrackerChecker checker;

    TrackerClient(FastdfsExecutor executor, TrackerSelector selector, List<TrackerServer> servers, int fall, int rise, long checkTimeout, long checkInterval) {
        this.executor = executor;
        this.checker = new TrackerChecker(executor, servers, fall, rise, checkTimeout, checkInterval);
        this.selector = servers.size() == 1 ? TrackerSelector.FIRST : selector;
    }

    private InetSocketAddress trackerSelect() {
        return checker.trackerSelect(selector).toInetAddress();
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
        return executor.execute(trackerSelect(), new UploadStorageGetEncoder(group), StorageServerDecoder.INSTANCE);
    }

    /**
     * @param fileId
     * @return
     */
    CompletableFuture<StorageServer> downloadStorageGet(FileId fileId) {
        CompletableFuture<List<StorageServer>> result = executor.execute(trackerSelect(), new DownloadStorageGetEncoder(fileId), StorageServerListDecoder.INSTANCE);
        return result.thenApply(FastdfsUtils::first);
    }

    /**
     * 获取更新存储服务器地址
     *
     * @param fileId
     */
    CompletableFuture<StorageServer> updateStorageGet(FileId fileId) {
        CompletableFuture<List<StorageServer>> result = executor.execute(trackerSelect(), new UpdateStorageGetEncoder(fileId), StorageServerListDecoder.INSTANCE);
        return result.thenApply(FastdfsUtils::first);
    }

    /**
     * @param fileId
     * @return
     */
    CompletableFuture<List<StorageServer>> downloadStorageList(FileId fileId) {
        return executor.execute(trackerSelect(), new DownloadStorageListEncoder(fileId), StorageServerListDecoder.INSTANCE);
    }

    @Override
    public void close() throws IOException {
        try {
            checker.close();
        } catch (Exception e) {
            // do nothing.
        }
    }
}
