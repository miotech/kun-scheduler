package com.miotech.spark.etl

import com.miotech.spark.etl.operator.Operator
import org.apache.commons.lang3.StringUtils
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

case class Config(appName:String = null,
                  operatorClass: String = null,
                  taskId: String = null,
                  taskConfig: String = null,
                  sparkConf: Map[String, String] = Map.empty)

object Application {
  var sparkSession: SparkSession = _

  private val logger = LoggerFactory.getLogger(getClass)
  private val parser = new scopt.OptionParser[Config]("spark-etl-operator") {
    head("spark-etl-operator", "1.0-SNAPSHOT")
    opt[String]('c', "operatorClass") action((param, config) =>
      config.copy(operatorClass = param)) text "Operator Class Name"
    opt[String]('i', "taskId") action((param, config) =>
      config.copy(taskId = param)) text "task id from task config center"
    opt[String]('f', "taskConfig") action((param, config) =>
      config.copy(taskConfig = param)) text "task config file location, support hdfs and s3 and local"
    opt[Map[String,String]]("sparkConf") action((param, config) =>
      config.copy(sparkConf = param)) text "extra user specified spark configurations"
  }

  def main(args: Array[String]): Unit = {

    logger.info(s"Start application with parameters: ${args}")
    parser.parse(args, Config()) foreach { config =>
      initSparkSession(config)
      if (StringUtils.isNotBlank(config.operatorClass)) {
        dispatchOperator(config)
      } else {
        throw new IllegalArgumentException(
          """
            |Application do not provided valid operatorClass name,
            | did not know how to handle it
            |""".stripMargin)
      }
    }
  }

  def dispatchOperator(config: Config): Unit = {
    val className: String = config.operatorClass
    logger.info(s"Invoke Operator : ${className} with config: ${config}")
    try {
      val clz = Class.forName(className)
      val operator = clz
        .getConstructor(classOf[Config], classOf[SparkSession])
        .newInstance(config, sparkSession)
        .asInstanceOf[Operator]
      operator.execute()
    } catch {
      case e: Throwable =>
        logger.error(s"Failed to execute operator: ${className}")
        throw e
    }
  }

  def initSparkSession(config: Config, spark: SparkSession = null): SparkSession = {
    if (sparkSession == null) {
      if (spark == null) {

        val sparkConf = new SparkConf()
        sparkConf.set("spark.sql.orc.impl", "native")
        sparkConf.set("spark.sql.parquet.writeLegacyFormat", "true")
        sparkConf.set("spark.sql.legacy.allowCreatingManagedTableUsingNonemptyLocation", "true")
        sparkConf.set("spark.sql.broadcastTimeout","6000")
        config.sparkConf.foreach(x => sparkConf.set(x._1, x._2)) // load user-defined spark configuration
        logger.info("Create new spark session with config: {}", sparkConf.toDebugString)

        sparkSession = SparkSession
          .builder()
          .config(sparkConf)
          .enableHiveSupport()
          .getOrCreate()
      } else {
        logger.info("Reuse spark session from current context.")
        sparkSession = spark
      }
    }

    sparkSession
  }
}