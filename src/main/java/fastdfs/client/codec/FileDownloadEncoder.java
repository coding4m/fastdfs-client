/**
 *
 */
package fastdfs.client.codec;

import fastdfs.client.FastdfsConstants;
import fastdfs.client.FileId;
import fastdfs.client.exchange.Requestor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;

import java.util.Collections;
import java.util.List;

import static fastdfs.client.FastdfsConstants.*;
import static fastdfs.client.FastdfsUtils.writeFixLength;

/**
 * 下载请求
 *
 * @author liulongbiao
 */
public class FileDownloadEncoder implements Requestor.Encoder {

    private static final int DEFAULT_OFFSET = 0;
    private static final int SIZE_UNLIMIT = 0;

    private final FileId fileId;
    private final int offset;
    private final int size;

    /**
     * @param fileId
     */
    public FileDownloadEncoder(FileId fileId) {
        this(fileId, DEFAULT_OFFSET, SIZE_UNLIMIT);
    }

    /**
     * @param fileId
     * @param offset
     * @param size
     */
    public FileDownloadEncoder(FileId fileId, int offset, int size) {
        this.fileId = fileId;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public List<Object> encode(ByteBufAllocator alloc) {
        byte[] pathBytes = fileId.pathBytes();
        int length = 2 * FDFS_LONG_LEN + FDFS_GROUP_LEN + pathBytes.length;
        byte cmd = FastdfsConstants.Commands.FILE_DOWNLOAD;

        ByteBuf buf = alloc.buffer(length + FDFS_HEAD_LEN);
        buf.writeLong(length);
        buf.writeByte(cmd);
        buf.writeByte(ERRNO_OK);

        buf.writeLong(offset);
        buf.writeLong(size);
        writeFixLength(buf, fileId.group(), FDFS_GROUP_LEN);
        ByteBufUtil.writeUtf8(buf, fileId.path());
        return Collections.singletonList(buf);
    }

}
