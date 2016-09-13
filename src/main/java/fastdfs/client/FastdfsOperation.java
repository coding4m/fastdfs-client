package fastdfs.client;

import fastdfs.client.exchange.Replier;
import fastdfs.client.exchange.Requestor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletableFuture;

/**
 * @author siuming
 */
final class FastdfsOperation<T> {

    private final static Logger LOG = LoggerFactory.getLogger(FastdfsOperation.class);

    private final Channel channel;
    private final Requestor requestor;
    private final Replier<T> replier;
    private final CompletableFuture<T> promise;

    FastdfsOperation(Channel channel, Requestor requestor, Replier<T> replier, CompletableFuture<T> promise) {
        this.channel = channel;
        this.requestor = requestor;
        this.replier = replier;
        this.promise = promise;
    }

    void execute() {

        channel.pipeline().get(FastdfsHandler.class).operation(this);
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug("channel {}, requestor {}, replier {}.", this);
            }

            requestor.request(channel);
        } catch (Exception e) {
            caught(e);
        }
    }

    boolean isDone() {
        return promise.isDone();
    }

    void await(ByteBuf in) {
        try {

            replier.reply(in, promise);
        } catch (Exception e) {
            caught(e);
        }
    }

    void caught(Throwable cause) {
        Throwable unwrap = cause;
        for (; ; ) {

            if (unwrap instanceof InvocationTargetException) {
                unwrap = ((InvocationTargetException) unwrap).getTargetException();
                continue;
            }

            if (unwrap instanceof UndeclaredThrowableException) {
                unwrap = ((UndeclaredThrowableException) unwrap).getUndeclaredThrowable();
                continue;
            }

            break;
        }

        promise.completeExceptionally(unwrap);
    }

    @Override
    public String toString() {
        return "FastdfsOperation{" +
                "channel=" + channel +
                ", replier=" + replier +
                ", requestor=" + requestor +
                '}';
    }
}
