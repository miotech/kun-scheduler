package com.miotech.spark.etl.operator

import java.util.Base64

import com.google.gson.{JsonObject, JsonParser, JsonPrimitive}
import com.miotech.spark.etl.Config
import com.miotech.spark.etl.utils.JSONUtils
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object Operator {
  val DATA_PLATFORM_PREFIX = "kun.dataplatform"
  val DATA_PLATFORM_DATASOURCE_PREFIX = "kun.dataplatform.datasource."
  val DATA_CONF_PREFIX_NAME = "job.config.prefix"
}

abstract class Operator(config: Config,
                        spark: SparkSession) {
  import Operator._
  import scala.collection.JavaConverters._

  private val logger = LoggerFactory.getLogger(getClass)

  private var taskPayload: JsonObject = _

  def execute(): Boolean = {
    taskPayload = getTaskPayload(config)
    logger.info(s"Using Task Payload: ${taskPayload}")
    run(taskPayload)
  }

  /**
    * run operator task
    * @param taskPayload
    */
  def run(taskPayload: JsonObject): Boolean

  protected def getTaskPayload(config: Config): JsonObject = {
    if (!StringUtils.isEmpty(config.taskConfig)){
      logger.info(s"Reading operator config from ${config.taskConfig}")
      val decodedBytes = Base64.getDecoder.decode(config.taskConfig)
      JSONUtils.toJsonObject(new String(decodedBytes))
        .getAsJsonObject
    } else {
      logger.info("Reading operator config from spark configuration, prefix: {}", DATA_PLATFORM_PREFIX)
      getDataPlatformConfig()
    }
  }

  def getDataPlatformConfig(): JsonObject = {
//    Preconditions.checkArgument(spark != null, "spark session should not be null")
    val sparkConf =  System.getProperties.asScala
    val encodedString = sparkConf(DATA_PLATFORM_PREFIX)
    val decodedBytes = Base64.getDecoder.decode(encodedString)
    JSONUtils.toJsonObject(new String(decodedBytes))
  }

  def getOrNone(key: String): Option[String] = getValue(key, None)

  def get(key: String): String = getValue(key, None)
    .getOrElse {
      throw new IllegalArgumentException(s"config `$key` cannot be resolved")
    }

  def getAsArray(key: String) = {
    if (taskPayload.has(key)) {
      taskPayload.get(key)
        .getAsJsonArray
        .asScala
        .toSeq
    } else Seq()
  }

  private def getValue(key: String, defaultValue: Option[String]): Option[String] = {
    if (taskPayload.has(key)) {
      Some(taskPayload.get(key).getAsString)
    } else {
      defaultValue
    }
  }
}
