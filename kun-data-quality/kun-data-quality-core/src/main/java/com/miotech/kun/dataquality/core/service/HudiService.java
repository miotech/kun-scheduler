package com.miotech.kun.dataquality.core.service;

import com.miotech.kun.commons.utils.HdfsFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class HudiService {

    private static final Logger logger = LoggerFactory.getLogger(HudiService.class);

    private final String VALIDATING = "validating";
    private final String VALIDATED = "commit";
    private final String HUDI_DIR = ".hoodie";

    private final HdfsFileSystem hdfsFileSystem;

    //todo:初始化 HdfsFileSystem，从配置文件读取 WAREHOUSE_URL、s3连接信息等
    public HudiService(HdfsFileSystem hdfsFileSystem) {
        this.hdfsFileSystem = hdfsFileSystem;
    }

    public String getValidatingVersion(String database, String table) throws IOException {
        String path = "/" + database + "/" + table + "/" + HUDI_DIR;
        List<String> files = hdfsFileSystem.getFilesInDir(path);
        String fileName = fetchValidatingFile(files);
        return getVersion(fileName);
    }

    public String transitToValidating(String database, String table, String version) throws IOException {
        String path = "/" + database + "/" + table + "/" + HUDI_DIR;
        List<String> files = hdfsFileSystem.getFilesInDir(path);
        String validatedFile = fetchValidatedFileByVersion(files, version);
        String validating = version + ".validating";
        hdfsFileSystem.renameFiles(path + "/" + validatedFile, path + "/" + validating);
        return validatedFile;
    }

    public String transitToValidated(String database, String table, String version) throws IOException {
        String path = "/" + database + "/" + table + "/" + HUDI_DIR;
        List<String> files = hdfsFileSystem.getFilesInDir(path);
        String validatingFile = fetchValidatingFile(files);
        if (getVersion(validatingFile).equals(version)) {
            String validatedFile = version + ".commit";
            hdfsFileSystem.renameFiles(path + "/" + validatingFile, path + "/" + validatedFile);
            saveLatestVersion(database, table, version);
            return validatedFile;
        } else {
            throw new IllegalStateException(version + " not in validating,could not transit to validated");
        }
    }

    private String fetchValidatingFile(List<String> files) {
        for (String file : files) {
            String suffix = file.substring(file.lastIndexOf(".") + 1);
            if (suffix.equals(VALIDATING)) {
                return file;
            }
        }
        throw new IllegalStateException("validating file not found");
    }

    private String fetchValidatedFileByVersion(List<String> files, String version) {
        for (String file : files) {
            String suffix = file.substring(file.lastIndexOf(".") + 1);
            if (suffix.equals(VALIDATED)) {
                String fileVersion = getVersion(file);
                if (fileVersion.equals(version)) {
                    return file;
                }
            }
        }
        throw new IllegalStateException("validated version " + version + " not found");
    }

    private String getVersion(String file) {
        logger.info("commit  file is : {}", file);
        return file.substring(0, file.lastIndexOf("."));
    }

    private void saveLatestVersion(String database, String table, String version) throws IOException {
        String path = "/" + database + "/" + table + "/" + HUDI_DIR;
        hdfsFileSystem.writeFile(path, table, version);
    }

}