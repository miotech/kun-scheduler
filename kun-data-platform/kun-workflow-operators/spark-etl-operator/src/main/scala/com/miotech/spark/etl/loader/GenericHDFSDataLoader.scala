package com.miotech.spark.etl.loader

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory


class GenericHDFSDataLoader(dataStore: DataStore, spark: SparkSession) extends DataLoader {

  private val logger = LoggerFactory.getLogger(getClass)

  /**
    * execute a script in datasource
    *
    * @param script
    */
  override def execute(script: String): Unit = spark.sql(script)

  override def read(dataSet: DataSet, config: Map[String, String]): DataFrame = {
    val format = config.getOrElse("format", "parquet")
    spark.read
      .format(format)
      .load(dataSet.datasetName)
  }

  override def write(dataFrame: DataFrame, dataSet: DataSet, config: Map[String, String]): Unit = {
    val writeMode = config.getOrElse("writeMode", "append")
    val format = config.getOrElse("format", "parquet")
    dataFrame
      .write
      .format(format)
      .mode(writeMode)
      .save(dataSet.datasetName)
  }
}
