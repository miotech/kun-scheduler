package com.spark.sparkapp

import org.apache.spark.sql.{SaveMode, SparkSession}

object MongoExample {
  def main(args: Array[String]) = {
    val spark: SparkSession = {
      SparkSession.builder()
        .appName("testMongo")
        .master("local[*]")
        .getOrCreate();
    }
    val df = spark.read.format("mongo")
      .option("uri", "mongodb://admin:123456@127.0.0.1:27017")
      .option("database", "sparkTest")
      .option("collection", "spline")
      .load()

    df.write.mode(SaveMode.Overwrite).format("mongo")
      .option("uri", "mongodb://admin:123456@127.0.0.1:27017")
      .option("database", "sparkTest")
      .option("collection", "test")
      .mode("overwrite")
      .save()
  }
}
