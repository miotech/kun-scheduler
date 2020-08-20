package com.miotech.spark.etl.operator

import java.util.Base64

import com.google.gson.JsonObject
import com.miotech.spark.etl.Config
import com.miotech.spark.etl.utils.JSONUtils
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class OperatorTestBase extends AnyFunSuite with BeforeAndAfterEach {

  var spark : SparkSession = _
  val config = Config()

  def configure(): Map[String, Object] = {
    Map.empty[String, Object]
  }

  override def beforeEach(): Unit = {
    val sparkConf = new SparkConf()
    val config = new JsonObject()
    configure().foreach(x => config.addProperty(x._1, x._2.toString))
    val encodeString = Base64.getEncoder.encodeToString(config.toString.getBytes())
    System.setProperty("kun.dataplatform", encodeString)

    spark = SparkSession.builder()
      .appName("test operator")
      .master("local")
      .config(sparkConf)
//      .enableHiveSupport()
      .getOrCreate()
  }
}
