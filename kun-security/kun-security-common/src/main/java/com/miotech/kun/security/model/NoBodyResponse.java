package com.miotech.kun.security.model;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author: Jie Chen
 * @created: 2020/11/30
 */
public class NoBodyResponse extends HttpServletResponseWrapper {

    private final NoBodyOutputStream noBody;
    private PrintWriter writer;
    private boolean didSetContentLength;

    public NoBodyResponse(HttpServletResponse r) {
        super(r);
        noBody = new NoBodyOutputStream(this);
    }

    public void setContentLength() {
        if (!didSetContentLength) {
            if (writer != null) {
                writer.flush();
            }
            super.setContentLength(noBody.getContentLength());
        }
    }


    // SERVLET RESPONSE interface methods

    @Override
    public void setContentLength(int len) {
        super.setContentLength(len);
        didSetContentLength = true;
    }

    @Override
    public void setContentLengthLong(long len) {
        super.setContentLengthLong(len);
        didSetContentLength = true;
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void setIntHeader(String name, int value) {
        super.setIntHeader(name, value);
        checkHeader(name);
    }

    @Override
    public void addIntHeader(String name, int value) {
        super.addIntHeader(name, value);
        checkHeader(name);
    }

    private void checkHeader(String name) {
        if ("content-length".equalsIgnoreCase(name)) {
            didSetContentLength = true;
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return noBody;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {

        if (writer == null) {
            OutputStreamWriter w;

            w = new OutputStreamWriter(noBody, getCharacterEncoding());
            writer = new PrintWriter(w);
        }
        return writer;
    }

    static class NoBodyOutputStream extends ServletOutputStream {

        private static final String LSTRING_FILE =
                "javax.servlet.http.LocalStrings";
        private static final ResourceBundle lStrings =
                ResourceBundle.getBundle(LSTRING_FILE);

        private final HttpServletResponse response;
        private boolean flushed = false;
        private int contentLength = 0;

        // file private
        NoBodyOutputStream(HttpServletResponse response) {
            this.response = response;
        }

        // file private
        int getContentLength() {
            return contentLength;
        }

        @Override
        public void write(int b) throws IOException {
            contentLength++;
            checkCommit();
        }

        @Override
        public void write(byte buf[], int offset, int len) throws IOException {
            if (buf == null) {
                throw new NullPointerException(
                        lStrings.getString("err.io.nullArray"));
            }

            if (offset < 0 || len < 0 || offset+len > buf.length) {
                String msg = lStrings.getString("err.io.indexOutOfBounds");
                Object[] msgArgs = new Object[3];
                msgArgs[0] = Integer.valueOf(offset);
                msgArgs[1] = Integer.valueOf(len);
                msgArgs[2] = Integer.valueOf(buf.length);
                msg = MessageFormat.format(msg, msgArgs);
                throw new IndexOutOfBoundsException(msg);
            }

            contentLength += len;
            checkCommit();
        }

        @Override
        public boolean isReady() {
            // TODO SERVLET 3.1
            return false;
        }

        @Override
        public void setWriteListener(javax.servlet.WriteListener listener) {
            // TODO SERVLET 3.1
        }

        private void checkCommit() throws IOException {
            if (!flushed && contentLength > response.getBufferSize()) {
                response.flushBuffer();
                flushed = true;
            }
        }
    }
}
