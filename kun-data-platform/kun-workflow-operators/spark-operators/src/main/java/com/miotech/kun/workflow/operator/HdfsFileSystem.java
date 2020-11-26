package com.miotech.kun.workflow.operator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HdfsFileSystem {

    private final Logger logger = LoggerFactory.getLogger(HdfsFileSystem.class);
    private FileSystem fileSystem;
    private String hdfsUri;
    public HdfsFileSystem(String hdfsUri, Configuration conf) throws URISyntaxException,IOException {
        this.hdfsUri = hdfsUri;
        this.fileSystem = FileSystem.get(new URI(hdfsUri),conf);
    }

    /**
     * copy files in dirPath from hdfs to local file
     * @param dirPath files directory in hdfs
     * @return
     * @throws IOException
     */
    public  List<String> copyFilesInDir(String dirPath) throws IOException {
        List<String> fileList = new ArrayList<>();
        Path path = new Path(hdfsUri + "/" + dirPath);
        FileStatus[] fileStatus = fileSystem.listStatus(path);
        logger.debug("file size = {}", fileStatus.length);
        for (FileStatus fileStat : fileStatus) {
            String localFile = fileStat.getPath().toUri().getPath();
            fileSystem.copyToLocalFile(fileStat.getPath(), new Path(localFile));
            logger.debug("copy hdfs file = {} to local file = {}", fileStat.getPath(), localFile);
            fileList.add(localFile);
        }
        return fileList;
    }

    /**
     * delete all file in dirPath
     * @param dirPath files directory in hdfs
     * @throws IOException
     */
    public void deleteFilesInDir(String dirPath) throws IOException{
        fileSystem.delete(new Path(hdfsUri + "/" + dirPath),true);
    }

}
