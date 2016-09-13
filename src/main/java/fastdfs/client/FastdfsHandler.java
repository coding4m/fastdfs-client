/**
 *
 */
package fastdfs.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

final class FastdfsHandler extends ByteToMessageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(FastdfsHandler.class);

    private volatile FastdfsOperation<?> operation;

    void operation(FastdfsOperation<?> operation) {
        this.operation = operation;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (null != operation) {
            operation.await(in);
            return;
        }

        if (in.readableBytes() <= 0) {
            return;
        }

        throw new FastdfsDataOverflowException(
                String.format(
                        "channel %s remain %s data bytes, but there is not operation await.",
                        ctx.channel(),
                        in.readableBytes()
                )
        );
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            throw new FastdfsTimeoutException("channel was idle for maxIdleSeconds.");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (null == operation) {
            return;
        }

        if (!operation.isDone()) {
            throw new FastdfsException("channel closed.");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        if (null != operation) {
            operation.caught(cause);
            return;
        }

        // idle timeout.
        if (cause instanceof FastdfsTimeoutException) {
            LOG.debug(cause.getMessage(), cause);
            return;
        }

        LOG.error(cause.getMessage(), cause);
    }
}
