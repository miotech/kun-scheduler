package com.miotech.kun.workflow.core.resource;

import java.io.IOException;
import java.io.InputStream;

public interface Resource {
    /**
     * 读取资源
     */
    InputStream getInputStream() throws IOException;
}
