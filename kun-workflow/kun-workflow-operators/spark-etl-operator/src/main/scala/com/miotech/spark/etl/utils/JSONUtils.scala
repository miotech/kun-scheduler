package com.miotech.spark.etl.utils

import com.google.gson.{Gson, JsonArray, JsonElement, JsonObject, JsonParser}
import org.apache.commons.lang3.StringUtils

object JSONUtils {

  def toJsonObject(str: String): JsonObject = {
    new Gson().fromJson(str, classOf[JsonObject])
      .getAsJsonObject
  }

  def toJsonArray(str: String): JsonArray = {
    new Gson().fromJson(str, classOf[JsonElement])
      .getAsJsonArray
  }

  def toJson(obj: Object): String = {
    new Gson().toJson(obj)
  }

  def isValueNotEmpty(elelment: JsonElement, key: String): Boolean = {
    if (elelment.isJsonNull) {
      return false
    } else if (elelment.isJsonPrimitive
      && elelment.getAsJsonPrimitive.isString) {
      return StringUtils.isNotBlank(elelment.getAsJsonPrimitive.getAsString)
    } else if (elelment.isJsonObject
      && key != null) {
      return elelment.getAsJsonObject.has(key) &&
        isValueNotEmpty(elelment.getAsJsonObject.get(key), null)
    } else if (elelment.isJsonArray) {
      return elelment.getAsJsonArray
        .iterator()
        .hasNext
    }
    true
  }
}
