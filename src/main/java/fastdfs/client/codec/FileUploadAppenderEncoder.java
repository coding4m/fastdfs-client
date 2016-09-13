/**
 *
 */
package fastdfs.client.codec;

import fastdfs.client.FastdfsConstants;

import java.io.File;

/**
 * 上传追加文件请求
 *
 * @author liulongbiao
 */
public class FileUploadAppenderEncoder extends FileUploadEncoder {

    /**
     * @param file
     * @param pathIdx
     */
    public FileUploadAppenderEncoder(File file, byte pathIdx) {
        super(file, pathIdx);
    }

    /**
     * @param content
     * @param filename
     * @param length
     * @param pathIdx
     */
    public FileUploadAppenderEncoder(Object content, String filename, long length, byte pathIdx) {
        super(content, filename, length, pathIdx);
    }

    @Override
    protected byte cmd() {
        return FastdfsConstants.Commands.APPENDER_FILE_UPLOAD;
    }
}
