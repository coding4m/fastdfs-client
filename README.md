# fastdfs-client

fastdfs-client is a [FastDFS](https://github.com/happyfish100/fastdfs) java client 
based on [Netty 4](http://netty.io) .

## Requirements

* Java 8.

## Usage

```java
    FastdfsClient  client = FastdfsClient.newBuilder()
                                            .maxIdleSeconds(3000)
                                            .tracker("192.168.1.2", 22222)
                                            .build();
                                          
    CompletableFuture<FileId> path = client.upload(new File("/tmp/test.dmg"));
    // do something.
    
    FileId fileId = FileId.fromString("group1/M00/00/00/ZfvfZlbz6VuAPdosAARXBcPHPhU268.log");
    //FileId fileId = FileId.fromBase64String("base64 string");
    CompletableFuture<FileInfo> promise = client.infoGet(fileId);
    
    // do something.
```
