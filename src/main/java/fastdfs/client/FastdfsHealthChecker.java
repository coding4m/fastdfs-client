package fastdfs.client;

import fastdfs.client.codec.ActiveTestReplier;
import fastdfs.client.codec.ActiveTestRequestor;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.util.concurrent.Future;

import java.util.concurrent.CompletableFuture;

public class FastdfsHealthChecker implements ChannelHealthChecker {
    @Override
    public Future<Boolean> isHealthy(Channel channel) {
        CompletableFuture<Boolean> promise = new CompletableFuture<>();
        FastdfsOperation<Boolean> operation = new FastdfsOperation<>(channel, new ActiveTestRequestor(), new ActiveTestReplier(), promise);
        operation.execute();
        // todo
        return null;
    }
}
