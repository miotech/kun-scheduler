package com.miotech.kun.commons.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HdfsFileSystem {

    private final Logger logger = LoggerFactory.getLogger(HdfsFileSystem.class);
    private FileSystem fileSystem;
    private String hdfsUri;

    public HdfsFileSystem(String hdfsUri, Configuration conf) throws URISyntaxException, IOException {
        this.hdfsUri = hdfsUri;
        this.fileSystem = FileSystem.get(new URI(hdfsUri), conf);
    }

    /**
     * copy files in dirPath from hdfs to local file
     *
     * @param dirPath files directory in hdfs
     * @return
     * @throws IOException
     */
    public List<String> copyFilesInDir(String dirPath) throws IOException {
        List<String> fileList = new ArrayList<>();
        Path path = new Path(hdfsUri + "/" + dirPath);
        FileStatus[] fileStatus = fileSystem.listStatus(path);
        logger.debug("file size = {}", fileStatus.length);
        for (FileStatus fileStat : fileStatus) {
            String localFile = "/tmp" + fileStat.getPath().toUri().getPath();
            logger.debug("copy hdfs file = {} to local file = {}", fileStat.getPath(), localFile);
            fileSystem.copyToLocalFile(fileStat.getPath(), new Path(localFile));
            fileList.add(localFile);
        }
        return fileList;
    }

    /**
     * get files name in dir
     * file name is the final component of path.
     *
     * @param dirPath files directory in hdfs
     * @return
     * @throws IOException
     */
    public List<String> getFilesInDir(String dirPath) throws IOException {
        List<String> fileList = new ArrayList<>();
        Path path = new Path(hdfsUri + "/" + dirPath);
        FileStatus[] fileStatus = fileSystem.listStatus(path);
        logger.debug("file size = {}", fileStatus.length);
        for (FileStatus fileStat : fileStatus) {
            fileList.add(fileStat.getPath().getName());
        }
        return fileList;
    }

    /**
     * delete all file in dirPath
     *
     * @param dirPath files directory in hdfs
     * @throws IOException
     */
    public void deleteFilesInDir(String dirPath) throws IOException {
        fileSystem.delete(new Path(hdfsUri + "/" + dirPath), true);
    }

    /**
     * rename file name
     *
     * @param oldFilePath
     * @param newFilePath
     * @throws IOException
     */
    public void renameFiles(String oldFilePath, String newFilePath) throws IOException {
        fileSystem.rename(new Path(hdfsUri + "/" + oldFilePath), new Path(hdfsUri + "/" + newFilePath));
    }

    /**
     * write file into warehouse
     *
     * @param path
     * @param fileName
     */
    public void writeFile(String path, String fileName, String content) throws IOException {
        String localFile = "/tmp/" + fileName;
        String dfsFile = hdfsUri + "/" + path + "/" + fileName;
        FileWriter fileWriter = new FileWriter(localFile);
        fileWriter.write(content);
        fileWriter.close();
        fileSystem.copyFromLocalFile(true, true, new Path(localFile), new Path(dfsFile));
    }

}
