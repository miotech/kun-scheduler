package com.miotech.kun.commons.network.utils;

import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class JavaUtils {
    private static final Logger logger = LoggerFactory.getLogger(JavaUtils.class);

    private JavaUtils() {}

    public static String bytesToString(ByteBuffer b) {
        return Unpooled.wrappedBuffer(b)
                .toString(StandardCharsets.UTF_8);
    }

    public static ByteBuffer stringToBytes(String s) {
        return Unpooled.wrappedBuffer(s.getBytes(StandardCharsets.UTF_8)).nioBuffer();
    }

    /** resolve localhost address */
    public static String getLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Closes the given object, ignoring IOExceptions. */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            logger.error("IOException should not have been thrown.", e);
        }
    }
}
