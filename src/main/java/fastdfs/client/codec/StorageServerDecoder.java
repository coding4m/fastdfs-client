/**
 *
 */
package fastdfs.client.codec;

import fastdfs.client.StorageServer;
import fastdfs.client.exchange.Replier;
import io.netty.buffer.ByteBuf;

import static fastdfs.client.FastdfsConstants.*;
import static fastdfs.client.FastdfsUtils.readString;

/**
 * 存储服务器信息解码器
 *
 * @author liulongbiao
 */
public enum StorageServerDecoder implements Replier.Decoder<StorageServer> {

    INSTANCE;

    @Override
    public long expectLength() {
        return FDFS_STORAGE_STORE_LEN;
    }

    @Override
    public StorageServer decode(ByteBuf in) {
        String group = readString(in, FDFS_GROUP_LEN);
        if (group.indexOf('\0') > 0) {
            group = group.substring(0, group.indexOf('\0'));
        }
        String host = readString(in, FDFS_HOST_LEN);
        if (host.indexOf('\0') > 0) {
            host = host.substring(0, host.indexOf('\0'));
        }
        int port = (int) in.readLong();
        byte idx = in.readByte();
        return new StorageServer(group, host, port, idx);
    }

}
