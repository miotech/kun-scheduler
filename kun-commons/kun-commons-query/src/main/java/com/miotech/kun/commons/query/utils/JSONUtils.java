package com.miotech.kun.commons.query.utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONUtils {

    private JSONUtils() {}

    public static JSONObject toJsonObject(String json) throws ParseException {
        return (JSONObject) new JSONParser().parse(json);
    }

}
