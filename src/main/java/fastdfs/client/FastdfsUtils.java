/**
 *
 */
package fastdfs.client;

import io.netty.buffer.ByteBuf;

import java.util.Collection;
import java.util.List;

/**
 * 帮助类
 *
 * @author liulongbiao
 */
public final class FastdfsUtils {

    private final static String WORDS = "[a-zA-Z0-9]{1,6}";

    private FastdfsUtils() {
    }

    /**
     * 判断字符串为空
     *
     * @param content
     * @return
     */
    public static boolean isEmpty(String content) {
        return content == null || content.isEmpty();
    }

    /**
     * 判断集合是否为空
     *
     * @param coll
     * @return
     */
    public static <T> boolean isEmpty(Collection<T> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * 获取列表头元素
     *
     * @param list
     * @return
     */
    public static <T> T first(List<T> list) {
        return isEmpty(list) ? null : list.get(0);
    }

    /**
     * 给 ByteBuf 写入定长字符串
     * <p>
     * 若字符串长度大于定长，则截取定长字节；若小于定长，则补零
     *
     * @param buf
     * @param content
     * @param length
     */
    public static void writeFixLength(ByteBuf buf, String content, int length) {
        byte[] bytes = content.getBytes(FastdfsConstants.UTF_8);
        int blen = bytes.length;
        int wlen = Math.min(blen, length);
        buf.writeBytes(bytes, 0, wlen);
        if (wlen < length) {
            buf.writeZero(length - wlen);
        }
    }

    /**
     * 读取固定长度的字符串(修剪掉补零的字节)
     *
     * @param in
     * @param length
     * @return
     */
    public static String readString(ByteBuf in, int length) {
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        return new String(bytes, FastdfsConstants.UTF_8).trim();
    }

    /**
     * 读取字符串(修剪掉补零的字节)
     *
     * @param in
     * @return
     */
    public static String readString(ByteBuf in) {
        return in.toString(FastdfsConstants.UTF_8);
    }

    /**
     * 获取文件扩展名
     *
     * @param filename
     * @return
     */
    public static String getFileExt(String filename, String defaultExt) {
        String fileExt = getFileExt(filename);
        return isEmpty(fileExt) || !fileExt.matches(WORDS) ? defaultExt : fileExt;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename
     * @return
     */
    private static String getFileExt(String filename) {
        if (filename == null) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        return idx == -1 ? "" : filename.substring(idx + 1).toLowerCase();
    }
}
