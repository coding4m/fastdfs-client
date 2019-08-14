package fastdfs.client.codec;

import fastdfs.client.FastdfsConstants;
import fastdfs.client.exchange.Replier;
import io.netty.buffer.ByteBuf;

import java.util.concurrent.CompletableFuture;

public class ActiveTestReplier implements Replier<Boolean> {
    @Override
    public void reply(ByteBuf in, CompletableFuture<Boolean> promise) {
        if (in.readableBytes() < FastdfsConstants.FDFS_HEAD_LEN) {
            return;
        }
        byte errno = in.skipBytes(9).readByte();
        promise.complete(errno == 0);
    }
}
