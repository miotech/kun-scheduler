package com.miotech.kun.workflow.utils;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

@SuppressWarnings("UnstableApiUsage")
public class HashUtils {
    private static final HashFunction murmur3 = Hashing.murmur3_128();

    public static String murmur(byte[] bytes) {
        return murmur3.hashBytes(bytes).toString();
    }

    public static String murmur(String text) {
        return murmur3.hashUnencodedChars(text).toString();
    }
}
