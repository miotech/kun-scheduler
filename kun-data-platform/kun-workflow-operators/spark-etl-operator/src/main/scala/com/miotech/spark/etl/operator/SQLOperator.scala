package com.miotech.spark.etl.operator
import com.google.gson.JsonObject
import com.miotech.spark.etl.Config
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

class SQLOperator(config: Config, spark: SparkSession)
  extends Operator(config, spark) {
  private val logger = LoggerFactory.getLogger(getClass)

  private val SCRIPT_NAME = "scriptName"
  private val TARGET_TABLE_NAME = "targetTableName"

  override def run(taskPayload: JsonObject) = {
    processTask(taskPayload)

    true
  }

  def processTask(taskPayload: JsonObject): Unit =  {
    val targetTableName = taskPayload.get(TARGET_TABLE_NAME)
      .getAsString
    val limitLines = if( taskPayload.has("limit")
      && taskPayload.get("limit").getAsInt>0)
      Option(taskPayload.get("limit").getAsInt)
    else None

    val writeMode = taskPayload.get("insertMode")
      .getAsString.toLowerCase

    val sql = taskPayload.get("sql")
      .getAsString.stripMargin

    val formatSQL = limitLines
      .fold(sql) { l => sql + s"SELECT * FROM (${sql}) LIMIT ${l}"}
    runSQL(formatSQL, writeMode, targetTableName)
  }

  def runSQL(sql: String, writeMode: String, targetTableName: String): Unit = {
//    Preconditions.checkArgument(StringUtils.isNotBlank(sql), "SQL should not empty")

    logger.info(s"Execute SQL: ${sql}")
    val result = spark.sql(sql)

    if (result.columns.nonEmpty) {
      val splits = targetTableName.split("\\.").toSeq
      val schemaAndTable = if (splits.size == 1) Seq("dm") ++ splits else splits
      val tableName = schemaAndTable.map(x => s"`${x}`").mkString(".")
      logger.info(s"Write to table: ${tableName} using ${writeMode}")
      result
        .write
        .mode(writeMode)
        .saveAsTable(tableName)
    }
  }
}
