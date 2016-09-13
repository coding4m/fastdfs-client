/**
 *
 */
package fastdfs.client;

import fastdfs.client.exchange.Replier;
import fastdfs.client.exchange.ReplierDecoder;
import fastdfs.client.exchange.Requestor;
import fastdfs.client.exchange.RequestorEncoder;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * FastdfsExecutor
 *
 * @author liulongbiao
 */
final class FastdfsExecutor implements Closeable {

    private final NioEventLoopGroup loopGroup;
    private final FastdfsPoolGroup poolGroup;

    FastdfsExecutor(FastdfsSettings settings) {
        loopGroup = new NioEventLoopGroup(settings.maxThreads());
        poolGroup = new FastdfsPoolGroup(loopGroup, settings);
    }

    /**
     * 访问 Fastdfs 服务器
     *
     * @param addr
     * @param encoder
     * @param decoder
     * @return
     */
    <T> CompletableFuture<T> execute(InetSocketAddress addr, Requestor.Encoder encoder, Replier.Decoder<T> decoder) {
        return execute(addr, new RequestorEncoder(encoder), new ReplierDecoder<>(decoder));
    }

    /**
     * @param addr
     * @param encoder
     * @param replier
     * @param <T>
     * @return
     */
    <T> CompletableFuture<T> execute(InetSocketAddress addr, Requestor.Encoder encoder, Replier<T> replier) {
        return execute(addr, new RequestorEncoder(encoder), replier);
    }

    /**
     * @param addr
     * @param requestor
     * @param replier
     * @param <T>
     * @return
     */
    <T> CompletableFuture<T> execute(InetSocketAddress addr, Requestor requestor, Replier<T> replier) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        execute(addr, requestor, replier, promise);
        return promise;
    }

    private <T> void execute(InetSocketAddress addr, Requestor requestor, Replier<T> replier, CompletableFuture<T> promise) {
        FastdfsPool pool = poolGroup.get(addr);
        pool.acquire().addListener(new FastdfsChannelListener<>(pool, requestor, replier, promise));
    }

    @PreDestroy
    public void close() throws IOException {
        if (null != poolGroup) {
            try {
                poolGroup.close();
            } catch (Exception e) {
                // ignore
            }
        }
        if (null != loopGroup) {
            loopGroup.shutdownGracefully();
        }
    }

    private static class FastdfsChannelListener<T> implements FutureListener<Channel> {

        final FastdfsPool pool;
        final Requestor requestor;
        final Replier<T> replier;
        final CompletableFuture<T> promise;

        FastdfsChannelListener(FastdfsPool pool,
                               Requestor requestor,
                               Replier<T> replier,
                               CompletableFuture<T> promise) {
            this.pool = pool;
            this.requestor = requestor;
            this.replier = replier;
            this.promise = promise;
        }

        @Override
        public void operationComplete(Future<Channel> future) throws Exception {

            if (future.isCancelled()) {
                promise.cancel(true);
                return;
            }

            if (!future.isSuccess()) {
                promise.completeExceptionally(future.cause());
                return;
            }

            Channel channel = future.getNow();
            promise.whenComplete((result, error) -> pool.release(channel));

            try {

                FastdfsOperation<T> fastdfsOperation = new FastdfsOperation<>(channel, requestor, replier, promise);
                fastdfsOperation.execute();
            } catch (Exception e) {
                promise.completeExceptionally(e);
            }
        }
    }
}
