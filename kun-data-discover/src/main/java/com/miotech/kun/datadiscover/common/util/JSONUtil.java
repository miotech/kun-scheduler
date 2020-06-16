package com.miotech.kun.datadiscover.common.util;

import com.google.gson.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JSONUtil {

    private JSONUtil() {}

    public static String objectToString(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }
}
