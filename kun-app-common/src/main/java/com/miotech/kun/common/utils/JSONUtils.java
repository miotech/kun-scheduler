package com.miotech.kun.common.utils;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class JSONUtils {

    private JSONUtils() {}

    public static String toJsonString(Object obj) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(obj);
    }

    public static JSONObject toJsonObject(Object object) {
        return toJsonObject(toJsonString(object));
    }

    public static JSONObject toJsonObject(String json) {
        return new JSONObject(toJavaObject(json, Map.class));
    }

    public static <T> T toJavaObject(JSONObject jsonObject, Class<T> clazz) {
        return toJavaObject(jsonObject.toJSONString(), clazz);
    }

    public static <T> T toJavaObject(String json, Class<T> clazz) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<Map>() {
                }.getType(), new MapTypeAdapter())
                .create();
        return gson.fromJson(json, clazz);
    }

    public static <T> T toJavaObject(String json, Type type) {
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    private static class MapTypeAdapter extends TypeAdapter<Object> {

        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<Object>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;

                case BEGIN_OBJECT:
                    Map<String, Object> map = new LinkedTreeMap<String, Object>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), read(in));
                    }
                    in.endObject();
                    return map;

                case STRING:
                    return in.nextString();

                case NUMBER:
                    double dbNum = in.nextDouble();

                    if (dbNum > Long.MAX_VALUE) {
                        return dbNum;
                    }

                    long lngNum = (long) dbNum;
                    if (dbNum == lngNum) {
                        return lngNum;
                    } else {
                        return dbNum;
                    }

                case BOOLEAN:
                    return in.nextBoolean();

                case NULL:
                    in.nextNull();
                    return null;

                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
        }

    }
}
