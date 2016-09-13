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

    private int connectTimeout;
    private int maxThreads;
    private int maxConnPerHost;
    private int maxIdleSeconds;

    FastdfsSettings(int connectTimeout, int maxThreads, int maxConnPerHost, int maxIdleSeconds) {
        this.connectTimeout = connectTimeout;
        this.maxThreads = maxThreads;
        this.maxConnPerHost = maxConnPerHost;
        this.maxIdleSeconds = maxIdleSeconds;
    }

    int connectTimeout() {
        return connectTimeout;
    }

    int maxThreads() {
        return maxThreads;
    }

    int maxConnPerHost() {
        return maxConnPerHost;
    }

    int maxIdleSeconds() {
        return maxIdleSeconds;
    }
}
