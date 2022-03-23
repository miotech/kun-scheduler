package com.miotech.kun.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class CloseableUtils {
    private static final Logger logger = LoggerFactory.getLogger(CloseableUtils.class);

    public static void closeIfPossible(Object instance) {
        if (instance instanceof Closeable) {
            try {
                ((Closeable) instance).close();
            } catch (IOException e) {
                logger.error("Failed to close object {}.", instance, e);
            }
        }
    }
}
