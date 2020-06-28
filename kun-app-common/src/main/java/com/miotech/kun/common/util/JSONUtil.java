package com.miotech.kun.common.util;

import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Type;

public class JSONUtil {

    private JSONUtil() {}

    public static String toJsonString(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static JSONObject toJsonObject(String json) throws ParseException {
        return (JSONObject) new JSONParser().parse(json);
    }

    public static <T> T toJavaObject(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    public static <T> T toJavaObject(String json, Type type) {
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }
}
