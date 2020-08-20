package com.miotech.spark.etl.loader

import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.funsuite.AnyFunSuite

class LoaderTestBase extends AnyFunSuite with BeforeAndAfterEach {

  var spark : SparkSession = _

  override def beforeEach(): Unit = {
    val sparkConf = new SparkConf()
    sparkConf.set("spark.sql.orc.impl", "native")
    spark = SparkSession.builder()
      .appName("test loader")
      .master("local")
      .config(sparkConf)
//      .enableHiveSupport()
      .getOrCreate()
  }

  protected def getSampleData(size: Int = 100) = {
    val data = ( 1 to size)
      .map( i => Row(i, "value - " + i))

    val rdd = spark.sparkContext.parallelize(data)
    val schema = StructType(Seq(
      StructField("id", IntegerType),
      StructField("value", StringType)
    ))
    spark.createDataFrame(rdd, schema)
  }
}
