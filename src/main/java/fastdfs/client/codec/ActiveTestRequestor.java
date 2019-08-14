package fastdfs.client.codec;

import fastdfs.client.exchange.Requestor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import static fastdfs.client.FastdfsConstants.*;

public class ActiveTestRequestor implements Requestor {

    @Override
    public void request(Channel channel) {
        ByteBuf buf = channel.alloc().buffer(FDFS_HEAD_LEN);
        buf.writeLong(0L);
        buf.writeByte(Commands.ACTIVE_TEST);
        buf.writeByte(ERRNO_OK);
        channel.writeAndFlush(buf);
    }
}
