package com.miotech.spark.etl.loader

import com.miotech.spark.etl.common.JdbcClient
import com.miotech.spark.etl.common.SparkDataUtils.implicits._
import org.apache.spark.sql.functions.{col, regexp_replace}
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.SaveMode._
import org.slf4j.LoggerFactory

import scala.collection.mutable

class JdbcDataLoader(dataStore: DataStore, spark: SparkSession) extends DataLoader {

  private val logger = LoggerFactory.getLogger(getClass)

  private val jdbcClient = JdbcClient(
    dataStore.dbType,
    dataStore.dataStoreUrl,
    dataStore.dataStoreUsername,
    dataStore.dataStorePassword
  )
  override def read(dataSet: DataSet, config: Map[String, String]): DataFrame = {
    val readOptions = getJdbcOptions(dataSet)
    // TODO: Read By Partition
    spark
      .read
      .format("jdbc")
      .options(readOptions)
      .load()
  }

  override def write(dataFrame: DataFrame,
                     dataSet: DataSet,
                     config: Map[String, String]): Unit = {
    val writeTableName = dataSet.datasetName
    logger.info(s"Writing table ${writeTableName} with config: ${config}")
    // Write tmp table
    val swapTableName = Seq(
      writeTableName,
      System.currentTimeMillis().toString,
      scala.util.Random.alphanumeric.take(10).mkString
    ).mkString("_")

    val writeOptions = getJdbcOptions(dataSet) ++ Map(
      "dbtable" -> swapTableName
    )
    logger.info(s"Creating intermediate table ${swapTableName} for target table ${writeTableName} ...")
    logger.info(s"Writing options ${writeOptions}")

    val writeMode = config.get("writeMode")
      .map {
        case "overwrite" =>  Overwrite
        case "append" => Append
        case "ignore" => Ignore
        case "errorifexists" => ErrorIfExists
        case _ => throw new IllegalArgumentException("Not supported writeMode")
      }
      .getOrElse(SaveMode.Append)

    // for postgresql, has to remove unicode
    val writeDF = if (dataStore.dbType.equals("postgresql"))
      removeUnicode(dataFrame)
    else dataFrame

    // TODO: add config whether use lowercase
    writeDF
      .lowerCaseColumns()
      .write
      .format("jdbc")
      .mode(writeMode)
      .options(writeOptions)
      .save()

    // Swap tmp table and target table
    execute(s"CREATE TABLE IF NOT EXISTS ${writeTableName} (LIKE ${swapTableName} INCLUDING ALL)")
    try {
      writeMode match {
        case Overwrite =>
          logger.info(s"Swapping table ${swapTableName} with ${writeTableName}")
          execute(
            s"""
               |ALTER TABLE ${writeTableName} RENAME TO ${swapTableName}_swp;
               |ALTER TABLE ${swapTableName} RENAME TO ${writeTableName};
               |DROP TABLE IF EXISTS ${swapTableName}_swp;
        """.stripMargin
          )
        case Append =>
          execute(
            s"""
               |INSERT INTO ${writeTableName}
               |SELECT *
               |FROM ${swapTableName};
               |DROP TABLE ${swapTableName};
               |""".stripMargin
          )
        case _ =>
          throw new IllegalArgumentException("Not supported writeMode")
      }
    } finally {
      execute(s"DROP TABLE IF EXISTS ${swapTableName};")
      execute(s"DROP TABLE IF EXISTS ${swapTableName}_swp;")
    }
  }

  override def execute(script: String): Unit = jdbcClient.execute(script)

  private def lowerCaseColumns(df: DataFrame) = {
    df.select(df.columns.map(c => col(c).alias(c.toLowerCase)): _*)
  }

  private def removeUnicode(df: DataFrame): DataFrame = {
    val selectedCols = df.schema
      .map { t => t.dataType match {
        case StringType => regexp_replace(col(t.name),
          "\\u0000", "")
          .alias(t.name)
        case _ => col(t.name)
      }}

    df.select(selectedCols: _*)
  }

  private def getJdbcOptions(dataSet: DataSet) = {
    mutable.Map (
      "driver" -> jdbcClient.driver,
      "url" -> dataSet.currentDatastore.dataStoreUrl,
      "dbtable" -> dataSet.datasetName,
      "user" -> dataSet.currentDatastore.dataStoreUsername,
      "password" -> dataSet.currentDatastore.dataStorePassword
    )
  }
}
