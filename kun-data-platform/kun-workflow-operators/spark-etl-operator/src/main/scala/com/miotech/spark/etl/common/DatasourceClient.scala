package com.miotech.spark.etl.common

import java.util.Base64

import com.google.gson.JsonObject
import com.miotech.spark.etl.loader.DataStore
import com.miotech.spark.etl.operator.Operator.DATA_PLATFORM_DATASOURCE_PREFIX
import com.miotech.spark.etl.utils.{HttpUtils, JSONUtils}
import org.slf4j.LoggerFactory

class DatasourceClient(baseHost: String) {
  import scala.collection.JavaConverters._
  private val logger = LoggerFactory.getLogger(getClass)
  private val typeMap = getDBTypes()

  def parseDatastore(db: JsonObject, typeMap: Map[String, String]): DataStore = {
    val dbType = typeMap(db.get("typeId").getAsString).toLowerCase
    val information = db.get("information").getAsJsonObject

    val userAndPass = dbType match {
      case "aws" =>
        (information.get("athenaUsername").getAsString, information.get("athenaPassword").getAsString)
      case _ =>
        (information.get("username").getAsString, information.get("password").getAsString)
    }
    // jdbc
    val dbUrl = dbType match {
      case "postgresql" | "mysql" | "sqlserver" =>
        val addr = hostAndPort(information)
        s"jdbc:${dbType}://${addr._1}:${addr._2}/"
      case "mongodb" =>
        val addr = hostAndPort(information)
        s"mongodb://${userAndPass._1}:${userAndPass._2}@${addr._1}:${addr._2}/"
      case "aws" =>
        information.get("athenaUrl").getAsString
      case _ =>
        val addr = hostAndPort(information)
        s"${addr._1}:${addr._2}"
    }

    DataStore(
      dbType,
      dbUrl,
      userAndPass._1,
      userAndPass._2
    )
  }

  def hostAndPort(information: JsonObject) = {
    val host = information.get("host").getAsString
    val port = information.get("port").getAsInt
    (host, port)
  }

  def getDBTypes() = {
    val url = baseHost + "/api/v1/metadata/database/types"
//    val response = HttpUtils.get(url, Map())
    val response = "{\"code\":0,\"note\":\"Operation Successful\",\"result\":[{\"id\":\"4\",\"fields\":[{\"id\":null,\"typeId\":null,\"format\":\"NUMBER_INPUT\",\"require\":true,\"key\":\"port\",\"order\":2},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":true,\"key\":\"host\",\"order\":1},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":false,\"key\":\"username\",\"order\":3},{\"id\":null,\"typeId\":null,\"format\":\"PASSWORD\",\"require\":false,\"key\":\"password\",\"order\":4}],\"type\":\"Elasticsearch\"},{\"id\":\"3\",\"fields\":[{\"id\":null,\"typeId\":null,\"format\":\"NUMBER_INPUT\",\"require\":true,\"key\":\"port\",\"order\":2},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":true,\"key\":\"host\",\"order\":1},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":false,\"key\":\"username\",\"order\":3},{\"id\":null,\"typeId\":null,\"format\":\"PASSWORD\",\"require\":false,\"key\":\"password\",\"order\":4}],\"type\":\"PostgreSQL\"},{\"id\":\"5\",\"fields\":[{\"id\":null,\"typeId\":null,\"format\":\"NUMBER_INPUT\",\"require\":true,\"key\":\"port\",\"order\":2},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":true,\"key\":\"host\",\"order\":1},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":false,\"key\":\"username\",\"order\":3},{\"id\":null,\"typeId\":null,\"format\":\"PASSWORD\",\"require\":false,\"key\":\"password\",\"order\":4}],\"type\":\"Arango\"},{\"id\":\"2\",\"fields\":[{\"id\":null,\"typeId\":null,\"format\":\"NUMBER_INPUT\",\"require\":true,\"key\":\"port\",\"order\":2},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":true,\"key\":\"host\",\"order\":1},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":false,\"key\":\"username\",\"order\":3},{\"id\":null,\"typeId\":null,\"format\":\"PASSWORD\",\"require\":false,\"key\":\"password\",\"order\":4}],\"type\":\"MongoDB\"},{\"id\":\"1\",\"fields\":[{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":false,\"key\":\"glueRegion\",\"order\":3},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":false,\"key\":\"athenaUsername\",\"order\":5},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":false,\"key\":\"glueSecretKey\",\"order\":2},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":false,\"key\":\"glueAccessKey\",\"order\":1},{\"id\":null,\"typeId\":null,\"format\":\"INPUT\",\"require\":false,\"key\":\"athenaUrl\",\"order\":4},{\"id\":null,\"typeId\":null,\"format\":\"PASSWORD\",\"require\":false,\"key\":\"athenaPassword\",\"order\":6}],\"type\":\"AWS\"}]}"
      JSONUtils.toJsonObject(response)
      .get("result")
      .getAsJsonArray
      .asScala
      .map( x => {
        val obj = x.getAsJsonObject
        obj.get("id").getAsString -> obj.get("type").getAsString
      })
      .toMap
  }

  def getDataStore(storeId: String): DataStore = {
    val sparkConf =  System.getProperties.asScala
    val encodedString = sparkConf(DATA_PLATFORM_DATASOURCE_PREFIX + storeId)
    val decodedBytes = Base64.getDecoder.decode(encodedString)
    val store = JSONUtils.toJsonObject(new String(decodedBytes))
    parseDatastore(store, typeMap)
  }
}
