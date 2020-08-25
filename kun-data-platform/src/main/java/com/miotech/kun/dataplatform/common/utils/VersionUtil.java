package com.miotech.kun.dataplatform.common.utils;

import com.google.common.base.Preconditions;

public class VersionUtil {
    private VersionUtil() {}

    private static final String VERSION_PREFIX = "V";

    public static String getVersion(int i) {
        Preconditions.checkArgument(i > 0, "Version id should be positive number");
        return VERSION_PREFIX + i;
    }

    public static int parseVersionNumber(String version) {
        Preconditions.checkNotNull(version);
        return Integer.parseInt(version.replace(VERSION_PREFIX, ""));
    }
}
