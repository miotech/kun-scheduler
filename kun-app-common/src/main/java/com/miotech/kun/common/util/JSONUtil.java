package com.miotech.kun.common.util;

import com.google.gson.Gson;

public class JSONUtil {

    private JSONUtil() {}

    public static String objectToString(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }
}
