package fastdfs.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * @author siuming
 */
public class FastdfsClientTest {

    FastdfsClient client;

    @Before
    public void setUp() throws Exception {
        client = FastdfsClient.newBuilder()
                                .maxIdleSeconds(3000)
                                .tracker("172.16.1.25", 22222)
                                .build();

    }

    @Test
    public void testUpload() throws Exception {
        long current = System.currentTimeMillis();
        CompletableFuture<FileId> path = client.upload(null, new File("/tmp/test.dmg"));
        System.out.println(path.get());
        System.out.println("==========");
        System.out.println(System.currentTimeMillis() - current + " ms");
    }

    @Test
    public void testUploadAppend() throws Exception {
        CompletableFuture<FileId> path = client.uploadAppender(new byte[0],"abc.jpg",0);
        FileId fileId = path.get();
        client.modify(fileId, new FileInputStream("/Users/coding4m/Downloads/1-140H20942260-L.jpg"),23753, 0).get();
        System.out.println(fileId.toBase64String());
        FileInfo fileInfo = client.infoGet(fileId).get();
        System.out.println(fileInfo);
    }

    @Test
    public void testDownload() throws Exception {

        long current = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            File file = new File("/tmp/logo2.png");
            FileId path = FileId.fromString("group1/M00/00/00/ZfvfZlbz1EyAC4FPAAAWNZ1l3ec600.png");
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                CompletableFuture<Void> promise = client.download(path, outputStream);
                promise.get();
            }
        }
        System.out.println(System.currentTimeMillis() - current + " ms");
    }

    @Test
    public void testSetMetadata() throws Exception {
        FileId path = FileId.fromString("group1/M00/00/00/ZfvfZlbz1EyAC4FPAAAWNZ1l3ec600.png");
        FileMetadata metadata = FileMetadata.newBuilder().put("test", "test1").build();
        CompletableFuture<Void> promise = client.metadataSet(path, metadata);
        promise.get();
    }

    @Test
    public void testGetMetadata() throws Exception {
        FileId path = FileId.fromString("group1/M00/00/00/ZfvfZlbz1EyAC4FPAAAWNZ1l3ec600.png");
        CompletableFuture<FileMetadata> promise = client.metadataGet(path);
        System.out.println(promise.get().values());
    }

    @Test
    public void testGetInfo() throws Exception {
        FileId path = FileId.fromString("group1/M00/00/00/ZfvfZlbz6VuAPdosAARXBcPHPhU268.log");
        CompletableFuture<FileInfo> promise = client.infoGet(path);
        System.out.println(promise.get());
    }

    @Test
    public void testDelete() throws Exception {
        FileId path = FileId.fromString("group1/M00/00/00/ZfvfZlbz7TaAeyUeAeJOH39coH0381.dmg");
        client.delete(path).get();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }
}
