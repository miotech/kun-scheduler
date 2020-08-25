package com.miotech.spark.etl.operator

import com.google.gson.JsonObject
import com.miotech.spark.etl.Config
import com.miotech.spark.etl.common.DatasourceClient
import com.miotech.spark.etl.common.SparkDataUtils.implicits._
import com.miotech.spark.etl.loader.{DataLoader, DataSet, DataStore, ElasticsearchDataLoader, GenericHDFSDataLoader, HiveDataLoader, JdbcDataLoader, MongoDataLoader}
import com.miotech.spark.etl.utils.JSONUtils
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

import scala.collection.mutable

class DataSyncOperator(config: Config, spark: SparkSession)
  extends Operator(config, spark) {
  import scala.collection.JavaConverters._

  private val logger = LoggerFactory.getLogger(getClass)
  private var datasourceClient: DatasourceClient = _

  def setDatasourceClient(client: DatasourceClient): Unit = {
    datasourceClient = client
  }

  /**
    * run operator task
    *
    * @param taskPayload
    */
  override def run(taskPayload: JsonObject): Boolean = {
    if (datasourceClient == null) {
      val datastoreHost = getOrNone("dataStoreHost").getOrElse("")
      datasourceClient = new DatasourceClient(datastoreHost)
    }

    val sourceDataSource = get("sourceDataSource")
    val sourceDataSetName = get("sourceDatasetName")
    val targetDataSource = get("targetDataSource")
    val targetDataSetName = get("targetDatasetName")
    val writeMode = get("writeMode")
    val preScript = getOrNone("preScript")
    val postScript = getOrNone("postScript")
    val globalIdFields = getAsArray("globalIdFields")
      .map(_.getAsString)
      .toList
    val idField = getOrNone("idField")

    // read
    val sourceDataStore = datasourceClient.getDataStore(sourceDataSource)
    val targetDataStore = datasourceClient.getDataStore(targetDataSource)
    val sourceDataset: DataSet = DataSet(sourceDataStore, sourceDataSetName)
    val targetDataset: DataSet = DataSet(targetDataStore, targetDataSetName)

    logger.info(s"source: ${sourceDataset}")
    logger.info(s"target: ${targetDataset}")

    val sourceLoader: DataLoader = getLoader(sourceDataset.currentDatastore)
    val targetLoader: DataLoader = getLoader(targetDataset.currentDatastore)
    // execute prescript
    executeScript(preScript, targetLoader)
    // read
    var df = sourceLoader.read(sourceDataset, Map())

    // add globalid if configured
    if (globalIdFields.nonEmpty) {
      df = df.addGlobalId(globalIdFields, targetDataset.name)
    }

    // TODO: column mapping, type mapping
    // write
    val writeConfig =  mutable.Map(
      "writeMode" -> writeMode
    )
    idField.foreach(x => writeConfig.put("idField", x))
    targetLoader.write(df, targetDataset, writeConfig.toMap)

    // execute postscript
    executeScript(postScript, targetLoader)

    true
  }

  private def getLoader(dataStore: DataStore): DataLoader = {
    dataStore.dbType match {
      case "postgresql" | "mysql" | "sqlserver" =>
        new JdbcDataLoader(dataStore, spark)
      case "mongo" | "mongodb" =>
        new MongoDataLoader(dataStore, spark)
      case "hive" | "aws" =>
        new HiveDataLoader(dataStore, spark)
      case "hdfs" =>
        new GenericHDFSDataLoader(dataStore, spark)
      case "elasticsearch" =>
        new ElasticsearchDataLoader(dataStore, spark)
      case _ =>
        throw new IllegalArgumentException(s"Unsupported dbType ${dataStore.dbType}")
    }
  }

  private def executeScript(scriptOption: Option[String], dataLoader: DataLoader): Unit = {
    scriptOption.foreach { script =>
      if (StringUtils.isNotBlank(script)) {
        logger.debug("execute script \"{}\"", script)
        dataLoader.execute(script)
      }
    }
  }
}
