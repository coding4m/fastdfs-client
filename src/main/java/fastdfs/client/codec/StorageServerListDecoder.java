/**
 *
 */
package fastdfs.client.codec;

import fastdfs.client.FastdfsException;
import fastdfs.client.StorageServer;
import fastdfs.client.exchange.Replier;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import static fastdfs.client.FastdfsConstants.*;
import static fastdfs.client.FastdfsUtils.readString;

/**
 * 存储服务器信息列表解码器
 *
 * @author liulongbiao
 */
public enum StorageServerListDecoder implements Replier.Decoder<List<StorageServer>> {
    INSTANCE;

    @Override
    public List<StorageServer> decode(ByteBuf in) {
        int size = in.readableBytes();
        if (size < FDFS_STORAGE_LEN) {
            throw new FastdfsException("body length : " + size + " is less than required length " + FDFS_STORAGE_LEN);
        }
        if ((size % FDFS_STORAGE_LEN) != 0) {
            throw new FastdfsException("body length : " + size + " is invalidate. ");
        }

        int count = size / FDFS_STORAGE_LEN;
        List<StorageServer> results = new ArrayList<StorageServer>(count);
        for (int i = 0; i < count; i++) {
            String group = readString(in, FDFS_GROUP_LEN);
            if (group.indexOf('\0') > 0) {
                group = group.substring(0, group.indexOf('\0'));
            }
            String host = readString(in, FDFS_HOST_LEN);
            if (host.indexOf('\0') > 0) {
                host = host.substring(0, host.indexOf('\0'));
            }
            int port = (int) in.readLong();
            results.add(new StorageServer(group, host, port));
        }

        return results;
    }

}
