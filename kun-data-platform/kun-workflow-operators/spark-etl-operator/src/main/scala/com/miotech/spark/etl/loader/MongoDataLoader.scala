package com.miotech.spark.etl.loader

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.{ReadConfig, WriteConfig}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory


class MongoDataLoader(dataStore: DataStore, spark: SparkSession) extends DataLoader {

  private val logger = LoggerFactory.getLogger(getClass)

  /**
    * execute a script in mongo, currently not supported yet
    *
    * @param script
    */
  override def execute(script: String): Unit =
    throw new UnsupportedOperationException()

  override def read(dataSet: DataSet, config: Map[String, String]): DataFrame = {
    logger.debug("read table from {}", dataSet.datasetName)

    val dataReadConfig = ReadConfig(
      databaseName = dataSet.currentDatabase.get,
      collectionName = dataSet.datasetName,
      connectionString = Option(dataSet.dataStore.dataStoreUrl)
    )

    val dropOid = config.getOrElse("dropOid", "true").equals("true")
    var df = MongoSpark.load(spark, dataReadConfig)

    // mark _id.oid as _id, if `dropOid` enabled
    if (dropOid) {
      df = df.withColumn("_id", col("_id.oid"))
    }

    df
  }

  override def write(dataFrame: DataFrame, dataSet: DataSet, config: Map[String, String]): Unit = {
    val writeMode = config.getOrElse("writeMode", "append")
    logger.debug(s"write table to ${dataSet.datasetName} in ${writeMode}")

    val dataWriteConfig = WriteConfig(
      databaseName = dataSet.currentDatabase.get,
      collectionName = dataSet.datasetName,
      connectionString = Option(dataSet.dataStore.dataStoreUrl)
    )

    MongoSpark.save(dataFrame, dataWriteConfig)
  }
}
