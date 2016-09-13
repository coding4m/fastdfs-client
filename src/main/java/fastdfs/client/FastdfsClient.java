/**
 *
 */
package fastdfs.client;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author siuming
 */
public class FastdfsClient implements Closeable {

    private final FastdfsExecutor executor;
    private final TrackerClient trackerClient;
    private final StorageClient storageClient;

    private FastdfsClient(Builder builder) {

        FastdfsSettings settings = new FastdfsSettings(
                builder.connectTimeout,
                builder.maxThreads,
                builder.maxConnPerHost,
                builder.maxIdleSeconds
        );

        this.executor = new FastdfsExecutor(settings);
        this.trackerClient = new TrackerClient(executor, builder.selector, builder.trackers);
        this.storageClient = new StorageClient(executor);
    }

    /**
     * @param file
     * @return
     */
    public CompletableFuture<FileId> upload(File file) {
        return upload(null, file);
    }


    /**
     * @param group
     * @param file
     * @return
     */
    public CompletableFuture<FileId> upload(String group, File file) {
        Objects.requireNonNull(file, "file must not be null.");
        return trackerClient
                .uploadStorageGet(group)
                .thenCompose(server -> storageClient.upload(server, file));
    }

    /**
     * @param filename
     * @param content
     * @return
     */
    public CompletableFuture<FileId> upload(String filename, byte[] content) {
        return upload(null, filename, content);
    }

    /**
     * @param filename
     * @param content
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(String filename, byte[] content, FileMetadata metadata) {
        return upload(null, filename, content, metadata);
    }

    /**
     * @param group
     * @param filename
     * @param content
     * @return
     */
    public CompletableFuture<FileId> upload(String group, String filename, byte[] content) {
        return upload(group, content, filename, content.length);
    }

    /**
     * @param group
     * @param filename
     * @param content
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(String group, String filename, byte[] content, FileMetadata metadata) {
        return upload(group, content, filename, content.length, metadata);
    }


    /**
     * @param file
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(File file, FileMetadata metadata) {
        return upload(null, file, metadata);
    }

    /**
     * @param group
     * @param file
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(String group, File file, FileMetadata metadata) {
        Objects.requireNonNull(file, "file must not be null.");
        Objects.requireNonNull(metadata, "metadata must not be null.");
        return upload(group, file).thenApply(fileId -> {
            metadataSet(fileId, metadata);
            return fileId;
        });
    }

    /**
     * @param content
     * @param filename
     * @param size
     * @return
     */
    public CompletableFuture<FileId> upload(Object content, String filename, long size) {
        return upload(null, content, filename, size);
    }

    /**
     * 上传文件，其中文件内容字段 content 的支持以下类型：
     * <p>
     * <ul>
     * <li><code>byte[]</code></li>
     * <li>{@link java.io.File}</li>
     * <li>{@link java.io.InputStream}</li>
     * <li>{@link java.nio.channels.ReadableByteChannel}</li>
     * </ul>
     *
     * @param group    分组
     * @param content  上传内容
     * @param size     内容长度
     * @param filename 扩展名
     * @return
     */
    public CompletableFuture<FileId> upload(String group, Object content, String filename, long size) {
        Objects.requireNonNull(content, "content must not be null.");
        Objects.requireNonNull(filename, "filename must not be null.");
        return trackerClient
                .uploadStorageGet(group)
                .thenCompose(server -> storageClient.upload(server, content, filename, size));
    }

    /**
     * @param content
     * @param filename
     * @param size
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(Object content, String filename, long size, FileMetadata metadata) {
        return upload(null, content, filename, size, metadata);
    }

    /**
     * @param group
     * @param content
     * @param filename
     * @param size
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> upload(String group, Object content, String filename, long size, FileMetadata metadata) {
        Objects.requireNonNull(content, "content must not be null.");
        Objects.requireNonNull(filename, "filename must not be null.");
        Objects.requireNonNull(metadata, "metadata must not be null.");
        return upload(group, content, filename, size).thenApply(fileId -> {
            metadataSet(fileId, metadata);
            return fileId;
        });
    }

    /**
     * @param file
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(File file) {
        return uploadAppender(null, file);
    }

    /**
     * @param file
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(File file, FileMetadata metadata) {
        return uploadAppender(null, file, metadata);
    }

    /**
     * @param group
     * @param file
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, File file) {
        Objects.requireNonNull(file, "file must not be null.");
        return trackerClient
                .uploadStorageGet(group)
                .thenCompose(server -> storageClient.uploadAppender(server, file));
    }

    /**
     * @param group
     * @param file
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, File file, FileMetadata metadata) {
        return uploadAppender(group, file)
                .thenApply(fileId -> {
                    metadataSet(fileId, metadata);
                    return fileId;
                });
    }

    /**
     * @param filename
     * @param content
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String filename, byte[] content) {
        return uploadAppender(null, content, filename, content.length);
    }

    /**
     * @param group
     * @param filename
     * @param content
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, String filename, byte[] content) {
        return uploadAppender(group, content, filename, content.length);
    }

    /**
     * @param group
     * @param filename
     * @param content
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, String filename, byte[] content, FileMetadata metadata) {
        return uploadAppender(group, content, filename, content.length, metadata);
    }

    /**
     * @param content
     * @param filename
     * @param size
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(Object content, String filename, long size) {
        return uploadAppender(null, content, filename, size);
    }


    /**
     * @param content
     * @param filename
     * @param size
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(Object content, String filename, long size, FileMetadata metadata) {
        return uploadAppender(null, content, filename, size, metadata);
    }

    /**
     * 上传可追加文件，其中文件内容字段 content 的支持以下类型：
     * <p>
     * <ul>
     * <li><code>byte[]</code></li>
     * <li>{@link java.io.File}</li>
     * <li>{@link java.io.InputStream}</li>
     * <li>{@link java.nio.channels.ReadableByteChannel}</li>
     * </ul>
     *
     * @param group    分组
     * @param content  上传内容
     * @param filename 文件名
     * @param size     内容长度
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, Object content, String filename, long size) {
        Objects.requireNonNull(content, "content must not be null.");
        Objects.requireNonNull(filename, "filename must not be null.");
        return trackerClient
                .uploadStorageGet(group)
                .thenCompose(server -> storageClient.uploadAppender(server, content, filename, size));
    }

    /**
     * @param group
     * @param content
     * @param filename
     * @param size
     * @param metadata
     * @return
     */
    public CompletableFuture<FileId> uploadAppender(String group, Object content, String filename, long size, FileMetadata metadata) {
        return uploadAppender(group, content, filename, size)
                .thenApply(fileId -> {
                    metadataSet(fileId, metadata);
                    return fileId;
                });
    }

    /**
     * 下载文件，其输出 output 参数支持以下类型
     * <p>
     * <ul>
     * <li>{@link java.io.OutputStream}</li>
     * <li>{@link java.nio.channels.GatheringByteChannel}</li>
     * </ul>
     *
     * @param fileId 服务器存储路径
     * @param out    输出流
     * @return
     */
    public CompletableFuture<Void> download(FileId fileId, Object out) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(out, "out must not be null.");
        return trackerClient
                .downloadStorageGet(fileId)
                .thenCompose(server -> storageClient.download(server, fileId, out));
    }

    /**
     * @param fileId
     * @param out
     * @param offset
     * @param size
     * @return
     */
    public CompletableFuture<Void> download(FileId fileId, Object out, int offset, int size) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(out, "out must not be null.");
        return trackerClient
                .downloadStorageGet(fileId)
                .thenCompose(server -> storageClient.download(server, fileId, out, offset, size));
    }

    /**
     * @param fileId
     * @return
     */
    public CompletableFuture<Void> delete(FileId fileId) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.delete(server, fileId));
    }

    /**
     * 追加文件
     *
     * @param fileId 服务器存储路径
     * @param file   内容
     * @return
     */
    public CompletableFuture<Void> append(FileId fileId, File file) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(file, "file must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.append(server, fileId, file));
    }

    /**
     * 追加文件
     *
     * @param fileId 服务器存储路径
     * @param bytes  内容
     * @return
     */
    public CompletableFuture<Void> append(FileId fileId, byte[] bytes) {
        return append(fileId, bytes, bytes.length);
    }

    /**
     * @param fileId
     * @param content
     * @param size
     * @return
     */
    public CompletableFuture<Void> append(FileId fileId, Object content, long size) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(content, "content must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.append(server, fileId, content, size));
    }

    /**
     * @param fileId
     * @param file
     * @param offset
     * @return
     */
    public CompletableFuture<Void> modify(FileId fileId, File file, int offset) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(file, "file must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.modify(server, fileId, file, offset));
    }

    /**
     * @param fileId
     * @param bytes
     * @param offset
     * @return
     */
    public CompletableFuture<Void> modify(FileId fileId, byte[] bytes, int offset) {
        return modify(fileId, bytes, bytes.length, offset);
    }

    /**
     * @param fileId
     * @param content
     * @param size
     * @param offset
     * @return
     */
    public CompletableFuture<Void> modify(FileId fileId, Object content, long size, int offset) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(content, "content must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.modify(server, fileId, content, size, offset));
    }

    /**
     * 截取文件
     *
     * @param fileId 服务器存储路径
     * @return
     */
    public CompletableFuture<Void> truncate(FileId fileId) {
        return truncate(fileId, 0);
    }

    /**
     * 截取文件
     *
     * @param fileId        服务器存储路径
     * @param truncatedSize 截取字节数
     * @return
     */
    public CompletableFuture<Void> truncate(FileId fileId, int truncatedSize) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.truncate(server, fileId, truncatedSize));
    }

    /**
     * 设置文件元数据
     *
     * @param fileId   服务器存储路径
     * @param metadata 元数据
     */
    public CompletableFuture<Void> metadataSet(FileId fileId, FileMetadata metadata) {
        return metadataSet(fileId, metadata, FastdfsConstants.METADATA_OVERWRITE);
    }

    /**
     * 设置文件元数据
     *
     * @param fileId   服务器存储路径
     * @param metadata 元数据
     * @param flag     设置标识
     */
    public CompletableFuture<Void> metadataSet(FileId fileId, FileMetadata metadata, byte flag) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        Objects.requireNonNull(metadata, "metadata must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.setMetadata(server, fileId, metadata, flag));
    }

    /**
     * 获取文件元数据
     *
     * @param fileId
     * @return
     */
    public CompletableFuture<FileMetadata> metadataGet(FileId fileId) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.getMetadata(server, fileId));
    }

    /**
     * 获取文件信息
     *
     * @param fileId
     * @return
     */
    public CompletableFuture<FileInfo> infoGet(FileId fileId) {
        Objects.requireNonNull(fileId, "fileId must not be null.");
        return trackerClient
                .updateStorageGet(fileId)
                .thenCompose(server -> storageClient.getInfo(server, fileId));
    }

    @Override
    public void close() throws IOException {
        executor.close();
    }

    /**
     * @return
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        int connectTimeout = 3000; // 连接超时时间(毫秒)
        int maxThreads = 0; // 线程数量
        int maxConnPerHost = 50; // 每个IP最大连接数
        int maxIdleSeconds = -1; // 最大闲置时间(秒)

        TrackerSelector selector = TrackerSelector.RANDOM;
        List<TrackerServer> trackers = new LinkedList<>();

        Builder() {
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder maxThreads(int maxThreads) {
            this.maxThreads = maxThreads;
            return this;
        }

        public Builder maxConnPerHost(int maxConnPerHost) {
            this.maxConnPerHost = maxConnPerHost;
            return this;
        }

        public Builder maxIdleSeconds(int maxIdleSeconds) {
            this.maxIdleSeconds = maxIdleSeconds;
            return this;
        }

        /**
         * @param selector
         * @return
         */
        public Builder selector(TrackerSelector selector) {
            this.selector = Objects.requireNonNull(selector, "selector must not be null.");
            return this;
        }

        /**
         * @param servers
         * @return
         */
        public Builder trackers(List<TrackerServer> servers) {
            this.trackers = new LinkedList<>(Objects.requireNonNull(servers, "servers must not be null."));
            return this;
        }

        /**
         * @param server
         * @return
         */
        public Builder tracker(TrackerServer server) {
            this.trackers.add(Objects.requireNonNull(server, "server must not be null."));
            return this;
        }

        /**
         * @param host
         * @param port
         * @return
         */
        public Builder tracker(String host, int port) {
            return tracker(new TrackerServer(host, port));
        }

        /**
         * @param host
         * @param port
         * @param weight
         * @return
         */
        public Builder tracker(String host, int port, int weight) {
            return tracker(new TrackerServer(host, port, weight));
        }

        public FastdfsClient build() {
            return new FastdfsClient(this);
        }
    }
}
