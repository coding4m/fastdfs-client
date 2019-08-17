/**
 *
 */
package fastdfs.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

final class FastdfsHandler extends ByteToMessageDecoder {
    static final AttributeKey<FastdfsOperation<?>> OPERATION_KEY = AttributeKey.newInstance("fastdfsOperation");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        FastdfsOperation<?> operation = ctx.channel().attr(OPERATION_KEY).get();

        if (null != operation) {
            operation.await(in);
            return;
        }

        if (in.readableBytes() <= 0) {
            return;
        }

        throw new FastdfsDataOverflowException(
                String.format(
                        "fastdfs channel %s remain %s data bytes, but there is not operation await.",
                        ctx.channel(),
                        in.readableBytes()
                )
        );
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        FastdfsOperation<?> operation = ctx.channel().attr(OPERATION_KEY).get();
        // read idle event.
        if (evt == IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT
                || evt == IdleStateEvent.READER_IDLE_STATE_EVENT) {

            if (null != operation) {
                throw new FastdfsReadTimeoutException(
                        String.format(
                                "execute %s read timeout.",
                                operation
                        )
                );
            }
        }

        // all idle event.
        if (evt == IdleStateEvent.FIRST_ALL_IDLE_STATE_EVENT
                || evt == IdleStateEvent.ALL_IDLE_STATE_EVENT) {
            throw new FastdfsTimeoutException("fastdfs channel was idle timeout.");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        FastdfsOperation<?> operation = ctx.channel().attr(OPERATION_KEY).get();
        if (null == operation) {
            return;
        }

        operation.caught(new FastdfsException("channel closed."));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        FastdfsOperation<?> operation = ctx.channel().attr(OPERATION_KEY).get();
        if(null == operation) {
            ctx.close();
            return;
        }

        operation.caught(translateException(cause));
    }

    private Throwable translateException(Throwable cause) {
        if (cause instanceof FastdfsException) {
            return cause;
        }

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

        return new FastdfsException("fastdfs operation error.", unwrap);
    }
}
