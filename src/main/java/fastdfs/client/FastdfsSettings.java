/**
 *
 */
package fastdfs.client;


/**
 * 连接设置
 *
 * @author liulongbiao
 */
final class FastdfsSettings {

    private long connectTimeout;
    private long readTimeout;
    private long idleTimeout;

    private int maxThreads;
    private int maxConnPerHost;
    private int maxPendingRequests;

    FastdfsSettings(long connectTimeout,
                    long readTimeout,
                    long idleTimeout,
                    int maxThreads,
                    int maxConnPerHost,
                    int maxPendingRequests) {

        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.idleTimeout = idleTimeout;

        this.maxThreads = maxThreads;
        this.maxConnPerHost = maxConnPerHost;
        this.maxPendingRequests = maxPendingRequests;
    }

    long connectTimeout() {
        return connectTimeout;
    }

    long readTimeout() {
        return readTimeout;
    }

    long idleTimeout() {
        return idleTimeout;
    }

    int maxThreads() {
        return maxThreads;
    }

    int maxConnPerHost() {
        return maxConnPerHost;
    }

    int maxPendingRequests() {
        return maxPendingRequests;
    }
}
