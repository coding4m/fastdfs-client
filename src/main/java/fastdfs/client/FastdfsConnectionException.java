/**
 * 
 */
package fastdfs.client;

/**
 * Fastdfs 连接异常
 * 
 * @author liulongbiao
 *
 */
public class FastdfsConnectionException extends FastdfsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2090584261332913146L;

	public FastdfsConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public FastdfsConnectionException(String message) {
		super(message);
	}

	public FastdfsConnectionException(Throwable cause) {
		super(cause);
	}

}
