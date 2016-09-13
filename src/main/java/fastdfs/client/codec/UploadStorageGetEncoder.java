/**
 *
 */
package fastdfs.client.codec;

import fastdfs.client.exchange.Requestor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.Collections;
import java.util.List;

import static fastdfs.client.FastdfsConstants.Commands.SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE;
import static fastdfs.client.FastdfsConstants.Commands.SERVICE_QUERY_STORE_WITH_GROUP_ONE;
import static fastdfs.client.FastdfsConstants.*;
import static fastdfs.client.FastdfsUtils.isEmpty;
import static fastdfs.client.FastdfsUtils.writeFixLength;

/**
 * 获取可上传的存储服务器
 *
 * @author liulongbiao
 */
public class UploadStorageGetEncoder implements Requestor.Encoder {

    private String group;

    public UploadStorageGetEncoder(String group) {
        this.group = group;
    }

    @Override
    public List<Object> encode(ByteBufAllocator alloc) {
        int length = isEmpty(group) ? 0 : FDFS_GROUP_LEN;
        byte cmd = isEmpty(group) ? SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE : SERVICE_QUERY_STORE_WITH_GROUP_ONE;

        ByteBuf buf = alloc.buffer(length + FDFS_HEAD_LEN);
        buf.writeLong(length);
        buf.writeByte(cmd);
        buf.writeByte(ERRNO_OK);
        if (!isEmpty(group)) {
            writeFixLength(buf, group, FDFS_GROUP_LEN);
        }
        return Collections.singletonList(buf);
    }
}
