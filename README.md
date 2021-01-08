# fastdfs-client

fastdfs-client is a [FastDFS](https://github.coma/happyfish100/fstdfs) java client 
based on [Netty 4](http://netty.io) . support multiple tracker server pool and active tracker server health check .

## Requirements

* Java 8+

## Usage

```java
    
    // FastdfsClient is threadsafe and use connection pool.
    FastdfsClient  client = FastdfsClient.newBuilder()
                                            .connectTimeout(3000)
                                            .readTimeout(100)
                                            .healthCheck(3, 2, 3000, 10000)
                                            .tracker("192.168.1.2", 22222)
                                            .build();
    
    // upload file
    CompletableFuture<FileId> promise = client.upload(new File("/tmp/test.dmg"));
    FileId fileId = promise.get();
    // do something.
    
    // download file
    OutputStream out = ...
    CompletableFuture<Void> promise = client.download(fileId, out);
    // promise.whenComplete(...);
    
    // delete file
    CompletableFuture<Void> promise = client.delete(fileId);
    // promise.whenComplete(...);
    
    // get file info
    CompletableFuture<FileInfo> promise = client.infoGet(fileId);
    FileInfo fileInfo = promise.get();
    // do something.
    
    // set file metadata
    FileMetadata metadata = FileMetadata.newBuilder().put("test", "test1").build();
    CompletableFuture<Void> promise = client.metadataSet(fileId, metadata);
    // do something.
    
    // get file metadata
    CompletableFuture<FileMetadata> promise = client.metadataGet(fileId);
    FileMetadata metadata = promise.get();
    // do something.
    
    client.close();
```
