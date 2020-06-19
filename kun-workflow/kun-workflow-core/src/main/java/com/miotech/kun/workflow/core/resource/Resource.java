package com.miotech.kun.workflow.core.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Resource {
    /**
     * 获得资源的路径。
     */
    String getLocation();

    /**
     * 读取资源
     */
    InputStream getInputStream() throws IOException;

    /**
     * 资源是否可写入。
     */
    boolean isWritable();

    /**
     * 写入资源
     */
    OutputStream getOutputStream() throws IOException;
}
