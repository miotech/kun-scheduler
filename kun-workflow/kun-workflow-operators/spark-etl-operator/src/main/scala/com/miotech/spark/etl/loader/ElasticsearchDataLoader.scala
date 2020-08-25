package com.miotech.spark.etl.loader

import com.miotech.spark.etl.common.ElasticsearchClient
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.spark.sql.EsSparkSQL
import org.slf4j.LoggerFactory


class ElasticsearchDataLoader(dataStore: DataStore, spark: SparkSession) extends DataLoader {

  private val logger = LoggerFactory.getLogger(getClass)

  val esClient = new ElasticsearchClient(
    dataStore.dataStoreUrl,
    dataStore.dataStoreUsername,
    dataStore.dataStorePassword)

  /**
    * execute es script
    * @param script: script should in the format like
    * POST  index-name/_doc/xxx
    * {
    *   "a": "b"
    * }
    */
  override def execute(script: String): Unit = {
      // first line should be request path
    import java.util.regex.Pattern
    val pattern = Pattern.compile("(?im)(PUT|DELETE|POST)\\s+.*", Pattern.MULTILINE)
    val matcher = pattern.matcher(script)
    if (!matcher.find()) {
      throw new IllegalArgumentException("Invalid script, should provide a RESTFUL request path: POST/DELETE/PUT path")
    }
    val splits = script.split("\n|\t")
      .filter(!_.isEmpty)
    val url = splits.head
    val esScript = script.replace(url, "")
    logger.debug(s"execute at endpoint: `${url}` \n es script\n: ${script}")
    esClient.executeAndWait(url, esScript)
  }

  override def read(dataSet: DataSet, config: Map[String, String]): DataFrame = {
    val resourceName = esClient.getResource(dataSet.name)
    logger.debug("read table from index {}", resourceName)
    val readConfig = getEsConfig(dataStore) ++ config
    EsSparkSQL.esDF(spark, resourceName, readConfig)
  }

  override def write(dataFrame: DataFrame, dataSet: DataSet, config: Map[String, String]): Unit = {
    val writeMode = config.getOrElse("writeMode", "insert")
    val idField = config.get("idField")

    val resourceName = esClient.getResource(dataSet.name)
    logger.debug(s"write table to ${resourceName} in ${writeMode}")
    val baseConfig = getEsConfig(dataStore)
    if (Seq("upsert", "update").contains(writeMode) && idField.isEmpty) {
      throw new IllegalArgumentException("idField should not be null when write in mode: " + writeMode)
    }
    val writeConfig = idField.fold(baseConfig) { c => baseConfig ++ Map(
      "es.mapping.id" -> c,
      "es.write.operation" -> writeMode
    )} ++ config

    // for compatibility
    EsSparkSQL.saveToEs(dataFrame, resourceName, writeConfig)
  }

  def getEsConfig(dataStore: DataStore) = {
    Map(
      "es.index.auto.create" -> "true",
      "es.net.http.auth.user" -> dataStore.dataStoreUsername,
      "es.net.http.auth.pass" -> dataStore.dataStorePassword,
      "es.nodes" -> dataStore.dataStoreUrl,
      "spark.serializer" -> "org.apache.spark.serializer.KryoSerializer"
    )
  }
}
