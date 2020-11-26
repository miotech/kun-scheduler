package com.spark.sparkapp

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}

object EsExample {
  def main(args: Array[String]) = {
    val spark: SparkSession = {
      SparkSession.builder()
        .appName("testEs")
        .master("local[*]")
        .getOrCreate();
    }


    val index = "test_es_spline"
    val docType = "blog"
    val esOptions = Map(
      "es.nodes" -> "localhost",
      "es.nodes.wan.only" -> "true",
      "es.index.auto.create" -> "true"
    )

    val testData: DataFrame = {
      val schema = StructType(StructField("id", IntegerType, nullable = false) :: StructField("name", StringType, nullable = false) :: Nil)
      val rdd = spark.sparkContext.parallelize(Row(999, "spline") :: Row(998, "spark") :: Nil)
      spark.sqlContext.createDataFrame(rdd, schema)
    }


    testData
      .write
      .mode(SaveMode.Append)
      .options(esOptions)
      .format("es")
      .save(s"$index/$docType")
  }


}
