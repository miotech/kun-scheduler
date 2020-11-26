package com.spark.sparkapp

import org.apache.spark.sql.{SaveMode, SparkSession}

object HiveExample1{
  def main(args: Array[String]) = {
    val spark: SparkSession = {
      SparkSession.builder()
        .appName("testHive1")
        .master("local[*]")
        .getOrCreate();
    }

    import spark.sql

    sql("create database if not exists sparktest")
    sql("use sparktest")
    sql("CREATE TABLE IF NOT EXISTS src (key INT, value STRING) USING hive")
    sql("LOAD DATA LOCAL INPATH 'examples/src/main/resources/kv1.txt' INTO TABLE src")

    sql("SELECT * FROM src").show()


    val sqlDF = sql("SELECT key, value FROM src WHERE key < 10 ORDER BY key")
=    sqlDF.write.format("hive").mode(SaveMode.Overwrite).saveAsTable("hive_test")
  }
}
