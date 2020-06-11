package com.miotech.kun.metadata.extract.tool;

public class UseDatabaseUtil {

    public static String useDatabase(String url, String database) {
        return url.substring(0, url.lastIndexOf("/")) + "/" + database;
    }

    public static String useSchema(String url, String database, String schema) {
        return useDatabase(url, database) + "?currentSchema=" + schema;
    }

}
