package com.miotech.spark.etl.loader

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory


class HiveDataLoader(dataStore: DataStore, spark: SparkSession) extends DataLoader {

  private val logger = LoggerFactory.getLogger(getClass)

  /**
    * execute a script in datasource
    *
    * @param script
    */
  override def execute(script: String): Unit = spark.sql(script)

  override def read(dataSet: DataSet, config: Map[String, String]): DataFrame = {
    logger.info("read table from {}", dataSet.name)
    spark.read.table(dataSet.name)
  }

  override def write(dataFrame: DataFrame, dataSet: DataSet, config: Map[String, String]): Unit = {
    val writeMode = config.getOrElse("writeMode", "append")
    logger.info(s"write table to ${dataSet.name} in ${writeMode}")

    dataFrame
      .write
      .mode(writeMode)
      .saveAsTable(dataSet.name)
  }
}
