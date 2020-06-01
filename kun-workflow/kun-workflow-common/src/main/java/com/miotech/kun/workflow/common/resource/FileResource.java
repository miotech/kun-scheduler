package com.miotech.kun.workflow.common.resource;

import com.google.common.base.Preconditions;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileResource implements Resource {
    public final static String FILE_SCHEME = "file";

    private final Logger logger = LoggerFactory.getLogger(FileResource.class);
    private final String fileLocation;
    private final File resourceFile;


    public FileResource(String fileLocation, boolean createIfNotExists) {
        Preconditions.checkNotNull(fileLocation);
        this.fileLocation = fileLocation;
        this.resourceFile = new File(fileLocation);

        boolean fileExists = this.resourceFile.exists();
        if (createIfNotExists && !fileExists) {
            try {
                logger.debug("Create resource file : {}", fileLocation);
                File parent = this.resourceFile.getParentFile();
                if (parent != null) parent.mkdirs();
                this.resourceFile.createNewFile();
            } catch (IOException e) {
                logger.error("Failed to create new File in {}", fileLocation, e);
                throw ExceptionUtils.wrapIfChecked(e);
            }
        } else if (!fileExists) {
            throw new ResourceNotFoundException("Resource file Not found: " + fileLocation);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(resourceFile);
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(resourceFile);
    }

    @Override
    public String getLocation() {
        return FILE_SCHEME + "://" + this.fileLocation;
    }
}
