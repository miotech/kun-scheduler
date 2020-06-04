package com.miotech.kun.workflow.web.mock;

import com.google.common.base.Preconditions;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DelegatingServletInputStream extends ServletInputStream {
    private final InputStream sourceStream;

    /**
     * Create a DelegatingServletInputStream for the given source stream.
     * @param sourceStream the source stream (never {@code null}
     */
    DelegatingServletInputStream(InputStream sourceStream) {
        Preconditions.checkNotNull("Source InputStream must not be null", sourceStream);
        this.sourceStream = sourceStream;
    }

    @Override
    public int read() throws IOException {
        return sourceStream.read();
    }

    @Override
    public void close() throws IOException {
        super.close();
        sourceStream.close();
    }

    @Override
    public boolean isFinished() {
        return Boolean.FALSE;
    }

    @Override
    public boolean isReady() {
        return Boolean.TRUE;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
    }
}