package com.miotech.kun.datadiscovery.service.rdm;

import java.io.InputStream;
import java.util.List;

public interface StorageFileManger {
    String putObject(String path, InputStream inputStream);

    InputStream getObjectContent(String path);
}
