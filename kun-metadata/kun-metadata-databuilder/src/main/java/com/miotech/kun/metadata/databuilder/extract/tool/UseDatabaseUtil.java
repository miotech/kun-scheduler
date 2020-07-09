package com.miotech.kun.metadata.databuilder.extract.tool;

public class UseDatabaseUtil {

    private UseDatabaseUtil() {
    }

    public static String useDatabase(String url, String database) {
        url = fixUrl(url);
        return url.substring(0, url.lastIndexOf('/') + 1) + database;
    }

    public static String useSchema(String url, String database, String schema) {
        url = fixUrl(url);
        return useDatabase(url, database) + "?currentSchema=" + schema;
    }

    private static String fixUrl(String url) {
        if (!url.contains("/")) {
            return url.concat("/");
        }

        return url;
    }

}
