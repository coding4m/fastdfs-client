/**
 *
 */
package fastdfs.client.codec;

import fastdfs.client.FastdfsConstants;
import fastdfs.client.FileId;

/**
 * 删除请求
 *
 * @author liulongbiao
 */
public class FileDeleteEncoder extends FileIdOperationEncoder {

    public FileDeleteEncoder(FileId fileId) {
        super(fileId);
    }

    @Override
    protected byte cmd() {
        return FastdfsConstants.Commands.FILE_DELETE;
    }
}
